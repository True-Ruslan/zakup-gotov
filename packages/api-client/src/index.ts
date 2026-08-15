import createClient from "openapi-fetch";

import type { components, operations, paths } from "./schema";

export const SYSTEM_INFO_PATH = "/api/v1/system" as const;
export const RETAILERS_PATH = "/api/v1/retailers" as const;
export const COMPARISON_PREVIEWS_PATH = "/api/v1/comparison-previews" as const;
export const RECIPE_SHOPPING_PREVIEWS_PATH = "/api/v1/recipe-shopping-previews" as const;
export const RECIPE_COMPARISON_PREVIEWS_PATH = "/api/v1/recipe-comparison-previews" as const;
export const WEEKLY_PLAN_SHOPPING_PREVIEWS_PATH = "/api/v1/weekly-plan-shopping-previews" as const;
export const WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEWS_PATH = "/api/v1/weekly-plan-pantry-shopping-previews" as const;
export const WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEWS_PATH = "/api/v1/weekly-plan-pantry-comparison-previews" as const;
export const WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH = "/api/v1/weekly-plan-comparison-previews" as const;

export function createZakupGotovClient(baseUrl: string) {
  return createClient<paths>({ baseUrl });
}

export type { components, operations, paths };
