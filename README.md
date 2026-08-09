# Закуп готов / Zakup Gotov

> Recipe-to-cart grocery comparison service for finding the best complete basket across nearby stores.

**Status:** M0 — Product & Integration Discovery

Zakup Gotov is an early-stage product for turning a recipe, meal plan, or manual grocery list into a location-aware comparison of complete grocery baskets.

The product is intentionally focused on the **whole basket**, not isolated “cheapest item” search:

```text
recipe / meal plan / grocery list
        ↓
shopping requirements
        ↓
location + retailer context
        ↓
product matching
        ↓
current price + availability
        ↓
package / quantity optimization
        ↓
complete basket comparison
```

## Product promise

Choose what you want to cook or buy. Zakup Gotov should help answer:

- Which nearby retailer can fulfill the whole list?
- What will the basket actually cost?
- Which items are missing or uncertain?
- How fresh are the prices and availability signals?
- Is one-store convenience better than splitting the basket?

The project will not hide incomplete matches or silently treat stale prices as current.

## Current focus

The first milestone is **not** a full recipe application. M0 exists to prove the hardest assumption first: that at least two target retailers can provide sufficiently reliable, location-specific product, price, and availability data through technically and legally acceptable integration paths.

M0 is split into:

- **M0A — Platform Foundation:** executable monorepo, API/web foundations, database, contracts, observability, CI/security, and repository governance.
- **M0B — Retailer Feasibility:** provider research, legal/technical evidence, probe harness, fixtures, and at least two proven retailer integrations.

See:

- [Project state](docs/PROJECT_STATE.md)
- [Roadmap](docs/ROADMAP.md)
- [Development](docs/DEVELOPMENT.md)
- [Engineering policy](docs/ENGINEERING.md)
- [Observability](docs/OBSERVABILITY.md)
- [Foundation design](docs/superpowers/specs/2026-08-09-zakup-gotov-foundation-design.md)
- [M0A implementation plan](docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md)
- [ADR-0001: Platform stack](docs/adr/0001-platform-stack.md)
- [Changelog](CHANGELOG.md)

## Approved technical foundation

The approved baseline is:

- **Backend:** Java 25 LTS, Spring Boot 4.1, Spring MVC, Virtual Threads, Spring Modulith
- **Database:** PostgreSQL 18
- **Persistence:** Flyway + jOOQ
- **API:** REST/JSON + OpenAPI 3.1.x
- **Web:** Next.js 16, React, TypeScript
- **Future mobile:** Expo + React Native + TypeScript
- **Testing:** JUnit 5, Testcontainers, Modulith tests, Vitest, Testing Library, Playwright
- **Observability:** Micrometer/Actuator with OpenTelemetry-compatible telemetry
- **Repository/CI:** GitHub Actions and GitHub-native security tooling

The architecture was approved on 2026-08-09 and is recorded in accepted ADR-0001.

## Development

The supported developer workflow is documented in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md). With the pinned Java 25 / Node 24.18.1 / pnpm 11.4.0 toolchains and Docker running, the primary local verification entrypoint is:

```bash
./scripts/verify.sh
```

The command intentionally fails rather than silently skipping Testcontainers, generated-contract drift, type checks, tests, or production builds.

Responsive Playwright browser tests are an explicit additional gate; see the development guide for the Chromium setup and command.

## Engineering principles

1. **TDD by default.** Executable behavior follows RED -> GREEN -> REFACTOR, with the expected failure observed before production implementation.
2. **Evidence before claims.** A change is complete only with fresh automated verification appropriate to that change.
3. **Automation first.** Repeated manual verification is automation debt; deterministic CI should cover everything reasonably automatable.
4. **Documentation is repository truth.** State, roadmap, ADRs, implementation plans, and changelog stay synchronized with actual work.
5. **Clean Git history.** Short-lived branches, cohesive commits, small PRs, required checks, and squash-only target history.
6. **Modular monolith first.** Preserve cheap refactoring while enforcing module boundaries.
7. **Retailers are adapters.** External provider behavior must not leak into the product domain.
8. **Freshness is data.** A comparable offer includes source, retailer context, availability, and observation time.
9. **Uncertainty is visible.** Ambiguous matching and incomplete baskets are explicit states.
10. **YAGNI infrastructure.** No Kafka, Kubernetes, Redis, Elasticsearch, vector DB, or microservices until evidence justifies them.

Full rules: [docs/ENGINEERING.md](docs/ENGINEERING.md).

## Roadmap

- **M0:** Product & Integration Discovery
- **M1:** Shopping Core
- **M2:** Recipes
- **M3:** Weekly Planning
- **M4:** Basket Optimization
- **M5:** Productization
- **M6:** Native Mobile

Details: [docs/ROADMAP.md](docs/ROADMAP.md).

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md), [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md), and [docs/ENGINEERING.md](docs/ENGINEERING.md) before proposing substantial changes. Security vulnerabilities must not be reported through public issues; see [SECURITY.md](SECURITY.md).

## License

This repository is public, but no open-source license has been selected yet. Unless and until a license is added, no additional license grant should be assumed.
