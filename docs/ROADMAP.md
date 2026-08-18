# Roadmap

Updated: 2026-08-18

The roadmap is evidence-driven. Technical connectivity, production-access readiness and deterministic product/core maturity are separate dimensions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible accepted acquisition path exists.

A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope. Technical accessibility is never automatic production/right-to-operate approval.

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1**. Accepted evidence established Perekrestok/Pyaterochka browser-bridge acquisition, Magnit public-web technical feasibility, multiple acquisition modes, deterministic sanitized verification and retailer-neutral architecture.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Deterministic provider-neutral shopping requirements, canonical quantities, matching, package-aware single-store basket calculation, truthful incomplete/uncertain/unavailable states, production-access gating, stateless comparison preview and responsive manual-list flow are accepted.

## M2 — Recipes — COMPLETE / ACCEPTED

Recipes are a deterministic first-class source of shopping requirements. Automatic Recipe merging remains exact normalized requirement + canonical unit; fuzzy/synonym/AI equivalence is never implicit.

## M3 — Weekly Planning / Pantry — COMPLETE / ACCEPTED

Ordered meal occurrences compose deterministic weekly demand and explicit request-scoped Pantry evidence subtracts from it without hidden ingredient loss. Explicit omit-all / never-buy semantics remain deferred.

## M4 — Basket Optimization — COMPLETE / ACCEPTED

Current accepted scope covers explicit known/unknown checkout economics, deterministic one-retailer comparability and exact cheapest comparable basket selection. Rich substitute/package optimization and multi-store split optimization remain deferred.

## M5 — Productization — CURRENT

### M5.1 — Private local WeeklyPlan draft — COMPLETE / ACCEPTED

One versioned same-origin semantic WeeklyPlan/Pantry draft is accepted. Generated identities, retailer results, economics, optimizer output and provider evidence never become persisted authority.

### M5.2 — INTENTIONALLY UNSELECTED

Do not preselect accounts, analytics, feature flags, provider health or saved history before real release/manual-use evidence identifies the highest-value productization constraint.

## Release validation history

### `v0.1.0-rc.3` — historical prerelease

Immutable source:

```text
d988b8c596a737326aeac67f74b6f65a6aaed3bf
```

Do not move, delete or reuse the tag.

### `v0.1.0-rc.4` — FAILED AT METADATA CONTRACT

Immutable source:

```text
8a269288addcb4aa8ea3d0ce46608b650cbdb6dc
```

Release run `32136955056` failed closed at `Release / Verify → Validate release metadata` because GitHub published a SemVer prerelease tag with `prerelease=false`.

The write-capable `Release / Publish` job was skipped, so rc.4 established no new GHCR/SBOM/attestation/staging/final-smoke evidence and did not mutate OCI `latest`.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

The rc.4 tag remains historical evidence. Its GitHub release presentation should be corrected to pre-release status, but rc.4 remains a failed release-contract attempt.

## Immediate mainline target — `v0.1.0-rc.5`

Issue: #152.

Required sequence:

1. merge the rc.4-failure/rc.5 canonical-documentation correction through fresh exact-head CI/review;
2. record the exact resulting `main` SHA in #152;
3. verify all normal push workflow groups against that exact SHA;
4. confirm `v0.1.0-rc.5` tag/release are absent immediately before publication;
5. publish one GitHub release targeting only that exact SHA with **Set as a pre-release enabled**;
6. require `Release / Verify` and `Release / Publish` to complete the existing release contract;
7. inspect all release evidence and verify OCI `latest` remains untouched;
8. run the manual product canary from immutable rc.5 artifacts;
9. fix release-canary defects before choosing M5.2.

A successful rc.5 must prove:

- source/main ancestry and release metadata;
- repository verification and production browser E2E;
- `linux/amd64` + `linux/arm64` staging images;
- unchanged fail-closed Trivy `HIGH,CRITICAL` policy;
- SPDX SBOM per candidate image manifest;
- staging exact-digest Compose smoke;
- copy-without-rebuild promotion with digest identity preserved;
- final exact-digest smoke;
- GitHub provenance attestations;
- prerelease SemVer OCI promotion without mutating `latest`;
- final manifest architecture checks;
- attached release evidence/checksums.

Stable `v0.1.0` remains blocked until prerelease evidence and manual acceptance are satisfactory.

## Parallel retailer connectivity

### Browser Bridge lifecycle — COMPLETE / ACCEPTED

Long-lived browser sessions are hardened across SPA navigation/store changes with event-driven lifecycle handling, fresh-context gating, bounded resource evidence and revision-safe writes without widening production extension permissions.

### Chizhik D1 — COMPLETE / ACCEPTED

Ordinary user-browser store-directory evidence succeeds; stock GitHub-hosted Chromium is not the selected acquisition environment. Accepted path: normal **user-browser MV3 Retailer Bridge**. Managed CI/server browser worker, stealth, proxy rotation, fingerprint spoofing, credential/cookie/header extraction and mobile impersonation remain out of scope.

### Chizhik D2 transport — IMPLEMENTED / MERGED; SCHEMA GATE OPEN

#169 remains open. PR #171 added exact bounded store-scoped delivery search while keeping successful JSON opaque and preventing automatic search/offer production before schema acceptance.

### Chizhik D2 evidenced store context — COMPLETE / ACCEPTED

#173/#174 accepts store context only from exact first-party delivery resource evidence already seen by the official browser session and intersected with the validated store directory. Missing/foreign/unknown/conflicting contexts fail closed.

### Chizhik D2 schema canary — IMPLEMENTED / DRAFT

Draft PR #177 adds an explicit user-invoked sanitized schema canary with no permission widening, a fixed candidate-field allowlist and real persistent-Chromium E2E. Its exact head `c38173f3b15b66fa892534989e1aa2f51d98468d` passed 9/9 PR workflow groups.

Do **not** implement production `BrowserObservation` / `ObservedOffer` mapping until #169 receives ordinary-user-browser evidence proving product container/identifier/name, price field **and monetary unit/scale**, plus explicit availability semantics if any. Unknown availability remains `UNKNOWN`.

### Other mandatory retailer work

Continue universal connectivity for #36 Kuper supported aggregator/API path and permitted reuse, Ozon Fresh, Samokat, Lenta, VkusVill and additional canonical retailers/banners. For every retailer, technical feasibility and production/right-to-operate approval remain separate.

## M6 — Native Mobile — FUTURE

Android/iOS clients should use shared API vocabulary/generated contracts only after browser/API semantics and release behavior are stable enough to justify native clients.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes behavior correct, explainable and worth the operational cost.