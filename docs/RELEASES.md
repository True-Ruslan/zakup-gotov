# Releases

Zakup Gotov is still **pre-release**. The production container topology is exercised in normal CI, and two real GitHub prereleases have now exercised progressively more of the versioned GHCR pipeline. The workflow is not yet considered end-to-end runtime-proven: `v0.1.0-rc.1` exposed a clean-checkout executable-mode defect, while `v0.1.0-rc.2` reached multi-platform publishing and then correctly failed closed at the first container vulnerability gate.

## Verified container bundle

The repository has an executable production-container baseline:

- `apps/api/Dockerfile` builds the Spring Boot API and runs it as a non-root user;
- `apps/web/Dockerfile` builds the Next.js standalone server and runs its final stage on distroless Node 24 Debian 13 as non-root;
- the final web runtime does not require a shell or package manager;
- `compose.release.yaml` starts PostgreSQL 18.4, API, and web without Compose `build:` directives;
- PostgreSQL data uses the PostgreSQL 18-compatible `/var/lib/postgresql` volume path;
- PostgreSQL health gates API startup, API readiness gates web startup;
- web health verifies both its own HTTP surface and the configured API over the Compose network;
- only web is published to the host by default;
- `Release Bundle CI` builds both application images, starts the complete topology, waits for health, smoke-tests API readiness inside the API container, and verifies the public web page;
- failing bundle verification prints Compose status/logs before cleanup.

Run the same contract locally with Docker and Compose v2:

```bash
./scripts/verify-release-bundle.sh
```

## Pre-release container-security gate

`Container Security CI` now runs in ordinary read-only CI on pull requests, `main`, and a daily schedule. It:

1. builds the exact production API and web Dockerfiles with `--pull`;
2. scans each resulting production image with the same Trivy `vuln` policy used by release publication;
3. fails on `HIGH` or `CRITICAL` findings (`exit-code: 1`).

It intentionally has only `contents: read`; it cannot publish packages or request OIDC credentials. The release workflow still performs its stronger per-platform scans on both `linux/amd64` and `linux/arm64` staging candidates before promotion.

No `.trivyignore`, `ignore-unfixed`, VEX suppression, severity downgrade, or scanner bypass was added to make this gate green.

## Versioned release contract

### Read-only release contract

`Release Contract CI` verifies without write permissions:

- strict tags `vMAJOR.MINOR.PATCH` or `vMAJOR.MINOR.PATCH-prerelease`;
- consistency between SemVer prerelease state and the GitHub Release prerelease flag;
- prereleases can never publish `latest`;
- normalized lowercase final/staging GHCR package names;
- unverified candidates remain in separate staging packages;
- final-package pre-version copies use deterministic `verified-<source-sha>` tags;
- release Compose application references must be GHCR `sha256` digests;
- release helper scripts retain executable Git modes;
- release Actions use immutable full-SHA pins;
- QEMU and BuildKit helper images are digest-pinned;
- build → scan → staging smoke → final-package copy → final smoke → attestation → version promotion → optional `latest` → evidence upload remains the approved trust order;
- `release.yml` remains valid YAML.

### Published-release workflow

`.github/workflows/release.yml` triggers only for a **published GitHub Release**.

`Release / Verify` stays read-only and requires the tagged commit to be contained in `main`. It reruns repository verification, production browser tests, and the production container-bundle smoke test.

Only after verification succeeds does `Release / Publish` receive `contents: write`, `packages: write`, `attestations: write`, and `id-token: write`. Its intended sequence is:

1. validate release metadata and derive final/staging image names;
2. authenticate to GHCR;
3. build/push API and web staging indexes for `linux/amd64` and `linux/arm64` with BuildKit provenance/SBOM;
4. scan both platforms of both staging images for `HIGH`/`CRITICAL` vulnerabilities;
5. generate per-platform SPDX JSON SBOM evidence;
6. render and smoke-test a Compose bundle pinned to exact staging digests;
7. copy verified indexes **without rebuild** into final packages under `verified-<source-sha>`, requiring digest identity;
8. render and smoke-test a Compose bundle pinned to exact final-package digests;
9. create GitHub provenance attestations for final digests;
10. create SemVer tags from the already verified digests, without rebuild;
11. move `latest` only for stable releases;
12. verify final manifests contain both target architectures;
13. attach Compose, manifests, vulnerability reports, SBOMs, verification metadata, and checksums to the GitHub Release.

Staging packages are a private trust boundary. Final package visibility is a separate distribution decision and must be verified independently.

## Runtime validation 1: `v0.1.0-rc.1`

`v0.1.0-rc.1` was published on 2026-08-09 as a GitHub prerelease targeting `d3066258915542c2488d9a3277680b2cc478d611`.

Proven before failure:

- release metadata validation including `publish_latest=false`;
- release source contained in `main`;
- Java 25 / Node 24.18.1 / pnpm 11.4.0 setup;
- complete `./scripts/verify.sh`;
- production web build;
- responsive Playwright **4/4**.

It then failed before container verification because `scripts/verify-release-bundle.sh` was stored as Git mode `100644`. The later publish helper was found with the same mode. `Release / Publish` was skipped, so no GHCR publication evidence is attributed to rc.1.

PR #28 added a regression test that was observed RED against the old modes, then changed both helpers to `100755` without changing their script contents.

## Runtime validation 2: `v0.1.0-rc.2`

`v0.1.0-rc.2` was published from corrected `main` at `184751e164f199fdc5262cf77ea86c931daf59f7`.

This run advanced the proof boundary substantially:

- **`Release / Verify` passed completely**, including production container-bundle verification;
- `Release / Publish` started with its separate write-capable permission set;
- GHCR authentication, QEMU, and Buildx setup passed;
- API and web multi-platform staging candidate indexes were built and pushed for `linux/amd64` + `linux/arm64`;
- the workflow reached the first real release vulnerability scan.

The release then stopped fail-closed at `API / amd64` Trivy scanning. The concrete blocker was:

- `org.postgresql:postgresql` `42.7.11`;
- `CVE-2026-54291`;
- severity `HIGH`;
- fixed version `42.7.12`.

The API's Ubuntu runtime OS itself produced zero HIGH/CRITICAL findings at that gate.

### TDD reproduction and broader runtime finding

Before changing production code, PR #29 introduced the ordinary `Container Security CI` and observed both production images fail under the unchanged HIGH/CRITICAL policy.

The web failure showed that the previous `node:24.18.1-bookworm-slim` final runtime carried:

- 22 Debian HIGH/CRITICAL findings;
- 7 npm/runtime-library HIGH/CRITICAL findings;
- no HIGH/CRITICAL findings in the actual Next.js/React application packages at that threshold.

Rather than suppressing scanner results, PR #29:

- updated pgJDBC to `42.7.12`;
- moved only the final web runtime to `gcr.io/distroless/nodejs24-debian13:nonroot`;
- removed shell/package-manager requirements from web startup;
- changed Docker/Compose health execution to the distroless Node binary;
- retained the same fail-closed Trivy policy.

The same security workflow then passed for API and web, and `Release Bundle CI` passed the complete PostgreSQL → API → web topology on the exact final PR head.

### What rc.2 does not prove

Because `rc.2` stopped before staging smoke/promotion, it does **not** prove:

- staging digest-pinned Compose smoke;
- final-package copy or final-package digest smoke;
- GitHub final-image attestations;
- SemVer OCI version tags;
- final manifest verification;
- attached release SBOM/scan/checksum evidence;
- final GHCR package visibility.

No `latest` update is attributed to rc.2.

## Next validation: `v0.1.0-rc.3`

The next validation must be a **new immutable prerelease** from current verified `main`:

```text
v0.1.0-rc.3
```

Do not rerun or retarget rc.1/rc.2.

A successful rc.3 must:

- pass both `Release / Verify` and `Release / Publish` end to end;
- verify both target architectures for API and web;
- pass every release Trivy and SBOM step;
- pass staging and final-package exact-digest Compose smoke tests;
- create final-package GitHub provenance attestations;
- promote the exact verified digests to the prerelease SemVer tags without rebuild;
- attach all expected release evidence and checksums;
- demonstrate that prerelease publication leaves `latest` untouched;
- keep staging packages private;
- allow final package visibility/anonymous pull behavior to be verified independently.

Do not create a stable release until at least one prerelease has completed the full workflow and its evidence has been inspected.

## GHCR visibility

Package publication and package visibility are separate concerns:

- staging packages must remain private;
- final API/web packages may be public only as a deliberate distribution decision;
- public repository visibility is not evidence that a new GHCR package is anonymously pullable.

After the first successful prerelease, verify both staging privacy and final-package visibility explicitly before documenting anonymous pull instructions.

## Manual local start

Build the images:

```bash
docker build -t zakup-gotov-api:local -f apps/api/Dockerfile .
docker build -t zakup-gotov-web:local -f apps/web/Dockerfile .
```

Then start the bundle:

```bash
export API_IMAGE='zakup-gotov-api:local'
export WEB_IMAGE='zakup-gotov-web:local'
export POSTGRES_PASSWORD='local-development-only'
export WEB_PORT='3000'

docker compose -f compose.release.yaml up -d --wait
```

Open `http://localhost:3000`.

The API remains internal; web reaches it through `API_BASE_URL=http://api:8080`.

Stop while retaining database data:

```bash
docker compose -f compose.release.yaml down
```

Remove containers **and** the PostgreSQL volume only when destructive removal is intended:

```bash
docker compose -f compose.release.yaml down --volumes
```

Never commit a real database password or populated local `.env` file.
