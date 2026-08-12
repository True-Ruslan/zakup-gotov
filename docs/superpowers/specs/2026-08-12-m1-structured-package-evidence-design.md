# M1 Structured Package Evidence Design

Updated: 2026-08-12
Status: approved implementation direction

## Context

M1 basket planning already requires explicit package quantity evidence keyed by `OfferSnapshotId`. Missing evidence produces `PACKAGE_QUANTITY_UNKNOWN`; product presentation text is deliberately not parsed. After the stateless comparison journey shipped in #80, the remaining gap is that provider observations and immutable offer snapshots do not yet carry an optional trusted package quantity, so basket evidence still has to be assembled separately.

Current accepted Perekrestok and Pyaterochka browser paths do not prove a dedicated structured package field. Their accepted contracts intentionally persist only allow-listed normalized page evidence and do not inspect response bodies. Magnit production activation is separately blocked by location/context and usage-rights work. This slice therefore must not invent a retailer extractor or relax any browser/privacy boundary.

## Goal

Create one provider-neutral path by which a source that has already proved structured package semantics can attach that evidence to an observed offer, preserve it through snapshotting, and expose it to basket planning without inference.

## Non-goals

- No parsing of `productName`, title, slug, SKU, source URL, visible free-form text, or arbitrary HTML for package size.
- No new live retailer requests.
- No response-body interception or broader browser-extension permissions.
- No change to retailer production-access status.
- No activation of Magnit recurring acquisition while #70 remains unresolved.
- No claim that Perekrestok, Pyaterochka, Magnit, or any other retailer already supports trusted structured package extraction.
- No API/UI schema change in this slice; package evidence remains an internal comparison input until a production retailer source is accepted.

## Design

### 1. Provider observation boundary

`ObservedOffer` gains an optional `packageQuantity` using the existing canonical `shopping.Quantity` value type.

Rules:

- presence means the provider/source adapter has already established that the value is structured package evidence for the observed SKU;
- absence is first-class and means unknown;
- the existing constructor remains available and delegates to an empty package quantity so current providers/fixtures retain identical behavior;
- no constructor or helper derives the field from `productName` or any presentation string.

Using canonical `Quantity` keeps unit normalization (`kg -> g`, `l -> ml`) in one place and avoids a second amount/unit model. The provider layer may depend on the lower-level canonical quantity value object, while shopping remains independent of provider data.

### 2. Immutable snapshot preservation

`OfferSnapshot` copies the optional package quantity from the validated `ObservedOffer` unchanged. Both observation-only and provider-updated freshness factories preserve the evidence.

Snapshot identity remains independent of the package value; package quantity is evidence attached to that observation, not part of identity generation.

### 3. Basket evidence projection

`PackageQuantitySet.fromSnapshots(List<OfferSnapshot>)` projects only snapshots with an explicit package quantity into existing `PackageQuantityBinding` records.

Rules:

- snapshots without package evidence create no binding;
- input order is preserved for bindings;
- null snapshot/list values fail closed;
- duplicate snapshot IDs continue to fail through the existing set invariant;
- no fallback inspection of `productName` occurs.

This keeps the basket planner unchanged: it still consumes `PackageQuantitySet`, and `PACKAGE_QUANTITY_UNKNOWN` semantics remain exactly as before for absent evidence.

## Compatibility

The legacy `ObservedOffer` constructor remains source-compatible. Existing provider fixtures, orchestrator tests, retailer probes, preview fixtures and browser bridge behavior need no migration and continue to represent package quantity as unknown.

`OfferSnapshot` adds an accessor only; existing callers continue to work.

## Security and privacy

This slice adds no network, credential, browser permission, address, or provider-routing surface. Package quantity is product metadata and inherits the existing observation provenance (`sourceProviderId`, acquisition mode, fulfillment context, source reference, observation time). Internal provenance remains internal at product/API boundaries.

A future retailer extractor must be reviewed separately and demonstrate that its source field has stable package semantics before populating this value.

## Invariants

1. `packageQuantity` is optional evidence, never an inferred default.
2. Product names such as `Молоко 3,2%, 970мл` do not create package evidence by themselves.
3. Present values use canonical positive `Quantity` semantics.
4. Snapshotting never drops or changes trusted package evidence.
5. Basket projection includes only explicitly present package evidence.
6. Missing evidence continues to surface as `PACKAGE_QUANTITY_UNKNOWN` when a matched offer reaches basket planning.
7. Ordinary CI performs no live retailer requests.
8. Existing browser-bridge security/privacy boundaries remain unchanged.

## Test strategy

TDD sequence:

1. RED: provider/snapshot tests require optional structured package evidence and preservation while proving presentation text alone remains empty.
2. RED: basket test requires `PackageQuantitySet.fromSnapshots` to include only explicit evidence and preserve canonical units/order.
3. GREEN: add the smallest compatible provider/snapshot fields and projection factory.
4. Run focused API tests, Spring Modulith/ArchUnit rules, then the full repository CI/security gate.

## Exit criteria

- provider observations can carry optional trusted package quantity without breaking existing callers;
- snapshots preserve the optional evidence exactly;
- basket package evidence can be constructed directly from snapshots;
- presentation text is proven non-authoritative by regression test;
- no retailer extractor, live access, browser permission or production-access status changes;
- exact-head CI/security checks are green and independent review finds no blocking issue.

## Follow-up

After this plumbing is accepted, investigate one retailer/source at a time for an authoritative structured field. The first extractor is a separate evidence-backed slice and must document field semantics, fixture provenance, failure behavior and production-access constraints before populating `packageQuantity`.