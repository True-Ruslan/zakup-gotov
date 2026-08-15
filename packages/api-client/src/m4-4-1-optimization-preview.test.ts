import { describe, expect, it } from "vitest";

import {
  WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH,
  createZakupGotovClient,
} from "./index";
import type { components, operations, paths } from "./schema";

describe("M4.4.1 server-owned optimization preview contract", () => {
  it("exposes the Pantry-aware optimization endpoint and server-owned economics/optimizer schemas", () => {
    const path: keyof paths = WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEWS_PATH;
    const client = createZakupGotovClient("https://api.example.test");
    type OptimizationPost = paths["/api/v1/weekly-plan-pantry-optimization-previews"]["post"];
    type OptimizationOperation = operations["createWeeklyPlanPantryOptimizationPreview"];
    type OptimizationRequest = components["schemas"]["WeeklyPlanPantryOptimizationPreviewRequest"];
    type OptimizationResponse = components["schemas"]["WeeklyPlanPantryOptimizationPreview"];
    type CheckoutOptimization = components["schemas"]["CheckoutOptimizationPreview"];
    type RetailerCheckout = components["schemas"]["RetailerCheckoutPreview"];
    type RetailerAssessment = components["schemas"]["RetailerCheckoutAssessmentPreview"];
    type OptimizationProblem = components["schemas"]["InvalidWeeklyPlanPantryOptimizationPreviewProblem"];
    const postExists: OptimizationPost | undefined = undefined;
    const operationExists: OptimizationOperation | undefined = undefined;
    const requestExists: OptimizationRequest | undefined = undefined;
    const responseExists: OptimizationResponse | undefined = undefined;
    const optimizationExists: CheckoutOptimization | undefined = undefined;
    const retailerExists: RetailerCheckout | undefined = undefined;
    const assessmentExists: RetailerAssessment | undefined = undefined;
    const problemExists: OptimizationProblem | undefined = undefined;

    expect(path).toBe("/api/v1/weekly-plan-pantry-optimization-previews");
    expect(client.POST).toBeTypeOf("function");
    expect(postExists).toBeUndefined();
    expect(operationExists).toBeUndefined();
    expect(requestExists).toBeUndefined();
    expect(responseExists).toBeUndefined();
    expect(optimizationExists).toBeUndefined();
    expect(retailerExists).toBeUndefined();
    expect(assessmentExists).toBeUndefined();
    expect(problemExists).toBeUndefined();
  });
});
