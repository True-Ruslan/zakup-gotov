import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

const productionManifest = JSON.parse(
  readFileSync(fileURLToPath(new URL("../static/manifest.json", import.meta.url)), "utf8"),
);
const e2eManifest = JSON.parse(
  readFileSync(fileURLToPath(new URL("../static/manifest.e2e.json", import.meta.url)), "utf8"),
);
const buildScript = readFileSync(
  fileURLToPath(new URL("../scripts/build.mjs", import.meta.url)),
  "utf8",
);

function assertPopupManifest(manifest: Record<string, unknown>): void {
  expect(manifest.permissions).toEqual(["storage"]);
  expect(manifest.host_permissions ?? []).toEqual([]);
  expect(manifest.action).toEqual({
    default_title: "Chizhik D2 schema canary",
    default_popup: "popup.html",
  });
}

describe("Chizhik schema canary popup contract", () => {
  it("declares the same no-permission-widening toolbar popup in production and E2E manifests", () => {
    assertPopupManifest(productionManifest);
    assertPopupManifest(e2eManifest);
  });

  it("packages popup HTML and JavaScript into both extension builds", () => {
    expect(buildScript).toContain('resolve(root, "static/popup.html")');
    expect(buildScript).toContain('resolve(outDir, "popup.html")');
    expect(buildScript).toContain('resolve(root, "static/popup.js")');
    expect(buildScript).toContain('resolve(outDir, "popup.js")');
  });
});
