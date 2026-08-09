# Project State

Updated: 2026-08-09

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is: choose what to cook or buy, provide a location, and compare complete grocery baskets across supported nearby retailers using current price and availability data.

Repository: `True-Ruslan/zakup-gotov`
Visibility: Public
Current phase: **M0 — Product & Integration Discovery**

## Product status

No production code has been implemented yet.

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

## Current repository state

- `main` contains only the initial README until foundation PR #1 is merged.
- Draft PR #1 contains the approved foundation documentation and repository community files.
- M0A Platform Foundation implementation plan is documented at `docs/superpowers/plans/2026-08-09-m0a-platform-foundation.md`.
- No production application code or CI has been implemented yet.
- No license decision has been made.
- No external retailer integration has been proven yet.

## Current critical unknowns

1. Which Russian retailers expose a technically stable and legally acceptable path to location-specific catalog, price, and availability data?
2. Can at least two providers be supported with sufficient freshness and reliability for a useful basket comparison?
3. What address/location precision must be retained versus used transiently?
4. How should delivery fees/minimum order constraints be obtained and normalized per retailer?
5. What baseline matching accuracy is achievable with deterministic normalization/ranking before AI-assisted matching is justified?

## Immediate next decision gate

Review the M0A Platform Foundation implementation plan. After approval, execute it task-by-task with TDD and verification checkpoints. M0B retailer feasibility is planned separately so volatile external integration experiments do not distort the platform foundation.

## Definition of M0 success

M0 is complete only when the project has evidence—not assumptions—that at least two retailer integrations can support a repeatable comparison flow for one supported city/location context, with automated fixtures/tests and documented legal/technical constraints.