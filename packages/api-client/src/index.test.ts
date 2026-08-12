import { describe, expect, it } from "vitest";

import {
  COMPARISON_PREVIEWS_PATH,
  RETAILERS_PATH,
  SYSTEM_INFO_PATH,
  createZakupGotovClient,
} from "./index";
import type { paths } from "./schema";

describe("Zakup Gotov API client", () => {
  it("exposes the generated system endpoint through a typed client", () => {
    const path: keyof paths = SYSTEM_INFO_PATH;
    const client = createZakupGotovClient("https://api.example.test");

    expect(path).toBe("/api/v1/system");
    expect(client.GET).toBeTypeOf("function");
  });

  it("exposes the retailer readiness endpoint through the generated contract", () => {
    const path: keyof paths = RETAILERS_PATH;
    const client = createZakupGotovClient("https://api.example.test");

    expect(path).toBe("/api/v1/retailers");
    expect(client.GET).toBeTypeOf("function");
  });

  it("exposes the comparison preview endpoint through the generated contract", () => {
    const path: keyof paths = COMPARISON_PREVIEWS_PATH;
    const client = createZakupGotovClient("https://api.example.test");
    type PreviewPost = paths["/api/v1/comparison-previews"]["post"];
    const operationExists: PreviewPost | undefined = undefined;

    expect(path).toBe("/api/v1/comparison-previews");
    expect(client.POST).toBeTypeOf("function");
    expect(operationExists).toBeUndefined();
  });
});
