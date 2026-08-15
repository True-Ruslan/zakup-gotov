# M4.1 Basket Economics Foundation — Implementation Plan

> Execute on `feat/m4-1-basket-economics-foundation` from accepted baseline `37ec650e59ede8773cb1c1258e70be341bfba7ef`.

**Goal:** Add a pure fail-closed basket economics model that preserves merchandise subtotal while explicitly representing fee and minimum-order knowledge before M4.2 compares retailer checkout totals.

**Architecture:** Extend only the existing `basket` domain. Do not change provider acquisition, comparison controllers, OpenAPI or UI. Reuse `BasketTotal` and exact `BigDecimal` semantics.

## Task 1 — Establish true RED for economics semantics

Create:
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/basket/BasketEconomicsCalculatorTest.java`

Tests must require, before production types exist:
- known delivery + service fees → known checkout total;
- known zero fees → known checkout total equal to subtotal;
- unknown delivery or service fee → unknown checkout total with subtotal retained;
- known threshold → MET / NOT_MET based on merchandise subtotal;
- unknown threshold → UNKNOWN;
- exact decimal addition with no implicit rounding;
- mixed-currency known fee / threshold rejection.

Commit the test-only RED and verify API CI fails for the expected missing production symbols rather than an unrelated tooling failure.

## Task 2 — Implement minimal value objects

Create in `apps/api/src/main/java/io/github/trueruslan/zakupgotov/basket/`:
- `BasketEconomicsKnowledgeStatus.java`
- `BasketFee.java`
- `MinimumOrderConstraint.java`
- `MinimumOrderStatus.java`
- `CheckoutTotalStatus.java`
- `BasketEconomics.java`
- `BasketEconomicsAssessment.java`

Requirements:
- immutable records/enums;
- explicit KNOWN/UNKNOWN invariant;
- known zero valid;
- unknown carries no amount;
- nulls rejected;
- reuse `BasketTotal` validation for non-negative amounts/currency code.

Run targeted tests. Keep implementation minimal; no provider evidence types or ranking.

## Task 3 — Implement calculator GREEN

Create:
- `BasketEconomicsCalculator.java`

Behavior:
1. validate inputs;
2. fail fast on any known component whose currency differs from merchandise subtotal;
3. evaluate minimum-order status from merchandise subtotal only;
4. checkout total is present only if delivery and service fees are both known;
5. add exact `BigDecimal` amounts without hidden rounding/rescaling;
6. preserve original subtotal/economics in the assessment.

Run targeted `BasketEconomicsCalculatorTest` to GREEN, then full API test suite.

## Task 4 — Architecture and regression proof

Update:
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/basket/BasketBoundaryArchitectureTest.java`

Prove the pure economics classes do not depend on provider/acquisition/browser packages and preserve the existing one-way upstream rule.

Run:
- basket tests;
- architecture tests;
- full Maven verification on Java 25 through repository CI.

No live retailer requests are allowed.

## Task 5 — Shipping evidence

Create:
- `docs/superpowers/plans/2026-08-15-m4-1-basket-economics-foundation-shipping.md`

Record:
- baseline;
- issue / PR;
- RED SHA and expected failure;
- GREEN SHA(s);
- final reviewed feature SHA;
- exact PR workflow count/results;
- review verdict;
- implementation merge SHA;
- post-merge workflow count/results.

Update root `CHANGELOG.md` in the feature PR with the implemented M4.1 semantics, but leave `PROJECT_STATE.md` / `ROADMAP.md` canonical acceptance advancement for a separate docs-only PR after the runtime merge is verified.

## Task 6 — Acceptance gate and merge

Before implementation merge:
- exact final feature head;
- exactly 9 normal PR workflow groups, all SUCCESS;
- read-only code review on exact head with no unresolved blocking findings/threads;
- PR mergeable.

Then:
- mark ready;
- squash merge with `expected_head_sha` protection;
- close #133 as completed after merge;
- verify exact merge SHA has exactly 8 normal push workflows, all SUCCESS.

Only then is M4.1 implementation accepted.

## Task 7 — Canonical docs acceptance

From accepted implementation main create a docs-only branch and PR updating only:
- a new M4.1 acceptance document;
- `docs/PROJECT_STATE.md`;
- `docs/ROADMAP.md`;
- root `CHANGELOG.md` only if acceptance bookkeeping requires an additional correction.

Advance M4.2 to NEXT only after implementation acceptance is proven.

Docs PR also requires exact 9/9 PR workflows + clean review, squash merge with expected-head protection, and exact final main 8/8 post-merge workflows.
