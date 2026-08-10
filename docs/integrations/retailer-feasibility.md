# Retailer Feasibility Matrix

Updated: 2026-08-10
Status: M0B discovery evidence — **no retailer/provider is supported yet**

This document records technical and usage-rights evidence for candidate retailer data paths. It is an engineering feasibility record, not legal advice.

## M0B integration policy

Zakup Gotov does **not** reject a data source merely because an API is undocumented or unofficial. A public consumer backend may be evaluated as `PUBLIC_UNOFFICIAL_API` when the ordinary web/mobile product itself uses it and the path can be exercised without bypassing access controls.

A public unofficial API is acceptable for an M0B spike only when all of the following remain true:

1. requests do not require stolen, forged, reverse-engineered private credentials or another user's session;
2. no CAPTCHA bypass, anti-bot bypass, browser-fingerprint evasion, proxy rotation, IP rotation, or deliberate blocking circumvention is required;
3. rate limits, `429`, retry hints and provider failures are respected rather than hidden;
4. data collection is minimized to what the product needs rather than mirroring an entire retailer database by default;
5. exact source and observation time are preserved;
6. location/store context is explicit so prices are not presented as universal when they are store-specific;
7. raw responses can be sanitized into deterministic fixtures without secrets or precise user-address data;
8. current terms/usage-rights evidence does not make the intended reuse clearly unacceptable.

Third-party wrappers are **technical evidence only**. Their existence or MIT license does not grant rights to the retailer's underlying data and does not make their anti-bot techniques acceptable for Zakup Gotov.

## Provider access types

The executable provider model distinguishes:

- `OFFICIAL_API` — documented API offered by the provider for the relevant use;
- `PUBLIC_UNOFFICIAL_API` — public consumer backend used by the ordinary product, subject to the M0B restrictions above;
- `PARTNER_API` — supported API that requires a partnership/seller/merchant agreement and may not fit a consumer comparison client.

## Decision labels

- `SPIKE_NOW` — enough technical evidence exists to justify a controlled M0B probe; support is still unproven.
- `SPIKE_IF_RAW_HTTP_WORKS` — third-party evidence exists, but browser-emulation/anti-bot coupling must first be removed from the path.
- `PROMISING_CONTACT_REQUIRED` — official integration surface exists, but access/scope/usage rights still need confirmation.
- `PARTNER_SIDE_ONLY` — official API exists, but its documented direction is for a retailer/merchant partner rather than a read-side comparison client.
- `BLOCKED_WITHOUT_AGREEMENT` — public consumer data exists, but current terms do not provide an acceptable production assumption for automated reuse.
- `RESEARCH_REQUIRED` — public surface exists but the location/catalog/price path is not sufficiently characterized yet.

## Current matrix

| Candidate | Technical evidence | Location / store evidence | Current decision | Next proof required |
|---|---|---|---|---|
| **Pyaterochka / 5ka** | Public 5ka catalog exposes prices. Open-Inflation `pyaterochka_api` documents store-scoped category/product calls and PLU identity. | Wrapper obtains the selected delivery store `sapCode` and passes it into catalog/product calls; geolocation suggest/geocode are also exposed. | `SPIKE_IF_RAW_HTTP_WORKS` | Reproduce selected-store + 20-item corpus using ordinary HTTP without Camoufox/stealth; capture sanitized fixtures; compare two stores. |
| **Perekrestok** | Open-Inflation `perekrestok_api` reproduces ordinary site network calls, exposes geolocation, category tree and product feed/IDs, with schema-snapshot tests. | City/session context is demonstrated; exact store-level price/availability semantics still need independent proof. | `SPIKE_NOW` | Identify fulfillment/store selector, run fixed corpus, prove price/store linkage and availability semantics, save fixtures. |
| **Magnit** | Official public catalog exposes current product prices and supports a `shopCode` query parameter. | Public catalog pages with different `shopCode` values return different catalog sizes/content, strong evidence that store context matters. | `SPIKE_NOW` | Discover the ordinary backend calls used by the public catalog, then test two explicit stores with a 20-item corpus using non-evasive HTTP. |
| **Chizhik** | Open-Inflation `chizhik_api` exposes cities, categories, products and active offers. | Geolocation/catalog APIs exist in the wrapper, but the README installs Camoufox and explicitly discusses optional proxies/imitating a regular user. | `SPIKE_IF_RAW_HTTP_WORKS` | Determine whether required catalog calls work with a plain HTTP client. If stealth/proxy rotation is required, mark the path unsuitable. |
| **Ozon / Ozon Fresh** | Open-Inflation has an experimental `ozon_api`; Ozon also has extensive official seller APIs, but seller APIs are not the target consumer read path. | Fresh fulfillment-zone/store and stock semantics have not yet been proven. | `RESEARCH_REQUIRED` | Characterize Ozon Fresh consumer requests, location semantics, SKU/price/availability and whether ordinary public HTTP is sufficient. |
| **Samokat** | Public consumer product exists, but no sufficiently characterized current read-side API path is yet recorded here. | Fulfillment is highly location-dependent by product design. | `RESEARCH_REQUIRED` | Inspect ordinary web/mobile network behavior and determine whether a non-evasive public catalog path exists. |
| **Kuper** | Official API portal exposes Merchant Service API, Fulfillment API, **Client apps API**, Other API, integration contact and technical support. | Exact Client apps API catalog/search/store semantics are not yet proven from accessible public docs. | `PROMISING_CONTACT_REQUIRED` | Resolve issue #36: read-side scope, auth, location, catalog, price, availability, caching/fixtures, comparison rights, sandbox and rate limits. |
| **Yandex Eats Retail API** | Official Retail API documentation includes nomenclature, availability and product price/promotion exchange. | Documented architecture has Yandex/Yango acting as the client of a partner retailer POS/system. | `PARTNER_SIDE_ONLY` | Find a separate documented read-side partner/product path for a comparison client; do not misuse the retailer-side integration. |
| **Lenta** | Consumer catalog/order experience is location-dependent and a business partnership surface exists. | Consumer agreement ties order assembly to the delivery address / nearest store. | `BLOCKED_WITHOUT_AGREEMENT` | Obtain an explicit supported data-access/partner basis before an automated production adapter. |
| **VkusVill** | Consumer web/app and VkusVill Business exist. | Delivery/catalog is consumer-context dependent; no reusable public client API is proven. | `BLOCKED_WITHOUT_AGREEMENT` | Obtain explicit partner/API permission before production catalog/price reuse. |
| **X5 Group** | X5 operates Pyaterochka, Perekrestok and Chizhik, but their consumer technical surfaces differ. | Store/location behavior must be proven independently per banner even if shared infrastructure later emerges. | `RESEARCH_REQUIRED` | Treat the three banners as separate provider spikes first; extract shared X5 infrastructure only after repeated behavior is proven. |

## Evidence

### Pyaterochka / 5ka

Official/public surfaces:

- https://5ka.ru/catalog/
- https://5ka.ru/docs/

Independent technical reference:

- https://github.com/Open-Inflation/pyaterochka_api

The reference documents a public-site network client that obtains a selected delivery store `sapCode`, uses that store ID for catalog/product calls, exposes PLU product identity, and includes geolocation calls. Its README also installs Camoufox. Zakup Gotov will therefore use it only to identify hypotheses/endpoints; the first spike must prove that plain HTTP is sufficient before this path can advance.

### Perekrestok

Independent technical reference:

- https://github.com/Open-Inflation/perekrestok_api

The project states that it reproduces the ordinary network behavior of a website user, demonstrates Moscow geolocation, category/product IDs, and has endpoint schema-snapshot tests. This is strong technical evidence for a controlled spike, but it is not evidence of store-specific availability or data-reuse permission.

### Magnit

Official/public surface:

- https://magnit.ru/catalog
- example store-scoped surface: https://magnit.ru/catalog?shopCode=611694

The public catalog currently exposes concrete prices and accepts a `shopCode`. Publicly indexed pages with different `shopCode` values show different catalog sizes/content. The next spike should inspect only the ordinary requests used by this page and must not add anti-bot circumvention.

### Chizhik

Independent technical reference:

- https://github.com/Open-Inflation/chizhik_api

The wrapper demonstrates city lookup, category tree, product lists and active offers. However, its documented setup includes Camoufox and optional proxies, and its own comments emphasize appearing like a regular user. Those techniques are explicitly **not** part of Zakup Gotov's acceptable provider path. Chizhik advances only if required calls are reproducible without them.

### Ozon / Ozon Fresh

Independent experimental reference:

- https://github.com/Open-Inflation/ozon_api

This repository currently has very little public documentation, so it is only evidence that investigation exists, not evidence of a usable Ozon Fresh provider path. Seller API integrations must not be confused with consumer Fresh catalog access.

### Kuper

Official API portal:

- https://docs.kuper.ru/
- integration contact: `new.partners@sbermarket.ru`
- technical support: `kuper-api@kuper.ru`

The portal explicitly lists Client apps API, but accessible public documentation still does not prove the exact read-side comparison scope required by Zakup Gotov. Issue #36 remains the access/rights gate.

### Yandex Eats Retail API

- https://yandex.ru/support/picker-app/en/ref/

The documented API is useful evidence that standardized assortment/price/availability exchange exists, but its direction is retailer-partner integration rather than a public comparison read API.

### Lenta

- https://lenta.com/i/pokupatelyam/online-sale/user-agreement/
- https://lenta.com/i/yuridicheskim-litsam

Current consumer terms are not treated as permission for a production scraping strategy. A supported B2B path remains required.

### VkusVill

- https://vkusvill.ru/legal/polzovatelskoe-soglashenie/

Current agreement restrictions make undocumented production scraping/reuse an unacceptable default assumption without a separate permission basis.

## Executable feasibility harness

M0B uses a shared provider contract before any retailer-specific implementation:

- provider access type;
- provider execution mode (`FIXTURE` or `LIVE`);
- declared capabilities (`LOCATION_RESOLUTION`, `CATALOG`, `PRODUCT_SEARCH`, `PRICE`, `AVAILABILITY`);
- provider-scoped `LocationContext`;
- normalized `ProductQuery`;
- `RetailerProvider` port;
- `ProviderFeasibilityHarness` provenance checks.

The offline harness must reject a `LIVE` provider before its network-capable method can execute. Normal PR CI therefore tests fixtures only. A live probe is always an explicit separate action.

## Provider-spike acceptance test

The first executable spike for each candidate uses one explicit supported fulfillment/location context and a fixed corpus of **20 common grocery requirements**. A serious candidate then repeats the corpus against a second context where possible.

A spike passes only when it can deterministically measure and preserve:

- location/fulfillment context;
- product identity/SKU;
- price and currency;
- availability as `AVAILABLE`, `UNAVAILABLE`, or `UNKNOWN` — never invented;
- source and observation timestamp;
- parser/contract failure behavior;
- sanitized replayable fixtures with zero live-network dependency;
- corpus coverage rate;
- differences between two stores/contexts where store-specific behavior is claimed.

A single successful manual request is not provider support.

## Implementation sequence

1. Land the shared feasibility harness (`feat/m0b-provider-feasibility-harness`).
2. First retailer spike: **Pyaterochka**, but only after proving the relevant calls work without Camoufox/stealth/proxy rotation.
3. In parallel or immediately after: **Perekrestok**.
4. Use **Magnit** as the priority independent non-X5 path.
5. Evaluate **Chizhik** only under the plain-HTTP gate.
6. Research **Ozon Fresh** and **Samokat** as the second wave.
7. Keep **Kuper** official-access work active in parallel through issue #36.
8. Do not enter M1 Shopping Core until at least two acceptable provider paths satisfy the M0 exit criteria, preferably including one non-X5 provider.
