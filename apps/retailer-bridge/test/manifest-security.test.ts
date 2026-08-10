import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

const manifestPath = fileURLToPath(new URL("../static/manifest.json", import.meta.url));
const manifest = JSON.parse(readFileSync(manifestPath, "utf8"));

describe("retailer bridge manifest", () => {
  it("uses Manifest V3 with the minimum production permissions", () => {
    expect(manifest.manifest_version).toBe(3);
    expect(manifest.permissions).toEqual(["storage"]);
    expect(manifest.host_permissions ?? []).toEqual([]);
    expect(manifest.content_scripts).toEqual([
      expect.objectContaining({
        matches: ["https://www.perekrestok.ru/*"],
        js: ["content.js"],
        run_at: "document_idle",
        world: "ISOLATED",
      }),
    ]);
    expect(JSON.stringify(manifest)).not.toMatch(
      /cookies|webRequest|debugger|proxy|declarativeNetRequest/i,
    );
  });
});
