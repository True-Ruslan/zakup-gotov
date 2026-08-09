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

See:

- [Project state](docs/PROJECT_STATE.md)
- [Roadmap](docs/ROADMAP.md)
- [Foundation design](docs/superpowers/specs/2026-08-09-zakup-gotov-foundation-design.md)
- [ADR-0001: Platform stack](docs/adr/0001-platform-stack.md)
- [Changelog](CHANGELOG.md)

## Proposed technical foundation

The current foundation proposal is:

- **Backend:** Java 25 LTS, Spring Boot 4.1, Spring MVC, Virtual Threads, Spring Modulith
- **Database:** PostgreSQL 18
- **Persistence:** Flyway + jOOQ
- **API:** REST/JSON + OpenAPI 3.1.x
- **Web:** Next.js 16, React, TypeScript
- **Future mobile:** Expo + React Native + TypeScript
- **Testing:** JUnit 5, Testcontainers, Modulith tests, Vitest, Testing Library, Playwright
- **Observability:** Micrometer/Actuator with OpenTelemetry-compatible telemetry
- **Repository/CI:** GitHub Actions and GitHub-native security tooling

No application implementation should be considered locked until the foundation design is reviewed and approved.

## Architectural principles

1. **Modular monolith first.** Preserve cheap refactoring while enforcing module boundaries.
2. **Retailers are adapters.** External provider behavior must not leak into the product domain.
3. **Freshness is data.** A comparable offer includes source, retailer context, availability, and observation time.
4. **Uncertainty is visible.** Ambiguous matching and incomplete baskets are explicit states.
5. **Automate what can be automated.** Provider fixtures/contracts and critical user journeys should minimize repeated manual verification.
6. **YAGNI infrastructure.** No Kafka, Kubernetes, Redis, Elasticsearch, vector DB, or microservices until evidence justifies them.

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

Read [CONTRIBUTING.md](CONTRIBUTING.md) before proposing substantial changes. Security vulnerabilities must not be reported through public issues; see [SECURITY.md](SECURITY.md).

## License

This repository is public, but no open-source license has been selected yet. Unless and until a license is added, no additional license grant should be assumed.
