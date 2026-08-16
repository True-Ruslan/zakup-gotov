import { describe, expect, it } from "vitest";

import {
  WEEKLY_PLAN_DRAFT_STORAGE_KEY,
  decodeWeeklyPlanDraft,
  readWeeklyPlanDraft,
  removeWeeklyPlanDraft,
  writeWeeklyPlanDraft,
  type WeeklyPlanDraftV1,
} from "./weekly-plan-draft";

class MemoryStorage implements Storage {
  private readonly values = new Map<string, string>();

  get length() {
    return this.values.size;
  }

  clear() {
    this.values.clear();
  }

  getItem(key: string) {
    return this.values.get(key) ?? null;
  }

  key(index: number) {
    return Array.from(this.values.keys())[index] ?? null;
  }

  removeItem(key: string) {
    this.values.delete(key);
  }

  setItem(key: string, value: string) {
    this.values.set(key, value);
  }
}

function draft(): WeeklyPlanDraftV1 {
  return {
    version: 1,
    locality: "Москва",
    occurrences: [
      {
        day: "TUESDAY",
        targetServings: "",
        title: "Блины",
        baseServings: "2",
        ingredients: [
          { requirement: "Молоко", amount: "0.5", unit: "LITER" },
          { requirement: "Яйца", amount: "", unit: "PIECE" },
        ],
      },
    ],
    pantry: [{ requirement: "Мука", amount: "250", unit: "GRAM" }],
  };
}

describe("private WeeklyPlan draft contract", () => {
  it("round-trips a supported unfinished ordered draft without presentation or server state", () => {
    const storage = new MemoryStorage();
    const value = {
      ...draft(),
      key: 77,
      optimizationPreview: { status: "UNIQUE_WINNER" },
      occurrences: draft().occurrences.map((occurrence) => ({
        ...occurrence,
        key: 12,
        ingredients: occurrence.ingredients.map((ingredient) => ({ ...ingredient, key: 4 })),
      })),
      pantry: draft().pantry.map((item) => ({ ...item, key: 9 })),
    };

    expect(writeWeeklyPlanDraft(storage, value)).toEqual({ kind: "saved" });

    const raw = storage.getItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY);
    expect(raw).not.toBeNull();
    expect(raw).not.toContain('"key"');
    expect(raw).not.toContain("optimizationPreview");
    expect(raw).not.toContain("retailerId");
    expect(raw).not.toContain("shoppingListId");
    expect(readWeeklyPlanDraft(storage)).toEqual({ kind: "ready", draft: draft() });
  });

  it("rejects malformed, unsupported and structurally invalid drafts", () => {
    expect(decodeWeeklyPlanDraft("{")) .toBeNull();
    expect(decodeWeeklyPlanDraft(JSON.stringify({ ...draft(), version: 2 }))).toBeNull();
    expect(decodeWeeklyPlanDraft(JSON.stringify({ ...draft(), occurrences: [] }))).toBeNull();
    expect(decodeWeeklyPlanDraft(JSON.stringify({
      ...draft(),
      occurrences: [{ ...draft().occurrences[0], day: "FUNDAY" }],
    }))).toBeNull();
    expect(decodeWeeklyPlanDraft(JSON.stringify({
      ...draft(),
      pantry: [{ requirement: "Мука", amount: "250", unit: "BOX" }],
    }))).toBeNull();
  });

  it("removes a corrupt stored value and returns the blank ready state", () => {
    const storage = new MemoryStorage();
    storage.setItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY, JSON.stringify({ version: 1, occurrences: [] }));

    expect(readWeeklyPlanDraft(storage)).toEqual({ kind: "ready", draft: null });
    expect(storage.getItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY)).toBeNull();
  });

  it("contains storage get/set/remove failures instead of throwing", () => {
    const throwingStorage = {
      getItem() { throw new DOMException("blocked", "SecurityError"); },
      setItem() { throw new DOMException("quota", "QuotaExceededError"); },
      removeItem() { throw new DOMException("blocked", "SecurityError"); },
    } as unknown as Storage;

    expect(readWeeklyPlanDraft(throwingStorage)).toEqual({ kind: "unavailable" });
    expect(writeWeeklyPlanDraft(throwingStorage, draft())).toEqual({ kind: "unavailable" });
    expect(removeWeeklyPlanDraft(throwingStorage)).toEqual({ kind: "unavailable" });
  });
});
