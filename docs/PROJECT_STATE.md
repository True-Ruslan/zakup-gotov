# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Retailer Feasibility (parallel)**  
Current focus: **classify the first failed Pyaterochka plain-HTTP Phase A run without inspecting raw retailer payloads, while keeping the still-outstanding `v0.1.0-rc.3` release proof explicit**

## Product status

The platform foundation is executable and automatically verified. The core retailer-comparison user flow is **not implemented yet**. The current web surface intentionally presents project status rather than fake retailer cards, prices, or basket comparison behavior.

M0B is now executable rather than research-only:

- PR #35 established the normalized `ObservedOffer` trust boundary;
- PR #37 established the reusable provider feasibility harness and was squash-merged to `main` as `e318c8ee92ab5f62dd593f4fd214735eb8c59750` after full CI/security verification;
- PR #39 implemented the first concrete retailer spike — a non-evasive Pyaterochka Phase A probe plus an opt-in live-probe workflow — and was squash-merged to `main` as `4efabe78f82d23fb24f58aa4c6a4a0e15cd93af0` after full CI/security verification;
- PR #40 added a narrow commit-status evidence channel and was squash-merged to `main` as `b58bd3db6881037c93854e68964ea129460339a7` after full CI/security verification;
- the first machine-readable Pyaterochka live run on that main SHA returned `failure`; PR #41 refines only the sanitized status context so connected tooling can distinguish an HTTP gate, response-shape mismatch, missing price evidence, or no-evidence/transport failure.

**No retailer/provider is supported yet.** Pyaterochka remains `SPIKE_IF_RAW_HTTP_WORKS`. The first live Phase A run is a confirmed failure; the exact sanitized category is not yet known. No browser/CAPTCHA/stealth/proxy workaround is permitted.

Starting retailer spikes does not waive the remaining M0A release requirement. `v0.1.0-rc.3` is still required before the versioned GHCR publication path can be called fully runtime-proven.

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
- Testcontainers with real PostgreSQL 18.4 integration tests.

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
- distroless Node 24 Debian 13 non-root production runtime;
- Vitest + Testing Library;
- Playwright desktop/mobile production-browser coverage.

### Operations and container topology

- separate multi-stage API/web production images;
- non-root application runtimes;
- `compose.release.yaml` contains no source `build:` directives;
- PostgreSQL 18.4 persistent storage uses `/var/lib/postgresql`;
- PostgreSQL health gates API startup and API readiness gates web startup;
- only web is host-published by default; API and PostgreSQL remain internal;
- release-bundle failures emit Compose state/log diagnostics before cleanup.

Public management HTTP remains limited to health/liveness/readiness/info. Environment, configuration-properties and metrics Actuator endpoints remain HTTP-inaccessible. Request-detail logging is disabled by default.

## Automated verification

Repository checks currently include:

- **API CI**;
- **Contract CI**;
- **Web CI**;
- **Web E2E**;
- **CodeQL / Java**;
- **CodeQL / JavaScript-TypeScript**;
- **Dependency Review**;
- **Release Bundle CI**;
- **Release Contract CI**;
- **Container Security CI** for API and web production images;
- local `./scripts/verify.sh`;
- local/CI `./scripts/verify-release-bundle.sh`.

The independently verified `main` ruleset still enforces the original seven required checks. `Release Bundle CI`, `Release Contract CI`, and `Container Security CI` are proven required-check candidates but have not yet been independently verified as enforced ruleset checks.

## M0B provider architecture

### Normalized offer boundary

`ObservedOffer` requires:

- provider ID;
- fulfillment-context ID;
- provider SKU;
- non-negative price;
- ISO 4217 currency;
- explicit availability (`AVAILABLE`, `UNAVAILABLE`, `UNKNOWN`);
- observation timestamp;
- source reference.

Incomplete or inconsistent external data fails closed before later comparison logic.

### Shared feasibility harness

Merged PR #37 provides:

- `ProviderAccessType`: `OFFICIAL_API`, `PUBLIC_UNOFFICIAL_API`, `PARTNER_API`;
- explicit capabilities for location, catalog, search, price and availability;
- provider-scoped `LocationContext`;
- normalized `ProductQuery`;
- common `RetailerProvider` port;
- structural `FixtureRetailerProvider` / `LiveRetailerProvider` separation;
- `ProviderFeasibilityHarness.offline()` accepting fixture providers only;
- separate `ProviderLiveProbe` for network-capable providers;
- validation requiring `PRODUCT_SEARCH` + `PRICE` and matching provider/fulfillment provenance.

PR #37 was developed in two TDD cycles. The first implementation passed but review found that a provider-supplied fixture/live enum could be misdeclared; a second RED→GREEN cycle replaced it with the structural type split. Final backend verification on the PR head proved `ProviderFeasibilityHarnessTest` **7/7**, `ObservedOfferTest` **10/10**, total API suite **22/22**, real PostgreSQL 18.4 and packaged JAR build. The full PR workflow set, including Web E2E, CodeQL, Release Bundle, Release Contract, Dependency Review and both production-image security scans, passed before merge.

## Public unofficial API policy

An undocumented consumer backend is not rejected solely because it is unofficial. It may be evaluated as `PUBLIC_UNOFFICIAL_API` only when the required path works without:

- stolen/forged private credentials;
- another user's session;
- CAPTCHA solving or bypass;
- anti-bot/access-control circumvention;
- browser-fingerprint evasion;
- proxy/IP rotation used to defeat blocking;
- retry behavior intended to bypass `403`/`429` or similar provider decisions.

Rate limits and provider failures are evidence, not obstacles to hide. Third-party wrappers are research references only; their source-code license does not grant rights to the retailer's underlying data.

Detailed evidence is in [`integrations/retailer-feasibility.md`](integrations/retailer-feasibility.md). The provider sequence and fixed 20-item corpus are in [`superpowers/plans/2026-08-10-m0b-provider-spikes.md`](superpowers/plans/2026-08-10-m0b-provider-spikes.md).

## Pyaterochka / 5ka Phase A

Tracking: issue #38; probe/workflow implementation merged in PR #39; machine-readable status channel merged in PR #40; sanitized outcome-category refinement is PR #41.

Current research identifies the consumer-backend base `https://5d.5ka.ru/api` and these hypotheses:

- coordinate-based store lookup;
- store identifier `sapCode`;
- `{sapCode}`-scoped catalog/search/product routes;
- product PLU identity;
- price-bearing search/product responses.

The current Open-Inflation reference obtains these calls through a Camoufox browser warm-up, optional robot/CAPTCHA interaction, and captured `x-app-version`, `x-device-id`, and `x-platform` request headers. **Zakup Gotov does not inherit those techniques.** A separate direct request to the public 5ka catalog surface also returned HTTP 403 during research, so raw HTTP viability must be tested rather than assumed.

PR #39 implements a separate JDK `HttpClient` Phase A probe with:

- only `Accept` and a transparent Zakup Gotov `User-Agent`;
- fixed connect/request timeouts;
- no cookies or authorization;
- no captured app/device/platform headers;
- no browser automation;
- no proxies;
- no retry/evasion loop;
- store lookup → search only, stopping at the first failed gate;
- sanitized evidence only: status codes plus booleans for `sapCode`, PLU and price evidence.

TDD and merge evidence:

- RED commit `099cb2ace80e7492b9ed0279f82420ec39106fd4`: API CI failed at `testCompile` only because `PyaterochkaPlainHttpProbe` did not yet exist;
- GREEN implementation commit `e91e6bfab20d5a700cbb08fc0c2b1a8193f82a3b`: `PyaterochkaPlainHttpProbeTest` ran **3 tests, 0 failures/errors, 1 intentionally skipped live test**; complete API suite ran **25 tests, 0 failures/errors, 1 skipped**; PostgreSQL 18.4 and JAR build passed;
- final PR #39 head passed API, Contract, Web + Web E2E, CodeQL, Dependency Review, Release Bundle, Release Contract and both production-image Container Security scans before squash merge;
- PR #40 kept `contents: read`, added only `statuses: write`, passed the same complete repository gate, and enabled a retrievable sanitized live status on the default-branch SHA.

The opt-in `Provider Live Probe` workflow is not part of ordinary PR CI. It has no retailer credentials/secrets. It uses GitHub's ephemeral token only for the commit-status write and publishes no response body, exact store ID or exact product ID.

The first machine-readable run after PR #40 produced legacy status state `failure` on `main` SHA `b58bd3db6881037c93854e68964ea129460339a7`. The connected status reader omits the legacy `description`, so that result proves **Phase A did not pass** but cannot distinguish why. PR #41 changes no retailer request and adds no permission; it maps the existing sanitized evidence into a finite context suffix:

- `pass`;
- `store-<HTTP status>`;
- `store-shape`;
- `search-<HTTP status>`;
- `search-shape`;
- `price-missing`;
- `no-evidence`;
- `failed`.

Context format becomes `Provider Live Probe / Pyaterochka / <outcome>`. No payload, store ID, PLU, address, credential or user-controlled text enters that context.

Current result: **Phase A failed once; sanitized failure category is pending the post-PR-#41 rerun.** No fixtures/20-item corpus are justified unless the rerun passes.

## Current retailer priorities

- **Pyaterochka / 5ka** — `SPIKE_IF_RAW_HTTP_WORKS`; first Phase A run failed, exact failure category pending.
- **Perekrestok** — `SPIKE_NOW`; next controlled provider spike.
- **Magnit** — `SPIKE_NOW`; priority independent non-X5 proof path.
- **Chizhik** — `SPIKE_IF_RAW_HTTP_WORKS`; reject if browser/proxy evasion is required.
- **Ozon Fresh / Samokat** — `RESEARCH_REQUIRED`.
- **Kuper** — `PROMISING_CONTACT_REQUIRED`; issue #36 tracks official Client apps API access/rights questions.
- **Yandex Eats Retail API** — `PARTNER_SIDE_ONLY` for the currently documented integration direction.
- **Lenta / VkusVill** — `BLOCKED_WITHOUT_AGREEMENT` under the current evidence.

## Release-engineering state

### `v0.1.0-rc.1`

The real release event proved metadata/main ancestry, repository verification, web build and Playwright, then failed because release helper scripts were not executable on a clean checkout. PR #28 fixed that defect with a regression test.

### `v0.1.0-rc.2`

`Release / Verify` passed and `Release / Publish` reached GHCR staging multi-platform builds. Publication then failed closed at the first Trivy gate on pgJDBC `42.7.11` / `CVE-2026-54291` (`HIGH`). Follow-up testing also exposed HIGH/CRITICAL findings in the previous Node Bookworm-slim web runtime. PR #29 updated pgJDBC to `42.7.12`, moved final web runtime to distroless Node 24 Debian 13/non-root, and added ordinary PR/main/daily production-image security scans without weakening Trivy policy.

`rc.2` therefore does **not** prove final package promotion, attestations, SemVer OCI tags, final digest smoke, release evidence assets or final GHCR visibility.

### Outstanding `v0.1.0-rc.3`

A successful real GitHub Release `published` event is still required to prove the complete staged build → scan/SBOM → digest smoke → no-rebuild final copy → final digest smoke → attestation → SemVer promotion → evidence path. The current assistant GitHub integration does not expose GitHub Release creation, so this proof remains explicitly outstanding rather than being replaced with a manual tag.

## Dependency maintenance state

Known incompatible major updates remain deferred rather than bypassing checks:

- TypeScript 7.0.2 vs current `openapi-typescript` peer range;
- ESLint 10 vs current lint configuration;
- `@types/node` 26 while runtime/toolchain remain Node 24.

## Immediate next work

1. Finish PR #41 with the complete repository gate and final read-only review, then squash-merge it.
2. Re-trigger exactly `/provider-probe pyaterochka` on issue #38.
3. Read the outcome-bearing `Provider Live Probe / Pyaterochka / <outcome>` status on the new `main` SHA.
4. If outcome is `pass`, record sanitized fixtures and run the fixed 20-item corpus against two stores.
5. If outcome shows a guarded HTTP path (`store-403`, `search-403`, etc.), record `UNSUITABLE_PUBLIC_PATH` and move on without bypassing it; if it shows a response-shape or transport problem, investigate only the minimum public/non-evasive evidence needed to distinguish API drift from access control.
6. Start Perekrestok and prioritize Magnit as the independent non-X5 path.
7. Continue Kuper issue #36 and second-wave Ozon Fresh/Samokat research in parallel.
8. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.
9. Add Release Bundle/Contract/Container Security to the `main` ruleset when the settings mutation can be applied and independently verified.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer/provider integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/usage-rights/freshness constraints. Prefer at least one non-X5 provider before entering M1 Shopping Core.
