# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Retailer Feasibility (parallel)**  
Current focus: **keep the outstanding `v0.1.0-rc.3` release proof explicit while beginning evidence-driven provider feasibility testing, starting with a normalized offer trust contract and official-access research rather than undocumented retailer scraping**

## Product status

The platform foundation is executable and automatically verified. The core retailer-comparison product is **not implemented yet**. The current web surface intentionally presents project status rather than fake store cards, prices, or comparison behavior.

M0B feasibility work has now begun in parallel with the remaining M0A release-event proof. The first normalized provider-offer boundary is implemented and TDD-verified, but **no retailer/provider is supported yet**. Support still requires an acceptable technical/usage-rights path plus reproducible recorded fixtures and provider contract/parser tests.

Starting M0B discovery does not waive the remaining M0A release requirements. `v0.1.0-rc.3` is still required before the versioned GHCR publication path can be called fully runtime-proven.

## Completed foundation work

- PR #1 — product, architecture, engineering, security, contribution, state/roadmap, and planning foundation.
- PR #2–#9 — Java/Node/pnpm workspace, Java 25 + Spring Boot API, PostgreSQL 18/Flyway/jOOQ persistence, OpenAPI/client generation, responsive Next.js/React web, constrained Actuator surface, security workflows, and unified verification.
- PR #10–#12 — repository governance, deterministic required-check candidates, immutable Action pins, and cleanup of generated boilerplate.
- PR #15/#19/#20/#23 — verified dependency/action maintenance without weakening compatibility or security gates.
- PR #25 — production API/web Docker images, PostgreSQL 18.4-compatible no-source-build Compose topology, and executable `Release Bundle CI` smoke verification.
- PR #26 — versioned release contract and staged multi-platform GHCR workflow with vulnerability scans, SBOM/provenance evidence, exact-digest smoke verification, no-rebuild promotion, release assets, and stable-only `latest` policy.
- PR #27 — synchronized release-engineering state before the first real release event.
- PR #28 — TDD executable-mode regression fix after `v0.1.0-rc.1` exposed non-executable release helpers on clean checkouts.
- PR #29 — TDD container-security gate and runtime hardening after `v0.1.0-rc.2`: pgJDBC `42.7.12`, distroless non-root web runtime, and read-only PR/main/daily HIGH/CRITICAL image scanning.
- PR #35 — initial M0B provider-offer trust boundary and retailer feasibility evidence: `ObservedOffer`, explicit normalized availability, TDD fail-closed validation, primary-source integration matrix, and Kuper access-proof tracking.

## Verified platform baseline

### Backend

- Java 25;
- Spring Boot 4.1;
- Spring MVC + Virtual Threads;
- Spring Modulith architecture verification;
- PostgreSQL 18;
- Flyway;
- jOOQ;
- pgJDBC `42.7.12`;
- Testcontainers with real PostgreSQL 18.4 in integration tests.

### Contracts and clients

- OpenAPI 3.1 source contract;
- generated `@zakup-gotov/api-client`;
- `openapi-typescript` + `openapi-fetch`;
- generated-schema drift gate;
- strict TypeScript typechecking.

### Web

- Next.js 16.3.0;
- React/React DOM 19.2.8;
- TypeScript 5.9.3 compatibility line;
- Node 24.18.1 builder/toolchain;
- Next.js standalone output;
- production runtime on `gcr.io/distroless/nodejs24-debian13:nonroot`;
- no shell or package manager required in the final web runtime;
- Vitest + Testing Library;
- production Playwright coverage for desktop and mobile viewports.

### Operations and container topology

Public management HTTP surface remains intentionally limited to:

- `/actuator/health`;
- `/actuator/health/liveness`;
- `/actuator/health/readiness`;
- `/actuator/info`.

Environment, configuration-properties, and metrics Actuator endpoints remain HTTP-inaccessible. Request-detail logging is disabled by default. Provider credentials, raw payloads, authorization material, precise user addresses, and arbitrary user input must not become telemetry labels/log content by default.

Production container baseline:

- separate multi-stage API and web Dockerfiles;
- non-root runtime users for both application images;
- web runtime is distroless and starts the standalone Next.js server without a shell;
- `compose.release.yaml` contains no local source `build:` directives;
- PostgreSQL 18.4 uses a persistent named volume at `/var/lib/postgresql`;
- PostgreSQL health gates API startup, API readiness gates web startup;
- web health verifies both its own HTTP surface and API reachability over `API_BASE_URL`;
- only web publishes a host port by default; API remains internal;
- release-bundle failures emit Compose state/log diagnostics before cleanup.

## Automated verification

PR/main checks currently available:

- **API CI**;
- **Contract CI**;
- **Web CI**;
- **Web E2E**;
- **CodeQL / Java**;
- **CodeQL / JavaScript-TypeScript**;
- **Dependency Review**;
- **Release Bundle CI** — builds production API/web images and smoke-tests PostgreSQL → API → web;
- **Release Contract CI** — read-only verification of SemVer/prerelease semantics, immutable release dependencies, digest-only Compose rendering, workflow syntax and release trust ordering;
- **Container Security CI** — read-only PR/main/daily build of the exact production API/web Dockerfiles with fresh bases followed by fail-closed Trivy `HIGH`/`CRITICAL` vulnerability scans;
- local/clean-runner `./scripts/verify.sh`;
- local/CI `./scripts/verify-release-bundle.sh`.

PR #29 proved the container-security gate TDD-first: the initial workflow failed for both production images, then the same unchanged `CRITICAL,HIGH` / `exit-code: 1` policy passed after root-cause remediation. The final PR head also passed Release Bundle CI, demonstrating that the distroless startup and health-check path works in the real Compose topology.

PR #35 starts M0B with the same evidence discipline. The first RED failed because `ObservedOffer` / `AvailabilityStatus` did not exist. The first GREEN passed Maven `verify`. A second RED then exposed inconsistent `NullPointerException` behavior for missing `price` / `availability`; the minimal validation fix produced `10/10` provider-offer tests and `15/15` total API tests, while Spring Modulith verification and real PostgreSQL 18.4 Testcontainers integration remained green. The complete PR workflow set, including Web E2E, Release Bundle, Release Contract and both production-image security scans, is required again on the final documentation-synchronized head before merge.

The independently verified `main` ruleset still enforces the original seven CI/security checks. `Release Bundle CI`, `Release Contract CI`, and `Container Security CI` are proven required-check candidates but are not yet independently verified as enforced ruleset checks.

## Repository governance and security state

Verified repository-admin baseline:

- squash merge only;
- auto-merge/update-branch enabled;
- merged source branches deleted automatically;
- steady state is `main` plus active PR branches;
- stale/missing required checks block `main` merge;
- default Actions token permissions are read-only and Actions cannot approve pull requests;
- Dependency Graph, Dependabot alerts/security updates, secret scanning, push protection, private vulnerability reporting, CodeQL, and Dependency Review are operational.

Release permissions remain separated:

- `Release / Verify` is read-only;
- only downstream `Release / Publish` receives `contents: write`, `packages: write`, `attestations: write`, and `id-token: write`;
- ordinary PR CI, including `Container Security CI`, has no package/OIDC write permission;
- security-sensitive Actions are pinned to full commit SHAs;
- QEMU binfmt and BuildKit helper images are digest-pinned in the release workflow.

No `.trivyignore`, VEX suppression, severity reduction, `ignore-unfixed`, or scanner bypass was introduced to resolve `rc.2`.

## Release-engineering state

### Runtime-proven locally and in PR CI

- production API/web images build successfully;
- PostgreSQL 18.4 → API → web Compose startup is automated and green;
- API remains internal while web is host-published;
- PostgreSQL 18 volume-layout compatibility is covered by real Compose execution;
- release helper executable modes are regression-tested;
- exact production images are scanned before release in ordinary read-only CI;
- the real GitHub `release: published` event checks out and verifies the exact release source.

### `v0.1.0-rc.1` — release-helper mode defect

`rc.1` targeted `d3066258915542c2488d9a3277680b2cc478d611`. `Release / Verify` passed release metadata/main-ancestry validation, repository verification, production web build, and all responsive Playwright tests, then failed because `scripts/verify-release-bundle.sh` was stored as mode `100644`. `Release / Publish` never started.

PR #28 reproduced the executable-mode problem in a regression test and corrected both release helpers to `100755`.

### `v0.1.0-rc.2` — publish path reached, security gate failed closed

`rc.2` targeted corrected `main` at `184751e164f199fdc5262cf77ea86c931daf59f7`.

Proven by the real release event:

- `Release / Verify` completed successfully, including the production container bundle;
- `Release / Publish` started with its separate write-capable permission boundary;
- GHCR login, QEMU, and Buildx setup succeeded;
- both API and web multi-platform staging candidate indexes built and pushed for `linux/amd64` + `linux/arm64`;
- publication stopped at the first Trivy gate before any final-package/version promotion.

The exact API blocker was `org.postgresql:postgresql 42.7.11`, `CVE-2026-54291` (`HIGH`), fixed in `42.7.12`; the API Ubuntu runtime itself had zero HIGH/CRITICAL findings.

A TDD reproduction moved the same policy into ordinary CI before applying fixes. It additionally showed that the old Node Bookworm-slim web runtime carried 22 Debian HIGH/CRITICAL findings plus 7 npm/runtime-library findings, while the application Next.js/React packages were clean at that threshold. PR #29 therefore updated pgJDBC and replaced only the final web runtime with distroless Node 24 Debian 13/non-root rather than suppressing findings.

Because `rc.2` failed before promotion, it does **not** prove final packages, GitHub provenance attestations, SemVer OCI tags, release evidence assets, or a successful final digest-pinned Compose smoke. No `latest` update is attributed to `rc.2`.

### Merged release design still awaiting complete end-to-end publication

The workflow still requires this sequence:

1. strict SemVer + GitHub prerelease-state validation;
2. multi-platform staging builds for API/web;
3. both-platform HIGH/CRITICAL scans and SPDX SBOM evidence;
4. staging digest-pinned Compose smoke;
5. no-rebuild copy into final packages under deterministic `verified-<source-sha>` tags;
6. final-package digest-pinned Compose smoke;
7. GitHub provenance attestations;
8. SemVer tag promotion without rebuild;
9. stable-only optional `latest`;
10. amd64/arm64 manifest verification and release evidence upload.

A successful `v0.1.0-rc.3` is required before this path can be called fully runtime-proven. GHCR package visibility must then be independently checked: staging must remain private, and final package anonymous/public availability must not be claimed unless explicitly verified.

The current assistant GitHub integration can mutate repository/PR/issue state but does not expose GitHub Release creation, and no separate authenticated release-capable CLI/API credential is available in this execution environment. Therefore `rc.3` has **not** been fabricated via a mutable tag or alternate trigger; the real `release: published` proof remains explicitly outstanding.

See [`RELEASES.md`](RELEASES.md).

## M0B retailer-feasibility state

Initial provider-domain trust boundary:

- `ObservedOffer` requires `providerId`, `fulfillmentContextId`, provider SKU, non-negative price, ISO 4217 currency, explicit availability, `observedAt`, and `sourceReference`;
- availability is normalized as `AVAILABLE`, `UNAVAILABLE`, or `UNKNOWN` rather than inferred silently;
- incomplete external data fails closed before later comparison logic;
- normal deterministic CI still performs **no live retailer calls**.

Primary-source research is recorded in [`integrations/retailer-feasibility.md`](integrations/retailer-feasibility.md). Current decisions are deliberately conservative:

- **Kuper** — `PROMISING_CONTACT_REQUIRED`: official API program publishes a Client apps API, but exact read-side scope, access, caching/fixture rights and comparison-use permission must be confirmed;
- **Yandex Eats Retail API** — `PARTNER_SIDE_ONLY`: documented API integrates Yandex/Yango with a partner retailer/POS and is not currently a read-side Zakup Gotov API;
- **Lenta / VkusVill** — `BLOCKED_WITHOUT_AGREEMENT`: consumer surfaces are not treated as permission for production scraping/reuse;
- **Magnit / X5** — `NO_PUBLIC_CLIENT_API_FOUND`: partnership paths are plausible, but a suitable public read-side catalog API has not been proven.

Issue #36 tracks the exact Kuper integration questions required before an adapter spike: location/store resolution, catalog/search, SKU, price/availability, freshness, auth/rate limits, sandbox, sanitized fixture retention, caching, comparison rights, attribution/deep links and checkout handoff.

No provider is considered supported until reproducible sanitized fixtures and parser/contract tests prove an acceptable path. A one-off successful live request is insufficient.

## Dependency maintenance state

Compatible dependency/Actions maintenance is merged only after full verification. Known incompatible major updates remain intentionally deferred rather than bypassed:

- TypeScript 7.0.2 is incompatible with the current `openapi-typescript 7.13.0` peer range;
- ESLint 10 breaks the current lint configuration;
- `@types/node` 26 remains deferred while the toolchain/runtime line is Node 24.

## Approved engineering policy

[`ENGINEERING.md`](ENGINEERING.md) remains mandatory:

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
4. How can delivery fees/minimum-order constraints be obtained and normalized per retailer?
5. What deterministic product-matching quality is achievable before AI assistance is justified?

## Immediate next work

1. Publish **`v0.1.0-rc.3`** through the real GitHub Release `published` event from verified `main`; do not reuse `rc.1` or `rc.2`, and do not substitute a manual tag for the release event.
2. Inspect the complete release proof: `Release / Verify` + `Release / Publish`, multi-platform digests, Trivy results, SBOMs, attestations, staging/final exact-digest Compose smoke tests, manifests, checksums, attached evidence, `latest` behavior, and package visibility.
3. Resolve issue #36 with the Kuper integration team and obtain a documented acceptable access/usage-rights path before implementing the first real provider adapter.
4. In parallel, pursue an independent second provider path (initially X5 and/or Magnit partnership/e-commerce channels) because M0 requires at least two acceptable integrations.
5. When the first access path is approved, run the first provider spike against one explicit fulfillment/location context and a fixed corpus of at least 10 common grocery requirements; record sanitized fixtures and make offline parser/contract tests the normal regression path.
6. Add `Release Bundle CI`, `Release Contract CI`, and `Container Security CI` to the `main` ruleset when the settings mutation can be applied and independently verified.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer/provider integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/legal/freshness constraints.
