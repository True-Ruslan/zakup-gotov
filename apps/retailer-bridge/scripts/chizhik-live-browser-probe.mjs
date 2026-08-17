import { chromium } from "@playwright/test";

const PAGE_ORIGIN = "https://chizhik.club/";
const PAGE_URL = `${PAGE_ORIGIN}catalog/`;
const SHOPS_ENDPOINT = "https://app.chizhik.club/api/v1/shops/";
const PAGE_TIMEOUT_MS = 20_000;
const FETCH_TIMEOUT_MS = 8_000;

function evidence(fields) {
  const values = Object.entries(fields).map(([key, value]) => `${key}=${String(value)}`);
  console.log(`CHIZHIK_PHASE_D ${values.join(" ")}`);
}

function validStoreShape(value) {
  return (
    value &&
    typeof value === "object" &&
    typeof value.sap_id === "string" &&
    value.sap_id.trim().length > 0 &&
    typeof value.lat === "number" &&
    Number.isFinite(value.lat) &&
    typeof value.lon === "number" &&
    Number.isFinite(value.lon) &&
    Number.isInteger(value.status) &&
    typeof value.name === "string" &&
    value.name.trim().length > 0 &&
    typeof value.locality === "string" &&
    value.locality.trim().length > 0
  );
}

const browser = await chromium.launch({ headless: true });
try {
  const context = await browser.newContext();
  const page = await context.newPage();

  let pageResponse;
  try {
    pageResponse = await page.goto(PAGE_URL, {
      waitUntil: "domcontentloaded",
      timeout: PAGE_TIMEOUT_MS,
    });
  } catch {
    evidence({
      status: "PAGE_UNAVAILABLE",
      page_http_status: -1,
      shops_http_status: -1,
      json: false,
      array: false,
      nonempty: false,
      store_shape: false,
    });
    process.exitCode = 0;
    await context.close();
    process.exit();
  }

  const pageStatus = pageResponse?.status() ?? -1;
  if (pageStatus < 200 || pageStatus >= 400) {
    evidence({
      status: "PAGE_UNAVAILABLE",
      page_http_status: pageStatus,
      shops_http_status: -1,
      json: false,
      array: false,
      nonempty: false,
      store_shape: false,
    });
    await context.close();
    process.exit();
  }

  const result = await page.evaluate(
    async ({ endpoint, timeoutMs }) => {
      const controller = new AbortController();
      const deadline = setTimeout(() => controller.abort(), timeoutMs);
      try {
        const response = await fetch(endpoint, {
          method: "GET",
          mode: "cors",
          credentials: "same-origin",
          headers: { Accept: "application/json, text/plain, */*" },
          signal: controller.signal,
        });
        const contentType = response.headers.get("content-type")?.toLowerCase() ?? "";
        if (!response.ok || !contentType.startsWith("application/json")) {
          return {
            status: "HTTP_UNAVAILABLE",
            httpStatus: response.status,
            json: false,
            payload: null,
          };
        }

        try {
          return {
            status: "RECEIVED",
            httpStatus: response.status,
            json: true,
            payload: await response.json(),
          };
        } catch {
          return {
            status: "INVALID_JSON",
            httpStatus: response.status,
            json: false,
            payload: null,
          };
        }
      } catch {
        return {
          status: "FETCH_UNAVAILABLE",
          httpStatus: -1,
          json: false,
          payload: null,
        };
      } finally {
        clearTimeout(deadline);
      }
    },
    { endpoint: SHOPS_ENDPOINT, timeoutMs: FETCH_TIMEOUT_MS },
  );

  const isArray = Array.isArray(result.payload);
  const nonempty = isArray && result.payload.length > 0;
  const storeShape = nonempty && result.payload.every(validStoreShape);
  const status =
    result.status === "RECEIVED" && result.httpStatus === 200 && result.json && storeShape
      ? "PASS"
      : result.status === "RECEIVED"
        ? "SHAPE_INVALID"
        : result.status;

  evidence({
    status,
    page_http_status: pageStatus,
    shops_http_status: result.httpStatus,
    json: result.json,
    array: isArray,
    nonempty,
    store_shape: Boolean(storeShape),
  });

  await context.close();
} finally {
  await browser.close();
}
