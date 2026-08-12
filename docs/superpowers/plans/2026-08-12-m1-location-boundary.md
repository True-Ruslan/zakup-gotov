# M1 Location / Fulfillment Context Boundary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Separate privacy-sensitive provider-neutral product location from provider-specific fulfillment contexts, then route fixture providers through typed bindings instead of raw provider-ID maps.

**Architecture:** Introduce a `location` domain package that owns product location identity and a redacted sensitive-address value object without any dependency on retailer/provider packages. The provider package owns bindings from an opaque `ProductLocationId` to source-provider-scoped `LocationContext` values and records whether a context was manually selected or resolved. `ProviderPathOrchestrator` consumes a typed `FulfillmentContextSet`, never a precise address or raw `Map<String, LocationContext>`.

**Tech Stack:** Java 25, JUnit 5, AssertJ, ArchUnit/Spring Modulith verification, Maven 3.9.16.

## Global Constraints

- Precise user addresses are sensitive user data and must be redacted from default string/log representations.
- `location` domain types must not depend on `provider` or `retailer` packages.
- Provider-specific identifiers such as Magnit `shopCode` and X5 store IDs must remain inside provider-scoped fulfillment contexts.
- Ordinary CI must remain fully deterministic and make no live retailer requests.
- Automatic location resolution is not claimed by this slice; manual and already-resolved provider contexts are both representable.
- TDD is mandatory: each behavior starts RED, then minimal GREEN, then full Maven verification.

---

### Task 1: Provider-neutral product location and sensitive address

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/location/ProductLocationId.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/location/SensitiveAddress.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/location/ProductLocation.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/location/ProductLocationTest.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/location/LocationBoundaryArchitectureTest.java`

**Interfaces:**
- Produces: `ProductLocationId(UUID value)`.
- Produces: `SensitiveAddress.of(String raw)`, `String reveal()`, redacted `toString()`.
- Produces: `ProductLocation.localityOnly(ProductLocationId id, String locality)`.
- Produces: `ProductLocation.withAddress(ProductLocationId id, String locality, String address)`.
- Produces: `id()`, `locality()`, `Optional<SensitiveAddress> address()`.

- [ ] **Step 1: Write failing value/privacy tests**

```java
@Test
void keepsPreciseAddressRedactedByDefault() {
    var location = ProductLocation.withAddress(ID, " Москва ", " ул. Тестовая, 10 ");
    assertThat(location.locality()).isEqualTo("Москва");
    assertThat(location.address()).get().extracting(SensitiveAddress::reveal).isEqualTo("ул. Тестовая, 10");
    assertThat(location.address().orElseThrow().toString()).isEqualTo("[REDACTED]");
    assertThat(location.toString()).doesNotContain("Тестовая", "10");
}
```

Also require locality-only locations, blank locality/address rejection, non-null ID, immutable equality semantics for IDs, and no raw provider identifiers on the product location API.

- [ ] **Step 2: Add architecture RED**

Use ArchUnit to require that `..location..` classes do not depend on `..provider..` or `..retailer..` classes.

- [ ] **Step 3: Run `./mvnw --batch-mode --no-transfer-progress verify` in `apps/api`**

Expected: RED because the `location` package/types do not exist.

- [ ] **Step 4: Implement minimal product location types**

`SensitiveAddress` stores trimmed raw text, exposes it only through explicit `reveal()`, implements value equality/hashCode, and always returns `[REDACTED]` from `toString()`. `ProductLocation` validates/normalizes locality, stores optional sensitive address, and uses a custom redacted `toString()`.

- [ ] **Step 5: Run full Maven verify and commit GREEN**

Expected: all API tests and architecture checks pass.

---

### Task 2: Typed fulfillment-context bindings

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextSelectionMode.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextBinding.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextSet.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextSetTest.java`

**Interfaces:**
- Produces enum: `MANUAL`, `RESOLVED`.
- Produces record: `FulfillmentContextBinding(ProductLocationId productLocationId, LocationContext context, FulfillmentContextSelectionMode mode)`.
- Produces: `FulfillmentContextSet.of(ProductLocationId productLocationId, List<FulfillmentContextBinding> bindings)`.
- Produces: `productLocationId()`, `bindings()`, `Optional<FulfillmentContextBinding> bindingFor(String sourceProviderId)`, `Optional<LocationContext> contextFor(String sourceProviderId)`.

- [ ] **Step 1: Write failing binding tests**

Require manual and resolved contexts to coexist, preserve stable input order, reject bindings for a different `ProductLocationId`, reject duplicate `sourceProviderId`, return immutable bindings, and return empty for unknown source providers.

- [ ] **Step 2: Run Maven verify**

Expected: RED because binding types do not exist.

- [ ] **Step 3: Implement minimal immutable binding set**

Use an insertion-preserving map keyed by `LocationContext.sourceProviderId()`. The set stores only opaque `ProductLocationId` plus provider contexts; it never stores `ProductLocation` or `SensitiveAddress`.

- [ ] **Step 4: Run Maven verify and commit GREEN**

Expected: all tests pass.

---

### Task 3: Route providers through typed fulfillment contexts

**Files:**
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/ProviderPathOrchestrator.java`
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/ProviderPathOrchestratorTest.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/ProviderPathLocationBoundaryTest.java`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Replaces the orchestration parameter `Map<String, LocationContext> contextsBySourceProvider` with `FulfillmentContextSet fulfillmentContexts`.
- Retains all existing path ordering, capability checks, fallback behavior and trust validation unchanged.

- [ ] **Step 1: Write failing typed-boundary test and migrate orchestration test calls**

Require the orchestrator to accept `FulfillmentContextSet`, preserve `MISSING_CONTEXT` behavior for absent bindings, and expose no method that accepts a raw provider-context map.

- [ ] **Step 2: Run Maven verify**

Expected: RED because the orchestrator still requires the raw `Map<String, LocationContext>` signature.

- [ ] **Step 3: Implement minimal orchestrator migration**

Replace direct map lookup with `fulfillmentContexts.contextFor(provider.sourceProviderId())`; do not change priority, fallback or offer validation.

- [ ] **Step 4: Run Maven verify**

Expected: all provider/orchestration tests remain green.

- [ ] **Step 5: Synchronize durable docs**

Mark M1 slice 4 complete, make price/availability snapshots the next active slice, record privacy/address redaction and typed fulfillment binding behavior in `CHANGELOG.md`.

- [ ] **Step 6: Run final repository CI/security gate and review**

Require API, Contract, Web/E2E, CodeQL Java+JS/TS, Dependency Review, Retailer Bridge, Container Security API+Web, Release Bundle and Release Contract to pass on the exact final head before squash merge.
