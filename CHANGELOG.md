# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; entries focus on user-visible behavior, architecture, security, integration evidence and release engineering rather than routine refactors.

## [Unreleased]

### Added

- Universal Retailer Connectivity design and evidence-driven acquisition-mode fallback policy.
- Chromium MV3 retailer bridge with minimal permissions, sanitized local storage, deterministic fixtures and persistent-Chromium E2E.
- Accepted first-party browser paths for Perekrestok v2 and Pyaterochka v1.
- Magnit public-page Phase A/B probes and evidence establishing `AVAILABLE_PUBLIC_WEB` technical feasibility for explicit `shopCode` contexts.
- M0 → M1 GO decision plus explicit Magnit location-resolution (#69) and production-usage-rights (#70) follow-ups.
- M1 canonical retailer registry with separate technical coverage and production-access status.
- M1 canonical quantities and shopping-list aggregate with stable UUID identity/order and explicit mutation semantics.
- Provenance-complete `ObservedOffer`, deterministic provider-path orchestration and explicit expected path-failure handling.
- Provider-neutral product location, redacted sensitive addresses and typed fulfillment-context bindings.
- Immutable offer snapshots and freshness evidence separating observation time from optional provider-side update time.
- Required observed `productName` evidence preserved through snapshots.
- Deterministic exact-before-normalized product matching with explicit matched/ambiguous/unmatched states, retailer/context scoping and no fuzzy/AI baseline.
- Explicit basket-layer package-quantity evidence keyed by `OfferSnapshotId`; absent evidence remains unknown instead of being guessed.
- Whole-package selection using canonical quantities and exact decimal arithmetic, including ceiling package count, provided quantity and line total.
- Single-store basket quote model with explicit per-item outcomes and `COMPLETE`, `UNCERTAIN`, `INCOMPLETE` aggregate states.
- Basket architecture rule preventing production provider/shopping/matching/retailer modules from depending back on basket.
- Durable basket design/plan in `docs/superpowers/specs/2026-08-12-m1-single-store-basket-design.md` and `docs/superpowers/plans/2026-08-12-m1-single-store-basket.md`.

### Changed

- Project phase advanced from M0 Product & Integration Discovery to **M1 Shopping Core** after satisfying technical exit criteria.
- Retailer onboarding remains transport-neutral and universal; failed direct access changes the investigated acquisition mode rather than retailer scope.
- Kuper remains provider/aggregator provenance rather than retailer identity.
- Shopping text remains user wording; semantic normalization is isolated in matching.
- `ObservedOffer` remains the provider trust boundary; `OfferSnapshot` remains the immutable comparison record.
- Observation time and provider-side update time remain distinct.
- Matching never breaks semantic ambiguity using price, availability, freshness, acquisition mode or SKU ordering.
- Package size/quantity is now modeled as explicit basket evidence and is **not** inferred from `productName` or assumed to be one unit per SKU.
- `UNKNOWN` availability now propagates into `AVAILABILITY_UNKNOWN` line state and an `UNCERTAIN` basket rather than a confirmed complete basket.
- Incomplete single-store baskets expose no aggregate total, preventing partial-price comparisons from masquerading as complete basket prices.
- Active M1 focus moves from basket-core construction to failure/coverage/freshness product/API/UX semantics before critical browser E2E.

### Fixed

- Provider offer validation rejects provenance/context mismatches before comparison logic.
- Precise addresses are excluded from default string representations and provider routing.
- Snapshot freshness rejects provider timestamps after observation time.
- Semantic matching rejects cross-retailer/context candidate mixing.
- Impossible matching result combinations fail closed at construction.
- Package-quantity bindings validate null elements before immutable-copy construction, preserving deterministic diagnostic failures.
- Duplicate package evidence for one snapshot fails closed.
- Package selection rejects incompatible canonical units.
- Mixed currencies across selected basket lines fail closed.
- Incomplete basket states cannot carry a basket total by construction.

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
