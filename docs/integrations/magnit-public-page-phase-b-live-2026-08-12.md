# Magnit public-web Phase B — final live evidence

Date: 2026-08-12  
Tracking: issue #45  
Final merged `main` SHA: `3bfadbf3ee569a561a6fc5222df9daebb21a5291`

## Decision

**Technical M0 decision: `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts.**

This decision proves a second acquisition mode and an independent non-X5 retailer path for M0 technical feasibility. It does **not** authorize recurring production collection: usage rights remain `UNRESOLVED` in issue #70, and automatic location/address → `shopCode` resolution remains open in issue #69.

## Fixed contexts and corpus

Phase B repeatedly exercised the same two explicit public store contexts used by Phase A:

- `shopCode=139147`;
- `shopCode=773577`.

The fixed 20-requirement corpus is recorded in [`magnit-phase-b.md`](magnit-phase-b.md). Ordinary PR/main CI never runs the live corpus; the live gate is owner/issue/command-gated and uses ordinary public HTTP only.

## Evidence progression

### 1. Initial Phase B live run

Run `31539718199`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=19 second_http_2xx=19 first_usable=0 second_usable=0 stable_identity=19 known_availability=6 promo_observations=0 failed_count=20
```

Interpretation: the public path and expected product identity were viable for 19/20 candidates, but the Phase B parser localized current price too narrowly.

### 2. SKU-bound current-price fallback

After PR #63, run `31540422755`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=19 second_http_2xx=19 first_usable=19 second_usable=19 stable_identity=19 known_availability=6 promo_observations=0 failed_count=1
```

Current-price coverage recovered without weakening rendered SKU identity. Embedded public-page evidence may fill current price only for an already-rendered expected SKU.

### 3. Sanitized failed-requirement diagnosis

After PR #64, run `31541050845`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=19 second_http_2xx=19 first_usable=19 second_usable=19 stable_identity=19 known_availability=6 promo_observations=0 failed_count=1 failed_requirements=eggs
```

Only the approved abstract taxonomy name was emitted; no SKU, URL, price, response body or location was persisted.

### 4. Full 20/20 corpus coverage

After PR #65 refreshed the stale `eggs` public candidate, run `31541623826`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=0 failed_count=0 failed_requirements=
```

Both explicit contexts now had full HTTP, expected-SKU and current-price coverage with stable identity across all 20 requirements.

### 5. Promo-shape investigation

A broad near-SKU diagnostic initially reported a marker on all 40 observations but no second nearby RUB-price candidate:

Run `31542338546`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=0 near_sku_multi_price=0 near_sku_promo_marker=40 failed_count=0 failed_requirements=
```

Because a broad window could be contaminated by neighboring/template content, this result was not used to set `promo=true`.

PR #67 then bound the diagnostic to the closest selected RUB-price → expected-SKU pair and added a contaminated neighboring-SKU negative fixture.

Run `31543089595`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=0 near_sku_multi_price=0 near_sku_promo_marker=40 price_bound_promo_marker=40 failed_count=0 failed_requirements=
```

This proved the marker belongs to the selected SKU-price evidence in the tested public shape. It still did not prove a second regular/old price.

### 6. Final promo-status semantics

PR #68 separated promo status from regular-price availability. A price-bound promo marker may set `promo=true`, while `regularPrice` remains empty unless a second higher supported price is actually present.

Final run `31544035409` on merged `main` SHA `3bfadbf3ee569a561a6fc5222df9daebb21a5291`:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=40 near_sku_multi_price=0 near_sku_promo_marker=40 price_bound_promo_marker=40 failed_count=0 failed_requirements=
```

Result: **technical Phase B PASS**.

## Final semantic boundary

The accepted feasibility path proves:

- explicit public store context through `shopCode`;
- expected SKU/product identity;
- current RUB price for the fixed corpus;
- promo/special-price status when a marker is bound to the selected SKU-price evidence;
- explicit `AVAILABLE` / `UNAVAILABLE` only when page semantics prove stock state;
- `UNKNOWN` availability otherwise;
- deterministic sanitized parser fixtures and fail-closed regressions;
- repeated ordinary public HTTP access without login, Cookie, Authorization, partner keys, browser automation, CAPTCHA handling, proxy rotation, fingerprint evasion or retry/evasion behavior.

It deliberately does **not** invent:

- a regular/old price when no second supported price exists;
- availability from catalog/product presence alone;
- address/location → `shopCode` resolution;
- legal/contractual permission for recurring production collection.

## Freshness

The public page does not provide a trusted provider-side observation timestamp in this feasibility path. The acquisition timestamp therefore represents **when Zakup Gotov observed the page**, not when Magnit last changed the underlying price or promotion. M1 freshness UX must expose that limitation.

## M0 scorecard

| Criterion | Result |
|---|---|
| Explicit context reproducibility | PASS — same two public `shopCode` contexts repeatedly exercised |
| Fixed corpus coverage | PASS — 20/20 in both contexts |
| SKU stability | PASS — 20/20 stable identity |
| Current price | PASS — 20/20 in both contexts |
| Promo semantics | PASS — 40/40 price-bound marker observations in final run |
| Regular/old price | PARTIAL — only when a second supported price is actually present; none proven in the final two contexts |
| Availability | PASS with limitation — 6/40 explicit in final run, remainder `UNKNOWN` |
| Deterministic fixtures | PASS — sanitized positive/negative parser fixtures |
| Public-path stability | PASS for M0 explicit-context feasibility across repeated live runs |
| Usage rights | `UNRESOLVED` — production blocker #70 |
| Automatic location resolution | OPEN — product/production blocker #69 |

## Resulting status

**`AVAILABLE_PUBLIC_WEB` for explicit-store-context M0 feasibility.**

Production recurring acquisition must remain disabled until issue #70 reaches an authoritative `ACCEPTABLE` decision. Product location → Magnit store resolution must remain explicit/manual or unavailable until issue #69 is resolved.
