# Закуп готов / Zakup Gotov

> От рецепта или списка продуктов — к честному сравнению полной корзины в доступных магазинах.

[![API CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/api-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/api-ci.yml)
[![Contract CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/contract-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/contract-ci.yml)
[![Web CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/web-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/web-ci.yml)
[![Release Bundle CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/release-bundle-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/release-bundle-ci.yml)

**Status:** M5 — Productization · **pre-release** · next release gate: **`v0.1.0-rc.3`**

Zakup Gotov — сервис, который превращает рецепт, недельное меню или обычный список покупок в сравнение **полной корзины** по магазинам с учётом местоположения, актуальности цены, наличия, упаковок, checkout-экономики и полноты сопоставления.

Проект намеренно не маскирует неизвестность: продуктовые/core-семантики уже реализованы и приняты, но production retailer coverage остаётся отдельной evidence-driven работой для каждого магазина. Техническая доступность источника не считается автоматически разрешением на его production-использование.

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

Результат явно показывает:

- какие магазины способны закрыть весь список;
- итоговую стоимость корзины, а не отдельных SKU;
- отсутствующие и неоднозначно сопоставленные позиции;
- свежесть цены и наличия;
- известные/неизвестные delivery/service fees и minimum-order evidence;
- разницу между complete/incomplete/uncertain/unavailable состояниями;
- уникального победителя или честный tie только среди сопоставимых корзин.

## Текущее состояние

Сейчас реализованы и автоматически проверяются:

- **M1 Shopping Core — COMPLETE / ACCEPTED:** canonical quantities, deterministic matching, package-aware single-store basket semantics, truthful complete/uncertain/incomplete/unavailable states и production-access gating;
- **M2 Recipes — COMPLETE / ACCEPTED:** Recipe domain, serving scaling, Recipe → ShoppingList → Comparison, responsive Recipe UI и deterministic multi-Recipe aggregation;
- **M3 Weekly Planning / Pantry — COMPLETE / ACCEPTED:** WeeklyPlan composition, responsive planner, request-scoped Pantry subtraction с audit evidence и Pantry-aware comparison;
- **M4 Basket Optimization — COMPLETE / ACCEPTED:** checkout economics, eligibility/comparability, deterministic cheapest-basket selection, explicit ties и responsive server-owned optimization UX;
- **M5.1 Private local WeeklyPlan draft — COMPLETE / ACCEPTED:** versioned browser-local semantic input draft без серверных аккаунтов, generated IDs, comparison/economics/optimizer results или provider evidence;
- Java 25 + Spring Boot 4.1 API;
- PostgreSQL 18 + Flyway + jOOQ;
- Spring Modulith architecture verification;
- OpenAPI 3.1 как источник клиентского контракта;
- генерируемый TypeScript API client;
- Next.js 16.3 / React 19.2 responsive web;
- Testcontainers с настоящим PostgreSQL;
- Vitest + Testing Library + Playwright desktop/mobile;
- безопасный Actuator health/readiness baseline;
- воспроизводимый `./scripts/verify.sh`;
- production Docker images для API/web и no-source-build `web + api + PostgreSQL` Compose topology, проверяемая отдельным `Release Bundle CI`.

### Retailer connectivity

Product/core maturity и retailer acquisition readiness считаются разными измерениями:

- Perekrestok/Pyaterochka имеют принятые browser-bridge acquisition evidence;
- Magnit технически доступен через public web, но production access остаётся `BLOCKED` по operating policy проекта до появления подтверждённого права/поддерживаемого канала;
- browser bridge для долгоживущих SPA/store-change сессий укреплён и принят (#153, закрывает #54);
- Chizhik прошёл Phase A/B/C implementation и merged Phase D1 foundation: ordinary-user-browser field canary для `GET https://app.chizhik.club/api/v1/shops/` получил `200 + JSON`, но CI-hosted stock Chromium сейчас даёт live evidence `page-unavailable`, поэтому D1 transport disposition, D2 store-scoped offer mapping и production activation **ещё не приняты**;
- Kuper, Ozon Fresh, Samokat, Lenta, VkusVill и остальные canonical retailers остаются обязательной connectivity work до воспроизводимого принятого acquisition path.

**Ещё не выбраны/не завершены:** M5.2 (accounts/preferences, analytics abstraction, feature flags, provider health — только после release/manual-use evidence), server-side saved-plan history, полное production retailer coverage, richer substitute/package optimization, multi-store split optimization и native mobile.

Новый stable `v0.1.0` также пока не разрешён: сначала должен успешно пройти полный immutable prerelease flow `v0.1.0-rc.3` с multi-platform images, Trivy, SBOM, digest smoke и provenance/attestation evidence.

Фактический статус всегда фиксируется в [`docs/PROJECT_STATE.md`](docs/PROJECT_STATE.md).

## Быстрый старт для разработки

Требуются Java 25, Node.js 24.18.1, pnpm 11.4.0 и запущенный Docker.

```bash
git clone https://github.com/True-Ruslan/zakup-gotov.git
cd zakup-gotov
pnpm install --frozen-lockfile
./scripts/verify.sh
```

Команда не пропускает молча недоступный Docker/Testcontainers, drift сгенерированного OpenAPI-клиента, type checks, tests или production builds.

Контейнерную production topology можно отдельно проверить тем же executable contract, что используется в CI:

```bash
./scripts/verify-release-bundle.sh
```

Полная настройка окружения и focused-команды: [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md). Контейнерный/release contract и требования к immutable GHCR publishing: [`docs/RELEASES.md`](docs/RELEASES.md).

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

Основной путь retailer-интеграций — backend provider adapters. Client-side/browser integration допускается как осознанный acquisition mode, если конкретный first-party источник воспроизводимо работает только/лучше в пользовательском browser context и при этом не требует stealth, credential extraction или приватной mobile-client impersonation.

Архитектурные решения: [`docs/adr/`](docs/adr/) и [`docs/superpowers/specs/`](docs/superpowers/specs/).

## Инженерный контракт

Проект следует строгой политике из [`docs/ENGINEERING.md`](docs/ENGINEERING.md):

- TDD: RED → проверка правильной причины → GREEN → regression → refactor;
- evidence before claims;
- automation first — повторяемая ручная проверка считается automation debt;
- реальные PostgreSQL integration tests вместо H2-подмены;
- provider fixtures/contracts вместо live retailer-зависимости обычного CI;
- короткие ветки, небольшие PR, squash-only target history;
- `PROJECT_STATE`, roadmap, ADR/spec/plan и changelog обновляются вместе с реальностью проекта.

## Документация

| Что нужно понять | Документ |
|---|---|
| Текущее фактическое состояние | [`PROJECT_STATE.md`](docs/PROJECT_STATE.md) |
| Что делаем дальше | [`ROADMAP.md`](docs/ROADMAP.md) |
| Карта всей документации | [`docs/README.md`](docs/README.md) |
| Разработка и локальная проверка | [`DEVELOPMENT.md`](docs/DEVELOPMENT.md) |
| Контейнеры и релизы | [`RELEASES.md`](docs/RELEASES.md) |
| Инженерные правила | [`ENGINEERING.md`](docs/ENGINEERING.md) |
| Repository governance | [`REPOSITORY_GOVERNANCE.md`](docs/REPOSITORY_GOVERNANCE.md) |
| Observability / privacy rules | [`OBSERVABILITY.md`](docs/OBSERVABILITY.md) |
| История заметных изменений | [`CHANGELOG.md`](CHANGELOG.md) |
| Участие в разработке | [`CONTRIBUTING.md`](CONTRIBUTING.md) |
| Сообщение об уязвимостях | [`SECURITY.md`](SECURITY.md) |
| Code of Conduct | [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) |

## Roadmap

- **M0 — Product & Integration Discovery:** COMPLETE.
- **M1 — Shopping Core:** COMPLETE / ACCEPTED.
- **M2 — Recipes:** COMPLETE / ACCEPTED.
- **M3 — Weekly Planning / Pantry:** COMPLETE / ACCEPTED.
- **M4 — Basket Optimization:** COMPLETE / ACCEPTED.
- **M5 — Productization:** CURRENT; M5.1 accepted, M5.2 intentionally unselected until RC/manual-use evidence.
- **Immediate release gate:** immutable **`v0.1.0-rc.3`** end-to-end validation.
- **M6 — Native Mobile:** future, after browser/API semantics stabilize enough to justify native clients.

Подробные scope и exit criteria: [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Security

Не публикуйте credentials, provider tokens, точные пользовательские адреса, приватные endpoints или чувствительные provider payloads в issues, logs, fixtures и screenshots.

Уязвимости **не следует** отправлять публичным issue. Порядок disclosure: [`SECURITY.md`](SECURITY.md).

## Contributing

Перед существенным изменением прочитайте [`CONTRIBUTING.md`](CONTRIBUTING.md), [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md) и [`docs/ENGINEERING.md`](docs/ENGINEERING.md).

## License

Репозиторий публичный, но open-source license пока не выбран. До появления `LICENSE` не следует предполагать дополнительное разрешение на использование, распространение или создание производных работ.
