import { act } from "react";
import { hydrateRoot, type Root } from "react-dom/client";
import { renderToString } from "react-dom/server";
import { afterEach, describe, expect, it, vi } from "vitest";

import { ComparisonPreviewForm } from "./comparison-preview-form";

vi.mock("./comparison-preview", () => ({
  createComparisonPreview: vi.fn(),
}));

afterEach(() => {
  document.body.innerHTML = "";
  vi.restoreAllMocks();
});

describe("comparison preview form hydration", () => {
  it("hydrates the initial form without server/client row identity drift", async () => {
    const randomUUID = vi
      .spyOn(globalThis.crypto, "randomUUID")
      .mockReturnValueOnce("00000000-0000-4000-8000-000000000001")
      .mockReturnValueOnce("00000000-0000-4000-8000-000000000002");
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => undefined);

    const serverHtml = renderToString(<ComparisonPreviewForm />);
    const container = document.createElement("div");
    container.innerHTML = serverHtml;
    document.body.append(container);

    let root: Root | undefined;
    await act(async () => {
      root = hydrateRoot(container, <ComparisonPreviewForm />);
    });

    const hydrationWarnings = consoleError.mock.calls
      .map((args) => args.map(String).join(" "))
      .filter((message) => /hydrated|hydration/i.test(message));

    expect(randomUUID).not.toHaveBeenCalled();
    expect(hydrationWarnings).toEqual([]);

    await act(async () => {
      root?.unmount();
    });
  });
});
