# M3.3 WeeklyPlan → Comparison Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a stateless WeeklyPlan → Comparison application/API composition that preserves accepted planner provenance and generated shopping-item identity while delegating all WeeklyPlan shopping composition and retailer comparison semantics to accepted boundaries.

**Architecture:** Introduce `weeklyplancomparisonpreview` as a thin orchestration package depending primarily on `weeklyplanpreview` and `preview`. Because those accepted public DTOs expose canonical Shopping values, the composer may additionally depend only on `shopping.Quantity` and `shopping.QuantityUnit`; ArchUnit must reject every other direct Shopping/domain/downstream dependency. The composer invokes M3.2 once, maps its canonical generated shopping items into the accepted ComparisonPreview request without semantic changes, invokes ComparisonPreview once, verifies cross-boundary identity/order/value invariants, and returns both accepted projections unchanged.

**Tech Stack:** Java 25, Spring Boot 4.1/Spring MVC, JUnit 5/AssertJ/MockMvc/ArchUnit, OpenAPI 3.1, generated TypeScript client, pnpm/Vitest, existing GitHub Actions matrix.

## Global Constraints

- Baseline: `main=4f3c171311f25c7aa03acb54680a5d1924cdb691`.
- Endpoint: `POST /api/v1/weekly-plan-comparison-previews`, operation `createWeeklyPlanComparisonPreview`, `200 OK`.
- Request: `WeeklyPlanComparisonPreviewRequest(String locality, WeeklyPlanShoppingPreviewRequest weeklyPlan)` with no client-controlled IDs.
- Response: `WeeklyPlanComparisonPreview(WeeklyPlanShoppingPreview weeklyPlanShoppingPreview, ComparisonPreview comparisonPreview)`.
- Primary direct dependencies: `weeklyplanpreview` and `preview`; finite canonical Shopping value bridge: only `Quantity` and `QuantityUnit`.
- No direct `weeklyplan`, `recipe`, `recipepreview`, provider, retailer, matching, basket, comparison-domain, database/persistence dependency and no other Shopping type dependency.
- Call `WeeklyPlanShoppingPreviewService#create(...)` exactly once and `ComparisonPreviewService#create(...)` exactly once.
- Preserve generated ShoppingItem UUID, order, requirement and canonical quantity exactly into comparison.
- Return the accepted M3.2 `WeeklyPlanShoppingPreview` unchanged so its occurrence/Recipe/ingredient provenance remains self-contained and authoritative.
- Do not duplicate WeeklyPlan/Recipe validation, scaling, aggregation, provenance, matching, basket, retailer visibility or production-access semantics.
- Locality is forwarded unchanged to ComparisonPreview; accepted comparison validation remains authoritative.
- Any JSON/binding failure anywhere in the composed request maps to sanitized `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW`; successfully bound M3.2 semantic failures remain `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW`; comparison semantic failures remain `INVALID_COMPARISON_PREVIEW`.
- Internal composition invariant drift remains an internal server error and is never converted to user-validation `400`.
- Ordinary CI makes no live retailer calls.
- OpenAPI is source of truth; generated `schema.d.ts` must be proven current by pinned regeneration plus clean diff.
- No UI, persistence, pantry, fuzzy/AI matching, retailer activation/provider changes or database migration in M3.3.

---

### Task 1: Lock the composed service contract with RED tests

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewServiceTest.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewArchitectureTest.java`

**Interfaces:**
- Consumes: `WeeklyPlanShoppingPreviewService#create(WeeklyPlanShoppingPreviewRequest)` and `ComparisonPreviewService#create(ComparisonPreviewRequest)`.
- Produces: desired `WeeklyPlanComparisonPreviewService#create(WeeklyPlanComparisonPreviewRequest)` behavior.

- [ ] **Step 1: Write service RED test** constructing an accepted M3.2 weekly plan with at least two ordered occurrences whose ingredients aggregate into deterministic generated shopping items; assert the future composed response preserves the exact M3.2 projection and comparison receives the same generated item UUID/order/requirement/canonical quantity.
- [ ] **Step 2: Add call-count seams in the test only** around accepted application services so the test proves M3.2 and ComparisonPreview are each invoked exactly once; do not add test-only production hooks.
- [ ] **Step 3: Write architecture RED test** asserting `weeklyplancomparisonpreview` may depend on `weeklyplanpreview`, `preview`, Java/Spring/Jackson, and only `shopping.Quantity` / `shopping.QuantityUnit`; forbid `weeklyplan`, `recipe`, `recipepreview`, provider, retailer, matching, basket, comparison-domain, persistence/database and every other Shopping type.
- [ ] **Step 4: Commit only RED tests** before any M3.3 production type exists.
- [ ] **Step 5: Execute RED through API CI on the exact commit** and record the expected failure caused by absent `WeeklyPlanComparisonPreview*` production types rather than syntax/test-fixture errors.

### Task 2: Implement minimal application composition GREEN and fail-closed invariants

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreview.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewService.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewConfiguration.java`
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewServiceTest.java`

**Interfaces:**
- `WeeklyPlanComparisonPreviewRequest(String locality, WeeklyPlanShoppingPreviewRequest weeklyPlan)`.
- `WeeklyPlanComparisonPreview(WeeklyPlanShoppingPreview weeklyPlanShoppingPreview, ComparisonPreview comparisonPreview)`.
- `WeeklyPlanComparisonPreviewService(WeeklyPlanShoppingPreviewService, ComparisonPreviewService)`.
- `WeeklyPlanComparisonPreview create(WeeklyPlanComparisonPreviewRequest request)`.
- package-visible/static `verifyComposition(WeeklyPlanShoppingPreview, ComparisonPreview)` for direct fail-closed regression assertions, following accepted M2.3 structure.

- [ ] **Step 1: Implement wrapper records and service constructor** with null-safe dependency checks and no new business semantics.
- [ ] **Step 2: Implement exact-once M3.2 delegation** using `weeklyPlanShoppingPreviewService.create(request.weeklyPlan())`.
- [ ] **Step 3: Map every returned M3.2 shopping item in existing order** to `ComparisonPreviewItemRequest(item.id(), item.requirement(), new ComparisonPreviewQuantityRequest(item.quantity().amount(), item.quantity().unit()))`.
- [ ] **Step 4: Delegate exact-once comparison** with `new ComparisonPreviewRequest(request.locality(), comparisonItems)` and return both accepted projections.
- [ ] **Step 5: Implement `verifyComposition`** requiring equal cardinality and per-index equality of item ID, requirement and canonical `Quantity`; throw stable `IllegalStateException` messages for cardinality, identity/order, requirement and quantity drift.
- [ ] **Step 6: Add explicit invariant regression assertions** injecting cardinality mismatch, reordered/different ID, changed requirement and changed canonical quantity.
- [ ] **Step 7: Wire `WeeklyPlanComparisonPreviewConfiguration`** from the accepted `WeeklyPlanShoppingPreviewService` and `ComparisonPreviewService` beans only.
- [ ] **Step 8: Execute full API CI/Maven verification on the exact GREEN commit** and require service tests, ArchUnit, Spring context, Modulith and PostgreSQL/Testcontainers baseline to pass.
- [ ] **Step 9: Commit the minimal application GREEN slice** and record RED→GREEN SHAs in the PR body later.

### Task 3: Add HTTP/binding/problem contract RED → GREEN

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewControllerTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewController.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/WeeklyPlanComparisonPreviewExceptionHandler.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplancomparisonpreview/InvalidWeeklyPlanComparisonPreviewProblem.java`

**Interfaces:**
- `@PostMapping("/api/v1/weekly-plan-comparison-previews")` delegates only to `WeeklyPlanComparisonPreviewService#create`.
- Any `HttpMessageNotReadableException` raised while binding the complete composed JSON maps to `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW` with one safe `$request` error.
- `InvalidWeeklyPlanShoppingPreviewRequestException` maps through the accepted `InvalidWeeklyPlanShoppingPreviewProblem` unchanged.
- `InvalidComparisonPreviewRequestException` maps through accepted `InvalidComparisonPreviewProblem` unchanged.
- `IllegalStateException` from internal composition drift is not caught by the controller advice.

- [ ] **Step 1: Write controller RED success test** for the exact endpoint, `200`, nested `weeklyPlanShoppingPreview`, nested `comparisonPreview`, and preserved generated item identity/provenance.
- [ ] **Step 2: Write semantic-problem RED tests** proving a successfully bound invalid weekly plan remains `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW` and invalid locality remains `INVALID_COMPARISON_PREVIEW`.
- [ ] **Step 3: Write transport-binding RED matrix** for malformed JSON, empty body, JSON `null`, unknown wrapper field, unknown nested WeeklyPlan/Recipe/quantity field, invalid day/unit enum and fractional integer servings; each must be `400 application/problem+json` with code `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW`, one `$request` error, and no parser/Jackson/internal detail leakage.
- [ ] **Step 4: Write internal-drift HTTP regression** proving an injected `IllegalStateException` is not mapped to a user-validation `400`.
- [ ] **Step 5: Commit controller tests only and execute API CI RED**; require failure because controller/advice/problem types are absent or behavior is unimplemented.
- [ ] **Step 6: Implement thin controller, sanitized problem record and controller-scoped advice** matching the approved error partition exactly.
- [ ] **Step 7: Execute focused/full API CI GREEN** and require all HTTP/problem tests plus existing M3.2/M2.3 regressions to pass.
- [ ] **Step 8: Commit the HTTP GREEN slice** and record RED→GREEN SHAs.

### Task 4: Add OpenAPI/generated-client contract RED → GREEN

**Files:**
- Modify: `packages/api-client/src/index.test.ts`
- Modify: `openapi/zakup-gotov.yaml`
- Modify: `packages/api-client/src/index.ts`
- Regenerate/verify: `packages/api-client/src/schema.d.ts`

**Interfaces:**
- Path: `/api/v1/weekly-plan-comparison-previews`.
- Operation: `createWeeklyPlanComparisonPreview`.
- Request schema: `WeeklyPlanComparisonPreviewRequest`, `additionalProperties: false`, required `locality` + `weeklyPlan`, where `weeklyPlan` references accepted `WeeklyPlanShoppingPreviewRequest`.
- Response schema: `WeeklyPlanComparisonPreview`, `additionalProperties: false`, required `weeklyPlanShoppingPreview` + `comparisonPreview`, referencing accepted M3.2/M1 schemas.
- Problem schema: `InvalidWeeklyPlanComparisonPreviewProblem`.
- 400 response documents wrapper binding, M3.2 semantic and Comparison semantic problem variants.
- Export: `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH`.

- [ ] **Step 1: Add TypeScript RED assertions** for the missing path constant, operation and generated request/response schemas before changing OpenAPI/client implementation.
- [ ] **Step 2: Commit contract RED and execute Contract CI**; require generated freshness to remain coherent for the old schema while typecheck/tests fail because M3.3 types/path are absent.
- [ ] **Step 3: Add the OpenAPI path and wrapper/problem schemas** exclusively by `$ref`-reusing accepted M3.2/M1 nested schemas rather than copying them.
- [ ] **Step 4: Add `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH`** to the public client surface.
- [ ] **Step 5: Synchronize `schema.d.ts` only as generated output** from repository-pinned `openapi-typescript`; if execution environment cannot run generation, use pinned Contract CI generated-freshness as the authoritative byte-equivalence proof and commit only the exact generated delta.
- [ ] **Step 6: Execute Contract CI GREEN** requiring generated-schema freshness, TypeScript typecheck, Vitest and package build all pass on the same exact head.
- [ ] **Step 7: Commit the contract/client GREEN slice** and record RED→GREEN SHAs.

### Task 5: Full regression, review and shipping gate

**Files:**
- Create: `docs/superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview-shipping.md`
- Modify implementation files only when an exact failing check/review finding proves a defect.

- [ ] **Step 1: Run/inspect exact-head API CI** and require full Maven `verify`, Spring context, Modulith and PostgreSQL/Testcontainers baseline green.
- [ ] **Step 2: Run/inspect exact-head Contract CI** and require generated freshness, client typecheck/tests/build green.
- [ ] **Step 3: Require Web CI + responsive E2E green** with no M3.3 UI/product claim and no live retailer traffic.
- [ ] **Step 4: Require every normal PR workflow group green on the exact final head:** API CI; Contract CI; Web CI + responsive E2E; CodeQL Java + JavaScript/TypeScript; Dependency Review; Container Security API + Web; Retailer Bridge CI; Release Contract CI; Release Bundle CI.
- [ ] **Step 5: Fix only evidence-backed failures**; any behavior-changing fix starts with a new failing regression test and preserves a RED→GREEN checkpoint.
- [ ] **Step 6: Perform independent read-only PR review on the exact final head** and require no unresolved P0/P1/P2; target no P3 and zero unresolved review threads.
- [ ] **Step 7: Write shipping evidence** with exact RED/GREEN/final SHAs, workflow group results, review verdict, scope and non-goals.
- [ ] **Step 8: Mark PR #116 ready only after exact-head checks and review are clean.**
- [ ] **Step 9: Squash merge PR #116 with expected-head protection.**
- [ ] **Step 10: Verify the resulting implementation `main` SHA has exactly the normal post-merge push workflow set and all runs succeed.**
- [ ] **Step 11: Confirm issue #115 is closed `completed` only after implementation acceptance proof.**

### Task 6: Canonical acceptance documentation

**Files:**
- Create: `docs/m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Branch from the accepted implementation `main` SHA** after Task 5 post-merge verification; do not document M3.3 as accepted before this point.
- [ ] **Step 2: Add dedicated acceptance evidence** containing final feature PR head, squash-merge SHA, exact 9/9 PR workflow proof, exact post-merge push proof, read-only review verdict and preserved non-goals.
- [ ] **Step 3: Update PROJECT_STATE/ROADMAP/CHANGELOG** from M3.3 NEXT/in-progress to `M3.3 — COMPLETE / ACCEPTED` and set the next deterministic target to `M3.4 — Responsive Weekly Planning UI`; preserve historical M3.1/M3.2 evidence.
- [ ] **Step 4: Open a docs-only acceptance PR** and require the same 9 normal PR workflow groups green on its exact final head plus read-only review with no unresolved findings.
- [ ] **Step 5: Squash merge the docs PR with expected-head protection.**
- [ ] **Step 6: Verify the final canonical `main` SHA has exactly the normal post-merge push workflow set and all runs succeed before reporting M3.3 documented/fully complete.**

## Self-review

- Spec coverage: endpoint, exact-once delegation, locality ownership, planner provenance preservation, identity/order/value bridge, fail-closed drift handling, transport-vs-semantic error partition, architecture constraints, OpenAPI/client and acceptance gates each map to explicit tasks.
- Placeholder scan: no TODO/TBD or unspecified implementation/error/test steps remain.
- Type consistency: `WeeklyPlanComparisonPreviewRequest`, `WeeklyPlanComparisonPreview`, `WeeklyPlanComparisonPreviewService`, endpoint, operation ID and client path constant are named identically across tasks.
- The finite Shopping value bridge is explicit and architecture-tested; no broad Shopping-domain dependency is implied.
- Scope remains one independently shippable deterministic application/API slice; responsive planner UI remains M3.4 and pantry/persistence remain later work.
- Acceptance terminology remains strict: implementation is not `COMPLETE / ACCEPTED` until exact post-merge main workflows pass, and canonical documentation follows in its own reviewed/verified PR.
