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
   - item-level gaps remain product-safe;
   - production runtime evidence is strict no-op/fail-closed;
   - desktop/mobile deterministic Playwright without hidden live retailer access.
10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**
   - optional canonical `ObservedOffer.packageQuantity`;
   - immutable snapshot preservation;
   - snapshot-derived `PackageQuantitySet`;
   - runtime rejects a parallel package set that disagrees with snapshots;
   - title/presentation parsing explicitly prohibited and regression-tested;
   - merged as `d8a9e5f0f67defeeac410e0a006eab57dc2bb637`, then all eight push-triggered main workflows passed.
11. **Magnit exact characteristic package extraction — IMPLEMENTED / SHIPPING (#82)**
   - pure source-specific extractor for exact `Характеристики` fields `Вес, кг` and `Объем, л`;
   - kg/l canonicalization reuses `Quantity`;
   - identical duplicates deduplicate;
   - weight + volume → `AMBIGUOUS_DIMENSIONS` and no package quantity;
   - conflicting same-dimension values → `CONFLICTING_VALUES`;
   - malformed/zero/negative supported values → `INVALID_VALUE`;
   - title/slug/description/script/style numbers do not create evidence;
   - `Количество в упаковке` is deferred from v1;
   - a provider/snapshot regression proves `FOUND` output can enter the #81 evidence path and ambiguous output remains unknown;
   - no HTTP exists in the production extractor and no production Magnit polling/access state is changed.

   Design: [`superpowers/specs/2026-08-12-magnit-structured-package-characteristics-design.md`](superpowers/specs/2026-08-12-magnit-structured-package-characteristics-design.md).  
   Evidence: [`integrations/magnit-structured-package-characteristics-2026-08-12.md`](integrations/magnit-structured-package-characteristics-2026-08-12.md).

### Important remaining basket-data limitation

Provider/snapshot/basket plumbing can carry trusted package evidence, and Magnit now has a narrowly supported weight/volume characteristic extractor. This does **not** mean every Magnit SKU yields a usable quantity: products with both weight and volume remain unknown under the current single-`Quantity` model, count is deferred, and other retailers still lack an accepted structured field.

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
- incomplete baskets cannot masquerade as complete winners;
- production activation remains separately gated by access, location/context and source semantics.

### Next M1 engineering focus

1. **Ship #82** after exact-head CI/security and independent review.
2. **Run an explicit/manual Magnit fixed-corpus package-evidence pass** after #82 acceptance. Extend the existing research probe to report `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES` and `INVALID_VALUE` counts without enabling recurring polling.
3. Use corpus evidence to decide whether weight/volume support is sufficient and whether `Количество в упаковке` requires multi-dimensional package modeling.
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
