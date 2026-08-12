# M1 Product Comparison Read Model Implementation Plan

Status: **READY FOR TDD IMPLEMENTATION**

**Goal:** expose a stable, user-safe retailer comparison/readiness contract from the M1 domain core through REST/OpenAPI/shared client into a truthful responsive web status surface.

**Design:** [`../specs/2026-08-12-m1-product-comparison-read-model-design.md`](../specs/2026-08-12-m1-product-comparison-read-model-design.md)

## Global constraints

- TDD RED→GREEN for every runtime behavior.
- Ordinary CI performs no live retailer requests.
- All canonical retailers stay visible.
- Technical coverage and production access stay independent.
- Provider IDs, acquisition modes, source references and precise addresses never cross the public comparison boundary.
- `UNKNOWN` availability stays uncertainty.
- Incomplete/unavailable comparisons expose no basket total or aggregate freshness.
- No invented stale/fresh threshold.
- OpenAPI remains the generated-client source of truth.

## Task 1 — product read model from registry state

1. RED tests for canonical retailer order, display names, coverage mapping and production-access mapping.
2. RED tests that non-connected coverage and non-ready production access produce `UNAVAILABLE` with stable reason codes.
3. RED test that technically/prod-ready retailer without runtime evidence is `DATA_NOT_AVAILABLE`.
4. GREEN minimal comparison enums/value objects/catalog/assembler.
5. Full Maven `verify` GREEN.

Expected production package:

`apps/api/src/main/java/io/github/trueruslan/zakupgotov/comparison/`

## Task 2 — provider/basket/freshness evidence mapping

1. RED source-unavailable provider outcome.
2. RED complete basket → `READY`, total, conservative freshness.
3. RED unknown availability → `UNCERTAIN / AVAILABILITY_UNKNOWN` with total.
4. RED incomplete basket → deduplicated ordered reasons, no total/freshness.
5. RED mixed observation/provider timestamps aggregate to observation-only.
6. RED cross-retailer evidence rejection and immutable collections.
7. GREEN minimal evidence/assembler mapping.
8. Full Maven `verify` GREEN.

## Task 3 — architecture boundary

1. RED/architecture rule: retailer/provider/shopping/matching/basket/location may not depend on `comparison`.
2. GREEN architecture verification.
3. Full Maven `verify` GREEN.

## Task 4 — REST + OpenAPI + generated client

1. RED MVC test for `GET /api/v1/retailers`: status 200, all canonical retailers, stable order, current truthful readiness states and no internal provider fields.
2. GREEN `RetailerController`/response DTO mapping using `RetailerRegistry.initial()` and the comparison assembler with no fabricated runtime evidence.
3. RED shared-client type/path test for retailer endpoint.
4. Update `openapi/zakup-gotov.yaml`.
5. Regenerate `packages/api-client/src/schema.d.ts`; expose stable `RETAILERS_PATH` constant.
6. Run API `verify`, API client generate/check/typecheck/test/build and contract verification GREEN.

## Task 5 — web status surface

1. RED component tests for successful retailer rendering and concise Russian product labels.
2. RED failure test: API error renders accessible service-unavailable alert and no fake cards.
3. GREEN server-side loader using `API_BASE_URL` + generated client contract.
4. Make page dynamic so production build has no live API dependency.
5. Replace stale M0 copy with honest M1 shopping-core/status copy.
6. Add semantic retailer list and optional loading state.
7. Update responsive Playwright E2E to prove explicit API-unavailable behavior in ordinary web CI.
8. Run web lint/typecheck/unit/build/E2E GREEN.

## Task 6 — durable docs and shipping

1. Synchronize `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md` and this plan.
2. Record exact RED/GREEN commit evidence.
3. Run full exact-head repository gate:
   - API CI
   - Contract CI
   - Web CI + responsive E2E
   - Retailer Bridge CI
   - Dependency Review
   - CodeQL Java + JS/TS
   - Container Security API + Web
   - Release Bundle CI
   - Release Contract CI
4. Perform read-only Change Review.
5. Record shipping evidence marker.
6. Re-run marker-head branch-protection gate.
7. Mark PR ready and squash merge with expected-head SHA guard.

## Exit condition

The slice is complete only after the merged `main` contains the product read model, REST/OpenAPI/generated client and truthful responsive web state with no live retailer dependency, and the exact merge candidate has passed the full repository/security gate and Change Review.
