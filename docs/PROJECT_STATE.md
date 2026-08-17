# Project State

Updated: 2026-08-18

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M5 — Productization**

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
- M4.4.1 Server-owned optimization preview API — **COMPLETE / ACCEPTED** (#142 / #143).
- M4.4.2 Responsive Optimization UX — **COMPLETE / ACCEPTED** (#145 / #146).
- M4.4 Optimization UX — **COMPLETE / ACCEPTED**.
- M4 Basket Optimization — **COMPLETE / ACCEPTED**.
- Pre-release web runtime hardening — **COMPLETE / ACCEPTED** (#150).
- M5.1 Private local WeeklyPlan draft — **COMPLETE / ACCEPTED** (#148 / #149).
- Retailer Bridge persistent-session / SPA / store-change lifecycle hardening — **COMPLETE / ACCEPTED** (#54 / #153).
- Chizhik Phase D1 active normal-browser foundation — **IMPLEMENTED / MERGED; LIVE TRANSPORT GATE UNRESOLVED** (#167 / #168).

Current deterministic product phase: **M5 — Productization**.  
Immediate operational target: **`v0.1.0-rc.3` end-to-end release validation**.  
M5.2 remains intentionally unselected until release-candidate/manual-use evidence justifies the next productization constraint.

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

## M4.4.1 — Server-owned optimization preview API — COMPLETE / ACCEPTED

Acceptance: [`m4-4-1-server-owned-optimization-preview-acceptance-2026-08-16.md`](m4-4-1-server-owned-optimization-preview-acceptance-2026-08-16.md).  
Authoritative design: [`superpowers/specs/2026-08-15-m4-4-1-server-owned-optimization-preview-design.md`](superpowers/specs/2026-08-15-m4-4-1-server-owned-optimization-preview-design.md).  
Implementation plan: [`superpowers/plans/2026-08-15-m4-4-1-server-owned-optimization-preview.md`](superpowers/plans/2026-08-15-m4-4-1-server-owned-optimization-preview.md).  
Accepted implementation merge: `67679282a388da16706c46a3caf3ff46b2b67d54`.

Accepted boundary:

`POST /api/v1/weekly-plan-pantry-optimization-previews`

Accepted semantics:

- accepted M3.5.3 projection remains unchanged and is returned as the Pantry/comparison audit;
- additive detailed-computation seams expose accepted comparison/catalog state to downstream server composition without reconstructing it from presentation DTOs;
- full Pantry coverage remains `NO_REMAINING_DEMAND`, omits `optimizationPreview` on the wire and stops before checkout-economics source or optimizer work;
- provider-neutral checkout economics are scoped only to assessable retailers; absent evidence is explicit `UNKNOWN`, never fabricated zero fees/minimum;
- M4.2 remains authoritative for eligibility/comparability and M4.3 remains authoritative for winner/tie/lowest comparable total;
- the M4.4.1 projection validates itself against the accepted `BasketOptimizationResult` instead of implementing a second minima algorithm;
- public retailer IDs are canonical product IDs and internal optimizer authority/provider/acquisition/fulfillment details remain off the wire;
- OpenAPI 3.1 and generated TypeScript client are synchronized;
- architecture guards reject provider/database/persistence coupling and reverse dependencies into accepted lower layers;
- production default checkout economics is intentionally no-op/unknown until a separately accepted evidence path exists;
- deterministic CI and browser acceptance make no live retailer request.

Acceptance proof:

- final reviewed feature head `9d343f18e1391a9d249625e2cdab6de02b13e913` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only Change Review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved review threads;
- squash merge `67679282a388da16706c46a3caf3ff46b2b67d54` with expected-head protection;
- issue #142 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**, including CodeQL Java and JavaScript/TypeScript.

## M4.4.2 — Responsive Optimization UX — COMPLETE / ACCEPTED

Acceptance: [`m4-4-2-responsive-optimization-ux-acceptance-2026-08-16.md`](m4-4-2-responsive-optimization-ux-acceptance-2026-08-16.md).  
Authoritative design: [`superpowers/specs/2026-08-16-m4-4-2-responsive-optimization-ux-design.md`](superpowers/specs/2026-08-16-m4-4-2-responsive-optimization-ux-design.md).  
Implementation plan: [`superpowers/plans/2026-08-16-m4-4-2-responsive-optimization-ux.md`](superpowers/plans/2026-08-16-m4-4-2-responsive-optimization-ux.md).  
Accepted implementation merge: `7252b9264ed7a2ffe896b1a1fcddb09a78edc04c`.

Accepted browser result:

- the primary WeeklyPlan/Pantry flow consumes only the generated M4.4.1 optimization endpoint and response vocabulary;
- original weekly demand, Pantry audit, remaining demand and retailer comparison remain visible from the accepted nested M3.5.3 projection;
- `NO_REMAINING_DEMAND` is terminal and renders no retailer/optimization result;
- `NO_COMPARABLE_CANDIDATES`, `UNIQUE_WINNER` and `TIE` render exactly from server-owned optimizer status/IDs/lowest-total evidence;
- every checkout row renders server-owned merchandise subtotal, known/unknown fees, minimum-order evidence/state, checkout-total knowledge/value, eligibility and comparability;
- known zero remains visibly different from unknown economics;
- retailer display metadata is joined by canonical identity with a `Map`; duplicate/missing/mismatched identities or contradictory optimizer structure fail closed;
- production browser code performs no M4.1 arithmetic, M4.2 assessment, M4.3 monetary ranking or tie-breaking;
- Recipe/manual-list flows remain regression-covered secondary journeys;
- deterministic desktop/mobile/accessibility browser acceptance makes no live retailer/provider request.

Acceptance proof:

- final reviewed feature head `ca2060546936f388556f62e49c6963d846274847` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- Web lint/typecheck/component tests/Next production build and Chromium Playwright acceptance — **SUCCESS**;
- read-only Change Review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved review threads;
- squash merge `7252b9264ed7a2ffe896b1a1fcddb09a78edc04c` with expected-head protection;
- issue #145 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**.

## M5.1 — Private local WeeklyPlan draft — COMPLETE / ACCEPTED

Acceptance: [`m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md`](m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md).  
Authoritative design: [`superpowers/specs/2026-08-16-m5-1-private-local-weekly-plan-draft-design.md`](superpowers/specs/2026-08-16-m5-1-private-local-weekly-plan-draft-design.md).  
Implementation plan: [`superpowers/plans/2026-08-16-m5-1-private-local-weekly-plan-draft.md`](superpowers/plans/2026-08-16-m5-1-private-local-weekly-plan-draft.md).  
Accepted implementation merge: `2f2b96d18521b8bb04f6ee17182d61711322de08`.

Accepted browser-local persistence result:

- exactly one versioned same-origin key, `zakup-gotov.weekly-plan-draft.v1`;
- only editable semantic WeeklyPlan/Pantry input is persisted: locality, occurrence order/day/servings, Recipe title/base servings/ingredients and Pantry requirement/amount/unit rows;
- editable numeric values persist as strings to preserve unfinished input without coercion;
- React presentation keys, generated server/domain identities, comparison results, checkout economics, optimizer state and provider/acquisition/fulfillment evidence are excluded from storage;
- restore happens only after client mount and never automatically submits a comparison;
- autosave starts only after the initial read/restore attempt settles, is debounced and skips semantic no-op writes;
- failed initial reads cannot trigger blind blank overwrite of unknown stored data;
- malformed/unsupported data is discarded fail-closed when cleanup is possible; cleanup/remove failure becomes explicit storage-unavailable state;
- storage get/set/remove exceptions never break editing or explicit comparison submission;
- explicit clear resets input, errors and derived results, removes the local key and performs no comparison request;
- clear is gated until restore readiness and while a comparison is pending, preventing delayed restore from resurrecting removed state;
- deterministic Playwright proves semantic-only JSON, reordered occurrence/Pantry restore, zero implicit POST before explicit submit, clear and blank second reload;
- accepted M4.4.2 remains the only comparison/economics/optimizer authority.

Acceptance proof:

- final reviewed feature head `6c54479044e41e5177739b57eb891830a79691f8` — **9/9 PR workflow groups SUCCESS**;
- Web lint/typecheck/component tests/Next production build and actually executed Chromium Playwright — **SUCCESS**;
- clean read-only review, no remaining P0/P1/P2/P3/nitpicks and no unresolved threads;
- squash merge `2f2b96d18521b8bb04f6ee17182d61711322de08` with expected-head protection;
- issue #148 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**, 0 failures, including both CodeQL languages.

### Pre-release web runtime hardening — COMPLETE / ACCEPTED

Manual source-checkout testing before M5.1 exposed two independent runtime/developer-experience defects. PR #150 fixed and accepted both:

- `web dev` builds `@zakup-gotov/api-client` before starting Next.js, so a clean installed workspace does not fail module resolution;
- manual-list form SSR/hydration uses deterministic presentation identity; transient request UUIDs are generated only on submit.

Accepted merge: `ef366c4ea65169dc3839cbf78c1df25d16f1dffa`; exact merge **8/8 normal push workflows SUCCESS**.

## Retailer Bridge operational hardening — COMPLETE / ACCEPTED

Issue #54 is no longer outstanding. PR #153 established the accepted long-lived browser-bridge lifecycle model and merged as `a3f7edb8028f2ba030c26d71ff10c17177e1abb9`.

Accepted result:

- persistent browser sessions no longer keep stale retailer/store context after SPA navigation or store changes;
- same-document navigation is event-driven rather than polling-based;
- fresh fulfillment-context evidence is required before recollecting a new route;
- stale pre-navigation resources and obsolete async collection results cannot republish old-store observations;
- monotonic lifecycle revisions prevent older service-worker writes from overwriting newer lifecycle state;
- retained resource evidence stays bounded;
- production extension permissions remain `storage` only and no cookies, auth headers, response bodies, `webRequest`, stealth or proxy behavior was added;
- deterministic Chromium coverage includes same-store SPA refresh, store change, overlapping navigation/context changes, rapid navigation and obsolete in-flight collection rejection.

This hardening is part of the accepted browser acquisition foundation for existing Perekrestok/Pyaterochka paths and for ongoing retailer onboarding.

## Chizhik connectivity track — IMPLEMENTED THROUGH D1 / LIVE GATE UNRESOLVED

Chizhik remains mandatory connectivity work, but merged implementation and live acquisition acceptance are intentionally separated.

### Phase A — plain HTTPS feasibility — IMPLEMENTED / MERGED

PR #156 merged as `1713b8c90b7370a54f733910c6302666b91d3413`.

- bounded opt-in direct HTTPS probes target only the known unauthenticated catalog surface;
- finite timeouts, no redirects/retries, sanitized response-shape evidence and no credential/session extraction;
- Phase A proves only technical transport evidence and does not activate a production provider.

### Phase B — browser discovery — IMPLEMENTED / MERGED

PR #158 merged as `18c4f16027cbf85cff9803bfd0ed4d0cdf74533a`; PR #160 merged trusted `main` canary artifact publication as `98cc08698a19fdc7b4967d938e505e1f36955d63`.

- exact official `chizhik.club` origin registration and observation-only discovery;
- only allow-listed first-party catalog resource paths are retained as sanitized evidence;
- discovery cannot persist guessed offers or infer fulfillment context;
- exact bridge build that passes trusted `main` Chromium E2E can be published as a short-lived SHA-named canary artifact.

### Phase C — public catalog document reachability — IMPLEMENTED / MERGED

PRs #164/#165/#166 merged as `1b044ab58704c1a44a4fe4aa89dbeed4d961f1eb`, `21dbbdfb8db61ea00270c1ca2e8ae69e9f3799e1` and `a6e306da3b5a33710f94ef1956d71b1a462509e7`.

- the probe is restricted to exact HTTPS `media.chizhik.club` catalog PDF paths;
- redirects are bounded and revalidated before following;
- production transport is body-less `HEAD`, with no cookies, auth, body parsing or header imitation;
- controlled issue-comment execution publishes only sanitized reachability evidence;
- transport failures use a finite safe category vocabulary and secondary GitHub status publication is bounded/best-effort;
- document reachability is explicitly not product/price/fulfillment connectivity.

### Phase D1 — active normal-browser store-directory foundation — IMPLEMENTED / MERGED; LIVE TRANSPORT GATE UNRESOLVED

Issue #167 remains open. PR #168 merged as `e49c151fa44681dffe85fe90116009c86690672e`.

Accepted field evidence on 2026-08-18 from an ordinary user-opened official Chizhik catalog page:

- browser-context `GET https://app.chizhik.club/api/v1/shops/` returned HTTP `200`;
- content type was JSON;
- response was an array with observed store identity/coordinate fields including `sap_id`, `lat` and `lon`;
- the full store response is not retained or published as project evidence.

Merged D1 implementation now provides:

- a fixed-endpoint active browser API client with an eight-second abort deadline;
- strict store-shape and coordinate-range validation;
- async retailer-adapter support while preserving existing synchronous adapters;
- one active Chizhik store-directory discovery attempt per content-script lifecycle;
- real extension Chromium E2E for successful CORS and fail-closed blocked transport;
- an owner-only opt-in stock-Chromium live workflow that emits sanitized status/shape evidence only;
- no stealth, fingerprint spoofing, proxy rotation, credential/header/cookie extraction, mobile impersonation or arbitrary URL proxying.

Current live disposition is **not accepted**: the CI-hosted Phase D probe on the merged D1 `main` head reports `Provider Live Probe / Chizhik Phase D / page-unavailable` as failure evidence. This does not invalidate ordinary repository CI, which passed on the PR head; it means the server-controlled stock-Chromium acquisition path has not reproduced the successful ordinary-user-browser canary.

Decision gate:

- if CI-hosted stock Chromium reproducibly obtains `200 + JSON + sap_id/lat/lon`, a server-controlled browser acquisition worker remains viable;
- if CI-hosted Chromium remains blocked while the ordinary user browser remains successful, prefer the active MV3 Retailer Bridge acquisition path;
- only after that transport disposition should D2 validate one store-scoped product/delivery search endpoint and map only evidenced payload fields to `BrowserObservation` / `ObservedOffer`.

Chizhik product/price offers, D2 and production activation remain **NOT ACCEPTED / NOT IMPLEMENTED**. Technical feasibility does not itself grant production/right-to-operate approval.

## Immediate operational target — v0.1.0-rc.3

The next highest-value mainline validation is a new immutable **`v0.1.0-rc.3` GitHub prerelease** from documentation-synchronized verified `main`.

It must prove the existing release workflow end to end: source/main ancestry verification, repository + browser + production bundle verification, multi-platform staging publication, unchanged HIGH/CRITICAL Trivy policy, SPDX SBOM evidence, staging exact-digest smoke, digest-identical final-package promotion, final exact-digest smoke, provenance attestations, prerelease SemVer tag promotion, final architecture manifest checks and attached release evidence/checksums while leaving `latest` untouched.

M5.2 is intentionally not selected before this release-candidate evidence and subsequent manual product canary. A stable `v0.1.0` remains blocked until at least one prerelease completes the full workflow and its evidence is inspected.

## Magnit production state

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because affirmative right to operate the intended recurring production acquisition/reuse model has not been established. It is not a legal adjudication. No production Spring/HTTP Magnit acquisition is activated.

## Parallel mandatory work

Continue without blocking deterministic M5/release work unless evidence invalidates accepted core assumptions:

- **#36** Kuper supported aggregator investigation;
- Chizhik D1 transport disposition followed by D2 store-scoped product/delivery acquisition only when evidence justifies it;
- Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific structured package semantics only where source evidence proves them;
- retailer-specific production-access decisions before activation;
- successful real **`v0.1.0-rc.3` GitHub Release** proving final image promotion, SBOM/attestation and digest smoke evidence.

Completed parallel hardening:

- **#54 / #153** browser-bridge persistent-session/store-change/SPA lifecycle hardening — **COMPLETE / ACCEPTED**.

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
29. M4.4.1 is the server-owned browser contract for checkout optimization: missing economics stays UNKNOWN, full Pantry coverage skips optimization entirely and accepted M4.2/M4.3 remain the sole assessment/optimizer authorities.
30. The primary WeeklyPlan/Pantry optimization browser flow consumes generated M4.4.1 only; browser joins are identity/structure checks and never become a second checkout-arithmetic, comparability or winner-selection implementation.
31. Browser-local repeat-use persistence is semantic input only: no generated identity, comparison/economics/optimizer result or provider evidence may become local draft authority.
32. Local draft restore never implies submission; browser storage failures fail closed while explicit editing/submission remain usable.
33. Browser acquisition lifecycle evidence must be revision-safe across SPA/store changes; stale context must not be republished after a newer navigation boundary.
34. A successful ordinary-user-browser canary and a successful CI/server-browser transport are separate evidence classes; neither may be silently substituted for the other.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 with generated TypeScript client;
- Next.js 16.3 / React 19.2 / TypeScript 5.9 / Node 24;
- Vitest + Playwright;
- deterministic CI/security/release gates before acceptance.
