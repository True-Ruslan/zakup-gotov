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
- Responsive Next.js 16.2 / React 19.2 web application scaffold linked to the shared API-client workspace package.
- Honest M0 landing shell that states retailer integrations and price freshness are still being validated instead of presenting unavailable comparison functionality.
- Vitest + Testing Library component test for the web shell.
- Playwright production-browser coverage for desktop and mobile viewports, horizontal-overflow protection, and keyboard-focus visibility.
- Read-only `Web CI` covering frozen dependency installation, shared-client build, ESLint, TypeScript, component tests, production Next.js build, and responsive Chromium E2E.
- Next.js build-cache persistence through GitHub Actions.
- Safe Actuator operational surface exposing only health, liveness, readiness, and non-sensitive info over HTTP.
- Executable Actuator security test that rejects HTTP exposure of environment, configuration-properties, and metrics endpoints.
- `docs/OBSERVABILITY.md` defining telemetry naming, low-cardinality constraints, provider redaction rules, and liveness/readiness semantics.
- Reproducible `./scripts/verify.sh` developer verification entrypoint covering backend, real PostgreSQL/Testcontainers, generated OpenAPI drift, client checks, and web checks/build.
- `docs/DEVELOPMENT.md` with pinned prerequisites, local run instructions, focused verification commands, Playwright setup, and Docker/Testcontainers troubleshooting.
- `docs/README.md` documentation index separating current state, roadmap, ADRs, specifications, implementation plans, and history.
- `docs/REPOSITORY_GOVERNANCE.md` defining merge, branch, Actions, security-feature, and future release-governance policy.
- `.github/SUPPORT.md` routing bugs, proposals, contributions, and confidential security reports to appropriate channels.
- Approved repository-governance and Docker/GHCR release-engineering specification, including multi-platform images, Compose distribution, supply-chain evidence, and backend-first provider policy.
- Repository-hardening implementation plan covering deterministic required checks, immutable Actions, GitHub-native security, rulesets, branch cleanup, and social-preview handoff.
- Permanent CodeQL scanning for Java and JavaScript/TypeScript, Dependency Review, and weekly Dependabot version-update configuration.
- Production multi-stage Docker images for the Java API and Next.js standalone web runtime, both running as non-root users.
- `compose.release.yaml` no-source-build production topology for PostgreSQL 18.4 → API → web with persistent database storage and health/readiness dependencies.
- `Release Bundle CI` and `./scripts/verify-release-bundle.sh`, which build both application images, start the complete container topology, wait for health, smoke-test API/web boundaries, and emit Compose diagnostics on failure.
- `docs/RELEASES.md` documenting the verified container bundle and explicitly separating it from the versioned GHCR publication workflow.
- `Release Contract CI` and `scripts/release/release_contract.py`, providing read-only PR verification for strict SemVer/prerelease rules, lower-case GHCR names, digest-only Compose rendering, immutable release dependencies, and release-workflow trust ordering.
- `scripts/release/verify-published-release.sh`, which rejects mutable/local-build release Compose files and smoke-tests the exact digest-pinned images pulled from the registry.
- Versioned `release: published` workflow implementing source verification, `linux/amd64` + `linux/arm64` GHCR candidate builds, per-platform Trivy vulnerability gates and SPDX SBOMs, exact-digest Compose smoke verification, GitHub attestations, no-rebuild digest promotion, stable-only `latest`, manifest verification, checksums, and GitHub Release evidence assets.
- Clean-checkout regression coverage that requires both release helper shell scripts to retain executable Git modes.
- Read-only `Container Security CI` for pull requests, `main`, and daily scheduled runs; it builds the exact production API/web Dockerfiles with fresh bases and fails closed on Trivy `HIGH`/`CRITICAL` vulnerabilities before a GitHub Release is created.
- Initial M0B normalized provider-offer trust contract: `ObservedOffer` preserves provider, fulfillment context, SKU, price/currency, explicit availability, observation time, and source reference, with TDD coverage that rejects incomplete or invalid external offer data before comparison logic.
- `docs/integrations/retailer-feasibility.md` with evidence-based provider decision labels, initial Kuper/Yandex Eats/Lenta/VkusVill/Magnit/X5 research, explicit no-scraping boundaries, and deterministic fixture/live-probe acceptance criteria for M0B.
- Shared M0B provider feasibility harness: `ProviderAccessType`, `ProviderCapability`, provider-scoped `LocationContext`, `ProductQuery`, the `RetailerProvider` port, structural `FixtureRetailerProvider`/`LiveRetailerProvider` separation, offline `ProviderFeasibilityHarness`, and explicit `ProviderLiveProbe`.
- Expanded retailer-feasibility research and `docs/superpowers/plans/2026-08-10-m0b-provider-spikes.md`, covering Pyaterochka, Perekrestok, Magnit, Chizhik, Ozon Fresh, Samokat and Kuper with a fixed 20-item corpus and measurable M0 decision criteria.
- Pyaterochka/5ka Phase A plain-HTTP probe using the JDK HTTP client with store-scoped request construction, fixed timeouts, no retries or browser-derived credentials/headers, plus an opt-in `Provider Live Probe` workflow that emits sanitized evidence and can publish that evidence as a dedicated commit status.

### Changed

- Foundation architecture was approved on 2026-08-09.
- Project execution entered M0A Platform Foundation before retailer feasibility work.
- API bootstrap dependencies were reduced after regression evidence exposed unnecessary zero-module Modulith runtime and unused Mockito warning noise.
- API CI was introduced earlier than the original M0A sequencing so backend TDD could be proven on the exact Java 25 toolchain instead of inferred from an incompatible local runtime.
- Spring application-context verification now boots against real PostgreSQL rather than a database-free context.
- Test JVM explicitly enables native access required by Testcontainers/JNA on Java 25 to avoid unsupported-access warning noise.
- TypeScript for the generated API-client toolchain is pinned to compatible 5.9.3 after CI proved that TypeScript 7.0.2 violates `openapi-typescript 7.13.0`'s supported peer range.
- Permanent Contract CI installs exact pnpm 11.4.0 after Node setup instead of using `pnpm/action-setup`, removing the known PNPM_HOME layout warning while keeping the toolchain pin explicit.
- pnpm supply-chain policy explicitly allows only the `sharp` and `unrs-resolver` dependency build scripts required by the generated Next.js toolchain; global build-script bypasses remain disabled.
- The generated nested web pnpm workspace file was removed so the repository has one authoritative workspace root and Next.js no longer reports ambiguous workspace-root detection.
- The default generated Geist webfont was replaced with a reliable Cyrillic-capable system UI font stack until typography is explicitly decided as part of the design system.
- Next.js anonymous telemetry is disabled in CI.
- Spring MVC request-detail logging is explicitly disabled by default.
- Health component/details disclosure is explicitly disabled while Kubernetes-style liveness/readiness probe groups are enabled.
- Public README was reorganized around product value, honest implementation status, CI visibility, quick verification, architecture, security, and a compact documentation map.
- `PROJECT_STATE.md` was converted from a stale task diary into a factual snapshot of merged work, open blockers, verified gates, and next actions.
- Repository branch lifecycle now explicitly keeps only `main` plus active pull-request branches; merged source branches are treated as disposable after squash merge.
- API CI, Contract CI, Web CI, and Web E2E now run predictably on every pull request so future required-check rules cannot deadlock on path-filtered workflows.
- API CI now runs Maven `verify` instead of only `test`, aligning the required backend gate with packaged-build verification.
- Recurring workflows now cancel superseded runs for the same PR/ref and have finite job timeouts.
- Web package agent guidance now points to repository engineering/development rules and current Next.js docs instead of relying on a minimal generated warning.
- Web-specific ignore rules were centralized into the root `.gitignore` so the monorepo has one authoritative ignore policy.
- Repository merge policy is now squash-only; merge commits and rebase merge are disabled, auto-merge/update-branch are enabled, and merged source branches are deleted automatically.
- Historical merged branches were removed so the steady state is `main` plus active pull-request branches only.
- The first dependency-maintenance cycle upgraded `actions/cache` to 6.1.0, `actions/checkout` to 7.0.1, `actions/setup-node` to 7.0.0, and `dependency-review-action` to 5.0.0 using immutable full-SHA pins and the complete required-check gate.
- The web dependency baseline advanced to Next.js 16.3.0, React/React DOM 19.2.8, and `eslint-config-next` 16.3.0 after a refreshed full CI/security run including production Web E2E.
- `main` merge protection is actively enforcing the seven proven required checks; maintenance merges were blocked while checks were stale or missing and admitted only after refresh plus a full pass.
- Incompatible automated major updates are intentionally deferred instead of bypassing quality gates: TypeScript 7.0.2 breaks the current OpenAPI generator, ESLint 10 breaks the current lint configuration, and `@types/node` 26 is deferred while the runtime remains Node 24.
- Next.js production output now uses standalone mode with monorepo tracing configured so the runtime image contains only the server/runtime artifacts needed by the web container.
- The release Compose topology publishes only the web port by default; API and PostgreSQL remain on the internal Compose network, while web health verifies real API reachability through runtime `API_BASE_URL`.
- Container-bundle verification is a distinct gate from source/unit/build verification so a healthy source build cannot mask broken Docker/Compose behavior.
- Versioned release publication now uses candidate image digests as the security boundary: scan, SBOM, exact-bundle smoke verification, and attestation happen before version-tag promotion, and promotion copies the verified image index without rebuild.
- Stable and prerelease semantics are explicit: a GitHub prerelease must use a SemVer prerelease tag and can never update `latest`; stable releases may update `latest` only after the same verification path succeeds.
- Release verification is intentionally split between read-only PR contract tests and the write-capable release-event workflow so package/OIDC permissions are never granted to ordinary pull-request CI.
- The first real prerelease, `v0.1.0-rc.1`, exercised the actual `release: published` trigger and proved release metadata/main-ancestry checks, the full source verification suite, production web build, and 4/4 Playwright tests before stopping at the release-helper mode defect; `Release / Publish` was skipped, so no GHCR publication evidence is attributed to rc.1.
- `v0.1.0-rc.2` proved the corrected `Release / Verify` path end to end, started `Release / Publish`, authenticated to GHCR, and built/pushed both multi-platform staging image indexes before the first Trivy gate intentionally stopped publication on a HIGH API dependency finding.
- The final Next.js production runtime moved from full Node 24 Bookworm-slim to distroless Node 24 Debian 13/non-root, removing shell/package-manager runtime requirements while preserving the Node 24.18.1 build toolchain and verified standalone-server behavior.
- M0B now permits controlled evaluation of `PUBLIC_UNOFFICIAL_API` consumer backends when ordinary requests work without access-control or anti-bot circumvention; third-party wrappers remain research evidence rather than an inherited permission or production dependency.
- Pyaterochka is now an active Phase A spike rather than a paper candidate: the exact public-backend request hypotheses and non-evasive probe are executable, while provider support remains explicitly blocked on the real raw-HTTP result.
- Live-probe evidence publication uses a dedicated legacy commit-status context so connected tooling can read the sanitized result without broadening the workflow to issue/content/package write permissions.

### Fixed

- Corrected the Spring Boot 4 Flyway wiring after the persistence test proved that adding Flyway libraries alone did not activate Boot's separated Flyway auto-configuration module; the project now uses `spring-boot-starter-flyway` plus the PostgreSQL Flyway module.
- Corrected Spring Boot 4 MVC test wiring by adding the focused `spring-boot-starter-webmvc-test` test module after the first controller test compile attempt exposed the split test auto-configuration.
- Corrected the initial web-test dependency updater to operate from the root pnpm workspace after local-directory installation could not resolve the shared workspace protocol package.
- Removed an unused-variable lint warning from the initial component test rather than accepting warning noise.
- Corrected `PROJECT_STATE.md` references that still described already-merged PR #7 and the pre-Task-8 repository state as current.
- Removed the future protected-branch deadlock risk caused by path-filtered required-check candidates.
- Removed stale `create-next-app` README content that advertised unsupported npm/yarn/bun workflows, Geist usage, and Vercel deployment after the project had already chosen different repository conventions.
- Corrected the Dependency Review v5 immutable-pin annotation so the human-readable workflow comment matches the actual pinned action release.
- Closed the obsolete Next.js 16.2.11 dependency PR after the verified 16.3.0 update superseded it.
- Corrected the release PostgreSQL volume from the pre-18 `/var/lib/postgresql/data` mount to the PostgreSQL 18-compatible `/var/lib/postgresql` mount after the first real Compose run exposed the upstream data-layout change.
- Release-bundle failures now print Compose service state and logs before cleanup instead of losing the root-cause evidence during teardown.
- Preserved executable Git modes for `scripts/verify-release-bundle.sh` and `scripts/release/verify-published-release.sh` after `v0.1.0-rc.1` failed with `Permission denied` / exit 126 on a clean GitHub Actions checkout; a TDD regression test now rejects mode `100644` for either release helper.
- Updated pgJDBC from `42.7.11` to `42.7.12` after `v0.1.0-rc.2` and the TDD PR security gate reproduced `CVE-2026-54291` (`HIGH`) in the API production image.

### Security

- Established security-reporting and secret/privacy handling policy.
- Production database credentials are required through external environment configuration and are not stored in source control.
- Contract CI and Web CI use read-only repository permissions and verify the lockfile through pnpm's supply-chain policy check during frozen installation.
- Actuator environment/configuration/metrics endpoints remain unavailable over public HTTP, and request logging defaults are restricted to reduce accidental credential/location leakage.
- Repository governance requires least-privilege Actions, no silent security-check bypasses, Dependency Graph/Dependabot/CodeQL/Dependency Review/secret scanning/push protection/PVR where available, and immutable full-SHA action pins.
- Permanent API/Contract/Web Actions are pinned to full commit SHAs verified from successful repository runs instead of mutable major tags.
- Read-only checkout steps disable persisted Git credentials after source retrieval.
- CodeQL and Dependency Review workflows use full-SHA pins, non-persisted checkout credentials, concurrency control, and finite timeouts.
- Dependency Graph is enabled and the repository SBOM endpoint is available, allowing Dependency Review to run successfully.
- Dependabot alerts and security updates are enabled.
- Secret scanning and secret-scanning push protection are enabled.
- Private vulnerability reporting is enabled for confidential security reports.
- Default GitHub Actions workflow token permissions are read-only and workflows cannot approve pull requests.
- No dependency update was allowed to weaken the pnpm minimum-release-age policy, required status checks, CodeQL, Dependency Review, contract generation, linting, or browser verification in order to become mergeable.
- Application container runtimes use non-root users, Compose requires the database password at runtime instead of committing one, and only the web service is host-published by default.
- `Release Bundle CI`, `Release Contract CI`, and `Container Security CI` remain read-only; package and OIDC write permissions exist only on the downstream release-publish job after read-only release verification succeeds.
- Release Docker/GitHub Actions are pinned to full commit SHAs, and the QEMU binfmt and BuildKit helper images are additionally pinned by digest to avoid mutable helper-image drift.
- Both target image architectures are scanned for `HIGH` and `CRITICAL` vulnerabilities before version promotion, and release consumers are bound to immutable application-image digests through the attached Compose asset.
- `v0.1.0-rc.2` demonstrated the release security boundary fail-closed: publication stopped at the first HIGH finding before final-package copy, attestation, SemVer promotion, evidence upload, or any stable `latest` action; remediation used dependency/runtime hardening rather than scanner suppression.
- Provider feasibility deliberately excludes CAPTCHA/anti-bot bypass, browser-fingerprint evasion and proxy/IP rotation used to circumvent blocking; offline feasibility code accepts fixture-provider types while live-capable adapters require the separate explicit live-probe entry point.
- The Pyaterochka live-probe workflow keeps `contents: read` and adds only `statuses: write` for the dedicated sanitized result context; it has no retailer credentials, a fixed owner/issue command gate, finite timeouts, no retry/evasion behavior, and publishes no retailer response bodies or exact store/product IDs.
