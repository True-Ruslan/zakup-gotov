# Roadmap

Updated: 2026-08-19

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

Immutable source: `d988b8c596a737326aeac67f74b6f65a6aaed3bf`. Do not move, delete or reuse the tag.

### `v0.1.0-rc.4` — FAILED AT METADATA CONTRACT

Immutable source: `8a269288addcb4aa8ea3d0ce46608b650cbdb6dc`.

Release run `32136955056` failed closed at `Release / Verify → Validate release metadata` because GitHub supplied `prerelease=false` for a SemVer prerelease tag. `Release / Publish` never started, so rc.4 established no new GHCR/SBOM/attestation/staging/final-smoke evidence and did not mutate OCI `latest`.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — FAILED AT WEB SECURITY GATE

Immutable source: `a485c80dc1eb36122791c629f92b247354b0ee09`.

Release run `32224834303` eventually completed `Release / Verify` after one infrastructure-only Playwright/Ubuntu mirror timeout. `Release / Publish` built API/web multi-arch staging images and passed both API HIGH/CRITICAL scans, then failed closed at web amd64 Trivy.

The unchanged production image reproduced the exact root cause in normal Container Security CI:

```text
libssl3t64 3.5.6-1~deb13u2
CVE-2026-14456
HIGH
fix_deferred
```

No final rc.5 OCI promotion, `latest`, provenance, final smoke or release evidence assets occurred.

Failure record: [`v0.1.0-rc.5-release-failure-2026-08-19.md`](v0.1.0-rc.5-release-failure-2026-08-19.md).

## Immediate mainline target — `v0.1.0-rc.6`

Issue: #152. Recovery implementation: draft PR #179.

CVE-2026-14456 affects an OpenSSL QUIC server-listener code path. The production web process does not enable experimental QUIC, and final-image inspection proves that neither runtime Node nor the current native addon set dynamically links system `libssl`/`libcrypto`.

Two attempted base-image replacements were rejected by fresh Trivy evidence because they increased the actionable HIGH/CRITICAL surface. Recovery therefore keeps the minimal Distroless Debian 13 runtime and uses a narrow OpenVEX statement scoped to the exact inherited package/version and CVE, with `not_affected / vulnerable_code_not_in_execute_path`.

This is **not** a weakened security threshold. Controls are:

- VEX contains exactly one reviewed CVE/package-version statement;
- CI fails if the statement is widened;
- final-image Entrypoint/Cmd must not enable `--experimental-quic`;
- final Node and every `*.node` addon must not link system `libssl`/`libcrypto`;
- VEX applies only to web scans; API scans are unchanged;
- all unsuppressed `HIGH,CRITICAL` findings retain `exit-code=1`;
- suppressed evidence is visible in ordinary CI logs;
- release CI repeats the runtime guard on both amd64 and arm64;
- the exact OpenVEX file becomes checksummed release evidence;
- future failed JSON scans are summarized into durable workflow logs.

Security assessment: [`security/CVE-2026-14456-vex-assessment.md`](security/CVE-2026-14456-vex-assessment.md).

### Required rc.6 sequence

1. finish #179 documentation and recovery code;
2. require a final exact-head **9/9 PR workflow** pass;
3. explicitly inspect Container Security/Web: VEX contract PASS, runtime guard PASS, exact suppression visible, zero unsuppressed HIGH/CRITICAL findings;
4. require Release Contract CI and Release Bundle CI PASS on that same head;
5. merge #179 only after final review has no blocking findings/threads;
6. verify all normal exact-main push workflows on the resulting merge SHA;
7. confirm `v0.1.0-rc.6` tag/release are absent;
8. freeze that exact SHA in #152;
9. publish exactly one prerelease `v0.1.0-rc.6` with **Set as a pre-release enabled**;
10. require `Release / Verify` + `Release / Publish` end to end;
11. inspect both architecture runtime/VEX guards, all four vulnerability gates, SPDX SBOMs, staging/final exact-digest smoke, digest-preserving promotion, provenance, manifests, VEX/checksums and package visibility;
12. verify prerelease OCI promotion does not mutate `latest`;
13. run manual product canary from immutable rc.6 artifacts;
14. only then release #177 back to mainline, refresh it against current `main`, and run fresh exact-head CI/review.

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

Draft PR #177 adds an explicit user-invoked sanitized schema canary with no permission widening, a fixed candidate-field allowlist and real persistent-Chromium E2E. It remains frozen until rc.6 release recovery is accepted; after that it must be refreshed against current `main` and reverified.

Do **not** implement production `BrowserObservation` / `ObservedOffer` mapping until #169 receives ordinary-user-browser evidence proving product container/identifier/name, price field **and monetary unit/scale**, plus explicit availability semantics if any. Unknown availability remains `UNKNOWN`.

### Other mandatory retailer work

Continue universal connectivity for #36 Kuper supported aggregator/API path and permitted reuse, Ozon Fresh, Samokat, Lenta, VkusVill and additional canonical retailers/banners. For every retailer, technical feasibility and production/right-to-operate approval remain separate.

## M6 — Native Mobile — FUTURE

Android/iOS clients should use shared API vocabulary/generated contracts only after browser/API semantics and release behavior are stable enough to justify native clients.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes behavior correct, explainable and worth the operational cost.