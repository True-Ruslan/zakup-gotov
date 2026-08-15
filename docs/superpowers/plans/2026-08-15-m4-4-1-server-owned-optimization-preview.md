# M4.4.1 Server-Owned Optimization Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose a stateless WeeklyPlan + Pantry optimization preview that projects accepted M3.5.3 comparison evidence through M4.2 checkout assessment and M4.3 deterministic optimization, with synchronized OpenAPI/generated TypeScript and no browser-side arithmetic.

**Architecture:** Preserve all accepted public endpoints. Add detailed computation seams behind `ComparisonPreviewService.create()` and `WeeklyPlanPantryComparisonPreviewService.create()`, then add a generic `optimizationpreview` application layer with a no-op/unknown production economics source and a `weeklyplanpantryoptimizationpreview` HTTP composition boundary. M4.4.1 projects accepted domain results only; it does not acquire provider-specific economics or modify M4.1–M4.3 rules.

**Tech Stack:** Java 25, Spring Boot 4.1, JUnit 5, ArchUnit, OpenAPI 3.1, generated TypeScript client.

## Global Constraints

- Existing `/api/v1/comparison-previews` and `/api/v1/weekly-plan-pantry-comparison-previews` wire behavior remains unchanged.
- Full Pantry coverage must skip economics-source and optimizer invocation.
- Missing economics means explicit UNKNOWN delivery/service/minimum evidence, never zero/free checkout.
- Only accepted M4.2 `COMPARABLE` candidates may compete; M4.3 remains the sole winner/tie authority.
- Economics source is retailer-scoped; out-of-request evidence fails closed.
- No provider-specific acquisition, live retailer network, database/jOOQ, Web/React or browser arithmetic in M4.4.1.
- Public output contains no provider/acquisition/fulfillment identifiers.
- Ordinary CI remains deterministic and network-safe.

---

### Task 1: Add detailed comparison computation seam

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewComputation.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewService.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewComputationTest.java`
- Regression: existing `ComparisonPreviewServiceTest`, controller/integration tests

**Interfaces:**
- Produces: `ComparisonPreviewComputation(input, preview, catalog)` and `ComparisonPreviewService.compute(ComparisonPreviewRequest)`.
- Compatibility: `create(request)` delegates to `compute(request).preview()`.

- [ ] Write a failing test requiring `compute()` to preserve normalized `ComparisonPreviewInput`, accepted `RetailerComparisonCatalog` order/IDs and the exact existing public preview.
- [ ] Run focused API test and confirm RED is missing computation symbols/method.
- [ ] Implement the immutable computation record with one-to-one preview/catalog retailer identity/order validation.
- [ ] Refactor existing service internals once so `compute()` performs the current work and `create()` returns its public preview.
- [ ] Run focused + existing preview regression tests; require GREEN and unchanged endpoint semantics.
- [ ] Commit the seam separately.

### Task 2: Add detailed Pantry comparison computation seam

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewComputation.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewService.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewComputationTest.java`
- Regression: existing M3.5.3 service/controller tests

**Interfaces:**
- Consumes: `ComparisonPreviewService.compute`.
- Produces: `WeeklyPlanPantryComparisonPreviewService.compute(request)` with optional detailed comparison computation.
- Compatibility: existing `create(request)` delegates to `compute(request).preview()`.

- [ ] Write RED tests for `NO_REMAINING_DEMAND -> no detailed comparison` and `COMPARED -> detailed computation present + exact public preview match`.
- [ ] Implement computation record invariants and service delegation without changing existing request validation/item-drift checks.
- [ ] Run focused + M3.5.3 regressions; require unchanged old endpoint behavior.
- [ ] Commit separately.

### Task 3: Add provider-neutral checkout economics source and deterministic optimization composition

**Files:**
- Create package: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/optimizationpreview/`
- Create: `CheckoutEconomicsEvidenceSource.java`
- Create: `NoopCheckoutEconomicsEvidenceSource.java`
- Create: `CheckoutOptimizationPreviewService.java`
- Create: projection records for fee/minimum/economics/retailer/optimization output
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/optimizationpreview/CheckoutOptimizationPreviewServiceTest.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/optimizationpreview/CheckoutOptimizationPreviewInvariantTest.java`

**Interfaces:**
- `CheckoutEconomicsEvidenceSource.load(ProductLocation, Set<RetailerId>) -> Map<RetailerId, BasketEconomics>`.
- `CheckoutOptimizationPreviewService.create(ComparisonPreviewComputation) -> CheckoutOptimizationPreview`.

- [ ] RED: no known economics yields explicit unknown economics for assessable rows and `NO_COMPARABLE_CANDIDATES` rather than zero fees.
- [ ] RED: known same-currency evidence yields a unique M4.3 winner; equal minima yield explicit tie; cheaper ineligible/uncertain candidate cannot win.
- [ ] RED: source is called only with retailers carrying merchandise subtotal; empty request set skips source; out-of-scope returned retailer fails closed.
- [ ] Implement no-op source and all-unknown fallback using `BasketFee.unknown()` / `MinimumOrderConstraint.unknown()`.
- [ ] Delegate every retailer assessment to accepted `RetailerCheckoutAssessmentService`, preserving catalog order.
- [ ] Delegate optimization once to accepted `BasketOptimizer` and project exact M4.3 status/optimal IDs/lowest total.
- [ ] Add self-validating projection invariants for one-to-one retailer alignment, assessment presence/value and optimizer projection consistency.
- [ ] Run focused tests to GREEN and commit.

### Task 4: Add WeeklyPlan Pantry optimization application/HTTP boundary

**Files:**
- Create package: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantryoptimizationpreview/`
- Create request/response/controller/service/configuration/problem/validation/error-handler records/classes
- Test service/controller/JSON failure behavior under corresponding test package

**Interfaces:**
- Public route: `POST /api/v1/weekly-plan-pantry-optimization-previews`.
- Service consumes accepted M3.5.3 detailed computation and Task 3 optimization service.

- [ ] RED: full Pantry coverage returns the accepted M3.5.3 `NO_REMAINING_DEMAND` projection with optimization absent and proves optimization/economics source is not invoked.
- [ ] RED: compared demand returns exact accepted M3.5.3 projection plus optimization payload.
- [ ] RED: M3.5.3 validation errors translate to M4.4.1 validation errors; malformed/unknown JSON is sanitized.
- [ ] Implement service with `optimizationPreview` present iff M3.5.3 outcome is `COMPARED`; constructor/result invariants enforce that relationship.
- [ ] Wire Spring beans with production `NoopCheckoutEconomicsEvidenceSource`.
- [ ] Run focused service/controller regressions and full API verify.
- [ ] Commit.

### Task 5: Add OpenAPI and generated TypeScript contract

**Files:**
- Modify: `openapi/zakup-gotov.yaml`
- Modify generated client entry/tests/schema under `packages/api-client/src/`

**Interfaces:**
- Path constant: `WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH`.
- Operation ID: `createWeeklyPlanPantryOptimizationPreview`.

- [ ] RED: add client contract test expecting the new path/operation/schema names before OpenAPI/schema generation is updated.
- [ ] Confirm Contract CI/typecheck fails specifically on missing M4.4.1 contract symbols.
- [ ] Add OpenAPI path and schemas mirroring Java projection optionality exactly: optimization absent only for no remaining demand; assessment absent for M4.2 non-assessable retailer states; fee/threshold/checkout totals optional according to accepted knowledge status.
- [ ] Regenerate/update generated TypeScript schema and path export with the repository-pinned generator.
- [ ] Run schema freshness, TS typecheck, contract tests and client build to GREEN.
- [ ] Commit.

### Task 6: Architecture and regression hardening

**Files:**
- Add architecture tests under `optimizationpreview` and `weeklyplanpantryoptimizationpreview`.
- Extend computation-seam tests only where required to lock reverse-dependency rules.

- [ ] Prove new optimization composition has no direct provider-specific, database/jOOQ, Spring-Web-client or web frontend dependency.
- [ ] Prove accepted M3.5.3/M4.1/M4.2/M4.3 packages do not depend back on M4.4.1 packages.
- [ ] Prove `weeklyplanpantryoptimizationpreview` depends only on accepted M3.5.3 + generic optimization preview plus finite request/projection vocabulary.
- [ ] Run full Java 25 Maven verify and contract/client gates.
- [ ] Commit hardening only if proof requires changes.

### Task 7: Shipping evidence, read-only review and exact-head acceptance

**Files:**
- Create: `docs/superpowers/plans/2026-08-15-m4-4-1-server-owned-optimization-preview-shipping.md`

- [ ] Record RED/GREEN SHAs for computation seams, composition, HTTP and contract work.
- [ ] Open/maintain draft implementation PR linked to #142.
- [ ] Remove any temporary breadcrumb/marker files before final gate.
- [ ] Freeze final feature SHA.
- [ ] Require exactly 9 normal PR workflow groups and 9/9 SUCCESS with 0 failure/skipped/cancelled.
- [ ] Perform read-only review for contract drift, fabricated economics, source-scope leakage, hidden optimizer recomputation, zero-demand invocation, architecture and public-data leakage.
- [ ] Require no P0/P1/P2/P3/nitpicks, zero unresolved review threads and mergeable=true.
- [ ] Mark ready and squash merge with expected-head protection.
- [ ] Verify exact implementation merge has exactly 8 normal push workflows and 8/8 SUCCESS before declaring accepted.

### Task 8: Canonical acceptance docs

**Files:**
- Add M4.4.1 acceptance record
- Update `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md`

- [ ] Record exact implementation evidence and mark M4.4.1 COMPLETE / ACCEPTED.
- [ ] Advance next target to M4.4.2 Responsive Optimization UX.
- [ ] Use a separate docs-only PR with exactly 4 changed docs files.
- [ ] Require docs PR 9/9 + clean review, expected-head squash merge and final main 8/8.