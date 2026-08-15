# M3.5.4 Responsive Pantry Controls — Implementation Plan

Date: 2026-08-15
Issue: #130
Branch: `feat/m3-5-4-responsive-pantry-controls`
Baseline: `3e030b787ea0afd807f9a8ffcdb167ed46a491f7`
Design: `docs/superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md`

## Objective

Advance the accepted WeeklyPlan-first browser journey from M3.3 transport to the generated M3.5.3 Pantry-aware comparison contract while adding request-scoped Pantry controls and server-owned original/audit/remaining/comparison rendering.

## Execution order

### Task 1 — Transport RED→GREEN

Files:
- `apps/web/src/app/weekly-plan-comparison.ts`
- `apps/web/src/app/weekly-plan-comparison.test.ts`

Steps:
1. First change tests to require `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH` and M3.5.3 generated request/response types.
2. Verify focused test fails because current action still calls M3.3.
3. Evolve the action to call only M3.5.3 with the existing 3-second timeout and fail-closed state model.
4. Keep product-safe 400 projection; include M3.5.3/M3.5.2/M3.2/Recipe/Comparison validation unions accepted by the generated contract.
5. Verify focused transport tests and web typecheck.

### Task 2 — Form RED→GREEN

Files:
- `apps/web/src/app/weekly-plan-comparison-form.tsx`
- `apps/web/src/app/weekly-plan-comparison-form.test.tsx`

Steps:
1. Add tests for zero Pantry rows by default, add/remove/edit row, positive-quantity validation, generated unit selection, `pantry: []` identity submit and absence of browser row keys in payload.
2. Verify focused tests fail before production changes.
3. Add request-scoped `PantryRow` state and controls after weekly occurrences.
4. Extend client preflight validation only for row completeness/positive amount.
5. Submit generated M3.5.3 request with `locality`, accepted `weeklyPlan`, and Pantry rows.
6. Preserve accepted occurrence/ingredient behavior and pending/error handling.
7. Verify form tests and web typecheck.

### Task 3 — Results RED→GREEN

Files:
- `apps/web/src/app/weekly-plan-comparison-results.tsx`
- `apps/web/src/app/weekly-plan-comparison-results.test.tsx`

Steps:
1. Add tests for server-owned original ShoppingList, Pantry audit evidence, remaining ShoppingList, `COMPARED`, `NO_REMAINING_DEMAND`, hidden UUIDs and contradictory missing comparison payload.
2. Verify focused tests fail.
3. Render original → Pantry coverage → remaining in response order.
4. Localize Pantry statuses without deriving subtraction.
5. Render `ComparisonPreviewResults` only for truthful `COMPARED` + payload.
6. Render terminal zero-demand state for `NO_REMAINING_DEMAND`.
7. Fail closed for impossible `COMPARED` without payload.
8. Verify result tests and web typecheck.

### Task 4 — Page integration RED→GREEN

Files:
- `apps/web/src/app/page.tsx`
- `apps/web/src/app/page.test.tsx`

Steps:
1. Update assertions/copy only where the accepted primary WeeklyPlan flow now includes Pantry.
2. Keep WeeklyPlan first, Recipe second, manual list available.
3. Verify page tests.

### Task 5 — Deterministic browser fixture RED→GREEN

Files:
- `apps/web/e2e/mock-api.mjs`
- `apps/web/e2e/home.spec.ts`

Steps:
1. Add browser assertions requiring M3.5.3 path and Pantry request payload.
2. Verify browser test fails against the old fixture route.
3. Add deterministic M3.5.3 fixture route with no retailer network.
4. Cover partial Pantry `COMPARED` and full Pantry `NO_REMAINING_DEMAND`.
5. Preserve existing Recipe/manual fixture behavior.

### Task 6 — Responsive/accessibility/regression gate

Run:
- `pnpm --filter @zakup-gotov/api-client build`
- `pnpm --filter web lint`
- `pnpm --filter web typecheck`
- `pnpm --filter web test`
- `NEXT_TELEMETRY_DISABLED=1 pnpm --filter web build`
- `pnpm --filter web test:e2e`

Required evidence:
- desktop partial-Pantry flow passes;
- zero-demand flow passes;
- 390px no horizontal overflow;
- keyboard/focus Pantry controls pass;
- Recipe/manual-list critical flows pass;
- no live retailer traffic.

### Task 7 — Documentation/shipping evidence

Files:
- `CHANGELOG.md`
- `docs/PROJECT_STATE.md` only as factual in-progress state if needed before acceptance;
- `docs/superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md`.

Record exact RED/GREEN SHAs, final verification commands/results, browser acceptance scope and non-goals.

### Task 8 — PR acceptance

1. Open/maintain draft PR linked to #130.
2. Verify exact final head has exactly 9 normal PR workflow groups and all are SUCCESS; no failure/skipped/cancelled.
3. Perform read-only review anchored to exact head; resolve every P0/P1/P2/P3/nitpick and thread.
4. Mark ready.
5. Squash merge with expected-head protection.
6. Verify exact merge SHA has exactly 8 normal post-merge push workflow groups and all SUCCESS.
7. Only then create separate canonical acceptance docs/state/roadmap/changelog sync.

## Scope guard

Do not add:
- Pantry persistence/history;
- explicit exclusions/never-buy;
- browser-side normalization or subtraction;
- provider/acquisition changes;
- new API/backend behavior;
- live retailer calls in CI/browser tests;
- unrelated UI redesign.
