# M1 Product Comparison Read Model Design

Status: **APPROVED FOR IMPLEMENTATION** — roadmap-aligned design, 2026-08-12.

## Goal

Define the first product-facing failure / coverage / freshness contract between the completed M1 domain core, the REST API and the web application.

The contract must let a user understand which retailers are visible, which can currently participate in comparison, why a retailer cannot produce a confirmed basket, and what freshness evidence exists — without exposing provider implementation details or inventing stock/freshness certainty.

## Audience and success criteria

Primary user: a shopper who wants to understand whether Zakup Gotov can compare their basket across the canonical retailer registry and why a retailer/result is unavailable or uncertain.

Success means:

- all canonical retailers remain visible in deterministic registry order;
- technical coverage and production-access readiness are not conflated;
- complete, uncertain, incomplete and unavailable comparison states are explicit;
- incomplete/unavailable results cannot expose a misleading basket total;
- freshness reports only timestamps actually carried by snapshots;
- no provider IDs, acquisition modes, source references, precise addresses, cookies/tokens or other transport details cross the product/API boundary;
- the OpenAPI contract is generated into the shared TypeScript client;
- the web renders success and service-unavailable states without fake retailer data.

## Selected product contract

### Retailer coverage presentation

Internal `RetailerCoverageState` is mapped to a stable product-facing `RetailerCoverageStatus`:

- `CONNECTED` — any accepted `AVAILABLE_*` technical path;
- `DISCOVERY` — `REQUIRED_UNIMPLEMENTED` or `DISCOVERY`;
- `DEGRADED` — `DEGRADED`;
- `BLOCKED` — `BLOCKED_EXTERNAL`.

The public API does not expose acquisition mode or source-provider identity.

### Production-access presentation

Internal `ProductionAccessStatus` maps to:

- `READY` — `ACCEPTABLE`;
- `PENDING` — `NOT_ASSESSED` or `UNRESOLVED`;
- `BLOCKED` — `BLOCKED`.

Technical connectivity must never imply production readiness.

### Comparison status

`RetailerComparisonStatus`:

- `READY` — a complete basket quote exists;
- `UNCERTAIN` — every line has a priced selection, but at least one selected offer has `UNKNOWN` availability;
- `INCOMPLETE` — a comparison was attempted but one or more shopping requirements cannot produce a complete selection;
- `UNAVAILABLE` — the retailer cannot currently participate in a product comparison because coverage/access/runtime evidence is unavailable.

The product layer must never rank or relabel `UNCERTAIN`, `INCOMPLETE` or `UNAVAILABLE` as a confirmed complete result.

## Product-safe reason codes

`RetailerComparisonReason` is intentionally finite and user/product oriented:

- `COVERAGE_DISCOVERY`
- `COVERAGE_DEGRADED`
- `COVERAGE_BLOCKED`
- `PRODUCTION_ACCESS_PENDING`
- `PRODUCTION_ACCESS_BLOCKED`
- `DATA_NOT_AVAILABLE`
- `SOURCE_UNAVAILABLE`
- `ITEM_UNMATCHED`
- `ITEM_AMBIGUOUS`
- `ITEM_UNAVAILABLE`
- `PACKAGE_QUANTITY_UNKNOWN`
- `QUANTITY_UNIT_MISMATCH`
- `AVAILABILITY_UNKNOWN`

Provider attempt details may influence these reasons, but source-provider IDs and acquisition modes are not returned to the user-facing contract.

## Decision order

For each canonical retailer:

1. map technical coverage;
2. if coverage is not `CONNECTED`, comparison is `UNAVAILABLE` with the corresponding coverage reason;
3. map production access;
4. if production access is `PENDING` or `BLOCKED`, comparison is `UNAVAILABLE` with the corresponding access reason;
5. if both gates are ready but no runtime comparison evidence exists, comparison is `UNAVAILABLE / DATA_NOT_AVAILABLE`;
6. if provider search evidence exists but no provider path succeeded, comparison is `UNAVAILABLE / SOURCE_UNAVAILABLE`;
7. otherwise a basket quote is required and maps to `READY`, `UNCERTAIN` or `INCOMPLETE`.

This ordering prevents technically reachable but legally/operationally unresolved data paths from appearing product-ready.

## Basket mapping

For `COMPLETE` basket quotes:

- comparison status `READY`;
- total required;
- no failure reasons;
- freshness summary required.

For `UNCERTAIN` basket quotes:

- comparison status `UNCERTAIN`;
- total required because every line has a concrete package selection;
- reason includes `AVAILABILITY_UNKNOWN`;
- freshness summary required.

For `INCOMPLETE` basket quotes:

- comparison status `INCOMPLETE`;
- no aggregate total;
- no basket-level freshness summary because only a subset of lines may be selected;
- reasons are the stable, deduplicated union of incomplete item statuses in shopping-list order.

Mapping from basket item status:

- `UNMATCHED` → `ITEM_UNMATCHED`
- `AMBIGUOUS` → `ITEM_AMBIGUOUS`
- `UNAVAILABLE` → `ITEM_UNAVAILABLE`
- `PACKAGE_QUANTITY_UNKNOWN` → `PACKAGE_QUANTITY_UNKNOWN`
- `QUANTITY_UNIT_MISMATCH` → `QUANTITY_UNIT_MISMATCH`
- `AVAILABILITY_UNKNOWN` → `AVAILABILITY_UNKNOWN`
- `FULFILLED` produces no reason.

## Freshness semantics

The public `RetailerFreshness` model contains:

- `basis`: `OBSERVATION_ONLY` or `PROVIDER_TIMESTAMP`;
- `observedAt`: the **oldest** selected offer observation timestamp in the complete/uncertain basket;
- optional `providerUpdatedAt`: the **oldest** selected provider timestamp, present only when every selected snapshot has provider-side update evidence.

Rules:

- if any selected snapshot is observation-only, aggregate basis is `OBSERVATION_ONLY` and `providerUpdatedAt` is absent;
- only when every selected snapshot has trusted provider update time may aggregate basis be `PROVIDER_TIMESTAMP`;
- no `fresh`, `stale`, age bucket or TTL is invented in this slice because no retailer-specific staleness policy has been proven;
- incomplete/unavailable results have no aggregate freshness summary.

Using the oldest selected timestamp is conservative: the basket is not presented as newer than its least-recent selected line.

## Read-model architecture

New package: `io.github.trueruslan.zakupgotov.comparison`.

Core types:

- `RetailerCoverageStatus`
- `RetailerProductionAccessStatus`
- `RetailerComparisonStatus`
- `RetailerComparisonReason`
- `RetailerFreshness`
- `RetailerComparisonEvidence`
- `RetailerComparisonView`
- `RetailerComparisonCatalog`
- `RetailerComparisonReadModelAssembler`

Dependency direction:

`comparison -> retailer + provider + basket`

Upstream production packages (`retailer`, `provider`, `shopping`, `matching`, `basket`, `location`) must not depend back on `comparison`.

`RetailerComparisonEvidence` binds one successful/failed `ProviderSearchOutcome` and optional `SingleStoreBasketQuote` to one retailer. Cross-retailer evidence fails closed.

The assembler always iterates canonical registry entries and therefore cannot silently omit unsupported/discovery retailers.

## REST / OpenAPI contract

Add:

`GET /api/v1/retailers`

Response:

```json
{
  "retailers": [
    {
      "id": "pyaterochka",
      "displayName": "Пятёрочка",
      "coverage": "CONNECTED",
      "productionAccess": "PENDING",
      "comparisonStatus": "UNAVAILABLE",
      "reasons": ["PRODUCTION_ACCESS_PENDING"]
    }
  ]
}
```

Optional fields:

- `total` only for `READY` / `UNCERTAIN`;
- `freshness` only for `READY` / `UNCERTAIN`.

The initial controller is fixture/runtime-neutral: it exposes the canonical registry through the assembler with no fabricated provider or basket evidence. As of this design, that means the API honestly reports technical progress while production-access/readiness remains pending.

OpenAPI is the source of truth for the shared TypeScript client. `packages/api-client/src/schema.d.ts` must be regenerated and checked by existing contract CI.

## Web experience

The home page advances from the stale M0 placeholder to an M1 status surface.

### Happy state

- product heading remains `Закуп готов`;
- phase copy states M1 shopping core rather than M0 discovery;
- section `Покрытие магазинов` renders every API retailer in returned order;
- each retailer card shows display name and concise coverage/access status;
- no card shows price unless the API provides a complete/uncertain comparison total;
- uncertainty/incomplete/unavailable reasons are rendered as plain product language, not provider error strings.

### Loading/pending

The route is server-rendered. A lightweight `loading.tsx` may expose `aria-busy=true` while Next.js waits; no fabricated retailer cards appear during loading.

### API failure

If the API request fails, the page itself remains usable and displays an alert equivalent to:

`Не удалось загрузить статус магазинов. Основной сервис временно недоступен.`

No cached/hard-coded retailer readiness is substituted as if current.

### Empty/invalid payload

An empty retailer list is treated as unavailable product status, not as “zero supported retailers”; universal registry coverage means a successful response should contain canonical retailers.

### Accessibility / responsive behavior

- one H1;
- retailer list uses semantic list markup;
- statuses are readable text, not color-only;
- error state uses an accessible alert;
- desktop and 390 px mobile layouts must not overflow.

## API loading boundary

The web reads `API_BASE_URL` on the server and uses the generated API client contract/path. The page is dynamic so production build does not require a live API.

In release compose, `API_BASE_URL=http://api:8080` already exists. Web CI/E2E may run without API; that environment must exercise the explicit service-unavailable state instead of crashing.

## Alternatives considered

### Expose internal retailer/provider enums directly

Rejected. It would couple the UI/API to acquisition strategy and make future transport changes a product-contract breaking change.

### Hide retailers until they are ready

Rejected. It violates Universal Retailer Connectivity and creates false coverage optimism.

### Compute `fresh/stale` from one global age threshold

Rejected. No source-specific freshness policy has been proven; timestamps are evidence, thresholds are policy.

### Parse provider failure messages into UI copy

Rejected. Internal errors can leak implementation/security details and are not stable product semantics.

### Keep the web static until live basket comparison exists

Rejected. The current M0 placeholder is already stale; exposing honest coverage/readiness is useful and gives browser E2E a real product contract to exercise.

## Non-goals

This slice does not add:

- live retailer requests;
- persistence;
- authentication/accounts;
- shopping-list entry UI;
- location selection UI;
- actual multi-retailer ranking;
- fuzzy/AI matching;
- package-size extraction from product names;
- retailer-specific stale thresholds;
- delivery/minimum-order/loyalty pricing;
- production activation for unresolved access paths.

## Acceptance criteria

- canonical retailers always appear in stable registry order;
- coverage and production access map independently;
- access/coverage failures take precedence over runtime quote evidence;
- no-runtime-data and source-unavailable states are distinct;
- complete/uncertain/incomplete basket semantics map without weakening domain invariants;
- incomplete/unavailable results have no total or aggregate freshness;
- freshness aggregation is conservative and never fabricates provider timestamps;
- public read model contains no provider IDs/acquisition modes/source references/precise addresses;
- architecture test protects upstream dependency direction;
- `GET /api/v1/retailers` is covered by API tests and OpenAPI;
- generated TypeScript contract is synchronized;
- web success/failure components are unit-tested;
- responsive browser E2E proves the explicit API-unavailable state in ordinary web CI;
- full exact-head repository CI/security gate and read-only Change Review pass before merge.
