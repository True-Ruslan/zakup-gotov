# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**
Current execution stage: **M0A — Platform Foundation**
Current task: **Task 2 — Java/Spring API bootstrap**

## Product status

The first executable platform behavior is implemented on PR #3: a minimal Spring Boot 4.1 API bootstrap with Java 25, Virtual Threads enabled, an Actuator health surface, and automated Spring context + Modulith architecture verification.

No shopping, recipe, retailer, matching, persistence, or user-facing web behavior is implemented yet.

## Completed foundation work

- PR #1: approved product/architecture/engineering foundation, squash-merged.
- PR #2 / M0A Task 1: Java/Node/pnpm toolchain pins, monorepo workspace hygiene, and ADR-0002, squash-merged.

## Task 2 TDD evidence

Task 2 was developed through an observed RED -> GREEN cycle in GitHub Actions:

- **RED:** API CI run `31306570727`, job `93227791164` ran successfully on Temurin Java 25.0.3 and Maven 3.9.16, then failed the new `@SpringBootTest` exactly because no `@SpringBootConfiguration` existed.
- Minimal `ZakupGotovApplication` production code was added only after that expected failure was observed.
- A Modulith architecture test verifies `ApplicationModules.of(ZakupGotovApplication.class).verify()`.
- **GREEN:** subsequent runs passed both bootstrap and architecture tests.
- Premature runtime Modulith insight and unused Mockito dependencies were removed after the first green run exposed avoidable warning noise.
- **Final regression:** API CI run `31306909477`, job `93228622348` verified Temurin Java 25.0.3, the generated Apache Maven Wrapper 3.3.4 selecting Maven 3.9.16, and `2/2` tests passing with `BUILD SUCCESS` and no prior Mockito/zero-module warning noise.

## Approved platform baseline

- Java 25 LTS
- Spring Boot 4.1
- Spring Modulith
- Spring MVC + Virtual Threads
- PostgreSQL 18
- Flyway + jOOQ
- REST/OpenAPI 3.1.x
- Next.js 16 + React + TypeScript
- Expo + React Native for future native clients
- OpenTelemetry-compatible observability
- GitHub Actions and GitHub security tooling

See accepted `docs/adr/0001-platform-stack.md`.

## Approved engineering policy

The project follows `docs/ENGINEERING.md`.

Mandatory defaults include:

- TDD for executable behavior: RED -> verify expected failure -> GREEN -> regression suite -> REFACTOR;
- evidence before completion claims;
- automation-first testing and CI, with recurring manual checks treated as automation debt;
- deterministic provider fixtures/contract tests and opt-in live probes rather than live-service-dependent normal CI;
- small reviewable branches/PRs and a squash-only target history;
- continuous `CHANGELOG.md` maintenance under `[Unreleased]`;
- project state, roadmap, ADRs/specs/plans, and public documentation synchronized with repository reality.

## Current repository state

- `main` contains the approved foundation and completed M0A Task 1.
- PR #3 `feat: bootstrap M0A API foundation` contains Task 2 and is green pending final documentation/PR gate and merge.
- `API CI` is already present earlier than originally sequenced in the plan because a real Java 25 CI environment was required to make Task 2 TDD evidence honest; broader CI/security work remains Task 7.
- Apache Maven Wrapper 3.3.4 is generated from the official Maven Wrapper plugin with Maven 3.9.16 pinned; the final API CI uses `./mvnw`, not a runner-provided Maven installation.
- M0A implementation plan is `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md`.
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

1. Merge verified PR #3 / M0A Task 2.
2. Start Task 3 from updated `main`: PostgreSQL 18 + Flyway + jOOQ persistence baseline, beginning with a failing Testcontainers-backed migration test.
3. Continue M0A task-by-task; do not begin retailer-specific provider implementation before M0A verification and a separate M0B plan.

## Definition of M0 success

M0 is complete only when the project has evidence—not assumptions—that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.
