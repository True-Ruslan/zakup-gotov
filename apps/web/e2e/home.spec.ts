import { expect, test, type Locator, type Page } from "@playwright/test";

function weeklyForm(page: Page): Locator {
  return page.locator("section[aria-labelledby='weekly-plan-comparison']");
}

function recipeForm(page: Page): Locator {
  return page.locator("section[aria-labelledby='recipe-comparison']");
}

function manualForm(page: Page): Locator {
  return page.locator("section[aria-labelledby='comparison-preview']");
}

async function fillWeeklyPlan(page: Page, locality = "Москва") {
  const form = weeklyForm(page);
  await form.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill(locality);

  const first = form.getByRole("group", { name: "Блюдо 1", exact: true });
  await first.getByRole("combobox", { name: "День", exact: true }).selectOption("MONDAY");
  await first.getByRole("spinbutton", { name: "Нужно порций", exact: true }).fill("4");
  await first.getByRole("textbox", { name: "Название рецепта", exact: true }).fill("Каша");
  await first.getByRole("spinbutton", { name: "Порций в рецепте", exact: true }).fill("2");
  await first.getByRole("textbox", { name: "Ингредиент", exact: true }).fill("Молоко");
  await first.getByRole("spinbutton", { name: "Количество", exact: true }).fill("0.5");
  await first.getByRole("combobox", { name: "Единица", exact: true }).selectOption("LITER");

  await form.getByRole("button", { name: "Добавить блюдо" }).click();
  const second = form.getByRole("group", { name: "Блюдо 2", exact: true });
  await second.getByRole("combobox", { name: "День", exact: true }).selectOption("SUNDAY");
  await second.getByRole("spinbutton", { name: "Нужно порций", exact: true }).fill("4");
  await second.getByRole("textbox", { name: "Название рецепта", exact: true }).fill("Омлет");
  await second.getByRole("spinbutton", { name: "Порций в рецепте", exact: true }).fill("2");
  await second.getByRole("textbox", { name: "Ингредиент", exact: true }).fill("Яйца");
  await second.getByRole("spinbutton", { name: "Количество", exact: true }).fill("5");
  await second.getByRole("combobox", { name: "Единица", exact: true }).selectOption("PIECE");
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
  await expect(page.getByRole("heading", { level: 2, name: "Результат для Москва" }).last()).toBeVisible();
  const retailerList = page.getByRole("list", { name: "Сравнение магазинов" }).last();
  await expect(retailerList.locator(":scope > li")).toHaveCount(8);
  for (const name of ["Пятёрочка", "Перекрёсток", "Чижик", "Магнит", "Лента", "ВкусВилл", "Ozon Fresh", "Самокат"]) {
    await expect(retailerList.getByRole("heading", { level: 3, name })).toBeVisible();
  }
  const body = await page.locator("body").innerText();
  expect(body).not.toContain("sourceProviderId");
  expect(body).not.toContain("acquisitionMode");
  expect(body).not.toContain("fulfillmentContextId");
  expect(body).not.toContain("sku-");
  expect(body).not.toMatch(/самый дешёвый|лучший выбор|рекомендуем/i);
}

test("runs desktop WeeklyPlan → weekly shopping → retailer comparison", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByText(/M3 · Weekly Planning/i)).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Собрать неделю" })).toBeVisible();

  await fillWeeklyPlan(page);
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();

  await expect(page.getByRole("heading", { level: 2, name: "Покупки на неделю" })).toBeVisible();
  const shopping = page.getByRole("list", { name: "Покупки на неделю" });
  await expect(shopping.getByText("Молоко", { exact: true })).toBeVisible();
  await expect(shopping.getByText("1000 MILLILITER", { exact: true })).toBeVisible();
  await expect(shopping.getByText("Яйца", { exact: true })).toBeVisible();
  await expect(shopping.getByText("10 PIECE", { exact: true })).toBeVisible();
  await expectSafeComparisonResult(page);

  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false);
});

test("keeps explicit occurrence order independent from day metadata", async ({ page }) => {
  await page.goto("/");
  await fillWeeklyPlan(page);
  const form = weeklyForm(page);
  await form.getByRole("button", { name: "Переместить блюдо 2 выше" }).click();
  const first = form.getByRole("group", { name: "Блюдо 1", exact: true });
  await expect(first.getByRole("textbox", { name: "Название рецепта", exact: true })).toHaveValue("Омлет");
  await expect(first.getByRole("combobox", { name: "День", exact: true })).toHaveValue("SUNDAY");
});

test("WeeklyPlan journey remains usable without horizontal overflow on mobile", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");
  await fillWeeklyPlan(page);
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();
  await expect(page.getByRole("heading", { level: 2, name: "Покупки на неделю" })).toBeVisible();
  const dimensions = await page.evaluate(() => ({ scrollWidth: document.documentElement.scrollWidth, clientWidth: document.documentElement.clientWidth }));
  expect(dimensions.scrollWidth).toBeLessThanOrEqual(dimensions.clientWidth);
});

test("WeeklyPlan journey fails closed when API is unavailable", async ({ page }) => {
  await page.goto("/");
  await fillWeeklyPlan(page, "Недоступно");
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();
  await expect(page.getByRole("alert").filter({ hasText: "Не удалось сравнить недельный план" })).toHaveCount(1);
  await expect(page.getByRole("list", { name: "Покупки на неделю" })).toHaveCount(0);
});

test("WeeklyPlan form exposes a visible keyboard focus path", async ({ page }) => {
  await page.goto("/");
  const locality = weeklyForm(page).getByRole("textbox", { name: "Населённый пункт", exact: true });
  await page.keyboard.press("Tab");
  await expect(locality).toBeFocused();
  const visible = await locality.evaluate((element) => {
    const style = window.getComputedStyle(element);
    return (style.outlineStyle !== "none" && style.outlineWidth !== "0px") || style.boxShadow !== "none";
  });
  expect(visible).toBe(true);
});

test("preserves Recipe → shopping list → retailer comparison as a secondary journey", async ({ page }) => {
  await page.goto("/");
  await fillRecipeForm(page);
  await recipeForm(page).getByRole("button", { name: "Сравнить рецепт" }).click();
  await expect(page.getByRole("heading", { level: 2, name: "Список покупок из рецепта" })).toBeVisible();
  const shoppingList = page.getByRole("list", { name: "Покупки из рецепта" });
  await expect(shoppingList.getByText("1000 MILLILITER", { exact: true })).toBeVisible();
  await expect(shoppingList.getByText("10 PIECE", { exact: true })).toBeVisible();
});

test("Recipe unavailable state remains fail-closed", async ({ page }) => {
  await page.goto("/");
  await fillRecipeForm(page, "Недоступно");
  await recipeForm(page).getByRole("button", { name: "Сравнить рецепт" }).click();
  await expect(page.getByRole("alert").filter({ hasText: "Не удалось сравнить рецепт" })).toHaveCount(1);
});

test("preserves deterministic manual comparison as a secondary path", async ({ page }) => {
  await page.goto("/");
  await fillManualComparisonForm(page);
  await manualForm(page).getByRole("button", { name: "Сравнить корзину" }).click();
  await expectSafeComparisonResult(page);
  await expect(page.getByText("Корзина рассчитана")).toHaveCount(1);
  await expect(page.getByText("Есть неопределённость")).toHaveCount(1);
  await expect(page.getByText("Корзина неполная")).toHaveCount(4);
  await expect(page.getByText("Сравнение пока недоступно")).toHaveCount(2);
  const magnit = page.getByRole("article", { name: "Магнит" }).last();
  await expect(magnit.getByText("Неизвестен размер упаковки", { exact: true })).toHaveCount(1);
  const lenta = page.getByRole("article", { name: "Лента" }).last();
  await expect(lenta.getByText("Товар не найден", { exact: true })).toHaveCount(1);
});
