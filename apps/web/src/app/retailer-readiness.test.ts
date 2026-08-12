import { afterEach, describe, expect, it, vi } from "vitest";

import { loadRetailerReadiness } from "./retailer-readiness";

const originalApiBaseUrl = process.env.API_BASE_URL;

function restoreApiBaseUrl() {
  if (originalApiBaseUrl === undefined) {
    delete process.env.API_BASE_URL;
  } else {
    process.env.API_BASE_URL = originalApiBaseUrl;
  }
}

function requestSignal(input: RequestInfo | URL, init?: RequestInit) {
  return input instanceof Request ? input.signal : init?.signal;
}

afterEach(() => {
  restoreApiBaseUrl();
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe("retailer readiness loader", () => {
  it("fails closed without API_BASE_URL and performs no request", async () => {
    delete process.env.API_BASE_URL;
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(loadRetailerReadiness()).resolves.toEqual({ kind: "unavailable" });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("returns typed retailer data for a successful non-empty response", async () => {
    process.env.API_BASE_URL = "http://api.test";
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            retailers: [
              {
                id: "pyaterochka",
                displayName: "Пятёрочка",
                coverage: "CONNECTED",
                productionAccess: "PENDING",
                comparisonStatus: "UNAVAILABLE",
                reasons: ["PRODUCTION_ACCESS_PENDING"],
              },
            ],
          }),
          { status: 200, headers: { "content-type": "application/json" } },
        ),
      ),
    );

    await expect(loadRetailerReadiness()).resolves.toEqual({
      kind: "ready",
      data: {
        retailers: [
          {
            id: "pyaterochka",
            displayName: "Пятёрочка",
            coverage: "CONNECTED",
            productionAccess: "PENDING",
            comparisonStatus: "UNAVAILABLE",
            reasons: ["PRODUCTION_ACCESS_PENDING"],
          },
        ],
      },
    });
  });

  it("fails closed for an empty retailer payload", async () => {
    process.env.API_BASE_URL = "http://api.test";
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ retailers: [] }), {
          status: 200,
          headers: { "content-type": "application/json" },
        }),
      ),
    );

    await expect(loadRetailerReadiness()).resolves.toEqual({ kind: "unavailable" });
  });

  it("aborts a hanging API request and resolves to unavailable within the bounded timeout", async () => {
    vi.useFakeTimers();
    process.env.API_BASE_URL = "http://api.test";

    let aborted = false;
    vi.stubGlobal(
      "fetch",
      vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
        const signal = requestSignal(input, init);
        return new Promise<Response>((_resolve, reject) => {
          signal?.addEventListener(
            "abort",
            () => {
              aborted = true;
              reject(new DOMException("The operation was aborted", "AbortError"));
            },
            { once: true },
          );
        });
      }),
    );

    let settled = false;
    const result = loadRetailerReadiness();
    void result.then(() => {
      settled = true;
    });

    await vi.advanceTimersByTimeAsync(5_000);

    expect(aborted).toBe(true);
    expect(settled).toBe(true);
    await expect(result).resolves.toEqual({ kind: "unavailable" });
  });
});
