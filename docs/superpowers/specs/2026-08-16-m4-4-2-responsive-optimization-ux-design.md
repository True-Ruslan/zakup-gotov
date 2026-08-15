# M4.4.2 Responsive Optimization UX — Design

Date: 2026-08-16
Status: Authoritative implementation design
Issue: #145
Baseline: `0d1c0c5a157c3eae8988bec7281de3ffdc7a14c6`

## Goal

Advance the accepted WeeklyPlan/Pantry browser journey from M3.5.3 comparison-only transport to the accepted M4.4.1 server-owned optimization contract:

**weekly meals → original weekly demand → Pantry coverage → remaining demand → retailer comparison → checkout economics → optimizer outcome**.

M4.4.2 is presentation/orchestration only. It must not reimplement M4.1 arithmetic, M4.2 eligibility/comparability assessment or M4.3 cheapest-candidate/tie selection in browser code.

## Existing accepted seams

The current M3.5.4 browser flow already provides:

- `weekly-plan-comparison.ts` server action with a finite 3-second fail-closed transport;
- `weekly-plan-comparison-form.tsx` for locality + ordered WeeklyPlan/Recipe + request-scoped Pantry editing;
- `weekly-plan-comparison-results.tsx` for original/Pantry/remaining audit and accepted retailer comparison;
- `comparison-preview-results.tsx` for truthful retailer basket readiness/details;
- Vitest transport/form/results coverage;
- deterministic `apps/web/e2e/mock-api.mjs` + `home.spec.ts` acceptance;
- Recipe/manual-list journeys that remain regressions.

Accepted M4.4.1 now provides generated vocabulary:

- `WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH`;
- `WeeklyPlanPantryOptimizationPreviewRequest`;
- `WeeklyPlanPantryOptimizationPreview`;
- `CheckoutOptimizationPreview`;
- `RetailerCheckoutPreview`;
- `RetailerCheckoutAssessmentPreview`;
- `BasketEconomicsKnowledgeStatus`;
- `MinimumOrderStatus`;
- `CheckoutTotalStatus`;
- `RetailerCheckoutEligibilityStatus`;
- `RetailerCheckoutComparabilityStatus`;
- `BasketOptimizationStatus = NO_COMPARABLE_CANDIDATES | UNIQUE_WINNER | TIE`.

The M4.4.1 response embeds the accepted M3.5.3 `pantryComparisonPreview`, so the browser does not need to call or compose lower endpoints.

## Chosen design

### 1. Evolve the existing primary WeeklyPlan/Pantry journey

Do not introduce a parallel “optimization” form. Preserve the current WeeklyPlan/Pantry editor and advance only its server transport/result projection to M4.4.1.

The existing internal file/component naming may remain for compatibility where renaming would create churn; exported request/response types and the server action should use M4.4.1 optimization terminology so new code cannot accidentally consume the old M3.5.3 response type.

Reasons:

- request shape is intentionally the same locality + WeeklyPlan + Pantry vocabulary;
- users should not have to choose between “comparison” and “optimization” modes;
- existing M3.5.4 responsive and accessibility behavior is already accepted;
- M3.5.3 backend remains independently available but the primary browser journey should have exactly one server-owned M4.4.1 transport.

### 2. Server action uses only the generated M4.4.1 boundary

The WeeklyPlan server action must:

- call only `WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH`;
- accept generated `WeeklyPlanPantryOptimizationPreviewRequest`;
- return generated `WeeklyPlanPantryOptimizationPreview` on success;
- preserve the 3-second timeout;
- preserve sanitized generated 400 fields/messages;
- return only `ready | invalid | unavailable` application states;
- fail closed on missing API configuration, timeout, network failure, unexpected status/body or malformed response;
- never fall back to M3.5.3 or deterministic fixtures in production code.

The form continues to perform only preflight shape checks and trimming/number parsing. It does not calculate Pantry subtraction, fees, totals or winners.

### 3. Preserve the accepted Pantry/comparison audit verbatim

`preview.pantryComparisonPreview` remains the authority for:

1. original weekly demand;
2. Pantry adjustment evidence;
3. remaining demand;
4. `COMPARED / NO_REMAINING_DEMAND`;
5. accepted retailer basket comparison when `COMPARED`.

M4.4.2 should reuse the existing visual hierarchy for these stages instead of duplicating or normalizing them into a client-owned model.

`NO_REMAINING_DEMAND` remains a terminal state. The browser must not infer this from array length and must not render or invent optimization content.

### 4. Add an explicit “Стоимость оформления” section after retailer comparison

When `comparisonOutcome === COMPARED`, require `optimizationPreview` and render a separate optimization section after the accepted retailer comparison.

The section has two layers:

#### Outcome summary

Render exactly the server-provided `BasketOptimizationStatus`:

- `NO_COMPARABLE_CANDIDATES` — explain that no retailer currently has enough known/eligible checkout evidence for a truthful minimum; never display a cheapest retailer;
- `UNIQUE_WINNER` — render the one server-provided optimal retailer plus `lowestComparableCheckoutTotal`;
- `TIE` — render every server-provided optimal retailer in server order plus the shared lowest comparable total.

The browser never derives `optimalRetailerIds` from totals, never sorts candidates by price and never chooses a tie-breaker.

#### Per-retailer checkout evidence

Render optimization rows in the server-provided canonical order. For each row display:

- retailer display name resolved by canonical `retailerId` from the accepted nested comparison metadata;
- merchandise subtotal;
- delivery fee;
- service fee;
- minimum-order threshold and `MET / NOT_MET / UNKNOWN` state;
- checkout total knowledge/value;
- eligibility;
- comparability.

Known zero fees render as monetary zero, not as unknown. Unknown amounts remain explicit “Неизвестно”.

A retailer without M4.2 assessment remains visible with an explicit “Расчёт оформления недоступен” state rather than fabricated zero/unknown arithmetic.

### 5. Identity-only join, fail closed on structural drift

M4.4.1 already validates server projection consistency, but the browser presentation still must not silently align rows by array index.

Build an identity lookup from `pantryComparisonPreview.comparisonPreview.retailers` keyed by canonical retailer ID, then validate before rendering optimization:

- comparison retailer IDs are unique;
- optimization retailer IDs are unique;
- every optimization row has exactly one comparison retailer metadata row;
- every comparison retailer has exactly one optimization row;
- every `optimalRetailerId` refers to an optimization row;
- `UNIQUE_WINNER` requires exactly one optimal ID and a lowest total;
- `TIE` requires at least two optimal IDs and a lowest total;
- `NO_COMPARABLE_CANDIDATES` requires zero optimal IDs and no lowest total.

These checks do **not** recalculate monetary minima; they only defend the rendering boundary against contradictory structural runtime data. A contradiction renders one fail-closed result panel instead of partial/misleading optimization cards.

### 6. Presentation formatting is not domain arithmetic

Existing `Intl.NumberFormat("ru-RU", { style: "currency" })` behavior may be reused for display only.

The browser may:

- format a server-supplied amount/currency;
- map enum values to Russian labels;
- join identity metadata for display names.

The browser may not:

- add merchandise + fees;
- compare checkout totals;
- evaluate minimum-order thresholds;
- infer eligibility/comparability;
- derive winner/tie/no-candidate status;
- reorder retailer cards by monetary value/freshness/readiness.

### 7. Outcome and state copy is explicit and non-color-only

Use the existing stone/amber/emerald visual language rather than a page redesign.

Suggested product copy:

- `NO_COMPARABLE_CANDIDATES`: “Пока нельзя честно выбрать минимальную стоимость”;
- `UNIQUE_WINNER`: “Минимальная подтверждённая стоимость”;
- `TIE`: “Одинаковая минимальная стоимость”;
- unknown fee/threshold/checkout: “Неизвестно”;
- `ELIGIBLE`: “Заказ доступен”;
- `INELIGIBLE`: “Условия заказа не выполнены”;
- `UNKNOWN`: “Доступность заказа не подтверждена”;
- `COMPARABLE`: “Можно сравнивать”;
- `NOT_COMPARABLE`: “Нельзя включать в минимум”.

Status meaning must be carried by visible text, not color alone.

### 8. Responsive/accessibility behavior

Preserve the current page shell and input layout.

Optimization UI requirements:

- outcome summary appears before checkout retailer cards;
- cards use a responsive one/two-column grid consistent with existing retailer comparison;
- monetary/evidence rows wrap safely at 390px with no horizontal overflow;
- headings form a logical sequence after the existing comparison section;
- no generated IDs/provider identifiers are exposed;
- all status text remains readable in the accessibility tree;
- existing form controls retain labels, minimum target sizes and visible keyboard focus.

No decorative image or new visual asset is required for this functional slice.

## Deterministic fixture strategy

`apps/web/e2e/mock-api.mjs` advances its WeeklyPlan route from M3.5.3 to M4.4.1. It remains test-only and makes no retailer network request.

Deterministic scenarios should cover:

- partial/no Pantry + `NO_COMPARABLE_CANDIDATES`, including UNKNOWN economics;
- a request-selected `UNIQUE_WINNER` scenario with known zero and non-zero fees;
- a request-selected `TIE` scenario proving all server-provided tied IDs remain visible and unsorted;
- full Pantry `NO_REMAINING_DEMAND` with no `optimizationPreview` property;
- unavailable server mode.

Fixture winner/tie values are test evidence only and must never enter production browser code.

## TDD strategy

### Transport RED→GREEN

Prove:

- old M3.5.3 path is no longer used by the primary WeeklyPlan/Pantry action;
- generated M4.4.1 request/response types are used;
- successful response passes through unchanged;
- generated M4.4.1 400 validation remains product-safe;
- missing config/network/timeout/unexpected failure returns unavailable.

### Results RED→GREEN

Prove:

- Pantry audit and retailer comparison remain visible from nested accepted M3.5.3 projection;
- `NO_REMAINING_DEMAND` renders no optimization section;
- `NO_COMPARABLE_CANDIDATES` shows no cheapest retailer;
- `UNIQUE_WINNER` uses server `optimalRetailerIds` and lowest total, not array order or local comparison;
- `TIE` preserves every server-provided tied ID in server order;
- known zero fee differs visibly from unknown fee;
- per-retailer M4.2 fields render directly from server evidence;
- duplicate/missing/mismatched retailer identities or contradictory optimizer structure fail closed;
- internal/provider/server-generated identity is not exposed.

### Form RED→GREEN

Prove existing accepted behavior remains:

- ordered WeeklyPlan edit/reorder;
- request-scoped Pantry add/edit/remove;
- client preflight validation only;
- local row keys absent from request;
- generated M4.4.1 request submitted;
- ready response renders optimization-aware results;
- unavailable state fails closed.

### Browser acceptance

Desktop:
- submit WeeklyPlan + Pantry;
- observe original → Pantry → remaining → retailer comparison → checkout economics → optimizer summary.

Outcome coverage:
- no comparable candidates;
- unique winner;
- exact tie;
- no remaining demand.

Mobile/accessibility:
- 390×844 no horizontal overflow;
- visible keyboard focus;
- status meaning available as text;
- existing inputs/actions remain keyboard reachable.

Regression:
- Recipe critical journey passes;
- manual-list critical journey passes;
- WeeklyPlan reorder behavior passes;
- no live retailer request.

## Non-goals

- provider-specific delivery/service/minimum acquisition;
- changing the production no-op economics source;
- discounts, loyalty, subscriptions or tips;
- substitute/package recomputation;
- multi-store optimization;
- Pantry persistence/history;
- explicit omit-all / never-buy exclusions;
- freshness/confidence scoring;
- redesign of Recipe/manual-list journeys;
- client-side sorting/ranking by total.

## Acceptance gate

M4.4.2 may be accepted only after:

1. transport/results/form/browser RED→GREEN evidence;
2. generated M4.4.1 contract is the only primary WeeklyPlan/Pantry optimization transport;
3. browser code contains no M4.1 arithmetic, M4.2 assessment or M4.3 ranking/tie logic;
4. deterministic E2E makes no live retailer request;
5. relevant Web typecheck/Vitest/build/Playwright suites pass;
6. exact final PR head has all 9 normal workflow groups SUCCESS with clean read-only review and no unresolved threads;
7. squash merge uses expected-head protection;
8. exact merge SHA has all 8 normal post-merge push workflows SUCCESS;
9. canonical acceptance/state/roadmap/changelog are updated separately after implementation acceptance.
