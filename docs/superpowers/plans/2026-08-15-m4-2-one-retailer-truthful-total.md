# M4.2 One-retailer Truthful Total Comparison — Implementation Plan

**Issue:** #136  
**Baseline:** `c1c45e4f95d395fe6e63faa9283d8394a18b0557`  
**Branch:** `feat/m4-2-one-retailer-truthful-total`

## Goal

Implement the accepted M4.2 design as a pure deterministic composition layer over M1 comparison and M4.1 basket economics, without winner selection, provider acquisition or public API changes.

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

## Task 3 — RED/GREEN hardening for impossible states

Add `RetailerCheckoutAssessmentInvariantTest` first, asserting public constructors reject:

- READY/UNCERTAIN assessment whose economics subtotal differs from comparison subtotal;
- forged eligibility inconsistent with comparison/minimum status;
- forged comparability inconsistent with state;
- comparable state without comparable total;
- not-comparable state carrying comparable total;
- comparable total differing from M4.1 checkout total;
- assessment for INCOMPLETE/UNAVAILABLE comparison;
- result with assessment presence inconsistent with original comparison;
- result whose assessment references another comparison value.

Verify RED where the current production constructor permits any impossible state, then strengthen canonical constructors and rerun API verification to GREEN.

## Task 4 — Architecture/regression proof

Add `RetailerCheckoutArchitectureTest` proving:

- M4.2 production classes depend only on JDK + accepted `basket` + `comparison` packages;
- no provider, retailer-domain, matching, shopping, location, database, Spring or preview coupling;
- accepted upstream `basket` and `comparison` packages do not depend on `retailercheckout`.

Run full API verification.

## Task 5 — Shipping evidence and PR

Create `docs/superpowers/plans/2026-08-15-m4-2-one-retailer-truthful-total-shipping.md` recording:

- baseline;
- design/plan;
- RED and GREEN SHAs;
- invariant hardening chain;
- architecture proof;
- final exact feature head.

Open a draft implementation PR linked to #136, then freeze runtime changes except for verified review findings.

## Task 6 — Exact-head acceptance gate

On the final feature SHA require:

- exactly 9 normal PR workflow groups;
- 9/9 SUCCESS, 0 failure/skipped/cancelled;
- read-only review `Looks good` with no P0/P1/P2/P3/nitpicks;
- zero unresolved review threads;
- mergeable=true.

Then mark ready and squash merge with `expected_head_sha` protection.

## Task 7 — Post-merge and canonical acceptance

Verify the exact implementation merge SHA has exactly 8 normal push workflows and 8/8 SUCCESS.

Close #136 as completed if GitHub did not auto-close it.

Create a separate docs-only acceptance PR updating:

- M4.2 acceptance record;
- `docs/PROJECT_STATE.md`;
- `docs/ROADMAP.md`;
- `CHANGELOG.md`;
- next deterministic target to M4.3 Basket optimizer.

Require 9/9 PR workflows + clean review, squash merge with expected-head protection, then final main 8/8 push workflows.
