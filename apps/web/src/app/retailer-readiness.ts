import {
  RETAILERS_PATH,
  createZakupGotovClient,
  type components,
} from "@zakup-gotov/api-client";

const RETAILER_READINESS_TIMEOUT_MS = 3_000;

export type RetailerReadinessResponse =
  components["schemas"]["RetailerReadinessResponse"];

export type RetailerReadinessState =
  | { kind: "ready"; data: RetailerReadinessResponse }
  | { kind: "unavailable" };

export async function loadRetailerReadiness(): Promise<RetailerReadinessState> {
  const baseUrl = process.env.API_BASE_URL;
  if (!baseUrl) {
    return { kind: "unavailable" };
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), RETAILER_READINESS_TIMEOUT_MS);

  try {
    const client = createZakupGotovClient(baseUrl);
    const { data, error } = await client.GET(RETAILERS_PATH, {
      signal: controller.signal,
    });

    if (error || !data || data.retailers.length === 0) {
      return { kind: "unavailable" };
    }

    return { kind: "ready", data };
  } catch {
    return { kind: "unavailable" };
  } finally {
    clearTimeout(timeout);
  }
}
