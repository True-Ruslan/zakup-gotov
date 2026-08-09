# Development

This document describes the reproducible local workflow for Zakup Gotov. Repository truth is defined by the pinned toolchain files, CI workflows, `docs/ENGINEERING.md`, and the current project state.

## Required tools

Install and make available on `PATH`:

- Java 25;
- Node.js 24.18.1 (pinned by `.nvmrc`);
- pnpm 11.4.0 (pinned by root `package.json#packageManager`);
- Docker with a running daemon;
- Git.

The Java build uses the repository-owned Apache Maven Wrapper 3.3.4, pinned to Maven 3.9.16. A global Maven installation is not required.

### Verify prerequisites

```bash
java -version
node --version
pnpm --version
docker info
./apps/api/mvnw -f apps/api/pom.xml --version
```

Expected Node version is exactly `v24.18.1`; expected pnpm version is exactly `11.4.0`; Java must be major version 25.

## Fresh checkout

```bash
git clone https://github.com/True-Ruslan/zakup-gotov.git
cd zakup-gotov
pnpm install --frozen-lockfile
```

Do not bypass pnpm dependency-build policy. The root `pnpm-workspace.yaml` explicitly allows only the build scripts currently required by the Next.js toolchain.

## One-command verification

Run:

```bash
./scripts/verify.sh
```

The script intentionally fails if the pinned Java/Node/pnpm toolchains are not active or Docker is unavailable. It executes:

1. Maven `verify` for the API, including real PostgreSQL 18.4 Testcontainers integration tests and Modulith architecture verification;
2. frozen pnpm workspace installation;
3. OpenAPI generated-client regeneration/drift check;
4. API-client strict typecheck, Vitest tests, and build;
5. web ESLint, strict typecheck, component tests, and production build.

It does not silently skip Testcontainers or convert infrastructure failures into success.

Cloud-only security gates such as CodeQL and Dependency Review remain GitHub checks and cannot be fully reproduced by this script.

## Responsive browser tests

Playwright is kept explicit because browser binaries are a separate local prerequisite and Task 10 treats browser verification as its own gate.

Install Chromium once:

```bash
pnpm --filter web exec playwright install chromium
```

On Linux, if system browser dependencies are missing:

```bash
pnpm --filter web exec playwright install --with-deps chromium
```

Then build and run the E2E suite:

```bash
pnpm --filter @zakup-gotov/api-client build
NEXT_TELEMETRY_DISABLED=1 pnpm --filter web build
pnpm --filter web test:e2e
```

The suite currently exercises desktop (1440×900) and mobile (390×844) Chromium profiles with retries disabled.

## Running PostgreSQL for the API

Tests create disposable PostgreSQL containers automatically. Running the application manually requires a persistent database endpoint.

Example local PostgreSQL 18.4 container:

```bash
docker run --rm \
  --name zakup-gotov-postgres \
  -e POSTGRES_DB=zakup_gotov \
  -e POSTGRES_USER=zakup_gotov \
  -e POSTGRES_PASSWORD=local-development-only \
  -p 5432:5432 \
  postgres:18.4
```

Use a separate terminal and provide runtime configuration through environment variables:

```bash
export DATABASE_URL='jdbc:postgresql://localhost:5432/zakup_gotov'
export DATABASE_USERNAME='zakup_gotov'
export DATABASE_PASSWORD='local-development-only'

./apps/api/mvnw -f apps/api/pom.xml spring-boot:run
```

Never commit real database/provider credentials or local `.env` files. `.env.example` files may be committed only when they contain placeholders, not secrets.

The API currently exposes:

- product API: `GET /api/v1/system`;
- health: `/actuator/health`;
- liveness: `/actuator/health/liveness`;
- readiness: `/actuator/health/readiness`;
- non-sensitive info: `/actuator/info`.

Environment, configuration-properties, and metrics Actuator endpoints are intentionally not exposed over HTTP.

## Running the web application

Install workspace dependencies first:

```bash
pnpm install --frozen-lockfile
```

Then:

```bash
pnpm --filter web dev
```

The M0 page is intentionally only a truthful project-status shell. Retailer comparison must not be represented as working before M0B proves real integrations.

## Focused commands

### Backend

```bash
./apps/api/mvnw -f apps/api/pom.xml --batch-mode --no-transfer-progress test
./apps/api/mvnw -f apps/api/pom.xml -Dtest=ApplicationArchitectureTest test
./apps/api/mvnw -f apps/api/pom.xml -Dtest=PostgresIntegrationTest test
```

### Generated API client

```bash
pnpm --filter @zakup-gotov/api-client check:generated
pnpm --filter @zakup-gotov/api-client typecheck
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build
```

### Web

```bash
pnpm --filter web lint
pnpm --filter web typecheck
pnpm --filter web test
pnpm --filter web build
pnpm --filter web test:e2e
```

## TDD workflow

For executable behavior:

1. write the smallest failing test;
2. run it and confirm it fails for the intended behavioral reason;
3. reject compile/infrastructure failures as invalid RED when they do not exercise the behavior;
4. add the minimum production implementation;
5. run the focused test and relevant regression suite;
6. refactor only while green;
7. update state/changelog/docs in the same PR when repository truth changed.

See `docs/ENGINEERING.md` for the complete policy.

## Docker/Testcontainers troubleshooting

If backend tests fail before Spring behavior is exercised:

```bash
docker info
docker version
```

Do not replace PostgreSQL integration tests with H2 merely to make local verification easier. Fix Docker availability or use a supported CI environment.

The test JVM explicitly grants the native access Testcontainers/JNA needs on Java 25. New unexplained warnings should be treated as defects rather than ignored.

## Dependency updates

Do not edit generated API schema by hand. Change `openapi/zakup-gotov.yaml` and regenerate through:

```bash
pnpm --filter @zakup-gotov/api-client generate
```

Then run `./scripts/verify.sh`.

Dependency version changes must keep lockfiles synchronized and pass the repository security/functional gates. Dependabot automation is introduced during M0A Task 7.
