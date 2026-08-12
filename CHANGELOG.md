# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; entries focus on user-visible behavior, architecture, security, retailer evidence and release engineering rather than routine refactors.

## [Unreleased]

### Added

#### Product and shopping core

- Canonical eight-retailer registry with independent technical-connectivity and production-access states.
- Canonical shopping quantities and shopping-list aggregate with stable UUID identity and deterministic mutation semantics.
- Provider/path orchestration preserving retailer, source-provider, acquisition-mode and fulfillment provenance.
- Provider-neutral `ProductLocation`, sensitive-address redaction and typed provider-scoped fulfillment bindings.
- Immutable offer snapshots with explicit observation/provider-freshness evidence and first-class `UNKNOWN` availability.
- Deterministic exact-before-normalized product matching with explicit matched/ambiguous/unmatched states and no fuzzy/AI baseline.
- Whole-package single-store basket calculation with explicit package evidence, deterministic decimal arithmetic and `COMPLETE / UNCERTAIN / INCOMPLETE` aggregate states.
- Product-facing retailer comparison/readiness model that always preserves all canonical retailers and keeps provider/acquisition identifiers internal.
- Stateless `POST /api/v1/comparison-previews` manual-list comparison API, synchronized OpenAPI/generated TypeScript client and responsive web journey.
- Desktop/mobile Playwright critical-journey coverage for ready, uncertain, incomplete, unavailable, unmatched, ambiguous, package-unknown and unit-mismatch paths.

#### Structured package evidence

- Optional canonical package quantity on `ObservedOffer`, preserved through immutable `OfferSnapshot` and projected into `PackageQuantitySet` from snapshot evidence.
- Runtime invariant rejecting a parallel package-evidence set when it disagrees with snapshots.
- Regression coverage proving presentation names such as `970мл` or `1,5л` do not create package evidence.
- Pure Magnit exact-characteristic semantics for `Вес, кг` / `Объем, л`, including explicit `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES` and `INVALID_VALUE` states.
- Magnit 20-product × 2-shop fixed-corpus package instrumentation that separates transport/identity failure from missing metadata.
- SKU-bound Magnit JSON-LD package extraction from the same PUBLIC_WEB response using exact `Product.sku`, proven scalar `weight` and exact `additionalProperty[name="Объем, л"]` semantics.
- Finite JSON-LD corpus evidence: 40/40 HTTP 2xx and usable observations, 20/20 stable identity, 36/40 `FOUND`, 0 `MISSING`, 4 explicit multi-dimensional ambiguities, 0 conflicts and 0 invalid values.
- Diagnostic evidence identifying the four ambiguity observations as milk SKU `1000013732` and kefir SKU `1000330180` in both shop contexts; structured egg mass remains mass rather than count.

#### Magnit location/store context

- Deterministic public Magnit geographic primitives and exact `box` request contract for `POST /webgate/v1/stores-facade/search`.
- Sanitized response parser accepting only `items.items[].externalId.storeCode + coordinates` and rejecting conflicting store identity evidence.
- Fail-closed store resolution semantics: zero → `NO_STORES`, exactly one → `RESOLVED`, many → `AMBIGUOUS`, conflicting duplicate identity → `CONFLICTING_STORE_EVIDENCE`.
- Provider-scoped Magnit fulfillment bindings reusing `sourceProviderId="magnit-public-page"`; `shopCode` remains internal `LocationContext.fulfillmentContextId`.
- Explicit manual store selection using the same provider identity without introducing a first/nearest-store heuristic.
- Test-only merged-main live gate for issue #69, runnable only by repository owner through exact issue command `/provider-probe magnit-shopcode` and never on a schedule.
- Direct-stateless live client contract with no cookie jar, no authenticator, `Redirect.NEVER`, no Magnit application/auth headers and exactly two requests.
- Merged-main LOCATION_RESOLUTION proof on SHA `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1`, workflow run `31642543544`:
  - both responses HTTP 200;
  - one candidate each;
  - public `shopCode=992301` present both times;
  - no response `Set-Cookie` in either attempt;
  - identical candidate-code sets;
  - no conflicting evidence;
  - exactly two requests;
  - focused tests 3/3 PASS and Maven `BUILD SUCCESS`.

#### Retailer connectivity and engineering

- Universal Retailer Connectivity design and evidence-driven acquisition-mode fallback policy.
- Chromium MV3 retailer bridge with minimal permissions, sanitized local storage, deterministic fixtures and persistent-Chromium E2E.
- Accepted first-party browser paths for Perekrestok v2 and Pyaterochka v1.
- Magnit public-page Phase A/B evidence establishing `AVAILABLE_PUBLIC_WEB` technical feasibility.
- M0 → M1 GO decision and explicit production-access/right-to-operate follow-up #70.
- Architecture guards protecting basket/comparison/preview dependency direction and preventing production code from depending on fixtures/test support.

### Changed

- Project phase advanced from M0 Product & Integration Discovery to **M1 Shopping Core** after satisfying technical exit criteria.
- Retailer onboarding remains transport-neutral and universal; a failed direct path changes acquisition mode rather than retailer scope.
- `ObservedOffer` is the provider trust boundary and `OfferSnapshot` the immutable comparison record.
- Observation time and provider-side update time remain distinct.
- Matching never breaks semantic ambiguity using price, availability, freshness, acquisition mode or SKU ordering.
- Package quantity is modeled only as explicit structured evidence; product names, URLs, slugs, category and other presentation text are non-authoritative.
- Existing integrations remain package-unknown unless they provide a proven structured quantity.
- Magnit visible-text corpus result `0 FOUND / 40 MISSING` is treated as a rendering blind spot because the same raw responses contain SKU-bound JSON-LD package metadata.
- Magnit corpus projection now consumes the JSON-LD extractor without changing price/promo/availability request or identity semantics.
- Package arithmetic requires canonical unit equality; mass/volume evidence cannot satisfy a `PIECE` requirement.
- `UNKNOWN` availability propagates into an uncertain basket instead of confirmed availability.
- Incomplete baskets expose no aggregate total and cannot masquerade as complete winners.
- The home page now centers the stateless comparison journey rather than readiness-only status.
- Production comparison evidence remains deliberately no-op/fail-closed; deterministic extraction and finite live research do not activate recurring retailer polling.
- Magnit technical location resolution for the proven bbox/store-selection boundary is now **accepted (#69)** after deterministic #86, test/workflow #87 and merged-main live reproduction.
- Automatic arbitrary text/address → coordinates remains intentionally unimplemented because no acceptable public contract was proven.
- Active Magnit M1 focus moves to **#70 production usage/right-to-operate**. Technical feasibility and a public endpoint do not silently authorize recurring acquisition.

### Fixed

- Provider offer validation rejects provenance/context mismatches before comparison logic.
- Precise addresses are excluded from default string representations and provider routing.
- Snapshot freshness rejects provider timestamps after observation time.
- Semantic matching rejects cross-retailer/context candidate mixing and impossible result combinations.
- Package bindings reject duplicate/invalid evidence and incompatible canonical units.
- Mixed currencies fail closed.
- Incomplete comparison states cannot expose misleading totals or freshness summaries.
- Public comparison/readiness objects reject impossible coverage/access/status combinations.
- Comparison preview rejects unknown JSON request fields instead of silently ignoring client-controlled data.
- Browser E2E item-gap assertions are retailer-scoped to avoid copy collisions.
- Magnit package extraction ignores foreign SKU nodes and unproven fields; conflicting/multi-dimensional values never become guessed quantities.
- Magnit corpus metrics exclude non-2xx and wrong-identity pages rather than counting them as missing metadata.
- Magnit store-search request constructors enforce the proven bbox/store-type invariants even when nested records are instantiated directly.
- Magnit store response parsing deduplicates equivalent candidates and exposes conflicting identity evidence instead of choosing an arbitrary record.
- Issue #69 was reopened after GitHub auto-closed it on #86 merge; it remained open until its own merged-main live acceptance contract was satisfied.

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
