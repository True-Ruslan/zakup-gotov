# M2.1 Recipe Domain Shipping and Acceptance Evidence

Date: 2026-08-13  
Issue: #93  
PR: #94  
Status: **COMPLETE / ACCEPTED**

## Approved design and plan

- Design: `docs/superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`
- Implementation plan: `docs/superpowers/plans/2026-08-13-m2-1-recipe-domain.md`
- Accepted code baseline before M2.1: `main=7fc38a8ef5c9866c015a3b63f913d7acee3675f7`

## Accepted scope

M2.1 introduces a separate top-level `recipe` domain and a pure deterministic converter into the accepted Shopping Core.

Accepted semantics:

- immutable Recipe aggregate with stable recipe/ingredient identity;
- normalized non-blank title and positive integer servings;
- existing `ShoppingRequirement` and `Quantity` reused as authoritative requirement/quantity boundaries;
- canonical compatible amounts grouped and summed before serving scaling;
- exact terminating decimal scaling, deterministic `MathContext.DECIMAL128` fallback for non-terminating ratios;
- exact-safe merge only by normalized requirement + canonical unit;
- deterministic list-scoped `ShoppingItemId` independent of amount/target servings;
- ordered deep-immutable `RecipeId + RecipeIngredientId` provenance outside Shopping Core;
- generated-ID collision across different merge keys fails closed;
- no Shopping Core production type depends on Recipe;
- no Spring/network/database/clock/persistence/REST/OpenAPI/web behavior added.

## TDD evidence

### Value objects

- RED `4e8ae28356ca982572bc0dc2150e0fbe3671c9db` — API test compilation failed only because new Recipe value objects did not exist.
- GREEN `15e47e142c784f6356b8237c885e0ddff94d4be8` — full API verification PASS.

### Immutable aggregate

- RED `187f65051421ba75f920e720c1149a64fdedec43` — expected missing `Recipe` / `RecipeIngredient` contract.
- GREEN `360e32515a7e54a4c47e1bd96e88f35c173a55ec` — full API verification PASS.

### Scaling and exact-safe merge

- RED `84a43731c1b9271d9fc387778ee1b62ca2a7d1da` — expected missing Recipe → ShoppingList converter API.
- GREEN `274bde76400b456cb4fff38de39cae4c9aaf09bd` — full API verification PASS for scaling, canonicalization, merge/non-merge, first-occurrence order and DECIMAL128 non-terminating ratios.

### Deterministic identity and provenance

- RED `5feb61e6b50840cf363ecd58cabbc8c374aa243e` — behavioral suite rejected temporary index-based identity and empty provenance.
- GREEN `7e95b8907cc8593c72e81172bd118ae808aa9524` — full API verification PASS for list-scoped key identity, ordered provenance and deep immutability.

### Collision fail-closed

- RED `fb28596377c4dda9c925218080467b7011db53d6` — expected missing package-private ID-derivation seam.
- GREEN `1f36680f051746533f78388b024cb43e01ba62f6` — full API verification PASS with artificial cross-key collision rejection.

### Conversion/lineage validation

- RED `de7ead00e102845d81ad4a5412bab55ef3e97386` — validation contract exposed nullable `RecipeIngredientRef` identities.
- GREEN/reviewed implementation `734ed53712b4327039eabfb358548828aa1a1dbe` — full API verification PASS.

## Architecture verification

`ApplicationArchitectureTest` executes `ApplicationModules.of(ZakupGotovApplication.class).verify()` and passed as part of full API verification.

Accepted dependency direction:

`recipe → shopping`

No Shopping Core production file was changed and no reverse Recipe dependency was introduced.

## Independent review

Reviewed implementation head: `734ed53712b4327039eabfb358548828aa1a1dbe`.

Verdict: **Looks good**.

- P0: none
- P1: none
- P2: none
- review threads: none

Review covered spec compliance, scaling/merge correctness, deterministic identity, provenance, collision behavior, Shopping Core compatibility, architecture direction, security/privacy surface and test coverage.

## Exact-head PR gates

Three successive exact heads passed all 9 required workflow groups:

1. reviewed implementation `734ed53712b4327039eabfb358548828aa1a1dbe` — 9/9 success;
2. code + canonical docs `250d00f10b1c51fee0826356dfb95f8e7b853c50` — 9/9 success;
3. final shipping marker `512be04a2a0147d9787465481388e6847a20d69d` — 9/9 success.

Required groups covered API CI, Contract CI, Web CI/Web E2E, CodeQL Java + JavaScript/TypeScript, Dependency Review, Container Security CI, Retailer Bridge CI, Release Contract CI and Release Bundle CI.

## Merge and post-merge acceptance

PR #94 was squash-merged strictly from final verified head `512be04a2a0147d9787465481388e6847a20d69d`.

Accepted merge/main SHA:

`423eb14f7c565bbe264257a92df89a6b42d0d158`

Post-merge verification on that exact `main` SHA:

- 8 push-triggered workflow runs total;
- 8/8 completed with `conclusion=success`;
- failures: 0.

Issue #93 was then closed with state reason `completed` and an evidence comment referencing the merge and post-merge proof.

This satisfies the final M2.1 acceptance criterion.

## Non-goals preserved

M2.1 does not add:

- REST/OpenAPI/generated client;
- persistence;
- recipe web UI;
- AI/NLP or arbitrary web import;
- fuzzy/case-insensitive ingredient equivalence;
- nutrition optimization;
- pantry prediction;
- fractional servings input;
- multi-recipe aggregation.

## Next design target

M2.2 will design the application/API boundary around:

`Recipe request → Recipe domain → RecipeShoppingListConversion → comparison input`

No lifecycle/persistence/UI choice is implied by M2.1 acceptance. Those decisions require a separate approved design.

Rollback remains a single squash-revert of PR #94; M2.1 introduced no migrations, external side effects or runtime retailer activation.
