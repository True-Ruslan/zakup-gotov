# Roadmap

Updated: 2026-08-15

The roadmap is evidence-driven. Milestones change when integration evidence, product behavior or production constraints contradict an earlier assumption.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

Technical coverage, production-access readiness and deterministic product/core maturity remain separate dimensions.

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Accepted evidence: Perekrestok/Pyaterochka browser-bridge paths, Magnit public-web technical feasibility, multiple acquisition modes, deterministic sanitized verification and retailer-neutral architecture.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).  
Accepted hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted guarantees include canonical retailer visibility/readiness, deterministic Shopping quantities/identity, provider/location provenance, freshness/availability evidence, exact/normalized product matching, whole-package basket semantics, truthful incomplete/uncertain states, pre-acquisition production-access gating and stateless comparison preview + responsive manual-list flow.

## M2 — Recipes — COMPLETE / ACCEPTED

Goal achieved: recipes are a deterministic first-class source of shopping requirements.

Accepted slices:

- **M2.1 Recipe domain + Recipe → ShoppingList** — COMPLETE / ACCEPTED;
- **M2.2 stateless Recipe shopping preview API** — COMPLETE / ACCEPTED;
- **M2.3 Recipe → Comparison composition** — COMPLETE / ACCEPTED;
- **M2.4 responsive Recipe UI** — COMPLETE / ACCEPTED;
- **M2.5 deterministic multi-Recipe aggregation** — COMPLETE / ACCEPTED.

Permanent direction: exact normalized requirement + canonical unit remains the only implicit Recipe merge rule. Fuzzy/synonym/AI equivalence is never introduced silently.

## M3 — Weekly Planning / Pantry — COMPLETE / ACCEPTED

Goal: combine meals into one deterministic weekly shopping projection, compare it across retailers and subtract explicit request-scoped Pantry evidence without contaminating accepted Recipe, Shopping, planner or provider semantics.

### M3.1 — WeeklyPlan domain + deterministic shopping composition — COMPLETE / ACCEPTED

Acceptance: [`m3-1-weekly-plan-acceptance-2026-08-14.md`](m3-1-weekly-plan-acceptance-2026-08-14.md).  
Accepted merge: `13e09c63959b050d431cc913597fc868aa408718`.

Established ordered meal occurrences, day metadata without meal-slot taxonomy, repeated Recipe use, target servings, deterministic WeeklyPlan ShoppingList and planner provenance while delegating Recipe scaling/merge/identity to accepted M2.5.

### M3.2 — Stateless WeeklyPlan shopping preview API — COMPLETE / ACCEPTED

Acceptance: [`m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md`](m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md).  
Accepted merge: `9682ad1230910fc268ca3cddd8601a3fad7b100e`.

Boundary: `POST /api/v1/weekly-plan-shopping-previews`.

M3.2 owns transient planner/Recipe identity, validation, canonical weekly shopping projection and self-contained occurrence + Recipe + ingredient provenance.

### M3.3 — WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Acceptance: [`m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md`](m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md).  
Accepted merge: `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`.

Boundary: `POST /api/v1/weekly-plan-comparison-previews`.

M3.3 composes accepted M3.2 with accepted ComparisonPreview while preserving ShoppingItem identity/order/requirement/quantity and keeping comparison/provider semantics downstream.

### M3.4 — Responsive Weekly Planning UI — COMPLETE / ACCEPTED

Acceptance: [`m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md`](m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md).  
Accepted merge: `1201030aed45075c676f796920b6268cdcf8e036`.

Weekly Planning is the primary browser journey. It supports `1..35` ordered occurrences, day/serving/Recipe editing, canonical weekly-shopping rendering before comparison, fail-closed transport and deterministic desktop/mobile/accessibility Playwright. Browser acceptance makes no live retailer request.

### M3.5 — Pantry / exclusions semantics — IN PROGRESS

Goal: subtract explicitly known-at-home requirements from accepted weekly shopping demand with inspectable evidence and without hidden ingredient loss, then compose the remaining demand into retailer comparison and responsive controls.

#### M3.5.1 — Pure Pantry subtraction semantics — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md`](superpowers/specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md)  
Acceptance: [`m3-5-1-pantry-subtraction-semantics-acceptance-2026-08-15.md`](m3-5-1-pantry-subtraction-semantics-acceptance-2026-08-15.md)  
Accepted merge: `bcc644bb243a63941e7629755f1b3196d94332c2`.

Accepted semantics:

- pure provider-neutral `pantry` package over canonical Shopping types;
- exact `(ShoppingRequirement, canonical QuantityUnit)` matching only;
- accepted kg→g / l→ml canonicalization reused;
- duplicate Pantry rows aggregate by exact key and stock is consumed once in source ShoppingList order;
- subtraction is `min(required, available)` and never creates zero/negative remaining items;
- partial coverage preserves ShoppingItem identity/order;
- full coverage removes remaining demand but retains ordered audit evidence;
- audit states: `UNCHANGED / PARTIALLY_COVERED / FULLY_COVERED`;
- no endpoint, persistence, UI, provider behavior, fuzzy/AI matching or omit-all semantics.

#### M3.5.2 — Stateless Pantry-aware WeeklyPlan shopping preview — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-design.md`](superpowers/specs/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview.md`](superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-shipping.md`](superpowers/plans/2026-08-15-m3-5-2-pantry-weekly-plan-shopping-preview-shipping.md)  
Acceptance: [`m3-5-2-pantry-weekly-plan-shopping-preview-acceptance-2026-08-15.md`](m3-5-2-pantry-weekly-plan-shopping-preview-acceptance-2026-08-15.md)  
Accepted merge: `0dfbef49d265069578968fdedd18828c9452baca`.

Boundary:

`POST /api/v1/weekly-plan-pantry-shopping-previews`

Accepted result:

- a new stateless composition, leaving M3.2 and M3.3 endpoints unchanged;
- accepted M3.2 remains authoritative for WeeklyPlan/Recipe validation, scaling, aggregation, Shopping identity/order and provenance;
- request-scoped Pantry rows may be empty and use accepted requirement/quantity vocabulary;
- accepted M3.5.1 adjustment is applied exactly once;
- response preserves original WeeklyPlan projection + original ShoppingList/provenance + ordered Pantry evidence + zero-or-more remaining ShoppingItems;
- full Pantry coverage legitimately yields zero remaining items without losing audit evidence;
- identity/order/requirement/quantity/evidence drift fails closed;
- sanitized HTTP validation, OpenAPI 3.1 and generated TypeScript client are synchronized;
- architecture keeps M3.5.2 on accepted M3.2 + Pantry + neutral Shopping boundaries only.

Acceptance proof:

- final reviewed head `1e08ee4f5111bb493eeb100cfc2579d6fbafa708` — **9/9 PR workflows SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks, no threads;
- squash merge `0dfbef49d265069578968fdedd18828c9452baca`;
- issue #124 closed `completed`;
- exact merge — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

#### M3.5.3 — Pantry-aware WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md`](superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md)  
Acceptance: [`m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md`](m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md)  
Accepted merge: `079a53be066fa488ee01da18a109f4f2b1484800`.

Boundary:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

Accepted result:

- accepted M3.5.2 owns original weekly projection, Pantry evidence and remaining demand;
- only remaining demand enters accepted ComparisonPreview;
- `NO_REMAINING_DEMAND` is explicit and skips ComparisonPreviewService/runtime acquisition entirely;
- zero-demand responses omit the comparison payload on the wire;
- locality validation is independent of Pantry coverage;
- UUID/order/requirement/canonical quantity preservation is fail-closed;
- downstream comparison validation is sanitized;
- OpenAPI/generated TypeScript and architecture/regression gates are synchronized;
- M3.3 and M3.5.2 remain unchanged.

Acceptance proof:

- final reviewed head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789` — **9/9 PR workflows SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no unresolved review findings/threads;
- squash merge `079a53be066fa488ee01da18a109f4f2b1484800`;
- issue #127 closed `completed`;
- exact merge — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

#### M3.5.4 — Responsive Pantry controls — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md`](superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md)  
Acceptance: [`m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md`](m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md)  
Accepted merge: `7a437b612b4e0a36e10f2ae2a5708346f93431ce`.

Accepted result:

- primary WeeklyPlan browser transport uses generated M3.5.3 only;
- request-scoped Pantry controls are optional and stateless;
- server-owned original/audit/remaining demand is rendered without browser subtraction;
- full Pantry coverage renders `NO_REMAINING_DEMAND` without retailer output;
- mobile/accessibility/fail-closed and Recipe/manual regressions are deterministic and network-safe.

Acceptance proof:

- final reviewed head `d2fefd5391b9ec471192aff4120adfc4e7c0cb4c` — **9/9 PR workflows SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no unresolved findings/threads;
- squash merge `7a437b612b4e0a36e10f2ae2a5708346f93431ce`;
- issue #130 closed `completed`;
- exact merge — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

#### Explicit omit-all exclusions — DEFERRED SEMANTIC DECISION

A boolean `never buy` / omit-all rule is not equivalent to Pantry stock and must not be encoded as zero/negative quantity. Add it only after a separate design establishes product need and truthful provenance semantics.

Persistence/saved-plan history remains deferred until repeat-use evidence demonstrates product value and correctness requirements justify it.

## Parallel connectivity / operational work

Continue without blocking deterministic M4 work unless evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- structured package semantics only where source evidence proves them;
- retailer-specific production-access/right-to-operate decisions before activation;
- successful real **`v0.1.0-rc.3`** release event with final image promotion, SBOM/attestation and digest smoke evidence.

## M4 — Basket Optimization — CURRENT

Goal: optimize real checkout cost rather than naive SKU sums while preserving truthful eligibility, completeness, uncertainty, retailer visibility and production-access semantics.

Scope: explicit checkout economics, one-retailer truthful totals, richer package/substitute optimization, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

### M4.1 — Basket economics foundation — COMPLETE / ACCEPTED

Acceptance: [`m4-1-basket-economics-foundation-acceptance-2026-08-15.md`](m4-1-basket-economics-foundation-acceptance-2026-08-15.md)  
Accepted implementation merge: `3ccaa7b2acc1e81d7360c55872882a4252c96cae`.

Accepted result:

- explicit known/unknown delivery and service fees with known zero preserved;
- explicit known/unknown minimum-order threshold and `MET / NOT_MET / UNKNOWN` assessment from merchandise subtotal only;
- merchandise subtotal remains inspectable independently from checkout-total knowledge;
- unknown material fee fails closed rather than becoming zero;
- exact currency-compatible `BigDecimal` checkout arithmetic with no hidden rounding;
- self-validating assessment prevents contradictory economics state;
- pure basket-domain boundary with no provider acquisition, optimizer, HTTP/OpenAPI/UI or M1 quote mutation.

### M4.2 — One-retailer truthful total comparison — NEXT

Compose accepted `SingleStoreBasketQuote` merchandise evidence with M4.1 economics and expose a deterministic retailer-level assessment without choosing a winner.

Required semantics:

- checkout-total knowledge and retailer eligibility are independent;
- minimum order `MET` may be eligible subject to accepted basket/access state;
- minimum order `NOT_MET` is ineligible even when checkout arithmetic is known;
- minimum order `UNKNOWN` yields unknown eligibility and is never silently eligible;
- an unknown material fee keeps checkout total unknown and cannot support a cheapest claim;
- accepted `COMPLETE / UNCERTAIN / INCOMPLETE / UNAVAILABLE`, matching ambiguity, retailer visibility and production-access rules remain authoritative;
- deterministic acceptance uses supplied/sanitized evidence only and makes no live retailer requests.

### M4.3 — Basket optimizer

After M4.2 establishes truthful comparable retailer assessments, define deterministic optimizer eligibility, candidate ordering/tie semantics, package/substitution policy and confidence/freshness handling. Do not let an unknown/ineligible/incomplete candidate become a hidden winner.

### M4.4 — Optimization UX

Project accepted optimizer evidence into responsive browser flows with explainable subtotal/fees/minimum-order/eligibility/total states. Browser code must render server-owned decisions rather than recomputing economics or winners.

## M5 — Productization

Goal: reliable repeat use with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: Android/iOS clients using shared API vocabulary/generated contracts after browser product semantics are stable enough to justify native clients.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes behavior correct, explainable and worth the operational cost.
