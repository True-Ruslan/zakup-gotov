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

`Container Security CI` runs in ordinary read-only CI and scans the exact production API/web images with unchanged Trivy `HIGH,CRITICAL` fail-closed policy. No `.trivyignore`, `ignore-unfixed`, VEX suppression or severity downgrade is used to make the gate green.

## Versioned release contract

`Release Contract CI` verifies without write permissions:

- strict stable/prerelease SemVer tags;
- consistency between SemVer prerelease state and the GitHub Release prerelease flag;
- prereleases never publish OCI `latest`;
- deterministic final/staging GHCR names and digest-pinned release Compose references;
- executable release helper modes;
- immutable Action pins and pinned QEMU/BuildKit helper images;
- approved trust order: build → scan → staging smoke → final copy → final smoke → attestation → version promotion → optional stable `latest` → evidence upload.

`.github/workflows/release.yml` triggers only for `release: published`.

### `Release / Verify`

Read-only verification requires the tagged commit to be contained in `main`, validates release metadata, reruns repository verification, production browser E2E and the production container-bundle smoke test.

### `Release / Publish`

Only after Verify succeeds does the write-capable job receive package/attestation permissions. Its intended order is:

1. validate metadata and derive names;
2. authenticate to GHCR;
3. build/push API and web staging indexes for `linux/amd64` and `linux/arm64` with provenance/SBOM;
4. scan every platform candidate for `HIGH`/`CRITICAL` vulnerabilities;
5. produce SPDX JSON SBOM evidence;
6. smoke-test exact staging digests;
7. copy verified indexes **without rebuild** into final packages, preserving digest identity;
8. smoke-test exact final-package digests;
9. create provenance attestations;
10. promote SemVer tags from the already verified digests without rebuild;
11. move OCI `latest` only for stable releases;
12. verify final manifest architectures;
13. attach release evidence/checksums to the GitHub Release.

Staging packages remain a private trust boundary. Final package visibility is a separate distribution decision.

## Runtime validation history

### `v0.1.0-rc.1` — 2026-08-09

The first real release event proved metadata/main-ancestry validation, complete repository verification and responsive browser testing, then failed before container verification because release helper scripts were stored without executable Git mode. The defect was regression-tested and corrected before rc.2.

### `v0.1.0-rc.2` — 2026-08-09

`Release / Verify` passed completely. `Release / Publish` authenticated to GHCR, set up QEMU/Buildx and published API/web multi-platform staging indexes. It then correctly failed closed at the first Trivy gate on pgJDBC `42.7.11` / `CVE-2026-54291` (`HIGH`, fixed in `42.7.12`).

Subsequent mainline work upgraded pgJDBC, moved the web final runtime to distroless Node 24 Debian 13/non-root and added ordinary Container Security CI under the same fail-closed policy.

### `v0.1.0-rc.3` — historical prerelease

Immutable source:

```text
d988b8c596a737326aeac67f74b6f65a6aaed3bf
```

The tag must not be moved, deleted or reused for later source.

### `v0.1.0-rc.4` — 2026-08-18 — FAILED METADATA GATE

Immutable source:

```text
8a269288addcb4aa8ea3d0ce46608b650cbdb6dc
```

Release workflow:

```text
run 32136955056
```

The tag/source was correct, but the GitHub Release was published with `prerelease=false` even though the tag is a SemVer prerelease. `Release / Verify → Validate release metadata` invoked the contract with:

```text
--tag "v0.1.0-rc.4"
--prerelease "false"
```

and failed with:

```text
ValueError: GitHub prerelease flag must match the SemVer prerelease state
```

All later verify steps were skipped. `Release / Publish` was skipped entirely, so rc.4 created no new GHCR promotion, OCI `latest` mutation, SBOM, attestation, staging/final release smoke or evidence/checksum assets.

Because GitHub received `prerelease=false`, the rc.4 release object was temporarily exposed as the repository's `Latest release`. That is GitHub presentation metadata only; it is not evidence of OCI `latest` mutation. The existing release presentation should be corrected to **Set as a pre-release** without moving or deleting its tag. rc.4 nevertheless remains a failed release-contract attempt.

Detailed record: [`v0.1.0-rc.4-release-failure-2026-08-18.md`](v0.1.0-rc.4-release-failure-2026-08-18.md).

## Next validation — `v0.1.0-rc.5`

The next attempt must be a **new immutable prerelease** from a newly selected exact verified `main` SHA:

```text
v0.1.0-rc.5
```

Issue #152 is the operational gate.

Before publication:

1. merge canonical rc.4-failure/rc.5 documentation through fresh CI/review;
2. record the resulting exact `main` SHA in #152;
3. verify all normal push workflow groups against that exact SHA;
4. confirm `v0.1.0-rc.5` is absent;
5. in the GitHub release form, explicitly enable **Set as a pre-release**;
6. target the exact selected SHA, not floating `main`;
7. publish rather than save a draft.

A successful rc.5 must pass both release jobs end to end and prove:

- exact metadata/main ancestry;
- repository verification and production browser E2E;
- API/web `linux/amd64` + `linux/arm64` staging indexes;
- unchanged Trivy `HIGH,CRITICAL` gate;
- SPDX SBOMs;
- staging exact-digest Compose smoke;
- copy-without-rebuild final promotion with digest identity;
- final exact-digest smoke;
- provenance attestations;
- prerelease OCI `0.1.0-rc.5` tags without OCI `latest` mutation;
- final manifest architecture checks;
- attached evidence/checksum assets;
- package visibility evidence.

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
