import { RetailerCoverageSection } from "./retailer-coverage";
import { loadRetailerReadiness } from "./retailer-readiness";

export const dynamic = "force-dynamic";

export default async function Home() {
  const retailerState = await loadRetailerReadiness();

  return (
    <main className="min-h-screen bg-stone-50 text-stone-950">
      <div className="mx-auto w-full max-w-5xl px-6 py-16 sm:px-10 lg:px-16 lg:py-24">
        <div className="max-w-2xl">
          <p className="text-sm font-medium uppercase tracking-[0.18em] text-stone-500">
            M1 · Shopping Core
          </p>

          <h1 className="mt-4 text-4xl font-semibold tracking-tight sm:text-6xl">
            Закуп готов
          </h1>

          <p className="mt-6 max-w-xl text-lg leading-8 text-stone-600 sm:text-xl">
            Список покупок, сопоставление товаров и расчёт одной корзины уже
            работают в ядре. Теперь показываем, какие магазины действительно
            готовы к сравнению и где данных пока недостаточно.
          </p>

          <section
            aria-labelledby="current-status"
            className="mt-10 border-l-2 border-stone-900 pl-5"
          >
            <h2 id="current-status" className="text-sm font-semibold text-stone-900">
              Сейчас
            </h2>
            <p className="mt-2 text-base leading-7 text-stone-600">
              Статус каждого магазина остаётся видимым: подключение источника,
              готовность использования данных и неопределённость не скрываются
              ради красивого результата.
            </p>
          </section>
        </div>

        <RetailerCoverageSection state={retailerState} />

        <a
          href="https://github.com/True-Ruslan/zakup-gotov#readme"
          target="_blank"
          rel="noreferrer"
          className="mt-10 inline-flex min-h-11 items-center rounded-full border border-stone-300 bg-white px-5 py-2.5 text-sm font-medium text-stone-900 transition hover:border-stone-500 hover:bg-stone-100 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-stone-900"
        >
          Документация проекта
        </a>
      </div>
    </main>
  );
}
