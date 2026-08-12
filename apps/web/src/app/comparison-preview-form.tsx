"use client";

import { useState, type FormEvent } from "react";

import type { components } from "@zakup-gotov/api-client";

import {
  createComparisonPreview,
  type ComparisonPreviewRequest,
  type ComparisonPreviewState,
} from "./comparison-preview";
import { ComparisonPreviewResults } from "./comparison-preview-results";

type QuantityUnit = components["schemas"]["QuantityInputUnit"];

type FormRow = {
  id: string;
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

function newRow(): FormRow {
  return {
    id: globalThis.crypto.randomUUID(),
    requirement: "",
    amount: "1",
    unit: "PIECE",
  };
}

function clientError(locality: string, rows: FormRow[]) {
  if (!locality.trim()) {
    return "Укажите населённый пункт.";
  }
  for (const [index, row] of rows.entries()) {
    if (!row.requirement.trim()) {
      return `Укажите товар в строке ${index + 1}.`;
    }
    const amount = Number(row.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      return `Количество в строке ${index + 1} должно быть больше нуля.`;
    }
  }
  return null;
}

export function ComparisonPreviewForm() {
  const [locality, setLocality] = useState("");
  const [rows, setRows] = useState<FormRow[]>(() => [newRow()]);
  const [state, setState] = useState<ComparisonPreviewState | null>(null);
  const [clientMessage, setClientMessage] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  function updateRow(id: string, patch: Partial<FormRow>) {
    setRows((current) => current.map((row) => (row.id === id ? { ...row, ...patch } : row)));
  }

  function removeRow(id: string) {
    setRows((current) => (current.length === 1 ? current : current.filter((row) => row.id !== id)));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const validationMessage = clientError(locality, rows);
    if (validationMessage) {
      setState(null);
      setClientMessage(validationMessage);
      return;
    }

    const request: ComparisonPreviewRequest = {
      locality: locality.trim(),
      items: rows.map((row) => ({
        id: row.id,
        requirement: row.requirement.trim(),
        quantity: {
          amount: Number(row.amount),
          unit: row.unit,
        },
      })),
    };

    setClientMessage(null);
    setPending(true);
    try {
      setState(await createComparisonPreview(request));
    } finally {
      setPending(false);
    }
  }

  const errorContent = clientMessage
    ? clientMessage
    : state?.kind === "invalid"
      ? state.errors.map((error) => `${error.field}: ${error.message}`).join("; ")
      : state?.kind === "unavailable"
        ? "Не удалось выполнить сравнение. Основной сервис временно недоступен."
        : null;

  return (
    <section aria-labelledby="comparison-preview" className="mt-12">
      <div className="max-w-2xl">
        <h2 id="comparison-preview" className="text-2xl font-semibold tracking-tight text-stone-950">
          Сравнить корзину
        </h2>
        <p className="mt-2 text-sm leading-6 text-stone-600">
          Укажите населённый пункт и список покупок. Покажем только те цены и статусы,
          для которых есть подтверждённые данные.
        </p>
      </div>

      <form onSubmit={submit} className="mt-6 space-y-6" noValidate>
        <div className="max-w-xl">
          <label htmlFor="comparison-locality" className="block text-sm font-medium text-stone-800">
            Населённый пункт
          </label>
          <input
            id="comparison-locality"
            value={locality}
            onChange={(event) => setLocality(event.target.value)}
            maxLength={160}
            autoComplete="address-level2"
            className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 bg-white px-4 py-2 text-base outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
          />
        </div>

        <div className="space-y-4" aria-label="Список покупок">
          {rows.map((row, index) => {
            const requirementId = `requirement-${row.id}`;
            const amountId = `amount-${row.id}`;
            const unitId = `unit-${row.id}`;
            return (
              <fieldset key={row.id} className="rounded-2xl border border-stone-200 bg-white p-4 sm:p-5">
                <legend className="px-1 text-sm font-semibold text-stone-800">Позиция {index + 1}</legend>
                <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_8rem_7rem_auto] sm:items-end">
                  <div>
                    <label htmlFor={requirementId} className="block text-sm font-medium text-stone-700">
                      Товар
                    </label>
                    <input
                      id={requirementId}
                      value={row.requirement}
                      onChange={(event) => updateRow(row.id, { requirement: event.target.value })}
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
                      value={row.amount}
                      onChange={(event) => updateRow(row.id, { amount: event.target.value })}
                      className="mt-2 min-h-11 w-full rounded-xl border border-stone-300 px-3 py-2 outline-none focus:border-stone-700 focus:ring-2 focus:ring-stone-200"
                    />
                  </div>
                  <div>
                    <label htmlFor={unitId} className="block text-sm font-medium text-stone-700">
                      Единица
                    </label>
                    <select
                      id={unitId}
                      value={row.unit}
                      onChange={(event) => updateRow(row.id, { unit: event.target.value as QuantityUnit })}
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
                    onClick={() => removeRow(row.id)}
                    disabled={rows.length === 1}
                    aria-label="Удалить товар"
                    className="min-h-11 rounded-xl border border-stone-300 px-3 text-sm font-medium text-stone-700 disabled:cursor-not-allowed disabled:opacity-40"
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
            onClick={() => setRows((current) => [...current, newRow()])}
            className="min-h-11 rounded-full border border-stone-300 bg-white px-5 py-2.5 text-sm font-medium text-stone-900 hover:bg-stone-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900"
          >
            Добавить товар
          </button>
          <button
            type="submit"
            disabled={pending}
            className="min-h-11 rounded-full bg-stone-950 px-5 py-2.5 text-sm font-medium text-white hover:bg-stone-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-stone-900 disabled:cursor-wait disabled:opacity-60"
          >
            {pending ? "Сравниваем…" : "Сравнить корзину"}
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

      {state?.kind === "ready" ? <ComparisonPreviewResults preview={state.data} /> : null}
    </section>
  );
}
