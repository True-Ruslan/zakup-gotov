import { afterEach, describe, expect, it, vi } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { createWeeklyPlanComparisonPreview } from "./weekly-plan-comparison";

const originalApiBaseUrl = process.env.API_BASE_URL;

type Request = components["schemas"]["WeeklyPlanComparisonPreviewRequest"];

const request: Request = {
  locality: "Москва",
  weeklyPlan: {
    occurrences: [
      {
        day: "MONDAY",
        targetServings: 4,
        recipe: {
          title: "Блины",
          baseServings: 2,
          ingredients: [
            { requirement: "Молоко", quantity: { amount: 0.5, unit: "LITER" } },
          ],
        },
      },
    ],
  },
};

function restoreApiBaseUrl() {
  if (originalApiBaseUrl === undefined) delete process.env.API_BASE_URL;
  else process.env.API_BASE_URL = originalApiBaseUrl;
}

afterEach(() => {
  restoreApiBaseUrl();
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe("WeeklyPlan comparison transport", () => {
  it("fails closed without API_BASE_URL and performs no request", async () => {
    delete process.env.API_BASE_URL;
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(createWeeklyPlanComparisonPreview(request)).resolves.toEqual({ kind: "unavailable" });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("returns the typed composed preview unchanged", async () => {
    process.env.API_BASE_URL = "http://api.test";
    const response: components["schemas"]["WeeklyPlanComparisonPreview"] = {
      weeklyPlanShoppingPreview: {
        weeklyPlan: {
          id: "10000000-0000-0000-0000-000000000001",
          occurrences: [
            {
              id: "11000000-0000-0000-0000-000000000001",
              day: "MONDAY",
              targetServings: 4,
              recipe: {
                id: "12000000-0000-0000-0000-000000000001",
                title: "Блины",
                baseServings: 2,
                ingredients: [],
              },
            },
          ],
        },
        shoppingList: { id: "13000000-0000-0000-0000-000000000001", items: [] },
      },
      comparisonPreview: { locality: "Москва", items: [], retailers: [] },
    };

    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify(response), {
          status: 200,
          headers: { "content-type": "application/json" },
        }),
      ),
    );

    await expect(createWeeklyPlanComparisonPreview(request)).resolves.toEqual({
      kind: "ready",
      data: response,
    });
  });

  it("returns only generated product-safe validation fields for HTTP 400", async () => {
    process.env.API_BASE_URL = "http://api.test";
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            type: "https://zakup-gotov.dev/problems/invalid-weekly-plan-shopping-preview",
            title: "Invalid weekly plan shopping preview request",
            status: 400,
            code: "INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW",
            errors: [{ field: "weeklyPlan.occurrences[0].targetServings", message: "must be greater than 0" }],
          }),
          { status: 400, headers: { "content-type": "application/problem+json" } },
        ),
      ),
    );

    await expect(createWeeklyPlanComparisonPreview(request)).resolves.toEqual({
      kind: "invalid",
      errors: [
        { field: "weeklyPlan.occurrences[0].targetServings", message: "must be greater than 0" },
      ],
    });
  });

  it("aborts a hanging backend request within the bounded timeout", async () => {
    vi.useFakeTimers();
    process.env.API_BASE_URL = "http://api.test";
    let aborted = false;
    vi.stubGlobal(
      "fetch",
      vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
        const signal = input instanceof globalThis.Request ? input.signal : init?.signal;
        return new Promise<Response>((_resolve, reject) => {
          signal?.addEventListener("abort", () => {
            aborted = true;
            reject(new DOMException("aborted", "AbortError"));
          }, { once: true });
        });
      }),
    );

    const result = createWeeklyPlanComparisonPreview(request);
    await vi.advanceTimersByTimeAsync(3_000);

    expect(aborted).toBe(true);
    await expect(result).resolves.toEqual({ kind: "unavailable" });
  });
});
