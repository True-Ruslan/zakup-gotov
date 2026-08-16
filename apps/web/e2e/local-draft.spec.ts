import { expect, test, type Locator } from "@playwright/test";

const DRAFT_STORAGE_KEY = "zakup-gotov.weekly-plan-draft.v1";

async function fillMeal(
  meal: Locator,
  values: {
    day: "TUESDAY" | "FRIDAY";
    targetServings: string;
    title: string;
    baseServings: string;
    requirement: string;
    amount: string;
    unit: "LITER" | "PIECE";
  },
) {
  await meal.getByLabel("День").selectOption(values.day);
  await meal.getByLabel("Нужно порций").fill(values.targetServings);
  await meal.getByLabel("Название рецепта").fill(values.title);
  await meal.getByLabel("Порций в рецепте").fill(values.baseServings);
  await meal.getByLabel("Ингредиент").fill(values.requirement);
  await meal.getByLabel("Количество").fill(values.amount);
  await meal.getByLabel("Единица").selectOption(values.unit);
}

test("private local draft restores across reload and clears without implicit comparison", async ({ page }) => {
  let postRequests = 0;
  page.on("request", (request) => {
    if (request.method() === "POST") postRequests += 1;
  });

  await page.goto("/");
  await expect(page.getByText(/Черновик сохраняется только в этом браузере/)).toBeVisible();
  await page.getByLabel("Населённый пункт").fill("Москва");

  const firstMeal = page.getByRole("group", { name: "Блюдо 1" });
  await fillMeal(firstMeal, {
    day: "TUESDAY",
    targetServings: "4",
    title: "Блины",
    baseServings: "2",
    requirement: "Молоко",
    amount: "0.5",
    unit: "LITER",
  });

  await page.getByRole("button", { name: "Добавить блюдо" }).click();
  const secondMeal = page.getByRole("group", { name: "Блюдо 2" });
  await fillMeal(secondMeal, {
    day: "FRIDAY",
    targetServings: "2",
    title: "Омлет",
    baseServings: "2",
    requirement: "Яйца",
    amount: "6",
    unit: "PIECE",
  });

  await page.getByRole("button", { name: "Добавить запас" }).click();
  const pantry = page.getByRole("group", { name: "Запас дома 1" });
  await pantry.getByLabel("Продукт дома").fill("Молоко");
  await pantry.getByLabel("Количество дома").fill("250");
  await pantry.getByLabel("Единица дома").selectOption("MILLILITER");

  await expect.poll(() => page.evaluate((key) => localStorage.getItem(key), DRAFT_STORAGE_KEY))
    .toContain('"locality":"Москва"');
  expect(postRequests).toBe(0);

  await page.reload();

  await expect(page.getByLabel("Населённый пункт")).toHaveValue("Москва");
  const restoredMeals = page.getByRole("group", { name: /Блюдо [12]/ });
  await expect(restoredMeals).toHaveCount(2);
  await expect(page.getByRole("group", { name: "Блюдо 1" }).getByLabel("Название рецепта"))
    .toHaveValue("Блины");
  await expect(page.getByRole("group", { name: "Блюдо 2" }).getByLabel("Название рецепта"))
    .toHaveValue("Омлет");
  await expect(page.getByRole("group", { name: "Запас дома 1" }).getByLabel("Продукт дома"))
    .toHaveValue("Молоко");
  expect(postRequests).toBe(0);

  await page.getByRole("button", { name: "Сравнить план" }).click();
  await expect(page.getByRole("heading", { name: "Сравнение магазинов" })).toBeVisible();
  expect(postRequests).toBeGreaterThan(0);

  await page.getByRole("button", { name: "Очистить форму и локальный черновик" }).click();
  await expect(page.getByLabel("Населённый пункт")).toHaveValue("");
  await expect.poll(() => page.evaluate((key) => localStorage.getItem(key), DRAFT_STORAGE_KEY)).toBeNull();

  const postsAfterExplicitSubmit = postRequests;
  await page.reload();
  await expect(page.getByLabel("Населённый пункт")).toHaveValue("");
  await expect(page.getByRole("group", { name: /Блюдо/ })).toHaveCount(1);
  await expect(page.getByRole("group", { name: "Блюдо 1" }).getByLabel("Название рецепта"))
    .toHaveValue("");
  await expect(page.getByRole("group", { name: /Запас дома/ })).toHaveCount(0);
  expect(postRequests).toBe(postsAfterExplicitSubmit);
});
