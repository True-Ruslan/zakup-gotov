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
- mass, volume and count are not interchangeable;
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
11. **Magnit visible exact-characteristic semantics — COMPLETE / ACCEPTED (#82)**
   - pure exact-field semantics for `Вес, кг` / `Объем, л` inside visible `Характеристики`;
   - canonical kg→g / l→ml;
   - fail-closed ambiguity/conflict/invalid states;
   - title/slug/description/script/style/out-of-section data cannot create evidence;
   - count semantics deferred;
   - no HTTP/Spring activation.
12. **Magnit fixed-corpus package-evidence instrumentation — COMPLETE / ACCEPTED (#83)**
   - existing explicit/manual 20-product × 2-shop corpus remains unchanged in request count and transport policy;
   - package status metrics include only HTTP 2xx + expected-SKU observations;
   - transport/error and wrong-identity pages are excluded rather than counted as `MISSING`;
   - structural summary reports `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES`, `INVALID_VALUE`;
   - status counts equal `packageEvidencePages` exactly;
   - ordinary CI remains live-retailer-free;
   - first visible-text live measurement: `FOUND=0`, `MISSING=40`, with 40 eligible pages and no ambiguity/conflict/invalid values.
13. **Magnit SKU-bound JSON-LD package evidence — IMPLEMENTED / LIVE EVIDENCE PROVEN / SHIPPING (#85)**
   - same raw PUBLIC_WEB response, no extra retailer request and no browser execution;
   - exact JSON-LD `Product` + exact expected `sku` identity;
   - scalar `Product.weight` accepted as kilograms only because Magnit provenance was explicitly proven;
   - exact `additionalProperty.name="Объем, л"` scalar value accepted as liters;
   - generic fields and presentation text remain non-authoritative;
   - duplicate equivalent values deduplicate;
   - invalid/conflicting/multi-dimensional source evidence remains fail-closed;
   - count remains deferred;
   - corpus projection uses the JSON-LD extractor without changing price/promo/availability transport logic;
   - deterministic extractor/corpus tests and full API verification are green;
   - same finite 40-request replay produced `FOUND=36`, `MISSING=0`, `AMBIGUOUS_DIMENSIONS=4`, `CONFLICTING_VALUES=0`, `INVALID_VALUE=0`, failed requirements `0`;
   - the four ambiguity observations are exactly milk SKU `1000013732` and kefir SKU `1000330180` in both shop contexts;
   - all other fixed requirements are `FOUND` in the one-shop diagnostic;
   - egg evidence is structured mass, not package count, and canonical unit mismatch prevents mass from satisfying `PIECE` requirements.

   Design: [`superpowers/specs/2026-08-12-magnit-jsonld-package-evidence-design.md`](superpowers/specs/2026-08-12-magnit-jsonld-package-evidence-design.md).  
   Plan: [`superpowers/plans/2026-08-12-magnit-jsonld-package-evidence.md`](superpowers/plans/2026-08-12-magnit-jsonld-package-evidence.md).  
   Evidence: [`integrations/magnit-jsonld-package-evidence-2026-08-12.md`](integrations/magnit-jsonld-package-evidence-2026-08-12.md).

### Important remaining basket-data limitation

Magnit now has strong structured weight/volume coverage on the measured fixed corpus, but this is not universal-catalog proof. Products with both weight and volume remain unknown under the current single-`Quantity` model; count is deferred; other retailers still lack an accepted structured package field.

Never replace this with product-name parsing or density/category guesses.

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
- package dimensions remain unit-compatible and count is never inferred from mass/volume;
- evidence-quality measurements separate transport/identity failure from missing metadata;
- incomplete baskets cannot masquerade as complete winners;
- production activation remains separately gated by access, location/context and source semantics.

### Next M1 engineering focus

1. **Ship #85** after exact-head CI/security and independent review; require post-merge `main` verification before calling it ACCEPTED.
2. Then prioritize **Magnit location → `shopCode` (#69)** as the highest-leverage implementable blocker for converting explicit-store feasibility into location-aware product use.
3. Keep **production usage-rights decision (#70)** independent and mandatory before recurring acquisition; technical feasibility is not authorization.
4. Keep multi-dimensional package-domain extension and `Количество в упаковке` as separate evidence-driven work, not blockers for shipping current mass/volume support.
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
