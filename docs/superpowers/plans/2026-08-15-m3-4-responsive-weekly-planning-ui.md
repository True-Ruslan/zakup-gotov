# M3.4 Responsive Weekly Planning UI — Implementation Plan

Date: 2026-08-15  
Issue: #118  
Design: `docs/superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md`

## Objective

Implement a responsive WeeklyPlan-first web journey over the already accepted M3.3 composed endpoint. Preserve all accepted lower-layer semantics and existing Recipe/manual journeys.

## Task 1 — Transport RED → GREEN

Files:
- create `apps/web/src/app/weekly-plan-comparison.test.ts` first;
- then create `apps/web/src/app/weekly-plan-comparison.ts`.

Tests prove:
- no API configuration fails closed without fetch;
- successful typed M3.3 response is returned unchanged;
- generated 400 validation fields/messages are sanitized;
- hanging request is aborted within the bounded timeout.

Implementation uses `createZakupGotovClient` + generated `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH` and generated schema aliases only.

## Task 2 — Results projection RED → GREEN

Files:
- create `apps/web/src/app/weekly-plan-comparison-results.test.tsx` first;
- then create `apps/web/src/app/weekly-plan-comparison-results.tsx`.

Render canonical weekly shopping items in server order before reusing `ComparisonPreviewResults`. Do not expose IDs/provenance tuples or recalculate quantities/totals.

## Task 3 — Planner form RED → GREEN

Files:
- create `apps/web/src/app/weekly-plan-comparison-form.test.tsx` first;
- then create `apps/web/src/app/weekly-plan-comparison-form.tsx`.

Cover:
- safe defaults: one occurrence, one ingredient;
- occurrence add/remove with 1..35 bound;
- explicit move up/down preserving caller order;
- day and target servings;
- nested Recipe title/base servings and ingredient add/remove;
- exact generated M3.3 request shape;
- preflight validation without backend call;
- generated field errors and unavailable state;
- pending submit protection.

Local keys remain React-only.

## Task 4 — Primary homepage integration RED → GREEN

Update `apps/web/src/app/page.test.tsx` first to require M3 Weekly Planning first and preserve Recipe/manual paths. Then update `apps/web/src/app/page.tsx`.

Order:
1. WeeklyPlan primary journey;
2. Recipe secondary journey;
3. manual-list secondary journey.

No existing journey is deleted.

## Task 5 — Deterministic browser acceptance RED → GREEN

Update `apps/web/e2e/home.spec.ts` first with planner critical journeys. Then extend `apps/web/e2e/mock-api.mjs` with a deterministic weekly-plan composed route.

Browser assertions:
- desktop planner request produces canonical weekly shopping and all eight retailer cards;
- occurrence order can be changed independently from day metadata;
- mobile 390px has no horizontal overflow;
- keyboard focus is visible;
- unavailable response yields one alert and no fabricated result;
- Recipe and manual-list regression scenarios remain green.

Mock route must not contain live retailer traffic or sensitive/provider internals.

## Task 6 — Hardening and shipping evidence

Run/verify through GitHub Actions on the exact feature head:
- API CI;
- Web CI (lint/typecheck/unit/build/Playwright as configured);
- Contract CI/generated-client freshness;
- Release Contract CI;
- Security CI;
- CodeQL;
- Dependency Review;
- Retailer Bridge CI;
- any remaining normal PR workflow group configured by the repository.

Fix all feature-caused failures. Add shipping evidence with RED/GREEN commit SHAs and exact-head workflow results.

Then perform read-only diff/review, resolve all P0/P1/P2/P3 findings, mark PR ready, squash merge with expected-head protection, close #118 and verify exactly 8 normal post-merge `main` workflows SUCCESS.

## Scope guard

No persistence, database, OpenAPI schema, backend M3.3 semantics, pantry/exclusion, retailer connector, production-access or release-version behavior changes unless an implementation defect proves one is strictly necessary. Any such discovery is split into a separate issue rather than silently expanding M3.4.