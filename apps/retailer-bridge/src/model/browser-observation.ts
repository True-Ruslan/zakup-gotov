export type BrowserAvailability = "AVAILABLE" | "UNAVAILABLE" | "UNKNOWN";

export type BrowserObservation = Readonly<{
  schemaVersion: 1;
  retailerId: string;
  sourceProviderId: string;
  sourceMode: "BROWSER_BRIDGE";
  fulfillmentContextId: string;
  sku: string;
  productName: string;
  priceMinor: number;
  currencyCode: "RUB";
  availability: BrowserAvailability;
  observedAt: string;
  sourceReference: string;
  adapterVersion: string;
}>;
