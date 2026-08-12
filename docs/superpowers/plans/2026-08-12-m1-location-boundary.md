# M1 Location / Fulfillment Context Boundary Implementation Plan

Status: **COMPLETE** — implemented in PR #75 on 2026-08-12.

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

### Task 1: Provider-neutral product location and sensitive address — COMPLETE

**Files:**
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/location/ProductLocationId.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/location/SensitiveAddress.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/location/ProductLocation.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/location/ProductLocationTest.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/location/LocationBoundaryArchitectureTest.java`

- [x] **Step 1: Write failing value/privacy tests**
- [x] **Step 2: Add architecture RED**
- [x] **Step 3: Run Maven verify and observe RED**
- [x] **Step 4: Implement minimal product location types**
- [x] **Step 5: Run full Maven verify and commit GREEN**

RED head `48df6d36994f42ff684e95d1d7bc9d2a46d20d87` failed at `testCompile` only because location production types did not exist. GREEN head `f840ebe8ddea0d93b60be0421388f5cb332da4a5` passed full Maven verification including the ArchUnit dependency rule.

Delivered behavior:

- `ProductLocationId(UUID value)` with non-null identity;
- locality-only or locality+address product locations;
- locality normalization and blank rejection;
- explicit `SensitiveAddress.reveal()`;
- default sensitive-address string representation `[REDACTED]`;
- `ProductLocation.toString()` never renders the precise address;
- production `location` package has no dependency on provider/retailer packages.

---

### Task 2: Typed fulfillment-context bindings — COMPLETE

**Files:**
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextSelectionMode.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextBinding.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextSet.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/FulfillmentContextSetTest.java`

- [x] **Step 1: Write failing binding tests**
- [x] **Step 2: Run Maven verify and observe RED**
- [x] **Step 3: Implement minimal immutable binding set**
- [x] **Step 4: Run Maven verify and commit GREEN**

RED head `bf4bcdf63c0d76cd0029f2fddc26874c79ffb367` failed at `testCompile` only because typed binding classes did not exist. GREEN head `4f9d08d1f7d902ef8fce3cf5829087b68f7de9a9` passed full Maven verification.

Delivered behavior:

- selection provenance `MANUAL` / `RESOLVED`;
- a binding stores only opaque `ProductLocationId` plus source-provider-scoped `LocationContext`;
- stable binding order;
- immutable binding snapshot;
- duplicate source-provider contexts rejected;
- binding to another product location rejected;
- unknown source provider returns no binding/context.

---

### Task 3: Route providers through typed fulfillment contexts — COMPLETE

**Files:**
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/ProviderPathOrchestrator.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/ProviderPathOrchestratorTest.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/ProviderPathLocationBoundaryTest.java`
- `docs/PROJECT_STATE.md`
- `docs/ROADMAP.md`
- `CHANGELOG.md`

- [x] **Step 1: Write failing typed-boundary test and migrate orchestration test calls**
- [x] **Step 2: Run Maven verify and observe RED**
- [x] **Step 3: Implement minimal orchestrator migration**
- [x] **Step 4: Run Maven verify**
- [x] **Step 5: Synchronize durable docs**
- [ ] **Step 6: Run final repository CI/security gate and review**

RED head `c2735cb11630f7b9da2d7f04bf983bd618c657d8` failed with seven expected compile errors because `ProviderPathOrchestrator` still required `Map<String, LocationContext>`. GREEN head `a58b0e3cf8baf29c8caa9da7ef5926e66a8d0fd9` replaced that raw map with `FulfillmentContextSet` and passed full Maven verification without changing priority, capability, fallback or provenance behavior.

Final repository-wide CI/security verification and exact-head review are the remaining shipping gate before squash merge.
