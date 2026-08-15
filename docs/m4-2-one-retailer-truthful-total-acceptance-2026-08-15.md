# M4.2 One-retailer Truthful Total Comparison — Acceptance

**Accepted:** 2026-08-15  
**Issue:** #136  
**Implementation PR:** #137  
**Accepted baseline:** `c1c45e4f95d395fe6e63faa9283d8394a18b0557`  
**Final reviewed feature head:** `1d6dae470c04ab1d8279f891766fc16698286edb`  
**Implementation merge:** `69f9cb1afd1b16af938052bbca570cbd4ce52557`

## Result

M4.2 is **COMPLETE / ACCEPTED**.

Zakup Gotov now has a deterministic one-retailer checkout assessment layer over accepted M1 retailer comparison and accepted M4.1 basket economics. It exposes enough evidence for a future optimizer to distinguish arithmetic knowledge from actual candidate eligibility without changing M1 merchandise-subtotal semantics or selecting a winner prematurely.

## Accepted boundary

New downstream package:

`io.github.trueruslan.zakupgotov.retailercheckout`

Public composition boundary:

`RetailerCheckoutAssessmentService.assess(RetailerComparisonView, RetailerCheckoutEconomicsEvidence)`

`RetailerCheckoutEconomicsEvidence` binds otherwise retailer-neutral M4.1 economics to a `RetailerId`. The service requires that retailer identity to equal `RetailerComparisonView.retailerId` **before** invoking M4.1 arithmetic. Cross-retailer fee/minimum-order evidence therefore fails closed rather than contaminating another retailer's assessment.

No provider, acquisition or fulfillment identifier becomes public. The only direct retailer-domain bridge is `RetailerId`.

## Accepted semantics

### Existing M1 comparison remains authoritative

- `RetailerComparisonView.total` continues to mean **merchandise subtotal**;
- M4.2 does not rename, overwrite or reinterpret that field as checkout total;
- existing retailer coverage, production-access, matching, package, availability and freshness states remain authoritative;
- accepted `READY / UNCERTAIN / INCOMPLETE / UNAVAILABLE` behavior remains unchanged.

### Checkout eligibility is separate from arithmetic knowledge

`RetailerCheckoutEligibilityStatus`:

- `ELIGIBLE`
- `INELIGIBLE`
- `UNKNOWN`

Deterministic rules:

1. known minimum order `NOT_MET` -> `INELIGIBLE`, including an otherwise `UNCERTAIN` basket;
2. otherwise upstream `UNCERTAIN` -> `UNKNOWN`;
3. otherwise minimum order `UNKNOWN` -> `UNKNOWN`;
4. `READY + MET` -> `ELIGIBLE`.

A retailer may therefore be `ELIGIBLE` while checkout total is still unknown because a material fee is unknown. Conversely, a fully known arithmetic checkout total is not enough to make an unmet or uncertain candidate eligible.

### Comparability is stricter than eligibility

`RetailerCheckoutComparabilityStatus`:

- `COMPARABLE`
- `NOT_COMPARABLE`

A candidate is `COMPARABLE` only when all are true:

- upstream comparison is `READY`;
- eligibility is `ELIGIBLE`;
- M4.1 checkout-total status is `KNOWN`;
- M4.1 checkout total is present.

Only `COMPARABLE` assessments expose `comparableCheckoutTotal`, and that value must equal the exact M4.1 checkout total. Known arithmetic totals for `INELIGIBLE`, `UNKNOWN` or upstream `UNCERTAIN` states remain inspectable inside the economics assessment but cannot become a cheapest/winner candidate.

### No fabricated checkout state

- `INCOMPLETE` and `UNAVAILABLE` retailer comparisons have no merchandise subtotal under accepted M1 semantics;
- M4.2 therefore returns no checkout assessment for those states instead of fabricating subtotal, eligibility, fees or checkout total;
- known zero delivery/service fees remain real `KNOWN` zero values through composition;
- mixed-currency and contradictory M4.1 economics continue to fail fast.

## Self-validating public model

`RetailerCheckoutAssessment` rejects:

- non-assessable upstream comparison states;
- merchandise-subtotal drift between comparison and economics assessment;
- forged eligibility;
- forged comparability;
- incorrect comparable-total presence or amount.

`RetailerCheckoutAssessmentResult` rejects assessment-presence drift and an assessment derived from another retailer comparison value.

The public service exposes no raw/unbound economics composition path. After retailer validation, raw arithmetic is reachable only through a private helper.

## Architecture

Accepted architecture proves:

- `retailercheckout` depends only on accepted `basket`, accepted `comparison`, JDK types and the finite `RetailerId` bridge;
- no provider, matching, shopping, location, preview, persistence/database, Spring, jOOQ, Recipe, WeeklyPlan or Pantry coupling;
- accepted `basket`, `comparison` and `retailer` packages do not depend back on `retailercheckout`;
- M4.2 performs no live retailer/network acquisition.

## Design and implementation evidence

- design: [`superpowers/specs/2026-08-15-m4-2-one-retailer-truthful-total-design.md`](superpowers/specs/2026-08-15-m4-2-one-retailer-truthful-total-design.md)
- implementation plan: [`superpowers/plans/2026-08-15-m4-2-one-retailer-truthful-total.md`](superpowers/plans/2026-08-15-m4-2-one-retailer-truthful-total.md)
- shipping evidence: [`superpowers/plans/2026-08-15-m4-2-one-retailer-truthful-total-shipping.md`](superpowers/plans/2026-08-15-m4-2-one-retailer-truthful-total-shipping.md)

### Core RED -> GREEN

- behavior RED test-only checkpoint: `11c6df3e1d383f42ee2e6baece7768cfb4df9a1c`;
- RED proof head: `f5f1458e99e991206d22947e1e8842ec3971d1b2`, API CI failed exactly on missing M4.2 symbols;
- behavior GREEN head: `43016a5caff19c2931c5771136c4ff2cfddf0a81`, full API CI SUCCESS with 415 tests, 0 failures, 0 errors and 5 intentionally skipped live probes;
- invariant hardening: `07a9c023de646f6d96cefdf26363b50de657b2f3`;
- initial architecture proof: `c1d69895d4a29205d54c6afebfdc3469505a67c7`.

### Review-driven retailer-binding RED -> GREEN

Read-only review found that the first public service signature accepted bare retailer-neutral `BasketEconomics`, allowing theoretical cross-retailer fee/minimum evidence mixing.

- corrective RED: `fea5e00e8bcd333dc0b66c8792777da6cfee1a97`, API CI failed exactly on missing `RetailerCheckoutEconomicsEvidence`;
- retailer-bound evidence: `1d3f69e7d4f8b5c03bb52cf71a9c42acf4cb9e87`;
- public mismatch validation before arithmetic: `076b9b2a35fbb538bada70f95e28324ac26f2849`;
- finite `RetailerId` architecture bridge: `ac876811f2a3696c9160765a0b4f872ce058a146`, API CI SUCCESS;
- final structural hardening removed the remaining package-visible raw-economics seam through `b8ce1d38bce578f43c7d3c491efdb1cdd5e37a6f` -> `22addde45c792f4ec62644351c24acfd3f1173a2` -> `239e1e774d7217e61e5d5959092e8dafede2c19f`, with API CI SUCCESS.

## Final acceptance proof

Final reviewed feature head:

`1d6dae470c04ab1d8279f891766fc16698286edb`

On that exact head:

- exactly **9 normal PR workflow groups**;
- **9/9 SUCCESS**;
- 0 failure/skipped/cancelled;
- API CI SUCCESS;
- CodeQL SUCCESS;
- read-only review: **Looks good**;
- no P0/P1/P2/P3/nitpicks;
- unresolved review threads: **0**;
- PR mergeable;
- final diff contains only the new M4.2 production/test/docs files and does not modify accepted M1/M4.1 production types.

Squash merge with expected-head protection:

`69f9cb1afd1b16af938052bbca570cbd4ce52557`

Post-merge evidence:

- `main` points to the exact implementation merge;
- issue #136 closed as `completed`;
- exactly **8 normal push workflows** on the merge SHA;
- **8/8 SUCCESS**, including CodeQL Java and JavaScript/TypeScript;
- 0 failures.

## Decision

**ACCEPT M4.2.**

Status:

**implemented -> tested -> reviewed -> merged -> accepted**

Canonical documentation is updated in the follow-up docs PR.

## Next deterministic target

**M4.3 — Basket optimizer.**

M4.3 may rank/select only M4.2 `COMPARABLE` candidates. It must define deterministic candidate ordering and tie semantics, keep `INELIGIBLE / UNKNOWN / NOT_COMPARABLE / INCOMPLETE / UNAVAILABLE` candidates out of winner selection, and explicitly design package/substitution plus confidence/freshness policy before exposing any cheapest/winner claim. Deterministic acceptance must remain supplied-evidence-only with no live retailer request.
