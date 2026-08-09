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

### Changed

- Foundation architecture was approved on 2026-08-09.
- Project execution entered M0A Platform Foundation before retailer feasibility work.
- API bootstrap dependencies were reduced after regression evidence exposed unnecessary zero-module Modulith runtime and unused Mockito warning noise.
- API CI was introduced earlier than the original M0A sequencing so backend TDD could be proven on the exact Java 25 toolchain instead of inferred from an incompatible local runtime.

### Fixed

- Nothing yet.

### Security

- Established security-reporting and secret/privacy handling policy; broader repository security automation will be activated during M0A.
