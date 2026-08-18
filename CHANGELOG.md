# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; this file summarizes user-visible behavior, architecture, security, retailer evidence and release engineering. Detailed RED/GREEN/review evidence belongs in linked acceptance/spec/release documents.

## [Unreleased]

### Added

#### Productization

- M5.1 adds one versioned same-origin browser-local WeeklyPlan/Pantry semantic input draft for repeat use without accounts or server persistence.
- Draft persistence excludes generated identities, comparison/economics/optimizer results and provider/acquisition/fulfillment evidence.
- Restore never auto-submits; storage failures fail closed without breaking explicit editing/submission.

#### Recipes / Weekly planning / Pantry

- First-class immutable Recipe domain with deterministic serving scaling and Recipe → ShoppingList conversion.
- Stateless Recipe and WeeklyPlan shopping/comparison boundaries with responsive browser flows.
- Ordered WeeklyPlan meal occurrences and request-scoped Pantry subtraction with explicit adjustment evidence.
- Automatic merging remains exact normalized requirement + canonical unit only; fuzzy/synonym/AI equivalence is never implicit.

#### Basket economics and optimization

- Explicit known/unknown delivery/service fees and minimum-order evidence.
- Separate merchandise subtotal, checkout-total knowledge, eligibility and comparability semantics.
- Deterministic cheapest comparable one-retailer basket selection with explicit exact ties.
- Server-owned WeeklyPlan/Pantry Optimization Preview and responsive Optimization UX.

#### Retailer connectivity

- Universal Retailer Connectivity remains a permanent invariant: every canonical retailer stays coverage work until at least one reproducible accepted acquisition path exists.
- Chromium MV3 Retailer Bridge provides minimal-permission sanitized first-party acquisition with deterministic fixtures and persistent-Chromium E2E.
- Perekrestok and Pyaterochka have accepted browser paths; browser lifecycle is hardened for SPA navigation/store changes.
- Magnit public-web feasibility is established, while recurring production acquisition remains policy-blocked pending affirmative permission or a supported/licensed path.

#### Chizhik connectivity

- D1 is **COMPLETE / ACCEPTED**: the normal user-browser MV3 Retailer Bridge is the selected acquisition architecture.
- D2 transport foundation (#169/#171) adds exact bounded store-scoped delivery search while keeping successful JSON opaque and automatic search/offer production disabled.
- D2 store-context binding (#173/#174) accepts only exactly one first-party browser-evidenced store intersecting the validated directory; missing/foreign/unknown/conflicting context fails closed.
- Draft PR #177 implements an explicit user-invoked privacy-hardened schema canary with no permission widening, fixed candidate-field allowlist and persistent-Chromium E2E; exact head `c38173f3b15b66fa892534989e1aa2f51d98468d` passed 9/9 PR workflow groups.
- Chizhik offer mapping remains blocked on ordinary-user-browser product-schema evidence plus independent monetary-unit/scale evidence. Unknown availability remains `UNKNOWN`.

### Changed

- Current deterministic product phase is **M5 Productization**; M5.1 is complete/accepted and M5.2 remains intentionally unselected until release/manual-use evidence identifies the next constraint.
- `v0.1.0-rc.4` is now historical **failed release-contract evidence**, not the next operational target.
- The next operational release gate is **`v0.1.0-rc.5`**, tracked by #152.
- Stable `v0.1.0` remains blocked until a prerelease completes the immutable release workflow and manual product canary satisfactorily.
- Technical retailer accessibility and production/right-to-operate readiness remain independent facts.

### Fixed

- Release planning no longer attempts to reuse an already-published prerelease tag for newer source.
- `v0.1.0-rc.4` exposed an operator metadata defect: a SemVer prerelease tag was published with GitHub `prerelease=false`. The release contract rejected the mismatch before any write-capable publication work.
- Canonical release planning now requires explicit pre-release-checkbox verification before publishing `v0.1.0-rc.5`.
- Chizhik store context can no longer be guessed from the first active store; exactly one browser-evidenced validated context is required.

### Security

- Precise addresses, credentials, provider tokens, private headers and raw sensitive provider payloads remain excluded from ordinary evidence/logging.
- Chizhik connectivity adds no anti-bot bypass, stealth, proxy rotation, credential extraction or private-client impersonation.
- Release vulnerability policy remains fail-closed at `HIGH,CRITICAL`; no ignore/suppression behavior is added to make releases pass.
- Published release tags, including failed candidates, are immutable historical evidence and are never repointed.

## [0.1.0-rc.4] — 2026-08-18 — FAILED RELEASE METADATA CONTRACT

Source:

```text
8a269288addcb4aa8ea3d0ce46608b650cbdb6dc
```

- Tag/source selection was correct and immutable.
- Release workflow run `32136955056` failed at `Release / Verify → Validate release metadata`.
- GitHub supplied `prerelease=false` for SemVer prerelease tag `v0.1.0-rc.4`; `release_contract.py` correctly rejected the mismatch.
- Every later verify step was skipped and `Release / Publish` never started.
- Therefore rc.4 produced no new GHCR promotion, OCI `latest` mutation, release SBOM, provenance attestation, staging/final exact-digest release smoke or release evidence assets.
- GitHub temporarily treated rc.4 as `Latest release` because the release object was published as non-prerelease; presentation metadata should be corrected without moving/deleting the tag.
- Full record: `docs/v0.1.0-rc.4-release-failure-2026-08-18.md`.

## [0.1.0-rc.3] — historical prerelease

- Immutable source: `d988b8c596a737326aeac67f74b6f65a6aaed3bf`.
- Later accepted connectivity/documentation changes belong to later prereleases rather than a rewritten rc.3.

## [0.1.0-rc.2] — 2026-08-09

- `Release / Verify` completed and `Release / Publish` reached multi-platform staging publication.
- The release correctly failed closed at the first real Trivy gate on pgJDBC `42.7.11` / `CVE-2026-54291` (`HIGH`, fixed in `42.7.12`).
- Subsequent mainline work upgraded pgJDBC, moved the web final runtime to distroless Node 24 Debian 13/non-root and added ordinary Container Security CI.

## [0.1.0-rc.1] — 2026-08-09

- First real GitHub prerelease event proved metadata/main-ancestry validation, source verification and production browser testing.
- It exposed executable-mode defects in release helper scripts; those modes were fixed before rc.2.

## Pre-release foundation — 2026-08-09 to 2026-08-11

- Java 25 / Spring Boot 4.1 API foundation with PostgreSQL 18, Flyway, jOOQ and Testcontainers.
- Contract-first OpenAPI 3.1 API plus generated TypeScript client.
- Next.js 16 / React 19 responsive web foundation with Vitest and Playwright.
- Reproducible verification, Docker/Compose release topology, CodeQL, Dependency Review, Container Security, Release Contract and Release Bundle CI.
- Evidence-driven retailer feasibility research for X5, Magnit, Chizhik, Ozon Fresh, Samokat, Kuper, Lenta and VkusVill.