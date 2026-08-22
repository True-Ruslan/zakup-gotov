# Project State

Updated: 2026-08-22

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current product phase: **M5 — Productization**  
Verified executable/release-tooling baseline: `a550501dd7fdaabcb51c2faf83a9bbbf4c96c731` (#189 merge; later documentation-only commits do not change executable behavior)  
Accepted stable release: **`v0.1.0`**  
Accepted stable product source: `b754f5193f852db0312011f3f6c3ec6c7dd22eb2` (immutable `v0.1.0-rc.7` source)  
Immediate technical target: **ordinary-user-browser Chizhik D2 evidence for #169**.

The current `main` branch and the accepted stable product source are intentionally different. Stable `v0.1.0` promoted the already accepted immutable rc.7 application artifacts **without rebuilding them**. Later `main` work adds Chizhik canary functionality and release-evidence/promotion hardening; it does not retroactively change `v0.1.0`.

The product/core and retailer-connectivity tracks remain independent. Technical retailer reachability is not production approval, merged transport code is not automatically an accepted offer provider, and release/tooling evidence does not authorize unsupported retailer semantics.

## Milestone status

- M0 Product & Integration Discovery — **COMPLETE**;
- M1 Shopping Core — **COMPLETE / ACCEPTED**;
- M2 Recipes — **COMPLETE / ACCEPTED**;
- M3 Weekly Planning / Pantry — **COMPLETE / ACCEPTED**;
- M4 Basket Optimization — **COMPLETE / ACCEPTED**;
- pre-release Web runtime hardening — **COMPLETE / ACCEPTED** (#150);
- M5.1 Private local WeeklyPlan draft — **COMPLETE / ACCEPTED** (#148/#149);
- Retailer Bridge persistent-session / SPA / store-change lifecycle hardening — **COMPLETE / ACCEPTED** (#54/#153);
- Chizhik D1 user-browser transport decision — **COMPLETE / ACCEPTED** (#167/#168);
- Chizhik D2 fixed store-scoped search transport — **IMPLEMENTED / MERGED, OFFER MAPPING DISABLED** (#169/#171);
- Chizhik D2 browser-evidenced store-context binding — **COMPLETE / ACCEPTED** (#173/#174);
- Chizhik D2 user-invoked sanitized schema canary — **IMPLEMENTED / MERGED, LIVE EVIDENCE PENDING** (#177);
- rc.5 Web security-gate recovery — **COMPLETE / VALIDATED IN RC.7** (#179);
- rc.6 multi-architecture runtime-guard materialization recovery — **COMPLETE / VALIDATED IN RC.7** (#180);
- `v0.1.0-rc.7` automated release contract — **ACCEPTED** (#152);
- `v0.1.0-rc.7` manual product canary — **ACCEPTED** (#152);
- stable `v0.1.0` — **RELEASED / ACCEPTED** (#152, stable promotion run `32384418147`);
- M5.2 — **INTENTIONALLY UNSELECTED**. The accepted manual canary exposed evidence-harness defects, not a new product-runtime constraint, so there is still no evidence-backed reason to preselect accounts, analytics, feature flags, provider health or saved history.

## Accepted product/core baseline

M1–M4 are accepted. The deterministic product supports canonical shopping requirements, exact/normalized matching with explicit ambiguity, package-aware single-store baskets, Recipes, WeeklyPlan/Pantry composition, truthful one-retailer checkout economics and deterministic cheapest comparable basket selection.

M5.1 adds one versioned same-origin semantic WeeklyPlan/Pantry draft. Generated identities, comparison/economics/optimizer output and provider evidence never become browser-local authority; restore never implies submission.

The accepted rc.7 / stable `v0.1.0` manual product evidence covers:

- WeeklyPlan → Pantry → comparison → optimization;
- private local draft save, reload/restore and clear;
- Recipe comparison;
- manual-list comparison;
- desktop and 390 px narrow layout without horizontal overflow;
- fail-closed API-unavailable behavior;
- API/Web restart and recovered comparison.

The no-comparable-candidates state seen in the canary is intentional: when retailer checkout assessments are absent, the product displays truthful `Расчёт оформления недоступен.` states and does not invent a minimum.

Permanent product rules remain unchanged: fuzzy/AI equivalence is never implicit, unknown availability/economics stay unknown, incomplete baskets cannot masquerade as complete, and browser UI renders server-owned decisions instead of recomputing domain behavior.

## Retailer connectivity

### Perekrestok / Pyaterochka

Accepted first-party browser-bridge acquisition exists. Long-lived SPA/store-change sessions are hardened through event-driven lifecycle handling, fresh-context gating, stale/in-flight rejection and revision-safe writes without permission widening.

### Magnit

Technical public-web coverage is **AVAILABLE_PUBLIC_WEB**, while recurring production acquisition remains **BLOCKED** by project operating policy pending affirmative permission or a supported/licensed path.

### Chizhik

D1 is accepted: an ordinary user browser can access the fixed `/api/v1/shops/` directory, while stock GitHub-hosted Chromium is not the selected acquisition environment. The architecture is the normal user-browser MV3 Retailer Bridge; stealth, proxy rotation, fingerprint spoofing and credential/header/cookie extraction remain out of scope.

D2 transport (#171) is merged but successful JSON remains opaque to production. D2 store context (#174) is accepted only when exactly one current-session first-party delivery resource produces a `sap_id` that intersects the validated store directory; missing/foreign/unknown/conflicting context fails closed.

PR #177 passed fresh exact-head 9/9 PR workflow groups plus final security/privacy review and was squash-merged as:

```text
9822659c1b43df978e191e6f7826775fc615926d
```

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

A real ordinary-browser #169 run reproduced `CHIZHIK_D2 status=MISSING_CONTEXT` even after the official Chizhik search UI loaded product cards for `кола`, disproving the assumption that normal search interaction necessarily exposes one accepted store-scoped resource path to the content script. Rather than guessing a new store-context source or broadening the accepted route, PR #192 (squash-merged `940b2423cb1f467f673f2c4ba967145dd7c7c074`) added privacy-safe route-family diagnostics: on `MISSING_CONTEXT`, the canary evidence now includes one fixed `CHIZHIK_D2_DIAG` line of `SEEN`/`NOT_SEEN` booleans over the Resource Timing entries already in memory, with no raw URL, store ID, or credential ever persisted or rendered.

A follow-up automated-browser reconnaissance session against `app.chizhik.club` (run by Claude, not an ordinary-user session; see invariant 20) observed a successful `/delivery/api/catalog/v1/categories/inout?store_id=...` request before the session was blocked by the site's own WAF (`403 Forbidden`, consistent with D1's finding that automated Chromium is not the selected acquisition environment). That route shape does not match the accepted `v2|v3/stores/{sap_id}/...` contract and was not previously distinguished from the generic "other version" flag. PR #193 (squash-merged `7f68cb5404df77e33c08f350bdd02f033de45099`) added a dedicated `storeScopedCategoriesInoutSeen` / `store_categories_inout` diagnostic flag for this shape, verified by unit and Playwright E2E coverage before merge. This is diagnostic-only: `categories/inout?store_id=` is not accepted as a resolvable store context, and no offer/availability mapping was enabled. Ordinary-user-browser confirmation of whether this flag actually fires on a real MISSING_CONTEXT run is still outstanding.

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

## Verified executable/release-hardening baseline

The latest verified executable/release-tooling baseline is:

```text
a550501dd7fdaabcb51c2faf83a9bbbf4c96c731
```

Later documentation-only synchronization commits do not change executable behavior and therefore do not replace this baseline.

Relevant post-rc.7 mainline changes are intentionally separated from the stable product source:

- #177 / `9822659...` — Chizhik user-invoked sanitized D2 schema canary;
- #183 / `4fb9a67...` — owner-gated immutable rc.7 manual canary evidence harness;
- #184 / `c57ae80...` — runner-context recovery before canary startup;
- #185 / `0e7aad7...` — fail-closed rc.7 release-asset checksum verification by exact `dist/<asset>` paths;
- #187 / `da35a5c...` — canary assertions aligned with the immutable candidate's truthful no-assessment state;
- #188 / `72ee6a1...` — owner-gated no-rebuild stable-promotion workflow;
- #189 / `a550501...` — strict OCI alias verification by raw manifest equivalence;
- #190 / `262eb59...` — documentation-only synchronization of stable `v0.1.0` acceptance.

PR #189 exact head `0b5e4bb85fd6467d5eb7ad98278eb4b6785ee811` was `behind=0`, had no review threads and passed all 9 expected PR workflow groups before merge: Release Contract, Release Bundle, Dependency Review, Contract, Container Security, Retailer Bridge, API, CodeQL and Web CI.

## Release history and acceptance evidence

### `v0.1.0-rc.3` — historical automated-accepted prerelease

Immutable source: `d988b8c596a737326aeac67f74b6f65a6aaed3bf`. Do not move, delete or reuse the tag.

### `v0.1.0-rc.4` — FAILED CLOSED AT METADATA GATE

Immutable source: `8a269288addcb4aa8ea3d0ce46608b650cbdb6dc`.

Release run `32136955056` failed at `Release / Verify → Validate release metadata` because GitHub supplied `prerelease=false` for a SemVer prerelease tag. `Release / Publish` never started. rc.4 remains failed historical evidence.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — FAILED CLOSED AT WEB SECURITY GATE

Immutable source: `a485c80dc1eb36122791c629f92b247354b0ee09`.

Release run `32224834303` failed closed at Web amd64 Trivy on inherited `libssl3t64 3.5.6-1~deb13u2 / CVE-2026-14456 / HIGH / fix_deferred` after repository verification. Recovery #179 retained the minimal Distroless Debian 13 runtime and introduced one exact-version reviewed OpenVEX statement only after runtime reachability guards prove the affected OpenSSL path is not in the production execution path. API receives no VEX; every unsuppressed Web `HIGH,CRITICAL` finding retains `exit-code=1`.

Failure record: [`v0.1.0-rc.5-release-failure-2026-08-19.md`](v0.1.0-rc.5-release-failure-2026-08-19.md).  
Security assessment: [`security/CVE-2026-14456-vex-assessment.md`](security/CVE-2026-14456-vex-assessment.md).

### `v0.1.0-rc.6` — FAILED CLOSED AT ARM64 RUNTIME-GUARD MATERIALIZATION

Immutable source: `946bc19d6ca4a544c13d74f420fce12b1e5fe815`.

`Release / Verify` succeeded. Publish failed before Trivy while materializing the same parent OCI-index digest as a second platform child. Recovery #180 reproduced the defect and changed registry-mode runtime inspection to resolve the requested exact platform child manifest before local pull/create. Missing, ambiguous or malformed descriptors fail closed. rc.7 later validated both architecture guards.

Failure record: [`v0.1.0-rc.6-release-failure-2026-08-19.md`](v0.1.0-rc.6-release-failure-2026-08-19.md).

### `v0.1.0-rc.7` — AUTOMATED + MANUAL ACCEPTED

Immutable source:

```text
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

Automated release workflow `32293764820`, attempt 1, completed **SUCCESS**. It accepted:

- exact release metadata/source identity and ancestry;
- repository verification, production Web build and responsive browser E2E;
- API/Web multi-architecture staging builds for `linux/amd64` + `linux/arm64`;
- exact Web VEX contract and both architecture runtime reachability guards;
- all four API/Web architecture-specific Trivy HIGH/CRITICAL gates;
- four SPDX SBOMs;
- exact-digest staging and final bundle smokes;
- staging-to-final copy without rebuild and digest-identity assertion;
- API/Web provenance attestations;
- prerelease OCI promotion;
- final manifest verification, checksums and release evidence.

Accepted immutable application indexes:

```text
API  sha256:1c5c4a104fee295cd579b0e69a23b508a297b1eb931a45c0ce71d8b1791e54e1
Web  sha256:5bc236f3f304dffe29f54921f5a2bbf27d3df67c18714d4cc268d6d25bafce68
```

Manual product canary run `32359437905` also completed **SUCCESS**. Evidence harness source: `da35a5cb7ef46c64d266cd29731167eaa4cbefb4`. Artifact id `9402970517`; artifact digest `sha256:158afcff6c270526823ad372cf883cb5eeaf723eacfacab4d2a46fb68c625c25`.

All normal product scenarios, API-unavailable fail-closed state and recovered/restarted state passed. The evidence archive and screenshots were manually reviewed and accepted in #152.

Two earlier canary failures were evidence-harness defects rather than immutable rc.7 product defects:

- #185 fixed downloaded release basename verification against exact `dist/<asset>` checksum entries;
- #187 removed stale assertions that required checkout-assessment rows when the immutable product correctly had no assessments and displayed `Расчёт оформления недоступен.`.

### Stable `v0.1.0` — RELEASED / ACCEPTED

Stable `v0.1.0` was promoted from accepted immutable rc.7 evidence by owner-gated workflow run:

```text
32384418147
```

Promotion/review harness on `main`:

```text
a550501dd7fdaabcb51c2faf83a9bbbf4c96c731
```

Stable source/tag target remained the accepted product source:

```text
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

GitHub Release id: `373829773`. Final workflow verification established `draft=false`, `prerelease=false`, and `refs/tags/v0.1.0` resolving exactly to the accepted source commit.

The stable-promotion trust chain is intentionally content-addressed:

1. accepted manual canary run/artifact/comment identities are verified;
2. immutable rc.7 GitHub Release identity, exact asset set, server-side asset digests and `SHA256SUMS` are verified;
3. `release-verification.json` must name the exact accepted source, API/Web digests, two platforms and unchanged `CRITICAL,HIGH` gate;
4. immutable `image@sha256` refs are the promotion sources — mutable tags are never promotion sources;
5. release manifests, immutable registry indexes and the rc tag are compared fail closed by raw OCI index semantics;
6. stable `0.1.0` and `latest` are created from the accepted immutable refs **without rebuild**;
7. all four final API/Web `0.1.0`/`latest` indexes are re-read and must remain exactly descriptor-equivalent to the accepted immutable indexes; only top-level optional `annotations` may differ;
8. the digest-pinned Compose bundle is started again and Postgres/API/Web must become healthy;
9. stable GitHub Release is published last, then its tag and server-side asset digests are verified again.

The first stable-promotion attempt (`32362833963`) failed closed **before any stable mutation** because the original preflight incorrectly required a mutable rc tag to preserve the source index's top-level descriptor digest. #189 replaced that assumption with strict raw-index equivalence while preserving immutable digest refs as the only promotion source. The accepted run `32384418147` then passed every gate.

**Stable `v0.1.0` verdict: ACCEPTED.** No application image was rebuilt for stable publication.

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
- GitHub-hosted Ubuntu package mirrors can transiently stall Playwright `--with-deps`; CI contains bounded fail-closed handling. Persistent recurrence should be treated as a separate CI-environment reliability problem rather than solved by unbounded retries or dropped dependency installation.

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
26. Multi-architecture registry evidence must resolve and verify the exact requested child manifest rather than relying on locally ambiguous parent-index materialization.
27. A CI rerun may confirm an infrastructure-only failure only when source identity is unchanged and no verification/security threshold is weakened.
28. Stable promotion is anchored to accepted immutable `image@sha256` references; mutable tags are aliases/evidence only and never the source of truth for promotion.
29. A stable OCI alias may differ only in explicitly permitted top-level optional annotation metadata; ordered child descriptors and every other verified index field must remain exact.
30. A stable GitHub Release is published only after immutable source evidence, stable draft assets, OCI promotion, post-promotion verification and exact-digest runtime smoke have passed.

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 + generated TypeScript client;
- Next.js 16.3 / React 19.2;
- Testcontainers / Vitest / Testing Library / Playwright;
- Docker multi-stage production images + no-source-build Compose release topology;
- CodeQL / Dependency Review / Container Security / Release Contract / Release Bundle CI.
