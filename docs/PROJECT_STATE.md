# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A — Platform Foundation**  
Current focus: **finish repository ruleset/release engineering, then enter M0B retailer feasibility**

## Product status

The platform foundation is executable and automatically verified. The core retailer-comparison product is **not implemented yet**. The current web surface intentionally presents project status rather than fake store cards, prices, or comparison behavior.

No retailer integration is considered supported until M0B proves it with acceptable technical/legal evidence and reproducible fixture/contract tests.

## Completed and merged foundation work

- PR #1 — product, architecture, engineering, security, contribution, state/roadmap, and planning foundation.
- PR #2 / M0A Task 1 — Java/Node/pnpm toolchains and monorepo conventions.
- PR #3 / Task 2 — Java 25 + Spring Boot 4.1 API bootstrap, Maven Wrapper, architecture/context verification, API CI.
- PR #4 / Task 3 — PostgreSQL 18 + Flyway + jOOQ + real Testcontainers persistence baseline.
- PR #5 / Task 4 — OpenAPI 3.1 contract, tested system endpoint, generated TypeScript client, Contract CI.
- PR #6 / Task 5 — Next.js 16 / React 19 responsive web shell, component tests, desktop/mobile Playwright, Web CI.
- PR #7 / Task 6 — constrained Actuator health/readiness surface, executable exposure guard, observability/privacy rules.
- PR #8 / Task 7 — CodeQL, Dependency Review, Dependabot version-update policy, and hardened security workflows.
- PR #9 / Task 8 — reproducible `./scripts/verify.sh`, `docs/DEVELOPMENT.md`, and verified clean-runner developer workflow.
- PR #10 — public README/documentation index, repository-governance policy, support routing, branch/workflow audit, and approved governance/release design.
- PR #11 — unconditional required-check candidates, immutable Action SHAs, non-persisted checkout credentials, finite timeouts/concurrency, and Maven `verify` in API CI.
- PR #12 — removal of stale `create-next-app` README/ignore boilerplate and centralized monorepo guidance.

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

## Automated gates on `main`

- **API CI** — Java 25, pinned Maven Wrapper/Maven 3.9.16, PostgreSQL/Testcontainers, backend tests, packaged JAR via Maven `verify`;
- **Contract CI** — exact Node/pnpm, frozen install, OpenAPI generation drift, typecheck, Vitest, build;
- **Web CI** — frozen install, shared client build, lint, typecheck, component tests, production build;
- **Web E2E** — Playwright against the production-built web app on desktop/mobile Chromium profiles;
- **CodeQL / Java**;
- **CodeQL / JavaScript-TypeScript**;
- **Dependency Review**;
- local/clean-runner **`./scripts/verify.sh`** — unified backend/contract/web verification.

Recurring CI Actions are pinned to immutable full commit SHAs where introduced by the project, use non-persisted checkout credentials for read-only jobs, have finite timeouts, and cancel superseded PR/ref runs.

## Repository governance and security state

Verified repository-admin baseline:

- squash merge enabled; merge commits and rebase merge disabled;
- auto-merge and update-branch enabled;
- merged source branches are deleted automatically;
- only `main` plus active PR branches are retained;
- default workflow token permissions are read-only and Actions cannot approve pull requests;
- Dependency Graph enabled and SBOM endpoint available;
- Dependabot alerts/security updates enabled;
- secret scanning enabled;
- secret scanning push protection enabled;
- private vulnerability reporting enabled;
- CodeQL and Dependency Review are operational and green after Dependency Graph enablement.

The remaining repository-governance step is to activate the `main` ruleset using the now-proven required check names, plus any final Actions/security policy toggles that are available and appropriate for this public repository.

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
- [`superpowers/specs/2026-08-09-repository-governance-and-release-design.md`](superpowers/specs/2026-08-09-repository-governance-and-release-design.md);
- [`superpowers/plans/2026-08-09-repository-hardening.md`](superpowers/plans/2026-08-09-repository-hardening.md).

The intended release experience is a ready Docker Compose bundle using prebuilt `web` and `api` GHCR images plus PostgreSQL, with multi-platform images, exact-bundle smoke tests, vulnerability scanning, SBOM, provenance/attestations, and immutable digests. This is **approved design, not implemented functionality yet**.

## Current critical unknowns

1. Which target retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers achieve useful basket coverage with acceptable freshness/reliability?
3. What location precision must be used transiently and what, if anything, may be persisted?
4. How can delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What deterministic product-matching quality is achievable before any AI-assisted stage is justified?

## Immediate next work

1. Activate and verify the `main` repository ruleset with proven required checks and no force-push/deletion bypasses.
2. Apply remaining appropriate GitHub Actions/security hardening toggles and verify the resulting repository settings.
3. Implement the approved Docker/GHCR release-engineering subsystem with multi-platform images, Compose smoke tests, scanning, SBOM/provenance/attestations, and stable/prerelease semantics.
4. Run final M0A verification.
5. Hand off to a separately planned M0B Retailer Feasibility phase.

## Definition of M0 success

M0 is complete only when evidence proves at least two retailer integrations can support a repeatable location-specific comparison flow with reproducible fixtures/tests and documented technical/legal/freshness constraints.
