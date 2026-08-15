# M3.5.4 Responsive Pantry Controls — Shipping Evidence

Date: 2026-08-15
Issue: #130
PR: #131
Baseline: `3e030b787ea0afd807f9a8ffcdb167ed46a491f7`

## Delivered scope

- accepted WeeklyPlan-first browser journey now uses only generated M3.5.3 `POST /api/v1/weekly-plan-pantry-comparison-previews` transport;
- Pantry is optional request-scoped browser form state with add/edit/remove controls and no persistence/history;
- browser submits `pantry: []` for the identity case and never serializes browser-local row keys;
- original weekly demand, Pantry adjustment evidence and remaining demand render directly from server response order/quantities;
- `COMPARED` renders the accepted retailer comparison only when its payload is present;
- `NO_REMAINING_DEMAND` renders a truthful terminal state with no fabricated retailer result;
- impossible `COMPARED` without comparison evidence fails closed;
- generated WeeklyPlan/Recipe/Shopping identities and provenance stay out of ordinary user-facing text;
- existing WeeklyPlan occurrence ordering/day/servings/Recipe editing is preserved;
- Recipe and manual-list browser journeys remain available and regression-covered;
- deterministic E2E fixture makes no live retailer request and contains no provider credentials/raw retailer payloads.

## TDD evidence

### Transport

RED: `0def1aa4bd26bae584f6e8bc5d4a17de554dda23`

- test required the generated M3.5.3 path and Pantry-aware request/response;
- lint/typecheck passed;
- component test failed because production still called the accepted M3.3 endpoint.

GREEN implementation: `1b2933927b90e030152e43631509b18f14033d17`

- server action switched to `WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH` only;
- finite 3-second timeout and fail-closed `ready | invalid | unavailable` behavior retained;
- generated product-safe validation errors preserved;
- no M3.3 fallback.

### Pantry form

RED: `939585d0896b7b697e0f98cf1da5dcf802d3c4b4`

Tests required:
- zero Pantry rows by default;
- `pantry: []` identity submit;
- add/edit/remove Pantry row;
- positive-quantity/incomplete-row preflight validation;
- browser-local row key absent from request.

GREEN: `833906ba8f72074b7e7110de240303292c25619b`

- added request-scoped Pantry controls using generated QuantityInputUnit vocabulary;
- browser performs only form-shape validation/trimming/number conversion;
- no Pantry merge, canonicalization, matching or subtraction is implemented in production browser code.

### Results

RED: `46d94c3f26284b639dc511d82c5c3f749eb2f36b`

Tests required server-owned:
- original weekly demand;
- Pantry audit evidence;
- remaining demand;
- `COMPARED` retailer comparison;
- `NO_REMAINING_DEMAND` terminal state;
- fail-closed missing comparison payload;
- hidden generated identities/provenance.

Initial GREEN: `b99ec30e8b6f4351d5dbf2f5bdcb644d4ea104fb`.

A subsequent accessibility test exposed an accessible-name mismatch caused by combining `aria-label` with `aria-labelledby`. The result sections were corrected to use their visible headings as accessible names, with explicit list labels. No product semantics changed.

### Homepage/browser

Browser RED: `b61d21fd678cc1e85eae757161eb153596713740`.

At this head:
- generated client build, lint, typecheck, Vitest and Next build were all SUCCESS;
- Web E2E alone failed because production had moved to M3.5.3 while the deterministic fixture server still exposed only the old M3.3 WeeklyPlan route.

Fixture/browser GREEN: `a20b585b15c22684279b21761f5d1af51f5e997b`.

Deterministic fixture now exposes only the M3.5.3 WeeklyPlan Pantry comparison route for the primary journey and implements test-only exact requirement + canonical-unit Pantry subtraction with ordered audit evidence. Recipe/manual fixtures remain unchanged.

Exact Web CI evidence on `a20b585b...`:

- Build shared API client — SUCCESS;
- Lint web — SUCCESS;
- Typecheck web — SUCCESS;
- Test web components — SUCCESS;
- Build web — SUCCESS;
- Web E2E / Run responsive browser tests — SUCCESS.

Browser acceptance covers:

- desktop WeeklyPlan → original shopping → partial Pantry audit → remaining demand → retailer comparison;
- full Pantry coverage → explicit `NO_REMAINING_DEMAND` with no retailer cards;
- explicit occurrence reorder semantics;
- 390×844 no horizontal overflow;
- keyboard-accessible Pantry controls and visible focus path;
- fail-closed unavailable WeeklyPlan service;
- Recipe journey regression;
- Recipe unavailable regression;
- manual-list comparison regression.

## Scope guard confirmed

Not added:

- Pantry persistence/history/accounts;
- `never buy` / omit-all exclusions;
- fuzzy/synonym/AI Pantry matching;
- browser-side Pantry canonicalization/subtraction;
- browser-side comparison/package/basket/winner recomputation;
- backend/API/provider/acquisition changes;
- live retailer traffic in ordinary CI/browser acceptance.

## Remaining acceptance gate

Before merge:

1. synchronize root CHANGELOG;
2. exact final PR head must have exactly 9 normal workflow groups SUCCESS with zero failure/skipped/cancelled;
3. independent read-only review must be clean with zero unresolved threads;
4. mark PR ready and squash merge with expected-head protection;
5. exact merge SHA must have exactly 8 normal post-merge push workflows SUCCESS;
6. canonical M3.5.4 acceptance/state/roadmap documentation follows in a separate docs-only PR.
