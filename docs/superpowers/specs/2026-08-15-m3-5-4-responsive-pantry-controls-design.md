# M3.5.4 Responsive Pantry Controls — Design

Date: 2026-08-15
Status: Authoritative implementation design
Issue: #130
Baseline: `3e030b787ea0afd807f9a8ffcdb167ed46a491f7`

## Goal

Extend the accepted WeeklyPlan-first browser journey with request-scoped Pantry input and an inspectable server-owned flow:

**weekly meals → original weekly demand → Pantry coverage → remaining demand → retailer comparison**.

M3.5.4 is presentation/orchestration only. It consumes the generated M3.5.3 API contract and must not reimplement Pantry, Shopping, WeeklyPlan or Comparison semantics in browser code.

## Existing accepted seams

M3.4 already has:

- `weekly-plan-comparison.ts` server action with a 3-second fail-closed transport;
- `weekly-plan-comparison-form.tsx` for locality + ordered WeeklyPlan/Recipe editing;
- `weekly-plan-comparison-results.tsx` for canonical weekly shopping + retailer comparison;
- component tests for transport/form/results/page behavior;
- deterministic `apps/web/e2e/mock-api.mjs` and `home.spec.ts` browser acceptance;
- Recipe/manual-list journeys that must remain intact.

M3.5.3 now provides generated request/response vocabulary for Pantry-aware comparison:

- `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH`;
- `WeeklyPlanPantryComparisonPreviewRequest`;
- `WeeklyPlanPantryItemInput`;
- `WeeklyPlanPantryComparisonPreview`;
- `PantryAdjustmentStatus` = `UNCHANGED | PARTIALLY_COVERED | FULLY_COVERED`;
- `WeeklyPlanPantryComparisonOutcome` = `COMPARED | NO_REMAINING_DEMAND`.

## Chosen design

### 1. Evolve the existing WeeklyPlan-first UI instead of creating a parallel Pantry journey

The current WeeklyPlan form remains the primary browser entry point. M3.5.4 changes its transport boundary from generated M3.3 comparison to generated M3.5.3 Pantry-aware comparison and adds one plan-level Pantry editor.

Reasons:

- preserves accepted M3.4 interaction model and responsive layout;
- avoids two nearly identical WeeklyPlan forms;
- keeps Pantry visibly subordinate to the weekly plan rather than introducing a separate product mode;
- M3.3 backend remains unchanged and independently available; only the accepted primary browser journey advances to M3.5.3.

### 2. Pantry input is explicit request-scoped form state

A Pantry row contains only:

- browser-local numeric `key` for React list rendering;
- requirement text;
- positive decimal amount text;
- generated `QuantityInputUnit`.

The local key is never serialized.

Pantry rows are optional. The initial form contains no Pantry rows, so the existing no-Pantry WeeklyPlan journey remains a natural identity case. Users can add/remove Pantry rows explicitly.

No persistence, history, saved Pantry, household profile or automatic Pantry inference is introduced.

### 3. Browser validation is preflight only

Client validation checks only obvious form shape:

- locality non-empty;
- existing accepted WeeklyPlan fields remain valid;
- each present Pantry row has non-empty requirement and finite positive quantity.

The server remains authoritative for requirement normalization, quantity canonicalization, exact matching, duplicate Pantry aggregation and subtraction.

The browser must not merge duplicate Pantry rows, convert kg↔g or l↔ml, subtract quantities, or infer matches.

### 4. Server action owns generated M3.5.3 transport

The existing WeeklyPlan server action is evolved/renamed to Pantry-aware terminology and must:

- use only `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH`;
- accept `WeeklyPlanPantryComparisonPreviewRequest`;
- keep the finite 3-second timeout;
- preserve product-safe generated 400 validation fields/messages;
- return `ready | invalid | unavailable` only;
- fail closed on missing API configuration, timeout, network error, malformed/unexpected non-400 response or any unrecognized failure.

No fallback to M3.3 or deterministic fixtures is allowed in production code.

### 5. Results are rendered in server-owned order

For a successful response, render four conceptual stages in this order:

1. **Original weekly demand** — `pantryShoppingPreview.originalShoppingList.items` in server order.
2. **Pantry coverage** — `pantryShoppingPreview.pantryAdjustments` in server order.
3. **Remaining to buy** — `pantryShoppingPreview.remainingShoppingList.items` in server order.
4. **Retailer comparison** — only when `comparisonOutcome === "COMPARED"`, from `comparisonPreview`.

User-facing Pantry audit rows show requirement, required quantity, Pantry-used quantity when present, remaining quantity when present, and a localized status label. They do not expose `itemId`.

Server-generated WeeklyPlan/Recipe/Shopping UUIDs and planner provenance remain hidden from ordinary UI.

### 6. Zero-demand is a first-class terminal state

When `comparisonOutcome === "NO_REMAINING_DEMAND"`:

- show that Pantry covers the complete weekly shopping demand;
- render the original demand and Pantry coverage evidence;
- render an explicit empty remaining-demand state;
- do not render `ComparisonPreviewResults` or fabricated retailer cards.

The browser does not infer zero demand from list length; it respects the explicit M3.5.3 outcome.

### 7. Fail closed on impossible response combinations

Because the generated OpenAPI type makes `comparisonPreview` optional, the presentation boundary must defensively reject/avoid rendering contradictory states:

- `COMPARED` without `comparisonPreview` → unavailable/fail-closed presentation, never fabricated retailer output;
- `NO_REMAINING_DEMAND` with a comparison payload → do not render that payload;
- unexpected outcome or malformed runtime value → fail closed.

The browser may trust server-owned shopping/audit values but must not synthesize missing comparison evidence.

### 8. Responsive/accessibility behavior

Pantry controls follow the existing WeeklyPlan visual language:

- plan-level fieldset/card after weekly meals and before submit;
- each Pantry row uses requirement + amount + unit + remove action;
- add button is keyboard reachable;
- inputs keep visible labels and minimum interactive height consistent with M3.4;
- mobile 390px layout collapses safely with no horizontal overflow;
- results use stacked cards/grid transitions already established by the project.

No redesign of the overall page shell is required.

## Deterministic browser fixture strategy

`apps/web/e2e/mock-api.mjs` gains a deterministic M3.5.3 route only for browser acceptance. It must never perform retailer network access.

Fixture scenarios are selected from request content in a deterministic, product-safe way:

- empty Pantry / partial Pantry → `COMPARED` response with original/audit/remaining/comparison evidence;
- a dedicated fully-covering Pantry fixture → `NO_REMAINING_DEMAND` with no `comparisonPreview` property;
- unavailable mode retains fail-closed transport coverage.

The fixture must not introduce provider credentials, proprietary raw retailer payloads or server identities into user-visible assertions.

## Test strategy

### Transport RED→GREEN

Prove:

- generated M3.5.3 path is called;
- Pantry request rows are forwarded unchanged apart from form-level trimming/number conversion performed before the action;
- generated product-safe 400 errors are preserved;
- missing config/network/timeout/unexpected failure returns unavailable;
- no M3.3 fallback.

### Form RED→GREEN

Prove:

- existing WeeklyPlan editing/reorder behavior remains;
- no Pantry rows by default;
- add/remove Pantry rows;
- requirement/quantity/unit editing;
- invalid Pantry row blocks submit with product-safe client message;
- local row keys are absent from submitted request;
- submit includes `pantry: []` for the identity case.

### Results RED→GREEN

Prove:

- original demand uses server response directly;
- audit statuses/used/remaining quantities are rendered without exposing item IDs;
- remaining demand uses server response directly;
- `COMPARED` renders accepted retailer comparison;
- `NO_REMAINING_DEMAND` shows terminal state and no retailer comparison;
- contradictory `COMPARED` without payload fails closed.

### Page/browser acceptance

Desktop:
- enter weekly meal + Pantry stock;
- submit;
- observe original → Pantry coverage → remaining → retailer comparison.

Zero demand:
- fully covering Pantry;
- observe `NO_REMAINING_DEMAND` state;
- no retailer cards.

Mobile/accessibility:
- 390×844 no horizontal overflow;
- Pantry add/remove controls and submit reachable by keyboard;
- visible focus;
- labels exposed to accessibility tree.

Regression:
- existing Recipe journey passes;
- existing manual-list journey passes;
- WeeklyPlan ordering/reorder behavior passes.

## Non-goals

- Pantry persistence or saved household state;
- explicit `never buy` / omit-all exclusions;
- fuzzy/synonym/AI Pantry matching;
- browser-side canonicalization/subtraction;
- provider/acquisition changes;
- comparison/basket/winner recomputation;
- new retailer integrations;
- redesign of Recipe/manual-list journeys.

## Acceptance gate

M3.5.4 may be accepted only after:

1. transport/form/results/page/browser RED→GREEN evidence;
2. generated M3.5.3 contract is the only WeeklyPlan Pantry comparison transport used by the primary browser journey;
3. deterministic E2E makes no live retailer request;
4. relevant web typecheck/Vitest/build/Playwright suites pass;
5. exact final PR head has all 9 normal workflow groups SUCCESS with clean review and no unresolved threads;
6. squash merge with expected-head protection;
7. exact merge SHA has all 8 normal post-merge push workflows SUCCESS;
8. canonical acceptance/state/roadmap/changelog are updated separately after implementation acceptance.
