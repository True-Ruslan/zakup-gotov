import { expect, test, type Page } from "@playwright/test";

async function fillComparisonForm(page: Page) {
  await page.getByLabel("Населённый пункт").fill("Москва");
  await page.getByLabel("Товар").fill("Молоко");
  await page.getByLabel("Количество").fill("2");
  await page.getByLabel("Единица").selectOption("LITER");

  await page.getByRole("button", { name: "Добавить товар" }).click();
  await page.getByLabel("Товар").nth(1).fill("Яйца");
  await page.getByLabel("Количество").nth(1).fill("10");
  await page.getByLabel("Единица").nth(1).selectOption("PIECE");
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

  await expect(page.getByText("Корзина рассчитана")).toBeVisible();
  await expect(page.getByText("Есть неопределённость")).toBeVisible();
  await expect(page.getByText("Корзина неполная")).toBeVisible();
  await expect(page.getByText("Сравнение пока недоступно")).toBeVisible();
  await expect(page.getByText("Неизвестен размер упаковки")).toBeVisible();
  await expect(page.getByText("Товар не найден")).toBeVisible();

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

  await page.getByLabel("Населённый пункт").fill("Недоступно");
  await page.getByLabel("Товар").fill("Молоко");
  await page.getByLabel("Количество").fill("1");
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

  const locality = page.getByLabel("Населённый пункт");
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
