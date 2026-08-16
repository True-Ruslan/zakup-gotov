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

async function setWeeklyScenario(page: Page, title: string) {
  const first = weeklyForm(page).getByRole("group", { name: "Блюдо 1", exact: true });
  await first.getByRole("textbox", { name: "Название рецепта", exact: true }).fill(title);
}

async function addPantryRow(
  page: Page,
  requirement: string,
  amount: string,
  unit: "PIECE" | "GRAM" | "KILOGRAM" | "MILLILITER" | "LITER",
) {
  const form = weeklyForm(page);
  await form.getByRole("button", { name: "Добавить запас" }).click();
  const groups = form.getByRole("group", { name: /Запас дома/ });
  const row = groups.nth((await groups.count()) - 1);
  await row.getByRole("textbox", { name: "Продукт дома", exact: true }).fill(requirement);
  await row.getByRole("spinbutton", { name: "Количество дома", exact: true }).fill(amount);
  await row.getByRole("combobox", { name: "Единица дома", exact: true }).selectOption(unit);
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
  expect(body).not.toContain("acceptedOptimizerResult");
  expect(body).not.toMatch(/самый дешёвый|лучший выбор|рекомендуем/i);
}

test("runs desktop WeeklyPlan → Pantry → comparison → truthful no-comparable optimization", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByText("M4 · Basket Optimization", { exact: true })).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Собрать неделю" })).toBeVisible();

  await fillWeeklyPlan(page);
  await addPantryRow(page, "Молоко", "250", "MILLILITER");
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();

  const original = page.getByRole("region", { name: "Покупки на неделю" });
  await expect(original.getByText("Молоко", { exact: true })).toBeVisible();
  await expect(original.getByText("1000 MILLILITER", { exact: true })).toBeVisible();
  await expect(original.getByText("Яйца", { exact: true })).toBeVisible();
  await expect(original.getByText("10 PIECE", { exact: true })).toBeVisible();

  const audit = page.getByRole("region", { name: "Учтено из запасов дома" });
  await expect(audit.getByText("Частично покрыто", { exact: true })).toBeVisible();
  await expect(audit.getByText("Из дома: 250 MILLILITER", { exact: true })).toBeVisible();
  await expect(audit.getByText("Осталось: 750 MILLILITER", { exact: true })).toBeVisible();

  const remaining = page.getByRole("list", { name: "Осталось купить" });
  await expect(remaining.getByText("750 MILLILITER", { exact: true })).toBeVisible();
  await expect(remaining.getByText("10 PIECE", { exact: true })).toBeVisible();
  await expectSafeComparisonResult(page);

  const optimization = page.getByRole("region", { name: "Стоимость оформления" });
  await expect(optimization).toBeVisible();
  await expect(optimization.getByRole("heading", { name: "Пока нельзя честно выбрать минимальную стоимость" })).toBeVisible();
  await expect(optimization.getByText("Доставка: Неизвестно").first()).toBeVisible();
  await expect(optimization.getByText("Нельзя включать в минимум").first()).toBeVisible();
  await expect(optimization.getByText(/минимальная подтверждённая стоимость/i)).toHaveCount(0);

  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false);
});

test("renders server-owned unique winner without using retailer array order", async ({ page }) => {
  await page.goto("/");
  await fillWeeklyPlan(page);
  await setWeeklyScenario(page, "Уникальный победитель");
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();

  const summary = page.getByRole("group", { name: "Результат оптимизации" });
  await expect(summary.getByRole("heading", { name: "Минимальная подтверждённая стоимость" })).toBeVisible();
  await expect(summary.getByText("Перекрёсток", { exact: true })).toBeVisible();
  await expect(summary.getByText("Пятёрочка", { exact: true })).toHaveCount(0);

  const pyaterochka = page.getByRole("article", { name: "Стоимость оформления — Пятёрочка" });
  await expect(pyaterochka.getByText("Доставка: Неизвестно", { exact: true })).toBeVisible();
  await expect(pyaterochka.getByText(/Сервисный сбор:.*0/)).toBeVisible();

  const perekrestok = page.getByRole("article", { name: "Стоимость оформления — Перекрёсток" });
  await expect(perekrestok.getByText("Заказ доступен", { exact: true })).toBeVisible();
  await expect(perekrestok.getByText("Можно сравнивать", { exact: true })).toBeVisible();
});

test("renders every exact server tie winner in server order", async ({ page }) => {
  await page.goto("/");
  await fillWeeklyPlan(page);
  await setWeeklyScenario(page, "Точная ничья");
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();

  const summary = page.getByRole("group", { name: "Результат оптимизации" });
  await expect(summary.getByRole("heading", { name: "Одинаковая минимальная стоимость" })).toBeVisible();
  await expect(summary.getByRole("listitem")).toHaveText(["Перекрёсток", "Пятёрочка"]);
});

test("renders explicit zero-demand state when Pantry covers the whole week", async ({ page }) => {
  await page.goto("/");
  await fillWeeklyPlan(page);
  await addPantryRow(page, "Молоко", "1", "LITER");
  await addPantryRow(page, "Яйца", "10", "PIECE");
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();

  await expect(page.getByRole("heading", { level: 2, name: "Покупать ничего не нужно" })).toBeVisible();
  await expect(page.getByText("Запасы дома полностью покрывают недельный список.")).toBeVisible();
  await expect(page.getByRole("list", { name: "Сравнение магазинов" })).toHaveCount(0);
  await expect(page.getByRole("heading", { name: /Результат для/ })).toHaveCount(0);
  await expect(page.getByRole("heading", { name: "Стоимость оформления" })).toHaveCount(0);
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

test("WeeklyPlan optimization journey remains usable without horizontal overflow on mobile", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");
  await fillWeeklyPlan(page);
  await addPantryRow(page, "Молоко", "250", "MILLILITER");
  await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();
  await expect(page.getByRole("heading", { level: 2, name: "Учтено из запасов дома" })).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Стоимость оформления" })).toBeVisible();
  await expect(page.getByText("Пока нельзя честно выбрать минимальную стоимость")).toBeVisible();
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

test("WeeklyPlan Pantry controls expose a visible keyboard focus path", async ({ page }) => {
  await page.goto("/");
  const form = weeklyForm(page);
  const locality = form.getByRole("textbox", { name: "Населённый пункт", exact: true });
  await page.keyboard.press("Tab");
  await expect(locality).toBeFocused();
  const localityVisible = await locality.evaluate((element) => {
    const style = window.getComputedStyle(element);
    return (style.outlineStyle !== "none" && style.outlineWidth !== "0px") || style.boxShadow !== "none";
  });
  expect(localityVisible).toBe(true);

  const addPantry = form.getByRole("button", { name: "Добавить запас" });
  await addPantry.focus();
  await expect(addPantry).toBeFocused();
  await page.keyboard.press("Enter");
  await expect(form.getByRole("group", { name: "Запас дома 1" })).toBeVisible();
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
