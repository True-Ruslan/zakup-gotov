# Magnit Phase B — fixed-corpus public-web validation

Updated: 2026-08-12
Status: `IMPLEMENTATION_READY_LIVE_PENDING`
Tracking: issue #45 / PR #62

## Purpose

Turn the successful Magnit Phase A public-page hypothesis into reproducible M0 evidence across the approved fixed grocery corpus without promoting spike code into a production provider prematurely.

Phase B must determine whether ordinary public product pages are sufficiently stable to count as an accepted non-X5 acquisition path and as a second acquisition mode alongside the browser bridge.

## Fixed store contexts

The same two explicit contexts proven in Phase A are retained:

- `shopCode=139147`;
- `shopCode=773577`.

Location/address → `shopCode` resolution remains a separate unresolved capability and is not inferred from these fixed contexts.

## Fixed 20-item corpus

The corpus is the repository-wide M0B corpus from `docs/superpowers/plans/2026-08-10-m0b-provider-spikes.md`.

| Requirement | Public product candidate | SKU/article |
|---|---|---:|
| milk | Село Зеленое UHT milk | `1000013732` |
| eggs | C1 eggs, 15 pcs | `1000135280` |
| bread | Borodinsky bread | `1000134831` |
| bananas | Bananas | `9072651501` |
| potatoes | Potatoes | `9072651210` |
| onions | Bulb onions | `9072651204` |
| tomatoes | Tomatoes | `3412070012` |
| cucumbers | Smooth cucumbers | `3412110001` |
| chicken | Chilled chicken breast | `1000233459` |
| beef/mince | EatMeat beef mince | `1000289907` |
| rice | Round rice, 800 g | `3152910003` |
| buckwheat | Buckwheat, 800 g | `3152910002` |
| pasta | Magnit spaghetti, 500 g | `1000166929` |
| sunflower oil | Refined sunflower oil, 900 ml | `1000029331` |
| butter | Magnit traditional butter 82.5% | `1855599922` |
| cheese | Landkaas Gouda | `1000500641` |
| kefir | Smetanin kefir | `1000330180` |
| sugar | White granulated sugar, 1 kg | `3133780401` |
| salt | Table salt, 1 kg | `3367460002` |
| tea | Beseda black tea, 100 bags | `1000534756` |

These are fixed investigation candidates, not a claim that all 20 are currently available in both stores. Phase B measures that fact rather than assuming it.

## Deterministic parser contract

`MagnitCorpusProbeTest` and sanitized semantic fixtures establish these fail-closed rules before the live corpus runs:

1. all 20 requirements are explicit and ordered;
2. every candidate has a unique nonblank SKU and product slug bound to that SKU;
3. product parsing requires the expected SKU after the page's primary product heading;
4. unrelated prices/stock text outside the product scope cannot create an observation;
5. the first product-scope RUB price is the current price;
6. a second higher price is treated as regular price only when a promo marker (`Финальная цена` or discount percent) is present;
7. `Нет в наличии` yields `UNAVAILABLE`;
8. explicit add-to-cart semantics yield `AVAILABLE`;
9. absent stock semantics yield `UNKNOWN` rather than guessed availability;
10. missing expected SKU yields no price and `UNKNOWN` availability.

Sanitized fixtures contain synthetic prices and no production response body, address, cookie, token, request header, or user data.

## Live request policy

The live corpus uses JDK `HttpClient` only and preserves Phase A restrictions:

- ordinary public `https://magnit.ru/product/...` pages;
- fixed connect/request timeouts;
- transparent Zakup Gotov User-Agent;
- no Cookie/login;
- no Authorization or partner API key;
- no browser automation;
- no CAPTCHA handling;
- no proxy/IP rotation or fingerprint evasion;
- no retry/evasion loop.

Ordinary PR/main CI does not run the live corpus. The live gate is issue-#45-only and requires the exact owner command:

`/provider-probe magnit-corpus`

The workflow has only `contents: read` and `statuses: write` and publishes one finite sanitized commit-status context.

## Sanitized live metrics

The live runner makes 40 requests: 20 fixed products × 2 fixed contexts. It emits counts only:

- `total_requirements`;
- `total_requests`;
- first/second HTTP 2xx counts;
- first/second usable counts (`2xx + expected SKU + current price`);
- cross-context stable identity count;
- explicit known-availability observation count;
- promo observation count;
- failed-requirement count.

It does not emit product prices, response bodies, exact failure payloads, street addresses, cookies, tokens, or credentials.

Workflow outcomes are observation labels, not support claims:

- `observed-full` — all 20 are usable in both contexts with stable identity and zero failed requirements;
- `observed-partial` — at least one usable observation exists but full coverage is not achieved;
- `observed-unusable` — structured evidence exists but no usable observation exists;
- `invalid-evidence` / `no-evidence` — the sanitized evidence contract itself failed.

## Acceptance decision

`observed-full` is necessary strong evidence but does not by itself auto-promote Magnit. After the merged-main live run, the evidence must be reviewed against the M0 scorecard:

- location/context reproducibility;
- corpus coverage;
- SKU stability;
- current/regular/promo price semantics;
- availability semantics;
- freshness limitations;
- deterministic fixture replay;
- public-path stability;
- usage-rights status.

Only that explicit post-live decision may advance Magnit to `AVAILABLE_PUBLIC_WEB` and count it toward the independent non-X5 and second-acquisition-mode M0 exit criteria.
