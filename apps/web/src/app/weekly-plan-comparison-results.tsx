import type { components } from "@zakup-gotov/api-client";

import { ComparisonPreviewResults } from "./comparison-preview-results";

type Preview = components["schemas"]["WeeklyPlanComparisonPreview"];
type ShoppingItem = components["schemas"]["WeeklyPlanShoppingPreviewShoppingItem"];

export function WeeklyPlanComparisonResults({ preview }: { preview: Preview }) {
  return (
    <>
      <section aria-labelledby="weekly-shopping-results" className="mt-12">
        <div className="max-w-2xl">
          <h2 id="weekly-shopping-results" className="text-2xl font-semibold tracking-tight text-stone-950">
            Покупки на неделю
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Сервис объединил блюда в один канонический список. Порядок и количества получены от серверной WeeklyPlan-композиции.
          </p>
        </div>

        <ul className="mt-6 grid gap-3 sm:grid-cols-2" aria-label="Покупки на неделю">
          {preview.weeklyPlanShoppingPreview.shoppingList.items.map((item: ShoppingItem) => (
            <li key={item.id} className="rounded-2xl border border-stone-200 bg-white p-4 shadow-sm">
              <p className="font-medium text-stone-950">{item.requirement}</p>
              <p className="mt-1 text-sm text-stone-600">{item.quantity.amount} {item.quantity.unit}</p>
            </li>
          ))}
        </ul>
      </section>

      <ComparisonPreviewResults preview={preview.comparisonPreview} />
    </>
  );
}
