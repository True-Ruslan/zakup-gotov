// @vitest-environment jsdom

import { describe, expect, it, vi } from "vitest";
import {
  chizhikBrowserAdapter,
  createChizhikBrowserAdapter,
} from "../src/adapters/chizhik-browser-adapter";

const OBSERVED_AT = "2026-08-18T00:15:00Z";
const PAGE_URL = new URL("https://chizhik.club/catalog/chay-kofe--264C39224/#fragment");
const HD87_RESOURCE =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search?mode=store&q=cola";

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
    {
      sapId: "HD88",
      longitude: 37.80898339,
      latitude: 55.39666279,
      active: true,
      name: "Домодедово, Вокзальная ул., Строение 2г",
      locality: "Домодедово",
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

  it("reports observation-only only after a delivery resource identifies one validated store", async () => {
    const listStores = vi.fn(async () => VALID_DISCOVERY);
    const searchStore = vi.fn(async () => ({ status: "unavailable" as const }));
    const adapter = createChizhikBrowserAdapter({ listStores, searchStore });

    await expect(
      adapter.collect({
        document: documentWithGuessedProduct(),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: [HD87_RESOURCE],
      }),
    ).resolves.toEqual({ status: "observation-only", observations: [] });
    expect(listStores).toHaveBeenCalledTimes(1);
    expect(searchStore).not.toHaveBeenCalled();
  });

  it("fails closed when no evidenced store context is present", async () => {
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
    ).resolves.toEqual({ status: "missing-context", observations: [] });
    expect(searchStore).not.toHaveBeenCalled();
  });

  it("ignores foreign-origin and unknown-store delivery resource candidates", async () => {
    for (const resourceUrls of [
      ["https://app.chizhik.club.evil.example/delivery/api/catalog/v3/stores/HD87/search"],
      ["https://app.chizhik.club/delivery/api/catalog/v3/stores/UNKNOWN/search"],
      ["https://app.chizhik.club/delivery/api/profile/v1/me"],
    ]) {
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
          resourceUrls,
        }),
      ).resolves.toEqual({ status: "missing-context", observations: [] });
      expect(searchStore).not.toHaveBeenCalled();
    }
  });

  it("fails closed when retained delivery resources conflict across validated stores", async () => {
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
        resourceUrls: [
          HD87_RESOURCE,
          "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD88/categories/drinks/products",
        ],
      }),
    ).resolves.toEqual({ status: "missing-context", observations: [] });
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
      resourceUrls: [HD87_RESOURCE],
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
          resourceUrls: [HD87_RESOURCE],
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
        resourceUrls: [HD87_RESOURCE],
      }),
    ).resolves.toEqual({ status: "observation-only", observations: [] });
    expect(searchStore).not.toHaveBeenCalled();
  });
});
