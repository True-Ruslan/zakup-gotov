#!/usr/bin/env python3

import json
import pathlib
import sys


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("usage: report_trivy_json.py REPORT.json")

    path = pathlib.Path(sys.argv[1])
    if not path.is_file():
        print(f"Trivy report was not created: {path}")
        return

    report = json.loads(path.read_text(encoding="utf-8"))
    findings = []
    for result in report.get("Results") or []:
        target = result.get("Target") or "<unknown-target>"
        for vulnerability in result.get("Vulnerabilities") or []:
            findings.append(
                {
                    "target": target,
                    "package": vulnerability.get("PkgName"),
                    "vulnerability": vulnerability.get("VulnerabilityID"),
                    "severity": vulnerability.get("Severity"),
                    "status": vulnerability.get("Status"),
                    "installed": vulnerability.get("InstalledVersion"),
                    "fixed": vulnerability.get("FixedVersion"),
                    "title": vulnerability.get("Title"),
                }
            )

    print(f"Trivy failure evidence from {path}: {len(findings)} finding(s)")
    print(json.dumps(findings, indent=2, ensure_ascii=False, sort_keys=True))


if __name__ == "__main__":
    main()
