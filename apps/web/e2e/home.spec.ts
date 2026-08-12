import { expect, test, type Page } from "@playwright/test";

async function fillComparisonForm(page: Page) {
  await page.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill("Москва");
  await page.getByRole("textbox", { name: "Товар", exact: true }).fill("Молоко");
  await page.getByRole("spinbutton", { name: "Количество", exact: true }).fill("2");
  await page.getByRole("combobox", { name: "Единица", exact: true }).selectOption("LITER");

  await page.getByRole("button", { name: "Добавить товар" }).click();
  await page.getByRole("textbox", { name: "Товар", exact: true }).nth(1).fill("Яйца");
  await page.getByRole("spinbutton", { name: "Количество", exact: true }).nth(1).fill("10");
  await page.getByRole("combobox", { name: "Единица", exact: true }).nth(1).selectOption("PIECE");
}

test("runs the deterministic comparison preview journey without horizontal overflow", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeVisible();
  await expect(page.getByText(/M1 · Shopping Core/i)).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Сравнить корзину" })).toBeVisible();

  await fillComparisonForm(page);
  await page.getByRole("button", { name: "Сравнить корзину" }).click();

  await expect(page.getByRole("heading", { level: 2, name: "Результат для Москва" })).toBeVisible();
  const retailerList = page.getByRole("list", { name: "Сравнение магазинов" });
  await expect(retailerList.locator(":scope > li")).toHaveCount(8);

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
    await expect(page.getByRole("heading", { level: 3, name })).toBeVisible();
  }

  await expect(page.getByText("Корзина рассчитана")).toHaveCount(1);
  await expect(page.getByText("Есть неопределённость")).toHaveCount(1);
  await expect(page.getByText("Корзина неполная")).toHaveCount(4);
  await expect(page.getByText("Сравнение пока недоступно")).toHaveCount(2);

  const magnit = page.getByRole("article", { name: "Магнит" });
  await expect(magnit.getByText("Неизвестен размер упаковки", { exact: true })).toHaveCount(1);

  const lenta = page.getByRole("article", { name: "Лента" });
  await expect(lenta.getByText("Товар не найден", { exact: true })).toHaveCount(1);

  const body = await page.locator("body").innerText();
  expect(body).not.toContain("sourceProviderId");
  expect(body).not.toContain("acquisitionMode");
  expect(body).not.toContain("fulfillmentContextId");
  expect(body).not.toContain("sku-");
  expect(body).not.toMatch(/самый дешёвый|лучший выбор|рекомендуем/i);

  const overflowsHorizontally = await page.evaluate(
    () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
  );
  expect(overflowsHorizontally).toBe(false);
});

test("shows one accessible error and no fabricated results when the API is unavailable", async ({ page }) => {
  await page.goto("/");

  await page.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill("Недоступно");
  await page.getByRole("textbox", { name: "Товар", exact: true }).fill("Молоко");
  await page.getByRole("spinbutton", { name: "Количество", exact: true }).fill("1");
  await page.getByRole("button", { name: "Сравнить корзину" }).click();

  const serviceAlert = page.getByRole("alert").filter({
    hasText: "Не удалось выполнить сравнение. Основной сервис временно недоступен.",
  });
  await expect(serviceAlert).toHaveCount(1);
  await expect(page.getByRole("list", { name: "Сравнение магазинов" })).toHaveCount(0);
  await expect(page.getByRole("heading", { level: 3 })).toHaveCount(0);
});

test("comparison form has a visible keyboard focus path", async ({ page }) => {
  await page.goto("/");

  const locality = page.getByRole("textbox", { name: "Населённый пункт", exact: true });
  await page.keyboard.press("Tab");
  await expect(locality).toBeFocused();

  const focusIsVisible = await locality.evaluate((element) => {
    const style = window.getComputedStyle(element);
    return (
      (style.outlineStyle !== "none" && style.outlineWidth !== "0px") ||
      style.boxShadow !== "none"
    );
  });

  expect(focusIsVisible).toBe(true);
});
