import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import Home from "./page";
import { loadRetailerReadiness } from "./retailer-readiness";

vi.mock("./retailer-readiness", () => ({ loadRetailerReadiness: vi.fn() }));

vi.mock("./weekly-plan-comparison-form", () => ({
  WeeklyPlanComparisonForm: () => (
    <section aria-labelledby="weekly-plan-comparison">
      <h2 id="weekly-plan-comparison">Собрать неделю</h2>
    </section>
  ),
}));

vi.mock("./recipe-comparison-form", () => ({
  RecipeComparisonForm: () => (
    <section aria-labelledby="recipe-comparison">
      <h2 id="recipe-comparison">Сравнить рецепт</h2>
    </section>
  ),
}));

vi.mock("./comparison-preview-form", () => ({
  ComparisonPreviewForm: () => (
    <section aria-labelledby="comparison-preview">
      <h2 id="comparison-preview">Сравнить корзину</h2>
    </section>
  ),
}));

const mockedLoadRetailerReadiness = vi.mocked(loadRetailerReadiness);

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("home page", () => {
  it("renders M3 Weekly Planning first while preserving Recipe and manual journeys", async () => {
    render(await Home());

    expect(screen.getAllByRole("heading", { level: 1 })).toHaveLength(1);
    expect(screen.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeDefined();
    expect(screen.getByText(/M3 · Weekly Planning/i)).toBeDefined();
    expect(screen.queryByText(/M2 · Recipes/i)).toBeNull();

    const weeklyHeading = screen.getByRole("heading", { level: 2, name: "Собрать неделю" });
    const recipeHeading = screen.getByRole("heading", { level: 2, name: "Сравнить рецепт" });
    const manualHeading = screen.getByRole("heading", { level: 2, name: "Сравнить корзину" });
    expect(weeklyHeading.compareDocumentPosition(recipeHeading) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(recipeHeading.compareDocumentPosition(manualHeading) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

    expect(screen.queryByRole("heading", { level: 2, name: "Покрытие магазинов" })).toBeNull();
    expect(mockedLoadRetailerReadiness).not.toHaveBeenCalled();

    const documentation = screen.getByRole("link", { name: "Документация проекта" });
    expect(documentation.getAttribute("href")).toBe("https://github.com/True-Ruslan/zakup-gotov#readme");
  });
});
