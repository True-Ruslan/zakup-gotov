# M4.4.1 Server-Owned Optimization Preview — Design

**Date:** 2026-08-15  
**Issue:** #142  
**Baseline:** `c151cab2aad2577dcef2644baf89c7a7bf93131f`

## Goal

Expose one stateless application/API boundary that carries accepted M3.5.3 Pantry-aware WeeklyPlan comparison evidence through accepted M4.2 checkout assessment and M4.3 deterministic optimization, so M4.4.2 can render checkout economics and optimizer outcomes without recomputing them in browser code.

M4.4.1 is a **server-owned projection slice**. It does not add retailer-specific economics acquisition and does not change accepted M3.5.3, M4.1, M4.2 or M4.3 semantics.

## Approaches considered

### A. Extend the existing M3.5.3 endpoint

Rejected. Adding checkout/optimizer fields to `/api/v1/weekly-plan-pantry-comparison-previews` would silently mutate an accepted public contract and blur the permanent boundary that M3.5.3 owns Pantry-aware comparison only.

### B. Return raw comparison/economics pieces and optimize in the browser

Rejected. This would violate the accepted M4.4 invariant that checkout arithmetic, eligibility, comparability, winner and tie decisions remain server-owned. It would also create a second implementation of M4.1–M4.3 semantics in TypeScript.

### C. Add an explicit optimization preview boundary over additive detailed-computation seams

**Selected.** Existing accepted `create()` methods keep their wire behavior. Internal application services expose a richer immutable computation result for downstream composition, and a new endpoint projects only product-safe economics/optimizer evidence.

## Public boundary

Add:

`POST /api/v1/weekly-plan-pantry-optimization-previews`

Operation ID:

`createWeeklyPlanPantryOptimizationPreview`

The request reuses the accepted M3.5.3 request vocabulary:

- `locality`;
- ordered WeeklyPlan input;
- request-scoped Pantry rows.

No client-supplied retailer, provider, fulfillment, economics or optimizer identities are accepted.

## Response shape

`WeeklyPlanPantryOptimizationPreview` contains:

- `pantryComparisonPreview` — the accepted M3.5.3 projection, including Pantry evidence and `COMPARED / NO_REMAINING_DEMAND`;
- optional `optimizationPreview` — present **iff** `pantryComparisonPreview.comparisonOutcome == COMPARED`.

This intentionally keeps M3.5.3 evidence intact rather than flattening or recreating it.

### Zero remaining demand

When M3.5.3 returns `NO_REMAINING_DEMAND`:

- `optimizationPreview` is absent;
- checkout-economics source is not invoked;
- M4.2 assessment is not invoked;
- M4.3 optimizer is not invoked;
- no dummy retailer candidate or zero checkout total is fabricated.

### Compared demand

When M3.5.3 returns `COMPARED`:

- accepted comparison output is preserved unchanged;
- one server-owned checkout row is returned for every comparison retailer in the same canonical order;
- M4.3 status is explicit `NO_COMPARABLE_CANDIDATES / UNIQUE_WINNER / TIE`;
- optimal retailer IDs are a direct projection of M4.3 `optimalCandidates`, never recomputed from totals in the client;
- lowest comparable checkout total is present only when M4.3 has at least one optimal candidate.

## Additive detailed-computation seams

M4.4.1 needs accepted domain state that is currently projected away before HTTP response construction. Reconstructing M1 domain state from public DTOs is intentionally avoided.

### Comparison preview computation

Add `ComparisonPreviewComputation` owned by `preview`:

- `ComparisonPreviewInput input`;
- `ComparisonPreview preview`;
- `RetailerComparisonCatalog catalog`.

Add:

`ComparisonPreviewService.compute(ComparisonPreviewRequest request)`

Existing:

`ComparisonPreviewService.create(request)`

becomes a compatibility facade returning `compute(request).preview()`.

The computation record validates that preview retailer IDs/order correspond exactly to the domain catalog. Existing `/api/v1/comparison-previews` behavior must remain byte-for-byte equivalent for accepted fixtures.

### Pantry comparison computation

Add `WeeklyPlanPantryComparisonPreviewComputation` owned by `weeklyplanpantrycomparisonpreview`:

- accepted `WeeklyPlanPantryComparisonPreview preview`;
- optional `ComparisonPreviewComputation comparisonComputation`.

Add:

`WeeklyPlanPantryComparisonPreviewService.compute(request)`

Existing `create(request)` becomes a compatibility facade returning `compute(request).preview()`.

Invariants:

- `NO_REMAINING_DEMAND` -> detailed comparison computation absent;
- `COMPARED` -> detailed computation present and its public preview equals the embedded accepted comparison preview;
- existing M3.5.3 locality/Pantry/Shopping identity-order-quantity checks remain authoritative and unchanged.

## Checkout economics evidence source

Add provider-neutral application interface:

`CheckoutEconomicsEvidenceSource.load(ProductLocation location, Set<RetailerId> requestedRetailers)`

returning a retailer-keyed immutable map of accepted M4.1 `BasketEconomics`.

Rules:

- source is invoked only for retailers whose accepted M1 comparison has a merchandise subtotal (`READY` or `UNCERTAIN`);
- requested retailer set is immutable and non-empty when the source is invoked;
- null source result, null keys/values or any retailer outside the requested set fail closed;
- source ordering is irrelevant; final projection always follows accepted comparison catalog order;
- a requested retailer missing from source output means **economics unknown**, not free checkout.

Missing economics is represented internally as:

- `BasketFee.unknown()` delivery;
- `BasketFee.unknown()` service;
- `MinimumOrderConstraint.unknown()`.

This is explicit unknown evidence, not fabricated monetary evidence.

Production wiring uses `NoopCheckoutEconomicsEvidenceSource`, returning no known economics until a separate provider/access slice proves truthful acquisition. Deterministic tests inject known evidence. No provider-specific transport is introduced here.

## Checkout assessment and optimization composition

For every domain retailer view in accepted catalog order:

1. choose source economics for the same `RetailerId`, otherwise explicit all-unknown economics;
2. bind it as `RetailerCheckoutEconomicsEvidence(retailerId, economics)`;
3. invoke accepted M4.2 `RetailerCheckoutAssessmentService.assess`;
4. collect every `RetailerCheckoutAssessmentResult` in catalog order;
5. invoke accepted M4.3 `BasketOptimizer.optimize` once over the complete ordered result list.

M4.4.1 never reimplements eligibility, comparability or winner rules.

## Product-safe checkout projection

`CheckoutOptimizationPreview` contains:

- ordered `retailers` rows aligned 1:1 with accepted comparison retailers;
- accepted M4.3 `status`;
- ordered `optimalRetailerIds` projected directly from M4.3 optimal candidates;
- optional `lowestComparableCheckoutTotal` projected directly from M4.3.

Each `RetailerCheckoutPreview` contains:

- public canonical `retailerId`;
- optional `assessment`.

Assessment is absent exactly when accepted M4.2 assessment is absent (`INCOMPLETE / UNAVAILABLE`).

`RetailerCheckoutAssessmentPreview` exposes only accepted server-owned facts needed by M4.4.2:

- merchandise subtotal;
- delivery fee knowledge + optional amount;
- service fee knowledge + optional amount;
- minimum-order knowledge + optional threshold;
- accepted `MinimumOrderStatus`;
- accepted `CheckoutTotalStatus` + optional checkout total;
- accepted `RetailerCheckoutEligibilityStatus`;
- accepted `RetailerCheckoutComparabilityStatus`;
- optional comparable checkout total.

No provider/acquisition/fulfillment identifiers or source-internal evidence are exposed.

## Projection invariants

Public M4.4.1 result fails closed if:

- checkout retailer count/order/ID differs from accepted comparison retailers;
- an assessment exists for an M4.2 result that has no assessment, or vice versa;
- projected economics/status/amount differs from accepted M4.2 value;
- optimizer optimal retailer IDs differ from accepted M4.3 optimal candidates/order;
- lowest total presence/value differs from accepted M4.3 result;
- `optimizationPreview` presence contradicts M3.5.3 comparison outcome.

The browser may use returned retailer IDs to associate presentation rows, but it does not infer or recompute optimizer state.

## Error boundary

Add M4.4.1-specific sanitized invalid-request problem vocabulary.

- semantic M3.5.3 validation errors are translated without losing product-safe field/message information;
- malformed/unknown JSON uses the M4.4.1 problem type rather than leaking Jackson/internal exceptions;
- internal composition/evidence contract violations fail server-side and never become fabricated product results.

## OpenAPI / generated client

Add OpenAPI 3.1 path and schemas for the new request/response/problem projection.

Generated TypeScript must expose:

- `WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH`;
- generated request/response/economics/optimizer types;
- generated client support for the new path.

M4.4.2 will consume only these generated types/path constants.

## Architecture

Dependency direction:

`weeklyplanpantryoptimizationpreview`
→ accepted `weeklyplanpantrycomparisonpreview`
→ new `optimizationpreview`
→ accepted `retailercheckout` + `basketoptimization`
→ accepted lower layers.

The new application layer may also consume the finite `ProductLocation`, `RetailerId` and M4.1 basket value vocabulary required by the evidence-source boundary/projection.

Forbidden:

- reverse dependency from M3.5.3/M4.1/M4.2/M4.3 into M4.4.1;
- direct provider-specific implementation in the new composition package;
- database/jOOQ dependency;
- web/React dependency;
- live retailer network in tests/CI.

## Acceptance scenarios

1. Full Pantry coverage -> accepted `NO_REMAINING_DEMAND`, no optimization payload, economics source not invoked.
2. Compared demand + no known economics -> assessable retailers carry explicit unknown economics, none silently becomes comparable.
3. Known same-currency economics with one lowest eligible READY retailer -> `UNIQUE_WINNER` and exact server-owned lowest total.
4. Two equal numeric minima -> explicit `TIE` containing both retailer IDs in accepted order.
5. Cheaper ineligible/unknown/uncertain candidate remains visible but cannot win.
6. `INCOMPLETE / UNAVAILABLE` rows remain visible with no fabricated assessment.
7. Known zero fees remain known zero in public projection.
8. Mixed comparable currencies fail closed through accepted M4.3.
9. Source returning out-of-request retailer fails closed before M4.2/M4.3 result publication.
10. Existing M3.5.3 endpoint/result remains unchanged and regression-green.
11. OpenAPI/generated TypeScript regeneration is clean.
12. Exact architecture guards prove no provider/database/web/reverse coupling.
13. Ordinary API/contract/browser acceptance performs no live retailer request.

## Non-goals

- responsive browser rendering (M4.4.2);
- provider-specific fee/minimum acquisition;
- changing M3.5.3 wire semantics;
- discounts, loyalty, subscriptions or tips;
- currency conversion;
- package/SKU/substitute search;
- multi-store basket optimization;
- freshness penalties, confidence scoring or hidden tie-breaks;
- persistence/history.