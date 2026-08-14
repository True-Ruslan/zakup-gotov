# M3.3 WeeklyPlan → Comparison Preview — Acceptance

Date: 2026-08-14  
Status: **COMPLETE / ACCEPTED**  
Issue: #115  
Implementation PR: #116

Baseline before M3.3: `4f3c171311f25c7aa03acb54680a5d1924cdb691`  
Final reviewed feature head: `396445c333ea369bed6d428b33f38f37765eff20`  
Accepted implementation squash merge: `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`

Authoritative design: [`superpowers/specs/2026-08-14-m3-3-weekly-plan-comparison-preview-design.md`](superpowers/specs/2026-08-14-m3-3-weekly-plan-comparison-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview.md`](superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview-shipping.md`](superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview-shipping.md)

## Accepted boundary

`POST /api/v1/weekly-plan-comparison-previews`

Accepted flow:

`locality + accepted M3.2 WeeklyPlan input → accepted WeeklyPlanShoppingPreview → generated canonical ShoppingItems → accepted ComparisonPreview`

M3.3 is a stateless composition adapter. It does not create a second WeeklyPlan/Recipe/shopping/comparison algorithm.

## Accepted behavior

- request contains provider-neutral locality plus the accepted M3.2 weekly-plan input shape and no client-controlled WeeklyPlan, meal-occurrence, Recipe, ingredient, ShoppingList or ShoppingItem identities;
- `WeeklyPlanShoppingPreviewService` is invoked through its accepted application boundary and remains authoritative for planner/Recipe validation, server-owned transient identities, serving scaling, canonicalization, deterministic weekly aggregation, ShoppingList/ShoppingItem identity and self-contained planner provenance;
- generated weekly ShoppingItems cross the composition boundary in the same order with the same UUID, normalized requirement and canonical quantity;
- `ComparisonPreviewService` remains authoritative for locality validation/normalization, canonical retailer visibility, production-access gating before evidence acquisition, runtime evidence, matching, package/basket semantics, uncertainty/incompleteness and product-safe retailer projection;
- the accepted M3.2 `WeeklyPlanShoppingPreview` is returned unchanged, so public `occurrenceId + recipeId + recipeIngredientId` lineage remains self-contained and comparison never reinterprets it;
- cross-boundary cardinality, ShoppingItem identity/order, requirement and canonical-quantity drift fail closed as internal errors;
- successfully bound planner semantic failures preserve `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW`;
- successfully bound locality/comparison semantic failures preserve `INVALID_COMPARISON_PREVIEW`;
- malformed/unreadable or binding failures anywhere in the composed JSON use sanitized `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW` with one safe `$request` error and no parser/Jackson/internal exception detail;
- OpenAPI 3.1 exposes operation `createWeeklyPlanComparisonPreview`; the generated TypeScript client exposes `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH` and synchronized request/response/problem types;
- architecture guards keep the adapter on accepted application boundaries, permit only the finite canonical Shopping `Quantity` / `QuantityUnit` value bridge, prohibit direct WeeklyPlan/Recipe/provider/retailer/matching/basket/comparison-domain/database coupling and protect reverse dependency direction;
- ordinary CI remains retailer-network-free; M3.3 introduces no provider adapter, retailer activation, production-access policy change, persistence, UI, pantry semantics or database migration.

## TDD evidence

### Application composition

RED `3c1cdbdfb74e5ae407e010ab1adcfeba2ed18757`:

- service/architecture tests existed before M3.3 production types;
- API CI failed on intentionally absent M3.3 application types.

GREEN `9a0d1b6fa34e28b584747185ca56cc754eefa775`:

- minimal wrapper/service/configuration implemented;
- full API Maven verification passed with **340 tests, 0 failures, 0 errors** (5 skipped), including Spring context, Modulith and PostgreSQL/Testcontainers coverage;
- tests prove generated ShoppingItem identity/order/value preservation and fail-closed cardinality/identity/requirement/quantity drift handling.

### HTTP / problem contract

Initial RED `df30faf5dd428d2b58f79992caf38e5d0a0e8967` revealed a test-only JSON helper defect. The fixture was corrected before production HTTP code.

Corrected RED `d7415a2d5ae49937a6a85df99ee97c14819144ed`:

- API CI failed only because the intended M3.3 controller/advice types did not yet exist.

GREEN `4f1c9d70e96950cab0aef8e8ee75554b826ec4a2`:

- thin controller, controller-scoped advice and sanitized wrapper problem added;
- exact-head API CI succeeded;
- binding coverage includes malformed/empty/null JSON, unknown wrapper/nested fields, invalid day/unit enums and fractional JSON for integer serving fields.

### OpenAPI / generated client

RED `d181b15e9b14f3e0855e1eeb9781a7e7cb281c20`:

- old generated-schema freshness remained green;
- TypeScript failed exactly on the intentionally missing M3.3 path constant, path, operation, request, response and problem types.

Test-only approved-name correction `a87dcc790240d5c903e780051228bb5e40287f51` aligned the RED test with schema name `WeeklyPlanComparisonPreview` before OpenAPI implementation.

OpenAPI/client source checkpoint `b4085e47965cf7616aed83ab5c5eee676ff72fcb` intentionally left generated output stale; pinned `openapi-typescript 7.13.0` Contract CI then failed only generated freshness and supplied the authoritative generated diff.

GREEN `cf4f0e7d2ed6b767b99966aaedbed8dba883a715`:

- generated `schema.d.ts` synchronized from the pinned generator diff;
- generated blob `a56ebd3b8d499915cb2ddfeab135ac0d82008509` matched the generator target shown by CI;
- exact-head Contract CI passed generated freshness, TypeScript typecheck, Vitest and build;
- exact-head API CI also passed.

## Final PR acceptance proof

Final reviewed PR #116 head:

`396445c333ea369bed6d428b33f38f37765eff20`

On that exact head all **9/9 normal PR workflow groups succeeded**:

1. API CI — SUCCESS;
2. Contract CI — SUCCESS;
3. Web CI + responsive E2E — SUCCESS;
4. CodeQL Java + JavaScript/TypeScript — SUCCESS;
5. Dependency Review — SUCCESS;
6. Container Security API + Web — SUCCESS;
7. Retailer Bridge CI — SUCCESS;
8. Release Contract CI — SUCCESS;
9. Release Bundle CI — SUCCESS.

Read-only review on the same exact head:

- verdict: **Looks good**;
- P0: none;
- P1: none;
- P2: none;
- P3: none;
- unresolved review threads: **0**;
- review changed no repository files.

## Merge and post-merge acceptance proof

PR #116 was squash-merged with expected-head protection.

Accepted implementation merge:

`89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`

Post-merge evidence on that exact `main` SHA:

- exactly **8 normal push workflows** were created;
- **8/8 SUCCESS**;
- CodeQL Java and JavaScript/TypeScript both succeeded;
- Container Security API and Web both succeeded;
- issue #115 is closed with state reason `completed`.

Therefore M3.3 implementation is accepted as:

**implemented → tested → reviewed → merged → accepted**.

## Non-goals preserved

M3.3 does not add:

- Responsive Weekly Planning UI;
- persistence, saved plans or history;
- pantry/exclusion subtraction;
- nutrition/macros;
- calendar dates, week numbers or time-zone semantics;
- fixed meal-slot taxonomy;
- fuzzy/synonym/AI ingredient equivalence;
- retailer/provider onboarding or activation;
- production-access policy changes;
- matching/basket/ranking redesign;
- precise-address input;
- database changes.

## Decision

**M3.3 WeeklyPlan → Comparison composition is COMPLETE / ACCEPTED.**

The next deterministic product slice is **M3.4 — Responsive Weekly Planning UI**, consuming the accepted composed endpoint while preserving manual-list and Recipe journeys. Pantry/exclusion semantics and persistence remain separate later slices.

Canonical documentation synchronization is performed by the docs-only acceptance PR containing this document; its own exact-head review/CI and post-merge push proof are verified separately from implementation acceptance.
