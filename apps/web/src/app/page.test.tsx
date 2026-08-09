import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import Home from "./page";

describe("home page", () => {
  it("describes the current product state without pretending retailer comparison is ready", () => {
    render(<Home />);

    expect(screen.getAllByRole("heading", { level: 1 })).toHaveLength(1);
    expect(screen.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeDefined();
    expect(screen.getByText(/проверяем интеграции с магазинами/i)).toBeDefined();

    const documentation = screen.getByRole("link", { name: "Документация проекта" });
    expect(documentation.getAttribute("href")).toBe(
      "https://github.com/True-Ruslan/zakup-gotov#readme",
    );
  });
});
