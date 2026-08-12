# Magnit Structured Package Characteristics Implementation Plan

> Implement with TDD. Keep live access manual/explicit and preserve #69/#70 production gates.

**Goal:** Prove and implement deterministic extraction of Magnit's exact labeled `Вес, кг` / `Объем, л` characteristics into canonical package quantity evidence without title parsing or production activation.

## Task 1 — Define extraction contract in tests

Files:
- add `MagnitPackageQuantityExtractorTest`
- add deterministic HTML fixtures under `apps/api/src/test/resources/provider/magnit/package/`

Cases:
- weight 0,45 → 450 g;
- volume 1.5 → 1500 ml;
- duplicate identical value → FOUND;
- weight + volume → AMBIGUOUS_DIMENSIONS;
- conflicting same-dimension values → CONFLICTING_VALUES;
- invalid/zero value → INVALID_VALUE;
- title-only `450г` → MISSING;
- count-only `Количество в упаковке 10` → MISSING in v1.

Run focused test and verify RED because extractor/result/status types do not exist.

## Task 2 — Implement pure production extractor

Files:
- add `provider/magnit/MagnitPackageQuantityExtractor.java`
- add result/status value types as needed.

Rules:
- no HTTP, Spring bean or side effects;
- strip script/style before visible-text parsing;
- scope only to `Характеристики` and stop at the earliest following known section marker;
- inspect only exact `Вес, кг` / `Объем, л` labels;
- canonicalize via existing `Quantity`;
- duplicate equal values deduplicate;
- conflicting, multidimensional or invalid evidence fails closed;
- no title/category/SKU/URL heuristics.

Run focused test and verify GREEN.

## Task 3 — Integrate deterministic Magnit probe

Files:
- modify `MagnitCorpusProbe.PageObservation` to carry the extraction result;
- invoke extractor from `parseProductPage`;
- update `MagnitCorpusProbeTest` with deterministic package-characteristic cases.

Do not change HTTP request policy, product URL generation, price selection, availability semantics or live-probe enablement.

Run Magnit probe tests and API architecture regressions.

## Task 4 — Durable evidence and shipping

Files:
- update `docs/PROJECT_STATE.md`
- update `docs/ROADMAP.md`
- update `CHANGELOG.md`
- add a Magnit integration evidence note documenting official examples and limitations.

State explicitly:
- technical structured-characteristic extraction proven;
- no production polling enabled;
- #69/#70 unchanged;
- count-field support deferred;
- ambiguous pages stay package-unknown.

Open draft PR, require exact-head API/Web/Contract/Retailer Bridge/CodeQL/Dependency/Container/Release gates, run independent read-only review, then squash merge only if no P0/P1/P2 blockers remain. Verify push-triggered `main` CI after merge.
