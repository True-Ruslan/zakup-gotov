# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A — Platform Foundation**  
Current focus: **repository hygiene, security/governance completion, then release engineering design implementation**

## Product status

The platform foundation is executable and automatically verified, but the core retailer-comparison product is **not implemented yet**. The current web surface intentionally presents project status rather than fake store cards, prices, or comparison behavior.

No retailer integration is considered supported until M0B proves it with acceptable technical/legal evidence and reproducible fixture/contract tests.

## Completed and merged foundation work

- PR #1 — product, architecture, engineering, security, contribution, state/roadmap, and planning foundation.
- PR #2 / M0A Task 1 — Java/Node/pnpm toolchains and monorepo conventions.
- PR #3 / Task 2 — Java 25 + Spring Boot 4.1 API bootstrap, Maven Wrapper, architecture/context verification, API CI.
- PR #4 / Task 3 — PostgreSQL 18 + Flyway + jOOQ + real Testcontainers persistence baseline.
- PR #5 / Task 4 — OpenAPI 3.1 contract, tested system endpoint, generated TypeScript client, Contract CI.
- PR #6 / Task 5 — Next.js 16 / React 19 responsive web shell, component tests, desktop/mobile Playwright, Web CI.
- PR #7 / Task 6 — constrained Actuator health/readiness surface, executable exposure guard, observability/privacy rules.
- PR #9 / Task 8 — reproducible `./scripts/verify.sh`, `docs/DEVELOPMENT.md`, and verified clean-runner developer workflow.

Task 8 was completed before Task 7 because Task 7 is externally blocked by a GitHub repository security setting; independent verified work was not held back artificially.

## Verified platform baseline

### Backend

- Java 25;
- Spring Boot 4.1;
- Spring MVC + Virtual Threads;
- Spring Modulith architecture verification;
- PostgreSQL 18;
- Flyway;
- jOOQ;
- Testcontainers with real PostgreSQL 18.4 in integration tests.

### Contracts and clients

- OpenAPI 3.1 source contract;
- generated `@zakup-gotov/api-client`;
- generated-schema drift gate;
- strict TypeScript typechecking.

### Web

- Next.js 16.2;
- React 19.2;
- TypeScript;
- Vitest + Testing Library;
- Playwright production-browser coverage for desktop and mobile viewports.

### Operations

Public management HTTP surface is intentionally limited to:

- `/actuator/health`;
- `/actuator/health/liveness`;
- `/actuator/health/readiness`;
- `/actuator/info`.

Environment, configuration-properties, and metrics Actuator endpoints remain HTTP-inaccessible. Request-detail logging is disabled by default. Provider credentials, raw payloads, authorization material, precise user addresses, and arbitrary user input must not become telemetry labels/log content by default.

## Current automated gates

Merged and active on `main`:

- **API CI** — Java 25, pinned Maven Wrapper/Maven 3.9.16, backend tests, PostgreSQL/Testcontainers;
- **Contract CI** — exact Node/pnpm, frozen install, OpenAPI generation drift, typecheck, Vitest, build;
- **Web CI** — frozen install, shared client build, lint, typecheck, component tests, production build;
- **Web E2E** — Playwright against the production-built web app on desktop/mobile Chromium profiles;
- local/clean-runner **`./scripts/verify.sh`** — unified backend/contract/web verification.

PR #8 adds permanent CodeQL, Dependency Review, and Dependabot configuration. CodeQL has already passed for Java and JavaScript/TypeScript. Dependency Review is intentionally still red because GitHub reports Dependency Graph as unavailable/disabled for this repository; the workflow is not weakened to hide that configuration defect.

## Current repository work

- `chore/repository-hygiene` — public README/documentation cleanup, branch/workflow audit, repository-governance policy, support routing, and approved governance/release design documentation.
- PR #8 / `ci/m0a-security-gates` — open and blocked only by repository Dependency Graph/security configuration.
- Historical merged branches are safe to delete; future merged PR branches should be removed automatically through repository settings.

No obsolete one-off generator/scaffold workflow remains on `main`; only recurring CI workflows are kept permanently.

## Approved engineering policy

[`ENGINEERING.md`](ENGINEERING.md) is mandatory repository policy:

- TDD for executable behavior;
- evidence before completion claims;
- automation-first verification;
- real integration dependencies when practical and deterministic;
- short-lived branches and small PRs;
- documentation/changelog synchronized with repository reality;
- no silent security/test bypasses;
- no retailer live calls in normal deterministic CI.

## Approved next platform direction

Repository governance and release engineering direction is approved and documented in:

- [`REPOSITORY_GOVERNANCE.md`](REPOSITORY_GOVERNANCE.md);
- [`superpowers/specs/2026-08-09-repository-governance-and-release-design.md`](superpowers/specs/2026-08-09-repository-governance-and-release-design.md).

The intended release experience is a ready Docker Compose bundle using prebuilt `web` and `api` GHCR images plus PostgreSQL, with multi-platform images, exact-bundle smoke tests, vulnerability scanning, SBOM, provenance/attestations, and immutable digests. This is **approved design, not implemented functionality yet**.

## Current critical unknowns

1. Which target retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers achieve useful basket coverage with acceptable freshness/reliability?
3. What location precision must be used transiently and what, if anything, may be persisted?
4. How can delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What deterministic product-matching quality is achievable before any AI-assisted stage is justified?

## Immediate next work

1. Complete and merge repository-hygiene documentation after CI verification.
2. Enable/verify required GitHub security settings so PR #8 Dependency Review becomes genuinely green, then merge PR #8.
3. Activate repository merge/ruleset/security governance using the exact proven check names.
4. Write and execute the implementation plan for the approved Docker/GHCR release-engineering design.
5. Run final M0A verification and hand off to a separately planned M0B Retailer Feasibility phase.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/legal/freshness constraints.
