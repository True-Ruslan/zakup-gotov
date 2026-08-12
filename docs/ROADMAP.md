# Roadmap

Updated: 2026-08-12

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner added to the target registry remains mandatory coverage work until at least one reproducible acquisition path is available.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence remains:
- Perekrestok `AVAILABLE_BROWSER_BRIDGE` v2;
- Pyaterochka `AVAILABLE_BROWSER_BRIDGE` v1;
- Magnit `AVAILABLE_PUBLIC_WEB` for explicit `shopCode` contexts;
- browser bridge + public web acquisition modes proven;
- deterministic sanitized verification and retailer-neutral architecture proven.

M0 completion is technical feasibility, not production access clearance. #69, #70 and #54 remain explicit constraints/hardening work.

## M1 — Shopping Core — CURRENT

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage, location/context, provenance, freshness, package evidence and uncertainty.

### Entry rules

- fixture/evidence-first core;
- unavailable/blocked retailers remain visible;
- exact addresses redacted by default;
- retailer/source-provider/acquisition-mode/fulfillment context remain distinct;
- `UNKNOWN` availability remains first-class;
- observation time is not provider-update time;
- freshness evidence basis remains explicit and structurally valid;
- package quantity is explicit structured evidence, never inferred from product names or presentation text;
- production activation respects usage-rights state;
- ordinary M1 tests make no live retailer requests.

### Implementation sequence

1. **Retailer registry + coverage-state model — COMPLETE (#72)**
2. **Shopping-list aggregate + canonical quantities/units — COMPLETE (#73)**
3. **Provider/path orchestration over deterministic fixtures — COMPLETE (#74)**
4. **Location / fulfillment-context boundary — COMPLETE (#75)**
5. **Price and availability snapshots — COMPLETE (#76)**
6. **Deterministic product matching — COMPLETE (#77)**
   - observed product labels preserved through snapshots;
   - matching-only deterministic normalization;
   - retailer/context-scoped exact-before-normalized matching;
   - explicit matched/ambiguous/unmatched states;
   - no semantic tie-break by price/availability/freshness/SKU and no fuzzy/AI baseline.
7. **Complete single-store basket comparison — COMPLETE (#78)**
   - explicit package-quantity evidence keyed by snapshot identity;
   - no package-size parsing from product names;
   - canonical unit-safe whole-package selection;
   - explicit item outcomes and `COMPLETE`, `UNCERTAIN`, `INCOMPLETE` basket states;
   - unknown availability cannot become a confirmed complete basket;
   - incomplete baskets expose no aggregate total;
   - mixed selected currencies fail closed.
8. **Failure / coverage / freshness product + API + UX boundary — COMPLETE (#79)**
   - product-facing retailer comparison/readiness model;
   - all eight canonical retailers remain visible in stable registry order;
   - technical coverage and production-access readiness map independently;
   - explicit `READY`, `UNCERTAIN`, `INCOMPLETE`, `UNAVAILABLE` states and finite product-safe reasons;
   - public records reject impossible status/coverage/access/total/freshness combinations;
   - provider-path failures translate without provider ID/acquisition/source leakage;
   - conservative freshness aggregation without invented stale thresholds;
   - `GET /api/v1/retailers` REST/OpenAPI/generated-client contract;
   - responsive M1 readiness surface with bounded server-side timeout and fail-closed unavailable behavior.
9. **Critical product journey — COMPLETE / ACCEPTED (#80)**
   - `POST /api/v1/comparison-previews` accepts locality-only context and a manual shopping list;
   - request construction reuses canonical shopping quantities and product location;
   - comparison orchestration reuses provider snapshots, matching, basket and comparison/read-model semantics instead of duplicating them;
   - all eight canonical retailers remain visible with `READY`, `UNCERTAIN`, `INCOMPLETE` or `UNAVAILABLE` state;
   - item-level unmatched/ambiguous/package-unknown/unit-mismatch outcomes are product-safe and preserve the same basket evidence;
   - no SKU, provider ID, acquisition mode, source reference or fulfillment-context ID crosses the public API/web boundary;
   - production evidence is strict no-op/fail-closed in this slice, so production never falls back to deterministic fixture prices;
   - deterministic evidence and mock API exist only for test/acceptance composition;
   - OpenAPI/generated TypeScript client and the primary Next.js comparison form/results are synchronized;
   - request timeout is bounded and API failure renders one accessible unavailable state with no fabricated retailer results;
   - desktop/mobile Playwright proves the deterministic browser journey without hidden live retailer dependencies;
   - architecture guards keep upstream production domains independent of `preview` and prevent production preview code from depending on fixture/test-support namespaces;
   - unknown JSON request fields fail closed, keeping runtime deserialization aligned with OpenAPI `additionalProperties: false`.

   Acceptance evidence is recorded in the #80 shipping marker. The reviewed code candidate `16eff25` passed all repository workflow groups and the final read-only review reported no P0/P1/P2 findings before the docs-only marker.

10. **Structured package-evidence plumbing — IMPLEMENTED / SHIPPING (#81)**
   - `ObservedOffer` carries optional canonical package quantity only when a source has already established structured semantics;
   - the existing constructor defaults to empty evidence so current retailer integrations retain identical package-unknown behavior;
   - `OfferSnapshot` preserves the optional package quantity through immutable observation snapshots;
   - `PackageQuantitySet.fromSnapshots(...)` projects only explicit snapshot evidence into basket bindings;
   - presentation-text regression tests prove labels such as `970мл` or `1,5л` are not parsed into package quantities;
   - runtime comparison evidence derives package bindings from snapshots rather than maintaining a second independent source of truth;
   - compatibility constructors that accept explicit package bindings reject any mismatch with structured snapshot evidence;
   - deterministic acceptance fixtures now attach package quantity at the provider observation boundary before snapshotting;
   - no public contract, browser permission, live request, production-access status or retailer polling behavior changes.

   Design: [`superpowers/specs/2026-08-12-m1-structured-package-evidence-design.md`](superpowers/specs/2026-08-12-m1-structured-package-evidence-design.md).  
   Implementation plan: [`superpowers/plans/2026-08-12-m1-structured-package-evidence.md`](superpowers/plans/2026-08-12-m1-structured-package-evidence.md).

### Important remaining basket-data limitation

The core/provider/snapshot/runtime path can now carry trusted package-quantity evidence internally from source observation to basket planning, but accepted retailer adapters still do **not** prove a supported structured package field. A retailer/source extractor may populate `packageQuantity` only after its field semantics are documented and verified. Missing evidence stays unknown. Do not parse presentation text heuristically or broaden browser permissions merely to obtain package size.

### M1 exit criteria

- critical journey covered by automated integration and browser E2E tests;
- incomplete/ambiguous/uncertain outcomes transparent;
- one-store quote totals deterministic and explainable;
- unavailable retailer coverage explicit;
- no test/user path requires hidden live retailer access;
- product location/privacy boundary preserved;
- provider provenance, fulfillment context and freshness survive into comparison internally without leaking implementation identifiers publicly;
- freshness evidence basis is visible without invented stale/fresh policy;
- package quantity, when present, remains structured source evidence through provider → snapshot → basket rather than a presentation-text guess;
- incomplete baskets cannot masquerade as complete winners;
- production retailer activation remains separately gated by access, location/context and source-evidence constraints.

### Next M1 engineering focus

1. **Ship #81 structured package-evidence plumbing** after exact-head CI/security and independent review.
2. **Prove the first source-specific structured package field and extractor.** Treat each retailer/source independently; document field semantics, provenance, absence behavior and production-access constraints. Do not parse product names or widen browser permissions without separate evidence.
3. **Browser bridge lifecycle hardening (#54)** for persistent sessions, same-document store changes and SPA navigation.
4. **Magnit location → `shopCode` resolution (#69)** without leaking precise address into default product telemetry or contracts.
5. **Magnit production usage-rights decision (#70)** before recurring production polling.
6. **Kuper supported aggregator investigation (#36)**.
7. Continue mandatory Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional retailer onboarding.
8. Prove a successful real `v0.1.0-rc.3` release event.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope: built-in/user recipes, servings, normalized ingredient quantities, instructions, recipe → shopping-requirement conversion, editing/duplication and import experiments after the core model is stable.

Do not start M2 merely because the fixture-driven M1 critical journey is merged. M1 evidence limitations and production activation constraints must remain explicit and accepted rather than hidden.

## M3 — Weekly Planning

Goal: generate one coherent shopping-requirement set from several meals.

Scope: weekly planner, safe duplicate merging/unit conversion, pantry/exclusion controls and shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package-size/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat-use product with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: native Android/iOS clients via Expo/React Native/TypeScript using generated API clients and shared product vocabulary/design tokens.

## Guiding rule

Do not add infrastructure or data semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
