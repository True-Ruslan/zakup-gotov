import { describe, expect, it, vi } from "vitest";
import {
  CHIZHIK_SCHEMA_CANARY_REQUEST,
  createChizhikSchemaCanaryMessageHandler,
  formatChizhikSchemaCanaryEvidence,
} from "../src/chizhik-schema-canary-message";

const PASS_RESULT = {
  status: "pass" as const,
  httpStatus: 200,
  contentType: "application/json; charset=utf-8",
  rootType: "object" as const,
  schema: [
    { path: "$", type: "object" as const, fields: { products: "array" as const } },
    { path: "$.products", type: "array" as const },
    {
      path: "$.products[]",
      type: "object" as const,
      fields: { sku: "string" as const, name: "string" as const, price: "number" as const },
    },
  ],
};

const MISSING_CONTEXT_RESULT = {
  status: "missing-context" as const,
  diagnostics: {
    appOriginSeen: true,
    deliveryApiSeen: true,
    deliveryCatalogSeen: true,
    deliveryOrdersSeen: false,
    storeScopedV2V3Seen: false,
    storeScopedOtherVersionSeen: true,
    storeScopedCategoriesInoutSeen: false,
    pageOriginDeliverySeen: false,
  },
};

describe("Chizhik schema canary message contract", () => {
  it("formats PASS as exactly two sanitized evidence lines", () => {
    expect(formatChizhikSchemaCanaryEvidence(PASS_RESULT)).toBe(
      'CHIZHIK_D2 status=PASS search_http_status=200 content_type=application/json root=object\n' +
        'CHIZHIK_D2_SCHEMA=[{"path":"$","type":"object","fields":{"products":"array"}},{"path":"$.products","type":"array"},{"path":"$.products[]","type":"object","fields":{"sku":"string","name":"string","price":"number"}}]',
    );
  });

  it("formats missing context with fixed privacy-safe route-family diagnostics only", () => {
    const evidence = formatChizhikSchemaCanaryEvidence(MISSING_CONTEXT_RESULT);
    expect(evidence).toBe(
      "CHIZHIK_D2 status=MISSING_CONTEXT\n" +
        "CHIZHIK_D2_DIAG app_origin=SEEN delivery_api=SEEN delivery_catalog=SEEN " +
        "delivery_orders=NOT_SEEN store_v2_v3=NOT_SEEN store_other_version=SEEN " +
        "store_categories_inout=NOT_SEEN page_origin_delivery=NOT_SEEN",
    );
    expect(evidence).not.toContain("http");
    expect(evidence).not.toContain("stores/");
    expect(evidence).not.toContain("sap");
    expect(evidence).not.toContain("price");
  });

  it("formats other failures without exception, store, product, or payload details", () => {
    expect(formatChizhikSchemaCanaryEvidence({ status: "wrong-origin" })).toBe(
      "CHIZHIK_D2 status=WRONG_ORIGIN",
    );
    expect(formatChizhikSchemaCanaryEvidence({ status: "stores-unavailable" })).toBe(
      "CHIZHIK_D2 status=STORES_UNAVAILABLE",
    );
    expect(formatChizhikSchemaCanaryEvidence({ status: "search-unavailable" })).toBe(
      "CHIZHIK_D2 status=SEARCH_UNAVAILABLE",
    );
  });

  it("runs only for the explicit user-invoked canary message", async () => {
    const runCanary = vi.fn(async () => PASS_RESULT);
    const handler = createChizhikSchemaCanaryMessageHandler(runCanary);

    await expect(handler({ type: "unrelated" })).resolves.toBeNull();
    expect(runCanary).not.toHaveBeenCalled();

    await expect(handler({ type: CHIZHIK_SCHEMA_CANARY_REQUEST })).resolves.toEqual({
      type: "zg-chizhik-schema-canary-result",
      evidence: formatChizhikSchemaCanaryEvidence(PASS_RESULT),
    });
    expect(runCanary).toHaveBeenCalledTimes(1);
  });
});
