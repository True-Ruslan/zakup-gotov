#!/usr/bin/env python3

import json
import pathlib
import sys

VEX_PATH = pathlib.Path("security/vex/CVE-2026-14456.openvex.json")
EXPECTED_PURL = "pkg:deb/debian/libssl3t64@3.5.6-1~deb13u2"
EXPECTED_CVE = "CVE-2026-14456"
EXPECTED_JUSTIFICATION = "vulnerable_code_not_in_execute_path"


def fail(message: str) -> None:
    raise SystemExit(f"web VEX contract failed: {message}")


def main() -> None:
    document = json.loads(VEX_PATH.read_text(encoding="utf-8"))

    if document.get("@context") != "https://openvex.dev/ns/v0.2.0":
        fail("unexpected OpenVEX context")
    if document.get("version") != 1:
        fail("version must remain exactly 1 until the reviewed statement changes")

    statements = document.get("statements")
    if not isinstance(statements, list) or len(statements) != 1:
        fail("exactly one VEX statement is allowed")

    statement = statements[0]
    vulnerability = statement.get("vulnerability")
    if vulnerability != {"name": EXPECTED_CVE}:
        fail(f"statement must target only {EXPECTED_CVE}")

    products = statement.get("products")
    if products != [{"@id": EXPECTED_PURL}]:
        fail(f"statement must target only exact package PURL {EXPECTED_PURL}")

    if statement.get("status") != "not_affected":
        fail("status must be not_affected")
    if statement.get("justification") != EXPECTED_JUSTIFICATION:
        fail(f"justification must be {EXPECTED_JUSTIFICATION}")

    impact = statement.get("impact_statement")
    if not isinstance(impact, str) or not impact.strip():
        fail("impact_statement is required")
    for required_fragment in ("--experimental-quic", "libssl/libcrypto", "QUIC server-listener"):
        if required_fragment not in impact:
            fail(f"impact_statement must preserve evidence fragment: {required_fragment}")

    print(
        "web VEX contract OK: "
        f"{EXPECTED_CVE} -> {EXPECTED_PURL} ({EXPECTED_JUSTIFICATION})"
    )


if __name__ == "__main__":
    main()
