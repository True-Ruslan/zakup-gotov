import { describe, expect, it } from "vitest";

import {
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
});
