export type AdapterFailureStatus =
  | "unsupported-page"
  | "missing-context"
  | "missing-product"
  | "malformed-state"
  | "observation-only";

export type AdapterResult =
  | { status: "ok"; observations: ReadonlyArray<Record<string, unknown>> }
  | { status: AdapterFailureStatus; observations: [] };

export interface RetailerBrowserAdapter {
  readonly adapterId: string;
  readonly retailerId: string;
  supports(url: URL): boolean;
  collect(input: {
    document: Document;
    url: URL;
    observedAt: string;
    resourceUrls?: readonly string[];
  }): AdapterResult | Promise<AdapterResult>;
}
