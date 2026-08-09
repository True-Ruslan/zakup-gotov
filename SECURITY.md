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

Versioned publishing is a separate trust boundary:

- ordinary pull-request and source-verification workflows remain read-only;
- `Release / Verify` remains read-only and reruns source, browser, and container verification for the tagged commit;
- only `Release / Publish`, after verification succeeds, receives the narrowly scoped `contents: write`, `packages: write`, `attestations: write`, and `id-token: write` permissions needed for release publication;
- Docker/GitHub Actions in the release path use immutable full commit SHAs;
- QEMU binfmt and BuildKit helper images are pinned by digest rather than mutable helper tags;
- candidate application images are built before public version-tag promotion;
- both `linux/amd64` and `linux/arm64` variants are scanned for `HIGH` and `CRITICAL` vulnerabilities before promotion;
- the exact candidate digests are rendered into Compose and smoke-tested before promotion;
- version tags are created from the already verified digests without rebuild;
- prereleases are contractually prevented from updating `latest`;
- BuildKit/GitHub provenance and SBOM/scan evidence are retained with the release.

The versioned workflow is implemented and statically/TDD verified in pull-request CI, but its registry/OIDC behavior is not considered proven until the first real published prerelease succeeds.

GHCR publication does not by itself prove anonymous public pull access. Package visibility must be verified explicitly after first publication before documentation promises a publicly consumable image.

Do not weaken required checks, dependency freshness policy, vulnerability thresholds, image verification, provenance/attestation requirements, or exact-digest smoke tests merely to make a release publish. Any security exception must be explicit, narrow, justified, and reviewable.
