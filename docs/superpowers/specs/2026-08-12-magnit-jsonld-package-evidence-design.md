# Magnit SKU-bound JSON-LD Package Evidence Design

Updated: 2026-08-12
Status: implementation direction

## Context

#82 accepted package-quantity semantics for exact Magnit weight/volume characteristics and #83 instrumented those semantics over the existing finite 20-product × 2-shop corpus. The first explicit live run was transport-healthy but the visible-text extractor reported `0 FOUND / 40 MISSING`.

Follow-up finite provenance diagnostics proved that the conclusion must be narrower: the same raw PUBLIC_WEB HTTP response contains SKU-bound JSON-LD `Product` data inside `script[type="application/ld+json"]`.

Observed examples:

- SKU `3042670099`: `Product.weight = "0.45"`, matching official `Вес, кг = 0.45`;
- SKU `1000166929`: `Product.weight = "0.5"`, matching a 500 g package;
- SKU `1000273122`: `Product.additionalProperty` contains exact `name = "Объем, л"`, `value = "0.5"`;
- SKU `1000548435`: both `weight = "1.028"` and exact `additionalProperty["Объем, л"] = "1"`, proving that multi-dimensional ambiguity must remain fail-closed;
- the investigated egg/count example exposed weight but no proven count field, so `Количество в упаковке` remains deferred.

No browser execution is required to obtain these JSON-LD nodes.

## Goal

Add a deterministic, SKU-bound extractor for package quantity from Magnit's raw JSON-LD `Product` evidence and use it in the existing finite corpus measurement.

The extractor reuses the accepted `MagnitPackageQuantityExtraction` / `MagnitPackageQuantityStatus` contract so downstream #81 snapshot/basket behavior does not change.

## Non-goals

- no production provider activation;
- no recurring polling or scheduled retailer requests;
- no location/address → `shopCode` resolution;
- no usage-rights decision;
- no browser acquisition;
- no title, slug, description, URL or product-name quantity parsing;
- no generic schema.org unit inference beyond fields specifically proven on Magnit;
- no `Количество в упаковке` support in this version.

## Accepted source boundary

Only standalone JSON bodies from HTML `<script>` elements whose `type` attribute is exactly `application/ld+json` (case-insensitive media type comparison) are considered.

Other scripts, Nuxt bootstrap JSON, JavaScript literals and rendered text remain outside this extractor. Nuxt data may be investigated later, but is unnecessary for the currently proven v1 path.

## Identity rule

Extraction requires an `expectedSku` supplied by the caller.

Only JSON-LD nodes satisfying both conditions participate:

1. `@type` is `Product`, or an array containing `Product`;
2. scalar `sku`, converted to text without presentation heuristics, equals `expectedSku` exactly after surrounding-whitespace trimming.

Foreign Product nodes are ignored. Non-Product JSON-LD nodes are ignored.

If no matching Product node exists, result is `MISSING`.

Multiple matching Product nodes are allowed only as duplicated structured evidence; their supported dimensions are aggregated under the same conflict rules below.

## Supported dimensions

### Weight

Accepted field: exact JSON-LD Product member `weight` when its value is a JSON string or JSON number.

Proven Magnit semantics: decimal kilograms.

Rules:

- parse as positive `BigDecimal`;
- construct `Quantity(amount, KILOGRAM)` so the existing canonical model converts to grams;
- object-valued schema.org QuantitativeValue forms are **not** accepted until separately proven on Magnit;
- malformed, zero or negative supported values make the extraction `INVALID_VALUE`.

### Volume

Accepted field: Product `additionalProperty` entries that are objects with exact scalar `name = "Объем, л"` and scalar string/number `value`.

Proven Magnit semantics: decimal liters.

Rules:

- parse as positive `BigDecimal`;
- construct `Quantity(amount, LITER)` so the existing canonical model converts to milliliters;
- `propertyID`, approximate labels, generic `volume`, `size`, URL/title text and other properties are ignored until separately proven;
- a recognized exact volume property with a missing/non-scalar/malformed/zero/negative value makes the extraction `INVALID_VALUE`.

## Aggregation and fail-closed status rules

Evidence is collected across all matching Product nodes.

For each supported dimension:

- equivalent canonical quantities deduplicate;
- more than one distinct canonical value in the same dimension => `CONFLICTING_VALUES`;
- any invalid recognized supported value => `INVALID_VALUE`;
- one valid weight and one valid volume, even when numerically convertible by density assumptions, => `AMBIGUOUS_DIMENSIONS`;
- exactly one valid dimension/value => `FOUND`;
- no supported valid or invalid dimension => `MISSING`.

Precedence follows the accepted #82 contract:

1. `INVALID_VALUE`;
2. `CONFLICTING_VALUES`;
3. `AMBIGUOUS_DIMENSIONS`;
4. `FOUND`;
5. `MISSING`.

No density conversion or domain guessing is permitted.

## HTML / JSON parsing safety

The implementation must not interpret arbitrary JavaScript. It extracts only text content of `application/ld+json` script elements and parses each body as JSON with Jackson.

Malformed JSON-LD scripts are ignored unless they are the only source; they do not become package evidence and cannot create a quantity. The extractor remains fail-closed rather than recovering values with regex from malformed JSON.

The HTML scanner must tolerate attribute order, quoting and additional attributes while never reading non-JSON-LD script bodies as evidence.

## Corpus integration

`MagnitCorpusProbe.parseProductPage(html, expectedSku)` already receives the full raw HTTP response and separately proves expected-SKU page identity for the existing price/availability observation.

Replace only package extraction in that observation with:

`MagnitJsonLdPackageQuantityExtractor.extract(html, expectedSku)`

Transport, request count, URLs, headers, price parsing, promo parsing and availability behavior remain unchanged.

The existing #83 package eligibility rule remains unchanged: only HTTP 2xx + expected-SKU observations enter package metrics.

## Deterministic acceptance tests

At minimum:

- exact matching Product weight `0.45` => `450 GRAM`;
- exact `additionalProperty` volume `0.5` => `500 MILLILITER`;
- matching Product with weight + volume => `AMBIGUOUS_DIMENSIONS`;
- foreign SKU Product is ignored;
- non-Product node is ignored;
- multiple matching nodes with equivalent weight deduplicate;
- conflicting matching weights => `CONFLICTING_VALUES`;
- malformed/zero/negative supported value => `INVALID_VALUE`;
- object-valued weight is not guessed;
- generic `volume`, `size`, title/name/description/URL and count-looking fields do not create evidence;
- malformed JSON-LD cannot create evidence;
- non-JSON-LD scripts cannot create evidence;
- corpus page parser projects JSON-LD package evidence without changing price/promo/availability behavior.

## Live replay gate

After deterministic tests and the normal API regression suite pass, run the same finite corpus:

- 20 fixed products;
- two explicit shop contexts;
- exactly 40 requests;
- no schedule;
- no production wiring.

Compare the new aggregate distribution against the earlier visible-text baseline (`FOUND=0`, `MISSING=40`). The measurement is evidence, not a predetermined pass-rate target.

A non-zero `FOUND` confirms practical value of the raw JSON-LD path. Any unexpected conflicts/invalid/multi-dimensional cases are retained as explicit uncertainty rather than coerced to FOUND.

## Production/access constraints

Even if corpus replay is strong:

- #69 location → public `shopCode` remains unresolved;
- #70 recurring production acquisition usage rights remain unresolved;
- production comparison evidence remains fail-closed/no-op until its own activation decision;
- ordinary CI remains live-retailer-free.

## Exit criteria

- deterministic JSON-LD extraction tests pass;
- package extraction is exact-SKU-bound and presentation-text-independent;
- existing Magnit price/promo/availability tests pass unchanged;
- full API verification passes;
- finite 40-request corpus replay is captured as aggregate evidence;
- docs distinguish visible-text failure from raw JSON-LD success/failure accurately;
- exact-head CI/security passes;
- independent review has no blocking findings.