# Changelog

All notable project changes will be documented in this file.

The project is currently pre-release. Changelog entries should describe user-visible, architectural, security, integration, and operational changes that matter for understanding project history. Routine internal refactors may be omitted unless they materially affect behavior or maintenance.

The format follows the spirit of Keep a Changelog and semantic versioning will be adopted when the first versioned release is prepared.

## [Unreleased]

### Added

- Initial public repository.
- Foundation product and architecture specification.
- Accepted platform stack ADR.
- Initial project state and evidence-driven roadmap.
- M0A Platform Foundation implementation plan.
- Engineering policy defining mandatory TDD, evidence-based verification, automation-first testing, documentation synchronization, clean Git/PR discipline, and changelog maintenance.
- Security, contribution, and conduct policies.
- Structured pull request and issue templates.
- M0A toolchain/workspace baseline: Java 25, Node.js 24 LTS, pnpm 11 workspace, repository text/ignore rules, and ADR-0002 for build tooling.
- First executable API foundation on Spring Boot 4.1 / Java 25 with Virtual Threads enabled and Actuator health exposure.
- Spring Modulith architecture verification test and Spring application-context bootstrap test.
- Apache Maven Wrapper 3.3.4 generated from the official plugin with Maven 3.9.16 pinned.
- Early `API CI` workflow verifying Java 25, the pinned Maven Wrapper, and backend tests on every affected pull request/main change.
- PostgreSQL 18 persistence baseline with environment-supplied datasource configuration.
- Flyway migration baseline creating the application-owned `app` schema and maintaining schema history.
- jOOQ PostgreSQL integration as the primary SQL access layer.
- Testcontainers-backed PostgreSQL 18.4 integration environment shared by Spring application and persistence tests.
- Contract-first OpenAPI 3.1 product API baseline with `GET /api/v1/system`.
- MVC contract test for the system endpoint against the real application/PostgreSQL integration context.
- Generated `@zakup-gotov/api-client` TypeScript package using `openapi-typescript` + `openapi-fetch`.
- Committed generated API schema and deterministic root `pnpm-lock.yaml`.
- Read-only `Contract CI` that verifies pinned Node/pnpm, frozen dependency installation, generated-schema drift, strict typecheck, Vitest, and package build.

### Changed

- Foundation architecture was approved on 2026-08-09.
- Project execution entered M0A Platform Foundation before retailer feasibility work.
- API bootstrap dependencies were reduced after regression evidence exposed unnecessary zero-module Modulith runtime and unused Mockito warning noise.
- API CI was introduced earlier than the original M0A sequencing so backend TDD could be proven on the exact Java 25 toolchain instead of inferred from an incompatible local runtime.
- Spring application-context verification now boots against real PostgreSQL rather than a database-free context.
- Test JVM explicitly enables native access required by Testcontainers/JNA on Java 25 to avoid unsupported-access warning noise.
- TypeScript for the generated API-client toolchain is pinned to compatible 5.9.3 after CI proved that TypeScript 7.0.2 violates `openapi-typescript 7.13.0`'s supported peer range.
- Permanent Contract CI installs exact pnpm 11.4.0 after Node setup instead of using `pnpm/action-setup`, removing the known PNPM_HOME layout warning while keeping the toolchain pin explicit.

### Fixed

- Corrected the Spring Boot 4 Flyway wiring after the persistence test proved that adding Flyway libraries alone did not activate Boot's separated Flyway auto-configuration module; the project now uses `spring-boot-starter-flyway` plus the PostgreSQL Flyway module.
- Corrected Spring Boot 4 MVC test wiring by adding the focused `spring-boot-starter-webmvc-test` test module after the first controller test compile attempt exposed the split test auto-configuration.

### Security

- Established security-reporting and secret/privacy handling policy; broader repository security automation will be activated during M0A.
- Production database credentials are required through external environment configuration and are not stored in source control.
- Contract CI uses read-only repository permissions and verifies the lockfile through pnpm's supply-chain policy check during frozen installation.
