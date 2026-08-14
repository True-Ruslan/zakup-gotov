# M2.4 Responsive Recipe UI — Acceptance Decision

Date: 2026-08-14  
Issue: #103  
Implementation PR: #104  
Accepted merge SHA: `aba20c9cee263a683c0d4383ad840d7415851861`  
Decision: **COMPLETE / ACCEPTED**

## Decision

M2.4 is accepted as the first real responsive Recipe-first product experience.

Accepted user journey:

`Recipe title/servings + ingredient editing + locality → POST /api/v1/recipe-comparison-previews → generated canonical shopping requirements → truthful retailer comparison`

The existing manual basket comparison remains available as a secondary path.

## Accepted behavior

- Recipe title, base servings, target servings and locality are editable in the primary homepage flow.
- The form supports 1..100 ingredient rows with add/remove behavior, explicit quantity and generated API unit vocabulary.
- Browser preflight rejects blank required fields, non-positive/non-integer serving counts and non-positive/non-finite ingredient quantities.
- The browser sends the generated M2.3 request contract and does not reimplement Recipe scaling, canonicalization, merge, identity, provenance, matching, basket or comparison semantics.
- Successful responses render generated canonical shopping requirements before the existing retailer comparison result projection.
- Transient Recipe/ingredient/ShoppingList/ShoppingItem UUIDs and provenance IDs remain hidden from user-facing output.
- `READY`, `UNCERTAIN`, `INCOMPLETE` and `UNAVAILABLE` retailer states remain truthful; no cheapest/best/recommended winner is fabricated.
- Missing configuration, timeout, network failure and unexpected service responses fail closed without fabricated shopping/comparison results.
- Known 400 problems expose only product-safe validation field/message information.
- Recipe ingredient row identities are deterministic local UI keys, avoiding SSR/hydration dependence on random UUID generation.
- Desktop and mobile layouts preserve visible keyboard focus and avoid horizontal overflow.
- Deterministic retailer evidence exists only in the Playwright mock API; production web code contains no fixture retailer data and ordinary browser acceptance performs no live retailer calls.

## TDD and verification evidence

Explicit RED checkpoints were retained for the transport, generated-shopping projection, Recipe form and homepage/browser integration.

Final reviewed implementation head:

`fb069d64b96f0d989951e67fd62b793277453024`

Exact-head PR evidence:

- all 9 normal PR workflow groups completed successfully;
- Web CI included lint, TypeScript, 30/30 unit/component tests, production build and responsive Playwright E2E;
- CodeQL Java and JavaScript/TypeScript succeeded;
- API, Contract, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle succeeded;
- read-only review verdict: **REVIEWED_READY / Looks good**;
- no unresolved P0/P1/P2 findings;
- review threads were empty.

PR #104 was marked ready and squash-merged using exact-head protection.

## Post-merge acceptance proof

Exact merged main SHA:

`aba20c9cee263a683c0d4383ad840d7415851861`

GitHub created exactly 8 normal push-triggered workflows for that SHA. Final result:

- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Web CI / responsive E2E — SUCCESS;
- CodeQL Java — SUCCESS;
- CodeQL JavaScript/TypeScript — SUCCESS;
- Container Security CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Release Bundle CI — SUCCESS.

At workflow-group level: **8/8 push workflows SUCCESS, 0 failures**.

Issue #103 is closed with state reason `completed`.

## Non-goals preserved

M2.4 does not add saved recipes, persistence/accounts, pantry state, weekly planning, multi-recipe aggregation, nutrition, arbitrary recipe import, fuzzy/synonym/semantic/AI ingredient interpretation, exact-address/store selection, retailer activation, provider/acquisition changes or cheapest-retailer recommendation.

## Next decision

M2 Recipe single-recipe flow is now complete from deterministic domain semantics through responsive product UI.

The next deterministic product/core target is **M2.5 — multi-recipe aggregation**, providing the merge/provenance foundation required by M3 Weekly Planning:

`several accepted Recipe conversions → one deterministic aggregated ShoppingList + per-recipe provenance`

Persistence and AI ingestion remain separate evidence-driven decisions and are not prerequisites for this aggregation slice.
