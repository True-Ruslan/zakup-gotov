# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **complete single-store basket comparison with deterministic package/quantity selection**

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

PR #72 established canonical retailer/banner IDs for Pyaterochka, Perekrestok, Chizhik, Magnit, Lenta, VkusVill, Ozon Fresh and Samokat; explicit technical coverage state; independent production-access readiness; fail-fast registry completeness; and Kuper as provider/aggregator provenance rather than retailer identity.

### Slice 2 — shopping-list core + canonical quantities — COMPLETE

PR #73 established positive decimal quantities; `kg → g`, `l → ml`, pieces unchanged; stable UUID list/item identity and insertion order; whitespace-only requirement normalization; explicit add/replace/remove semantics; immutable item views; and deferred package/container selection.

### Slice 3 — provenance-aware provider/path orchestration — COMPLETE

PR #74 established explicit `retailerId`, `sourceProviderId`, acquisition mode and fulfillment context; deterministic fixture-only path priority `DIRECT_API → AGGREGATOR → PUBLIC_WEB → BROWSER_BRIDGE`; explicit attempt outcomes; fallback only on expected path-unavailable failures; and fail-closed provenance validation.

### Slice 4 — product location / fulfillment-context boundary — COMPLETE

PR #75 established opaque provider-neutral `ProductLocationId`; redacted `SensitiveAddress`; typed `FulfillmentContextBinding` / `FulfillmentContextSet`; explicit `MANUAL` / `RESOLVED` context provenance; and an orchestration boundary that never receives precise addresses.

### Slice 5 — price / availability snapshots — COMPLETE

PR #76 established:

- `ObservedOffer` remains the normalized provider trust-boundary record;
- `FreshnessEvidence` separates Zakup Gotov observation time from optional trusted provider-side update time;
- provider update time may equal but never exceed observation time;
- immutable `OfferSnapshot` identity derives only from validated `ObservedOffer`;
- retailer/source-provider/acquisition-mode/fulfillment-context/SKU/price/currency/availability/source-reference evidence survives exactly;
- `AVAILABLE`, `UNAVAILABLE` and `UNKNOWN` remain distinct;
- no provider-specific stale threshold, persistence, REST/UI or live acquisition was added.

### Slice 6 — deterministic product matching — COMPLETE

PR #77 establishes the first semantic matching baseline required before basket construction:

- provider `ObservedOffer` now requires a nonblank observed `productName`; there is no compatibility constructor that permits label-less observations;
- boundary whitespace is normalized at ingestion, while matching semantics never leak into provider/shopping models;
- `OfferSnapshot` preserves the validated product label exactly;
- matching owns a package-local deterministic normalizer: Unicode NFKC, `Locale.ROOT` lowercase, `ё → е`, punctuation/symbols as collapsed separators;
- the normalizer deliberately does **not** add synonyms, stemming, token reordering, transliteration, substring/edit-distance matching, embeddings or LLM behavior;
- `MatchScope` requires exactly one retailer and one fulfillment context; foreign candidates fail closed rather than being silently filtered;
- result states are explicit and structurally validated: `MATCHED`, `AMBIGUOUS`, `UNMATCHED` with `EXACT`, `NORMALIZED`, `NONE` strength and concrete reasons;
- exact text always outranks normalized text;
- multiple semantically equivalent candidates remain `AMBIGUOUS`; price, availability, freshness, acquisition mode and SKU ordering never act as hidden semantic tie-breakers;
- candidate input order is retained and result candidate lists are immutable;
- ArchUnit prevents production `provider`, `shopping` and `retailer` packages from depending back on `matching`;
- all implementation cycles and the architecture contract pass full Maven `verify` with no live retailer traffic.

Design: [`superpowers/specs/2026-08-12-m1-deterministic-matching-design.md`](superpowers/specs/2026-08-12-m1-deterministic-matching-design.md).  
Implementation plan/evidence: [`superpowers/plans/2026-08-12-m1-deterministic-matching.md`](superpowers/plans/2026-08-12-m1-deterministic-matching.md).

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
3. product location remains provider-neutral and precise addresses are redacted by default;
4. provider-specific store/fulfillment IDs remain inside provider-scoped contexts;
5. retailer, source-provider, acquisition mode and fulfillment-context provenance remain distinct;
6. `UNKNOWN` availability is preserved;
7. observation time is not misrepresented as provider-side freshness;
8. snapshots derive only from validated provider observations;
9. semantic matching never silently turns ambiguity into a winner;
10. production activation respects recorded usage-rights state;
11. universal retailer connectivity continues for every registry entry.

## Immediate next work

1. **Complete single-store basket comparison — NEXT**
   - match every shopping requirement against one retailer/context snapshot set;
   - define deterministic package/quantity selection against canonical requirement quantities;
   - keep complete vs incomplete basket state explicit;
   - define how `AVAILABLE`, `UNAVAILABLE` and `UNKNOWN` affect basket eligibility without inventing stock certainty;
   - compute deterministic totals only from selected package offers;
   - never present an incomplete/ambiguous basket as the cheapest complete basket;
   - keep delivery fees/minimum-order rules out until supported evidence exists.
2. Failure/coverage/freshness UX.
3. Critical-journey browser E2E.

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
