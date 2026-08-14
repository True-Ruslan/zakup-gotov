# M3.4 Responsive Weekly Planning UI — Design

Date: 2026-08-15  
Issue: #118  
Status: APPROVED FOR IMPLEMENTATION

## Goal

Expose the accepted M3.3 WeeklyPlan → Comparison composition as the primary M3 browser journey without moving planner, Recipe, Shopping or Comparison semantics into browser code.

Product flow:

`ordered meal occurrences + locality → POST /api/v1/weekly-plan-comparison-previews → canonical weekly shopping requirements → truthful retailer comparison`

## Authoritative boundary

The browser uses only the generated `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH` contract. It must not compose `/weekly-plan-shopping-previews` and `/comparison-previews` itself.

Generated OpenAPI types are authoritative for request/response/day/unit vocabulary. Browser-local numeric keys exist only for React list identity and are never sent as WeeklyPlan, occurrence, Recipe, ingredient, ShoppingList or ShoppingItem identity.

## Interaction model

The page advances its primary journey to `M3 · Weekly Planning` while keeping Recipe and manual-list comparison as secondary regression-covered paths.

The planner begins with one occurrence. Each occurrence contains:

- day (`MONDAY..SUNDAY`);
- target servings;
- Recipe title;
- base servings;
- one or more explicit ingredients with requirement, quantity and generated unit vocabulary.

Users may add/remove occurrences up to the accepted API limit of 35 and move an occurrence one position up/down. Reordering changes only explicit caller order; the UI never auto-sorts by day. No breakfast/lunch/dinner/snack taxonomy is introduced.

Each Recipe keeps at least one ingredient. Browser validation is presentation/preflight only: required text, positive finite quantities and positive integer servings. Authoritative semantic validation remains server-side.

## Results

A successful response renders:

1. canonical weekly shopping requirements from `weeklyPlanShoppingPreview.shoppingList.items`, in server order;
2. the existing `ComparisonPreviewResults` projection unchanged.

Generated IDs and source tuples are intentionally hidden from normal user-facing output. The browser does not recalculate scaling, aggregation, canonicalization, matching, package counts, totals, winner ranking or retailer status.

## Failure behavior

Transport follows the accepted Recipe UI pattern:

- missing `API_BASE_URL` → unavailable;
- finite timeout → unavailable;
- network/non-400/unexpected failure → unavailable;
- generated 400 problem fields/messages only → invalid;
- never fabricate shopping or retailer results after failure.

Changing form input after a prior result does not mutate the accepted response; a new submit replaces state atomically.

## Responsive and accessibility requirements

- semantic `section`, `form`, `fieldset`, `legend`, `label` relationships;
- all controls keyboard reachable with visible focus;
- move buttons have occurrence-specific accessible names;
- disabled boundary move/remove controls communicate impossible actions;
- mobile width 390px has no horizontal document overflow;
- pending submit is disabled and announces concise progress through button text;
- one accessible alert is used for preflight/backend/unavailable failures.

## Deterministic testing boundary

Ordinary Playwright uses the existing local mock API only. Add a deterministic `/api/v1/weekly-plan-comparison-previews` route that projects request data into fixed server-owned IDs, canonical weekly shopping and the existing eight-retailer comparison fixture. No live retailer request, cookie, token, store address or provider identifier enters browser acceptance.

Required regression coverage:

- desktop planner → weekly shopping → comparison;
- add/remove/reorder and day/servings request projection;
- mobile no-overflow;
- keyboard focus;
- unavailable fail-closed path;
- existing Recipe journey;
- existing manual-list journey.

## Non-goals

- persistence/saved plans/history;
- pantry or exclusion subtraction;
- calendar dates/time zones;
- meal-slot taxonomy;
- nutrition;
- fuzzy/synonym/AI ingredient equivalence;
- retailer onboarding or production-access changes;
- browser-side comparison optimization.

## Acceptance gate

M3.4 is accepted only after RED→GREEN evidence, fresh exact-head 9/9 normal PR workflow groups, clean review/no unresolved threads, squash merge, issue closure and 8/8 normal post-merge `main` workflows.