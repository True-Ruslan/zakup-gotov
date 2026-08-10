import { copyFile, mkdir, readFile, rm, writeFile } from "node:fs/promises";
import { createRequire } from "node:module";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const e2e = process.argv.includes("--e2e");
const outDir = resolve(root, e2e ? "dist-e2e" : "dist");

const requireFromWeb = createRequire(resolve(root, "../web/package.json"));
const ts = requireFromWeb("typescript");
const contentSource = await readFile(resolve(root, "src/content.ts"), "utf8");
const transpiled = ts.transpileModule(contentSource, {
  compilerOptions: {
    target: ts.ScriptTarget.ES2022,
    module: ts.ModuleKind.None,
    strict: true,
  },
  fileName: "content.ts",
  reportDiagnostics: true,
});

const errors = (transpiled.diagnostics ?? []).filter(
  (diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error,
);
if (errors.length > 0) {
  const host = {
    getCanonicalFileName: (fileName) => fileName,
    getCurrentDirectory: () => root,
    getNewLine: () => "\n",
  };
  throw new Error(ts.formatDiagnosticsWithColorAndContext(errors, host));
}

await rm(outDir, { recursive: true, force: true });
await mkdir(outDir, { recursive: true });
await writeFile(resolve(outDir, "content.js"), transpiled.outputText, "utf8");
await copyFile(
  resolve(root, e2e ? "static/manifest.e2e.json" : "static/manifest.json"),
  resolve(outDir, "manifest.json"),
);
await copyFile(resolve(root, "static/service-worker.js"), resolve(outDir, "service-worker.js"));
