# Magnit public-page Phase A — live evidence

Date: 2026-08-12 (+03:00)  
Workflow run timestamp: 2026-08-11T21:29Z  
Tracking: issue #45 / PR #46  
Merged `main` SHA: `295c82cf95ecf23aa6e5ca851a977d96d89c3f9f`  
Workflow run: `31538076809`

## Result

The issue-gated Magnit Phase A workflow exercised two ordinary public `magnit.ru` product-page requests for the same fixed product under two explicit `shopCode` contexts.

Sanitized evidence emitted by the test:

```text
MAGNIT_PHASE_A first_status=200 first_sku_evidence=true first_price_present=true second_status=200 second_sku_evidence=true second_price_present=true prices_equal=true
```

Published commit status:

```text
Provider Live Probe / Magnit / pass-same-price
```

Result: **PASS**.

## What this proves

- both explicit store contexts returned HTTP 2xx;
- the expected product article/SKU was observable in both responses;
- a RUB price was observable in both responses;
- the two parsed prices were equal in this run;
- the path required no login, Cookie, Authorization header, partner API key, browser automation, CAPTCHA handling, proxy rotation, fingerprint evasion, or retry/evasion loop;
- the live workflow ran from merged `main`, not from an unmerged feature branch.

Equal prices are an accepted Phase A outcome. Phase A requires reproducible observation of the same SKU and price evidence under both explicit store contexts; it does not require those two prices to differ on every run.

## Privacy and evidence boundary

The persisted evidence intentionally excludes:

- numeric prices;
- response bodies;
- street addresses;
- cookies, tokens, headers, or partner credentials;
- arbitrary production HTML.

The workflow publishes only HTTP status, SKU-evidence booleans, price-presence booleans, price-equality state, and a finite outcome label.

## Non-claims

This PASS does **not** yet establish:

- location/address → `shopCode` resolution;
- 20-item corpus coverage;
- stable product identity across the full target corpus;
- regular/promo price distinction across representative products;
- explicit availability semantics across representative products;
- deterministic sanitized fixture replay for Magnit;
- a production-ready Magnit provider adapter.

Therefore Magnit is currently **`PUBLIC_WEB_PHASE_A_PASS / PHASE_B_REQUIRED`**, not yet an accepted M0 retailer path.

## Next gate

Phase B must run the fixed 20-item corpus against two explicit contexts and preserve sanitized deterministic fixtures. It must measure coverage, SKU stability, current/promo price semantics where present, availability semantics, freshness limitations, and public-path stability. Only a successful Phase B decision may advance Magnit to an accepted public-web acquisition path and count it toward the independent non-X5 / second-acquisition-mode M0 exit criteria.
