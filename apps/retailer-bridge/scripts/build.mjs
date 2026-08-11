import { copyFile, mkdir, readFile, readdir, rm, writeFile } from "node:fs/promises";
import { createRequire } from "node:module";
import { relative, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("..", import.meta.url)));
const srcDir = resolve(root, "src");
const e2e = process.argv.includes("--e2e");
const outDir = resolve(root, e2e ? "dist-e2e" : "dist");

const require = createRequire(import.meta.url);
const ts = require("typescript");

async function sourceFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    const path = resolve(directory, entry.name);
    if (entry.isDirectory()) {
      files.push(...(await sourceFiles(path)));
    } else if (entry.isFile() && entry.name.endsWith(".ts") && !entry.name.endsWith(".d.ts")) {
      files.push(path);
    }
  }
  return files.sort();
}

function moduleId(file) {
  const path = relative(srcDir, file).split(sep).join("/").replace(/\.ts$/, "");
  return `./${path}`;
}

function formatDiagnostics(diagnostics) {
  const host = {
    getCanonicalFileName: (fileName) => fileName,
    getCurrentDirectory: () => root,
    getNewLine: () => "\n",
  };
  return ts.formatDiagnosticsWithColorAndContext(diagnostics, host);
}

const modules = [];
for (const file of await sourceFiles(srcDir)) {
  const source = await readFile(file, "utf8");
  const transpiled = ts.transpileModule(source, {
    compilerOptions: {
      target: ts.ScriptTarget.ES2022,
      module: ts.ModuleKind.CommonJS,
      strict: true,
    },
    fileName: file,
    reportDiagnostics: true,
  });
  const errors = (transpiled.diagnostics ?? []).filter(
    (diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error,
  );
  if (errors.length > 0) {
    throw new Error(formatDiagnostics(errors));
  }
  modules.push([moduleId(file), transpiled.outputText]);
}

const moduleTable = modules
  .map(
    ([id, code]) =>
      `${JSON.stringify(id)}: function(module, exports, require) {\n${code}\n}`,
  )
  .join(",\n");

const bundle = `(() => {
  "use strict";
  const modules = {
${moduleTable}
  };
  const cache = new Map();

  function resolveRequest(fromId, request) {
    if (!request.startsWith(".")) {
      throw new Error("Retailer bridge bundle only allows relative imports: " + request);
    }
    const parts = fromId.slice(2).split("/");
    parts.pop();
    for (const segment of request.split("/")) {
      if (segment === "." || segment === "") continue;
      if (segment === "..") parts.pop();
      else parts.push(segment);
    }
    return "./" + parts.join("/").replace(/\\.js$/, "");
  }

  function load(id) {
    if (cache.has(id)) return cache.get(id).exports;
    const factory = modules[id];
    if (!factory) throw new Error("Unknown retailer bridge module: " + id);
    const module = { exports: {} };
    cache.set(id, module);
    factory(module, module.exports, (request) => load(resolveRequest(id, request)));
    return module.exports;
  }

  load("./content");
})();
`;

await rm(outDir, { recursive: true, force: true });
await mkdir(outDir, { recursive: true });
await writeFile(resolve(outDir, "content.js"), bundle, "utf8");
await copyFile(
  resolve(root, e2e ? "static/manifest.e2e.json" : "static/manifest.json"),
  resolve(outDir, "manifest.json"),
);
await copyFile(resolve(root, "static/service-worker.js"), resolve(outDir, "service-worker.js"));
