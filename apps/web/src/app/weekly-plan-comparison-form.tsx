"use client";

import { useState, type FormEvent } from "react";

import type { components } from "@zakup-gotov/api-client";
import {
  createWeeklyPlanComparisonPreview,
  type WeeklyPlanComparisonPreviewRequest,
  type WeeklyPlanComparisonState,
} from "./weekly-plan-comparison";
import { WeeklyPlanComparisonResults } from "./weekly-plan-comparison-results";

type QuantityUnit = components["schemas"]["QuantityInputUnit"];
type WeeklyPlanDay = components["schemas"]["WeeklyPlanDay"];

type IngredientRow = {
  key: number;
  requirement: string;
  amount: string;
  unit: QuantityUnit;
};

type OccurrenceRow = {
  key: number;
  day: WeeklyPlanDay;
  targetServings: string;
  title: string;
  baseServings: string;
  ingredients: IngredientRow[];
};

type PantryRow = {
  key: number;
  requirement: string;
  amount: string;
  unit: QuantityUnit;
};

const days: Array<{ value: WeeklyPlanDay; label: string }> = [
  { value: "MONDAY", label: "Понедельник" },
  { value: "TUESDAY", label: "Вторник" },
  { value: "WEDNESDAY", label: "Среда" },
  { value: "THURSDAY", label: "Четверг" },
  { value: "FRIDAY", label: "Пятница" },
  { value: "SATURDAY", label: "Суббота" },
  { value: "SUNDAY", label: "Воскресенье" },
];

const units: Array<{ value: QuantityUnit; label: string }> = [
  { value: "PIECE", label: "шт." },
  { value: "GRAM", label: "г" },
  { value: "KILOGRAM", label: "кг" },
  { value: "MILLILITER", label: "мл" },
  { value: "LITER", label: "л" },
];

function newIngredient(key = 1): IngredientRow {
  return { key, requirement: "", amount: "1", unit: "PIECE" };
}

function newOccurrence(key: number): OccurrenceRow {
  return {
    key,
    day: "MONDAY",
    targetServings: "2",
    title: "",
    baseServings: "2",
    ingredients: [newIngredient()],
  };
}

function newPantryRow(key: number): PantryRow {
  return { key, requirement: "", amount: "1", unit: "PIECE" };
}

function clientErrors(locality: string, occurrences: OccurrenceRow[], pantryRows: PantryRow[]) {
  const errors: string[] = [];
  if (!locality.trim()) errors.push("Укажите населённый пункт.");

  occurrences.forEach((occurrence, occurrenceIndex) => {
    const number = occurrenceIndex + 1;
    const targetServings = Number(occurrence.targetServings);
    const baseServings = Number(occurrence.baseServings);

    if (!occurrence.title.trim()) errors.push(`Укажите название рецепта для блюда ${number}.`);
    if (!Number.isInteger(targetServings) || targetServings <= 0) {
      errors.push(`Нужно порций для блюда ${number} должно быть целым числом больше 0.`);
    }
    if (!Number.isInteger(baseServings) || baseServings <= 0) {
      errors.push(`Порций в рецепте для блюда ${number} должно быть целым числом больше 0.`);
    }

    occurrence.ingredients.forEach((ingredient, ingredientIndex) => {
      if (!ingredient.requirement.trim()) {
        errors.push(
          occurrence.ingredients.length === 1
            ? `Укажите ингредиент для блюда ${number}.`
            : `Укажите ингредиент ${ingredientIndex + 1} для блюда ${number}.`,
        );
      }
      const amount = Number(ingredient.amount);
      if (!Number.isFinite(amount) || amount <= 0) {
        errors.push(`Количество ингредиента ${ingredientIndex + 1} для блюда ${number} должно быть больше 0.`);
      }
    });
  });

  pantryRows.forEach((pantry, pantryIndex) => {
    const number = pantryIndex + 1;
    if (!pantry.requirement.trim()) errors.push(`Укажите продукт для запаса дома ${number}.`);
    const amount = Number(pantry.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      errors.push(`Количество запаса дома ${number} должно быть больше 0.`);
    }
  });

  return errors;
}

export function WeeklyPlanComparisonForm() {
  const [locality, setLocality] = useState("");
  const [occurrences, setOccurrences] = useState<OccurrenceRow[]>(() => [newOccurrence(1)]);
  const [pantryRows, setPantryRows] = useState<PantryRow[]>([]);
  const [state, setState] = useState<WeeklyPlanComparisonState | null>(null);
  const [clientMessages, setClientMessages] = useState<string[]>([]);
  const [pending, setPending] = useState(false);

  function updateOccurrence(key: number, patch: Partial<OccurrenceRow>) {
    setOccurrences((current) => current.map((item) => item.key === key ? { ...item, ...patch } : item));
  }

  function addOccurrence() {
    setOccurrences((current) => {
      if (current.length >= 35) return current;
      const key = Math.max(...current.map((item) => item.key)) + 1;
      return [...current, newOccurrence(key)];
    });
  }

  function removeOccurrence(key: number) {
    setOccurrences((current) => current.length === 1 ? current : current.filter((item) => item.key !== key));
  }

  function moveOccurrence(index: number, delta: -1 | 1) {
    setOccurrences((current) => {
      const target = index + delta;
      if (index < 0 || index >= current.length || target < 0 || target >= current.length) return current;
      const next = [...current];
      [next[index], next[target]] = [next[target]!, next[index]!];
      return next;
    });
  }

  function updateIngredient(occurrenceKey: number, ingredientKey: number, patch: Partial<IngredientRow>) {
    setOccurrences((current) => current.map((occurrence) => {
      if (occurrence.key !== occurrenceKey) return occurrence;
      return {
        ...occurrence,
        ingredients: occurrence.ingredients.map((ingredient) =>
          ingredient.key === ingredientKey ? { ...ingredient, ...patch } : ingredient,
        ),
      };
    }));
  }

  function addIngredient(occurrenceKey: number) {
    setOccurrences((current) => current.map((occurrence) => {
      if (occurrence.key !== occurrenceKey || occurrence.ingredients.length >= 100) return occurrence;
      const key = Math.max(...occurrence.ingredients.map((ingredient) => ingredient.key)) + 1;
      return { ...occurrence, ingredients: [...occurrence.ingredients, newIngredient(key)] };
    }));
  }

  function removeIngredient(occurrenceKey: number, ingredientKey: number) {
    setOccurrences((current) => current.map((occurrence) => {
      if (occurrence.key !== occurrenceKey || occurrence.ingredients.length === 1) return occurrence;
      return { ...occurrence, ingredients: occurrence.ingredients.filter((ingredient) => ingredient.key !== ingredientKey) };
    }));
  }

  function addPantryRow() {
    setPantryRows((current) => {
      const key = current.length === 0 ? 1 : Math.max(...current.map((item) => item.key)) + 1;
      return [...current, newPantryRow(key)];
    });
  }

  function updatePantryRow(key: number, patch: Partial<PantryRow>) {
    setPantryRows((current) => current.map((item) => item.key === key ? { ...item, ...patch } : item));
  }

  function removePantryRow(key: number) {
    setPantryRows((current) => current.filter((item) => item.key !== key));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending) return;

    const validationErrors = clientErrors(locality, occurrences, pantryRows);
    if (validationErrors.length > 0) {
      setState(null);
      setClientMessages(validationErrors);
      return;
    }

    const request: WeeklyPlanComparisonPreviewRequest = {
      locality: locality.trim(),
      weeklyPlan: {
        occurrences: occurrences.map((occurrence) => ({
          day: occurrence.day,
          targetServings: Number(occurrence.targetServings),
          recipe: {
            title: occurrence.title.trim(),
            baseServings: Number(occurrence.baseServings),
            ingredients: occurrence.ingredients.map((ingredient) => ({
              requirement: ingredient.requirement.trim(),
              quantity: { amount: Number(ingredient.amount), unit: ingredient.unit },
            })),
          },
        })),
      },
      pantry: pantryRows.map((pantry) => ({
        requirement: pantry.requirement.trim(),
        quantity: { amount: Number(pantry.amount), unit: pantry.unit },
      })),
    };

    setClientMessages([]);
    setPending(true);
    try {
      setState(await createWeeklyPlanComparisonPreview(request));
    } finally {
      setPending(false);
    }
  }

  const errorContent = clientMessages.length > 0
    ? clientMessages.join(" ")
    : state?.kind === "invalid"
      ? state.errors.map((error) => `${error.field}: ${error.message}`).join("; ")
      : state?.kind === "unavailable"
        ? "Не удалось сравнить недельный план. Основной сервис временно недоступен."
        : null;

  return (
    <section aria-labelledby="weekly-plan-comparison" className="mt-12">
      <div className="max-w-2xl">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-stone-500">Неделя → запасы дома → покупки → магазины</p>
        <h2 id="weekly-plan-comparison" className="mt-2 text-2xl font-semibold tracking-tight text-stone-950">
          Собрать неделю
        </h2>
        <p className="mt-2 text-sm leading-6 text-stone-600">
          Добавьте блюда в нужном порядке, выберите дни и порции. При желании укажите продукты, которые уже есть дома: сервер учтёт их и сравнит в магазинах только оставшийся список покупок.
        </p>
      </div>

      <form onSubmit={submit} className="mt-6 space-y-6" noValidate>
        <div className="max-w-xl">
          <label htmlFor="weekly-plan-locality" className="block text-sm font-medium text-stone-800">Населённый пункт</label>
          <input
            id="weekly-plan-locality"
            value={locality}
            onChange={(event) => setLocality(event.target.value)}
            maxLength={160}
            autoComplete="address-level2"
            className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-4 py-2 text-base outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
          />
        </div>

        <div className="space-y-5" aria-label="Блюда недели">
          {occurrences.map((occurrence, occurrenceIndex) => (
            <fieldset key={occurrence.key} className="rounded-3xl border border-stone-300 bg-stone-100/60 p-4 sm:p-6">
              <legend className="px-2 text-base font-semibold text-stone-950">Блюдо {occurrenceIndex + 1}</legend>

              <div className="flex flex-wrap gap-2 pb-5">
                <button type="button" onClick={() => moveOccurrence(occurrenceIndex, -1)} disabled={occurrenceIndex === 0} aria-label={`Переместить блюдо ${occurrenceIndex + 1} выше`} className="min-h-10 rounded-full border border-stone-300 bg-white px-3 text-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:opacity-40">Выше</button>
                <button type="button" onClick={() => moveOccurrence(occurrenceIndex, 1)} disabled={occurrenceIndex === occurrences.length - 1} aria-label={`Переместить блюдо ${occurrenceIndex + 1} ниже`} className="min-h-10 rounded-full border border-stone-300 bg-white px-3 text-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:opacity-40">Ниже</button>
                <button type="button" onClick={() => removeOccurrence(occurrence.key)} disabled={occurrences.length === 1} aria-label={`Удалить блюдо ${occurrenceIndex + 1}`} className="min-h-10 rounded-full border border-stone-300 bg-white px-3 text-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:opacity-40">Удалить блюдо</button>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label htmlFor={`weekly-day-${occurrence.key}`} className="block text-sm font-medium text-stone-700">День</label>
                  <select id={`weekly-day-${occurrence.key}`} value={occurrence.day} onChange={(event) => updateOccurrence(occurrence.key, { day: event.target.value as WeeklyPlanDay })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200">
                    {days.map((day) => <option key={day.value} value={day.value}>{day.label}</option>)}
                  </select>
                </div>
                <div>
                  <label htmlFor={`weekly-target-${occurrence.key}`} className="block text-sm font-medium text-stone-700">Нужно порций</label>
                  <input id={`weekly-target-${occurrence.key}`} type="number" inputMode="numeric" min="1" step="1" value={occurrence.targetServings} onChange={(event) => updateOccurrence(occurrence.key, { targetServings: event.target.value })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                </div>
                <div>
                  <label htmlFor={`weekly-title-${occurrence.key}`} className="block text-sm font-medium text-stone-700">Название рецепта</label>
                  <input id={`weekly-title-${occurrence.key}`} value={occurrence.title} onChange={(event) => updateOccurrence(occurrence.key, { title: event.target.value })} maxLength={240} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                </div>
                <div>
                  <label htmlFor={`weekly-base-${occurrence.key}`} className="block text-sm font-medium text-stone-700">Порций в рецепте</label>
                  <input id={`weekly-base-${occurrence.key}`} type="number" inputMode="numeric" min="1" step="1" value={occurrence.baseServings} onChange={(event) => updateOccurrence(occurrence.key, { baseServings: event.target.value })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                </div>
              </div>

              <div className="mt-5 space-y-3" aria-label={`Ингредиенты блюда ${occurrenceIndex + 1}`}>
                {occurrence.ingredients.map((ingredient, ingredientIndex) => (
                  <fieldset key={ingredient.key} className="rounded-2xl border border-stone-200 bg-white p-4">
                    <legend className="px-1 text-sm font-medium text-stone-700">Ингредиент {ingredientIndex + 1}</legend>
                    <div className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_7rem_7rem_auto] sm:items-end">
                      <div>
                        <label htmlFor={`weekly-ingredient-${occurrence.key}-${ingredient.key}`} className="block text-sm font-medium text-stone-700">Ингредиент</label>
                        <input id={`weekly-ingredient-${occurrence.key}-${ingredient.key}`} value={ingredient.requirement} onChange={(event) => updateIngredient(occurrence.key, ingredient.key, { requirement: event.target.value })} maxLength={240} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                      </div>
                      <div>
                        <label htmlFor={`weekly-amount-${occurrence.key}-${ingredient.key}`} className="block text-sm font-medium text-stone-700">Количество</label>
                        <input id={`weekly-amount-${occurrence.key}-${ingredient.key}`} type="number" inputMode="decimal" min="0" step="any" value={ingredient.amount} onChange={(event) => updateIngredient(occurrence.key, ingredient.key, { amount: event.target.value })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                      </div>
                      <div>
                        <label htmlFor={`weekly-unit-${occurrence.key}-${ingredient.key}`} className="block text-sm font-medium text-stone-700">Единица</label>
                        <select id={`weekly-unit-${occurrence.key}-${ingredient.key}`} value={ingredient.unit} onChange={(event) => updateIngredient(occurrence.key, ingredient.key, { unit: event.target.value as QuantityUnit })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200">
                          {units.map((unit) => <option key={unit.value} value={unit.value}>{unit.label}</option>)}
                        </select>
                      </div>
                      <button type="button" onClick={() => removeIngredient(occurrence.key, ingredient.key)} disabled={occurrence.ingredients.length === 1} aria-label={`Удалить ингредиент ${ingredientIndex + 1} блюда ${occurrenceIndex + 1}`} className="min-h-11 rounded-xl border border-stone-300 px-3 text-sm font-medium text-stone-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:opacity-40">Удалить</button>
                    </div>
                  </fieldset>
                ))}
                <button type="button" onClick={() => addIngredient(occurrence.key)} disabled={occurrence.ingredients.length >= 100} className="min-h-10 rounded-full border border-stone-300 bg-white px-4 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:opacity-40">Добавить ингредиент</button>
              </div>
            </fieldset>
          ))}
        </div>

        <div>
          <div className="max-w-2xl">
            <h3 className="text-lg font-semibold text-stone-950">Что уже есть дома</h3>
            <p className="mt-1 text-sm leading-6 text-stone-600">
              Необязательно. Укажите только известные запасы: сервер сопоставит их с недельным списком по точному названию и совместимой единице и покажет, что осталось купить.
            </p>
          </div>

          <div className="mt-4 space-y-3" aria-label="Запасы дома">
            {pantryRows.map((pantry, pantryIndex) => (
              <fieldset key={pantry.key} className="rounded-2xl border border-stone-200 bg-stone-50 p-4">
                <legend className="px-1 text-sm font-medium text-stone-800">Запас дома {pantryIndex + 1}</legend>
                <div className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_7rem_7rem_auto] sm:items-end">
                  <div>
                    <label htmlFor={`weekly-pantry-requirement-${pantry.key}`} className="block text-sm font-medium text-stone-700">Продукт дома</label>
                    <input id={`weekly-pantry-requirement-${pantry.key}`} value={pantry.requirement} onChange={(event) => updatePantryRow(pantry.key, { requirement: event.target.value })} maxLength={240} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                  </div>
                  <div>
                    <label htmlFor={`weekly-pantry-amount-${pantry.key}`} className="block text-sm font-medium text-stone-700">Количество дома</label>
                    <input id={`weekly-pantry-amount-${pantry.key}`} type="number" inputMode="decimal" min="0" step="any" value={pantry.amount} onChange={(event) => updatePantryRow(pantry.key, { amount: event.target.value })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200" />
                  </div>
                  <div>
                    <label htmlFor={`weekly-pantry-unit-${pantry.key}`} className="block text-sm font-medium text-stone-700">Единица дома</label>
                    <select id={`weekly-pantry-unit-${pantry.key}`} value={pantry.unit} onChange={(event) => updatePantryRow(pantry.key, { unit: event.target.value as QuantityUnit })} className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200">
                      {units.map((unit) => <option key={unit.value} value={unit.value}>{unit.label}</option>)}
                    </select>
                  </div>
                  <button type="button" onClick={() => removePantryRow(pantry.key)} aria-label={`Удалить запас дома ${pantryIndex + 1}`} className="min-h-11 rounded-xl border border-stone-300 bg-white px-3 text-sm font-medium text-stone-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900">Удалить</button>
                </div>
              </fieldset>
            ))}
          </div>

          <button type="button" onClick={addPantryRow} className="mt-4 min-h-10 rounded-full border border-stone-300 bg-white px-4 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900">Добавить запас</button>
        </div>

        <div className="flex flex-wrap gap-3">
          <button type="button" onClick={addOccurrence} disabled={occurrences.length >= 35} className="min-h-11 rounded-full border border-stone-300 bg-white px-5 py-2.5 text-sm font-medium text-stone-900 hover:bg-stone-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:opacity-40">Добавить блюдо</button>
          <button type="submit" disabled={pending} className="min-h-11 rounded-full bg-stone-950 px-5 py-2.5 text-sm font-medium text-white hover:bg-stone-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:cursor-wait disabled:opacity-60">{pending ? "Сравниваем…" : "Сравнить план"}</button>
        </div>
      </form>

      {errorContent ? <div role="alert" className="mt-6 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">{errorContent}</div> : null}
      {state?.kind === "ready" ? <WeeklyPlanComparisonResults preview={state.data} /> : null}
    </section>
  );
}
