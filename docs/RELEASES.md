# Releases

Zakup Gotov is still **pre-release**. The production container topology is exercised in normal CI. The versioned GHCR publishing workflow has now received its first real `release: published` execution, but it is not yet considered end-to-end runtime-proven because `v0.1.0-rc.1` exposed and stopped on a release-helper executable-mode defect before the publish job began.

## Verified container bundle

The repository has an executable production-container baseline:

- `apps/api/Dockerfile` builds the Spring Boot API and runs it as a non-root user;
- `apps/web/Dockerfile` builds the Next.js standalone server and runs it as a non-root user;
- `compose.release.yaml` starts PostgreSQL 18.4, API, and web without Compose `build:` directives;
- PostgreSQL data is stored in a named volume mounted at the PostgreSQL 18-compatible `/var/lib/postgresql` path;
- API waits for PostgreSQL health;
- web waits for API readiness and its healthcheck verifies both its own HTTP surface and the configured API over the Compose network;
- only the web service is published to the host by default;
- `Release Bundle CI` builds both application images, starts the complete bundle, waits for health, smoke-tests API readiness inside the API container, and verifies the public web page;
- failing bundle verification prints Compose status and logs before cleanup.

Run the same executable contract locally with a running Docker daemon and Docker Compose v2:

```bash
./scripts/verify-release-bundle.sh
```

## Versioned release contract

The repository contains two separate verification layers for versioned releases.

### Read-only release contract

`Release Contract CI` runs on ordinary pull requests with only `contents: read`. It verifies:

- strict release tags in the form `vMAJOR.MINOR.PATCH` or `vMAJOR.MINOR.PATCH-prerelease`;
- consistency between SemVer prerelease state and the GitHub Release `prerelease` flag;
- prereleases can never publish the `latest` tag;
- repository-scoped GHCR image names are normalized to lowercase;
- unverified candidates use separate `*-staging-api` / `*-staging-web` package names rather than final package names;
- final-package pre-version copies use deterministic `verified-<source-sha>` tags;
- application images in a release-specific Compose file must be GHCR references pinned by `sha256` digest;
- release helper shell scripts retain executable modes in a clean Git checkout;
- the release workflow uses immutable full-SHA action pins;
- QEMU and BuildKit helper images are themselves digest-pinned;
- build, scan, staging smoke, final-package copy, final-package smoke, attestation, version promotion, optional `latest`, and release-asset upload remain in the approved trust order;
- `release.yml` remains syntactically parseable YAML.

This keeps security-sensitive release semantics testable without granting package or OIDC write permissions to pull-request CI.

### Published-release workflow

`.github/workflows/release.yml` is triggered only by a **published GitHub Release**. It has two jobs with distinct trust boundaries.

`Release / Verify` remains read-only and requires the tagged commit to be contained in `main`. It reruns repository verification, responsive production-browser tests, and the production container-bundle smoke test.

Only after that succeeds, `Release / Publish` receives narrowly scoped release permissions and performs this sequence:

1. validate release metadata and derive lowercase final/staging GHCR package names;
2. authenticate to GHCR with the workflow token;
3. build and push candidate API/web image indexes for `linux/amd64` and `linux/arm64` into dedicated staging packages;
4. generate BuildKit provenance and SBOM attestations during the build;
5. scan both target platforms of both staging images for `HIGH` and `CRITICAL` vulnerabilities;
6. generate per-platform SPDX JSON SBOM files as release evidence;
7. render a temporary Compose file with the exact staging image digests and smoke-test that registry-pulled bundle;
8. copy those verified indexes **without rebuild** into the final API/web packages under `verified-<source-sha>` tags and require the copied digest to remain identical;
9. render the release Compose file using the final package names with those exact digests and smoke-test the final-package bundle;
10. create GitHub provenance attestations for the final-package image digests;
11. only now create the SemVer version tags from the already verified final-package digests, again without rebuild;
12. move `latest` only when the release is stable;
13. verify the final manifests contain both target Linux architectures;
14. attach the digest-pinned Compose file, manifests, vulnerability reports, SBOMs, verification metadata, and checksums to the GitHub Release.

The staging packages are intentionally separate from the final packages so an unverified candidate is never placed in a future public release package. Staging packages must remain private. Final package visibility is a separate product/distribution setting and is verified independently after first successful publication.

Docker Actions are pinned to immutable commit SHAs. The QEMU binfmt helper image and BuildKit daemon image are also pinned by digest so the release builder does not silently inherit mutable `latest`/`buildx-stable-1` dependencies.

## First runtime validation: `v0.1.0-rc.1`

The first real prerelease was published on 2026-08-09 as `v0.1.0-rc.1`, marked as a GitHub prerelease and targeting commit `d3066258915542c2488d9a3277680b2cc478d611`.

The real `release: published` workflow fired and proved the following on the release-event runner:

- release metadata validation passed, including `publish_latest=false`;
- the release commit was confirmed to be contained in `main`;
- Java 25, Node 24.18.1, and pnpm 11.4.0 setup passed;
- the complete `./scripts/verify.sh` suite passed;
- production web build passed;
- responsive Playwright coverage passed **4/4**;
- the workflow reached production container-bundle verification.

The run then failed before executing the bundle because `scripts/verify-release-bundle.sh` was stored in Git as mode `100644`. A clean Linux checkout therefore returned `Permission denied` / exit 126 when the workflow invoked the script directly. The same Git-tree audit found `scripts/release/verify-published-release.sh` was also `100644`, so the later publish smoke would have had the same defect.

`Release / Publish` was skipped because `Release / Verify` failed. Therefore rc.1 produced **no verified GHCR publication path evidence**: no candidate/final application images, release vulnerability reports, release SBOM assets, final-package attestations, SemVer OCI tags, or `latest` update are claimed from that run.

The fix is intentionally at the source rather than in the workflow command: both release helpers are stored as `100755`, and `Release Contract CI` now has a clean-checkout regression test for their executable modes. The test was observed RED against the old `100644` modes before the mode-only fix made it GREEN.

## Next validation

The next runtime validation must be a **new prerelease** from corrected `main`; do not rerun rc.1 because its immutable tag points at the defective source commit. The expected next tag is:

```text
v0.1.0-rc.2
```

It must remain a GitHub prerelease. A successful rc.2 must:

- pass both `Release / Verify` and `Release / Publish`;
- create and verify both target architectures;
- pass all vulnerability and digest-pinned staging/final smoke gates;
- attach the expected evidence assets;
- preserve `latest` untouched;
- keep staging packages private;
- allow final package visibility to be verified independently.

Do not create a stable release until at least one prerelease has exercised the complete workflow successfully and its attached evidence has been inspected.

## GHCR visibility

Package publication and package visibility are separate concerns.

- staging packages are an internal release-engineering boundary and must remain private;
- final API/web packages may be made public only as a deliberate distribution decision;
- a public source repository is not treated as proof that a newly created GHCR package is anonymously pullable.

After the first successful prerelease, verify both staging-package privacy and final-package visibility explicitly. Until final-package visibility is proven, documentation must not promise anonymous public pulls.

## Manual local start

Build the images:

```bash
docker build -t zakup-gotov-api:local -f apps/api/Dockerfile .
docker build -t zakup-gotov-web:local -f apps/web/Dockerfile .
```

Then provide runtime values and start the bundle:

```bash
export API_IMAGE='zakup-gotov-api:local'
export WEB_IMAGE='zakup-gotov-web:local'
export POSTGRES_PASSWORD='local-development-only'
export WEB_PORT='3000'

docker compose -f compose.release.yaml up -d --wait
```

Open `http://localhost:3000`.

The API remains internal to the Compose network. The web container reaches it through `API_BASE_URL=http://api:8080`.

Stop containers while retaining database data:

```bash
docker compose -f compose.release.yaml down
```

Remove containers **and** the PostgreSQL volume only when destructive data removal is intended:

```bash
docker compose -f compose.release.yaml down --volumes
```

Never commit a real database password or a populated local `.env` file.
