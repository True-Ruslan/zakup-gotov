# M1 Deterministic Product Matching Implementation Plan

Status: **COMPLETE — final marker head must satisfy branch protection before merge**

**Goal:** Preserve human product labels through the validated provider/snapshot pipeline and add a conservative deterministic exact/normalized matcher for one retailer + fulfillment context.

**Architecture:** `ObservedOffer` remains the fail-closed provider trust boundary and requires observed `productName`; `OfferSnapshot` preserves that validated value. The `matching` package owns matching normalization and decision logic. Exact text outranks narrowly normalized text; ambiguity is preserved rather than broken by price, availability, freshness, provider priority or SKU ordering.

**Design:** [`../specs/2026-08-12-m1-deterministic-matching-design.md`](../specs/2026-08-12-m1-deterministic-matching-design.md)

## Global constraints

- TDD is mandatory.
- Ordinary CI makes no live retailer requests.
- Product labels originate from observed provider evidence; no query/SKU-derived synthetic label.
- Matching normalization stays inside `matching`.
- Match operations are scoped to exactly one retailer and one fulfillment context.
- No fuzzy/edit-distance, substring, stemming, token reordering, synonyms, transliteration, embeddings or LLM matching.
- Price, availability, freshness, acquisition mode and SKU ordering never resolve semantic ambiguity.
- No persistence, REST API, UI, canonical catalog identity or basket optimization in this slice.

## Task 1 — preserve product labels — COMPLETE

- [x] Write RED product-label preservation test.
- [x] Confirm clean `testCompile` RED.
- [x] Require nonblank `ObservedOffer.productName` with boundary `strip()` only.
- [x] Preserve product name into both `OfferSnapshot` factories.
- [x] Migrate all Java fixture constructors without adding a compatibility escape hatch.
- [x] Run full Maven `verify` GREEN.

Evidence:

- RED head `664163cbd1f4fcd2e830680b9a6688a26e014d7f` produced exactly six compile errors from the absent product-name constructor/accessor contract; unrelated tests remained intact.
- GREEN head `8eba156d4b49e7f063c2c6fd808b3b8820329c28` introduced the strict label contract and migrated existing fixtures; full Maven `verify` passed.
- Browser bridge already exposed `productName`, so no new browser permission/acquisition behavior was required.

## Task 2 — deterministic matching-only text normalization — COMPLETE

- [x] Write RED normalization tests.
- [x] Confirm RED only on absent `MatchTextNormalizer`.
- [x] Implement package-local NFKC/case/`ё→е`/separator normalizer.
- [x] Prove synonyms, stemming and token reordering are not introduced.
- [x] Run full Maven `verify` GREEN.

Evidence:

- RED head `8212e03e4d1640795b13e1d198e819447b52eaa8` produced 12 compile errors, all from the absent normalizer.
- GREEN head `346cb4d2c3ae3a2e5734bffcf24dbc97ac50013f` implemented only the approved deterministic normalization; full Maven `verify` passed.

## Task 3 — scoped exact/normalized matcher — COMPLETE

- [x] Write RED matcher/result-model tests.
- [x] Confirm RED only on absent matching types.
- [x] Implement `MatchScope` retailer + fulfillment-context boundary.
- [x] Implement explicit status/strength/reason result invariants.
- [x] Implement exact-before-normalized matching.
- [x] Preserve input order and immutable candidate evidence.
- [x] Reject cross-retailer/context candidates fail-closed.
- [x] Keep semantic ambiguity independent from price/availability/freshness/source mode/SKU.
- [x] Run full Maven `verify` GREEN.

Evidence:

- RED head `dc96540025d495979ad4e160c35a08fa5bc83600` produced 31 compile errors, all for the absent matcher/result/scope contract; the completed normalizer compiled.
- GREEN head `cb74528f44822f744cce91a04429d17a7ac56d75` added the deterministic matcher and structural result invariants; full Maven `verify` passed.

## Task 4 — architecture, durable docs and shipping — COMPLETE EXCEPT MARKER GATE/MERGE

- [x] Add ArchUnit rule: production provider/shopping/retailer must not depend on matching.
- [x] Run full Maven `verify` with architecture rule.
- [x] Synchronize `PROJECT_STATE.md`, `ROADMAP.md`, `CHANGELOG.md` and this plan.
- [x] Run full exact-head repository CI/security gate.
- [x] Perform read-only Change Review.
- [x] Record final shipping evidence in this plan.
- [ ] Require the docs-only marker head to pass branch-protection checks again.
- [ ] Mark PR ready and squash merge with expected-head SHA guard.

Architecture evidence:

- Head `a7a7a1ce153c4c429a0ec331346040d3ecd5f2e4` added `MatchingBoundaryArchitectureTest`; full Maven `verify` passed.

Shipping evidence:

- Docs-synchronized exact head `9fb998086b49f9760b57febfe33caefe1a0b049c` passed API CI, Contract CI, Web CI + Web E2E, CodeQL Java + JavaScript/TypeScript, Dependency Review, Retailer Bridge CI, Container Security API + Web, Release Bundle CI and Release Contract CI.
- Read-only Change Review on that implementation found no P0/P1/P2 blockers: product labels are observed evidence rather than query/SKU synthesis; matching normalization remains package-local; cross-retailer/context mixing fails closed; no price/availability/freshness/source-mode/SKU semantic tie-break exists; no fuzzy/AI behavior is hidden in the baseline; upstream module direction is protected by ArchUnit.
- This marker commit changes only historical shipping evidence. Its exact head must still pass branch protection before merge.

## Delivered behavior

- `ObservedOffer` requires a truthful nonblank observed product name and has no label-less compatibility constructor.
- `OfferSnapshot` preserves the validated product label exactly.
- Matching normalization is package-local and deterministic: NFKC, lowercase via `Locale.ROOT`, `ё → е`, non-letter/digit separators collapsed to spaces.
- Baseline semantics do not include aliases/synonyms, stemming, token reordering, substring/fuzzy/edit-distance matching, transliteration, embeddings or LLMs.
- `MatchScope` isolates one retailer + fulfillment context and rejects foreign candidates.
- Exact text always outranks normalized text.
- One candidate produces `MATCHED`; multiple equivalent candidates produce `AMBIGUOUS`; no candidates produce `UNMATCHED`.
- Result cardinality/strength/reason combinations are validated at construction.
- Price, availability, freshness, acquisition mode and SKU ordering never silently choose among semantically equivalent candidates.
- Upstream provider/shopping/retailer production modules cannot depend on matching.

## Next after merge

M1 moves to **complete single-store basket comparison**, beginning with deterministic package/quantity selection and explicit complete/incomplete basket semantics. Delivery fees/minimum-order rules remain out until supported evidence exists.
