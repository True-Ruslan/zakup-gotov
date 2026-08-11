# Magnit Phase A — public-page store-price feasibility

Updated: 2026-08-10
Status: `PHASE_A_IMPLEMENTATION_READY_LIVE_PENDING`
Tracking: issue #45 / PR #46

## Purpose

Prove an independent non-X5 provider path using Magnit's ordinary public SSR pages rather than private or partner APIs.

## Research evidence

Magnit's public product/catalog pages accept `shopCode` and expose prices for a selected store context. Publicly indexed product pages for the same SKU/article `1000379971` have appeared under multiple Moscow `shopCode` values with different displayed prices, which is sufficient evidence to test store-context price observation directly from the public page.

The official Magnit Market partner API is a different seller/partner surface requiring `X-Api-Key`; it is not used by this spike.

## Phase A product and contexts

Fixed public product:

- article/SKU: `1000379971`;
- slug: `1000379971-moloko_sgushchennoe_360g_zhestyanaya_banka_zao_amkk_45`.

Explicit Moscow store contexts:

- `shopCode=139147`;
- `shopCode=773577`.

The probe requests the same public product page for both contexts with `shopType=1`.

## Request policy

`MagnitPublicPageProbe` uses JDK `HttpClient` only:

- `Accept: text/html,application/xhtml+xml`;
- transparent Zakup Gotov `User-Agent`;
- fixed connect/request timeouts;
- no Cookie or login;
- no Authorization / partner API key;
- no browser automation;
- no CAPTCHA/anti-bot bypass;
- no proxy/IP rotation or fingerprint evasion;
- no retry loop.

## Phase A acceptance

Each public page must return:

1. HTTP 2xx;
2. expected SKU/article evidence;
3. ruble price evidence.

Both contexts must pass. The observed prices may be equal or different; either result proves the same SKU is observable under two explicit store contexts. A difference is additional store-specific pricing evidence.

Location → `shopCode` resolution is deliberately **not** claimed by this slice and remains a separate capability gate.

## Sanitized evidence

Live evidence contains only:

- first/second HTTP status;
- SKU-evidence boolean for each page;
- price-present boolean for each page;
- whether the two parsed prices are equal.

No numeric price, street address, response body, cookie, token, or hidden identifier is emitted.

The dedicated live workflow publishes:

`Provider Live Probe / Magnit / <outcome>`

Possible outcomes include `pass-same-price`, `pass-different-price`, `first-<status>`, `first-sku-missing`, `first-price-missing`, corresponding second-page failures, `no-evidence`, or `failed`.

Only a pass outcome permits Phase B fixture/corpus work.
