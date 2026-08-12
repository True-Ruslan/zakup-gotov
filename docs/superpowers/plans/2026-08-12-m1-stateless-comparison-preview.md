# M1 Stateless Comparison Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a stateless `POST /api/v1/comparison-previews` vertical journey from validated shopping input and locality through testable runtime evidence, matching, basket planning, comparison projection, OpenAPI/generated client, and responsive web UI without fabricating production retailer data.

**Architecture:** Add a thin `preview` application layer that owns request construction, evidence-port orchestration, and product-safe projection while reusing the existing shopping/location/provider/matching/basket/comparison modules. Production evidence is a strict no-op/fail-closed adapter; deterministic mixed retailer evidence exists only in test support. The public API remains versioned and provider-neutral so later persistence, accounts, entitlements, mobile clients, caches, commercial feeds, and horizontal scaling can wrap the use case without changing Shopping Core.

**Tech Stack:** Java 25, Spring Boot, Maven 3.9.16, JUnit 5, AssertJ, MockMvc, ArchUnit, OpenAPI 3.1, `openapi-typescript`, TypeScript 5, React 19, Next.js 16.3, Vitest, Testing Library, Playwright, pnpm workspace.

## Global Constraints

- Public endpoint: `POST /api/v1/comparison-previews`; operationId `createComparisonPreview`.
- Production evidence adapter in this slice is strict no-op/fail-closed and makes zero retailer HTTP/browser calls.
- Ordinary CI makes zero live retailer requests.
- Public request accepts locality only; no exact address or provider/store identifiers.
- Request bounds: locality 1..160 normalized Unicode characters, 1..100 items, requirement 1..240 normalized Unicode characters, client UUID item IDs, strictly positive decimal quantities.
- Supported input units: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, `LITER`; response quantities are canonicalized by existing `Quantity` semantics.
- Public responses never expose SKU, source provider ID, acquisition mode, source reference/URL, fulfillment-context ID, raw provider payloads, cookies, headers, or tokens.
- HTTP 400 validation failures use `application/problem+json` with code `INVALID_COMPARISON_PREVIEW` and deterministic public field paths.
- Retailer/source failure is retailer result data when the application remains healthy; it is not a whole-request 5xx.
- All eight canonical retailers remain visible in registry order.
- No cheapest/recommended retailer ranking in this slice.
- No fuzzy/semantic/LLM matching and no package-quantity inference from product names.
- Fixture evidence is test-only and must not be loadable by production composition/artifacts.
- New application/web boundaries must remain stateless and persistence-free.

---

### Task 1: Preview Request Domain Boundary

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewItemRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewRequestFactory.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewInput.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewRequestFactoryTest.java`

**Interfaces:**
- Consumes: existing `ShoppingList`, `ShoppingListId`, `ShoppingItem`, `ShoppingItemId`, `ShoppingRequirement`, `Quantity`, `QuantityUnit`, `ProductLocation`, `ProductLocationId`.
- Produces: `ComparisonPreviewRequestFactory.create(ComparisonPreviewRequest)` returning immutable `ComparisonPreviewInput(ShoppingList shoppingList, ProductLocation productLocation)`.

- [ ] **Step 1: Write the failing request-boundary tests**

Cover exact behaviors:

```java
var request = new ComparisonPreviewRequest(
        "  Москва  ",
        List.of(new ComparisonPreviewItemRequest(
                UUID.fromString("c281d71c-2b27-46ef-a7af-3d624a7447cf"),
                "  Молоко 3,2%  ",
                new BigDecimal("2"),
                QuantityUnit.LITER)));

var input = ComparisonPreviewRequestFactory.create(request);
assertThat(input.productLocation().locality()).isEqualTo("Москва");
assertThat(input.shoppingList().items()).hasSize(1);
assertThat(input.shoppingList().items().getFirst().quantity())
        .isEqualTo(Quantity.of(new BigDecimal("2000"), QuantityUnit.MILLILITER));
```

Also assert rejection of locality length 0/161, item count 0/101, duplicate item UUIDs, requirement length 0/241, null unit, zero/negative amount, and ensure raw address/provider fields do not exist on the request records.

- [ ] **Step 2: Run RED**

Run from `apps/api`:

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=ComparisonPreviewRequestFactoryTest test
```

Expected: `testCompile` failure because preview request/factory types do not exist.

- [ ] **Step 3: Implement the minimal boundary**

Use immutable records for transport-neutral request values and a factory that:

```java
var list = new ShoppingList(new ShoppingListId(UUID.randomUUID()));
var location = ProductLocation.localityOnly(new ProductLocationId(UUID.randomUUID()), request.locality());
```

Validate public length/item-count bounds before constructing existing domain types. Preserve client item UUIDs as `ShoppingItemId`. Do not add persistence or Spring annotations in this task.

- [ ] **Step 4: Run GREEN and full API verify**

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=ComparisonPreviewRequestFactoryTest test
./mvnw --batch-mode --no-transfer-progress verify
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewRequestFactoryTest.java
git commit -m "feat(preview): add stateless comparison request boundary"
```

---

### Task 2: Runtime Evidence Port and Fail-Closed Production Adapter

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonRuntimeEvidenceSource.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/RetailerRuntimeEvidence.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonRuntimeEvidence.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/NoopComparisonRuntimeEvidenceSource.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonRuntimeEvidenceBoundaryTest.java`

**Interfaces:**
- Consumes: `ShoppingList`, `ProductLocation`, `RetailerId`, `ProviderSearchOutcome`, `OfferSnapshot`, `PackageQuantitySet`.
- Produces: `ComparisonRuntimeEvidenceSource.load(ShoppingList, ProductLocation)` returning one immutable `ComparisonRuntimeEvidence`; production `NoopComparisonRuntimeEvidenceSource` always returns empty retailer evidence.

- [ ] **Step 1: Write RED tests for evidence separation**

Assert:

```java
var evidence = new NoopComparisonRuntimeEvidenceSource().load(shoppingList, location);
assertThat(evidence.retailers()).isEmpty();
```

Also assert duplicate retailer evidence is rejected, evidence retailer IDs agree with provider outcome/snapshots, and the no-op class has no dependencies on `java.net.http`, browser bridge packages, fixture provider types, or test-support packages.

- [ ] **Step 2: Run RED**

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=ComparisonRuntimeEvidenceBoundaryTest test
```

Expected: compile failure because evidence types do not exist.

- [ ] **Step 3: Implement immutable evidence types and no-op adapter**

Model successful runtime evidence as the already normalized components required later:

```java
public record RetailerRuntimeEvidence(
        RetailerId retailerId,
        ProviderSearchOutcome providerOutcome,
        List<OfferSnapshot> snapshots,
        PackageQuantitySet packageQuantities) { ... }
```

`ComparisonRuntimeEvidence` stores a defensive ordered map/list and rejects duplicate retailer IDs. `NoopComparisonRuntimeEvidenceSource` returns `ComparisonRuntimeEvidence.empty()` and performs no I/O.

- [ ] **Step 4: Verify GREEN**

```bash
./mvnw --batch-mode --no-transfer-progress verify
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonRuntimeEvidenceBoundaryTest.java
git commit -m "feat(preview): add fail-closed runtime evidence port"
```

---

### Task 3: End-to-End Application Orchestration and Product-Safe Projection

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewService.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreview.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewRetailer.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewItem.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewSelection.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewServiceTest.java`

**Interfaces:**
- Consumes: Task 1 input, Task 2 evidence source, `RetailerRegistry.initial()`, `DeterministicProductMatcher`, `SingleStoreBasketPlanner`, `RetailerComparisonReadModelAssembler`.
- Produces: `ComparisonPreviewService.create(ComparisonPreviewRequest)` returning product-safe `ComparisonPreview` with request context, all eight retailers, summary states and item-level details.

- [ ] **Step 1: Write RED orchestration tests with a test-local fake evidence source**

Construct controlled evidence for four retailers and assert in one service call:

```java
assertThat(preview.retailers()).extracting(ComparisonPreviewRetailer::id)
        .containsExactly("pyaterochka", "perekrestok", "chizhik", "magnit", "lenta", "vkusvill", "ozon-fresh", "samokat");
assertThat(preview.require("pyaterochka").comparisonStatus()).isEqualTo(READY);
assertThat(preview.require("perekrestok").comparisonStatus()).isEqualTo(UNCERTAIN);
assertThat(preview.require("magnit").comparisonStatus()).isEqualTo(INCOMPLETE);
assertThat(preview.require("samokat").comparisonStatus()).isEqualTo(UNAVAILABLE);
```

Exercise unmatched, ambiguous, package-quantity-unknown and unit-mismatch rows. Assert item order equals shopping-list order. Assert `READY/UNCERTAIN` totals/freshness match the same basket quote used for item details.

- [ ] **Step 2: Run RED**

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=ComparisonPreviewServiceTest test
```

Expected: compile failure because service/projection types do not exist.

- [ ] **Step 3: Implement orchestration without duplicating domain rules**

For each retailer runtime evidence:

1. use its `ProviderSearchOutcome`;
2. convert trusted observations to already available `OfferSnapshot` values supplied by the evidence port;
3. derive `MatchScope` from retailer plus the single fulfillment context represented by candidate snapshots;
4. invoke `SingleStoreBasketPlanner.quote(...)`;
5. create `RetailerComparisonEvidence`;
6. assemble all retailer summaries through `RetailerComparisonReadModelAssembler`.

Project item-level selections from the same `SingleStoreBasketQuote`. Bound ambiguous public names to the first 10 candidate display names while preserving `AMBIGUOUS` status.

- [ ] **Step 4: Add anti-leak reflection/serialization-safe tests**

Ensure public preview records/classes expose no accessor/field containing `sku`, `sourceProvider`, `acquisitionMode`, `sourceReference`, `fulfillmentContext`, `cookie`, `token`, or raw URL/payload values.

- [ ] **Step 5: Run GREEN**

```bash
./mvnw --batch-mode --no-transfer-progress verify
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewServiceTest.java
git commit -m "feat(preview): orchestrate stateless comparison previews"
```

---

### Task 4: REST Endpoint and Stable Problem Details

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewController.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/InvalidComparisonPreviewProblem.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewExceptionHandler.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewControllerTest.java`

**Interfaces:**
- Consumes: `ComparisonPreviewService` with production `NoopComparisonRuntimeEvidenceSource`.
- Produces: `POST /api/v1/comparison-previews` JSON success and deterministic `application/problem+json` HTTP 400 failures.

- [ ] **Step 1: Write MockMvc RED for production behavior**

Success request must return HTTP 200, canonicalized quantities, all eight retailers and no fabricated totals. Assert raw JSON does not contain forbidden provider fields.

Because production evidence is no-op, connected/access-ready retailers with no runtime data must surface existing `DATA_NOT_AVAILABLE` behavior rather than fixture prices.

- [ ] **Step 2: Write validation RED**

For zero quantity and malformed JSON assert exactly:

```json
{
  "type": "https://zakup-gotov.dev/problems/invalid-comparison-preview",
  "title": "Invalid comparison preview request",
  "status": 400,
  "code": "INVALID_COMPARISON_PREVIEW",
  "errors": [{"field":"items[0].quantity.amount","message":"must be greater than 0"}]
}
```

Malformed JSON uses `$request`. Assert `Content-Type` is `application/problem+json` and payload excludes class names/stack traces/provider details.

- [ ] **Step 3: Run RED**

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=ComparisonPreviewControllerTest test
```

Expected: 404/compile failure before controller exists.

- [ ] **Step 4: Implement controller and exception mapping**

Keep DTO mapping inside the preview boundary. Inject/construct the no-op production evidence source explicitly; do not expose a fixture Spring bean/profile.

- [ ] **Step 5: Run GREEN**

```bash
./mvnw --batch-mode --no-transfer-progress verify
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/preview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewControllerTest.java
git commit -m "feat(api): expose stateless comparison preview endpoint"
```

---

### Task 5: OpenAPI and Generated TypeScript Client

**Files:**
- Modify: `openapi/zakup-gotov.yaml`
- Modify: `packages/api-client/src/index.ts`
- Modify: `packages/api-client/src/index.test.ts`
- Regenerate: `packages/api-client/src/schema.d.ts`

**Interfaces:**
- Produces OpenAPI operation `createComparisonPreview` and export `COMPARISON_PREVIEWS_PATH = "/api/v1/comparison-previews"`.

- [ ] **Step 1: Write client RED**

Add to `packages/api-client/src/index.test.ts`:

```ts
expect(COMPARISON_PREVIEWS_PATH).toBe("/api/v1/comparison-previews");
```

and a compile-time/use-site assertion that generated `paths["/api/v1/comparison-previews"]["post"]` accepts the request and returns the response/problem schemas.

- [ ] **Step 2: Run RED**

From repo root run the existing contract/client checks. Expected failure: path/schema export is absent.

- [ ] **Step 3: Extend OpenAPI**

Define explicit schemas for request, normalized quantity, preview response, retailer summary, item result/selection, and `InvalidComparisonPreviewProblem`. Keep `additionalProperties: false`; preserve existing retailer summary enums rather than duplicating alternate vocabularies.

- [ ] **Step 4: Regenerate schema**

Use the repository's existing generated-client command/check so `schema.d.ts` is produced by pinned `openapi-typescript`, not handwritten.

- [ ] **Step 5: Run contract GREEN**

Expected: generated-check, typecheck, tests and build all PASS.

- [ ] **Step 6: Commit**

```bash
git add openapi/zakup-gotov.yaml packages/api-client/src
git commit -m "feat(contract): add comparison preview API"
```

---

### Task 6: Accessible Web Form and Product State

**Files:**
- Create: `apps/web/src/app/comparison-preview.ts`
- Create: `apps/web/src/app/comparison-preview-form.tsx`
- Create: `apps/web/src/app/comparison-preview-results.tsx`
- Create: `apps/web/src/app/comparison-preview.test.ts`
- Create: `apps/web/src/app/comparison-preview-form.test.tsx`
- Create: `apps/web/src/app/comparison-preview-results.test.tsx`
- Modify: `apps/web/src/app/page.tsx`
- Modify: `apps/web/src/app/page.test.tsx`

**Interfaces:**
- Consumes generated API client only.
- Produces a client-side form with locality and repeatable item rows plus explicit loading/success/error states.

- [ ] **Step 1: Write pure transport/state RED**

Test `createComparisonPreview(request, fetchImpl?)` with 3-second AbortController behavior analogous to the existing retailer-readiness loader. Assert 400 problem responses map to field-safe form errors; timeout/network failures map to one generic accessible service error without fabricated data.

- [ ] **Step 2: Write form RED**

Using Testing Library assert:

- initial row exists;
- add row creates a new UUID-backed row;
- remove row is disabled when only one row exists;
- locality/requirement/amount/unit labels are associated with controls;
- submit calls the transport with current rows;
- invalid amount/locality is presented accessibly;
- keyboard interaction works without pointer-only handlers.

- [ ] **Step 3: Write results RED**

Render a deterministic response with mixed retailer states and assert all eight cards stay visible, total only appears where supplied, item-level reasons display, freshness basis is explicit, and no cheapest-winner copy exists.

- [ ] **Step 4: Implement minimal components**

Keep transport in `comparison-preview.ts`, mutable form state in `comparison-preview-form.tsx`, and read-only output in `comparison-preview-results.tsx`. Do not put generated client/provider details into UI components.

- [ ] **Step 5: Integrate `page.tsx`**

Retain current M1 positioning and retailer truthfulness copy, but make the comparison form the primary action. The previous readiness-only surface may remain as supporting context only if it does not duplicate the result cards.

- [ ] **Step 6: Run web GREEN**

From `apps/web`:

```bash
pnpm lint
pnpm typecheck
pnpm test
pnpm build
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add apps/web/src/app
git commit -m "feat(web): add comparison preview journey"
```

---

### Task 7: Deterministic End-to-End Test Composition and Browser Acceptance

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/DeterministicComparisonRuntimeEvidenceSource.java`
- Create/Modify test configuration under `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/` as needed without adding production fixture beans.
- Create/Modify: existing Playwright spec under `apps/web/e2e/` (discover exact current filename before edit).
- Modify test-only startup scripts/config only if required for API + web acceptance.

**Interfaces:**
- Test composition only; production artifact behavior remains no-op evidence.

- [ ] **Step 1: Add API integration RED using deterministic evidence**

Boot Spring test context with an explicitly test-scoped evidence source and POST the same request used by browser tests. Assert response visibly contains at least one `READY`, one `UNCERTAIN`, one `INCOMPLETE`, and one `UNAVAILABLE` retailer plus item-level unmatched/ambiguous/package-gap/unit-mismatch scenarios.

- [ ] **Step 2: Implement test-only evidence source**

Use deterministic synthetic/recorded values only. Do not call the network. Ensure the class lives under test sources and cannot be loaded from the production jar.

- [ ] **Step 3: Add Playwright RED**

On desktop and mobile:

1. enter locality;
2. fill first item;
3. add a second item;
4. choose quantity/unit;
5. submit;
6. assert all eight retailers render;
7. assert mixed states and at least one item-level reason;
8. assert no provider IDs/SKUs/source URLs appear;
9. assert no horizontal overflow;
10. preserve existing focus/accessibility checks.

Also keep an API-unavailable browser scenario with one accessible alert and no fabricated result cards.

- [ ] **Step 4: Run full deterministic acceptance GREEN**

Run API verify, web unit/type/build, then Playwright against deterministic test composition. Expected: PASS with zero live retailer requests.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/test apps/web/e2e
git commit -m "test(e2e): cover comparison preview critical journey"
```

---

### Task 8: Architecture Guards, Durable Docs, Review and Shipping

**Files:**
- Create/Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/preview/ComparisonPreviewArchitectureTest.java`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`
- Modify: `docs/superpowers/plans/2026-08-12-m1-stateless-comparison-preview.md`

**Interfaces:**
- Architecture rule: upstream `shopping`, `location`, `provider`, `matching`, `basket`, `comparison`, `retailer` packages must not depend on `preview`; production preview code must not depend on fixture/test packages.

- [ ] **Step 1: Add architecture tests**

Use ArchUnit to assert dependency direction and absence of test/fixture production dependencies.

- [ ] **Step 2: Run full API/web verification**

API:

```bash
cd apps/api
./mvnw --batch-mode --no-transfer-progress verify
```

Web:

```bash
cd apps/web
pnpm lint
pnpm typecheck
pnpm test
pnpm build
```

Then run repository Playwright/contract checks through existing CI-equivalent commands.

- [ ] **Step 3: Synchronize durable docs**

Record exact RED/GREEN commit SHAs, explicitly state that production preview evidence remains no-op/fail-closed, mark the critical journey implemented but not production retailer acquisition, and set the next roadmap focus without overstating readiness.

- [ ] **Step 4: Open/update draft PR and run exact-head repository gate**

Required groups: API, Contract, Web + responsive E2E, Retailer Bridge, Dependency Review, CodeQL Java + JS/TS, Container Security API + Web, Release Bundle, Release Contract.

No bypass, no stale-head merge.

- [ ] **Step 5: Read-only Change Review**

Review the exact candidate for P0/P1/P2 issues, with special attention to fixture leakage, provider-field leakage, validation sanitization, impossible public states, timeout behavior, request amplification, and architecture direction.

- [ ] **Step 6: Record one docs-only shipping marker**

Only after the code/docs candidate has all required gates GREEN and review has no unresolved P0/P1/P2. Marker records evidence in this plan and changes no runtime code.

- [ ] **Step 7: Re-run the full branch-protection gate on marker head**

All required groups must be GREEN again on the exact marker SHA.

- [ ] **Step 8: Squash merge with exact-head guard and verify post-merge `main`**

Merge only the marker head, then verify the push workflows on the resulting main merge SHA.
