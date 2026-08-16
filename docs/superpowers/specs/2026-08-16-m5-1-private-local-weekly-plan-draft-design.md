# M5.1 Private Local WeeklyPlan Draft — Design

**Date:** 2026-08-16  
**Issue:** #148  
**Baseline:** `3e6ed1656520a8e71d6a4c109e4158b554260874`

## Goal

Make the accepted primary WeeklyPlan/Pantry journey useful across repeat visits by preserving one current input draft in the user's browser, without creating accounts, server persistence, cloud sync or a new personal-data trust boundary.

M5.1 is a **browser-local productization slice**. It changes only the editable input experience. All accepted Shopping, Recipe, WeeklyPlan, Pantry, retailer comparison, checkout economics and optimizer semantics remain server-owned and unchanged.

## Why this is M5.1

Repository evidence makes local draft persistence the smallest useful repeat-use improvement:

- M4 is COMPLETE / ACCEPTED and M5 Productization is current;
- the primary form currently initializes with blank locality, one blank occurrence and no Pantry rows on every mount;
- the API has no authentication/account layer or Spring Security dependency;
- Flyway contains only the baseline migration and no user/preferences/saved-plan schema;
- server-side plan/history persistence remains intentionally deferred until repeat-use value is established;
- the production comparison source is still no-op because no retailer is production-access READY, so provider-health monitoring currently has little live production state to measure.

This slice therefore proves repeat-use value while minimizing privacy and operational cost.

## Product behavior

### One current local draft

The browser stores at most one supported current WeeklyPlan/Pantry input draft under one versioned same-origin storage key.

The draft is **not** a saved plan, history item, account preference or synchronization record. It is a convenience copy of the current editable form state for this browser only.

### Persisted fields

Persist only user-authored form vocabulary:

- `locality` text;
- ordered meal occurrences:
  - day;
  - target-servings text;
  - Recipe title;
  - base-servings text;
  - ordered ingredient requirement/amount/unit text;
- ordered Pantry requirement/amount/unit text.

Numeric form fields remain text in the draft. This preserves incomplete editing states such as an empty amount field instead of inventing valid domain values during persistence.

### Fields that must never be persisted

- presentation-only React row keys;
- generated WeeklyPlan/occurrence/Recipe/ingredient/ShoppingList/ShoppingItem IDs;
- Pantry adjustment evidence;
- comparison retailer results;
- checkout economics;
- optimizer status/winners/totals;
- API errors, pending state or previous response state;
- provider/acquisition/fulfillment identifiers;
- cookies, auth tokens, credentials or browser-bridge state.

## Draft contract

Add a focused browser module next to the WeeklyPlan UI, e.g. `weekly-plan-draft.ts`.

Canonical storage key:

`zakup-gotov.weekly-plan-draft.v1`

Stored shape:

```ts
type WeeklyPlanDraftV1 = {
  version: 1;
  locality: string;
  occurrences: Array<{
    day: WeeklyPlanDay;
    targetServings: string;
    title: string;
    baseServings: string;
    ingredients: Array<{
      requirement: string;
      amount: string;
      unit: QuantityUnit;
    }>;
  }>;
  pantry: Array<{
    requirement: string;
    amount: string;
    unit: QuantityUnit;
  }>;
};
```

The public helper surface should remain narrow:

- encode/sanitize form draft for storage;
- decode/validate a supported stored draft;
- read/write/remove through an injected or explicit `Storage` boundary so failure behavior is testable.

Do not introduce a generic persistence framework, repository abstraction or global state library.

## Structural restore validation

Restore is intentionally structural, not domain-semantic validation. Drafts may contain unfinished user input.

Accepted restore structure:

- exact supported `version: 1`;
- locality is a string and cannot exceed the accepted form maximum of 160 characters;
- occurrence array contains `1..35` entries;
- day is one of the generated accepted WeeklyPlan day values;
- target/base servings are strings;
- Recipe title is a string no longer than the accepted form maximum of 240 characters;
- each occurrence has `1..100` ingredients;
- ingredient requirement is a string no longer than 240 characters;
- ingredient amount is a string;
- ingredient unit is one of the accepted generated quantity units;
- Pantry is an array;
- Pantry requirement is a string no longer than 240 characters;
- Pantry amount is a string;
- Pantry unit is one of the accepted generated quantity units;
- no unknown persisted object members are required for behavior; decoding creates a fresh canonical object containing only allowed fields.

A malformed JSON document, unsupported version, invalid enum, invalid cardinality or wrong field type is unusable and must not be partially restored.

## Tampered/corrupt draft behavior

When a stored value exists but cannot be decoded as the current supported contract:

1. do not throw into React;
2. remove that invalid value when storage removal is available;
3. show the existing blank initial form;
4. continue with local persistence if subsequent storage writes work.

A malformed draft is not migrated heuristically. Future versions require an explicit versioned migration design.

## Storage failure behavior

`localStorage` may be unavailable or throw during get/set/remove, including privacy-policy or quota errors.

Rules:

- form editing/submission remains usable;
- no storage exception escapes the browser adapter or React effect;
- show concise non-alarming copy that local saving is unavailable and data may be lost when leaving the page;
- do not retry on an interval or create polling;
- a later direct user edit may attempt the next normal autosave, but no background retry loop exists.

## Hydration and autosave

The server-rendered initial form remains the existing blank form.

Restore must happen after client mount. Autosave must be gated until the initial restore attempt has completed, otherwise the blank server-render defaults could overwrite an existing draft before it is read.

After restore readiness:

- input changes schedule one short debounced local save;
- changing fields again before the delay replaces the pending save;
- the saved payload is derived from current semantic form values only, never React row keys or server response state;
- no API request is triggered by restore or autosave.

Use a bounded debounce suitable for typing (approximately 250–400 ms). It is a UI write-coalescing delay, not polling.

## Presentation-key reconstruction

Stored JSON does not contain row keys.

After restore, create deterministic presentation keys from current array order:

- occurrence keys: `1..N`;
- ingredient keys independently inside each occurrence: `1..N`;
- Pantry keys: `1..N`.

Subsequent existing add/remove/reorder behavior continues from those local keys.

## Clear-draft behavior

Add one explicit secondary control near the privacy/draft message:

**`Очистить форму и локальный черновик`**

When activated:

- remove the current storage key;
- reset locality to blank;
- reset occurrences to the existing one-blank-occurrence initial state;
- reset Pantry to empty;
- clear previous comparison result and client/API error presentation;
- do not send an API request;
- keep local persistence enabled so later edits create a new draft.

Disable the clear action while an M4.4.2 comparison submission is pending to avoid mixing reset state with an in-flight result.

## Privacy copy

Show persistent explanatory copy near the primary form:

> Черновик сохраняется только в этом браузере и не синхронизируется с аккаунтом или сервером.

When storage is unavailable, replace/augment it with product-safe recovery copy such as:

> Локальное сохранение недоступно. Форма работает, но изменения могут потеряться после закрытия страницы.

Do not claim local browser storage is encrypted or secure credential storage.

## Submission semantics

The accepted M4.4.2 submit path is unchanged:

- validation remains existing browser preflight + server validation;
- request conversion remains generated M4.4.1 vocabulary;
- only explicit submit invokes the server action;
- restore/autosave/clear never invokes comparison;
- results remain server-owned and are never persisted in the draft.

## Test strategy

### Pure draft/storage tests

Prove:

1. supported V1 draft round-trips preserving order and unfinished string values;
2. persisted JSON contains no `key` or server/result identity fields;
3. malformed JSON, unsupported version, invalid enum/type/cardinality fails closed;
4. corrupt values are removed where possible;
5. storage get/set/remove exceptions become explicit adapter outcomes rather than thrown errors.

### React form tests

Prove:

1. existing blank state remains the first no-draft state;
2. a valid draft restores after mount with occurrence/ingredient/Pantry order intact;
3. restore does not call the M4.4.1 server action;
4. edits autosave only after restore readiness;
5. clear resets the visible form and stored value and clears previous result/error state;
6. storage failure leaves form editable and shows local-save-unavailable copy;
7. accepted submit behavior remains regression-green.

### Browser acceptance

Deterministic Playwright must prove:

- enter a non-trivial WeeklyPlan + Pantry draft;
- reload before comparison;
- fields restore in the same order;
- mock API recorded no comparison request merely from restore;
- submit still exercises accepted M4.4.2 output;
- clear the form/local draft;
- reload again and observe the existing blank initial state;
- existing 390px no-overflow, focus, Recipe and manual-list critical journeys stay green.

No live retailer request belongs in M5.1 acceptance.

## Architecture boundaries

M5.1 is web-presentation state only.

Allowed:

- React state/effects;
- generated day/unit type vocabulary;
- browser `Storage` boundary;
- existing M4.4.2 action and result components.

Forbidden:

- API endpoint changes;
- Flyway/database changes;
- Spring Security/accounts;
- server cookies/session persistence;
- provider/browser-bridge storage reuse;
- Shopping/Recipe/WeeklyPlan/Pantry domain changes;
- comparison/economics/optimizer changes;
- analytics or feature-flag infrastructure bundled into this slice.

## Acceptance criteria

M5.1 is acceptable when:

1. one versioned browser-local primary-form draft is restored safely across reloads;
2. unsupported/tampered/storage-failure cases fail closed without breaking form use;
3. persisted JSON contains only approved user-authored input fields;
4. presentation keys and all server/result identities remain unpersisted;
5. privacy and unavailable-storage copy are visible and accurate;
6. explicit clear resets both form and local draft without network activity;
7. restore/autosave never submits a comparison;
8. accepted M4.4.2 request/result semantics are unchanged;
9. deterministic desktop/mobile/accessibility + Recipe/manual regressions remain green;
10. exact-head CI/review and post-merge verification meet the repository's existing acceptance standard.

## Non-goals

- accounts/login;
- cloud or multi-device sync;
- multiple named plans/history;
- server-side user/profile/preferences tables;
- cross-browser synchronization;
- storing comparison results;
- storing precise address/provider/browser-bridge session data;
- local-storage encryption claims;
- analytics/feature flags/provider health;
- any retailer acquisition change.
