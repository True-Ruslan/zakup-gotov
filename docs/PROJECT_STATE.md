# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**
Current execution stage: **M0A — Platform Foundation**
Current task: **Task 1 — Toolchains and monorepo workspace**

## Product status

No production application behavior has been implemented yet.

Foundation PR #1 was squash-merged on 2026-08-09. The foundation specification is Approved and ADR-0001 is Accepted.

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

- `main` contains the approved foundation from PR #1.
- Task 1 is being implemented on `chore/m0a-toolchains`.
- Java, Node, pnpm, line-ending/editor, ignore, and workspace pins are being established together with ADR-0002.
- M0A implementation plan is `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md`.
- Execution mode in this ChatGPT environment is Inline Execution because independent subagent dispatch is not exposed here; task/review gates remain mandatory.
- No backend/web application code or CI has been merged yet.
- No license decision has been made.
- No external retailer integration has been proven yet.

## Current critical unknowns

1. Which Russian retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers be supported with sufficient freshness and reliability for a useful basket comparison?
3. What address/location precision must be retained versus used transiently?
4. How should delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What baseline matching accuracy is achievable with deterministic normalization/ranking before AI-assisted matching is justified?

## Immediate next work

1. Verify and merge M0A Task 1 toolchain/workspace PR.
2. Start Task 2 from updated `main`: bootstrap the Spring Boot API with failing application/Modulith tests first.
3. Continue M0A task-by-task; do not begin retailer-specific provider implementation before M0A verification and a separate M0B plan.

## Definition of M0 success

M0 is complete only when the project has evidence—not assumptions—that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.
