# Magnit Public `shopCode` Resolution Implementation Plan

Updated: 2026-08-12
Issue: #69
Design: `docs/superpowers/specs/2026-08-12-magnit-shopcode-resolution-design.md`
Baseline: `main@25df1c018d30a9427231fd2f9f564fcb4b4ce1e4`
Branch: `feat/magnit-shopcode-resolution`

## Task 1 — RED: geographic/request contract

Create tests before production code for:

- valid `MagnitGeoPoint` and `MagnitGeoBoundingBox`;
- invalid finite/range/order cases;
- deterministic store-search request JSON;
- exact `typeName=box`;
- exact ordered store types `MM, GM, DG, MO, ME, MC, DARKSTORE, MM_MINI, ZARYAD`;
- no address/user identifiers in request model.

Run API verification and confirm RED is caused by missing new contract classes.

## Task 2 — GREEN: geographic/request primitives

Implement the smallest immutable provider-scoped types and Jackson request serialization data model. No HTTP/Spring client.

Run focused tests and full API verification.

## Task 3 — RED/GREEN: public response parsing

Define parser tests for exact proven shape:

`items.items[].externalId.storeCode + items.items[].coordinates.latitude/longitude`

Cover:

- one valid candidate;
- missing/malformed code/coordinates ignored;
- unrelated address/name metadata ignored;
- equivalent duplicates deduplicate;
- conflicting coordinates for one `shopCode` fail closed;
- invalid coordinates cannot create evidence;
- unknown fields ignored;
- malformed JSON cannot create candidates.

Implement parser with Jackson 3 tree model. No address fields are stored.

## Task 4 — RED/GREEN: resolution semantics

Test and implement deterministic order-independent resolution:

- 0 candidates → `NO_STORES`;
- 1 → `RESOLVED` carrying candidate;
- >1 → `AMBIGUOUS` without implicit ranking;
- conflicting parser evidence → `CONFLICTING_STORE_EVIDENCE`;
- non-`RESOLVED` results cannot expose a chosen candidate.

## Task 5 — RED/GREEN: provider-scoped fulfillment binding

Test and implement a narrow Magnit binding factory:

- reuse `sourceProviderId="magnit-public-page"`;
- chosen `shopCode` becomes `LocationContext.fulfillmentContextId`;
- caller locality is preserved;
- automatic unique resolution → `RESOLVED` selection mode;
- explicit manual candidate → `MANUAL` mode;
- unresolved/ambiguous/conflicting results cannot auto-bind;
- no mutation/extension of `ProductLocation`.

## Task 6 — durable evidence/docs

Record the finite #69 provenance:

- public `/shops` surface;
- stateless `POST /webgate/v1/stores-facade/search` contract;
- exact known bbox twice → stable `992301` without auth/app headers/cookie jar;
- Moscow/St Petersburg boxes → distinct large candidate sets, proving ambiguity is normal;
- text/address → coordinates not proven and therefore not implemented;
- #70 remains independent.

Update `PROJECT_STATE`, `ROADMAP`, and `CHANGELOG` to actual implementation proof level only.

## Task 7 — review / exact-head shipping

Run all branch-protection CI/security workflows on the exact candidate head.

Independent review must check:

- no private/session-bound assumptions;
- exact response identity path;
- conflict handling;
- coordinate validation;
- no nearest/first selection;
- no address retention/logging;
- existing provider ID reuse;
- no live network in ordinary CI or production runtime;
- #70 remains unresolved.

Fix P0/P1/P2 findings. Record non-blocking P3 follow-ups.

Add docs-only shipping evidence, rerun final exact-head gate, mark PR ready, squash merge using expected head SHA, then verify post-merge `main` workflows.

## Task 8 — merged-main guarded live acceptance

Only after the deterministic slice is merged and post-merge CI is green, run an explicit guarded live probe against merged `main`:

- same known public bbox twice;
- minimal headers only;
- no cookie jar;
- HTTP 2xx;
- stable same candidate set;
- expected `shopCode=992301`;
- sanitized output only.

If this merged-main gate passes, issue #69 may be marked accepted/closed for **location-to-provider-context technical resolution**. This does not resolve #70 or enable recurring product acquisition.