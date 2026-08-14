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

function normalizeText(value) {
  return String(value).trim().replace(/\s+/g, " ");
}

function canonicalQuantity(quantity) {
  if (quantity.unit === "KILOGRAM") {
    return { amount: quantity.amount * 1000, unit: "GRAM" };
  }
  if (quantity.unit === "LITER") {
    return { amount: quantity.amount * 1000, unit: "MILLILITER" };
  }
  return { amount: quantity.amount, unit: quantity.unit };
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

function selection(item, productName, packageQuantity, packagePrice) {
  const requested = canonicalQuantity(item.quantity);
  const packageCount =
    requested.unit === packageQuantity.unit
      ? Math.max(1, Math.ceil(requested.amount / packageQuantity.amount))
      : 1;
  return {
    productName,
    packageQuantity,
    packageCount,
    coveredQuantity: {
      amount: packageQuantity.amount * packageCount,
      unit: packageQuantity.unit,
    },
    lineTotal: packagePrice * packageCount,
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
    100,
  );
  const eggsSelection = selection(
    eggs,
    "Яйца",
    { amount: 10, unit: "PIECE" },
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
        total: {
          amount: milkSelection.lineTotal + eggsSelection.lineTotal,
          currencyCode: "RUB",
        },
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
        total: {
          amount: milkSelection.lineTotal + eggsSelection.lineTotal + 10,
          currencyCode: "RUB",
        },
        freshness: {
          basis: "PROVIDER_TIMESTAMP",
          observedAt: "2026-08-12T10:00:00Z",
          providerUpdatedAt: "2026-08-12T09:55:00Z",
        },
        items: [
          itemResult(milk, "AVAILABILITY_UNKNOWN", {
            selection: { ...milkSelection, lineTotal: milkSelection.lineTotal + 5 },
          }),
          itemResult(eggs, "FULFILLED", {
            selection: { ...eggsSelection, lineTotal: eggsSelection.lineTotal + 5 },
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

function fixedUuid(prefix, index = 1) {
  return `${prefix}0000000-0000-0000-0000-${String(index).padStart(12, "0")}`;
}

function buildRecipeComparisonPreview(request) {
  const recipe = request.recipe;
  const scale = recipe.targetServings / recipe.baseServings;
  const recipeId = fixedUuid("1");
  const shoppingListId = fixedUuid("3");

  const sourceIngredients = recipe.ingredients.map((ingredient, index) => ({
    id: fixedUuid("2", index + 1),
    requirement: normalizeText(ingredient.requirement),
    quantity: canonicalQuantity(ingredient.quantity),
  }));

  const groups = new Map();
  for (const ingredient of sourceIngredients) {
    const key = `${ingredient.requirement}\u0000${ingredient.quantity.unit}`;
    const scaledAmount = ingredient.quantity.amount * scale;
    const existing = groups.get(key);
    if (existing) {
      existing.quantity.amount += scaledAmount;
      existing.sourceIngredientIds.push(ingredient.id);
    } else {
      groups.set(key, {
        requirement: ingredient.requirement,
        quantity: { amount: scaledAmount, unit: ingredient.quantity.unit },
        sourceIngredientIds: [ingredient.id],
      });
    }
  }

  const shoppingItems = Array.from(groups.values()).map((item, index) => ({
    id: fixedUuid("4", index + 1),
    ...item,
  }));

  return {
    recipeShoppingPreview: {
      recipe: {
        id: recipeId,
        title: normalizeText(recipe.title),
        baseServings: recipe.baseServings,
        targetServings: recipe.targetServings,
        ingredients: sourceIngredients,
      },
      shoppingList: {
        id: shoppingListId,
        items: shoppingItems,
      },
    },
    comparisonPreview: buildPreview({
      locality: request.locality,
      items: shoppingItems,
    }),
  };
}

function invalidComparison(response, message = "deterministic acceptance requires two items") {
  writeJson(response, 400, {
    type: "https://zakup-gotov.dev/problems/invalid-comparison-preview",
    title: "Invalid comparison preview request",
    status: 400,
    code: "INVALID_COMPARISON_PREVIEW",
    errors: [{ field: "items", message }],
  });
}

function invalidRecipeComparison(response, field, message) {
  writeJson(response, 400, {
    type: "https://zakup-gotov.dev/problems/invalid-recipe-comparison-preview",
    title: "Invalid recipe comparison preview request",
    status: 400,
    code: "INVALID_RECIPE_COMPARISON_PREVIEW",
    errors: [{ field, message }],
  });
}

const server = http.createServer(async (request, response) => {
  if (request.method === "GET" && request.url === "/health") {
    response.writeHead(204);
    response.end();
    return;
  }

  if (request.method !== "POST") {
    writeJson(response, 404, { error: "not found" });
    return;
  }

  try {
    const body = await readJson(request);

    if (request.url === "/api/v1/recipe-comparison-previews") {
      if (body.locality === "Недоступно") {
        writeJson(response, 503, { error: "deterministic unavailable scenario" });
        return;
      }
      if (
        !body.recipe ||
        !Array.isArray(body.recipe.ingredients) ||
        body.recipe.ingredients.length < 2
      ) {
        invalidRecipeComparison(
          response,
          "recipe.ingredients",
          "deterministic acceptance requires two ingredients",
        );
        return;
      }
      writeJson(response, 200, buildRecipeComparisonPreview(body));
      return;
    }

    if (request.url === "/api/v1/comparison-previews") {
      if (body.locality === "Недоступно") {
        writeJson(response, 503, { error: "deterministic unavailable scenario" });
        return;
      }
      if (!Array.isArray(body.items) || body.items.length < 2) {
        invalidComparison(response);
        return;
      }
      writeJson(response, 200, buildPreview(body));
      return;
    }

    writeJson(response, 404, { error: "not found" });
  } catch {
    writeJson(response, 400, {
      type: "https://zakup-gotov.dev/problems/invalid-recipe-comparison-preview",
      title: "Invalid recipe comparison preview request",
      status: 400,
      code: "INVALID_RECIPE_COMPARISON_PREVIEW",
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
