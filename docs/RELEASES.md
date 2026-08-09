# Releases

Zakup Gotov is still **pre-release**. This document separates the container bundle that is already verified in CI from the versioned GHCR release pipeline that is still being implemented.

## Verified container bundle

The repository now has an executable production-container baseline:

- `apps/api/Dockerfile` builds the Spring Boot API and runs it as a non-root user;
- `apps/web/Dockerfile` builds the Next.js standalone server and runs it as a non-root user;
- `compose.release.yaml` starts PostgreSQL 18.4, API, and web without Compose `build:` directives;
- PostgreSQL data is stored in a named volume mounted at the PostgreSQL 18-compatible `/var/lib/postgresql` path;
- API waits for PostgreSQL health;
- web waits for API readiness and its healthcheck verifies both its own HTTP surface and the configured API over the Compose network;
- only the web service is published to the host by default;
- `Release Bundle CI` builds both application images, starts the complete bundle, waits for health, smoke-tests API readiness inside the API container, and verifies the public web page;
- failing bundle verification prints Compose status and logs before cleanup.

This proves the production container topology and startup contract. It does **not** mean a versioned public release is available yet.

## Local release-bundle verification

Prerequisite: a running Docker daemon with Docker Compose v2.

Run the same executable contract as CI:

```bash
./scripts/verify-release-bundle.sh
```

The script builds disposable local images named `zakup-gotov-api:ci` and `zakup-gotov-web:ci`, starts the Compose project with a test-only password, verifies it, and removes its containers and disposable test volume on exit.

To use custom local image names:

```bash
API_IMAGE=my-api:test \
WEB_IMAGE=my-web:test \
POSTGRES_PASSWORD='local-only-password' \
./scripts/verify-release-bundle.sh
```

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

The API remains internal to the Compose network in this bundle. The web container reaches it through `API_BASE_URL=http://api:8080`.

Stop containers while retaining database data:

```bash
docker compose -f compose.release.yaml down
```

Remove containers **and** the PostgreSQL volume only when destructive data removal is intended:

```bash
docker compose -f compose.release.yaml down --volumes
```

Never commit a real database password or a populated local `.env` file.

## Versioned release contract — not implemented yet

The next release-engineering slice must complete the approved public-release contract:

1. trigger authoritative packaging from a published GitHub Release;
2. build and publish `linux/amd64` and `linux/arm64` API/web images to GHCR;
3. scan the application images for actionable vulnerabilities;
4. produce SBOM and provenance/attestation evidence;
5. resolve immutable OCI digests;
6. generate a release-specific Compose file pinned to those application digests;
7. start and smoke-test that exact published bundle in CI;
8. attach the tested Compose file and verification metadata to the GitHub Release;
9. allow stable releases to move `latest` while ensuring prereleases never do.

Until those steps are implemented and verified, do not describe the repository as having a consumable versioned release.
