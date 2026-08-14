# M3.3 WeeklyPlan → Comparison Preview — Shipping Evidence

Date: 2026-08-14  
Issue: #115  
PR: #116  
Status: **IMPLEMENTED / TESTED / SHIPPING — acceptance pending**

Authoritative design: `docs/superpowers/specs/2026-08-14-m3-3-weekly-plan-comparison-preview-design.md`  
Implementation plan: `docs/superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview.md`

## Delivered scope

M3.3 adds one stateless composition boundary over accepted M3.2 WeeklyPlan shopping composition and accepted M1 ComparisonPreview behavior:

`WeeklyPlan input + locality → accepted WeeklyPlanShoppingPreview → canonical generated ShoppingItems → accepted ComparisonPreview`

Delivered behavior:

- `POST /api/v1/weekly-plan-comparison-previews` returns a transient composed result and persists nothing;
- request contains provider-neutral locality plus the accepted M3.2 `WeeklyPlanShoppingPreviewRequest` shape and no client-controlled WeeklyPlan/occurrence/Recipe/ingredient/ShoppingList/ShoppingItem IDs;
- `WeeklyPlanShoppingPreviewService` remains authoritative for server-owned planner/Recipe identities, validation, servings, normalization, multi-Recipe aggregation, ShoppingList/ShoppingItem identity and self-contained planner provenance;
- M3.3 reads generated M3.2 shopping items in accepted order and forwards the same UUID, requirement and canonical quantity to `ComparisonPreviewService` without sorting, re-normalization, quantity arithmetic or merge logic;
- `ComparisonPreviewService` remains authoritative for locality validation/normalization, canonical retailer visibility, production-access gating, runtime evidence, matching, basket semantics, uncertainty/incompleteness and product-safe retailer projection;
- the response returns the accepted M3.2 `WeeklyPlanShoppingPreview` unchanged alongside the accepted `ComparisonPreview` projection;
- fail-closed composition checks require equal generated/comparison cardinality and per-index equality of ShoppingItem ID, requirement and canonical quantity;
- successfully bound planner semantic failures preserve `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW`;
- successfully bound locality/comparison semantic failures preserve `INVALID_COMPARISON_PREVIEW`;
- malformed/unreadable/binding failures anywhere in the composed JSON, including nested unknown fields, invalid enum tokens and fractional integer-serving JSON, return sanitized `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW` with one safe `$request: malformed JSON request` error;
- internal composition drift is not mapped into any user-validation `400` contract;
- OpenAPI 3.1 source, generated TypeScript client, operation ID and typed path constant are synchronized;
- ArchUnit prevents the M3.3 package from reaching WeeklyPlan/Recipe internals, provider, retailer, matching, basket, comparison-domain or database layers and prevents accepted `weeklyplanpreview` / `preview` packages from depending back on M3.3;
- no Weekly Planning UI, persistence/history, pantry/exclusions, nutrition, calendar/time-zone, fuzzy/AI ingredient semantics, retailer/provider onboarding, production-access policy change or database migration is included.

## Explicit TDD evidence

### Application composition and architecture

RED `3c1cdbdfb74e5ae407e010ab1adcfeba2ed18757`:

- M3.3 service and architecture tests were committed before production types;
- API CI failed at test compilation only on intentionally absent `WeeklyPlanComparisonPreview`, `WeeklyPlanComparisonPreviewRequest` and `WeeklyPlanComparisonPreviewService` types.

GREEN `9a0d1b6fa34e28b584747185ca56cc754eefa775`:

- minimal request/response wrapper, orchestration service and Spring wiring added;
- accepted M3.2 service is invoked through its public application boundary, then accepted ComparisonPreview is invoked through its public application boundary;
- tests prove a two-occurrence example (`0.5 LITER` scaled by servings plus `250 MILLILITER` scaled by servings) becomes one canonical `Milk 1500 MILLILITER` item with two ordered M3.2 source tuples and the same comparison item identity/value;
- explicit fail-closed tests cover cardinality, identity/order, requirement and canonical quantity drift;
- architecture tests enforce the M3.3 dependency boundary and reverse-dependency protection;
- full API/Maven verification passed with 340 tests, 0 failures, 0 errors (5 skipped), including Spring context, Modulith and PostgreSQL/Testcontainers coverage.

### HTTP / problem contract

Initial RED `df30faf5dd428d2b58f79992caf38e5d0a0e8967` exposed a test-only JSON helper defect: several intended nested binding cases did not close the occurrence object and therefore could have collapsed into generic malformed-JSON coverage.

Corrected RED `d7415a2d5ae49937a6a85df99ee97c14819144ed`:

- fixtures were corrected before production HTTP code existed;
- API CI failed at test compilation only on intentionally absent M3.3 controller/advice types.

GREEN `4f1c9d70e96950cab0aef8e8ee75554b826ec4a2`:

- thin controller, controller-scoped exception advice and sanitized wrapper Problem Detail were added;
- exact-head API CI succeeded;
- MockMvc proves composed `200` output with M3.2 provenance, M3.2 semantic problem preservation, ComparisonPreview semantic problem preservation and whole-wrapper binding sanitization;
- binding matrix includes malformed JSON, empty body, JSON `null`, unknown wrapper/nested fields, invalid day/unit enums and fractional integer serving fields;
- no Jackson/parser/internal exception detail is exposed.

### OpenAPI / generated TypeScript client

RED `d181b15e9b14f3e0855e1eeb9781a7e7cb281c20`:

- Contract CI generated-freshness check for the old schema remained green;
- TypeScript compilation then failed exactly on the intentionally missing M3.3 path constant, generated path, operation, request, response and wrapper problem types.

Test-only naming correction `a87dcc790240d5c903e780051228bb5e40287f51`:

- before changing OpenAPI, the RED test was aligned with the approved design schema name `WeeklyPlanComparisonPreview` rather than the unapproved `WeeklyPlanComparisonPreviewResponse` name.

OpenAPI checkpoint `2e99c23a2956c0d9358d9a9173eac938edf5ece0` and public path checkpoint `b4085e47965cf7616aed83ab5c5eee676ff72fcb`:

- `/api/v1/weekly-plan-comparison-previews`, operation `createWeeklyPlanComparisonPreview`, request/composed/problem schemas and 400 problem union were added using `$ref` reuse of accepted M3.2/M1 schemas;
- generated `schema.d.ts` was intentionally left stale;
- pinned Contract CI (`openapi-typescript 7.13.0`) failed only at generated freshness and supplied the authoritative generated diff.

Contract GREEN `cf4f0e7d2ed6b767b99966aaedbed8dba883a715`:

- `schema.d.ts` was synchronized literally from the pinned generator diff; its Git blob SHA is `a56ebd3b8d499915cb2ddfeab135ac0d82008509`, matching the generator target shown by CI;
- exact-head Contract CI passed generated-schema freshness, TypeScript typecheck, Vitest and package build;
- exact-head API CI also passed after the contract/client update.

## Architecture / security / network scope

M3.3 introduces no persistence, authentication/authorization policy change, precise-address input, provider adapter, retailer activation or acquisition path. It delegates retailer/runtime behavior to the already accepted ComparisonPreview application boundary, so production-access gating remains before evidence acquisition and ordinary CI remains retailer-network-free.

The M3.3 package composes only accepted application boundaries. It does not directly access WeeklyPlan/Recipe internals, provider/retailer/matching/basket/comparison-domain/database packages. Public M3.2 provenance is returned unchanged rather than copied or reinterpreted.

## Current shipping checkpoint

Production code, OpenAPI and generated client are frozen at GREEN head `cf4f0e7d2ed6b767b99966aaedbed8dba883a715`.

The shipping-evidence commit creates a new exact PR head. M3.3 remains **acceptance pending** until all of the following are proven on the final reviewed head and merged main:

1. 9/9 normal PR workflow groups SUCCESS on one exact final head;
2. read-only review reports no unresolved P0/P1/P2 blocker, target no P3 findings, and review threads are clear;
3. PR #116 is marked ready and squash-merged with expected-head protection;
4. issue #115 closes completed;
5. exactly 8 normal push workflows succeed on the implementation merge SHA;
6. canonical PROJECT_STATE/ROADMAP/CHANGELOG and dedicated acceptance evidence are synchronized in a docs-only acceptance PR;
7. the final canonical docs merge SHA also passes exactly the normal post-merge push workflow set.
