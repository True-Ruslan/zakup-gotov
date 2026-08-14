# M3.1 WeeklyPlan Domain + Deterministic Shopping Composition — Acceptance

Date: 2026-08-14  
Issue: #109  
Implementation PR: #110  
Status: **COMPLETE / ACCEPTED**

Authoritative design: `docs/superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md`  
Implementation plan: `docs/superpowers/plans/2026-08-14-m3-1-weekly-plan-domain.md`  
Shipping evidence: `docs/superpowers/plans/2026-08-14-m3-1-weekly-plan-domain-shipping.md`

## Decision

**ACCEPT M3.1** and advance the deterministic roadmap to **M3.2 — stateless WeeklyPlan application/API boundary**.

M3.1 provides the first accepted Weekly Planning domain/application foundation without introducing persistence, transport or UI scope.

Accepted flow:

`ordered WeeklyPlan meal occurrences + per-occurrence target servings → accepted M2.5 aggregation → one canonical weekly ShoppingList + occurrence-aware Recipe ingredient provenance`

## Accepted behavior

- immutable WeeklyPlan identity and ordered non-empty meal occurrence model;
- Monday-through-Sunday planner metadata with no premature fixed breakfast/lunch/dinner/snack slots;
- multiple occurrences may share one day;
- the same Recipe may intentionally occur multiple times through distinct WeeklyMealOccurrenceIds;
- each occurrence owns its own accepted positive RecipeServings target;
- occurrence order remains explicit caller/user order and is never implicitly sorted by day;
- weekly ShoppingList identity derives deterministically from WeeklyPlanId;
- internal RecipeAggregationEntry identity derives deterministically from WeeklyPlanId + WeeklyMealOccurrenceId;
- accepted M2.5 remains authoritative for Recipe scaling, source-unit canonicalization, exact-safe merge, quantity sum, first-compatible output ordering and final ShoppingItem identity;
- day, target servings and occurrence position do not directly enter final ShoppingItem identity;
- reordering occurrences may change output group order while unchanged merge-key IDs remain stable inside one plan;
- planner provenance projects to WeeklyMealOccurrenceId + exact accepted RecipeIngredientRef and never exposes internal RecipeAggregationEntryId;
- ShoppingList/provenance key drift, empty lineage, unknown internal occurrence mapping, deterministic internal-ID collision and missing inputs fail closed;
- planner provenance map/nested lists are defensive, ordered and immutable;
- weeklyplan project dependencies are limited to accepted recipe/shopping packages and JDK classes;
- recipe/shopping remain independent from weeklyplan;
- no persistence/database schema, REST/OpenAPI/generated client, web UI, pantry/exclusions, nutrition, AI/fuzzy semantics, provider/retailer acquisition or comparison orchestration was introduced.

## TDD / verification evidence

### WeeklyPlan domain

- RED `4fc807aef66a2d861f3707477186ce29c3d7a022`: testCompile failed only on intentionally absent WeeklyPlan production types.
- GREEN `f9f8f4372dab7c15aa4e5c08fc1f0d841ca3be04`: minimal domain model added; full API/Maven verify SUCCESS.

### WeeklyPlan composition

- RED `3faa4c8a93ee725082264751079a23fbd43f28b4`: testCompile failed on intentionally absent composer/provenance result types.
- GREEN `5b2cf92f9f3c1caa071a953f977fb09fce090697`: deterministic M2.5 composition and planner provenance implemented; full API/Maven verify SUCCESS.

### Fail-closed hardening

- RED `a2aeb7e9fdb45b646cb0d19407acca1291bf6cf1`: full Maven run had exactly 3 intended failures / 0 errors for missing final-item provenance, orphan provenance key and empty lineage; all other M3.1/M2 regressions remained green.
- GREEN `25677c697292f63649579720e02aefd666093322`: exact ShoppingList/provenance key validation and non-empty lineage enforcement added; full API/Maven verify SUCCESS.

### Architecture

- checkpoint `f3874d21b3569964a20d5b1bfb17c07ac250a821`: ArchUnit boundary tests plus full Maven/Testcontainers/Modulith verification SUCCESS.

## Final PR gate

Final reviewed feature head:

`ec1af08cbaf373f79c54858e9654451cebc4f009`

Evidence on that exact head:

- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Web CI + responsive E2E — SUCCESS;
- CodeQL Java + JavaScript/TypeScript — SUCCESS;
- Dependency Review — SUCCESS;
- Container Security API + Web — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Release Bundle CI — SUCCESS.

Result: **9/9 normal PR workflow groups SUCCESS**.

Read-only final review: **Looks good**; no P0/P1/P2/P3 findings; review threads empty.

## Merge / post-merge proof

PR #110 was squash-merged with exact-head protection.

Accepted main implementation SHA:

`13e09c63959b050d431cc913597fc868aa408718`

Post-merge evidence:

- issue #109 closed with reason `completed`;
- exactly 8 normal push-triggered workflow runs exist for the accepted main SHA;
- no run remained in progress and no run concluded failure/cancelled/null;
- therefore **8/8 normal push workflow groups SUCCESS**.

## Next deterministic target

Advance to **M3.2 — stateless WeeklyPlan application/API boundary**.

Recommended M3.2 direction:

`explicit weekly-plan request + locality-independent planner data → server-owned transient WeeklyPlan/occurrence/Recipe/ingredient identities → accepted M3.1 WeeklyPlan composition → self-contained weekly ShoppingList + planner/recipe provenance`

M3.2 should remain stateless and contract-first. Persistence, saved plans/history, pantry subtraction, comparison orchestration and Weekly Planning UI remain separate follow-on slices unless the approved M3.2 design explicitly introduces the minimum needed boundary.
