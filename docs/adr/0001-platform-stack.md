# ADR-0001: Platform Stack and Architectural Baseline

- Status: Accepted
- Date: 2026-08-09
- Accepted: 2026-08-09

## Context

Zakup Gotov is expected to support a public responsive web application first, many volatile retailer integrations, SQL-heavy basket calculations, and native Android/iOS applications later. The project must also support rapid product experiments without locking the core business model to one client or one retailer integration mechanism.

## Decision

Use the following baseline:

- Architecture: modular monolith with explicit ports/adapters at external boundaries.
- Backend: Java 25 LTS, Spring Boot 4.1, Spring MVC, Virtual Threads, Spring Modulith.
- Database: PostgreSQL 18.
- Schema/data access: Flyway + jOOQ.
- API: REST/JSON described by OpenAPI 3.1.x.
- Web: Next.js 16, React, TypeScript strict mode, App Router.
- Future native mobile: Expo + React Native + TypeScript.
- Testing: JUnit 5, AssertJ, Testcontainers, Spring Modulith tests; Vitest, Testing Library, Playwright on web.
- Observability: Micrometer/Actuator with OpenTelemetry-compatible telemetry.
- CI/CD and repository automation: GitHub Actions and GitHub-native security tooling.

## Rationale

### Modular monolith before microservices

The product boundaries are still evolving. A modular monolith preserves low-cost refactoring while allowing structural verification and future extraction of stable modules.

### Java/Spring for the backend

The backend is expected to be dominated by integration orchestration, fault handling, concurrency, data normalization, SQL-heavy comparison logic, and long-term maintainability. Java/Spring is a strong fit for this workload.

### Next.js for web

The initial product must be excellent on both desktop and mobile browsers, support public/SEO-friendly pages later, and allow fast UI experiments. Next.js provides a mature React-based path without coupling the backend to the web runtime.

### Expo/React Native for future mobile

Future native applications can reuse TypeScript API contracts, generated clients, analytics vocabulary, and design tokens while retaining native UI semantics.

### jOOQ rather than JPA as the primary persistence layer

Expected workloads include bulk offer ingestion, upserts, ranking, aggregate basket queries, history, package optimization, and advanced PostgreSQL features. SQL-first type-safe access is preferable to forcing these flows into an ORM entity graph.

### REST/OpenAPI

REST keeps the public product API simple and portable across web/mobile clients. OpenAPI becomes the source for generated client contracts and compatibility checks.

## Consequences

Positive:

- strong separation between clients and product core;
- retailer integrations are replaceable adapters;
- SQL remains explicit and testable;
- mobile can be added without backend redesign;
- the system can evolve toward services only when justified;
- fast web experiments remain independent of backend implementation details.

Costs:

- Java and TypeScript are both required;
- generated API-client workflow must be maintained;
- module boundaries require discipline and automated verification;
- jOOQ/Flyway demand explicit schema design rather than ORM-first iteration.

## Rejected alternatives

- Full TypeScript stack: simpler language story, weaker fit for the chosen long-lived integration/data core.
- Flutter everywhere: strong application UI sharing, less attractive for the public web/SEO-first path.
- Kotlin/Compose Multiplatform everywhere: promising for native sharing, but web remains less suitable for this product direction than Next.js.
- Microservices from day one: unjustified operational and design cost before boundaries/scaling are proven.

## Revisit triggers

Revisit this ADR only when evidence shows one of the following:

- a module requires independent scaling/deployment/ownership;
- REST causes measurable client/product limitations;
- PostgreSQL is no longer adequate for a proven workload;
- native mobile requirements cannot be met efficiently with Expo/React Native;
- an external integration imposes architectural constraints that cannot be isolated behind the provider boundary.