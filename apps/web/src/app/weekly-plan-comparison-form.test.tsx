import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { createWeeklyPlanComparisonPreview } from "./weekly-plan-comparison";
import { WeeklyPlanComparisonForm } from "./weekly-plan-comparison-form";

vi.mock("./weekly-plan-comparison", () => ({ createWeeklyPlanComparisonPreview: vi.fn() }));
vi.mock("./weekly-plan-comparison-results", () => ({
  WeeklyPlanComparisonResults: () => <section aria-label="Результаты недельного плана">Результаты недельного плана</section>,
}));

const mockedCreate = vi.mocked(createWeeklyPlanComparisonPreview);

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

function ready() {
  mockedCreate.mockResolvedValue({
    kind: "ready",
    data: {
      weeklyPlanShoppingPreview: {
        weeklyPlan: { id: "10000000-0000-0000-0000-000000000001", occurrences: [] },
        shoppingList: { id: "13000000-0000-0000-0000-000000000001", items: [] },
      },
      comparisonPreview: { locality: "Москва", items: [], retailers: [] },
    },
  });
}

describe("WeeklyPlan comparison form", () => {
  it("starts with one occurrence and one protected ingredient", () => {
    render(<WeeklyPlanComparisonForm />);
    expect(screen.getAllByRole("group", { name: /Блюдо 1/ })).toHaveLength(1);
    expect(screen.getAllByLabelText("Ингредиент")).toHaveLength(1);
    expect((screen.getByRole("button", { name: "Удалить блюдо 1" }) as HTMLButtonElement).disabled).toBe(true);
    expect((screen.getByRole("button", { name: "Переместить блюдо 1 выше" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("adds and explicitly reorders occurrences without sorting by day", () => {
    render(<WeeklyPlanComparisonForm />);
    fireEvent.click(screen.getByRole("button", { name: "Добавить блюдо" }));

    const groups = screen.getAllByRole("group", { name: /Блюдо/ });
    fireEvent.change(within(groups[0]!).getByLabelText("День"), { target: { value: "SUNDAY" } });
    fireEvent.change(within(groups[1]!).getByLabelText("День"), { target: { value: "MONDAY" } });
    fireEvent.change(within(groups[0]!).getByLabelText("Название рецепта"), { target: { value: "Первое" } });
    fireEvent.change(within(groups[1]!).getByLabelText("Название рецепта"), { target: { value: "Второе" } });

    fireEvent.click(screen.getByRole("button", { name: "Переместить блюдо 2 выше" }));
    const reordered = screen.getAllByRole("group", { name: /Блюдо/ });
    expect((within(reordered[0]!).getByLabelText("Название рецепта") as HTMLInputElement).value).toBe("Второе");
    expect((within(reordered[0]!).getByLabelText("День") as HTMLSelectElement).value).toBe("MONDAY");
  });

  it("submits the generated M3.3 request shape and trims presentation text", async () => {
    ready();
    render(<WeeklyPlanComparisonForm />);

    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "  Москва  " } });
    const group = screen.getByRole("group", { name: /Блюдо 1/ });
    fireEvent.change(within(group).getByLabelText("День"), { target: { value: "TUESDAY" } });
    fireEvent.change(within(group).getByLabelText("Нужно порций"), { target: { value: "4" } });
    fireEvent.change(within(group).getByLabelText("Название рецепта"), { target: { value: "  Блины  " } });
    fireEvent.change(within(group).getByLabelText("Порций в рецепте"), { target: { value: "2" } });
    fireEvent.change(within(group).getByLabelText("Ингредиент"), { target: { value: "  Молоко  " } });
    fireEvent.change(within(group).getByLabelText("Количество"), { target: { value: "0.5" } });
    fireEvent.change(within(group).getByLabelText("Единица"), { target: { value: "LITER" } });

    fireEvent.click(screen.getByRole("button", { name: "Сравнить план" }));

    await waitFor(() => expect(mockedCreate).toHaveBeenCalledTimes(1));
    expect(mockedCreate).toHaveBeenCalledWith({
      locality: "Москва",
      weeklyPlan: {
        occurrences: [
          {
            day: "TUESDAY",
            targetServings: 4,
            recipe: {
              title: "Блины",
              baseServings: 2,
              ingredients: [{ requirement: "Молоко", quantity: { amount: 0.5, unit: "LITER" } }],
            },
          },
        ],
      },
    });
    expect(screen.getByLabelText("Результаты недельного плана")).toBeDefined();
  });

  it("rejects invalid input before contacting the backend", () => {
    render(<WeeklyPlanComparisonForm />);
    fireEvent.click(screen.getByRole("button", { name: "Сравнить план" }));
    const alert = screen.getByRole("alert");
    expect(alert.textContent).toContain("Укажите населённый пункт");
    expect(alert.textContent).toContain("Укажите название рецепта для блюда 1");
    expect(alert.textContent).toContain("Укажите ингредиент для блюда 1");
    expect(mockedCreate).not.toHaveBeenCalled();
  });

  it("fails closed when the service is unavailable", async () => {
    mockedCreate.mockResolvedValue({ kind: "unavailable" });
    render(<WeeklyPlanComparisonForm />);
    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Москва" } });
    const group = screen.getByRole("group", { name: /Блюдо 1/ });
    fireEvent.change(within(group).getByLabelText("Название рецепта"), { target: { value: "Блины" } });
    fireEvent.change(within(group).getByLabelText("Ингредиент"), { target: { value: "Молоко" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить план" }));
    expect((await screen.findByRole("alert")).textContent).toContain("Не удалось сравнить недельный план");
    expect(screen.queryByLabelText("Результаты недельного плана")).toBeNull();
  });
});
