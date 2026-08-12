# M1 Single-Store Basket Comparison Design

Status: **APPROVED FOR IMPLEMENTATION** — roadmap-aligned design, 2026-08-12.

## Goal

Build the first deterministic single-store basket quote for one retailer + fulfillment context using the completed shopping-list, offer-snapshot and deterministic matching foundations.

The quote must answer honestly whether the entire requested list can be fulfilled, what package count is needed when package quantity is known, and what the selected item cost is. It must never present an incomplete or uncertain basket as a confirmed complete cheapest basket.

## Critical prerequisite: package quantity is separate evidence

A shopping item already carries a canonical required `Quantity`, while `OfferSnapshot` currently carries SKU, product name, price, availability, provenance and freshness but **not package size/quantity**. Browser observations also do not expose structured package quantity.

Therefore the baseline must not assume one matched SKU satisfies one shopping requirement and must not parse package size from `productName`.

Selected design:

- package quantity is explicit evidence bound to an `OfferSnapshotId` in the basket layer;
- a binding contains a validated canonical shopping `Quantity` describing one purchasable package;
- no binding means package quantity is **unknown** for this quote;
- unknown package quantity is a visible incomplete-item reason, not a guessed 1-piece package;
- future provider/catalog work may populate this evidence from structured supported source data without changing basket math.

Rejected alternatives:

1. **Infer package size from product name** — rejected because names are presentation text and parsing would create hidden unreliable semantics.
2. **Assume one matched SKU/package per requirement** — rejected because 750 g milk and a 500 g package require two packages, while 7 eggs and a 6-piece pack also require two.
3. **Force package quantity into every provider observation now** — rejected because current accepted providers do not all expose structured package quantity and that would either fabricate values or block already-valid price/availability observations.

## Package quantity evidence

New basket-layer types:

- `PackageQuantityBinding(OfferSnapshotId snapshotId, Quantity packageQuantity)`
- `PackageQuantitySet`

Rules:

- only known package quantities are stored;
- `Quantity` already canonicalizes kg→g and l→ml and guarantees positive amounts;
- duplicate snapshot bindings fail closed;
- the set is immutable and preserves insertion order;
- `quantityFor(snapshotId)` returns empty when no trusted package quantity is known.

Binding package evidence to the snapshot identity preserves the price/provenance/freshness context of the product observation without making provider ingestion depend on shopping/basket types.

## Basket scope

A quote operates inside one existing `MatchScope`:

- one `RetailerId`;
- one nonblank fulfillment context.

All candidate offer snapshots are passed to `DeterministicProductMatcher`, which already fails closed on cross-retailer/context candidates.

## Per-item resolution

For every `ShoppingItem`, the planner first invokes the deterministic matcher.

Resolution statuses:

- `FULFILLED` — exactly one semantic match, package quantity known and compatible, availability `AVAILABLE`;
- `AVAILABILITY_UNKNOWN` — same selection math succeeds but availability is `UNKNOWN`;
- `UNMATCHED` — matcher returned `UNMATCHED`;
- `AMBIGUOUS` — matcher returned `AMBIGUOUS`;
- `UNAVAILABLE` — selected snapshot availability is explicitly `UNAVAILABLE`;
- `PACKAGE_QUANTITY_UNKNOWN` — matched snapshot has no known package-quantity evidence;
- `QUANTITY_UNIT_MISMATCH` — required and package canonical units differ.

Order of fail-closed decisions for a matched candidate:

1. explicit `UNAVAILABLE` wins immediately;
2. package quantity must be known;
3. canonical units must match;
4. package count and line total can then be calculated;
5. availability `UNKNOWN` yields an uncertain resolution instead of confirmed fulfillment.

## Deterministic package selection math

For compatible positive canonical quantities:

`packageCount = ceil(requiredAmount / packageAmount)`

`providedAmount = packageAmount * packageCount`

`lineTotal = snapshot.price * packageCount`

Rules:

- package count is an exact positive `BigInteger`, avoiding arbitrary integer overflow limits;
- provided quantity is a canonical `Quantity` in the same unit;
- no fractional package purchase;
- oversupply is allowed only as the deterministic consequence of whole-package rounding;
- no substitute/package-size optimization across multiple candidate SKUs yet because semantic ambiguity remains explicit in the matching baseline.

## Selection and result types

New basket types:

- `BasketItemResolutionStatus`
- `PackageSelection`
- `BasketItemResolution`
- `BasketQuoteStatus`
- `BasketTotal`
- `SingleStoreBasketQuote`
- `SingleStoreBasketPlanner`

`PackageSelection` contains:

- selected `OfferSnapshot`;
- known package `Quantity`;
- positive integral package count;
- provided `Quantity`;
- line total.

`BasketItemResolution` contains the original `ShoppingItem`, `ProductMatchResult`, status and optional selection.

Structural invariants:

- `FULFILLED` / `AVAILABILITY_UNKNOWN` require matcher status `MATCHED` and a package selection;
- all other statuses have no package selection;
- `UNMATCHED` / `AMBIGUOUS` correspond to the same matcher status;
- matched failure statuses (`UNAVAILABLE`, package unknown, unit mismatch) require matcher status `MATCHED`.

## Basket-level status

`BasketQuoteStatus`:

- `COMPLETE` — every item is `FULFILLED`;
- `UNCERTAIN` — every item has a selection, at least one item is `AVAILABILITY_UNKNOWN`, and none are incomplete;
- `INCOMPLETE` — at least one item is unmatched, ambiguous, unavailable, package-unknown or unit-incompatible.

Total semantics:

- `COMPLETE` and `UNCERTAIN` quotes contain a `BasketTotal` because every item has a concrete package selection;
- `INCOMPLETE` quotes have **no basket total**, preventing partial totals from being mistaken for complete-basket prices;
- all selected lines must use one currency code; mixed currency fails closed as invalid comparison input;
- empty shopping lists are rejected because they have no meaningful retailer quote/currency context.

`UNCERTAIN` is deliberately not `COMPLETE`: a price can be calculated while stock certainty remains unknown.

## Ranking boundary

This slice produces one single-store quote. It does not yet rank several retailer quotes. Future comparison may rank only confirmed `COMPLETE` quotes by total; `UNCERTAIN` and `INCOMPLETE` must remain visibly separate unless product policy explicitly defines otherwise.

## Testing strategy

All tests are fixture-only and TDD.

### Package evidence tests

- known package quantity lookup;
- kg/l canonicalization inherited from `Quantity`;
- absent evidence returns empty;
- duplicate snapshot ID rejected;
- immutable binding list.

### Planner tests

- exact quantity → one package;
- ceiling selection (750 g / 500 g = 2);
- canonical conversion (1 kg / 400 g = 3);
- piece packs (7 / 6 = 2);
- correct provided quantity and line total;
- multiple shopping items preserve order;
- all available → `COMPLETE` with total;
- one unknown availability → `UNCERTAIN` with total;
- unavailable → `INCOMPLETE`, no total;
- unmatched/ambiguous → `INCOMPLETE`, no total;
- package evidence absent → `INCOMPLETE`, no total;
- unit mismatch → `INCOMPLETE`, no total;
- mixed currencies among otherwise selected items fail closed;
- empty list rejected;
- result lists immutable;
- matching scope behavior remains fail closed.

Architecture verification should prevent shopping/provider/matching/retailer production packages from depending on basket.

## Non-goals

This slice does not implement:

- parsing package size from names;
- structured package extraction in retailer adapters;
- synonym/fuzzy/AI matching;
- substitute selection among ambiguous semantic matches;
- multi-store optimization;
- delivery/service fees;
- minimum orders;
- loyalty prices;
- persistence;
- REST/UI;
- retailer ranking;
- live retailer access changes.

## Exit criteria

The slice is complete when:

- known/unknown package quantity evidence is explicit;
- whole-package selection is deterministic and unit-safe;
- complete/uncertain/incomplete basket states are explicit;
- incomplete baskets never expose a misleading complete total;
- all behavior passes RED→GREEN Maven verification;
- upstream architecture direction is protected;
- durable state/roadmap/changelog are synchronized;
- full exact-head repository CI/security gate passes;
- read-only Change Review has no blocking findings;
- squash merge lands in `main`.
