#!/usr/bin/env python3
from __future__ import annotations

import argparse
import dataclasses
import pathlib
import re
from typing import TextIO


SEMVER_TAG_RE = re.compile(
    r"^v(0|[1-9][0-9]*)\."
    r"(0|[1-9][0-9]*)\."
    r"(0|[1-9][0-9]*)"
    r"(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$"
)
COMMIT_SHA_RE = re.compile(r"^[0-9a-f]{40}$")
GHCR_DIGEST_REF_RE = re.compile(
    r"^ghcr\.io/"
    r"[a-z0-9][a-z0-9._-]*"
    r"(?:/[a-z0-9][a-z0-9._-]*)+"
    r"@sha256:[0-9a-f]{64}$"
)
GHCR_SLUG_RE = re.compile(r"^[a-z0-9][a-z0-9._-]*$")

API_IMAGE_TOKEN = "image: ${API_IMAGE:?API_IMAGE must be set}"
WEB_IMAGE_TOKEN = "image: ${WEB_IMAGE:?WEB_IMAGE must be set}"


@dataclasses.dataclass(frozen=True)
class ReleaseMetadata:
    version: str
    stable: bool
    version_tag: str
    candidate_tag: str
    publish_latest: bool


def _validate_prerelease_identifiers(prerelease: str | None) -> None:
    if prerelease is None:
        return

    for identifier in prerelease.split("."):
        if identifier.isdigit() and len(identifier) > 1 and identifier.startswith("0"):
            raise ValueError(
                "numeric SemVer prerelease identifiers must not contain leading zeroes"
            )


def parse_release(tag: str, *, prerelease: bool, commit_sha: str) -> ReleaseMetadata:
    match = SEMVER_TAG_RE.fullmatch(tag)
    if match is None:
        raise ValueError(
            "release tag must match vMAJOR.MINOR.PATCH or "
            "vMAJOR.MINOR.PATCH-prerelease without build metadata"
        )

    prerelease_text = match.group(4)
    _validate_prerelease_identifiers(prerelease_text)

    tag_is_prerelease = prerelease_text is not None
    if prerelease != tag_is_prerelease:
        raise ValueError("GitHub prerelease flag must match the SemVer prerelease state")

    if COMMIT_SHA_RE.fullmatch(commit_sha) is None:
        raise ValueError("commit SHA must be a full lowercase 40-character SHA-1")

    version = tag.removeprefix("v")
    stable = not tag_is_prerelease
    return ReleaseMetadata(
        version=version,
        stable=stable,
        version_tag=version,
        candidate_tag=f"candidate-{commit_sha}",
        publish_latest=stable,
    )


def _build_image_prefix(owner: str, repository: str) -> str:
    owner_slug = owner.strip().lower()
    repository_slug = repository.strip().lower()

    if GHCR_SLUG_RE.fullmatch(owner_slug) is None:
        raise ValueError("repository owner cannot be represented safely in a GHCR name")
    if GHCR_SLUG_RE.fullmatch(repository_slug) is None:
        raise ValueError("repository name cannot be represented safely in a GHCR name")

    return f"ghcr.io/{owner_slug}/{repository_slug}"


def build_image_names(owner: str, repository: str) -> tuple[str, str]:
    prefix = _build_image_prefix(owner, repository)
    return f"{prefix}-api", f"{prefix}-web"


def build_staging_image_names(owner: str, repository: str) -> tuple[str, str]:
    prefix = _build_image_prefix(owner, repository)
    return f"{prefix}-staging-api", f"{prefix}-staging-web"


def _validate_digest_ref(ref: str) -> None:
    if GHCR_DIGEST_REF_RE.fullmatch(ref) is None:
        raise ValueError(
            "release application image reference must be a lowercase GHCR "
            "reference pinned by sha256 digest"
        )


def render_release_compose(source: str, *, api_ref: str, web_ref: str) -> str:
    _validate_digest_ref(api_ref)
    _validate_digest_ref(web_ref)

    if source.count(API_IMAGE_TOKEN) != 1 or source.count(WEB_IMAGE_TOKEN) != 1:
        raise ValueError(
            "compose release source must contain exactly one API_IMAGE and one "
            "WEB_IMAGE token"
        )

    rendered = source.replace(API_IMAGE_TOKEN, f"image: {api_ref}")
    rendered = rendered.replace(WEB_IMAGE_TOKEN, f"image: {web_ref}")
    return rendered


def _parse_bool(value: str) -> bool:
    normalized = value.strip().lower()
    if normalized == "true":
        return True
    if normalized == "false":
        return False
    raise argparse.ArgumentTypeError("expected true or false")


def _append_github_output(path: pathlib.Path, values: dict[str, str]) -> None:
    with path.open("a", encoding="utf-8") as output:
        _write_key_values(output, values)


def _write_key_values(output: TextIO, values: dict[str, str]) -> None:
    for key, value in values.items():
        if "\n" in value or "\r" in value:
            raise ValueError(f"multi-line GitHub output is not supported for {key}")
        print(f"{key}={value}", file=output)


def _metadata_command(args: argparse.Namespace) -> None:
    metadata = parse_release(
        args.tag,
        prerelease=args.prerelease,
        commit_sha=args.commit_sha,
    )
    api_image, web_image = build_image_names(args.owner, args.repository)
    api_staging_image, web_staging_image = build_staging_image_names(
        args.owner, args.repository
    )

    values = {
        "version": metadata.version,
        "stable": str(metadata.stable).lower(),
        "version_tag": metadata.version_tag,
        "candidate_tag": metadata.candidate_tag,
        "publish_latest": str(metadata.publish_latest).lower(),
        "api_image": api_image,
        "web_image": web_image,
        "api_staging_image": api_staging_image,
        "web_staging_image": web_staging_image,
        "api_candidate": f"{api_staging_image}:{metadata.candidate_tag}",
        "web_candidate": f"{web_staging_image}:{metadata.candidate_tag}",
    }

    if args.github_output is not None:
        _append_github_output(args.github_output, values)
    else:
        _write_key_values(output=__import__("sys").stdout, values=values)


def _render_compose_command(args: argparse.Namespace) -> None:
    source = args.input.read_text(encoding="utf-8")
    rendered = render_release_compose(
        source,
        api_ref=args.api_ref,
        web_ref=args.web_ref,
    )
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(rendered, encoding="utf-8")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Zakup Gotov release contract helper")
    subparsers = parser.add_subparsers(dest="command", required=True)

    metadata = subparsers.add_parser("metadata", help="validate release metadata")
    metadata.add_argument("--tag", required=True)
    metadata.add_argument("--prerelease", required=True, type=_parse_bool)
    metadata.add_argument("--commit-sha", required=True)
    metadata.add_argument("--owner", required=True)
    metadata.add_argument("--repository", required=True)
    metadata.add_argument("--github-output", type=pathlib.Path)
    metadata.set_defaults(func=_metadata_command)

    render = subparsers.add_parser(
        "render-compose", help="render immutable release Compose asset"
    )
    render.add_argument("--input", required=True, type=pathlib.Path)
    render.add_argument("--output", required=True, type=pathlib.Path)
    render.add_argument("--api-ref", required=True)
    render.add_argument("--web-ref", required=True)
    render.set_defaults(func=_render_compose_command)

    return parser


def main() -> None:
    parser = build_parser()
    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
