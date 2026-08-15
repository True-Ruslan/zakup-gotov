"use server";

import {
  WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH,
  createZakupGotovClient,
  type components,
} from "@zakup-gotov/api-client";

const WEEKLY_PLAN_OPTIMIZATION_PREVIEW_TIMEOUT_MS = 3_000;

export type WeeklyPlanOptimizationPreviewRequest =
  components["schemas"]["WeeklyPlanPantryOptimizationPreviewRequest"];
export type WeeklyPlanOptimizationPreviewResponse =
  components["schemas"]["WeeklyPlanPantryOptimizationPreview"];
export type WeeklyPlanOptimizationPreviewValidationError =
  components["schemas"]["WeeklyPlanPantryOptimizationPreviewValidationError"];

export type WeeklyPlanOptimizationState =
  | { kind: "ready"; data: WeeklyPlanOptimizationPreviewResponse }
  | { kind: "invalid"; errors: WeeklyPlanOptimizationPreviewValidationError[] }
  | { kind: "unavailable" };

export async function createWeeklyPlanOptimizationPreview(
  request: WeeklyPlanOptimizationPreviewRequest,
): Promise<WeeklyPlanOptimizationState> {
  const baseUrl = process.env.API_BASE_URL;
  if (!baseUrl) return { kind: "unavailable" };

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), WEEKLY_PLAN_OPTIMIZATION_PREVIEW_TIMEOUT_MS);

  try {
    const client = createZakupGotovClient(baseUrl);
    const { data, error, response } = await client.POST(WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH, {
      body: request,
      signal: controller.signal,
    });

    if (data) return { kind: "ready", data };
    if (response.status === 400 && error && "errors" in error) {
      const errors = error.errors as WeeklyPlanOptimizationPreviewValidationError[];
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

// Compatibility aliases keep the accepted form behavior stable while M4.4.2 migrates its presentation layer.
export type WeeklyPlanComparisonPreviewRequest = WeeklyPlanOptimizationPreviewRequest;
export type WeeklyPlanComparisonPreviewResponse = WeeklyPlanOptimizationPreviewResponse;
export type WeeklyPlanComparisonPreviewValidationError = WeeklyPlanOptimizationPreviewValidationError;
export type WeeklyPlanComparisonState = WeeklyPlanOptimizationState;

export async function createWeeklyPlanComparisonPreview(
  request: WeeklyPlanComparisonPreviewRequest,
): Promise<WeeklyPlanComparisonState> {
  return createWeeklyPlanOptimizationPreview(request);
}
