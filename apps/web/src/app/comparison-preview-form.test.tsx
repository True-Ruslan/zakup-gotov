import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { createComparisonPreview } from "./comparison-preview";
import { ComparisonPreviewForm } from "./comparison-preview-form";

vi.mock("./comparison-preview", () => ({
  createComparisonPreview: vi.fn(),
}));

const mockedCreateComparisonPreview = vi.mocked(createComparisonPreview);

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("comparison preview form", () => {
  it("supports adding and removing shopping rows without allowing an empty list", () => {
    render(<ComparisonPreviewForm />);

    expect(screen.getAllByLabelText("Товар")).toHaveLength(1);
    expect((screen.getByRole("button", { name: "Удалить товар" }) as HTMLButtonElement).disabled).toBe(true);

    fireEvent.click(screen.getByRole("button", { name: "Добавить товар" }));
    expect(screen.getAllByLabelText("Товар")).toHaveLength(2);
    expect((screen.getAllByRole("button", { name: "Удалить товар" })[0] as HTMLButtonElement).disabled).toBe(false);

    fireEvent.click(screen.getAllByRole("button", { name: "Удалить товар" })[1]!);
    expect(screen.getAllByLabelText("Товар")).toHaveLength(1);
    expect((screen.getByRole("button", { name: "Удалить товар" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("submits locality and nested quantity through the typed transport", async () => {
    mockedCreateComparisonPreview.mockResolvedValue({
      kind: "ready",
      data: { locality: "Москва", items: [], retailers: [] },
    });
    render(<ComparisonPreviewForm />);

    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Москва" } });
    fireEvent.change(screen.getByLabelText("Товар"), { target: { value: "Молоко" } });
    fireEvent.change(screen.getByLabelText("Количество"), { target: { value: "2" } });
    fireEvent.change(screen.getByLabelText("Единица"), { target: { value: "LITER" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить корзину" }));

    await waitFor(() => expect(mockedCreateComparisonPreview).toHaveBeenCalledTimes(1));
    expect(mockedCreateComparisonPreview).toHaveBeenCalledWith({
      locality: "Москва",
      items: [
        {
          id: expect.any(String),
          requirement: "Молоко",
          quantity: { amount: 2, unit: "LITER" },
        },
      ],
    });
  });

  it("shows client validation errors accessibly before making a request", () => {
    render(<ComparisonPreviewForm />);

    fireEvent.click(screen.getByRole("button", { name: "Сравнить корзину" }));

    expect(screen.getByRole("alert").textContent).toContain("Укажите населённый пункт");
    expect(mockedCreateComparisonPreview).not.toHaveBeenCalled();
  });

  it("shows backend validation errors without exposing transport details", async () => {
    mockedCreateComparisonPreview.mockResolvedValue({
      kind: "invalid",
      errors: [{ field: "items[0].quantity.amount", message: "must be greater than 0" }],
    });
    render(<ComparisonPreviewForm />);

    fireEvent.change(screen.getByLabelText("Населённый пункт"), { target: { value: "Москва" } });
    fireEvent.change(screen.getByLabelText("Товар"), { target: { value: "Молоко" } });
    fireEvent.change(screen.getByLabelText("Количество"), { target: { value: "2" } });
    fireEvent.click(screen.getByRole("button", { name: "Сравнить корзину" }));

    await screen.findByRole("alert");
    expect(screen.getByRole("alert").textContent).toContain("items[0].quantity.amount");
    expect(screen.getByRole("alert").textContent).not.toContain("sourceProviderId");
  });
});
