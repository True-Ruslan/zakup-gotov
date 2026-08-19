import fs from "node:fs/promises";
import path from "node:path";
import process from "node:process";

import { chromium, expect } from "@playwright/test";

const mode = process.argv[2];
const allowedModes = new Set(["normal", "api-unavailable", "recovered"]);
if (!allowedModes.has(mode)) {
  throw new Error(`unsupported release canary mode: ${mode ?? "<missing>"}`);
}

const baseUrl = process.env.CANARY_BASE_URL ?? "http://127.0.0.1:3000";
const evidenceDir = path.resolve(process.env.CANARY_EVIDENCE_DIR ?? "release-canary-evidence");
const reportPath = path.join(evidenceDir, "release-canary-report.json");
const releaseTag = process.env.RC7_TAG ?? "v0.1.0-rc.7";
const releaseSource = process.env.RC7_SOURCE_SHA ?? "b754f5193f852db0312011f3f6c3ec6c7dd22eb2";
const draftStorageKey = "zakup-gotov.weekly-plan-draft.v1";

await fs.mkdir(evidenceDir, { recursive: true });

async function readReport() {
  try {
    return JSON.parse(await fs.readFile(reportPath, "utf8"));
  } catch (error) {
    if (error?.code !== "ENOENT") throw error;
    return {
      schemaVersion: 1,
      releaseTag,
      releaseSource,
      verdict: "manual-review-required",
      generatedAt: new Date().toISOString(),
      modes: {},
      screenshots: [],
    };
  }
}

const report = await readReport();
report.releaseTag = releaseTag;
report.releaseSource = releaseSource;
report.verdict = "manual-review-required";
report.generatedAt = new Date().toISOString();
report.modes[mode] ??= { scenarios: [] };

async function persistReport() {
  await fs.writeFile(reportPath, `${JSON.stringify(report, null, 2)}\n`, "utf8");
}

function compactError(error) {
  const firstLine = String(error?.message ?? error).split("\n", 1)[0].slice(0, 300);
  return { name: error?.name ?? "Error", summary: firstLine };
}

async function capture(page, name) {
  const fileName = `${mode}-${name}.png`;
  await page.screenshot({ path: path.join(evidenceDir, fileName), fullPage: true });
  if (!report.screenshots.includes(fileName)) report.screenshots.push(fileName);
  await persistReport();
}

async function scenario(browser, name, viewport, run) {
  const context = await browser.newContext({ viewport });
  const page = await context.newPage();
  page.setDefaultTimeout(15_000);
  const entry = { name, status: "running", startedAt: new Date().toISOString() };
  report.modes[mode].scenarios.push(entry);
  await persistReport();

  try {
    await run(page);
    entry.status = "pass";
  } catch (error) {
    entry.status = "fail";
    entry.error = compactError(error);
  } finally {
    entry.finishedAt = new Date().toISOString();
    await persistReport();
    await context.close();
  }
}

function weeklyForm(page) {
  return page.locator("section[aria-labelledby='weekly-plan-comparison']");
}

function recipeForm(page) {
  return page.locator("section[aria-labelledby='recipe-comparison']");
}

function manualForm(page) {
  return page.locator("section[aria-labelledby='comparison-preview']");
}

async function fillWeeklyPlan(page, locality = "Москва") {
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

async function addPantry(page) {
  const form = weeklyForm(page);
  await form.getByRole("button", { name: "Добавить запас" }).click();
  const pantry = form.getByRole("group", { name: "Запас дома 1", exact: true });
  await pantry.getByRole("textbox", { name: "Продукт дома", exact: true }).fill("Молоко");
  await pantry.getByRole("spinbutton", { name: "Количество дома", exact: true }).fill("250");
  await pantry.getByRole("combobox", { name: "Единица дома", exact: true }).selectOption("MILLILITER");
}

async function fillRecipe(page) {
  const form = recipeForm(page);
  await form.getByRole("textbox", { name: "Название рецепта", exact: true }).fill("Блины");
  await form.getByRole("spinbutton", { name: "Порций в рецепте", exact: true }).fill("2");
  await form.getByRole("spinbutton", { name: "Нужно порций", exact: true }).fill("4");
  await form.getByRole("textbox", { name: "Населённый пункт", exact: true }).fill("Москва");
  await form.getByRole("textbox", { name: "Ингредиент", exact: true }).fill("Молоко");
  await form.getByRole("spinbutton", { name: "Количество", exact: true }).fill("0.5");
  await form.getByRole("combobox", { name: "Единица", exact: true }).selectOption("LITER");
  await form.getByRole("button", { name: "Добавить ингредиент" }).click();
  await form.getByRole("textbox", { name: "Ингредиент", exact: true }).nth(1).fill("Яйца");
  await form.getByRole("spinbutton", { name: "Количество", exact: true }).nth(1).fill("5");
  await form.getByRole("combobox", { name: "Единица", exact: true }).nth(1).selectOption("PIECE");
}

async function fillManual(page) {
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

async function normalScenarios(browser) {
  await scenario(browser, "desktop-weekly-pantry-optimization", { width: 1440, height: 1000 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    await fillWeeklyPlan(page);
    await addPantry(page);
    await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();
    await expect(page.getByRole("heading", { level: 2, name: "Учтено из запасов дома" })).toBeVisible();
    await expect(page.getByRole("heading", { level: 2, name: "Стоимость оформления" })).toBeVisible();
    await capture(page, "desktop-weekly-pantry-optimization");
  });

  await scenario(browser, "private-draft-restore-clear", { width: 1280, height: 900 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    const form = weeklyForm(page);
    await fillWeeklyPlan(page);
    await addPantry(page);
    await page.waitForFunction((key) => window.localStorage.getItem(key) !== null, draftStorageKey);
    await page.reload({ waitUntil: "domcontentloaded" });
    await expect(form.getByRole("textbox", { name: "Населённый пункт", exact: true })).toHaveValue("Москва");
    await expect(form.getByRole("group", { name: "Блюдо 2", exact: true })).toBeVisible();
    await expect(form.getByRole("group", { name: "Запас дома 1", exact: true })).toBeVisible();
    await capture(page, "draft-restored");
    await form.getByRole("button", { name: "Очистить форму и локальный черновик" }).click();
    await page.waitForFunction((key) => window.localStorage.getItem(key) === null, draftStorageKey);
    await expect(form.getByRole("textbox", { name: "Населённый пункт", exact: true })).toHaveValue("");
    await capture(page, "draft-cleared");
  });

  await scenario(browser, "recipe-comparison", { width: 1280, height: 900 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    await fillRecipe(page);
    await recipeForm(page).getByRole("button", { name: "Сравнить рецепт" }).click();
    await expect(page.getByRole("heading", { level: 2, name: "Список покупок из рецепта" })).toBeVisible();
    await capture(page, "recipe-comparison");
  });

  await scenario(browser, "manual-list-comparison", { width: 1280, height: 900 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    await fillManual(page);
    await manualForm(page).getByRole("button", { name: "Сравнить корзину" }).click();
    await expect(page.getByRole("heading", { level: 2, name: "Результат для Москва" }).last()).toBeVisible();
    await capture(page, "manual-list-comparison");
  });

  await scenario(browser, "narrow-layout", { width: 390, height: 844 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    await fillWeeklyPlan(page);
    await addPantry(page);
    await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();
    await expect(page.getByRole("heading", { level: 2, name: "Стоимость оформления" })).toBeVisible();
    const widths = await page.evaluate(() => ({
      scrollWidth: document.documentElement.scrollWidth,
      clientWidth: document.documentElement.clientWidth,
    }));
    expect(widths.scrollWidth).toBeLessThanOrEqual(widths.clientWidth);
    report.modes[mode].narrowViewport = widths;
    await capture(page, "narrow-weekly-result");
  });
}

async function unavailableScenario(browser) {
  await scenario(browser, "api-unavailable-fails-closed", { width: 1280, height: 900 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    await fillWeeklyPlan(page);
    await weeklyForm(page).getByRole("button", { name: "Сравнить план" }).click();
    await expect(page.getByRole("alert").filter({ hasText: "Не удалось сравнить недельный план" })).toHaveCount(1);
    await expect(page.getByRole("heading", { name: /Результат для/ })).toHaveCount(0);
    await capture(page, "api-unavailable");
  });
}

async function recoveredScenario(browser) {
  await scenario(browser, "api-recovered-after-restart", { width: 1280, height: 900 }, async (page) => {
    await page.goto(baseUrl, { waitUntil: "domcontentloaded" });
    await fillManual(page);
    await manualForm(page).getByRole("button", { name: "Сравнить корзину" }).click();
    await expect(page.getByRole("heading", { level: 2, name: "Результат для Москва" }).last()).toBeVisible();
    await capture(page, "recovered-manual-comparison");
  });
}

const browser = await chromium.launch({ headless: true });
try {
  if (mode === "normal") await normalScenarios(browser);
  if (mode === "api-unavailable") await unavailableScenario(browser);
  if (mode === "recovered") await recoveredScenario(browser);
} finally {
  await browser.close();
  report.modes[mode].finishedAt = new Date().toISOString();
  await persistReport();
}

const failures = report.modes[mode].scenarios.filter((entry) => entry.status !== "pass");
if (failures.length > 0) {
  console.error(`release canary ${mode}: ${failures.length} scenario(s) failed; manual-review-required evidence preserved`);
  process.exitCode = 1;
} else {
  console.log(`release canary ${mode}: all evidence scenarios captured; manual-review-required`);
}
