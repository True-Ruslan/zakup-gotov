const SEARCH_URL = 'https://magnit.ru/webgate/v1/stores-facade/search';
const STORE_TYPES = ['MM', 'GM', 'DG', 'MO', 'ME', 'MC', 'DARKSTORE', 'MM_MINI', 'ZARYAD'];

const BBOXES = {
  moscow: {
    leftTopPoint: { latitude: 55.760, longitude: 37.600 },
    rightBottomPoint: { latitude: 55.740, longitude: 37.640 },
  },
  saintPetersburg: {
    leftTopPoint: { latitude: 59.945, longitude: 30.300 },
    rightBottomPoint: { latitude: 59.925, longitude: 30.340 },
  },
};

const HEADER_PROFILES = [
  {
    name: 'minimal',
    headers: {},
  },
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

function collectStores(root) {
  const result = new Map();
  const seen = new Set();

  function walk(value) {
    if (!value || typeof value !== 'object') return;
    if (seen.has(value)) return;
    seen.add(value);

    if (Array.isArray(value)) {
      for (const child of value) walk(child);
      return;
    }

    const code = value.storeCode;
    const coordinates = value.coordinates;
    const latitude = coordinates && typeof coordinates === 'object' ? Number(coordinates.latitude) : NaN;
    const longitude = coordinates && typeof coordinates === 'object' ? Number(coordinates.longitude) : NaN;
    if ((typeof code === 'string' || typeof code === 'number') && Number.isFinite(latitude) && Number.isFinite(longitude)) {
      result.set(String(code), {
        code: String(code),
        latitude: latitude.toFixed(3),
        longitude: longitude.toFixed(3),
      });
    }

    for (const child of Object.values(value)) walk(child);
  }

  walk(root);
  return [...result.values()].sort((a, b) => a.code.localeCompare(b.code));
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
    stores: payload ? collectStores(payload) : [],
  };
}

let acceptedProfile = null;
let firstMoscow = null;
for (const profile of HEADER_PROFILES) {
  const result = await search(BBOXES.moscow, profile);
  notice(
    'magnit-direct-profile',
    `profile=${profile.name};status=${result.status};contentType=${result.contentType};setCookie=${result.setCookie};stores=${result.stores.length};sampleCodes=${result.stores.slice(0, 5).map((store) => store.code).join(',') || 'none'}`,
  );
  if (result.status >= 200 && result.status < 300 && result.stores.length > 0) {
    acceptedProfile = profile;
    firstMoscow = result;
    break;
  }
}

if (!acceptedProfile || !firstMoscow) {
  throw new Error('no stateless public header profile produced storeCode + coordinates');
}

// Node fetch has no cookie jar: the second request is independent and does not replay Set-Cookie.
const secondMoscow = await search(BBOXES.moscow, acceptedProfile);
const moscowStable = fingerprint(firstMoscow.stores) === fingerprint(secondMoscow.stores);
notice(
  'magnit-direct-reproducibility',
  `profile=${acceptedProfile.name};firstStatus=${firstMoscow.status};secondStatus=${secondMoscow.status};firstStores=${firstMoscow.stores.length};secondStores=${secondMoscow.stores.length};sameStoreSet=${moscowStable};firstFingerprintCount=${firstMoscow.stores.length}`,
);

if (!moscowStable || secondMoscow.status < 200 || secondMoscow.status >= 300) {
  throw new Error('stateless Moscow store search was not reproducible');
}

const petersburg = await search(BBOXES.saintPetersburg, acceptedProfile);
const differentLocationSet = fingerprint(firstMoscow.stores) !== fingerprint(petersburg.stores);
notice(
  'magnit-direct-location-dependence',
  `profile=${acceptedProfile.name};moscowStores=${firstMoscow.stores.length};petersburgStatus=${petersburg.status};petersburgStores=${petersburg.stores.length};differentStoreSet=${differentLocationSet};petersburgSampleCodes=${petersburg.stores.slice(0, 5).map((store) => store.code).join(',') || 'none'}`,
);

if (petersburg.status < 200 || petersburg.status >= 300 || petersburg.stores.length === 0 || !differentLocationSet) {
  throw new Error('coarse public bbox did not produce location-dependent store evidence');
}

notice(
  'magnit-direct-summary',
  `acceptedProfile=${acceptedProfile.name};noCookieJar=true;moscowStable=${moscowStable};locationDependent=${differentLocationSet};requestsAtMost=${HEADER_PROFILES.indexOf(acceptedProfile) + 3}`,
);
