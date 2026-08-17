# Chizhik D2 external schema hypothesis — 2026-08-18

## Classification

**Discovery hint only — not accepted provider evidence.**

This note records public third-party reverse-engineering material to make the next ordinary-browser canary easier to interpret. It must not be used by production offer mapping until the same field structure and price semantics are confirmed from the official Chizhik page origin.

Tracking issue: #169.

## Source inspected

Public repository: `Open-Inflation/chizhik_api`.

Relevant current files:

- `chizhik_api/endpoints/catalog.py` — suggests the store-scoped delivery search template;
- `tests/api_test.py` — takes `sap_id` from shop search and passes it as `store_id` to delivery search;
- `tests/__snapshots__/ClassCatalog.delivery_search.schema.json` — generated response-schema snapshot;
- `tests/__snapshots__/ClassCatalog.delivery_search.json` — historical response snapshot.

ZakupGotov does **not** adopt that project's Camoufox/proxy/session-emulation approach. Only public endpoint/schema hints are being inspected.

## Hypothesized response shape

The third-party schema snapshot reports an object with a `products` array. A product item includes, among many other fields:

| Candidate field | Snapshot type | Possible ZakupGotov role | Accepted? |
| --- | --- | --- | --- |
| `plu` | integer | SKU | **No** |
| `name` | string | product name | **No** |
| `prices.regular` | string | price candidate | **No** |
| `is_available` | boolean | availability candidate | **No** |
| `stock_limit` | string | stock metadata | **No** |
| `uom` | string | unit hint | **No** |
| `property_clarification` | string | package/display hint | **No** |

The historical snapshot shows `prices.regular` formatted as a decimal-looking string, which is consistent with rubles rather than integer kopecks. That is still insufficient evidence to implement `priceMinor` conversion: the live official-origin canary must confirm the current field and its unit semantics first.

## Fields deliberately excluded from current mapping

The snapshot also contains promotion/loyalty/advertising fields such as `promo`, `prices.discount`, `cpd_promo_price`, `orange_loyalty_points`, labels and badges. None of these may be interpreted until separately evidenced. D2's minimum mapping is limited to identity, name, base price and availability.

## Acceptance gate

The ordinary-browser canary documented in `chizhik-d2-delivery-search-canary-2026-08-18.md` must confirm at minimum:

```text
$.products -> array
$.products[] -> object
$.products[].plu -> integer/number
$.products[].name -> string
$.products[].prices -> object
$.products[].prices.regular -> string/number
$.products[].is_available -> boolean
```

Only after that evidence is accepted may a new RED test specify `BrowserObservation` mapping. Until then the active Chizhik adapter remains `observation-only` and performs zero automatic delivery-search calls.
