import {
  RETAILERS_PATH,
  createZakupGotovClient,
  type components,
} from "@zakup-gotov/api-client";

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

  try {
    const client = createZakupGotovClient(baseUrl);
    const { data, error } = await client.GET(RETAILERS_PATH);

    if (error || !data || data.retailers.length === 0) {
      return { kind: "unavailable" };
    }

    return { kind: "ready", data };
  } catch {
    return { kind: "unavailable" };
  }
}
