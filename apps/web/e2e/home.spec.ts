import { expect, test, type Locator, type Page } from "@playwright/test";

function recipeForm(page: Page): Locator {
  return page.locator("section[aria-labelledby='recipe-comparison']");
}

function manualForm(page: Page): Locator {
  return page.locator("section[aria-labelledby='comparison-preview']");
}

async function fillRecipeForm(page: Page, locality = "Москва") {
  const form = recipeForm(page);
  await form.getByRole("textbox", { name: "Название рецепта", exact: true }).fill("Блины");
  await form.getByRole("spinbutton", { name: "Порций в рецепте", exact: true }).fill("2");
  await form.getByRole("spinbutton", { name: "Нужно порций", exact: true }).fill("4");
  await form.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill(locality);
  await form.getByRole("textbox", { name: "Ингредиент", exact: true }).fill("Молоко");
  await form.getByRole("spinbutton", { name: "Количество", exact: true }).fill("0.5");
  await form.getByRole("combobox", { name: "Единица", exact: true }).selectOption("LITER");

  await form.getByRole("button", { name: "Добавить ингредиент" }).click();
  await form.getByRole("textbox", { name: "Ингредиент", exact: true }).nth(1).fill("Яйца");
  await form.getByRole("spinbutton", { name: "Количество", exact: true }).nth(1).fill("5");
  await form.getByRole("combobox", { name: "Единица", exact: true }).nth(1).selectOption("PIECE");
}

async function fillManualComparisonForm(page: Page) {
  const form = manualForm(page);
  await form.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill("Москва");
  await form.getByRole("textbox", { name: "Товар", exact: true }).fill("Молоко");
  await form.getByRole("spinbutton", { name: "Количество", exact: true }).fill("2");
  await form.getByRole("combobox", { name: "Единица", exact: true }).selectOption("LITER");

  await form.getByRole("button", { name: "Добавить товар" }).click();
  await form.getByRole("textbox", { name: "Товар", exact: true }).nth(1).fill("Яйца");
  await form.getByRole("spinbutton", { name: "Количество", exact: true }).nth(1).fill("10");
  await form.getByRole("combobox", { name: "Единица", exact: true }).nth(1).selectOption("PIECE");
}

async function expectSafeComparisonResult(page: Page) {
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

  const body = await page.locator("body").innerText();
  expect(body).not.toContain("sourceProviderId");
  expect(body).not.toContain("acquisitionMode");
  expect(body).not.toContain("fulfillmentContextId");
  expect(body).not.toContain("sku-");
  expect(body).not.toMatch(/самый дешёвый|лучший выбор|рекомендуем/i);
}

test("runs the desktop Recipe → shopping list → retailer comparison journey", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeVisible();
  await expect(page.getByText(/M2 · Recipes/i)).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Сравнить рецепт" })).toBeVisible();

  await fillRecipeForm(page);
  await recipeForm(page).getByRole("button", { name: "Сравнить рецепт" }).click();

  await expect(page.getByRole("heading", { level: 2, name: "Список покупок из рецепта" })).toBeVisible();
  const shoppingList = page.getByRole("list", { name: "Покупки из рецепта" });
  await expect(shoppingList.getByText("Молоко", { exact: true })).toBeVisible();
  await expect(shoppingList.getByText("1000 MILLILITER", { exact: true })).toBeVisible();
  await expect(shoppingList.getByText("Яйца", { exact: true })).toBeVisible();
  await expect(shoppingList.getByText("10 PIECE", { exact: true })).toBeVisible();
  await expectSafeComparisonResult(page);

  const overflowsHorizontally = await page.evaluate(
    () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
  );
  expect(overflowsHorizontally).toBe(false);
});

test("Recipe journey remains usable without horizontal overflow on mobile", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");

  await fillRecipeForm(page);
  await recipeForm(page).getByRole("button", { name: "Сравнить рецепт" }).click();

  await expect(page.getByRole("heading", { level: 2, name: "Список покупок из рецепта" })).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Результат для Москва" })).toBeVisible();
  const dimensions = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    clientWidth: document.documentElement.clientWidth,
  }));
  expect(dimensions.scrollWidth).toBeLessThanOrEqual(dimensions.clientWidth);
});

test("Recipe journey shows one accessible error and no fabricated results when the API is unavailable", async ({ page }) => {
  await page.goto("/");

  await fillRecipeForm(page, "Недоступно");
  await recipeForm(page).getByRole("button", { name: "Сравнить рецепт" }).click();

  const serviceAlert = page.getByRole("alert").filter({
    hasText: "Не удалось сравнить рецепт. Основной сервис временно недоступен.",
  });
  await expect(serviceAlert).toHaveCount(1);
  await expect(page.getByRole("list", { name: "Покупки из рецепта" })).toHaveCount(0);
  await expect(page.getByRole("list", { name: "Сравнение магазинов" })).toHaveCount(0);
});

test("Recipe form has a visible keyboard focus path", async ({ page }) => {
  await page.goto("/");

  const title = recipeForm(page).getByRole("textbox", { name: "Название рецепта", exact: true });
  await page.keyboard.press("Tab");
  await expect(title).toBeFocused();

  const focusIsVisible = await title.evaluate((element) => {
    const style = window.getComputedStyle(element);
    return (
      (style.outlineStyle !== "none" && style.outlineWidth !== "0px") ||
      style.boxShadow !== "none"
    );
  });
  expect(focusIsVisible).toBe(true);
});

test("preserves the deterministic manual comparison journey as a secondary path", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { level: 2, name: "Сравнить корзину" })).toBeVisible();
  await fillManualComparisonForm(page);
  await manualForm(page).getByRole("button", { name: "Сравнить корзину" }).click();

  await expectSafeComparisonResult(page);
  await expect(page.getByText("Корзина рассчитана")).toHaveCount(1);
  await expect(page.getByText("Есть неопределённость")).toHaveCount(1);
  await expect(page.getByText("Корзина неполная")).toHaveCount(4);
  await expect(page.getByText("Сравнение пока недоступно")).toHaveCount(2);

  const magnit = page.getByRole("article", { name: "Магнит" });
  await expect(magnit.getByText("Неизвестен размер упаковки", { exact: true })).toHaveCount(1);
  const lenta = page.getByRole("article", { name: "Лента" });
  await expect(lenta.getByText("Товар не найден", { exact: true })).toHaveCount(1);
});
