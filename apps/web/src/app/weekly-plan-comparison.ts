"use server";

import {
  WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH,
  createZakupGotovClient,
  type components,
} from "@zakup-gotov/api-client";

const WEEKLY_PLAN_COMPARISON_PREVIEW_TIMEOUT_MS = 3_000;

export type WeeklyPlanComparisonPreviewRequest =
  components["schemas"]["WeeklyPlanComparisonPreviewRequest"];
export type WeeklyPlanComparisonPreviewResponse =
  components["schemas"]["WeeklyPlanComparisonPreview"];
export type WeeklyPlanComparisonPreviewValidationError =
  | components["schemas"]["WeeklyPlanComparisonPreviewValidationError"]
  | components["schemas"]["WeeklyPlanShoppingPreviewValidationError"]
  | components["schemas"]["RecipeShoppingPreviewValidationError"]
  | components["schemas"]["ComparisonPreviewValidationError"];

export type WeeklyPlanComparisonState =
  | { kind: "ready"; data: WeeklyPlanComparisonPreviewResponse }
  | { kind: "invalid"; errors: WeeklyPlanComparisonPreviewValidationError[] }
  | { kind: "unavailable" };

export async function createWeeklyPlanComparisonPreview(
  request: WeeklyPlanComparisonPreviewRequest,
): Promise<WeeklyPlanComparisonState> {
  const baseUrl = process.env.API_BASE_URL;
  if (!baseUrl) return { kind: "unavailable" };

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), WEEKLY_PLAN_COMPARISON_PREVIEW_TIMEOUT_MS);

  try {
    const client = createZakupGotovClient(baseUrl);
    const { data, error, response } = await client.POST(WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH, {
      body: request,
      signal: controller.signal,
    });

    if (data) return { kind: "ready", data };
    if (response.status === 400 && error && "errors" in error) {
      const errors = error.errors as WeeklyPlanComparisonPreviewValidationError[];
      return {
        kind: "invalid",
        errors: errors.map(({ field, message }) => ({ field, message })),
      };
    }
    return { kind: "unavailable" };
  } catch {
    return { kind: "unavailable" };
  } finally {
    clearTimeout(timeout);
  }
}
