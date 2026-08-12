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

const labels = new Set(['Характеристики', 'Вес, кг', 'Объем, л']);

function safe(value) {
  return String(value).replaceAll(/\s+/g, '_').replaceAll('|', '/');
}

function log(id, phase, fields) {
  const parts = Object.entries(fields).map(([key, value]) => `${key}=${safe(value)}`);
  console.log(`MAGNIT_BOOTSTRAP id=${id} phase=${phase} ${parts.join(' ')}`);
}

function scriptBodies(raw) {
  const scripts = [];
  const pattern = /<script\b([^>]*)>([\s\S]*?)<\/script>/gi;
  let match;
  let index = 0;
  while ((match = pattern.exec(raw)) !== null) {
    const attrs = match[1];
    const body = match[2];
    const type = attrs.match(/\btype=["']([^"']+)["']/i)?.[1] ?? 'default';
    if (/json/i.test(type)) scripts.push({ index, type, body });
    index++;
  }
  return scripts;
}

function scalarSummary(object) {
  return Object.entries(object)
    .filter(([, value]) => value === null || ['string', 'number', 'boolean'].includes(typeof value))
    .map(([key, value]) => {
      if (typeof value === 'string' && value.length > 80 && !labels.has(value)) return `${key}=string-len:${value.length}`;
      return `${key}=${safe(value)}`;
    })
    .slice(0, 16)
    .join(',') || 'none';
}

function inspectJson(id, scriptIndex, root) {
  const seen = new Set();
  let labelHits = 0;
  let keyHits = 0;

  function walk(value, path) {
    if (value === null || value === undefined) return;
    if (typeof value === 'object') {
      if (seen.has(value)) return;
      seen.add(value);
    }

    if (Array.isArray(value)) {
      for (let i = 0; i < value.length; i++) {
        const child = value[i];
        if (typeof child === 'string' && labels.has(child)) {
          labelHits++;
          const before = i > 0 && ['string', 'number'].includes(typeof value[i - 1]) ? safe(value[i - 1]) : 'none';
          const after = i + 1 < value.length && ['string', 'number'].includes(typeof value[i + 1]) ? safe(value[i + 1]) : 'none';
          log(id, 'json-label-array', { script: scriptIndex, path: `${path}[${i}]`, label: child, before, after });
        }
        walk(child, `${path}[${i}]`);
      }
      return;
    }

    if (typeof value !== 'object') return;

    for (const [key, child] of Object.entries(value)) {
      const childPath = `${path}.${key}`;
      const lower = key.toLowerCase();
      if (/^(characteristics|specifications|attributes|weight|mass|volume|capacity)$/i.test(lower)) {
        keyHits++;
        log(id, 'json-key', {
          script: scriptIndex,
          path: childPath,
          key,
          type: Array.isArray(child) ? 'array' : typeof child,
          value: child === null || ['string', 'number', 'boolean'].includes(typeof child) ? child : 'structured',
          parentScalars: scalarSummary(value),
        });
      }
      if (typeof child === 'string' && labels.has(child)) {
        labelHits++;
        log(id, 'json-label-object', {
          script: scriptIndex,
          path: childPath,
          key,
          label: child,
          parentScalars: scalarSummary(value),
          parentKeys: Object.keys(value).slice(0, 20).join(','),
        });
      }
      walk(child, childPath);
    }
  }

  walk(root, '$');
  log(id, 'json-summary', { script: scriptIndex, labelHits, keyHits });
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
  const scripts = scriptBodies(raw);
  log(target.id, 'raw', { status: response.status, bytes: Buffer.byteLength(raw), jsonScripts: scripts.length });

  for (const script of scripts) {
    try {
      inspectJson(target.id, script.index, JSON.parse(script.body));
    } catch {
      log(target.id, 'json-parse-failed', { script: script.index, chars: script.body.length });
    }
  }
}
