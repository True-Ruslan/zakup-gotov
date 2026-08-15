# M3.5.2 Pantry-aware WeeklyPlan Shopping Preview — Shipping Evidence

Date: 2026-08-15  
Status: IMPLEMENTED / FINAL PR GATE PENDING

Baseline: `b94ad6779ea58384e01f262355c99843458b4820`  
Branch: `feat/m3-5-2-pantry-weekly-plan-shopping-preview`  
Issue: #124  
Implementation PR: #125

## Scope guard

Implemented scope:

- new stateless `weeklyplanpantrypreview` application/API composition package;
- `POST /api/v1/weekly-plan-pantry-shopping-previews`;
- accepted M3.2 WeeklyPlan shopping projection reused as the authoritative planner/Recipe boundary;
- accepted M3.5.1 `PantryShoppingListAdjuster` applied exactly once;
- original WeeklyPlan projection + original ShoppingList provenance + ordered Pantry adjustment evidence + zero-or-more remaining ShoppingList returned together;
- stable ShoppingList/ShoppingItem identity, order, requirement and provenance guarded across the composition bridge;
- sanitized semantic and malformed-request validation problems;
- OpenAPI 3.1 + generated TypeScript client synchronization;
- architecture/regression guards preserving M3.2/M3.3 independence.

Explicitly excluded:

- modifications to accepted M3.2/M3.3 endpoint behavior;
- Pantry-aware retailer comparison integration;
- persistence/database migrations;
- browser UI;
- retailer/provider/acquisition code or live retailer traffic;
- fuzzy/synonym/AI matching;
- omit-all / never-buy exclusion semantics.

## TDD evidence

### Application composition

RED: `a865bf229b4f86cfde8ba68c4f5ba8b6a944799c`

The service contract was added before the M3.5.2 production composition existed. API verification failed as expected against the absent application boundary.

GREEN implementation was built through the subsequent request/response/composition commits and reached the first complete service + HTTP green head:

`bd8cd61936ee0ea6e571151523cbcdd2691b6774`

That exact head completed all 9 normal PR workflow groups successfully before OpenAPI/client work began.

### HTTP boundary

RED: `3b977fa68fba7ec88db4af216edeadfadd3c935c`

The controller contract preceded the endpoint/controller/advice wiring.

GREEN is included in `bd8cd61936ee0ea6e571151523cbcdd2691b6774`, covering:

- partial Pantry coverage;
- full coverage with an empty remaining list;
- empty Pantry preserving the original projection;
- invalid Pantry quantities;
- nested M3.2 validation with `weeklyPlan.` field prefixes;
- malformed JSON, unknown top-level fields and unsupported units sanitized into the M3.5.2 problem contract.

### OpenAPI / generated TypeScript contract

RED: `68bfaf076ee48e81cbb622c65a35e31f2184d259`

`Contract CI` failed specifically at **Typecheck API client** because the new endpoint/path/operation/schema types were intentionally required by the test before they existed. The pre-existing generated-schema freshness check succeeded on that same RED head, proving the failure represented the missing M3.5.2 public contract rather than stale generated output.

GREEN: `7ff828501ff102ab4cc663411554c304f9e17e58`

Exact-head `Contract CI` succeeded with:

- generated schema freshness check — SUCCESS;
- TypeScript typecheck — SUCCESS;
- API-client tests — SUCCESS;
- API-client build — SUCCESS.

The generated declarations therefore match `openapi/zakup-gotov.yaml`; no handwritten parallel DTO vocabulary is used by the client.

### Architecture / regression boundary

Gate head: `4917e55f2d7d11118575c4f04644f2adf9af63c9`

Full Java 25 / Maven API verification — **SUCCESS**.

ArchUnit guards establish that:

- `weeklyplanpantrypreview` composes only accepted `weeklyplanpreview`, `pantry` and neutral `shopping` project packages;
- it does not reach into comparison, basket, matching, retailer, provider or database packages;
- accepted M3.2 `weeklyplanpreview` and M3.3 `weeklyplancomparisonpreview` do not depend back on the new M3.5.2 package.

## Cleanup

Temporary implementation/TDD breadcrumb files used while preserving the RED-first sequence were removed before the final PR candidate. They are not part of the shipping surface.

## Final PR gate

Pending on the exact final feature head after this shipping-evidence update:

- exactly 9 normal PR workflow groups;
- 9/9 SUCCESS, 0 failures/skips/cancellations;
- clean read-only review with no P0/P1/P2/P3 findings or unresolved threads;
- mergeability true before ready/merge transition.

## Merge gate

To be filled only after the accepted exact feature head is squash-merged and all normal `main` push workflows complete. Until then M3.5.2 is implemented but not yet accepted.
