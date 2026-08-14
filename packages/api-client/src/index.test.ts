import { describe, expect, it } from "vitest";

import {
  COMPARISON_PREVIEWS_PATH,
  RECIPE_COMPARISON_PREVIEWS_PATH,
  RECIPE_SHOPPING_PREVIEWS_PATH,
  RETAILERS_PATH,
  SYSTEM_INFO_PATH,
  WEEKLY_PLAN_SHOPPING_PREVIEWS_PATH,
  createZakupGotovClient,
} from "./index";
import type { components, paths } from "./schema";

describe("Zakup Gotov API client", () => {
  it("exposes the generated system endpoint through a typed client", () => {
    const path: keyof paths = SYSTEM_INFO_PATH;
    const client = createZakupGotovClient("https://api.example.test");

    expect(path).toBe("/api/v1/system");
    expect(client.GET).toBeTypeOf("function");
  });

  it("exposes the retailer readiness endpoint through the generated contract", () => {
    const path: keyof paths = RETAILERS_PATH;
    const client = createZakupGotovClient("https://api.example.test");

    expect(path).toBe("/api/v1/retailers");
    expect(client.GET).toBeTypeOf("function");
  });

  it("exposes the comparison preview endpoint through the generated contract", () => {
    const path: keyof paths = COMPARISON_PREVIEWS_PATH;
    const client = createZakupGotovClient("https://api.example.test");
    type PreviewPost = paths["/api/v1/comparison-previews"]["post"];
    const operationExists: PreviewPost | undefined = undefined;

    expect(path).toBe("/api/v1/comparison-previews");
    expect(client.POST).toBeTypeOf("function");
    expect(operationExists).toBeUndefined();
  });

  it("exposes the recipe shopping preview endpoint and response through the generated contract", () => {
    const path: keyof paths = RECIPE_SHOPPING_PREVIEWS_PATH;
    const client = createZakupGotovClient("https://api.example.test");
    type RecipePreviewPost = paths["/api/v1/recipe-shopping-previews"]["post"];
    type RecipePreviewResponse = components["schemas"]["RecipeShoppingPreviewResponse"];
    const operationExists: RecipePreviewPost | undefined = undefined;
    const responseExists: RecipePreviewResponse | undefined = undefined;

    expect(path).toBe("/api/v1/recipe-shopping-previews");
    expect(client.POST).toBeTypeOf("function");
    expect(operationExists).toBeUndefined();
    expect(responseExists).toBeUndefined();
  });

  it("exposes the composed recipe comparison endpoint through the generated contract", () => {
    const path: keyof paths = RECIPE_COMPARISON_PREVIEWS_PATH;
    const client = createZakupGotovClient("https://api.example.test");
    type ComposedPost = paths["/api/v1/recipe-comparison-previews"]["post"];
    type ComposedRequest = components["schemas"]["RecipeComparisonPreviewRequest"];
    type ComposedResponse = components["schemas"]["RecipeComparisonPreviewResponse"];
    const operationExists: ComposedPost | undefined = undefined;
    const requestExists: ComposedRequest | undefined = undefined;
    const responseExists: ComposedResponse | undefined = undefined;

    expect(path).toBe("/api/v1/recipe-comparison-previews");
    expect(client.POST).toBeTypeOf("function");
    expect(operationExists).toBeUndefined();
    expect(requestExists).toBeUndefined();
    expect(responseExists).toBeUndefined();
  });

  it("exposes the weekly plan shopping preview endpoint through the generated contract", () => {
    const path: keyof paths = WEEKLY_PLAN_SHOPPING_PREVIEWS_PATH;
    const client = createZakupGotovClient("https://api.example.test");
    type WeeklyPreviewPost = paths["/api/v1/weekly-plan-shopping-previews"]["post"];
    type WeeklyPreviewRequest = components["schemas"]["WeeklyPlanShoppingPreviewRequest"];
    type WeeklyPreviewResponse = components["schemas"]["WeeklyPlanShoppingPreviewResponse"];
    type WeeklyPreviewProblem = components["schemas"]["InvalidWeeklyPlanShoppingPreviewProblem"];
    const operationExists: WeeklyPreviewPost | undefined = undefined;
    const requestExists: WeeklyPreviewRequest | undefined = undefined;
    const responseExists: WeeklyPreviewResponse | undefined = undefined;
    const problemExists: WeeklyPreviewProblem | undefined = undefined;

    expect(path).toBe("/api/v1/weekly-plan-shopping-previews");
    expect(client.POST).toBeTypeOf("function");
    expect(operationExists).toBeUndefined();
    expect(requestExists).toBeUndefined();
    expect(responseExists).toBeUndefined();
    expect(problemExists).toBeUndefined();
  });
});
