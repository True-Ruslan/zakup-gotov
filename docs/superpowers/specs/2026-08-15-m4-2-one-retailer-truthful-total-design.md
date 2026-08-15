# M4.2 One-retailer Truthful Total Comparison — Design

**Date:** 2026-08-15  
**Issue:** #136  
**Baseline:** `c1c45e4f95d395fe6e63faa9283d8394a18b0557`

## Problem

M1 exposes a truthful retailer comparison view whose `total` is the selected merchandise basket subtotal. M4.1 can deterministically assess delivery/service fees, minimum-order constraints and arithmetic checkout total. M4.2 must compose those accepted facts without silently redefining M1 totals and without selecting a winner.

A known arithmetic checkout total is not automatically a valid optimization candidate. Minimum-order failure, minimum-order uncertainty, uncertain basket availability, incomplete evidence or unavailable retailer state must remain visible and must not become a hidden cheapest claim.

M4.1 economics are deliberately retailer-neutral. At the M4.2 composition boundary they must therefore be bound to an explicit retailer identity before they can be combined with retailer comparison evidence. Cross-retailer fee/minimum-order evidence is a contract violation and fails closed before checkout arithmetic.

## Boundary

Add a new downstream package:

`io.github.trueruslan.zakupgotov.retailercheckout`

It may depend on accepted `comparison` and `basket` types plus the finite neutral `RetailerId` identity bridge required to bind M4.1 economics to the same retailer. Neither `basket`, `comparison` nor `retailer` may depend back on it.

M4.2 is deterministic over supplied evidence. It performs no provider/browser/network acquisition and introduces no HTTP/OpenAPI/UI contract.

## Inputs

The public composition boundary is:

`RetailerCheckoutAssessmentService.assess(RetailerComparisonView comparison, RetailerCheckoutEconomicsEvidence economicsEvidence)`

`RetailerCheckoutEconomicsEvidence` contains:

- `RetailerId retailerId`;
- accepted M4.1 `BasketEconomics economics`.

Before any M4.1 calculation, the service requires `economicsEvidence.retailerId == comparison.retailerId`. A mismatch fails closed. No provider, acquisition or fulfillment identifier becomes public.

The service never mutates or reinterprets `RetailerComparisonView.total`. That field remains the merchandise subtotal.

## Assessment availability

`RetailerCheckoutAssessmentResult` always retains the original `RetailerComparisonView` and optionally carries a `RetailerCheckoutAssessment`.

- `READY` and `UNCERTAIN` comparison states have a merchandise subtotal and therefore produce an economics-backed checkout assessment;
- `INCOMPLETE` and `UNAVAILABLE` have no aggregate merchandise subtotal by accepted M1 invariant, so the result carries **no** checkout assessment rather than fabricating one.

The outer result is self-validating: an assessment is present iff the accepted comparison state has a merchandise subtotal and the assessment belongs to the same original comparison.

## Eligibility

`RetailerCheckoutEligibilityStatus` has exactly:

- `ELIGIBLE`
- `INELIGIBLE`
- `UNKNOWN`

Eligibility is derived from accepted comparison state plus M4.1 minimum-order status.

Rules, in precedence order:

1. `MinimumOrderStatus.NOT_MET` -> `INELIGIBLE` for any assessable (`READY` or `UNCERTAIN`) basket. A known blocking constraint is not erased by unrelated uncertainty.
2. `RetailerComparisonStatus.UNCERTAIN` -> `UNKNOWN` unless rule 1 already made the basket ineligible. Availability uncertainty must never be upgraded to eligible.
3. `MinimumOrderStatus.UNKNOWN` -> `UNKNOWN`.
4. `READY + MinimumOrderStatus.MET` -> `ELIGIBLE`.

`INCOMPLETE` and `UNAVAILABLE` do not produce a checkout assessment, so they have no fabricated eligibility value.

Checkout-total knowledge does not determine eligibility. A retailer may be `ELIGIBLE` while a material fee is unknown; in that case the arithmetic checkout total is unknown and the retailer is not comparable.

## Comparability

`RetailerCheckoutComparabilityStatus` has exactly:

- `COMPARABLE`
- `NOT_COMPARABLE`

A checkout assessment is `COMPARABLE` **only** when all of the following hold:

- original comparison status is `READY`;
- checkout eligibility is `ELIGIBLE`;
- M4.1 `CheckoutTotalStatus` is `KNOWN`;
- M4.1 checkout total is present.

Only then does `RetailerCheckoutAssessment.comparableCheckoutTotal` contain the exact M4.1 checkout total.

Every other assessable state is `NOT_COMPARABLE` and carries no comparable total. The underlying M4.1 economics assessment remains inspectable, so known arithmetic totals for `INELIGIBLE`, `UNKNOWN` or upstream `UNCERTAIN` states are not hidden, but they cannot support a later cheapest claim.

## Important scenarios

### READY + minimum MET + known fees

- eligibility: `ELIGIBLE`
- arithmetic checkout total: `KNOWN`
- comparability: `COMPARABLE`
- comparable total: present and exactly equal to M4.1 checkout total

### READY + minimum NOT_MET + known fees

- eligibility: `INELIGIBLE`
- arithmetic checkout total: still inspectable and may be `KNOWN`
- comparability: `NOT_COMPARABLE`
- comparable total: absent

### READY + minimum UNKNOWN + known fees

- eligibility: `UNKNOWN`
- arithmetic checkout total may be `KNOWN`
- comparability: `NOT_COMPARABLE`

### READY + minimum MET + unknown material fee

- eligibility: `ELIGIBLE`
- arithmetic checkout total: `UNKNOWN`
- comparability: `NOT_COMPARABLE`

### UNCERTAIN + minimum MET + known fees

- eligibility: `UNKNOWN`
- arithmetic checkout total may be `KNOWN`
- comparability: `NOT_COMPARABLE`

### UNCERTAIN + minimum NOT_MET

- eligibility: `INELIGIBLE`
- comparability: `NOT_COMPARABLE`

### INCOMPLETE / UNAVAILABLE

- original comparison remains authoritative;
- checkout assessment absent;
- no fabricated subtotal, fee arithmetic, eligibility or comparable total.

### Cross-retailer economics evidence

- no arithmetic is performed;
- composition fails closed because the economics retailer identity does not match the retailer comparison identity.

## Invariants

`RetailerCheckoutEconomicsEvidence` is self-validating:

- retailer identity is required;
- M4.1 economics are required.

The public service boundary additionally requires economics and comparison retailer identities to match before calculation.

`RetailerCheckoutAssessment` is self-validating:

- comparison state must be `READY` or `UNCERTAIN`;
- economics merchandise subtotal must exactly equal `RetailerComparisonView.total`;
- eligibility must equal the deterministic rules above;
- comparability must equal the deterministic rules above;
- comparable total is present iff comparability is `COMPARABLE`;
- when present, comparable total exactly equals the M4.1 checkout total.

`RetailerCheckoutAssessmentResult` is self-validating:

- assessment presence matches whether the comparison has a merchandise subtotal;
- present assessment must reference the exact same comparison value.

M4.1 currency and impossible-state invariants remain authoritative and fail fast.

## Non-goals

- winner selection or cheapest claim;
- sorting or ranking retailers;
- multi-store optimization;
- provider acquisition of fee/minimum-order evidence;
- provider/acquisition/fulfillment provenance for economics beyond retailer identity in this slice;
- modifying M1 `RetailerComparisonView` or `SingleStoreBasketQuote` semantics;
- HTTP/OpenAPI/browser changes;
- discounts, loyalty, subscriptions, tips or currency conversion.

## Acceptance proof required

1. READY + known fees + minimum MET -> eligible and comparable known checkout total.
2. READY + known fees + minimum NOT_MET -> ineligible, arithmetic total inspectable, not comparable.
3. READY + known fees + minimum UNKNOWN -> eligibility unknown, not comparable.
4. READY + minimum MET + unknown fee -> eligible, checkout total unknown, not comparable.
5. UNCERTAIN never becomes comparable; NOT_MET can still prove ineligibility.
6. INCOMPLETE/UNAVAILABLE produce no checkout assessment.
7. Known zero fees remain exact known zero through M4.1 composition.
8. Currency/invariant failures remain fail-fast.
9. Public result/assessment objects reject forged contradictory states.
10. Retailer-bound economics must match the retailer comparison identity; cross-retailer mixing fails before arithmetic.
11. Architecture tests prove `retailercheckout -> comparison + basket + RetailerId only`, with no reverse/provider/database/network coupling.
12. Existing M1/M4.1 tests remain unchanged and green.
