# Chizhik D2 store-scoped delivery-search canary — 2026-08-18

## Status

**Implementation in draft PR #177; ordinary-user-browser evidence still required.**

This canary exists only to obtain privacy-safe structural evidence for issue #169. It does not accept the delivery payload schema, monetary unit, availability semantics, offer mapping or production/right-to-operate status by itself.

## Why this procedure exists

Phase D1 and the accepted #173/#174 store-context rule established two permanent constraints:

- Chizhik acquisition uses the normal user-browser MV3 Retailer Bridge, not a managed CI/server browser worker;
- store context must come from an exact first-party Chizhik delivery resource already observed by the current browser session and must intersect the validated `/api/v1/shops/` directory.

The earlier DevTools draft in this document selected the first active store from `/api/v1/shops/`. That is no longer valid and has been removed. **The canary must never guess a store from directory order, active status or convenience.**

## Fixed transport under test

The already-merged D2 transport uses only:

```text
GET https://app.chizhik.club/delivery/api/catalog/v3/stores/{sap_id}/search?mode=store&include_restrict=true&q=%D0%BA%D0%BE%D0%BB%D0%B0&limit=1
```

The canary keeps the response payload opaque to ordinary bridge behavior. It is invoked only by the user and summarizes structural field-name/type evidence without returning raw values.

## Safety boundary

The implementation in #177:

- runs only after an explicit toolbar-popup button click;
- adds no new extension permissions and no `host_permissions`;
- uses only the current official `https://chizhik.club/...` content-script session;
- obtains valid store IDs from the fixed `/api/v1/shops/` directory;
- derives the candidate `sap_id` only from already-observed exact first-party delivery resource paths;
- requires exactly one distinct browser-evidenced store that is present in the validated directory;
- fails closed on missing, foreign-origin, unknown or conflicting context;
- issues exactly one fixed `кола`, `limit=1` D2 search per user invocation;
- retains only HTTP status, base content type, root type and bounded candidate-field/type structure;
- reports only the fixed candidate keys already recorded in the external schema hypothesis: `products`, `plu`, `name`, `prices`, `regular`, `is_available`, `stock_limit`, `uom`, `property_clarification`;
- ignores every other object key instead of attempting generic schema discovery, preventing dynamic IDs or unrelated payload keys from entering evidence;
- never emits raw response bodies, store IDs, addresses, coordinates, product names, SKU values, numeric price values, promotion values, request IDs, cookies, headers or credentials;
- leaves automatic D2 search and `BrowserObservation` / `ObservedOffer` production disabled.

This fixed allowlist means the canary may report that an expected candidate is absent, but it will not reveal arbitrary replacement field names if the live schema changed. That is intentional privacy-first behavior; expanding the allowlist requires a separate reviewed hypothesis update rather than dumping raw schema.

## Running the canary

Use an extension build containing #177. Until the PR is accepted and merged, use only an exact reviewed branch commit; do not treat a local or PR build as production evidence.

1. Open an official Chizhik catalog/delivery page under `https://chizhik.club/` in the ordinary user browser.
2. Interact with the official page normally until the current session has loaded a first-party delivery catalog resource for the selected store.
3. Open the **Zakup Gotov Retailer Bridge** toolbar popup.
4. Click **Run sanitized Chizhik canary** once.
5. Copy only the rendered evidence text.

Expected successful shape:

```text
CHIZHIK_D2 status=PASS search_http_status=200 content_type=application/json root=object
CHIZHIK_D2_SCHEMA=[...approved candidate field names and types only...]
```

Possible fail-closed statuses include:

```text
CHIZHIK_D2 status=WRONG_ORIGIN
CHIZHIK_D2 status=STORES_UNAVAILABLE
CHIZHIK_D2 status=MISSING_CONTEXT
CHIZHIK_D2 status=SEARCH_UNAVAILABLE
CHIZHIK_D2 status=PROBE_ERROR
CHIZHIK_D2 status=UNAVAILABLE
```

`MISSING_CONTEXT` is not permission to pick the first active store manually. Continue using the official page until one real first-party delivery context is observed, then invoke the canary again.

## Evidence to retain

Retain only lines beginning with:

```text
CHIZHIK_D2 status=
CHIZHIK_D2_SCHEMA=
```

Do not paste screenshots of Network response bodies or raw DevTools objects into issues, PRs, chats or repository files.

The accepted external hypothesis currently expects the canary to confirm or reject these minimum paths/types:

```text
$.products -> array
$.products[] -> object
$.products[].plu -> number/integer
$.products[].name -> string
$.products[].prices -> object
$.products[].prices.regular -> string/number
$.products[].is_available -> boolean
```

`stock_limit`, `uom` and `property_clarification` may also be reported as structural hints, but they do not become accepted stock/package semantics from this canary alone.

### Monetary unit remains a separate gate

A field name and JSON number/string type do **not** prove whether a price is rubles, kopeks/minor units, or another scaled representation. The structural canary therefore does not authorize `priceMinor` mapping by itself.

After the candidate price field is identified, #169 still requires independent sanitized evidence of its monetary unit/scale before any price mapping is implemented. Do not infer the unit from an integer-looking value or from a third-party implementation.

If explicit availability semantics are not proven, availability must remain `UNKNOWN`. Promotion, loyalty, package and discount semantics remain unavailable until separately evidenced.

## Test evidence in #177

The draft implementation is regression-protected so that:

- no D2 search occurs before the explicit user click;
- exactly one search occurs after invocation;
- valid, unknown, foreign and conflicting store contexts follow the accepted #173 fail-closed rule;
- successful evidence includes only the fixed candidate-field allowlist plus sanitized HTTP metadata;
- identifier-like dynamic keys and unrelated fields such as request IDs, promotion and discount keys are omitted from evidence;
- sentinel store/product/SKU/price/promotion/request values are absent from rendered evidence;
- production and E2E manifests keep `permissions: ["storage"]` with no host-permission widening;
- real persistent-Chromium extension E2E exercises the popup → active Chizhik tab → content-script canary path.

## Next gate

After real ordinary-user-browser evidence is supplied and accepted:

1. confirm the minimum evidenced product container/identifier/name/price candidate fields;
2. establish monetary unit/scale independently;
3. freeze a minimal sanitized fixture containing only accepted fields and semantics;
4. add RED tests for exact `BrowserObservation` mapping;
5. implement only the evidenced mapping;
6. keep unknown availability as `UNKNOWN` and do not invent promotion/package/loyalty semantics;
7. keep technical feasibility separate from production/right-to-operate approval.
