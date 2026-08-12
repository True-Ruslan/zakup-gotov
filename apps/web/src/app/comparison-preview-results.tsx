import type { components } from "@zakup-gotov/api-client";

type Preview = components["schemas"]["ComparisonPreviewResponse"];
type Retailer = components["schemas"]["ComparisonPreviewRetailer"];
type Reason = components["schemas"]["RetailerComparisonReason"];
type ItemStatus = components["schemas"]["BasketItemResolutionStatus"];
type Freshness = components["schemas"]["RetailerFreshness"];

const comparisonLabels: Record<Retailer["comparisonStatus"], string> = {
  READY: "Корзина рассчитана",
  UNCERTAIN: "Есть неопределённость",
  INCOMPLETE: "Корзина неполная",
  UNAVAILABLE: "Сравнение пока недоступно",
};

const reasonLabels: Record<Reason, string> = {
  COVERAGE_DISCOVERY: "Интеграция с магазином ещё исследуется.",
  COVERAGE_DEGRADED: "Источник данных сейчас работает нестабильно.",
  COVERAGE_BLOCKED: "Источник данных сейчас недоступен.",
  PRODUCTION_ACCESS_PENDING: "Условия использования данных ещё проверяются.",
  PRODUCTION_ACCESS_BLOCKED: "Использование источника для продукта заблокировано.",
  DATA_NOT_AVAILABLE: "Для этого магазина пока нет данных сравнения.",
  SOURCE_UNAVAILABLE: "Не удалось получить данные из доступного источника.",
  ITEM_UNMATCHED: "Для части списка товар не найден.",
  ITEM_AMBIGUOUS: "Для части списка найдено несколько равнозначных товаров.",
  ITEM_UNAVAILABLE: "Часть выбранных товаров недоступна.",
  PACKAGE_QUANTITY_UNKNOWN: "Не для всех товаров известен размер упаковки.",
  QUANTITY_UNIT_MISMATCH: "Количество товара нельзя корректно сопоставить с упаковкой.",
  AVAILABILITY_UNKNOWN: "Наличие части товаров не подтверждено.",
};

const itemLabels: Record<ItemStatus, string> = {
  FULFILLED: "Подобрано",
  AVAILABILITY_UNKNOWN: "Наличие не подтверждено",
  UNMATCHED: "Товар не найден",
  AMBIGUOUS: "Нужно уточнить товар",
  UNAVAILABLE: "Товар недоступен",
  PACKAGE_QUANTITY_UNKNOWN: "Неизвестен размер упаковки",
  QUANTITY_UNIT_MISMATCH: "Несовместимые единицы количества",
};

function money(amount: number, currencyCode: string) {
  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: currencyCode,
  }).format(amount);
}

function FreshnessEvidence({ freshness }: { freshness: Freshness }) {
  return (
    <div className="mt-4 space-y-1 text-xs leading-5 text-stone-500">
      <p>Последнее наблюдение: {freshness.observedAt}</p>
      {freshness.basis === "PROVIDER_TIMESTAMP" && freshness.providerUpdatedAt ? (
        <p>Обновлено источником: {freshness.providerUpdatedAt}</p>
      ) : (
        <p>Источник не сообщает отдельное время обновления.</p>
      )}
    </div>
  );
}

export function ComparisonPreviewResults({ preview }: { preview: Preview }) {
  return (
    <section aria-labelledby="comparison-results" className="mt-12">
      <div className="max-w-2xl">
        <h2 id="comparison-results" className="text-2xl font-semibold tracking-tight text-stone-950">
          Результат для {preview.locality}
        </h2>
        <p className="mt-2 text-sm leading-6 text-stone-600">
          Неполные и неопределённые корзины не скрываются и не получают статус победителя.
        </p>
      </div>

      <ul className="mt-6 grid gap-4 sm:grid-cols-2" aria-label="Сравнение магазинов">
        {preview.retailers.map((retailer) => (
          <li key={retailer.id}>
            <article
              aria-label={retailer.displayName}
              className="h-full rounded-2xl border border-stone-200 bg-white p-5 shadow-sm"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h3 className="text-lg font-semibold text-stone-950">{retailer.displayName}</h3>
                  <p className="mt-1 text-sm text-stone-600">
                    {comparisonLabels[retailer.comparisonStatus]}
                  </p>
                </div>
                {retailer.total ? (
                  <p className="shrink-0 text-base font-semibold text-stone-950">
                    Итого: {money(retailer.total.amount, retailer.total.currencyCode)}
                  </p>
                ) : null}
              </div>

              {retailer.reasons.length > 0 ? (
                <ul className="mt-4 space-y-1 text-sm leading-5 text-stone-600">
                  {retailer.reasons.map((reason) => (
                    <li key={reason}>{reasonLabels[reason]}</li>
                  ))}
                </ul>
              ) : null}

              {retailer.items.length > 0 ? (
                <ul className="mt-5 space-y-3 border-t border-stone-100 pt-4" aria-label={`Позиции ${retailer.displayName}`}>
                  {retailer.items.map((item) => (
                    <li key={item.id} className="text-sm leading-5 text-stone-700">
                      <div className="flex flex-wrap items-baseline justify-between gap-2">
                        <span className="font-medium text-stone-900">
                          {item.selection?.productName ?? item.requirement}
                        </span>
                        <span className="text-stone-500">{itemLabels[item.status]}</span>
                      </div>
                      {item.selection ? (
                        <p className="mt-1 text-xs text-stone-500">
                          {item.selection.packageCount} упак. · {item.selection.packageQuantity.amount} {item.selection.packageQuantity.unit}
                        </p>
                      ) : null}
                      {item.status === "AMBIGUOUS" && item.candidateProductNames.length > 0 ? (
                        <p className="mt-1 text-xs text-stone-500">
                          Варианты: {item.candidateProductNames.join(", ")}
                        </p>
                      ) : null}
                    </li>
                  ))}
                </ul>
              ) : null}

              {retailer.freshness ? <FreshnessEvidence freshness={retailer.freshness} /> : null}
            </article>
          </li>
        ))}
      </ul>
    </section>
  );
}
