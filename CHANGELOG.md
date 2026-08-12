# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; entries focus on user-visible behavior, architecture, security, integration evidence and release engineering rather than routine refactors.

## [Unreleased]

### Added

- M0 provider feasibility harness with provider-scoped `LocationContext`, normalized `ObservedOffer`, fixture/live separation and explicit provenance validation.
- Universal Retailer Connectivity design: every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists.
- Chromium Manifest V3 retailer bridge with minimal permissions, sanitized local storage, deterministic fixtures and persistent-Chromium E2E coverage.
- Perekrestok browser adapter v2 and real first-party browser evidence establishing `AVAILABLE_BROWSER_BRIDGE` for reload-based page snapshots.
- Pyaterochka browser adapter v1 and real first-party browser evidence establishing `AVAILABLE_BROWSER_BRIDGE` for reload-based page snapshots.
- First-class `apps/retailer-bridge` pnpm workspace package with pinned bridge-owned TypeScript/Vitest/jsdom/Playwright tooling.
- Magnit ordinary-public-page Phase A and Phase B probes using plain JDK `HttpClient`, explicit public `shopCode` contexts, fixed timeouts and no login, private credentials, browser automation, CAPTCHA handling, proxy rotation or anti-bot bypass.
- Magnit fixed 20-item corpus, sanitized deterministic fixtures, fail-closed SKU/current-price/availability semantics and price-bound promo evidence.
- Final merged-main Magnit Phase B live proof: 20/20 HTTP and usable observations in both explicit store contexts, stable identity 20/20, zero failed requirements and preserved `UNKNOWN` availability where stock semantics are absent.
- `AVAILABLE_PUBLIC_WEB` technical feasibility decision for Magnit explicit store contexts.
- M0 → M1 GO decision recording completion of technical discovery and approval to begin M1 Shopping Core.
- Explicit Magnit follow-up issues for safe location → `shopCode` resolution (#69) and production catalog usage-rights verification (#70).
- M1 canonical retailer registry covering Pyaterochka, Perekrestok, Chizhik, Magnit, Lenta, VkusVill, Ozon Fresh and Samokat with explicit technical coverage and independent production-access status.

### Changed

- Project phase advanced from **M0 — Product & Integration Discovery** to **M1 — Shopping Core** after satisfying the technical exit criteria.
- Both mandatory X5 banners now have accepted browser-assisted acquisition paths.
- Magnit supplies the accepted independent non-X5 path and proves public web as a second acquisition mode alongside the browser bridge.
- M1 entry rules are fixture-first, coverage-explicit, provenance-aware, fulfillment-context-aware and fail-closed for freshness, availability and unresolved production usage rights.
- Retailer onboarding remains transport-neutral; direct API failure changes the acquisition mode under investigation rather than removing the retailer from scope.
- Technical retailer connectivity and production-access readiness are modeled as separate decisions so an accepted feasibility path cannot silently enable production acquisition.
- Kuper remains acquisition-provider/aggregator provenance rather than a retailer/banner identity in the canonical retailer registry.
- `docs/README.md`, `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, the feasibility matrix and Magnit evidence documents were synchronized around the M0 completion decision.

### Fixed

- Magnit Phase A price parsing now prefers SKU-local evidence instead of allowing a geometrically closer unrelated footer price to win.
- Magnit Phase B current-price parsing can fall back to already-proven SKU-bound embedded public-page state when rendered product scope contains no price.
- The stale Magnit `eggs` corpus candidate was replaced with the current public product candidate after diagnostics isolated it as the sole failed requirement.
- Magnit promo status is now independent from regular-price presence: a proven price-bound promo marker may set `promo=true`, while regular/old price remains empty unless a second supported price is actually present.
- Documentation index links were corrected to reference only documents that exist in the repository.

## [0.1.0-rc.2] — 2026-08-09

### Added

- Real `release: published` verification and publication path for multi-platform GHCR images.
- Candidate-digest security boundary with Trivy scans, SPDX SBOMs, attestations and digest-pinned Compose smoke verification.

### Security

- Release publication correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291` after staging image publication.
- Subsequent mainline work upgraded pgJDBC to `42.7.12`, hardened the web runtime to distroless Node 24 Debian 13/non-root and added pull-request/main container security scanning.

## [0.1.0-rc.1] — 2026-08-09

### Added

- First real GitHub prerelease event proving release metadata/main-ancestry validation, source verification and production browser testing.

### Fixed

- A release-helper executable-mode defect found by the real rc.1 event was corrected before rc.2.

## Pre-release foundation — 2026-08-09 to 2026-08-11

### Added

- Java 25 / Spring Boot 4.1 API foundation with Virtual Threads, Spring Modulith verification, PostgreSQL 18, Flyway, jOOQ and Testcontainers.
- Contract-first OpenAPI 3.1 API plus generated TypeScript client.
- Next.js 16 / React 19 responsive web foundation with Vitest and Playwright.
- Reproducible repository verification, Docker/Compose release topology, CodeQL, Dependency Review, Container Security CI and release-contract checks.
- Public operational surface limited to health/liveness/readiness/info.
- Evidence-driven retailer feasibility research for X5, Magnit, Chizhik, Ozon Fresh, Samokat, Kuper, Lenta and VkusVill.

### Changed

- Repository governance became squash-only with required checks, branch cleanup and immutable Action pins.
- Public web surface deliberately remained honest about unavailable retailer comparison functionality while M0 feasibility work was incomplete.
