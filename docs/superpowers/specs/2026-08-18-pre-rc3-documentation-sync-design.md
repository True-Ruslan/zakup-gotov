# Pre-RC3 documentation synchronization design

Date: 2026-08-18

## Context

Canonical product documentation was last synchronized after M5.1 acceptance on 2026-08-16. Since then `main` has advanced through retailer-bridge lifecycle hardening and Chizhik acquisition investigation through the merged Phase D1 foundation. The release roadmap requires `v0.1.0-rc.3` to be cut from documentation-synchronized verified `main`, so documentation drift is now a release blocker.

## Goal

Synchronize the public project narrative with the verified repository state without changing runtime behavior and without overstating live retailer readiness.

## Approaches considered

1. **Minimal state-only patch** — update only `PROJECT_STATE.md` and `ROADMAP.md`. Lowest diff, but leaves the public README and changelog materially misleading.
2. **Canonical four-file synchronization** — update `PROJECT_STATE.md`, `ROADMAP.md`, root `CHANGELOG.md`, and `README.md`. This keeps the release gate, public project status, and historical record aligned. **Chosen.**
3. **Broad documentation rewrite** — audit and rewrite all integration docs at once. More comprehensive, but unnecessarily expands scope before RC3.

## Scope

### `docs/PROJECT_STATE.md`

- advance the update date to 2026-08-18;
- retain M5 / `v0.1.0-rc.3` as the deterministic product/release target;
- mark browser-bridge persistent-session / SPA / store-change hardening (#54/#153) complete;
- record Chizhik Phase A, B, C, and merged D1 implementation evidence;
- record the accepted ordinary-user-browser `/api/v1/shops/` field canary;
- explicitly keep Chizhik production offer acquisition and D2 unaccepted;
- record the current CI-hosted Phase D `page-unavailable` live outcome as negative evidence, not as ordinary CI failure;
- keep production/right-to-operate approval separate from technical feasibility.

### `docs/ROADMAP.md`

- remove #54 from outstanding parallel work and record it as completed hardening;
- describe Chizhik as an active connectivity track with D1 implementation merged but live transport gate unresolved;
- keep D2 as the next Chizhik evidence slice only after D1 transport disposition;
- preserve `v0.1.0-rc.3` as the next mainline release validation and keep M5.2 intentionally unselected.

### `CHANGELOG.md`

- add Unreleased entries for retailer-bridge lifecycle hardening;
- add Chizhik Phase A/B/C/D1 work and its safety boundaries;
- distinguish merged implementation from unresolved live acquisition evidence.

### `README.md`

- replace obsolete M0 status with current M5 Productization / pre-release status;
- replace the stale “not implemented” section with the accepted M1–M5.1 capability summary;
- describe retailer connectivity truthfully: core product semantics are implemented, but production acquisition coverage remains incomplete and retailer-specific;
- keep `v0.1.0-rc.3` as the immediate release target.

## Invariants

- No runtime, API, OpenAPI, generated client, provider behavior, database schema, release workflow, or dependency changes.
- No claim that Chizhik D1 is accepted as a production acquisition path while the CI-hosted live probe remains `page-unavailable`.
- No claim that technical accessibility grants legal or operational permission.
- No hidden live retailer traffic is introduced into ordinary CI.
- Existing accepted M1–M5.1 semantics are not redefined.

## Verification

- review the final diff for contradictions between all four documents;
- ensure references to #54 as outstanding work are removed;
- ensure README no longer claims M1–M4 are unimplemented;
- run repository PR CI through the normal GitHub workflow after opening the docs-only PR;
- merge only after the exact PR head is green and the branch is current with `main`.

## Non-goals

- creating `v0.1.0-rc.3` in this patch;
- selecting M5.2;
- implementing Chizhik D2;
- merging dependency-update PRs;
- changing retailer production-access policy.
