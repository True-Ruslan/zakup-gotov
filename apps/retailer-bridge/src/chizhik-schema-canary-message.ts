import type { ChizhikSchemaCanaryResult } from "./chizhik-schema-canary";

export const CHIZHIK_SCHEMA_CANARY_REQUEST = "zg-chizhik-schema-canary";
export const CHIZHIK_SCHEMA_CANARY_RESULT = "zg-chizhik-schema-canary-result";

export type ChizhikSchemaCanaryMessageResult = Readonly<{
  type: typeof CHIZHIK_SCHEMA_CANARY_RESULT;
  evidence: string;
}>;

type RunCanary = () => Promise<ChizhikSchemaCanaryResult>;

function baseContentType(contentType: string): string {
  const [base] = contentType.split(";", 1);
  return base?.trim() || "unknown";
}

export function formatChizhikSchemaCanaryEvidence(
  result: ChizhikSchemaCanaryResult,
): string {
  switch (result.status) {
    case "pass":
      return (
        `CHIZHIK_D2 status=PASS search_http_status=${result.httpStatus} ` +
        `content_type=${baseContentType(result.contentType)} root=${result.rootType}\n` +
        `CHIZHIK_D2_SCHEMA=${JSON.stringify(result.schema)}`
      );
    case "wrong-origin":
      return "CHIZHIK_D2 status=WRONG_ORIGIN";
    case "stores-unavailable":
      return "CHIZHIK_D2 status=STORES_UNAVAILABLE";
    case "missing-context":
      return "CHIZHIK_D2 status=MISSING_CONTEXT";
    case "search-unavailable":
      return "CHIZHIK_D2 status=SEARCH_UNAVAILABLE";
  }
}

function isCanaryRequest(message: unknown): boolean {
  return (
    typeof message === "object" &&
    message !== null &&
    !Array.isArray(message) &&
    (message as { type?: unknown }).type === CHIZHIK_SCHEMA_CANARY_REQUEST
  );
}

export function createChizhikSchemaCanaryMessageHandler(runCanary: RunCanary) {
  return async (message: unknown): Promise<ChizhikSchemaCanaryMessageResult | null> => {
    if (!isCanaryRequest(message)) return null;

    const result = await runCanary();
    return {
      type: CHIZHIK_SCHEMA_CANARY_RESULT,
      evidence: formatChizhikSchemaCanaryEvidence(result),
    };
  };
}
