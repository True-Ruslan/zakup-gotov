import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import { RetailerCoverageSection } from "./retailer-coverage";
import type { RetailerReadinessState } from "./retailer-readiness";

const readyState: RetailerReadinessState = {
  kind: "ready",
  data: {
    retailers: [
      {
        id: "pyaterochka",
        displayName: "Пятёрочка",
        coverage: "CONNECTED",
        productionAccess: "PENDING",
        comparisonStatus: "UNAVAILABLE",
        reasons: ["PRODUCTION_ACCESS_PENDING"],
      },
      {
        id: "chizhik",
        displayName: "Чижик",
        coverage: "DISCOVERY",
        productionAccess: "PENDING",
        comparisonStatus: "UNAVAILABLE",
        reasons: ["COVERAGE_DISCOVERY"],
      },
    ],
  },
};

const freshnessState: RetailerReadinessState = {
  kind: "ready",
  data: {
    retailers: [
      {
        id: "pyaterochka",
        displayName: "Пятёрочка",
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "READY",
        reasons: [],
        total: { amount: 123.45, currencyCode: "RUB" },
        freshness: {
          basis: "OBSERVATION_ONLY",
          observedAt: "2026-08-12T10:00:00Z",
        },
      },
      {
        id: "perekrestok",
        displayName: "Перекрёсток",
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "READY",
        reasons: [],
        total: { amount: 234.56, currencyCode: "RUB" },
        freshness: {
          basis: "PROVIDER_TIMESTAMP",
          observedAt: "2026-08-12T10:05:00Z",
          providerUpdatedAt: "2026-08-12T09:55:00Z",
        },
      },
    ],
  },
};

afterEach(() => cleanup());

describe("retailer coverage section", () => {
  it("renders every retailer and product-safe readiness language", () => {
    render(<RetailerCoverageSection state={readyState} />);

    expect(screen.getByRole("heading", { level: 2, name: "Покрытие магазинов" })).toBeDefined();
    expect(screen.getByRole("list", { name: "Статус магазинов" }).children).toHaveLength(2);
    expect(screen.getByRole("heading", { level: 3, name: "Пятёрочка" })).toBeDefined();
    expect(screen.getByText("Источник подключён")).toBeDefined();
    expect(screen.getAllByText("Доступ к данным проверяется")).toHaveLength(2);
    expect(screen.getByRole("heading", { level: 3, name: "Чижик" })).toBeDefined();
    expect(screen.getByText("Интеграция в работе")).toBeDefined();
    expect(screen.queryByText(/fixture-provider|DIRECT_API|sourceReference/i)).toBeNull();
  });

  it("distinguishes observation-only and provider timestamp freshness without invented age labels", () => {
    render(<RetailerCoverageSection state={freshnessState} />);

    expect(screen.getByText("Последнее наблюдение: 2026-08-12T10:00:00Z")).toBeDefined();
    expect(screen.getByText("Источник не сообщает отдельное время обновления.")).toBeDefined();
    expect(screen.getByText("Последнее наблюдение: 2026-08-12T10:05:00Z")).toBeDefined();
    expect(screen.getByText("Обновлено источником: 2026-08-12T09:55:00Z")).toBeDefined();
    expect(screen.queryByText(/свеж|устар/i)).toBeNull();
  });

  it("renders an accessible service error without fabricated retailer cards", () => {
    render(<RetailerCoverageSection state={{ kind: "unavailable" }} />);

    expect(screen.getByRole("alert").textContent).toMatch(
      /не удалось загрузить статус магазинов\. основной сервис временно недоступен\./i,
    );
    expect(screen.queryByRole("list")).toBeNull();
    expect(screen.queryByRole("heading", { level: 3 })).toBeNull();
  });
});
