# Roadmap

Updated: 2026-08-12

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner added to the target registry remains mandatory coverage work until at least one reproducible acquisition path is available.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence remains Perekrestok/Pyaterochka browser bridge, Magnit public web for explicit `shopCode`, two acquisition modes, deterministic sanitized verification and retailer-neutral architecture. M0 completion is technical feasibility, not production-access clearance; #54, #69 and #70 remain explicit constraints.

## M1 — Shopping Core — CURRENT

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage, location/context, provenance, freshness, package evidence and uncertainty.

### Entry rules

- fixture/evidence-first core;
- unavailable/blocked retailers remain visible;
- exact addresses redacted by default;
- retailer/source-provider/acquisition-mode/fulfillment context remain distinct;
- `UNKNOWN` availability remains first-class;
- observation time is not provider-update time;
- package quantity is explicit structured evidence, never inferred from product names, slugs, category or presentation text;
- source-specific extraction fails closed on ambiguity/conflict;
- corpus quality metrics classify only transport-successful, identity-valid pages;
- production activation respects usage-rights state;
- ordinary M1 tests make no live retailer requests.

### Implementation sequence

1. **Retailer registry + coverage-state model — COMPLETE (#72)**
2. **Shopping-list aggregate + canonical quantities/units — COMPLETE (#73)**
3. **Provider/path orchestration over deterministic fixtures — COMPLETE (#74)**
4. **Location / fulfillment-context boundary — COMPLETE (#75)**
5. **Price and availability snapshots — COMPLETE (#76)**
6. **Deterministic product matching — COMPLETE (#77)**
7. **Complete single-store basket comparison — COMPLETE (#78)**
8. **Failure / coverage / freshness product + API + UX boundary — COMPLETE (#79)**
9. **Critical product journey — COMPLETE / ACCEPTED (#80)**
   - stateless manual-list comparison API and responsive web journey;
   - all eight retailers remain visible with explicit comparison states;
   - product-safe item gaps;
   - production runtime evidence strict no-op/fail-closed;
   - desktop/mobile deterministic Playwright without hidden live retailer access.
10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**
   - optional canonical `ObservedOffer.packageQuantity`;
   - immutable snapshot preservation;
   - snapshot-derived `PackageQuantitySet`;
   - runtime rejects a parallel package set that disagrees with snapshots;
   - title/presentation parsing explicitly prohibited and regression-tested.
11. **Magnit exact characteristic package extraction — COMPLETE / ACCEPTED (#82)**
   - pure exact-field extractor for `Вес, кг` / `Объем, л` inside `Характеристики`;
   - canonical kg→g / l→ml;
   - fail-closed ambiguity/conflict/invalid states;
   - title/slug/description/script/style/out-of-section data cannot create evidence;
   - count semantics deferred;
   - no HTTP/Spring activation;
   - merged as `3753a9296562354939e86876a8096c15b2957e35`, then all eight push-triggered `main` workflows passed with zero failures.
12. **Magnit fixed-corpus package-evidence instrumentation — IMPLEMENTED / SHIPPING (#83)**
   - existing explicit/manual 20-product × 2-shop corpus remains unchanged in request count and transport policy;
   - identity-valid pages carry #82 package extraction alongside existing price/promo/availability evidence;
   - package status metrics include only HTTP 2xx + expected-SKU observations;
   - transport/error and wrong-identity pages are excluded rather than counted as `MISSING`;
   - structural summary reports `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES`, `INVALID_VALUE`;
   - status counts must equal `packageEvidencePages` exactly;
   - evidence line exposes only aggregate counters;
   - existing guarded live property remains `-Dzakup.live.magnit.corpus=true`;
   - no pre-evidence minimum `FOUND` threshold is invented;
   - ordinary CI remains live-retailer-free.

   Design: [`superpowers/specs/2026-08-12-magnit-package-evidence-corpus-design.md`](superpowers/specs/2026-08-12-magnit-package-evidence-corpus-design.md).  
   Plan: [`superpowers/plans/2026-08-12-magnit-package-evidence-corpus.md`](superpowers/plans/2026-08-12-magnit-package-evidence-corpus.md).

### Important remaining basket-data limitation

Magnit now has accepted weight/volume semantics and an instrumented fixed-corpus measurement path, but the first live package-status distribution is still pending. Products with both weight and volume remain unknown under the current single-`Quantity` model; count is deferred; other retailers still lack an accepted structured field.

Never replace this with product-name parsing.

### M1 exit criteria

- critical journey covered by automated integration/browser E2E;
- incomplete/ambiguous/uncertain outcomes transparent;
- deterministic explainable one-store quote totals;
- unavailable retailer coverage explicit;
- no hidden live retailer dependency in ordinary tests/product journey;
- product location/privacy boundary preserved;
- provenance/context/freshness survive internally without leaking implementation IDs;
- package evidence remains structured source evidence through provider → snapshot → basket;
- source-specific ambiguity/conflict never becomes a guessed quantity;
- evidence-quality measurements separate transport/identity failure from missing metadata;
- incomplete baskets cannot masquerade as complete winners;
- production activation remains separately gated by access, location/context and source semantics.

### Next M1 engineering focus

1. **Ship #83** after exact-head CI/security and independent review.
2. Run the existing guarded **explicit/manual Magnit fixed corpus** once and record the first accepted package-status distribution. This remains research evidence and does not enable recurring polling.
3. Use the measured distribution to decide whether exact weight/volume support is sufficient and whether `Количество в упаковке` / multiple package dimensions require a domain extension.
4. **Magnit location → `shopCode` (#69)** and **production usage-rights decision (#70)** remain mandatory before production activation.
5. **Browser bridge lifecycle hardening (#54)** for persistent sessions/store changes/SPA navigation.
6. **Kuper supported aggregator investigation (#36)**.
7. Continue Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer onboarding.
8. Prove a successful real `v0.1.0-rc.3` release event.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope: built-in/user recipes, servings, normalized ingredient quantities, instructions, recipe → shopping-requirement conversion, editing/duplication and import experiments after the core model is stable.

Do not start M2 merely because fixture-driven M1 UI is complete. M1 evidence and production constraints must remain explicit and accepted rather than hidden.

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
