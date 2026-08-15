# M3.5.3 Pantry-aware WeeklyPlan → Comparison — Design

Date: 2026-08-15
Status: APPROVED FOR IMPLEMENTATION
Issue: #127

## Goal

Compare only the canonical WeeklyPlan shopping demand that remains after accepted M3.5.1 Pantry subtraction, while preserving the complete M3.5.2 original-demand, Pantry-evidence and remaining-demand audit trail.

M3.5.3 is a new stateless composition boundary. It does not silently change accepted M3.3 or M3.5.2 behavior.

## Boundary

New endpoint:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

Request:

- `locality`: provider-neutral comparison locality using accepted ComparisonPreview limits;
- `weeklyPlan`: accepted `WeeklyPlanShoppingPreviewRequest` vocabulary;
- `pantry`: accepted request-scoped M3.5.2 Pantry rows.

Response:

- `pantryShoppingPreview`: the complete accepted M3.5.2 result, including WeeklyPlan projection, original ShoppingList/provenance, ordered Pantry adjustments and zero-or-more remaining ShoppingItems;
- `comparisonOutcome`: `COMPARED | NO_REMAINING_DEMAND`;
- `comparisonPreview`: accepted `ComparisonPreview` when `comparisonOutcome = COMPARED`; omitted/null when `comparisonOutcome = NO_REMAINING_DEMAND`.

The explicit outcome prevents an empty or absent comparison from being mistaken for retailer failure or silent ingredient loss.

## Zero-remaining-demand semantics

When `pantryShoppingPreview.remainingShoppingList.items` is empty:

1. return `comparisonOutcome = NO_REMAINING_DEMAND`;
2. return no `comparisonPreview`;
3. do not fabricate a ShoppingItem;
4. do not invoke `ComparisonPreviewService`;
5. therefore do not invoke runtime retailer evidence/acquisition merely to satisfy the non-empty accepted ComparisonPreview input contract;
6. retain the full M3.5.2 original ShoppingList and Pantry evidence so the empty remaining demand is explainable.

`NO_REMAINING_DEMAND` is a successful 200 result, not an error and not a retailer availability state.

## Locality semantics

Locality validity is independent of Pantry coverage. The new boundary validates locality before branching on remaining demand so an invalid locality cannot become valid merely because Pantry covers every item.

Validation mirrors accepted ComparisonPreview input semantics:

- non-null/non-blank after `strip()` and whitespace collapse;
- maximum normalized length 160 characters.

For non-empty remaining demand, accepted `ComparisonPreviewService` remains authoritative and re-validates/normalizes locality as usual. M3.5.3 does not change ComparisonPreview behavior.

## Composition

1. Validate the M3.5.3 wrapper and locality.
2. Delegate WeeklyPlan/Pantry validation and composition entirely to `WeeklyPlanPantryShoppingPreviewService` by adapting `weeklyPlan` + `pantry` into the accepted M3.5.2 request.
3. If the M3.5.2 remaining list is empty, return the zero-demand outcome without invoking comparison.
4. Otherwise adapt only remaining ShoppingItems into `ComparisonPreviewItemRequest` using their exact UUID, requirement and canonical quantity.
5. Invoke accepted `ComparisonPreviewService` exactly once with the request locality and remaining items.
6. Map accepted caller-facing `InvalidComparisonPreviewRequestException` validation into the M3.5.3 sanitized problem namespace using `comparison.*` field paths; this covers derived limits such as more than 100 remaining comparison items without turning them into 500 responses.
7. Verify the returned `ComparisonPreview.items` against the remaining list by cardinality, UUID/order, requirement and quantity.
8. Return the complete M3.5.2 preview plus explicit outcome and accepted comparison result.

## Invariants

- Existing M3.3 `/api/v1/weekly-plan-comparison-previews` remains behaviorally unchanged.
- Existing M3.5.2 `/api/v1/weekly-plan-pantry-shopping-previews` remains behaviorally unchanged.
- M3.5.2 is the sole owner of original WeeklyPlan shopping projection, Pantry subtraction, audit evidence and remaining-demand provenance.
- M3.5.3 never re-runs Pantry subtraction and never recomputes planner provenance.
- Only remaining ShoppingItems may enter ComparisonPreview.
- Non-empty comparison input preserves remaining UUID/order/requirement/canonical quantity exactly.
- Comparison item cardinality/order/identity/requirement/quantity drift fails closed.
- Caller-facing accepted comparison validation is sanitized; internal bridge drift is never downgraded into caller validation.
- Zero remaining demand never enters ComparisonPreview.
- `COMPARED` requires a non-null ComparisonPreview and non-empty remaining demand.
- `NO_REMAINING_DEMAND` requires a null/absent ComparisonPreview and an empty remaining list.
- Pantry rows remain provider-neutral and carry no server-owned acquisition identifiers.
- No persistence, browser Pantry UI, provider behavior, retailer activation, fuzzy/AI matching or omit-all exclusion semantics are introduced.

## Validation and HTTP safety

- Null request and invalid locality produce a new sanitized M3.5.3 validation problem.
- M3.5.2 validation errors are mapped into the new boundary without leaking implementation exceptions and retain their `weeklyPlan.*` / `pantry[*]` field paths.
- Accepted ComparisonPreview validation errors caused by the derived remaining demand are mapped into the same boundary with `comparison.*` field paths.
- Unknown top-level fields, malformed JSON and deserialization/enum failures return one sanitized `$request: malformed JSON request` error.
- Internal bridge drift is not converted into caller validation; it remains a fail-closed server defect.
- Error payload code: `INVALID_WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEW`.

## OpenAPI/client

OpenAPI 3.1 gains only the new operation and schemas required by this boundary.

Expected generated-client surface:

- operation: `createWeeklyPlanPantryComparisonPreview`;
- path constant: `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH`;
- request/response/problem/outcome types synchronized from OpenAPI;
- `comparisonPreview` optional/nullable in the response, with `comparisonOutcome` making the semantic state explicit.

Existing M3.3 and M3.5.2 generated contracts remain unchanged.

## Architecture

New package:

`io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview`

Allowed production dependencies:

- `weeklyplanpantrypreview` for accepted M3.5.2 composition and Pantry request/response vocabulary;
- `weeklyplanpreview` only for the accepted WeeklyPlan request vocabulary used by the flattened HTTP request;
- `preview` for accepted ComparisonPreview request/result/service;
- read-only canonical `shopping` quantity vocabulary exposed by accepted M3.5.2 output; this does not grant M3.5.3 ownership of Shopping aggregation or Pantry semantics;
- Java/Spring/Jackson support.

Forbidden direct dependencies:

- `pantry` domain implementation;
- `recipe` or `weeklyplan` domain owners;
- Shopping mutation/aggregation logic beyond reading the canonical quantity vocabulary already exposed by M3.5.2;
- `retailer`, `provider`, acquisition bridges;
- persistence/database;
- browser/web UI.

M3.3 and M3.5.2 must not acquire reverse dependencies on the new package.

## Test strategy

RED→GREEN in layers:

1. service composition and zero-demand short-circuit;
2. fail-closed drift and validation mapping;
3. HTTP success/zero-demand/sanitized-error contract;
4. OpenAPI/generated TypeScript contract;
5. ArchUnit/regression guards proving M3.3/M3.5.2 remain unchanged and no provider/database coupling appears.

Critical deterministic proofs:

- partial Pantry coverage compares only remaining demand;
- full Pantry coverage returns `NO_REMAINING_DEMAND` and the comparison invoker call count remains zero;
- empty Pantry preserves all accepted weekly demand into comparison;
- invalid locality remains invalid even under full Pantry coverage;
- accepted derived comparison validation cannot leak as a 500;
- ordinary tests/CI make no live retailer request.

Final gate: exact-head 9/9 PR workflows + clean read-only review, squash merge, exact main 8/8 post-merge workflows, then a separate canonical acceptance docs PR.