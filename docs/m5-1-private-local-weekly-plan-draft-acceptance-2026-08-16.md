# M5.1 Private Local WeeklyPlan Draft — Acceptance

**Date:** 2026-08-16  
**Status:** COMPLETE / ACCEPTED  
**Issue:** #148  
**Implementation PR:** #149  
**Baseline:** accepted M4.4.2 + pre-release web runtime hardening #150  
**Final reviewed feature head:** `6c54479044e41e5177739b57eb891830a79691f8`  
**Accepted implementation merge:** `2f2b96d18521b8bb04f6ee17182d61711322de08`

## Decision

M5.1 is accepted. The primary WeeklyPlan/Pantry browser journey now has exactly one private, versioned, same-origin local input draft that improves repeat use without introducing accounts, server persistence, hidden synchronization or client-owned comparison semantics.

This is deliberately an input-draft capability, not saved-plan history. Accepted M4.4.2 remains authoritative for comparison, checkout economics and optimizer results.

## Authoritative inputs

- Design: [`superpowers/specs/2026-08-16-m5-1-private-local-weekly-plan-draft-design.md`](superpowers/specs/2026-08-16-m5-1-private-local-weekly-plan-draft-design.md)
- Implementation plan: [`superpowers/plans/2026-08-16-m5-1-private-local-weekly-plan-draft.md`](superpowers/plans/2026-08-16-m5-1-private-local-weekly-plan-draft.md)
- Accepted primary comparison boundary: M4.4.1 `POST /api/v1/weekly-plan-pantry-optimization-previews`

## Accepted persistence boundary

The browser owns one storage key:

`zakup-gotov.weekly-plan-draft.v1`

Only editable semantic input is persisted:

- locality;
- ordered WeeklyPlan occurrences;
- day metadata;
- target servings;
- Recipe title and base servings;
- ordered Recipe ingredients with requirement, editable amount string and unit;
- ordered Pantry rows with requirement, editable amount string and unit.

The draft does **not** persist:

- React/presentation row keys;
- generated WeeklyPlan, Recipe, ingredient, ShoppingList or ShoppingItem identities;
- comparison results;
- checkout economics;
- optimizer outcomes or optimal retailer IDs;
- provider/acquisition/fulfillment identifiers;
- cookies, authorization material or browser-session secrets;
- server error payloads.

Numeric form values remain strings in the draft so unfinished user input round-trips without coercion.

## Accepted browser behavior

- restore occurs only after client mount; SSR never reads browser storage;
- autosave is gated until the initial read/restore attempt settles;
- writes are debounced and compare semantic serialized state against the last confirmed persisted baseline;
- restore does not trigger comparison automatically;
- no POST is emitted before an explicit user comparison action;
- explicit reorder order is preserved across reload independently from day metadata;
- Pantry rows restore with the same semantic values and order;
- malformed or unsupported drafts are discarded fail-closed when possible;
- `localStorage.getItem`, `setItem` and `removeItem` failures leave the form usable and surface a product-safe local-saving-unavailable state;
- failed initial read does not cause a blank form to overwrite unknown stored data;
- corrupt-draft cleanup failure is treated as storage unavailable rather than reported as successful cleanup;
- clear is disabled until restore readiness and while comparison is pending, preventing delayed restore from resurrecting a deleted draft;
- `Очистить форму и локальный черновик` resets editable input, derived result/error state and the local draft without making an API request;
- a reload after clear remains blank and the storage key remains absent.

## Privacy / ownership boundary

The healthy browser copy states that the draft stays only in the current browser and is not synchronized with an account or server. Storage failure copy explicitly states that local saving is unavailable while keeping editing/submission usable.

M5.1 does not add telemetry, account identity, cloud synchronization, server-side saved-plan history or provider data persistence.

## TDD evidence

Representative RED → GREEN checkpoints include:

- versioned semantic draft codec/storage contract → local storage adapter;
- restore/autosave contract → post-mount restore plus restore-ready debounced persistence;
- explicit clear/failure recovery contract → local reset and product-safe storage-unavailable behavior;
- failed-read overwrite RED `e0464b1d792507230eea879f0722e04ad8241a9f` → semantic persisted baseline `3449cccbbe2fd0668a0091450553569981a9e6de`;
- clear-before-restore race RED `0752d176dc35bce0530fc7f4b8fd3390df03ce49` → restore-ready clear gate `951721f1711c9c24dfa85eec5468b92b4bf0fc84`;
- corrupt-draft cleanup failure RED `86b9cc4e99a15e413a6c821cb0fe7ce7fd75476f` → fail-closed remove handling `6c54479044e41e5177739b57eb891830a79691f8`.

## Browser acceptance

Deterministic Playwright proves on a production build that:

- the exact semantic draft payload is stored under the single versioned key;
- presentation keys and comparison/optimizer/provider evidence are absent from persisted JSON;
- the browser emits zero comparison POST requests before explicit submit, including after reload/restore;
- locality, reordered WeeklyPlan occurrences, Recipe fields and Pantry rows restore correctly;
- derived comparison/checkout results do not restore from local storage;
- explicit submit still runs the accepted server-owned M4.4.2 journey;
- explicit clear removes the local key and visible derived state;
- a second reload remains blank with the local key absent.

Acceptance remains deterministic and makes no live retailer/provider request.

## Final feature gate

Final reviewed feature head:

`6c54479044e41e5177739b57eb891830a79691f8`

Evidence on that exact head:

- **9/9 PR workflow groups SUCCESS**;
- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Web CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Release Bundle CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Dependency Review — SUCCESS;
- Container Security CI — SUCCESS;
- CodeQL — SUCCESS;
- Web lint/typecheck/component tests/Next production build — SUCCESS;
- Chromium Playwright Web E2E — SUCCESS and actually executed;
- local-draft reload/restore/clear browser acceptance — PASS;
- read-only final review — no P0/P1/P2/P3 findings or nitpicks;
- unresolved review threads — 0.

## Merge acceptance

PR #149 was moved out of draft only after the exact-head gate and clean review, then squash-merged with expected-head protection.

Accepted implementation merge:

`2f2b96d18521b8bb04f6ee17182d61711322de08`

Post-merge evidence on that exact SHA:

- issue #148 is closed with state reason `completed`;
- exactly **8 normal push workflow groups** were created on `main`;
- **8/8 SUCCESS**;
- CodeQL Java and JavaScript/TypeScript both completed successfully;
- **0 failed normal push workflows**.

## Related pre-release hardening accepted before M5.1

Manual source-checkout testing exposed two independent web-runtime defects before M5.1 acceptance. PR #150 fixed them and was independently accepted on `main`:

- clean-checkout `web dev` now builds `@zakup-gotov/api-client` before Next.js starts;
- manual-list SSR/hydration uses deterministic presentation identity and generates request UUIDs only on submit.

Accepted hardening merge: `ef366c4ea65169dc3839cbf78c1df25d16f1dffa`, with 8/8 post-merge push workflows successful.

## Non-goals preserved

M5.1 does not add:

- accounts or authentication;
- server-side saved plans/history;
- cloud/local cross-device synchronization;
- local persistence of retailer or optimizer results;
- telemetry/analytics;
- feature flags;
- provider health monitoring;
- production retailer activation;
- live retailer traffic in deterministic acceptance.

## Next operational target

The next highest-value validation is **`v0.1.0-rc.3`** from a documentation-synchronized, verified `main` commit.

The release candidate must complete the already-defined release contract end to end: Release / Verify, multi-platform staging publication, fail-closed vulnerability/SBOM gates, staging exact-digest Compose smoke, digest-identical final-package promotion, final exact-digest smoke, provenance attestations, prerelease SemVer promotion, final manifest checks and attached release evidence while leaving `latest` untouched.

A stable `v0.1.0` remains blocked until at least one prerelease completes the full release workflow and its evidence is inspected.