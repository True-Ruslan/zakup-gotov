# Project State

Updated: 2026-08-11

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0B Universal Retailer Connectivity**  
Current focus: **prove an independent non-X5 retailer path and a second acquisition mode, then make the M0 → M1 go/no-go decision**

## Product connectivity invariant

PR #48 was squash-merged to `main` as `f72c2f9b5f6c631b8dc0ed135d60e69ec55d7d90` and records the approved Universal Retailer Connectivity design.

**Every retailer/banner added to the target retailer registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists.** A failed direct API changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Supported acquisition modes are:

1. direct supported/partner API;
2. aggregator-backed observations with explicit retailer/provider provenance;
3. stable public web/API surfaces;
4. user-assisted first-party browser bridge.

The initial priority registry includes Pyaterochka, Perekrestok, Chizhik, Magnit-family grocery surfaces, Lenta, VkusVill, Ozon Fresh, Samokat and relevant aggregator/provider surfaces such as Kuper.

## Current product status

The platform foundation is executable and automatically verified. The core multi-retailer basket-comparison user flow is **not implemented yet**. The web surface intentionally does not fake retailer prices or availability.

**Both mandatory X5 banners now have accepted page-snapshot acquisition paths:**

- **Perekrestok:** `AVAILABLE_BROWSER_BRIDGE`, adapter v2;
- **Pyaterochka:** `AVAILABLE_BROWSER_BRIDGE`, adapter v1.

The retailer-bridge maintenance threshold is now satisfied: `apps/retailer-bridge` is a first-class pnpm workspace importer with its own pinned TypeScript/Vitest/jsdom/Playwright toolchain and no `apps/web/node_modules` coupling.

The remaining M0 blockers are:

- at least one independent non-X5 retailer with a reproducible accepted path;
- at least two distinct acquisition modes proven end to end;
- preservation of explicit retailer/provider/store provenance and deterministic sanitized verification as coverage expands.

## M0B verified foundation

PR #35 established the normalized `ObservedOffer` trust boundary.

PR #37 was squash-merged to `main` as `e318c8ee92ab5f62dd593f4fd214735eb8c59750` and established the reusable provider feasibility harness: provider capabilities, provider-scoped `LocationContext`, normalized `ProductQuery`, structural fixture/live provider separation, offline fixture verification and explicit live-probe entry points.

PR #48 established Universal Retailer Connectivity as a registry invariant rather than a best-effort set of easy integrations.

## Retailer evidence

### Pyaterochka / 5ka direct path

Final direct live evidence on `main` SHA `73d9f18d714bd1eafc165e7f5941405a0ce10b5b`:

**`Provider Live Probe / Pyaterochka / store-403` → failure**

Interpretation:

- direct anonymous server-side path: `DIRECT_ANONYMOUS_HTTP_UNSUITABLE`;
- Pyaterochka product coverage: still mandatory;
- selected fallback: user-assisted first-party browser bridge;
- supported X5/partner access and aggregator-backed coverage remain valid parallel tracks.

### Pyaterochka Browser Bridge Phase A — LIVE PASS

PR #58 was squash-merged to `main` as `95e83c1c2d3e8217de10bf9c2bb160735ba17f94`.

Deterministic implementation:

- production MV3 routing includes `https://5ka.ru/*` and `https://www.5ka.ru/*` while keeping `storage` as the only extension permission;
- `pyaterochkaBrowserAdapter` uses `retailerId=pyaterochka`, `sourceProviderId=pyaterochka-browser`, `adapterVersion=1`;
- product identity is derived only from official `/product/<slug>--<numeric-id>/` links;
- product-local visible RUB price is normalized to integer `priceMinor`;
- catalog DOM emits `availability=UNKNOWN` unless a supported stock semantic is actually present;
- fulfillment context is accepted only from canonical `https://5d.5ka.ru/api/catalog/v2/stores/<store-id>/...` resource pathname metadata;
- runtime rejects other 5d paths, lookalike origins and cross-retailer use;
- query strings/fragments are removed before resource evidence reaches adapters;
- the retailer-neutral adapter registry contains Perekrestok and Pyaterochka;
- shared `PerformanceObserver` + `MutationObserver` behavior handles late store context and late product DOM;
- fail-closed stale clearing remains in place;
- no cookies, tokens, request headers, response bodies, arbitrary storage values or raw production HTML are captured.

TDD proof is recorded in [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md).

Key executable GREEN head before final documentation synchronization: `73b73f1049c9e3b5f6c5000644ead07e1b0754b4`:

- 23 unit tests PASS;
- TypeScript PASS;
- production bridge build PASS;
- all 3 persistent-Chromium E2E scenarios PASS;
- Pyaterochka async cross-origin store-context + delayed-DOM scenario PASS;
- resource-query and browser-cookie sentinels do not reach extension storage.

#### Real first-party browser gate — 2026-08-11

The extension rebuilt from merged `main` SHA `95e83c1c2d3e8217de10bf9c2bb160735ba17f94` was exercised on the official Pyaterochka catalog in a normal Chromium-compatible first-party browser profile after normal user-controlled store/location selection and a full reload.

Sanitized result:

- `data-zg-bridge-status = ok`;
- normalized observation count `12`;
- retailer IDs: exactly `pyaterochka`;
- provider IDs: exactly `pyaterochka-browser`;
- adapter versions: exactly `1`;
- exactly one nonblank fulfillment context;
- normalized validation failures: `0`.

Result: **PASS**.

Current Pyaterochka decision: **`AVAILABLE_BROWSER_BRIDGE`** for reload-based page-snapshot acquisition through the user-assisted first-party browser transport.

Evidence:

- Phase A/TDD/security decision: [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md);
- final live PASS: [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md).

### Perekrestok direct path

PR #44 was squash-merged to `main` as `2d827479830c9ce4946f10bf80c145efb8ec6bf3`.

Its ordinary first-party-cookie HTTP path returned:

**`Provider Live Probe / Perekrestok / store-403` → failure**

No browser-derived `Auth` was exported or replayed. The direct anonymous/ordinary-cookie server-side path remains unsuitable.

### Perekrestok Browser Bridge Phase A — LIVE PASS

PR #49 was squash-merged to `main` as `333ad5d6ffbdcce3622a587b09004690afbe8e60`. It established the Chromium Manifest V3 bridge, normalized observation boundary, sanitized extension-local storage, fail-closed stale-data clearing, deterministic fixtures and persistent-Chromium E2E.

The first real browser gate on 2026-08-10 exposed an adapter-v1 mismatch. PR #53 then introduced adapter v2, using current semantic `.product-card` DOM plus same-origin shop-resource evidence.

PR #53 was squash-merged to `main` as `218c96def777622ab66f1f8663f0466e35a9d804`; its exact final head passed the complete repository workflow/security gate.

Repeated real-browser v2 evidence on 2026-08-11:

- bridge status `ok`;
- 90 normalized observations;
- adapter version exactly `2`;
- exactly one fulfillment context;
- zero invalid observations under the acceptance predicate;
- canonical source references without query/hash.

Result: **PASS**.

Current Perekrestok decision: **`AVAILABLE_BROWSER_BRIDGE`** for reload-based page-snapshot acquisition.

Evidence:

- initial live failure/root cause: [`integrations/perekrestok-browser-bridge-live-2026-08-10.md`](integrations/perekrestok-browser-bridge-live-2026-08-10.md);
- final live PASS: [`integrations/perekrestok-browser-bridge-live-2026-08-11.md`](integrations/perekrestok-browser-bridge-live-2026-08-11.md);
- Phase A decision/procedure: [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md).

Issue #54 remains a non-blocking lifecycle-hardening item: after the first successful snapshot, same-document store changes or SPA navigation are not yet automatically refreshed. The accepted current path therefore assumes intended store selection followed by page reload.

### Magnit

PR #46 remains the nearest independent non-X5 path candidate using public SSR product pages under explicit `shopCode` contexts without cookies/auth/API keys.

Its current branch is **not merge-ready** because `API CI` is failing. The failure must be diagnosed against current `main` before the path can be accepted. No support claim is made until the complete evidence path and CI gate pass.

### Kuper

Issue #36 remains active for supported aggregator-backed access. Any Kuper observation must preserve aggregator provider provenance separately from the underlying retailer/banner identity.

Kuper remains strategically important because an accepted aggregator-backed path could satisfy the outstanding second-acquisition-mode criterion while broadening retailer coverage.

## Retailer Bridge workspace — maintenance gate satisfied

Issue #50 tracked the temporary tooling debt created when early bridge work reused TypeScript/Vitest/Playwright from `apps/web`.

PR #60 resolves that boundary without changing retailer behavior:

- `apps/retailer-bridge` is registered in the root pnpm workspace;
- the bridge owns explicit pinned devDependencies for `typescript`, `vitest`, `jsdom`, `@playwright/test` and `@types/node`;
- package scripts execute the bridge-owned toolchain directly;
- `build.mjs` resolves TypeScript from the bridge package rather than `apps/web`;
- bridge E2E imports `@playwright/test` normally rather than through `apps/web/node_modules`;
- Retailer Bridge CI installs Playwright through the bridge importer;
- `pnpm-lock.yaml` was regenerated normally with pinned pnpm `11.4.0` and contains an explicit `apps/retailer-bridge` importer;
- frozen install continues to enforce the repository supply-chain policy;
- unit tests, typecheck, production build and persistent-Chromium E2E all pass after the refactor.

TDD/refactoring evidence:

- RED: the intentionally stale lockfile failed `pnpm install --frozen-lockfile` with `ERR_PNPM_OUTDATED_LOCKFILE` and named exactly the five newly owned bridge dependencies;
- GREEN: pnpm `11.4.0` regenerated the lockfile, a fresh frozen install passed, and the complete bridge behavior suite returned green.

No adapter, manifest, observation-model, resource-policy or permission behavior is changed by this maintenance gate.

## Verified platform baseline

### Backend

- Java 25;
- Spring Boot 4.1;
- Spring MVC + Virtual Threads;
- Spring Modulith verification;
- PostgreSQL 18 / Testcontainers PostgreSQL 18.4;
- Flyway;
- jOOQ;
- pgJDBC `42.7.12`.

### Contracts/web

- OpenAPI 3.1;
- generated `@zakup-gotov/api-client`;
- generated-schema drift/type gates;
- Next.js 16.3.0;
- React 19.2.8;
- TypeScript 5.9.3;
- Node 24.18.1;
- distroless Node 24 Debian 13 non-root runtime;
- Vitest/Testing Library;
- Playwright production-browser tests.

### Operations/security

- separate non-root API/web production images;
- PostgreSQL 18.4 → API → web release topology;
- CodeQL Java + JS/TS;
- Dependency Review;
- Release Bundle/Contract CI;
- Container Security CI with HIGH/CRITICAL fail-closed scans;
- first-class `apps/retailer-bridge` pnpm workspace importer;
- `Retailer Bridge CI` with frozen install plus bridge-owned unit/type/build/persistent-Chromium E2E verification;
- public Actuator limited to health/liveness/readiness/info.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed it.

`v0.1.0-rc.2` passed release verification, built/pushed staging indexes, then correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.

## Immediate next work

1. Resolve at least one independent non-X5 retailer path, with Magnit PR #46 currently the nearest existing candidate after its failing API CI is diagnosed/fixed against current `main`.
2. Prove a second accepted acquisition mode so M0 does not depend only on browser-assisted acquisition; Kuper/another supported aggregator or a stable public-web path are preferred candidates.
3. Run additional Perekrestok/Pyaterochka corpus/context validation as hardening and product-quality evidence; neither is now a Phase A connectivity blocker.
4. Resolve issue #54 before treating the browser bridge as a persistent-session transport across post-success store changes / SPA navigation.
5. Continue Kuper/X5 supported-access work in parallel.
6. Continue Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional chains through the same registry/adapter process.
7. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.
8. Once the non-X5 and second-mode criteria are satisfied, make the explicit M0 → M1 go/no-go decision instead of starting Shopping Core prematurely.

## Definition of M0 success

M0 is complete only when:

- Pyaterochka has at least one reproducible accepted path — **satisfied via `AVAILABLE_BROWSER_BRIDGE`**;
- Perekrestok has at least one reproducible accepted path — **satisfied via `AVAILABLE_BROWSER_BRIDGE`**;
- at least one independent non-X5 retailer has a reproducible accepted path — **outstanding**;
- at least two acquisition modes are proven end to end — **outstanding; browser bridge is currently the only accepted mode**;
- deterministic sanitized fixtures/tests preserve retailer/provider/store provenance;
- the registry/adapter architecture can add another chain without retailer-specific changes to shopping/basket domain logic.

Universal retailer connectivity remains the product invariant beyond M0: an unavailable registry entry is an explicit coverage blocker, not an omitted retailer.
