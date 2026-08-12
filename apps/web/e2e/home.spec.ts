import { expect, test } from "@playwright/test";

test("renders the honest M1 retailer status without horizontal overflow", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeVisible();
  await expect(page.getByText(/M1 · Shopping Core/i)).toBeVisible();
  await expect(page.getByRole("heading", { level: 2, name: "Покрытие магазинов" })).toBeVisible();
  await expect(page.getByRole("alert")).toHaveText(
    "Не удалось загрузить статус магазинов. Основной сервис временно недоступен.",
  );
  await expect(page.getByRole("list", { name: "Статус магазинов" })).toHaveCount(0);
  await expect(page.getByRole("heading", { level: 3 })).toHaveCount(0);

  const overflowsHorizontally = await page.evaluate(
    () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
  );
  expect(overflowsHorizontally).toBe(false);
});

test("project documentation link has a visible keyboard focus state", async ({ page }) => {
  await page.goto("/");

  const documentation = page.getByRole("link", { name: "Документация проекта" });
  await page.keyboard.press("Tab");
  await expect(documentation).toBeFocused();

  const focusIsVisible = await documentation.evaluate((element) => {
    const style = window.getComputedStyle(element);
    return style.outlineStyle !== "none" && style.outlineWidth !== "0px";
  });

  expect(focusIsVisible).toBe(true);
});
