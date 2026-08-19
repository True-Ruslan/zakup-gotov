# Releases

Zakup Gotov is still **pre-release**. The release contract is deliberately fail-closed: every failed candidate remains historical evidence and receives a new prerelease number rather than a moved/reused tag.

## Verified container baseline

The production topology is built from the checked-in API/web Dockerfiles and `compose.release.yaml` without Compose `build:` directives. PostgreSQL health gates API startup, API readiness gates web startup, and only web is published to the host by default.

`Release Bundle CI` builds the production images, starts the complete PostgreSQL → API → web topology, waits for health, smoke-tests API readiness and verifies the public web page.

Local bundle verification:

```bash
./scripts/verify-release-bundle.sh
```

## Pre-release security gate

`Container Security CI` scans the exact production API/web images with Trivy `HIGH,CRITICAL` + `exit-code=1`.

API has no vulnerability suppression. Web has one machine-readable OpenVEX assessment for `CVE-2026-14456` scoped to the exact inherited package `pkg:deb/debian/libssl3t64@3.5.6-1~deb13u2`. It is accepted only after final-image guards prove that experimental QUIC is disabled and runtime Node/native addons do not dynamically link system `libssl`/`libcrypto`. Every other HIGH/CRITICAL finding remains fail-closed.

Normal web CI enables suppressed-result output so the assessed CVE remains visible. The release workflow repeats the runtime guard for both `linux/amd64` and `linux/arm64` before applying the same VEX. Registry-mode guards resolve the requested architecture to its exact child-manifest digest from the immutable parent OCI index before pulling the image, preventing cross-platform local-image-store collisions while preserving content-addressed evidence.

Assessment: [`security/CVE-2026-14456-vex-assessment.md`](security/CVE-2026-14456-vex-assessment.md).

## Versioned release contract

`Release Contract CI` verifies without write permissions:

- strict stable/prerelease SemVer tags;
- consistency between SemVer prerelease state and GitHub Release prerelease flag;
- prereleases never publish OCI `latest`;
- deterministic final/staging GHCR names and digest-pinned release Compose references;
- immutable Action pins and pinned QEMU/BuildKit helpers;
- approved trust order: build → guard/scan → staging smoke → final copy → final smoke → attestation → version promotion → optional stable `latest` → evidence upload.

`.github/workflows/release.yml` triggers only for `release: published`.

### `Release / Verify`

Read-only verification requires the tagged commit to be contained in `main`, validates release metadata, reruns repository verification, production browser E2E and the production container-bundle smoke test.

### `Release / Publish`

Only after Verify succeeds does the write-capable job receive package/attestation permissions. Its intended order is:

1. validate metadata and derive names;
2. authenticate to GHCR;
3. build/push API and web staging indexes for `linux/amd64` and `linux/arm64`;
4. validate the web VEX contract and final-image runtime assumptions on both architectures using exact platform child manifests;
5. scan every platform candidate for unsuppressed `HIGH`/`CRITICAL` vulnerabilities;
6. produce SPDX JSON SBOM evidence;
7. smoke-test exact staging digests;
8. copy verified indexes **without rebuild** into final packages, preserving digest identity;
9. smoke-test exact final-package digests;
10. create provenance attestations;
11. promote SemVer tags from already verified digests;
12. move OCI `latest` only for stable releases;
13. verify final manifest architectures;
14. attach release evidence/checksums, including the exact OpenVEX document.

If a vulnerability scan fails, already-created Trivy JSON reports are summarized into the durable Actions log so root-cause evidence is not lost when downstream release-asset upload is skipped.

## Runtime validation history

### `v0.1.0-rc.1` — 2026-08-09

First real release event exposed executable-mode defects in release helper scripts. They were fixed before rc.2.

### `v0.1.0-rc.2` — 2026-08-09

Verify passed; Publish failed closed on pgJDBC `42.7.11` / `CVE-2026-54291` (`HIGH`, fixed in `42.7.12`). Mainline subsequently upgraded pgJDBC and retained the security gate.

### `v0.1.0-rc.3` — historical prerelease

Immutable source: `d988b8c596a737326aeac67f74b6f65a6aaed3bf`. Do not move/delete/reuse the tag.

### `v0.1.0-rc.4` — 2026-08-18 — FAILED METADATA GATE

Immutable source: `8a269288addcb4aa8ea3d0ce46608b650cbdb6dc`.

Run `32136955056` failed before write-capable publication because GitHub supplied `prerelease=false` for a SemVer prerelease. No final package/evidence/latest side effects occurred.

Detailed record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

### `v0.1.0-rc.5` — 2026-08-19 — FAILED WEB SECURITY GATE

Immutable source: `a485c80dc1eb36122791c629f92b247354b0ee09`.

Run `32224834303` eventually completed Verify on attempt 2 after an infrastructure-only Chromium/Ubuntu-mirror timeout on attempt 1. Publish built API/web multi-arch staging candidates and passed both API HIGH/CRITICAL gates, then failed at web amd64.

Fresh normal CI reproduced exactly one OS-level HIGH finding on the unchanged Distroless Debian 13 web runtime:

```text
libssl3t64 3.5.6-1~deb13u2
CVE-2026-14456
status: fix_deferred
```

No final `0.1.0-rc.5` OCI promotion, `latest`, final smoke, provenance or release evidence assets occurred.

Detailed record: [`v0.1.0-rc.5-release-failure-2026-08-19.md`](v0.1.0-rc.5-release-failure-2026-08-19.md).

### `v0.1.0-rc.6` — 2026-08-19 — FAILED ARM64 RUNTIME-GUARD MATERIALIZATION

Immutable source: `946bc19d6ca4a544c13d74f420fce12b1e5fe815`.

GitHub prerelease metadata and tag/source were correct. `Release / Verify` completed successfully. `Release / Publish` then built both multi-architecture staging candidates, validated the exact web VEX contract and passed the real amd64 runtime guard.

The following arm64 runtime guard failed before Trivy with:

```text
cannot overwrite digest sha256:715c4484cabfcac849bf3d2b9bbbede380f705fb9b666fef67287021a764b460
```

The immutable parent web OCI index had been pulled sequentially as amd64 and arm64 through the same local digest reference. Recovery PR #180 reproduced the failure on a fresh GitHub runner and proved the fix against the exact same rc.6 index by resolving distinct immutable child manifests before pull/create:

```text
linux/amd64 -> sha256:9eb77c8f70331def690af0e20e2ae2160ef4ef37d2666826499ddb968fa41d35
linux/arm64 -> sha256:387275fa31e3b06a39264533d3f7409646af600079aea04d1216518bef5ca0c5
```

Both runtime guards then passed, including the real arm64 `@img/sharp-linux-arm64@0.35.3` ELF inspection. The OpenVEX statement and Trivy fail-closed policy were not widened or weakened.

No rc.6 Trivy-release completion, SBOM completion, staging/final smoke, final OCI promotion, provenance, `latest` mutation or verified release assets occurred.

Detailed record: [`v0.1.0-rc.6-release-failure-2026-08-19.md`](v0.1.0-rc.6-release-failure-2026-08-19.md).

## Next validation — `v0.1.0-rc.7`

Issue #152 and recovery PR #180 are the operational gate.

The recovery changes only registry materialization for multi-architecture runtime inspection. It keeps the exact reviewed OpenVEX assessment and unchanged Trivy `CRITICAL,HIGH` + `exit-code=1` policy.

Before publication:

1. #180 must finish on one exact head with all normal PR workflow groups SUCCESS;
2. Release Contract CI must pass the permanent multi-platform collision regression and fail-closed OCI resolver tests;
3. Container Security CI must prove the local final-image VEX/runtime guard still passes before the web scan;
4. merge #180 only after fresh review/CI;
5. verify all normal exact-main push workflow groups;
6. record the exact verified `main` SHA in #152;
7. confirm `v0.1.0-rc.7` is absent;
8. create one GitHub prerelease targeting that exact SHA with **Set as a pre-release enabled**.

A successful rc.7 must prove:

- exact metadata/main ancestry;
- repository verification and production browser E2E;
- API/web `linux/amd64` + `linux/arm64` staging indexes;
- parent-index → exact child-manifest resolution and VEX/runtime guards on both web architectures;
- API scans without VEX and web scans with only the reviewed suppression;
- unchanged fail-closed `HIGH,CRITICAL` behavior for every other finding;
- SPDX SBOMs;
- staging exact-digest Compose smoke;
- digest-preserving copy-without-rebuild final promotion;
- final exact-digest smoke;
- provenance attestations;
- prerelease OCI `0.1.0-rc.7` tags without OCI `latest` mutation;
- final manifest architecture checks;
- attached manifests, vulnerability reports, SBOMs, OpenVEX and checksums.

Do not create stable `v0.1.0` until at least one prerelease completes the entire workflow and its manual product canary is accepted.

## Manual product canary after successful prerelease

Run from immutable release artifacts, not a source checkout:

- WeeklyPlan → Pantry → comparison → optimization;
- local draft save → reload → restore → clear;
- Recipe comparison;
- manual-list comparison;
- desktop/narrow layout sanity;
- restart/reload and safe unavailable/error states.

M5.2 remains intentionally unselected until this evidence is reviewed.

## Manual local start

Build images:

```bash
docker build -t zakup-gotov-api:local -f apps/api/Dockerfile .
docker build -t zakup-gotov-web:local -f apps/web/Dockerfile .
```

Start:

```bash
export API_IMAGE='zakup-gotov-api:local'
export WEB_IMAGE='zakup-gotov-web:local'
export POSTGRES_PASSWORD='local-development-only'
export WEB_PORT='3000'
docker compose -f compose.release.yaml up -d --wait
```

Stop while retaining database data:

```bash
docker compose -f compose.release.yaml down
```
