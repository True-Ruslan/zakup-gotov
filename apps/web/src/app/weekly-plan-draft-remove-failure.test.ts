import { describe, expect, it } from "vitest";

import { WEEKLY_PLAN_DRAFT_STORAGE_KEY, readWeeklyPlanDraft } from "./weekly-plan-draft";

describe("WeeklyPlan corrupt draft cleanup", () => {
  it("reports storage unavailable when an invalid stored draft cannot be removed", () => {
    const storage = {
      getItem(key: string) {
        return key === WEEKLY_PLAN_DRAFT_STORAGE_KEY
          ? JSON.stringify({ version: 1, locality: "", occurrences: [], pantry: [] })
          : null;
      },
      removeItem() {
        throw new DOMException("blocked", "SecurityError");
      },
    } as unknown as Storage;

    expect(readWeeklyPlanDraft(storage)).toEqual({ kind: "unavailable" });
  });
});
