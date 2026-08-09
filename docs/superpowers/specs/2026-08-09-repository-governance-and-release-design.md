# Repository Governance and Release Engineering Design

**Status:** Approved
**Approved:** 2026-08-09

## Purpose

Define the durable repository-governance, supply-chain-security, container-packaging, and release-distribution model for Zakup Gotov before release engineering is implemented.

This specification complements the platform foundation. It does not itself make a release or claim container support exists.

## Goals

- keep the public repository understandable, auditable, and difficult to modify accidentally;
- use GitHub-native security controls where they provide concrete value;
- keep permanent GitHub Actions minimal, reproducible, and least-privileged;
- make every future application release directly testable without requiring users to install Java, Node.js, pnpm, Maven, or PostgreSQL;
- publish the exact container artifacts that CI has verified;
- support both common x86-64 hosts and ARM64/Apple-Silicon Docker hosts;
- preserve an architecture that can later serve web, Android, and iOS clients without moving retailer secrets or core integration logic into clients.

## Non-goals

- Kubernetes or another orchestrator;
- microservices;
- a separate external container registry;
- continuous deployment to production infrastructure;
- making every GitHub feature active regardless of need;
- moving retailer integration logic to browsers merely to avoid backend work.

## Repository governance

`main` is protected repository truth.

Target policy:

- pull-request-only changes;
- squash-only merge policy;
- linear history;
- automatic deletion of merged source branches;
- blocked force pushes and deletion of `main`;
- required resolution of review conversations;
- required automated checks based on check names observed successfully in real runs;
- no mandatory second-human approval while there is only one maintainer;
- no silent security-check bypasses.

Only `main` and active PR branches should normally exist.

## GitHub Actions security

Permanent workflows must:

- default to `contents: read` or narrower permissions;
- request write permissions explicitly and only for the required job/action;
- avoid persistent credentials when they are unnecessary;
- use reproducible/frozen dependency resolution;
- converge on full-commit-SHA action pinning for immutable action code;
- remain covered by Dependabot GitHub Actions updates;
- never gain pull-request approval permission by default.

One-off scaffolding/generation workflows must be removed immediately after their output is committed and independently verified.

## GitHub security baseline

Enable all repository controls that materially reduce risk:

- Dependency Graph;
- Dependabot alerts;
- Dependabot security updates;
- Dependabot version updates;
- CodeQL for Java and JavaScript/TypeScript;
- Dependency Review;
- secret scanning;
- push protection;
- Private Vulnerability Reporting;
- GitHub Security Advisories.

A check blocked by repository configuration is treated as an actionable repository defect. It must not be made conditional merely to obtain a green PR.

## Release unit

A release is a tested application bundle composed of three services:

```text
web -> api -> PostgreSQL
```

The user-facing release experience is one Docker Compose project, not one oversized physical container.

The web and API images are built in CI and published to GHCR. PostgreSQL uses an official version-pinned upstream image with a persistent named volume.

## Release images

Publish:

```text
ghcr.io/true-ruslan/zakup-gotov-api:<version>
ghcr.io/true-ruslan/zakup-gotov-web:<version>
```

Each application image must support:

- `linux/amd64`;
- `linux/arm64`.

Stable releases may additionally update `latest`. Prereleases must never move `latest`.

Every published release remains addressable by immutable OCI digest even when convenient semantic tags exist.

## Release trigger

The authoritative stable/prerelease packaging flow is triggered by a published GitHub Release, not by arbitrary pushes or unreviewed tags.

A separate explicitly manual workflow may build/test snapshot images for maintainers when a full versioned release is unnecessary.

## Release verification sequence

A release workflow performs, in order:

1. verify release/tag/version consistency;
2. run the complete repository verification baseline;
3. run responsive browser verification;
4. run GitHub-native security/dependency gates required for release;
5. build API and web images;
6. scan built images for actionable vulnerabilities;
7. produce SBOM and build provenance/attestation;
8. publish multi-platform images to GHCR;
9. resolve immutable image digests;
10. generate a release-specific Compose file pinned to those digests;
11. start the exact Compose bundle in CI;
12. wait for PostgreSQL and API readiness rather than only process start;
13. smoke-test API and web through their released container boundaries;
14. attach the tested Compose bundle and relevant verification metadata to the GitHub Release.

An image that merely builds is not release-ready.

## Compose contract

The release Compose file must:

- require no local source build;
- contain no committed real secrets;
- use a persistent PostgreSQL volume;
- declare explicit health/readiness dependencies;
- expose a predictable local web port;
- wire web to API through the Compose network/runtime configuration;
- use application images by immutable digest for a specific release bundle;
- document clean start, stop, update, and data-removal commands.

Expected user flow:

```bash
docker compose -f compose.release.yaml pull
docker compose -f compose.release.yaml up -d
```

The exact file name/ports may be refined during implementation without changing the architectural contract.

## Supply-chain evidence

For release application images, produce and retain:

- OCI digest;
- SBOM;
- provenance/build attestation;
- vulnerability-scan result;
- source repository/commit identity;
- tested release Compose bundle.

Security exceptions must be explicit, narrow, time-bounded where possible, and documented. Global ignore lists are not acceptable as a default strategy.

## Client/backend retailer-integration policy

The authoritative retailer-integration path remains backend-side:

```text
Web / Mobile -> Zakup Gotov API -> GroceryProvider -> retailer
```

Reasons:

- credentials and partner tokens remain server-side;
- normalization/rate limiting/retry/freshness policies remain centralized;
- web and future mobile clients share the same product behavior;
- fixture/contract testing remains deterministic;
- retailer-specific response models do not leak into client code.

A client-side provider path is permitted only as an explicit provider-specific exception when all relevant conditions are satisfied, such as:

- no secret or privileged credential is required;
- browser/mobile direct use is permitted by the provider;
- CORS or equivalent platform access is supported;
- the interaction genuinely depends on a user-owned browser/session context;
- privacy/security implications are documented;
- the provider still conforms to a clear product-domain contract rather than spreading retailer-specific logic through UI code.

M0B must evaluate integration feasibility per retailer rather than assuming one transport model applies to all providers.

## Test philosophy

Release engineering follows the existing project policy:

- executable behavior is developed test-first when a meaningful behavioral RED/GREEN cycle exists;
- configuration is validated by automated execution rather than YAML inspection alone;
- Dockerfiles and Compose definitions must be exercised by CI;
- release smoke tests run against the exact built/published artifact set;
- repeated manual release acceptance is automation debt;
- manual checks remain only for genuinely non-deterministic or visual concerns that cannot be automated reliably.

## Documentation contract

When release engineering is implemented, update together:

- `README.md` quick start;
- `docs/DEVELOPMENT.md` developer/container workflows;
- `docs/REPOSITORY_GOVERNANCE.md` required checks/settings;
- `SECURITY.md` supply-chain/security support policy;
- `docs/PROJECT_STATE.md` factual completion state;
- `CHANGELOG.md` under `[Unreleased]` and later versioned release sections;
- release/runbook documentation.

## Acceptance criteria

This design is implemented only when evidence proves that:

- repository protections/security features are actually active or documented as platform-limited;
- permanent CI is least-privileged and security checks are green;
- a versioned release can publish verified multi-platform API/web images;
- an external user can start the complete application from a release Compose bundle without building source;
- CI starts and tests the exact released Compose bundle;
- artifacts carry digest, SBOM, vulnerability result, and provenance/attestation evidence;
- stable and prerelease tagging semantics cannot accidentally replace one another.
