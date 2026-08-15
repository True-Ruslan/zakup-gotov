# M4.3 Deterministic Basket Optimizer — Shipping Evidence

**Issue:** #139  
**Implementation PR:** #140  
**Baseline:** `b32c461eb49eefa5ab37f23d45491e9f46356c10`  
**Branch:** `feat/m4-3-deterministic-basket-optimizer`

## Design and plan

- initial design: `dae2eae96c0dfeffa9f6ed2e5251ee4deb2912d0`
- initial plan: `86b88ec2badbe5b0e43626a8a7f5bdc79d56d098`
- architecture-hardening design amendment: `836e2669f1b0b52ea5ed289fa0c428c6ce2b4990`
- architecture-hardening plan amendment: `2f685b934a055ebc6145daecb1e634f8a9ff742a`

The final design keeps M4.3 a pure selection layer over accepted M4.2 checkout assessments. It never recomputes M1 packages/substitutions, never converts freshness into a hidden price penalty and never breaks a monetary tie by retailer/freshness/provider metadata.

## Core TDD evidence

### RED — optimizer behavior contract

Test-only checkpoint:

`e6923b46aa80a4ca8c9536568f8072be7c4f7c53`

Draft PR #140 API CI:

- run: `31901576243`
- job: `95053058883`
- Java 25/toolchain setup: SUCCESS
- expected test-compilation FAILURE
- exact cause: missing `BasketOptimizer` / `BasketOptimizationStatus` production symbols
- no `basketoptimization` production package existed at the RED checkpoint.

The RED contract covers unique winner, explicit numeric tie, no-comparable state, exclusion of cheaper ineligible/uncertain candidates, comparable-currency failure, non-comparable foreign-currency tolerance, freshness-neutral ties and preservation of input order.

### GREEN — minimal deterministic optimizer

Initial production commit:

`0df063bd00f37c1e6ed02831b8fc0b67dc3635a9`

Added:

- `BasketOptimizationStatus`
- package-private `BasketOptimizationEvaluation`
- package-private `BasketOptimizationRules`
- self-validating `BasketOptimizationResult`
- `BasketOptimizer`

The first compile candidate used the wrong accepted `BasketTotal` accessor (`currency()` instead of `currencyCode()`). This was a compile-only defect with no semantic change.

Behavior-GREEN head:

`6ccf4296c7112b7171a9ad08ef8c619fb3abbb3e`

API CI:

- run: `31901741891`
- job: `95053464294`
- status: **SUCCESS**
- optimizer behavior tests: 8/8 PASS
- full accepted upstream regression suite remained green.

## Invariant hardening

Invariant test commit:

`567261da582fe1e5385bbc0a8042c1c187485c03`

`BasketOptimizationInvariantTest` proves:

- empty input fails closed;
- null candidate fails closed;
- duplicate retailer IDs fail closed;
- wrong optimization status cannot be forged;
- a real tie cannot be collapsed into a hidden winner;
- a unique minimum cannot be inflated into a tie;
- non-comparable/non-minimum candidates cannot be forged as optimal;
- tied minima must be complete and retain original order;
- mixed comparable currencies fail closed;
- candidate lists are defensively copied.

The shared deterministic rules already enforce these states, so no artificial invariant RED was manufactured.

## Architecture proof and corrective TDD

Initial architecture-test head:

`0c607f121682738b795a41cfae995446a7d3164f`

API CI:

- run: `31901840678`
- job: `95053726783`
- optimizer behavior: 8/8 PASS
- optimizer invariants: 10/10 PASS
- full suite: **450 tests, 2 failures, 0 errors, 5 intentionally skipped live probes**
- both failures were architecture-only.

ArchUnit found a real abstraction leak: duplicate-retailer validation called `candidate.comparison().retailerId()`, creating a direct M4.3 dependency on the accepted M1 `comparison` layer. The architecture test also proved there was no actual direct `retailer` dependency, so weakening the boundary to allow `comparison` would have been incorrect.

### RED — M4.2-owned retailer identity projection

Targeted test-only checkpoint:

`06b55b1f71bf2cb4bc5750384a0892020a5017d4`

API CI:

- run: `31901963844`
- job: `95053998802`
- Java 25/toolchain setup: SUCCESS
- expected test-compilation FAILURE
- exact cause: missing `RetailerCheckoutAssessmentResult.retailerId()`.

### GREEN — remove M4.3 direct comparison/retailer dependency

Corrective chain:

- `187b7f41cb3b7e75c2c4df5adecc7262745f7065` — additive, non-semantic M4.2 `retailerId()` projection;
- `d24604e7a0091e05fecc985ebf80952266852221` — M4.3 consumes retailer identity through the M4.2 boundary instead of `comparison()`;
- `f410d175c535100b091882b8495a55138ee9c7db` — remove the redundant source-level direct `RetailerId` generic;
- `cab385ff194c295a073862a35f8ff088e2d45579` — final architecture guard requires no direct M4.3 dependency on either `comparison` or `retailer`.

API CI on `cab385ff...`:

- run: `31902117168`
- job: `95054389341`
- status: **SUCCESS**
- behavior and invariant tests remain green;
- M4.2 identity-projection regression passes;
- architecture guards pass.

The M4.2 addition is intentionally non-semantic: `retailerId()` returns the retailer identity already owned by the embedded accepted `RetailerComparisonView`; it does not alter M4.2 eligibility, comparability, economics or validation.

## Final implemented semantics awaiting immutable PR gate

- optimizer input is a non-empty ordered list of M4.2 results with unique retailer identities;
- all original candidates remain visible in original order;
- only explicit M4.2 `COMPARABLE` candidates compete;
- comparable candidates must use one currency;
- exact `BigDecimal.compareTo` ordering is used with no rounding/rescaling;
- numerically equal minima are an explicit `TIE`, including different decimal scales;
- all tied minima remain in original input order;
- no input/canonical retailer order is a tie-break;
- freshness/provider timestamps/package/SKU evidence never break a tie or become hidden price penalties;
- cheaper ineligible/unknown/uncertain/incomplete/unavailable evidence cannot win;
- no package/SKU/substitution recomputation, provider acquisition, HTTP/OpenAPI/UI or multi-store split is introduced;
- `BasketOptimizationResult` recomputes and validates its own deterministic status/optimal candidate set;
- final M4.3 production dependencies are accepted M4.2 plus neutral `BasketTotal`; no direct `comparison` or `retailer` dependency exists.

## Final gate

This evidence document is committed before the immutable final PR gate. Acceptance still requires the resulting exact PR head to prove:

- exactly 9 normal PR workflow groups;
- 9/9 SUCCESS, 0 failure/skipped/cancelled;
- clean read-only review with no P0/P1/P2/P3/nitpicks;
- zero unresolved review threads;
- mergeable=true;
- expected-head protected squash merge;
- exact implementation merge with 8/8 normal push workflows SUCCESS.

Canonical project-state/roadmap/changelog acceptance remains a separate post-merge docs PR.
