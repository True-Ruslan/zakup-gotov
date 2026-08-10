# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Retailer Feasibility (parallel)**  
Current focus: **finish the outstanding `v0.1.0-rc.3` release proof in parallel with controlled retailer-provider spikes, using the shared feasibility harness and allowing public unofficial consumer APIs only when they work without access-control or anti-bot circumvention**

## Product status

The platform foundation is executable and automatically verified. The core retailer-comparison product is **not implemented yet**. The current web surface intentionally presents project status rather than fake store cards, prices, or comparison behavior.

M0B feasibility work is active in parallel with the remaining M0A release-event proof. The normalized provider-offer boundary and reusable provider feasibility harness are implemented and TDD-verified in PR #37, but **no retailer/provider is supported yet**. Support still requires an acceptable technical/usage-rights path plus reproducible sanitized fixtures and provider contract/parser tests.

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
- PR #37 — shared M0B provider feasibility harness: access/capability model, provider-scoped location/query model, `RetailerProvider` port, structural fixture/live provider split, offline harness, explicit live-probe entry point, expanded retailer research, and the executable multi-provider spike plan.

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

PR #35 started M0B with the same evidence discipline. Its TDD cycles produced the normalized `ObservedOffer` boundary with `10/10` provider-offer tests and `15/15` total API tests while Spring Modulith verification and real PostgreSQL 18.4 integration remained green.

PR #37 continues the TDD chain:

- RED #1: test-only commit failed at `testCompile` because the feasibility-harness types did not exist;
- GREEN #1: the initial harness implementation passed `ProviderFeasibilityHarnessTest` `5/5`, the complete API suite `20/20`, real PostgreSQL 18.4/Testcontainers and packaged JAR verification;
- independent change-review then found that the first fixture/live boundary relied on a provider-supplied execution-mode enum and could therefore be misdeclared;
- RED #2 replaced that expectation with structural `FixtureRetailerProvider` / `LiveRetailerProvider` types plus a separate `ProviderLiveProbe`, and failed on exactly those missing types;
- the implementation removed the self-declared execution-mode flag, made the offline harness accept only fixture-provider types, added the explicit live-probe path, and also requires both `PRODUCT_SEARCH` and `PRICE` capabilities for offer search.

The complete PR workflow set, including Web E2E, Release Bundle, Release Contract, CodeQL, Dependency Review and both production-image security scans, must pass again on the final documentation-synchronized head before merge.

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

### Shared provider boundary

- `ObservedOffer` requires `providerId`, `fulfillmentContextId`, provider SKU, non-negative price, ISO 4217 currency, explicit availability, `observedAt`, and `sourceReference`;
- availability is normalized as `AVAILABLE`, `UNAVAILABLE`, or `UNKNOWN` rather than inferred silently;
- provider access is classified as `OFFICIAL_API`, `PUBLIC_UNOFFICIAL_API`, or `PARTNER_API`;
- provider capabilities explicitly model location resolution, catalog, product search, price and availability;
- `LocationContext` is provider-scoped and contains a fulfillment-context identifier plus coarse locality rather than a precise user address;
- `ProviderFeasibilityHarness.offline()` accepts only `FixtureRetailerProvider`;
- network-capable integrations implement `LiveRetailerProvider` and use the separate explicit `ProviderLiveProbe` path;
- offer-search validation requires `PRODUCT_SEARCH` and `PRICE`, and returned offers must match both provider identity and requested fulfillment context;
- normal deterministic CI performs **no live retailer calls**.

### Public unofficial API policy

A data source is not rejected merely because its API is undocumented. A public consumer backend may be tested as `PUBLIC_UNOFFICIAL_API` when the ordinary product can use it without bypassing access controls. M0B explicitly excludes stolen/forged private credentials, other users' sessions, CAPTCHA/anti-bot bypass, browser-fingerprint evasion, proxy/IP rotation used to circumvent blocking, and ignoring provider rate limits. Third-party wrappers are research evidence only; their source-code license does not grant rights to retailer data.

Detailed evidence and decisions are recorded in [`integrations/retailer-feasibility.md`](integrations/retailer-feasibility.md), and the executable sequence is in [`superpowers/plans/2026-08-10-m0b-provider-spikes.md`](superpowers/plans/2026-08-10-m0b-provider-spikes.md).

Current priority decisions:

- **Pyaterochka / 5ka** — `SPIKE_IF_RAW_HTTP_WORKS`: strong store-scoped technical evidence exists, but the first gate is proving the necessary calls work without Camoufox/stealth/proxy rotation;
- **Perekrestok** — `SPIKE_NOW`: public-site request/schema evidence is sufficient for a controlled feasibility probe; exact store-specific price/availability semantics still need proof;
- **Magnit** — `SPIKE_NOW`: public catalog exposes `shopCode`-scoped behavior and is the priority independent non-X5 path;
- **Chizhik** — `SPIKE_IF_RAW_HTTP_WORKS`: third-party evidence is useful, but the path is rejected if plain HTTP cannot replace browser/proxy evasion techniques;
- **Ozon Fresh / Samokat** — `RESEARCH_REQUIRED`: location-specific consumer-backend semantics still need characterization;
- **Kuper** — `PROMISING_CONTACT_REQUIRED`: official API program publishes a Client apps API, but exact read-side scope, access, caching/fixture rights and comparison-use permission must be confirmed;
- **Yandex Eats Retail API** — `PARTNER_SIDE_ONLY`: documented API integrates Yandex/Yango with a partner retailer/POS and is not currently a read-side Zakup Gotov API;
- **Lenta / VkusVill** — `BLOCKED_WITHOUT_AGREEMENT`: current consumer surfaces/terms are not treated as a sufficient production reuse basis.

Issue #36 continues to track the exact Kuper integration questions. No provider is considered supported until reproducible sanitized fixtures and parser/contract tests prove an acceptable path. A one-off successful live request is insufficient.

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

1. Which target retailers expose a technically stable and acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers achieve useful basket coverage with acceptable freshness/reliability?
3. Can public unofficial APIs remain usable through ordinary non-evasive HTTP, or do some candidates depend on unstable anti-bot behavior?
4. What location precision must be used transiently and what, if anything, may be persisted?
5. How can delivery fees/minimum-order constraints be obtained and normalized per retailer?
6. What deterministic product-matching quality is achievable before AI assistance is justified?

## Immediate next work

1. Merge PR #37 only after the final documentation-synchronized head passes API/Contract/Web+E2E/CodeQL/Dependency Review/Release Bundle/Release Contract/Container Security gates.
2. Start `spike/m0b-pyaterochka`: first prove the required store/catalog/product calls with ordinary HTTP and no Camoufox/stealth/proxy rotation; stop and record `UNSUITABLE_PUBLIC_PATH` if bypass techniques are required.
3. If the plain-HTTP gate passes, run the fixed 20-item grocery corpus against one Moscow store, capture sanitized fixtures, add deterministic parser/contract tests, then repeat against a second store and measure coverage plus price/availability differences.
4. Run the same harness for **Perekrestok**, and prioritize **Magnit** as the independent non-X5 proof path.
5. Continue Kuper issue #36 and second-wave Ozon Fresh/Samokat discovery in parallel.
6. Publish **`v0.1.0-rc.3`** through the real GitHub Release `published` event when a release-capable path is available, then inspect the full release/GHCR proof; do not substitute a manual tag.
7. Add `Release Bundle CI`, `Release Contract CI`, and `Container Security CI` to the `main` ruleset when the settings mutation can be applied and independently verified.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer/provider integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/usage-rights/freshness constraints. Prefer at least one non-X5 provider before entering M1 Shopping Core.
