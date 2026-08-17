import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

const workflowPath = fileURLToPath(
  new URL("../../../.github/workflows/provider-live-probe-chizhik-browser.yml", import.meta.url),
);
const workflow = readFileSync(workflowPath, "utf8");
const scriptPath = fileURLToPath(
  new URL("../scripts/chizhik-live-browser-probe.mjs", import.meta.url),
);
const script = readFileSync(scriptPath, "utf8");

describe("Chizhik Phase D stock Chromium live probe", () => {
  it("is owner-only, issue-scoped, opt-in, and minimally privileged", () => {
    expect(workflow).toContain("issue_comment:");
    expect(workflow).toContain("github.event.issue.number == 167");
    expect(workflow).toContain("github.event.comment.user.login == github.repository_owner");
    expect(workflow).toContain("/provider-probe chizhik-browser");
    expect(workflow).toContain("permissions:\n  contents: read\n  statuses: write");
    expect(workflow).not.toMatch(/(?:packages|issues|pull-requests|id-token):\s*write/);
  });

  it("uses stock Chromium and emits only sanitized Phase D evidence", () => {
    expect(workflow).toContain("playwright install --with-deps chromium");
    expect(workflow).toContain("chizhik-live-browser-probe.mjs");
    expect(script).toContain("https://chizhik.club/");
    expect(script).toContain("https://app.chizhik.club/api/v1/shops/");
    expect(script).toContain("CHIZHIK_PHASE_D");
    expect(script).not.toMatch(/camoufox|proxy|user-agent|cookie|authorization/i);
    expect(script).not.toContain("JSON.stringify(data)");
  });
});
