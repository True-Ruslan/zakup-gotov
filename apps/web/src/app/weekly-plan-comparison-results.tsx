import type { components } from "@zakup-gotov/api-client";

import { ComparisonPreviewResults } from "./comparison-preview-results";

type Preview = components["schemas"]["WeeklyPlanPantryOptimizationPreview"];
type PantryComparison = components["schemas"]["WeeklyPlanPantryComparisonPreview"];
type ShoppingItem = components["schemas"]["WeeklyPlanShoppingPreviewShoppingItem"];
type Adjustment = components["schemas"]["WeeklyPlanPantryAdjustmentEvidence"];
type PantryStatus = components["schemas"]["PantryAdjustmentStatus"];
type ComparisonPreview = components["schemas"]["ComparisonPreviewResponse"];
type ComparisonRetailer = components["schemas"]["ComparisonPreviewRetailer"];
type OptimizationPreview = components["schemas"]["CheckoutOptimizationPreview"];
type RetailerCheckout = components["schemas"]["RetailerCheckoutPreview"];
type RetailerId = components["schemas"]["RetailerId"];
type BasketTotal = components["schemas"]["BasketTotal"];
type BasketFee = components["schemas"]["BasketFee"];
type MinimumOrder = components["schemas"]["MinimumOrderConstraint"];
type MinimumOrderStatus = components["schemas"]["MinimumOrderStatus"];
type EligibilityStatus = components["schemas"]["RetailerCheckoutEligibilityStatus"];
type ComparabilityStatus = components["schemas"]["RetailerCheckoutComparabilityStatus"];

const statusLabels: Record<PantryStatus, string> = {
  UNCHANGED: "Запас не применён",
  PARTIALLY_COVERED: "Частично покрыто",
  FULLY_COVERED: "Полностью покрыто",
};

const minimumOrderLabels: Record<MinimumOrderStatus, string> = {
  MET: "Минимум выполнен",
  NOT_MET: "Минимум не выполнен",
  UNKNOWN: "Статус минимума неизвестен",
};

const eligibilityLabels: Record<EligibilityStatus, string> = {
  ELIGIBLE: "Заказ доступен",
  INELIGIBLE: "Условия заказа не выполнены",
  UNKNOWN: "Доступность заказа не подтверждена",
};

const comparabilityLabels: Record<ComparabilityStatus, string> = {
  COMPARABLE: "Можно сравнивать",
  NOT_COMPARABLE: "Нельзя включать в минимум",
};

function quantityText(quantity: components["schemas"]["CanonicalQuantity"]) {
  return `${quantity.amount} ${quantity.unit}`;
}

function money(total: BasketTotal) {
  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: total.currencyCode,
  }).format(total.amount);
}

function feeText(fee: BasketFee) {
  return fee.status === "KNOWN" && fee.amount ? money(fee.amount) : "Неизвестно";
}

function minimumOrderText(minimumOrder: MinimumOrder) {
  return minimumOrder.status === "KNOWN" && minimumOrder.threshold
    ? money(minimumOrder.threshold)
    : "Неизвестно";
}

function ShoppingItems({
  items,
  emptyText,
  ariaLabel,
}: {
  items: ShoppingItem[];
  emptyText: string;
  ariaLabel: string;
}) {
  if (items.length === 0) {
    return <p className="mt-4 rounded-2xl border border-stone-200 bg-stone-50 px-4 py-3 text-sm text-stone-600">{emptyText}</p>;
  }

  return (
    <ul aria-label={ariaLabel} className="mt-6 grid gap-3 sm:grid-cols-2">
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
    <ul aria-label="Учёт запасов дома" className="mt-6 grid gap-3 sm:grid-cols-2">
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

type ValidatedOptimization = {
  comparisonById: Map<RetailerId, ComparisonRetailer>;
  optimization: OptimizationPreview;
};

function validateOptimizationProjection(
  comparison: ComparisonPreview,
  optimization: OptimizationPreview,
): ValidatedOptimization | null {
  const comparisonById = new Map<RetailerId, ComparisonRetailer>();
  for (const retailer of comparison.retailers) {
    if (comparisonById.has(retailer.id)) return null;
    comparisonById.set(retailer.id, retailer);
  }

  const optimizationIds = new Set<RetailerId>();
  for (const retailer of optimization.retailers) {
    if (optimizationIds.has(retailer.retailerId)) return null;
    if (!comparisonById.has(retailer.retailerId)) return null;
    optimizationIds.add(retailer.retailerId);
  }
  if (optimizationIds.size !== comparisonById.size) return null;

  const optimalIds = new Set<RetailerId>();
  for (const retailerId of optimization.optimalRetailerIds) {
    if (optimalIds.has(retailerId) || !optimizationIds.has(retailerId)) return null;
    optimalIds.add(retailerId);
  }

  switch (optimization.status) {
    case "NO_COMPARABLE_CANDIDATES":
      if (optimization.optimalRetailerIds.length !== 0 || optimization.lowestComparableCheckoutTotal) return null;
      break;
    case "UNIQUE_WINNER":
      if (optimization.optimalRetailerIds.length !== 1 || !optimization.lowestComparableCheckoutTotal) return null;
      break;
    case "TIE":
      if (optimization.optimalRetailerIds.length < 2 || !optimization.lowestComparableCheckoutTotal) return null;
      break;
    default:
      return null;
  }

  return { comparisonById, optimization };
}

function OptimizationSummary({
  validated,
}: {
  validated: ValidatedOptimization;
}) {
  const { comparisonById, optimization } = validated;

  if (optimization.status === "NO_COMPARABLE_CANDIDATES") {
    return (
      <div role="group" aria-label="Результат оптимизации" className="rounded-2xl border border-amber-200 bg-amber-50 p-5">
        <h3 className="text-lg font-semibold text-stone-950">Пока нельзя честно выбрать минимальную стоимость</h3>
        <p className="mt-2 text-sm leading-6 text-stone-700">
          Ни у одного магазина пока нет полного набора подтверждённых условий оформления, который можно честно включить в минимум.
        </p>
      </div>
    );
  }

  const winnerNames = optimization.optimalRetailerIds.map(
    (retailerId: RetailerId) => comparisonById.get(retailerId)!.displayName,
  );
  const lowest = money(optimization.lowestComparableCheckoutTotal!);

  if (optimization.status === "UNIQUE_WINNER") {
    return (
      <div role="group" aria-label="Результат оптимизации" className="rounded-2xl border border-emerald-200 bg-emerald-50 p-5">
        <h3 className="text-lg font-semibold text-stone-950">Минимальная подтверждённая стоимость</h3>
        <p className="mt-2 font-medium text-stone-950">{winnerNames[0]}</p>
        <p className="mt-1 text-sm text-stone-700">Стоимость оформления: {lowest}</p>
      </div>
    );
  }

  return (
    <div role="group" aria-label="Результат оптимизации" className="rounded-2xl border border-emerald-200 bg-emerald-50 p-5">
      <h3 className="text-lg font-semibold text-stone-950">Одинаковая минимальная стоимость</h3>
      <p className="mt-2 text-sm text-stone-700">Подтверждённая стоимость оформления: {lowest}</p>
      <ul className="mt-3 list-disc space-y-1 pl-5 text-sm font-medium text-stone-950">
        {winnerNames.map((name: string, index: number) => (
          <li key={`${optimization.optimalRetailerIds[index]}-${name}`}>{name}</li>
        ))}
      </ul>
    </div>
  );
}

function CheckoutCard({
  retailer,
  displayName,
}: {
  retailer: RetailerCheckout;
  displayName: string;
}) {
  const assessment = retailer.assessment;

  return (
    <article aria-label={`Стоимость оформления — ${displayName}`} className="rounded-3xl border border-stone-200 bg-white p-5 shadow-sm">
      <h3 className="text-lg font-semibold text-stone-950">{displayName}</h3>
      {!assessment ? (
        <p className="mt-3 text-sm leading-6 text-stone-600">Расчёт оформления недоступен.</p>
      ) : (
        <>
          <div className="mt-4 space-y-2 text-sm leading-6 text-stone-700">
            <p>Товары: {money(assessment.merchandiseSubtotal)}</p>
            <p>Доставка: {feeText(assessment.deliveryFee)}</p>
            <p>Сервисный сбор: {feeText(assessment.serviceFee)}</p>
            <p>Минимальный заказ: {minimumOrderText(assessment.minimumOrder)}</p>
            <p>{minimumOrderLabels[assessment.minimumOrderStatus]}</p>
            <p>
              Стоимость оформления: {assessment.checkoutTotalStatus === "KNOWN" && assessment.checkoutTotal
                ? money(assessment.checkoutTotal)
                : "Неизвестно"}
            </p>
          </div>
          <div className="mt-4 flex flex-wrap gap-2 text-xs font-medium">
            <span className="rounded-full bg-stone-100 px-2.5 py-1 text-stone-700">
              {eligibilityLabels[assessment.eligibilityStatus]}
            </span>
            <span className="rounded-full bg-stone-100 px-2.5 py-1 text-stone-700">
              {comparabilityLabels[assessment.comparabilityStatus]}
            </span>
          </div>
        </>
      )}
    </article>
  );
}

function OptimizationResults({
  comparison,
  optimization,
}: {
  comparison: ComparisonPreview;
  optimization: OptimizationPreview;
}) {
  const validated = validateOptimizationProjection(comparison, optimization);
  if (!validated) {
    return (
      <div role="alert" className="mt-10 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">
        Не удалось показать стоимость оформления: сервис вернул противоречивые данные оптимизации.
      </div>
    );
  }

  return (
    <section aria-labelledby="weekly-optimization-results" className="mt-10">
      <div className="max-w-2xl">
        <h2 id="weekly-optimization-results" className="text-2xl font-semibold tracking-tight text-stone-950">
          Стоимость оформления
        </h2>
        <p className="mt-2 text-sm leading-6 text-stone-600">
          Суммы, условия заказа и результат оптимизации получены от сервера. Браузер их не пересчитывает и не выбирает победителя самостоятельно.
        </p>
      </div>

      <div className="mt-6">
        <OptimizationSummary validated={validated} />
      </div>

      <div aria-label="Стоимость оформления по магазинам" className="mt-6 grid gap-4 sm:grid-cols-2">
        {optimization.retailers.map((retailer: RetailerCheckout) => (
          <CheckoutCard
            key={retailer.retailerId}
            retailer={retailer}
            displayName={validated.comparisonById.get(retailer.retailerId)!.displayName}
          />
        ))}
      </div>
    </section>
  );
}

function ComparedResults({
  preview,
}: {
  preview: Preview;
}) {
  const pantryComparison = preview.pantryComparisonPreview;
  const comparison = pantryComparison.comparisonPreview;
  if (!comparison) {
    return (
      <div role="alert" className="mt-10 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">
        Не удалось показать сравнение магазинов: сервис не вернул подтверждённые данные сравнения.
      </div>
    );
  }

  return (
    <>
      <ComparisonPreviewResults preview={comparison} />
      {preview.optimizationPreview ? (
        <OptimizationResults comparison={comparison} optimization={preview.optimizationPreview} />
      ) : (
        <div role="alert" className="mt-10 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">
          Не удалось показать стоимость оформления: сервис не вернул подтверждённые данные оптимизации.
        </div>
      )}
    </>
  );
}

export function WeeklyPlanComparisonResults({ preview }: { preview: Preview }) {
  const pantryComparison: PantryComparison = preview.pantryComparisonPreview;
  const pantryPreview = pantryComparison.pantryShoppingPreview;

  return (
    <>
      <section aria-labelledby="weekly-shopping-results" className="mt-12">
        <div className="max-w-2xl">
          <h2 id="weekly-shopping-results" className="text-2xl font-semibold tracking-tight text-stone-950">
            Покупки на неделю
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Это исходный канонический список до учёта домашних запасов. Порядок и количества получены от серверной WeeklyPlan-композиции.
          </p>
        </div>
        <ShoppingItems
          items={pantryPreview.originalShoppingList.items}
          emptyText="Исходный недельный список пуст."
          ariaLabel="Покупки на неделю"
        />
      </section>

      <section aria-labelledby="weekly-pantry-results" className="mt-10">
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

      <section aria-labelledby="weekly-remaining-results" className="mt-10">
        <div className="max-w-2xl">
          <h2 id="weekly-remaining-results" className="text-2xl font-semibold tracking-tight text-stone-950">
            Осталось купить
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-600">
            Только этот серверный список может участвовать в сравнении магазинов.
          </p>
        </div>
        <ShoppingItems
          items={pantryPreview.remainingShoppingList.items}
          emptyText="После учёта запасов список покупок пуст."
          ariaLabel="Осталось купить"
        />
      </section>

      {pantryComparison.comparisonOutcome === "NO_REMAINING_DEMAND" ? (
        <section className="mt-10 rounded-3xl border border-emerald-200 bg-emerald-50 p-5 sm:p-6" aria-labelledby="weekly-zero-demand">
          <h2 id="weekly-zero-demand" className="text-2xl font-semibold tracking-tight text-stone-950">
            Покупать ничего не нужно
          </h2>
          <p className="mt-2 text-sm leading-6 text-stone-700">
            Запасы дома полностью покрывают недельный список.
          </p>
        </section>
      ) : pantryComparison.comparisonOutcome === "COMPARED" ? (
        <ComparedResults preview={preview} />
      ) : (
        <div role="alert" className="mt-10 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950">
          Не удалось показать сравнение магазинов: получен неизвестный результат.
        </div>
      )}
    </>
  );
}
