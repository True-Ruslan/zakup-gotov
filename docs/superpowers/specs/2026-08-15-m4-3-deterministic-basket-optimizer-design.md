# M4.3 Deterministic Basket Optimizer — Design

**Date:** 2026-08-15  
**Issue:** #139  
**Baseline:** `b32c461eb49eefa5ab37f23d45491e9f46356c10`

## Goal

Select the lowest truthful single-retailer checkout option over accepted M4.2 retailer checkout assessments without recomputing retailer baskets, inventing confidence, applying hidden freshness penalties or selecting an arbitrary winner from an exact monetary tie.

M4.3 is a **selection layer**, not a package/SKU optimizer. Accepted M1 matching/package/basket decisions and accepted M4.2 eligibility/comparability decisions are immutable inputs.

## Boundary

New downstream package:

`io.github.trueruslan.zakupgotov.basketoptimization`

Public boundary:

`BasketOptimizer.optimize(List<RetailerCheckoutAssessmentResult> candidates)`

Allowed domain dependencies:

- accepted `retailercheckout` M4.2 types;
- `BasketTotal` for explicit lowest-total projection.

Retailer identity is owned and projected by M4.2 through `RetailerCheckoutAssessmentResult.retailerId()`. M4.3 must not reach through that result into `RetailerComparisonView`, and it has no direct `comparison` or `retailer` dependency.

The `retailerId()` convenience projection is a non-semantic M4.2 abstraction seam: it returns the already-public identity of the embedded accepted comparison and changes no M4.2 eligibility/comparability behavior.

No reverse dependency from basket/retailercheckout/comparison/retailer into basketoptimization.

No provider, matching, shopping, package recomputation, persistence, Spring, HTTP/OpenAPI/UI, network acquisition or multi-store split.

## Input contract

The optimizer accepts an **ordered, non-empty** list of M4.2 `RetailerCheckoutAssessmentResult` values.

Input rules:

- list is required and defensively copied;
- null candidates are rejected;
- retailer IDs, obtained only through the M4.2 result abstraction, must be unique;
- original candidate order is retained in the result for inspectability;
- original order is never used to prefer one retailer over another.

The pure optimizer does not require the entire canonical registry itself. Canonical retailer visibility remains an upstream comparison/composition responsibility. It does reject an empty set because silently optimizing no visible retailers would hide an integration/composition error. `NO_COMPARABLE_CANDIDATES` is reserved for a non-empty visible candidate set whose members are all non-comparable.

## Competitive eligibility

Only an M4.2 assessment whose `comparabilityStatus == COMPARABLE` may compete.

By accepted M4.2 invariant, every comparable assessment has `comparableCheckoutTotal` present.

All other candidates remain in `BasketOptimizationResult.candidates` but are excluded from winner calculation:

- M4.2 `NOT_COMPARABLE`;
- `INELIGIBLE`;
- eligibility `UNKNOWN`;
- upstream `UNCERTAIN`;
- upstream `INCOMPLETE`;
- upstream `UNAVAILABLE`.

M4.3 never attempts to repair or upgrade an upstream candidate.

## Currency rule

All **comparable** candidates in one optimization call must use the same normalized ISO-4217 currency.

If two comparable candidates use different currencies, optimization fails closed with `IllegalArgumentException`. M4.3 has no currency-conversion contract and therefore must not rank one currency against another.

A non-comparable candidate in another currency does not poison a valid comparable set because it does not participate in monetary comparison. It remains visible in the original candidate list.

## Amount comparison

Comparable checkout amounts are compared using exact `BigDecimal.compareTo` semantics:

- no `double`/`float` conversion;
- no rounding;
- no scale normalization/rescaling;
- numerically equal amounts compare equal even if source decimal scales differ, e.g. `1200.0` and `1200.00`.

This preserves exact arithmetic while avoiding a false winner based only on decimal representation scale.

## Outcome model

`BasketOptimizationStatus` has exactly:

- `NO_COMPARABLE_CANDIDATES`
- `UNIQUE_WINNER`
- `TIE`

`BasketOptimizationResult` contains:

- `List<RetailerCheckoutAssessmentResult> candidates` — all original candidates in original order;
- `BasketOptimizationStatus status`;
- `List<RetailerCheckoutAssessmentResult> optimalCandidates` — none, one or all exact minima in original order.

A convenience method `lowestComparableCheckoutTotal()` derives the lowest total from the first optimal candidate when one exists. It is absent for `NO_COMPARABLE_CANDIDATES`.

### NO_COMPARABLE_CANDIDATES

- input candidates remain non-empty;
- no candidate is M4.2 COMPARABLE;
- `optimalCandidates` is empty;
- lowest total is absent.

### UNIQUE_WINNER

- at least one comparable candidate exists;
- exactly one comparable candidate has the numerically lowest checkout amount;
- `optimalCandidates` contains exactly that candidate;
- lowest total is its exact M4.2 `BasketTotal` value.

### TIE

- at least two comparable candidates share the numerically lowest amount;
- `optimalCandidates` contains **all** tied minima in original input order;
- no other candidate is included;
- no single winner is exposed.

## Tie policy

A tie is never broken by:

- original/canonical retailer order;
- retailer ID;
- freshness basis;
- `observedAt`;
- provider update time;
- package/SKU identity;
- availability observation timing;
- arbitrary iteration order.

M4.3 exposes the tie honestly. A later product policy may add user-selected convenience preferences, but that requires a separate explicit design.

## Package/substitution policy

M4.3 does not alter product/package selections.

The accepted M1/M4.2 candidate already represents a deterministic retailer basket. M4.3 compares only final M4.2 comparable checkout totals. It does not:

- search alternative SKUs;
- substitute brands/products;
- change package counts;
- combine products across retailers;
- rebuild matching or package arithmetic.

Any future intra-retailer substitute/package optimizer must be designed as a separate upstream slice that produces a new truthful M4.2-compatible candidate. M4.3 must never infer such alternatives from presentation text or price.

## Freshness/confidence policy

Freshness remains preserved inside the original M1 comparison evidence carried by each M4.2 candidate.

M4.3 does **not**:

- convert freshness into a monetary penalty;
- use newer freshness as a tie-break;
- invent a confidence score;
- use provider timestamp presence as ranking preference.

Reason: the project has no accepted quantitative staleness threshold or confidence model. Inventing one would create hidden optimization semantics. Matching/availability uncertainty already makes candidates non-comparable upstream where appropriate.

## Self-validation

`BasketOptimizationResult` recomputes the expected optimization outcome from its candidate list and rejects forged states:

- empty candidates;
- null entries;
- duplicate retailer IDs;
- mixed currencies among comparable candidates;
- wrong status;
- missing/extra/reordered optimal candidates;
- a non-comparable optimal candidate;
- a candidate above the numeric minimum appearing as optimal;
- hidden unique winner when the true outcome is a tie;
- hidden tie when a unique minimum exists.

The service and result share one package-private deterministic evaluation helper so validation and production selection cannot drift.

## Review-driven architecture hardening

The first proof-layer implementation obtained retailer identity through `candidate.comparison().retailerId()`. ArchUnit correctly rejected this as a direct M4.3 dependency on the M1 `comparison` layer.

The accepted correction is:

1. M4.2 `RetailerCheckoutAssessmentResult` exposes `retailerId()` as an identity projection of its already-owned comparison;
2. M4.3 consumes only that M4.2 projection;
3. duplicate-retailer validation remains fail-closed;
4. final architecture has no direct M4.3 dependency on `comparison` or `retailer` at all.

This hardening changes only dependency direction, not optimizer selection semantics or accepted M4.2 state semantics.

## Non-goals

- HTTP/OpenAPI/browser UX;
- multi-store split basket;
- package/SKU/substitute search;
- discount/loyalty/subscription/tip modeling;
- currency conversion;
- freshness thresholds/penalties;
- confidence scoring;
- user-preference tie-breaks;
- provider acquisition.

## Required acceptance proof

1. Unique lowest comparable candidate -> `UNIQUE_WINNER`.
2. Equal numeric totals, including different decimal scale -> `TIE` with every minimum in input order.
3. Non-empty all-non-comparable set -> `NO_COMPARABLE_CANDIDATES`.
4. Cheaper ineligible/unknown/uncertain/incomplete/unavailable candidate never wins.
5. Mixed currencies among comparable candidates fail closed.
6. Different-currency non-comparable candidate does not poison a valid comparable winner.
7. Empty input, null candidate and duplicate retailer IDs fail closed.
8. Public result rejects forged status/optimal sets.
9. Freshness differences never break a monetary tie.
10. Candidate list/order is preserved and no input object is mutated.
11. Architecture allows only accepted M4.2 plus neutral `BasketTotal`, with no direct `comparison`/`retailer` and no reverse/provider/matching/API/database coupling.
12. M4.2 identity projection returns the embedded accepted retailer identity without changing M4.2 semantics.
13. Existing M1/M4.1/M4.2 tests remain unchanged and green except for the additive M4.2 identity-projection regression.
