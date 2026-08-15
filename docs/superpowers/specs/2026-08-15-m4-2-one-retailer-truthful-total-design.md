# M4.2 One-retailer Truthful Total Comparison — Design

**Date:** 2026-08-15  
**Issue:** #136  
**Baseline:** `c1c45e4f95d395fe6e63faa9283d8394a18b0557`

## Problem

M1 exposes a truthful retailer comparison view whose `total` is the selected merchandise basket subtotal. M4.1 can deterministically assess delivery/service fees, minimum-order constraints and arithmetic checkout total. M4.2 must compose those accepted facts without silently redefining M1 totals and without selecting a winner.

A known arithmetic checkout total is not automatically a valid optimization candidate. Minimum-order failure, minimum-order uncertainty, uncertain basket availability, incomplete evidence or unavailable retailer state must remain visible and must not become a hidden cheapest claim.

## Boundary

Add a new downstream package:

`io.github.trueruslan.zakupgotov.retailercheckout`

It may depend on accepted `comparison` and `basket` types. Neither `basket` nor `comparison` may depend back on it.

M4.2 is deterministic over supplied evidence. It performs no provider/browser/network acquisition and introduces no HTTP/OpenAPI/UI contract.

## Inputs

`RetailerCheckoutAssessmentService.assess(RetailerComparisonView comparison, BasketEconomics economics)` receives:

- the accepted M1 retailer comparison projection;
- explicit M4.1 basket-economics evidence.

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

## Invariants

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
10. Architecture tests prove `retailercheckout -> comparison + basket` only, with no reverse/provider/database/network coupling.
11. Existing M1/M4.1 tests remain unchanged and green.
