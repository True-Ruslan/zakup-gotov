import { cleanup, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { ComparisonPreviewResults } from "./comparison-preview-results";

type Preview = components["schemas"]["ComparisonPreviewResponse"];
type Retailer = components["schemas"]["ComparisonPreviewRetailer"];

const unavailable = (id: Retailer["id"], displayName: string): Retailer => ({
  id,
  displayName,
  coverage: "DISCOVERY",
  productionAccess: "PENDING",
  comparisonStatus: "UNAVAILABLE",
  reasons: ["COVERAGE_DISCOVERY"],
  items: [],
});

const preview: Preview = {
  locality: "Москва",
  items: [
    {
      id: "c281d71c-2b27-46ef-a7af-3d624a7447cf",
      requirement: "Молоко",
      quantity: { amount: 2000, unit: "MILLILITER" },
    },
  ],
  retailers: [
    {
      id: "pyaterochka",
      displayName: "Пятёрочка",
      coverage: "CONNECTED",
      productionAccess: "READY",
      comparisonStatus: "READY",
      reasons: [],
      total: { amount: 200, currencyCode: "RUB" },
      freshness: { basis: "OBSERVATION_ONLY", observedAt: "2026-08-12T10:00:00Z" },
      items: [
        {
          id: "c281d71c-2b27-46ef-a7af-3d624a7447cf",
          requirement: "Молоко",
          requestedQuantity: { amount: 2000, unit: "MILLILITER" },
          status: "FULFILLED",
          candidateProductNames: ["Молоко"],
          selection: {
            productName: "Молоко",
            packageQuantity: { amount: 1000, unit: "MILLILITER" },
            packageCount: 2,
            coveredQuantity: { amount: 2000, unit: "MILLILITER" },
            lineTotal: 200,
            currencyCode: "RUB",
          },
        },
      ],
    },
    {
      id: "perekrestok",
      displayName: "Перекрёсток",
      coverage: "CONNECTED",
      productionAccess: "READY",
      comparisonStatus: "UNCERTAIN",
      reasons: ["AVAILABILITY_UNKNOWN"],
      total: { amount: 210, currencyCode: "RUB" },
      freshness: {
        basis: "PROVIDER_TIMESTAMP",
        observedAt: "2026-08-12T10:00:00Z",
        providerUpdatedAt: "2026-08-12T09:55:00Z",
      },
      items: [],
    },
    {
      id: "chizhik",
      displayName: "Чижик",
      coverage: "CONNECTED",
      productionAccess: "READY",
      comparisonStatus: "INCOMPLETE",
      reasons: ["ITEM_UNMATCHED"],
      items: [
        {
          id: "c281d71c-2b27-46ef-a7af-3d624a7447cf",
          requirement: "Молоко",
          requestedQuantity: { amount: 2000, unit: "MILLILITER" },
          status: "UNMATCHED",
          candidateProductNames: [],
        },
      ],
    },
    unavailable("magnit", "Магнит"),
    unavailable("lenta", "Лента"),
    unavailable("vkusvill", "ВкусВилл"),
    unavailable("ozon-fresh", "Ozon Fresh"),
    unavailable("samokat", "Самокат"),
  ],
};

afterEach(cleanup);

describe("comparison preview results", () => {
  it("keeps all retailers visible and shows totals only where supplied", () => {
    render(<ComparisonPreviewResults preview={preview} />);

    const list = screen.getByRole("list", { name: "Сравнение магазинов" });
    expect(list.children).toHaveLength(8);
    for (const name of [
      "Пятёрочка",
      "Перекрёсток",
      "Чижик",
      "Магнит",
      "Лента",
      "ВкусВилл",
      "Ozon Fresh",
      "Самокат",
    ]) {
      expect(screen.getByRole("heading", { level: 3, name })).toBeDefined();
    }

    const pyaterochka = screen.getByRole("article", { name: "Пятёрочка" });
    expect(within(pyaterochka).getByText(/200/)).toBeDefined();
    expect(within(pyaterochka).getByText("Молоко")).toBeDefined();
    expect(within(pyaterochka).getByText(/2 упак/)).toBeDefined();

    const magnit = screen.getByRole("article", { name: "Магнит" });
    expect(within(magnit).queryByText(/₽/)).toBeNull();
  });

  it("shows uncertainty, item-level gaps and explicit freshness evidence without a winner", () => {
    render(<ComparisonPreviewResults preview={preview} />);

    expect(screen.getByText("Есть неопределённость")).toBeDefined();
    expect(screen.getByText("Товар не найден")).toBeDefined();
    expect(screen.getByText(/Источник не сообщает отдельное время обновления/)).toBeDefined();
    expect(screen.getByText(/Обновлено источником/)).toBeDefined();
    expect(screen.queryByText(/самый дешёвый|лучший выбор|рекомендуем/i)).toBeNull();
  });
});
