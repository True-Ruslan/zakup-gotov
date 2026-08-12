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
- Product-facing retailer comparison/readiness model that always preserves the eight canonical retailer entries and separates technical coverage, production access, runtime comparison state and product-safe failure reasons.
- Conservative comparison freshness summary using the oldest selected observation and provider timestamps only when every selected line has trusted provider-side timestamp evidence.
- `GET /api/v1/retailers` REST/OpenAPI contract plus synchronized generated TypeScript client path/types.
- M1 web retailer-status surface with product-safe Russian labels, responsive semantic retailer cards and explicit service-unavailable behavior when the API cannot be reached.
- Comparison architecture rule preventing retailer/provider/shopping/matching/basket/location production packages from depending back on the product read model.
- Explicit web freshness evidence copy distinguishing observation-only evidence from a trusted provider-side update timestamp without inventing a stale/fresh verdict.
- Stateless `POST /api/v1/comparison-previews` product boundary for locality-only manual shopping-list comparison.
- Product-safe comparison preview response containing every canonical retailer and item-level resolution details without SKU, source-provider, acquisition-mode, source-reference or fulfillment-context identifiers.
- Strict production `NoopComparisonRuntimeEvidenceSource` so the new preview journey fails closed and never falls back to fixture prices or hidden live retailer calls.
- Deterministic test-only runtime evidence and browser mock API covering `READY`, `UNCERTAIN`, `INCOMPLETE`, `UNAVAILABLE`, unmatched, ambiguous, package-unknown and unit-mismatch cases.
- Responsive M1 comparison form/results UI with repeatable shopping rows, typed quantities, accessible error states and a bounded server-side comparison timeout.
- Desktop/mobile Playwright critical-journey coverage that submits a real product request shape, keeps all eight retailer results visible and verifies that internal provider identifiers do not leak.
- Preview architecture guard preventing upstream shopping/location/provider/matching/basket/comparison/retailer production packages from depending back on `preview`, and preventing production preview code from depending on fixture/test-support namespaces.
- Optional source-validated canonical package quantity on `ObservedOffer`, preserved unchanged through immutable `OfferSnapshot` creation.
- Snapshot-driven `PackageQuantitySet.fromSnapshots(...)` projection that creates basket bindings only for explicit structured package evidence.
- Runtime package-evidence derivation from snapshots so comparison fixtures and future provider paths carry one provenance-preserving source of package truth.
- Regression coverage proving presentation names such as `Молоко 3,2%, 970мл` and `Вода 1,5л` do not create package evidence.
- Pure Magnit source-specific `MagnitPackageQuantityExtractor` for exact `Характеристики` fields `Вес, кг` and `Объем, л`, with canonical kg→g and l→ml conversion.
- Explicit Magnit package-extraction states `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES` and `INVALID_VALUE` so source ambiguity never becomes a guessed basket quantity.
- Magnit provider/snapshot regression proving a `FOUND` characteristic can populate #81 structured package evidence while multi-dimensional characteristics remain package-unknown.
- Evidence note documenting official Magnit weight-only, volume-only, multi-dimensional and count-selector examples plus unchanged #69/#70 production gates.

### Changed

- Project phase advanced from M0 Product & Integration Discovery to **M1 Shopping Core** after satisfying technical exit criteria.
- Retailer onboarding remains transport-neutral and universal; failed direct access changes the investigated acquisition mode rather than retailer scope.
- Kuper remains provider/aggregator provenance rather than retailer identity.
- Shopping text remains user wording; semantic normalization is isolated in matching.
- `ObservedOffer` remains the provider trust boundary; `OfferSnapshot` remains the immutable comparison record.
- Observation time and provider-side update time remain distinct.
- Matching never breaks semantic ambiguity using price, availability, freshness, acquisition mode or SKU ordering.
- Package size/quantity is modeled as explicit structured evidence and is **not** inferred from `productName`, title, URL or other presentation text, nor assumed to be one unit per SKU.
- Existing provider integrations remain source-compatible and package-unknown unless they explicitly supply a proven structured package quantity.
- Runtime comparison package bindings now derive from immutable snapshot evidence instead of being independently injected downstream.
- Deterministic comparison fixtures attach package quantity at the provider observation boundary before snapshotting, mirroring the production evidence flow without live retailer access.
- Magnit v1 structured package semantics accept only exact weight/volume characteristics; simultaneous dimensions or conflicting/invalid values remain unknown rather than using category/title heuristics.
- Magnit `Количество в упаковке` remains deferred pending separate source and multi-dimensional domain evidence.
- `UNKNOWN` availability propagates into `AVAILABILITY_UNKNOWN` line state and an `UNCERTAIN` basket rather than a confirmed complete basket.
- Incomplete single-store baskets expose no aggregate total, preventing partial-price comparisons from masquerading as complete basket prices.
- Technical retailer connectivity no longer leaks directly into the product/API contract: public readiness uses stable coverage/access/comparison states and finite product-safe reason codes instead of provider IDs, acquisition modes or source references.
- The home page now makes the stateless comparison preview the primary M1 action instead of the previous readiness-only surface.
- The critical browser journey is driven through the generated OpenAPI client and the same product-safe comparison vocabulary as the core/read model.
- Production comparison evidence remains deliberately no-op/fail-closed; deterministic source extraction does not activate recurring retailer polling.
- Active M1 focus moves to Magnit corpus package-evidence measurement plus retailer connectivity/lifecycle and production-access constraints after #82 ships.

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
- Product comparison evidence rejects cross-retailer or structurally impossible provider/basket combinations.
- Public comparison views reject impossible status/coverage/access/total/freshness combinations instead of relying only on assembler correctness.
- Comparison reason codes are constrained to the semantics of their status and coverage/access gate: uncertain availability, item-level incomplete causes, and one matching unavailable cause.
- `RetailerFreshness` rejects basis/provider-timestamp contradictions and provider update timestamps after the observation time.
- Incomplete/unavailable product comparison states cannot expose a misleading aggregate total or freshness summary.
- Responsive Playwright coverage distinguishes product service-unavailable alerts from framework route announcements while preserving accessible alert semantics.
- Critical-journey Playwright item-gap assertions are scoped to the relevant retailer card with exact text matching, avoiding collisions between item labels and explanatory retailer reason copy.
- Runtime evidence rejects an explicit package-quantity set when it differs from the structured quantities preserved by its snapshots, preventing two package-evidence sources from silently diverging.
- Magnit package extraction ignores title/slug/description/script/style numbers and fails closed on weight+volume or conflicting supported characteristics.

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
