# Changelog

All notable project changes are recorded here. Zakup Gotov is pre-release; this file summarizes user-visible behavior, architecture, security, retailer evidence and release engineering. Detailed RED/GREEN/review evidence belongs in linked acceptance/spec/release documents.

## [Unreleased]

### Added

#### Productization

- M5.1 adds one versioned same-origin browser-local WeeklyPlan/Pantry semantic input draft for repeat use without accounts or server persistence.
- Draft persistence excludes generated identities, comparison/economics/optimizer results and provider/acquisition/fulfillment evidence.
- Restore never auto-submits; storage failures fail closed without breaking explicit editing/submission.

#### Recipes / Weekly planning / Pantry

- First-class immutable Recipe domain with deterministic serving scaling and Recipe → ShoppingList conversion.
- Stateless Recipe and WeeklyPlan shopping/comparison boundaries with responsive browser flows.
- Ordered WeeklyPlan meal occurrences and request-scoped Pantry subtraction with explicit adjustment evidence.
- Automatic merging remains exact normalized requirement + canonical unit only; fuzzy/synonym/AI equivalence is never implicit.

#### Basket economics and optimization

- Explicit known/unknown delivery/service fees and minimum-order evidence.
- Separate merchandise subtotal, checkout-total knowledge, eligibility and comparability semantics.
- Deterministic cheapest comparable one-retailer basket selection with explicit exact ties.
- Server-owned WeeklyPlan/Pantry Optimization Preview and responsive Optimization UX.

#### Retailer connectivity

- Universal Retailer Connectivity remains a permanent invariant: every canonical retailer stays coverage work until at least one reproducible accepted acquisition path exists.
- Chromium MV3 Retailer Bridge provides minimal-permission sanitized first-party acquisition with deterministic fixtures and persistent-Chromium E2E.
- Perekrestok and Pyaterochka have accepted browser paths; browser lifecycle is hardened for SPA navigation/store changes.
- Magnit public-web feasibility is established, while recurring production acquisition remains policy-blocked pending affirmative permission or a supported/licensed path.

#### Chizhik connectivity

- D1 is **COMPLETE / ACCEPTED**: the normal user-browser MV3 Retailer Bridge is the selected acquisition architecture.
- D2 transport foundation (#169/#171) adds exact bounded store-scoped delivery search while keeping successful JSON opaque and automatic search/offer production disabled.
- D2 store-context binding (#173/#174) accepts only exactly one first-party browser-evidenced store intersecting the validated directory; missing/foreign/unknown/conflicting context fails closed.
- PR #177 adds an explicit user-invoked privacy-hardened D2 schema canary with no permission widening, exactly one fixed bounded search, a fixed reviewed candidate-field allowlist and persistent-Chromium E2E. It was refreshed after rc.7 automated acceptance, passed fresh exact-head 9/9 PR CI plus final security/privacy review, and was squash-merged as `9822659c1b43df978e191e6f7826775fc615926d`.
- The canary output is limited to sanitized HTTP metadata plus bounded allowlisted field/type structure; raw store/product/SKU/name/price/promotion/request/header/cookie/credential values are not emitted.
- Chizhik offer mapping remains blocked on ordinary-user-browser structural evidence plus independent monetary-unit/scale evidence. Availability remains `UNKNOWN` unless separately evidenced semantics are accepted.

#### Release security and multi-architecture recovery

- Added a machine-readable OpenVEX assessment for exactly `CVE-2026-14456` on `pkg:deb/debian/libssl3t64@3.5.6-1~deb13u2` with `not_affected / vulnerable_code_not_in_execute_path`.
- Added a VEX contract validator that fails if the reviewed statement is widened to another CVE, package or version.
- Added a final-image runtime guard that fails if Web enables `--experimental-quic` or if runtime Node/native addons dynamically link system `libssl`/`libcrypto`.
- Added strict OCI platform-child resolution for registry-mode multi-architecture runtime inspection; missing, ambiguous and malformed platform descriptors fail closed.
- Added a permanent regression test reproducing the rc.6 parent-index Docker collision and requiring distinct immutable child-manifest refs for sequential amd64/arm64 guards.
- Added durable release failure diagnostics that summarize already-created Trivy JSON findings into the Actions log when a fail-closed scan stops publication before release assets can be attached.
- Successful releases include the exact OpenVEX document in release assets and `SHA256SUMS`.
- Playwright Chromium installation in Web CI, Retailer Bridge CI and Release Verify uses bounded APT retries/timeouts plus descendant-safe process supervision so a timed-out privileged package-manager subtree cannot leak into a same-run retry and keep `dpkg` locks.

### Changed

- Current deterministic product phase remains **M5 Productization**; M5.2 remains intentionally unselected until release/manual-use evidence identifies the next constraint.
- `v0.1.0-rc.4`, `v0.1.0-rc.5` and `v0.1.0-rc.6` remain immutable historical **failed release-contract evidence**.
- `v0.1.0-rc.7` is the first current candidate with the full automated release contract accepted after the rc.5 security and rc.6 multi-architecture recovery work.
- Stable `v0.1.0` remains blocked on a separate manual product canary using immutable rc.7 artifacts.
- Normal mainline development has resumed after the rc.7 release freeze; later `main` commits do not change or retag immutable rc.7 source evidence.
- Web Container Security CI applies VEX only after its exact contract and final-image reachability assumptions pass; API scans remain unfiltered.
- Release CI applies the same Web VEX only after independent amd64 and arm64 runtime guards; registry-mode guards inspect exact platform child manifests derived from the immutable parent OCI index.

### Fixed

- Release planning does not reuse published prerelease tags for newer source.
- `v0.1.0-rc.4` metadata mismatch remains recorded as a failed historical candidate rather than being retroactively accepted.
- `v0.1.0-rc.5` exposed a real Web image security-gate blocker; recovery distinguishes package inventory from reachable vulnerable code through evidence-backed VEX rather than weakening Trivy severity.
- `v0.1.0-rc.6` exposed a Docker local image-store collision when one parent OCI-index digest was pulled sequentially for amd64 then arm64; runtime inspection now resolves content-addressed per-platform child manifests before local pull/create.
- The rc.6 platform-materialization defect did not recur in the real rc.7 publish path: both Web architecture runtime guards passed before Trivy.
- Failed release Trivy JSON evidence is no longer silently lost with the ephemeral runner.
- Chizhik store context can no longer be guessed from the first active store; exactly one browser-evidenced validated context is required.
- Chizhik schema evidence no longer generically enumerates arbitrary object keys; unrelated or dynamic-looking fields are excluded by the fixed reviewed allowlist.

### Security

- Trivy `CRITICAL,HIGH` and `exit-code=1` remain unchanged for all unsuppressed findings.
- The rc.5 Web finding was reproduced outside the release workflow as `libssl3t64 3.5.6-1~deb13u2 / CVE-2026-14456 / HIGH / fix_deferred` with zero Node-package findings.
- The production Node process does not enable experimental QUIC, and the final image guard proves Node plus native addons do not dynamically link system `libssl`/`libcrypto` before VEX is accepted.
- Full Node Debian 12 and Distroless Debian 12 alternatives were rejected after fresh scans showed broader actionable HIGH/CRITICAL surfaces; the project does not trade one non-reachable inherited finding for a larger vulnerability surface.
- Normal Web CI shows suppressed findings for auditability; a future package-version change is not silently covered because the VEX PURL is exact-version scoped.
- Chizhik canary production/E2E manifests retain `permissions: ["storage"]` with no host-permission widening; the canary is explicit-user-invocation only and fails closed on missing/foreign/unknown/conflicting context.
- Precise addresses, credentials, provider tokens, private headers and raw sensitive provider payloads remain excluded from ordinary evidence/logging.
- Published release tags, including failed candidates, are immutable historical evidence and are never repointed.

## [0.1.0-rc.7] — 2026-08-19 — AUTOMATED RELEASE CONTRACT ACCEPTED

Source:

```text
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

Release workflow: `32293764820`, attempt 1 — **SUCCESS**.

- Tag/source identity, GitHub prerelease metadata and release ancestry passed.
- Repository verification, production Web build, responsive browser E2E and production release-bundle verification passed.
- API and Web staging candidates were built for `linux/amd64` + `linux/arm64`.
- The exact Web VEX contract passed and both registry-mode architecture runtime guards passed.
- All four API/Web amd64/arm64 Trivy HIGH/CRITICAL gates passed.
- Four per-architecture SPDX SBOMs were produced.
- Digest-pinned staging and final Compose smoke passed.
- Verified staging indexes were copied to final packages without rebuild and digest identity was asserted.
- API and Web provenance attestations were created.
- `0.1.0-rc.7` prerelease OCI promotion succeeded while `latest` remained untouched.
- Final manifests, `release-verification.json`, `SHA256SUMS`, vulnerability reports, four SBOMs, Compose and the exact OpenVEX evidence were attached to the GitHub Release.
- The rc.6 `cannot overwrite digest ...` failure did not recur.
- Automated release verdict: **ACCEPTED**. A separate manual product canary remains required before stable `v0.1.0`.

## [0.1.0-rc.6] — 2026-08-19 — FAILED ARM64 RUNTIME-GUARD MATERIALIZATION

Source:

```text
946bc19d6ca4a544c13d74f420fce12b1e5fe815
```

- GitHub prerelease metadata and immutable tag/source were correct.
- `Release / Verify` passed metadata, ancestry, repository verification, bounded Chromium installation, browser E2E and production-bundle verification.
- Publish built API and Web staging indexes for `linux/amd64` + `linux/arm64`, validated the exact Web VEX contract and passed the amd64 runtime guard.
- Arm64 failed before Trivy because Docker could not materialize the same parent OCI-index digest as a second platform: `cannot overwrite digest sha256:715c4484...`.
- Recovery PR #180 reproduced the failure on a fresh GitHub runner, then proved the same immutable rc.6 index succeeds when amd64/arm64 are resolved to distinct child-manifest digests before pull/create.
- No completed release Trivy/SBOM/final-smoke/provenance/final OCI promotion or verified release assets were accepted from rc.6.
- Full record: `docs/v0.1.0-rc.6-release-failure-2026-08-19.md`.

## [0.1.0-rc.5] — 2026-08-19 — FAILED WEB SECURITY GATE

Source:

```text
a485c80dc1eb36122791c629f92b247354b0ee09
```

- GitHub prerelease metadata and immutable tag/source were correct.
- Release run `32224834303` attempt 1 timed out during external Ubuntu package retrieval while installing Chromium dependencies after repository verification had passed.
- Exact immutable Verify was rerun; attempt 2 passed metadata, ancestry, repository verification, browser E2E and production release-bundle verification.
- Publish built API/Web multi-arch staging images and passed API amd64/arm64 Trivy gates.
- Web amd64 then failed closed on `CVE-2026-14456` in inherited `libssl3t64 3.5.6-1~deb13u2`.
- No final `0.1.0-rc.5` OCI promotion, OCI `latest` mutation, final smoke, provenance or completed verified release evidence occurred.
- Full record: `docs/v0.1.0-rc.5-release-failure-2026-08-19.md`.

## [0.1.0-rc.4] — 2026-08-18 — FAILED RELEASE METADATA CONTRACT

Source:

```text
8a269288addcb4aa8ea3d0ce46608b650cbdb6dc
```

- GitHub Release was published with `prerelease=false` even though the tag is a SemVer prerelease.
- `Release / Verify` failed closed at the metadata contract before repository verification, browser E2E or production-bundle verification.
- `Release / Publish` never started, so rc.4 created no new GHCR promotion, Trivy/SBOM/provenance/final-smoke evidence and did not mutate OCI `latest`.
- The immutable rc.4 tag remains historical failed release evidence; its GitHub presentation can be corrected without reusing or moving the tag.
- Full record: `docs/v0.1.0-rc.4-release-failure-2026-08-18.md`.

## [0.1.0-rc.3] — 2026-08-18 — AUTOMATED RELEASE CONTRACT ACCEPTED

Source:

```text
d988b8c596a737326aeac67f74b6f65a6aaed3bf
```

- `Release / Verify` and `Release / Publish` completed successfully.
- API/Web multi-platform images were published for `linux/amd64` + `linux/arm64`.
- HIGH/CRITICAL Trivy gates passed.
- Per-architecture SPDX SBOMs were produced.
- Staging and final exact-digest Compose smoke tests passed.
- Verified digests were copied/promoted without rebuild and digest identity was preserved.
- API/Web provenance attestations were created.
- `0.1.0-rc.3` was promoted while OCI `latest` remained untouched.
- Release verification metadata, checksums, manifests, vulnerability reports and SBOMs were attached to the GitHub Release.
- Automated release contract accepted; separate manual product canary remained the product-acceptance gate.

## [0.1.0-rc.2] — 2026-08-17 — FAILED RELEASE CONTRACT

Source:

```text
19e6080cff464de9701e2f38ff6f06959037971a
```

- Release verification failed because the tag did not point to the required exact source after main moved.
- No release publication/promotion was accepted from this candidate.

## [0.1.0-rc.1] — 2026-08-17 — FAILED RELEASE CONTRACT

Source:

```text
69a4566963ef99fd5204e1f0bc924f6ab242e6f7
```

- Initial release candidate exposed release-contract and publication-path defects.
- Candidate remains immutable historical evidence and is not reused.
