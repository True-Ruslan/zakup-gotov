import type { components } from "@zakup-gotov/api-client";

import { ComparisonPreviewResults } from "./comparison-preview-results";

type Preview = components["schemas"]["RecipeComparisonPreviewResponse"];
type ShoppingItem = components["schemas"]["RecipeShoppingPreviewShoppingItem"];

export function RecipeComparisonResults({ preview }: { preview: Preview }) {
  return (
    <>
      <section aria-labelledby="recipe-shopping-results" className="mt-12">
        <div className="max-w-2xl">
          <h2
            id="recipe-shopping-results"
            className="text-2xl font-semibold tracking-tight text-stone-950"
          >
            Список покупок из рецепта
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Количества пересчитаны для выбранного числа порций и приведены к каноническим единицам.
          </p>
        </div>

        <ul className="mt-6 grid gap-3 sm:grid-cols-2" aria-label="Покупки из рецепта">
          {preview.recipeShoppingPreview.shoppingList.items.map((item: ShoppingItem) => (
            <li
              key={item.id}
              className="rounded-2xl border border-stone-200 bg-white p-4 shadow-sm"
            >
              <p className="font-medium text-stone-950">{item.requirement}</p>
              <p className="mt-1 text-sm text-stone-600">
                {item.quantity.amount} {item.quantity.unit}
              </p>
            </li>
          ))}
        </ul>
      </section>

      <ComparisonPreviewResults preview={preview.comparisonPreview} />
    </>
  );
}
