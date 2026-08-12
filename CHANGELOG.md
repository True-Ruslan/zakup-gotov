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
- Magnit ordinary-public-page Phase A/Phase B probes, deterministic corpus/fixtures and final merged-main evidence establishing `AVAILABLE_PUBLIC_WEB` technical feasibility for explicit `shopCode` contexts.
- M0 → M1 GO decision plus explicit Magnit location-resolution (#69) and production-usage-rights (#70) follow-ups.
- M1 canonical retailer registry with independent technical coverage and production-access status.
- M1 canonical quantity primitives and shopping-list aggregate with stable UUID identity/order and explicit mutation semantics.
- Provenance-complete `ObservedOffer`, `AcquisitionMode`, deterministic provider-path orchestration and explicit expected path-failure handling.
- Provider-neutral product location, redacted sensitive addresses and typed fulfillment-context bindings.
- `FreshnessEvidence` / `FreshnessBasis` and immutable `OfferSnapshotId` / `OfferSnapshot` comparison records derived only from validated observations.
- Required observed `productName` evidence in Java `ObservedOffer`, preserved through `OfferSnapshot` for semantic matching without synthesizing labels from provider queries or SKUs.
- Matching-only deterministic text normalization using Unicode NFKC, `Locale.ROOT` case folding, `ё → е`, and punctuation/symbol separator normalization.
- Scoped deterministic product matching with `MatchScope`, explicit `MATCHED` / `AMBIGUOUS` / `UNMATCHED` states, `EXACT` / `NORMALIZED` / `NONE` strengths and concrete match reasons.
- Architecture verification preventing production provider/shopping/retailer packages from depending back on matching.
- Matching design and implementation evidence in `docs/superpowers/specs/2026-08-12-m1-deterministic-matching-design.md` and `docs/superpowers/plans/2026-08-12-m1-deterministic-matching.md`.

### Changed

- Project phase advanced from **M0 — Product & Integration Discovery** to **M1 — Shopping Core** after satisfying the technical exit criteria.
- Both mandatory X5 banners have accepted browser-assisted acquisition paths; Magnit provides the independent non-X5 public-web feasibility path.
- M1 entry rules are fixture-first, coverage-explicit, provenance-aware, fulfillment-context-aware and fail-closed for freshness, availability and unresolved production usage rights.
- Retailer onboarding remains transport-neutral; direct API failure changes the acquisition mode under investigation rather than removing the retailer from scope.
- Technical retailer connectivity and production-access readiness remain separate decisions.
- Kuper remains acquisition-provider/aggregator provenance rather than a retailer/banner identity.
- Shopping requirement text remains user wording; matching-specific Unicode/case/punctuation normalization is isolated in the matching layer.
- Provider routing uses typed fulfillment contexts and never receives precise product-location addresses.
- `ObservedOffer` remains the provider trust-boundary record; `OfferSnapshot` owns immutable comparison snapshot semantics.
- Observation time and optional provider-side update time remain distinct and cannot be silently conflated.
- Product semantic matching is conservative: exact text outranks normalized text; multiple equivalent candidates remain ambiguous instead of being broken by price, availability, freshness, acquisition mode or SKU ordering.
- Baseline matching deliberately excludes aliases/synonyms, stemming, token reordering, substring/fuzzy/edit-distance logic, transliteration, embeddings and LLM ranking.
- `docs/PROJECT_STATE.md` and `docs/ROADMAP.md` now mark deterministic matching complete and move the active M1 focus to complete single-store basket comparison/package quantity selection.

### Fixed

- Magnit Phase A/Phase B parsing preserves SKU-local/current-price evidence and fail-closed promo/availability semantics.
- Provider offer validation rejects retailer, source-provider, acquisition-mode and fulfillment-context mismatches before comparison logic.
- Precise user addresses are excluded from default string representations.
- Snapshot freshness rejects provider timestamps after observation time.
- Semantic matching rejects candidates from another retailer or fulfillment context instead of silently filtering/mixing them.
- Impossible match result combinations (for example `MATCHED` without exactly one candidate or `UNMATCHED` with non-`NONE` strength) fail closed at construction.

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
