import { chromium } from '@playwright/test';

const TARGET = 'https://magnit.ru/shops';
const QUERY = 'Москва, Красная площадь, 1';

function workflowEscape(value) {
  return String(value).replaceAll('%', '%25').replaceAll('\r', '%0D').replaceAll('\n', '%0A');
}

function notice(title, message) {
  console.log(`::notice title=${workflowEscape(title)}::${workflowEscape(message)}`);
}

function queryNames(url) {
  return [...new URL(url).searchParams.keys()].sort().join(',') || 'none';
}

function bodyShape(request) {
  const raw = request.postData();
  if (!raw) return 'none';
  try {
    return sanitize(JSON.parse(raw));
  } catch {
    return `non-json(${raw.length})`;
  }
}

function sanitize(value, key = 'root', depth = 0) {
  if (depth > 5) return 'depth-limit';
  if (value === null) return 'null';
  if (Array.isArray(value)) return `array(${value.length})[${value.slice(0, 5).map((item) => sanitize(item, key, depth + 1)).join(',')}]`;
  if (typeof value === 'object') {
    return `object(${Object.entries(value).slice(0, 30).map(([childKey, child]) => `${childKey}:${sanitize(child, childKey, depth + 1)}`).join(',')})`;
  }
  if (/query|search|text|address|name|token|cookie|value/i.test(key)) return `${typeof value}:redacted`;
  if (/^(lat|latitude|lon|lng|long|longitude)$/i.test(key)) {
    const number = Number(value);
    return Number.isFinite(number) ? `coordinate:${number.toFixed(3)}` : `${typeof value}:invalid-coordinate`;
  }
  if (/^(storeCode|shopCode|storeType|storeTypeV2|typeName|offset|size|sortBy|sortType)$/i.test(key)) return `${typeof value}:${value}`;
  return typeof value;
}

function jsonShape(root) {
  const seen = new Set();
  const keys = new Set();
  let objects = 0;
  let coordinateObjects = 0;
  let storeCodeObjects = 0;
  let addressLikeObjects = 0;

  function walk(value) {
    if (!value || typeof value !== 'object' || seen.has(value)) return;
    seen.add(value);
    if (Array.isArray(value)) {
      for (const child of value) walk(child);
      return;
    }
    objects++;
    const objectKeys = Object.keys(value);
    for (const key of objectKeys) {
      if (/(lat|lon|lng|coord|address|city|locality|region|settlement|store|shop|code|point)/i.test(key)) keys.add(key);
    }
    if (objectKeys.some((key) => /address|city|locality|settlement/i.test(key))) addressLikeObjects++;
    const lat = objectKeys.find((key) => /^(lat|latitude)$/i.test(key));
    const lon = objectKeys.find((key) => /^(lon|lng|long|longitude)$/i.test(key));
    if (lat && lon) coordinateObjects++;
    if (objectKeys.some((key) => /^(storeCode|shopCode)$/i.test(key))) storeCodeObjects++;
    for (const child of Object.values(value)) walk(child);
  }

  walk(root);
  return `objects=${objects};coordinateObjects=${coordinateObjects};storeCodeObjects=${storeCodeObjects};addressLikeObjects=${addressLikeObjects};keys=${[...keys].sort().slice(0, 80).join(',') || 'none'}`;
}

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  locale: 'ru-RU',
  userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/151 Safari/537.36',
});
const page = await context.newPage();
const seen = new Set();

page.on('request', (request) => {
  let url;
  try { url = new URL(request.url()); } catch { return; }
  if (!/(address|geo|suggest|stores-facade\/search)/i.test(url.pathname)) return;
  const signature = `req:${request.method()}:${url.hostname}:${url.pathname}:${queryNames(request.url())}`;
  if (seen.has(signature)) return;
  seen.add(signature);
  notice('magnit-landmark-request', `host=${url.hostname};firstParty=${url.hostname === 'magnit.ru'};method=${request.method()};path=${url.pathname};queryNames=${queryNames(request.url())};body=${bodyShape(request)}`);
});

page.on('response', async (response) => {
  let url;
  try { url = new URL(response.url()); } catch { return; }
  if (!/(address|geo|suggest|stores-facade\/search)/i.test(url.pathname)) return;
  const signature = `res:${response.request().method()}:${url.hostname}:${url.pathname}:${queryNames(response.url())}:${response.status()}`;
  if (seen.has(signature)) return;
  seen.add(signature);
  const contentType = response.headers()['content-type'] ?? '';
  let shape = 'unparsed';
  if (/json/i.test(contentType)) {
    try { shape = jsonShape(await response.json()); } catch { shape = 'json-parse-failed'; }
  }
  notice('magnit-landmark-response', `host=${url.hostname};firstParty=${url.hostname === 'magnit.ru'};method=${response.request().method()};path=${url.pathname};queryNames=${queryNames(response.url())};status=${response.status()};contentType=${contentType.split(';')[0] || 'none'};${shape}`);
});

const navigation = await page.goto(TARGET, { waitUntil: 'domcontentloaded', timeout: 45_000 });
notice('magnit-landmark-navigation', `status=${navigation?.status() ?? 0}`);
await page.waitForTimeout(4_000);

const input = page.getByPlaceholder('Найти магазин по адресу');
if (await input.count() !== 1) throw new Error('expected exactly one public address input');
await input.click();
await input.pressSequentially(QUERY, { delay: 120 });
await page.waitForTimeout(7_000);

const options = page.locator('[role="option"]');
const optionCount = await options.count();
notice('magnit-landmark-options', `count=${optionCount};inputLength=${(await input.inputValue()).length}`);

if (optionCount > 0) {
  await options.first().click();
} else {
  await input.press('ArrowDown').catch(() => {});
  await input.press('Enter').catch(() => {});
}
await page.waitForTimeout(7_000);
notice('magnit-landmark-after-selection', `urlPath=${new URL(page.url()).pathname}`);

await context.close();
await browser.close();
