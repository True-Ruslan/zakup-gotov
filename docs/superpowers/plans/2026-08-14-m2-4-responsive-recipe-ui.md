# M2.4 Responsive Recipe UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a responsive Recipe-first web journey backed exclusively by the accepted generated M2.3 Recipe → Comparison contract while preserving the manual comparison flow.

**Architecture:** Add a generated-client server action, a focused client Recipe form, and a generated-shopping result projection that reuses the existing comparison result renderer. Extend deterministic browser fixtures only under `apps/web/e2e`; production browser code never reimplements Recipe scaling/merge/comparison logic or retailer evidence.

**Tech Stack:** Next.js 16.3.0, React 19.2.8, TypeScript, Tailwind CSS 4, generated OpenAPI client, Vitest/Testing Library, Playwright.

## Global Constraints

- Use `RECIPE_COMPARISON_PREVIEWS_PATH` and generated request/response/problem schemas from `@zakup-gotov/api-client`.
- Recipe-first becomes primary; manual basket comparison remains functional below it.
- Browser code owns form state and preflight UX only; backend remains authoritative for Recipe and comparison semantics.
- No persistence/local storage, exact address, provider identifiers, retailer credentials, fuzzy/AI logic, recommendation ranking or production retailer calls.
- Keep existing stone-based visual language; M2.4 is not a redesign.
- Component behavior and desktop/mobile Playwright journey start RED-first.

---

### Task 1: Generated-client server action

**Files:**
- Create: `apps/web/src/app/recipe-comparison.ts`
- Create: `apps/web/src/app/recipe-comparison.test.ts`

**Interfaces:**
- Consumes: `RECIPE_COMPARISON_PREVIEWS_PATH`, `createZakupGotovClient`, generated `RecipeComparisonPreviewRequest`, `RecipeComparisonPreviewResponse` and 400 problem schemas.
- Produces: `createRecipeComparisonPreview(request): Promise<RecipeComparisonState>` and exported generated-type aliases used by the form.

- [ ] **Step 1: Write the failing server-action contract test**

Assert that a successful generated-client POST returns `{kind: "ready"}`, a generated 400 problem returns `{kind: "invalid"}` with only `{field,message}` errors, and missing/unreachable `API_BASE_URL` returns `{kind: "unavailable"}`.

- [ ] **Step 2: Run the focused test and preserve RED evidence**

Run through Web CI / `pnpm --filter web test -- recipe-comparison.test.ts`; expected failure is missing `recipe-comparison` production module/action.

- [ ] **Step 3: Implement the minimal server action**

Use the existing finite-timeout pattern from `comparison-preview.ts`; POST only `RECIPE_COMPARISON_PREVIEWS_PATH`; map 200/400/unavailable without leaking problem metadata or internal errors.

- [ ] **Step 4: Verify GREEN**

Focused test, web typecheck and existing `comparison-preview.test.ts` pass.

### Task 2: Recipe form component

**Files:**
- Create: `apps/web/src/app/recipe-comparison-form.tsx`
- Create: `apps/web/src/app/recipe-comparison-form.test.tsx`

**Interfaces:**
- Consumes: `createRecipeComparisonPreview`, generated `RecipeComparisonPreviewRequest`, generated `QuantityInputUnit`.
- Produces: `<RecipeComparisonForm />` with primary Recipe editing workflow.

- [ ] **Step 1: Write component RED tests**

Require default base/target servings `2`, one ingredient row, add/remove behavior, quantity/unit editing, serving changes, client preflight messages, submit request shape, pending state, generated 400 display and unavailable display.

- [ ] **Step 2: Run focused test and preserve RED**

Expected failure: missing `RecipeComparisonForm` and/or missing controls.

- [ ] **Step 3: Implement minimal form state and validation**

Use labels `Название рецепта`, `Порций в рецепте`, `Нужно порций`, `Населённый пункт`, `Ингредиент`, `Количество`, `Единица`; actions `Добавить ингредиент`, `Удалить ингредиент`, `Сравнить рецепт`.

Client validation rejects blank title/locality, non-positive or non-integer servings, blank ingredient requirement and non-positive/non-finite quantity. Do not calculate serving scaling in browser code.

- [ ] **Step 4: Verify GREEN**

Focused component tests and typecheck pass.

### Task 3: Recipe result projection

**Files:**
- Create: `apps/web/src/app/recipe-comparison-results.tsx`
- Create: `apps/web/src/app/recipe-comparison-results.test.tsx`
- Reuse: `apps/web/src/app/comparison-preview-results.tsx`

**Interfaces:**
- Consumes: generated `RecipeComparisonPreviewResponse`.
- Produces: `<RecipeComparisonResults preview={...} />`.

- [ ] **Step 1: Write result RED tests**

Require heading `Список покупок из рецепта`, canonical generated shopping items in order, no raw UUID rendering, and reuse of the existing comparison result output including uncertainty/incomplete/unavailable states.

- [ ] **Step 2: Run RED**

Expected failure: missing result component.

- [ ] **Step 3: Implement minimal projection**

Render each generated item requirement plus canonical amount/unit and pass `preview.comparisonPreview` directly to `ComparisonPreviewResults`.

- [ ] **Step 4: Verify GREEN**

Focused result tests and existing comparison result tests pass.

### Task 4: Homepage integration and deterministic browser fixture

**Files:**
- Modify: `apps/web/src/app/page.tsx`
- Modify: `apps/web/src/app/page.test.tsx`
- Modify: `apps/web/e2e/home.spec.ts`
- Modify: `apps/web/e2e/mock-api.mjs`

**Interfaces:**
- Homepage renders Recipe journey first and manual comparison second.
- Mock API supports both accepted endpoints without live retailer traffic.

- [ ] **Step 1: Write homepage and Playwright RED tests before production integration**

Desktop Recipe scenario:
1. open `/`;
2. fill title `Блины`, base servings `2`, target servings `4`, locality `Москва`;
3. fill first ingredient `Молоко`, `0.5 LITER`;
4. add second ingredient `Яйца`, `5 PIECE`;
5. submit `Сравнить рецепт`;
6. expect `Список покупок из рецепта` with canonical scaled `Молоко 1000 MILLILITER` and `Яйца 10 PIECE`;
7. expect `Результат для Москва` and eight retailer entries with truthful mixed statuses.

Mobile scenario uses a mobile viewport, edits Recipe fields, submits, and asserts `scrollWidth <= clientWidth`.

Failure scenario uses locality `Недоступно`, expects exactly one alert and no generated shopping list/comparison results.

Keyboard scenario tabs into the first Recipe field and verifies visible focus.

Keep the existing manual comparison journey as a regression scenario.

- [ ] **Step 2: Run Playwright and preserve RED**

Expected failures: Recipe controls/endpoint are not yet integrated.

- [ ] **Step 3: Extend deterministic mock API**

Handle `/api/v1/recipe-comparison-previews` separately. Construct deterministic recipe/ingredient/list/item UUIDs only in the test fixture; canonicalize units and scale each test ingredient by `targetServings/baseServings`; feed generated shopping items into the existing deterministic comparison builder. Return the accepted response shape.

This fixture is browser-test evidence only and must not be imported by production code.

- [ ] **Step 4: Integrate Recipe form on homepage**

Update page copy from stale `M1 · Shopping Core` positioning to Recipe-first M2 language. Render `<RecipeComparisonForm />` before the existing `<ComparisonPreviewForm />`; clearly present manual comparison as an alternate entry without hiding it.

- [ ] **Step 5: Verify GREEN across desktop/mobile Playwright**

Run full browser suite; expected all Recipe and manual scenarios PASS with no horizontal overflow and no provider/internal fields in visible text.

### Task 5: Full verification, review and shipping

**Files:**
- Update: `docs/PROJECT_STATE.md` only to `IMPLEMENTED / TESTED / SHIPPING` before merge, never `ACCEPTED` prematurely.
- Update: `docs/ROADMAP.md`
- Update: `CHANGELOG.md`
- Create/update shipping evidence under `docs/superpowers/plans/`.

- [ ] **Step 1: Run complete Web gates**

`lint`, `typecheck`, unit tests, build and Playwright all pass. Generated API client stays unchanged/fresh because M2.4 changes no backend/OpenAPI contract.

- [ ] **Step 2: Require exact-head repository CI**

API CI, Contract CI, Web CI/E2E, CodeQL, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle all succeed on the same reviewed PR head.

- [ ] **Step 3: Perform read-only review**

Review request/response typing, no browser domain duplication, failure behavior, accessibility/responsiveness, test-fixture isolation, privacy/security and regression scope. Block P0/P1/P2.

- [ ] **Step 4: Mark PR ready and squash-merge with exact head SHA**

Do not merge from a stale or partially checked head.

- [ ] **Step 5: Run post-merge acceptance gate**

Require all normal push-triggered workflows on the merged `main` SHA to succeed. Only then close #103 as accepted and synchronize canonical docs in a docs-only acceptance PR if needed.

## Self-review

- Spec coverage: product workflow, generated contract boundary, error UX, manual-flow retention, accessibility, responsive behavior, deterministic fixture isolation and acceptance gates are each mapped to a task.
- Placeholder scan: no TODO/TBD implementation placeholders.
- Type consistency: all web request/response types come from `components["schemas"]`; no duplicate backend DTO is introduced.
