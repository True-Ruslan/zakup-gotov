# M1 Single-Store Basket Comparison Implementation Plan

Status: **COMPLETE — final marker head must satisfy branch protection before merge**

**Goal:** Build a deterministic single-store basket quote for one retailer + fulfillment context, using explicit package-quantity evidence and preserving complete/uncertain/incomplete semantics.

**Architecture:** Basket depends on shopping, provider snapshots and matching. Provider/shopping/matching/retailer remain upstream and must never depend back on basket. Package quantity evidence is basket-scoped and keyed by `OfferSnapshotId`; absence means unknown. No package data is inferred from product names.

**Design:** [`../specs/2026-08-12-m1-single-store-basket-design.md`](../specs/2026-08-12-m1-single-store-basket-design.md)

## Global constraints

- TDD RED→GREEN for each behavior.
- Ordinary CI stays offline from live retailers.
- No package-size parsing from `productName` and no one-SKU-equals-one-requirement assumption.
- No semantic tie-breaking of ambiguous matches.
- `UNKNOWN` availability remains uncertainty, not availability.
- Incomplete quotes expose no aggregate total.
- No delivery/minimum-order/loyalty assumptions, persistence/API/UI or multi-store ranking.

## Task 1 — typed package-quantity evidence — COMPLETE

- [x] RED known/unknown lookup, canonical quantities, duplicate/null handling and immutability.
- [x] Implement explicit known bindings keyed by `OfferSnapshotId`.
- [x] Preserve deterministic null diagnostics before immutable copy.
- [x] Full Maven `verify` GREEN.

Evidence:

- RED `2871c4863170d62cc20f1996989b67aa76d39474`: 14 compile errors, all from absent `PackageQuantityBinding` / `PackageQuantitySet`.
- Initial GREEN attempt `1853863a7fc48c4eb229d553df1340ba80f13c2f` exposed one real boundary defect: `List.copyOf` threw an undiagnostic NPE before explicit null-element validation. 112/113 tests passed; the failing RED assertion was retained.
- Fix `40932c9ea4923f89aae7da9d99b816fd0e907b47` validates elements before immutable copy; full Maven `verify` passed with the original RED test unchanged.

Delivered:
- known package evidence only;
- absence means unknown;
- duplicate snapshot evidence rejected;
- kg/l canonicalization inherited from `Quantity`;
- stable immutable bindings.

## Task 2 — whole-package selection math — COMPLETE

- [x] RED exact package, ceiling selection, kg/g canonical selection, piece packs, totals and unit mismatch.
- [x] Implement `PackageSelection` structural invariants.
- [x] Implement package-local exact calculator using whole-package ceiling arithmetic.
- [x] Full Maven `verify` GREEN.

Evidence:

- RED `2c77ca4352dc70c6dee409d05d06d048d3492a37`: 8 compile errors, all from absent `PackageSelectionCalculator`/selection contract.
- GREEN `3d14633337472fa9b109e4e879af5bf0ad6ff5e7`: full Maven `verify` passed.

Delivered math:
- `packageCount = ceil(required / package)` as positive `BigInteger`;
- `providedQuantity = packageQuantity * packageCount`;
- `lineTotal = snapshot.price * packageCount`;
- canonical unit mismatch fails closed;
- no fractional package or floating-point arithmetic.

## Task 3 — single-store quote semantics — COMPLETE

- [x] Define complete multi-item quote/order/total RED.
- [x] Define unknown availability → uncertain RED.
- [x] Define unmatched/ambiguous/unavailable/package-unknown/unit-mismatch incomplete RED.
- [x] Define mixed-currency, empty-list and immutability RED.
- [x] Correct one test-harness assumption before accepting RED.
- [x] Implement item resolution, structural result invariants and aggregate quote planner.
- [x] Full Maven `verify` GREEN.

Evidence:

- Initial RED `356878266da9e3f7cbc549e09dc3e373b9204e4b` correctly exposed absent basket types but also revealed one invalid test assumption (`ShoppingList.create` does not exist). That contaminated RED was not accepted.
- Corrected test head `9c2bb39ecbb14323bce950e180ccfd4995bdfd8e` used the actual public `ShoppingList` constructor and produced a clean RED: 20 compile errors, all only from absent basket quote/result/planner types.
- GREEN `6984776425df149e818da4708502137b21e6773b`: full Maven `verify` passed.

Delivered semantics:
- explicit item statuses `FULFILLED`, `AVAILABILITY_UNKNOWN`, `UNMATCHED`, `AMBIGUOUS`, `UNAVAILABLE`, `PACKAGE_QUANTITY_UNKNOWN`, `QUANTITY_UNIT_MISMATCH`;
- explicit `COMPLETE`, `UNCERTAIN`, `INCOMPLETE` quote states;
- `UNAVAILABLE` fails before package math;
- `UNKNOWN` availability retains a concrete selection/price but produces `UNCERTAIN`;
- incomplete quote has no total by construction;
- selected currencies must agree;
- shopping item order/results immutable;
- selection snapshot must be the matcher-selected candidate.

## Task 4 — architecture, docs and shipping — COMPLETE EXCEPT MARKER GATE/MERGE

- [x] Add upstream module-direction ArchUnit rule.
- [x] Run full Maven `verify` with architecture rule.
- [x] Synchronize `PROJECT_STATE.md`, `ROADMAP.md`, `CHANGELOG.md` and this plan.
- [x] Run full exact-head repository CI/security gate.
- [x] Perform read-only Change Review.
- [x] Record final shipping evidence in this plan.
- [ ] Require this docs-only marker head to pass branch-protection checks again.
- [ ] Mark PR ready and squash merge with expected-head SHA guard.

Architecture evidence:

- `d056c6e2be84534bfd881cafc58abdee3d28ea4a` adds `BasketBoundaryArchitectureTest`; full Maven `verify` passed.

Shipping evidence:

- Docs-synchronized exact head `b4734c994cc1af8627e27237588eea532137d950` passed API CI, Contract CI, Web CI + Web E2E, CodeQL Java + JavaScript/TypeScript, Dependency Review, Retailer Bridge CI, Container Security API + Web, Release Bundle CI and Release Contract CI.
- Read-only Change Review found no P0/P1/P2 blockers: package quantity is never parsed/inferred from names; one SKU is not assumed to satisfy a requirement; ambiguity remains unresolved; explicit `UNAVAILABLE` wins before package math; `UNKNOWN` availability stays uncertain; package math uses canonical positive quantities and exact decimal ceiling arithmetic; incomplete quotes cannot carry a total; mixed currencies fail closed; quote scope remains one retailer/context through the matcher; upstream module direction is protected; no live retailer behavior changed.
- This marker commit changes only historical shipping evidence. Its exact head must still pass branch protection before squash merge.

## Important limitation

The basket core consumes explicit trusted package-quantity evidence, but accepted retailer adapters do not yet expose a universal structured package-quantity field. This implementation does **not** claim a production end-to-end basket flow and deliberately does not parse package size from presentation text.

## Next after merge

M1 moves to **failure / coverage / freshness product/API/UX semantics** before critical browser E2E. Structured package-quantity extraction remains parallel evidence-driven integration work only where a retailer/source exposes trustworthy semantics.
