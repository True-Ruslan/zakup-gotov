import { cleanup, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { WeeklyPlanComparisonResults } from "./weekly-plan-comparison-results";

afterEach(cleanup);

const milkId = "14000000-0000-0000-0000-000000000001";
const eggsId = "14000000-0000-0000-0000-000000000002";

const comparedPreview: components["schemas"]["WeeklyPlanPantryComparisonPreview"] = {
  pantryShoppingPreview: {
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
    originalShoppingList: {
      id: "13000000-0000-0000-0000-000000000001",
      items: [
        {
          id: milkId,
          requirement: "Молоко",
          quantity: { amount: 1000, unit: "MILLILITER" },
          sources: [{ occurrenceId: "11000000-0000-0000-0000-000000000001", recipeId: "12000000-0000-0000-0000-000000000001", recipeIngredientId: "15000000-0000-0000-0000-000000000001" }],
        },
        {
          id: eggsId,
          requirement: "Яйца",
          quantity: { amount: 10, unit: "PIECE" },
          sources: [],
        },
      ],
    },
    pantryAdjustments: [
      {
        itemId: milkId,
        requirement: "Молоко",
        required: { amount: 1000, unit: "MILLILITER" },
        pantryUsed: { amount: 250, unit: "MILLILITER" },
        remaining: { amount: 750, unit: "MILLILITER" },
        status: "PARTIALLY_COVERED",
      },
      {
        itemId: eggsId,
        requirement: "Яйца",
        required: { amount: 10, unit: "PIECE" },
        pantryUsed: { amount: 10, unit: "PIECE" },
        status: "FULLY_COVERED",
      },
    ],
    remainingShoppingList: {
      id: "13000000-0000-0000-0000-000000000001",
      items: [
        {
          id: milkId,
          requirement: "Молоко",
          quantity: { amount: 750, unit: "MILLILITER" },
          sources: [{ occurrenceId: "11000000-0000-0000-0000-000000000001", recipeId: "12000000-0000-0000-0000-000000000001", recipeIngredientId: "15000000-0000-0000-0000-000000000001" }],
        },
      ],
    },
  },
  comparisonOutcome: "COMPARED",
  comparisonPreview: {
    locality: "Москва",
    items: [
      { id: milkId, requirement: "Молоко", quantity: { amount: 750, unit: "MILLILITER" } },
    ],
    retailers: [],
  },
};

const zeroDemandPreview: components["schemas"]["WeeklyPlanPantryComparisonPreview"] = {
  pantryShoppingPreview: {
    weeklyPlan: { id: "10000000-0000-0000-0000-000000000002", occurrences: [] },
    originalShoppingList: {
      id: "13000000-0000-0000-0000-000000000002",
      items: [{ id: eggsId, requirement: "Яйца", quantity: { amount: 10, unit: "PIECE" }, sources: [] }],
    },
    pantryAdjustments: [{
      itemId: eggsId,
      requirement: "Яйца",
      required: { amount: 10, unit: "PIECE" },
      pantryUsed: { amount: 10, unit: "PIECE" },
      status: "FULLY_COVERED",
    }],
    remainingShoppingList: { id: "13000000-0000-0000-0000-000000000002", items: [] },
  },
  comparisonOutcome: "NO_REMAINING_DEMAND",
};

describe("WeeklyPlan Pantry comparison results", () => {
  it("shows server-owned original demand, Pantry audit, remaining demand and comparison in order", () => {
    render(<WeeklyPlanComparisonResults preview={comparedPreview} />);

    const original = screen.getByRole("region", { name: "Исходный список на неделю" });
    expect(within(original).getByText("Молоко")).toBeDefined();
    expect(within(original).getByText("1000 MILLILITER")).toBeDefined();
    expect(within(original).getByText("Яйца")).toBeDefined();

    const audit = screen.getByRole("region", { name: "Учёт запасов дома" });
    expect(within(audit).getByText("Частично покрыто")).toBeDefined();
    expect(within(audit).getByText("Полностью покрыто")).toBeDefined();
    expect(within(audit).getByText("Из дома: 250 MILLILITER")).toBeDefined();
    expect(within(audit).getByText("Осталось: 750 MILLILITER")).toBeDefined();

    const remaining = screen.getByRole("region", { name: "Осталось купить" });
    expect(within(remaining).getByText("Молоко")).toBeDefined();
    expect(within(remaining).getByText("750 MILLILITER")).toBeDefined();
    expect(within(remaining).queryByText("Яйца")).toBeNull();

    expect(screen.getByRole("heading", { level: 2, name: "Результат для Москва" })).toBeDefined();
  });

  it("renders a truthful zero-demand terminal state without retailer comparison", () => {
    render(<WeeklyPlanComparisonResults preview={zeroDemandPreview} />);

    expect(screen.getByRole("heading", { level: 2, name: "Покупать ничего не нужно" })).toBeDefined();
    expect(screen.getByText("Запасы дома полностью покрывают недельный список.")).toBeDefined();
    expect(screen.queryByRole("heading", { name: /Результат для/ })).toBeNull();
  });

  it("fails closed when COMPARED has no comparison payload", () => {
    render(<WeeklyPlanComparisonResults preview={{ ...comparedPreview, comparisonPreview: undefined }} />);

    expect(screen.getByRole("alert").textContent).toContain("Не удалось показать сравнение магазинов");
    expect(screen.queryByRole("heading", { name: /Результат для/ })).toBeNull();
  });

  it("keeps generated identities and provenance out of user-facing text", () => {
    const { container } = render(<WeeklyPlanComparisonResults preview={comparedPreview} />);
    const text = container.textContent ?? "";
    expect(text).not.toMatch(/[0-9a-f]{8}-[0-9a-f-]{27,}/i);
    expect(text).not.toContain("itemId");
    expect(text).not.toContain("occurrenceId");
    expect(text).not.toContain("recipeIngredientId");
  });
});
