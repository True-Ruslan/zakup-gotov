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

Accepted slices:

- M2.1 Recipe domain + Recipe → ShoppingList;
- M2.2 stateless Recipe shopping preview API;
- M2.3 Recipe → Comparison composition;
- M2.4 responsive Recipe UI;
- M2.5 deterministic multi-Recipe aggregation.

Permanent rule: automatic Recipe merging remains exact normalized requirement + canonical unit. Fuzzy/synonym/AI equivalence is never implicit.

## M3 — Weekly Planning / Pantry — COMPLETE / ACCEPTED

Goal achieved: combine ordered meal occurrences into deterministic weekly demand, compare it across retailers and subtract explicit request-scoped Pantry evidence without hidden ingredient loss.

Accepted slices:

- M3.1 WeeklyPlan domain + deterministic shopping composition;
- M3.2 stateless WeeklyPlan shopping preview;
- M3.3 WeeklyPlan → Comparison composition;
- M3.4 responsive Weekly Planning UI;
- M3.5.1 pure Pantry subtraction semantics;
- M3.5.2 Pantry-aware weekly shopping preview;
- M3.5.3 Pantry-aware comparison composition;
- M3.5.4 responsive Pantry controls.

Explicit omit-all / never-buy semantics remain deferred and must not be encoded as Pantry stock.

## M4 — Basket Optimization — COMPLETE / ACCEPTED

Goal achieved for the current deterministic slice: optimize truthful one-retailer checkout cost rather than naive SKU sums.

Accepted scope:

- M4.1 explicit known/unknown delivery/service fees and minimum-order evidence;
- M4.2 one-retailer checkout eligibility/comparability;
- M4.3 deterministic cheapest comparable basket with explicit ties;
- M4.4 server-owned Optimization Preview and responsive Optimization UX.

Deferred: richer substitute/package optimization, multi-store split optimization and any confidence/freshness pricing policy.

## M5 — Productization — CURRENT

Goal: make the accepted product reliable for real repeat use, then choose further productization only from release/manual-use evidence.

### M5.1 — Private local WeeklyPlan draft — COMPLETE / ACCEPTED

Acceptance: [`m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md`](m5-1-private-local-weekly-plan-draft-acceptance-2026-08-16.md).

Accepted result: one versioned same-origin semantic WeeklyPlan/Pantry draft. Generated identities, comparison/economics/optimizer output and provider evidence are never persisted as authority. Restore never implies submission and storage failures fail closed.

### M5.2 — INTENTIONALLY UNSELECTED

Do not preselect accounts, analytics, feature flags, provider health or saved history before the real release-candidate/manual-use canary demonstrates the highest-value constraint.

## Immediate mainline target — `v0.1.0-rc.3`

Issue: #152.

The previous M5.1-only target SHA is invalidated because accepted connectivity work merged afterwards. The next release must be cut from the **documentation-synchronized, fully verified final `main` SHA** recorded in #152.

Required sequence:

1. merge the post-D2 canonical documentation synchronization;
2. record the exact resulting `main` SHA in #152;
3. verify that exact SHA with all normal required push workflow groups and zero failures;
4. confirm `v0.1.0-rc.3` Release and tag/ref are still absent;
5. publish one GitHub prerelease targeting only that exact SHA;
6. require `Release / Verify` and `Release / Publish` to complete the existing immutable release contract;
7. inspect evidence/checksums/digests and verify `latest` remains unchanged;
8. manually exercise the product from immutable rc.3 artifacts;
9. fix release-canary defects before choosing M5.2.

The existing release contract must prove:

- source/main ancestry and release metadata;
- repository verification and production browser E2E;
- `linux/amd64` + `linux/arm64` staging images;
- unchanged fail-closed Trivy `HIGH,CRITICAL` gate;
- SPDX SBOM per candidate image manifest;
- staging exact-digest Compose smoke;
- copy-without-rebuild promotion to final packages with digest identity preserved;
- final exact-digest smoke;
- GitHub provenance attestations;
- prerelease SemVer OCI promotion without mutating `latest`;
- final manifest architecture checks;
- attached release evidence and checksums.

Stable `v0.1.0` remains blocked until prerelease evidence and manual acceptance are satisfactory.

## Parallel retailer connectivity

Connectivity work may proceed in parallel, but it must not silently move the final rc.3 SHA after the release issue is returned to READY.

### Browser Bridge lifecycle — COMPLETE / ACCEPTED

#54/#153 hardened long-lived browser sessions across SPA navigation/store changes with event-driven lifecycle handling, fresh-context gating, bounded resource evidence and revision-safe writes without widening production extension permissions.

### Chizhik D1 — COMPLETE / ACCEPTED

#167/#168 resolved the transport decision:

- ordinary user browser: `/api/v1/shops/` returns valid JSON store directory;
- stock GitHub-hosted Chromium: `page-unavailable`;
- accepted path: normal **user-browser MV3 Retailer Bridge**;
- managed CI/server browser worker: not selected;
- no stealth, proxy rotation, fingerprint spoofing, credential/cookie/header extraction, mobile impersonation or arbitrary forwarding.

The CI-browser negative result is evidence, not an unresolved decision gate.

### Chizhik D2 transport — IMPLEMENTED / MERGED; SCHEMA GATE OPEN

#169 remains open. PR #171 added the exact, bounded store-scoped delivery-search transport while deliberately keeping successful JSON opaque and preventing automatic search/offer production before schema acceptance.

Canary: [`integrations/chizhik-d2-delivery-search-canary-2026-08-18.md`](integrations/chizhik-d2-delivery-search-canary-2026-08-18.md).

### Chizhik D2 evidenced store context — COMPLETE / ACCEPTED

#173/#174 establishes that store context cannot be guessed:

- context comes only from exact first-party Chizhik delivery catalog resource paths already seen by the official browser session;
- path-embedded `sap_id` must intersect with the validated `/api/v1/shops/` directory;
- exactly one distinct validated store is required;
- missing, foreign-origin, unknown-store and conflicting contexts fail closed;
- `searchStore` remains disabled and observations remain empty;
- Chromium E2E covers the accepted failure modes;
- PR #174 merge: `6c0af6ffa347c434e02600e83533244f8e2d15db`.

### Chizhik next slice — BLOCKED ON ORDINARY-USER-BROWSER EVIDENCE

Do **not** implement `BrowserObservation` / `ObservedOffer` mapping until #169 receives sanitized evidence from the documented ordinary-user-browser canary proving:

- product-array/container path;
- product identifier field;
- product-name field;
- price field and monetary unit/scale;
- explicit availability semantics, if any.

If availability semantics are not proven, map `UNKNOWN`. Promotion, loyalty, package and discount semantics remain unavailable until separately evidenced.

### Other mandatory retailer work

Continue universal connectivity for:

- #36 Kuper supported aggregator/API path and permitted reuse;
- Ozon Fresh;
- Samokat;
- Lenta;
- VkusVill;
- additional canonical retailers/banners.

For every retailer, keep technical feasibility and production-access/right-to-operate approval separate.

## M6 — Native Mobile — FUTURE

Goal: Android/iOS clients using the shared API vocabulary/generated contracts only after browser/API product semantics and release behavior are stable enough to justify native clients.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes behavior correct, explainable and worth the operational cost.
