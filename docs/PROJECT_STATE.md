# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**
Current execution stage: **M0A — Platform Foundation**
Current task: **Task 3 — PostgreSQL/Flyway/jOOQ persistence baseline**

## Product status

No shopping, recipe, retailer, matching, or user-facing web behavior is implemented yet. The platform foundation now has an executable Spring Boot API and a verified PostgreSQL persistence baseline.

## Completed foundation work

- PR #1: approved product/architecture/engineering foundation, squash-merged.
- PR #2 / M0A Task 1: Java/Node/pnpm toolchains, monorepo workspace hygiene, ADR-0002, squash-merged.
- PR #3 / M0A Task 2: Java 25 + Spring Boot 4.1 API bootstrap, Maven Wrapper, architecture/context tests, early API CI, squash-merged.

## Task 3 TDD evidence

Task 3 was developed with an observed RED -> GREEN cycle against a real PostgreSQL container in GitHub Actions.

- Initial test-only scaffolding added Testcontainers and a `PostgresIntegrationTest`; production jOOQ/Flyway/PostgreSQL dependencies and migrations did not exist yet.
- An initial compile failure caused by test assertion typing was rejected as an invalid RED and fixed without changing production code.
- **Valid RED:** API CI run `31307158160`, job `93229212970` started Docker/Testcontainers and `postgres:18.4`, reached the Spring context, then failed exactly because no `DSLContext` bean existed.
- Production persistence dependencies, required environment-based datasource configuration, shared PostgreSQL integration test support, and `V1__baseline.sql` were added only after the valid RED.
- The first GREEN attempt exposed a real wiring defect: jOOQ connected to PostgreSQL, but the `app` schema was absent because Flyway auto-configuration had not been enabled under Spring Boot 4 modularization. The test was kept strict.
- The Flyway dependency was corrected to Spring Boot 4.1's `spring-boot-starter-flyway` plus `flyway-database-postgresql`.
- **GREEN:** API CI run `31307410620`, job `93229821357` verified PostgreSQL 18.4, Flyway validation and application of `V1 - baseline`, jOOQ PostgreSQL 18.4 support, `3/3` tests passing, and `BUILD SUCCESS`.
- Java 25/Testcontainers native-access warning was removed by explicitly granting native access to the test JVM through Surefire.

## Persistence baseline

- PostgreSQL target: 18; integration image pinned to `postgres:18.4`.
- Flyway owns schema evolution; history currently lives in `public.flyway_schema_history`.
- `V1__baseline.sql` creates the application-owned `app` schema.
- jOOQ is the primary SQL access layer and is configured for the PostgreSQL dialect.
- Production datasource URL/username/password are required from environment configuration; no credentials are committed.
- Spring application-context tests now bootstrap against real PostgreSQL rather than H2 or mocked persistence.

## Approved engineering policy

The project follows `docs/ENGINEERING.md`: TDD, evidence-before-claims, automation-first verification, clean Git/PR discipline, continuous changelog maintenance, and documentation synchronized with repository reality.

## Current repository state

- `main` contains the approved foundation and completed M0A Tasks 1-2.
- PR #4 contains verified Task 3 persistence work and is green pending final documentation/current-head gate and merge.
- `API CI` verifies Java 25, Maven 3.9.16 through the generated Maven Wrapper, and the full backend test suite on affected PRs/main changes.
- M0A plan: `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md`.
- Execution mode in this ChatGPT environment is Inline Execution because independent subagent dispatch is not exposed here; task/review gates remain mandatory.
- No license decision has been made.
- No external retailer integration has been proven yet.

## Current critical unknowns

1. Which Russian retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers be supported with sufficient freshness and reliability for a useful basket comparison?
3. What address/location precision must be retained versus used transiently?
4. How should delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What baseline matching accuracy is achievable with deterministic normalization/ranking before AI-assisted matching is justified?

## Immediate next work

1. Merge verified PR #4 / M0A Task 3.
2. Start Task 4 from updated `main`: contract-first OpenAPI baseline and generated TypeScript API client, beginning with an automated contract/generation check before adding client consumption.
3. Continue M0A task-by-task; retailer-specific implementation waits for M0A verification and a separate M0B plan.

## Definition of M0 success

M0 is complete only when evidence shows that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.
