# M2.4 Responsive Recipe UI — Shipping Evidence

Date: 2026-08-14  
Issue: #103  
PR: #104  
Status: **IMPLEMENTED / TESTED / SHIPPING — acceptance pending**

Authoritative design: `docs/superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md`  
Implementation plan: `docs/superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md`

## Delivered scope

M2.4 adds the first real Recipe-first browser journey over the accepted M2.3 API:

`Recipe title/servings + ingredient editing + locality → POST /api/v1/recipe-comparison-previews → generated shopping requirements → existing truthful retailer comparison renderer`

Delivered behavior:

- generated-client-only server action for `RECIPE_COMPARISON_PREVIEWS_PATH`;
- finite request timeout and fail-closed unavailable state;
- product-safe generated 400 validation messages without problem metadata/internal leakage;
- responsive Recipe form with title, base/target servings, locality and 1..100 editable ingredient rows;
- quantity/unit editing over generated `QuantityInputUnit`;
- client preflight for blank required values, positive integer servings and positive finite quantities;
- no browser-side Recipe scaling, unit canonicalization, merge, identity, provenance or comparison-domain implementation;
- generated canonical shopping requirements rendered before retailer comparison;
- transient Recipe/ingredient/list/item UUIDs remain hidden from user-facing output;
- existing `ComparisonPreviewResults` reused unchanged, preserving READY/UNCERTAIN/INCOMPLETE/UNAVAILABLE semantics and no invented winner;
- Recipe-first homepage positioning with manual basket comparison retained as a secondary path;
- deterministic E2E-only M2.3 mock endpoint; production code contains no fixture retailer data;
- desktop/mobile Recipe journey, unavailable state, keyboard focus and manual regression coverage.

## Explicit TDD evidence

### Server transport

RED head `69a209a80054b1f8f545b3ed68d395bd157336b8`:

- Web CI reached TypeScript compilation;
- expected failure: `TS2307 Cannot find module './recipe-comparison'` from the new transport contract test.

GREEN sequence:

- production server action added;
- generated 400 union typing tightened after TypeScript caught an implicit-any projection;
- head `71539f3c4a8c930e32c4aa6b5678da8c7fc5bf7e` passed lint, typecheck, component tests and production build.

### Generated shopping result projection

RED head `b455e030ce4873528f2e5f454e6bca982466c472`:

- expected failure because `recipe-comparison-results` did not exist.

GREEN sequence:

- canonical shopping item projection added;
- generated item type made explicit after TypeScript rejected an implicit-any map parameter;
- head `51664143f46198284db6c17cbb6a3fa93ac606f7` passed lint, typecheck, component tests and production build.

### Recipe form

RED head `7cce42123ff3a6f17345d163c90bb292f107c857`:

- Web CI lint succeeded;
- expected typecheck failure: `TS2307 Cannot find module './recipe-comparison-form'`.

GREEN head `da628eef9694c0ce4cfff9e9dd1b763cfe253162`:

- lint PASS;
- typecheck PASS;
- all component tests PASS;
- production build PASS.

Covered form behavior includes sensible serving defaults, one-row floor, add/remove editing, exact generated M2.3 request shape, serving/quantity/unit changes, client validation, generated backend validation and unavailable state.

### Homepage + browser journey

RED head `1b20fbf8feaef621da016e68597f26328d3bec1b`:

- lint PASS;
- typecheck PASS;
- 29 existing/new component tests PASS;
- only the new homepage test failed because production still rendered `M1 · Shopping Core` and no Recipe-first form.

GREEN implementation checkpoint `dab2c9f8c32ef89684d0fdc326b100f033f78117`:

- Web lint PASS;
- Web typecheck PASS;
- **30/30 component/unit tests PASS**;
- production Next.js build PASS;
- **Playwright responsive browser suite PASS**, covering:
  - desktop `Блины`, base 2 → target 4, `0.5 LITER` milk + `5 PIECE` eggs;
  - generated `Молоко 1000 MILLILITER` and `Яйца 10 PIECE`;
  - eight truthful retailer results;
  - mobile viewport with no horizontal overflow;
  - fail-closed unavailable API state with one accessible alert and no fabricated results;
  - visible keyboard focus path;
  - unchanged manual basket comparison journey.

## Repository-wide CI checkpoint

On implementation checkpoint `dab2c9f8c32ef89684d0fdc326b100f033f78117`:

- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Web CI + E2E — SUCCESS;
- CodeQL — SUCCESS;
- Dependency Review — SUCCESS;
- Container Security CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Release Bundle CI was still running when this shipping record was authored.

The final PR head after this documentation commit must re-run all normal PR workflow groups. This checkpoint is evidence of implementation correctness, not final acceptance evidence.

## Security/privacy and architecture review points

- browser code imports generated OpenAPI types/path; no duplicate backend request/response DTO is introduced;
- Recipe business semantics remain server-owned by accepted M2.1–M2.3 boundaries;
- no exact address, account/session persistence, localStorage, provider identifier, SKU, acquisition mode, fulfillment context or retailer credential is introduced;
- deterministic retailer data lives only in `apps/web/e2e/mock-api.mjs`;
- service failures never fabricate shopping requirements or retailer results;
- no cheapest/best/recommended retailer claim is introduced.

## Remaining acceptance gates

M2.4 is **not ACCEPTED** until all of the following hold on the final reviewed PR head:

1. all 9 normal PR workflow groups succeed on the exact same head;
2. independent read-only review reports no unresolved P0/P1/P2 finding;
3. PR #104 is marked ready and squash-merged with expected-head protection;
4. all normal push-triggered workflows on the resulting `main` SHA succeed;
5. only then may #103 and canonical state/roadmap/changelog be marked COMPLETE / ACCEPTED.
