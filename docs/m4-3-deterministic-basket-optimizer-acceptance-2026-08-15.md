# M4.3 Deterministic Basket Optimizer — Acceptance

**Date:** 2026-08-15  
**Issue:** #139  
**Implementation PR:** #140  
**Baseline:** `b32c461eb49eefa5ab37f23d45491e9f46356c10`  
**Final reviewed feature head:** `ddc5fed0d3bb98d9c17e5f1ec739ffad9ba77ad5`  
**Accepted implementation merge:** `c854526c30a1b0b1b6b435ae37608da0d9501955`

## Decision

M4.3 is **COMPLETE / ACCEPTED**.

The project now has a pure deterministic selection layer over accepted M4.2 retailer checkout assessments. It can truthfully expose no comparable candidates, one unique cheapest comparable retailer, or an exact monetary tie without recomputing accepted baskets or inventing hidden confidence/freshness policy.

## Accepted semantics

- input is an ordered, non-empty set of M4.2 `RetailerCheckoutAssessmentResult` values with unique retailer identities;
- all original candidates remain visible in their original order for inspectability;
- only M4.2 `COMPARABLE` candidates compete for the optimum;
- `INELIGIBLE`, eligibility `UNKNOWN`, `NOT_COMPARABLE`, upstream `UNCERTAIN`, `INCOMPLETE` and `UNAVAILABLE` candidates never become hidden winners;
- comparable candidates in one optimization must use one currency; mixed comparable currencies fail closed;
- monetary ordering uses exact `BigDecimal.compareTo` semantics with no rounding or rescaling;
- numerically equal minima remain an explicit `TIE`, including amounts that differ only in decimal scale;
- every tied minimum is retained in original input order; no retailer-order, retailer-ID, freshness, provider timestamp, package/SKU or iteration-order tie-break exists;
- accepted M1 matching/package/basket selections are immutable inputs; M4.3 performs no substitute search, package-count recomputation or multi-store split;
- freshness remains inspectable upstream evidence and is not converted into a monetary penalty or confidence score;
- result objects recompute the deterministic evaluation and reject forged status/optimal-candidate sets;
- M4.3 has no provider acquisition, HTTP/OpenAPI/UI, persistence or live-retailer behavior.

## Architecture acceptance

The accepted `basketoptimization` package consumes M4.2 checkout results plus neutral `BasketTotal` only. Retailer identity is projected by the M4.2-owned `RetailerCheckoutAssessmentResult.retailerId()` boundary, so M4.3 has no direct dependency on `comparison` or `retailer` internals.

The additive M4.2 identity projection does not change M4.2 eligibility, comparability or checkout-total semantics.

Reverse dependencies from accepted basket/comparison/retailer/retailercheckout layers into `basketoptimization` remain forbidden.

## TDD and hardening evidence

Core behavior:

- RED: `e6923b46…` — API CI failed at test compilation on missing `BasketOptimizer` / `BasketOptimizationStatus` symbols;
- initial production implementation: `0df063bd…`;
- compile-only correction: `6ccf4296…` candidate reached full GREEN;
- behavior GREEN: full API verification SUCCESS.

Invariant / architecture hardening:

- invariant suite: `567261da…`;
- initial architecture proof: `0c607f12…` exposed a real direct M4.3 → `comparison` abstraction leak while behavior and result invariants remained green;
- targeted identity-seam RED: `06b55b1f…` failed only because M4.2 did not yet expose `retailerId()`;
- corrective chain `187b7f41…` → `d24604e7…` → `f410d175…` → `cab385ff…` introduced the M4.2-owned identity projection and removed direct M4.3 `comparison` / `retailer` dependencies;
- exact corrective API gate on `cab385ff…` — SUCCESS;
- design/plan/shipping evidence synchronized before final freeze.

## Final implementation gate

Exact final feature head `ddc5fed0d3bb98d9c17e5f1ec739ffad9ba77ad5`:

- exactly **9 normal PR workflow groups**;
- **9/9 SUCCESS**;
- 0 failure / skipped / cancelled;
- read-only review: **Looks good**;
- no P0/P1/P2/P3 findings or nitpicks;
- unresolved review threads: **0**;
- mergeable: true;
- squash merged with expected-head protection.

## Post-merge acceptance gate

Exact implementation merge `c854526c30a1b0b1b6b435ae37608da0d9501955`:

- `main` points to the merge SHA;
- issue #139 is closed with state reason `completed`;
- exactly **8 normal push workflows**;
- **8/8 SUCCESS**;
- no failed post-merge workflow.

Therefore M4.3 status is:

**implemented → tested → reviewed → merged → accepted**

## Next deterministic target

**M4.4 — Optimization UX**.

The browser should project server-owned optimizer/economics evidence into the primary shopping flow without recomputing eligibility, checkout totals or winner/tie decisions client-side. The UX must keep no-comparable, unique-winner and tie states explicit and explain subtotal/fees/minimum-order/eligibility/comparability truthfully.
