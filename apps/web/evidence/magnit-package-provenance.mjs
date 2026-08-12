const targets = [
  {
    id: 'known-weight-example',
    url: 'https://magnit.ru/product/3042670099-makarony_makfa_vitki_450g?shopCode=683800&shopType=1',
  },
  {
    id: 'fixed-corpus-pasta',
    url: 'https://magnit.ru/product/1000166929-makarony_magnit_spagetti_500g?shopCode=139147&shopType=1',
  },
  {
    id: 'known-volume-example',
    url: 'https://magnit.ru/product/1000273122-voda_aqua_minerale_pitevaya_negazirovannaya_500ml',
  },
  {
    id: 'known-multidimensional-milk',
    url: 'https://magnit.ru/product/1000548435-kaloriya_moloko_pitevoe_ultrapast_2_5_1000ml',
  },
];

function workflowEscape(value) {
  return String(value).replaceAll('%', '%25').replaceAll('\r', '%0D').replaceAll('\n', '%0A');
}

function notice(title, message) {
  console.log(`::notice title=${workflowEscape(title)}::${workflowEscape(message)}`);
}

function scripts(raw) {
  const result = [];
  const pattern = /<script\b([^>]*)>([\s\S]*?)<\/script>/gi;
  let match;
  let index = 0;
  while ((match = pattern.exec(raw)) !== null) {
    const attrs = match[1];
    const body = match[2];
    const type = attrs.match(/\btype=["']([^"']+)["']/i)?.[1] ?? 'default';
    result.push({ index, type, body });
    index++;
  }
  return result;
}

function summarizeProduct(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return null;
  const type = value['@type'];
  const types = Array.isArray(type) ? type : [type];
  if (!types.includes('Product')) return null;
  return {
    sku: value.sku ?? 'missing',
    weight: scalar(value.weight),
    volume: scalar(value.volume),
    width: scalar(value.width),
    height: scalar(value.height),
    depth: scalar(value.depth),
    size: scalar(value.size),
    additionalProperty: additionalProperties(value.additionalProperty),
  };
}

function scalar(value) {
  if (value === undefined) return 'missing';
  if (value === null) return 'null';
  if (typeof value === 'number' || typeof value === 'boolean') return String(value);
  if (typeof value === 'string') return value.length <= 64 ? value : `string(${value.length})`;
  if (Array.isArray(value)) return `array(${value.length})`;
  if (typeof value === 'object') {
    const keys = Object.keys(value).slice(0, 12).join(',');
    const unitCode = value.unitCode ?? value.unitText ?? 'none';
    const nestedValue = value.value ?? value.maxValue ?? value.minValue ?? 'none';
    return `object(keys=${keys};value=${nestedValue};unit=${unitCode})`;
  }
  return typeof value;
}

function additionalProperties(value) {
  if (!Array.isArray(value)) return value === undefined ? 'missing' : scalar(value);
  return value.slice(0, 20).map((item) => {
    if (!item || typeof item !== 'object') return scalar(item);
    const name = item.name ?? item.propertyID ?? 'none';
    const val = item.value ?? item.valueReference ?? 'none';
    const unit = item.unitCode ?? item.unitText ?? 'none';
    return `${name}:${scalar(val)}:${unit}`;
  }).join('|') || 'empty';
}

function walkJsonLd(root, visitor) {
  const stack = [root];
  const seen = new Set();
  while (stack.length) {
    const value = stack.pop();
    if (!value || typeof value !== 'object') continue;
    if (seen.has(value)) continue;
    seen.add(value);
    visitor(value);
    if (Array.isArray(value)) {
      for (const child of value) stack.push(child);
    } else {
      for (const child of Object.values(value)) stack.push(child);
    }
  }
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
  const allScripts = scripts(raw);
  notice(`${target.id}:raw`, `status=${response.status};bytes=${Buffer.byteLength(raw)};scripts=${allScripts.length}`);

  let products = 0;
  for (const script of allScripts) {
    if (!/ld\+json/i.test(script.type)) continue;
    try {
      const parsed = JSON.parse(script.body);
      walkJsonLd(parsed, (node) => {
        const product = summarizeProduct(node);
        if (!product) return;
        products++;
        notice(`${target.id}:jsonld-product`,
          `script=${script.index};sku=${product.sku};weight=${product.weight};volume=${product.volume};size=${product.size};additionalProperty=${product.additionalProperty}`);
      });
    } catch {
      notice(`${target.id}:jsonld-parse-failed`, `script=${script.index};chars=${script.body.length}`);
    }
  }
  notice(`${target.id}:summary`, `jsonldProducts=${products}`);
}
