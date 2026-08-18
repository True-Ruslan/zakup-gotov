# Закуп готов / Zakup Gotov

> От рецепта или списка продуктов — к честному сравнению полной корзины в доступных магазинах.

[![API CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/api-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/api-ci.yml)
[![Contract CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/contract-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/contract-ci.yml)
[![Web CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/web-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/web-ci.yml)
[![Release Bundle CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/release-bundle-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/release-bundle-ci.yml)

**Status:** M5 — Productization · **pre-release** · next release gate: **`v0.1.0-rc.3`**

Zakup Gotov превращает рецепт, недельное меню или обычный список покупок в сравнение **полной корзины** по магазинам с учётом местоположения, актуальности цены, наличия, упаковок, checkout-экономики и полноты сопоставления.

Проект намеренно не маскирует неизвестность: продуктовые/core-семантики уже реализованы и приняты, а retailer connectivity развивается отдельным evidence-driven треком. Техническая доступность источника не считается автоматически разрешением на production-использование.

## Зачем проект

Большинство сравнений отвечает на вопрос «где дешевле конкретный товар?». Zakup Gotov ориентирован на более практичный вопрос:

> **Где выгоднее и реально возможно купить всю нужную корзину?**

```text
рецепт / меню / список продуктов
              ↓
     shopping requirements
              ↓
      location + retailer
              ↓
       product matching
              ↓
   price + availability + age
              ↓
      quantity / package fit
              ↓
   checkout economics / eligibility
              ↓
     complete basket ranking
```

Результат сохраняет:

- все canonical retailers, включая недоступные;
- итоговую стоимость корзины, а не отдельных SKU;
- отсутствующие и неоднозначно сопоставленные позиции;
- свежесть цены и наличия;
- известные/неизвестные delivery/service fees и minimum-order evidence;
- различие complete/incomplete/uncertain/unavailable состояний;
- уникального победителя или честный tie только среди сопоставимых корзин.

## Текущее состояние продукта

- **M1 Shopping Core — COMPLETE / ACCEPTED:** canonical quantities, deterministic matching, package-aware basket semantics, truthful incomplete/uncertain/unavailable states и production-access gating.
- **M2 Recipes — COMPLETE / ACCEPTED:** Recipe domain, serving scaling, Recipe → ShoppingList → Comparison, responsive Recipe UI и deterministic multi-Recipe aggregation.
- **M3 Weekly Planning / Pantry — COMPLETE / ACCEPTED:** WeeklyPlan composition, responsive planner, request-scoped Pantry subtraction с audit evidence и Pantry-aware comparison.
- **M4 Basket Optimization — COMPLETE / ACCEPTED:** checkout economics, eligibility/comparability, deterministic cheapest-basket selection, explicit ties и responsive server-owned optimization UX.
- **M5.1 Private local WeeklyPlan draft — COMPLETE / ACCEPTED:** versioned browser-local semantic input draft без серверных аккаунтов, generated IDs, comparison/economics/optimizer results или provider evidence.
- **M5.2 — intentionally unselected:** следующий productization slice выбирается только после реального rc/manual-use evidence.

Платформенная основа:

- Java 25 + Spring Boot 4.1 modular monolith;
- PostgreSQL 18 + Flyway + jOOQ;
- OpenAPI 3.1 + generated TypeScript client;
- Next.js 16.3 / React 19.2;
- Testcontainers, Vitest, Testing Library, Playwright;
- deterministic repository verification;
- production API/web Docker images и no-source-build Compose topology;
- CodeQL, Dependency Review, Container Security, release-contract и release-bundle CI.

## Retailer connectivity

Product/core maturity и retailer acquisition readiness — разные измерения.

- **Perekrestok / Pyaterochka:** приняты browser-bridge acquisition paths; long-lived SPA/store-change lifecycle hardening принято в #153.
- **Magnit:** public-web technical feasibility доказана, но production access остаётся `BLOCKED` по operating policy проекта до подтверждённого права/поддерживаемого канала.
- **Chizhik D1:** транспортное решение принято в #167/#168. Обычный пользовательский браузер получает `/api/v1/shops/`; stock GitHub-hosted Chromium даёт `page-unavailable`. Поэтому выбран **user-browser MV3 Retailer Bridge**, а managed CI/server browser worker не используется.
- **Chizhik D2 transport:** PR #171 добавил фиксированный store-scoped delivery-search transport, но response schema остаётся opaque и автоматический product search/offer mapping выключен.
- **Chizhik store context:** #173/#174 принято. `sap_id` берётся только из exact first-party delivery resource evidence текущей browser-session и обязан существовать в валидированном `/api/v1/shops/`; missing/foreign/unknown/conflicting context fail closed.
- **Chizhik next gate:** #169 ждёт ordinary-user-browser sanitized schema evidence, включая обязательное подтверждение **price field + monetary unit/scale**. До этого `BrowserObservation` / `ObservedOffer` не маппятся.
- **Kuper, Ozon Fresh, Samokat, Lenta, VkusVill** и остальные canonical retailers остаются обязательной connectivity work.

Privacy-safe Chizhik D2 canary: [`docs/integrations/chizhik-d2-delivery-search-canary-2026-08-18.md`](docs/integrations/chizhik-d2-delivery-search-canary-2026-08-18.md).

## Release gate

Следующий основной operational milestone — immutable **`v0.1.0-rc.3`**.

Issue #152 требует:

1. documentation-synchronized final `main`;
2. exact final `main` SHA с зелёными required push workflows;
3. отсутствие существующих `v0.1.0-rc.3` tag/release перед публикацией;
4. published GitHub prerelease, направленный только на этот exact SHA;
5. multi-platform `linux/amd64` + `linux/arm64` staging images;
6. неизменённый fail-closed Trivy `HIGH,CRITICAL` gate;
7. SPDX SBOM;
8. exact-digest staging/final smoke;
9. copy-without-rebuild promotion с сохранением digest identity;
10. provenance attestations и release evidence/checksums;
11. отсутствие `latest` promotion для prerelease;
12. manual product canary именно из immutable rc.3 artifacts.

Stable `v0.1.0` остаётся заблокирован до успешного prerelease и manual acceptance.

## Быстрый старт для разработки

Требуются Java 25, Node.js 24.18.1, pnpm 11.4.0 и запущенный Docker.

```bash
git clone https://github.com/True-Ruslan/zakup-gotov.git
cd zakup-gotov
pnpm install --frozen-lockfile
./scripts/verify.sh
```

Контейнерную production topology можно отдельно проверить:

```bash
./scripts/verify-release-bundle.sh
```

Полная настройка окружения: [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md). Release contract: [`docs/RELEASES.md`](docs/RELEASES.md).

## Архитектура

```text
┌──────────────────────┐          ┌──────────────────────┐
│      Next.js Web     │          │  Future Expo Mobile  │
└──────────┬───────────┘          └──────────┬───────────┘
           └──────────────┬───────────────────┘
                          │ OpenAPI
                          ▼
              ┌──────────────────────┐
              │ Spring Boot / Java 25│
              │   Modular Monolith   │
              └──────────┬───────────┘
                         │
              ┌──────────▼───────────┐
              │ PostgreSQL 18 / jOOQ │
              └──────────────────────┘
                         │
              provider adapter boundary
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
       retailer A     retailer B     retailer ...
```

Основной путь retailer-интеграций — backend provider adapters. Browser integration используется как явный acquisition mode только когда first-party evidence показывает, что пользовательский browser context является корректной границей. Stealth, credential extraction, proxy evasion и private-client impersonation не являются допустимым путём.

## Инженерный контракт

[`docs/ENGINEERING.md`](docs/ENGINEERING.md) фиксирует основные правила:

- TDD: RED → правильная причина → GREEN → regression → refactor;
- evidence before claims;
- automation first;
- реальные PostgreSQL integration tests вместо H2-подмены;
- provider fixtures/contracts вместо live retailer-зависимости обычного CI;
- короткие ветки, небольшие PR, squash-only target history;
- canonical docs обновляются вместе с фактическим состоянием проекта.

## Документация

| Что нужно понять | Документ |
|---|---|
| Текущее фактическое состояние | [`PROJECT_STATE.md`](docs/PROJECT_STATE.md) |
| Что делаем дальше | [`ROADMAP.md`](docs/ROADMAP.md) |
| Карта документации | [`docs/README.md`](docs/README.md) |
| Разработка | [`DEVELOPMENT.md`](docs/DEVELOPMENT.md) |
| Контейнеры и релизы | [`RELEASES.md`](docs/RELEASES.md) |
| Инженерные правила | [`ENGINEERING.md`](docs/ENGINEERING.md) |
| Repository governance | [`REPOSITORY_GOVERNANCE.md`](docs/REPOSITORY_GOVERNANCE.md) |
| Observability / privacy | [`OBSERVABILITY.md`](docs/OBSERVABILITY.md) |
| История изменений | [`CHANGELOG.md`](CHANGELOG.md) |
| Contributing | [`CONTRIBUTING.md`](CONTRIBUTING.md) |
| Security disclosure | [`SECURITY.md`](SECURITY.md) |

## Roadmap

- M0 — COMPLETE;
- M1 — COMPLETE / ACCEPTED;
- M2 — COMPLETE / ACCEPTED;
- M3 — COMPLETE / ACCEPTED;
- M4 — COMPLETE / ACCEPTED;
- M5 — CURRENT; M5.1 accepted, M5.2 intentionally unselected;
- immediate gate — `v0.1.0-rc.3`;
- M6 Native Mobile — future.

Подробности: [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Security

Не публикуйте credentials, provider tokens, точные пользовательские адреса, приватные endpoints или чувствительные provider payloads в issues, logs, fixtures и screenshots.

Уязвимости не следует отправлять публичным issue. Порядок disclosure: [`SECURITY.md`](SECURITY.md).

## License

Репозиторий публичный, но open-source license пока не выбран. До появления `LICENSE` не следует предполагать дополнительное разрешение на использование, распространение или создание производных работ.
