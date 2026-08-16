import { expect, test, type Locator, type Page } from "@playwright/test";

const DRAFT_STORAGE_KEY = "zakup-gotov.weekly-plan-draft.v1";

function weeklyForm(page: Page): Locator {
  return page.locator("section[aria-labelledby='weekly-plan-comparison']");
}

async function fillDraft(page: Page) {
  const form = weeklyForm(page);
  await form.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill("Москва");

  const first = form.getByRole("group", { name: "Блюдо 1", exact: true });
  await first.getByRole("combobox", { name: "День", exact: true }).selectOption("TUESDAY");
  await first.getByRole("spinbutton", { name: "Нужно порций", exact: true }).fill("3");
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

  await form.getByRole("button", { name: "Переместить блюдо 2 выше" }).click();
  await form.getByRole("button", { name: "Добавить запас" }).click();
  const pantry = form.getByRole("group", { name: "Запас дома 1", exact: true });
  await pantry.getByRole("textbox", { name: "Продукт дома", exact: true }).fill("Молоко");
  await pantry.getByRole("spinbutton", { name: "Количество дома", exact: true }).fill("250");
  await pantry.getByRole("combobox", { name: "Единица дома", exact: true }).selectOption("MILLILITER");
}

test("persists only the private WeeklyPlan input draft across reload and clears it explicitly", async ({ page }) => {
  await page.goto("/");
  const form = weeklyForm(page);

  await expect(
    form.getByText("Черновик сохраняется только в этом браузере и не синхронизируется с аккаунтом или сервером."),
  ).toBeVisible();

  await fillDraft(page);

  await expect
    .poll(async () => page.evaluate((key) => window.localStorage.getItem(key), DRAFT_STORAGE_KEY))
    .not.toBeNull();

  const stored = await page.evaluate((key) => JSON.parse(window.localStorage.getItem(key) ?? "null"), DRAFT_STORAGE_KEY);
  expect(stored).toEqual({
    version: 1,
    locality: "Москва",
    occurrences: [
      {
        day: "SUNDAY",
        targetServings: "4",
        title: "Омлет",
        baseServings: "2",
        ingredients: [{ requirement: "Яйца", amount: "5", unit: "PIECE" }],
      },
      {
        day: "TUESDAY",
        targetServings: "3",
        title: "Каша",
        baseServings: "2",
        ingredients: [{ requirement: "Молоко", amount: "0.5", unit: "LITER" }],
      },
    ],
    pantry: [{ requirement: "Молоко", amount: "250", unit: "MILLILITER" }],
  });
  expect(JSON.stringify(stored)).not.toMatch(/"key"|comparison|optimizer|provider/i);

  await page.reload();

  await expect(page.getByRole("heading", { name: /Результат для/ })).toHaveCount(0);
  await expect(page.getByRole("heading", { name: "Стоимость оформления" })).toHaveCount(0);
  await expect(form.getByRole("textbox", { name: "Населённый пункт", exact: true })).toHaveValue("Москва");

  const restoredFirst = form.getByRole("group", { name: "Блюдо 1", exact: true });
  await expect(restoredFirst.getByRole("combobox", { name: "День", exact: true })).toHaveValue("SUNDAY");
  await expect(restoredFirst.getByRole("spinbutton", { name: "Нужно порций", exact: true })).toHaveValue("4");
  await expect(restoredFirst.getByRole("textbox", { name: "Название рецепта", exact: true })).toHaveValue("Омлет");
  await expect(restoredFirst.getByRole("textbox", { name: "Ингредиент", exact: true })).toHaveValue("Яйца");

  const restoredSecond = form.getByRole("group", { name: "Блюдо 2", exact: true });
  await expect(restoredSecond.getByRole("combobox", { name: "День", exact: true })).toHaveValue("TUESDAY");
  await expect(restoredSecond.getByRole("spinbutton", { name: "Нужно порций", exact: true })).toHaveValue("3");
  await expect(restoredSecond.getByRole("textbox", { name: "Название рецепта", exact: true })).toHaveValue("Каша");
  await expect(restoredSecond.getByRole("textbox", { name: "Ингредиент", exact: true })).toHaveValue("Молоко");

  const restoredPantry = form.getByRole("group", { name: "Запас дома 1", exact: true });
  await expect(restoredPantry.getByRole("textbox", { name: "Продукт дома", exact: true })).toHaveValue("Молоко");
  await expect(restoredPantry.getByRole("spinbutton", { name: "Количество дома", exact: true })).toHaveValue("250");
  await expect(restoredPantry.getByRole("combobox", { name: "Единица дома", exact: true })).toHaveValue("MILLILITER");

  await form.getByRole("button", { name: "Сравнить план" }).click();
  await expect(page.getByRole("heading", { name: "Стоимость оформления" })).toBeVisible();

  await form.getByRole("button", { name: "Очистить форму и локальный черновик" }).click();

  await expect(form.getByRole("textbox", { name: "Населённый пункт", exact: true })).toHaveValue("");
  await expect(form.getByRole("group", { name: "Блюдо 1", exact: true }).getByRole("textbox", { name: "Название рецепта", exact: true })).toHaveValue("");
  await expect(form.getByRole("group", { name: /Блюдо/ })).toHaveCount(1);
  await expect(form.getByRole("group", { name: /Запас дома/ })).toHaveCount(0);
  await expect(page.getByRole("heading", { name: "Стоимость оформления" })).toHaveCount(0);
  await expect.poll(async () => page.evaluate((key) => window.localStorage.getItem(key), DRAFT_STORAGE_KEY)).toBeNull();

  await page.reload();
  await expect(form.getByRole("textbox", { name: "Населённый пункт", exact: true })).toHaveValue("");
  await expect(form.getByRole("group", { name: /Блюдо/ })).toHaveCount(1);
  await expect(form.getByRole("group", { name: /Запас дома/ })).toHaveCount(0);
  await expect.poll(async () => page.evaluate((key) => window.localStorage.getItem(key), DRAFT_STORAGE_KEY)).toBeNull();
});
