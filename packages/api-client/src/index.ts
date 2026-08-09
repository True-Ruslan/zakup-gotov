import createClient from "openapi-fetch";

import type { components, operations, paths } from "./schema";

export const SYSTEM_INFO_PATH = "/api/v1/system" as const;

export function createZakupGotovClient(baseUrl: string) {
  return createClient<paths>({ baseUrl });
}

export type { components, operations, paths };
