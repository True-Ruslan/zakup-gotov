# M4.4.2 Responsive Optimization UX — Acceptance

**Date:** 2026-08-16  
**Status:** COMPLETE / ACCEPTED  
**Issue:** #145  
**Implementation PR:** #146  
**Baseline:** `0d1c0c5a157c3eae8988bec7281de3ffdc7a14c6`  
**Accepted implementation merge:** `7252b9264ed7a2ffe896b1a1fcddb09a78edc04c`

## Decision

M4.4.2 is accepted. The primary WeeklyPlan/Pantry browser journey now consumes the generated M4.4.1 server-owned optimization contract and renders checkout economics, eligibility/comparability and optimizer outcomes without reimplementing M4.1–M4.3 semantics in React.

M4 Basket Optimization is therefore complete at the currently planned product slice. The next canonical phase is M5 Productization.

## Authoritative inputs

- Design: [`superpowers/specs/2026-08-16-m4-4-2-responsive-optimization-ux-design.md`](superpowers/specs/2026-08-16-m4-4-2-responsive-optimization-ux-design.md)
- Implementation plan: [`superpowers/plans/2026-08-16-m4-4-2-responsive-optimization-ux.md`](superpowers/plans/2026-08-16-m4-4-2-responsive-optimization-ux.md)
- Accepted server contract: M4.4.1 `POST /api/v1/weekly-plan-pantry-optimization-previews`

## Accepted browser behavior

The primary browser journey is now:

**weekly meals → original weekly demand → Pantry coverage → remaining demand → retailer comparison → checkout economics → optimizer outcome**.

Accepted behavior:

- the primary WeeklyPlan/Pantry action uses only generated M4.4.1 request/response types and `WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH`;
- the accepted nested M3.5.3 audit remains visible: original demand, Pantry evidence, remaining demand and retailer comparison;
- `NO_REMAINING_DEMAND` remains terminal and renders no retailer or optimization result;
- `NO_COMPARABLE_CANDIDATES` explicitly states that a truthful minimum cannot currently be selected and never fabricates a cheapest retailer;
- `UNIQUE_WINNER` renders the server-provided optimal retailer and server-provided lowest comparable checkout total;
- `TIE` renders every server-provided optimal retailer in server order and never applies a browser tie-break;
- per-retailer checkout presentation includes merchandise subtotal, delivery fee, service fee, minimum-order evidence/state, checkout-total knowledge/value, eligibility and comparability directly from M4.4.1;
- known zero remains a monetary zero while unknown economics remains explicitly unknown;
- retailers without an M4.2 assessment remain visible with an explicit unavailable checkout-calculation state;
- retailer display metadata is joined by canonical retailer identity through a `Map`, not by array index or monetary ranking;
- duplicate/missing/mismatched retailer identities and contradictory optimizer structure fail closed before economics cards are rendered;
- Recipe and manual-list journeys remain available as regression-covered secondary flows;
- generated UUIDs, provider/acquisition/fulfillment details and internal optimizer authority are not exposed in ordinary user-facing output.

## Browser ownership boundary

M4.4.2 permits presentation-only operations such as enum-to-label mapping, identity lookup and `Intl.NumberFormat` formatting.

The browser does **not**:

- add merchandise subtotal and fees;
- evaluate minimum-order thresholds;
- infer eligibility or comparability;
- compare checkout totals;
- derive `NO_COMPARABLE_CANDIDATES / UNIQUE_WINNER / TIE`;
- derive optimal retailer IDs;
- sort retailer cards by monetary value, freshness or readiness;
- break ties by retailer order, ID or metadata.

M4.1 remains the checkout-arithmetic authority, M4.2 remains the eligibility/comparability authority and M4.3 remains the optimizer authority.

## TDD evidence

Recorded checkpoints:

- design: `8a69f801ef87b00e8aeeb3c2b8dc17019c2c6a8d`;
- implementation plan: `d25383ede7779c56b09d652aa674d8ce383397bb`;
- transport RED: `21763af8854125cdd2a48422f8c6c01e0bcd47d3`;
- transport GREEN: `2878bfca507dbeb7414023840d77a386937e9915`;
- result-projection RED: `f768a37736599314cc09b2a656a8b7e2d04fa37a`;
- result-projection GREEN: `f241f84b63f1371762c4532188d14659aeefb70f`;
- generated callback type correction: `fbeb7cfc0f378b4813cbb118661d6bd50b9395eb`;
- form RED: `75e9e7ab6b106d95ac040c5b6c75c05dd2a56cf1`;
- form GREEN: `5f8164054b2cfed2e324afa98c74fc45dd84d837`;
- legacy primary comparison-action alias removed: `5aff92ca8b41aab85119e8e3404f6ef802608ddb`;
- browser acceptance RED: `3730a696265a9f09cdf15e2dfecb7a6c810a1106`;
- deterministic M4.4.1 browser fixtures: `b84136cc591b55fa6ad9be71ba73d766d4951332`;
- M4 phase-copy RED: `6929dc37024fb0699345ea151656983f9ef81ef3`;
- M4 phase-copy GREEN: `29584a4e25c45b456880e61b2e1e27d55a29b497`;
- stale homepage smoke assertion corrected without production-code change: `ca2060546936f388556f62e49c6963d846274847`.

The deterministic E2E mock constructs test-only checkout evidence and makes no retailer/provider network request. Fixture arithmetic is not present in production browser code.

## Final feature gate

Final reviewed feature head:

`ca2060546936f388556f62e49c6963d846274847`

Evidence on that exact head:

- **9/9 normal PR workflow groups SUCCESS**;
- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Dependency Review — SUCCESS;
- Container Security CI — SUCCESS;
- Release Bundle CI — SUCCESS;
- CodeQL — SUCCESS;
- Web CI — SUCCESS;
- Web lint — SUCCESS;
- Web TypeScript typecheck — SUCCESS;
- component tests — SUCCESS;
- Next production build — SUCCESS;
- production-style Chromium Playwright / Web E2E — SUCCESS;
- 390px responsive no-overflow scenario — PASS;
- visible keyboard-focus scenario — PASS;
- Recipe/manual-list regression scenarios — PASS;
- read-only Change Review — **Looks good**;
- P0/P1/P2/P3 findings — none;
- nitpicks — none;
- unresolved review threads — 0.

## Merge acceptance

PR #146 was marked ready only after the exact-head gate and clean review, then squash-merged with expected-head protection.

Accepted implementation merge:

`7252b9264ed7a2ffe896b1a1fcddb09a78edc04c`

Post-merge evidence on that exact SHA:

- `main` points to the accepted merge;
- issue #145 is closed with state reason `completed`;
- exactly **8 normal push workflow groups** were created;
- **8/8 SUCCESS**;
- no failed, skipped or cancelled normal push gate remains.

## Non-goals preserved

M4.4.2 does not add:

- provider-specific checkout-economics acquisition;
- browser-side basket arithmetic or ranking;
- discounts, loyalty, subscriptions or tips;
- currency conversion;
- substitute/package recomputation;
- multi-store optimization;
- persistence, saved plans or Pantry history;
- live retailer traffic in deterministic acceptance.

## Next phase

The roadmap now advances to **M5 — Productization**. Its first implementation slice must be selected from repository/product evidence rather than invented as an arbitrary M4 continuation. M5 remains responsible for reliable repeat use, privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.
