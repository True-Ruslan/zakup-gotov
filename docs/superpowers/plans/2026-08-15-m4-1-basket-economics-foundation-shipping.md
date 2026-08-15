# M4.1 Basket Economics Foundation — Shipping Evidence

**Date:** 2026-08-15  
**Issue:** #133  
**Implementation PR:** #134  
**Accepted baseline:** `37ec650e59ede8773cb1c1258e70be341bfba7ef`

## Scope

M4.1 adds a pure basket-domain economics foundation only. It does not rank retailers, acquire provider data, alter accepted M1 quote semantics, or add HTTP/OpenAPI/UI contracts.

Implemented semantics:

- explicit `KNOWN / UNKNOWN` knowledge for delivery and service fees;
- genuine known-zero fee distinct from unknown;
- explicit known/unknown minimum-order constraint;
- deterministic `MET / NOT_MET / UNKNOWN` minimum-order status from merchandise subtotal only;
- merchandise subtotal retained independently from checkout-total knowledge;
- checkout total known only when both material fee components are known;
- exact `BigDecimal` arithmetic with no hidden rounding/rescaling;
- fail-fast mixed-currency economics;
- architecture guard keeping the new economics foundation out of provider/matching/retailer/comparison acquisition paths.

## TDD evidence

### RED

Commit: `ce425113bb8fc573748cf015f2f4ccf6036733fd`

Only `BasketEconomicsCalculatorTest` was added after the design/plan. API CI run `31897515594` failed in `Run API verification`; check annotations pointed at the new test with `cannot find symbol` for the not-yet-existing M4.1 production types. This is the expected behavior-level RED.

### Production implementation

Commit: `6a582dabe244ed3a6aec2542cb1634c7b460c647`

Added the minimal immutable value objects/statuses plus `BasketEconomicsCalculator` while preserving the existing `BasketTotal(BigDecimal, ISO-4217)` convention.

### Proof/hardening checkpoints

- `d2b02131f4792f1bb1a8032b2a61b0d3fbd752f4` added value-object and architecture proof; API CI exposed a Java compact-record lambda-capture compile error in `BasketEconomicsAssessment`.
- `8d9c11d343b8f69388b721939f8e96918cb5411d` removed that lambda capture without changing semantics; API CI then exposed an unsupported ArchUnit convenience predicate in the new proof only.
- `ed325da335e550e634cbc65a5dcb85cf7b1ba43d` replaced it with an explicit fixed-set `DescribedPredicate<JavaClass>`; API CI run `31897846179` completed `SUCCESS` on Java 25.

The intermediate failures were compile/proof-harness defects, not weakened business semantics; both were fixed directly and retained as evidence in Git history.

## Acceptance criteria mapped to tests

`BasketEconomicsCalculatorTest` proves:

1. known positive delivery + service fees produce a known checkout total;
2. known zero fees remain known and do not become unknown;
3. unknown delivery fee fails closed while retaining merchandise subtotal;
4. unknown service fee fails closed even with known-zero delivery;
5. known minimum-order threshold is evaluated from merchandise subtotal only;
6. known thresholds produce `MET` and `NOT_MET` deterministically;
7. unknown threshold produces `UNKNOWN`;
8. exact decimal arithmetic preserves scale and adds no hidden rounding;
9. known fee/threshold currency mismatch fails fast;
10. calculator null inputs fail fast.

`BasketEconomicsValueObjectsTest` proves impossible knowledge shapes and negative known amounts are rejected.

`BasketBoundaryArchitectureTest` preserves the accepted upstream→basket dependency direction and explicitly prevents the fixed M4.1 economics type set from depending on provider, matching, retailer or comparison packages.

## Final gate

The final PR workflow count, exact reviewed head, review verdict, squash merge SHA and post-merge 8/8 evidence are intentionally recorded in the post-merge M4.1 acceptance document. This file cannot truthfully self-record a future commit SHA before that commit exists.

Canonical `PROJECT_STATE.md`, `ROADMAP.md` and acceptance-level `CHANGELOG.md` advancement are also deferred until the implementation merge is verified on `main`; this prevents documentation from claiming acceptance before the gate exists.
