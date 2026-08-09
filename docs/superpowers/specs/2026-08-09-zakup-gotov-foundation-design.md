# Zakup Gotov — Foundation Design

Date: 2026-08-09
Status: Proposed for foundation approval

## 1. Goal

Build a product that turns a recipe, meal plan, or manual grocery list into a location-aware comparison of complete grocery baskets across nearby retailers.

The core user promise is not “find the cheapest single product”. It is:

> Choose what you want to cook. Zakup Gotov finds where the whole basket is actually more convenient and cost-effective to buy.

## 2. Primary users

Initial target users are people in Russia who already know what they want to cook or buy and want to minimize the time and cost of assembling the grocery basket.

Primary jobs to be done:

1. Convert a recipe into normalized shopping requirements.
2. Combine ingredients from several recipes into one shopping list.
3. Resolve the user location and retailer fulfillment context.
4. Find matching store SKUs for each requirement.
5. Compare full-basket cost, availability, freshness, delivery fees, and minimum order constraints.
6. Prefer either one-store convenience or minimum total cost across multiple stores.

## 3. Success criteria

The first product milestone is successful when, for at least two retailers and one supported city, the system can:

- accept a manual list of at least 10 grocery requirements;
- resolve a user-provided address into a retailer-specific fulfillment context;
- find a valid SKU candidate for at least 90% of supported common grocery requirements in the test corpus;
- surface current price, availability, source, and observation timestamp;
- calculate a complete-basket comparison without silently substituting incompatible products;
- clearly identify unmatched or uncertain items;
- reproduce comparison results through automated integration tests using recorded provider fixtures.

## 4. Product scope

### In scope for the initial product

- Manual grocery list.
- Address/location input.
- Store/provider discovery for a location.
- Product requirement normalization.
- SKU candidate matching.
- Price and availability snapshots.
- Single-store basket comparison.
- Transparent freshness timestamps.
- Provider health/failure states.
- Recipe model after the comparison core is proven.
- Weekly meal planning after recipe support is stable.

### Explicitly out of scope initially

- Native Android/iOS clients.
- Multi-store checkout orchestration.
- Payment processing.
- Loyalty-card account linking.
- Automatic coupon scraping.
- Kafka, Kubernetes, Elasticsearch/OpenSearch, dedicated vector databases.
- Microservices.
- AI-first matching as a hard dependency.

These may be added only when measured requirements justify them.

## 5. UX model

### Core flow

1. User creates or opens a shopping requirement set.
2. User enters an address or selects a saved location.
3. System resolves location and supported retailer providers.
4. Providers are queried in parallel.
5. Product matching produces one or more ranked SKU candidates per requirement.
6. Basket optimizer chooses quantities/packages and computes totals.
7. User receives a comparison of retailers.
8. User can inspect substitutions, missing items, timestamps, and assumptions before leaving for retailer checkout.

### Required UI states

**Happy:** all requirements matched with fresh offers and at least one complete basket.

**Empty:** no list yet, no supported retailers for the selected location, or no recipes selected.

**Loading:** comparison must show retailer-level progress rather than one indefinite global spinner.

**Partial failure:** one retailer may fail without invalidating successful results from other retailers.

**Error:** failures must identify the failed action and recovery path; internal provider errors must not be exposed raw.

**Constraint:** incomplete baskets must never be ranked as fully comparable without an explicit completeness penalty/label.

## 6. Architecture

### 6.1 Architectural style

Start as a modular monolith with strict module boundaries, not microservices.

Rationale:

- product boundaries are still evolving;
- cross-module refactoring is cheap inside one deployable;
- external grocery integrations are already operationally complex;
- Spring Modulith can verify module dependencies and support module-focused tests;
- stable modules can be extracted later if scale or ownership requires it.

### 6.2 Backend

- Java 25 LTS
- Spring Boot 4.1
- Spring MVC
- Virtual Threads for concurrent blocking I/O
- Spring Modulith
- Bean Validation
- Actuator/Micrometer
- OpenTelemetry-compatible telemetry

Candidate application modules:

- `shopping`
- `recipe`
- `ingredient`
- `location`
- `provider`
- `catalog`
- `matching`
- `pricing`
- `basket`
- `user`

Module internals are private by default. Cross-module access is only through declared APIs/events.

### 6.3 Provider boundary

External retailer systems are isolated behind a provider port.

Conceptual responsibilities:

- resolve fulfillment context for a location;
- discover searchable catalog scope;
- search or retrieve product candidates;
- retrieve price and availability;
- expose retailer checkout/deep-link capabilities where legally and technically supported.

The domain must not know whether a provider uses an official API, partner API, documented catalog endpoint, or another approved integration mechanism.

Every provider adapter must normalize external data into internal domain records and preserve source metadata.

### 6.4 Data model

Primary database: PostgreSQL 18.

Persistence strategy:

- Flyway owns schema migrations;
- jOOQ is the primary SQL access layer;
- SQL is explicit and type-safe;
- JPA/Hibernate is not the default persistence abstraction.

Key concepts include:

- canonical ingredient/product requirement;
- retailer;
- retailer location/fulfillment context;
- retailer SKU;
- observed offer;
- price;
- availability;
- package quantity/unit;
- match candidate and confidence;
- basket quote;
- observation timestamp and source.

A price without retailer context and observation time is not a valid comparable offer.

### 6.5 API

Public product API uses REST/JSON with an OpenAPI contract.

Initial OpenAPI target: 3.1.x for tooling maturity.

Web and future mobile clients consume generated API clients. Backend APIs are product APIs, not implementation-specific endpoints for one frontend.

### 6.6 Web

- Next.js 16
- React
- TypeScript with strict mode
- App Router
- responsive web UI from the first milestone
- accessible semantic HTML
- server rendering where it improves initial load/SEO
- client-side interactivity only where required

The web application must work well on desktop and mobile browsers before a native app exists.

### 6.7 Mobile path

Future native clients:

- Expo
- React Native
- TypeScript

Shared assets should focus on API contracts, generated clients, analytics vocabulary, validation/domain schemas where appropriate, and design tokens. We do not require web DOM components and React Native views to share the same component implementation.

## 7. Matching strategy

Matching is confidence-based and explainable.

Initial pipeline:

1. normalize requirement text and units;
2. map aliases/synonyms to canonical concepts;
3. retrieve lexical candidates;
4. apply category/unit/package constraints;
5. rank candidates;
6. expose confidence and reasons;
7. require explicit handling for ambiguous/low-confidence matches.

Embeddings or LLM-assisted ranking may be added later as an optional stage, but deterministic rules and test corpora remain the baseline.

## 8. Basket optimization

Two product modes are planned:

### One store

Choose the best complete basket from a single retailer, considering:

- product cost;
- package quantities;
- availability;
- minimum order;
- delivery/service fees when available;
- completeness;
- data freshness.

### Lowest total cost

Future mode may split the basket across retailers. It must include delivery and minimum-order costs, otherwise the comparison is misleading.

## 9. Freshness and trust

Every offer snapshot stores at minimum:

- provider;
- retailer/fulfillment context;
- SKU;
- price;
- availability;
- observed timestamp;
- source identifier/metadata needed for diagnostics.

The UI must show when price data was last checked. Stale data thresholds will be provider-specific and configurable.

## 10. Reliability

Provider failures are expected, not exceptional architecture events.

Required behavior:

- timeouts per provider;
- bounded retries only for retry-safe failures;
- rate-limit awareness;
- partial comparison results;
- schema-change detection through contract fixtures;
- no silent fallback from current to stale price without marking it;
- structured provider diagnostics and metrics.

## 11. Testing strategy

Backend:

- JUnit 5
- AssertJ
- Testcontainers with real PostgreSQL
- Spring Modulith verification/module tests
- provider contract/fixture tests
- deterministic basket optimizer tests
- property-based tests for quantity/package edge cases where useful

Web:

- Vitest
- Testing Library
- Playwright for critical end-to-end journeys

Critical journey:

`shopping requirements -> location -> provider results -> matching -> basket comparison`

Manual testing should focus on genuinely external or visual behavior that cannot be made reliable in automation.

## 12. Observability

Instrumentation vocabulary should be vendor-neutral and OpenTelemetry-compatible.

Important measurements include:

- provider request latency/error/timeout rate;
- provider schema/parse failures;
- offer age/freshness;
- matching success and confidence distribution;
- complete basket rate;
- basket computation latency;
- retailer coverage by location.

Product analytics is isolated behind an internal analytics abstraction so PostHog or another provider can be replaced without rewriting business UI code.

## 13. Security and privacy

- No retailer or third-party credentials in the repository.
- Secrets are injected through environment/secret stores.
- Precise user addresses are treated as sensitive user data.
- Store only location precision that is justified by product behavior.
- Logs and traces must redact credentials, authorization headers, and unnecessary address details.
- Public repository security reports should use GitHub Private Vulnerability Reporting once enabled.
- Dependency and code scanning are mandatory CI/repository controls.

## 14. Repository strategy

Monorepo structure is preferred:

```text
apps/
  api/
  web/
packages/
  api-client/
  analytics/
  design-tokens/
openapi/
docs/
  adr/
  integrations/
  product/
  superpowers/specs/
.github/
```

`apps/mobile` is created only when the mobile milestone begins.

Main branch policy target:

- PR-only changes;
- squash merge;
- linear history;
- required CI/status checks;
- no force pushes;
- no branch deletion;
- CodeQL/dependency/security checks.

## 15. Delivery sequence

### M0 — Product & Integration Discovery

Prove provider feasibility for at least two retailers before building the full recipe product.

### M1 — Shopping Core

Manual list -> address -> two retailers -> matching -> complete basket comparison.

### M2 — Recipes

Recipe CRUD/import model, servings, ingredient quantities, recipe -> shopping requirements.

### M3 — Weekly Planning

Meal plan -> merged shopping requirements -> pantry exclusions.

### M4 — Basket Optimization

Package optimization, fees, minimum orders, single-store and multi-store strategies.

### M5 — Productization

Accounts, saved lists, analytics, experiments, improved resilience.

### M6 — Native Mobile

Expo/React Native clients on top of the stable product API.

## 16. Alternatives considered

### Full TypeScript backend

Pros: one language, rapid prototyping, broad frontend sharing.

Rejected as the primary recommendation because the backend will be integration-heavy, concurrency-heavy, SQL-heavy, and long-lived. Java/Spring provides a stronger fit without slowing web hypothesis testing.

### Kotlin Multiplatform / Compose Multiplatform everywhere

Pros: substantial Kotlin sharing and strong native targets.

Not selected because the public web experience, SEO, browser ecosystem, and fast frontend experimentation are better served by Next.js today. KMP remains an option for future non-UI shared native modules if a real need appears.

### Microservices from day one

Rejected because the domain boundaries and scaling needs are not yet proven. The operational and distributed-systems cost would slow discovery.

## 17. Acceptance criteria for foundation approval

The foundation is approved when:

- product promise and initial user journey are agreed;
- modular-monolith architecture is accepted;
- Java/Spring backend and Next.js web are accepted;
- Expo/React Native is accepted as the future native path;
- PostgreSQL + Flyway + jOOQ is accepted as persistence baseline;
- REST/OpenAPI is accepted as the client contract;
- provider adapters are accepted as the core external-integration boundary;
- M0 explicitly prioritizes provider feasibility over recipe feature implementation;
- no unresolved architectural blocker remains before writing the implementation plan.

## 18. Recommended next step

After this specification is reviewed and approved, write a detailed implementation plan for M0. The first executable work should establish repository quality gates and build an integration feasibility harness, not the complete application UI.