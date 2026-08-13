# M2.1 — Deterministic Recipe Domain and Recipe → ShoppingList Conversion

Date: 2026-08-13  
Status: **DESIGN APPROVED / SPEC REVIEW**  
Issue: #93  
Baseline: `7fc38a8ef5c9866c015a3b63f913d7acee3675f7`  
Parent decision: M1 Shopping Core **COMPLETE / ACCEPTED**, GO to M2 Recipes

## 1. Goal

Introduce recipes as a first-class deterministic source of shopping requirements without weakening the accepted M1 Shopping Core boundaries.

The first M2 slice is deliberately domain-only:

`Recipe → explicit ingredients → serving scaling → exact-safe merge → ShoppingList + provenance`

REST/OpenAPI, generated client and web UI are separate follow-up slices after this model is accepted.

## 2. Architectural decision

Use a **separate Recipe aggregate plus a converter into Shopping Core**.

Dependency direction:

```text
recipe domain
    |
    | uses existing ShoppingRequirement / Quantity / ShoppingList types
    v
shopping domain
```

`shopping` must not import or reference `recipe`.

Recipe-specific lineage is not added to `ShoppingItem`. Manual lists therefore remain recipe-agnostic and the M1 aggregate does not gain optional origin fields that only M2 understands.

## 3. Domain model

### 3.1 RecipeId

`RecipeId` is a non-null UUID-backed value object.

### 3.2 RecipeIngredientId

`RecipeIngredientId` is a non-null UUID-backed value object.

Ingredient IDs must be unique inside one recipe. Duplicate IDs fail construction.

### 3.3 RecipeTitle

`RecipeTitle` owns presentation-safe normalization:

- non-null;
- `strip()` leading/trailing whitespace;
- collapse internal whitespace to one space;
- blank after normalization is invalid;
- preserve case and punctuation.

Title normalization is not ingredient semantic normalization.

### 3.4 RecipeServings

`RecipeServings` is a positive integer value object.

The first slice intentionally does not support fractional servings. This keeps scaling input and identity semantics explicit while still allowing non-integer scale ratios such as 1 serving from a 3-serving recipe.

No arbitrary upper bound is introduced without product evidence.

### 3.5 RecipeIngredient

Each ingredient contains:

- `RecipeIngredientId id`;
- existing `ShoppingRequirement requirement`;
- existing `Quantity quantity`.

The Recipe domain does not introduce another unit enum or quantity-normalization implementation. `Quantity` remains authoritative for canonical `kg → g` and `l → ml` conversion and positive-amount validation.

### 3.6 Recipe

`Recipe` contains:

- `RecipeId id`;
- `RecipeTitle title`;
- `RecipeServings baseServings`;
- ordered immutable `List<RecipeIngredient> ingredients`.

Rules:

- recipe must contain at least one ingredient;
- ingredient order is preserved;
- duplicate `RecipeIngredientId` values are rejected;
- duplicate requirements are allowed because the converter owns safe merging.

The initial aggregate is immutable. Mutation commands/persistence are intentionally deferred until an API/storage slice needs them.

## 4. Conversion contract

Introduce a pure converter with no network, database, Spring context or clock dependency.

Input:

- `Recipe recipe`;
- `RecipeServings targetServings`;
- caller-provided `ShoppingListId shoppingListId`.

Output:

`RecipeShoppingListConversion` containing:

1. generated `ShoppingList`;
2. immutable provenance map keyed by generated `ShoppingItemId`.

Each provenance value is an ordered immutable list of `RecipeIngredientRef(recipeId, ingredientId)` so lineage remains self-contained even when future M3 work combines multiple recipes.

The converter does not mutate the input recipe.

## 5. Serving scaling

For each merge group, calculate:

```text
scaledAmount = canonicalBaseAmount * targetServings / baseServings
```

Rules:

1. Use `BigDecimal` only; never `double`/`float`.
2. Sum canonical base amounts inside a merge group **before** scaling. This prevents repeated rounding from changing a merged result.
3. Multiply by target servings before division.
4. If division terminates, keep the exact decimal result.
5. If division is non-terminating, use `MathContext.DECIMAL128` for the division.
6. Construct the resulting existing `Quantity`, which performs the established canonical normalization.

Examples:

- 400 g at 4 → 2 servings = 200 g exactly;
- 0.5 kg at 2 → 4 servings = 1000 g through existing canonicalization;
- 100 g at 3 → 1 serving = deterministic DECIMAL128 representation of 100/3 g.

Formatting/rounding for human recipe presentation is a later UI concern and must not silently alter the shopping requirement in this domain slice.

## 6. Exact-safe ingredient merge

Ingredients merge only when both values are equal **after existing value-object normalization**:

```text
MergeKey = (ShoppingRequirement, canonical QuantityUnit)
```

Consequences:

- `"Молоко"` + `"Молоко"`, both volume → merge;
- `" Молоко   "` + `"Молоко"`, both volume → merge because `ShoppingRequirement` already normalizes whitespace;
- `"Молоко"` + `"молоко"` → do not merge;
- `"томаты"` + `"помидоры"` → do not merge;
- same requirement in grams + kilograms → merge because `Quantity` canonicalizes both to grams;
- same requirement in grams + pieces → do not merge;
- same requirement in milliliters + liters → merge because both canonicalize to milliliters.

No fuzzy matching, synonym dictionary, category inference or AI equivalence is introduced.

Group order follows the first ingredient that introduced each merge key. Provenance order follows original recipe ingredient order.

## 7. Deterministic ShoppingItem identity

The same conversion inputs must produce the same generated shopping-item IDs.

For each merge group, derive `ShoppingItemId` from a canonical UTF-8 identity payload containing:

- `ShoppingListId` UUID;
- normalized requirement text;
- canonical quantity-unit name.

Use Java's deterministic name-based UUID facility over this payload. IDs do not depend on amount, ingredient order after grouping, recipe title or target servings.

Rationale:

- changing servings changes quantity but not the logical shopping requirement identity;
- kg/g input representation does not change identity after canonicalization;
- a caller can intentionally obtain a new identity namespace by supplying a different `ShoppingListId`.

The converter must fail closed if a generated ID collision maps two different merge keys to the same ID, even though such a collision is practically improbable.

## 8. Provenance semantics

For every generated `ShoppingItem` there must be exactly one provenance-map entry.

For a non-merged ingredient:

```text
ShoppingItemId -> [RecipeIngredientRef(recipeId, ingredientId)]
```

For a merged group:

```text
ShoppingItemId -> [ref(first ingredient), ref(second ingredient), ...]
```

No contributing ingredient may be lost or duplicated.

Provenance is read-only output metadata. Shopping Core types remain unchanged.

## 9. Error handling / fail-closed rules

Reject at construction/conversion boundary:

- null IDs/value objects/lists;
- blank recipe title;
- zero/negative servings;
- empty recipe ingredient list;
- duplicate ingredient IDs;
- null ingredients;
- impossible/null conversion inputs;
- generated ShoppingItem ID collision across different merge keys.

Existing `ShoppingRequirement` and `Quantity` continue to reject their own invalid states.

There is no best-effort conversion that silently drops a malformed ingredient.

## 10. Package/module boundary

New production code belongs under a dedicated recipe package/module, e.g.:

```text
io.github.trueruslan.zakupgotov.recipe
```

Allowed direction:

```text
recipe -> shopping
```

Forbidden direction:

```text
shopping -> recipe
```

Add/extend architecture verification so the accepted Shopping Core cannot acquire a Recipe dependency accidentally.

## 11. Testing strategy

Follow RED → GREEN with focused deterministic tests.

### Domain validation

- ID null rejection;
- title whitespace normalization and blank rejection;
- positive servings only;
- non-empty ingredient collection;
- duplicate ingredient-ID rejection;
- immutable ingredient exposure.

### Scaling

- target == base;
- integer scale up/down;
- exact non-integer ratio;
- non-terminating ratio (`1/3`) uses deterministic DECIMAL128;
- kg/g and l/ml reuse existing canonicalization;
- repeated conversion returns equal quantities.

### Merge

- exact normalized requirement + canonical unit merges;
- whitespace-normalized text merges;
- case difference does not merge;
- synonym-like text does not merge;
- unit-dimension mismatch does not merge;
- merged quantity sums before scaling;
- first-occurrence output order is stable.

### Identity

- same list ID + merge key → same item ID;
- servings change → same item ID;
- kg/g representation after canonicalization → same item ID;
- different list ID → different item ID;
- different requirement/unit → different item ID.

### Provenance

- every item has one provenance entry;
- non-merged item points to the exact recipe/ingredient;
- merged item preserves all contributing refs in source order;
- output map/lists are immutable.

### Architecture

- recipe may depend on shopping;
- shopping must not depend on recipe;
- no Spring/network/persistence dependency in converter.

Then run full API verification and repository PR gates.

## 12. Non-goals

Not part of this slice:

- REST endpoints;
- OpenAPI schemas;
- generated TypeScript client;
- recipe web UI;
- persistence/repository layer;
- AI/NLP ingredient extraction;
- arbitrary website import;
- fuzzy/case-insensitive ingredient equivalence;
- nutrition/calorie calculations;
- pantry prediction;
- unit conversion across physical dimensions;
- fractional servings input;
- combining multiple recipes into one list (M3 concern).

## 13. Acceptance criteria

M2.1 domain slice is accepted when:

1. Recipe value objects and immutable aggregate enforce all invariants.
2. Existing Shopping Core quantity/text semantics are reused rather than reimplemented.
3. Serving scaling is deterministic for terminating and non-terminating ratios.
4. Only exact-safe compatible requirements merge.
5. Generated shopping-item identity is deterministic and list-scoped.
6. Complete immutable recipe/ingredient provenance accompanies every generated item.
7. Shopping Core has no dependency on Recipe.
8. Normal CI remains network-free.
9. Full API/repository gates pass on exact PR head.
10. Independent review reports no P0/P1/P2.
11. Merge is followed by green post-merge `main` verification.

## 14. Follow-up after acceptance

The next slice may add the contract/application boundary:

`Recipe request → Recipe domain → RecipeShoppingListConversion → comparison input`

That follow-up will own REST/OpenAPI/generated-client decisions and only then introduce the minimal responsive recipe flow.