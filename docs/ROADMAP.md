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
- package quantity is explicit evidence, never inferred from product names;
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
7. **Complete single-store basket comparison — COMPLETE (#78 implementation; shipping gate pending)**
   - explicit package-quantity evidence keyed by snapshot identity;
   - no package-size parsing from product names and no one-SKU-equals-one-requirement assumption;
   - canonical unit-safe whole-package `ceil(required/package)` selection;
   - exact provided quantity and line-total evidence;
   - explicit per-item fulfilled/unknown/unmatched/ambiguous/unavailable/package-unknown/unit-mismatch states;
   - `COMPLETE`, `UNCERTAIN`, `INCOMPLETE` basket status;
   - unknown availability may produce a priced uncertain basket but never a confirmed complete basket;
   - incomplete baskets expose no aggregate total;
   - mixed selected currencies fail closed;
   - architecture rule prevents upstream provider/shopping/matching/retailer dependence on basket.
8. **Failure / coverage / freshness product + API + UX boundary — NEXT**
   - product-facing retailer comparison/read model;
   - explicit connected/discovery/blocked/unavailable coverage representation;
   - provider attempt/failure information translated to product-safe outcomes;
   - basket complete/uncertain/incomplete and matching ambiguity surfaced honestly;
   - observation-only vs provider-timestamp freshness surfaced without fake precision;
   - contract-first API shape and generated client integration;
   - no UI that silently hides missing retailers or unresolved production access.
9. **Critical-journey browser E2E**
   - enter list;
   - choose location/context where supported;
   - compare visible retailer states/quotes;
   - inspect incomplete/uncertain/freshness/provenance outcomes;
   - prove responsive desktop/mobile journey.

### Important remaining basket-data limitation

The core can consume trusted package-quantity evidence, but accepted retailer adapters do not yet expose a universal structured package-quantity field. Add provider/source extraction only where supported evidence proves semantics. Do not parse presentation text heuristically.

### M1 exit criteria

- critical journey covered by automated integration and browser E2E tests;
- incomplete/ambiguous/uncertain outcomes transparent;
- one-store quote totals deterministic and explainable;
- unavailable retailer coverage explicit;
- no test/user path requires hidden live retailer access;
- product location/privacy boundary preserved;
- provider provenance, fulfillment context and freshness survive into comparison;
- incomplete baskets cannot masquerade as complete winners.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope: built-in/user recipes, servings, normalized ingredient quantities, instructions, recipe → shopping-requirement conversion, editing/duplication and import experiments after the core model is stable.

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

## Parallel connectivity and engineering work

- Kuper supported aggregator investigation (#36);
- browser bridge persistent-session lifecycle hardening (#54);
- Magnit location → public `shopCode` resolution (#69);
- Magnit production usage-rights decision (#70);
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer onboarding;
- structured package-quantity extraction where source evidence is trustworthy;
- successful real `v0.1.0-rc.3` release proof.

## Guiding rule

Do not add infrastructure or data semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
