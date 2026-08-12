# Magnit SKU-bound JSON-LD package evidence — 2026-08-12

Status: implementation/live-evidence record for M1 research; **not production activation**.

## Why this investigation was necessary

The accepted #82 visible-characteristic extractor deliberately read only visible `Характеристики` text and ignored scripts. The #83 finite corpus instrumentation then ran 20 fixed products across two explicit Magnit `shopCode` contexts (40 total requests).

That first live measurement was transport-healthy and identity-stable, but visible-text package extraction produced:

- eligible package pages: 40;
- `FOUND`: 0;
- `MISSING`: 40;
- `AMBIGUOUS_DIMENSIONS`: 0;
- `CONFLICTING_VALUES`: 0;
- `INVALID_VALUE`: 0.

The correct conclusion was therefore **visible-text blind spot**, not absence of structured data in the raw response.

## Provenance finding

Finite follow-up diagnostics inspected only the already-fetched public product-page response and proved that Magnit emits SKU-bound JSON-LD `Product` objects in `<script type="application/ld+json">`.

Observed source semantics:

| Evidence | Proven example | Accepted v1 meaning |
|---|---|---|
| `Product.sku` | `3042670099`, `1000166929`, etc. | exact product identity boundary |
| scalar `Product.weight` | `0.45`, `0.5` | kilograms |
| exact `additionalProperty.name = "Объем, л"` with scalar `value` | water SKU `1000273122`, value `0.5` | liters |
| both weight and exact volume | milk SKU `1000548435` | two dimensions exist; do **not** choose one |

The investigated count example did not expose a separately proven count field in JSON-LD. `Количество в упаковке` therefore remains outside v1.

The extractor does not use product title, slug, name, description, URL, category, generic `size`, generic `volume`, density assumptions, or malformed-JSON recovery.

## Implemented extraction contract

`MagnitJsonLdPackageQuantityExtractor`:

1. reads only `script[type="application/ld+json"]` from the same raw HTTP response;
2. parses each script as JSON with Jackson tree model rather than executing JavaScript;
3. recursively considers only nodes whose `@type` is `Product` (or an array containing `Product`);
4. requires scalar `sku` to equal the caller's expected SKU exactly after surrounding-whitespace trimming;
5. accepts only scalar string/number `weight` as kilograms;
6. accepts only exact `additionalProperty.name="Объем, л"` with scalar string/number `value` as liters;
7. canonicalizes through the shared `Quantity` model (`kg→g`, `l→ml`);
8. deduplicates equivalent structured values;
9. returns `INVALID_VALUE`, `CONFLICTING_VALUES`, `AMBIGUOUS_DIMENSIONS`, `FOUND`, or `MISSING` fail-closed;
10. ignores malformed JSON-LD instead of attempting regex recovery.

No extra retailer request and no browser execution is introduced. The corpus parser simply projects package evidence from the raw response it already owns.

## Same-corpus live replay

After deterministic RED→GREEN tests and full API verification, the exact same finite research corpus was replayed:

- 20 fixed product requirements;
- explicit shop contexts `139147` and `773577`;
- exactly 40 HTTP requests;
- no schedule;
- no recurring polling;
- no production provider activation.

Package result:

| Metric | Visible-text baseline | SKU-bound JSON-LD |
|---|---:|---:|
| eligible pages | 40 | 40 |
| `FOUND` | 0 | **36** |
| `MISSING` | 40 | **0** |
| `AMBIGUOUS_DIMENSIONS` | 0 | **4** |
| `CONFLICTING_VALUES` | 0 | **0** |
| `INVALID_VALUE` | 0 | **0** |

Transport/identity remained healthy in the JSON-LD replay:

- first-shop HTTP 2xx: 20/20;
- second-shop HTTP 2xx: 20/20;
- first-shop usable: 20/20;
- second-shop usable: 20/20;
- stable identity: 20/20;
- failed requirements: 0.

This is a practical coverage increase from 0/40 attributable package quantities to 36/40, while the remaining four observations remain explicit uncertainty rather than guessed data.

## Ambiguity diagnostic

A separate one-shop finite diagnostic over the same 20 requirements identified the ambiguous products without storing HTML:

- `milk`, SKU `1000013732` → `AMBIGUOUS_DIMENSIONS`;
- `kefir`, SKU `1000330180` → `AMBIGUOUS_DIMENSIONS`;
- every other requirement → `FOUND`.

Because each of those two products is ambiguous in both shop contexts, they account exactly for `4/40` ambiguous observations. No same-dimension conflict or invalid structured value appeared.

Selected structured results from that diagnostic include:

- eggs `2047000014` → `700 g` mass evidence (not count evidence);
- bread `1000134831` → `450 g`;
- bananas `9072651501` → `1000 g`;
- potatoes `9072651210` → `2000 g`;
- pasta `1000166929` → `500 g`;
- sunflower oil `1000029331` → `900 ml`;
- butter `1855599922` → `180 g`;
- sugar `3133780401` → `1000 g`;
- tea `1000534756` → `200 g`.

Mass evidence for eggs does not imply package count. The basket calculator requires canonical unit compatibility, so `700 g` cannot satisfy a `PIECE` requirement.

## Interpretation

### Accepted

- The existing Magnit PUBLIC_WEB response is sufficient for high-coverage package weight/volume evidence on the measured fixed corpus.
- Browser acquisition is unnecessary for this package-evidence slice.
- Exact SKU binding prevents unrelated JSON-LD Product nodes from becoming package evidence.
- Fail-closed multi-dimensional handling is exercised by real data, not only fixtures.
- Existing single-`Quantity` basket semantics can consume the 36 `FOUND` observations when required and package units are compatible.

### Not accepted / still unresolved

- This is not evidence for universal Magnit catalog coverage outside the measured corpus.
- `Количество в упаковке` remains unproven and unsupported.
- Multi-dimensional products remain package-unknown under the current single-quantity model; no density/category heuristic is allowed.
- Automatic user location/address → public `shopCode` remains unresolved (#69).
- Recurring production acquisition usage rights remain unresolved (#70).
- The result does not enable scheduled polling or production retailer traffic.

## Security/privacy/operational boundary

The live work was finite and explicit. Durable evidence stores only aggregate status counts and sanitized requirement/SKU/status diagnostics; product-page HTML was not committed. Ordinary CI remains retailer-network-free.

Temporary one-shot evidence workflows/tests were isolated on an evidence branch and are not part of the product PR or `main`.

## Decision

**GO for shipping the SKU-bound JSON-LD extraction slice (#85), subject to exact-head CI/security and independent review.**

After #85 is accepted, the highest-leverage Magnit engineering blocker becomes #69 location → public `shopCode`. #70 remains a separate mandatory production-access decision and must not be inferred from technical feasibility.