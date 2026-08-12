# M1 pre-acquisition production-access gate design

Date: 2026-08-13
Issue: #90

## Problem

`ComparisonPreviewService` currently calls `ComparisonRuntimeEvidenceSource.load(shoppingList, productLocation)` before it filters returned retailer evidence through `RetailerRegistryEntry.isProductionReady()`.

That is safe with today's production `NoopComparisonRuntimeEvidenceSource`, but the safety property is accidental: a future live source could issue a network request for a retailer whose technical path is connected while production access is `BLOCKED` or pending, and only then have its evidence discarded by the service.

M1 must make the access boundary structural before a GO to M2.

## Decision

Move production-access scoping **before acquisition**.

`ComparisonRuntimeEvidenceSource` receives an immutable `Set<RetailerId> requestedRetailers` in addition to shopping list and product location.

`ComparisonPreviewService` computes this set exclusively from registry entries where `RetailerRegistryEntry.isProductionReady()` is true. That already requires both:

- technically available coverage; and
- `ProductionAccessStatus.ACCEPTABLE`.

### Empty scope

If the requested retailer set is empty, `ComparisonPreviewService` must not invoke the evidence source at all and instead use empty runtime evidence.

This is important for the current production registry: Pyaterochka/Perekrestok access is not assessed, Magnit is `BLOCKED`, and discovery retailers are not technically available. Production preview therefore cannot accidentally trigger retailer acquisition.

### Non-empty scope

A source may return evidence only for requested retailer IDs.

The service keeps a defensive post-load boundary: evidence for an unrequested retailer is a contract violation and must fail closed rather than be silently accepted or projected.

### Test-only deterministic source

The deterministic acceptance source must honor `requestedRetailers` and construct evidence only for requested IDs. The existing all-production-ready deterministic integration registry requests all eight retailers, preserving its current mixed-state coverage.

## Invariants

1. Technical connectivity never grants acquisition permission by itself.
2. `BLOCKED`, `UNRESOLVED` and `NOT_ASSESSED` retailers are excluded before evidence loading.
3. Discovery/degraded/externally blocked retailers are excluded even if access status were accidentally marked acceptable.
4. Empty production scope means the runtime evidence source is not invoked.
5. A runtime source cannot return evidence outside its requested scope without failing closed.
6. The existing comparison-layer access check remains as defense in depth.
7. Production preview remains no-op/live-free under the current registry.
8. No public API schema changes are required.
9. Ordinary CI remains deterministic and network-free.

## TDD plan

### RED 1 — empty production scope suppresses acquisition

Add a `ComparisonPreviewServiceTest` using `RetailerRegistry.initial()` and an evidence source that fails if invoked. The preview must succeed without invoking the source and Magnit must remain `CONNECTED + BLOCKED + UNAVAILABLE`.

This test may already pass only if the service learns to skip `load()`; under the current implementation it fails because `load()` is always called.

### GREEN 1 — precompute scope and skip empty load

Compute immutable production-ready retailer IDs before acquisition. If empty, use `ComparisonRuntimeEvidence.empty()`.

### RED 2 — source contract receives exact allowed scope

Change the evidence-source contract to accept `requestedRetailers` and add a custom registry with only selected production-ready entries. Verify the source receives exactly those IDs.

### GREEN 2 — propagate immutable scope

Update `ComparisonRuntimeEvidenceSource`, `NoopComparisonRuntimeEvidenceSource`, deterministic test source and existing test lambdas/fixtures.

### RED/GREEN 3 — reject out-of-scope evidence

Add a regression where a source is asked only for one retailer but returns another. The service must reject the contract violation before quote construction.

## Acceptance

- full API verification PASS;
- existing deterministic critical-journey integration remains PASS;
- current production preview cannot invoke a runtime evidence source because no registry entry is production-ready;
- Magnit remains product-visible as `CONNECTED + BLOCKED + UNAVAILABLE`;
- no retailer/provider/store IDs leak into the public response;
- no production network implementation is introduced.

After this hardening, #90 continues with the final M1 evidence matrix and explicit GO / NO-GO to M2 Recipes.
