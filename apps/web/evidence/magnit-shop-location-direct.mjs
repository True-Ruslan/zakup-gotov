const SEARCH_URL = 'https://magnit.ru/webgate/v1/stores-facade/search';
const STORE_TYPES = ['MM', 'GM', 'DG', 'MO', 'ME', 'MC', 'DARKSTORE', 'MM_MINI', 'ZARYAD'];
const BBOX = {
  leftTopPoint: { latitude: 45.069, longitude: 38.967 },
  rightBottomPoint: { latitude: 45.065, longitude: 38.980 },
};

function workflowEscape(value) {
  return String(value).replaceAll('%', '%25').replaceAll('\r', '%0D').replaceAll('\n', '%0A');
}

function notice(title, message) {
  console.log(`::notice title=${workflowEscape(title)}::${workflowEscape(message)}`);
}

function safeScalar(key, value) {
  if (/address|name|city|region|token|cookie/i.test(key)) return 'redacted';
  if (/^(storeCode|storeType|storeTypeV2|latitude|longitude|lat|lon|lng)$/i.test(key)) return String(value);
  return typeof value;
}

function inspect(root) {
  const seen = new Set();
  let objectCount = 0;

  function walk(value, path, parentKeys = 'root') {
    if (!value || typeof value !== 'object') return;
    if (seen.has(value)) return;
    seen.add(value);

    if (Array.isArray(value)) {
      notice('magnit-direct-array-shape', `path=${path};length=${value.length};parentKeys=${parentKeys}`);
      for (let index = 0; index < value.length; index++) walk(value[index], `${path}[${index}]`, parentKeys);
      return;
    }

    objectCount++;
    const keys = Object.keys(value);
    const interesting = keys.filter((key) => /(store|code|coord|lat|lon|lng|point|cluster)/i.test(key));
    const scalars = Object.entries(value)
      .filter(([, child]) => child === null || ['string', 'number', 'boolean'].includes(typeof child))
      .filter(([key]) => !/address|name|city|region/i.test(key))
      .slice(0, 20)
      .map(([key, child]) => `${key}=${safeScalar(key, child)}`)
      .join(',') || 'none';

    if (interesting.length || keys.some((key) => /^(latitude|longitude|lat|lon|lng)$/i.test(key))) {
      const coordinateShape = Object.prototype.hasOwnProperty.call(value, 'coordinates')
        ? Array.isArray(value.coordinates)
          ? `array(${value.coordinates.length})`
          : value.coordinates && typeof value.coordinates === 'object'
            ? `object(${Object.keys(value.coordinates).join(',')})`
            : `${typeof value.coordinates}:${value.coordinates}`
        : 'none';
      notice(
        'magnit-direct-object-shape',
        `path=${path};keys=${keys.join(',')};interesting=${interesting.join(',') || 'none'};coordinates=${coordinateShape};scalars=${scalars}`,
      );
    }

    for (const [key, child] of Object.entries(value)) {
      walk(child, `${path}.${key}`, keys.join(','));
    }
  }

  walk(root, '$');
  notice('magnit-direct-shape-summary', `objects=${objectCount}`);
}

const response = await fetch(SEARCH_URL, {
  method: 'POST',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    'User-Agent': 'ZakupGotov-Magnit-Location-Resolver-Probe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)',
  },
  body: JSON.stringify({
    filters: {
      geo: { typeName: 'box', ...BBOX },
      storeTypeListV2: STORE_TYPES,
    },
  }),
});

notice('magnit-direct-known-bbox', `status=${response.status};setCookie=${response.headers.has('set-cookie')};contentType=${response.headers.get('content-type')?.split(';')[0] ?? 'none'}`);
const payload = await response.json();
inspect(payload);
