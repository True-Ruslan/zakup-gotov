// @vitest-environment jsdom

import { describe, expect, it, vi } from "vitest";
import {
  chizhikBrowserAdapter,
  createChizhikBrowserAdapter,
} from "../src/adapters/chizhik-browser-adapter";

const OBSERVED_AT = "2026-08-18T00:15:00Z";
const PAGE_URL = new URL("https://chizhik.club/catalog/chay-kofe--264C39224/#fragment");

function documentWithGuessedProduct(): Document {
  return new DOMParser().parseFromString(
    `<!doctype html><html><body>
      <article data-product-id="123" data-price="99.99">
        <a href="/product/123">Guessed product</a>
        <span>99 ₽</span>
      </article>
    </body></html>`,
    "text/html",
  );
}

const VALID_DISCOVERY = {
  status: "ok" as const,
  stores: [
    {
      sapId: "HD87",
      longitude: 37.83372708,
      latitude: 55.76833314,
      active: true,
      name: "Москва, Саянская ул., Дом 11Б",
      locality: "Москва",
    },
  ],
};

describe("chizhikBrowserAdapter", () => {
  it("supports only the explicit official Chizhik HTTPS page origin", () => {
    expect(chizhikBrowserAdapter.supports(new URL("https://chizhik.club/"))).toBe(true);
    expect(chizhikBrowserAdapter.supports(new URL("https://chizhik.club/catalog/test"))).toBe(true);

    expect(chizhikBrowserAdapter.supports(new URL("http://chizhik.club/"))).toBe(false);
    expect(chizhikBrowserAdapter.supports(new URL("https://www.chizhik.club/"))).toBe(false);
    expect(chizhikBrowserAdapter.supports(new URL("https://app.chizhik.club/"))).toBe(false);
    expect(chizhikBrowserAdapter.supports(new URL("https://chizhik.club.evil.example/"))).toBe(false);
  });

  it("reports observation-only after active fixed-endpoint store discovery succeeds without auto-searching products", async () => {
    const listStores = vi.fn(async () => VALID_DISCOVERY);
    const searchStore = vi.fn(async () => ({ status: "unavailable" as const }));
    const adapter = createChizhikBrowserAdapter({ listStores, searchStore });

    await expect(
      adapter.collect({
        document: documentWithGuessedProduct(),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: [],
      }),
    ).resolves.toEqual({ status: "observation-only", observations: [] });
    expect(listStores).toHaveBeenCalledTimes(1);
    expect(searchStore).not.toHaveBeenCalled();
  });

  it("reuses one store discovery request for repeated collections in the same lifecycle", async () => {
    const listStores = vi.fn(async () => VALID_DISCOVERY);
    const searchStore = vi.fn(async () => ({ status: "unavailable" as const }));
    const adapter = createChizhikBrowserAdapter({ listStores, searchStore });
    const input = {
      document: documentWithGuessedProduct(),
      url: PAGE_URL,
      observedAt: OBSERVED_AT,
      resourceUrls: [] as string[],
    };

    await adapter.collect(input);
    await adapter.collect(input);
    await adapter.collect(input);

    expect(listStores).toHaveBeenCalledTimes(1);
    expect(searchStore).not.toHaveBeenCalled();
  });

  it("fails closed when active store discovery is unavailable or empty", async () => {
    for (const result of [
      { status: "unavailable" as const, stores: [] as const },
      { status: "ok" as const, stores: [] as const },
    ]) {
      const searchStore = vi.fn(async () => ({ status: "unavailable" as const }));
      const adapter = createChizhikBrowserAdapter({
        listStores: vi.fn(async () => result),
        searchStore,
      });
      await expect(
        adapter.collect({
          document: documentWithGuessedProduct(),
          url: PAGE_URL,
          observedAt: OBSERVED_AT,
          resourceUrls: ["https://app.chizhik.club/api/v1/catalog/unauthorized/products/"],
        }),
      ).resolves.toEqual({ status: "missing-context", observations: [] });
      expect(searchStore).not.toHaveBeenCalled();
    }
  });

  it("does not fabricate product offers from DOM or passive resource names", async () => {
    const searchStore = vi.fn(async () => ({ status: "unavailable" as const }));
    const adapter = createChizhikBrowserAdapter({
      listStores: vi.fn(async () => VALID_DISCOVERY),
      searchStore,
    });

    await expect(
      adapter.collect({
        document: documentWithGuessedProduct(),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: ["https://app.chizhik.club/api/v1/catalog/unauthorized/products/"],
      }),
    ).resolves.toEqual({ status: "observation-only", observations: [] });
    expect(searchStore).not.toHaveBeenCalled();
  });
});
