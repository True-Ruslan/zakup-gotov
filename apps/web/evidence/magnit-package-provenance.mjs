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

function workflowEscape(value) {
  return String(value)
    .replaceAll('%', '%25')
    .replaceAll('\r', '%0D')
    .replaceAll('\n', '%0A');
}

function notice(title, message) {
  console.log(`::notice title=${workflowEscape(title)}::${workflowEscape(message)}`);
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

function compactScalar(value) {
  if (value === null) return 'null';
  if (typeof value === 'number' || typeof value === 'boolean') return String(value);
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (/^-?\d+(?:[.,]\d+)?$/.test(trimmed)) return trimmed.replace(',', '.');
    if (labels.has(trimmed)) return trimmed;
    if (trimmed.length <= 48 && /^[\p{L}\p{N} _.,:%+\-/]+$/u.test(trimmed)) return trimmed;
    return `string(${trimmed.length})`;
  }
  if (Array.isArray(value)) return `array(${value.length})`;
  if (typeof value === 'object') return `object(${Object.keys(value).slice(0, 10).join(',')})`;
  return typeof value;
}

function scalarSiblings(object) {
  return Object.entries(object)
    .filter(([, value]) => value === null || ['string', 'number', 'boolean'].includes(typeof value))
    .map(([key, value]) => `${key}=${compactScalar(value)}`)
    .slice(0, 20)
    .join(';');
}

function inspectJson(targetId, scriptIndex, root) {
  const seen = new Set();
  let emitted = 0;

  function emit(kind, path, details) {
    if (emitted >= 24) return;
    emitted++;
    notice(`${targetId}:${kind}`, `script=${scriptIndex};path=${path};${details}`);
  }

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
          const before = i > 0 ? compactScalar(value[i - 1]) : 'none';
          const after = i + 1 < value.length ? compactScalar(value[i + 1]) : 'none';
          emit('label-array', `${path}[${i}]`, `label=${child};before=${before};after=${after}`);
        }
        walk(child, `${path}[${i}]`);
      }
      return;
    }

    if (typeof value !== 'object') return;

    for (const [key, child] of Object.entries(value)) {
      const childPath = `${path}.${key}`;
      if (/^(characteristics|specifications|attributes|weight|mass|volume|capacity)$/i.test(key)) {
        emit('semantic-key', childPath, `key=${key};value=${compactScalar(child)};siblings=${scalarSiblings(value)}`);
      }
      if (typeof child === 'string' && labels.has(child.trim())) {
        emit('exact-label', childPath, `key=${key};label=${child.trim()};siblings=${scalarSiblings(value)};keys=${Object.keys(value).slice(0, 24).join(',')}`);
      }
      walk(child, childPath);
    }
  }

  walk(root, '$');
  notice(`${targetId}:summary`, `script=${scriptIndex};annotations=${emitted}`);
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
  notice(`${target.id}:raw`, `status=${response.status};bytes=${Buffer.byteLength(raw)};jsonScripts=${scripts.length}`);

  for (const script of scripts) {
    try {
      inspectJson(target.id, script.index, JSON.parse(script.body));
    } catch {
      notice(`${target.id}:json-parse-failed`, `script=${script.index};chars=${script.body.length}`);
    }
  }
}
