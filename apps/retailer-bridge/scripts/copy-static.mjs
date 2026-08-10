import { copyFile, mkdir } from "node:fs/promises";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const e2e = process.argv.includes("--e2e");
const outDir = resolve(root, e2e ? "dist-e2e" : "dist");

await mkdir(outDir, { recursive: true });
await copyFile(
  resolve(root, e2e ? "static/manifest.e2e.json" : "static/manifest.json"),
  resolve(outDir, "manifest.json"),
);
await copyFile(resolve(root, "static/service-worker.js"), resolve(outDir, "service-worker.js"));
