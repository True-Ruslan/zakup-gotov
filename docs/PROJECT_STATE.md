# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Retailer Feasibility (parallel)**  
Current focus: **move from the rejected Pyaterochka public path to Perekrestok and Magnit feasibility, while keeping the still-outstanding `v0.1.0-rc.3` release proof explicit**

## Product status

The platform foundation is executable and automatically verified. The core retailer-comparison user flow is **not implemented yet**. The current web surface intentionally presents project status rather than fake retailer cards, prices, or basket comparison behavior.

M0B is now executable rather than research-only:

- PR #35 established the normalized `ObservedOffer` trust boundary;
- PR #37 established the reusable provider feasibility harness and was squash-merged to `main` as `e318c8ee92ab5f62dd593f4fd214735eb8c59750` after full CI/security verification;
- PR #39 implemented the first concrete retailer spike — a non-evasive Pyaterochka Phase A probe plus an opt-in live-probe workflow — and was squash-merged to `main` as `4efabe78f82d23fb24f58aa4c6a4a0e15cd93af0` after full CI/security verification;
- PR #40 added a narrow commit-status evidence channel and was squash-merged to `main` as `b58bd3db6881037c93854e68964ea129460339a7` after full CI/security verification;
- PR #41 added a finite sanitized outcome suffix to the live status and was squash-merged to `main` as `73d9f18d714bd1eafc165e7f5941405a0ce10b5b` after the complete repository gate;
- the outcome-bearing Pyaterochka live run on that main SHA returned **`Provider Live Probe / Pyaterochka / store-403`**.

**No retailer/provider is supported yet.** The currently known Pyaterochka/5ka public consumer path is now classified **`UNSUITABLE_PUBLIC_PATH`**: the first coordinate→store request was rejected with HTTP 403 by an ordinary transparent HTTP client. The spike stopped before store ID, product search, PLU, price, fixtures or corpus work. We will not add browser/CAPTCHA/stealth/proxy techniques to bypass that result.

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

## Pyaterochka / 5ka Phase A — CLOSED

Tracking: issue #38. Implementation/evidence: PRs #39, #40, #41.

Research identified the consumer-backend base `https://5d.5ka.ru/api`, coordinate-based store lookup, `{sapCode}`-scoped catalog/search/product routes and PLU identity. The existing Open-Inflation reference reaches those calls after Camoufox browser warm-up, optional robot/CAPTCHA interaction and captured browser/app headers. Zakup Gotov deliberately did not inherit that behavior.

The independent JDK `HttpClient` probe used only:

- `Accept: application/json`;
- transparent Zakup Gotov `User-Agent`;
- fixed connect/request timeouts;
- no Cookie/Authorization;
- no captured app/device/platform headers;
- no browser automation;
- no proxies;
- no retry/evasion loop.

TDD/verification evidence for the probe itself:

- RED commit `099cb2ace80e7492b9ed0279f82420ec39106fd4` failed at `testCompile` only because `PyaterochkaPlainHttpProbe` did not exist;
- GREEN commit `e91e6bfab20d5a700cbb08fc0c2b1a8193f82a3b` produced `PyaterochkaPlainHttpProbeTest` **3 tests, 0 failures/errors, 1 intentionally skipped live test** and the complete API suite **25 tests, 0 failures/errors, 1 skipped**;
- PR #39 passed API, Contract, Web + Web E2E, CodeQL, Dependency Review, Release Bundle, Release Contract and both production-image Container Security scans before merge;
- PRs #40/#41 changed only the sanitized evidence transport and passed the complete repository gates before merge.

Final live evidence on `main` SHA `73d9f18d714bd1eafc165e7f5941405a0ce10b5b`:

**`Provider Live Probe / Pyaterochka / store-403` → failure**

The first coordinate→store lookup returned HTTP 403. No `sapCode`, product search, PLU, price, fixture, or 20-item corpus request was made after that gate.

Decision: **`UNSUITABLE_PUBLIC_PATH`** for the currently known consumer backend. Pyaterochka may be reconsidered only if an acceptable documented API/partner path or a genuinely public non-evasive consumer path becomes available.

## Current retailer priorities

- **Pyaterochka / 5ka** — `UNSUITABLE_PUBLIC_PATH`; stopped after `store-403`, no bypass.
- **Perekrestok** — `SPIKE_NOW`; **next implementation target**.
- **Magnit** — `SPIKE_NOW`; high-priority independent non-X5 path, ideally in parallel/immediately after Perekrestok Phase A.
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

1. Merge the documentation-only Pyaterochka result synchronization after the full repository gate.
2. Close issue #38 as the currently known public path is rejected by Phase A.
3. Start `spike/m0b-perekrestok` with the same plain-HTTP/non-evasive Phase A discipline.
4. Start or immediately follow with `spike/m0b-magnit` so M0 gains an independent non-X5 candidate.
5. Only providers that pass Phase A proceed to sanitized fixtures and the fixed 20-item corpus across two stores/contexts.
6. Continue Kuper issue #36 and second-wave Ozon Fresh/Samokat research in parallel.
7. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.
8. Add Release Bundle/Contract/Container Security to the `main` ruleset when the settings mutation can be applied and independently verified.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer/provider integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/usage-rights/freshness constraints. Prefer at least one non-X5 provider before entering M1 Shopping Core.
