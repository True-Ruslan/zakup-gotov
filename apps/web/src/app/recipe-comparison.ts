"use server";

import {
  RECIPE_COMPARISON_PREVIEWS_PATH,
  createZakupGotovClient,
  type components,
} from "@zakup-gotov/api-client";

const RECIPE_COMPARISON_PREVIEW_TIMEOUT_MS = 3_000;

export type RecipeComparisonPreviewRequest =
  components["schemas"]["RecipeComparisonPreviewRequest"];
export type RecipeComparisonPreviewResponse =
  components["schemas"]["RecipeComparisonPreviewResponse"];
export type RecipeComparisonPreviewValidationError =
  | components["schemas"]["RecipeComparisonPreviewValidationError"]
  | components["schemas"]["RecipeShoppingPreviewValidationError"]
  | components["schemas"]["ComparisonPreviewValidationError"];

export type RecipeComparisonState =
  | { kind: "ready"; data: RecipeComparisonPreviewResponse }
  | { kind: "invalid"; errors: RecipeComparisonPreviewValidationError[] }
  | { kind: "unavailable" };

export async function createRecipeComparisonPreview(
  request: RecipeComparisonPreviewRequest,
): Promise<RecipeComparisonState> {
  const baseUrl = process.env.API_BASE_URL;
  if (!baseUrl) {
    return { kind: "unavailable" };
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), RECIPE_COMPARISON_PREVIEW_TIMEOUT_MS);

  try {
    const client = createZakupGotovClient(baseUrl);
    const { data, error, response } = await client.POST(RECIPE_COMPARISON_PREVIEWS_PATH, {
      body: request,
      signal: controller.signal,
    });

    if (data) {
      return { kind: "ready", data };
    }
    if (response.status === 400 && error) {
      return {
        kind: "invalid",
        errors: error.errors.map(({ field, message }) => ({ field, message })),
      };
    }
    return { kind: "unavailable" };
  } catch {
    return { kind: "unavailable" };
  } finally {
    clearTimeout(timeout);
  }
}
