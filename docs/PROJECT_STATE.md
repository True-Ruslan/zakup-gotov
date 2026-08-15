# Project State

Updated: 2026-08-15

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M4 — Basket Optimization**

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
- M3.5.2 Stateless Pantry-aware WeeklyPlan shopping preview API — **COMPLETE / ACCEPTED** (#124 / #125);
- M3.5.3 Pantry-aware WeeklyPlan → Comparison composition — **COMPLETE / ACCEPTED** (#127 / #128);
- M3.5.4 Responsive Pantry controls — **COMPLETE / ACCEPTED** (#130 / #131);
- M3 Weekly Planning / Pantry deterministic product slice — **COMPLETE / ACCEPTED**.
- M4.1 Basket economics foundation — **COMPLETE / ACCEPTED** (#133 / #134).
- M4.2 One-retailer truthful total comparison — **COMPLETE / ACCEPTED** (#136 / #137).
- M4.3 Deterministic basket optimizer — **COMPLETE / ACCEPTED** (#139 / #140).

Current deterministic target: **M4.4 — Optimization UX**.

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

## M3.5 — Pantry / exclusions semantics — COMPLETE / ACCEPTED

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

### M3.5.3 — Pantry-aware WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md`](superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md)  
Acceptance: [`m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md`](m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md)  
Accepted merge: `079a53be066fa488ee01da18a109f4f2b1484800`.

Accepted boundary:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

Accepted result:

- accepted M3.5.2 remains authoritative for original WeeklyPlan projection, Pantry evidence and remaining demand;
- only non-empty remaining demand enters accepted ComparisonPreview;
- full Pantry coverage returns explicit `NO_REMAINING_DEMAND` and never invokes ComparisonPreviewService/runtime retailer acquisition;
- zero-demand wire output omits `comparisonPreview` rather than serializing null;
- locality is validated independently of Pantry coverage;
- ShoppingItem identity/order/requirement/canonical quantity are preserved exactly and bridge drift fails closed;
- derived ComparisonPreview validation is sanitized under the M3.5.3 problem boundary;
- OpenAPI 3.1/generated TypeScript and architecture/regression gates are synchronized;
- existing M3.3/M3.5.2 behavior remains unchanged.

Acceptance proof:

- final reviewed feature head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no unresolved P0/P1/P2/P3/nitpicks or threads;
- squash merge `079a53be066fa488ee01da18a109f4f2b1484800`;
- issue #127 closed `completed`;
- exact merge SHA — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

### M3.5.4 — Responsive Pantry controls — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md`](superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md)  
Acceptance: [`m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md`](m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md)  
Accepted merge: `7a437b612b4e0a36e10f2ae2a5708346f93431ce`.

Accepted browser result:

- primary WeeklyPlan journey consumes generated M3.5.3 Pantry-aware comparison only;
- Pantry rows are optional request-scoped browser state with no persistence/history;
- original demand, Pantry audit and remaining demand render directly from server evidence;
- production browser performs no Pantry matching/canonicalization/subtraction;
- `NO_REMAINING_DEMAND` is a truthful terminal state with no fabricated retailer comparison;
- Recipe/manual-list journeys remain regression-covered;
- deterministic desktop/mobile/accessibility acceptance makes no live retailer request.

Acceptance proof:

- final reviewed feature head `d2fefd5391b9ec471192aff4120adfc4e7c0cb4c` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks or unresolved threads;
- squash merge `7a437b612b4e0a36e10f2ae2a5708346f93431ce`;
- issue #130 closed `completed`;
- exact merge SHA — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

Explicit omit-all / never-buy exclusions remain intentionally deferred. They are not Pantry stock and must not be represented as zero/negative quantities.

## M4.1 — Basket economics foundation — COMPLETE / ACCEPTED

Acceptance: [`m4-1-basket-economics-foundation-acceptance-2026-08-15.md`](m4-1-basket-economics-foundation-acceptance-2026-08-15.md).  
Accepted implementation merge: `3ccaa7b2acc1e81d7360c55872882a4252c96cae`.

Accepted semantics:

- existing `BasketTotal(BigDecimal, ISO-4217)` remains the monetary convention;
- delivery/service fees are explicitly `KNOWN / UNKNOWN`; known zero is not unknown;
- minimum-order evidence is explicitly known/unknown and evaluates to `MET / NOT_MET / UNKNOWN` from merchandise subtotal only;
- merchandise subtotal and checkout-total knowledge are separate;
- any unknown material fee makes checkout total unknown without inventing zero or hiding merchandise subtotal;
- known economics components must share the merchandise-subtotal currency;
- exact `BigDecimal` arithmetic adds no hidden rounding/rescaling;
- public `BasketEconomicsAssessment` rejects contradictory status/amount constructions;
- the M4.1 foundation remains pure basket-domain code and does not acquire provider data or change accepted M1 quote/planner behavior.

Acceptance proof:

- final reviewed feature head `a0fcd626017f93e49fc6a70c4403b68404efe6d7` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved threads;
- squash merge `3ccaa7b2acc1e81d7360c55872882a4252c96cae` with expected-head protection;
- issue #133 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**.

## M4.2 — One-retailer truthful total comparison — COMPLETE / ACCEPTED

Acceptance: [`m4-2-one-retailer-truthful-total-acceptance-2026-08-15.md`](m4-2-one-retailer-truthful-total-acceptance-2026-08-15.md).  
Accepted implementation merge: `69f9cb1afd1b16af938052bbca570cbd4ce52557`.

Accepted semantics:

- accepted M1 `RetailerComparisonView.total` remains merchandise subtotal and is never silently redefined as checkout total;
- M4.1 economics are bound to `RetailerId` at the M4.2 public composition boundary and cross-retailer fee/minimum evidence fails closed before arithmetic;
- checkout eligibility is explicit `ELIGIBLE / INELIGIBLE / UNKNOWN` and remains independent from arithmetic checkout-total knowledge;
- known minimum `NOT_MET` is ineligible even when checkout arithmetic is fully known; upstream `UNCERTAIN` or unknown minimum never becomes silently eligible;
- comparability is explicit `COMPARABLE / NOT_COMPARABLE` and requires `READY + ELIGIBLE + KNOWN checkout total`;
- only comparable assessments expose `comparableCheckoutTotal`, exactly equal to the accepted M4.1 checkout total;
- known arithmetic totals for ineligible/unknown/uncertain states remain inspectable but cannot support a cheapest claim;
- `INCOMPLETE / UNAVAILABLE` produce no fabricated checkout assessment;
- public assessment/result objects reject subtotal, eligibility, comparability and cross-comparison drift;
- architecture permits only accepted `basket`, accepted `comparison` and the finite `RetailerId` bridge, with no provider/network/API/UI or winner/ranking behavior.

Acceptance proof:

- final reviewed feature head `1d6dae470c04ab1d8279f891766fc16698286edb` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved threads;
- squash merge `69f9cb1afd1b16af938052bbca570cbd4ce52557` with expected-head protection;
- issue #136 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**, including CodeQL Java and JavaScript/TypeScript.

## M4.3 — Deterministic basket optimizer — COMPLETE / ACCEPTED

Acceptance: [`m4-3-deterministic-basket-optimizer-acceptance-2026-08-15.md`](m4-3-deterministic-basket-optimizer-acceptance-2026-08-15.md).  
Accepted implementation merge: `c854526c30a1b0b1b6b435ae37608da0d9501955`.

Accepted semantics:

- optimizer input is a non-empty ordered set of accepted M4.2 checkout results with unique retailer identity;
- all input candidates remain visible in original order, but input order is never a winner/tie-break rule;
- only explicit M4.2 `COMPARABLE` candidates compete;
- comparable candidates must use one currency; mixed comparable currencies fail closed;
- exact `BigDecimal.compareTo` ordering adds no rounding or rescaling;
- numeric-equal minima are explicit `TIE`, including differing decimal scales, and every tied minimum remains visible in original order;
- no retailer order/ID, freshness, provider timestamp, package/SKU identity or arbitrary iteration order breaks a tie;
- accepted M1 package/basket selections are immutable optimizer inputs; no substitute/package recomputation or multi-store split occurs;
- freshness remains inspectable evidence but is not converted into a monetary penalty or fabricated confidence score;
- public optimization result recomputes expected status/minimum set and rejects forged state;
- M4.3 consumes retailer identity through the M4.2-owned result projection and has no direct `comparison` / `retailer` dependency;
- no provider acquisition, HTTP/OpenAPI/UI, persistence or live retailer request is introduced.

Acceptance proof:

- final reviewed feature head `ddc5fed0d3bb98d9c17e5f1ec739ffad9ba77ad5` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved threads;
- squash merge `c854526c30a1b0b1b6b435ae37608da0d9501955` with expected-head protection;
- issue #139 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**.

## Next deterministic target — M4.4 Optimization UX

Project accepted M4.1–M4.3 evidence into responsive browser flows. The browser must render server-owned merchandise subtotal, known/unknown fees, minimum-order state, eligibility/comparability and `NO_COMPARABLE_CANDIDATES / UNIQUE_WINNER / TIE` outcomes without recomputing economics or choosing a winner client-side. Browser acceptance remains deterministic and must make no live retailer requests.

## Magnit production state

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because affirmative right to operate the intended recurring production acquisition/reuse model has not been established. It is not a legal adjudication. No production Spring/HTTP Magnit acquisition is activated.

## Parallel mandatory work

Continue without blocking deterministic M4 work unless evidence invalidates accepted core assumptions:

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
22. Merchandise subtotal, checkout-total knowledge, checkout eligibility and optimizer comparability are separate facts; one must never be silently substituted for another.
23. Retailer checkout economics must be bound to the same `RetailerId` as the retailer comparison before arithmetic; cross-retailer economics evidence fails closed.
24. A known arithmetic checkout total does not imply an eligible or comparable candidate.
25. Only explicit M4.2 `COMPARABLE` candidates may participate in M4.3 cheapest-basket selection; all other candidate states remain inspectable but cannot win.
26. Exact numeric minimum ties remain explicit ties; retailer order/ID, freshness, timestamps and package/SKU metadata never select a hidden winner.
27. M4.3 never recomputes accepted package/SKU selections or converts freshness into a monetary penalty/confidence score.
28. Browser optimization UX must render server-owned economics and optimizer decisions rather than recomputing them client-side.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 with generated TypeScript client;
- Next.js 16.3 / React 19.2 / TypeScript 5.9 / Node 24;
- Vitest + Playwright;
- deterministic CI/security/release gates before acceptance.