# Magnit Public `shopCode` Resolution Design

Updated: 2026-08-12
Status: implementation direction
Issue: #69
Baseline: `main@25df1c018d30a9427231fd2f9f564fcb4b4ce1e4`

## Context

Magnit product-page feasibility already uses explicit public `shopCode` values. M1 needs a safe provider-scoped way to turn a location/store choice into that stable fulfillment-context identifier without leaking Magnit IDs into shopping/basket semantics and without inventing a private or session-bound mechanism.

Finite public-surface research on `https://magnit.ru/shops` proved the following ordinary first-party mechanism:

- public page requests `POST /webgate/v1/stores-facade/search`;
- request body uses a geographic bounding box plus a fixed store-type list;
- response items expose `coordinates { latitude, longitude }` and `externalId { owner, storeCode }`;
- the exact browser-observed bbox was reproduced twice by plain Node `fetch` using only `Accept`, `Content-Type` and a User-Agent;
- no auth headers, app headers, cookies or cookie jar were required;
- the repeated known bbox returned the same store set containing `storeCode=992301`;
- broad Moscow and Saint Petersburg boxes returned distinct non-empty store sets (466 and 652 candidates respectively).

Text/locality/address → coordinates was **not** proven. Typing both `Москва` and the public landmark `Москва, Красная площадь, 1` in a clean public `/shops` session did not produce an address/geocoder request or selectable autocomplete result. Static/lazy-client exploration also did not yield a safely reproducible text-geocoder contract.

Therefore this slice accepts the proven **bbox → store candidates** boundary and explicitly refuses to invent a hidden text geocoder.

## Existing provider identity

Existing Magnit evidence already uses:

- retailer: `RetailerId.MAGNIT`;
- source provider ID: `magnit-public-page`;
- acquisition mode: `PUBLIC_WEB`;
- fulfillment context IDs scoped to the selected public shop.

The new code must reuse `magnit-public-page`; it must not introduce a second Magnit provider identity.

## Goal

Create deterministic production-domain primitives for:

1. representing a validated Magnit geographic bbox;
2. building the exact proven public store-search request body;
3. parsing sanitized public store-search JSON into provider-scoped candidates;
4. resolving only an unambiguous candidate set;
5. converting an unambiguous or explicitly selected store into the existing `FulfillmentContextBinding` boundary.

This slice deliberately stops before production live HTTP wiring. It proves the contract and selection semantics while #70 recurring production acquisition usage rights remains unresolved.

## Non-goals

- no text/address geocoder;
- no new coordinates on `ProductLocation`;
- no precise address persistence or logging;
- no nearest-store ranking;
- no first-candidate selection;
- no distance/delivery-area heuristic;
- no production Magnit HTTP client/Spring bean;
- no comparison-preview live Magnit requests;
- no scheduled/recurring polling;
- no bypass of #70;
- no provider IDs exposed through public shopping/basket/comparison vocabulary.

## Public request contract

The proven public endpoint accepts a POST body equivalent to:

```json
{
  "filters": {
    "geo": {
      "typeName": "box",
      "leftTopPoint": {"latitude": 45.069, "longitude": 38.967},
      "rightBottomPoint": {"latitude": 45.065, "longitude": 38.980}
    },
    "storeTypeListV2": [
      "MM", "GM", "DG", "MO", "ME", "MC", "DARKSTORE", "MM_MINI", "ZARYAD"
    ]
  }
}
```

The store-type list and `typeName=box` are part of the deterministic provider contract. This class builds data only; it does not send a request.

## Geographic model

`MagnitGeoPoint`:

- latitude finite and in `[-90, 90]`;
- longitude finite and in `[-180, 180]`.

`MagnitGeoBoundingBox`:

- left-top latitude must be strictly greater than right-bottom latitude;
- left-top longitude must be strictly less than right-bottom longitude;
- degenerate or inverted boxes fail construction.

Coordinates are provider-routing data for this specific operation. They are not added to `ProductLocation` and do not appear in default public comparison semantics.

## Response contract

Only exact proven response fields participate:

- root `items.items` array;
- each candidate item `externalId.storeCode` scalar string/number;
- each candidate item `coordinates.latitude` and `coordinates.longitude` numeric/scalar-number values.

Everything else, including address/name/city/metadata, is ignored by the parser.

Malformed entries cannot create candidates.

Candidate identity is `shopCode`. Equivalent duplicates (`same code + same canonical coordinates`) deduplicate. Reusing the same `shopCode` with conflicting coordinates causes the parse result to fail closed as `CONFLICTING_STORE_EVIDENCE` rather than arbitrarily choosing one record.

## Resolution semantics

`MagnitStoreResolution` is deterministic and order-independent:

- zero valid candidates → `NO_STORES`;
- exactly one unique valid candidate → `RESOLVED`;
- more than one unique candidate → `AMBIGUOUS`;
- conflicting duplicate identity evidence → `CONFLICTING_STORE_EVIDENCE`.

Only `RESOLVED` carries a chosen store candidate.

No distance-based ranking or implicit first element is permitted. Broad boxes returning hundreds of stores are expected to be `AMBIGUOUS` and require an explicit downstream/manual selection interaction.

## Provider binding

Provider-scoped binding uses the existing M1 location boundary:

- `sourceProviderId = "magnit-public-page"`;
- `fulfillmentContextId = shopCode`;
- locality copied from the user's existing `ProductLocation`/caller context;
- automatically unique candidate → `FulfillmentContextSelectionMode.RESOLVED`;
- explicitly chosen validated shop code → `FulfillmentContextSelectionMode.MANUAL`.

The binder never adds the shop code to `ProductLocation`, shopping-list data, basket items or product-facing comparison reason text.

## Manual selection

This slice provides a safe explicit binding factory for a manually chosen candidate/shop code. It does not build the UI yet.

The product rule is:

- auto-bind only `RESOLVED`;
- `NO_STORES`, `AMBIGUOUS`, and `CONFLICTING_STORE_EVIDENCE` do not auto-bind;
- a later/manual user selection may create a `MANUAL` provider binding.

## Test contract

Deterministic tests must prove:

- valid bbox payload preserves exact proven store-type ordering and geometry;
- invalid coordinates / degenerate boxes fail construction;
- exact response item parses `externalId.storeCode` + coordinates;
- address/name fields do not enter candidate state;
- missing/malformed code or coordinates cannot create candidates;
- equivalent duplicate entries deduplicate;
- same code with conflicting coordinates fails closed;
- parser ignores unrelated JSON fields;
- zero → `NO_STORES`;
- one → `RESOLVED`;
- many → `AMBIGUOUS`, independent of response order;
- automatic binding uses `magnit-public-page`, shopCode and `RESOLVED`;
- manual binding uses the same provider ID/shopCode and `MANUAL`;
- non-resolved results cannot produce automatic bindings;
- precise-address content is not retained or exposed by these objects.

## Live acceptance gate

After deterministic implementation and normal CI pass, the guarded/live acceptance evidence may call only the proven public search endpoint and must remain explicit/manual.

Before issue #69 is closed, merged-main live evidence should prove at minimum:

1. the known public bbox twice;
2. HTTP 2xx both times;
3. no auth/app headers or cookie jar required;
4. stable store set and expected public code `992301`;
5. sanitized output only.

Broad-city requests are useful ambiguity evidence but are not required for recurring acceptance runs.

## Production/access boundary

#69 proves location-context resolution mechanics, **not permission for recurring product acquisition**.

#70 remains independent and unresolved. Therefore:

- ordinary CI stays live-retailer-free;
- production preview evidence remains no-op/fail-closed;
- no scheduled Magnit polling is introduced;
- the endpoint contract is not wired into automatic production traffic in this slice.

## Exit criteria

- deterministic request/response/resolution/binding tests pass;
- no new Magnit provider identity is created;
- no product-address geocoder is invented;
- no provider IDs leak outside the existing internal provider/location boundary;
- exact-head CI/security and independent review pass;
- squash merge and post-merge `main` verification pass;
- merged-main guarded live bbox proof passes before #69 is marked accepted/closed.