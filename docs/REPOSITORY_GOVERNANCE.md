# Repository Governance

This document defines the intended GitHub repository controls for `True-Ruslan/zakup-gotov`. Git-tracked configuration and GitHub repository settings are both part of the engineering baseline.

## Principles

1. `main` is reviewed repository truth, not a development branch.
2. Functional work happens in short-lived branches and merges through pull requests.
3. Required checks are based on real successful check names, never guessed strings.
4. Security checks are not disabled merely to make a pull request green.
5. Repository settings should minimize accidental privilege, irreversible history changes, and supply-chain risk.
6. Rules remain practical for a single-maintainer project; controls that require a second human are introduced only when another maintainer exists.

## Merge policy

Target repository settings:

- squash merge: enabled;
- merge commits: disabled;
- rebase merge: disabled;
- automatic head-branch deletion after merge: enabled;
- auto-merge: enabled;
- update-branch button: enabled;
- default branch: `main`.

This gives a linear target history while preserving rich development history in pull requests.

## `main` ruleset

Target rules for `main`:

- changes require a pull request;
- linear history is required;
- force pushes are blocked;
- branch deletion is blocked;
- unresolved review conversations block merge;
- required status checks must pass;
- no mandatory second-human approval while the project has one maintainer;
- administrators should normally follow the same rules rather than silently bypass them.

Required checks are activated only after they have completed successfully at least once in the repository. The currently independently verified enforced baseline is:

- `API CI`;
- `Contract CI`;
- `Web CI`;
- `Web E2E`;
- CodeQL Java;
- CodeQL JavaScript/TypeScript;
- `Dependency Review`.

Additional proven candidates:

- `Release Bundle CI` — builds the production API/web images and executes the full PostgreSQL → API → web Compose topology;
- `Release Contract CI` — verifies version/prerelease semantics, digest-only release Compose rendering, immutable release dependencies, release-workflow syntax, and security/promotion ordering without write permissions;
- `Container Security / API` and `Container Security / Web` — build the exact production images with fresh base images and fail closed on Trivy `HIGH`/`CRITICAL` vulnerabilities before release publication.

These candidates should be added to `main` required checks only when the ruleset change can be applied and independently verified. Documentation must not describe them as already enforced until then.

## Branch lifecycle

Long-lived branches are avoided. Normally the repository should contain:

- `main`;
- active pull-request branches only.

After a pull request is squash-merged, its source branch should be deleted automatically. Historical context remains in the pull request and target history.

## GitHub Actions policy

Permanent workflows must solve a recurring repository need. One-off generators/scaffold workflows are deleted immediately after the generated artifact is committed and independently verified.

Default workflow permissions remain read-only. A workflow receives write permissions only for the smallest explicit capability needed by that workflow.

Third-party and GitHub-owned actions in permanent security-sensitive workflows are pinned by full commit SHA. Mutable helper images used by security-sensitive release actions should also be pinned by digest where supported.

Do not grant workflows permission to approve pull requests.

Release and container-security boundaries:

- `Release Bundle CI` is ordinary read-only PR/main verification;
- `Release Contract CI` is ordinary read-only PR/main verification;
- `Container Security CI` is ordinary read-only PR/main/daily verification and must not receive package or OIDC write permissions;
- `.github/workflows/release.yml` runs only for a published GitHub Release;
- its `Release / Verify` job remains `contents: read`;
- only the downstream `Release / Publish` job may receive `contents: write`, `packages: write`, `attestations: write`, and `id-token: write`;
- package/OIDC write permissions must never be copied into ordinary PR CI.

Container vulnerability policy:

- exact production API/web Dockerfiles are scanned before release in ordinary CI;
- the release workflow still performs both-platform scans on the published staging candidates;
- `HIGH` and `CRITICAL` findings fail the gate;
- scanner ignores, severity reduction, `ignore-unfixed`, or equivalent bypasses require explicit security justification and must never be introduced merely to unblock a release;
- removing unnecessary runtime software is preferred over suppressing findings when the software is not required for application execution.

## Security features

Target security baseline for this public repository:

- Dependency Graph;
- Dependabot alerts;
- Dependabot security updates;
- Dependabot version updates;
- CodeQL code scanning;
- Dependency Review on pull requests;
- production-container vulnerability scanning before release;
- secret scanning;
- push protection;
- Private Vulnerability Reporting;
- GitHub Security Advisories for coordinated fixes.

A known security gate that cannot execute because a repository setting is disabled is a repository defect, not a reason to weaken the workflow.

## Issues and community files

The repository tracks:

- structured bug reports;
- structured feature requests;
- public contribution rules;
- security disclosure policy;
- Code of Conduct;
- CODEOWNERS;
- pull request quality template.

Security vulnerabilities must not be filed as public issues.

GitHub Discussions, Wiki, Projects, sponsorship/funding, and CITATION metadata are intentionally not enabled merely to maximize feature count. They should be enabled only when the project has a real workflow that benefits from them.

## License

Repository visibility and software licensing are separate decisions. The repository is public but currently has no open-source license. A license will be added only after the project owner deliberately chooses the intended reuse/distribution model.

## Releases and packages

Runtime-proven production container baseline:

- separate production `api` and `web` images;
- Next.js standalone runtime;
- distroless non-root final web runtime with no shell/package-manager dependency;
- a no-source-build Compose definition for `web + api + PostgreSQL 18.4`;
- persistent PostgreSQL named volume;
- explicit health/readiness dependencies;
- API kept internal to the Compose network while the web port is host-published;
- automated exact-topology startup/smoke verification with failure diagnostics;
- read-only pre-release production-image HIGH/CRITICAL scanning.

Implemented versioned publishing contract:

- published GitHub Release is the authoritative trigger;
- tagged source must belong to `main` history;
- strict SemVer and GitHub prerelease-state consistency are enforced;
- prebuilt `api` and `web` OCI indexes target `linux/amd64` and `linux/arm64`;
- candidate image digests are scanned on both target platforms before promotion;
- per-platform SPDX SBOM evidence is generated;
- BuildKit provenance/SBOM and GitHub provenance attestations are created for exact image digests;
- release-specific Compose is pinned to immutable application-image digests;
- staging and final-package digest sets are pulled and smoke-tested before version promotion;
- promotion copies the verified image indexes without rebuild;
- stable releases may update `latest`; prereleases never do;
- Compose, manifests, scans, SBOMs, verification metadata, and checksums are attached to the GitHub Release.

`v0.1.0-rc.1` and `v0.1.0-rc.2` have exercised the real release-event path and exposed two defects at progressively later boundaries. Neither constitutes complete end-to-end publication proof. A new prerelease must complete the full workflow before a stable release is considered.

GHCR package visibility must be independently checked after the first successful publication. Public repository visibility is not treated as evidence that new package images are anonymously pullable.

## Audit cadence

Repository governance should be reviewed when any of the following changes:

- a new required workflow/check is introduced;
- another maintainer joins;
- releases begin;
- secrets/provider credentials are introduced;
- GitHub changes available security controls;
- a security incident or CI bypass reveals a policy gap.
