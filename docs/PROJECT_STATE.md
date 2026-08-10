# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Universal Retailer Connectivity**  
Current focus: **make retailer connectivity transport-agnostic, keep Pyaterochka/Perekrestok mandatory, prove a non-X5 path, and design the first user-assisted browser bridge while the outstanding `v0.1.0-rc.3` release proof remains explicit**

## Product connectivity decision

The product direction is now broader than supporting a convenient subset of retailers.

**Every retailer/banner added to the target retailer registry is mandatory coverage work until at least one reproducible acquisition path is available.** A failed direct API does not remove a retailer from scope.

The accepted architecture supports multiple paths per retailer:

1. direct supported/partner API;
2. aggregator-backed observation with explicit retailer/provider provenance;
3. stable public web/API surface;
4. user-assisted first-party browser bridge.

The durable design is [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md). The X5-specific strategy remains in [`integrations/x5-mandatory-coverage.md`](integrations/x5-mandatory-coverage.md).

The initial priority registry includes Pyaterochka, Perekrestok, Chizhik, Magnit-family grocery surfaces, Lenta, VkusVill, Ozon Fresh, Samokat and relevant aggregator/provider surfaces such as Kuper. New chains must be onboarded through the same retailer registry/adaptor contract rather than retailer-specific shopping-core branches.

## Current product status

The platform foundation is executable and automatically verified. The core retailer-comparison user flow is **not implemented yet**. The current web surface intentionally presents project status rather than fake retailer cards, prices, or basket-comparison behavior.

**No retailer/provider is supported yet.** M0B is proving acquisition paths and normalized evidence before M1.

## M0B verified foundation

### Normalized offer boundary

PR #35 established `ObservedOffer`, requiring:

- provider ID;
- fulfillment-context ID;
- provider SKU;
- non-negative price;
- ISO 4217 currency;
- explicit availability (`AVAILABLE`, `UNAVAILABLE`, `UNKNOWN`);
- observation timestamp;
- source reference.

### Shared provider feasibility harness

PR #37 was squash-merged to `main` as `e318c8ee92ab5f62dd593f4fd214735eb8c59750` after the full CI/security gate.

It provides:

- `ProviderAccessType`;
- explicit provider capabilities;
- provider-scoped `LocationContext`;
- normalized `ProductQuery`;
- common `RetailerProvider` port;
- structural `FixtureRetailerProvider` / `LiveRetailerProvider` separation;
- offline `ProviderFeasibilityHarness` for fixtures only;
- explicit `ProviderLiveProbe` for network-capable paths;
- fail-closed provider/location/capability/provenance validation.

The fixture/live boundary was strengthened through a second RED→GREEN cycle after review found the original provider-supplied execution-mode flag could be misdeclared. Final PR verification proved the harness tests, complete API suite, PostgreSQL 18.4 integration and all repository CI/security gates.

## Retailer evidence

### Pyaterochka / 5ka — direct anonymous path rejected, retailer remains mandatory

PRs #39/#40/#41 implemented and instrumented a transparent JDK `HttpClient` Phase A probe.

Final live evidence on `main` SHA `73d9f18d714bd1eafc165e7f5941405a0ce10b5b`:

**`Provider Live Probe / Pyaterochka / store-403` → failure**

The first coordinate→store request returned HTTP 403. No `sapCode`, product search, PLU, price, fixture or 20-item corpus work followed.

Current interpretation:

- direct anonymous server-side path: **`DIRECT_ANONYMOUS_HTTP_UNSUITABLE`**;
- Pyaterochka product coverage: **still mandatory**;
- next accepted paths: X5/partner access, aggregator-backed coverage, or user-assisted first-party browser bridge.

### Perekrestok — direct anonymous/first-party-cookie path rejected, retailer remains mandatory

PR #44 was squash-merged to `main` as `2d827479830c9ce4946f10bf80c145efb8ec6bf3` after full CI/security verification.

Its plain JDK HTTP probe allowed only ordinary first-party cookies issued directly by Perekrestok and did not use browser-derived `Auth`.

Live evidence:

**`Provider Live Probe / Perekrestok / store-403` → failure**

The nearby-store API returned HTTP 403 before pickup selection, product search, PLU or price evidence.

Current interpretation:

- direct anonymous/ordinary-cookie server-side path: **`DIRECT_ANONYMOUS_HTTP_UNSUITABLE`**;
- Perekrestok product coverage: **still mandatory**;
- first technical fallback selected for design: user-assisted first-party browser bridge.

### Magnit — independent non-X5 public-page spike in progress

PR #46 is open on branch `spike/m0b-magnit`.

The spike targets ordinary public SSR product pages for one fixed SKU under two explicit `shopCode` contexts, without cookies/auth/API keys. Review identified and fixed a potential false-positive parser issue by associating price evidence with the expected SKU rather than accepting the first ruble value on the page.

PR #46 is **not merged yet** and no Magnit live PASS/FAIL is claimed in repository truth.

### Kuper — supported aggregator path investigation active

Issue #36 now explicitly asks whether Kuper `Client apps API` can expose Pyaterochka and Perekrestok with:

- banner provenance;
- store/fulfillment context;
- stable product identity;
- price/promotion semantics;
- availability/freshness;
- deterministic sanitized fixture rights;
- comparison/caching/deep-link requirements.

Aggregator observations must remain modeled as e.g. `sourceProvider=kuper`, `retailer=pyaterochka`, not as direct X5 observations.

## Browser-bridge design direction

The next connectivity architecture adds a Chromium extension/local browser-side path for retailer surfaces that are available to a user's ordinary first-party browser session.

The user performs login, store selection and any CAPTCHA interaction manually. The connector reads data already rendered or delivered to that browser context, preferring semantic DOM and embedded structured state.

Browser session credentials remain in the browser profile. Backend/fixtures/logs receive normalized observations only; they do not receive cookies, auth tokens, browser storage, CAPTCHA artifacts or raw private session material.

The first planned implementation slice after written-spec review is **Perekrestok Browser Bridge Phase A**, followed by Pyaterochka using the same browser-adapter contract.

## Verified platform baseline

### Backend

- Java 25;
- Spring Boot 4.1;
- Spring MVC + Virtual Threads;
- Spring Modulith architecture verification;
- PostgreSQL 18 / Testcontainers PostgreSQL 18.4;
- Flyway;
- jOOQ;
- pgJDBC `42.7.12`.

### Contracts/web

- OpenAPI 3.1 source contract;
- generated `@zakup-gotov/api-client`;
- strict generated-schema drift/type gates;
- Next.js 16.3.0;
- React 19.2.8;
- TypeScript 5.9.3;
- Node 24.18.1 build toolchain;
- distroless Node 24 Debian 13 non-root runtime;
- Vitest/Testing Library;
- Playwright desktop/mobile production-browser tests.

### Operations/security

- separate non-root API/web production images;
- PostgreSQL 18.4 → API → web release topology;
- web-only host publication by default;
- API/PostgreSQL internal networking;
- CodeQL Java + JS/TS;
- Dependency Review;
- Release Bundle/Contract CI;
- Container Security CI with HIGH/CRITICAL fail-closed scans;
- public Actuator limited to health/liveness/readiness/info.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed that defect.

`v0.1.0-rc.2` passed release verification, built/pushed staging indexes, then correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added production-image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.

## Immediate next work

1. Complete review/merge of PR #48, which records mandatory X5 coverage and the broader universal-retailer design.
2. Review and approve the written Universal Retailer Connectivity spec before executable browser-bridge implementation.
3. Write a TDD implementation plan for Perekrestok Browser Bridge Phase A.
4. Reuse the same browser-adapter/observation contract for Pyaterochka next.
5. Continue Kuper issue #36 and X5 supported-access work in parallel.
6. Resolve open Magnit PR #46 and then move successful non-X5 paths into deterministic fixture/corpus testing.
7. Continue Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional chains through the same retailer registry/path process rather than creating new core architecture.
8. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.

## Definition of M0 success

M0 is complete only when:

- Pyaterochka has at least one reproducible accepted path;
- Perekrestok has at least one reproducible accepted path;
- at least one independent non-X5 retailer has a reproducible accepted path;
- at least two acquisition modes are proven end to end;
- deterministic sanitized fixtures/tests preserve retailer/provider/store provenance;
- the registry/adapter architecture can add another chain without retailer-specific changes to shopping/basket domain logic.

Universal retailer connectivity remains the product invariant beyond M0: an unavailable registry entry is an explicit coverage blocker, not an omitted retailer.