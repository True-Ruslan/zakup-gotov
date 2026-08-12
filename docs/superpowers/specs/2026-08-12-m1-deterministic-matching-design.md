# M1 Deterministic Product Matching Design

Status: **APPROVED FOR IMPLEMENTATION** — roadmap-aligned design, 2026-08-12.

## Goal

Add the first deterministic M1 product-matching baseline that maps a `ShoppingRequirement` to retailer offer snapshots without fuzzy heuristics, AI, hidden tie-breaking, or cross-retailer/context mixing.

The baseline must be conservative and explainable: exact text first, then a narrowly normalized text form, with explicit `MATCHED`, `AMBIGUOUS`, and `UNMATCHED` outcomes.

## Context and prerequisite

The browser bridge already normalizes a human-readable `productName` for browser observations, but the Java provider trust boundary currently drops that semantic field. `ObservedOffer` and `OfferSnapshot` therefore contain SKU/price/availability/provenance but no candidate label that can be compared to user requirement text.

Matching by SKU or by the provider query would be semantically invalid: a SKU is retailer identity rather than human product meaning, and the query is not evidence that a returned product label actually matches the requirement.

Before implementing the matcher, the Java provider → snapshot pipeline must preserve a validated human-readable product name.

## Considered approaches

### 1. Introduce a full catalog/canonical-product module first

Pros:
- clean long-term separation between retailer SKUs and canonical products;
- natural future home for aliases, categories and canonical concepts.

Cons:
- creates catalog identity and persistence questions before the deterministic baseline needs them;
- expands M1 scope substantially;
- risks premature abstraction before real matching evidence exists.

Decision: **not now**. A catalog module remains a later evolution when canonical product identity is required.

### 2. Match requirements against SKU or provider query text

Pros:
- minimal model changes.

Cons:
- semantically wrong;
- can report a match without product-label evidence;
- creates false confidence and makes later migration harder.

Decision: **rejected**.

### 3. Preserve `productName` in validated observations/snapshots and match snapshots directly

Pros:
- preserves evidence already available from integrations;
- no new live acquisition or scraping behavior;
- smallest correct change;
- supports deterministic fixture-first matching immediately;
- leaves room for a future catalog/canonical-product layer.

Cons:
- matching temporarily depends directly on provider snapshot types.

Decision: **selected**.

## Provider and snapshot changes

`ObservedOffer` gains a required nonblank `productName` value.

Rules:
- provider adapters/harnesses must supply the product label they actually observed;
- whitespace at the boundaries may be normalized by the value constructor, but matching-specific lowercasing/punctuation/Unicode equivalence must not leak into provider or shopping models;
- blank/missing names fail closed;
- `OfferSnapshot` copies the validated `productName` exactly from `ObservedOffer`;
- browser bridge TypeScript schema requires no change because it already contains `productName`.

`ObservedOffer` remains the provider trust boundary. `OfferSnapshot` remains the immutable comparison record. Neither owns matching rules.

## Matching module

Create a new production package:

`io.github.trueruslan.zakupgotov.matching`

The matching layer may depend on:
- `shopping` for `ShoppingRequirement`;
- `provider` for `OfferSnapshot`;
- `retailer` for retailer scope identity.

Provider, retailer and shopping modules must not depend on matching.

Initial components:

- `MatchTextNormalizer` — deterministic matching-only text normalization;
- `MatchScope` — explicit retailer + fulfillment-context scope;
- `ProductMatchStatus` — `MATCHED`, `AMBIGUOUS`, `UNMATCHED`;
- `ProductMatchStrength` — `EXACT`, `NORMALIZED`, `NONE`;
- `ProductMatchReason` — explicit reason for the result;
- `ProductMatchResult` — immutable result including stable candidate snapshots;
- `DeterministicProductMatcher` — exact/normalized decision engine.

## Text normalization

Shopping text remains user wording. Provider product names remain observed wording. Matching owns a derived normalized representation.

The baseline normalizer will:
- apply Unicode NFKC normalization;
- trim and collapse Unicode whitespace;
- lowercase with `Locale.ROOT`;
- normalize Russian `ё` to `е`;
- treat punctuation/symbol separators as spaces and collapse the result;
- reject a normalized result that becomes blank.

The baseline will **not**:
- stem words;
- reorder tokens;
- remove brands, package sizes or qualifiers;
- expand aliases/synonyms;
- perform transliteration;
- use substring or edit-distance matching;
- use embeddings or an LLM.

These rules deliberately keep NORMALIZED equivalence narrow and auditable.

## Scope and isolation

A match operation is performed inside one explicit `MatchScope`:

- one `RetailerId`;
- one nonblank `fulfillmentContextId`.

Every candidate snapshot supplied to the matcher must belong to that exact scope. A scope mismatch is a caller/configuration error and fails closed rather than being silently filtered.

Reason: a shopping requirement should be matched independently for each retailer/context. Combining offers from different stores would turn equivalent products into false ambiguity and would make later basket comparison incorrect.

## Matching algorithm

Given one requirement and a stable candidate list in one scope:

1. Validate input and scope.
2. Compare requirement text to `productName` using exact text after only boundary whitespace normalization already present in domain values.
3. If exactly one exact candidate exists → `MATCHED / EXACT`.
4. If more than one exact candidate exists → `AMBIGUOUS / EXACT`.
5. If no exact candidate exists, normalize requirement and product names with `MatchTextNormalizer`.
6. If exactly one normalized candidate exists → `MATCHED / NORMALIZED`.
7. If more than one normalized candidate exists → `AMBIGUOUS / NORMALIZED`.
8. If none exists → `UNMATCHED / NONE`.

Exact matches always outrank normalized matches. Candidate input order is preserved in result evidence.

## No hidden tie-breakers

The matcher must not choose between semantically equivalent candidates using:
- price;
- availability;
- acquisition mode;
- provider priority;
- freshness;
- SKU lexical order.

Those properties belong to basket selection/optimization, not semantic matching. Multiple semantically equivalent candidates remain `AMBIGUOUS` until later product/package constraints can distinguish them.

## Result semantics

Suggested reasons:
- `SINGLE_EXACT_TEXT_MATCH`;
- `MULTIPLE_EXACT_TEXT_MATCHES`;
- `SINGLE_NORMALIZED_TEXT_MATCH`;
- `MULTIPLE_NORMALIZED_TEXT_MATCHES`;
- `NO_TEXT_MATCH`.

Invariants:
- `MATCHED` has exactly one candidate and strength `EXACT` or `NORMALIZED`;
- `AMBIGUOUS` has at least two candidates and strength `EXACT` or `NORMALIZED`;
- `UNMATCHED` has zero candidates and strength `NONE`;
- candidate snapshots are immutable and retain full provenance/freshness evidence.

No arbitrary floating-point confidence score is introduced in this baseline. Strength + reason provide deterministic explainability without inventing statistical meaning.

## Testing strategy

All work is TDD and fixture-only.

### Provider-label preservation tests
- nonblank observed product name is required;
- name survives `ObservedOffer → OfferSnapshot` unchanged semantically;
- existing provider/harness tests migrate without weakening provenance validation.

### Normalizer tests
- NFKC compatibility forms;
- case normalization;
- whitespace normalization;
- `ё/е` equivalence;
- punctuation-as-separator behavior;
- blank-after-normalization rejection;
- no substring/stemming/token-reorder behavior.

### Matcher tests
- one exact match;
- exact match outranks normalized alternatives;
- multiple exact matches are ambiguous;
- one normalized match;
- multiple normalized matches are ambiguous;
- unmatched result;
- stable candidate order and immutable result;
- different price/availability does not break ambiguity;
- retailer mismatch rejected;
- fulfillment-context mismatch rejected;
- null inputs rejected.

Architecture verification should prevent provider/shopping/retailer production packages from depending on matching.

## Non-goals

This slice does not implement:
- canonical catalog/product identity;
- aliases or synonym dictionaries;
- category/unit/package compatibility;
- package quantity optimization;
- fuzzy/edit-distance matching;
- AI/LLM/embedding ranking;
- persistence;
- REST/UI;
- cross-retailer winner selection;
- live retailer acquisition.

## Exit criteria

The slice is complete when:
- Java provider/snapshot pipeline preserves validated product labels;
- exact/normalized scoped matching is deterministic and explicit;
- ambiguous and unmatched states cannot be silently converted into a winner;
- full Maven verification passes after each RED→GREEN cycle;
- durable state/roadmap/changelog are synchronized;
- full exact-head repository CI/security gate passes;
- read-only Change Review has no blocking findings;
- squash merge lands in `main`.