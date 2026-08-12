# Magnit Structured Package Characteristics — Shipping Evidence

Date: 2026-08-12  
PR: #82 `feat(magnit): extract structured package characteristics`

## Accepted scope

- pure transport-free `MagnitPackageQuantityExtractor`;
- exact `Характеристики` labels `Вес, кг` and `Объем, л` only;
- canonical kg→g / l→ml through `Quantity`;
- explicit `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES`, `INVALID_VALUE` results;
- no title/slug/category/description/script/style inference;
- exact section boundaries guarded before/after known section markers;
- `Количество в упаковке` deferred from v1;
- `FOUND` evidence proven compatible with #81 `ObservedOffer` → `OfferSnapshot` plumbing;
- ambiguous extraction remains package-unknown;
- no HTTP/Spring activation/live polling added;
- #69 and #70 unchanged.

## TDD / review hardening

RED head `0d15d18` failed API test compilation exactly because extractor/result/status types did not exist.

During review, two hardening changes were made before acceptance:

1. exact-label section-end regressions were added so supported-looking fields after `Условия хранения` / `Документы` cannot create evidence;
2. the original design coupling extractor semantics with fixed-corpus instrumentation was split. #82 accepts the semantic extractor and #81 bridge first; the next explicit/manual evidence slice instruments the corpus after these semantics are accepted.

## Exact reviewed candidate

SHA: `ccc8ff6c29ad3bd8e2b9c71396edfba0484d616b`

All required PR workflow groups passed:
- API CI — PASS
- Contract CI — PASS
- Web CI + responsive Web E2E — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- CodeQL Java + JavaScript/TypeScript — PASS
- Container Security CI — PASS
- Release Bundle CI — PASS
- Release Contract CI — PASS

Ordinary CI performed no live Magnit request.

## Read-only review

Verdict: **Looks good**

- P0: none
- P1: none
- P2: none
- P3: the `Характеристики` start boundary intentionally uses an exact normalized visible-text marker instead of a Magnit DOM-role/component selector. This avoids coupling to unstable site markup; risk is bounded by exact supported labels, known section-end markers, script/style stripping and regression coverage. Revisit only if live corpus evidence shows false section matches.

No blocking security, privacy, architecture, compatibility or production-access issue was found.

## Final gate

This commit changes documentation only. Re-run the full branch-protection workflow set on the final PR head. Squash merge only if the exact head is green.

## Post-merge next step

Instrument the existing explicit/manual Magnit fixed-corpus probe with the accepted extractor and measure extraction status distribution. This remains evidence research, not recurring production acquisition, and does not bypass #69/#70.
