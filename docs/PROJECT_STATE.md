# Project State

Updated: 2026-08-18

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current product phase: **M5 — Productization**  
Immediate operational target: **`v0.1.0-rc.4` end-to-end release validation**

The product/core and retailer-connectivity tracks are intentionally separate. A retailer can be technically reachable without being approved for production acquisition, and a merged transport implementation is not automatically an accepted product/price provider.

## Milestone status

- M0 Product & Integration Discovery — **COMPLETE**;
- M1 Shopping Core — **COMPLETE / ACCEPTED**;
- M2 Recipes — **COMPLETE / ACCEPTED**;
- M3 Weekly Planning / Pantry — **COMPLETE / ACCEPTED**;
- M4 Basket Optimization — **COMPLETE / ACCEPTED**;
- pre-release web runtime hardening — **COMPLETE / ACCEPTED** (#150);
- M5.1 Private local WeeklyPlan draft — **COMPLETE / ACCEPTED** (#148/#149);
- Retailer Bridge persistent-session / SPA / store-change lifecycle hardening — **COMPLETE / ACCEPTED** (#54/#153);
- Chizhik D1 user-browser transport decision — **COMPLETE / ACCEPTED** (#167/#168);
- Chizhik D2 fixed store-scoped search transport — **IMPLEMENTED / MERGED, OFFER MAPPING DISABLED** (#169/#171);
- Chizhik D2 browser-evidenced store-context binding — **COMPLETE / ACCEPTED** (#173/#174);
- M5.2 — **INTENTIONALLY UNSELECTED** until release-candidate/manual-use evidence identifies the next productization constraint.

## Accepted product/core baseline

### M1 — Shopping Core

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).

Accepted behavior includes canonical retailer visibility/readiness, canonical quantities and stable Shopping identities, provider/location provenance, freshness/availability evidence, deterministic exact/normalized matching with explicit ambiguity, whole-package single-store basket arithmetic, truthful `READY / UNCERTAIN / INCOMPLETE / UNAVAILABLE` states, production-access gating before acquisition and stateless responsive comparison flows.

### M2 — Recipes

Recipes are a deterministic first-class source of shopping requirements. Accepted behavior includes Recipe domain/conversion, serving scaling, Recipe → ShoppingList → Comparison, responsive Recipe UI and deterministic occurrence-aware multi-Recipe aggregation.

Permanent rule: automatic Recipe merging is exact normalized requirement + canonical unit only. Fuzzy/synonym/AI equivalence is never implicit.

### M3 — Weekly Planning / Pantry

Weekly Planning is the primary browser journey. Accepted behavior includes ordered meal occurrences, day metadata, target servings, deterministic weekly shopping composition, stateless WeeklyPlan shopping/comparison boundaries, request-scoped Pantry subtraction with ordered audit evidence, truthful `NO_REMAINING_DEMAND`, responsive browser acceptance and no browser-side Recipe scaling/Pantry subtraction/matching/basket arithmetic.

Explicit omit-all / never-buy semantics remain intentionally deferred; they are not Pantry stock.

### M4 — Basket Optimization

Accepted behavior includes explicit known/unknown delivery and service fees, explicit minimum-order evidence, separate merchandise subtotal / checkout-total knowledge / eligibility / comparability, deterministic single-retailer checkout assessment, exact cheapest comparable basket selection with explicit ties, server-owned Optimization Preview and responsive Optimization UX.

Production checkout economics remains fail-closed/unknown until retailer-specific evidence is accepted. Rich substitute/package optimization and multi-store split optimization remain deferred.

### M5.1 — Private local WeeklyPlan draft

Acceptance: [`m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md`](m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md).

Only editable semantic WeeklyPlan/Pantry input is persisted under one versioned same-origin key. Generated identities, retailer results, economics, optimizer output and provider evidence never become local draft authority. Restore never implies submission; storage failures fail closed without breaking explicit editing/submission.

## Retailer connectivity

### Permanent rule

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical connectivity, production-access readiness and deterministic product/core maturity are independent dimensions.

### Perekrestok / Pyaterochka

Accepted browser-bridge acquisition evidence exists. #54/#153 hardened long-lived SPA/store-change sessions through event-driven navigation handling, fresh-context gating, stale/in-flight rejection, revision-safe writes and bounded retained resource evidence without widening production extension permissions.

### Magnit

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`** by Zakup Gotov operating policy;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

Technical accessibility is not treated as permission for recurring production acquisition/reuse.

### Chizhik — current accepted state

#### D1 transport decision — COMPLETE / ACCEPTED

Issue #167 is closed completed; PR #168 merged as `e49c151fa44681dffe85fe90116009c86690672e`.

An ordinary user-opened official Chizhik page successfully fetched `GET https://app.chizhik.club/api/v1/shops/` with HTTP `200`, JSON and valid store rows containing `sap_id` and coordinates. Stock GitHub-hosted Chromium produced sanitized `page-unavailable` evidence. Therefore the accepted acquisition architecture is the normal **user-browser MV3 Retailer Bridge**; managed CI/server browser worker is not selected for Chizhik.

Stealth, fingerprint spoofing, proxy rotation, cookie/header/credential extraction, private/mobile-client impersonation and arbitrary URL forwarding remain out of scope.

#### D2 search transport — IMPLEMENTED / MERGED, NOT YET AN OFFER PROVIDER

Issue #169 remains open. PR #171 merged as `bb11bd45d0c5da20eed2a24dfdf585714912c1b1`.

Merged foundation uses an exact store-scoped Chizhik delivery-search endpoint family, validated-form `sap_id`, URL-encoded query, bounded limit, finite abort deadline and ordinary browser CORS behavior. Successful JSON remains opaque until live schema acceptance; automatic delivery search and offer mapping remain disabled.

Privacy-safe canary: [`integrations/chizhik-d2-delivery-search-canary-2026-08-18.md`](integrations/chizhik-d2-delivery-search-canary-2026-08-18.md).

#### D2 store-context binding — COMPLETE / ACCEPTED

Issue #173 is closed completed. PR #174 squash-merged as `6c0af6ffa347c434e02600e83533244f8e2d15db`.

Candidate fulfillment context comes only from already-observed exact first-party delivery catalog resource paths. Path-embedded `sap_id` must match the safe ID shape and exist in the validated `/api/v1/shops/` directory. Exactly one distinct validated context is required; missing, foreign, unknown or conflicting context fails closed. `searchStore` remains uncalled until #169 schema evidence is accepted.

PR #174 final head `0a0da74d744b85a9a936fc6049946174d96a4d09` passed **9/9 PR workflow groups SUCCESS** before merge.

#### Chizhik next evidence gate

#169 is blocked on ordinary-user-browser sanitized schema and price-unit evidence. Before `BrowserObservation` / `ObservedOffer` mapping, evidence must establish product-array/container path, stable product identifier field, product-name field, price field **and its monetary unit/scale**, plus explicit availability semantics if present. Unknown availability stays `UNKNOWN`; promo/loyalty/package/discount semantics are never inferred.

## Other mandatory connectivity work

Continue universal coverage for #36 Kuper supported aggregator/API access and permitted reuse, Ozon Fresh, Samokat, Lenta, VkusVill and additional major banners. Retailer-specific production/right-to-operate decisions remain mandatory before activation.

## Release history and next gate

### `v0.1.0-rc.3` — EXISTING HISTORICAL PRERELEASE

The immutable `v0.1.0-rc.3` ref already exists and resolves to:

`d988b8c596a737326aeac67f74b6f65a6aaed3bf`

Current `main` is 13 commits ahead of that ref as of this correction. The rc.3 tag/release must not be deleted, moved or reused for later source.

### `v0.1.0-rc.4` — NEXT OPERATIONAL TARGET

Issue: #152.

The `v0.1.0-rc.4` ref is currently absent. The required sequence is:

1. synchronize canonical docs so rc.3 is historical and rc.4 is the next gate;
2. merge that docs-only correction through fresh exact-head CI/review;
3. record the exact resulting `main` SHA in #152;
4. verify the final release target per repository policy;
5. re-check that `v0.1.0-rc.4` tag/release is absent immediately before publication;
6. publish one immutable GitHub prerelease targeting only that exact SHA;
7. require the existing release workflow to pass repository verification, production browser E2E, `linux/amd64` + `linux/arm64` staging, unchanged Trivy `HIGH,CRITICAL`, SPDX SBOM, exact-digest staging/final smoke, copy-without-rebuild promotion, provenance attestations, manifest checks and release evidence attachment;
8. leave `latest` untouched;
9. run the manual product canary from immutable rc.4 artifacts;
10. choose M5.2 only from resulting evidence.

`.github/workflows/release.yml` validates generic SemVer prereleases, so `v0.1.0-rc.4` requires no release-code change.

Stable `v0.1.0` remains blocked until prerelease and manual-canary evidence are satisfactory.

## Known constraints / technical debt

- Chizhik offer mapping is blocked on real ordinary-browser schema/price-unit evidence (#169).
- Full production retailer coverage is incomplete.
- Magnit production acquisition remains policy-blocked despite technical public-web feasibility.
- Kuper remains blocked on provider confirmation/access/reuse terms (#36).
- Real retailer checkout-economics evidence is not yet broadly available; unknown stays unknown.
- Explicit omit-all/never-buy semantics are deferred.
- Server-side saved-plan history/accounts/auth are not implemented.
- Analytics abstraction, feature flags and provider-health monitoring remain possible M5.2 candidates, not preselected work.
- Richer substitute/package optimization and multi-store split optimization are deferred.
- Native mobile remains future M6 work.

## Permanent invariants

1. Shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer remains visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness are independent.
4. Precise addresses are sensitive and redacted by default.
5. Provider/acquisition/fulfillment identifiers remain internal unless an accepted public contract explicitly exposes a product-safe identifier.
6. `UNKNOWN` availability is never coerced; observation time is not provider freshness.
7. Matching ambiguity never becomes a hidden winner.
8. Package quantity is explicit structured evidence; mass, volume and count are not interchangeable.
9. Incomplete baskets never expose misleading complete-basket totals.
10. Ordinary CI/browser acceptance makes no live retailer requests unless a separately controlled live workflow explicitly opts in.
11. Production-access policy scopes acquisition before provider invocation.
12. Recipe/WeeklyPlan/Pantry automatic matching remains exact requirement + canonical unit.
13. Pantry subtraction preserves original demand and ordered audit evidence.
14. Merchandise subtotal, checkout-total knowledge, eligibility and optimizer comparability are separate facts.
15. Only explicit comparable checkout candidates may participate in cheapest-basket selection.
16. Exact numeric minima remain explicit ties; no hidden retailer-order/freshness tie-break exists.
17. Browser optimization renders server-owned economics/optimizer decisions instead of recomputing them.
18. Browser-local persistence contains semantic editable input only and restore never implies submission.
19. Browser acquisition lifecycle evidence is revision-safe across SPA/store changes.
20. Ordinary-user-browser evidence and CI/server-browser evidence are separate evidence classes.
21. Browser fulfillment context must be evidenced by the current official session and validated against accepted retailer context evidence; it is never guessed.
22. No price mapping is accepted until the source field and monetary unit/scale are evidenced.
23. Published prerelease tags are immutable historical evidence and are never repointed to newer source.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 + generated TypeScript client;
- Next.js 16.3 / React 19.2;
- Testcontainers / Vitest / Testing Library / Playwright;
- Docker multi-stage production images + no-source-build Compose release topology;
- CodeQL / Dependency Review / Container Security / Release Contract / Release Bundle CI.
