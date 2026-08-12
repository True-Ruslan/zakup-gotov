import { defineConfig } from "@playwright/test";

const webBaseUrl = "http://127.0.0.1:3000";
const mockApiBaseUrl = "http://127.0.0.1:4010";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: true,
  retries: 0,
  reporter: "line",
  use: {
    baseURL: webBaseUrl,
    trace: "retain-on-failure",
  },
  projects: [
    {
      name: "desktop-chromium",
      use: {
        browserName: "chromium",
        viewport: { width: 1440, height: 900 },
      },
    },
    {
      name: "mobile-chromium",
      use: {
        browserName: "chromium",
        viewport: { width: 390, height: 844 },
        isMobile: true,
        hasTouch: true,
      },
    },
  ],
  webServer: [
    {
      command: "node e2e/mock-api.mjs",
      url: `${mockApiBaseUrl}/health`,
      reuseExistingServer: false,
      timeout: 120_000,
    },
    {
      command: "pnpm start",
      url: webBaseUrl,
      reuseExistingServer: false,
      timeout: 120_000,
      env: {
        API_BASE_URL: mockApiBaseUrl,
        NEXT_TELEMETRY_DISABLED: "1",
      },
    },
  ],
});
