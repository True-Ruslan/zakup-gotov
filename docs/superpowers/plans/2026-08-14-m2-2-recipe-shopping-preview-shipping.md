# M2.2 Recipe shopping preview — shipping evidence

Date: 2026-08-14  
Issue: #96  
PR: #97  
Authoritative design: `docs/superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`  
Execution plan: `docs/superpowers/plans/2026-08-13-m2-2-recipe-shopping-preview-v2.md`  
Status: **SHIPPING CANDIDATE — merge/post-merge acceptance pending**

## Scope delivered

- stateless `POST /api/v1/recipe-shopping-previews`;
- server-owned transient Recipe, ingredient and ShoppingList identities;
- request validation before UUID allocation;
- delegation to the accepted M2.1 `RecipeShoppingListConverter` rather than duplicating scaling/merge logic;
- canonical recipe/shopping quantities and self-contained item-local provenance;
- ordered semantic validation errors and sanitized unreadable-body failures;
- local strict integer JSON binding for `baseServings` / `targetServings` without changing global Jackson coercion;
- OpenAPI source contract, regenerated TypeScript schema and API-client path constant;
- architecture boundary `recipepreview -> recipe -> shopping`, with no database/retailer/comparison dependency.

No persistence, retailer traffic, Recipe→Comparison orchestration, fuzzy/AI matching or Recipe UI was added.

## TDD evidence

The branch was developed with test-only RED checkpoints followed by minimal GREEN commits. Important checkpoints include:

| Behavior | RED evidence | GREEN evidence |
|---|---|---|
| package/application boundary | architecture-only RED; API CI run `31719307947` failed on the missing `recipepreview` production boundary | `dce281e8...`; API CI `31719556591` success |
| request mapping / IDs | `1ca2f35e...`; API CI failed on missing M2.2 request/application types | `604caa99...`; API CI `31720734100` success |
| null request | behavioral RED; API CI `31721232997` failed with the pre-fix NPE | `cc8c5508...`; API CI `31721518186` success |
| null top-level fields | `4a9d20ce...`; behavioral RED | `db523655...`; API CI `31722183167` success |
| title / ingredient-count limits | corrected behavioral RED `3d403881...`; API CI `31723065779` failed only on the two absent limits | `967764a2...`; API CI `31723428769` success |
| nested ingredient / quantity validation | test-only RED produced 269 tests with exactly one intended failing contract | `f25bba39216fdf096afef113d1576d12f8c6e135`; nested validation GREEN |
| service/projection | `56cc1387...`; compile RED on missing service/projection surface | `1c810b65...`; API CI `31725775360` success |
| projection fail-closed invariants | `3fecff3f...`; RED for absent corruption boundary | `8d748485938b6302dcfa99fa889a987471bcf655`; API CI `31726289197` success |
| HTTP endpoint / problem contract | corrected test-only RED `79b4d75e...`; API CI `31731899645` failed on missing controller/advice | subsequent HTTP implementation exercised by full API verification |
| strict fractional servings binding | head `03f6daa6...`; API CI `31777261636` ran 279 tests with exactly one failure: `1.5` was incorrectly coerced to integer | local `StrictIntegerDeserializer` introduced and applied; later exact-head API CI passes with production-default Jackson binding |
| OpenAPI/generated client | `7ef2fa82...`; Contract CI failed only because path/operation/response types were absent | OpenAPI + generated schema/client synchronized; exact-head Contract CI success |

Invalid RED attempts caused by test syntax/compile mistakes were explicitly corrected and rerun before production behavior was changed; they are not counted as behavioral RED evidence.

## Backend verification

The full API workflow executes Maven `verify`, including the existing Spring Boot context, Spring Modulith architecture verification and PostgreSQL/Testcontainers/Flyway integration baseline. M2.2 itself is stateless, so no artificial persistence or M2.2-specific Testcontainers production path was introduced merely to increase test count.

At implementation checkpoint `b451dacbec41e3d7bd75ce4580f76fb6f86d5cae`:

- API CI `31777817368`: success;
- Contract CI `31777817391`: success;
- Web CI `31777817425`: success;
- Retailer Bridge CI `31777817443`: success;
- Dependency Review `31777817375`: success;
- CodeQL `31777817399`: success;
- Container Security CI `31777817369`: success;
- Release Contract CI `31777817436`: success;
- Release Bundle CI `31777817374`: success.

The same SHA produced 13/13 successful check runs across all 9 normal PR workflow groups, including separate Web E2E, CodeQL language and container-security jobs.

## Frontend / browser regression

M2.2 introduces no browser-visible Recipe UI. The existing frontend regression gate remains mandatory instead of fabricating a screen only to claim E2E coverage.

At `b451dacb...`, Web CI `31777817425` succeeded for both jobs. `Web E2E` successfully built the production Next.js app, installed Chromium and completed the responsive Playwright regression.

The next real Recipe frontend slice must start RED-first and add desktop/mobile Playwright coverage for recipe editing, servings, ingredient add/remove, quantity/unit input, API errors, generated shopping list and transition into comparison.

## Independent review

Independent read-only review checked the authoritative v2 design, request and response contracts, ID ownership, validation ordering, Jackson binding, conversion delegation, provenance integrity, internal-error propagation, OpenAPI/generated-client synchronization, architecture boundaries, regression surface and security/privacy implications.

Initial verdict: **Caution**, with no P0/P1/P2 findings and one P3 structural drift: unreadable-body handling lived in the controller instead of the controller-scoped advice required by the approved design.

The P3 was corrected before final shipping:

- `RecipeShoppingPreviewController` now only delegates to the application service;
- `RecipeShoppingPreviewExceptionHandler` is the single controller-scoped advice handling both `InvalidRecipeShoppingPreviewRequestException` and `HttpMessageNotReadableException`;
- there is still no catch-all conversion of internal invariant failures to public 400 responses.

After that correction, no known P0/P1/P2/P3 review finding remains. The final exact documentation/cleanup head still requires CI proof because every code or documentation commit invalidates exact-head shipping evidence.

A transient execution-marker file used during branch setup was removed before shipping.

## Final gates still required

Before merge:

1. require all normal PR workflows on the final exact head to succeed;
2. confirm no unresolved review threads or blocking reviews;
3. mark the PR ready for review;
4. squash-merge using the exact reviewed/verified head SHA;
5. require all normal push workflows on the resulting `main` SHA to succeed.

Only after those gates may issue #96 and canonical project state be marked **COMPLETE / ACCEPTED**.

## Post-merge acceptance evidence

Pending.
