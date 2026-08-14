# M2.3 Recipe → Comparison Preview — Acceptance Decision

Date: 2026-08-14  
Issue: #100  
PR: #101  
Status: **COMPLETE / ACCEPTED**

## Decision

M2.3 is accepted as the deterministic Recipe → Comparison composition boundary.

Accepted product path:

`Recipe input + locality → RecipeShoppingPreview → generated canonical Shopping requirements → ComparisonPreview`

Public endpoint:

`POST /api/v1/recipe-comparison-previews`

The composed response returns the accepted self-contained Recipe shopping preview together with the accepted product-safe retailer comparison preview.

## Authoritative design and execution evidence

- Design: [`superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md`](superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md)
- Implementation plan: [`superpowers/plans/2026-08-14-m2-3-recipe-comparison-preview.md`](superpowers/plans/2026-08-14-m2-3-recipe-comparison-preview.md)
- Shipping evidence: [`superpowers/plans/2026-08-14-m2-3-recipe-comparison-preview-shipping.md`](superpowers/plans/2026-08-14-m2-3-recipe-comparison-preview-shipping.md)

## Accepted implementation

Accepted squash merge:

`15a086d135f40277c655b39549c3e7a04c2e914e` — `feat(m2): compose recipe into comparison preview (#101)`

Accepted behavior:

- the composition delegates Recipe validation, transient ID ownership, serving scaling, canonicalization, exact-safe merging and ingredient provenance to the accepted M2.2/M2.1 Recipe boundary;
- generated ShoppingItem UUID, order, normalized requirement and canonical quantity are preserved unchanged when entering comparison;
- comparison validation, production-access scoping, runtime evidence loading, matching, basket planning and retailer-state assembly remain owned by the accepted M1 comparison boundary;
- the composition verifies item cardinality, identity/order, requirement and canonical-quantity equality after comparison projection and fails closed on impossible drift;
- Recipe provenance remains self-contained in `recipeShoppingPreview`; comparison does not duplicate or reinterpret Recipe lineage;
- wrapper binding errors use sanitized `INVALID_RECIPE_COMPARISON_PREVIEW`, while nested Recipe and Comparison semantic errors retain their existing problem vocabularies;
- OpenAPI 3.1 remains the source of truth and the generated TypeScript client exports `RECIPE_COMPARISON_PREVIEWS_PATH` plus generated request/response types;
- the composer has no direct dependency on Recipe domain, provider, retailer, matching, basket, comparison domain or persistence/database packages;
- the only direct Shopping value bridge is the finite architecture-tested `Quantity` / `QuantityUnit` pair required by the accepted application DTO signatures;
- ordinary CI remains retailer-network-free;
- no retailer was activated and no existing production-access policy was weakened.

## TDD and review proof

The implementation retained explicit RED → GREEN checkpoints for:

- application composition;
- HTTP/controller contract;
- OpenAPI/generated-client contract.

Additional fail-closed tests cover cross-boundary cardinality, identity/order, requirement and quantity drift.

Final reviewed PR head:

`b6575f03b668f8bbaacd5b2897c4fb9301d94cdf`

On that exact head all **9/9 normal PR workflow groups succeeded**:

- API CI;
- Contract CI;
- Web CI / Playwright regression;
- CodeQL;
- Dependency Review;
- Container Security CI;
- Retailer Bridge CI;
- Release Contract CI;
- Release Bundle CI.

Independent read-only review verdict: **Looks good / REVIEWED_READY**. No unresolved P0/P1/P2 findings remained and review threads were empty.

## Post-merge acceptance proof

PR #101 was squash-merged from the reviewed exact head to:

`main=15a086d135f40277c655b39549c3e7a04c2e914e`

GitHub created exactly eight normal push-triggered workflows for that merged SHA.

Acceptance result:

- 8/8 normal post-merge push workflows completed successfully;
- failures: 0;
- issue #100 closed with state reason `completed`.

Therefore M2.3 is **COMPLETE / ACCEPTED**.

## Explicit non-goals preserved

M2.3 did not add:

- Recipe persistence, CRUD, history or saved recipes;
- Recipe UI;
- fuzzy, synonym, semantic or AI ingredient matching;
- retailer/provider activation or new acquisition transport;
- exact-address handling;
- database migrations;
- multi-recipe aggregation;
- nutrition, pantry or ranking redesign.

## Next target

The next deterministic product slice is **M2.4 — Responsive Recipe UI**.

The UI should use `POST /api/v1/recipe-comparison-previews` as its primary product boundary and be developed RED-first with frontend component tests plus desktop/mobile Playwright coverage. Persistence, fuzzy/AI ingestion and multi-recipe aggregation remain separate decisions.
