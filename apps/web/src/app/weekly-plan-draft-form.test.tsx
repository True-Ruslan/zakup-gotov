import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { createWeeklyPlanOptimizationPreview } from "./weekly-plan-comparison";
import { WeeklyPlanComparisonForm } from "./weekly-plan-comparison-form";
import { WEEKLY_PLAN_DRAFT_STORAGE_KEY, type WeeklyPlanDraftV1 } from "./weekly-plan-draft";

vi.mock("./weekly-plan-comparison", () => ({ createWeeklyPlanOptimizationPreview: vi.fn() }));
vi.mock("./weekly-plan-comparison-results", () => ({
  WeeklyPlanComparisonResults: () => <section aria-label="Результаты недельного плана">Результаты недельного плана</section>,
}));

const mockedCreate = vi.mocked(createWeeklyPlanOptimizationPreview);

afterEach(() => {
  cleanup();
  window.localStorage.clear();
  vi.restoreAllMocks();
  vi.clearAllMocks();
});

function storedDraft(): WeeklyPlanDraftV1 {
  return {
    version: 1,
    locality: "Москва",
    occurrences: [
      {
        day: "FRIDAY",
        targetServings: "4",
        title: "Суп",
        baseServings: "2",
        ingredients: [{ requirement: "Картофель", amount: "500", unit: "GRAM" }],
      },
    ],
    pantry: [{ requirement: "Соль", amount: "10", unit: "GRAM" }],
  };
}

function fillRequiredWeeklyPlan() {
  fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Москва" } });
  const group = screen.getByRole("group", { name: "Блюдо 1" });
  fireEvent.change(within(group).getByLabelText("Название рецепта"), { target: { value: "Омлет" } });
  fireEvent.change(within(group).getByLabelText("Ингредиент"), { target: { value: "Яйца" } });
}

describe("WeeklyPlan local draft recovery controls", () => {
  it("clears the visible form, client error and stored draft without submitting", async () => {
    window.localStorage.setItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY, JSON.stringify(storedDraft()));
    render(<WeeklyPlanComparisonForm />);

    await waitFor(() => expect((screen.getByLabelText("Населённый пункт") as HTMLInputElement).value).toBe("Москва"));
    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить план" }));
    expect(screen.getByRole("alert").textContent).toContain("Укажите населённый пункт");

    fireEvent.click(screen.getByRole("button", { name: "Очистить форму и локальный черновик" }));

    expect((screen.getByLabelText("Населённый пункт") as HTMLInputElement).value).toBe("");
    expect(screen.getAllByRole("group", { name: /Блюдо/ })).toHaveLength(1);
    expect((screen.getByLabelText("Название рецепта") as HTMLInputElement).value).toBe("");
    expect(screen.queryByRole("group", { name: /Запас дома/ })).toBeNull();
    expect(screen.queryByRole("alert")).toBeNull();
    expect(window.localStorage.getItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY)).toBeNull();
    expect(mockedCreate).not.toHaveBeenCalled();
  });

  it("keeps editing usable and exposes accurate copy when local storage cannot be read", async () => {
    vi.spyOn(Storage.prototype, "getItem").mockImplementation(() => {
      throw new DOMException("blocked", "SecurityError");
    });

    render(<WeeklyPlanComparisonForm />);

    expect(await screen.findByText(/Локальное сохранение недоступно/)).toBeDefined();
    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Казань" } });
    expect((screen.getByLabelText("Населённый пункт") as HTMLInputElement).value).toBe("Казань");
  });

  it("does not overwrite unknown storage after a read failure until the user edits", async () => {
    const setItem = vi.spyOn(Storage.prototype, "setItem");
    vi.spyOn(Storage.prototype, "getItem").mockImplementation(() => {
      throw new DOMException("blocked", "SecurityError");
    });

    render(<WeeklyPlanComparisonForm />);

    expect(await screen.findByText(/Локальное сохранение недоступно/)).toBeDefined();
    await new Promise((resolve) => window.setTimeout(resolve, 350));
    expect(setItem).not.toHaveBeenCalled();

    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Казань" } });
    await waitFor(() => expect(setItem).toHaveBeenCalledWith(WEEKLY_PLAN_DRAFT_STORAGE_KEY, expect.any(String)));
  });

  it("ignores an unsupported draft and restores the existing blank initial state", async () => {
    window.localStorage.setItem(
      WEEKLY_PLAN_DRAFT_STORAGE_KEY,
      JSON.stringify({ ...storedDraft(), version: 99 }),
    );

    render(<WeeklyPlanComparisonForm />);

    await waitFor(() => expect((screen.getByLabelText("Населённый пункт") as HTMLInputElement).value).toBe(""));
    expect((screen.getByLabelText("Название рецепта") as HTMLInputElement).value).toBe("");
    expect(screen.queryByRole("group", { name: /Запас дома/ })).toBeNull();
  });

  it("disables clear while an accepted comparison request is pending", async () => {
    let resolveRequest: ((value: { kind: "unavailable" }) => void) | undefined;
    mockedCreate.mockImplementation(() => new Promise((resolve) => {
      resolveRequest = resolve;
    }));

    render(<WeeklyPlanComparisonForm />);
    fillRequiredWeeklyPlan();
    fireEvent.click(screen.getByRole("button", { name: "Сравнить план" }));

    await waitFor(() => expect(mockedCreate).toHaveBeenCalledTimes(1));
    expect((screen.getByRole("button", { name: "Очистить форму и локальный черновик" }) as HTMLButtonElement).disabled).toBe(true);

    resolveRequest?.({ kind: "unavailable" });
    await waitFor(() => expect((screen.getByRole("button", { name: "Очистить форму и локальный черновик" }) as HTMLButtonElement).disabled).toBe(false));
  });
});
