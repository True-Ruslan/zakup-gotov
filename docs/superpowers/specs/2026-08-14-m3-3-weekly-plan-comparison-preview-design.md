# M3.3 WeeklyPlan → Comparison Preview Composition — Design

Date: 2026-08-14  
Issue: #115  
Baseline: `main=4f3c171311f25c7aa03acb54680a5d1924cdb691`  
Status: **AUTHORITATIVE DESIGN — approved by user on 2026-08-14**

## Goal

Deliver the next deterministic M3 vertical slice:

`WeeklyPlan + locality → accepted M3.2 WeeklyPlanShoppingPreview → canonical generated shopping requirements → accepted ComparisonPreview`

M3.3 composes two already accepted stateless application boundaries without duplicating WeeklyPlan, Recipe, Shopping, retailer-comparison, basket or runtime-evidence semantics.

M3.2 remains the planner-only shopping-preview boundary. M1 `ComparisonPreviewService` remains the only owner of locality validation, production-access gating, runtime evidence acquisition, matching, basket planning and retailer comparison projection.

## Chosen approach

Create a dedicated composition adapter and endpoint over accepted M3.2 + ComparisonPreview application services.

Rejected alternatives:

1. **Compose directly from WeeklyPlan/M3.1 domain.** This would create a second public path for planner identity/projection semantics and duplicate behavior already accepted in M3.2.
2. **Generalize ComparisonPreview into a polymorphic source-payload endpoint.** This would redesign an accepted M1 contract before there is evidence that the additional abstraction is needed.
3. **Extend M3.2 with locality/comparison fields.** This would contaminate the accepted planner-only boundary with retailer/runtime concerns and make shopping-only preview harder to reuse.

## Public endpoint

`POST /api/v1/weekly-plan-comparison-previews`  
Operation ID: `createWeeklyPlanComparisonPreview`  
Success: `200 OK` because the result is transient and not persisted.

Request:

```json
{
  "locality": "Москва",
  "weeklyPlan": {
    "occurrences": [
      {
        "day": "MONDAY",
        "targetServings": 4,
        "recipe": {
          "title": "Паста",
          "baseServings": 2,
          "ingredients": [
            {
              "requirement": "Молоко",
              "quantity": {
                "amount": 0.5,
                "unit": "LITER"
              }
            }
          ]
        }
      }
    ]
  }
}
```

The nested `weeklyPlan` value reuses the accepted M3.2 `WeeklyPlanShoppingPreviewRequest` shape. The request contains no client-controlled WeeklyPlan, occurrence, Recipe, ingredient, ShoppingList or ShoppingItem UUIDs.

Response:

```json
{
  "weeklyPlanShoppingPreview": {
    "weeklyPlan": {
      "id": "...",
      "occurrences": [ ... ]
    },
    "shoppingList": {
      "id": "...",
      "items": [ ... ]
    }
  },
  "comparisonPreview": {
    "locality": "Москва",
    "items": [ ... ],
    "retailers": [ ... ]
  }
}
```

The response reuses the accepted M3.2 `WeeklyPlanShoppingPreview` and M1 `ComparisonPreview` projections. M3.3 does not introduce a third representation of WeeklyPlan, ShoppingList or retailer comparison state.

## Application data flow

Create package:

`io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview`

Primary service flow:

1. `WeeklyPlanComparisonPreviewController` receives `WeeklyPlanComparisonPreviewRequest(locality, weeklyPlan)`.
2. `WeeklyPlanComparisonPreviewService` rejects a null wrapper request.
3. The service calls `WeeklyPlanShoppingPreviewService.create(request.weeklyPlan())` **exactly once**.
4. It reads the accepted M3.2 `weeklyPlanShoppingPreview.shoppingList().items()` in returned order.
5. For every generated weekly shopping item, it creates one `ComparisonPreviewItemRequest` with:
   - the same UUID;
   - the same requirement text;
   - the same canonical quantity amount;
   - the same canonical quantity unit.
6. It calls `ComparisonPreviewService.create(new ComparisonPreviewRequest(request.locality(), items))` **exactly once**.
7. It verifies cross-boundary invariants.
8. It returns `WeeklyPlanComparisonPreview(weeklyPlanShoppingPreview, comparisonPreview)`.

No sorting, normalization, serving scaling, ingredient merge, quantity arithmetic, ShoppingItem-ID generation, provenance conversion, matching, basket calculation, retailer filtering, provider invocation or runtime-evidence acquisition occurs in M3.3 itself.

## Cross-boundary invariants

M3.3 fails closed if the accepted comparison boundary unexpectedly drifts from the generated M3.2 shopping projection.

Let `generated = weeklyPlanShoppingPreview.shoppingList.items` and `compared = comparisonPreview.items`.

Required invariants:

1. `generated.size == compared.size`.
2. For every index `i`, `generated[i].id == compared[i].id`.
3. For every index `i`, `generated[i].requirement == compared[i].requirement`.
4. For every index `i`, `generated[i].quantity == compared[i].quantity`.
5. Order is therefore preserved exactly as emitted by accepted M3.2/M3.1/M2.5 composition.

Invariant violations are internal server failures. They must not be converted into user-facing validation `400` responses.

The composition service does **not** re-validate M3.2 provenance. M3.2 already guarantees that every public source tuple resolves inside the returned WeeklyPlan response. The full `weeklyPlanShoppingPreview` object is returned unchanged, so its self-contained provenance remains authoritative and comparison does not duplicate or reinterpret it.

M3.3 adds no separate public correlation/request ID. Existing transient WeeklyPlan/occurrence/Recipe/ingredient/ShoppingList/ShoppingItem identities already provide the identity needed by the future UI and diagnostics.

## Locality and retailer/runtime semantics

Locality belongs to the M3.3 wrapper because accepted M3.2 is intentionally location-independent while comparison requires provider-neutral locality.

M3.3 does not normalize or validate locality itself. It passes the locality unchanged to `ComparisonPreviewService`; the accepted ComparisonPreview request factory remains authoritative for locality semantics and limits.

Existing ComparisonPreview behavior remains authoritative for:

- canonical retailer registry visibility;
- production-access readiness filtering before evidence acquisition;
- no live acquisition for non-production-ready retailers;
- runtime evidence validation;
- matching ambiguity;
- whole-package basket semantics;
- complete/uncertain/incomplete/unavailable comparison states;
- freshness and availability evidence;
- retailer totals and item selections.

Ordinary CI and browser tests remain retailer-network-free. M3.3 adds no provider adapter and no fallback fixture behavior.

## Error contract

Transport binding and application semantic validation are intentionally separate.

### Nested WeeklyPlan semantic failures

After the composed JSON has bound successfully, errors emitted while constructing the accepted M3.2 weekly shopping preview remain the existing M3.2 problem contract:

- code: `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW`;
- status: `400`;
- existing M3.2 validation paths/messages remain authoritative.

M3.3 must not relabel successfully bound M3.2 semantic validation failures as generic composition errors.

### Locality/comparison semantic failures

After successful request binding and M3.2 projection, errors produced by the accepted comparison request/application boundary remain the existing comparison problem contract:

- code: `INVALID_COMPARISON_PREVIEW`;
- status: `400`.

This includes blank/over-limit locality and any accepted comparison-request semantic validation.

### Composed JSON binding failures

The M3.3 controller owns transport binding for the entire composed request. Any unreadable or binding failure anywhere in that JSON returns one sanitized M3.3 wrapper problem. This includes:

- malformed JSON;
- an empty/missing request body;
- JSON literal `null`;
- unknown fields at the M3.3 wrapper level;
- unknown fields nested under `weeklyPlan`, occurrences, Recipes, ingredients or quantities;
- invalid enum tokens such as an unknown day/unit;
- fractional JSON supplied for integer serving fields;
- other Jackson binding failures before application semantic validation begins.

Problem contract:

- type: `https://zakup-gotov.dev/problems/invalid-weekly-plan-comparison-preview`;
- title: `Invalid weekly plan comparison preview request`;
- status: `400`;
- code: `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW`;
- one safe `$request` error;
- no Jackson class names, parser details, stack traces or internal exception messages.

The existing M3.2 problem contract applies only to M3.2 **semantic validation exceptions after successful composed-request binding**, not to Jackson binding failures encountered by the M3.3 controller.

Internal composition invariant failures remain server errors and are never mapped to one of the user-validation problem contracts.

## Architecture

New package dependencies:

- `weeklyplancomparisonpreview → weeklyplanpreview`;
- `weeklyplancomparisonpreview → preview`.

The adapter composes public application DTOs/services rather than lower-level domain internals.

Forbidden direct dependencies:

- `weeklyplan` domain;
- `recipe` domain;
- `recipepreview` application package;
- `provider`;
- `retailer`;
- `matching`;
- `basket`;
- `comparison` domain;
- database/persistence packages.

### Canonical Shopping value bridge

As in accepted M2.3, public application DTO signatures expose canonical Shopping quantity values. The M3.3 adapter may directly reference only the canonical value types required to bridge those DTOs:

- `io.github.trueruslan.zakupgotov.shopping.Quantity`;
- `io.github.trueruslan.zakupgotov.shopping.QuantityUnit`.

The adapter must not depend directly on `ShoppingList`, `ShoppingItem`, `ShoppingRequirement`, Shopping IDs, repositories or other Shopping types.

ArchUnit will enforce this finite allow-list and also ensure accepted `weeklyplanpreview` and `preview` packages do not acquire reverse dependencies on `weeklyplancomparisonpreview`.

## Spring composition

A small `WeeklyPlanComparisonPreviewConfiguration` wires the accepted `WeeklyPlanShoppingPreviewService` and `ComparisonPreviewService` into the new composition service.

No new runtime-evidence source, provider adapter, retailer registry implementation, persistence component or domain service is introduced.

The controller remains a thin transport adapter:

`HTTP request → composition service → composed response`.

## OpenAPI and generated TypeScript client

`openapi/zakup-gotov.yaml` remains the source of truth.

Add:

- path `/api/v1/weekly-plan-comparison-previews`;
- operation ID `createWeeklyPlanComparisonPreview`;
- `WeeklyPlanComparisonPreviewRequest`;
- `WeeklyPlanComparisonPreview`;
- `InvalidWeeklyPlanComparisonPreviewProblem`.

The wrapper schemas reuse/reference accepted M3.2 WeeklyPlan shopping-preview and M1 comparison schemas rather than copying nested definitions. Wrapper objects use `additionalProperties: false` and explicit required fields.

Export from the generated/client surface:

```ts
export const WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH = "/api/v1/weekly-plan-comparison-previews" as const;
```

`packages/api-client/src/schema.d.ts` remains generated output only. Contract CI must prove byte-for-byte freshness using the repository-pinned `openapi-typescript` version, then pass TypeScript typecheck, Vitest and package build.

## Verification strategy

Every behavior-changing production step begins with a failing test against the branch.

### Cycle 1 — Composition service RED → GREEN

Tests prove:

- M3.2 service called exactly once;
- comparison service called exactly once;
- locality forwarded unchanged;
- generated weekly ShoppingItem UUID/order/requirement/canonical quantity are passed unchanged;
- returned object contains the exact accepted nested projections.

### Cycle 2 — Fail-closed invariant RED → GREEN

Explicit tests inject structurally possible drift and require failure for:

- cardinality mismatch;
- UUID/order mismatch;
- requirement mismatch;
- canonical quantity mismatch.

### Cycle 3 — HTTP/problem RED → GREEN

MockMvc/controller tests prove:

- `200` composed success response;
- M3.2 semantic problem propagation after successful JSON binding;
- comparison/locality semantic problem propagation;
- malformed, missing, JSON-`null`, unknown wrapper/nested fields, invalid enum tokens and fractional integer-serving JSON become sanitized `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW`;
- internal invariant failures are not converted to `400`.

### Cycle 4 — OpenAPI/client RED → GREEN

Contract tests first reference the missing endpoint/path/schemas, then schema/client generation is implemented and regenerated output verified clean.

### Cycle 5 — Architecture/full regression

ArchUnit proves allowed dependencies and reverse-dependency protection. Full API Maven verification includes Spring context, Modulith and PostgreSQL/Testcontainers baseline.

## Acceptance gate

Before merge, the exact final PR head must pass all normal repository PR workflow groups:

1. API CI;
2. Contract CI;
3. Web CI + responsive E2E;
4. CodeQL Java + JavaScript/TypeScript;
5. Dependency Review;
6. Container Security API + Web;
7. Retailer Bridge CI;
8. Release Contract CI;
9. Release Bundle CI.

Independent read-only review must report no unresolved P0/P1/P2 findings; target is no P3 findings and no unresolved review threads.

The reviewed exact head is squash-merged with expected-head protection. M3.3 is marked **COMPLETE / ACCEPTED** only after the resulting `main` SHA has exactly the normal push workflow set and all workflows succeed.

After implementation acceptance, a docs-only acceptance PR updates:

- `docs/PROJECT_STATE.md`;
- `docs/ROADMAP.md`;
- `CHANGELOG.md`;
- dedicated M3.3 acceptance evidence.

## Non-goals

M3.3 does not include:

- Weekly Planning UI;
- persistence, saved plans or history;
- pantry/exclusion subtraction;
- nutrition/macros;
- calendar dates, week numbers or time zones;
- fixed meal-slot taxonomy;
- fuzzy/synonym/AI ingredient equivalence;
- retailer/provider onboarding or activation;
- changes to production-access policy;
- matching/basket/ranking redesign;
- exact-address input;
- database changes;
- changes to accepted M3.2 shopping-preview semantics.

## Next slice

After M3.3 acceptance, proceed to **M3.4 — Responsive Weekly Planning UI**, consuming the composed endpoint as the primary WeeklyPlan product boundary while preserving manual-list and Recipe journeys.