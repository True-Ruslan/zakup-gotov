import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { RecipeComparisonResults } from "./recipe-comparison-results";

afterEach(cleanup);

const preview: components["schemas"]["RecipeComparisonPreviewResponse"] = {
  recipeShoppingPreview: {
    recipe: {
      id: "10000000-0000-0000-0000-000000000001",
      title: "Блины",
      baseServings: 2,
      targetServings: 4,
      ingredients: [
        {
          id: "20000000-0000-0000-0000-000000000001",
          requirement: "Молоко",
          quantity: { amount: 500, unit: "MILLILITER" },
        },
      ],
    },
    shoppingList: {
      id: "30000000-0000-0000-0000-000000000001",
      items: [
        {
          id: "40000000-0000-0000-0000-000000000001",
          requirement: "Молоко",
          quantity: { amount: 1000, unit: "MILLILITER" },
          sourceIngredientIds: ["20000000-0000-0000-0000-000000000001"],
        },
        {
          id: "40000000-0000-0000-0000-000000000002",
          requirement: "Яйца",
          quantity: { amount: 10, unit: "PIECE" },
          sourceIngredientIds: ["20000000-0000-0000-0000-000000000002"],
        },
      ],
    },
  },
  comparisonPreview: {
    locality: "Москва",
    items: [
      {
        id: "40000000-0000-0000-0000-000000000001",
        requirement: "Молоко",
        quantity: { amount: 1000, unit: "MILLILITER" },
      },
      {
        id: "40000000-0000-0000-0000-000000000002",
        requirement: "Яйца",
        quantity: { amount: 10, unit: "PIECE" },
      },
    ],
    retailers: [],
  },
};

describe("Recipe comparison results", () => {
  it("shows canonical generated shopping requirements before the retailer comparison", () => {
    render(<RecipeComparisonResults preview={preview} />);

    expect(screen.getByRole("heading", { level: 2, name: "Список покупок из рецепта" })).toBeDefined();
    expect(screen.getByText("Молоко")).toBeDefined();
    expect(screen.getByText("1000 MILLILITER")).toBeDefined();
    expect(screen.getByText("Яйца")).toBeDefined();
    expect(screen.getByText("10 PIECE")).toBeDefined();
    expect(screen.getByRole("heading", { level: 2, name: "Результат для Москва" })).toBeDefined();
  });

  it("does not render transient UUIDs as user-facing content", () => {
    const { container } = render(<RecipeComparisonResults preview={preview} />);
    const text = container.textContent ?? "";

    expect(text).not.toContain("10000000-0000-0000-0000-000000000001");
    expect(text).not.toContain("20000000-0000-0000-0000-000000000001");
    expect(text).not.toContain("30000000-0000-0000-0000-000000000001");
    expect(text).not.toContain("40000000-0000-0000-0000-000000000001");
  });
});
