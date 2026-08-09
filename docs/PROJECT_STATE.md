# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A — Platform Foundation**  
Current focus: **fix the first prerelease runtime defect, validate the corrected release pipeline with a follow-up prerelease, finish repository/release gates, then enter M0B retailer feasibility**

## Product status

The platform foundation is executable and automatically verified. The core retailer-comparison product is **not implemented yet**. The current web surface intentionally presents project status rather than fake store cards, prices, or comparison behavior.

No retailer integration is considered supported until M0B proves it with acceptable technical/legal evidence and reproducible fixture/contract tests.

## Completed foundation work

- PR #1 — product, architecture, engineering, security, contribution, state/roadmap, and planning foundation.
- PR #2 / M0A Task 1 — Java/Node/pnpm toolchains and monorepo conventions.
- PR #3 / Task 2 — Java 25 + Spring Boot 4.1 API bootstrap, Maven Wrapper, architecture/context verification, API CI.
- PR #4 / Task 3 — PostgreSQL 18 + Flyway + jOOQ + real Testcontainers persistence baseline.
- PR #5 / Task 4 — OpenAPI 3.1 contract, tested system endpoint, generated TypeScript client, Contract CI.
- PR #6 / Task 5 — Next.js 16 / React 19 responsive web shell, component tests, desktop/mobile Playwright, Web CI.
- PR #7 / Task 6 — constrained Actuator health/readiness surface, executable exposure guard, observability/privacy rules.
- PR #8 / Task 7 — CodeQL, Dependency Review, Dependabot version-update policy, and hardened security workflows.
- PR #9 / Task 8 — reproducible `./scripts/verify.sh`, `docs/DEVELOPMENT.md`, and verified clean-runner developer workflow.
- PR #10 — public README/documentation index, repository-governance policy, support routing, branch/workflow audit, and approved governance/release design.
- PR #11 — unconditional required-check candidates, immutable Action SHAs, non-persisted checkout credentials, finite timeouts/concurrency, and Maven `verify` in API CI.
- PR #12 — removal of stale `create-next-app` README/ignore boilerplate and centralized monorepo guidance.
- PR #19 — `actions/cache` 6.1.0 maintenance after a full required-check pass.
- PR #20 — `actions/checkout` 7.0.1 maintenance after a fresh required-check pass on current `main`.
- PR #23 — consolidated `actions/setup-node` 7.0.0 and `dependency-review-action` 5.0.0 maintenance with corrected immutable-pin annotations and full CI/security verification.
- PR #15 — CI-verified web dependency maintenance to Next.js 16.3.0, React/React DOM 19.2.8, and `eslint-config-next` 16.3.0.
- PR #25 — production API/web Docker images, Next.js standalone runtime, PostgreSQL 18.4-compatible no-source-build Compose topology, and executable `Release Bundle CI` smoke verification.
- PR #26 — merged versioned-release contract and release workflow implementation: strict SemVer/prerelease semantics, multi-platform GHCR candidates, per-platform vulnerability scans/SBOMs, immutable digest rendering, exact-published-bundle smoke verification, attestations, no-rebuild promotion, release assets, and stable-only `latest` policy.
- PR #27 — synchronized post-merge release-engineering state before the first real release event.
- PR #28 — executable-mode regression fix for the release helper scripts, developed TDD-first after `v0.1.0-rc.1` exposed that clean Git checkouts stored the two release helpers as non-executable files.

## Verified platform baseline

### Backend

- Java 25;
- Spring Boot 4.1;
- Spring MVC + Virtual Threads;
- Spring Modulith architecture verification;
- PostgreSQL 18;
- Flyway;
- jOOQ;
- Testcontainers with real PostgreSQL 18.4 in integration tests.

### Contracts and clients

- OpenAPI 3.1 source contract;
- generated `@zakup-gotov/api-client`;
- generated-schema drift gate;
- strict TypeScript typechecking.

### Web

- Next.js 16.3.0;
- React 19.2.8;
- TypeScript 5.x compatibility line;
- Next.js standalone production-container output;
- Vitest + Testing Library;
- Playwright production-browser coverage for desktop and mobile viewports.

### Operations and container topology

Public management HTTP surface is intentionally limited to:

- `/actuator/health`;
- `/actuator/health/liveness`;
- `/actuator/health/readiness`;
- `/actuator/info`.

Environment, configuration-properties, and metrics Actuator endpoints remain HTTP-inaccessible. Request-detail logging is disabled by default. Provider credentials, raw payloads, authorization material, precise user addresses, and arbitrary user input must not become telemetry labels/log content by default.

Production container baseline:

- separate multi-stage API and web Dockerfiles;
- non-root runtime users for both application images;
- `compose.release.yaml` contains no local source `build:` directives;
- PostgreSQL 18.4 uses a persistent named volume at `/var/lib/postgresql`;
- PostgreSQL health gates API startup, API readiness gates web startup;
- web health verifies both its own HTTP surface and API reachability over `API_BASE_URL` on the Compose network;
- only the web service publishes a host port by default; API remains internal to the Compose network;
- failures in release-bundle verification automatically emit Compose status/log diagnostics before cleanup.

## Automated verification

PR/main checks currently available:

- **API CI**;
- **Contract CI**;
- **Web CI**;
- **Web E2E**;
- **CodeQL / Java**;
- **CodeQL / JavaScript-TypeScript**;
- **Dependency Review**;
- **Release Bundle CI** — builds production API/web images and smoke-tests the complete PostgreSQL → API → web topology;
- **Release Contract CI** — read-only verification of release SemVer/prerelease semantics, digest-only Compose rendering, immutable action/helper pins, release-workflow YAML syntax, build/scan/smoke/attest/promotion ordering, and executable release-helper modes in a clean checkout;
- local/clean-runner **`./scripts/verify.sh`**;
- local/CI **`./scripts/verify-release-bundle.sh`**.

The currently independently verified `main` ruleset still enforces the original seven CI/security checks. `Release Bundle CI` and `Release Contract CI` are proven check candidates but are not yet independently verified as required ruleset checks.

## Repository governance and security state

Verified repository-admin baseline:

- squash merge enabled; merge commits and rebase merge disabled;
- auto-merge and update-branch enabled;
- merged source branches are deleted automatically;
- only `main` plus active PR branches are retained;
- stale/missing required checks block `main` merge;
- default workflow token permissions are read-only and Actions cannot approve pull requests;
- Dependency Graph enabled and SBOM endpoint available;
- Dependabot alerts/security updates enabled;
- secret scanning enabled;
- secret scanning push protection enabled;
- private vulnerability reporting enabled;
- CodeQL and Dependency Review operational.

The versioned release workflow intentionally separates permissions:

- `Release / Verify` uses read-only repository access;
- only `Release / Publish`, after verification, receives `contents: write`, `packages: write`, `attestations: write`, and `id-token: write`;
- ordinary PR CI never receives registry-publish or OIDC release permissions;
- Docker/GitHub Actions are pinned to full commit SHAs;
- QEMU binfmt and BuildKit helper images used by release publishing are also digest-pinned.

## Dependency maintenance state

The first Dependabot maintenance cycle was intentionally triaged rather than blindly merged:

- compatible Actions updates were verified and merged;
- compatible Next.js/React maintenance was refreshed against current `main` and passed full CI including production Web E2E;
- TypeScript 7.0.2 remains deferred because `openapi-typescript` 7.13.0 is incompatible with it;
- ESLint 10 remains deferred because the current web lint configuration fails under that major line;
- `@types/node` 26 remains deferred while the repository runtime is pinned to Node 24.

No required test, supply-chain, or security gate was weakened to make an automated dependency update pass.

## Release-engineering state

### Runtime-proven

- production API/web Docker images build successfully;
- PostgreSQL 18.4 → API → web Compose startup is automated and green;
- API remains internal while web is host-published;
- exact local production topology smoke verification is green;
- PostgreSQL 18 volume-layout compatibility is covered by real Compose execution;
- the real GitHub `release: published` event fires for a published prerelease and checks out the exact release tag;
- `v0.1.0-rc.1` proved release metadata validation, the `main` ancestry guard, Java/Node/pnpm setup, full `./scripts/verify.sh`, production web build, and all four responsive Playwright tests on the release-event runner.

### First prerelease runtime finding

`v0.1.0-rc.1` targeted `d3066258915542c2488d9a3277680b2cc478d611` and was correctly marked as a GitHub prerelease. `Release / Verify` passed every step through production browser tests, then failed before container verification because `scripts/verify-release-bundle.sh` was stored in Git with mode `100644`; the runner therefore returned `Permission denied` / exit 126. `Release / Publish` was skipped, so no GHCR candidate/final images, scans, SBOM release assets, attestations, version OCI tags, or `latest` update can be claimed from rc.1.

The root cause was independently confirmed from the Git tree: both `scripts/verify-release-bundle.sh` and `scripts/release/verify-published-release.sh` were `100644` while the working `scripts/verify.sh` was `100755`. PR #28 adds a regression test that first failed on both helpers and changes only those two Git tree modes to `100755`, preserving their blob contents.

### Merged release design awaiting successful end-to-end publication

- strict SemVer + GitHub prerelease flag validation;
- prereleases never move `latest`;
- unverified multi-platform `linux/amd64` + `linux/arm64` API/web candidates are isolated in staging packages;
- BuildKit provenance and SBOM attestations;
- per-platform `HIGH`/`CRITICAL` Trivy vulnerability gates;
- per-platform SPDX JSON SBOM release evidence;
- staging candidate and final-package Compose bundles are rendered from exact GHCR digests and smoke-tested in sequence;
- verified staging digests are copied without rebuild into final packages under deterministic `verified-<source-sha>` tags;
- GitHub provenance attestations are pushed for the final-package image digests;
- SemVer tags are created only after final-package smoke verification and attestation;
- stable `latest` promotion is conditional and unavailable to prereleases;
- manifest verification for amd64/arm64;
- release verification JSON, manifests, scans, SBOMs, checksums, and digest-pinned Compose are attached as GitHub Release assets.

A follow-up prerelease from corrected `main` is required before the publishing path moves from implementation evidence to complete runtime evidence. GHCR package visibility must also be checked after first successful publication; a public source repository alone is not treated as proof of anonymous image pullability.

See [`RELEASES.md`](RELEASES.md).

## Approved engineering policy

[`ENGINEERING.md`](ENGINEERING.md) is mandatory repository policy:

- TDD for executable behavior;
- evidence before completion claims;
- automation-first verification;
- real integration dependencies when practical and deterministic;
- short-lived branches and small PRs;
- documentation/changelog synchronized with repository reality;
- no silent security/test bypasses;
- no retailer live calls in normal deterministic CI.

## Current critical unknowns

1. Which target retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers achieve useful basket coverage with acceptable freshness/reliability?
3. What location precision must be used transiently and what, if anything, may be persisted?
4. How can delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What deterministic product-matching quality is achievable before any AI-assisted stage is justified?

## Immediate next work

1. Publish a follow-up **prerelease** from corrected `main` (expected `v0.1.0-rc.2`) and verify both `Release / Verify` and `Release / Publish` end to end.
2. Verify multi-platform GHCR digests, Trivy gates, SBOMs, attestations, attached evidence, both staging/final exact digest-pinned Compose smoke tests, and confirm that prerelease publication leaves `latest` untouched.
3. Verify staging packages remain private and verify the intended final GHCR package visibility; do not claim anonymous availability before it is proven.
4. Add `Release Bundle CI` and `Release Contract CI` to the `main` ruleset when the settings API/UI change can be applied and independently verified.
5. Run final M0A verification, then hand off to separately planned M0B Retailer Feasibility.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/legal/freshness constraints.
