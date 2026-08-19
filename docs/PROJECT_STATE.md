# Project State

Updated: 2026-08-19

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. Recipes, weekly meal plans or a manual grocery list become a locality-aware comparison of complete retailer baskets while preserving package semantics, provenance, freshness, uncertainty and truthful unavailable/incomplete states.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current product phase: **M5 — Productization**  
Immediate operational target: **complete rc.6 release-tooling recovery and validate `v0.1.0-rc.7` end to end**

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
- rc.5 web security-gate recovery — **IMPLEMENTED / MERGED** (#179);
- rc.6 multi-architecture VEX runtime-guard materialization recovery — **IMPLEMENTED IN RECOVERY PR #180; RC.7 VALIDATION NEXT**;
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

Draft PR #177 implements a user-invoked privacy-hardened schema canary. Exact head `c38173f3b15b66fa892534989e1aa2f51d98468d` passed 9/9 PR workflow groups on its previous baseline, but remains unmerged while release recovery is active. After rc.7 acceptance it must be refreshed against current `main` and reverified before merge.

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

Release run `32136955056` failed at `Release / Verify → Validate release metadata` because GitHub supplied `prerelease=false` for a SemVer prerelease tag. `Release / Publish` never started, so there was no GHCR promotion, OCI `latest` mutation, release SBOM/attestation or release smoke/evidence publication.

The historical GitHub release presentation still reports `prerelease=false`; this UI metadata should be corrected without moving/deleting the tag. rc.4 remains failed historical evidence regardless.

Failure record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — FAILED CLOSED AT WEB SECURITY GATE

Immutable source:

```text
a485c80dc1eb36122791c629f92b247354b0ee09
```

Release workflow `32224834303` had two Verify attempts. The first reached repository verification and then timed out while Ubuntu package mirrors stalled during Playwright Chromium dependency installation. The exact immutable Verify job was rerun; attempt 2 completed metadata, ancestry, repository verification, browser E2E and production release-bundle verification successfully.

`Release / Publish` then built API and web staging indexes for `linux/amd64` + `linux/arm64`; both API HIGH/CRITICAL Trivy gates passed. The web amd64 gate failed closed on the exact staging digest:

```text
ghcr.io/true-ruslan/zakup-gotov-staging-web@sha256:8483c5e5a17d9208964230f715b312475b780cb001d7aafe684eea0f6a0fd171
```

Fresh ordinary Container Security CI reproduced the finding on the unchanged production image:

```text
package: libssl3t64
version: 3.5.6-1~deb13u2
CVE: CVE-2026-14456
severity: HIGH
status: fix_deferred
```

No Node/application package vulnerability was reported. Because the web gate failed, all downstream SBOM completion, staging/final exact-digest smoke, digest-preserving final copy, provenance, `0.1.0-rc.5` OCI promotion, `latest`, manifests/checksums and release assets were skipped. The release contract therefore failed closed correctly.

Failure record: [`v0.1.0-rc.5-release-failure-2026-08-19.md`](v0.1.0-rc.5-release-failure-2026-08-19.md).

### rc.5 security root cause and recovery

CVE-2026-14456 affects an OpenSSL QUIC server-listener path. The production web runtime does not enable Node experimental QUIC. A final-image guard proves before scanning that:

- final Entrypoint/Cmd does not contain `--experimental-quic`;
- `/nodejs/bin/node` does not dynamically link system `libssl` or `libcrypto`;
- no final native `*.node` addon dynamically links system `libssl` or `libcrypto`.

Two base-image substitutions were tested and rejected: full Node Debian 12 expanded the runtime surface to 29 HIGH/CRITICAL findings; Distroless Debian 12 exposed seven HIGH/CRITICAL OpenSSL findings including a CRITICAL fixable issue.

Recovery PR #179 kept the minimal Distroless Debian 13 runtime and added a reviewed OpenVEX statement scoped only to:

```text
CVE-2026-14456
pkg:deb/debian/libssl3t64@3.5.6-1~deb13u2
status: not_affected
justification: vulnerable_code_not_in_execute_path
```

The VEX is guarded by an exact contract validator and final-image reachability checks. API scans receive no VEX. Web scans retain `CRITICAL,HIGH` and `exit-code=1` for every unsuppressed finding. Normal CI exposes suppressed findings for auditability; release CI guards both architectures, includes the VEX in checksummed release evidence, and prints concise Trivy JSON evidence to logs if a future security gate fails before assets can be attached.

Security assessment: [`security/CVE-2026-14456-vex-assessment.md`](security/CVE-2026-14456-vex-assessment.md).

### `v0.1.0-rc.6` — FAILED CLOSED AT ARM64 RUNTIME-GUARD MATERIALIZATION

Immutable source:

```text
946bc19d6ca4a544c13d74f420fce12b1e5fe815
```

GitHub prerelease metadata and tag/source were correct. `Release / Verify` completed successfully. `Release / Publish` built both multi-architecture staging candidates, validated the exact web VEX contract and passed the real `linux/amd64` runtime guard.

The following `linux/arm64` runtime guard failed before Trivy while pulling the same parent OCI index under a second platform:

```text
cannot overwrite digest sha256:715c4484cabfcac849bf3d2b9bbbede380f705fb9b666fef67287021a764b460
```

Recovery PR #180 reproduced the failure against the immutable rc.6 staging index on a fresh GitHub runner. The root cause is the local Docker image-store boundary: the same parent index digest cannot be materialized sequentially as two different platform children under one digest reference.

Registry-mode runtime inspection now resolves the exact requested child manifest from the immutable parent index before pull/create and verifies the materialized OS/architecture. Missing, ambiguous or malformed descriptors fail closed. The same rc.6 index then passed both real architecture guards:

```text
linux/amd64 -> sha256:9eb77c8f70331def690af0e20e2ae2160ef4ef37d2666826499ddb968fa41d35
linux/arm64 -> sha256:387275fa31e3b06a39264533d3f7409646af600079aea04d1216518bef5ca0c5
```

The arm64 child contained `@img/sharp-linux-arm64@0.35.3` and passed the same no-system-OpenSSL ELF checks. The VEX and Trivy policy remain unchanged.

Because Publish stopped at the runtime guard, rc.6 produced no completed release vulnerability gates, SBOM/final-smoke/provenance, final OCI promotion, `latest` mutation or verified release assets.

Failure record: [`v0.1.0-rc.6-release-failure-2026-08-19.md`](v0.1.0-rc.6-release-failure-2026-08-19.md).

### `v0.1.0-rc.7` — NEXT OPERATIONAL TARGET

Issue: #152. Recovery implementation: #180.

Required sequence:

1. complete #180 on one final exact head with all normal PR workflow groups green;
2. require Release Contract CI to pass the permanent parent-index collision regression and strict OCI platform-resolver tests;
3. require Container Security CI to prove local final-image guard behavior remains intact;
4. review the final diff and merge #180 only after all gates pass;
5. record the resulting exact `main` SHA and verify all normal exact-main push workflow groups;
6. confirm `v0.1.0-rc.7` tag/release is absent immediately before publication;
7. publish one GitHub prerelease targeting only that exact SHA with **Set as a pre-release enabled**;
8. require `Release / Verify` and `Release / Publish` to complete end to end;
9. inspect exact child-manifest resolution for both web architectures, all four Trivy gates, SPDX SBOMs, staging/final exact-digest smoke, copy-without-rebuild digest identity, provenance, manifests, OpenVEX/checksums and package visibility;
10. verify OCI `latest` remains untouched;
11. run the manual product canary from immutable rc.7 artifacts.

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
- rc.4 GitHub presentation metadata remains stale (`prerelease=false`).

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

## Platform baseline

- Java 25 / Spring Boot 4.1 / Spring MVC virtual threads / Spring Modulith;
- PostgreSQL 18 / Flyway / jOOQ;
- OpenAPI 3.1 + generated TypeScript client;
- Next.js 16.3 / React 19.2;
- Testcontainers / Vitest / Testing Library / Playwright;
- Docker multi-stage production images + no-source-build Compose release topology;
- CodeQL / Dependency Review / Container Security / Release Contract / Release Bundle CI.
