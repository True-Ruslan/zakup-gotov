# M4.2 One-retailer Truthful Total Comparison — Implementation Plan

**Issue:** #136  
**Baseline:** `c1c45e4f95d395fe6e63faa9283d8394a18b0557`  
**Branch:** `feat/m4-2-one-retailer-truthful-total`

## Goal

Implement the accepted M4.2 design as a pure deterministic composition layer over M1 comparison and M4.1 basket economics, without winner selection, provider acquisition or public API changes. Bind otherwise retailer-neutral M4.1 economics to an explicit `RetailerId` at the M4.2 trust boundary so cross-retailer fee/minimum-order evidence fails closed.

## Task 1 — RED: core behavior contract

Add `RetailerCheckoutAssessmentServiceTest` under a new `retailercheckout` test package before production types exist.

Required failing scenarios:

1. READY + known fees + minimum MET -> `ELIGIBLE`, `COMPARABLE`, exact comparable checkout total.
2. READY + known fees + minimum NOT_MET -> `INELIGIBLE`, known arithmetic checkout total remains inspectable, `NOT_COMPARABLE`.
3. READY + known fees + minimum UNKNOWN -> `UNKNOWN`, `NOT_COMPARABLE`.
4. READY + minimum MET + unknown delivery/service fee -> `ELIGIBLE`, M4.1 checkout total unknown, `NOT_COMPARABLE`.
5. UNCERTAIN + minimum MET -> `UNKNOWN`, never comparable.
6. UNCERTAIN + minimum NOT_MET -> `INELIGIBLE`, never comparable.
7. INCOMPLETE and UNAVAILABLE -> result contains no checkout assessment.
8. Known zero fees remain known zero and preserve subtotal as checkout total.

Commit the RED test-only checkpoint and verify API CI fails for missing M4.2 symbols rather than infrastructure.

## Task 2 — GREEN: minimal production model/service

Add package `io.github.trueruslan.zakupgotov.retailercheckout` with minimal types:

- `RetailerCheckoutEligibilityStatus`
- `RetailerCheckoutComparabilityStatus`
- `RetailerCheckoutAssessment`
- `RetailerCheckoutAssessmentResult`
- `RetailerCheckoutAssessmentService`

Implementation rules:

- call accepted `BasketEconomicsCalculator` only when `RetailerComparisonView.total` is present;
- never reinterpret or mutate `RetailerComparisonView.total`;
- deterministic eligibility precedence: `NOT_MET -> INELIGIBLE`, then upstream `UNCERTAIN -> UNKNOWN`, then minimum `UNKNOWN -> UNKNOWN`, else READY+MET -> ELIGIBLE;
- comparable iff READY + ELIGIBLE + known M4.1 checkout total;
- comparable total equals M4.1 checkout total exactly;
- INCOMPLETE/UNAVAILABLE return no checkout assessment.

Run full API verification and require GREEN.

## Task 3 — impossible-state hardening

Add `RetailerCheckoutAssessmentInvariantTest` asserting public constructors reject:

- READY/UNCERTAIN assessment whose economics subtotal differs from comparison subtotal;
- forged eligibility inconsistent with comparison/minimum status;
- forged comparability inconsistent with state;
- comparable state without comparable total;
- not-comparable state carrying comparable total;
- comparable total differing from M4.1 checkout total;
- assessment for INCOMPLETE/UNAVAILABLE comparison;
- result with assessment presence inconsistent with original comparison;
- result whose assessment references another comparison value;
- mixed-currency M4.1 economics through the M4.2 composition path.

If the implementation already rejects every impossible state, record a GREEN hardening proof rather than manufacturing an artificial RED.

## Task 4 — Architecture/regression proof

Add `RetailerCheckoutArchitectureTest` proving:

- M4.2 production classes depend on accepted `basket` + `comparison` plus only the finite `RetailerId` identity bridge from retailer-domain;
- no provider, matching, shopping, location, database, Spring, preview, Recipe/WeeklyPlan/Pantry coupling;
- accepted upstream `basket`, `comparison` and `retailer` packages do not depend on `retailercheckout`.

Run full API verification.

## Task 5 — review hardening: bind economics to retailer

Read-only review must check whether otherwise neutral M4.1 economics can be mixed across retailers. If that gap exists:

1. add test-first `RetailerCheckoutEconomicsBindingTest` requiring retailer-bound economics and cross-retailer rejection;
2. verify RED on the absent binding type;
3. add `RetailerCheckoutEconomicsEvidence(RetailerId, BasketEconomics)`;
4. make the public service composition boundary accept the bound evidence and validate retailer equality **before** arithmetic;
5. keep provider/acquisition/fulfillment identifiers out of M4.2;
6. update architecture proof so the only direct retailer-domain dependency is `RetailerId`;
7. rerun full API verification to GREEN.

## Task 6 — Shipping evidence and PR

Create/update `docs/superpowers/plans/2026-08-15-m4-2-one-retailer-truthful-total-shipping.md` recording:

- baseline;
- design/plan;
- core RED and GREEN SHAs;
- invariant hardening chain;
- architecture proof;
- any review-driven RED/GREEN hardening;
- clean final feature candidate.

Open a draft implementation PR linked to #136, then freeze runtime changes except for verified review findings.

## Task 7 — Exact-head acceptance gate

On the final feature SHA require:

- exactly 9 normal PR workflow groups;
- 9/9 SUCCESS, 0 failure/skipped/cancelled;
- read-only review `Looks good` with no P0/P1/P2/P3/nitpicks;
- zero unresolved review threads;
- mergeable=true.

Then mark ready and squash merge with `expected_head_sha` protection.

## Task 8 — Post-merge and canonical acceptance

Verify the exact implementation merge SHA has exactly 8 normal push workflows and 8/8 SUCCESS.

Close #136 as completed if GitHub did not auto-close it.

Create a separate docs-only acceptance PR updating:

- M4.2 acceptance record;
- `docs/PROJECT_STATE.md`;
- `docs/ROADMAP.md`;
- `CHANGELOG.md`;
- next deterministic target to M4.3 Basket optimizer.

Require 9/9 PR workflows + clean review, squash merge with expected-head protection, then final main 8/8 push workflows.
