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
- transport success never implies metadata availability;
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
10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**
11. **Magnit exact characteristic package semantics — COMPLETE / ACCEPTED (#82)**
12. **Magnit fixed-corpus package-evidence instrumentation — COMPLETE / ACCEPTED (#83)**
   - explicit/manual 20-product × 2-shop corpus;
   - package metrics only for HTTP 2xx + expected-SKU observations;
   - five fail-closed extraction states with structural count invariant;
   - no recurring polling or production activation;
   - merged as `bee69a7bf84f1c2b98f20f76fe244d4bf3ade4a6`, then all eight push-triggered `main` workflows passed.
13. **Magnit live package corpus — RESEARCH EVIDENCE COMPLETE / CURRENT NO-GO for raw PUBLIC_WEB package extraction**
   - one explicit finite run: 20 products × 2 shop contexts = 40 requests;
   - 40/40 HTTP 2xx;
   - 40/40 usable observations;
   - stable identity 20/20 across contexts;
   - 40 package-evidence-eligible pages;
   - `FOUND = 0`, `MISSING = 40`, all other package states = 0;
   - zero failed corpus requirements;
   - therefore the current Java `HttpClient` server-side PUBLIC_WEB HTML surface is not an accepted package-quantity path despite remaining valid for SKU/current-price feasibility.

   Evidence: [`integrations/magnit-package-evidence-corpus-live-2026-08-12.md`](integrations/magnit-package-evidence-corpus-live-2026-08-12.md).

### Important remaining basket-data limitation

#81 can carry trusted package evidence and #82 defines exact Magnit weight/volume semantics, but the current raw Magnit PUBLIC_WEB surface yielded **0/40** supported fields in a clean live corpus. Do not wire that path into basket package evidence and never replace missing metadata with product-name parsing.

The unresolved question is provenance: official rendered pages can expose labeled characteristics, but current raw server HTML does not expose them to the accepted extractor. Determine whether they live in embedded/bootstrap structured data, a separate public page request, or only browser-rendered DOM.

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
- acquisition mode used for package evidence is explicitly proven rather than inferred from a different successful data field;
- incomplete baskets cannot masquerade as complete winners;
- production activation remains separately gated by access, location/context and source semantics.

### Next M1 engineering focus

1. **Magnit package-characteristics provenance investigation — NEXT.** Compare raw HTML markers, embedded/bootstrap machine data, separate public page requests and hydrated browser DOM with finite sanitized diagnostics.
2. If a stable raw/bootstrap/public-request structured field exists, build a narrow extractor/replay corpus before any production wiring.
3. If characteristics exist only after browser execution, treat Magnit browser acquisition as a separate design/evidence decision; do not silently redefine the accepted PUBLIC_WEB path.
4. Keep **#69 location → `shopCode`** and **#70 production usage-rights** as mandatory blockers before recurring Magnit activation.
5. Continue **#54 browser bridge lifecycle hardening**, **#36 Kuper supported aggregator investigation**, and mandatory Chizhik/Ozon Fresh/Samokat/Lenta/VkusVill onboarding.
6. Prove a successful real `v0.1.0-rc.3` release event.

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
