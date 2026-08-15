# M3.5.3 Pantry-aware WeeklyPlan → Comparison Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a stateless Pantry-aware WeeklyPlan → Comparison endpoint that compares only remaining demand and returns an explicit successful zero-demand outcome without invoking retailer comparison/acquisition.

**Architecture:** A new `weeklyplanpantrycomparisonpreview` composition package delegates all WeeklyPlan/Pantry semantics to accepted M3.5.2, then either short-circuits on an empty remaining list or adapts the exact remaining items into accepted `ComparisonPreviewService`. It owns only wrapper validation, explicit outcome projection and bridge drift checks.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring MVC, Jackson, JUnit 5, ArchUnit, OpenAPI 3.1, TypeScript 5.9, Vitest, pnpm.

## Global Constraints

- New endpoint: `POST /api/v1/weekly-plan-pantry-comparison-previews`.
- Existing M3.3 and M3.5.2 runtime/API behavior must remain unchanged.
- M3.5.2 remains the sole owner of WeeklyPlan projection, Pantry subtraction, audit evidence and remaining-demand provenance.
- Zero remaining demand returns `NO_REMAINING_DEMAND`, no comparison payload, no fabricated item and zero comparison-service invocations.
- Non-empty demand preserves UUID/order/requirement/canonical quantity exactly into ComparisonPreview.
- Locality remains invalid when blank or normalized length exceeds 160 even if Pantry fully covers demand.
- No persistence, browser Pantry UI, provider/acquisition changes, fuzzy/AI matching or omit-all semantics.
- Ordinary tests/CI make no live retailer request.
- Full API verification command: `cd apps/api && ./mvnw -B verify`.
- Contract verification uses repository-standard pnpm scripts and generated-schema freshness checks.

---

### Task 1: RED application composition and zero-demand contract

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewServiceTest.java`

**Interfaces:**
- Consumes: accepted `WeeklyPlanPantryShoppingPreviewService`, `ComparisonPreview` vocabulary.
- Produces requirements for `WeeklyPlanPantryComparisonPreviewService#create(WeeklyPlanPantryComparisonPreviewRequest)` and `WeeklyPlanPantryComparisonOutcome`.

- [ ] **Step 1: Write failing service tests** requiring:
  - partial Pantry coverage compares only remaining items;
  - full Pantry coverage returns `NO_REMAINING_DEMAND`, null/absent comparison and does not invoke the injected comparison creator;
  - empty Pantry compares every remaining accepted weekly item;
  - invalid locality fails before the zero-demand branch;
  - nested M3.5.2 validation paths are preserved;
  - returned comparison cardinality/id/order/requirement/quantity drift throws `IllegalStateException`.

Use a package-private constructor accepting `Function<ComparisonPreviewRequest, ComparisonPreview>` so tests can count/forbid downstream comparison calls without mocking final classes.

- [ ] **Step 2: Run full API verification to prove RED**

Run: `cd apps/api && ./mvnw -B verify`
Expected: FAIL because M3.5.3 production types do not exist.

- [ ] **Step 3: Commit the RED checkpoint**

Commit message: `test(m3): define Pantry-aware weekly comparison contract`.

---

### Task 2: GREEN application composition

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonOutcome.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreview.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewValidationError.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/InvalidWeeklyPlanPantryComparisonPreviewRequestException.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewService.java`

**Interfaces:**

```java
public enum WeeklyPlanPantryComparisonOutcome {
    COMPARED,
    NO_REMAINING_DEMAND
}
```

```java
public record WeeklyPlanPantryComparisonPreviewRequest(
        String locality,
        WeeklyPlanShoppingPreviewRequest weeklyPlan,
        List<WeeklyPlanPantryItemRequest> pantry) {}
```

```java
public record WeeklyPlanPantryComparisonPreview(
        WeeklyPlanPantryShoppingPreview pantryShoppingPreview,
        WeeklyPlanPantryComparisonOutcome comparisonOutcome,
        ComparisonPreview comparisonPreview) {}
```

- [ ] **Step 1: Implement wrapper/locality validation**

Normalize locality with `strip().replaceAll("\\s+", " ")`; reject null/blank and normalized length > 160 with `InvalidWeeklyPlanPantryComparisonPreviewRequestException`.

- [ ] **Step 2: Delegate WeeklyPlan/Pantry semantics to M3.5.2**

Construct `WeeklyPlanPantryShoppingPreviewRequest(request.weeklyPlan(), request.pantry())`, call `WeeklyPlanPantryShoppingPreviewService#create` exactly once, and map its validation errors unchanged into the new exception boundary.

- [ ] **Step 3: Implement zero-demand short-circuit**

If `remainingShoppingList().items().isEmpty()`, return `NO_REMAINING_DEMAND` with `comparisonPreview == null` without evaluating the comparison creator.

- [ ] **Step 4: Implement non-empty comparison bridge**

Map each remaining item to `ComparisonPreviewItemRequest(id, requirement, new ComparisonPreviewQuantityRequest(amount, unit))`, invoke comparison exactly once, and verify exact cardinality/id/order/requirement/quantity against `ComparisonPreview.items()`.

- [ ] **Step 5: Enforce response invariants in the record constructor**

`COMPARED` requires non-empty remaining items and non-null comparison; `NO_REMAINING_DEMAND` requires empty remaining items and null comparison.

- [ ] **Step 6: Run full API verification**

Run: `cd apps/api && ./mvnw -B verify`
Expected: PASS.

- [ ] **Step 7: Commit**

Commit message: `feat(m3): compose Pantry-adjusted weekly comparison`.

---

### Task 3: RED→GREEN HTTP boundary

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewControllerTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewController.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewConfiguration.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewExceptionHandler.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/InvalidWeeklyPlanPantryComparisonPreviewProblem.java`

**Interfaces:**
- `POST /api/v1/weekly-plan-pantry-comparison-previews`.
- Validation code: `INVALID_WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEW`.

- [ ] **Step 1: Write failing HTTP tests** for:
  - non-empty comparison 200 with `comparisonOutcome=COMPARED`;
  - full Pantry 200 with `comparisonOutcome=NO_REMAINING_DEMAND` and no comparison payload;
  - blank/oversized locality 400, including full Pantry case;
  - nested invalid WeeklyPlan/Pantry input 400 with sanitized field paths;
  - malformed JSON, unknown top-level property and unsupported unit -> one sanitized `$request: malformed JSON request` problem.

- [ ] **Step 2: Run API verification and retain RED checkpoint**

Run: `cd apps/api && ./mvnw -B verify`
Expected: FAIL because controller/config/problem boundary does not exist.

- [ ] **Step 3: Implement controller/config/exception handler** following accepted M3.5.2/M3.3 patterns and without adding provider-specific behavior.

- [ ] **Step 4: Run full API verification**

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat(m3): expose Pantry-aware weekly comparison API`.

---

### Task 4: RED→GREEN OpenAPI and generated client

**Files:**
- Modify: `openapi/zakup-gotov.yaml`
- Modify: `packages/api-client/src/index.test.ts`
- Modify: `packages/api-client/src/index.ts`
- Regenerate: `packages/api-client/src/schema.d.ts`

**Interfaces:**
- Operation ID: `createWeeklyPlanPantryComparisonPreview`.
- Path constant: `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH`.
- New outcome schema enum: `COMPARED | NO_REMAINING_DEMAND`.
- Response requires `pantryShoppingPreview` + `comparisonOutcome`; `comparisonPreview` is optional/nullable and documented by outcome.

- [ ] **Step 1: Add a failing api-client contract test** that imports the new path type/operation/request/response and asserts the path constant.

- [ ] **Step 2: Run repository contract/type checks to prove RED**

Use the scripts declared in `packages/api-client/package.json` / root workspace; expected failure is missing new path/types.

- [ ] **Step 3: Update OpenAPI with the new path and schemas only** and preserve all existing M3.3/M3.5.2 schemas unchanged.

- [ ] **Step 4: Add the path constant and regenerate `schema.d.ts` using repository-standard generation tooling.**

- [ ] **Step 5: Run generated freshness, TypeScript typecheck, tests and build.**

Expected: PASS.

- [ ] **Step 6: Commit**

Commit message: `feat(m3): publish Pantry-aware weekly comparison contract`.

---

### Task 5: Architecture and regression gate

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpantrycomparisonpreview/WeeklyPlanPantryComparisonPreviewArchitectureTest.java`

**Interfaces:**
- New package may depend only on `weeklyplanpantrypreview`, request vocabulary from `weeklyplanpreview`, accepted `preview`, Java/Spring/Jackson.
- Existing `weeklyplancomparisonpreview` and `weeklyplanpantrypreview` must not depend on `weeklyplanpantrycomparisonpreview`.

- [ ] **Step 1: Add ArchUnit tests** rejecting direct dependencies from the new package to Pantry domain, shopping/recipe/weeklyplan domain internals, retailer, provider, persistence/database and browser packages.

- [ ] **Step 2: Add reverse-dependency guards** for M3.3 and M3.5.2.

- [ ] **Step 3: Run full API verification**

Run: `cd apps/api && ./mvnw -B verify`
Expected: PASS.

- [ ] **Step 4: Commit**

Commit message: `test(m3): guard Pantry weekly comparison architecture`.

---

### Task 6: Shipping and acceptance

**Files:**
- Create: `docs/superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md`

- [ ] **Step 1: Record RED→GREEN SHA chain and exact verification commands/results.**
- [ ] **Step 2: Open/update draft PR closing #127 and inspect changed-file scope.**
- [ ] **Step 3: Require exact final head 9/9 normal PR workflow groups SUCCESS with zero failure/skipped/cancelled.**
- [ ] **Step 4: Perform independent read-only change review on exact head; require no P0/P1/P2/P3/nitpicks and zero unresolved threads.**
- [ ] **Step 5: Mark ready and squash merge with expected-head protection.**
- [ ] **Step 6: Require exact implementation merge 8/8 normal push workflows SUCCESS and issue #127 closed `completed`.**
- [ ] **Step 7: In a separate docs-only PR, add M3.5.3 acceptance evidence, synchronize `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, root `CHANGELOG.md`, and advance the deterministic target to M3.5.4 responsive Pantry controls. Require docs PR 9/9 and final main 8/8.**