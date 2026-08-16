# M5.1 Private Local WeeklyPlan Draft — Acceptance

**Date:** 2026-08-16  
**Status:** COMPLETE / ACCEPTED  
**Issue:** #148  
**Implementation PR:** #149  
**Accepted implementation merge:** `2f2b96d18521b8bb04f6ee17182d61711322de08`

## Decision

M5.1 is accepted. The primary WeeklyPlan/Pantry browser journey now preserves one current editable draft across reloads using a versioned same-origin browser-local contract, without adding accounts, server-side user persistence, cloud sync, authentication, database schema or a new retailer/provider trust boundary.

This is product convenience state only. Accepted M4.4.2 submit behavior and all server-owned Shopping, Recipe, WeeklyPlan, Pantry, comparison, checkout-economics and optimizer semantics remain unchanged.

## Authoritative inputs

- Design: [`superpowers/specs/2026-08-16-m5-1-private-local-weekly-plan-draft-design.md`](superpowers/specs/2026-08-16-m5-1-private-local-weekly-plan-draft-design.md)
- Implementation plan: [`superpowers/plans/2026-08-16-m5-1-private-local-weekly-plan-draft.md`](superpowers/plans/2026-08-16-m5-1-private-local-weekly-plan-draft.md)
- Storage key: `zakup-gotov.weekly-plan-draft.v1`

## Accepted local-draft contract

Persisted V1 state contains only user-authored editable form values:

- locality text;
- ordered WeeklyPlan occurrences;
- day, target-servings text, Recipe title/base-servings text;
- ordered ingredient requirement/amount/unit values;
- ordered Pantry requirement/amount/unit values.

Numeric edit fields remain strings so unfinished browser input is not silently converted into domain numbers during persistence.

The persisted representation never carries:

- React/presentation row keys;
- generated WeeklyPlan/occurrence/Recipe/ingredient/ShoppingList/ShoppingItem identities;
- Pantry adjustment evidence;
- retailer comparison, checkout-economics or optimizer results;
- API/client errors or pending state;
- provider/acquisition/fulfillment identities;
- credentials, cookies or tokens.

Decoding creates a fresh allow-listed object, so unknown/tampered members cannot leak through a round-trip.

## Restore and failure semantics

Accepted behavior:

- server-rendered initial UI remains the existing blank form;
- storage is read only after client mount;
- supported structurally valid V1 drafts restore in the stored order;
- presentation-only keys are reconstructed locally from restored array order;
- restore never submits a comparison;
- unchanged restored state is not written back merely because it was restored;
- malformed/unsupported drafts fail closed and are removed when possible;
- corrupt draft + successful cleanup becomes a blank ready state;
- corrupt draft + failed cleanup becomes explicit local-storage unavailable state;
- a failed `getItem()` does not trigger a blind blank-default write that could overwrite unknown unread storage;
- later real user edits may attempt the next ordinary autosave;
- storage exceptions never escape into form rendering/submission.

## Autosave and clear semantics

Autosave:

- uses a bounded 300 ms write-coalescing delay, not polling;
- starts only after initial restore readiness;
- writes only when semantic editable V1 JSON differs from the last read/successfully saved state;
- successful saves advance the last-persisted reference;
- failed saves do not pretend persistence succeeded.

Explicit clear:

`Очистить форму и локальный черновик`

- is disabled until initial restore completes;
- is disabled while a comparison request is pending;
- the handler also fails closed when called before restore readiness or during pending state;
- removes the storage key;
- resets locality, occurrences and Pantry to the existing blank initial form;
- clears prior comparison result and client/API error presentation;
- never invokes comparison;
- prevents the blank reset itself from recreating the just-cleared draft.

## Privacy UX

Normal copy:

> Черновик сохраняется только в этом браузере и не синхронизируется с аккаунтом или сервером.

Unavailable-storage copy:

> Локальное сохранение недоступно. Форма работает, но изменения могут потеряться после закрытия страницы.

The product does not claim local storage is encrypted or a credential vault.

## TDD and hardening evidence

### Draft codec/storage boundary

RED:

`b368bba43833f9010ba8d3c1684a857ec5f32672`

Web CI failed because `./weekly-plan-draft` did not yet exist (`TS2307`).

GREEN:

`ebda52da46a7f59920b6ca58be7af286c02b964c`

Added versioned codec and safe `Storage` read/write/remove boundary.

### Form restore/autosave

RED:

`76255cf4e244eaa1c264ee611b44805986cd0cff`

Existing tests remained green while exactly the new restore/autosave expectations failed: locality stayed blank and no draft was stored.

Initial GREEN implementation:

`330e3f8484a743e5dda626f768fa0cef32874a1a`

React 19 lint then correctly rejected synchronous state changes in the mount effect.

Lint-safe GREEN:

`dcf29e5649f9c4b9b52710a2b5be835a0ee86eef`

Restore application moved to a cancellable post-mount callback while preserving the restore gate.

### Explicit clear / storage failure UX

RED:

`9d3658175db4b8d4fd5f83bf8c68bebd4ad1535b`

56 existing/new tests passed and only the two missing-clear expectations failed.

GREEN lineage:

`11d423a4a6498d6aee2f060d635a10615514b2aa`

### Unreadable-storage overwrite hardening

A review found that a failed storage read could still be followed by an autosave of blank defaults.

RED:

`e0464b1d792507230eea879f0722e04ad8241a9f`

The test reproduced a blank V1 `setItem()` after `getItem()` failure.

GREEN:

`3449cccbbe2fd0668a0091450553569981a9e6de`

Autosave ownership moved to semantic-difference tracking through the last read/successfully persisted V1 JSON.

### Clear-before-restore race hardening

RED:

`0752d176dc35bce0530fc7f4b8fd3390df03ce49`

63/64 component tests passed; the clear button was incorrectly actionable before deferred restore completed.

GREEN:

`951721f1711c9c24dfa85eec5468b92b4bf0fc84`

Both the button and handler now guard restore readiness/pending state.

### Corrupt cleanup failure hardening

RED:

`86b9cc4e99a15e413a6c821cb0fe7ce7fd75476f`

64/65 component tests passed; failed removal of a corrupt value was incorrectly reported as storage-ready.

Final GREEN:

`6c54479044e41e5177739b57eb891830a79691f8`

Corrupt cleanup failure now reports local storage unavailable.

## Browser acceptance

Canonical Playwright acceptance uses the real production form/localStorage behavior and deterministic test API environment.

It proves:

1. a non-trivial two-occurrence WeeklyPlan + Pantry draft is edited;
2. occurrence order is changed explicitly;
3. exact stored V1 JSON preserves that order and excludes row keys/comparison/optimizer/provider state;
4. **zero browser POST requests** occur before explicit comparison submit;
5. reload restores locality, occurrence order/values and Pantry values;
6. no comparison/result UI is restored from storage;
7. reload/restore still causes **zero browser POST requests**;
8. explicit submit continues to exercise accepted M4.4.2 and renders checkout optimization output;
9. clear removes visible draft/result state and the local storage key;
10. a second reload returns to the blank initial state and causes no additional implicit POST.

No live retailer request belongs to this acceptance path.

## Final feature gate

Final reviewed feature head:

`6c54479044e41e5177739b57eb891830a79691f8`

Evidence on that exact head:

- **9/9 normal PR workflow groups SUCCESS**;
- Web lint — SUCCESS;
- TypeScript typecheck — SUCCESS;
- **65/65 component tests — SUCCESS**;
- Next production build — SUCCESS;
- Chromium Playwright / Web E2E — SUCCESS;
- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Dependency Review — SUCCESS;
- Container Security CI — SUCCESS;
- Release Bundle CI — SUCCESS;
- CodeQL — SUCCESS;
- read-only Change Review — **Looks good**;
- P0/P1/P2/P3 findings — none;
- nitpicks — none;
- unresolved review threads — 0.

The implementation changes only the browser-local productization layer. There are no M5.1 production changes to API/OpenAPI/generated client, Flyway/database, authentication/accounts, provider acquisition or domain semantics.

## Merge acceptance

PR #149 was squash-merged after the final exact-head green gate.

Accepted implementation merge:

`2f2b96d18521b8bb04f6ee17182d61711322de08`

Post-merge evidence on that exact SHA:

- `main` points to the accepted implementation merge;
- issue #148 is closed with state reason `completed`;
- exactly **8 normal push workflow groups** were created;
- **8/8 SUCCESS**;
- no failed, skipped or cancelled normal push workflow remains.

## Non-goals preserved

M5.1 does not add:

- accounts/login;
- server-side user/profile/preferences tables;
- cloud or cross-device synchronization;
- multiple named plans/history;
- persistence of comparison/economics/optimizer results;
- local-storage encryption/security claims;
- analytics or feature flags;
- provider-health infrastructure;
- retailer acquisition changes.

## Next M5 decision

M5 remains the current phase. M5.2 must be selected from a fresh repository/product survey after this acceptance rather than inferred automatically from the broad Productization roadmap. In particular, M5.1 does not by itself justify introducing accounts/cloud persistence, and provider-health monitoring remains low-value until production evidence sources have meaningful live state.
