# Magnit Structured Package Characteristics Implementation Plan

> Implement with TDD. Keep live access manual/explicit and preserve #69/#70 production gates.

**Goal:** Prove and implement deterministic extraction of Magnit's exact labeled `Вес, кг` / `Объем, л` characteristics into canonical package quantity evidence without title parsing or production activation.

## Task 1 — Define extraction contract in tests — COMPLETE

Cases covered:
- weight 0,45 → 450 g;
- volume 1.5 → 1500 ml;
- duplicate identical value → FOUND;
- weight + volume → AMBIGUOUS_DIMENSIONS;
- conflicting same-dimension values → CONFLICTING_VALUES;
- invalid/zero/negative value → INVALID_VALUE;
- title-only `450г` → MISSING;
- count-only `Количество в упаковке 10` → MISSING in v1;
- script/style contamination → MISSING;
- supported labels outside the bounded characteristics section → MISSING.

RED was verified on `0d15d18`: API test compilation failed exactly because extractor/result/status types did not yet exist.

## Task 2 — Implement pure production extractor — COMPLETE

Implemented:
- `provider/magnit/MagnitPackageQuantityExtractor.java`;
- `MagnitPackageQuantityExtraction`;
- `MagnitPackageQuantityStatus`.

Rules:
- no HTTP, Spring bean or side effects;
- strip script/style before visible-text parsing;
- scope only to `Характеристики` and stop at the earliest following known section marker;
- inspect only exact `Вес, кг` / `Объем, л` labels;
- canonicalize via existing `Quantity`;
- duplicate equal values deduplicate;
- conflicting, multidimensional or invalid evidence fails closed;
- no title/category/SKU/URL heuristics.

## Task 3 — Prove #81 provider/snapshot compatibility — COMPLETE

Review changed the original draft plan here. Instead of modifying the existing fixed-corpus measurement probe in the same PR, #82 keeps semantic acceptance and corpus measurement separate.

Implemented bridge regression:
- `FOUND` extraction populates `ObservedOffer.packageQuantity`;
- `OfferSnapshot` preserves the same canonical quantity;
- ambiguous weight+volume extraction remains empty downstream.

Why the corpus probe is not modified in #82:
- its current purpose is stable price/availability feasibility measurement;
- changing its output before extractor semantics are accepted would mix measurement-harness evolution with semantic acceptance;
- the immediate next evidence slice will instrument the existing explicit/manual corpus probe with this accepted extractor and measure real status distribution.

Existing Magnit HTTP policy, URL generation, price selection, availability semantics and live-probe enablement remain unchanged.

## Task 4 — Durable evidence and shipping — IN PROGRESS

Completed:
- `docs/PROJECT_STATE.md` synchronized;
- `docs/ROADMAP.md` synchronized;
- root `CHANGELOG.md` synchronized;
- `docs/integrations/magnit-structured-package-characteristics-2026-08-12.md` records official examples, semantics and #69/#70 limits;
- spec review hardened section-boundary and extractor-vs-corpus scope.

Remaining:
1. exact-head API/Web/Contract/Retailer Bridge/CodeQL/Dependency/Container/Release gates;
2. independent read-only review;
3. docs-only shipping marker after reviewed candidate passes;
4. ready-for-review + squash merge using exact head;
5. push-triggered `main` verification.

## Immediate follow-up after #82

Instrument the explicit/manual Magnit fixed-corpus probe to report:
- `FOUND`;
- `MISSING`;
- `AMBIGUOUS_DIMENSIONS`;
- `CONFLICTING_VALUES`;
- `INVALID_VALUE`.

That follow-up remains research evidence only and must not enable recurring production polling or bypass #69/#70.
