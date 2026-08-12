"use server";

import {
  COMPARISON_PREVIEWS_PATH,
  createZakupGotovClient,
  type components,
} from "@zakup-gotov/api-client";

const COMPARISON_PREVIEW_TIMEOUT_MS = 3_000;

export type ComparisonPreviewRequest = components["schemas"]["ComparisonPreviewRequest"];
export type ComparisonPreviewResponse = components["schemas"]["ComparisonPreviewResponse"];
export type ComparisonPreviewValidationError =
  components["schemas"]["ComparisonPreviewValidationError"];

export type ComparisonPreviewState =
  | { kind: "ready"; data: ComparisonPreviewResponse }
  | { kind: "invalid"; errors: ComparisonPreviewValidationError[] }
  | { kind: "unavailable" };

export async function createComparisonPreview(
  request: ComparisonPreviewRequest,
): Promise<ComparisonPreviewState> {
  const baseUrl = process.env.API_BASE_URL;
  if (!baseUrl) {
    return { kind: "unavailable" };
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), COMPARISON_PREVIEW_TIMEOUT_MS);

  try {
    const client = createZakupGotovClient(baseUrl);
    const { data, error, response } = await client.POST(COMPARISON_PREVIEWS_PATH, {
      body: request,
      signal: controller.signal,
    });

    if (data) {
      return { kind: "ready", data };
    }
    if (response.status === 400 && error) {
      return { kind: "invalid", errors: error.errors };
    }
    return { kind: "unavailable" };
  } catch {
    return { kind: "unavailable" };
  } finally {
    clearTimeout(timeout);
  }
}
