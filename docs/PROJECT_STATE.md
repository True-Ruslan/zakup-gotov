# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**
Current execution stage: **M0A — Platform Foundation**
Current task: **Task 6 — Observability and safe operational defaults**

## Product status

The project has a verified API, persistence baseline, contract/generated client, responsive web shell, and now a constrained operational-health surface. Retailer comparison itself is still intentionally unavailable until M0B proves real integrations.

## Completed M0A work

- PR #1: approved product/architecture/engineering foundation, squash-merged.
- PR #2 / Task 1: Java/Node/pnpm toolchains, monorepo workspace hygiene, ADR-0002, squash-merged.
- PR #3 / Task 2: Java 25 + Spring Boot 4.1 API bootstrap, Maven Wrapper, architecture/context tests, API CI, squash-merged.
- PR #4 / Task 3: PostgreSQL 18 + Flyway + jOOQ + Testcontainers persistence baseline, squash-merged.
- PR #5 / Task 4: OpenAPI contract, tested system endpoint, generated TypeScript API client, Contract CI, squash-merged.
- PR #6 / Task 5: responsive Next.js web shell with component + desktop/mobile browser tests and Web CI, squash-merged.

## Task 6 TDD evidence

- A new `ActuatorSecurityTest` was written before changing production management configuration.
- The test requires `/actuator/health`, `/actuator/info`, `/actuator/health/liveness`, and `/actuator/health/readiness` to be available while `/actuator/env`, `/actuator/configprops`, and `/actuator/metrics` remain HTTP-unavailable.
- **Valid RED:** API CI run `31309354018`, job `93234641102` started the full Java 25/PostgreSQL 18.4 application and showed `/actuator/health` already returning `200 UP`, but `/actuator/info` returning `404`; the suite ended `Tests run: 5, Failures: 1` and `BUILD FAILURE` for exactly that expected contract gap.
- Production configuration was then changed minimally: HTTP exposure only `health,info`, health probes enabled, health details/components hidden, and Spring MVC request-detail logging explicitly disabled.
- `docs/OBSERVABILITY.md` documents vendor-neutral metric/trace vocabulary, low-cardinality rules, liveness/readiness semantics, and logging/redaction constraints for future provider work.
- **GREEN:** API CI run `31309422819`, job `93234803394` shows `Exposing 2 endpoints beneath base path '/actuator'`, `ActuatorSecurityTest` PASS, total `5/5` tests PASS, and `BUILD SUCCESS`.
- Sensitive management endpoints remain inaccessible because the executable test asserts their HTTP 404 behavior.

## Operational baseline

Public management HTTP surface:

- `/actuator/health`;
- `/actuator/health/liveness`;
- `/actuator/health/readiness`;
- `/actuator/info`.

Explicitly not exposed over HTTP:

- `/actuator/env`;
- `/actuator/configprops`;
- `/actuator/metrics`;
- other management endpoints unless a future security review and test intentionally adds them.

Request-detail logging is disabled. Raw provider payloads, credentials, authorization headers, tokens, precise user addresses, and arbitrary user input are not acceptable telemetry labels/log content by default.

Reserved M0B/M1 metric vocabulary is documented in `docs/OBSERVABILITY.md`, including provider latency/errors/offer age, matching confidence, basket completeness, and basket compute duration.

## Current automated gates

- `API CI`: Java 25, Maven Wrapper/Maven 3.9.16, PostgreSQL/Testcontainers, backend tests.
- `Contract CI`: exact Node/pnpm, frozen install, generated OpenAPI drift check, strict TypeScript, Vitest, package build.
- `Web CI`: frozen install, shared client build, lint, strict TypeScript, component tests, production build, desktop/mobile Playwright E2E.

## Approved engineering policy

The project follows `docs/ENGINEERING.md`: TDD, evidence-before-claims, automation-first verification, clean Git/PR discipline, continuous changelog maintenance, and documentation synchronized with repository reality.

## Current repository state

- `main` contains completed M0A Tasks 1-5.
- PR #7 contains verified Task 6 operational-observability work and is green pending the final documentation/current-head CI gate and squash merge.
- Broader repository security/automation work remains Task 7: CodeQL, Dependency Review, Dependabot, and consolidation of required CI/security gates.
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

1. Merge verified PR #7 / M0A Task 6.
2. Execute Task 7 from updated `main`: add CodeQL, Dependency Review, Dependabot, and complete repository CI/security gates.
3. Then establish one-command developer verification and reproducible development documentation in Task 8.
4. Retailer-specific implementation waits for verified M0A and a separate M0B plan.

## Definition of M0 success

M0 is complete only when evidence shows that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.
