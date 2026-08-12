# M1 Stateless Comparison Preview — Design

**Status:** Approved design, implementation not started  
**Date:** 2026-08-12  
**Base:** `main@3c4a60f94d55470aaa4365fc9755ef8ab5b1a11a`

## 1. Goal

Deliver the first end-to-end user journey that accepts a shopping list and a provider-neutral locality, runs the existing deterministic shopping/matching/basket/comparison core, and returns truthful comparison outcomes for all canonical retailers.

The slice must be useful as a real product boundary while remaining safe when production retailer evidence is unavailable. It must not fabricate prices, package sizes, availability, store identifiers, or source provenance.

The design must also preserve a direct path to mass distribution, mobile clients, horizontal scaling, commercial retailer data providers, user accounts, entitlements, usage metering, and monetization without requiring a rewrite of Shopping Core.

## 2. Product outcome

A user can submit:

- a locality such as a city or settlement;
- one or more shopping items;
- a positive quantity and supported unit for every item.

The service returns a comparison preview containing all eight canonical retailer/banner identities in stable registry order. Each retailer exposes a product-safe summary plus item-level resolution details when runtime evidence exists.

The preview is transient. M1 does not persist the request, create an account, create a saved shopping list, or start background refresh work.

## 3. Non-goals

This slice does **not** implement:

- authentication, accounts, households, workspaces, or shared lists;
- database persistence or comparison history;
- billing, subscriptions, payment providers, or tariff definitions;
- Redis, queues, background workers, Kafka, or Kubernetes;
- automatic cheapest-retailer ranking;
- fuzzy, synonym, semantic, or LLM-based product matching;
- parsing package quantity from product names;
- address-to-store resolution for retailers that do not yet have an accepted resolver;
- production activation of a retailer whose access/usage-rights gate is unresolved;
- live retailer calls from ordinary CI;
- public exposure of provider IDs, acquisition modes, source references, SKUs, or fulfillment-context identifiers.

## 4. Architecture principles

### 4.1 Stateless-first application boundary

`POST /api/v1/comparison-previews` is a stateless application use case. A request contains everything needed to construct the transient shopping list and product location.

The request does not depend on HTTP session state or server affinity. This allows future horizontal API scaling without changing the comparison domain.

The endpoint may later be wrapped by authentication, entitlements, rate limiting, caching, or metering, but those concerns must not become dependencies of Shopping Core.

### 4.2 Existing domain remains authoritative

The new use case composes existing bounded responsibilities rather than duplicating them:

1. `shopping` — validated requirements and canonical quantities;
2. `location` — provider-neutral product location;
3. evidence acquisition port — retailer runtime evidence for that product location;
4. `provider` — normalized observations and immutable snapshots;
5. `matching` — deterministic exact/normalized matching;
6. `basket` — package selection and single-store quote;
7. `comparison` — product-safe retailer state and freshness summary.

The application layer orchestrates these modules but does not reimplement their business rules.

### 4.3 External systems enter through ports

The M1 use case introduces an application-facing runtime-evidence port. A concrete production adapter may return evidence only when a retailer path is explicitly allowed and configured.

Future external concerns remain ports/adapters rather than domain dependencies, including:

- location/context resolution;
- persisted comparison history;
- identity and household ownership;
- entitlement checks;
- usage metering;
- commercial data providers;
- caches and background acquisition workers.

### 4.4 Fixture and production evidence are physically separated

Deterministic fixture evidence is permitted only in tests/test composition.

Production composition must never silently fall back to fixture prices or package quantities. When production evidence is absent, the retailer remains visible with a truthful `UNAVAILABLE`/`INCOMPLETE` reason according to the existing comparison vocabulary.

Ordinary CI remains offline with respect to live retailer traffic.

## 5. Public API contract

### 5.1 Endpoint

`POST /api/v1/comparison-previews`

The operation name is `createComparisonPreview`.

The endpoint is versioned under `/api/v1` and is described in OpenAPI. The generated TypeScript client is the only web transport contract.

### 5.2 Request

```json
{
  "locality": "Москва",
  "items": [
    {
      "id": "c281d71c-2b27-46ef-a7af-3d624a7447cf",
      "requirement": "Молоко 3,2%",
      "quantity": {
        "amount": 2,
        "unit": "LITER"
      }
    }
  ]
}
```

Initial validation limits:

- `locality`: 1..160 Unicode characters after trimming/collapsing whitespace;
- `items`: 1..100;
- item `id`: UUID supplied by the client;
- `requirement`: 1..240 Unicode characters after boundary normalization;
- `quantity.amount`: strictly positive decimal;
- `quantity.unit`: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, or `LITER`.

The API must reject malformed or over-limit input with HTTP 400. It must not truncate user input silently.

The public request intentionally contains no provider/store IDs and no exact address in this slice.

### 5.3 Response

The response preserves the normalized request context and returns retailers in canonical registry order.

```json
{
  "locality": "Москва",
  "items": [
    {
      "id": "c281d71c-2b27-46ef-a7af-3d624a7447cf",
      "requirement": "Молоко 3,2%",
      "quantity": {
        "amount": 2000,
        "unit": "MILLILITER"
      }
    }
  ],
  "retailers": []
}
```

Each retailer uses the existing product-safe summary vocabulary:

- coverage;
- production access;
- comparison status: `READY`, `UNCERTAIN`, `INCOMPLETE`, or `UNAVAILABLE`;
- finite reason codes;
- optional basket total only where structurally valid;
- optional conservative freshness only where structurally valid.

When a retailer has a successful basket quote, the response additionally exposes item-level results in shopping-list order.

### 5.4 Item-level result vocabulary

Public item outcomes are derived from the existing basket resolution vocabulary:

- `FULFILLED`;
- `AVAILABILITY_UNKNOWN`;
- `UNMATCHED`;
- `AMBIGUOUS`;
- `UNAVAILABLE`;
- `PACKAGE_QUANTITY_UNKNOWN`;
- `QUANTITY_UNIT_MISMATCH`.

For a selected item, the API may expose only product-safe values needed to understand the quote:

- requested item ID and requirement;
- selected product display name;
- requested canonical quantity;
- package quantity when trusted evidence exists;
- whole package count;
- covered quantity;
- line total and ISO currency;
- item outcome.

It must not expose:

- SKU;
- `sourceProviderId`;
- acquisition mode;
- source reference or URL;
- provider fulfillment-context ID;
- cookies, headers, tokens, raw HTML, or raw provider payloads.

Ambiguous results may expose a bounded list of candidate product display names, but not their provider/internal identifiers. The initial bound is 10 candidates; additional ambiguity is represented by the same `AMBIGUOUS` status without leaking unlimited result sets.

## 6. Application use case

Introduce a single application service responsible for the vertical flow. The intended responsibility is equivalent to:

```text
ComparisonPreviewService
  -> validate/construct ShoppingList
  -> construct locality-only ProductLocation
  -> request retailer runtime evidence
  -> assemble retailer comparison result
  -> project product-safe preview
```

The service must be deterministic for a fixed evidence input. It must not persist data or emit live retailer requests by itself.

### 6.1 Runtime evidence port

The application layer depends on a port that returns retailer-scoped evidence for one transient shopping list and one `ProductLocation`.

The port result must preserve explicit distinctions between:

- no production path/data;
- source unavailable;
- successful provider evidence;
- package quantity known/unknown;
- freshness evidence available/unavailable.

The application service must not infer missing package quantities or availability.

### 6.2 Production adapter

The initial production adapter is fail-closed. It uses only explicitly approved/configured production paths. Retailers without a safe active path produce no fabricated runtime evidence.

Issue-specific blockers such as Magnit location resolution and usage-rights remain blockers; this endpoint must not bypass them simply because M0 technical feasibility was proven.

### 6.3 Test adapter

A deterministic test-only evidence adapter drives integration and browser acceptance scenarios.

It must be placed under test/test-support composition so it cannot be activated by the production Spring profile or production artifact accidentally.

It supplies controlled cases covering at least:

- `READY` retailer;
- `UNCERTAIN` retailer with unknown availability;
- `INCOMPLETE` retailer;
- `UNAVAILABLE` retailer;
- unmatched item;
- ambiguous item;
- package quantity unknown;
- incompatible quantity unit.

Fixture evidence is evidence for correctness of the product pipeline, not evidence that a retailer is production-ready.

## 7. Location and privacy boundary

The public M1 request accepts locality only.

The application creates `ProductLocation.localityOnly(...)`. Exact residential address is neither required nor persisted by this use case.

Provider-specific store/delivery contexts stay behind the evidence/location adapter boundary. They are never accepted as public request fields and never returned in the public response.

When exact-address resolution is introduced later, it must follow:

```text
sensitive address -> ephemeral resolver -> provider-scoped context
```

Default telemetry, logs, analytics, comparison history, and usage events must not contain raw addresses or shopping-list contents.

## 8. Web critical journey

The home experience evolves from status-only M1 to an actual comparison preview form.

The page contains:

- locality input;
- repeatable shopping-item rows;
- requirement text;
- positive quantity amount;
- supported unit selector;
- add/remove item actions;
- `Сравнить корзину` submit action.

After submit:

- all eight retailers remain visible;
- retailer status and reasons remain explicit;
- totals appear only for structurally valid states;
- item-level gaps remain visible;
- freshness basis is explicit;
- no cheapest-winner badge is shown in this slice;
- API/network timeout produces one accessible error state and no fabricated retailer cards.

The form must be keyboard accessible and responsive on the existing desktop/mobile Playwright viewports.

## 9. Error handling

### 9.1 Client input errors

Invalid request shape or validation failure returns HTTP 400 with a stable product-safe problem response.

No Java class names, stack traces, SQL details, provider IDs, tokens, source URLs, or raw upstream errors may be returned.

### 9.2 Runtime retailer failures

A retailer/source failure is data in the comparison result, not a whole-request HTTP 5xx, as long as the application itself remains healthy.

The comparison returns all canonical retailers with the appropriate product-safe reason.

### 9.3 Application failure

Unexpected application defects remain fail-fast server errors and are logged with correlation metadata. Public responses remain sanitized.

The web request to the Zakup Gotov API remains bounded by timeout/abort behavior; a hanging backend must not leave the page indefinitely pending.

## 10. Security and abuse controls

The endpoint must include product-level protections suitable for later public exposure:

- bounded request size and item count;
- explicit validation before orchestration;
- no arbitrary provider/store identifiers from clients;
- no raw provider payload passthrough;
- sanitized failures;
- no fixture fallback in production;
- no live retailer calls from ordinary CI;
- existing security/scanning gates remain required.

Rate limiting is not implemented in this slice, but the endpoint is designed so a gateway/application limiter can be added without changing domain APIs.

## 11. Scale, distribution, and monetization readiness

### 11.1 Horizontal scaling

The preview use case is stateless and side-effect free with respect to product persistence. API instances do not require affinity.

Future scaling may place load balancing, caching, request coalescing, queues, or background provider workers around the application/evidence ports without changing Shopping Core.

### 11.2 Acquisition scaling

The long-term acquisition path may evolve from request-time retrieval to:

```text
refresh jobs -> provider workers -> normalized observation/snapshot store -> cached comparison
```

The comparison application consumes normalized evidence and therefore does not care whether it was collected synchronously, from cache, or by background workers.

### 11.3 Multi-client distribution

OpenAPI is the public product contract. The Next.js app is one client.

Future iOS, Android, desktop, PWA, or partner/API clients must be able to use the same versioned product contract without accessing Java internals or provider-specific identifiers.

### 11.4 Multi-user readiness

M1 remains anonymous and transient.

Future persisted ownership can be introduced around, not inside, the current core:

```text
User / Household / Workspace -> ShoppingList / SavedComparison
```

The current stateless request does not embed session/global-user assumptions into domain objects.

### 11.5 Entitlements before billing

Future monetization must use an entitlement/capability boundary rather than `if (premium)` conditions spread across controllers and domain code.

Potential monetizable capabilities may include saved lists, automatic refresh, alerts, history, advanced comparison, household sharing, export, API access, or higher refresh frequency. This spec does not define tariffs.

### 11.6 Metering separate from billing

Future usage metering must emit product-safe events independent of payment-provider logic. Examples include `comparison_requested`, `retailer_refresh_used`, or `shopping_list_created`.

Usage events must not include exact addresses or raw shopping-list contents by default.

Billing provider choice remains infrastructure and may change without changing product/domain semantics.

### 11.7 Commercial retailer providers

Retailer identity remains separate from source-provider identity. A future official/partner provider, aggregator, or licensed commercial feed can replace or coexist with an existing acquisition path while preserving retailer/product contracts.

Production activation remains gated by explicit technical, legal/usage-rights, reliability, and operational acceptance.

## 12. Observability readiness

This slice does not add a full telemetry platform, but application boundaries must allow future metrics such as:

- comparison request latency;
- provider/path latency;
- timeout and failure rate;
- retailer `READY`/`UNCERTAIN`/`INCOMPLETE`/`UNAVAILABLE` distribution;
- matching ambiguity rate;
- package-quantity evidence gaps;
- cache hit rate;
- provider quota consumption;
- infrastructure cost per comparison.

Default telemetry must not include exact addresses, shopping-list contents, cookies, tokens, source payloads, or raw provider URLs.

## 13. Determinism and ranking

The endpoint returns retailer results in canonical registry order.

This slice does not choose a cheapest or recommended retailer. A later ranking policy may compare only structurally valid comparable quotes and must keep incomplete/uncertain semantics explicit.

Price, availability, freshness, acquisition mode, and SKU remain prohibited as hidden semantic tie-breakers for product matching.

## 14. Testing strategy

Implementation proceeds in independent RED -> GREEN checkpoints:

1. application request/value contract and bounds;
2. runtime evidence port plus fail-closed production/test separation;
3. end-to-end application orchestration using existing shopping/matching/basket/comparison layers;
4. item-level product-safe result projection and anti-leak tests;
5. REST integration behavior and sanitized validation failures;
6. OpenAPI plus generated TypeScript client;
7. web form/state unit tests;
8. deterministic API integration composition;
9. responsive Playwright critical journey on desktop and mobile;
10. architecture/security rules and full exact-head repository gate.

The browser acceptance fixture must visibly exercise mixed retailer states (`READY`, `UNCERTAIN`, `INCOMPLETE`, `UNAVAILABLE`) without making production-readiness claims.

## 15. Acceptance criteria

The slice is accepted only when all of the following hold:

- one public request can traverse HTTP -> shopping -> location -> evidence -> snapshots -> matching -> basket -> comparison -> HTTP response;
- all eight canonical retailers are present in stable order;
- production without valid runtime evidence never fabricates a quote;
- item-level reasons remain explicit and consistent with the retailer summary;
- no provider/source/store implementation IDs leak through API or web;
- locality-only comparison works without requiring a sensitive address;
- web users can submit, edit, add, and remove items with accessible controls;
- desktop and mobile browser tests exercise the full deterministic critical journey;
- API unavailable/timeout behavior is explicit and bounded;
- ordinary CI makes no live retailer requests;
- OpenAPI/generated client is synchronized;
- Maven, web unit/type/build, Playwright, CodeQL, Dependency Review, container security, release bundle/contract, and retailer bridge gates are all green on the exact merge candidate;
- final read-only review has no unresolved P0/P1/P2 findings.

## 16. Architectural rule for future work

> Do not build infrastructure for hypothetical scale prematurely, but no M1 decision may close the path to multi-user ownership, horizontal scaling, mobile clients, commercial retailer data providers, entitlement-based monetization, or provider-independent acquisition.

This rule is subordinate to privacy, truthful evidence, and fail-closed behavior: future commercial potential never justifies fabricating retailer data, weakening provenance, or bypassing production access/usage-rights gates.
