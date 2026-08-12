# Magnit Structured Package Characteristics — Evidence

Date: 2026-08-12  
Scope: official public Magnit product pages; package-quantity semantics only  
Status: **technical source-specific evidence; production activation unchanged**

## Decision

Magnit's official product page surface is accepted as a candidate source of structured package quantity **only for exact supported fields inside the `Характеристики` section**:

- `Вес, кг` → canonical mass quantity;
- `Объем, л` → canonical volume quantity.

The extractor is fail-closed. It does not use product titles, slugs, categories, descriptions, URLs or arbitrary numbers as package evidence.

`Количество в упаковке` is deliberately deferred from v1 because current evidence shows it as a product option/selector and because count can coexist with another package dimension under the current single-`Quantity` model.

## Official evidence sampled

### Weight-only example

Official page:
`https://magnit.ru/product/3042670099-makarony_makfa_vitki_450g?shopCode=683800&shopType=1`

Observed characteristic:
- `Вес, кг 0.45`

Accepted deterministic interpretation:
- `450 GRAM`

### Volume-only examples

Official pages:
- `https://magnit.ru/product/1000273122-voda_aqua_minerale_pitevaya_negazirovannaya_500ml`
- `https://magnit.ru/product/1000279107-moya_tsena_voda_pitevaya_n_gaz_1_5l_pl_but_rossiya_6?shopCode=610041&shopType=1`

Observed characteristics:
- `Объем, л 0.5`
- `Объем, л 1.5`

Accepted deterministic interpretations:
- `500 MILLILITER`
- `1500 MILLILITER`

### Multi-dimensional example — fail closed

Official page:
`https://magnit.ru/product/1000548435-kaloriya_moloko_pitevoe_ultrapast_2_5_1000ml`

Observed characteristics:
- `Объем, л 1`
- `Вес, кг 1.028`

Decision:
- `AMBIGUOUS_DIMENSIONS`
- no `packageQuantity`

The implementation must not infer from the word `молоко`, the title's `1л`, category, shopping-list unit or any other context that volume is preferable to weight.

### Count evidence — deferred

Official page:
`https://magnit.ru/product/1000246228-leto_yaytsa_kurinoe_kategoriya_pervaya_10_0_65_kg_kartonnaya_upakovka_ooo_belyanka_16`

Observed page option:
- `Количество в упаковке: 10`

Decision:
- not accepted in v1;
- count-field provenance/structure and multi-dimensional domain semantics require separate evidence.

## Implementation semantics

`MagnitPackageQuantityExtractor` is a pure production parser with no HTTP or Spring wiring.

Supported result states:

- `FOUND`
- `MISSING`
- `AMBIGUOUS_DIMENSIONS`
- `CONFLICTING_VALUES`
- `INVALID_VALUE`

Only `FOUND` carries a `Quantity`.

Exact behavior is covered by deterministic tests for:

- comma/dot decimals;
- canonical kg→g and l→ml conversion;
- duplicate equal values;
- weight+volume ambiguity;
- conflicting same-dimension values;
- zero/negative/malformed values;
- title/description numbers outside `Характеристики`;
- script/style contamination;
- count-only option text.

A bridge regression proves a `FOUND` extraction can populate the accepted #81 `ObservedOffer.packageQuantity` and survive into `OfferSnapshot`; ambiguous extraction remains empty downstream.

## Access / production constraints unchanged

This evidence does **not** enable Magnit production polling.

Still open:

- #69 — provider-neutral location/address → public `shopCode` resolution;
- #70 — authoritative recurring production catalog acquisition usage-rights decision.

No new credential, browser permission, cookie, authentication, address or live CI requirement is introduced.

Ordinary CI remains deterministic and makes no live Magnit request. Existing Magnit live probes remain explicit/manual research tooling.

## Next evidence gate

After this extractor is accepted, run a deliberate/manual fixed-corpus evidence pass to measure extraction-state distribution across the existing Magnit corpus. The purpose is not to activate production but to answer:

- how often exact v1 characteristics are `FOUND`;
- how often products are multi-dimensional;
- whether conflicting/invalid values occur;
- which grocery classes remain `MISSING`;
- whether `Количество в упаковке` warrants a separate domain extension.

Only after that evidence, and subject to #69/#70, should a production Magnit adapter activation be designed.
