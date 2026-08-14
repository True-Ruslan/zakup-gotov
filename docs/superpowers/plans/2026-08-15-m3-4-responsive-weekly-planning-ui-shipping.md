# M3.4 Responsive Weekly Planning UI — Shipping Evidence

Date: 2026-08-15  
Issue: #118  
PR: #119  
Status: **IMPLEMENTED / TESTED / SHIPPING — acceptance pending**

Authoritative design: `docs/superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md`  
Implementation plan: `docs/superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md`

## Delivered scope

M3.4 makes the already accepted M3.3 composed boundary the primary browser journey:

`ordered weekly meal occurrences + locality → POST /api/v1/weekly-plan-comparison-previews → canonical weekly shopping → truthful retailer comparison`

Delivered behavior:

- the homepage advances to `M3 · Weekly Planning` and keeps Recipe and manual-list comparison as secondary flows;
- the planner edits explicit ordered meal occurrences without adding breakfast/lunch/dinner/snack taxonomy;
- users can add/remove occurrences within the accepted `1..35` bound and explicitly move an occurrence up/down without automatic day sorting;
- each occurrence edits accepted Monday..Sunday day metadata, positive target servings, Recipe title/base servings and explicit ingredients;
- nested ingredients retain the accepted Recipe input vocabulary and at least one ingredient per Recipe;
- all WeeklyPlan/occurrence/Recipe/ingredient/ShoppingList/ShoppingItem identities remain server-owned; browser-local numeric keys exist only for React list identity and never cross the request boundary;
- the transport consumes generated `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH` and generated OpenAPI component types rather than defining parallel frontend DTOs;
- browser preflight validation is limited to presentation-safe required/positive/integer checks; authoritative planner/Recipe/comparison semantics remain server-side;
- canonical weekly shopping requirements are rendered in server order before the existing `ComparisonPreviewResults` projection;
- the browser performs no serving scaling, cross-Recipe merge, canonicalization, matching, package arithmetic, basket total, winner or retailer-state recomputation;
- generated IDs and public planner provenance tuples remain hidden from ordinary user-facing output;
- missing API configuration, timeout, network/non-400/unexpected service failures fail closed as one unavailable state; generated 400 field/message evidence is projected without transport/provider internals;
- responsive and accessibility coverage includes 390 px mobile overflow, keyboard focus, occurrence-specific move/remove control names and pending submit protection;
- deterministic Playwright uses only the local mock API; no live retailer request, credential, cookie, token, precise address or provider identifier is introduced;
- persistence/history, pantry/exclusions, nutrition, calendar/time-zone semantics, fuzzy/AI equivalence, retailer onboarding and production-access policy remain out of scope.

## Explicit TDD evidence

### M3.3 transport

RED `8f6cce844be477fca5cc5777c552ce1bd6479e59`:

- `weekly-plan-comparison.test.ts` was committed before the production transport module;
- tests require fail-closed missing configuration, typed successful composed response, sanitized generated 400 errors and bounded request abort;
- the exact RED head triggered the normal PR workflow set before `weekly-plan-comparison.ts` existed.

GREEN `ec0f768c49bdcdd75f4924e5bbfbf7abfbcbec50`:

- added the minimal server transport over generated `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH`;
- uses a 3 second `AbortController` boundary and returns only `ready / invalid / unavailable` presentation states;
- generated M3.3/M3.2/Recipe/Comparison validation fields/messages are retained while all other failures fail closed.

### Canonical weekly-shopping result projection

RED `afdd80457ccb25234023d7e34ee024689cf7580a`:

- tests required canonical weekly shopping output before retailer comparison and prohibited generated UUID/provenance text.

GREEN `55bfacc885c8c80defcbc5f105c67d04461aeaa2`:

- added `WeeklyPlanComparisonResults`;
- reads `weeklyPlanShoppingPreview.shoppingList.items` directly in accepted server order;
- reuses existing `ComparisonPreviewResults` unchanged after canonical shopping output.

### Planner editor

RED `b0a2cd9afba1eb0e93a52409afd21b9042bd79a6`:

- tests required safe one-occurrence/one-ingredient defaults, add/reorder behavior independent from day metadata, exact generated M3.3 request projection, preflight rejection and unavailable fail-closed behavior.

GREEN `507d596d0451849ef6a8d0948c79988be463ca29`:

- added the responsive nested WeeklyPlan editor;
- explicit array order is authoritative and move operations swap only adjacent occurrence positions;
- add/remove limits mirror accepted API cardinality without synthesizing domain identities;
- only generated day/unit vocabulary is submitted;
- successful response replaces result state atomically and failures never fabricate prior/new comparison output.

### Homepage integration

RED `45e85d6746bb71d330d908305bb3b941728d0034`:

- homepage test required Weekly Planning to become the first product journey while preserving Recipe and manual-list flows.

GREEN `3537a724d1705b7653c617fe3f7c629b8e8162b0`:

- homepage advances from M2 Recipe-first positioning to M3 Weekly Planning;
- Recipe and manual comparison remain rendered below as explicit secondary paths.

### Deterministic browser acceptance

RED `da51ab446218a7349fca36a9f879b69d3270812f`:

- Playwright coverage was updated first to require desktop WeeklyPlan → weekly shopping → retailer comparison, explicit reorder behavior, 390 px no-overflow, unavailable fail-closed state, keyboard focus and preserved Recipe/manual regressions before the deterministic weekly endpoint existed.

GREEN `b690a9b9e21fde748c5102e5d57e1977cca3f6ff`:

- deterministic `/api/v1/weekly-plan-comparison-previews` fixture was added without live traffic;
- fixture creates server-owned test identities, applies deterministic serving scale/canonical-unit grouping for browser-contract verification and projects the existing eight-retailer comparison fixture;
- exact-head Web CI passed API-client build, lint, TypeScript typecheck, Vitest/component tests and production Next.js build;
- exact-head Web E2E passed production build, Chromium installation and the full responsive Playwright suite, including the new M3.4 and retained M2/M1 journeys.

## Architecture / privacy / network scope

M3.4 changes only browser/application presentation code and deterministic test support plus design/shipping documentation. It adds no database migration, persistence, authentication/authorization behavior, provider adapter, retailer activation, production-access rule or backend M3.3 domain/application behavior.

The production web transport addresses only the accepted generated M3.3 application path. Runtime provider/acquisition identifiers, store bindings and precise addresses are neither requested nor rendered. Normal browser acceptance remains completely retailer-network-free.

## Current shipping checkpoint

Functional implementation and deterministic browser fixtures are frozen at GREEN head `b690a9b9e21fde748c5102e5d57e1977cca3f6ff`.

The shipping-evidence commit creates a new exact PR head. M3.4 remains **acceptance pending** until all of the following are proven:

1. 9/9 normal PR workflow groups SUCCESS on one exact final head;
2. read-only review reports no unresolved P0/P1/P2/P3 findings and review threads are clear;
3. PR #119 is marked ready and squash-merged with expected-head protection;
4. issue #118 closes completed;
5. exactly 8 normal push workflows succeed on the implementation merge SHA;
6. canonical PROJECT_STATE/ROADMAP/CHANGELOG and dedicated M3.4 acceptance evidence are synchronized in a docs-only acceptance PR;
7. the final canonical docs merge SHA also passes the normal post-merge push workflow set.
