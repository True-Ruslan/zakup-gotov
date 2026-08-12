# Magnit Structured Package Characteristics Design

Updated: 2026-08-12
Status: implementation direction, review-hardened

## Context

M1 package-evidence plumbing is accepted in #81: an `ObservedOffer` may carry optional canonical `Quantity packageQuantity`, snapshots preserve it, and basket/runtime evidence is derived from snapshots. The remaining requirement is source-specific proof that a retailer surface exposes package size as an authoritative labeled field rather than presentation text.

Magnit's official public product pages expose a `Характеристики` section with separately labeled product fields. Verified official examples on 2026-08-12 include:

- Makfa pasta: `Вес, кг 0.45` — https://magnit.ru/product/3042670099-makarony_makfa_vitki_450g?shopCode=683800&shopType=1
- Aqua Minerale water: `Объем, л 0.5` — https://magnit.ru/product/1000273122-voda_aqua_minerale_pitevaya_negazirovannaya_500ml
- Kaloriya milk: both `Объем, л 1` and `Вес, кг 1.028` — https://magnit.ru/product/1000548435-kaloriya_moloko_pitevoe_ultrapast_2_5_1000ml
- Leto eggs expose `Количество в упаковке: 10` as a product option/selector, but this has not yet been proven to be the same stable characteristic contract as `Вес, кг` / `Объем, л` — https://magnit.ru/product/1000246228-leto_yaytsa_kurinoe_kategoriya_pervaya_10_0_65_kg_kartonnaya_upakovka_ooo_belyanka_16

The source-specific extractor must therefore distinguish **named characteristics** from titles, slugs, option labels and free-form description text.

Magnit remains constrained by:

- #69 — location/address → public `shopCode` resolution not proven;
- #70 — recurring production catalog acquisition usage rights unresolved.

This slice is technical evidence/extraction only and must not activate recurring production polling.

## Goal

Implement a pure, deterministic, fail-closed Magnit package-quantity extractor that reads only supported labeled fields inside the official `Характеристики` section and returns canonical package evidence when exactly one supported physical dimension is unambiguous.

Prove that a `FOUND` extraction can enter the already accepted #81 provider/snapshot package-evidence path, without changing the existing Magnit fixed-corpus probe or production activation in the same slice.

### Why corpus instrumentation is separate

The original draft coupled extractor semantics with `MagnitCorpusProbe` instrumentation. Review split these concerns deliberately:

1. #82 establishes and reviews the semantic contract of the pure extractor;
2. the next evidence slice instruments the existing explicit/manual fixed-corpus probe and measures real distribution across products.

This avoids changing the measurement harness before the semantics being measured are accepted. Existing Magnit price/availability probes therefore remain behaviorally unchanged in #82.

## Supported v1 fields

### `Вес, кг`

- exact semantic: packaged product mass in kilograms as published in Magnit characteristics;
- convert through canonical `Quantity(..., KILOGRAM)` → grams;
- positive decimal only;
- dot and comma decimal separators accepted.

### `Объем, л`

- exact semantic: packaged product volume in liters as published in Magnit characteristics;
- convert through canonical `Quantity(..., LITER)` → milliliters;
- positive decimal only;
- dot and comma decimal separators accepted.

### Deliberately deferred: `Количество в упаковке`

Although official pages can expose this label, current evidence shows it as a variant/selector surface and not yet as the same stable characteristics contract. Supporting count would also create multi-dimensional products under the current single-`Quantity` model.

Do not accept it in v1. It requires a separate evidence decision.

## Section boundary

The extractor may inspect rendered HTML/text only to locate the exact `Характеристики` section and exact supported labels. It must not scan the entire page for arbitrary numbers.

Start marker: `Характеристики`.

End at the earliest known following section marker, when present:

- `Условия хранения`
- `Документы`
- `Отзывы`
- `Дополнительная информация`

If no end marker exists, the extractor may use the remaining visible text after `Характеристики`, but still only exact supported labels are authoritative.

Script/style contents are excluded before visible-text extraction.

## Result model

Use an explicit result rather than bare `Optional` so failed extraction remains diagnosable without guessing.

Statuses:

- `FOUND` — exactly one supported dimension has one unique valid value;
- `MISSING` — no supported characteristic exists inside the characteristics section;
- `AMBIGUOUS_DIMENSIONS` — both weight and volume are present;
- `CONFLICTING_VALUES` — one dimension contains more than one distinct valid value;
- `INVALID_VALUE` — a supported label is present but at least one associated value is missing, malformed, zero or negative.

`packageQuantity` is present only for `FOUND`.

## Fail-closed rules

1. **Never parse product title, slug, SKU, URL, description or marketing text.** A title like `Макароны 450г` alone yields `MISSING`.
2. Only exact supported labels inside `Характеристики` may create evidence.
3. Exact supported labels before the section or after a known following section marker do not create evidence.
4. Duplicate occurrences of the same label/value are harmless and deduplicated.
5. Distinct values for the same dimension are `CONFLICTING_VALUES`; choose neither.
6. Weight + volume on the same page are `AMBIGUOUS_DIMENSIONS`; do not infer that one is more appropriate from category/title/product type.
7. Invalid supported fields make the result `INVALID_VALUE`; do not silently ignore a malformed authoritative field and use another value.
8. `Количество в упаковке` is ignored in v1 and cannot create a piece quantity, even if it appears inside `Характеристики`.
9. Missing/ambiguous/conflicting/invalid extraction remains package-unknown to M1 basket logic.
10. No network call exists in the production extractor.
11. Existing live Magnit probes remain explicit/manual test tooling; ordinary CI remains deterministic and live-retailer-free.

## Architecture

Production boundary:

`io.github.trueruslan.zakupgotov.provider.magnit`

```java
public final class MagnitPackageQuantityExtractor {
    public static MagnitPackageQuantityExtraction extract(String html) { ... }
}

public record MagnitPackageQuantityExtraction(
        MagnitPackageQuantityStatus status,
        Optional<Quantity> packageQuantity) { ... }
```

The extractor is source-specific but transport-free: it takes already obtained HTML, performs no HTTP and owns only Magnit's exact characteristic semantics.

A bridge regression creates an `ObservedOffer` using the extraction result and verifies that #81 preserves it through `OfferSnapshot`. Ambiguous extraction produces an empty package quantity downstream.

Do not add a Spring bean or wire the production `ComparisonRuntimeEvidenceSource` in this slice.

Do not modify the existing fixed-corpus live measurement behavior in this slice; that is the immediate follow-up once extractor semantics are accepted.

## Deterministic evidence matrix

| Fixture | Characteristics | Expected |
|---|---|---|
| pasta | `Вес, кг 0,45` | `FOUND`, `450 GRAM` |
| water | `Объем, л 1.5` | `FOUND`, `1500 MILLILITER` |
| duplicate weight | `Вес, кг 0.45` repeated | `FOUND`, `450 GRAM` |
| milk | `Объем, л 1` + `Вес, кг 1.028` | `AMBIGUOUS_DIMENSIONS`, empty |
| conflicting | `Вес, кг 0.45` + `Вес, кг 0.50` | `CONFLICTING_VALUES`, empty |
| invalid | `Вес, кг 0` / negative / malformed | `INVALID_VALUE`, empty |
| title-only | title contains `450г`, no characteristic | `MISSING`, empty |
| outside section | supported label before/after section boundary | `MISSING`, empty |
| count-only | `Количество в упаковке 10`, no weight/volume | `MISSING`, empty in v1 |
| script/style | supported-looking text only in script/style | `MISSING`, empty |

## Security / privacy / access

- No new credentials, cookies, browser permissions or authentication.
- No exact address input or telemetry change.
- No response-body interception is added to the browser bridge.
- No recurring Magnit production fetch is enabled.
- #69 and #70 remain blocking constraints for production activation.
- The extractor cannot change retailer coverage/access status.

## Exit criteria

- deterministic tests cover the evidence matrix and section boundaries;
- exact labeled weight/volume characteristics produce canonical quantities;
- multi-dimensional/conflicting/invalid pages fail closed;
- title/slug/count-only/script/style/out-of-section text cannot produce package evidence;
- `FOUND` output is proven compatible with `ObservedOffer` → `OfferSnapshot` package evidence;
- ambiguous output is proven package-unknown downstream;
- existing Magnit live/price/availability probe behavior is unchanged;
- ordinary CI contains no live Magnit dependency;
- durable docs explicitly distinguish technical extraction proof from production activation and corpus measurement;
- exact-head CI/security gate passes and independent review has no blocking findings.

## Follow-up

After this slice ships:

1. instrument the explicit/manual Magnit fixed-corpus probe with the accepted extractor and measure `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES`, `INVALID_VALUE` distribution;
2. only if evidence quality is sufficient, design the production adapter activation path subject to #69/#70;
3. evaluate `Количество в упаковке` separately, including whether the domain should support multiple package dimensions rather than forcing one `Quantity`.
