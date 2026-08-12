const targets = [
  {
    id: 'known-multidimensional-milk',
    url: 'https://magnit.ru/product/1000548435-kaloriya_moloko_pitevoe_ultrapast_2_5_1000ml',
  },
  {
    id: 'known-count-example',
    url: 'https://magnit.ru/product/1000246228-leto_yaytsa_kurinoe_kategoriya_pervaya_10_0_65_kg_kartonnaya_upakovka_ooo_belyanka_16',
  },
];

function escapeWorkflow(value) {
  return String(value).replaceAll('%', '%25').replaceAll('\r', '%0D').replaceAll('\n', '%0A');
}
function notice(title, message) {
  console.log(`::notice title=${escapeWorkflow(title)}::${escapeWorkflow(message)}`);
}
function scripts(raw) {
  const result = [];
  const pattern = /<script\b([^>]*)>([\s\S]*?)<\/script>/gi;
  let match;
  let index = 0;
  while ((match = pattern.exec(raw)) !== null) {
    const attrs = match[1];
    const type = attrs.match(/\btype=["']([^"']+)["']/i)?.[1] ?? 'default';
    result.push({ index, type, body: match[2] });
    index++;
  }
  return result;
}
function scalar(value) {
  if (value === undefined) return 'missing';
  if (value === null) return 'null';
  if (['string', 'number', 'boolean'].includes(typeof value)) return String(value).replaceAll(/\s+/g, '_');
  if (Array.isArray(value)) return `array(${value.length})`;
  return `object(${Object.keys(value).slice(0, 10).join(',')})`;
}
function additionalProperties(value) {
  if (!Array.isArray(value)) return value === undefined ? 'missing' : scalar(value);
  return value.slice(0, 24).map((item) => {
    if (!item || typeof item !== 'object') return scalar(item);
    return `${scalar(item.name ?? item.propertyID ?? 'none')}:${scalar(item.value ?? 'none')}:${scalar(item.unitCode ?? item.unitText ?? 'none')}`;
  }).join('|') || 'empty';
}
function walk(root, visitor) {
  const stack = [root];
  const seen = new Set();
  while (stack.length) {
    const value = stack.pop();
    if (!value || typeof value !== 'object' || seen.has(value)) continue;
    seen.add(value);
    visitor(value);
    if (Array.isArray(value)) stack.push(...value);
    else stack.push(...Object.values(value));
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
  notice(`${target.id}:raw`, `status=${response.status};bytes=${Buffer.byteLength(raw)}`);
  let products = 0;
  for (const script of scripts(raw)) {
    if (!/ld\+json/i.test(script.type)) continue;
    try {
      walk(JSON.parse(script.body), (node) => {
        const types = Array.isArray(node['@type']) ? node['@type'] : [node['@type']];
        if (!types.includes('Product')) return;
        products++;
        notice(`${target.id}:jsonld-product`,
          `script=${script.index};sku=${scalar(node.sku)};weight=${scalar(node.weight)};volume=${scalar(node.volume)};additionalProperty=${additionalProperties(node.additionalProperty)}`);
      });
    } catch {
      notice(`${target.id}:jsonld-parse-failed`, `script=${script.index};chars=${script.body.length}`);
    }
  }
  notice(`${target.id}:summary`, `jsonldProducts=${products}`);
}
