# Chizhik D2 store-scoped delivery-search canary — 2026-08-18

## Status

**Ready for ordinary-user-browser execution.** This document does not accept the delivery payload schema by itself.

Phase D1 established the technical transport split:

- an ordinary user-opened `https://chizhik.club/` page can fetch the fixed first-party store directory at `https://app.chizhik.club/api/v1/shops/` and receive valid JSON;
- stock GitHub-hosted Chromium was `page-unavailable`;
- therefore Chizhik acquisition remains a normal user-browser MV3 Retailer Bridge path rather than a managed browser worker.

Issue: #169.

## Candidate D2 endpoint

A public third-party implementation (`Open-Inflation/chizhik_api`) is used only as endpoint-discovery input. Its current source suggests:

```text
GET https://app.chizhik.club/delivery/api/catalog/v3/stores/{sap_id}/search?mode=store&include_restrict=true&q={query}&limit={limit}
```

Its tests pass a store `sap_id` into this delivery-search path. Neither the endpoint nor its response schema becomes an accepted ZakupGotov contract until the ordinary-browser canary below succeeds.

## Safety boundary

The canary:

- must be run only from DevTools on an already-open official `https://chizhik.club/...` page;
- requests only the exact `/api/v1/shops/` directory and the fixed store-scoped delivery-search template;
- selects one active store internally and **does not print its `sap_id`, address, coordinates, or other store values**;
- searches only the fixed term `кола` with `limit=1`;
- uses ordinary `cors`, `same-origin` credentials and an 8-second deadline;
- prints HTTP status plus structural schema only;
- filters reported object field names to ASCII identifier-like keys before printing them, preventing dynamic IDs/values used as object keys from leaking into evidence;
- never prints or persists the raw JSON body, product names, SKUs, prices, promotion values, cookies, headers or credentials.

Do not paste a raw response body into an issue, PR, chat or repository.

## DevTools canary

Open an official Chizhik catalog page, open DevTools → Console, paste the whole block and run it once:

```js
(async () => {
  const PAGE_ORIGIN = "https://chizhik.club";
  const SHOPS_ENDPOINT = "https://app.chizhik.club/api/v1/shops/";
  const SEARCH_BASE = "https://app.chizhik.club/delivery/api/catalog/v3/stores";
  const QUERY = "кола";
  const LIMIT = 1;
  const TIMEOUT_MS = 8_000;
  const SAP_ID_PATTERN = /^[A-Za-z0-9_-]{1,32}$/;
  const SAFE_FIELD = /^[A-Za-z_][A-Za-z0-9_]{0,63}$/;

  const valueType = (value) => {
    if (value === null) return "null";
    if (Array.isArray(value)) return "array";
    return typeof value;
  };

  const requestJson = async (url) => {
    const controller = new AbortController();
    const deadline = setTimeout(() => controller.abort(), TIMEOUT_MS);
    try {
      const response = await fetch(url, {
        method: "GET",
        mode: "cors",
        credentials: "same-origin",
        headers: { Accept: "application/json, text/plain, */*" },
        signal: controller.signal,
      });
      const contentType = response.headers.get("content-type")?.toLowerCase() ?? "";
      if (!response.ok || !contentType.startsWith("application/json")) {
        return { status: "HTTP_UNAVAILABLE", httpStatus: response.status, contentType, payload: null };
      }
      try {
        return {
          status: "RECEIVED",
          httpStatus: response.status,
          contentType,
          payload: await response.json(),
        };
      } catch {
        return { status: "INVALID_JSON", httpStatus: response.status, contentType, payload: null };
      }
    } catch {
      return { status: "FETCH_UNAVAILABLE", httpStatus: -1, contentType: "", payload: null };
    } finally {
      clearTimeout(deadline);
    }
  };

  const schema = [];
  const seenPaths = new Set();
  const visit = (value, path = "$", depth = 0) => {
    if (depth > 5 || schema.length >= 80 || seenPaths.has(path)) return;
    seenPaths.add(path);

    if (Array.isArray(value)) {
      schema.push({ path, type: "array" });
      if (value.length > 0) visit(value[0], `${path}[]`, depth + 1);
      return;
    }
    if (!value || typeof value !== "object") return;

    const safeEntries = Object.entries(value).filter(([key]) => SAFE_FIELD.test(key));
    schema.push({
      path,
      type: "object",
      fields: Object.fromEntries(safeEntries.map(([key, child]) => [key, valueType(child)])),
    });
    for (const [key, child] of safeEntries) {
      if (child && typeof child === "object") visit(child, `${path}.${key}`, depth + 1);
    }
  };

  if (location.origin !== PAGE_ORIGIN) {
    console.log("CHIZHIK_D2 status=WRONG_ORIGIN");
    return;
  }

  const shops = await requestJson(SHOPS_ENDPOINT);
  if (shops.status !== "RECEIVED" || !Array.isArray(shops.payload)) {
    console.log(
      `CHIZHIK_D2 status=SHOPS_UNAVAILABLE shops_http_status=${shops.httpStatus} search_http_status=-1 root=unknown`,
    );
    return;
  }

  const store = shops.payload.find(
    (row) =>
      row &&
      typeof row === "object" &&
      row.status === 1 &&
      typeof row.sap_id === "string" &&
      SAP_ID_PATTERN.test(row.sap_id),
  );
  if (!store) {
    console.log(
      `CHIZHIK_D2 status=NO_VALID_STORE shops_http_status=${shops.httpStatus} search_http_status=-1 root=unknown`,
    );
    return;
  }

  const searchUrl = `${SEARCH_BASE}/${encodeURIComponent(store.sap_id)}/search?mode=store&include_restrict=true&q=${encodeURIComponent(QUERY)}&limit=${LIMIT}`;
  const search = await requestJson(searchUrl);
  if (search.status !== "RECEIVED") {
    console.log(
      `CHIZHIK_D2 status=${search.status} shops_http_status=${shops.httpStatus} search_http_status=${search.httpStatus} root=unknown`,
    );
    return;
  }

  visit(search.payload);
  console.log(
    `CHIZHIK_D2 status=PASS shops_http_status=${shops.httpStatus} search_http_status=${search.httpStatus} content_type=application/json root=${valueType(search.payload)}`,
  );
  console.log(`CHIZHIK_D2_SCHEMA=${JSON.stringify(schema)}`);
})().catch(() => {
  console.log("CHIZHIK_D2 status=PROBE_ERROR shops_http_status=-1 search_http_status=-1 root=unknown");
});
```

## Evidence to retain

Retain only the two lines whose prefixes are:

```text
CHIZHIK_D2 ...
CHIZHIK_D2_SCHEMA=...
```

A transport PASS alone is not enough for offer production. The schema line must show unambiguous fields/types for the minimum `BrowserObservation` mapping:

- store fulfillment context remains the already validated `sap_id` from D1;
- SKU/product identifier;
- product name;
- price plus enough evidence to determine whether it is already minor units or requires conversion;
- availability only if an explicit, understood field exists; otherwise ZakupGotov must use `UNKNOWN`.

Promotion, loyalty, package and discount semantics remain unavailable unless separately evidenced.

## Next gate

After sanitized browser evidence is accepted:

1. freeze a minimal fixture containing only the evidenced fields;
2. add a failing adapter test for exact `BrowserObservation` mapping;
3. implement the minimum mapping;
4. add Chromium E2E for search success, malformed JSON/schema, blocked transport and unknown-field non-persistence;
5. keep technical feasibility separate from production/right-to-operate approval.
