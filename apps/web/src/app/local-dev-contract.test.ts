import { readFileSync } from "node:fs";

import { describe, expect, it } from "vitest";

type PackageJson = {
  scripts?: Record<string, string>;
};

const packageJson = JSON.parse(
  readFileSync(new URL("../../package.json", import.meta.url), "utf8"),
) as PackageJson;

describe("local web development contract", () => {
  it("builds the generated API client before starting Next dev", () => {
    expect(packageJson.scripts?.dev).toBe(
      "pnpm --filter @zakup-gotov/api-client build && next dev",
    );
  });
});
