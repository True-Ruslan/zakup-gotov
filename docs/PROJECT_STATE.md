# Project State

Updated: 2026-08-15

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M3 — Weekly Planning / Pantry**

Milestone status:

- M0 Product & Integration Discovery — **COMPLETE**;
- M1 Shopping Core — **COMPLETE / ACCEPTED**;
- M2 Recipes — **COMPLETE / ACCEPTED**;
- M2.1 Recipe domain + Recipe → ShoppingList — **COMPLETE / ACCEPTED**;
- M2.2 Recipe application/API boundary — **COMPLETE / ACCEPTED**;
- M2.3 Recipe → Comparison composition — **COMPLETE / ACCEPTED**;
- M2.4 Responsive Recipe UI — **COMPLETE / ACCEPTED**;
- M2.5 Deterministic multi-Recipe aggregation — **COMPLETE / ACCEPTED**;
- M3.1 WeeklyPlan domain + deterministic shopping composition — **COMPLETE / ACCEPTED**;
- M3.2 Stateless WeeklyPlan shopping preview — **COMPLETE / ACCEPTED**;
- M3.3 WeeklyPlan → Comparison composition — **COMPLETE / ACCEPTED**;
- M3.4 Responsive Weekly Planning UI — **COMPLETE / ACCEPTED**;
- M3.5.1 Pure Pantry subtraction semantics — **COMPLETE / ACCEPTED** (#121 / #122);
- M3.5.2 Stateless Pantry-aware WeeklyPlan shopping preview API — **COMPLETE / ACCEPTED** (#124 / #125).

Current deterministic target: **M3.5.3 — Pantry-aware WeeklyPlan → Comparison composition**.

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility, production-access readiness and deterministic product/core maturity are separate dimensions.

## Accepted product/core baseline

### M0 — Product & Integration Discovery

Accepted evidence established Perekrestok/Pyaterochka browser-bridge acquisition, Magnit public-web technical feasibility, at least two acquisition modes, deterministic sanitized fixtures/E2E and provider-neutral retailer architecture.

M0 proves technical feasibility only; it does not grant blanket production acquisition permission.

### M1 — Shopping Core

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).  
Accepted hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted core includes canonical retailer visibility/readiness, shopping requirements and canonical quantities, provider/location provenance, immutable offer/freshness evidence, deterministic matching, whole-package basket calculation, truthful complete/uncertain/incomplete/unavailable comparison states, pre-acquisition production-access gating, stateless comparison preview and responsive manual-list flow.

### M2 — Recipes

Accepted slices established Recipe domain/conversion, stateless Recipe shopping API, Recipe→Comparison composition, responsive Recipe UI and deterministic occurrence-aware multi-Recipe aggregation.

Permanent Recipe rule: automatic merge is only exact normalized requirement + canonical unit. Fuzzy/synonym/AI equivalence is never implicit.

### M3.1–M3.4 — Weekly Planning

Accepted WeeklyPlan behavior includes:

- ordered non-empty meal occurrences with Monday-through-Sunday metadata but no fixed meal-slot taxonomy;
- repeated Recipe use through distinct occurrence identities;
- target servings delegated through accepted Recipe semantics;
- deterministic WeeklyPlan-scoped ShoppingList and explicit planner provenance;
- stateless `POST /api/v1/weekly-plan-shopping-previews`;
- stateless `POST /api/v1/weekly-plan-comparison-previews`;
- responsive WeeklyPlan-first browser journey using only the generated M3.3 contract;
- no browser-side scaling, cross-Recipe merge, product matching, package arithmetic, basket-total or winner recomputation;
- deterministic desktop/mobile/accessibility browser acceptance with no live retailer traffic.

M3.4 acceptance: [`m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md`](m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md).  
Accepted M3.4 merge: `1201030aed45075c676f796920b6268cdcf8e036`.

## M3.5 — Pantry / exclusions semantics — IN PROGRESS

### M3.5.1 — Pure Pantry subtraction semantics — COMPLETE / ACCEPTED

Acceptance: [`m3-5-1-pantry-subtraction-semantics-acceptance-2026-08-15.md`](m3-5-1-pantry-subtraction-semantics-acceptance-2026-08-15.md).  
Accepted merge: `bcc644bb243a63941e7629755f1b3196d94332c2`.

Accepted semantics:

- pure `pantry` package over canonical Shopping types;
- exact `(ShoppingRequirement, canonical QuantityUnit)` matching only;
- existing kg→g and l→ml canonicalization reused;
- duplicate Pantry rows additive, stock consumed once in ShoppingList order;
- each item consumes `min(required, available)`;
- unmatched items remain unchanged;
- partial coverage preserves ShoppingItem identity/order and reduces only quantity;
- full coverage removes the item from remaining demand but retains ordered audit evidence;
- audit states are `UNCHANGED / PARTIALLY_COVERED / FULLY_COVERED`;
- no persistence, endpoint, UI, provider behavior, fuzzy matching or boolean omit-all semantics.

### M3.5.2 — Stateless Pantry-aware WeeklyPlan shopping preview — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-design.md`](superpowers/specs/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview.md`](superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-shipping.md`](superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-shipping.md)  
Acceptance: [`m3-5-2-pantry-weekly-plan-shopping-preview-acceptance-2026-08-15.md`](m3-5-2-pantry-weekly-plan-shopping-preview-acceptance-2026-08-15.md)  
Accepted merge: `0dfbef49d265069578968fdedd18828c9452baca`.

Accepted boundary:

`POST /api/v1/weekly-plan-pantry-shopping-previews`

Accepted result:

- new explicit stateless composition; existing M3.2/M3.3 endpoints remain unchanged;
- accepted M3.2 remains authoritative for WeeklyPlan/Recipe construction, validation, scaling, aggregation, Shopping identities/order and provenance;
- request-scoped Pantry rows use accepted requirement/quantity vocabulary and may be empty;
- accepted M3.5.1 Pantry adjustment is applied exactly once;
- response contains the original WeeklyPlan projection, original ShoppingList/provenance, ordered Pantry evidence and zero-or-more remaining ShoppingItems;
- full Pantry coverage may yield an empty remaining list without hiding the original requirement/evidence;
- identity/order/requirement/quantity/evidence drift fails closed;
- malformed and semantic request errors are sanitized;
- OpenAPI 3.1 and generated TypeScript client are synchronized;
- architecture guards constrain M3.5.2 to accepted M3.2 + Pantry + neutral Shopping dependencies and protect M3.2/M3.3 reverse dependency direction.

Acceptance proof:

- final reviewed feature head `1e08ee4f5111bb493eeb100cfc2579d6fbafa708` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks, no threads;
- squash merge `0dfbef49d265069578968fdedd18828c9452baca`;
- issue #124 closed `completed`;
- exact merge SHA — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

## Next deterministic target — M3.5.3 Pantry-aware WeeklyPlan → Comparison composition

M3.5.3 should remain a **new explicit stateless boundary**, not mutate accepted M3.3.

Required design questions before production code:

1. accept locality + WeeklyPlan + request-scoped Pantry while reusing accepted M3.5.2 input semantics;
2. treat M3.5.2 as authoritative for original weekly projection, Pantry audit evidence and remaining demand;
3. pass only remaining shopping demand to accepted ComparisonPreview when demand exists;
4. preserve original weekly shopping + Pantry evidence + remaining demand beside retailer comparison evidence;
5. fail closed on cross-boundary identity/order/quantity drift;
6. explicitly define the truthful result when Pantry covers **all** shopping demand, because accepted ComparisonPreview currently assumes a non-empty request and must not receive fabricated demand;
7. leave persistence, browser controls and provider acquisition out of this slice;
8. keep existing M3.3 behavior unchanged with regression/architecture tests.

M3.5.4 remains the responsive Pantry-control/browser slice after M3.5.3 semantics are accepted.

Explicit omit-all / never-buy exclusions remain a separate future semantic decision rather than being encoded as zero/negative Pantry quantities.

## Magnit production state

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because affirmative right to operate the intended recurring production acquisition/reuse model has not been established. It is not a legal adjudication. No production Spring/HTTP Magnit acquisition is activated.

## Parallel mandatory work

Continue without blocking deterministic M3 work unless evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific structured package semantics only where source evidence proves them;
- retailer-specific production-access decisions before activation;
- successful real **`v0.1.0-rc.3` GitHub Release** proving final image promotion, SBOM/attestation and digest smoke evidence.

## Permanent core invariants

1. Shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer remains visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness are independent.
4. Precise addresses are sensitive and redacted by default.
5. Provider/acquisition/fulfillment identifiers remain internal.
6. `UNKNOWN` availability is never coerced and observation time is not misrepresented as provider freshness.
7. Matching ambiguity never becomes a hidden winner.
8. Package quantity is explicit structured evidence; mass, volume and count are not interchangeable.
9. Incomplete baskets never expose misleading complete-basket totals.
10. Production-access policy scopes acquisition before source invocation; out-of-scope evidence is a contract violation.
11. Ordinary CI/browser acceptance makes no live retailer requests and production preview never falls back to deterministic fixtures.
12. Universal retailer connectivity remains mandatory; public technical accessibility alone is never production authorization.
13. Recipe/WeeklyPlan semantics reuse neutral Shopping requirement/quantity normalization rather than duplicating it.
14. Recipe/planner/Pantry provenance remains outside neutral Shopping Core types.
15. Automatic Recipe/WeeklyPlan/Pantry matching remains exact requirement + canonical unit; no fuzzy/AI equivalence is implicit.
16. ShoppingItem identity remains list + normalized requirement + canonical unit scoped and independent of amount/servings.
17. WeeklyPlan caller order is explicit and independent from day metadata.
18. M3.2 owns planner projection; M3.3 owns planner→comparison composition; neither is silently mutated by Pantry slices.
19. M3.5.1 owns Pantry subtraction semantics; higher layers compose it rather than reimplementing subtraction.
20. M3.5.2 preserves original weekly demand and audit evidence even when remaining demand is empty.
21. Pantry-aware comparison must never fabricate shopping demand solely to satisfy a downstream non-empty comparison contract.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 with generated TypeScript client;
- Next.js 16.3 / React 19.2 / TypeScript 5.9 / Node 24;
- Vitest + Playwright;
- deterministic CI/security/release gates before acceptance.
