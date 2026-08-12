# Magnit public `shopCode` resolution evidence — 2026-08-12/13

Status: **M1 technical LOCATION_RESOLUTION accepted for issue #69**. This is **not recurring production-access authorization**; issue #70 remains independent.

## Objective

Prove whether ordinary public Magnit surfaces can map a geographic store-search context to stable public `shopCode` values without private APIs, borrowed sessions, anti-bot workarounds, hidden credentials, precise-address fixtures or browser-only state.

## Public first-party surface

The ordinary public page `https://magnit.ru/shops` loads without authentication and exposes store-search state. Clean browser observation identified first-party requests to:

- `POST /webgate/v1/stores-facade/search`;
- `POST /webgate/v1/stores-facade/search/detail`.

The accepted search-response candidate path is intentionally narrow:

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

Direct reproduction proved Magnit application/version headers are not required for this store-search contract.

## Clean stateless research reproduction

The first finite research probe called the endpoint directly with ordinary JSON request headers and no Authorization, Magnit app/version headers, supplied cookies or cookie jar.

For the known coarse bbox, two independent requests produced:

- HTTP 200 both times;
- no response `Set-Cookie` header in the accepted run;
- one candidate each time;
- identical candidate sets;
- public `shopCode=992301` both times.

Broad public city boxes also proved that automatic selection must fail closed when many stores exist:

| Coarse box | HTTP | candidates | Example public codes |
|---|---:|---:|---|
| Moscow | 200 | 466 | `011830`, `019945`, `032005`, `043050`, `044800` |
| Saint Petersburg | 200 | 652 | `012333`, `021945`, `022380`, `023463`, `023493` |

A first/nearest-store rule is therefore not inferred from provider ordering.

## Text/address → coordinates investigation

A safe text-geocoder contract was **not** proven.

Clean public `/shops` sessions were tested only with public/coarse text, never a user's private address:

- `Москва`;
- `Москва, Красная площадь, 1`.

Neither produced a safely reproducible text-address contract. Static public bundle inspection and a parameterless `/webgate/v1/address/search` probe also did not establish an acceptable contract.

Decision: **do not invent or reverse-engineer an unproven locality/address → coordinates service.**

## Accepted deterministic product boundary — #86

PR #86 introduced only deterministic domain/contract behavior and did not activate production network traffic.

Accepted operation:

`validated bbox → public Magnit store candidates → fail-closed resolution → provider-scoped fulfillment binding`

Rules:

- zero candidates → `NO_STORES`;
- exactly one candidate → `RESOLVED`;
- more than one candidate → `AMBIGUOUS`;
- same `shopCode` with conflicting coordinates → `CONFLICTING_STORE_EVIDENCE`;
- no implicit first/nearest/distance ranking;
- explicit user/store choice may create a `MANUAL` binding.

Provider identity remains:

- `sourceProviderId = "magnit-public-page"`;
- `shopCode` is internal `LocationContext.fulfillmentContextId`;
- `RESOLVED` and `MANUAL` reuse `FulfillmentContextSelectionMode`.

`shopCode` does not enter `ProductLocation`, shopping-list items, basket items or product-facing comparison reasons. Store address/name metadata is not retained. Coordinates remain provider evidence rather than an expansion of the public `ProductLocation` model.

PR #86 squash-merged as `c3d10c672b6b67e8f03cc17823041bc88cc9bdee` and passed its full post-merge `main` gate.

## Merged-main live acceptance gate — #87

Issue #69 explicitly required a **merged-main** live proof before the project could claim `LOCATION_RESOLUTION`.

PR #87 added a test-only, owner-triggered issue-comment workflow. The production application remains network-no-op for Magnit. Ordinary CI does not run the live test.

The live client is deliberately stricter than the initial research probe:

- fresh `HttpClient`;
- no `CookieHandler`;
- no `Authenticator`;
- `Redirect.NEVER`;
- no Magnit app/version/auth headers;
- request body comes from production `MagnitStoreSearchRequest`;
- response interpretation comes from production `MagnitStoreSearchResponseParser`;
- exactly two requests;
- evidence output contains only statuses, candidate counts, booleans and request count.

PR #87 squash-merged as `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1`. Its merged-main push baseline completed successfully before the live command was issued.

The exact owner-only command `/provider-probe magnit-shopcode` was then posted to issue #69.

GitHub Actions run: `31642543544`  
Workflow: `Provider Live Probe - Magnit ShopCode`  
Checked-out SHA: `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1`

Sanitized evidence:

```text
MAGNIT_SHOPCODE_LOCATION first_status=200 first_candidates=1 first_has_992301=true first_set_cookie=false second_status=200 second_candidates=1 second_has_992301=true second_set_cookie=false same_candidate_set=true conflicting_evidence=false total_requests=2
```

The focused live test also reported:

- tests run: 3;
- failures: 0;
- errors: 0;
- skipped: 0;
- Maven build: SUCCESS.

This satisfies the final issue #69 acceptance requirement: the same explicit public store context is reproducible across a clean, direct, merged-main stateless boundary.

## Accepted claim

The project may now claim **Magnit technical `LOCATION_RESOLUTION` for the proven bbox/store-selection boundary**.

This means:

- a validated coarse geographic bbox can obtain public store candidates through the proven first-party surface;
- an unambiguous single candidate may produce `RESOLVED` provider context;
- ambiguous/empty/conflicting evidence remains fail-closed;
- a user may explicitly select a candidate to create `MANUAL` context;
- no user address geocoder, borrowed session or hidden credentials are required by the accepted mechanism.

It does **not** mean arbitrary text/locality automatically becomes coordinates, nor that recurring product traffic is authorized.

## Production-access boundary — #70

Issue #70 remains unresolved and independent.

Until #70 is explicitly accepted:

- no recurring Magnit polling is enabled;
- no production Spring/HTTP client is wired from comparison preview;
- ordinary CI makes no live retailer calls;
- live checks remain explicit guarded evidence only;
- production comparison evidence remains fail-closed/no-op.

Technical feasibility, package evidence and location resolution do not themselves grant a right to operate recurring acquisition in production.
