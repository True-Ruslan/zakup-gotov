# Magnit Phase B — fixed-corpus public-web validation

Updated: 2026-08-12  
Status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context M0 feasibility**  
Tracking: issue #45  
Production follow-up: issues #69 and #70

## Purpose

Magnit Phase B turns the successful Phase A public-page hypothesis into reproducible M0 evidence across the approved fixed grocery corpus without promoting the research probe into an unrestricted production provider.

The final decision proves the ordinary public-web acquisition mode technically viable under explicit public `shopCode` contexts and satisfies both:

- the independent non-X5 retailer M0 criterion;
- the second distinct acquisition-mode M0 criterion.

Production recurring collection is a separate decision because catalog usage rights remain unresolved in #70. Automatic location/address → public `shopCode` resolution remains open in #69.

## Fixed store contexts

The same two explicit public contexts proven in Phase A were retained throughout Phase B:

- `shopCode=139147`;
- `shopCode=773577`.

Repeated selection of these explicit contexts satisfies the M0 context-reproducibility gate. This does not claim automatic location discovery.

## Fixed 20-item corpus

The corpus follows `docs/superpowers/plans/2026-08-10-m0b-provider-spikes.md`.

| Requirement | Fixed public product candidate | SKU/article |
|---|---|---:|
| milk | Село Зеленое UHT milk | `1000013732` |
| eggs | Яйцо куриное столовое СО, 10 шт | `2047000014` |
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

The original egg candidate became stale during Phase B. Sanitized diagnostics identified only the abstract requirement `eggs`, and PR #65 replaced it with the current public candidate before the final 20×2 run.

## Deterministic parser contract

The Phase B test suite and sanitized semantic fixtures enforce these fail-closed rules:

1. all 20 requirements are explicit and ordered;
2. every candidate has a unique nonblank SKU and product slug bound to that SKU;
3. rendered product identity requires the expected SKU after the page's primary product heading;
4. unrelated prices/stock/promo text cannot create an observation for a missing SKU;
5. rendered product-scope current price is preferred;
6. when rendered scope lacks current price, the already-tested SKU-bound public-page fallback may fill **current price only** for an already-rendered expected SKU;
7. a second higher supported rendered price may populate `regularPrice` when the product-scope promo semantics support it;
8. a price-bound `Финальная цена` / discount marker may preserve `promo=true` independently from regular-price availability;
9. a promo marker alone never synthesizes an old/regular price;
10. explicit `Нет в наличии` yields `UNAVAILABLE`;
11. explicit add-to-cart semantics yield `AVAILABLE`;
12. absent stock semantics yield `UNKNOWN` rather than guessed availability;
13. contaminated neighboring-SKU promo fixtures must not promote the expected product.

All deterministic fixtures are synthetic/sanitized. They contain no production response body, address, cookie, token, request header or numeric live price.

## Live request policy

The corpus probe uses JDK `HttpClient` only:

- ordinary public `https://magnit.ru/product/...` pages;
- fixed connect/request timeouts;
- transparent Zakup Gotov User-Agent;
- no Cookie/login;
- no Authorization or partner API key;
- no browser automation;
- no CAPTCHA handling;
- no proxy/IP rotation or fingerprint evasion;
- no retry/evasion loop.

Ordinary PR/main CI does not run the live corpus. The issue-#45-only live gate requires the exact owner command:

`/provider-probe magnit-corpus`

The workflow has only `contents: read` and `statuses: write` and publishes sanitized aggregate evidence.

## Live evidence progression

The full chronology is recorded in [`magnit-public-page-phase-b-live-2026-08-12.md`](magnit-public-page-phase-b-live-2026-08-12.md).

Key progression:

1. initial Phase B: 19/20 identities but 0 usable prices due to parser localization mismatch;
2. SKU-bound current-price fallback: 19/20 usable in both contexts;
3. sanitized failed requirement: `eggs`;
4. refreshed egg candidate: 20/20 HTTP + usable + stable identity;
5. broad promo diagnostic: marker 40/40, too broad to trust;
6. price-bound promo diagnostic with negative contamination fixture: marker still 40/40;
7. final promo-status semantics: full technical PASS without inventing regular price.

Final run `31544035409` on merged `main` SHA `3bfadbf3ee569a561a6fc5222df9daebb21a5291`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=40 near_sku_multi_price=0 near_sku_promo_marker=40 price_bound_promo_marker=40 failed_count=0 failed_requirements=
```

Result: **PASS**.

## Semantic interpretation

### Current price

Current RUB price is proven for all 20 requirements in both explicit contexts.

### Promo status

`promo=true` is supported by a marker bound to the same selected SKU-price evidence. The contaminated neighboring-SKU negative fixture proves that a marker from another product cannot promote the expected product.

### Regular/old price

The final two contexts exposed no second near-SKU RUB-price candidate (`near_sku_multi_price=0`). Therefore the path does **not** invent a regular/old price. `regularPrice` remains empty unless a second supported price is explicitly present.

### Availability

Final run exposed explicit known availability on 6/40 observations. The remaining observations intentionally remain `UNKNOWN`. Product-page/catalog presence is not treated as proof of stock.

### Freshness

The public-page path does not expose a trusted provider-side observation timestamp. The stored/normalized observation time represents when Zakup Gotov observed the public page, not when Magnit last changed the source price or promotion.

## M0 scorecard

| Criterion | Result |
|---|---|
| Explicit context reproducibility | **PASS** |
| Fixed 20-item corpus | **PASS — 20/20 in both contexts** |
| SKU stability | **PASS — 20/20** |
| Current price | **PASS — 20/20 in both contexts** |
| Promo semantics | **PASS — 40/40 price-bound marker in final run** |
| Regular/old price | **Partial by evidence** — only when a second supported price exists; none proven in final contexts |
| Availability | **PASS with limitation** — explicit where present, otherwise `UNKNOWN` |
| Deterministic fixtures | **PASS** |
| Public-path stability | **PASS for explicit-context M0 feasibility** |
| Usage-rights status | **`UNRESOLVED`** — #70 |
| Automatic location resolution | **OPEN** — #69 |

## Decision

**Magnit advances to `AVAILABLE_PUBLIC_WEB` for explicit-store-context M0 feasibility.**

This accepted technical path counts as:

- the independent non-X5 M0 retailer path;
- the public-web acquisition mode, distinct from the X5 browser bridge.

This status is deliberately narrower than `production-ready`:

- recurring automated production acquisition remains disabled until #70 reaches an authoritative `ACCEPTABLE` decision;
- automatic location/address → `shopCode` discovery is not claimed until #69 is resolved;
- regular/old price remains absent unless explicitly supported;
- availability remains `UNKNOWN` where stock semantics are absent.

With these limitations explicit, Magnit Phase B closes the remaining technical M0 connectivity gates and supports the recorded M0 → M1 GO decision.
