# M4.3 Deterministic Basket Optimizer — Implementation Plan

**Issue:** #139  
**Baseline:** `b32c461eb49eefa5ab37f23d45491e9f46356c10`  
**Branch:** `feat/m4-3-deterministic-basket-optimizer`

## Goal

Implement a pure deterministic optimizer over accepted M4.2 checkout assessments. Only explicit `COMPARABLE` candidates may compete; exact monetary ties remain ties; M1 package/freshness evidence is preserved rather than reinterpreted.

## Task 1 — RED: optimizer behavior

Add `BasketOptimizerTest` before production optimizer types exist.

Required scenarios:

1. one comparable candidate with the lowest checkout total -> `UNIQUE_WINNER`;
2. two numeric-equal minima, including different decimal scales -> `TIE`, both retained in input order;
3. all candidates non-comparable -> `NO_COMPARABLE_CANDIDATES`;
4. a cheaper arithmetic total from an ineligible/non-comparable candidate is excluded;
5. an upstream UNCERTAIN candidate with a lower arithmetic total is excluded;
6. mixed comparable currencies fail closed;
7. a different-currency non-comparable candidate does not poison a valid winner;
8. freshness differences do not break an exact monetary tie;
9. input candidate order is preserved even when the unique winner is not first.

Commit test-only RED and use draft PR API CI to prove failure is missing M4.3 symbols, not infrastructure.

## Task 2 — GREEN: minimal optimizer

Add package `io.github.trueruslan.zakupgotov.basketoptimization`:

- `BasketOptimizationStatus`
- package-private `BasketOptimizationEvaluation`
- package-private `BasketOptimizationRules`
- `BasketOptimizationResult`
- `BasketOptimizer`

Rules:

- input list required, non-empty, defensive-copy;
- null candidate rejected;
- duplicate retailer IDs rejected through the M4.2 result abstraction;
- only M4.2 COMPARABLE candidates enter monetary comparison;
- mixed comparable currencies rejected;
- numeric ordering uses exact `BigDecimal.compareTo`, no rounding/rescaling;
- unique minimum -> one optimal candidate;
- numeric tie -> all minima, original order;
- no comparable candidate -> explicit status;
- result exposes original candidates and optimal candidates, not a sorted ranking.

Run full API verification to GREEN.

## Task 3 — invariant hardening

Add `BasketOptimizationInvariantTest` proving `BasketOptimizationResult` rejects:

- empty/null/duplicate-retailer candidate set;
- wrong status;
- missing unique winner;
- fabricated unique winner from a tie;
- fabricated tie when a unique minimum exists;
- non-comparable optimal candidate;
- non-minimum optimal candidate;
- missing/extra/reordered tied optimal candidates;
- mixed comparable currencies.

If shared deterministic rules already reject all states, record GREEN proof without manufacturing an artificial RED.

## Task 4 — architecture proof

Add `BasketOptimizationArchitectureTest` proving:

- production package has no provider, matching, shopping, location, direct comparison, direct retailer, preview, persistence/database, Spring, jOOQ, Recipe, WeeklyPlan or Pantry dependency;
- the only direct basket dependency is neutral `BasketTotal`;
- retailer identity is consumed through accepted M4.2 `RetailerCheckoutAssessmentResult` rather than by reaching through to M1 comparison;
- `basket`, `comparison`, `retailer`, and `retailercheckout` do not depend back on `basketoptimization`.

If the first architecture proof finds direct M4.3 access to `RetailerComparisonView`, treat it as a real abstraction leak rather than weakening the guard:

1. add a test-first M4.2 identity projection requirement;
2. expose non-semantic `RetailerCheckoutAssessmentResult.retailerId()` returning its embedded accepted comparison identity;
3. consume only that M4.2 projection from M4.3;
4. require final M4.3 bytecode to have no direct `comparison` or `retailer` dependency.

Run full API verification.

## Task 5 — shipping evidence and review

Record RED/GREEN/architecture evidence in:

`docs/superpowers/plans/2026-08-15-m4-3-deterministic-basket-optimizer-shipping.md`

Open/maintain draft PR linked to #139. Perform read-only review for:

- hidden tie-breaks;
- currency behavior;
- freshness/confidence leakage into winner selection;
- package/substitution recomputation;
- preservation of non-comparable candidates;
- constructor forgeability;
- architecture direction;
- whether the additive M4.2 identity projection changes accepted M4.2 semantics (it must not).

Correct verified findings through additional TDD only.

## Task 6 — exact-head acceptance

Freeze final feature SHA and require:

- exactly 9 normal PR workflow groups;
- 9/9 SUCCESS, no failure/skipped/cancelled;
- clean read-only review, no P0/P1/P2/P3/nitpicks;
- zero unresolved review threads;
- mergeable=true.

Mark ready and squash merge with `expected_head_sha`.

## Task 7 — post-merge and canonical docs

Verify exact implementation merge:

- `main` points to merge SHA;
- issue #139 closed completed;
- exactly 8 normal push workflows;
- 8/8 SUCCESS.

Then create separate docs-only acceptance PR updating:

- M4.3 acceptance record;
- `docs/PROJECT_STATE.md`;
- `docs/ROADMAP.md`;
- `CHANGELOG.md`;
- next deterministic target to M4.4 Optimization UX.

Require docs PR 9/9 + clean review, expected-head squash merge, final main 8/8.
