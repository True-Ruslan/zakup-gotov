import type { components } from "@zakup-gotov/api-client";

import { ComparisonPreviewResults } from "./comparison-preview-results";

type Preview = components["schemas"]["WeeklyPlanPantryComparisonPreview"];
type ShoppingItem = components["schemas"]["WeeklyPlanShoppingPreviewShoppingItem"];
type Adjustment = components["schemas"]["WeeklyPlanPantryAdjustmentEvidence"];
type PantryStatus = components["schemas"]["PantryAdjustmentStatus"];

const statusLabels: Record<PantryStatus, string> = {
  UNCHANGED: "Запас не применён",
  PARTIALLY_COVERED: "Частично покрыто",
  FULLY_COVERED: "Полностью покрыто",
};

function quantityText(quantity: components["schemas"]["CanonicalQuantity"]) {
  return `${quantity.amount} ${quantity.unit}`;
}

function ShoppingItems({ items, emptyText }: { items: ShoppingItem[]; emptyText: string }) {
  if (items.length === 0) {
    return <p className="mt-4 rounded-2xl border border-stone-200 bg-stone-50 px-4 py-3 text-sm text-stone-600">{emptyText}</p>;
  }

  return (
    <ul className="mt-6 grid gap-3 sm:grid-cols-2">
      {items.map((item) => (
        <li key={item.id} className="rounded-2xl border border-stone-200 bg-white p-4 shadow-sm">
          <p className="font-medium text-stone-950">{item.requirement}</p>
          <p className="mt-1 text-sm text-stone-600">{quantityText(item.quantity)}</p>
        </li>
      ))}
    </ul>
  );
}

function PantryAdjustmentItems({ items }: { items: Adjustment[] }) {
  return (
    <ul className="mt-6 grid gap-3 sm:grid-cols-2">
      {items.map((item) => (
        <li key={item.itemId} className="rounded-2xl border border-stone-200 bg-white p-4 shadow-sm">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <p className="font-medium text-stone-950">{item.requirement}</p>
            <span className="rounded-full bg-stone-100 px-2.5 py-1 text-xs font-medium text-stone-700">
              {statusLabels[item.status]}
            </span>
          </div>
          <div className="mt-3 space-y-1 text-sm text-stone-600">
            <p>Нужно: {quantityText(item.required)}</p>
            {item.pantryUsed ? <p>Из дома: {quantityText(item.pantryUsed)}</p> : null}
            {item.remaining ? <p>Осталось: {quantityText(item.remaining)}</p> : null}
          </div>
        </li>
      ))}
    </ul>
  );
}

export function WeeklyPlanComparisonResults({ preview }: { preview: Preview }) {
  const pantryPreview = preview.pantryShoppingPreview;

  return (
    <>
      <section aria-labelledby="weekly-shopping-results" aria-label="Исходный список на неделю" className="mt-12">
        <div className="max-w-2xl">
          <h2 id="weekly-shopping-results" className="text-2xl font-semibold tracking-tight text-stone-950">
            Покупки на неделю
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Это исходный канонический список до учёта домашних запасов. Порядок и количества получены от серверной WeeklyPlan-композиции.
          </p>
        </div>
        <ShoppingItems items={pantryPreview.originalShoppingList.items} emptyText="Исходный недельный список пуст." />
      </section>

      <section aria-labelledby="weekly-pantry-results" aria-label="Учёт запасов дома" className="mt-10">
        <div className="max-w-2xl">
          <h2 id="weekly-pantry-results" className="text-2xl font-semibold tracking-tight text-stone-950">
            Учтено из запасов дома
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Сервер показывает, какой объём требования покрыт Pantry и сколько осталось. Браузер эти значения не пересчитывает.
          </p>
        </div>
        <PantryAdjustmentItems items={pantryPreview.pantryAdjustments} />
      </section>

      <section aria-labelledby="weekly-remaining-results" aria-label="Осталось купить" className="mt-10">
        <div className="max-w-2xl">
          <h2 id="weekly-remaining-results" className="text-2xl font-semibold tracking-tight text-stone-950">
            Осталось купить
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Только этот серверный список может участвовать в сравнении магазинов.
          </p>
        </div>
        <ShoppingItems items={pantryPreview.remainingShoppingList.items} emptyText="После учёта запасов список покупок пуст." />
      </section>

      {preview.comparisonOutcome === "NO_REMAINING_DEMAND" ? (
        <section className="mt-10 rounded-3xl border border-emerald-200 bg-emerald-50 p-5 sm:p-6" aria-labelledby="weekly-zero-demand">
          <h2 id="weekly-zero-demand" className="text-2xl font-semibold tracking-tight text-stone-950">
            Покупать ничего не нужно
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-700">
            Запасы дома полностью покрывают недельный список.
          </p>
        </section>
      ) : preview.comparisonOutcome === "COMPARED" ? (
        preview.comparisonPreview ? (
          <ComparisonPreviewResults preview={preview.comparisonPreview} />
        ) : (
          <div role="alert" className="mt-10 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">
            Не удалось показать сравнение магазинов: сервис не вернул подтверждённые данные сравнения.
          </div>
        )
      ) : (
        <div role="alert" className="mt-10 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">
          Не удалось показать сравнение магазинов: получен неизвестный результат.
        </div>
      )}
    </>
  );
}
