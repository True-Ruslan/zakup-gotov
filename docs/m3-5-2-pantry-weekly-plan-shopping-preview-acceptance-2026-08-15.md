# M3.5.2 Pantry-aware WeeklyPlan Shopping Preview — Acceptance

Date: 2026-08-15  
Status: **COMPLETE / ACCEPTED**  
Issue: #124  
Implementation PR: #125

Baseline before M3.5.2: `b94ad6779ea58384e01f262355c99843458b4820`  
Final reviewed feature head: `1e08ee4f5111bb493eeb100cfc2579d6fbafa708`  
Accepted implementation squash merge: `0dfbef49d265069578968fdedd18828c9452baca`

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-design.md`](superpowers/specs/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview.md`](superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-shipping.md`](superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-shipping.md)

## Accepted boundary

M3.5.2 adds a new stateless application/API composition boundary:

`WeeklyPlan request + request-scoped Pantry rows → original WeeklyPlan shopping projection + Pantry adjustment evidence + remaining ShoppingList`

Public endpoint:

`POST /api/v1/weekly-plan-pantry-shopping-previews`

The existing accepted M3.2 `POST /api/v1/weekly-plan-shopping-previews` and M3.3 `POST /api/v1/weekly-plan-comparison-previews` endpoints are unchanged.

## Accepted composition semantics

- accepted M3.2 remains authoritative for transient WeeklyPlan/Recipe identity, nested Recipe validation, serving scaling, deterministic aggregation, ShoppingList identity/order and planner provenance;
- M3.5.2 reconstructs only the neutral Shopping representation needed to call the accepted M3.5.1 Pantry boundary;
- source ShoppingList UUID, ShoppingItem UUID, normalized requirement and canonical quantity are preserved across that bridge;
- accepted `PantryShoppingListAdjuster` is applied exactly once;
- Pantry input is request-scoped and may be empty;
- an empty Pantry request preserves the original weekly shopping projection unchanged;
- partial coverage keeps the source ShoppingItem identity/provenance and reduces only canonical quantity;
- full coverage remains explicit in ordered Pantry evidence even though the covered ShoppingItem is absent from the remaining ShoppingList;
- full Pantry coverage may therefore produce a valid remaining ShoppingList with zero items;
- original WeeklyPlan projection and original ShoppingList provenance remain inspectable in the same response, preventing hidden ingredient loss;
- drift in identity, order, requirement, canonical quantity, evidence cardinality or remaining-item subsequence fails closed rather than being silently adapted.

## Accepted validation and HTTP semantics

- semantic invalidity is projected as sanitized `INVALID_WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEW` problem details;
- nested accepted M3.2 validation is preserved with `weeklyPlan.` field prefixes;
- malformed JSON, unknown top-level fields and unsupported units are sanitized without parser internals;
- Pantry quantities remain positive and reuse accepted quantity/unit vocabulary;
- the endpoint has no locality, retailer, provider, database or persistence concern.

## Public contract

OpenAPI 3.1 and the generated TypeScript client now expose:

- `/api/v1/weekly-plan-pantry-shopping-previews`;
- operation `createWeeklyPlanPantryShoppingPreview`;
- `WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEWS_PATH`;
- request-scoped Pantry requirement/quantity rows;
- original weekly shopping projection;
- ordered Pantry adjustment evidence;
- a zero-or-more remaining ShoppingList;
- sanitized M3.5.2 problem details.

The generated-schema freshness gate proves `packages/api-client/src/schema.d.ts` matches `openapi/zakup-gotov.yaml`.

## TDD evidence

### Application composition

RED: `a865bf229b4f86cfde8ba68c4f5ba8b6a944799c`

The service contract existed before the production composition boundary and API verification failed as expected.

### HTTP boundary

RED: `3b977fa68fba7ec88db4af216edeadfadd3c935c`

The controller contract preceded production endpoint/controller/advice wiring.

Service + HTTP GREEN:

`bd8cd61936ee0ea6e571151523cbcdd2691b6774`

That head passed all 9 normal PR workflow groups before public-contract work began.

### OpenAPI / generated TypeScript contract

RED: `68bfaf076ee48e81cbb622c65a35e31f2184d259`

`Contract CI` failed specifically at API-client typecheck because the new public path/operation/schema vocabulary was intentionally required before it existed. Generated-schema freshness still passed on that RED head.

GREEN: `7ff828501ff102ab4cc663411554c304f9e17e58`

Generated-schema freshness, TypeScript typecheck, client tests and client build all succeeded.

### Architecture / regression gate

Gate head: `4917e55f2d7d11118575c4f04644f2adf9af63c9`

Full Java 25 / Maven API verification succeeded.

ArchUnit proves:

- `weeklyplanpantrypreview` depends only on accepted `weeklyplanpreview`, `pantry` and neutral `shopping` project packages;
- it does not reach comparison, basket, matching, retailer, provider or database packages;
- accepted M3.2 and M3.3 boundaries do not depend back on M3.5.2.

## Final PR acceptance proof

On exact reviewed PR #125 head `1e08ee4f5111bb493eeb100cfc2579d6fbafa708`:

- exactly **9 normal PR workflow groups** were created;
- **9/9 SUCCESS**;
- **0 failures, 0 skipped, 0 cancelled**;
- read-only review verdict: **Looks good**;
- P0: none;
- P1: none;
- P2: none;
- P3: none;
- nitpicks: none;
- unresolved review threads: **0**;
- mergeability was true before the ready/merge transition.

The review changed no repository files.

## Merge and post-merge acceptance proof

PR #125 was marked ready only after the exact-head gate and squash-merged with expected-head protection.

Accepted implementation merge:

`0dfbef49d265069578968fdedd18828c9452baca`

Post-merge evidence on that exact `main` SHA:

- issue #124 closed with state reason `completed`;
- exactly **8 normal push workflows** were created;
- **8/8 SUCCESS, 0 failures**;
- CodeQL Java and JavaScript/TypeScript completed successfully;
- normal API, contract, web, browser, retailer-bridge and release checks remained green.

Therefore M3.5.2 is accepted as:

**implemented → tested → reviewed → merged → accepted**.

## Non-goals preserved

M3.5.2 does not add:

- Pantry-aware retailer comparison;
- changes to accepted M3.2 or M3.3 endpoint behavior;
- persistence/history/database schema;
- browser Pantry controls;
- retailer/provider/acquisition behavior or live retailer traffic;
- fuzzy/synonym/AI ingredient equivalence;
- boolean omit-all / never-buy exclusion rules;
- nutrition/macros;
- calendar/time-zone semantics.

## Decision

**M3.5.2 Stateless Pantry-aware WeeklyPlan Shopping Preview is COMPLETE / ACCEPTED.**

The next deterministic slice is **M3.5.3 — Pantry-aware WeeklyPlan → Comparison composition**.

M3.5.3 must remain a new explicit composition boundary rather than mutating accepted M3.3. It should reuse M3.5.2 as the authoritative Pantry-aware weekly projection, pass only remaining shopping demand into accepted ComparisonPreview when such demand exists, preserve original weekly shopping + Pantry evidence + remaining demand alongside comparison evidence, and explicitly design the truthful zero-remaining-demand behavior before production code. Persistence, browser controls and provider acquisition remain later concerns.
