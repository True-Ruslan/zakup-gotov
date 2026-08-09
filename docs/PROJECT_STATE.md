# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**
Current execution stage: **M0A — Platform Foundation**

## Product status

No production application code has been merged yet.

The foundation direction was approved on 2026-08-09:

- responsive web first;
- native Android/iOS later;
- retailer-provider abstraction as the critical integration boundary;
- full-basket comparison rather than cheapest-single-item search;
- transparency around missing items, matching confidence, and offer freshness.

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
- `PROJECT_STATE.md`, roadmap, ADRs/specs/plans, and public documentation synchronized with repository reality in the same PR that changes that reality.

## Current repository state

- `main` contains only the initial README until foundation PR #1 is merged.
- PR #1 contains the approved foundation documentation, M0A implementation plan, engineering policy, and repository community files.
- M0A Platform Foundation implementation plan is `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md`.
- Execution mode in this ChatGPT environment is Inline Execution because independent subagent dispatch is not exposed here; task/review gates from the plan remain mandatory.
- No production application code or CI has been merged yet.
- No license decision has been made.
- No external retailer integration has been proven yet.

## Current critical unknowns

1. Which Russian retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers be supported with sufficient freshness and reliability for a useful basket comparison?
3. What address/location precision must be retained versus used transiently?
4. How should delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What baseline matching accuracy is achievable with deterministic normalization/ranking before AI-assisted matching is justified?

## Immediate next work

1. Finish and merge foundation PR #1 after documentation verification.
2. Create an isolated M0A implementation branch from the updated `main`.
3. Execute `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md` task-by-task using TDD and fresh verification evidence.
4. Do not begin retailer-specific provider implementation until M0A is verified and the separate M0B plan exists.

## Definition of M0 success

M0 is complete only when the project has evidence—not assumptions—that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.
