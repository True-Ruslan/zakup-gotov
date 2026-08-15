# M3.5.2 Pantry-aware WeeklyPlan Shopping Preview — Implementation Plan

## Scope

Deliver one new stateless API composition over accepted M3.2 + M3.5.1. Do not change M3.2/M3.3 runtime behavior, retailer comparison, persistence or web UI.

## Task 1 — RED service contract

Add tests in a new `weeklyplanpantrypreview` test package that require:

- accepted M3.2 projection as original authority;
- partial, full and unchanged Pantry outcomes;
- empty remaining list under full coverage;
- preservation of original item UUID/order/requirement/source provenance;
- exact one-to-one ordered Pantry evidence for every original item;
- fail-closed drift verification.

Run full `cd apps/api && ./mvnw verify` through CI and retain the failing exact SHA before production classes exist.

## Task 2 — GREEN application composition

Implement request/projection/service types under `weeklyplanpantrypreview`:

- wrapper request reusing `WeeklyPlanShoppingPreviewRequest`;
- request-scoped Pantry item/quantity input;
- response with weekly plan, original list, ordered evidence and dedicated zero-or-more remaining list;
- adapter from accepted M3.2 projection to neutral Shopping domain;
- exactly one call to `PantryShoppingListAdjuster`;
- projection/drift guards and provenance preservation.

Run full API verification.

## Task 3 — RED→GREEN HTTP contract

Add controller/error tests first, covering success, full coverage, invalid Pantry semantic input, nested invalid WeeklyPlan, malformed JSON, unknown fields and unsupported units.

Implement controller/configuration/sanitized problem boundary and pass full API verification.

## Task 4 — Contract synchronization

Update OpenAPI 3.1 with the new operation and schemas only. Regenerate/check the TypeScript client using repository-standard contract tooling. Add/adjust contract tests as required; generated output must be cleanly synchronized.

## Task 5 — Architecture/regression gate

Add ArchUnit guards proving the new package only composes accepted WeeklyPlan/Pantry/Shopping boundaries and cannot depend on comparison/retailer/provider/database. Preserve existing M3.2/M3.3 behavior and tests unchanged unless a pure test-only regression assertion is necessary.

## Task 6 — Shipping

Create shipping evidence, inspect changed-file scope, open/update draft PR, wait for exact final head 9/9 normal PR workflows, run a read-only review, resolve zero threads, mark ready, squash merge with expected-head protection, and require exact implementation merge 8/8 post-merge workflows before acceptance.

## Task 7 — Canonical acceptance docs

In a separate docs-only PR after implementation acceptance:

- add M3.5.2 acceptance evidence;
- update `docs/PROJECT_STATE.md`, `docs/ROADMAP.md` and root `CHANGELOG.md` as appropriate;
- advance next deterministic target to M3.5.3 Pantry-aware WeeklyPlan → Comparison composition;
- pass docs PR 9/9 and post-merge 8/8 gates.