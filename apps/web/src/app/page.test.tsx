import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import Home from "./page";
import { loadRetailerReadiness } from "./retailer-readiness";

vi.mock("./retailer-readiness", () => ({
  loadRetailerReadiness: vi.fn(),
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
  it("renders the M2 Recipe journey first while preserving manual basket comparison", async () => {
    render(await Home());

    expect(screen.getAllByRole("heading", { level: 1 })).toHaveLength(1);
    expect(screen.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeDefined();
    expect(screen.getByText(/M2 · Recipes/i)).toBeDefined();
    expect(screen.queryByText(/M1 · Shopping Core/i)).toBeNull();
    expect(screen.queryByText(/M0 · Product & Integration Discovery/i)).toBeNull();

    const recipeHeading = screen.getByRole("heading", { level: 2, name: "Сравнить рецепт" });
    const manualHeading = screen.getByRole("heading", { level: 2, name: "Сравнить корзину" });
    expect(recipeHeading).toBeDefined();
    expect(manualHeading).toBeDefined();
    expect(recipeHeading.compareDocumentPosition(manualHeading) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

    expect(screen.queryByRole("heading", { level: 2, name: "Покрытие магазинов" })).toBeNull();
    expect(mockedLoadRetailerReadiness).not.toHaveBeenCalled();

    const documentation = screen.getByRole("link", { name: "Документация проекта" });
    expect(documentation.getAttribute("href")).toBe(
      "https://github.com/True-Ruslan/zakup-gotov#readme",
    );
  });
});
