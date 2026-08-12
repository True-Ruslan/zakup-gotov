# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **price / availability snapshot model with explicit provenance and freshness semantics**

## Product connectivity invariant

Universal Retailer Connectivity remains a permanent product rule beyond M0:

> Every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Accepted acquisition-mode families are supported/direct API, aggregator-backed observations, stable public web/API surfaces and user-assisted first-party browser bridge.

## M0 exit status

All technical M0 exit gates remain satisfied:

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 accepted technical path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts |
| Two distinct acquisition modes | **PASS** | Browser bridge + ordinary public web |
| Deterministic sanitized verification | **PASS** | Bridge fixtures/E2E + Magnit Phase B fixtures/regressions |
| Retailer-neutral connectivity boundary | **PASS** | provider harness, normalized observations and canonical retailer registry |

M0 completion is a **technical feasibility decision**, not blanket production-data-access approval.

## M1 delivered foundations

### Slice 1 — canonical retailer registry — COMPLETE

PR #72 established:

- canonical retailer/banner IDs for Pyaterochka, Perekrestok, Chizhik, Magnit, Lenta, VkusVill, Ozon Fresh and Samokat;
- explicit `RetailerCoverageState`;
- independent `ProductionAccessStatus` so technical feasibility cannot silently become production activation;
- immutable entries, stable product order and fail-fast registry completeness;
- Kuper remains acquisition-provider/aggregator provenance rather than retailer identity.

### Slice 2 — shopping-list core + canonical quantities — COMPLETE

PR #73 established:

- positive decimal `Quantity` values;
- `kg → g`, `l → ml`, pieces unchanged;
- stable UUID list/item identity and insertion order;
- whitespace-only requirement normalization;
- explicit add/replace/remove semantics with duplicate/unknown IDs rejected;
- immutable item views;
- package/container selection and duplicate-name merging intentionally deferred to later matching/planning layers.

### Slice 3 — provenance-aware provider/path orchestration — COMPLETE

PR #74 established:

- `ObservedOffer` preserves `retailerId`, `sourceProviderId` and `sourceMode` independently from fulfillment context, SKU, price, availability, observation time and source reference;
- `AcquisitionMode`: `DIRECT_API`, `AGGREGATOR`, `PUBLIC_WEB`, `BROWSER_BRIDGE`;
- `RetailerProvider` declares retailer, source-provider and acquisition mode explicitly;
- deterministic fixture-only priority `DIRECT_API → AGGREGATOR → PUBLIC_WEB → BROWSER_BRIDGE` with stable source-provider tie-break;
- explicit attempt states for missing capabilities, missing context, expected path failure and success;
- fallback only on `ProviderPathUnavailableException`;
- successful empty search does not silently mix a lower-priority source;
- unexpected provider defects propagate;
- trust validation rejects retailer/source-provider/source-mode/fulfillment-context mismatch.

### Slice 4 — product location / fulfillment-context boundary — COMPLETE

PR #75 establishes the privacy and modularity boundary required before snapshots and comparison:

- provider-neutral `ProductLocationId` is an opaque UUID identity;
- `ProductLocation` contains only product location identity, normalized locality and optional `SensitiveAddress`;
- `SensitiveAddress` exposes the raw value only through explicit `reveal()` and always renders as `[REDACTED]` by default;
- `ProductLocation.toString()` never renders the precise address;
- ArchUnit enforces that production `..location..` classes do not depend on `..provider..` or `..retailer..` packages;
- `FulfillmentContextBinding` links an opaque `ProductLocationId` to a source-provider-scoped `LocationContext` without storing `ProductLocation` or precise address;
- binding provenance distinguishes `MANUAL` and `RESOLVED` context selection without claiming that automatic resolution exists for every provider;
- `FulfillmentContextSet` preserves stable binding order, rejects bindings for another product location and rejects duplicate source-provider contexts;
- `ProviderPathOrchestrator` now consumes `FulfillmentContextSet`; the raw `Map<String, LocationContext>` boundary was removed;
- orchestration has no `ProductLocation` or `SensitiveAddress` parameter, so provider routing cannot accidentally receive/log a precise user address;
- all previous priority, fallback, capability and provenance-validation behavior remains unchanged.

The slice was delivered through three explicit RED→GREEN cycles. Ordinary CI still makes no live retailer requests.

## Accepted retailer paths

### Pyaterochka

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter `pyaterochka-browser` v1. The real first-party browser gate produced 12 normalized observations, one fulfillment context and zero normalized validation failures. The direct anonymous server path remains unsuitable (`store-403`).

Evidence:

- [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md);
- [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md).

### Perekrestok

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter v2. Repeated real first-party browser evidence produced 90 normalized observations, one fulfillment context and zero acceptance-validation failures.

Issue #54 remains lifecycle hardening for same-document store changes / SPA navigation; accepted page-snapshot operation assumes intended store selection followed by reload.

### Magnit

Status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context technical feasibility**.

Final Phase B evidence proved 20/20 HTTP and usable SKU/current-price observations in each of two explicit public `shopCode` contexts, stable identity 20/20 and zero failed requirements while availability remains `UNKNOWN` when stock semantics are not proven.

Production constraints remain explicit:

- **#69** — automatic location/address → public `shopCode` resolution is not proven;
- **#70** — recurring production catalog acquisition usage rights are `UNRESOLVED`.

M1 must not enable default recurring Magnit production polling until #70 reaches an authoritative `ACCEPTABLE` decision.

Evidence:

- [`integrations/magnit-phase-b.md`](integrations/magnit-phase-b.md);
- [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md).

## M1 invariants

1. shopping/basket logic runs deterministically over fixtures;
2. retailer coverage remains explicit and unavailable paths are never silently omitted;
3. product location remains provider-neutral;
4. precise addresses are sensitive and redacted by default;
5. provider-specific store/fulfillment IDs remain inside provider-scoped contexts;
6. retailer, source-provider, acquisition mode and fulfillment-context provenance remain distinct;
7. `UNKNOWN` availability is preserved;
8. observation time is not misrepresented as provider-side freshness;
9. production activation respects recorded usage-rights state;
10. universal retailer connectivity continues for every registry entry.

## Immediate next work

1. **Price / availability snapshots — NEXT**
   - define immutable snapshot identity/value boundary;
   - preserve retailer, source-provider, acquisition mode and fulfillment context;
   - preserve explicit `AVAILABLE` / `UNAVAILABLE` / `UNKNOWN`;
   - distinguish observation time from provider-side freshness/update time;
   - reject invalid currency/price/timestamps/provenance fail-closed;
   - remain fixture-first and independent from live retailer access.
2. Deterministic product-matching baseline.
3. Complete single-store basket comparison.
4. Coverage/failure/freshness UX and critical browser E2E.

## Parallel open work

- #36 — Kuper supported aggregator access;
- #54 — browser bridge persistent-session lifecycle hardening;
- #69 — Magnit location → public `shopCode` resolution;
- #70 — Magnit production usage-rights decision;
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and other mandatory retailer onboarding;
- successful real `v0.1.0-rc.3` release-pipeline proof.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers 18.4, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed it.

`v0.1.0-rc.2` passed release verification and correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
