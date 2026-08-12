# Magnit public `shopCode` resolution evidence — 2026-08-12

Status: M1 technical evidence for issue #69; **not recurring production-access authorization**.

## Objective

Prove whether ordinary public Magnit surfaces can map a geographic store-search context to stable public `shopCode` values without private APIs, borrowed sessions, anti-bot workarounds, hidden credentials, precise-address fixtures or browser-only state.

## Public first-party surface

The ordinary public page `https://magnit.ru/shops` loads successfully without authentication and exposes store-search state. Clean browser observation showed first-party requests to:

- `POST /webgate/v1/stores-facade/search`;
- `POST /webgate/v1/stores-facade/search/detail`.

The search response contains public store identity and coordinates. The exact proven candidate path is:

- `items.items[].externalId.storeCode`;
- `items.items[].coordinates.latitude`;
- `items.items[].coordinates.longitude`.

Addresses/names are not required by the resolution contract and are not retained by the production-domain parser.

## Proven request contract

The public page sends a geographic bounding-box search:

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

Browser observation exposed optional public app headers, but direct reproduction proved they are unnecessary for this search contract.

## Clean stateless reproduction

A one-shot Node probe called the public search endpoint directly using only:

- `Accept: application/json`;
- `Content-Type: application/json`;
- a descriptive User-Agent;
- no Authorization;
- no Magnit app/version headers;
- no supplied cookies;
- no cookie jar.

### Known bbox

The exact coarse bbox observed from the public `/shops` page was requested twice independently.

Result:

- HTTP 200 both times;
- `Set-Cookie` absent both times;
- one candidate each time;
- identical candidate set;
- public `shopCode=992301` both times;
- candidate coordinates matched the observed public store context.

This proves the store-search result is reproducible across clean stateless requests rather than depending on a borrowed browser session.

### Broad-city ambiguity evidence

Two coarse public city boxes were also queried:

| Coarse box | HTTP | candidates | Example public codes |
|---|---:|---:|---|
| Moscow | 200 | 466 | `011830`, `019945`, `032005`, `043050`, `044800` |
| Saint Petersburg | 200 | 652 | `012333`, `021945`, `022380`, `023463`, `023493` |

The candidate sets were different.

This is important product evidence: a location search commonly returns **many** stores. Therefore selecting the first result or inventing a nearest-store rule would be an unjustified semantic decision.

## Text/address → coordinates investigation

A safe text-geocoder contract was **not** proven.

Clean public `/shops` sessions were tested with only public/coarse text, never a user's private address:

- `Москва`;
- `Москва, Красная площадь, 1`.

Neither input produced a selectable address-autocomplete result or an attributable first-party address/geocoder request. Static Nuxt-bundle inspection also did not yield a safely reproducible text-address contract, and `GET /webgate/v1/address/search` without parameters returned a normal 404 rather than a useful validation schema.

Decision: **do not invent or reverse-engineer an unproven locality/address → coordinates service.**

## Accepted #69 technical boundary

The implemented product-domain boundary accepts only the proven operation:

`validated bbox → public Magnit store candidates → fail-closed resolution → provider-scoped fulfillment binding`

Rules:

- zero candidates → `NO_STORES`;
- exactly one candidate → `RESOLVED`;
- more than one candidate → `AMBIGUOUS`;
- same `shopCode` with conflicting coordinates → `CONFLICTING_STORE_EVIDENCE`;
- no implicit first/nearest/distance ranking;
- explicit user/store choice may create a `MANUAL` binding.

## Provider identity and privacy

Existing Magnit provider identity is reused:

- `sourceProviderId = "magnit-public-page"`;
- `shopCode` becomes internal `LocationContext.fulfillmentContextId`;
- `RESOLVED` and `MANUAL` reuse the existing `FulfillmentContextSelectionMode` boundary.

`shopCode` is not added to `ProductLocation`, shopping-list items, basket items or product-facing reason text.

The implementation does not retain public store address/name metadata. `MagnitStoreCandidate` contains only public `shopCode` and coordinates needed to interpret the candidate evidence.

Coordinates are not added to `ProductLocation`; this slice therefore does not expand the user-location privacy surface.

## Production-access boundary

Issue #69 proves a reproducible technical location/store-context mechanism. It does **not** resolve issue #70.

Until #70 is explicitly accepted:

- no recurring Magnit polling is enabled;
- no new production Spring/HTTP client is wired from comparison preview;
- ordinary CI makes no live retailer calls;
- live checks remain explicit guarded evidence only;
- production comparison evidence remains fail-closed/no-op.

## Acceptance path

Before #69 is closed:

1. deterministic request/parser/resolution/binding tests must pass;
2. exact-head CI/security and independent review must pass;
3. #86 must squash-merge and post-merge `main` CI must pass;
4. a merged-main explicit guarded live check must repeat the known bbox twice and confirm stable public `992301` without auth/app headers/cookie jar.

Only then may the project call Magnit location→provider-context technical resolution accepted. #70 remains independent.