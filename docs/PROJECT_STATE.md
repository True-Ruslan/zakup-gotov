# Project State

Updated: 2026-08-20

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current product phase: **M5 — Productization**  
Current `main`: `9822659c1b43df978e191e6f7826775fc615926d`  
Immediate operational targets: **manual product acceptance of immutable `v0.1.0-rc.7`** and **ordinary-user-browser Chizhik D2 evidence for #169**.

The product/core and retailer-connectivity tracks remain independent. Technical retailer reachability is not production approval, merged transport code is not automatically an accepted offer provider, and automated release acceptance is not manual product acceptance.

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
- Chizhik D2 user-invoked sanitized schema canary — **IMPLEMENTED / MERGED, LIVE EVIDENCE PENDING** (#177);
- rc.5 web security-gate recovery — **COMPLETE / VALIDATED IN RC.7** (#179);
- rc.6 multi-architecture runtime-guard materialization recovery — **COMPLETE / VALIDATED IN RC.7** (#180);
- `v0.1.0-rc.7` automated release contract — **ACCEPTED** (#152);
- stable `v0.1.0` — **BLOCKED ON MANUAL RC.7 PRODUCT CANARY**;
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

PR #177 was refreshed after rc.7 automated acceptance, passed fresh exact-head **9/9 PR workflow groups** plus final security/privacy review, and was squash-merged as current `main` commit `9822659c1b43df978e191e6f7826775fc615926d`.

The merged schema canary:

- runs only after an explicit toolbar-popup click;
- adds no `host_permissions`; production permissions remain `storage` only;
- requires the exact official `https://chizhik.club` page origin;
- requires exactly one browser-evidenced store intersecting the validated directory;
- performs exactly one fixed `кола`, `limit=1` D2 search per invocation;
- emits only sanitized HTTP metadata plus bounded field/type structure for the reviewed allowlist;
- never emits raw store/product/SKU/name/price/promotion/request/header/cookie/credential values;
- leaves automatic D2 search and `BrowserObservation` / `ObservedOffer` production disabled.

Issue #169 remains **OPEN**. Before any offer/price mapping it still requires:

1. ordinary-user-browser live structural evidence from the merged sanitized canary;
2. separate evidence proving the monetary unit/scale of `prices.regular` before `priceMinor` mapping;
3. separately accepted availability semantics, otherwise availability stays `UNKNOWN`;
4. no inferred promotion/loyalty/package/discount semantics.

### Verified Retailer Bridge artifact

Post-merge Retailer Bridge CI produced the exact-SHA artifact:

```text
retailer-bridge-9822659c1b43df978e191e6f7826775fc615926d
```

Artifact id: `9384831391`  
Artifact SHA-256: `255429d51f5df9c6dae0d49c73aeb34ec8358287833027c8d7c93e293c130dc3`  
Expires: 2026-09-02.

The downloaded ZIP hash matches GitHub's artifact digest. It contains only `content.js`, `manifest.json`, `popup.html`, `popup.js` and `service-worker.js`; the packaged manifest retains only `permissions: ["storage"]`, no `host_permissions`, and the reviewed first-party content-script origins.

This artifact is suitable as the exact verified bridge build for the ordinary-user-browser #169 canary. Its availability does not itself accept the live retailer schema.

## Current main CI state

Exact post-merge source:

```text
9822659c1b43df978e191e6f7826775fc615926d
```

All **8 expected `main` push workflow groups** are accepted successfully:

- Release Contract CI;
- Contract CI;
- API CI;
- Container Security CI;
- CodeQL (Java + JavaScript/TypeScript);
- Release Bundle CI;
- Web CI / responsive Web E2E;
- Retailer Bridge CI / persistent-Chromium extension E2E.

Dependency Review is PR-only and therefore is not expected on a `main` push.

The first post-merge Web E2E and Retailer Bridge browser jobs failed before tests because Ubuntu/Azure package mirrors stalled during bounded Playwright `--with-deps` installation. In both cases all source-level build/test gates preceding browser installation passed. Logs showed the configured 360-second bounded attempts terminating the package-manager process tree with exit 124; no product/browser-test assertion failed.

Only those infrastructure-failed jobs were rerun against the **same exact SHA**, with no source change, timeout relaxation or security-threshold change. The targeted reruns completed Chromium installation and the real browser E2E successfully. Retailer Bridge additionally uploaded the verified exact-SHA artifact above.

The bounded installer remains intentional: it uses finite attempts, APT network retries/timeouts and descendant-safe process termination rather than allowing an external mirror stall to hang CI indefinitely.

## Release history and current gate

### `v0.1.0-rc.3` — historical automated-accepted prerelease

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

Release run `32136955056` failed at `Release / Verify → Validate release metadata` because GitHub supplied `prerelease=false` for a SemVer prerelease tag. `Release / Publish` never started, so there was no GHCR promotion, OCI `latest` mutation, release SBOM/attestation or release smoke/evidence publication.

The historical GitHub Release presentation still reports `prerelease=false`; this UI metadata should be corrected without moving/deleting the tag. rc.4 remains failed historical evidence regardless.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — FAILED CLOSED AT WEB SECURITY GATE

Immutable source:

```text
a485c80dc1eb36122791c629f92b247354b0ee09
```

Release run `32224834303` eventually completed Verify after one infrastructure-only Chromium/Ubuntu-mirror timeout, then failed closed at Web amd64 Trivy on inherited `libssl3t64 3.5.6-1~deb13u2 / CVE-2026-14456 / HIGH / fix_deferred`.

Recovery #179 retained the minimal Distroless Debian 13 runtime and introduced one exact-version reviewed OpenVEX statement only after runtime reachability guards prove the affected OpenSSL path is not in the production execution path. API receives no VEX; every unsuppressed Web `HIGH,CRITICAL` finding retains `exit-code=1`.

Failure record: [`v0.1.0-rc.5-release-failure-2026-08-19.md`](v0.1.0-rc.5-release-failure-2026-08-19.md).  
Security assessment: [`security/CVE-2026-14456-vex-assessment.md`](security/CVE-2026-14456-vex-assessment.md).

### `v0.1.0-rc.6` — FAILED CLOSED AT ARM64 RUNTIME-GUARD MATERIALIZATION

Immutable source:

```text
946bc19d6ca4a544c13d74f420fce12b1e5fe815
```

`Release / Verify` succeeded. `Release / Publish` built multi-architecture staging candidates and passed the amd64 Web runtime guard, then failed before Trivy when Docker attempted to materialize the same parent OCI-index digest as a second platform child:

```text
cannot overwrite digest sha256:715c4484cabfcac849bf3d2b9bbbede380f705fb9b666fef67287021a764b460
```

Recovery #180 reproduced the failure against the immutable rc.6 index and changed registry-mode runtime inspection to resolve the requested exact platform child manifest before local pull/create. Missing, ambiguous or malformed descriptors fail closed. That recovery was later validated by both real architecture guards in rc.7.

Failure record: [`v0.1.0-rc.6-release-failure-2026-08-19.md`](v0.1.0-rc.6-release-failure-2026-08-19.md).

### `v0.1.0-rc.7` — AUTOMATED RELEASE CONTRACT ACCEPTED

Immutable source:

```text
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

GitHub Release metadata is correct: `draft=false`, `prerelease=true`, exact tag/source identity. Release workflow `32293764820`, attempt 1, completed **SUCCESS**.

`Release / Verify` passed metadata, ancestry, pinned toolchain, repository verification, production Web build, responsive browser E2E and release-bundle verification.

`Release / Publish` passed:

- API/Web multi-architecture staging builds for `linux/amd64` + `linux/arm64`;
- exact Web VEX contract;
- Web runtime reachability guards on both architectures;
- all four API/Web architecture-specific Trivy HIGH/CRITICAL gates;
- four SPDX SBOM generations;
- exact-digest staging smoke;
- copy to final packages without rebuild and digest-identity assertion;
- exact final-package smoke;
- API/Web provenance attestations;
- prerelease OCI promotion;
- final manifest architecture verification;
- release metadata/checksum/evidence generation and upload.

The rc.6 platform-materialization failure did not recur. OCI `latest` was correctly skipped for the prerelease. The GitHub Release exposes the expected 14 evidence assets.

**Automated rc.7 verdict: ACCEPTED.** Issue #152 records the immutable evidence. Later `main` commits, including #177, do not alter or retag rc.7.

### Current stable-release gate — manual product canary

Stable `v0.1.0` is **not accepted or released**.

The remaining release gate is a manual product canary using immutable rc.7 release artifacts, not a rebuilt image or later `main` checkout. Required scenarios include:

- WeeklyPlan → Pantry → comparison → optimization;
- local draft save → reload → restore → clear;
- Recipe comparison;
- manual-list comparison;
- desktop/narrow layout sanity;
- restart/reload and safe unavailable/error states.

Record the exact rc.7 artifacts/digests and pass/fail per scenario in #152. Only satisfactory manual acceptance can unblock a stable `v0.1.0` decision.

## Known constraints / technical debt

- Chizhik offer mapping is blocked on ordinary-user-browser structural evidence plus independent price monetary-unit/scale evidence (#169).
- Chizhik availability remains `UNKNOWN` until explicit semantics are separately evidenced.
- Full production retailer coverage remains incomplete.
- Magnit production acquisition remains policy-blocked despite technical public-web feasibility.
- Kuper remains blocked on provider confirmation/access/reuse terms (#36).
- Ozon Fresh, Samokat, Lenta, VkusVill and additional canonical banners remain mandatory connectivity work.
- Real retailer checkout-economics evidence is not yet broadly available; unknown stays unknown.
- Explicit omit-all/never-buy semantics are deferred.
- Server-side saved-plan history/accounts/auth are not implemented.
- Analytics abstraction, feature flags and provider-health monitoring remain possible M5.2 candidates, not preselected work.
- Richer substitute/package optimization and multi-store split optimization are deferred.
- Native mobile remains future M6 work.
- rc.4 GitHub presentation metadata remains stale (`prerelease=false`).
- GitHub-hosted Ubuntu package mirrors can transiently stall Playwright `--with-deps`; CI contains bounded fail-closed handling, but persistent recurrence would justify a separate evidence-backed CI-environment reliability change rather than unbounded retries or silently dropping dependency installation.

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
25. Security exceptions must be machine-readable, narrowly scoped, evidence-backed and fail closed when their runtime assumptions change.
26. Multi-architecture registry evidence must resolve and verify the exact requested child manifest rather than relying on mutable or locally ambiguous parent-index materialization.
27. A CI rerun may confirm an infrastructure-only failure only when source identity is unchanged and no verification/security threshold is weakened.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 + generated TypeScript client;
- Next.js 16.3 / React 19.2;
- Testcontainers / Vitest / Testing Library / Playwright;
- Docker multi-stage production images + no-source-build Compose release topology;
- CodeQL / Dependency Review / Container Security / Release Contract / Release Bundle CI.
