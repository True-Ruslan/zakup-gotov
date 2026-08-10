import { copyFile, mkdir, rm } from "node:fs/promises";
import { createRequire } from "node:module";
import { resolve } from "node:path";
import { pathToFileURL, fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const e2e = process.argv.includes("--e2e");
const outDir = resolve(root, e2e ? "dist-e2e" : "dist");

const requireFromVitest = createRequire(
  pathToFileURL(resolve(root, "../web/node_modules/vitest/package.json")),
);
const viteEntry = requireFromVitest.resolve("vite");
const { build } = await import(pathToFileURL(viteEntry).href);

await rm(outDir, { recursive: true, force: true });
await build({
  root,
  configFile: false,
  build: {
    outDir,
    emptyOutDir: true,
    lib: {
      entry: resolve(root, "src/content.ts"),
      name: "ZakupGotovRetailerBridge",
      formats: ["iife"],
      fileName: () => "content.js",
    },
    target: "es2022",
    sourcemap: true,
    minify: false,
  },
});

await mkdir(outDir, { recursive: true });
await copyFile(
  resolve(root, e2e ? "static/manifest.e2e.json" : "static/manifest.json"),
  resolve(outDir, "manifest.json"),
);
await copyFile(resolve(root, "static/service-worker.js"), resolve(outDir, "service-worker.js"));
