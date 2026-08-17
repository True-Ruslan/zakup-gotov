# Pre-RC3 documentation synchronization shipping evidence

Date: 2026-08-18

## Scope

Docs-only synchronization before the `v0.1.0-rc.3` release gate.

Changed documentation:

- `README.md`
- `CHANGELOG.md`
- `docs/PROJECT_STATE.md`
- `docs/ROADMAP.md`
- design and implementation plan for this synchronization

No runtime, API/OpenAPI, generated client, provider implementation, database schema, dependency or release-workflow behavior is changed.

## Pre-PR verification

Branch: `docs/sync-state-before-rc3`

Comparison against `main=e49c151fa44681dffe85fe90116009c86690672e` before PR creation:

- branch status: ahead, behind `0`;
- changed runtime/config/dependency paths: `0`;
- README reports `M5 — Productization` rather than obsolete M0;
- README no longer claims Shopping Core, Recipes, matching or basket optimization are unimplemented;
- #54/#153 is recorded as complete/accepted rather than outstanding;
- Chizhik Phase A/B/C/D1 implementation is recorded without claiming production activation;
- ordinary-user-browser `/api/v1/shops/` success and CI-hosted `page-unavailable` are kept as separate evidence classes;
- D2 remains unimplemented/unaccepted;
- `v0.1.0-rc.3` remains the immediate mainline release gate;
- M5.2 remains intentionally unselected.

## Acceptance gate

This documentation synchronization is not accepted until the exact PR head passes normal repository PR CI, the PR is squash-merged with expected-head protection, and post-merge normal `main` workflows are verified.
