# M1 Shopping Core acceptance shipping evidence

Date: 2026-08-13
PR: #92
Issue: #90

## Reviewed candidate

- acceptance/docs SHA: `5c0783c0d91524081f760098d761d30503359e4b`
- accepted pre-acquisition hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`
- decision: **M1 Shopping Core COMPLETE / ACCEPTED; GO to M2 Recipes for deterministic product/core development**

The GO explicitly does not claim that all retailers are production-ready. Connectivity, production-access decisions, browser lifecycle hardening and release proof continue as parallel mandatory work.

## Acceptance evidence

The durable matrix in `docs/m1-shopping-core-acceptance-2026-08-13.md` confirms:

- all 8 canonical retailers remain visible;
- `READY / UNCERTAIN / INCOMPLETE / UNAVAILABLE` remain distinct;
- unmatched, ambiguous, package-unknown and unit-mismatch cases fail safely;
- `UNKNOWN` availability remains uncertain;
- incomplete baskets expose no misleading complete total;
- provenance/freshness/privacy boundaries survive the critical journey;
- provider/store implementation identifiers do not leak into public preview;
- technical connectivity and production access remain independent;
- Magnit remains technically connected but production `BLOCKED`;
- production access is enforced before evidence acquisition;
- an empty production-ready retailer scope skips runtime evidence loading entirely;
- runtime evidence sources receive the exact immutable allowed retailer set;
- evidence outside the requested retailer set fails closed before matching/quote construction;
- production preview does not fall back to deterministic fixtures or hidden live retailer traffic;
- ordinary CI remains retailer-network-free;
- unknown JSON request fields fail closed;
- desktop/mobile Playwright critical journey remains part of the protected gate.

## CI

All nine PR workflow groups passed on `5c0783c0d91524081f760098d761d30503359e4b`:

- API CI — PASS
- Contract CI — PASS
- Web CI / responsive E2E — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- Container Security CI — PASS
- CodeQL Java + JavaScript/TypeScript — PASS
- Release Contract CI — PASS
- Release Bundle CI — PASS

## Review

Verdict: **Looks good**.

- P0: none
- P1: none
- P2: none
- blocking P3: none
- open review threads: none

Review specifically checked that `M2 current` / `GO` is scoped to deterministic product/core development and does not overstate retailer production readiness.

## M2 entry

First vertical slice:

`Recipe → explicit ingredients → canonical quantities → ShoppingList`

Reuse accepted Shopping Core primitives rather than duplicating semantics:

- `Quantity` owns positive quantities and canonical unit conversion;
- `ShoppingRequirement` owns whitespace normalization and blank rejection;
- `ShoppingList` owns ordered item identity/mutation semantics.

The Recipe domain must add only recipe-specific identity, servings/scaling, deterministic equivalence/merge rules and provenance.

Initial non-goals remain AI parsing, arbitrary web import, fuzzy ingredient equivalence, nutrition optimization and pantry prediction.

## Shipping boundary

After the final docs-only marker gate, #92 may merge. M1 becomes canonically complete only after the resulting `main` SHA passes its post-merge push gates. Issue #90 closes only after that verification.
