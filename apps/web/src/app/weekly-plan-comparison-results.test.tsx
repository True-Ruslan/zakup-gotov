import { cleanup, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { WeeklyPlanComparisonResults } from "./weekly-plan-comparison-results";

afterEach(cleanup);

const milkId = "14000000-0000-0000-0000-000000000001";
const eggsId = "14000000-0000-0000-0000-000000000002";

type OptimizationPreview = components["schemas"]["WeeklyPlanPantryOptimizationPreview"];
type PantryComparison = components["schemas"]["WeeklyPlanPantryComparisonPreview"];
type CheckoutPreview = components["schemas"]["CheckoutOptimizationPreview"];
type RetailerCheckout = components["schemas"]["RetailerCheckoutPreview"];
type RetailerId = components["schemas"]["RetailerId"];

const pantryComparison: PantryComparison = {
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
    items: [{ id: milkId, requirement: "Молоко", quantity: { amount: 750, unit: "MILLILITER" } }],
    retailers: [
      {
        id: "pyaterochka",
        displayName: "Пятёрочка",
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "READY",
        reasons: [],
        total: { amount: 1000, currencyCode: "RUB" },
        items: [],
      },
      {
        id: "perekrestok",
        displayName: "Перекрёсток",
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "READY",
        reasons: [],
        total: { amount: 900, currencyCode: "RUB" },
        items: [],
      },
    ],
  },
};

const zeroDemand: OptimizationPreview = {
  pantryComparisonPreview: {
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
  },
};

function unknownAssessment(retailerId: RetailerId): RetailerCheckout {
  return {
    retailerId,
    assessment: {
      merchandiseSubtotal: { amount: retailerId === "pyaterochka" ? 1000 : 900, currencyCode: "RUB" },
      deliveryFee: { status: "UNKNOWN" },
      serviceFee: { status: "UNKNOWN" },
      minimumOrder: { status: "UNKNOWN" },
      minimumOrderStatus: "UNKNOWN",
      checkoutTotalStatus: "UNKNOWN",
      eligibilityStatus: "UNKNOWN",
      comparabilityStatus: "NOT_COMPARABLE",
    },
  };
}

function comparableAssessment(
  retailerId: RetailerId,
  merchandiseSubtotal: number,
  deliveryFee: number,
  serviceFee: number,
  checkoutTotal: number,
): RetailerCheckout {
  return {
    retailerId,
    assessment: {
      merchandiseSubtotal: { amount: merchandiseSubtotal, currencyCode: "RUB" },
      deliveryFee: { status: "KNOWN", amount: { amount: deliveryFee, currencyCode: "RUB" } },
      serviceFee: { status: "KNOWN", amount: { amount: serviceFee, currencyCode: "RUB" } },
      minimumOrder: { status: "KNOWN", threshold: { amount: 500, currencyCode: "RUB" } },
      minimumOrderStatus: "MET",
      checkoutTotalStatus: "KNOWN",
      checkoutTotal: { amount: checkoutTotal, currencyCode: "RUB" },
      eligibilityStatus: "ELIGIBLE",
      comparabilityStatus: "COMPARABLE",
      comparableCheckoutTotal: { amount: checkoutTotal, currencyCode: "RUB" },
    },
  };
}

function wrapper(optimizationPreview: CheckoutPreview): OptimizationPreview {
  return { pantryComparisonPreview: pantryComparison, optimizationPreview };
}

function noComparable(): OptimizationPreview {
  return wrapper({
    retailers: [unknownAssessment("pyaterochka"), unknownAssessment("perekrestok")],
    status: "NO_COMPARABLE_CANDIDATES",
    optimalRetailerIds: [],
  });
}

function uniqueWinner(): OptimizationPreview {
  return wrapper({
    retailers: [
      {
        retailerId: "pyaterochka",
        assessment: {
          merchandiseSubtotal: { amount: 1000, currencyCode: "RUB" },
          deliveryFee: { status: "UNKNOWN" },
          serviceFee: { status: "KNOWN", amount: { amount: 0, currencyCode: "RUB" } },
          minimumOrder: { status: "KNOWN", threshold: { amount: 800, currencyCode: "RUB" } },
          minimumOrderStatus: "MET",
          checkoutTotalStatus: "UNKNOWN",
          eligibilityStatus: "ELIGIBLE",
          comparabilityStatus: "NOT_COMPARABLE",
        },
      },
      comparableAssessment("perekrestok", 900, 100, 0, 1000),
    ],
    status: "UNIQUE_WINNER",
    optimalRetailerIds: ["perekrestok"],
    lowestComparableCheckoutTotal: { amount: 1000, currencyCode: "RUB" },
  });
}

function tie(): OptimizationPreview {
  return wrapper({
    retailers: [
      comparableAssessment("perekrestok", 900, 100, 0, 1000),
      comparableAssessment("pyaterochka", 1000, 0, 0, 1000),
    ],
    status: "TIE",
    optimalRetailerIds: ["perekrestok", "pyaterochka"],
    lowestComparableCheckoutTotal: { amount: 1000, currencyCode: "RUB" },
  });
}

describe("WeeklyPlan Pantry optimization results", () => {
  it("preserves the accepted original, Pantry, remaining and retailer-comparison audit", () => {
    render(<WeeklyPlanComparisonResults preview={noComparable()} />);

    const original = screen.getByRole("region", { name: "Покупки на неделю" });
    expect(within(original).getByText("Молоко")).toBeDefined();
    expect(within(original).getByText("1000 MILLILITER")).toBeDefined();

    const audit = screen.getByRole("region", { name: "Учтено из запасов дома" });
    expect(within(audit).getByText("Частично покрыто")).toBeDefined();
    expect(within(audit).getByText("Из дома: 250 MILLILITER")).toBeDefined();

    const remaining = screen.getByRole("region", { name: "Осталось купить" });
    expect(within(remaining).getByText("750 MILLILITER")).toBeDefined();
    expect(within(remaining).queryByText("Яйца")).toBeNull();

    expect(screen.getByRole("heading", { level: 2, name: "Результат для Москва" })).toBeDefined();
  });

  it("keeps NO_REMAINING_DEMAND terminal and renders no optimization section", () => {
    render(<WeeklyPlanComparisonResults preview={zeroDemand} />);

    expect(screen.getByRole("heading", { level: 2, name: "Покупать ничего не нужно" })).toBeDefined();
    expect(screen.getByText("Запасы дома полностью покрывают недельный список.")).toBeDefined();
    expect(screen.queryByRole("heading", { name: "Стоимость оформления" })).toBeNull();
    expect(screen.queryByRole("heading", { name: /Результат для/ })).toBeNull();
  });

  it("shows NO_COMPARABLE_CANDIDATES without inventing a cheapest retailer", () => {
    render(<WeeklyPlanComparisonResults preview={noComparable()} />);

    const optimization = screen.getByRole("region", { name: "Стоимость оформления" });
    expect(within(optimization).getByRole("heading", { name: "Пока нельзя честно выбрать минимальную стоимость" })).toBeDefined();
    expect(within(optimization).getAllByText("Доставка: Неизвестно")).toHaveLength(2);
    expect(within(optimization).queryByText(/самый дешёвый/i)).toBeNull();
    expect(within(optimization).queryByText(/минимальная подтверждённая стоимость/i)).toBeNull();
  });

  it("renders UNIQUE_WINNER from server optimalRetailerIds instead of retailer-array order", () => {
    render(<WeeklyPlanComparisonResults preview={uniqueWinner()} />);

    const optimization = screen.getByRole("region", { name: "Стоимость оформления" });
    const summary = within(optimization).getByRole("group", { name: "Результат оптимизации" });
    expect(within(summary).getByRole("heading", { name: "Минимальная подтверждённая стоимость" })).toBeDefined();
    expect(within(summary).getByText("Перекрёсток")).toBeDefined();
    expect(within(summary).queryByText("Пятёрочка")).toBeNull();

    const perekrestok = within(optimization).getByRole("article", { name: "Стоимость оформления — Перекрёсток" });
    expect(within(perekrestok).getByText(/Товары:.*900/)).toBeDefined();
    expect(within(perekrestok).getByText(/Доставка:.*100/)).toBeDefined();
    expect(within(perekrestok).getByText(/Сервисный сбор:.*0/)).toBeDefined();
    expect(within(perekrestok).getByText(/Минимальный заказ:.*500/)).toBeDefined();
    expect(within(perekrestok).getByText("Минимум выполнен")).toBeDefined();
    expect(within(perekrestok).getByText(/Стоимость оформления:.*1.*000/)).toBeDefined();
    expect(within(perekrestok).getByText("Заказ доступен")).toBeDefined();
    expect(within(perekrestok).getByText("Можно сравнивать")).toBeDefined();
  });

  it("renders known zero as money and UNKNOWN as unknown without calculating either", () => {
    render(<WeeklyPlanComparisonResults preview={uniqueWinner()} />);

    const optimization = screen.getByRole("region", { name: "Стоимость оформления" });
    const pyaterochka = within(optimization).getByRole("article", { name: "Стоимость оформления — Пятёрочка" });
    expect(within(pyaterochka).getByText("Доставка: Неизвестно")).toBeDefined();
    expect(within(pyaterochka).getByText(/Сервисный сбор:.*0/)).toBeDefined();
    expect(within(pyaterochka).getByText("Стоимость оформления: Неизвестно")).toBeDefined();
    expect(within(pyaterochka).getByText("Нельзя включать в минимум")).toBeDefined();
  });

  it("renders every server-provided TIE winner in server order without a client tie-break", () => {
    render(<WeeklyPlanComparisonResults preview={tie()} />);

    const summary = screen.getByRole("group", { name: "Результат оптимизации" });
    expect(within(summary).getByRole("heading", { name: "Одинаковая минимальная стоимость" })).toBeDefined();
    const winners = within(summary).getAllByRole("listitem").map((item) => item.textContent);
    expect(winners).toEqual(["Перекрёсток", "Пятёрочка"]);
  });

  it("keeps retailers without an assessment visible", () => {
    const preview = wrapper({
      retailers: [unknownAssessment("pyaterochka"), { retailerId: "perekrestok" }],
      status: "NO_COMPARABLE_CANDIDATES",
      optimalRetailerIds: [],
    });
    render(<WeeklyPlanComparisonResults preview={preview} />);

    const perekrestok = screen.getByRole("article", { name: "Стоимость оформления — Перекрёсток" });
    expect(within(perekrestok).getByText("Расчёт оформления недоступен.")).toBeDefined();
  });

  it("fails closed on structural retailer or optimizer drift instead of partially rendering economics", () => {
    const mismatched: OptimizationPreview = {
      pantryComparisonPreview: pantryComparison,
      optimizationPreview: {
        retailers: [unknownAssessment("magnit")],
        status: "UNIQUE_WINNER",
        optimalRetailerIds: ["magnit"],
        lowestComparableCheckoutTotal: { amount: 1, currencyCode: "RUB" },
      },
    };
    render(<WeeklyPlanComparisonResults preview={mismatched} />);

    expect(screen.getByRole("alert").textContent).toContain("Не удалось показать стоимость оформления");
    expect(screen.queryByRole("heading", { name: "Стоимость оформления" })).toBeNull();
  });

  it("keeps generated identities, provider details and optimizer authority out of user-facing text", () => {
    const { container } = render(<WeeklyPlanComparisonResults preview={uniqueWinner()} />);
    const text = container.textContent ?? "";
    expect(text).not.toMatch(/[0-9a-f]{8}-[0-9a-f-]{27,}/i);
    expect(text).not.toContain("itemId");
    expect(text).not.toContain("occurrenceId");
    expect(text).not.toContain("recipeIngredientId");
    expect(text).not.toContain("acceptedOptimizerResult");
    expect(text).not.toContain("provider");
    expect(text).not.toContain("acquisition");
  });
});
