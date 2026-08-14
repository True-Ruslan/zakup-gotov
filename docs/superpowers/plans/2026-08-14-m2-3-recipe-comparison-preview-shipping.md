# M2.3 Recipe → Comparison Preview — Shipping Evidence

Date: 2026-08-14  
Issue: #100  
PR: #101  
Baseline: `main=bcfa16e1497f72cc36aa379e0effb75b0c2f3532`  
Reviewed implementation/pre-shipping head: `fb6e9bf583d0da230c30b77fb9245b0552be823b`  
Status: **SHIPPING CANDIDATE — not COMPLETE / ACCEPTED until squash merge and post-merge main proof**

## Delivered boundary

M2.3 adds the stateless composed application/API boundary:

`POST /api/v1/recipe-comparison-previews`

Flow:

`Recipe request + locality → accepted RecipeShoppingPreviewService → generated canonical Shopping items → accepted ComparisonPreviewService → self-contained Recipe provenance + product-safe comparison projection`

The composition preserves generated ShoppingItem UUID, order, normalized requirement and canonical quantity from the Recipe shopping projection into comparison. It does not duplicate Recipe scaling/merge/provenance rules or matching/basket/comparison rules.

Production-access enforcement remains inside the accepted ComparisonPreviewService and scopes retailer evidence acquisition before the runtime evidence source can execute. M2.3 adds no retailer activation, provider adapter, persistence, database migration, exact-address handling, fuzzy/AI semantics or browser-visible Recipe UI.

## Architecture result

Production package: `io.github.trueruslan.zakupgotov.recipecomparisonpreview`.

Direct application dependencies are the accepted `recipepreview` and `preview` boundaries. Because those public DTO signatures expose canonical Shopping values, the finite bridge additionally allows only:

- `shopping.Quantity`;
- `shopping.QuantityUnit`.

ArchUnit rejects direct dependencies on Recipe domain, provider, retailer, matching, basket, comparison domain, database/persistence and every other Shopping type. Existing `recipepreview` and `preview` packages remain independent from the composer.

## TDD evidence

### Service composition

RED head: `e8b58bbd01615eec5cdbdb8d7347d90746e488b3`.

API CI failed for the intended reason: the tests referenced missing M2.3 production types (`cannot find symbol`). Production code had not yet been added.

GREEN proof: `1503305e65f8520f9ae81c0c273d9e798be86e52` passed API CI / full Maven `verify` after the minimal composition implementation and architecture guard were in place.

Final service tests additionally cover fail-closed drift for:

- item cardinality;
- item identity/order;
- normalized requirement;
- canonical quantity.

### HTTP boundary

RED head: `5c188fff00207627368f19a8c9c029b11242d5da`.

API CI failed for the intended reason: the new controller/advice types did not yet exist.

GREEN proof: `8bfb7590b28228b397ec49b6759d110d89c54ebd` passed full API CI after adding the thin controller and controller-scoped error mapping.

The final HTTP contract proves:

- successful composed JSON response;
- nested Recipe semantic failures remain `INVALID_RECIPE_SHOPPING_PREVIEW`;
- locality/comparison semantic failures remain `INVALID_COMPARISON_PREVIEW`;
- malformed/unknown wrapper input returns sanitized `INVALID_RECIPE_COMPARISON_PREVIEW`;
- JSON `null` is treated by Spring MVC as an unreadable required request body and returns the same sanitized wrapper problem.

### Generated client/OpenAPI

RED head: `790c7ba4122f71da1978bf4d33c0a15c8ab2daff`.

At that checkpoint generated-schema freshness still passed while TypeScript typecheck failed because the new path/request/response types did not yet exist. This isolates the intended contract RED.

GREEN proof: `ffdc6201885f74d11871d2ad262b85e1a37d55b3` passed Contract CI including:

- pinned OpenAPI regeneration / clean generated-schema diff;
- TypeScript typecheck;
- Vitest;
- API-client build.

This proves committed `packages/api-client/src/schema.d.ts` is derived from `openapi/zakup-gotov.yaml` under the repository-pinned generator rather than an independently maintained contract.

## Null-wrapper investigation

A review pass initially hypothesized that JSON `null` might reach `RecipeComparisonPreviewService` and become a 500. A regression test was added on head `733fc52313c9aeefe6c82e32b14b5f0ddf1f90e8` expecting a dedicated null-wrapper message.

The decoded API CI job log disproved that hypothesis: Spring MVC rejects JSON `null` for the required `@RequestBody` before service invocation and the existing `HttpMessageNotReadableException` handler already returns HTTP 400 with sanitized `malformed JSON request`.

The temporary extra exception layer was therefore removed rather than retained as dead/unreachable complexity. The final test documents the actual binding behavior and the final production path stays minimal.

## CI orchestration anomaly and recovery

Predecessor exact head `0050aca36e544fb54ecdaceb7a86ef8ab822b040` reached 8/9 successful normal PR workflow groups, while API CI run `31783024562` remained `pending` with zero jobs created. No newer/in-progress API run existed for PR #101. The workflow is PR-scoped with `cancel-in-progress: true`.

A docs-only synchronize commit created the reviewed pre-shipping head `fb6e9bf583d0da230c30b77fb9245b0552be823b`, superseding the orphaned run. The fresh API run received a runner normally and passed. No runtime or contract change was used to obtain the retry.

## Exact-head pre-shipping verification

On `fb6e9bf583d0da230c30b77fb9245b0552be823b`, all nine normal PR workflow groups completed successfully:

| Workflow | Run | Result |
|---|---:|---|
| API CI | `31783412024` | SUCCESS |
| Contract CI | `31783411847` | SUCCESS |
| Web CI | `31783411858` | SUCCESS |
| CodeQL | `31783412162` | SUCCESS |
| Dependency Review | `31783411852` | SUCCESS |
| Container Security CI | `31783411883` | SUCCESS |
| Retailer Bridge CI | `31783412029` | SUCCESS |
| Release Contract CI | `31783411848` | SUCCESS |
| Release Bundle CI | `31783411918` | SUCCESS |

API CI executed `./mvnw --batch-mode --no-transfer-progress verify`, covering the M2.3 tests plus the repository's existing Spring Modulith and PostgreSQL/Testcontainers baseline. Web CI is regression-only for M2.3; this milestone introduces no Recipe UI and makes no new browser-visible behavior claim.

## Read-only review verdict

Review scope traced the complete affected runtime path rather than only the new package:

`HTTP wrapper → RecipeShoppingPreview validation/conversion → canonical Shopping projection → M2.3 mapping/invariants → ComparisonPreview request factory → production-access scoped runtime evidence → matching/basket/comparison projection`.

Verdict on reviewed implementation head `fb6e9bf583d0da230c30b77fb9245b0552be823b`: **REVIEWED_READY / Looks good**.

Findings:

- P0: none;
- P1: none;
- P2: none;
- unresolved review threads: none.

Specifically verified:

- no production-access bypass;
- no new retailer network path;
- no persistence/database dependency;
- no duplicate Recipe scaling/merge/provenance implementation;
- no duplicate matching/basket/comparison implementation;
- no provider/SKU/fulfillment identifier exposure;
- wrapper, Recipe and Comparison error vocabularies remain distinct;
- impossible cross-boundary projection drift fails closed;
- OpenAPI remains contract source of truth;
- ordinary CI remains retailer-network-free.

## Final shipping gate

This evidence document is a docs-only change after the reviewed implementation head, so PR #101 must still pass all nine normal workflow groups on the new exact PR head containing this document.

Only after that exact-head proof may the PR be marked ready and squash-merged using the expected head SHA. M2.3 and #100 become **COMPLETE / ACCEPTED** only after the resulting `main` SHA has all normal post-merge push workflows green.

After acceptance, the next product slice is the real responsive Recipe UI using this composed endpoint, developed with frontend component TDD and desktop/mobile Playwright RED-first.
