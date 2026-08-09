# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**
Current execution stage: **M0A — Platform Foundation**
Current task: **Task 4 — Contract-first product API and generated TypeScript client**

## Product status

No shopping, recipe, retailer, matching, or user-facing web behavior is implemented yet. The platform now has a verified Java/Spring API, PostgreSQL persistence baseline, a first product API endpoint, an OpenAPI 3.1 source contract, and a reproducibly generated typed TypeScript client package.

## Completed foundation work

- PR #1: approved product/architecture/engineering foundation, squash-merged.
- PR #2 / M0A Task 1: Java/Node/pnpm toolchains, monorepo workspace hygiene, ADR-0002, squash-merged.
- PR #3 / M0A Task 2: Java 25 + Spring Boot 4.1 API bootstrap, Maven Wrapper, architecture/context tests, early API CI, squash-merged.
- PR #4 / M0A Task 3: PostgreSQL 18 + Flyway + jOOQ + Testcontainers persistence baseline, squash-merged.

## Task 4 backend TDD evidence

The product API was specified before controller implementation.

- `openapi/zakup-gotov.yaml` defines OpenAPI 3.1 `GET /api/v1/system` returning exactly `name` and `status`.
- The first MVC-test attempt failed to compile because Spring Boot 4 moved focused MVC test auto-configuration into `spring-boot-starter-webmvc-test`; this was rejected as an invalid RED and only test-scoped wiring was corrected.
- **Valid RED:** API CI run `31307609717`, job `93230304772` booted the real application/PostgreSQL stack, reached `DispatcherServlet`, and returned the expected `404` for `/api/v1/system` while no controller existed.
- Minimal `SystemController` was added only after that failure was observed.
- **GREEN:** API CI run `31307694669`, job `93230524342` passed the complete backend suite with `4/4` tests and `BUILD SUCCESS`.
- Latest Task 4 head API CI run `31308226513`, job `93231867424` is also green.

## Task 4 generated client evidence

- `openapi-typescript` generates `packages/api-client/src/schema.d.ts`; the generated file is explicitly marked auto-generated and contains literal `SystemInfo` types for `name: "zakup-gotov-api"` and `status: "UP"`.
- The root `pnpm-lock.yaml` and initial generated schema were produced by a temporary branch-constrained GitHub Actions job and committed by `github-actions[bot]`; that write-capable workflow was deleted immediately afterwards.
- An attempted TypeScript 7.0.2 toolchain exposed an actual peer incompatibility with `openapi-typescript 7.13.0`; the client package was corrected to compatible TypeScript 5.9.3 rather than suppressing the failure.
- **Valid client RED:** Contract CI run `31308014703`, job `93231340332` verified Node 24.18.1, pnpm 11.4.0, frozen lockfile installation, and generated-schema reproducibility, then failed exactly with `TS2307: Cannot find module './index'` because the client wrapper did not yet exist.
- Minimal `createZakupGotovClient()` and `SYSTEM_INFO_PATH` were added only after that RED.
- **GREEN:** Contract CI run `31308075976`, job `93231494408` passed regeneration/no-diff, strict typecheck, `1/1` Vitest test, and TypeScript build.
- A persistent `pnpm/action-setup` PNPM_HOME layout warning was not accepted as normal noise. Permanent Contract CI now installs exact `pnpm@11.4.0` after `actions/setup-node@v6` instead.
- **Clean final GREEN:** Contract CI run `31308226522`, job `93231867437` verifies Node 24.18.1, pnpm 11.4.0, frozen lockfile supply-chain check, regeneration/no-diff, typecheck, `1/1` Vitest test, build, and contains no previous PNPM_HOME layout warning.

## Contract baseline

- OpenAPI 3.1 is the source of truth for client-visible product API contracts.
- Generated TypeScript schema is committed but never hand-edited; Contract CI regenerates it and fails on drift.
- `@zakup-gotov/api-client` uses `openapi-fetch` over generated `paths` types.
- Permanent Contract CI has `contents: read` only.
- Current contract exposes only `GET /api/v1/system`; it is a platform availability contract, not shopping functionality.

## Persistence baseline

- PostgreSQL target: 18; integration image pinned to `postgres:18.4`.
- Flyway owns schema evolution; history currently lives in `public.flyway_schema_history`.
- `V1__baseline.sql` creates the application-owned `app` schema.
- jOOQ is the primary SQL access layer and is configured for PostgreSQL.
- Production datasource credentials are externally supplied; no credentials are committed.

## Approved engineering policy

The project follows `docs/ENGINEERING.md`: TDD, evidence-before-claims, automation-first verification, clean Git/PR discipline, continuous changelog maintenance, and documentation synchronized with repository reality.

## Current repository state

- `main` contains the approved foundation and completed M0A Tasks 1-3.
- PR #5 contains verified Task 4 contract/API-client work and is green pending final documentation/current-head gate and squash merge.
- `API CI` verifies Java 25, Maven 3.9.16 via Maven Wrapper, PostgreSQL integration, and backend tests.
- `Contract CI` verifies pinned Node/pnpm, frozen dependency installation, generated contract drift, strict TypeScript, Vitest, and client build.
- M0A plan: `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md`.
- Execution mode is Inline Execution because independent subagent dispatch is not exposed in this chat; task/review gates remain mandatory.
- No license decision has been made.
- No external retailer integration has been proven yet.

## Current critical unknowns

1. Which Russian retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers be supported with sufficient freshness and reliability for a useful basket comparison?
3. What address/location precision must be retained versus used transiently?
4. How should delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What baseline matching accuracy is achievable with deterministic normalization/ranking before AI-assisted matching is justified?

## Immediate next work

1. Merge verified PR #5 / M0A Task 4.
2. Start Task 5 from updated `main`: responsive Next.js web shell using the shared contract package, with tests first and desktop/mobile browser behavior automated.
3. Continue M0A task-by-task; retailer-specific implementation waits for verified M0A and a separate M0B plan.

## Definition of M0 success

M0 is complete only when evidence shows that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.
