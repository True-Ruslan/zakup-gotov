# M4.1 Basket Economics Foundation — Acceptance

**Accepted:** 2026-08-15  
**Issue:** #133  
**Implementation PR:** #134  
**Accepted baseline:** `37ec650e59ede8773cb1c1258e70be341bfba7ef`  
**Implementation merge:** `3ccaa7b2acc1e81d7360c55872882a4252c96cae`

## Result

M4.1 is **COMPLETE / ACCEPTED**.

The basket domain now has an explicit, fail-closed economics vocabulary that can distinguish merchandise subtotal from effective checkout-total knowledge before any retailer ranking is introduced.

Accepted semantics:

- the existing `BasketTotal(BigDecimal, ISO-4217)` monetary convention is reused;
- delivery and service fees are explicitly `KNOWN` or `UNKNOWN`;
- known zero is a real zero-fee state and is never conflated with unknown;
- minimum-order threshold is explicitly known or unknown;
- known minimum-order thresholds are evaluated against **merchandise subtotal only**;
- minimum-order assessment is `MET / NOT_MET / UNKNOWN`;
- checkout total is known only when both material fee components are known;
- an unknown material fee fails closed: checkout total is absent/unknown while merchandise subtotal remains inspectable;
- known monetary components must use the merchandise-subtotal currency; mixed-currency assessment fails fast;
- arithmetic is exact `BigDecimal` addition/comparison with no hidden rounding or rescaling;
- `BasketEconomicsAssessment` is self-validating and cannot be manually forged with contradictory minimum-order status, checkout-total knowledge or checkout-total amount;
- the new M4.1 economics types remain pure basket-domain code and do not acquire provider, matching, retailer or comparison data.

Accepted M1 `SingleStoreBasketPlanner` / `SingleStoreBasketQuote` semantics are unchanged. M4.1 adds no optimizer, provider/browser/network work, HTTP/OpenAPI contract or UI.

## Design and plan

- design: [`superpowers/plans/2026-08-15-m4-1-basket-economics-foundation-design.md`](superpowers/plans/2026-08-15-m4-1-basket-economics-foundation-design.md)
- implementation plan: [`superpowers/plans/2026-08-15-m4-1-basket-economics-foundation.md`](superpowers/plans/2026-08-15-m4-1-basket-economics-foundation.md)
- shipping evidence: [`superpowers/plans/2026-08-15-m4-1-basket-economics-foundation-shipping.md`](superpowers/plans/2026-08-15-m4-1-basket-economics-foundation-shipping.md)

## TDD / verification evidence

### Initial behavior RED

`ce425113bb8fc573748cf015f2f4ccf6036733fd`

Only the new economics behavior test existed. API CI run `31897515594` failed in `Run API verification`; annotations reported `cannot find symbol` for the deliberately absent M4.1 production types.

### Initial production implementation

`6a582dabe244ed3a6aec2542cb1634c7b460c647`

Added the minimal immutable knowledge/fee/minimum/economics/assessment/calculator model.

### Proof-harness hardening

- `d2b02131f4792f1bb1a8032b2a61b0d3fbd752f4` added value-object and architecture proof and exposed a Java compact-record lambda-capture compile issue;
- `8d9c11d343b8f69388b721939f8e96918cb5411d` fixed only that compile issue and exposed an unsupported ArchUnit convenience predicate in the proof;
- `ed325da335e550e634cbc65a5dcb85cf7b1ba43d` replaced it with an explicit fixed-set `DescribedPredicate<JavaClass>`; API CI run `31897846179` completed SUCCESS.

### Review-driven invariant RED

`2eff6ef1bb22a7ef68d6297070599548a136ffa6`

After the first green implementation, read-only review identified that callers could manually construct a contradictory public `BasketEconomicsAssessment`. A test-only commit added required rejection cases for:

- minimum-order status contradicting the known threshold;
- `KNOWN` checkout status despite an unknown material fee;
- checkout amount contradicting subtotal + known fees.

API CI failed on this test-only head while the preceding production/proof head was green. GitHub exposed only generic failed-check annotations for this second RED and the job-log endpoint did not provide a useful assertion transcript; the causal evidence is therefore the isolated test-only diff plus the immediately preceding and following green checkpoints, not an invented log excerpt.

### Final invariant GREEN / reviewed head

`a0fcd626017f93e49fc6a70c4403b68404efe6d7`

The public assessment constructor now recomputes and validates the expected minimum-order status, checkout-total status and checkout amount using the same deterministic rules as the calculator.

Exact final PR gate on this head:

- **9/9 standard PR workflow groups SUCCESS**;
- failure/skipped/cancelled: 0;
- read-only review: **Looks good**;
- P0/P1/P2/P3: none;
- nitpicks: none;
- unresolved review threads: 0;
- PR mergeable.

PR #134 was marked ready and squash-merged with `expected_head_sha` protection.

## Post-merge acceptance

Implementation merge:

`3ccaa7b2acc1e81d7360c55872882a4252c96cae`

Issue #133 is closed as `completed`.

Exact implementation merge SHA has **8/8 normal push workflow groups SUCCESS**. A GitHub Actions query scoped to this head and `status=success` returns `total_count: 8`; no failed/skipped/cancelled merge workflow exists for the accepted head.

Therefore the implementation state is:

**implemented → tested → reviewed → merged → accepted**.

## Mandatory M4.2 constraint

M4.2 must keep **arithmetic checkout-total knowledge** separate from **retailer eligibility**.

A retailer may have a mathematically known checkout total while its minimum-order status is `NOT_MET` or `UNKNOWN`. Such a retailer must not be treated as an eligible winner merely because arithmetic is possible.

At minimum, M4.2 must preserve these distinctions:

- known checkout + minimum `MET` → potentially eligible, subject to existing basket completeness/uncertainty/access rules;
- known checkout + minimum `NOT_MET` → ineligible;
- known checkout + minimum `UNKNOWN` → eligibility unknown, never silently eligible;
- unknown checkout because of unknown material fee → total not truthfully comparable as cheapest;
- existing `INCOMPLETE / UNCERTAIN / UNAVAILABLE` and production-access semantics remain authoritative and cannot be bypassed by the economics layer.

No live retailer acquisition is required to prove this composition in CI.