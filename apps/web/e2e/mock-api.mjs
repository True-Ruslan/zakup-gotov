import http from "node:http";

const host = "127.0.0.1";
const port = 4010;

const retailerNames = {
  pyaterochka: "Пятёрочка",
  perekrestok: "Перекрёсток",
  chizhik: "Чижик",
  magnit: "Магнит",
  lenta: "Лента",
  vkusvill: "ВкусВилл",
  "ozon-fresh": "Ozon Fresh",
  samokat: "Самокат",
};

function canonicalQuantity(quantity) {
  if (quantity.unit === "KILOGRAM") {
    return { amount: quantity.amount * 1000, unit: "GRAM" };
  }
  if (quantity.unit === "LITER") {
    return { amount: quantity.amount * 1000, unit: "MILLILITER" };
  }
  return quantity;
}

function readJson(request) {
  return new Promise((resolve, reject) => {
    let body = "";
    request.setEncoding("utf8");
    request.on("data", (chunk) => {
      body += chunk;
      if (body.length > 1_000_000) {
        reject(new Error("request too large"));
        request.destroy();
      }
    });
    request.on("end", () => {
      try {
        resolve(JSON.parse(body));
      } catch (error) {
        reject(error);
      }
    });
    request.on("error", reject);
  });
}

function writeJson(response, status, payload) {
  response.writeHead(status, { "content-type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(payload));
}

function requestedItem(item) {
  return {
    id: item.id,
    requirement: item.requirement,
    quantity: canonicalQuantity(item.quantity),
  };
}

function selection(item, productName, packageQuantity, packageCount, lineTotal) {
  const requested = canonicalQuantity(item.quantity);
  return {
    productName,
    packageQuantity,
    packageCount,
    coveredQuantity: requested,
    lineTotal,
    currencyCode: "RUB",
  };
}

function itemResult(item, status, options = {}) {
  return {
    id: item.id,
    requirement: item.requirement,
    requestedQuantity: canonicalQuantity(item.quantity),
    status,
    candidateProductNames: options.candidateProductNames ?? [],
    ...(options.selection ? { selection: options.selection } : {}),
  };
}

function unavailable(id, reason, coverage = "CONNECTED", productionAccess = "READY") {
  return {
    id,
    displayName: retailerNames[id],
    coverage,
    productionAccess,
    comparisonStatus: "UNAVAILABLE",
    reasons: [reason],
    items: [],
  };
}

function buildPreview(request) {
  const [milk, eggs] = request.items;
  const milkSelection = selection(
    milk,
    "Молоко",
    { amount: 1000, unit: "MILLILITER" },
    2,
    200,
  );
  const eggsSelection = selection(
    eggs,
    "Яйца",
    { amount: 10, unit: "PIECE" },
    1,
    120,
  );

  return {
    locality: request.locality.trim(),
    items: request.items.map(requestedItem),
    retailers: [
      {
        id: "pyaterochka",
        displayName: retailerNames.pyaterochka,
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "READY",
        reasons: [],
        total: { amount: 320, currencyCode: "RUB" },
        freshness: {
          basis: "OBSERVATION_ONLY",
          observedAt: "2026-08-12T10:00:00Z",
        },
        items: [
          itemResult(milk, "FULFILLED", { selection: milkSelection }),
          itemResult(eggs, "FULFILLED", { selection: eggsSelection }),
        ],
      },
      {
        id: "perekrestok",
        displayName: retailerNames.perekrestok,
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "UNCERTAIN",
        reasons: ["AVAILABILITY_UNKNOWN"],
        total: { amount: 330, currencyCode: "RUB" },
        freshness: {
          basis: "PROVIDER_TIMESTAMP",
          observedAt: "2026-08-12T10:00:00Z",
          providerUpdatedAt: "2026-08-12T09:55:00Z",
        },
        items: [
          itemResult(milk, "AVAILABILITY_UNKNOWN", {
            selection: { ...milkSelection, lineTotal: 205 },
          }),
          itemResult(eggs, "FULFILLED", {
            selection: { ...eggsSelection, lineTotal: 125 },
          }),
        ],
      },
      unavailable("chizhik", "DATA_NOT_AVAILABLE"),
      {
        id: "magnit",
        displayName: retailerNames.magnit,
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "INCOMPLETE",
        reasons: ["PACKAGE_QUANTITY_UNKNOWN"],
        items: [
          itemResult(milk, "PACKAGE_QUANTITY_UNKNOWN", {
            candidateProductNames: ["Молоко"],
          }),
          itemResult(eggs, "FULFILLED", { selection: eggsSelection }),
        ],
      },
      {
        id: "lenta",
        displayName: retailerNames.lenta,
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "INCOMPLETE",
        reasons: ["ITEM_UNMATCHED"],
        items: [itemResult(milk, "UNMATCHED")],
      },
      {
        id: "vkusvill",
        displayName: retailerNames.vkusvill,
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "INCOMPLETE",
        reasons: ["ITEM_AMBIGUOUS"],
        items: [
          itemResult(milk, "AMBIGUOUS", {
            candidateProductNames: ["Молоко", "Молоко"],
          }),
        ],
      },
      {
        id: "ozon-fresh",
        displayName: retailerNames["ozon-fresh"],
        coverage: "CONNECTED",
        productionAccess: "READY",
        comparisonStatus: "INCOMPLETE",
        reasons: ["QUANTITY_UNIT_MISMATCH"],
        items: [
          itemResult(milk, "QUANTITY_UNIT_MISMATCH", {
            candidateProductNames: ["Молоко"],
          }),
        ],
      },
      unavailable("samokat", "SOURCE_UNAVAILABLE"),
    ],
  };
}

const server = http.createServer(async (request, response) => {
  if (request.method === "GET" && request.url === "/health") {
    response.writeHead(204);
    response.end();
    return;
  }

  if (request.method !== "POST" || request.url !== "/api/v1/comparison-previews") {
    writeJson(response, 404, { error: "not found" });
    return;
  }

  try {
    const body = await readJson(request);
    if (body.locality === "Недоступно") {
      writeJson(response, 503, { error: "deterministic unavailable scenario" });
      return;
    }
    if (!Array.isArray(body.items) || body.items.length < 2) {
      writeJson(response, 400, {
        type: "https://zakup-gotov.dev/problems/invalid-comparison-preview",
        title: "Invalid comparison preview request",
        status: 400,
        code: "INVALID_COMPARISON_PREVIEW",
        errors: [{ field: "items", message: "deterministic acceptance requires two items" }],
      });
      return;
    }
    writeJson(response, 200, buildPreview(body));
  } catch {
    writeJson(response, 400, {
      type: "https://zakup-gotov.dev/problems/invalid-comparison-preview",
      title: "Invalid comparison preview request",
      status: 400,
      code: "INVALID_COMPARISON_PREVIEW",
      errors: [{ field: "$request", message: "malformed JSON request" }],
    });
  }
});

server.listen(port, host, () => {
  process.stdout.write(`deterministic comparison API listening on http://${host}:${port}\n`);
});

function close() {
  server.close(() => process.exit(0));
}

process.on("SIGINT", close);
process.on("SIGTERM", close);
