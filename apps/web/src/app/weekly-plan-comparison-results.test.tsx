import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { WeeklyPlanComparisonResults } from "./weekly-plan-comparison-results";

afterEach(cleanup);

const preview: components["schemas"]["WeeklyPlanComparisonPreview"] = {
  weeklyPlanShoppingPreview: {
    weeklyPlan: {
      id: "10000000-0000-0000-0000-000000000001",
      occurrences: [
        {
          id: "11000000-0000-0000-0000-000000000001",
          day: "MONDAY",
          targetServings: 4,
          recipe: { id: "12000000-0000-0000-0000-000000000001", title: "Блины", baseServings: 2, ingredients: [] },
        },
      ],
    },
    shoppingList: {
      id: "13000000-0000-0000-0000-000000000001",
      items: [
        {
          id: "14000000-0000-0000-0000-000000000001",
          requirement: "Молоко",
          quantity: { amount: 1000, unit: "MILLILITER" },
          sources: [{ occurrenceId: "11000000-0000-0000-0000-000000000001", recipeId: "12000000-0000-0000-0000-000000000001", recipeIngredientId: "15000000-0000-0000-0000-000000000001" }],
        },
        {
          id: "14000000-0000-0000-0000-000000000002",
          requirement: "Яйца",
          quantity: { amount: 10, unit: "PIECE" },
          sources: [],
        },
      ],
    },
  },
  comparisonPreview: {
    locality: "Москва",
    items: [
      { id: "14000000-0000-0000-0000-000000000001", requirement: "Молоко", quantity: { amount: 1000, unit: "MILLILITER" } },
      { id: "14000000-0000-0000-0000-000000000002", requirement: "Яйца", quantity: { amount: 10, unit: "PIECE" } },
    ],
    retailers: [],
  },
};

describe("WeeklyPlan comparison results", () => {
  it("shows canonical weekly shopping before retailer comparison", () => {
    render(<WeeklyPlanComparisonResults preview={preview} />);

    expect(screen.getByRole("heading", { level: 2, name: "Покупки на неделю" })).toBeDefined();
    expect(screen.getByText("Молоко")).toBeDefined();
    expect(screen.getByText("1000 MILLILITER")).toBeDefined();
    expect(screen.getByText("Яйца")).toBeDefined();
    expect(screen.getByText("10 PIECE")).toBeDefined();
    expect(screen.getByRole("heading", { level: 2, name: "Результат для Москва" })).toBeDefined();
  });

  it("keeps generated identities and provenance out of user-facing text", () => {
    const { container } = render(<WeeklyPlanComparisonResults preview={preview} />);
    const text = container.textContent ?? "";
    expect(text).not.toMatch(/[0-9a-f]{8}-[0-9a-f-]{27,}/i);
    expect(text).not.toContain("occurrenceId");
    expect(text).not.toContain("recipeIngredientId");
  });
});
