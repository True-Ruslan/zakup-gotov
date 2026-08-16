import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

const workflowPath = fileURLToPath(
  new URL("../../../.github/workflows/retailer-bridge-ci.yml", import.meta.url),
);
const workflow = readFileSync(workflowPath, "utf8");

describe("Retailer Bridge CI canary artifact", () => {
  it("uploads the verified dist only after Chromium E2E on trusted main pushes", () => {
    const e2eStep = workflow.indexOf("- name: Run bridge Chromium E2E");
    const uploadStep = workflow.indexOf("- name: Upload verified bridge artifact");

    expect(e2eStep).toBeGreaterThanOrEqual(0);
    expect(uploadStep).toBeGreaterThan(e2eStep);
    expect(workflow).toContain(
      "if: ${{ github.event_name == 'push' && github.ref == 'refs/heads/main' }}",
    );
    expect(workflow).toContain(
      "uses: actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02 # v4",
    );
    expect(workflow).toContain("name: retailer-bridge-${{ github.sha }}");
    expect(workflow).toContain("path: apps/retailer-bridge/dist");
    expect(workflow).toContain("if-no-files-found: error");
    expect(workflow).toContain("retention-days: 14");
  });

  it("does not widen workflow permissions for artifact publication", () => {
    expect(workflow).toContain("permissions:\n  contents: read");
    expect(workflow).not.toMatch(/(?:actions|contents|packages|id-token):\s*write/);
  });
});
