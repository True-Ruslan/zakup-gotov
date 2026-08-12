const SEARCH_URL = 'https://magnit.ru/webgate/v1/stores-facade/search';
const STORE_TYPES = ['MM', 'GM', 'DG', 'MO', 'ME', 'MC', 'DARKSTORE', 'MM_MINI', 'ZARYAD'];

const BBOXES = {
  observedKrasnodar: {
    leftTopPoint: { latitude: 45.069, longitude: 38.967 },
    rightBottomPoint: { latitude: 45.065, longitude: 38.980 },
  },
  moscow: {
    leftTopPoint: { latitude: 55.850, longitude: 37.450 },
    rightBottomPoint: { latitude: 55.600, longitude: 37.850 },
  },
  saintPetersburg: {
    leftTopPoint: { latitude: 60.050, longitude: 30.150 },
    rightBottomPoint: { latitude: 59.800, longitude: 30.550 },
  },
};

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

function collectStores(payload) {
  const items = payload?.items?.items;
  if (!Array.isArray(items)) return [];

  const stores = [];
  for (const item of items) {
    const code = item?.externalId?.storeCode;
    const latitude = Number(item?.coordinates?.latitude);
    const longitude = Number(item?.coordinates?.longitude);
    if ((typeof code !== 'string' && typeof code !== 'number') || !Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      continue;
    }
    stores.push({
      code: String(code),
      latitude: latitude.toFixed(3),
      longitude: longitude.toFixed(3),
      storeTypeV2: typeof item.storeTypeV2 === 'string' ? item.storeTypeV2 : 'unknown',
    });
  }

  return stores.sort((a, b) => a.code.localeCompare(b.code));
}

function fingerprint(stores) {
  return stores.map((store) => `${store.code}@${store.latitude},${store.longitude}:${store.storeTypeV2}`).join('|');
}

async function search(bbox) {
  const response = await fetch(SEARCH_URL, {
    method: 'POST',
    redirect: 'follow',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'User-Agent': 'ZakupGotov-Magnit-Location-Resolver-Probe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)',
    },
    body: JSON.stringify(bodyFor(bbox)),
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch {
    // Non-JSON response cannot become store evidence.
  }

  return {
    status: response.status,
    contentType: response.headers.get('content-type')?.split(';')[0] ?? 'none',
    setCookie: response.headers.has('set-cookie'),
    stores: payload ? collectStores(payload) : [],
  };
}

const firstKnown = await search(BBOXES.observedKrasnodar);
const secondKnown = await search(BBOXES.observedKrasnodar);
const knownStable = fingerprint(firstKnown.stores) === fingerprint(secondKnown.stores);
notice(
  'magnit-direct-known-reproducibility',
  `headers=minimal;firstStatus=${firstKnown.status};secondStatus=${secondKnown.status};firstSetCookie=${firstKnown.setCookie};secondSetCookie=${secondKnown.setCookie};firstStores=${firstKnown.stores.length};secondStores=${secondKnown.stores.length};sameStoreSet=${knownStable};sampleCodes=${firstKnown.stores.slice(0, 5).map((store) => store.code).join(',') || 'none'}`,
);

if (
  firstKnown.status < 200 || firstKnown.status >= 300 ||
  secondKnown.status < 200 || secondKnown.status >= 300 ||
  firstKnown.stores.length === 0 ||
  !knownStable
) {
  throw new Error('known public bbox was not reproducible across stateless minimal-header requests');
}

const moscow = await search(BBOXES.moscow);
const petersburg = await search(BBOXES.saintPetersburg);
const differentCitySets = fingerprint(moscow.stores) !== fingerprint(petersburg.stores);
notice(
  'magnit-direct-city-boxes',
  `headers=minimal;moscowStatus=${moscow.status};moscowSetCookie=${moscow.setCookie};moscowStores=${moscow.stores.length};moscowCodes=${moscow.stores.slice(0, 5).map((store) => store.code).join(',') || 'none'};petersburgStatus=${petersburg.status};petersburgSetCookie=${petersburg.setCookie};petersburgStores=${petersburg.stores.length};petersburgCodes=${petersburg.stores.slice(0, 5).map((store) => store.code).join(',') || 'none'};differentCitySets=${differentCitySets}`,
);

if (
  moscow.status < 200 || moscow.status >= 300 ||
  petersburg.status < 200 || petersburg.status >= 300 ||
  moscow.stores.length === 0 ||
  petersburg.stores.length === 0 ||
  !differentCitySets
) {
  throw new Error('public city-scale bboxes did not produce distinct non-empty store sets');
}

notice(
  'magnit-direct-summary',
  `publicEndpoint=true;authHeaders=false;appHeaders=false;cookieJar=false;knownStable=${knownStable};knownStores=${firstKnown.stores.length};moscowStores=${moscow.stores.length};petersburgStores=${petersburg.stores.length};requests=4`,
);
