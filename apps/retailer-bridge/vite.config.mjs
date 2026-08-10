import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL(".", import.meta.url));
const e2e = process.env.ZG_BRIDGE_E2E === "1";

export default {
  root,
  build: {
    outDir: resolve(root, e2e ? "dist-e2e" : "dist"),
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
};
