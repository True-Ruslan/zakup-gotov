"use client";

import { useState, type FormEvent } from "react";

import type { components } from "@zakup-gotov/api-client";

import {
  createRecipeComparisonPreview,
  type RecipeComparisonPreviewRequest,
  type RecipeComparisonState,
} from "./recipe-comparison";
import { RecipeComparisonResults } from "./recipe-comparison-results";

type QuantityUnit = components["schemas"]["QuantityInputUnit"];

type IngredientRow = {
  key: number;
  requirement: string;
  amount: string;
  unit: QuantityUnit;
};

const units: Array<{ value: QuantityUnit; label: string }> = [
  { value: "PIECE", label: "шт." },
  { value: "GRAM", label: "г" },
  { value: "KILOGRAM", label: "кг" },
  { value: "MILLILITER", label: "мл" },
  { value: "LITER", label: "л" },
];

function newIngredient(key: number): IngredientRow {
  return {
    key,
    requirement: "",
    amount: "1",
    unit: "PIECE",
  };
}

function clientErrors(
  title: string,
  locality: string,
  baseServings: string,
  targetServings: string,
  ingredients: IngredientRow[],
) {
  const errors: string[] = [];

  if (!title.trim()) {
    errors.push("Укажите название рецепта.");
  }
  if (!locality.trim()) {
    errors.push("Укажите населённый пункт.");
  }

  const base = Number(baseServings);
  if (!Number.isInteger(base) || base <= 0) {
    errors.push("Порций в рецепте должно быть целым числом больше 0.");
  }

  const target = Number(targetServings);
  if (!Number.isInteger(target) || target <= 0) {
    errors.push("Нужно порций должно быть целым числом больше 0.");
  }

  for (const [index, ingredient] of ingredients.entries()) {
    if (!ingredient.requirement.trim()) {
      errors.push(
        ingredients.length === 1
          ? "Укажите ингредиент."
          : `Укажите ингредиент в строке ${index + 1}.`,
      );
    }
    const amount = Number(ingredient.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      errors.push(
        ingredients.length === 1
          ? "Количество ингредиента должно быть больше 0."
          : `Количество ингредиента в строке ${index + 1} должно быть больше 0.`,
      );
    }
  }

  return errors;
}

export function RecipeComparisonForm() {
  const [title, setTitle] = useState("");
  const [baseServings, setBaseServings] = useState("2");
  const [targetServings, setTargetServings] = useState("2");
  const [locality, setLocality] = useState("");
  const [ingredients, setIngredients] = useState<IngredientRow[]>(() => [newIngredient(1)]);
  const [state, setState] = useState<RecipeComparisonState | null>(null);
  const [clientMessages, setClientMessages] = useState<string[]>([]);
  const [pending, setPending] = useState(false);

  function updateIngredient(key: number, patch: Partial<IngredientRow>) {
    setIngredients((current) =>
      current.map((ingredient) =>
        ingredient.key === key ? { ...ingredient, ...patch } : ingredient,
      ),
    );
  }

  function removeIngredient(key: number) {
    setIngredients((current) =>
      current.length === 1
        ? current
        : current.filter((ingredient) => ingredient.key !== key),
    );
  }

  function addIngredient() {
    setIngredients((current) => {
      if (current.length >= 100) {
        return current;
      }
      const nextKey = Math.max(...current.map((ingredient) => ingredient.key)) + 1;
      return [...current, newIngredient(nextKey)];
    });
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending) {
      return;
    }

    const validationErrors = clientErrors(
      title,
      locality,
      baseServings,
      targetServings,
      ingredients,
    );
    if (validationErrors.length > 0) {
      setState(null);
      setClientMessages(validationErrors);
      return;
    }

    const request: RecipeComparisonPreviewRequest = {
      locality: locality.trim(),
      recipe: {
        title: title.trim(),
        baseServings: Number(baseServings),
        targetServings: Number(targetServings),
        ingredients: ingredients.map((ingredient) => ({
          requirement: ingredient.requirement.trim(),
          quantity: {
            amount: Number(ingredient.amount),
            unit: ingredient.unit,
          },
        })),
      },
    };

    setClientMessages([]);
    setPending(true);
    try {
      setState(await createRecipeComparisonPreview(request));
    } finally {
      setPending(false);
    }
  }

  const errorContent =
    clientMessages.length > 0
      ? clientMessages.join(" ")
      : state?.kind === "invalid"
        ? state.errors.map((error) => `${error.field}: ${error.message}`).join("; ")
        : state?.kind === "unavailable"
          ? "Не удалось сравнить рецепт. Основной сервис временно недоступен."
          : null;

  return (
    <section aria-labelledby="recipe-comparison" className="mt-12">
      <div className="max-w-2xl">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-stone-500">
          Рецепт → покупки → магазины
        </p>
        <h2
          id="recipe-comparison"
          className="mt-2 text-2xl font-semibold tracking-tight text-stone-950"
        >
          Сравнить рецепт
        </h2>
        <p className="mt-2 text-sm leading-6 text-stone-600">
          Укажите рецепт, число порций и населённый пункт. Сервис сам пересчитает
          ингредиенты в список покупок и сравнит его по доступным данным магазинов.
        </p>
      </div>

      <form onSubmit={submit} className="mt-6 space-y-6" noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label htmlFor="recipe-title" className="block text-sm font-medium text-stone-800">
              Название рецепта
            </label>
            <input
              id="recipe-title"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              maxLength={240}
              autoComplete="off"
              className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-4 py-2 text-base outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
            />
          </div>

          <div>
            <label htmlFor="recipe-base-servings" className="block text-sm font-medium text-stone-800">
              Порций в рецепте
            </label>
            <input
              id="recipe-base-servings"
              type="number"
              inputMode="numeric"
              min="1"
              step="1"
              value={baseServings}
              onChange={(event) => setBaseServings(event.target.value)}
              className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-4 py-2 text-base outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
            />
          </div>

          <div>
            <label htmlFor="recipe-target-servings" className="block text-sm font-medium text-stone-800">
              Нужно порций
            </label>
            <input
              id="recipe-target-servings"
              type="number"
              inputMode="numeric"
              min="1"
              step="1"
              value={targetServings}
              onChange={(event) => setTargetServings(event.target.value)}
              className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-4 py-2 text-base outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
            />
          </div>

          <div className="sm:col-span-2 max-w-xl">
            <label htmlFor="recipe-locality" className="block text-sm font-medium text-stone-800">
              Населённый пункт
            </label>
            <input
              id="recipe-locality"
              value={locality}
              onChange={(event) => setLocality(event.target.value)}
              maxLength={160}
              autoComplete="address-level2"
              className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-4 py-2 text-base outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
            />
          </div>
        </div>

        <div className="space-y-4" aria-label="Ингредиенты рецепта">
          {ingredients.map((ingredient, index) => {
            const requirementId = `recipe-ingredient-${ingredient.key}`;
            const amountId = `recipe-amount-${ingredient.key}`;
            const unitId = `recipe-unit-${ingredient.key}`;
            return (
              <fieldset
                key={ingredient.key}
                className="rounded-2xl border border-stone-200 bg-white p-4 sm:p-5"
              >
                <legend className="px-1 text-sm font-semibold text-stone-800">
                  Ингредиент {index + 1}
                </legend>
                <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_8rem_7rem_auto] sm:items-end">
                  <div>
                    <label htmlFor={requirementId} className="block text-sm font-medium text-stone-700">
                      Ингредиент
                    </label>
                    <input
                      id={requirementId}
                      value={ingredient.requirement}
                      onChange={(event) =>
                        updateIngredient(ingredient.key, { requirement: event.target.value })
                      }
                      maxLength={240}
                      className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
                    />
                  </div>
                  <div>
                    <label htmlFor={amountId} className="block text-sm font-medium text-stone-700">
                      Количество
                    </label>
                    <input
                      id={amountId}
                      type="number"
                      inputMode="decimal"
                      min="0"
                      step="any"
                      value={ingredient.amount}
                      onChange={(event) =>
                        updateIngredient(ingredient.key, { amount: event.target.value })
                      }
                      className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
                    />
                  </div>
                  <div>
                    <label htmlFor={unitId} className="block text-sm font-medium text-stone-700">
                      Единица
                    </label>
                    <select
                      id={unitId}
                      value={ingredient.unit}
                      onChange={(event) =>
                        updateIngredient(ingredient.key, {
                          unit: event.target.value as QuantityUnit,
                        })
                      }
                      className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
                    >
                      {units.map((unit) => (
                        <option key={unit.value} value={unit.value}>
                          {unit.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  <button
                    type="button"
                    onClick={() => removeIngredient(ingredient.key)}
                    disabled={ingredients.length === 1}
                    aria-label="Удалить ингредиент"
                    className="min-h-11 rounded-xl border border-stone-300 px-3 text-sm font-medium text-stone-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:cursor-not-allowed disabled:opacity-40"
                  >
                    Удалить
                  </button>
                </div>
              </fieldset>
            );
          })}
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={addIngredient}
            disabled={ingredients.length >= 100}
            className="min-h-11 rounded-full border border-stone-300 bg-white px-5 py-2.5 text-sm font-medium text-stone-900 hover:bg-stone-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Добавить ингредиент
          </button>
          <button
            type="submit"
            disabled={pending}
            className="min-h-11 rounded-full bg-stone-950 px-5 py-2.5 text-sm font-medium text-white hover:bg-stone-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:cursor-wait disabled:opacity-60"
          >
            {pending ? "Сравниваем…" : "Сравнить рецепт"}
          </button>
        </div>
      </form>

      {errorContent ? (
        <div
          role="alert"
          className="mt-6 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950"
        >
          {errorContent}
        </div>
      ) : null}

      {state?.kind === "ready" ? <RecipeComparisonResults preview={state.data} /> : null}
    </section>
  );
}
