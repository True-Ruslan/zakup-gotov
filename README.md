# Закуп готов / Zakup Gotov

> От рецепта или списка продуктов — к честному сравнению полной корзины в доступных магазинах.

[![API CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/api-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/api-ci.yml)
[![Contract CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/contract-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/contract-ci.yml)
[![Web CI](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/web-ci.yml/badge.svg)](https://github.com/True-Ruslan/zakup-gotov/actions/workflows/web-ci.yml)

**Status:** M0 — Product & Integration Discovery · **pre-release**

Zakup Gotov — сервис, который должен превращать рецепт, недельное меню или обычный список покупок в сравнение **полной корзины** по магазинам с учётом местоположения, актуальности цены, наличия и полноты сопоставления.

Проект намеренно не маскирует неизвестность: пока реальные retailer-интеграции не доказаны в M0B, интерфейс не выдаёт сравнение магазинов за готовую функцию.

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
     complete basket ranking
```

Целевой результат должен явно показывать:

- какие магазины способны закрыть весь список;
- итоговую стоимость корзины, а не отдельных SKU;
- отсутствующие и неоднозначно сопоставленные позиции;
- свежесть цены и наличия;
- разницу между удобством одной корзины и минимальной ценой нескольких магазинов.

## Текущее состояние

Сейчас реализована и автоматически проверяется платформенная основа:

- Java 25 + Spring Boot 4.1 API;
- PostgreSQL 18 + Flyway + jOOQ;
- Spring Modulith architecture verification;
- OpenAPI 3.1 как источник клиентского контракта;
- генерируемый TypeScript API client;
- Next.js 16 / React 19 responsive web shell;
- Testcontainers с настоящим PostgreSQL;
- Vitest + Testing Library + Playwright desktop/mobile;
- безопасный Actuator health/readiness baseline;
- воспроизводимый `./scripts/verify.sh`.

**Ещё не реализованы:** retailer integrations, shopping-list domain, recipes, matching, basket optimization, auth и native mobile. Это запланированные этапы, а не скрытые незавершённые функции.

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

Полная настройка окружения, focused-команды и Playwright: [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md).

> Готовый `docker compose` релиз без локальной сборки — утверждённое направление release engineering и будет добавлен отдельным этапом после repository baseline.

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

Основной путь retailer-интеграций — backend provider adapters. Client-side integration допускается только как осознанное исключение, если конкретный публичный API не требует секретов, разрешает CORS/browser use или действительно зависит от пользовательской browser-session.

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
| Инженерные правила | [`ENGINEERING.md`](docs/ENGINEERING.md) |
| Repository governance | [`REPOSITORY_GOVERNANCE.md`](docs/REPOSITORY_GOVERNANCE.md) |
| Observability / privacy rules | [`OBSERVABILITY.md`](docs/OBSERVABILITY.md) |
| История заметных изменений | [`CHANGELOG.md`](CHANGELOG.md) |
| Участие в разработке | [`CONTRIBUTING.md`](CONTRIBUTING.md) |
| Сообщение об уязвимостях | [`SECURITY.md`](SECURITY.md) |
| Code of Conduct | [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) |

## Roadmap

- **M0A — Platform Foundation:** завершаем security/governance/release-ready repository baseline.
- **M0B — Retailer Feasibility:** доказываем минимум две технически и юридически приемлемые интеграции.
- **M1 — Shopping Core**
- **M2 — Recipes**
- **M3 — Weekly Planning**
- **M4 — Basket Optimization**
- **M5 — Productization**
- **M6 — Native Mobile**

Подробные scope и exit criteria: [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Security

Не публикуйте credentials, provider tokens, точные пользовательские адреса, приватные endpoints или чувствительные provider payloads в issues, logs, fixtures и screenshots.

Уязвимости **не следует** отправлять публичным issue. Порядок disclosure: [`SECURITY.md`](SECURITY.md).

## Contributing

Перед существенным изменением прочитайте [`CONTRIBUTING.md`](CONTRIBUTING.md), [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md) и [`docs/ENGINEERING.md`](docs/ENGINEERING.md).

## License

Репозиторий публичный, но open-source license пока не выбран. До появления `LICENSE` не следует предполагать дополнительное разрешение на использование, распространение или создание производных работ.
