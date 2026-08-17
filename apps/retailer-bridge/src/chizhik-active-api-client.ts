export const CHIZHIK_SHOPS_ENDPOINT = "https://app.chizhik.club/api/v1/shops/";
const REQUEST_TIMEOUT_MS = 8_000;

export type ChizhikStoreSummary = Readonly<{
  sapId: string;
  longitude: number;
  latitude: number;
  active: boolean;
  name: string;
  locality: string;
}>;

export type ChizhikStoreDiscoveryResult =
  | Readonly<{ status: "ok"; stores: readonly ChizhikStoreSummary[] }>
  | Readonly<{ status: "unavailable"; stores: readonly [] }>;

export type ChizhikActiveApiClient = Readonly<{
  listStores(): Promise<ChizhikStoreDiscoveryResult>;
}>;

type Fetcher = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;

function isNonBlank(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function isFiniteNumber(value: unknown): value is number {
  return typeof value === "number" && Number.isFinite(value);
}

function isLongitude(value: unknown): value is number {
  return isFiniteNumber(value) && value >= -180 && value <= 180;
}

function isLatitude(value: unknown): value is number {
  return isFiniteNumber(value) && value >= -90 && value <= 90;
}

function projectStore(raw: unknown): ChizhikStoreSummary | null {
  if (typeof raw !== "object" || raw === null || Array.isArray(raw)) return null;

  const record = raw as Record<string, unknown>;
  if (
    !isNonBlank(record.sap_id) ||
    !isLongitude(record.lon) ||
    !isLatitude(record.lat) ||
    typeof record.status !== "number" ||
    !Number.isInteger(record.status) ||
    !isNonBlank(record.name) ||
    !isNonBlank(record.locality)
  ) {
    return null;
  }

  return {
    sapId: record.sap_id,
    longitude: record.lon,
    latitude: record.lat,
    active: record.status === 1,
    name: record.name,
    locality: record.locality,
  };
}

export function createChizhikActiveApiClient(
  fetcher: Fetcher = globalThis.fetch.bind(globalThis),
): ChizhikActiveApiClient {
  return {
    async listStores(): Promise<ChizhikStoreDiscoveryResult> {
      const controller = new AbortController();
      const deadline = globalThis.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
      try {
        const response = await fetcher(CHIZHIK_SHOPS_ENDPOINT, {
          method: "GET",
          mode: "cors",
          credentials: "same-origin",
          headers: { Accept: "application/json, text/plain, */*" },
          signal: controller.signal,
        });

        if (!response.ok) return { status: "unavailable", stores: [] };

        const contentType = response.headers.get("content-type")?.toLowerCase() ?? "";
        if (!contentType.startsWith("application/json")) {
          return { status: "unavailable", stores: [] };
        }

        const payload: unknown = await response.json();
        if (!Array.isArray(payload)) return { status: "unavailable", stores: [] };

        const stores: ChizhikStoreSummary[] = [];
        for (const raw of payload) {
          const store = projectStore(raw);
          if (!store) return { status: "unavailable", stores: [] };
          stores.push(store);
        }

        return { status: "ok", stores };
      } catch {
        return { status: "unavailable", stores: [] };
      } finally {
        globalThis.clearTimeout(deadline);
      }
    },
  };
}
