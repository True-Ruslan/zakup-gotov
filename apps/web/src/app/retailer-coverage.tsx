import type { components } from "@zakup-gotov/api-client";

import type { RetailerReadinessState } from "./retailer-readiness";

type RetailerItem = components["schemas"]["RetailerReadinessItem"];
type Coverage = components["schemas"]["RetailerCoverageStatus"];
type ProductionAccess = components["schemas"]["RetailerProductionAccessStatus"];
type ComparisonStatus = components["schemas"]["RetailerComparisonStatus"];
type Reason = components["schemas"]["RetailerComparisonReason"];

const coverageLabels: Record<Coverage, string> = {
  CONNECTED: "Источник подключён",
  DISCOVERY: "Интеграция в работе",
  DEGRADED: "Источник работает нестабильно",
  BLOCKED: "Источник недоступен",
};

const accessLabels: Record<ProductionAccess, string> = {
  READY: "Доступ к данным подтверждён",
  PENDING: "Доступ к данным проверяется",
  BLOCKED: "Использование данных недоступно",
};

const comparisonLabels: Record<ComparisonStatus, string> = {
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

function formatTotal(item: RetailerItem) {
  if (!item.total) {
    return null;
  }

  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: item.total.currencyCode,
  }).format(item.total.amount);
}

export function RetailerCoverageSection({
  state,
}: {
  state: RetailerReadinessState;
}) {
  return (
    <section aria-labelledby="retailer-coverage" className="mt-12">
      <div className="max-w-2xl">
        <h2
          id="retailer-coverage"
          className="text-2xl font-semibold tracking-tight text-stone-950"
        >
          Покрытие магазинов
        </h2>
        <p className="mt-2 text-sm leading-6 text-stone-600">
          Показываем каждый магазин из целевого списка — даже если интеграция или
          доступ к данным ещё не готовы.
        </p>
      </div>

      {state.kind === "unavailable" ? (
        <div
          role="alert"
          className="mt-6 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm leading-6 text-amber-950"
        >
          Не удалось загрузить статус магазинов. Основной сервис временно недоступен.
        </div>
      ) : (
        <ul className="mt-6 grid gap-4 sm:grid-cols-2" aria-label="Статус магазинов">
          {state.data.retailers.map((retailer) => {
            const total = formatTotal(retailer);
            return (
              <li
                key={retailer.id}
                className="rounded-2xl border border-stone-200 bg-white p-5 shadow-sm"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="text-lg font-semibold text-stone-950">
                      {retailer.displayName}
                    </h3>
                    <p className="mt-1 text-sm text-stone-600">
                      {comparisonLabels[retailer.comparisonStatus]}
                    </p>
                  </div>
                  {total ? (
                    <p className="shrink-0 text-base font-semibold text-stone-950">
                      {total}
                    </p>
                  ) : null}
                </div>

                <dl className="mt-4 space-y-2 text-sm">
                  <div className="flex flex-wrap justify-between gap-x-4 gap-y-1">
                    <dt className="text-stone-500">Интеграция</dt>
                    <dd className="font-medium text-stone-800">
                      {coverageLabels[retailer.coverage]}
                    </dd>
                  </div>
                  <div className="flex flex-wrap justify-between gap-x-4 gap-y-1">
                    <dt className="text-stone-500">Данные</dt>
                    <dd className="font-medium text-stone-800">
                      {accessLabels[retailer.productionAccess]}
                    </dd>
                  </div>
                </dl>

                {retailer.reasons.length > 0 ? (
                  <ul className="mt-4 space-y-1 text-sm leading-5 text-stone-600">
                    {retailer.reasons.map((reason) => (
                      <li key={reason}>{reasonLabels[reason]}</li>
                    ))}
                  </ul>
                ) : null}

                {retailer.freshness ? (
                  <p className="mt-4 text-xs leading-5 text-stone-500">
                    Данные наблюдались: {retailer.freshness.observedAt}
                  </p>
                ) : null}
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
