# Roadmap

Updated: 2026-08-20

The roadmap is evidence-driven. Technical connectivity, production-access readiness, deterministic product/core maturity and release acceptance are separate dimensions.

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

### Stable product baseline — `v0.1.0` RELEASED / ACCEPTED

The first stable release is accepted. It promotes the already accepted immutable `v0.1.0-rc.7` application artifacts without rebuilding them.

Stable product source:

```text
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

Accepted application indexes:

```text
API  sha256:1c5c4a104fee295cd579b0e69a23b508a297b1eb931a45c0ce71d8b1791e54e1
Web  sha256:5bc236f3f304dffe29f54921f5a2bbf27d3df67c18714d4cc268d6d25bafce68
```

Automated rc.7 release run `32293764820`, manual product canary run `32359437905` and stable-promotion run `32384418147` are all accepted. Stable publication uses immutable `image@sha256` sources, strict raw-OCI-index verification, no rebuild, exact-digest runtime smoke and GitHub Release publication last.

Current `main` is intentionally ahead of the stable product source. Post-rc.7 work includes Chizhik schema-canary functionality and release/manual-canary/stable-promotion hardening. Published stable tags remain immutable historical evidence and are never repointed to later `main`.

### M5.2 — INTENTIONALLY UNSELECTED

Do **not** preselect accounts, analytics, feature flags, provider health or saved history merely because stable `v0.1.0` now exists.

The rc.7 manual product canary found no new product-runtime blocker: normal WeeklyPlan/Pantry, Recipe, manual-list, responsive, API-unavailable and recovery paths passed. The failed attempts that preceded acceptance were evidence-harness defects (#185/#187), not unmet product requirements. Therefore the current evidence does not justify inventing an M5.2 scope.

M5.2 should be selected from real stable-use evidence or a concrete product constraint with measurable value. Until then, work that already has stronger evidence — especially retailer connectivity — remains the priority.

## Release validation history

### `v0.1.0-rc.3` — historical prerelease

Immutable source: `d988b8c596a737326aeac67f74b6f65a6aaed3bf`. Do not move, delete or reuse the tag.

### `v0.1.0-rc.4` — FAILED AT METADATA CONTRACT

Immutable source: `8a269288addcb4aa8ea3d0ce46608b650cbdb6dc`.

Release run `32136955056` failed closed at `Release / Verify → Validate release metadata` because GitHub supplied `prerelease=false` for a SemVer prerelease tag. `Release / Publish` never started, so rc.4 established no accepted new GHCR/SBOM/attestation/staging/final-smoke evidence and did not mutate OCI `latest`.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — FAILED AT WEB SECURITY GATE

Immutable source: `a485c80dc1eb36122791c629f92b247354b0ee09`.

Release run `32224834303` eventually completed `Release / Verify`, then failed closed in `Release / Publish` at the Web amd64 HIGH/CRITICAL Trivy gate on inherited `libssl3t64 3.5.6-1~deb13u2 / CVE-2026-14456 / HIGH / fix_deferred`.

The accepted recovery keeps the minimal Distroless Debian 13 runtime, proves the vulnerable OpenSSL path is not reachable from the production Web runtime, and applies one exact-version OpenVEX statement only after final-image reachability guards pass. Trivy severity and `exit-code=1` remain unchanged for every unsuppressed finding.

Failure record: [`v0.1.0-rc.5-release-failure-2026-08-19.md`](v0.1.0-rc.5-release-failure-2026-08-19.md). Security assessment: [`security/CVE-2026-14456-vex-assessment.md`](security/CVE-2026-14456-vex-assessment.md).

### `v0.1.0-rc.6` — FAILED AT ARM64 RUNTIME-GUARD MATERIALIZATION

Immutable source: `946bc19d6ca4a544c13d74f420fce12b1e5fe815`.

`Release / Verify` passed. Publish built the multi-architecture candidates and passed the amd64 Web runtime guard, then failed before Trivy when Docker attempted to materialize the same parent OCI-index digest as a second platform child and reported `cannot overwrite digest ...`.

Recovery PR #180 reproduced the defect against the immutable rc.6 index, then changed registry-mode inspection to resolve and verify an exact platform child manifest before local pull/create. Missing, ambiguous or malformed platform descriptors fail closed.

Failure record: [`v0.1.0-rc.6-release-failure-2026-08-19.md`](v0.1.0-rc.6-release-failure-2026-08-19.md).

### `v0.1.0-rc.7` — AUTOMATED + MANUAL ACCEPTED

Immutable source: `b754f5193f852db0312011f3f6c3ec6c7dd22eb2`.

Release run `32293764820` completed **SUCCESS on attempt 1**. The automated release contract passed end to end:

- release metadata/source identity and ancestry;
- repository verification, production Web build and responsive browser E2E;
- multi-architecture API/Web staging builds;
- exact Web VEX contract plus amd64 and arm64 runtime reachability guards;
- all four API/Web amd64/arm64 Trivy HIGH/CRITICAL gates;
- four SPDX SBOMs;
- exact-digest staging and final bundle smoke;
- staging-to-final copy without rebuild and digest-identity assertion;
- API/Web provenance attestations;
- prerelease OCI promotion;
- manifests, verification metadata, checksums, vulnerability reports, SBOMs, Compose and exact VEX release evidence.

Manual canary run `32359437905` then passed the normal product flows, narrow-layout check, fail-closed API-unavailable state and recovered/restarted state using the immutable rc.7 bundle. Evidence artifact id `9402970517`, digest `sha256:158afcff6c270526823ad372cf883cb5eeaf723eacfacab4d2a46fb68c625c25`, was manually reviewed and accepted in #152.

**rc.7 verdict: ACCEPTED.**

### `v0.1.0` — STABLE RELEASE ACCEPTED

Stable promotion is an explicit owner-gated release action, not a rebuild of later `main`.

The first promotion attempt `32362833963` failed closed before stable mutation because the workflow incorrectly assumed a mutable rc alias must preserve the source OCI index's top-level descriptor digest. PR #189 replaced that assumption with strict raw-index equivalence while keeping immutable accepted digest refs as the only promotion sources.

Accepted stable-promotion run:

```text
32384418147
```

Promotion harness/main source:

```text
a550501dd7fdaabcb51c2faf83a9bbbf4c96c731
```

The accepted run verified manual-canary evidence, immutable rc.7 release identity/assets/checksums, stable draft assets, accepted API/Web registry indexes, no-rebuild promotion to `0.1.0` and `latest`, strict post-promotion OCI descriptor equivalence, exact-digest runtime health and final GitHub Release tag/assets. GitHub Release `373829773` was published last as `draft=false`, `prerelease=false` with tag `v0.1.0` resolving exactly to `b754f5193f852db0312011f3f6c3ec6c7dd22eb2`.

**Stable `v0.1.0` verdict: RELEASED / ACCEPTED.**

## Immediate mainline targets

The release gate is complete. The strongest evidence-backed next engineering lane is retailer connectivity rather than speculative productization.

1. **Chizhik #169 — next priority.** Obtain ordinary-user-browser sanitized D2 structural evidence from the merged canary. Independently establish the monetary unit/scale of `prices.regular`. Separately establish availability semantics if possible; otherwise availability remains `UNKNOWN`. Implement offer mapping only after all relevant evidence gates are accepted.
2. **Other canonical retailers.** Continue universal connectivity for Kuper (#36), Ozon Fresh, Samokat, Lenta, VkusVill and additional banners. Technical feasibility and production/right-to-operate approval remain separate.
3. **Stable-use feedback.** Observe real `v0.1.0` usage before selecting M5.2. Create M5.2 only when a concrete product constraint outranks connectivity work by evidence and expected value.
4. **Release maintenance.** Keep immutable release tags/evidence intact, retain fail-closed HIGH/CRITICAL security policy and treat CI-environment reliability separately from product correctness.

## Parallel retailer connectivity

### Browser Bridge lifecycle — COMPLETE / ACCEPTED

Long-lived browser sessions are hardened across SPA navigation/store changes with event-driven lifecycle handling, fresh-context gating, bounded resource evidence and revision-safe writes without widening production extension permissions.

### Chizhik D1 — COMPLETE / ACCEPTED

Ordinary user-browser store-directory evidence succeeds; stock GitHub-hosted Chromium is not the selected acquisition environment. Accepted path: normal **user-browser MV3 Retailer Bridge**. Managed CI/server browser worker, stealth, proxy rotation, fingerprint spoofing, credential/cookie/header extraction and mobile impersonation remain out of scope.

### Chizhik D2 transport — IMPLEMENTED / MERGED; SCHEMA GATE OPEN

#169 remains open. PR #171 added exact bounded store-scoped delivery search while keeping successful JSON opaque and preventing automatic search/offer production before schema acceptance.

### Chizhik D2 evidenced store context — COMPLETE / ACCEPTED

#173/#174 accepts store context only from exact first-party delivery resource evidence already seen by the official browser session and intersected with the validated store directory. Missing/foreign/unknown/conflicting contexts fail closed.

### Chizhik D2 schema canary — IMPLEMENTED / MERGED; LIVE EVIDENCE PENDING

PR #177 passed fresh exact-head **9/9 PR workflow groups** plus final security/privacy review and was squash-merged as `9822659c1b43df978e191e6f7826775fc615926d`.

The merged canary is explicit user invocation only, performs exactly one fixed bounded search, requires exactly one browser-evidenced validated store context, adds no host permissions, and emits only bounded allowlisted field/type structure plus sanitized HTTP metadata.

Do **not** implement production `BrowserObservation` / `ObservedOffer` mapping until #169 receives ordinary-user-browser evidence proving product container/identifier/name and price candidate structure, plus independent evidence of the price **monetary unit/scale**. Availability maps only from separately accepted semantics; otherwise it remains `UNKNOWN`. Promotion/package/loyalty/discount semantics are not inferred.

### Other mandatory retailer work

Continue universal connectivity for #36 Kuper supported aggregator/API path and permitted reuse, Ozon Fresh, Samokat, Lenta, VkusVill and additional canonical retailers/banners. For every retailer, technical feasibility and production/right-to-operate approval remain separate.

## M6 — Native Mobile — FUTURE

Android/iOS clients should use shared API vocabulary/generated contracts only after browser/API semantics and real stable-use behavior justify native clients.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes behavior correct, explainable and worth the operational cost.
