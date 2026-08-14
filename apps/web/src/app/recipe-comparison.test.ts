import { afterEach, describe, expect, it, vi } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { createRecipeComparisonPreview } from "./recipe-comparison";

const originalApiBaseUrl = process.env.API_BASE_URL;

type Request = components["schemas"]["RecipeComparisonPreviewRequest"];

const request: Request = {
  locality: "Москва",
  recipe: {
    title: "Блины",
    baseServings: 2,
    targetServings: 4,
    ingredients: [
      {
        requirement: "Молоко",
        quantity: { amount: 0.5, unit: "LITER" },
      },
    ],
  },
};

function restoreApiBaseUrl() {
  if (originalApiBaseUrl === undefined) {
    delete process.env.API_BASE_URL;
  } else {
    process.env.API_BASE_URL = originalApiBaseUrl;
  }
}

afterEach(() => {
  restoreApiBaseUrl();
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe("recipe comparison transport", () => {
  it("fails closed without API_BASE_URL and performs no request", async () => {
    delete process.env.API_BASE_URL;
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(createRecipeComparisonPreview(request)).resolves.toEqual({ kind: "unavailable" });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("returns the typed composed preview for a successful response", async () => {
    process.env.API_BASE_URL = "http://api.test";
    const response: components["schemas"]["RecipeComparisonPreviewResponse"] = {
      recipeShoppingPreview: {
        recipe: {
          id: "10000000-0000-0000-0000-000000000001",
          title: "Блины",
          baseServings: 2,
          targetServings: 4,
          ingredients: [
            {
              id: "20000000-0000-0000-0000-000000000001",
              requirement: "Молоко",
              quantity: { amount: 500, unit: "MILLILITER" },
            },
          ],
        },
        shoppingList: {
          id: "30000000-0000-0000-0000-000000000001",
          items: [
            {
              id: "40000000-0000-0000-0000-000000000001",
              requirement: "Молоко",
              quantity: { amount: 1000, unit: "MILLILITER" },
              sourceIngredientIds: ["20000000-0000-0000-0000-000000000001"],
            },
          ],
        },
      },
      comparisonPreview: {
        locality: "Москва",
        items: [
          {
            id: "40000000-0000-0000-0000-000000000001",
            requirement: "Молоко",
            quantity: { amount: 1000, unit: "MILLILITER" },
          },
        ],
        retailers: [],
      },
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

    await expect(createRecipeComparisonPreview(request)).resolves.toEqual({
      kind: "ready",
      data: response,
    });
  });

  it("returns only product-safe validation fields for any generated HTTP 400 problem", async () => {
    process.env.API_BASE_URL = "http://api.test";
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            type: "https://zakup-gotov.dev/problems/invalid-recipe-shopping-preview",
            title: "Invalid recipe shopping preview request",
            status: 400,
            code: "INVALID_RECIPE_SHOPPING_PREVIEW",
            errors: [{ field: "ingredients[0].quantity.amount", message: "must be greater than 0" }],
          }),
          {
            status: 400,
            headers: { "content-type": "application/problem+json" },
          },
        ),
      ),
    );

    await expect(createRecipeComparisonPreview(request)).resolves.toEqual({
      kind: "invalid",
      errors: [{ field: "ingredients[0].quantity.amount", message: "must be greater than 0" }],
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
          signal?.addEventListener(
            "abort",
            () => {
              aborted = true;
              reject(new DOMException("aborted", "AbortError"));
            },
            { once: true },
          );
        });
      }),
    );

    const result = createRecipeComparisonPreview(request);
    await vi.advanceTimersByTimeAsync(5_000);

    expect(aborted).toBe(true);
    await expect(result).resolves.toEqual({ kind: "unavailable" });
  });
});
