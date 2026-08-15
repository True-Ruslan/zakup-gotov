# M4.4.1 Server-Owned Optimization Preview — Acceptance

Date: 2026-08-16  
Repository: `True-Ruslan/zakup-gotov`

## Decision

**ACCEPTED.**

M4.4.1 establishes the server-owned application/API boundary required before responsive Optimization UX. Browser code can now consume accepted Pantry-aware WeeklyPlan comparison, checkout-economics assessment and deterministic optimizer evidence without reconstructing M4.1–M4.3 semantics client-side.

## Scope

Accepted endpoint:

`POST /api/v1/weekly-plan-pantry-optimization-previews`

Accepted composition:

1. accepted M3.5.3 builds the Pantry-aware WeeklyPlan comparison computation;
2. full Pantry coverage remains `NO_REMAINING_DEMAND` and stops before checkout-economics/optimizer work;
3. otherwise provider-neutral checkout-economics evidence is scoped to assessable retailers;
4. accepted M4.2 owns eligibility/comparability assessment;
5. accepted M4.3 owns `NO_COMPARABLE_CANDIDATES / UNIQUE_WINNER / TIE` and lowest comparable checkout total;
6. the public response projects those server-owned facts without provider/acquisition/fulfillment identifiers.

## Accepted semantics

- accepted M3.5.3 endpoint and wire behavior remain unchanged;
- `ComparisonPreviewService.create()` remains a compatibility facade over additive detailed `compute()` state;
- `WeeklyPlanPantryComparisonPreviewService.create()` remains compatible while production composition may consume its detailed computation;
- full Pantry coverage omits `optimizationPreview` on the wire and does not invoke the checkout-economics source or optimizer;
- missing checkout-economics evidence is explicit `UNKNOWN`, never fabricated zero delivery/service fee or minimum order;
- checkout-economics evidence is retailer-scoped; null, cross-retailer or otherwise out-of-scope evidence fails closed;
- every accepted comparison retailer remains represented in canonical order;
- only accepted M4.2 `COMPARABLE` candidates can win;
- exact monetary minima are delegated to accepted M4.3, including explicit ties;
- the public optimizer status, optimal retailer IDs and lowest total are checked against the accepted self-validating `BasketOptimizationResult`; M4.4.1 does not implement a second minima algorithm;
- public retailer IDs use canonical product values such as `pyaterochka`, not Java enum names;
- the internal accepted optimizer authority is excluded from JSON;
- OpenAPI 3.1 and generated `openapi-typescript` client are synchronized;
- architecture guards reject provider/database/persistence coupling and reverse dependencies from accepted lower layers;
- ordinary CI performs no live retailer request.

Production checkout-economics acquisition remains intentionally absent in this slice. The production `NoopCheckoutEconomicsEvidenceSource` truthfully yields unknown economics until a separately accepted provider-neutral evidence path exists.

## Non-goals retained

- responsive browser rendering — M4.4.2;
- provider-specific delivery/service/minimum acquisition;
- discounts, loyalty, subscriptions or tips;
- substitute/package recomputation;
- multi-store optimization;
- hidden freshness/confidence scoring.

## TDD and hardening evidence

Representative checkpoints:

- `2cc260aa…` — `ComparisonPreviewService.compute()` seam GREEN while existing `create()` behavior remains compatible;
- `6152a831…` — detailed M3.5.3 computation seam GREEN with accepted service/controller regressions;
- `ed053204…` — generic checkout optimization projection RED on missing evidence/projection types;
- `a7eac02f…` — generic server-owned M4.2/M4.3 composition GREEN, including UNKNOWN-economics fail-closed behavior;
- `838bcc96…` — WeeklyPlan optimization application contract RED;
- `bbbb3903…` — application/service GREEN;
- `68748413…` — HTTP/problem/zero-demand omission GREEN;
- `53b5616d…` — contract RED before OpenAPI/generated client update;
- `36b92aa6…` — targeted HTTP RED proving Java enum leakage (`PYATEROCHKA` instead of `pyaterochka`);
- `2d93fd29…` — canonical product retailer IDs GREEN;
- `646bbc58…` — pinned generated-schema freshness, TypeScript typecheck/tests/build GREEN;
- `0d58661a…` — authority RED showing projection did not yet consume accepted M4.3 result;
- `0d37e552…` — accepted `BasketOptimizationResult` becomes sole optimizer authority and API verification is GREEN;
- `4254d02e…` — architecture test exposed an over-restrictive test assumption about the accepted comparison catalog dependency;
- `9d343f18e1391a9d249625e2cdab6de02b13e913` — corrected architecture guard and final reviewed feature head.

## Acceptance proof

Baseline before M4.4.1 implementation: `c151cab2aad2577dcef2644baf89c7a7bf93131f`.

Issue: #142  
Implementation PR: #143  
Final reviewed feature head: `9d343f18e1391a9d249625e2cdab6de02b13e913`  
Accepted implementation merge: `67679282a388da16706c46a3caf3ff46b2b67d54`

Final feature-head gate:

- exactly **9 pull-request workflow groups**;
- **9/9 SUCCESS**;
- failures: 0;
- skipped: 0;
- cancelled: 0;
- CodeQL: SUCCESS;
- generated-schema freshness, API-client typecheck/tests/build: SUCCESS;
- read-only Change Review: **Looks good**;
- P0/P1/P2/P3: none;
- nitpicks: none;
- unresolved review threads: 0;
- merge used expected-head protection.

Post-merge `main` gate on `67679282a388da16706c46a3caf3ff46b2b67d54`:

- exactly **8 normal push workflows**;
- **8/8 SUCCESS**;
- CodeQL Java: SUCCESS;
- CodeQL JavaScript/TypeScript: SUCCESS;
- issue #142 closed as `completed`.

## Next deterministic target

**M4.4.2 — Responsive Optimization UX.**

The browser should consume only the generated M4.4.1 contract and render server-owned merchandise subtotal, fee/minimum knowledge, eligibility/comparability and optimizer outcome. It must not reimplement M4.1 arithmetic, M4.2 assessment or M4.3 winner/tie selection. Desktop/mobile/accessibility acceptance remains deterministic and network-safe.
