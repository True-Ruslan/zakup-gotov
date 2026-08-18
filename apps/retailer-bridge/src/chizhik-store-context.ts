import { fulfillmentContextResource } from "./resource-observation-policy";

const CONTEXT_PREFIX = "chizhik:";

export function resolveChizhikEvidencedStoreId(
  resourceUrls: readonly string[],
  pageUrl: URL,
  validStoreIds: ReadonlySet<string>,
): string | null {
  const contexts = new Set<string>();

  for (const rawUrl of resourceUrls) {
    const resource = fulfillmentContextResource(rawUrl, pageUrl);
    if (!resource?.contextKey.startsWith(CONTEXT_PREFIX)) continue;

    const sapId = resource.contextKey.slice(CONTEXT_PREFIX.length);
    if (validStoreIds.has(sapId)) contexts.add(sapId);
  }

  if (contexts.size !== 1) return null;
  return contexts.values().next().value ?? null;
}
