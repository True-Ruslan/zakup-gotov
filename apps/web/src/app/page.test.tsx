import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import Home from "./page";
import { loadRetailerReadiness } from "./retailer-readiness";

vi.mock("./retailer-readiness", () => ({
  loadRetailerReadiness: vi.fn(),
}));

const mockedLoadRetailerReadiness = vi.mocked(loadRetailerReadiness);

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("home page", () => {
  it("renders the M1 product shell and retailer coverage from the read model", async () => {
    mockedLoadRetailerReadiness.mockResolvedValue({
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
        ],
      },
    });

    render(await Home());

    expect(screen.getAllByRole("heading", { level: 1 })).toHaveLength(1);
    expect(screen.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeDefined();
    expect(screen.getByText(/M1 · Shopping Core/i)).toBeDefined();
    expect(screen.queryByText(/M0 · Product & Integration Discovery/i)).toBeNull();
    expect(screen.getByRole("heading", { level: 2, name: "Покрытие магазинов" })).toBeDefined();
    expect(screen.getByRole("heading", { level: 3, name: "Пятёрочка" })).toBeDefined();
    expect(mockedLoadRetailerReadiness).toHaveBeenCalledTimes(1);

    const documentation = screen.getByRole("link", { name: "Документация проекта" });
    expect(documentation.getAttribute("href")).toBe(
      "https://github.com/True-Ruslan/zakup-gotov#readme",
    );
  });
});
