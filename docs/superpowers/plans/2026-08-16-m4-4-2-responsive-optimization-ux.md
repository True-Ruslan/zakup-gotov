# M4.4.2 Responsive Optimization UX — Implementation Plan

Date: 2026-08-16
Issue: #145
Design: `docs/superpowers/specs/2026-08-16-m4-4-2-responsive-optimization-ux-design.md`
Baseline: `0d1c0c5a157c3eae8988bec7281de3ffdc7a14c6`

## Objective

Advance the accepted primary WeeklyPlan/Pantry browser journey to generated M4.4.1 optimization evidence without introducing browser-owned checkout arithmetic, eligibility/comparability assessment or winner/tie logic.

## Task 1 — Generated M4.4.1 transport

### RED

Update `apps/web/src/app/weekly-plan-comparison.test.ts` first so it requires:

- `WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH`;
- generated `WeeklyPlanPantryOptimizationPreviewRequest` request semantics;
- generated `WeeklyPlanPantryOptimizationPreview` response passthrough;
- product-safe M4.4.1 validation errors;
- existing unavailable fail-closed behavior;
- no request to the old M3.5.3 path.

Run/observe Web CI RED caused by the current M3.5.3 transport.

### GREEN

Evolve `weekly-plan-comparison.ts` to M4.4.1 generated path/types while preserving timeout and `ready | invalid | unavailable` behavior.

Run full Web verification.

## Task 2 — Optimization-aware result projection

### RED

Extend `weekly-plan-comparison-results.test.tsx` with generated-contract fixtures proving:

1. nested original/Pantry/remaining/comparison audit remains visible;
2. `NO_REMAINING_DEMAND` renders no optimization section;
3. `NO_COMPARABLE_CANDIDATES` never names a cheapest retailer;
4. `UNIQUE_WINNER` displays exactly server `optimalRetailerIds` + lowest total even when another candidate appears first;
5. `TIE` displays every server-provided tied ID in server order;
6. known zero delivery/service fee is visibly distinct from UNKNOWN;
7. merchandise subtotal, minimum order state, checkout total, eligibility and comparability render from server evidence;
8. no-assessment retailer remains visible;
9. duplicate/missing/mismatched retailer identities fail closed;
10. contradictory optimizer status/IDs/lowest-total structure fails closed;
11. internal IDs/provider/acquisition/optimizer-authority data is not displayed.

### GREEN

Evolve `weekly-plan-comparison-results.tsx`:

- unwrap `pantryComparisonPreview`;
- preserve accepted M3.5.4 result stages;
- add `OptimizationResults` section;
- create identity-only retailer metadata map;
- validate structural consistency without monetary recomputation;
- map enums to explicit Russian labels;
- reuse display-only currency formatting;
- preserve responsive/accessibility semantics.

Run component tests + typecheck.

## Task 3 — Form integration and primary journey

### RED

Update `weekly-plan-comparison-form.test.tsx` so ready data uses generated M4.4.1 wrapper and the form expects optimization-aware results.

Prove existing WeeklyPlan reorder/Pantry editing/preflight/local-key/unavailable semantics unchanged.

### GREEN

Update form imports/types/action call/result prop only as required. Avoid unrelated UI changes.

Run form/page regressions.

## Task 4 — Deterministic browser fixtures and Playwright

### RED

Update browser assertions before fixture implementation to require:

- M4.4.1 route usage;
- no-comparable state;
- unique-winner state;
- tie state;
- no-remaining-demand state;
- 390px no-overflow;
- keyboard focus / accessible status text;
- Recipe/manual-list regressions.

The current M3.5.3 mock route should make the new tests fail for the intended reason.

### GREEN

Advance `apps/web/e2e/mock-api.mjs` to deterministic M4.4.1 response shapes selected only from test request content. No network/provider traffic.

Update `home.spec.ts` minimally for the four optimization states while preserving existing critical journeys.

Run Playwright production-style acceptance.

## Task 5 — Hardening and cleanup

- search production `apps/web` for the old `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH` primary WeeklyPlan usage; it must be absent from the primary transport;
- search for browser-side arithmetic/ranking patterns over checkout economics; none should exist;
- verify identity lookup uses `Map` rather than repeated linear joins/index alignment;
- verify impossible runtime structures render a fail-closed panel;
- verify generated IDs/provider/acquisition identifiers are not exposed;
- verify no unnecessary component renames or duplicate DTO layers were introduced;
- update implementation/shipping evidence only after exact test checkpoints are known.

## Task 6 — PR acceptance

1. open/keep implementation PR draft during RED→GREEN work;
2. require exact final head with 9/9 normal PR workflow groups SUCCESS;
3. perform read-only Change Review against issue #145 + design + implementation plan;
4. resolve any findings and rerun exact-head gate;
5. require zero unresolved review threads;
6. mark ready and squash merge with expected-head protection;
7. require exact merge SHA with 8/8 normal push workflows SUCCESS;
8. close #145 as completed;
9. create a separate docs-only acceptance PR syncing `PROJECT_STATE`, `ROADMAP`, `CHANGELOG` and an M4.4.2 acceptance record.

## Verification commands represented by CI

Web/contract verification should cover the repository-pinned equivalents of:

- generated API-client freshness/typecheck/tests/build;
- Web TypeScript typecheck;
- Vitest;
- Next production build;
- Playwright deterministic critical journeys;
- repository release/security gates.

No live retailer request is part of M4.4.2 verification.
