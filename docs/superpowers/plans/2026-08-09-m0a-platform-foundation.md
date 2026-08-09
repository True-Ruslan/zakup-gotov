# M0A Platform Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish an executable, secure, testable monorepo foundation for Zakup Gotov before retailer-specific integration experiments begin.

**Architecture:** Build the first deployable as a modular monolith: Java 25/Spring Boot 4.1 API, PostgreSQL 18 persistence baseline, contract-first REST/OpenAPI, and a responsive Next.js 16 web client. Keep retailer integrations out of M0A; M0B will use this foundation to prove real provider feasibility without changing the platform architecture.

**Tech Stack:** Java 25 LTS, Maven 3.9.16, Spring Boot 4.1.0, Spring Modulith 2.1.0, PostgreSQL 18, Flyway, jOOQ, Testcontainers, OpenAPI 3.1.x, Node.js 24 LTS, pnpm 11, Next.js 16.2, React 19, TypeScript strict mode, Vitest, Testing Library, Playwright, GitHub Actions.

## Global Constraints

- Architecture remains a modular monolith; no microservices in M0A.
- Backend baseline is Java 25 LTS + Spring Boot 4.1 + Spring MVC + Virtual Threads + Spring Modulith.
- Database baseline is PostgreSQL 18; Flyway owns schema migrations and jOOQ is the primary SQL access layer.
- Public product API is REST/JSON described by OpenAPI 3.1.x.
- Web baseline is Next.js 16 + React + TypeScript strict mode + App Router and must be responsive from the first executable milestone.
- Future native mobile remains Expo + React Native + TypeScript; do not create `apps/mobile` in this plan.
- No Kafka, Kubernetes, Redis, Elasticsearch/OpenSearch, vector database, GraphQL, or AI-first matching dependency.
- No secrets, credentials, private retailer endpoints, or personal address data may enter source control, fixtures, logs, or CI artifacts.
- M0A does not claim retailer feasibility. M0 succeeds only after M0B proves at least two acceptable retailer integrations.
- Use TDD for executable behavior and small reviewable commits.

---

## Planned file structure

```text
zakup-gotov/
├── .github/
│   ├── workflows/
│   │   ├── api-ci.yml
│   │   ├── web-ci.yml
│   │   ├── codeql.yml
│   │   └── dependency-review.yml
│   └── dependabot.yml
├── apps/
│   ├── api/
│   │   ├── .mvn/wrapper/
│   │   ├── mvnw
│   │   ├── mvnw.cmd
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/java/io/github/trueruslan/zakupgotov/
│   │       ├── main/resources/
│   │       └── test/java/io/github/trueruslan/zakupgotov/
│   └── web/
├── packages/
│   └── api-client/
├── openapi/
│   └── zakup-gotov.yaml
├── scripts/
│   └── verify.sh
├── .editorconfig
├── .gitattributes
├── .gitignore
├── .java-version
├── .nvmrc
├── package.json
└── pnpm-workspace.yaml
```

Responsibilities:

- `apps/api`: product backend and module boundaries; no retailer-specific code in M0A.
- `apps/web`: responsive browser client; no duplicated backend domain logic.
- `openapi`: source-of-truth HTTP contract.
- `packages/api-client`: generated TypeScript API types/client wrapper shared by web and future mobile.
- `scripts/verify.sh`: one deterministic local verification entrypoint mirrored by CI.

---

### Task 1: Pin toolchains and create monorepo workspace

**Files:**
- Create: `.java-version`
- Create: `.nvmrc`
- Create: `.editorconfig`
- Create: `.gitattributes`
- Create: `.gitignore`
- Create: `package.json`
- Create: `pnpm-workspace.yaml`
- Create: `docs/adr/0002-build-and-workspace-tooling.md`

**Interfaces:**
- Consumes: ADR-0001 platform baseline.
- Produces: Java/Node/pnpm version contract and JS workspace used by later tasks.

- [ ] **Step 1: Add exact runtime pins**

`.java-version`:

```text
25
```

`.nvmrc`:

```text
24.18.0
```

Root `package.json`:

```json
{
  "name": "zakup-gotov",
  "private": true,
  "packageManager": "pnpm@11.4.0",
  "engines": {
    "node": "24.x",
    "pnpm": "11.x"
  },
  "scripts": {
    "web:dev": "pnpm --dir apps/web dev",
    "web:build": "pnpm --dir apps/web build",
    "web:test": "pnpm --dir apps/web test",
    "web:lint": "pnpm --dir apps/web lint",
    "api:test": "./apps/api/mvnw -f apps/api/pom.xml verify"
  }
}
```

`pnpm-workspace.yaml`:

```yaml
packages:
  - apps/web
  - packages/*
```

- [ ] **Step 2: Add repository-wide text/build hygiene**

`.editorconfig` must enforce UTF-8, LF, final newlines, 2 spaces for YAML/JSON/TS and 4 spaces for Java. `.gitattributes` must normalize text to LF. `.gitignore` must exclude Java targets, Node artifacts, Next build output, IDE metadata, local env files, Playwright output, coverage, and OS junk while keeping `.env.example` trackable.

- [ ] **Step 3: Record build-tool decision**

Create ADR-0002 accepting Maven 3.9.16 for the Java build and pnpm workspaces for TypeScript. State why Maven 4 RC is not used before GA and why a single polyglot build orchestrator is not introduced yet.

- [ ] **Step 4: Verify toolchain locally**

Run:

```bash
java --version
node --version
corepack enable
pnpm --version
```

Expected:

```text
Java major: 25
Node: v24.18.0
pnpm: 11.4.0
```

- [ ] **Step 5: Commit**

```bash
git add .java-version .nvmrc .editorconfig .gitattributes .gitignore package.json pnpm-workspace.yaml docs/adr/0002-build-and-workspace-tooling.md
git commit -m "chore: establish project toolchains"
```

---

### Task 2: Bootstrap the Spring Boot API and enforce Modulith boundaries

**Files:**
- Create: `apps/api/pom.xml`
- Create: `apps/api/.mvn/wrapper/*`
- Create: `apps/api/mvnw`
- Create: `apps/api/mvnw.cmd`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/ZakupGotovApplication.java`
- Create: `apps/api/src/main/resources/application.yml`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/ZakupGotovApplicationTest.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/ApplicationArchitectureTest.java`

**Interfaces:**
- Consumes: Java 25 toolchain from Task 1.
- Produces: executable Spring application and architecture verification command `./apps/api/mvnw -f apps/api/pom.xml verify`.

- [ ] **Step 1: Write failing application and architecture tests first**

```java
package io.github.trueruslan.zakupgotov;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZakupGotovApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

```java
package io.github.trueruslan.zakupgotov;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationArchitectureTest {
    @Test
    void moduleStructureIsValid() {
        ApplicationModules.of(ZakupGotovApplication.class).verify();
    }
}
```

- [ ] **Step 2: Run tests and confirm they fail because the application/build does not exist yet**

```bash
./apps/api/mvnw -f apps/api/pom.xml test
```

Expected: FAIL before bootstrap is created.

- [ ] **Step 3: Create Maven build**

Use Spring Boot parent `4.1.0`, Java release `25`, and Spring Modulith BOM `2.1.0`. Include only foundation dependencies:

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-core</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-insight</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

Generate Maven Wrapper pinned to 3.9.16.

- [ ] **Step 4: Add minimal application**

```java
package io.github.trueruslan.zakupgotov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZakupGotovApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZakupGotovApplication.class, args);
    }
}
```

`application.yml` must enable virtual threads and expose only health/info actuator endpoints by default:

```yaml
spring:
  application:
    name: zakup-gotov-api
  threads:
    virtual:
      enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      probes:
        enabled: true
```

- [ ] **Step 5: Run tests**

```bash
./apps/api/mvnw -f apps/api/pom.xml verify
```

Expected: PASS for context and Modulith verification.

- [ ] **Step 6: Commit**

```bash
git add apps/api
git commit -m "feat: bootstrap modular Spring API"
```

---

### Task 3: Establish PostgreSQL, Flyway, jOOQ, and Testcontainers baseline

**Files:**
- Modify: `apps/api/pom.xml`
- Modify: `apps/api/src/main/resources/application.yml`
- Create: `apps/api/src/main/resources/db/migration/V1__baseline.sql`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/database/PostgresIntegrationTest.java`

**Interfaces:**
- Consumes: Spring API from Task 2.
- Produces: verified real-PostgreSQL persistence baseline and `DSLContext` available to later domain modules.

- [ ] **Step 1: Write failing PostgreSQL integration test**

The test must start PostgreSQL `18.4`, inject datasource properties, allow Flyway to migrate, and assert jOOQ can query both the server version and the `app` schema.

Core assertion:

```java
assertThat(dsl.fetchValue("select current_schema()", String.class)).isEqualTo("app");
assertThat(dsl.fetchValue("select current_setting('server_version_num')::int", Integer.class))
        .isGreaterThanOrEqualTo(180000);
```

- [ ] **Step 2: Run the test and verify failure before persistence dependencies/configuration exist**

```bash
./apps/api/mvnw -f apps/api/pom.xml -Dtest=PostgresIntegrationTest test
```

Expected: FAIL due to missing datasource/jOOQ/Flyway setup.

- [ ] **Step 3: Add managed persistence dependencies**

Add Spring Boot managed artifacts without overriding their BOM versions:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jooq</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-database-postgresql</artifactId>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers-postgresql</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers-junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
```

Spring Boot 4.1.0 manages jOOQ 3.21.x, Flyway 12.4.x, and Testcontainers 2.0.x; do not override those versions in M0A.

- [ ] **Step 4: Add baseline migration and configuration**

`V1__baseline.sql`:

```sql
CREATE SCHEMA IF NOT EXISTS app;
```

Configure Flyway default schema and jOOQ PostgreSQL dialect. Production/local datasource credentials must come from environment variables; no credentials are committed.

- [ ] **Step 5: Run full backend verification**

```bash
./apps/api/mvnw -f apps/api/pom.xml verify
```

Expected: PASS against a real PostgreSQL Testcontainer.

- [ ] **Step 6: Commit**

```bash
git add apps/api
git commit -m "feat: establish PostgreSQL persistence baseline"
```

---

### Task 4: Create contract-first OpenAPI and generated TypeScript API client

**Files:**
- Create: `openapi/zakup-gotov.yaml`
- Create: `packages/api-client/package.json`
- Create: `packages/api-client/tsconfig.json`
- Create: `packages/api-client/src/index.ts`
- Generate: `packages/api-client/src/schema.d.ts`
- Create: `packages/api-client/src/index.test.ts`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/system/SystemController.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/system/SystemControllerTest.java`

**Interfaces:**
- Produces endpoint `GET /api/v1/system` returning `{name,status}`.
- Produces package `@zakup-gotov/api-client` used by web now and native clients later.

- [ ] **Step 1: Define OpenAPI 3.1 contract first**

```yaml
openapi: 3.1.0
info:
  title: Zakup Gotov API
  version: 0.0.0
paths:
  /api/v1/system:
    get:
      operationId: getSystemInfo
      responses:
        '200':
          description: API availability information
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SystemInfo'
components:
  schemas:
    SystemInfo:
      type: object
      additionalProperties: false
      required: [name, status]
      properties:
        name:
          type: string
          const: zakup-gotov-api
        status:
          type: string
          enum: [UP]
```

- [ ] **Step 2: Write backend MVC test before controller implementation**

Assert HTTP 200 and exact JSON fields `name=zakup-gotov-api`, `status=UP`.

- [ ] **Step 3: Verify the test fails**

```bash
./apps/api/mvnw -f apps/api/pom.xml -Dtest=SystemControllerTest test
```

Expected: FAIL with 404.

- [ ] **Step 4: Implement minimal controller and make the test pass**

Return an immutable response record; do not expose actuator payloads as product API.

- [ ] **Step 5: Generate the TypeScript contract**

Inside `packages/api-client`, add `openapi-typescript` and `openapi-fetch`. Generate `schema.d.ts` from `../../openapi/zakup-gotov.yaml` and expose a `createZakupGotovClient(baseUrl)` wrapper.

Test that the generated path type contains `/api/v1/system` and that the wrapper builds without `any` escapes.

- [ ] **Step 6: Verify Java and TypeScript sides**

```bash
./apps/api/mvnw -f apps/api/pom.xml verify
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add openapi packages/api-client apps/api
git commit -m "feat: establish product API contract"
```

---

### Task 5: Bootstrap responsive Next.js web shell

**Files:**
- Create: `apps/web/*` through `create-next-app` using Next.js 16.2, TypeScript, App Router, ESLint, Tailwind, and `src/` layout.
- Modify: `apps/web/src/app/page.tsx`
- Modify: `apps/web/src/app/layout.tsx`
- Create: `apps/web/src/app/page.test.tsx`
- Create: `apps/web/playwright.config.ts`
- Create: `apps/web/e2e/home.spec.ts`

**Interfaces:**
- Consumes: `@zakup-gotov/api-client` package.
- Produces: accessible responsive application shell, not the final shopping experience.

- [ ] **Step 1: Scaffold web app**

```bash
pnpm create next-app@16.2.0 apps/web --ts --eslint --tailwind --app --src-dir --import-alias "@/*" --use-pnpm
```

Add workspace dependency on `@zakup-gotov/api-client`.

- [ ] **Step 2: Write component test for the product shell before replacing generated page**

Assert the page contains exactly one H1 `Закуп готов`, product-status text explaining that retailer integrations are being validated, and a link to project documentation. Avoid fake working CTAs before M1.

- [ ] **Step 3: Run component test and verify failure**

```bash
pnpm --dir apps/web test
```

Expected: FAIL against generated Next page.

- [ ] **Step 4: Implement minimal responsive shell**

Requirements:

- semantic `main`, `h1`, body copy;
- no horizontal overflow at 320px width;
- readable max text width on desktop;
- visible focus state;
- system color preference respected or a neutral single theme; no theme framework yet;
- no claims that current retailer comparison already works.

- [ ] **Step 5: Add Playwright smoke test**

Test Chromium at desktop and mobile viewport. Both must render H1 and have no page-level horizontal overflow.

- [ ] **Step 6: Verify web**

```bash
pnpm --dir apps/web lint
pnpm --dir apps/web test
pnpm --dir apps/web build
pnpm --dir apps/web exec playwright test
```

Expected: all PASS.

- [ ] **Step 7: Commit**

```bash
git add apps/web pnpm-lock.yaml package.json pnpm-workspace.yaml
git commit -m "feat: bootstrap responsive web shell"
```

---

### Task 6: Add observability and safe operational defaults

**Files:**
- Modify: `apps/api/src/main/resources/application.yml`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/operations/ActuatorSecurityTest.java`
- Create: `docs/OBSERVABILITY.md`

**Interfaces:**
- Produces: health/readiness signals and a documented vendor-neutral telemetry vocabulary for later provider metrics.

- [ ] **Step 1: Write failing actuator exposure test**

Assert `/actuator/health` is available, while sensitive endpoints such as `/actuator/env` are not exposed over HTTP.

- [ ] **Step 2: Run test and confirm expected failure if configuration is too broad/absent**

```bash
./apps/api/mvnw -f apps/api/pom.xml -Dtest=ActuatorSecurityTest test
```

- [ ] **Step 3: Configure safe defaults**

Expose only `health` and `info`. Enable liveness/readiness probes. Keep all request/header/body logging disabled by default for external-provider payloads until explicit redaction rules exist.

- [ ] **Step 4: Document metric names reserved for M0B/M1**

Use stable names such as:

```text
zakup.provider.request.duration
zakup.provider.request.errors
zakup.provider.offer.age
zakup.matching.confidence
zakup.basket.completeness
zakup.basket.compute.duration
```

No vendor-specific instrumentation calls in domain modules.

- [ ] **Step 5: Run backend verification and commit**

```bash
./apps/api/mvnw -f apps/api/pom.xml verify
git add apps/api docs/OBSERVABILITY.md
git commit -m "chore: add operational safety baseline"
```

---

### Task 7: Add CI, dependency automation, and security gates

**Files:**
- Create: `.github/workflows/api-ci.yml`
- Create: `.github/workflows/web-ci.yml`
- Create: `.github/workflows/codeql.yml`
- Create: `.github/workflows/dependency-review.yml`
- Create: `.github/dependabot.yml`

**Interfaces:**
- Produces required checks intended for protected `main`: `API CI`, `Web CI`, `CodeQL`, `Dependency Review`.

- [ ] **Step 1: Add API CI**

Trigger on pull requests and pushes to `main`. Use Java 25, Maven dependency cache, and run:

```bash
./apps/api/mvnw -f apps/api/pom.xml --batch-mode --no-transfer-progress verify
```

Testcontainers must run on GitHub-hosted Linux without a separately mocked database.

- [ ] **Step 2: Add web CI**

Use Node 24, Corepack/pnpm lockfile caching, frozen installs, and run:

```bash
pnpm install --frozen-lockfile
pnpm --dir apps/web lint
pnpm --dir apps/web test
pnpm --dir apps/web build
```

Playwright browser E2E may be a separate job so component/build feedback arrives first.

- [ ] **Step 3: Add CodeQL**

Analyze Java and JavaScript/TypeScript on pull requests, pushes to `main`, and a weekly schedule. Generated sources/build output must not be committed merely to satisfy CodeQL.

- [ ] **Step 4: Add dependency review**

Run GitHub Dependency Review on pull requests and fail for newly introduced high/critical known vulnerabilities unless an explicit documented exception is approved.

- [ ] **Step 5: Configure Dependabot**

Configure weekly updates for:

```text
maven -> /apps/api
npm -> /
github-actions -> /
```

Group low-risk patch/minor updates where supported; security updates remain independently actionable.

- [ ] **Step 6: Verify workflow syntax by opening/updating the PR and observing actual runs**

Expected: all four named check families execute; failures are fixed before repository rules make them required.

- [ ] **Step 7: Commit**

```bash
git add .github/workflows .github/dependabot.yml
git commit -m "ci: establish quality and security gates"
```

---

### Task 8: Add one-command verification and developer bootstrap documentation

**Files:**
- Create: `scripts/verify.sh`
- Create: `docs/DEVELOPMENT.md`
- Modify: `CONTRIBUTING.md`
- Modify: `README.md`

**Interfaces:**
- Produces: local command `./scripts/verify.sh` whose substantive checks mirror CI.

- [ ] **Step 1: Create verification script**

```bash
#!/usr/bin/env bash
set -euo pipefail

./apps/api/mvnw -f apps/api/pom.xml --batch-mode --no-transfer-progress verify
pnpm install --frozen-lockfile
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build
pnpm --dir apps/web lint
pnpm --dir apps/web test
pnpm --dir apps/web build
```

Do not silently skip Docker/Testcontainers requirements.

- [ ] **Step 2: Document fresh-machine bootstrap**

`docs/DEVELOPMENT.md` must list Java 25, Node 24.18.0, pnpm 11.4.0, Docker, commands to run API/web, environment-variable policy, and troubleshooting for unavailable Docker/Testcontainers.

- [ ] **Step 3: Run clean verification**

```bash
./scripts/verify.sh
```

Expected: PASS from a clean checkout after installing documented prerequisites.

- [ ] **Step 4: Commit**

```bash
git add scripts/verify.sh docs/DEVELOPMENT.md README.md CONTRIBUTING.md
git commit -m "docs: document reproducible development workflow"
```

---

### Task 9: Activate repository governance after checks exist

**Files:**
- Create: `docs/REPOSITORY_GOVERNANCE.md`
- Modify: `SECURITY.md`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes actual check names from Task 7.
- Produces protected `main` policy and an auditable list of settings that cannot be represented in Git-tracked files.

- [ ] **Step 1: Confirm CI names from a real PR run before protecting main**

Required checks must be based on exact successful GitHub check names, not guessed strings.

- [ ] **Step 2: Configure repository merge policy**

Target state:

```text
allow_squash_merge=true
allow_merge_commit=false
allow_rebase_merge=false
delete_branch_on_merge=true
allow_auto_merge=true
```

- [ ] **Step 3: Configure main ruleset**

Require pull requests, required successful checks, linear history, conversation resolution, and block force pushes/deletion. Because this repository currently has a single maintainer, do not require an impossible second human approval unless another maintainer is added; CODEOWNERS remains useful for future collaboration.

- [ ] **Step 4: Enable security repository settings**

Enable Dependabot alerts/security updates, secret scanning, push protection where GitHub supports it for the repository, and Private Vulnerability Reporting. Record any GitHub-plan limitation honestly in `docs/REPOSITORY_GOVERNANCE.md`.

- [ ] **Step 5: Update state and changelog**

`PROJECT_STATE.md` must state that foundation architecture is approved, M0A implementation is complete, and M0B retailer feasibility is the next gate. `CHANGELOG.md` records the executable platform/CI foundation under `[Unreleased]`.

- [ ] **Step 6: Commit documentation changes**

```bash
git add docs/REPOSITORY_GOVERNANCE.md docs/PROJECT_STATE.md SECURITY.md CHANGELOG.md
git commit -m "docs: record repository governance baseline"
```

---

### Task 10: Final M0A verification and handoff to M0B

**Files:**
- Modify only if verification reveals a concrete defect.
- Next plan to create after M0A passes: `docs/superpowers/plans/2026-08-09-m0b-retailer-feasibility.md`.

**Interfaces:**
- Produces a clean, executable foundation for provider discovery work.

- [ ] **Step 1: Run complete local verification**

```bash
./scripts/verify.sh
```

Expected: PASS.

- [ ] **Step 2: Run Playwright E2E explicitly**

```bash
pnpm --dir apps/web exec playwright test
```

Expected: PASS on configured Chromium desktop/mobile projects.

- [ ] **Step 3: Verify architecture rules**

```bash
./apps/api/mvnw -f apps/api/pom.xml -Dtest=ApplicationArchitectureTest test
```

Expected: PASS with no module dependency violations.

- [ ] **Step 4: Inspect the PR checks on GitHub**

Expected: API CI, Web CI, CodeQL, and Dependency Review all green. No secret-scanning or dependency alert introduced by the foundation.

- [ ] **Step 5: Scope check**

Verify that M0A contains no retailer-specific scraping/API code, no recipe feature implementation, no auth, no mobile app, and none of the explicitly deferred infrastructure.

- [ ] **Step 6: Prepare M0B plan rather than coding providers ad hoc**

M0B must cover, in order: retailer research matrix; legal/terms evidence; location/fulfillment-context model; `GroceryProvider` port; opt-in live probe harness; fixture sanitization; at least two retailer spikes; parser/contract failure tests; freshness/rate-limit evidence; and M0 go/no-go decision.

---

## Self-review results

- **Spec coverage:** M0A covers repository quality/security, executable platform, modular architecture verification, PostgreSQL/Flyway/jOOQ baseline, REST/OpenAPI contract, responsive web path, observability baseline, CI, and reproducible development. Retailer feasibility is deliberately deferred to the separately scoped M0B plan because it is an independent externally constrained subsystem.
- **Placeholder scan:** no implementation placeholders are intentionally left in this plan; version/tool choices needed for M0A are explicit.
- **Type/interface consistency:** Java base package is `io.github.trueruslan.zakupgotov`; HTTP smoke contract is `GET /api/v1/system`; shared TypeScript package is `@zakup-gotov/api-client`; verification entrypoint is `./scripts/verify.sh`.

## Completion gate

M0A is complete only when a fresh checkout can build/test the Java API and TypeScript web/client, PostgreSQL integration tests run against a real Testcontainer, architecture verification passes, the responsive web smoke test passes, CI/security checks are green, and `main` governance is activated.

Passing M0A does **not** mean the product is viable. The next required milestone is M0B retailer feasibility, and M0 remains incomplete until at least two retailer integrations are proven with acceptable technical/legal evidence and reproducible fixtures/tests.