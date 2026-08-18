# Project State

Updated: 2026-08-18

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current product phase: **M5 — Productization**  
Immediate operational target: **`v0.1.0-rc.5` end-to-end release validation**

The product/core and retailer-connectivity tracks remain independent. Technical retailer reachability is not production approval, and merged transport code is not automatically an accepted offer provider.

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
- Chizhik D2 user-invoked schema-canary implementation — **IMPLEMENTED / DRAFT, NOT MERGED** (#177);
- M5.2 — **INTENTIONALLY UNSELECTED** until release/manual-use evidence identifies the next productization constraint.

## Accepted product/core baseline

M1–M4 are accepted. The current deterministic product supports canonical shopping requirements, exact/normalized matching with explicit ambiguity, package-aware single-store baskets, Recipes, WeeklyPlan/Pantry composition, truthful one-retailer checkout economics and deterministic cheapest comparable basket selection.

M5.1 adds one versioned same-origin semantic WeeklyPlan/Pantry draft. Generated identities, comparison/economics/optimizer output and provider evidence never become local authority; restore never implies submission.

Permanent product rules remain unchanged: fuzzy/AI equivalence is never implicit, unknown availability/economics stay unknown, incomplete baskets cannot masquerade as complete, and browser UI renders server-owned decisions instead of recomputing domain behavior.

## Retailer connectivity

### Perekrestok / Pyaterochka

Accepted first-party browser-bridge acquisition exists. Long-lived SPA/store-change sessions are hardened through event-driven lifecycle handling, fresh-context gating, stale/in-flight rejection and revision-safe writes without permission widening.

### Magnit

Technical public-web coverage is **AVAILABLE_PUBLIC_WEB**, while recurring production acquisition remains **BLOCKED** by project operating policy pending affirmative permission or a supported/licensed path.

### Chizhik

D1 is accepted: an ordinary user browser can access the fixed `/api/v1/shops/` directory, while stock GitHub-hosted Chromium is not the selected acquisition environment. The architecture is the normal user-browser MV3 Retailer Bridge; stealth, proxy rotation, fingerprint spoofing and credential/header/cookie extraction remain out of scope.

D2 transport (#171) is merged but successful JSON remains opaque to production. D2 store context (#174) is accepted only when exactly one current-session first-party delivery resource produces a `sap_id` that intersects the validated store directory; missing/foreign/unknown/conflicting context fails closed.

Issue #169 remains open for ordinary-user-browser schema and monetary-unit evidence before any `BrowserObservation` / `ObservedOffer` mapping. Availability remains `UNKNOWN` unless explicit semantics are proven.

Draft PR #177 implements a user-invoked privacy-hardened schema canary. Exact head `c38173f3b15b66fa892534989e1aa2f51d98468d` passed 9/9 PR workflow groups, including persistent-Chromium E2E, but remains unmerged until the current release-target sequence permits it.

## Release history and current gate

### `v0.1.0-rc.3` — historical prerelease

Immutable source:

```text
d988b8c596a737326aeac67f74b6f65a6aaed3bf
```

Do not move, delete or reuse the tag.

### `v0.1.0-rc.4` — FAILED CLOSED AT METADATA GATE

Immutable source:

```text
8a269288addcb4aa8ea3d0ce46608b650cbdb6dc
```

Release workflow run `32136955056` failed in `Release / Verify` at `Validate release metadata` because GitHub published the SemVer prerelease tag with `prerelease=false`.

The contract raised:

```text
ValueError: GitHub prerelease flag must match the SemVer prerelease state
```

All later verify work was skipped and `Release / Publish` never started. Therefore rc.4 produced no new GHCR publication/promotion, no OCI `latest` mutation, no release SBOM/attestation/evidence assets and no staging/final release smoke evidence.

GitHub temporarily treats rc.4 as `Latest release` because the release object was published as non-prerelease. Its presentation metadata should be corrected to `prerelease=true`, but the tag must remain immutable and rc.4 remains a **failed** release-contract attempt.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — NEXT OPERATIONAL TARGET

Issue: #152.

Required sequence:

1. merge this canonical documentation correction through fresh exact-head CI/review;
2. record the resulting exact `main` SHA in #152;
3. verify all normal exact-main push workflow groups are SUCCESS;
4. confirm `v0.1.0-rc.5` tag/release is absent immediately before publication;
5. publish one GitHub prerelease targeting only the recorded exact SHA with **Set as a pre-release enabled**;
6. require `Release / Verify` and `Release / Publish` to complete the existing immutable contract;
7. inspect multi-arch staging, Trivy `HIGH,CRITICAL`, SPDX SBOM, exact-digest staging/final smoke, copy-without-rebuild promotion, provenance, manifests, evidence/checksums and package visibility;
8. verify OCI `latest` remains untouched;
9. run the manual product canary from immutable rc.5 artifacts;
10. select M5.2 only from resulting evidence.

Stable `v0.1.0` remains blocked until a prerelease completes the full release workflow and manual acceptance is satisfactory.

## Known constraints / technical debt

- Chizhik offer mapping is blocked on real ordinary-browser schema/price-unit evidence (#169).
- Full production retailer coverage remains incomplete.
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
5. Provider/acquisition/fulfillment identifiers remain internal unless an accepted public contract exposes a product-safe identifier.
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
23. Published release tags are immutable historical evidence and are never repointed, including failed release candidates.
24. Release metadata must match SemVer prerelease state before any write-capable publication work.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 + generated TypeScript client;
- Next.js 16.3 / React 19.2;
- Testcontainers / Vitest / Testing Library / Playwright;
- Docker multi-stage production images + no-source-build Compose release topology;
- CodeQL / Dependency Review / Container Security / Release Contract / Release Bundle CI.