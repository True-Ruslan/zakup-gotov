# M5.1 Private Local WeeklyPlan Draft Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve one current WeeklyPlan/Pantry input draft across browser reloads using versioned local-only storage without changing any server/domain semantics.

**Architecture:** Add one focused `weekly-plan-draft.ts` module that owns the V1 stored shape, structural decoder and safe `Storage` read/write/remove boundary. The existing client form restores after mount, reconstructs presentation-only keys, gates autosave until restore completes, and exposes privacy/failure copy plus an explicit clear action. No API/database/auth change is permitted.

**Tech Stack:** Next.js 16.3, React 19.2, TypeScript 5.9, Vitest 4.1, Testing Library, Playwright 1.62, generated `@zakup-gotov/api-client` types.

## Global Constraints

- Storage key is exactly `zakup-gotov.weekly-plan-draft.v1`.
- Persist only user-authored locality/occurrence/Recipe ingredient/Pantry input vocabulary.
- Never persist React row keys, generated domain/server IDs, comparison/economics/optimizer results, provider metadata, errors or pending state.
- Restore only after client mount; autosave must not run until the initial restore attempt is complete.
- Invalid/unsupported/tampered drafts fail closed and are removed when possible.
- `Storage` failures never break form editing/submission and never create background retry polling.
- Existing M4.4.2 submit/result semantics remain unchanged and remain the only server path.
- No API, OpenAPI, generated-client, Flyway/database, auth/security, provider or domain changes.
- Ordinary component/E2E acceptance makes no live retailer request.

---

### Task 1: Versioned draft codec and safe storage boundary

**Files:**
- Create: `apps/web/src/app/weekly-plan-draft.ts`
- Create: `apps/web/src/app/weekly-plan-draft.test.ts`

**Interfaces:**
- Produces exported `WeeklyPlanDraftV1`, `WeeklyPlanDraftOccurrence`, `WeeklyPlanDraftIngredient`, `WeeklyPlanDraftPantryItem` types.
- Produces `WEEKLY_PLAN_DRAFT_STORAGE_KEY`.
- Produces safe `readWeeklyPlanDraft(storage)`, `writeWeeklyPlanDraft(storage, draft)` and `removeWeeklyPlanDraft(storage)` operations whose failures are returned, never thrown.
- Produces a structural decoder that emits a fresh allowed-field-only V1 object.

- [ ] **Step 1: Write RED codec/storage tests**

Cover:

```ts
it("round-trips a supported unfinished ordered draft without presentation keys", () => { /* V1 shape */ });
it("rejects malformed JSON, unsupported versions, invalid days/units and invalid cardinality", () => { /* null/unusable */ });
it("removes a corrupt stored value when removal is available", () => { /* memory Storage */ });
it("returns unavailable instead of throwing when get/set/remove throw", () => { /* throwing Storage */ });
it("serialized payload contains no row key or server/result identity vocabulary", () => { /* inspect JSON */ });
```

Use generated `WeeklyPlanDay` / `QuantityInputUnit` type vocabulary rather than parallel domain enums.

- [ ] **Step 2: Run focused tests and capture meaningful RED**

Run from repository root:

```bash
pnpm --filter web test -- weekly-plan-draft.test.ts
```

Expected: FAIL because the draft module/API does not exist.

- [ ] **Step 3: Implement minimal V1 codec/storage adapter**

Create a narrow module with:

```ts
export const WEEKLY_PLAN_DRAFT_STORAGE_KEY = "zakup-gotov.weekly-plan-draft.v1";

export type WeeklyPlanDraftV1 = {
  version: 1;
  locality: string;
  occurrences: WeeklyPlanDraftOccurrence[];
  pantry: WeeklyPlanDraftPantryItem[];
};

export type DraftReadResult =
  | { kind: "ready"; draft: WeeklyPlanDraftV1 | null }
  | { kind: "unavailable" };

export type DraftWriteResult = { kind: "saved" } | { kind: "unavailable" };
```

Decoder rules must exactly follow the design limits: locality <=160, 1..35 occurrences, 1..100 ingredients per occurrence, generated day/unit values, 240-character requirement/title maxima, string edit values, Pantry array structural validation. Parse into fresh objects containing only approved fields.

- [ ] **Step 4: Run focused tests GREEN**

```bash
pnpm --filter web test -- weekly-plan-draft.test.ts
pnpm --filter web typecheck
```

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message:

```text
test(m5): prove local WeeklyPlan draft contract
```

followed by the minimal implementation commit if RED and GREEN are intentionally kept separate.

---

### Task 2: Form restore, autosave and privacy state

**Files:**
- Modify: `apps/web/src/app/weekly-plan-comparison-form.tsx`
- Modify: `apps/web/src/app/weekly-plan-comparison-form.test.tsx`
- Reuse: `apps/web/src/app/weekly-plan-draft.ts`

**Interfaces:**
- Form maps presentation rows → `WeeklyPlanDraftV1` without keys.
- Form maps restored V1 arrays → deterministic local keys `1..N`.
- Existing `createWeeklyPlanOptimizationPreview()` submit call remains untouched in ownership and timing.

- [ ] **Step 1: Add RED React tests for restore/autosave/no-submit**

Add tests that install a valid local draft before render and verify:

```ts
expect(screen.getByLabelText("Населённый пункт")).toHaveValue("Москва");
expect(screen.getByLabelText("Название рецепта")).toHaveValue("Омлет");
expect(screen.getByLabelText("Продукт дома")).toHaveValue("Яйца");
expect(mockedCreateWeeklyPlanOptimizationPreview).not.toHaveBeenCalled();
```

Add an autosave test that edits locality/title/Pantry, waits for the bounded debounce, reads the storage key and asserts the semantic values are present while `key` is absent.

- [ ] **Step 2: Run focused RED**

```bash
pnpm --filter web test -- weekly-plan-comparison-form.test.tsx
```

Expected: restore/autosave assertions fail because form persistence is not implemented.

- [ ] **Step 3: Implement mount restore + gated debounced autosave**

Modify the form to:

- initialize existing blank UI exactly as before;
- run one mount effect that reads `window.localStorage` safely;
- reconstruct deterministic row keys from restored order;
- set a `draftReady` gate after the read attempt;
- run a separate autosave effect only when `draftReady` is true;
- debounce writes by a short constant in the 250–400 ms design range;
- derive stored state only from current locality/occurrence/ingredient/Pantry editable values;
- never include result/client messages/pending state.

- [ ] **Step 4: Add privacy/status presentation**

Normal copy:

```text
Черновик сохраняется только в этом браузере и не синхронизируется с аккаунтом или сервером.
```

Unavailable copy:

```text
Локальное сохранение недоступно. Форма работает, но изменения могут потеряться после закрытия страницы.
```

Use ordinary text, not a success/error claim about cloud security.

- [ ] **Step 5: Run form tests GREEN plus regression**

```bash
pnpm --filter web test -- weekly-plan-comparison-form.test.tsx weekly-plan-draft.test.ts
pnpm --filter web typecheck
```

Expected: PASS.

- [ ] **Step 6: Commit**

```text
feat(m5): restore and autosave private weekly draft
```

---

### Task 3: Explicit clear behavior and storage-failure recovery

**Files:**
- Modify: `apps/web/src/app/weekly-plan-comparison-form.tsx`
- Modify: `apps/web/src/app/weekly-plan-comparison-form.test.tsx`
- Modify if needed: `apps/web/src/app/weekly-plan-draft.ts`

**Interfaces:**
- Clear button text exactly: `Очистить форму и локальный черновик`.
- Clear resets form and current response/error state, removes storage, does not call API.

- [ ] **Step 1: Add RED clear and failure tests**

Cover:

```ts
it("clears visible form, result/error state and local draft without submitting", async () => { /* ... */ });
it("keeps the form usable and shows unavailable copy when storage throws", async () => { /* ... */ });
it("ignores and removes corrupt storage before showing blank defaults", async () => { /* ... */ });
```

Also assert clear is disabled while an accepted comparison request is pending.

- [ ] **Step 2: Run RED**

```bash
pnpm --filter web test -- weekly-plan-comparison-form.test.tsx
```

Expected: new clear/failure assertions fail.

- [ ] **Step 3: Implement minimal clear/failure state**

Add one secondary button near draft/privacy copy. Its handler:

```ts
removeWeeklyPlanDraft(window.localStorage);
setLocality("");
setOccurrences([newOccurrence(1)]);
setPantryRows([]);
setState(null);
setClientMessages([]);
```

Preserve draft readiness so later edits create a fresh draft. Storage removal failure changes only local-save availability presentation; it must not prevent the visible reset.

- [ ] **Step 4: Run focused GREEN**

```bash
pnpm --filter web test -- weekly-plan-comparison-form.test.tsx weekly-plan-draft.test.ts
pnpm --filter web typecheck
```

Expected: PASS.

- [ ] **Step 5: Commit**

```text
feat(m5): add explicit local draft reset
```

---

### Task 4: Deterministic browser reload acceptance

**Files:**
- Modify: `apps/web/e2e/home.spec.ts`
- Modify only if instrumentation is necessary: `apps/web/e2e/mock-api.mjs`

**Interfaces:**
- Test uses real browser `localStorage` behavior through the production UI.
- No production fixture/persistence logic is added.

- [ ] **Step 1: Add RED Playwright scenario**

Scenario:

1. open homepage;
2. fill locality, Recipe/ingredient and Pantry values, including at least two ordered occurrences or ingredients so order restoration is observable;
3. do **not** submit;
4. reload;
5. assert exact editable values/order restored;
6. assert mock API has received no M4.4.1 comparison request merely due to reload/restore;
7. submit once and prove accepted M4.4.2 rendering still works;
8. click `Очистить форму и локальный черновик`;
9. reload;
10. assert blank locality, one blank occurrence and no Pantry row.

- [ ] **Step 2: Run RED browser test**

```bash
pnpm --filter web test:e2e -- --grep "local draft"
```

Expected: FAIL until production local-draft behavior is available in the Playwright environment or until any required deterministic request-count instrumentation is added.

- [ ] **Step 3: Add only test instrumentation strictly needed for request-count proof**

If existing mock API cannot expose/verify comparison request count, add a test-only endpoint/state or Playwright route observation. Do not alter production code for test observability.

- [ ] **Step 4: Run focused browser GREEN**

```bash
pnpm --filter web test:e2e -- --grep "local draft"
```

Expected: PASS.

- [ ] **Step 5: Run existing responsive/accessibility critical regressions**

```bash
pnpm --filter web test:e2e
```

Expected: PASS for M4.4.2 states, 390px no-overflow, keyboard focus, Recipe and manual-list journeys.

- [ ] **Step 6: Commit**

```text
test(m5): cover local draft reload and clear
```

---

### Task 5: Full verification and review handoff

**Files:**
- No new production scope unless a failing gate reveals a confirmed defect.

- [ ] **Step 1: Run full Web verification**

```bash
pnpm --filter web lint
pnpm --filter web typecheck
pnpm --filter web test
pnpm --filter web build
pnpm --filter web test:e2e
```

Expected: all PASS.

- [ ] **Step 2: Run repository PR workflows on exact head**

Require the repository's standard nine PR workflow groups to succeed on the final feature SHA.

- [ ] **Step 3: Read-only hardening review**

Verify:

- local storage key/version is explicit;
- no row keys/server/result/provider identity is persisted;
- no restore/autosave API call exists;
- no API/DB/auth/generated-client changes exist;
- storage exceptions are contained;
- hydration guard prevents default overwrite;
- existing M4.4.2 submit ownership is unchanged;
- privacy copy makes only accurate local-storage claims.

- [ ] **Step 4: Change-review gate**

Require no unresolved P0/P1/P2/P3 findings and no review threads before ready/merge.

- [ ] **Step 5: Shipping gate**

After exact-head 9/9 + clean review, mark PR ready, squash-merge with expected-head protection, then require exactly 8/8 normal push workflows on the merge SHA before acceptance documentation.

- [ ] **Step 6: Separate docs acceptance PR**

After implementation acceptance, add M5.1 acceptance evidence and synchronize `PROJECT_STATE.md`, `ROADMAP.md` and `CHANGELOG.md` without mixing runtime changes into the acceptance PR.
