import { expect, test } from "@playwright/test";

test("renders the honest product shell without horizontal overflow", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { level: 1, name: "Закуп готов" })).toBeVisible();
  await expect(page.getByText(/проверяем интеграции с магазинами/i)).toBeVisible();

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
