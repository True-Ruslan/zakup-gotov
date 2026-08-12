# M1 pre-acquisition production-access gate shipping evidence

Date: 2026-08-13
PR: #91
Issue: #90

## Reviewed candidate

- reviewed implementation SHA: `bb87c3d110edeb916cfdae1bcf07744a0e4e9e21`
- public API schema changes: none
- production retailer HTTP activation: none
- ordinary CI retailer network: none

## Closed architecture gap

Before this change, `ComparisonPreviewService` invoked `ComparisonRuntimeEvidenceSource.load(...)` before filtering evidence with retailer production-access policy. Output was safe with the current no-op production source, but a future live source could have performed a blocked retailer request before its evidence was discarded.

The accepted candidate moves access scoping before acquisition:

1. compute immutable retailer IDs where `RetailerRegistryEntry.isProductionReady()` is true;
2. if that set is empty, do not invoke the runtime evidence source;
3. otherwise pass the exact immutable set into `ComparisonRuntimeEvidenceSource.load(...)`;
4. reject any returned retailer evidence outside the requested scope before matching or basket construction;
5. retain the existing post-load production-readiness check as defense in depth.

With the current production registry the requested set is empty, so production comparison preview cannot initiate retailer acquisition.

## TDD evidence

### RED 1 — empty scope

With `RetailerRegistry.initial()`, a source that throws if invoked produced exactly one failing test because `load()` was always called. All other API tests passed.

### GREEN 1

The service began computing production-ready retailer IDs before acquisition and skips `load()` when the set is empty. Full API verification passed.

### RED 2 — explicit immutable scope

A test required the source to receive exactly `{PYATEROCHKA, PEREKRESTOK}` and an immutable set. Compilation failed only because the old functional interface still accepted two arguments.

### GREEN 2

`ComparisonRuntimeEvidenceSource` moved to `(ShoppingList, ProductLocation, Set<RetailerId>)`. The compiler identified exactly three remaining legacy test usages and no missed production callers. After migrating them, full API verification passed.

### RED 3 — source contract violation

A source requested only for Pyaterochka returned Magnit evidence. The service silently ignored it, yielding exactly one failing regression in a 234-test run.

### GREEN 3

The service now throws an explicit fail-closed contract error for an unrequested retailer before matching/quote construction. Full API verification passed.

## Exact-head CI

All nine PR workflow groups passed on `bb87c3d110edeb916cfdae1bcf07744a0e4e9e21`:

- API CI — PASS
- Contract CI — PASS
- Web CI / responsive E2E — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- Container Security CI — PASS
- CodeQL Java + JavaScript/TypeScript — PASS
- Release Contract CI — PASS
- Release Bundle CI — PASS

## Change review

Verdict: **Looks good**.

- P0: none
- P1: none
- P2: none
- blocking P3: none
- open review threads: none

Review confirmed:

- technical connectivity cannot grant acquisition permission by itself;
- `isProductionReady()` is the pre-acquisition source of truth;
- blocked/pending/discovery retailers cannot enter the runtime evidence request scope;
- empty scope prevents source invocation entirely;
- scope is immutable;
- evidence outside scope fails closed;
- existing product-facing coverage/access state remains visible even when no source is invoked;
- deterministic test evidence remains test-only and honors the requested scope;
- production no-op source remains network-free.

## Shipping boundary

#91 is ready to merge after the final docs-only exact-head gate.

M1 is **not** declared complete by this PR alone. After #91 merges and post-merge `main` is green, issue #90 will receive a separate docs-only final acceptance decision with an explicit GO/NO-GO to M2 Recipes.

## Rollback

The change is an internal evidence-source contract plus service gating and tests. No persistence or public API migration exists. A normal revert restores the prior post-load-only gate; retailer registry/access policy remains unchanged.
