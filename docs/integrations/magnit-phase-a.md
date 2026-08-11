# Magnit Phase A — public-page store-price feasibility

Updated: 2026-08-12
Status: `PUBLIC_WEB_PHASE_A_PASS / PHASE_B_REQUIRED`
Tracking: issue #45 / PR #46

## Purpose

Prove an independent non-X5 provider path using Magnit's ordinary public SSR pages rather than private or partner APIs.

## Research evidence

Magnit's public product/catalog pages accept `shopCode` and expose prices for a selected store context. Publicly indexed product pages for the same SKU/article `1000379971` have appeared under multiple Moscow `shopCode` values, which is sufficient evidence to test store-context price observation directly from the public page.

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

## TDD / parser evidence

PR #46 retained a deterministic regression test that binds price evidence to the expected SKU/article rather than accepting the first or nearest unrelated page price.

The failing implementation selected an unrelated footer price after the SKU because it minimized flattened-text distance. The accepted implementation prefers the nearest price preceding the SKU and uses a following price only as a fallback when no preceding price exists. The regression test was not weakened.

PR #46 was updated onto the then-current `main`, passed the complete repository CI/security gate, and was squash-merged as:

`295c82cf95ecf23aa6e5ca851a977d96d89c3f9f`

## Phase A acceptance

Each public page must return:

1. HTTP 2xx;
2. expected SKU/article evidence;
3. ruble price evidence.

Both contexts must pass. The observed prices may be equal or different; either result proves the same SKU is observable under two explicit store contexts. A difference would be additional store-specific pricing evidence but is not required for Phase A.

Location → `shopCode` resolution is deliberately **not** claimed by this slice and remains a separate capability gate.

## Real live gate — PASS

The dedicated issue-gated workflow was run from merged `main` SHA `295c82cf95ecf23aa6e5ca851a977d96d89c3f9f`.

Sanitized evidence:

```text
MAGNIT_PHASE_A first_status=200 first_sku_evidence=true first_price_present=true second_status=200 second_sku_evidence=true second_price_present=true prices_equal=true
```

Published status:

```text
Provider Live Probe / Magnit / pass-same-price
```

The Maven live test run completed with 4 tests, 0 failures, 0 errors and 0 skipped tests.

Evidence record: [`magnit-public-page-live-2026-08-12.md`](magnit-public-page-live-2026-08-12.md).

## Sanitized evidence boundary

Live evidence contains only:

- first/second HTTP status;
- SKU-evidence boolean for each page;
- price-present boolean for each page;
- whether the two parsed prices are equal.

No numeric price, street address, response body, cookie, token, hidden identifier, or arbitrary production HTML is persisted in the evidence record.

## Decision

Phase A is **PASS** and the ordinary public-web hypothesis is viable enough to enter Phase B.

Current decision: **`PUBLIC_WEB_PHASE_A_PASS / PHASE_B_REQUIRED`**.

This is intentionally not yet `AVAILABLE_PUBLIC_WEB`. Phase B must prove the fixed 20-item corpus, stable identity, representative price/promo semantics, availability semantics, deterministic sanitized fixtures, and repeatability across two explicit contexts before Magnit can count as an accepted independent non-X5 path or as the second M0 acquisition mode.

## Phase B gate

Use the fixed M0B 20-item corpus:

- milk;
- eggs;
- bread;
- bananas;
- potatoes;
- onions;
- tomatoes;
- cucumbers;
- chicken;
- beef/mince;
- rice;
- buckwheat;
- pasta;
- sunflower oil;
- butter;
- cheese;
- kefir;
- sugar;
- salt;
- tea.

For two explicit `shopCode` contexts, record aggregate coverage and sanitized deterministic fixtures. Explicitly distinguish current/promo/regular price when evidence supports it; do not infer availability from catalog presence when an explicit stock semantic is absent. Record freshness and location-resolution limitations instead of hiding them.
