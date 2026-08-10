# Retailer Feasibility Matrix

Updated: 2026-08-10
Status: M0B discovery evidence — **no retailer/provider is supported yet**

This document records technical and usage-rights evidence for candidate retailer data paths. It is an engineering feasibility record, not legal advice.

A candidate may advance to a provider spike only when there is a plausible path to all of the following without relying on prohibited or fragile access:

1. resolve a retailer/store/fulfillment context from a user location;
2. discover or search a useful grocery catalog;
3. obtain SKU identity plus current price and availability;
4. preserve source and observation/freshness metadata;
5. operate within documented authentication/rate-limit constraints;
6. use the data for Zakup Gotov's product purpose under an acceptable permission/contract basis;
7. retain sanitized recorded fixtures for deterministic parser/contract tests;
8. keep live retailer calls out of ordinary deterministic CI.

## Decision labels

- `PROMISING_CONTACT_REQUIRED` — official integration surface exists, but access/scope/usage rights still need confirmation.
- `PARTNER_SIDE_ONLY` — official API exists, but its documented direction is for a retailer/merchant partner rather than a read-side comparison client.
- `BLOCKED_WITHOUT_AGREEMENT` — public consumer data exists, but current consumer terms do not provide an acceptable basis for production scraping/reuse.
- `NO_PUBLIC_CLIENT_API_FOUND` — current primary-source research found commercial/partner surfaces, but no documented client catalog API suitable for the product.

## Current matrix

| Candidate | Official integration evidence | Location/catalog/price evidence | Usage-rights signal | Current decision | Next proof required |
|---|---|---|---|---|---|
| **Kuper** | Official API portal exposes Merchant Service API, Fulfillment API, **Client apps API**, Other API, integration contact and technical support. | Exact Client apps API catalog/search/store semantics are not yet proven from accessible documentation. | Official integration contact exists; commercial access terms still unknown. | `PROMISING_CONTACT_REQUIRED` | Confirm read-side Client apps API scope, auth, store/location resolution, product search, price, availability, freshness, rate limits, sandbox and fixture/caching rights. |
| **Yandex Eats Retail API** | Official Retail API documentation exists. | Documentation includes nomenclature composition, availability, product prices/promotions and store-related methods. | Documented architecture has Yandex/Yango acting as the client of a **partner retailer's POS/system**. | `PARTNER_SIDE_ONLY` | Obtain a separate documented partner/product path that explicitly lets Zakup Gotov read consumer-facing retailer catalog data; do not misuse the merchant-side API. |
| **Lenta** | Consumer web/app and business partnership surfaces exist. | Consumer agreement states the nearest store is selected from the delivery address; catalog/order behavior is therefore fulfillment-context dependent. | Consumer agreement defines the user as a physical person ordering for personal/family/home needs unrelated to entrepreneurship and prohibits technical bypass of unavailable/limited functions. | `BLOCKED_WITHOUT_AGREEMENT` | Obtain explicit B2B/partner permission and a supported data-access mechanism before any production adapter or automated scraping spike. |
| **VkusVill** | Consumer web/app and VkusVill Business exist. | Consumer services provide product purchasing/delivery flows; exact reusable client API is not documented publicly. | Agreement limits use to the service's direct functional purpose, prohibits use in one's own commercial purposes and unpermitted use of service IP. | `BLOCKED_WITHOUT_AGREEMENT` | Obtain explicit partner/API permission for catalog/price/availability reuse before any production adapter or automated scraping spike. |
| **Magnit** | Magnit operates its own grocery delivery and documented integrations with aggregator partners. | Public materials confirm online catalog/delivery behavior and partner delivery integrations, but no suitable public read-side grocery catalog API was found in this pass. | Partner/integration relationship is plausible, public client-data rights are not established. | `NO_PUBLIC_CLIENT_API_FOUND` | Ask e-commerce/partnership team whether a documented catalog/store/price/availability API is available to comparison/affiliate partners. |
| **X5 / Pyaterochka / Perekrestok** | X5 publishes partner channels and operates retailer digital/e-commerce integrations. | No suitable documented public read-side consumer catalog API was found in this pass. | Partner path is plausible; public client-data rights are not established. | `NO_PUBLIC_CLIENT_API_FOUND` | Ask X5 partnership/e-commerce team for a supported location-aware catalog/price/availability data path and fixture/test permissions. |

## Primary-source evidence

### Kuper

- API portal: https://docs.kuper.ru/
- Stores API is present under the merchant-service documentation: https://docs.kuper.ru/api-products/merchant-service/stores-api
- Integration contact published by the API portal: `new.partners@sbermarket.ru`
- Technical support published by the API portal: `kuper-api@kuper.ru`

The portal's existence is evidence of a supported integration program. It is **not** yet evidence that Zakup Gotov can use Client apps API for read-side catalog comparison; access and permitted use must be confirmed before an adapter spike.

### Yandex Eats Retail API

- Official reference: https://yandex.ru/support/picker-app/en/ref/

The reference describes integration with a partner POS/system and explicitly documents Yandex/Yango as the client for PULL methods. It is useful evidence that standardized product/availability/price exchange exists in the ecosystem, but it is not currently a read API for Zakup Gotov.

### Lenta

- Consumer agreement: https://lenta.com/i/pokupatelyam/online-sale/user-agreement/
- Business partnership entry point: https://lenta.com/i/yuridicheskim-litsam

The agreement states that orders are assembled in the store nearest the selected delivery address and defines consumer use around personal/family/home needs unrelated to entrepreneurship. That makes consumer-surface scraping an unacceptable production assumption without a separate agreement.

### VkusVill

- Consumer agreement: https://vkusvill.ru/legal/polzovatelskoe-soglashenie/

Relevant restrictions are in sections 5 and 10: direct functional-purpose use, no use by an unprovided method, no own commercial-purpose use, and no unpermitted use of service intellectual property.

### Magnit

- Store/e-commerce formats: https://www.magnit.com/ru/about-company/store-formats/
- Example aggregator integration evidence: https://www.magnit.com/ru/media/press-releases/magnit-i-delivery-club-zapustili-ekspress-dostavku-produktov-za-30-minut/

These prove that partner integrations and online grocery delivery exist, not that a public comparison API is available.

### X5

- Partner entry point: https://www.x5.ru/ru/partners/

No suitable public client catalog API was established from the current primary-source pass.

## Provider-spike acceptance test

Once a candidate supplies an acceptable documented access path, its first executable spike must use one explicit fulfillment/location context and a fixed corpus of at least 10 common grocery requirements. The spike is acceptable only if it can record sanitized fixtures and deterministically prove:

- location/fulfillment context is reproducible;
- product identity/SKU is preserved;
- price currency/value is present;
- availability is explicit (`AVAILABLE`, `UNAVAILABLE`, or `UNKNOWN` rather than invented);
- observation/source metadata is preserved;
- parser failures are fail-closed and visible;
- fixture replays require no live network access;
- supported common-grocery match coverage can be measured against the M0 target rather than guessed.

The first normalized domain guard for these fixtures is `provider.ObservedOffer`; it rejects incomplete or context-free offers before they can enter later comparison logic.

## Immediate next actions

1. Treat **Kuper** as the highest-priority access inquiry because an official API program and Client apps API are explicitly published.
2. In parallel, request a supported read-side data path from **X5** and/or **Magnit** so M0 does not depend on a single aggregator.
3. Do not implement Lenta/VkusVill consumer scraping as a fallback while their current terms remain the only permission basis.
4. When one candidate grants suitable access, add the first provider adapter behind a narrow port using recorded fixtures first; live sandbox verification is a separate, explicit test step.
5. M0B is not complete until at least two acceptable provider paths meet the roadmap exit criteria.
