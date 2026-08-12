# Magnit Package Evidence Fixed Corpus — Live Result

Date: 2026-08-12  
Base `main`: `bee69a7bf84f1c2b98f20f76fe244d4bf3ade4a6`  
One-shot evidence commit: `bf129c0aae3fdd80f043bfec90eafe8c545a8f7e`  
GitHub Actions run: `31623235860`  
Mode: explicit finite research run, **not recurring production polling**

## Purpose

Measure how often the accepted #82 Magnit exact-characteristic extractor can recover structured package quantity from the same server-side public HTML surface already used by the fixed Magnit Phase B corpus.

The run used the #83 methodology:

- 20 fixed grocery products;
- two explicit shop contexts (`139147`, `773577`);
- exactly 40 public product-page requests;
- package metadata classified only for HTTP 2xx observations whose expected SKU identity was proven;
- transport/error and wrong-identity responses excluded from package-quality metrics;
- no threshold for `FOUND` was assumed before the measurement.

## Exact aggregate result

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=40 near_sku_multi_price=0 near_sku_promo_marker=40 price_bound_promo_marker=40 package_evidence_pages=40 package_found=0 package_missing=40 package_ambiguous_dimensions=0 package_conflicting_values=0 package_invalid_values=0 failed_count=0 failed_requirements=
```

The guarded JUnit evidence test completed successfully with 1 test, 0 failures, 0 errors and 0 skipped.

## Interpretation

### Transport and product identity are not the cause

Both shop contexts returned:

- 20/20 HTTP 2xx;
- 20/20 usable product observations;
- 20/20 stable product identity across the pair of contexts;
- zero failed corpus requirements.

Therefore the package result cannot be explained by broad HTTP failure, wrong-product routing or loss of the existing price/SKU surface.

### Current server-side public HTML is not sufficient for package extraction

All 40 package-evidence-eligible observations classified as:

- `FOUND`: **0**
- `MISSING`: **40**
- `AMBIGUOUS_DIMENSIONS`: 0
- `CONFLICTING_VALUES`: 0
- `INVALID_VALUE`: 0

This is a clean **NO-GO for claiming package quantity from the current server-side PUBLIC_WEB HTML path**. The exact `Вес, кг` / `Объем, л` semantics accepted in #82 remain valid as source semantics, but those labels are not present in the parser-visible HTML returned to the current Java `HttpClient` corpus for these observations.

Do not compensate by parsing quantities from product titles, slugs or descriptions.

## What this does and does not prove

It proves:

- the current raw/public server-side product-page acquisition path is still excellent for the existing SKU/current-price corpus (40/40 usable in this run);
- it does not expose any #82-supported package characteristic in the 40 eligible observations;
- package extraction therefore must not be wired into production Magnit basket evidence through this raw HTML path yet.

It does **not** prove that Magnit has no structured package metadata. Official rendered product pages have separately labeled characteristic examples, so the remaining question is where those fields originate relative to the raw server response.

## Root-cause hypotheses to test next

1. **Client-side rendering/hydration:** `Характеристики` or its values are fetched/rendered only after JavaScript executes.
2. **Embedded bootstrap/JSON data:** structured values exist in the raw response, but as machine data rather than visible `Характеристики` labels.
3. **Alternate public data request:** the browser obtains characteristics from a separate public request made during page load.
4. **Corpus-specific absence:** the fixed corpus genuinely lacks those fields while other official product pages expose them.

The next diagnostic must distinguish these hypotheses with safe aggregate markers or sanitized fixtures. It must not dump raw production HTML into the repository, parse product-name quantities, broaden browser permissions, or turn the finite evidence run into recurring polling.

## Production/access constraints unchanged

- #69 location/address → `shopCode` remains unresolved.
- #70 recurring production acquisition usage rights remain unresolved.
- production comparison evidence remains fail-closed/no-op.
- no recurring Magnit job was created.
- no browser permission or authenticated session was added.

## Decision

**Do not activate Magnit structured package evidence on the current server-side PUBLIC_WEB path.**

Proceed with a focused package-characteristics provenance investigation: raw HTML markers → embedded/bootstrap structured data → browser-rendered DOM/public request boundary. Choose a new acquisition/extraction path only after one of those surfaces is reproducible and its semantics remain exact and fail-closed.
