# M4.1 Basket Economics Foundation — Design

**Date:** 2026-08-15  
**Issue:** #133  
**Baseline:** `37ec650e59ede8773cb1c1258e70be341bfba7ef`

## Problem

M1 can produce a truthful merchandise basket subtotal, but a retailer checkout may also depend on delivery fees, service fees and minimum-order constraints. Treating an unknown fee as zero would make later optimization untruthful. M4.1 therefore establishes a pure, deterministic economics model before any retailer ranking is allowed.

## Existing monetary convention

The accepted basket domain already represents money as `BasketTotal(BigDecimal amount, String currencyCode)` and validates non-negative amounts plus ISO-4217 currency codes. Existing package and basket arithmetic uses exact `BigDecimal` operations and does not silently round or normalize scale.

M4.1 reuses that convention. It does not introduce a parallel `Money` type or implicit currency rounding.

## Model

The new model stays in the existing `basket` package and has no provider acquisition responsibility.

### Knowledge state

`BasketEconomicsKnowledgeStatus` has exactly:

- `KNOWN`
- `UNKNOWN`

A known zero amount is still `KNOWN`; `UNKNOWN` never carries an amount.

### Fees

`BasketFee` contains a knowledge status and optional `BasketTotal` amount.

Factories:

- `BasketFee.known(BasketTotal)`
- `BasketFee.unknown()`

Invariants:

- `KNOWN` requires an amount;
- `UNKNOWN` requires no amount;
- negative values are already rejected by `BasketTotal`.

Delivery fee and service fee are separate fields in `BasketEconomics`, even though both reuse the same value object.

### Minimum order

`MinimumOrderConstraint` contains the same explicit knowledge state and an optional `BasketTotal` threshold.

Factories:

- `MinimumOrderConstraint.known(BasketTotal)`
- `MinimumOrderConstraint.unknown()`

A known zero threshold means there is no positive merchandise minimum and evaluates as met for any non-negative merchandise subtotal.

`MinimumOrderStatus` is:

- `MET`
- `NOT_MET`
- `UNKNOWN`

A known threshold is compared against **merchandise subtotal only**. Delivery and service fees do not make a merchandise minimum pass.

### Economics input

`BasketEconomics` contains:

- delivery fee;
- service fee;
- minimum-order constraint.

It is retailer-economics evidence supplied to the pure calculation boundary; M4.1 does not define how providers acquire that evidence.

### Assessment

`BasketEconomicsCalculator.assess(BasketTotal merchandiseSubtotal, BasketEconomics economics)` returns `BasketEconomicsAssessment` containing:

- original merchandise subtotal;
- original economics knowledge;
- minimum-order status;
- checkout-total status;
- optional checkout total.

`CheckoutTotalStatus` is `KNOWN` or `UNKNOWN`.

Checkout total is `KNOWN` only when both material fee components are known. It is then:

`merchandise subtotal + delivery fee + service fee`

If either fee is unknown, checkout total is `UNKNOWN` and absent. The merchandise subtotal remains available.

Minimum-order status is independent of checkout-total knowledge: a known threshold can still be evaluated when a fee is unknown.

## Currency semantics

Every known fee and known minimum-order threshold used in an assessment must have the same currency code as the merchandise subtotal. Mixed-currency arithmetic fails fast with `IllegalArgumentException`.

Unknown components carry no synthetic currency and do not trigger fake conversion.

## Decimal and rounding semantics

M4.1 performs exact `BigDecimal` addition/comparison only. It does not add implicit rounding, rescaling, or currency-fraction normalization. This preserves the already accepted basket convention and prevents a hidden policy change inside the economics layer.

If currency-specific checkout rounding is later proven necessary, it must be a separate explicit policy/evidence boundary.

## Architectural boundary

The implementation lives in `io.github.trueruslan.zakupgotov.basket` and depends only on JDK types plus existing basket value objects. It must not invoke provider/browser/network code.

Accepted M1 `SingleStoreBasketPlanner` / `SingleStoreBasketQuote` semantics are not changed in M4.1. Their current `total` remains the merchandise subtotal produced by selected package lines. A later M4 composition slice may feed that subtotal into the economics calculator.

## Non-goals

- retailer ranking or winner selection;
- split-basket optimization;
- provider acquisition or browser work;
- HTTP/OpenAPI/UI changes;
- changing accepted M1 quote behavior;
- adding discounts, subscriptions, loyalty pricing, tips or checkout-rounding policies without evidence.

## Acceptance invariants

1. Known positive fees produce a known checkout total.
2. Known zero fees remain distinguishable from unknown fees.
3. Any unknown material fee makes checkout total unknown without losing subtotal.
4. Known minimum-order thresholds produce deterministic `MET` / `NOT_MET` from merchandise subtotal.
5. Unknown threshold produces `UNKNOWN`.
6. Mixed currencies fail fast.
7. No hidden decimal rounding/normalization occurs.
8. No provider acquisition dependency is introduced.
