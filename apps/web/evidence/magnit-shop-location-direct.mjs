const SEARCH_URL = 'https://magnit.ru/webgate/v1/stores-facade/search';
const STORE_TYPES = ['MM', 'GM', 'DG', 'MO', 'ME', 'MC', 'DARKSTORE', 'MM_MINI', 'ZARYAD'];

const BBOXES = {
  // Exact coarse viewport observed from the ordinary public /shops browser request.
  observedKrasnodar: {
    leftTopPoint: { latitude: 45.069, longitude: 38.967 },
    rightBottomPoint: { latitude: 45.065, longitude: 38.980 },
  },
  // Public city-scale boxes; no user location/address is involved.
  moscow: {
    leftTopPoint: { latitude: 55.850, longitude: 37.450 },
    rightBottomPoint: { latitude: 55.600, longitude: 37.850 },
  },
  saintPetersburg: {
    leftTopPoint: { latitude: 60.050, longitude: 30.150 },
    rightBottomPoint: { latitude: 59.800, longitude: 30.550 },
  },
};

const HEADER_PROFILES = [
  { name: 'minimal', headers: {} },
  {
    name: 'stable-public-app',
    headers: {
      'x-client-name': 'magnit',
      'x-device-platform': 'Web',
      'x-new-magnit': 'true',
    },
  },
  {
    name: 'observed-public-app-version',
    headers: {
      'x-client-name': 'magnit',
      'x-device-platform': 'Web',
      'x-new-magnit': 'true',
      'x-app-version': '2026.8.12-14.46',
    },
  },
];

function workflowEscape(value) {
  return String(value).replaceAll('%', '%25').replaceAll('\r', '%0D').replaceAll('\n', '%0A');
}

function notice(title, message) {
  console.log(`::notice title=${workflowEscape(title)}::${workflowEscape(message)}`);
}

function bodyFor(bbox) {
  return {
    filters: {
      geo: {
        typeName: 'box',
        leftTopPoint: bbox.leftTopPoint,
        rightBottomPoint: bbox.rightBottomPoint,
      },
      storeTypeListV2: STORE_TYPES,
    },
  };
}

function inspectPayload(root) {
  const stores = new Map();
  const storeCodes = new Set();
  const keys = new Set();
  const seen = new Set();
  let objects = 0;
  let storeCodeObjects = 0;
  let coordinateObjects = 0;
  let storeCodeWithCoordinatesProperty = 0;

  function walk(value) {
    if (!value || typeof value !== 'object') return;
    if (seen.has(value)) return;
    seen.add(value);

    if (Array.isArray(value)) {
      for (const child of value) walk(child);
      return;
    }

    objects++;
    const objectKeys = Object.keys(value);
    for (const key of objectKeys) {
      if (/(shop|store|code|lat|lon|lng|coord|point|cluster)/i.test(key)) keys.add(key);
    }

    const code = value.storeCode;
    if (typeof code === 'string' || typeof code === 'number') {
      storeCodeObjects++;
      storeCodes.add(String(code));
      const coordinates = value.coordinates;
      if (coordinates && typeof coordinates === 'object') {
        storeCodeWithCoordinatesProperty++;
        const latitude = Number(coordinates.latitude);
        const longitude = Number(coordinates.longitude);
        if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
          stores.set(String(code), {
            code: String(code),
            latitude: latitude.toFixed(3),
            longitude: longitude.toFixed(3),
          });
        }
      }
    }

    if (Number.isFinite(Number(value.latitude)) && Number.isFinite(Number(value.longitude))) {
      coordinateObjects++;
    }

    for (const child of Object.values(value)) walk(child);
  }

  walk(root);
  return {
    objects,
    storeCodeObjects,
    coordinateObjects,
    storeCodeWithCoordinatesProperty,
    storeCodes: [...storeCodes].sort(),
    stores: [...stores.values()].sort((a, b) => a.code.localeCompare(b.code)),
    keys: [...keys].sort().slice(0, 60),
  };
}

function fingerprint(stores) {
  return stores.map((store) => `${store.code}@${store.latitude},${store.longitude}`).join('|');
}

async function search(bbox, profile) {
  const response = await fetch(SEARCH_URL, {
    method: 'POST',
    redirect: 'follow',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'User-Agent': 'ZakupGotov-Magnit-Location-Resolver-Probe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)',
      ...profile.headers,
    },
    body: JSON.stringify(bodyFor(bbox)),
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch {
    // Non-JSON failures are retained only as status metadata.
  }
  return {
    status: response.status,
    contentType: response.headers.get('content-type')?.split(';')[0] ?? 'none',
    setCookie: response.headers.has('set-cookie'),
    shape: payload ? inspectPayload(payload) : inspectPayload(null),
  };
}

let acceptedProfile = null;
let firstObserved = null;
for (const profile of HEADER_PROFILES) {
  const result = await search(BBOXES.observedKrasnodar, profile);
  notice(
    'magnit-direct-known-bbox',
    `profile=${profile.name};status=${result.status};contentType=${result.contentType};setCookie=${result.setCookie};objects=${result.shape.objects};storeCodeObjects=${result.shape.storeCodeObjects};coordinateObjects=${result.shape.coordinateObjects};codeWithCoordinates=${result.shape.storeCodeWithCoordinatesProperty};stores=${result.shape.stores.length};sampleCodes=${result.shape.storeCodes.slice(0, 5).join(',') || 'none'};keys=${result.shape.keys.join(',') || 'none'}`,
  );
  if (result.status >= 200 && result.status < 300 && result.shape.stores.length > 0) {
    acceptedProfile = profile;
    firstObserved = result;
    break;
  }
}

if (!acceptedProfile || !firstObserved) {
  throw new Error('known public bbox was not reproducible without browser/session state');
}

// Node fetch has no cookie jar: Set-Cookie from one response is never replayed by the next request.
const secondObserved = await search(BBOXES.observedKrasnodar, acceptedProfile);
const knownStable = fingerprint(firstObserved.shape.stores) === fingerprint(secondObserved.shape.stores);
notice(
  'magnit-direct-known-reproducibility',
  `profile=${acceptedProfile.name};firstStatus=${firstObserved.status};secondStatus=${secondObserved.status};firstStores=${firstObserved.shape.stores.length};secondStores=${secondObserved.shape.stores.length};sameStoreSet=${knownStable};sampleCodes=${firstObserved.shape.storeCodes.slice(0, 5).join(',') || 'none'}`,
);
if (!knownStable || secondObserved.status < 200 || secondObserved.status >= 300) {
  throw new Error('known public bbox was not reproducible across stateless requests');
}

const moscow = await search(BBOXES.moscow, acceptedProfile);
const petersburg = await search(BBOXES.saintPetersburg, acceptedProfile);
notice(
  'magnit-direct-city-boxes',
  `profile=${acceptedProfile.name};moscowStatus=${moscow.status};moscowStores=${moscow.shape.stores.length};moscowCodes=${moscow.shape.storeCodes.slice(0, 5).join(',') || 'none'};petersburgStatus=${petersburg.status};petersburgStores=${petersburg.shape.stores.length};petersburgCodes=${petersburg.shape.storeCodes.slice(0, 5).join(',') || 'none'};differentCitySets=${fingerprint(moscow.shape.stores) !== fingerprint(petersburg.shape.stores)}`,
);

notice(
  'magnit-direct-summary',
  `acceptedProfile=${acceptedProfile.name};noCookieJar=true;knownStable=${knownStable};knownStores=${firstObserved.shape.stores.length};moscowStores=${moscow.shape.stores.length};petersburgStores=${petersburg.shape.stores.length};requestsAtMost=${HEADER_PROFILES.indexOf(acceptedProfile) + 4}`,
);
