import { afterEach, describe, expect, it, vi } from "vitest";

import type { components } from "@zakup-gotov/api-client";
import { createComparisonPreview } from "./comparison-preview";

const originalApiBaseUrl = process.env.API_BASE_URL;

type Request = components["schemas"]["ComparisonPreviewRequest"];

const request: Request = {
  locality: "Москва",
  items: [
    {
      id: "c281d71c-2b27-46ef-a7af-3d624a7447cf",
      requirement: "Молоко",
      quantity: { amount: 2, unit: "LITER" },
    },
  ],
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

describe("comparison preview transport", () => {
  it("fails closed without API_BASE_URL and performs no request", async () => {
    delete process.env.API_BASE_URL;
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(createComparisonPreview(request)).resolves.toEqual({ kind: "unavailable" });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("returns a typed preview for a successful response", async () => {
    process.env.API_BASE_URL = "http://api.test";
    const response = {
      locality: "Москва",
      items: [
        {
          id: request.items[0]!.id,
          requirement: "Молоко",
          quantity: { amount: 2000, unit: "MILLILITER" as const },
        },
      ],
      retailers: [],
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

    await expect(createComparisonPreview(request)).resolves.toEqual({
      kind: "ready",
      data: response,
    });
  });

  it("returns product-safe field errors for HTTP 400", async () => {
    process.env.API_BASE_URL = "http://api.test";
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            type: "https://zakup-gotov.dev/problems/invalid-comparison-preview",
            title: "Invalid comparison preview request",
            status: 400,
            code: "INVALID_COMPARISON_PREVIEW",
            errors: [{ field: "items[0].quantity.amount", message: "must be greater than 0" }],
          }),
          {
            status: 400,
            headers: { "content-type": "application/problem+json" },
          },
        ),
      ),
    );

    await expect(createComparisonPreview(request)).resolves.toEqual({
      kind: "invalid",
      errors: [{ field: "items[0].quantity.amount", message: "must be greater than 0" }],
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

    const result = createComparisonPreview(request);
    await vi.advanceTimersByTimeAsync(5_000);

    expect(aborted).toBe(true);
    await expect(result).resolves.toEqual({ kind: "unavailable" });
  });
});
