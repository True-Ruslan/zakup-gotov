#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import pathlib
import re
import sys
from collections.abc import Sequence


CHECKSUM_LINE = re.compile(r"^([0-9a-f]{64}) ([ *])(.+)$")


def _sha256(path: pathlib.Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _validate_asset_name(asset: str) -> None:
    if not asset or pathlib.PurePosixPath(asset).name != asset or asset in {".", ".."}:
        raise ValueError(f"asset must be a basename: {asset!r}")


def verify_assets(
    checksum_file: pathlib.Path,
    download_directory: pathlib.Path,
    assets: Sequence[str],
) -> None:
    lines = checksum_file.read_text(encoding="utf-8").splitlines()

    parsed: list[tuple[str, str]] = []
    malformed: list[str] = []
    for line in lines:
        if not line:
            continue
        match = CHECKSUM_LINE.fullmatch(line)
        if match is None:
            malformed.append(line)
            continue
        digest, _mode, filename = match.groups()
        parsed.append((digest, filename))

    for asset in assets:
        _validate_asset_name(asset)
        release_path = f"dist/{asset}"

        if any(line.endswith(release_path) for line in malformed):
            raise ValueError(f"malformed checksum entry for {release_path}")

        matches = [digest for digest, filename in parsed if filename == release_path]
        if len(matches) != 1:
            raise ValueError(
                f"expected exactly one checksum entry for {release_path}, found {len(matches)}"
            )

        downloaded = download_directory / asset
        if not downloaded.is_file():
            raise ValueError(f"downloaded release asset is missing: {asset}")

        actual = _sha256(downloaded)
        expected = matches[0]
        if actual != expected:
            raise ValueError(
                f"checksum mismatch for {asset}: expected {expected}, got {actual}"
            )


def _parse_args(argv: Sequence[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Verify selected downloaded GitHub Release assets against dist/ paths in SHA256SUMS."
    )
    parser.add_argument("checksums", type=pathlib.Path)
    parser.add_argument("download_directory", type=pathlib.Path)
    parser.add_argument("assets", nargs="+")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = _parse_args(sys.argv[1:] if argv is None else argv)
    try:
        verify_assets(args.checksums, args.download_directory, args.assets)
    except (OSError, UnicodeError, ValueError) as exc:
        print(f"release asset checksum verification failed: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
