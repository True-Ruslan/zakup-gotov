# Security Policy

## Supported versions

Zakup Gotov is currently in pre-release development. No released version is considered supported yet.

Once public releases begin, this section will list supported versions and security-fix policy explicitly.

## Reporting a vulnerability

Please do **not** open a public issue for a suspected vulnerability.

Preferred reporting path: GitHub Private Vulnerability Reporting for this repository. It is enabled and should be used for confidential vulnerability reports.

A useful report should include:

- affected component and version/commit;
- reproduction steps;
- expected and actual behavior;
- realistic impact;
- proof of concept where safe;
- suggested mitigation if known.

## Security principles

The project treats the following as security-sensitive by design:

- retailer/provider credentials and tokens;
- user addresses and precise location data;
- authentication/session material;
- external provider responses that may contain identifiers;
- logs/traces that could accidentally include credentials or location data.

Secrets must never be committed to the repository. Logs, traces, fixtures, and test artifacts must be reviewed for secret and personal-data leakage before publication.

## Dependency and supply-chain policy

The repository uses GitHub-native Dependency Review, Dependabot, secret scanning/push protection, and CodeQL as quality/security controls. High-severity security issues block release until triaged and resolved or explicitly documented with a justified exception.

Production container topology is exercised by `Release Bundle CI` with read-only repository permissions. The release Compose file requires externally supplied application-image references and a database password; it contains no real committed secret and publishes only the web service to the host by default.

Versioned public release publishing is intentionally a separate trust boundary. When implemented, only that release workflow may receive the narrowly required package/attestation write permissions. It must produce immutable application-image digests, vulnerability-scan results, SBOM, source/build provenance or equivalent attestations, and a tested release-specific Compose bundle before a release is considered consumable.

Do not weaken required checks, dependency freshness policy, vulnerability thresholds, or image verification merely to make a release publish. Any security exception must be explicit, narrow, justified, and reviewable.
