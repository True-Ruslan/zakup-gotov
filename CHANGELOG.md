# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; this file summarizes user-visible behavior, architecture, security, retailer evidence and release engineering. Detailed RED/GREEN/review evidence belongs in the linked acceptance/spec/plan documents rather than being duplicated here.

## [Unreleased]

### Added

#### Productization

- M5.1 adds one versioned same-origin browser-local WeeklyPlan/Pantry semantic input draft under `zakup-gotov.weekly-plan-draft.v1` for repeat use without accounts or server persistence.
- Draft persistence excludes presentation keys, generated identities, comparison/economics/optimizer results and provider/acquisition/fulfillment evidence.
- Restore happens only after client mount and never auto-submits comparison; autosave is restore-gated, debounced and semantic-no-op aware.
- Local-storage failures fail closed without breaking explicit editing/submission; explicit clear removes the draft without issuing a comparison request.

#### Recipes

- First-class immutable Recipe domain, deterministic serving scaling and Recipe → ShoppingList conversion using accepted Shopping quantity semantics.
- Stateless Recipe shopping/comparison preview boundaries with synchronized OpenAPI/generated TypeScript contracts.
- Responsive Recipe-first web journey with deterministic desktop/mobile/fail-closed browser coverage.
- Deterministic occurrence-aware multi-Recipe aggregation; automatic merge remains exact normalized requirement + canonical unit only.

#### Weekly planning and Pantry

- Ordered WeeklyPlan meal occurrences with day metadata, target servings, repeated Recipe support and deterministic weekly ShoppingList composition.
- Stateless WeeklyPlan shopping/comparison preview boundaries and responsive WeeklyPlan-first browser journey.
- Request-scoped Pantry subtraction with explicit `UNCHANGED / PARTIALLY_COVERED / FULLY_COVERED` audit evidence.
- Pantry-aware weekly shopping/comparison composition, including truthful `NO_REMAINING_DEMAND` without fabricated retailer traffic.
- Responsive Pantry controls that render server-owned original demand, adjustment evidence and remaining demand without browser-side subtraction.

#### Basket economics and optimization

- Explicit known/unknown delivery and service fees and minimum-order evidence; known zero is distinct from unknown.
- Separate merchandise subtotal, checkout-total knowledge, eligibility and comparability semantics.
- Deterministic cheapest comparable one-retailer basket selection with explicit exact ties and no hidden retailer/freshness tie-break.
- Server-owned WeeklyPlan/Pantry Optimization Preview API and responsive Optimization UX.
- Production checkout economics remains fail-closed/unknown until retailer-specific evidence is accepted.

#### Retailer connectivity

- Universal Retailer Connectivity remains a permanent product invariant: every canonical retailer stays coverage work until at least one reproducible accepted acquisition path exists.
- Chromium MV3 Retailer Bridge supports minimal-permission, sanitized first-party acquisition with deterministic fixtures and real persistent-Chromium E2E.
- Perekrestok and Pyaterochka have accepted first-party browser paths.
- Retailer Bridge long-lived-session hardening (#153) refreshes truthfully across SPA navigation and store changes using event-driven lifecycle handling, fresh-context gating, bounded retained resource evidence and revision-safe writes without widening production extension permissions.
- Magnit public-web feasibility, structured package evidence and store-resolution evidence are established; recurring production acquisition remains policy-blocked pending affirmative permission or a supported/licensed path.

#### Chizhik connectivity

- Phase A adds bounded opt-in plain HTTPS feasibility probes with finite timeouts and sanitized evidence.
- Phase B adds exact-origin observation-only browser discovery and trusted `main` bridge canary artifact publication.
- Phase C adds bounded body-less public catalog-document reachability probing with controlled live execution and finite sanitized transport-failure classification; document reachability is not product/price connectivity.
- Phase D1 (#167/#168) adds active ordinary-browser access to the fixed `/api/v1/shops/` store directory with strict store-shape/coordinate validation and real extension Chromium E2E.
- D1 transport decision is **COMPLETE / ACCEPTED**: ordinary user-browser store-directory evidence succeeds while stock GitHub-hosted Chromium is `page-unavailable`; the selected architecture is therefore normal user-browser MV3 Retailer Bridge, not managed CI/server browser worker.
- D2 transport foundation (#169/#171) adds exact bounded store-scoped delivery search; successful JSON deliberately remains opaque and automatic search/offer production stays disabled pending live schema acceptance.
- A privacy-safe ordinary-user-browser D2 schema canary retains structural evidence only and never raw response/product/store values.
- D2 store-context binding (#173/#174) accepts only exact first-party delivery resource evidence already observed by the official browser session, intersects path-embedded `sap_id` with the validated store directory and requires exactly one distinct validated context.
- Missing context, foreign origin, unknown store and conflicting validated stores fail closed; `searchStore` remains uncalled before #169 schema acceptance.
- Chizhik offer mapping is blocked specifically on ordinary-user-browser evidence for product container/identifier/name, price field **and monetary unit/scale**, plus explicit availability semantics if present. Unknown availability remains `UNKNOWN`; promotion/loyalty/package/discount semantics are not inferred.
- Chizhik safety boundary remains unchanged: no stealth/fingerprint spoofing, proxy rotation, credential/header/cookie extraction, private/mobile-client impersonation or arbitrary URL forwarding.

#### Product and Shopping Core

- Canonical retailer registry with independent technical-connectivity and production-access states.
- Canonical shopping quantities, stable Shopping identities, provider/location provenance and immutable offer/freshness evidence.
- Deterministic exact-before-normalized matching with explicit matched/ambiguous/unmatched states and no fuzzy/AI baseline.
- Whole-package single-store basket calculation with explicit package evidence, exact decimal arithmetic and truthful complete/uncertain/incomplete states.
- Stateless comparison preview API plus responsive manual-list web journey.
- Pre-acquisition production-access gate prevents non-approved retailers from being queried in production and rejects out-of-scope evidence.

### Changed

- Project phase advanced through M1 Shopping Core, M2 Recipes, M3 Weekly Planning / Pantry and M4 Basket Optimization; the current deterministic product phase is **M5 Productization**.
- M5.1 Private local WeeklyPlan draft is **COMPLETE / ACCEPTED**; M5.2 remains intentionally unselected until real release-candidate/manual-use evidence identifies the next constraint.
- Chizhik D1 no longer has an unresolved server-vs-browser transport decision: the accepted path is user-browser MV3 Retailer Bridge.
- Chizhik D2 now has a merged fixed search transport and an accepted browser-evidenced store-context rule, but it is **not** yet an offer provider; #169 remains open for live schema/price-unit evidence and mapping acceptance.
- Release history correction: `v0.1.0-rc.3` already exists and its immutable tag resolves to `d988b8c596a737326aeac67f74b6f65a6aaed3bf`; it must not be repointed to newer source.
- The next operational release gate is **`v0.1.0-rc.4`**, tracked by #152. Its final exact target is selected only after this correction is merged and verified.
- Stable `v0.1.0` remains blocked until a prerelease completes the immutable release workflow and manual product canary satisfactorily.
- Technical retailer accessibility and production/right-to-operate readiness remain independent facts.

### Fixed

- Corrected canonical release planning that had incorrectly attempted to reuse the already-published `v0.1.0-rc.3` tag for newer source.
- Clean-checkout local web development builds the generated `@zakup-gotov/api-client` before `next dev`, preventing module-resolution failure found during pre-release manual testing (#150).
- Manual-list SSR/hydration uses deterministic presentation identity and creates request UUIDs only on explicit submit (#150).
- Local draft persistence prevents failed reads from causing blind blank autosave, gates clear until restore readiness and surfaces cleanup/storage failure instead of claiming success.
- Recipe/WeeklyPlan/Pantry composition fails closed on identity/order/quantity/provenance drift rather than silently repairing evidence.
- Incomplete baskets cannot expose misleading complete totals; mixed currencies and contradictory public basket/economics states fail closed.
- Browser-bridge lifecycle rejects stale pre-navigation resources and obsolete in-flight collection results across SPA/store changes.
- Chizhik store context can no longer be guessed from the first active store: only exactly one browser-evidenced store validated against the current directory is accepted.

### Security

- Precise addresses, credentials, provider tokens, private headers and raw sensitive provider payloads remain excluded from ordinary evidence/logging.
- Ordinary CI remains deterministic and does not perform live retailer acquisition; controlled live workflows are explicit and sanitized.
- Chizhik connectivity adds no anti-bot bypass, stealth, proxy rotation, credential extraction or private-client impersonation.
- Release vulnerability policy remains fail-closed at `HIGH,CRITICAL`; no ignore/suppression behavior is added to make a release pass.
- Published prerelease tags are treated as immutable release evidence and are never repointed to later source.

## [0.1.0-rc.3] — existing historical prerelease

- GitHub prerelease/tag already exists at immutable source `d988b8c596a737326aeac67f74b6f65a6aaed3bf`.
- Later accepted connectivity and documentation work advanced `main`; those changes belong to the next prerelease rather than a rewritten rc.3.

## [0.1.0-rc.2] — 2026-08-09

### Added

- Real `release: published` verification and publication path for multi-platform GHCR images.
- Candidate-digest security boundary with Trivy scans, SPDX SBOMs, attestations and digest-pinned Compose smoke verification.

### Security

- Release publication correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291` after staging image publication.
- Subsequent mainline work upgraded pgJDBC, hardened the web runtime to distroless Node 24 Debian 13/non-root and added pull-request/main container security scanning.

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
- Product UI deliberately preserves unknown/unavailable retailer states instead of inventing comparison evidence.
