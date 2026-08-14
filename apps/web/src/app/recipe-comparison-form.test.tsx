import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { createRecipeComparisonPreview } from "./recipe-comparison";
import { RecipeComparisonForm } from "./recipe-comparison-form";

vi.mock("./recipe-comparison", () => ({
  createRecipeComparisonPreview: vi.fn(),
}));

vi.mock("./recipe-comparison-results", () => ({
  RecipeComparisonResults: () => <section aria-label="Результаты рецепта">Результаты рецепта</section>,
}));

const mockedCreateRecipeComparisonPreview = vi.mocked(createRecipeComparisonPreview);

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("Recipe comparison form", () => {
  it("starts with sensible serving defaults and one removable-protected ingredient", () => {
    render(<RecipeComparisonForm />);

    expect((screen.getByLabelText("Порций в рецепте") as HTMLInputElement).value).toBe("2");
    expect((screen.getByLabelText("Нужно порций") as HTMLInputElement).value).toBe("2");
    expect(screen.getAllByLabelText("Ингредиент")).toHaveLength(1);
    expect((screen.getByLabelText("Количество") as HTMLInputElement).value).toBe("1");
    expect((screen.getByLabelText("Единица") as HTMLSelectElement).value).toBe("PIECE");
    expect((screen.getByRole("button", { name: "Удалить ингредиент" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("adds and removes ingredient rows without allowing an empty recipe", () => {
    render(<RecipeComparisonForm />);

    fireEvent.click(screen.getByRole("button", { name: "Добавить ингредиент" }));
    expect(screen.getAllByLabelText("Ингредиент")).toHaveLength(2);
    expect((screen.getAllByRole("button", { name: "Удалить ингредиент" })[0] as HTMLButtonElement).disabled).toBe(false);

    fireEvent.click(screen.getAllByRole("button", { name: "Удалить ингредиент" })[1]!);
    expect(screen.getAllByLabelText("Ингредиент")).toHaveLength(1);
    expect((screen.getByRole("button", { name: "Удалить ингредиент" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("submits locality, servings and ingredients through the generated M2.3 request shape", async () => {
    mockedCreateRecipeComparisonPreview.mockResolvedValue({
      kind: "ready",
      data: {
        recipeShoppingPreview: {
          recipe: {
            id: "10000000-0000-0000-0000-000000000001",
            title: "Блины",
            baseServings: 2,
            targetServings: 4,
            ingredients: [],
          },
          shoppingList: { id: "30000000-0000-0000-0000-000000000001", items: [] },
        },
        comparisonPreview: { locality: "Москва", items: [], retailers: [] },
      },
    });
    render(<RecipeComparisonForm />);

    fireEvent.change(screen.getByLabelText("Название рецепта"), { target: { value: "  Блины  " } });
    fireEvent.change(screen.getByLabelText("Порций в рецепте"), { target: { value: "2" } });
    fireEvent.change(screen.getByLabelText("Нужно порций"), { target: { value: "4" } });
    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "  Москва  " } });
    fireEvent.change(screen.getByLabelText("Ингредиент"), { target: { value: "  Молоко  " } });
    fireEvent.change(screen.getByLabelText("Количество"), { target: { value: "0.5" } });
    fireEvent.change(screen.getByLabelText("Единица"), { target: { value: "LITER" } });

    fireEvent.click(screen.getByRole("button", { name: "Добавить ингредиент" }));
    fireEvent.change(screen.getAllByLabelText("Ингредиент")[1]!, { target: { value: "Яйца" } });
    fireEvent.change(screen.getAllByLabelText("Количество")[1]!, { target: { value: "5" } });
    fireEvent.change(screen.getAllByLabelText("Единица")[1]!, { target: { value: "PIECE" } });

    fireEvent.click(screen.getByRole("button", { name: "Сравнить рецепт" }));

    await waitFor(() => expect(mockedCreateRecipeComparisonPreview).toHaveBeenCalledTimes(1));
    expect(mockedCreateRecipeComparisonPreview).toHaveBeenCalledWith({
      locality: "Москва",
      recipe: {
        title: "Блины",
        baseServings: 2,
        targetServings: 4,
        ingredients: [
          { requirement: "Молоко", quantity: { amount: 0.5, unit: "LITER" } },
          { requirement: "Яйца", quantity: { amount: 5, unit: "PIECE" } },
        ],
      },
    });
    expect(screen.getByLabelText("Результаты рецепта")).toBeDefined();
  });

  it("rejects incomplete or invalid input before contacting the backend", () => {
    render(<RecipeComparisonForm />);

    fireEvent.change(screen.getByLabelText("Порций в рецепте"), { target: { value: "1.5" } });
    fireEvent.change(screen.getByLabelText("Нужно порций"), { target: { value: "0" } });
    fireEvent.change(screen.getByLabelText("Количество"), { target: { value: "0" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить рецепт" }));

    const alert = screen.getByRole("alert");
    expect(alert.textContent).toContain("Укажите название рецепта");
    expect(alert.textContent).toContain("Укажите населённый пункт");
    expect(alert.textContent).toContain("Порций в рецепте должно быть целым числом больше 0");
    expect(alert.textContent).toContain("Нужно порций должно быть целым числом больше 0");
    expect(alert.textContent).toContain("Укажите ингредиент");
    expect(alert.textContent).toContain("Количество ингредиента должно быть больше 0");
    expect(mockedCreateRecipeComparisonPreview).not.toHaveBeenCalled();
  });

  it("shows backend field errors without exposing transport metadata", async () => {
    mockedCreateRecipeComparisonPreview.mockResolvedValue({
      kind: "invalid",
      errors: [{ field: "recipe.ingredients[0].quantity.amount", message: "must be greater than 0" }],
    });
    render(<RecipeComparisonForm />);

    fireEvent.change(screen.getByLabelText("Название рецепта"), { target: { value: "Блины" } });
    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Москва" } });
    fireEvent.change(screen.getByLabelText("Ингредиент"), { target: { value: "Молоко" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить рецепт" }));

    await screen.findByRole("alert");
    expect(screen.getByRole("alert").textContent).toContain("recipe.ingredients[0].quantity.amount");
    expect(screen.getByRole("alert").textContent).not.toContain("sourceProviderId");
    expect(screen.queryByLabelText("Результаты рецепта")).toBeNull();
  });

  it("shows one accessible fail-closed state when the service is unavailable", async () => {
    mockedCreateRecipeComparisonPreview.mockResolvedValue({ kind: "unavailable" });
    render(<RecipeComparisonForm />);

    fireEvent.change(screen.getByLabelText("Название рецепта"), { target: { value: "Блины" } });
    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Москва" } });
    fireEvent.change(screen.getByLabelText("Ингредиент"), { target: { value: "Молоко" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить рецепт" }));

    const alert = await screen.findByRole("alert");
    expect(alert.textContent).toContain("Не удалось сравнить рецепт. Основной сервис временно недоступен.");
    expect(screen.queryByLabelText("Результаты рецепта")).toBeNull();
  });
});
