# M4.2 One-retailer Truthful Total Comparison — Shipping Evidence

**Issue:** #136  
**Implementation PR:** #137  
**Baseline:** `c1c45e4f95d395fe6e63faa9283d8394a18b0557`  
**Branch:** `feat/m4-2-one-retailer-truthful-total`

## Design and plan

Initial semantics-first documents:

- design commit: `c80a907bff76eded6cc587c5ad3b6f9b3a1a98a7`
- implementation-plan commit: `3043cf0a1040f78ae4e44308f49283f569fd1d31`

Review-hardening synchronization after the retailer-binding finding:

- design amendment: `6b338a6b85577a507eecd33f21b42344bdc4f190`
- plan amendment: `91004d2e010b56270a88ac4dee0850c86430d214`

The accepted design keeps four facts separate: existing M1 retailer comparison state, M4.2 checkout eligibility, M4.1 arithmetic checkout-total knowledge and M4.2 comparability. `RetailerComparisonView.total` remains the merchandise subtotal and M4.2 does not select or rank a winner.

M4.1 economics are retailer-neutral, so the final public M4.2 composition boundary additionally binds them to `RetailerId` and rejects cross-retailer mixing before arithmetic.

## TDD evidence

### Core behavior RED

Test-only checkpoint: `11c6df3e1d383f42ee2e6baece7768cfb4df9a1c`.

`RetailerCheckoutAssessmentServiceTest` defined eight behavior scenarios before any production `retailercheckout` type existed.

Draft PR RED proof head: `f5f1458e99e991206d22947e1e8842ec3971d1b2`.

- API CI run: `31899672106`
- API CI job: `95048339246`
- Java 25/toolchain setup: SUCCESS
- `Run API verification`: expected FAILURE in test compilation
- failure reason: `cannot find symbol` for the new `RetailerCheckoutAssessmentService`, eligibility/comparability enums and related M4.2 result types
- no production M4.2 classes existed at the RED checkpoint

### Core behavior GREEN

Initial production implementation commit: `43b41aedc1b02264610091d224be7c236a2170fb`.

This added only the new downstream `retailercheckout` package:

- `RetailerCheckoutEligibilityStatus`
- `RetailerCheckoutComparabilityStatus`
- `RetailerCheckoutAssessment`
- `RetailerCheckoutAssessmentResult`
- `RetailerCheckoutAssessmentService`

The first candidate exposed one local Java generic-inference compile defect: the not-comparable ternary branch inferred `Optional<Object>`. No domain semantics were changed.

Compile-fix / behavior-GREEN head: `43016a5caff19c2931c5771136c4ff2cfddf0a81`.

API CI run `31899826664`, job `95048709488`:

- status: **SUCCESS**
- new M4.2 behavior tests: 8/8 PASS
- full API verification: **415 tests, 0 failures, 0 errors, 5 intentionally skipped live probes**
- Maven result: **BUILD SUCCESS**

### Impossible-state hardening

Invariant-test commit: `07a9c023de646f6d96cefdf26363b50de657b2f3`.

`RetailerCheckoutAssessmentInvariantTest` proves public objects reject:

- economics/comparison merchandise-subtotal drift;
- forged eligibility;
- forged comparability;
- comparable-total drift;
- checkout assessment for an INCOMPLETE comparison;
- missing assessment for an assessable comparison;
- assessment/result cross-comparison drift;
- mixed-currency M4.1 economics through the M4.2 service.

The existing self-validating production constructors already rejected these states, so this hardening suite was GREEN without a production correction. No artificial second RED was introduced.

### Initial architecture proof

Architecture-test commit: `c1d69895d4a29205d54c6afebfdc3469505a67c7`.

Temporary M4.2 breadcrumb files were removed before review. Clean pre-review runtime/proof head: `38e8a9fe2d9caa3ff9d653d394ef96e490063169`.

API CI run `31900066704`, job `95049298411` on that clean head: **SUCCESS**.

## Read-only review finding and corrective TDD

Read-only review found a correctness gap: the first public service signature accepted bare `BasketEconomics`. Because M4.1 economics are retailer-neutral, a caller could otherwise apply fee/minimum-order evidence for one retailer to another retailer comparison without detection. This violates the project's fail-closed cross-retailer evidence rule.

### Retailer binding RED

Test-only finding checkpoint: `fea5e00e8bcd333dc0b66c8792777da6cfee1a97`.

`RetailerCheckoutEconomicsBindingTest` requires:

- matching retailer-bound economics compose normally;
- cross-retailer economics fail closed before checkout arithmetic.

API CI run `31900225908`, job `95049704905`:

- Java 25/toolchain setup: SUCCESS
- expected test compilation FAILURE
- exact reason: missing `RetailerCheckoutEconomicsEvidence`
- two `cannot find symbol` errors, both from the new binding test

### Retailer binding GREEN

Corrective implementation chain:

- `1d3f69e7d4f8b5c03bb52cf71a9c42acf4cb9e87` — add `RetailerCheckoutEconomicsEvidence(RetailerId, BasketEconomics)`;
- `076b9b2a35fbb538bada70f95e28324ac26f2849` — make the public service boundary require retailer-bound economics and reject identity mismatch before arithmetic;
- `ac876811f2a3696c9160765a0b4f872ce058a146` — harden architecture so the only direct retailer-domain dependency is `RetailerId`, while `basket`, `comparison` and `retailer` remain free of reverse `retailercheckout` dependencies.

The former bare-economics overload is no longer public; it remains package-private only as the internal arithmetic/test seam after the public binding check.

API CI run `31900314015`, job `95049960731` on `ac876811...`: **SUCCESS**.

No provider, acquisition or fulfillment identifier was introduced.

## Final implementation semantics awaiting immutable PR gate

- retailer-bound economics must match `RetailerComparisonView.retailerId` before calculation;
- READY + minimum MET + known fees -> ELIGIBLE and COMPARABLE with exact M4.1 checkout total;
- READY + minimum NOT_MET -> INELIGIBLE even when arithmetic checkout total is known;
- READY + minimum UNKNOWN -> UNKNOWN eligibility and NOT_COMPARABLE;
- READY + minimum MET + unknown material fee -> ELIGIBLE but arithmetic checkout total UNKNOWN and NOT_COMPARABLE;
- UNCERTAIN is never upgraded to comparable; a known NOT_MET minimum may still prove INELIGIBLE;
- INCOMPLETE / UNAVAILABLE produce no fabricated checkout assessment;
- known zero fees remain known zero;
- M1 merchandise subtotal, retailer visibility, production-access and uncertainty semantics remain unchanged;
- no provider acquisition, HTTP/OpenAPI/UI change, sorting, cheapest claim or winner selection is introduced.

## Final gate

This document is committed before the immutable final PR gate. Acceptance requires the resulting exact PR head to have:

- exactly 9 normal PR workflow groups;
- 9/9 SUCCESS, 0 failure/skipped/cancelled;
- clean read-only review with no P0/P1/P2/P3/nitpicks;
- zero unresolved review threads;
- mergeable=true;
- expected-head protected squash merge;
- exact implementation merge with 8/8 normal push workflows SUCCESS.

The separate canonical acceptance record after merge will capture the final reviewed head and merge SHA.
