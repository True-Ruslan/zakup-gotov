import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

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

describe("retailer coverage section", () => {
  it("renders every retailer and product-safe readiness language", () => {
    render(<RetailerCoverageSection state={readyState} />);

    expect(screen.getByRole("heading", { level: 2, name: "Покрытие магазинов" })).toBeDefined();
    expect(screen.getAllByRole("listitem")).toHaveLength(2);
    expect(screen.getByRole("heading", { level: 3, name: "Пятёрочка" })).toBeDefined();
    expect(screen.getByText("Источник подключён")).toBeDefined();
    expect(screen.getByText("Доступ к данным проверяется")).toBeDefined();
    expect(screen.getByRole("heading", { level: 3, name: "Чижик" })).toBeDefined();
    expect(screen.getByText("Интеграция в работе")).toBeDefined();
    expect(screen.queryByText(/fixture-provider|DIRECT_API|sourceReference/i)).toBeNull();
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
