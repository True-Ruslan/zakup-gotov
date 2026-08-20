#!/usr/bin/env python3
"""Fail-closed comparison for OCI multi-platform image indexes.

Mutable registry tags may be re-serialized by a registry and therefore acquire a
new top-level digest even when they still reference the exact same platform
manifests.  This verifier deliberately ignores only top-level annotations and
requires every other part of the index, including ordered child descriptors,
to remain identical.
"""

from __future__ import annotations

import copy
import json
import pathlib
import re
import sys
from typing import Any


OCI_INDEX = "application/vnd.oci.image.index.v1+json"
SHA256_RE = re.compile(r"^sha256:[0-9a-f]{64}$")


def fail(message: str) -> "NoReturn":
    raise SystemExit(message)


def load_json(path: pathlib.Path, label: str) -> dict[str, Any]:
    try:
        raw = path.read_text(encoding="utf-8")
    except OSError as exc:
        fail(f"{label}: cannot read {path}: {exc}")

    try:
        value = json.loads(raw)
    except json.JSONDecodeError as exc:
        fail(f"{label}: invalid JSON in {path}: {exc.msg}")

    if not isinstance(value, dict):
        fail(f"{label}: top-level OCI document must be an object")
    return value


def validate_string(value: Any, *, label: str) -> str:
    if not isinstance(value, str) or not value:
        fail(f"{label}: expected non-empty string")
    return value


def validate_platform(value: Any, *, label: str) -> None:
    if not isinstance(value, dict):
        fail(f"{label}: platform must be an object")

    for key, item in value.items():
        if key == "os.features":
            if not isinstance(item, list) or not all(
                isinstance(feature, str) and feature for feature in item
            ):
                fail(f"{label}.{key}: expected a list of non-empty strings")
            continue
        if not isinstance(item, str) or not item:
            fail(f"{label}.{key}: expected non-empty string")

    for required in ("os", "architecture"):
        validate_string(value.get(required), label=f"{label}.{required}")


def validate_descriptor(value: Any, *, label: str) -> None:
    if not isinstance(value, dict):
        fail(f"{label}: descriptor must be an object")

    validate_string(value.get("mediaType"), label=f"{label}.mediaType")
    digest = validate_string(value.get("digest"), label=f"{label}.digest")
    if not SHA256_RE.fullmatch(digest):
        fail(f"{label}.digest: expected lowercase sha256 digest")

    size = value.get("size")
    if isinstance(size, bool) or not isinstance(size, int) or size <= 0:
        fail(f"{label}.size: expected positive integer")

    if "platform" in value:
        validate_platform(value["platform"], label=f"{label}.platform")

    annotations = value.get("annotations")
    if annotations is not None:
        if not isinstance(annotations, dict) or not all(
            isinstance(key, str)
            and key
            and isinstance(item, str)
            for key, item in annotations.items()
        ):
            fail(f"{label}.annotations: expected string-to-string object")


def canonical(value: Any) -> str:
    return json.dumps(value, sort_keys=True, separators=(",", ":"), ensure_ascii=False)


def validate_index(value: dict[str, Any], *, label: str) -> None:
    if value.get("schemaVersion") != 2:
        fail(f"{label}: schemaVersion must be 2")
    if value.get("mediaType") != OCI_INDEX:
        fail(f"{label}: mediaType must be {OCI_INDEX}")

    manifests = value.get("manifests")
    if not isinstance(manifests, list) or not manifests:
        fail(f"{label}: manifests must be a non-empty array")

    seen: set[str] = set()
    for index, descriptor in enumerate(manifests):
        descriptor_label = f"{label}.manifests[{index}]"
        validate_descriptor(descriptor, label=descriptor_label)
        identity = canonical(descriptor)
        if identity in seen:
            fail(f"{label}: duplicate child descriptor at index {index}")
        seen.add(identity)

    annotations = value.get("annotations")
    if annotations is not None:
        if not isinstance(annotations, dict) or not all(
            isinstance(key, str)
            and key
            and isinstance(item, str)
            for key, item in annotations.items()
        ):
            fail(f"{label}.annotations: expected string-to-string object")


def normalized_index(value: dict[str, Any]) -> dict[str, Any]:
    normalized = copy.deepcopy(value)
    # Registry/tag-specific metadata may legitimately change while the content
    # addressable child descriptors stay exactly the same.  Do not ignore any
    # child annotations or any other top-level field.
    normalized.pop("annotations", None)
    return normalized


def compare(source: dict[str, Any], alias: dict[str, Any]) -> None:
    validate_index(source, label="source")
    validate_index(alias, label="alias")

    source_normalized = normalized_index(source)
    alias_normalized = normalized_index(alias)
    if source_normalized == alias_normalized:
        return

    source_manifests = source_normalized.get("manifests", [])
    alias_manifests = alias_normalized.get("manifests", [])
    if source_manifests != alias_manifests:
        fail("OCI index mismatch: ordered child descriptors differ")
    fail("OCI index mismatch outside allowed top-level annotations")


def main(argv: list[str]) -> int:
    if len(argv) != 3:
        print(
            f"usage: {pathlib.Path(argv[0]).name} SOURCE_RAW_JSON ALIAS_RAW_JSON",
            file=sys.stderr,
        )
        return 2

    source = load_json(pathlib.Path(argv[1]), "source")
    alias = load_json(pathlib.Path(argv[2]), "alias")
    compare(source, alias)
    print("OCI index equivalence verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
