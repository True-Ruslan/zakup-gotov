import type {
  ChizhikDeliverySearchRequest,
  ChizhikStoreDiscoveryResult,
} from "./chizhik-active-api-client";
import { fulfillmentContextResource } from "./resource-observation-policy";

const OFFICIAL_PAGE_ORIGIN = "https://chizhik.club";
const CONTEXT_PREFIX = "chizhik:";
const CANARY_QUERY = "кола";
const CANARY_LIMIT = 1;
const SAFE_FIELD = /^[A-Za-z_][A-Za-z0-9_]{0,63}$/;
const MAX_SCHEMA_DEPTH = 5;
const MAX_SCHEMA_NODES = 80;

type JsonValueType = "null" | "array" | "object" | "string" | "number" | "boolean" | "undefined" | "bigint" | "symbol" | "function";

type SchemaNode =
  | Readonly<{ path: string; type: "array" }>
  | Readonly<{
      path: string;
      type: "object";
      fields: Readonly<Record<string, JsonValueType>>;
    }>;

type CanarySearchResult =
  | Readonly<{
      status: "received";
      httpStatus: number;
      contentType: string;
      payload: unknown;
    }>
  | Readonly<{ status: "unavailable" }>;

type CanaryClient = Readonly<{
  listStores(): Promise<ChizhikStoreDiscoveryResult>;
  searchStore(request: ChizhikDeliverySearchRequest): Promise<CanarySearchResult>;
}>;

export type ChizhikSchemaCanaryResult =
  | Readonly<{ status: "wrong-origin" }>
  | Readonly<{ status: "stores-unavailable" }>
  | Readonly<{ status: "missing-context" }>
  | Readonly<{ status: "search-unavailable" }>
  | Readonly<{
      status: "pass";
      httpStatus: number;
      contentType: string;
      rootType: JsonValueType;
      schema: readonly SchemaNode[];
    }>;

export type ChizhikSchemaCanaryInput = Readonly<{
  client: CanaryClient;
  pageUrl: URL;
  resourceUrls: readonly string[];
}>;

function valueType(value: unknown): JsonValueType {
  if (value === null) return "null";
  if (Array.isArray(value)) return "array";
  return typeof value;
}

function evidencedStoreIds(
  resourceUrls: readonly string[],
  pageUrl: URL,
  validStoreIds: ReadonlySet<string>,
): Set<string> {
  const contexts = new Set<string>();
  for (const rawUrl of resourceUrls) {
    const resource = fulfillmentContextResource(rawUrl, pageUrl);
    if (!resource?.contextKey.startsWith(CONTEXT_PREFIX)) continue;

    const sapId = resource.contextKey.slice(CONTEXT_PREFIX.length);
    if (validStoreIds.has(sapId)) contexts.add(sapId);
  }
  return contexts;
}

function summarizeSchema(payload: unknown): readonly SchemaNode[] {
  const schema: SchemaNode[] = [];
  const seenPaths = new Set<string>();

  const visit = (value: unknown, path = "$", depth = 0): void => {
    if (depth > MAX_SCHEMA_DEPTH || schema.length >= MAX_SCHEMA_NODES || seenPaths.has(path)) {
      return;
    }
    seenPaths.add(path);

    if (Array.isArray(value)) {
      schema.push({ path, type: "array" });
      if (value.length > 0) visit(value[0], `${path}[]`, depth + 1);
      return;
    }
    if (typeof value !== "object" || value === null) return;

    const safeEntries = Object.entries(value).filter(([key]) => SAFE_FIELD.test(key));
    schema.push({
      path,
      type: "object",
      fields: Object.fromEntries(safeEntries.map(([key, child]) => [key, valueType(child)])),
    });
    for (const [key, child] of safeEntries) {
      if (typeof child === "object" && child !== null) {
        visit(child, `${path}.${key}`, depth + 1);
      }
    }
  };

  visit(payload);
  return schema;
}

export async function runChizhikSchemaCanary(
  input: ChizhikSchemaCanaryInput,
): Promise<ChizhikSchemaCanaryResult> {
  if (input.pageUrl.origin !== OFFICIAL_PAGE_ORIGIN) {
    return { status: "wrong-origin" };
  }

  const stores = await input.client.listStores();
  if (stores.status !== "ok" || stores.stores.length === 0) {
    return { status: "stores-unavailable" };
  }

  const validStoreIds = new Set(stores.stores.map((store) => store.sapId));
  const contexts = evidencedStoreIds(input.resourceUrls, input.pageUrl, validStoreIds);
  if (contexts.size !== 1) {
    return { status: "missing-context" };
  }

  const [sapId] = contexts;
  if (!sapId) return { status: "missing-context" };

  const search = await input.client.searchStore({
    sapId,
    query: CANARY_QUERY,
    limit: CANARY_LIMIT,
  });
  if (search.status !== "received") {
    return { status: "search-unavailable" };
  }

  return {
    status: "pass",
    httpStatus: search.httpStatus,
    contentType: search.contentType,
    rootType: valueType(search.payload),
    schema: summarizeSchema(search.payload),
  };
}
