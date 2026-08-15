# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; entries focus on user-visible behavior, architecture, security, retailer evidence and release engineering rather than routine refactors.

## [Unreleased]

### Added

#### Recipes

- Separate immutable `recipe` domain for M2.1 with UUID-backed recipe/ingredient identity, normalized titles, positive integer servings and ordered explicit ingredients.
- Deterministic `Recipe → ShoppingList` conversion reusing accepted `ShoppingRequirement` and `Quantity` semantics instead of duplicating quantity/unit normalization.
- Serving scaling that sums compatible canonical quantities before applying the serving ratio; terminating division remains exact and non-terminating ratios use deterministic `MathContext.DECIMAL128` without `double`/`float`.
- Exact-safe ingredient grouping by normalized requirement + canonical unit only; case differences, synonyms, fuzzy equivalence and physical-dimension mismatches remain separate requirements.
- Deterministic list-scoped `ShoppingItemId` derivation from `ShoppingListId + requirement + canonical unit`, independent of amount and requested servings.
- Deep-immutable ordered provenance `ShoppingItemId → RecipeIngredientRef(RecipeId, RecipeIngredientId)` kept outside Shopping Core types.
- Fail-closed generated-ID collision detection through a package-private deterministic-ID seam covered by regression tests.
- M2.1 design, implementation plan and shipping evidence documenting the domain/conversion boundary, TDD chain, review and acceptance gates.
- Stateless M2.2 `POST /api/v1/recipe-shopping-previews` application boundary converting explicit recipe input into a canonical ShoppingList preview without persistence or retailer traffic.
- Server-owned transient Recipe, ingredient and ShoppingList UUID generation at the application boundary; client-supplied server identities are not part of the contract.
- Self-contained recipe preview provenance through ordered `sourceIngredientIds` on every generated shopping item, with fail-closed checks for missing, orphan, cross-recipe or mismatched-list evidence.
- Recipe-preview request validation and sanitized `INVALID_RECIPE_SHOPPING_PREVIEW` problem details for semantic and unreadable-body failures.
- Strict JSON integer handling for base/target servings while preserving decimal ingredient quantities and existing Shopping Core quantity canonicalization.
- OpenAPI 3.1 recipe-shopping-preview request/response/problem schemas plus generated TypeScript path and types.
- Recipe-preview architecture tests keeping the application adapter dependent inward on `recipe`/`shopping` and independent from provider, retailer, matching, basket, comparison and database packages.
- M2.2 acceptance evidence documenting exact-head review, squash merge `8f0c1d8d31cfc1673656780a7989512d38788aff` and 8/8 successful post-merge `main` workflows.
- Stateless M2.3 `POST /api/v1/recipe-comparison-previews` boundary composing the accepted Recipe shopping preview with the accepted retailer comparison preview in one request.
- End-to-end preservation of generated ShoppingItem UUID, order, normalized requirement and canonical quantity from Recipe conversion into comparison, with explicit fail-closed drift checks.
- Composed response keeps Recipe ingredient provenance self-contained while reusing the existing product-safe retailer comparison projection without exposing provider, SKU or fulfillment identifiers.
- Dedicated sanitized `INVALID_RECIPE_COMPARISON_PREVIEW` wrapper-binding problem while preserving the existing nested Recipe and Comparison semantic problem vocabularies.
- OpenAPI 3.1 composed request/response/problem contract plus generated TypeScript `RECIPE_COMPARISON_PREVIEWS_PATH` and generated types, verified by pinned regeneration and clean-diff CI.
- Architecture guards for `recipecomparisonpreview` that permit only accepted application-boundary dependencies plus the finite canonical Shopping `Quantity` / `QuantityUnit` value bridge and reject Recipe-domain/downstream/persistence coupling.
- M2.3 design, implementation plan, shipping evidence and acceptance decision documenting RED→GREEN checkpoints, exact-head review, squash merge `15a086d135f40277c655b39549c3e7a04c2e914e` and 8/8 successful post-merge `main` workflows.
- M2.4 responsive Recipe-first web journey using the generated `POST /api/v1/recipe-comparison-previews` contract rather than duplicating Recipe/comparison DTOs or domain semantics in browser code.
- Recipe form for title, base/target servings, locality and 1..100 editable ingredients with explicit quantity/unit controls, add/remove behavior, pending state and product-safe preflight errors.
- Generated canonical Recipe shopping requirements rendered before the existing truthful retailer comparison result projection while transient Recipe/ingredient/list/item IDs stay hidden from user-facing output.
- Fail-closed Recipe web transport with a finite timeout, sanitized generated 400 validation messages and no fabricated result on missing configuration, timeout, network or unexpected-service failure.
- Responsive desktop/mobile Recipe acceptance coverage for serving scaling, generated shopping output, unavailable API state, visible keyboard focus, no horizontal overflow and continued manual-list comparison regression.
- Deterministic E2E-only `/api/v1/recipe-comparison-previews` fixture path; production browser code contains no retailer fixture evidence and browser acceptance makes no live retailer request.
- M2.4 design, implementation plan, shipping evidence and acceptance decision documenting explicit RED→GREEN checkpoints, reviewed head `fb069d64b96f0d989951e67fd62b793277453024`, squash merge `aba20c9cee263a683c0d4383ad840d7415851861` and 8/8 successful post-merge `main` workflows.
- M2.5 `RecipeAggregationEntryId` and `RecipeAggregationEntry` distinguish one Recipe occurrence from `RecipeId`, allowing the same Recipe to participate multiple times with independent target servings.
- Deterministic `RecipeShoppingListAggregator` reuses the accepted per-Recipe converter, derives internal occurrence-list identities, merges only exact normalized requirement + canonical unit and sums already-canonical quantities with exact decimal addition.
- Occurrence-aware `RecipeAggregationIngredientRef` lineage preserves aggregation-entry identity plus the accepted `RecipeIngredientRef`, so repeated inclusion of the same Recipe remains unambiguous.
- Aggregate ShoppingItem order follows first compatible occurrence, while final ShoppingItem identity reuses the accepted list+requirement+canonical-unit derivation and remains independent of amount/target servings.
- Shared package-private `RecipeShoppingMergeKey` and `RecipeShoppingItemIds` seams remove algorithm duplication while preserving the accepted M2.1 identity payload byte-for-byte.
- Literal compatibility fixture locks accepted single-Recipe ShoppingItem UUID `3d737f10-a263-39b3-b90a-fe7868c035b9` across the shared-helper extraction.
- Deep-immutable ordered multi-Recipe provenance plus fail-closed empty input, duplicate occurrence identity, missing provenance and generated-ID collision behavior.
- M2.5 design, implementation plan, shipping evidence and acceptance decision documenting explicit RED→GREEN checkpoints, reviewed head `a6e1095696ebfd67fafe7675a37b125ae02b3170`, squash merge `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0` and 8/8 successful post-merge `main` workflows.

#### Weekly planning

- M3.1 immutable `WeeklyPlan` domain with stable plan/meal-occurrence identities, required Monday-through-Sunday metadata, positive per-occurrence target servings and explicit caller ordering.
- Weekly plans allow multiple meals on the same day and repeated use of one Recipe through distinct `WeeklyMealOccurrenceId` values without imposing premature breakfast/lunch/dinner/snack slots.
- Deterministic WeeklyPlan-scoped ShoppingList identity and internal M2.5 aggregation-entry identity use versioned namespaced UUID payloads based on `WeeklyPlanId` and `WeeklyMealOccurrenceId`.
- `WeeklyPlanShoppingListComposer` delegates Recipe scaling, canonicalization, exact cross-Recipe merge, canonical arithmetic, final ordering and ShoppingItem identity to the accepted M2.5 aggregator rather than duplicating those semantics.
- Planner provenance projects accepted M2.5 lineage to ordered `WeeklyMealOccurrenceId + RecipeIngredientRef` while internal `RecipeAggregationEntryId` remains hidden.
- Fail-closed WeeklyPlan composition rejects deterministic internal-ID collisions, missing/orphan/empty ShoppingItem provenance and unknown aggregation-entry lineage.
- ArchUnit guards constrain `weeklyplan` production dependencies to accepted `recipe`/`shopping` packages and protect reverse dependency direction.
- M3.1 design, implementation plan, shipping evidence and acceptance decision document domain/composition/hardening RED→GREEN chains, reviewed head `ec1af08cbaf373f79c54858e9654451cebc4f009`, squash merge `13e09c63959b050d431cc913597fc868aa408718` and 8/8 successful post-merge `main` workflows.
- Stateless M3.2 `POST /api/v1/weekly-plan-shopping-previews` boundary accepts `1..35` ordered weekly meal occurrences without client-supplied planner, Recipe or ingredient identities.
- M3.2 generates transient WeeklyPlan/occurrence identities while reusing accepted M2.2 Recipe/ingredient construction and nested Recipe validation/normalization semantics.
- Weekly shopping composition remains delegated to accepted M3.1/M2.5, preserving plan-scoped ShoppingList identity, canonical quantities, exact merge semantics, output ordering and final ShoppingItem identity without parallel planner arithmetic.
- Public M3.2 provenance exposes only self-contained `occurrenceId + recipeId + recipeIngredientId` source tuples; internal `RecipeAggregationEntryId` remains hidden.
- M3.2 rejects malformed JSON, unknown fields/day/unit and fractional serving JSON through sanitized `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW` problems without exposing parser internals.
- OpenAPI 3.1 and generated TypeScript expose `createWeeklyPlanShoppingPreview`, `WEEKLY_PLAN_SHOPPING_PREVIEWS_PATH` and synchronized request/response/problem types.
- ArchUnit guards keep `weeklyplanpreview` out of provider, retailer, matching, basket, comparison and database layers and protect reverse dependency direction into accepted Recipe/Shopping/WeeklyPlan packages.
- M3.2 design, implementation plan, shipping evidence and acceptance decision document request/composition/provenance/HTTP/contract/hardening RED→GREEN chains, reviewed head `250aedb85b675036ffcb20e96a67db1afc03167a`, squash merge `9682ad1230910fc268ca3cddd8601a3fad7b100e`, issue #112 closure and 8/8 successful post-merge `main` workflows.
- Stateless M3.3 `POST /api/v1/weekly-plan-comparison-previews` composes provider-neutral locality plus the accepted M3.2 WeeklyPlan input into one accepted weekly shopping projection and retailer comparison without client-controlled server identities.
- M3.3 preserves generated weekly ShoppingItem UUID, order, normalized requirement and canonical quantity unchanged into ComparisonPreview while returning M3.2 self-contained `occurrenceId + recipeId + recipeIngredientId` provenance unchanged.
- Accepted ComparisonPreview remains authoritative for locality validation, canonical retailer visibility, production-access gating before acquisition, runtime evidence, matching, package/basket semantics and truthful complete/uncertain/incomplete/unavailable states.
- M3.3 fails closed on cross-boundary cardinality, identity/order, requirement or canonical-quantity drift instead of silently adapting mismatched projections.
- Whole-wrapper JSON binding failures use sanitized `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW`; successfully bound M3.2 and comparison semantic failures retain their accepted problem contracts.
- OpenAPI 3.1 and generated TypeScript expose `createWeeklyPlanComparisonPreview`, `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH` and synchronized composed request/response/problem types.
- ArchUnit keeps `weeklyplancomparisonpreview` on accepted M3.2/ComparisonPreview application boundaries with only the finite Shopping `Quantity` / `QuantityUnit` bridge, rejecting direct planner-domain/provider/retailer/matching/basket/comparison-domain/database coupling.
- M3.3 design, implementation plan, shipping evidence and acceptance decision document explicit RED→GREEN checkpoints, reviewed head `396445c333ea369bed6d428b33f38f37765eff20`, squash merge `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`, issue #115 closure and 8/8 successful post-merge `main` workflows.
- M3.4 responsive WeeklyPlan-first web journey consumes generated `POST /api/v1/weekly-plan-comparison-previews` as the only planner comparison transport instead of composing lower boundaries or duplicating planner/comparison DTOs in browser code.
- WeeklyPlan editor supports `1..35` explicit ordered meal occurrences, add/remove and adjacent up/down reordering while keeping caller order independent from Monday-through-Sunday metadata and avoiding a fixed meal-slot taxonomy.
- Each occurrence edits target servings plus explicit nested Recipe title/base servings/ingredients using generated day and quantity-unit vocabulary; browser-local numeric row keys remain presentation-only and are never sent as domain/server identity.
- Canonical weekly shopping requirements render directly from accepted `WeeklyPlanShoppingPreview.shoppingList.items` in server order before the existing truthful `ComparisonPreviewResults` projection.
- M3.4 browser code performs no serving scaling, cross-Recipe merge, canonicalization, matching, package arithmetic, basket-total or winner recomputation and does not expose generated WeeklyPlan/Recipe/Shopping identities or planner provenance in ordinary user-facing output.
- WeeklyPlan web transport uses a finite three-second timeout, preserves generated product-safe 400 field/message evidence and fails closed on missing configuration, timeout, network, non-400 or unexpected service failure without fabricated results.
- Responsive deterministic Playwright covers desktop WeeklyPlan → weekly shopping → retailer comparison, explicit reorder semantics, 390px no-overflow, keyboard focus, fail-closed unavailable state and continued Recipe/manual-list critical journeys.
- Deterministic E2E-only `/api/v1/weekly-plan-comparison-previews` fixture creates server-owned test identities and canonical weekly shopping for browser-contract verification; production browser acceptance makes no live retailer request and carries no credential/provider internals.
- M3.4 design, implementation plan, shipping evidence and acceptance decision document explicit RED→GREEN transport/results/form/homepage/browser checkpoints, reviewed head `12973650f274f76ec54865be41963843afcb4558`, squash merge `1201030aed45075c676f796920b6268cdcf8e036`, issue #118 closure and 8/8 successful post-merge `main` workflows.

#### Pantry-aware weekly planning

- M3.5.1 pure `pantry` domain layer subtracts request-scoped known-at-home stock from an already canonical ShoppingList using only exact `(ShoppingRequirement, canonical QuantityUnit)` semantics and accepted kg→g / l→ml canonicalization.
- Duplicate Pantry rows aggregate by exact key, stock is consumed once in source ShoppingList order, and each row consumes `min(required, available)` without producing zero/negative remaining quantities.
- Pantry adjustment retains ordered immutable `UNCHANGED / PARTIALLY_COVERED / FULLY_COVERED` evidence; partial coverage preserves ShoppingItem identity/order and full coverage removes only remaining demand while retaining audit evidence.
- M3.5.1 remains provider-neutral and introduces no endpoint, persistence, browser UI, fuzzy/AI matching or boolean omit-all semantics.
- M3.5.1 acceptance records reviewed head `b48a88e4ded457f81245223b75477be16ccf3051`, squash merge `bcc644bb243a63941e7629755f1b3196d94332c2`, issue #121 closure and 8/8 successful post-merge `main` workflows.
- Stateless M3.5.2 `POST /api/v1/weekly-plan-pantry-shopping-previews` composes the accepted M3.2 WeeklyPlan shopping projection with accepted M3.5.1 Pantry subtraction without silently modifying M3.2 or M3.3.
- M3.5.2 returns original WeeklyPlan projection, original canonical ShoppingList/provenance, ordered Pantry adjustment evidence and zero-or-more remaining ShoppingItems in one inspectable response.
- Request-scoped Pantry may be empty; full Pantry coverage may produce an empty remaining list while original demand and full-coverage evidence remain explicit.
- M3.5.2 preserves ShoppingList/ShoppingItem UUID, order, normalized requirement, canonical quantity and planner provenance across the composition bridge and fails closed on identity/order/quantity/evidence drift.
- Sanitized M3.5.2 validation handles nested WeeklyPlan errors, invalid Pantry quantities, malformed JSON, unknown top-level fields and unsupported units without exposing parser internals.
- OpenAPI 3.1 and generated TypeScript expose `createWeeklyPlanPantryShoppingPreview`, `WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEWS_PATH` and synchronized Pantry request/response/problem types.
- ArchUnit constrains `weeklyplanpantrypreview` to accepted M3.2 + Pantry + neutral Shopping dependencies and protects M3.2/M3.3 from reverse Pantry-composition coupling.
- M3.5.2 acceptance records final reviewed head `1e08ee4f5111bb493eeb100cfc2579d6fbafa708`, squash merge `0dfbef49d265069578968fdedd18828c9452baca`, issue #124 closure and 8/8 successful post-merge `main` workflows.
- Stateless M3.5.3 `POST /api/v1/weekly-plan-pantry-comparison-previews` composes accepted M3.5.2 remaining demand into accepted ComparisonPreview without modifying M3.3 or M3.5.2.
- M3.5.3 returns explicit `COMPARED / NO_REMAINING_DEMAND`; full Pantry coverage skips ComparisonPreviewService/runtime retailer acquisition rather than fabricating non-empty demand.
- Locality remains independently validated, only non-empty remaining demand reaches comparison, and ShoppingItem UUID/order/requirement/canonical quantity drift fails closed.
- Zero-demand responses omit `comparisonPreview` on the wire; derived ComparisonPreview validation is translated into sanitized M3.5.3 problem details.
- OpenAPI 3.1/generated TypeScript plus architecture/regression coverage protect the new boundary and existing M3.3/M3.5.2 behavior.
- M3.5.3 acceptance records final reviewed head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789`, squash merge `079a53be066fa488ee01da18a109f4f2b1484800`, issue #127 closure and 8/8 successful post-merge `main` workflows.

#### Product and shopping core

- Canonical eight-retailer registry with independent technical-connectivity and production-access states.
- Canonical shopping quantities and shopping-list aggregate with stable UUID identity and deterministic mutation semantics.
- Provider/path orchestration preserving retailer, source-provider, acquisition-mode and fulfillment provenance.
- Provider-neutral `ProductLocation`, sensitive-address redaction and typed provider-scoped fulfillment bindings.
- Immutable offer snapshots with explicit observation/provider-freshness evidence and first-class `UNKNOWN` availability.
- Deterministic exact-before-normalized product matching with explicit matched/ambiguous/unmatched states and no fuzzy/AI baseline.
- Whole-package single-store basket calculation with explicit package evidence, deterministic decimal arithmetic and `COMPLETE / UNCERTAIN / INCOMPLETE` aggregate states.
- Product-facing retailer comparison/readiness model that always preserves all canonical retailers and keeps provider/acquisition identifiers internal.
- Stateless `POST /api/v1/comparison-previews` manual-list comparison API, synchronized OpenAPI/generated TypeScript client and responsive web journey.
- Desktop/mobile Playwright critical-journey coverage for ready, uncertain, incomplete, unavailable, unmatched, ambiguous, package-unknown and unit-mismatch paths.
- M1 final acceptance document with explicit **GO to M2 Recipes** for deterministic product/core development.

#### Structured package evidence

- Optional canonical package quantity on `ObservedOffer`, preserved through immutable `OfferSnapshot` and projected into `PackageQuantitySet` from snapshot evidence.
- Runtime invariant rejecting a parallel package-evidence set when it disagrees with snapshots.
- Regression coverage proving presentation names such as `970мл` or `1,5л` do not create package evidence.
- Pure Magnit exact-characteristic semantics for `Вес, кг` / `Объем, л`, including explicit `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES` and `INVALID_VALUE` states.
- Magnit 20-product × 2-shop fixed-corpus package instrumentation that separates transport/identity failure from missing metadata.
- SKU-bound Magnit JSON-LD package extraction from the same PUBLIC_WEB response using exact `Product.sku`, proven scalar `weight` and exact `additionalProperty[name="Объем, л"]` semantics.
- Finite JSON-LD corpus evidence: 40/40 HTTP 2xx and usable observations, 20/20 stable identity, 36/40 `FOUND`, 0 `MISSING`, 4 explicit multi-dimensional ambiguities, 0 conflicts and 0 invalid values.

#### Magnit location/store context

- Deterministic public Magnit geographic primitives and exact `box` request contract for `POST /webgate/v1/stores-facade/search`.
- Sanitized response parser accepting only `items.items[].externalId.storeCode + coordinates` and rejecting conflicting store identity evidence.
- Fail-closed store resolution semantics: zero → `NO_STORES`, exactly one → `RESOLVED`, many → `AMBIGUOUS`, conflicting duplicate identity → `CONFLICTING_STORE_EVIDENCE`.
- Provider-scoped Magnit fulfillment bindings reusing `sourceProviderId="magnit-public-page"`; `shopCode` remains internal `LocationContext.fulfillmentContextId`.
- Owner-only merged-main live gate for issue #69 with direct stateless no-cookie/no-auth/no-redirect requests and sanitized evidence.
- Merged-main location-resolution proof for public `shopCode=992301` across two identical candidate-set requests.

#### Retailer connectivity and engineering

- Universal Retailer Connectivity design and evidence-driven acquisition-mode fallback policy.
- Chromium MV3 retailer bridge with minimal permissions, sanitized local storage, deterministic fixtures and persistent-Chromium E2E.
- Accepted first-party browser paths for Perekrestok v2 and Pyaterochka v1.
- Magnit public-page Phase A/B evidence establishing `AVAILABLE_PUBLIC_WEB` technical feasibility.
- Magnit right-to-operate decision memo documenting why recurring production public-web reuse remains disabled pending affirmative permission or licensed/supported access terms.
- Pre-acquisition production-access gate: runtime evidence sources receive only immutable production-ready retailer IDs; empty scope skips source invocation; out-of-scope evidence is rejected before matching/basket work.
- Architecture guards protecting basket/comparison/preview dependency direction and preventing production code from depending on fixtures/test support.

### Changed

- Project phase advanced from M0 Product & Integration Discovery to M1 Shopping Core, then M2 Recipes, and now **M3 Weekly Planning / Pantry**.
- M1 Shopping Core is **COMPLETE / ACCEPTED** on the post-merge pre-acquisition-gate baseline `779d0b219a13e0bf82263a1e655fb732553ed5fe`.
- The M1→M2 decision is **GO for deterministic product/core development**; it does not claim every retailer is production-ready.
- M2.1 `Recipe → explicit ingredients → canonical quantities → ShoppingList` is **COMPLETE / ACCEPTED** after squash merge `423eb14f7c565bbe264257a92df89a6b42d0d158` and 8/8 successful post-merge `main` workflows.
- M2.2 stateless Recipe application/API boundary is **COMPLETE / ACCEPTED** after final reviewed head `318a48c569d0d001a4c27b5792e1681f7884e518`, squash merge `8f0c1d8d31cfc1673656780a7989512d38788aff`, issue #96 closure and 8/8 successful post-merge `main` workflows.
- M2.3 composed Recipe → Comparison boundary is **COMPLETE / ACCEPTED** after final reviewed head `b6575f03b668f8bbaacd5b2897c4fb9301d94cdf`, squash merge `15a086d135f40277c655b39549c3e7a04c2e914e`, issue #100 closure and 8/8 successful post-merge `main` workflows.
- M2.4 responsive Recipe UI is **COMPLETE / ACCEPTED** after final reviewed head `fb069d64b96f0d989951e67fd62b793277453024`, squash merge `aba20c9cee263a683c0d4383ad840d7415851861`, issue #103 closure and 8/8 successful post-merge `main` workflows.
- M2.5 deterministic multi-Recipe aggregation is **COMPLETE / ACCEPTED** after final reviewed head `a6e1095696ebfd67fafe7675a37b125ae02b3170`, squash merge `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`, issue #106 closure and 8/8 successful post-merge `main` workflows.
- M2 Recipes is **COMPLETE / ACCEPTED**.
- M3.1 WeeklyPlan domain + deterministic shopping composition is **COMPLETE / ACCEPTED** after final reviewed head `ec1af08cbaf373f79c54858e9654451cebc4f009`, squash merge `13e09c63959b050d431cc913597fc868aa408718`, issue #109 closure and 8/8 successful post-merge `main` workflows.
- M3.2 stateless WeeklyPlan shopping-preview application/API boundary is **COMPLETE / ACCEPTED** after final reviewed head `250aedb85b675036ffcb20e96a67db1afc03167a`, squash merge `9682ad1230910fc268ca3cddd8601a3fad7b100e`, issue #112 closure and 8/8 successful post-merge `main` workflows.
- M3.3 WeeklyPlan → Comparison composition is **COMPLETE / ACCEPTED** after final reviewed head `396445c333ea369bed6d428b33f38f37765eff20`, squash merge `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`, issue #115 closure and 8/8 successful post-merge `main` workflows.
- M3.4 Responsive Weekly Planning UI is **COMPLETE / ACCEPTED** after final reviewed head `12973650f274f76ec54865be41963843afcb4558`, squash merge `1201030aed45075c676f796920b6268cdcf8e036`, issue #118 closure and 8/8 successful post-merge `main` workflows.
- M3.5.1 pure Pantry subtraction semantics is **COMPLETE / ACCEPTED** after final reviewed head `b48a88e4ded457f81245223b75477be16ccf3051`, squash merge `bcc644bb243a63941e7629755f1b3196d94332c2`, issue #121 closure and 8/8 successful post-merge `main` workflows.
- M3.5.2 stateless Pantry-aware WeeklyPlan shopping preview is **COMPLETE / ACCEPTED** after final reviewed head `1e08ee4f5111bb493eeb100cfc2579d6fbafa708`, squash merge `0dfbef49d265069578968fdedd18828c9452baca`, issue #124 closure and 8/8 successful post-merge `main` workflows.
- The current deterministic target is **M3.5.3 Pantry-aware WeeklyPlan → Comparison composition**; accepted M3.3 remains unchanged and zero-remaining-demand semantics must be designed explicitly before production code.
- Recipe → ShoppingList merging is intentionally stricter than product matching: only exact normalized requirements with the same canonical unit merge; no case-folding/synonym/AI equivalence is introduced.
- Recipe provenance remains conversion metadata rather than an optional Recipe field added to neutral `ShoppingItem`; M2.2 projects that provenance publicly as self-contained source ingredient IDs instead of modifying Shopping Core types.
- Recipe lifecycle and Weekly Planning/Pantry composition remain stateless by default; persistence stays deferred until reusable saved plans/history demonstrate product value.
- Recipe → Comparison has an accepted primary product boundary at `/api/v1/recipe-comparison-previews`; M2.4 consumes it directly as the primary Recipe-first browser journey.
- Multi-Recipe aggregation distinguishes Recipe identity from occurrence identity; repeated Recipe use is valid only through distinct occurrence IDs and does not weaken accepted exact merge semantics.
- WeeklyPlan distinguishes planner occurrence identity from Recipe identity and keeps day metadata outside Recipe/Shopping merge and quantity semantics.
- WeeklyPlan shopping preview remains locality/retailer-independent; retailer comparison is an explicit composition concern owned by M3.3 rather than hidden planner behavior.
- M3.4 browser composition consumes the accepted M3.3 endpoint directly; generated contract types and server output remain authoritative and Recipe/manual-list critical journeys remain regression-covered.
- Pantry subtraction preserves inspectable original/result quantities and evidence rather than silently deleting or mutating weekly requirements; M3.5.2 exposes this behavior as a separate stateless API without mutating M3.2/M3.3.
- Retailer onboarding remains transport-neutral and universal; a failed direct path changes acquisition mode rather than retailer scope.
- `ObservedOffer` is the provider trust boundary and `OfferSnapshot` the immutable comparison record.
- Observation time and provider-side update time remain distinct.
- Matching never breaks semantic ambiguity using price, availability, freshness, acquisition mode or SKU ordering.
- Package quantity is modeled only as explicit structured evidence; product names, URLs, slugs, category and other presentation text are non-authoritative.
- Package arithmetic requires canonical unit equality; mass/volume evidence cannot satisfy a `PIECE` requirement.
- `UNKNOWN` availability propagates into an uncertain basket instead of confirmed availability.
- Incomplete baskets expose no aggregate total and cannot masquerade as complete winners.
- Production comparison evidence remains deliberately no-op/fail-closed under the current production registry.
- Magnit technical location resolution for the proven bbox/store-selection boundary is accepted (#69).
- Automatic arbitrary text/address → coordinates remains intentionally unimplemented because no acceptable public contract was proven.
- Magnit remains technically `AVAILABLE_PUBLIC_WEB`, while production access is **`BLOCKED` by Zakup Gotov product policy (#70)** pending affirmative permission or licensed/supported terms.
- Product-facing Magnit readiness is `CONNECTED + BLOCKED + UNAVAILABLE` with reason `PRODUCTION_ACCESS_BLOCKED`, without totals or freshness evidence.
- Production-access policy is enforced before acquisition rather than relying only on post-load filtering.

### Fixed

- Recipe aggregate rejects missing fields, empty ingredient lists, null ingredients and duplicate ingredient IDs instead of producing a partially valid recipe.
- Recipe conversion rejects missing inputs and invalid provenance identities.
- Recipe generated-item identity collision across different merge keys fails closed rather than relying on overwrite/order behavior.
- Recipe provenance maps and nested lineage lists are defensively copied and immutable.
- Recipe preview production wiring explicitly supplies the transient ID generator, request factory, accepted converter and application service required by the Spring controller.
- Fractional JSON serving counts are rejected by recipe-specific strict integer deserialization instead of being silently coerced to integers; this strictness does not change unrelated API binding behavior.
- Recipe preview unreadable-body handling is centralized in the approved controller-scoped advice, keeping the controller thin and preventing raw Jackson/internal exception details from becoming public 400 responses.
- Recipe comparison wrapper rejects unknown/malformed JSON with a sanitized problem instead of exposing binding internals.
- Recipe comparison composition fails closed if generated shopping items and returned comparison items drift in cardinality, identity/order, normalized requirement or canonical quantity.
- Recipe UI ingredient row keys are deterministic local integers instead of random UUIDs so server render/hydration does not depend on nondeterministic initial IDs.
- Shared Recipe ShoppingItem-ID extraction preserves the accepted literal single-Recipe UUID fixture instead of silently changing historical IDs.
- Multi-Recipe aggregation rejects an empty occurrence set and duplicate aggregation occurrence IDs instead of producing an empty/ambiguous aggregate.
- Multi-Recipe aggregation rejects generated final ShoppingItem-ID collisions across different merge keys and missing/empty converted provenance instead of silently overwriting lineage.
- WeeklyPlan rejects missing/empty occurrence state and duplicate meal-occurrence identity rather than producing an ambiguous planner aggregate.
- WeeklyPlan composition rejects missing/orphan/empty ShoppingItem provenance, unknown internal aggregation lineage and deterministic internal aggregation-ID collisions rather than repairing or silently dropping evidence.
- WeeklyPlan shopping preview preserves independent nested Recipe validation errors even when target servings are also invalid instead of stopping validation at the planner wrapper.
- WeeklyPlan shopping preview rejects missing/orphan/mismatched occurrence/Recipe/ingredient lineage and sanitizes unreadable JSON instead of exposing or repairing invalid provenance/parser state.
- Pantry-aware WeeklyPlan preview fails closed on original/remaining ShoppingList identity, order, requirement, canonical-quantity or evidence drift instead of silently adapting mismatched Pantry output.
- Full Pantry coverage remains valid without fabricating a zero-quantity ShoppingItem; the remaining shopping list may be empty while original demand and audit evidence remain visible.
- Provider offer validation rejects provenance/context mismatches before comparison logic.
- Precise addresses are excluded from default string representations and provider routing.
- Snapshot freshness rejects provider timestamps after observation time.
- Semantic matching rejects cross-retailer/context candidate mixing and impossible result combinations.
- Package bindings reject duplicate/invalid evidence and incompatible canonical units.
- Mixed currencies fail closed.
- Incomplete comparison states cannot expose misleading totals or freshness summaries.
- Public comparison/readiness objects reject impossible coverage/access/status combinations.
- Comparison preview rejects unknown JSON request fields instead of silently ignoring client-controlled data.
- Browser E2E item-gap assertions are retailer-scoped to avoid copy collisions.
- Magnit package extraction ignores foreign SKU nodes and unproven fields; conflicting/multi-dimensional values never become guessed quantities.
- Magnit corpus metrics exclude non-2xx and wrong-identity pages rather than counting them as missing metadata.
- Magnit store-search request constructors enforce the proven bbox/store-type invariants even when nested records are instantiated directly.
- Magnit store response parsing deduplicates equivalent candidates and exposes conflicting identity evidence instead of choosing an arbitrary record.
- A future runtime evidence source can no longer be invoked when the registry has no production-ready retailers.
- A runtime evidence source cannot silently return evidence for a retailer outside its requested production-ready scope.

## [0.1.0-rc.2] — 2026-08-09

### Added

- Real `release: published` verification and publication path for multi-platform GHCR images.
- Candidate-digest security boundary with Trivy scans, SPDX SBOMs, attestations and digest-pinned Compose smoke verification.

### Security

- Release publication correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291` after staging image publication.
- Subsequent mainline work upgraded pgJDBC to `42.7.12`, hardened the web runtime to distroless Node 24 Debian 13/non-root and added pull-request/main container security scanning.

## [0.1.0-rc.1] — 2026-08-09

### Added

- First real GitHub prerelease event proving release metadata/main-ancestry validation, source verification and production browser testing.

### Fixed

- A release-helper executable-mode defect found by the real rc.1 event was corrected before rc.2.

## Pre-release foundation — 2026-08-09 to 2026-08-11

### Added

- Java 25 / Spring Boot 4.1 API foundation with Virtual Threads, Spring Modulith verification, PostgreSQL 18, Flyway, jOOQ and Testcontainers.
- Contract-first OpenAPI 3.1 API plus generated TypeScript client.
- Next.js 16 / React 19 responsive web foundation with Vitest and Playwright.
- Reproducible repository verification, Docker/Compose release topology, CodeQL, Dependency Review, Container Security CI and release-contract checks.
- Public operational surface limited to health/liveness/readiness/info.
- Evidence-driven retailer feasibility research for X5, Magnit, Chizhik, Ozon Fresh, Samokat, Kuper, Lenta and VkusVill.

### Changed

- Repository governance became squash-only with required checks, branch cleanup and immutable Action pins.
- Public web surface deliberately remained honest about unavailable retailer comparison functionality while M0 feasibility work was incomplete.
