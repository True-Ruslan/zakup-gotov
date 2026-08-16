import { expect, test } from "@playwright/test";

test("homepage describes the current M4 basket optimization phase", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByText("M4 · Basket Optimization", { exact: true })).toBeVisible();
  await expect(page.getByText(/стоимость оформления/i)).toBeVisible();
  await expect(page.getByText(/минимальную подтверждённую стоимость/i)).toBeVisible();
});
