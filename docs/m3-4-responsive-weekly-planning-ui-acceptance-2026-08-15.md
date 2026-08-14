# M3.4 Responsive Weekly Planning UI — Acceptance

Date: 2026-08-15  
Status: **COMPLETE / ACCEPTED**  
Issue: #118  
Implementation PR: #119

Baseline before M3.4: `4375c8433e42671356a18dee6a29d2bb7dd82f95`  
Final reviewed feature head: `12973650f274f76ec54865be41963843afcb4558`  
Accepted implementation squash merge: `1201030aed45075c676f796920b6268cdcf8e036`

Authoritative design: [`superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md`](superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md`](superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui-shipping.md`](superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui-shipping.md)

## Accepted product boundary

The primary M3 browser journey now consumes only the already accepted composed boundary:

`ordered weekly meal occurrences + locality → POST /api/v1/weekly-plan-comparison-previews → canonical weekly shopping requirements → truthful retailer comparison`

The browser does not compose M3.2 and Comparison Preview independently and does not implement a second WeeklyPlan, Recipe, Shopping or Comparison algorithm.

## Accepted behavior

- the homepage primary journey is `M3 · Weekly Planning` while the accepted Recipe and manual-list comparison journeys remain available as explicit secondary paths;
- the planner starts with one meal occurrence and one ingredient and supports `1..35` ordered occurrences, matching the accepted M3.2/M3.3 request cardinality;
- users may add/remove meal occurrences and explicitly move them up/down; caller order remains independent from Monday-through-Sunday day metadata and the browser never auto-sorts by day;
- each occurrence edits accepted day metadata, positive target servings, Recipe title/base servings and one or more explicit ingredients without adding breakfast/lunch/dinner/snack taxonomy;
- generated `WeeklyPlanComparisonPreviewRequest` / response / day / quantity-unit vocabulary is authoritative; no parallel browser DTO contract is introduced;
- browser-local numeric row keys exist only for React list identity and never become WeeklyPlan, occurrence, Recipe, ingredient, ShoppingList or ShoppingItem identity;
- server-owned generated identities remain hidden from ordinary editable and result presentation;
- presentation preflight checks only required text, positive finite quantities and positive integer serving fields; authoritative semantic validation remains on accepted server boundaries;
- the production browser/server transport uses generated `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH`, a bounded three-second request timeout and fail-closed `ready / invalid / unavailable` presentation states;
- generated product-safe 400 validation field/message evidence is preserved; missing configuration, timeout, network, non-400 and unexpected failures never fabricate a result;
- canonical weekly shopping requirements are rendered directly from `weeklyPlanShoppingPreview.shoppingList.items`, in server order, before retailer comparison;
- the existing accepted `ComparisonPreviewResults` projection is reused unchanged after weekly shopping output;
- the browser does not recalculate serving scale, cross-Recipe merge, canonical quantities, product matching, package counts, basket totals, winner ranking or retailer complete/uncertain/incomplete/unavailable states;
- generated UUIDs and occurrence/Recipe/ingredient provenance tuples are not exposed in normal user-facing result text;
- deterministic browser acceptance covers desktop critical flow, explicit reorder behavior, mobile 390px no-overflow, visible keyboard focus, fail-closed unavailable behavior and preserved Recipe/manual-list critical journeys;
- ordinary Playwright acceptance uses only the deterministic local mock API and makes no live retailer request;
- M3.4 introduces no persistence/history, pantry/exclusions, nutrition, calendar/time-zone semantics, fuzzy/AI equivalence, retailer onboarding, provider activation, production-access policy change, database migration or backend M3.3 semantic change.

## TDD evidence

Accepted RED → GREEN checkpoints:

- transport: `8f6cce844be477fca5cc5777c552ce1bd6479e59` → `ec0f768c49bdcdd75f4924e5bbfbf7abfbcbec50`;
- canonical weekly-shopping projection: `afdd80457ccb25234023d7e34ee024689cf7580a` → `55bfacc885c8c80defcbc5f105c67d04461aeaa2`;
- planner form/editor: `b0a2cd9afba1eb0e93a52409afd21b9042bd79a6` → `507d596d0451849ef6a8d0948c79988be463ca29`;
- WeeklyPlan-first homepage integration: `45e85d6746bb71d330d908305bb3b941728d0034` → `3537a724d1705b7653c617fe3f7c629b8e8162b0`;
- deterministic browser acceptance: `da51ab446218a7349fca36a9f879b69d3270812f` → `b690a9b9e21fde748c5102e5d57e1977cca3f6ff`.

The final shipping-evidence head is `12973650f274f76ec54865be41963843afcb4558`.

## Final PR acceptance proof

On exact reviewed PR #119 head `12973650f274f76ec54865be41963843afcb4558`, all **9/9 normal PR workflow groups succeeded**:

1. API CI — SUCCESS;
2. Contract CI — SUCCESS;
3. Web CI + responsive E2E — SUCCESS;
4. CodeQL Java + JavaScript/TypeScript — SUCCESS;
5. Dependency Review — SUCCESS;
6. Container Security API + Web — SUCCESS;
7. Retailer Bridge CI — SUCCESS;
8. Release Contract CI — SUCCESS;
9. Release Bundle CI — SUCCESS.

Web verification on the exact head included:

- generated/shared API-client build — SUCCESS;
- web lint — SUCCESS;
- TypeScript typecheck — SUCCESS;
- Vitest/component tests — SUCCESS;
- production Next.js build — SUCCESS;
- production browser build + Chromium responsive Playwright suite — SUCCESS.

Read-only review on the same exact head:

- verdict: **Looks good**;
- P0: none;
- P1: none;
- P2: none;
- P3: none;
- nitpicks: none;
- unresolved review threads: **0**;
- review changed no repository files.

## Merge and post-merge acceptance proof

PR #119 was marked ready only after the exact-head workflow/review gate and was squash-merged with expected-head protection.

Accepted implementation merge:

`1201030aed45075c676f796920b6268cdcf8e036`

Post-merge evidence on that exact `main` SHA:

- exactly **8 normal push workflows** were created;
- **8/8 SUCCESS, 0 failures**;
- Web CI and responsive Web E2E both succeeded;
- CodeQL Java and JavaScript/TypeScript both succeeded;
- issue #118 is closed with state reason `completed`.

Therefore M3.4 implementation is accepted as:

**implemented → tested → reviewed → merged → accepted**.

## Non-goals preserved

M3.4 does not add:

- pantry or exclusion subtraction;
- persistence, saved weekly plans or history;
- exact dates, week numbers, calendar or time-zone semantics;
- fixed meal-slot taxonomy;
- nutrition/macros;
- fuzzy/synonym/AI ingredient equivalence;
- browser-side Shopping/Comparison optimization;
- retailer/provider onboarding or activation;
- production-access policy changes;
- precise-address persistence;
- database changes.

## Decision

**M3.4 Responsive Weekly Planning UI is COMPLETE / ACCEPTED.**

The base Weekly Planning product flow is now complete from domain through responsive browser composition. The next deterministic product slice is **M3.5 — Pantry / exclusions semantics**.

M3.5 must be designed as an explicit subtraction/provenance layer and must not silently mutate the accepted M2.5/M3.1/M3.2/M3.3/M3.4 behavior. Persistence remains deferred until saved-plan reuse/history demonstrates product value.

Canonical documentation synchronization is performed by the docs-only acceptance PR containing this document; its own exact-head review/CI and post-merge push proof are verified separately from implementation acceptance.
