# Roadmap

Updated: 2026-08-18

The roadmap is evidence-driven. Technical connectivity, production-access readiness and deterministic product/core maturity are separate dimensions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible accepted acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope. Technical accessibility is never treated as automatic production/right-to-operate approval.

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Accepted evidence established Perekrestok/Pyaterochka browser-bridge acquisition, Magnit public-web technical feasibility, multiple acquisition modes, deterministic sanitized verification and retailer-neutral architecture.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).

Goal achieved: deterministic provider-neutral shopping requirements, canonical quantities, matching, package-aware single-store basket calculation, truthful incomplete/uncertain/unavailable states, production-access gating, stateless comparison preview and responsive manual-list flow.

## M2 — Recipes — COMPLETE / ACCEPTED

Goal achieved: recipes are a deterministic first-class source of shopping requirements.

Accepted slices: Recipe domain + ShoppingList conversion, stateless Recipe shopping preview, Recipe → Comparison composition, responsive Recipe UI and deterministic multi-Recipe aggregation.

Permanent rule: automatic Recipe merging remains exact normalized requirement + canonical unit. Fuzzy/synonym/AI equivalence is never implicit.

## M3 — Weekly Planning / Pantry — COMPLETE / ACCEPTED

Goal achieved: combine ordered meal occurrences into deterministic weekly demand, compare it across retailers and subtract explicit request-scoped Pantry evidence without hidden ingredient loss.

Accepted slices include WeeklyPlan domain/composition, stateless shopping/comparison boundaries, responsive planning UI, pure Pantry subtraction, Pantry-aware shopping/comparison composition and responsive Pantry controls.

Explicit omit-all / never-buy semantics remain deferred and must not be encoded as Pantry stock.

## M4 — Basket Optimization — COMPLETE / ACCEPTED

Goal achieved for the current deterministic slice: optimize truthful one-retailer checkout cost rather than naive SKU sums.

Accepted scope: explicit known/unknown delivery/service fees and minimum-order evidence, checkout eligibility/comparability, deterministic cheapest comparable basket with explicit ties, server-owned Optimization Preview and responsive Optimization UX.

Deferred: richer substitute/package optimization, multi-store split optimization and confidence/freshness pricing policy.

## M5 — Productization — CURRENT

Goal: make the accepted product reliable for real repeat use, then choose further productization only from release/manual-use evidence.

### M5.1 — Private local WeeklyPlan draft — COMPLETE / ACCEPTED

Acceptance: [`m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md`](m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md).

Accepted result: one versioned same-origin semantic WeeklyPlan/Pantry draft. Generated identities, comparison/economics/optimizer output and provider evidence are never persisted as authority. Restore never implies submission and storage failures fail closed.

### M5.2 — INTENTIONALLY UNSELECTED

Do not preselect accounts, analytics, feature flags, provider health or saved history before the real release-candidate/manual-use canary demonstrates the highest-value constraint.

## Release history correction

`v0.1.0-rc.3` is **already an existing immutable prerelease**. Its tag resolves to:

`d988b8c596a737326aeac67f74b6f65a6aaed3bf`

Current main is 13 commits ahead of that tag at the time of this correction. rc.3 must not be moved, deleted or reused for newer source.

## Immediate mainline target — `v0.1.0-rc.4`

Issue: #152.

The next release must be cut from the documentation-synchronized, fully verified final `main` SHA recorded in #152. `v0.1.0-rc.4` is currently absent.

Required sequence:

1. merge the rc.4 canonical-documentation correction;
2. record the exact resulting `main` SHA in #152;
3. verify the final target per repository release policy;
4. confirm `v0.1.0-rc.4` Release and tag/ref are absent immediately before publication;
5. publish one GitHub prerelease targeting only that exact SHA;
6. require `Release / Verify` and `Release / Publish` to complete the existing immutable release contract;
7. inspect evidence/checksums/digests and verify `latest` remains unchanged;
8. manually exercise the product from immutable rc.4 artifacts;
9. fix release-canary defects before choosing M5.2.

The release contract must prove:

- source/main ancestry and release metadata;
- repository verification and production browser E2E;
- `linux/amd64` + `linux/arm64` staging images;
- unchanged fail-closed Trivy `HIGH,CRITICAL` gate;
- SPDX SBOM per candidate image manifest;
- staging exact-digest Compose smoke;
- copy-without-rebuild promotion with digest identity preserved;
- final exact-digest smoke;
- GitHub provenance attestations;
- prerelease SemVer OCI promotion without mutating `latest`;
- final manifest architecture checks;
- attached release evidence and checksums.

`.github/workflows/release.yml` accepts generic SemVer prereleases, so rc.4 does not require release-code changes.

Stable `v0.1.0` remains blocked until prerelease evidence and manual acceptance are satisfactory.

## Parallel retailer connectivity

Connectivity work may proceed in parallel before the final rc.4 SHA is frozen. Once #152 returns to READY with an exact SHA, no merge may silently move that target.

### Browser Bridge lifecycle — COMPLETE / ACCEPTED

#54/#153 hardened long-lived browser sessions across SPA navigation/store changes with event-driven lifecycle handling, fresh-context gating, bounded resource evidence and revision-safe writes without widening production extension permissions.

### Chizhik D1 — COMPLETE / ACCEPTED

#167/#168 resolved the transport decision:

- ordinary user browser: `/api/v1/shops/` returns valid JSON store directory;
- stock GitHub-hosted Chromium: `page-unavailable`;
- accepted path: normal **user-browser MV3 Retailer Bridge**;
- managed CI/server browser worker: not selected;
- no stealth, proxy rotation, fingerprint spoofing, credential/cookie/header extraction, mobile impersonation or arbitrary forwarding.

### Chizhik D2 transport — IMPLEMENTED / MERGED; SCHEMA GATE OPEN

#169 remains open. PR #171 added the exact bounded store-scoped delivery-search transport while deliberately keeping successful JSON opaque and preventing automatic search/offer production before schema acceptance.

Canary: [`integrations/chizhik-d2-delivery-search-canary-2026-08-18.md`](integrations/chizhik-d2-delivery-search-canary-2026-08-18.md).

### Chizhik D2 evidenced store context — COMPLETE / ACCEPTED

#173/#174 requires store context to come only from exact first-party Chizhik delivery resource evidence already seen by the official browser session, intersected with the validated `/api/v1/shops/` directory. Exactly one distinct validated store is required; missing/foreign/unknown/conflicting contexts fail closed. `searchStore` remains disabled before schema acceptance.

### Chizhik next slice — BLOCKED ON ORDINARY-USER-BROWSER EVIDENCE

Do **not** implement `BrowserObservation` / `ObservedOffer` mapping until #169 receives sanitized evidence proving product-array/container path, product identifier, product name, price field **and monetary unit/scale**, plus explicit availability semantics if any.

If availability semantics are not proven, map `UNKNOWN`. Promotion, loyalty, package and discount semantics remain unavailable until separately evidenced.

### Other mandatory retailer work

Continue universal connectivity for #36 Kuper supported aggregator/API path and permitted reuse, Ozon Fresh, Samokat, Lenta, VkusVill and additional canonical retailers/banners.

For every retailer, keep technical feasibility and production-access/right-to-operate approval separate.

## M6 — Native Mobile — FUTURE

Goal: Android/iOS clients using the shared API vocabulary/generated contracts only after browser/API product semantics and release behavior are stable enough to justify native clients.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes behavior correct, explainable and worth the operational cost.
