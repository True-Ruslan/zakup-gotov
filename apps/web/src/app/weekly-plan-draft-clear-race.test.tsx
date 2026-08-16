import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { WeeklyPlanComparisonForm } from "./weekly-plan-comparison-form";
import { WEEKLY_PLAN_DRAFT_STORAGE_KEY } from "./weekly-plan-draft";

vi.mock("./weekly-plan-comparison", () => ({
  createWeeklyPlanOptimizationPreview: vi.fn(),
}));
vi.mock("./weekly-plan-comparison-results", () => ({
  WeeklyPlanComparisonResults: () => null,
}));

afterEach(() => {
  cleanup();
  window.localStorage.clear();
  vi.clearAllMocks();
});

describe("WeeklyPlan local draft clear restore gate", () => {
  it("keeps clear disabled until the initial local draft restore has settled", async () => {
    window.localStorage.setItem(
      WEEKLY_PLAN_DRAFT_STORAGE_KEY,
      JSON.stringify({
        version: 1,
        locality: "Москва",
        occurrences: [
          {
            day: "MONDAY",
            targetServings: "2",
            title: "Каша",
            baseServings: "2",
            ingredients: [{ requirement: "Молоко", amount: "1", unit: "LITER" }],
          },
        ],
        pantry: [],
      }),
    );

    render(<WeeklyPlanComparisonForm />);

    const clear = screen.getByRole("button", { name: "Очистить форму и локальный черновик" });
    expect((clear as HTMLButtonElement).disabled).toBe(true);

    await waitFor(() =>
      expect((screen.getByLabelText("Населённый пункт") as HTMLInputElement).value).toBe("Москва"),
    );
    expect((clear as HTMLButtonElement).disabled).toBe(false);
  });
});
