// @vitest-environment jsdom

import { describe, expect, it } from "vitest";
import { chizhikBrowserAdapter } from "../src/adapters/chizhik-browser-adapter";

const OBSERVED_AT = "2026-08-17T00:15:00Z";
const PAGE_URL = new URL("https://chizhik.club/deeplink?action_type=to_screen#fragment");

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

describe("chizhikBrowserAdapter", () => {
  it("supports only the explicit official Chizhik HTTPS page origin", () => {
    expect(chizhikBrowserAdapter.supports(new URL("https://chizhik.club/"))).toBe(true);
    expect(chizhikBrowserAdapter.supports(new URL("https://chizhik.club/deeplink"))).toBe(true);

    expect(chizhikBrowserAdapter.supports(new URL("http://chizhik.club/"))).toBe(false);
    expect(chizhikBrowserAdapter.supports(new URL("https://www.chizhik.club/"))).toBe(false);
    expect(chizhikBrowserAdapter.supports(new URL("https://app.chizhik.club/"))).toBe(false);
    expect(chizhikBrowserAdapter.supports(new URL("https://chizhik.club.evil.example/"))).toBe(false);
  });

  it("reports observation-only when a sanitized public catalog resource was browser-observed", () => {
    expect(
      chizhikBrowserAdapter.collect({
        document: documentWithGuessedProduct(),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: [
          "https://app.chizhik.club/api/v1/catalog/unauthorized/categories/",
          "https://app.chizhik.club/api/v1/catalog/unauthorized/products/",
        ],
      }),
    ).toEqual({ status: "observation-only", observations: [] });
  });

  it("does not fabricate offers from guessed DOM fields or unrelated resource URLs", () => {
    expect(
      chizhikBrowserAdapter.collect({
        document: documentWithGuessedProduct(),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: ["https://analytics.example.test/api/v1/catalog/unauthorized/products/"],
      }),
    ).toEqual({ status: "missing-context", observations: [] });
  });
});
