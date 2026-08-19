#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from typing import Any


DIGEST_RE = re.compile(r"^sha256:[0-9a-f]{64}$")
PLATFORM_RE = re.compile(
    r"^(?P<os>[a-z0-9][a-z0-9._-]*)/"
    r"(?P<arch>[a-z0-9][a-z0-9._-]*)"
    r"(?:/(?P<variant>[a-z0-9][a-z0-9._-]*))?$"
)
INDEX_MEDIA_TYPES = {
    "application/vnd.oci.image.index.v1+json",
    "application/vnd.docker.distribution.manifest.list.v2+json",
}


class ResolutionError(ValueError):
    pass


def parse_platform(value: str) -> tuple[str, str, str | None]:
    match = PLATFORM_RE.fullmatch(value)
    if match is None:
        raise ResolutionError(
            "platform must be OS/ARCH or OS/ARCH/VARIANT using lowercase OCI tokens"
        )
    return match.group("os"), match.group("arch"), match.group("variant")


def resolve_platform_digest(index: Any, platform: str) -> str:
    expected_os, expected_arch, expected_variant = parse_platform(platform)

    if not isinstance(index, dict):
        raise ResolutionError("registry descriptor must be a JSON object")
    if index.get("schemaVersion") != 2:
        raise ResolutionError("registry descriptor must use schemaVersion 2")
    if index.get("mediaType") not in INDEX_MEDIA_TYPES:
        raise ResolutionError("registry descriptor is not an OCI index or Docker manifest list")

    manifests = index.get("manifests")
    if not isinstance(manifests, list) or not manifests:
        raise ResolutionError("registry index must contain at least one manifest descriptor")

    matches: list[dict[str, Any]] = []
    for descriptor in manifests:
        if not isinstance(descriptor, dict):
            raise ResolutionError("registry index contains a non-object manifest descriptor")

        descriptor_platform = descriptor.get("platform")
        if not isinstance(descriptor_platform, dict):
            continue

        descriptor_os = descriptor_platform.get("os")
        descriptor_arch = descriptor_platform.get("architecture")
        descriptor_variant = descriptor_platform.get("variant")
        if descriptor_os != expected_os or descriptor_arch != expected_arch:
            continue
        if expected_variant is not None and descriptor_variant != expected_variant:
            continue

        matches.append(descriptor)

    if len(matches) != 1:
        raise ResolutionError(
            f"expected exactly one manifest for {platform}; found {len(matches)}"
        )

    digest = matches[0].get("digest")
    if not isinstance(digest, str) or DIGEST_RE.fullmatch(digest) is None:
        raise ResolutionError(f"manifest for {platform} has an invalid sha256 digest")
    return digest


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: resolve_oci_platform.py OS/ARCH[/VARIANT]", file=sys.stderr)
        return 64

    try:
        index = json.load(sys.stdin)
        digest = resolve_platform_digest(index, sys.argv[1])
    except (json.JSONDecodeError, ResolutionError) as exc:
        print(f"OCI platform resolution failed: {exc}", file=sys.stderr)
        return 1

    print(digest)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
