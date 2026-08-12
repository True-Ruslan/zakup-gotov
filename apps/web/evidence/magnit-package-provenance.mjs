import { chromium } from '@playwright/test';

const targets = [
  {
    id: 'known-weight-example',
    url: 'https://magnit.ru/product/3042670099-makarony_makfa_vitki_450g?shopCode=683800&shopType=1',
  },
  {
    id: 'fixed-corpus-pasta',
    url: 'https://magnit.ru/product/1000166929-makarony_magnit_spagetti_500g?shopCode=139147&shopType=1',
  },
];

const markers = {
  characteristics: 'Характеристики',
  weight: 'Вес, кг',
  volume: 'Объем, л',
};

function unicodeEscaped(value) {
  return [...value]
    .map((char) => {
      const code = char.codePointAt(0);
      return code > 127 ? `\\u${code.toString(16).padStart(4, '0')}` : char;
    })
    .join('');
}

function markerShape(text) {
  const lower = text.toLowerCase();
  return {
    characteristics: text.includes(markers.characteristics),
    weight: text.includes(markers.weight),
    volume: text.includes(markers.volume),
    escapedCharacteristics: lower.includes(unicodeEscaped(markers.characteristics).toLowerCase()),
    escapedWeight: lower.includes(unicodeEscaped(markers.weight).toLowerCase()),
    escapedVolume: lower.includes(unicodeEscaped(markers.volume).toLowerCase()),
    nextData: text.includes('__NEXT_DATA__'),
    nextFlight: text.includes('self.__next_f.push') || text.includes('__next_f'),
    jsonLd: /application\/ld\+json/i.test(text),
    applicationJson: /application\/json/i.test(text),
    characteristicsKey: /["'](?:characteristics|specifications|attributes)["']\s*:/i.test(text),
    weightKey: /["'](?:weight|mass)["']\s*:/i.test(text),
    volumeKey: /["'](?:volume|capacity)["']\s*:/i.test(text),
  };
}

function log(id, phase, fields) {
  const parts = Object.entries(fields).map(([key, value]) => `${key}=${String(value).replaceAll(' ', '_')}`);
  console.log(`MAGNIT_PROVENANCE id=${id} phase=${phase} ${parts.join(' ')}`);
}

function scriptContainers(raw) {
  const scripts = [];
  const pattern = /<script\b([^>]*)>([\s\S]*?)<\/script>/gi;
  let match;
  let index = 0;
  while ((match = pattern.exec(raw)) !== null) {
    const attrs = match[1];
    const body = match[2];
    const type = attrs.match(/\btype=["']([^"']+)["']/i)?.[1] ?? 'default';
    const id = attrs.match(/\bid=["']([^"']+)["']/i)?.[1] ?? 'none';
    const dataAttrs = [...attrs.matchAll(/\b(data-[\w-]+)(?:=|\s|$)/gi)].map((item) => item[1]).join(',') || 'none';
    const shape = markerShape(body);
    if (
      shape.characteristics ||
      shape.weight ||
      shape.volume ||
      shape.escapedCharacteristics ||
      shape.escapedWeight ||
      shape.escapedVolume ||
      shape.characteristicsKey ||
      shape.weightKey ||
      shape.volumeKey
    ) {
      let jsonParsed = false;
      let rootType = 'none';
      let jsonWeightPaths = [];
      let jsonVolumePaths = [];
      let jsonLabelPaths = [];
      try {
        const parsed = JSON.parse(body);
        jsonParsed = true;
        rootType = Array.isArray(parsed) ? 'array' : typeof parsed;
        const hits = collectJsonHits(parsed);
        jsonWeightPaths = hits.weight.slice(0, 8);
        jsonVolumePaths = hits.volume.slice(0, 8);
        jsonLabelPaths = hits.labels.slice(0, 8);
      } catch {
        // Not every script containing markers is standalone JSON.
      }
      scripts.push({
        index,
        type,
        id,
        dataAttrs,
        chars: body.length,
        jsonParsed,
        rootType,
        weightPaths: jsonWeightPaths.join('|') || 'none',
        volumePaths: jsonVolumePaths.join('|') || 'none',
        labelPaths: jsonLabelPaths.join('|') || 'none',
        ...shape,
      });
    }
    index++;
  }
  return scripts;
}

function collectJsonHits(root) {
  const hits = { weight: [], volume: [], labels: [] };
  const seen = new Set();
  function walk(value, path) {
    if (value === null || value === undefined || seen.has(value)) return;
    if (typeof value === 'object') seen.add(value);

    if (Array.isArray(value)) {
      for (let i = 0; i < value.length; i++) walk(value[i], `${path}[${i}]`);
      return;
    }
    if (typeof value !== 'object') return;

    for (const [key, child] of Object.entries(value)) {
      const childPath = path ? `${path}.${key}` : key;
      const lowerKey = key.toLowerCase();
      if (/(^|_)(weight|mass)(_|$)/i.test(lowerKey)) hits.weight.push(`${childPath}:${scalarShape(child)}`);
      if (/(^|_)(volume|capacity)(_|$)/i.test(lowerKey)) hits.volume.push(`${childPath}:${scalarShape(child)}`);
      if (typeof child === 'string' && (child === markers.weight || child === markers.volume || child === markers.characteristics)) {
        hits.labels.push(`${childPath}:${child.replaceAll(' ', '_')}`);
      }
      walk(child, childPath);
    }
  }
  walk(root, '$');
  return hits;
}

function scalarShape(value) {
  if (value === null) return 'null';
  if (typeof value === 'string') {
    if (/^-?\d+(?:[.,]\d+)?$/.test(value.trim())) return `number-string:${value.trim().replace(',', '.')}`;
    return `string-len:${value.length}`;
  }
  if (typeof value === 'number' || typeof value === 'boolean') return `${typeof value}:${value}`;
  if (Array.isArray(value)) return `array-len:${value.length}`;
  if (typeof value === 'object') return `object-keys:${Object.keys(value).slice(0, 8).join(',')}`;
  return typeof value;
}

for (const target of targets) {
  const response = await fetch(target.url, {
    headers: {
      Accept: 'text/html,application/xhtml+xml',
      'User-Agent': 'ZakupGotov-Magnit-Provenance/0.1 (+https://github.com/True-Ruslan/zakup-gotov)',
    },
    redirect: 'follow',
  });
  const raw = await response.text();
  log(target.id, 'raw', {
    status: response.status,
    bytes: Buffer.byteLength(raw, 'utf8'),
    ...markerShape(raw),
  });
  for (const container of scriptContainers(raw)) {
    log(target.id, 'raw-script-marker', container);
  }
}

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  locale: 'ru-RU',
  userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/140 Safari/537.36',
});

for (const target of targets) {
  const page = await context.newPage();
  const candidateResponses = [];

  page.on('response', async (response) => {
    try {
      const url = new URL(response.url());
      if (!url.hostname.endsWith('magnit.ru')) return;
      const contentType = response.headers()['content-type'] ?? '';
      if (!/json|html|javascript|text/i.test(contentType)) return;
      if (candidateResponses.length >= 30) return;
      const body = await response.text();
      const shape = markerShape(body);
      if (
        shape.characteristics || shape.weight || shape.volume ||
        shape.escapedCharacteristics || shape.escapedWeight || shape.escapedVolume ||
        shape.characteristicsKey || shape.weightKey || shape.volumeKey
      ) {
        candidateResponses.push({
          status: response.status(),
          path: url.pathname,
          contentType: contentType.split(';')[0],
          ...shape,
        });
      }
    } catch {
      // Diagnostic evidence is intentionally best-effort per response.
    }
  });

  const navigation = await page.goto(target.url, { waitUntil: 'domcontentloaded', timeout: 45_000 });
  await page.waitForTimeout(8_000);
  const bodyText = await page.locator('body').innerText().catch(() => '');
  const html = await page.content().catch(() => '');
  log(target.id, 'dom', {
    status: navigation?.status() ?? 0,
    bodyChars: bodyText.length,
    htmlChars: html.length,
    bodyCharacteristics: bodyText.includes(markers.characteristics),
    bodyWeight: bodyText.includes(markers.weight),
    bodyVolume: bodyText.includes(markers.volume),
    htmlCharacteristics: html.includes(markers.characteristics),
    htmlWeight: html.includes(markers.weight),
    htmlVolume: html.includes(markers.volume),
  });

  for (const candidate of candidateResponses) log(target.id, 'response-marker', candidate);
  await page.close();
}

await context.close();
await browser.close();
