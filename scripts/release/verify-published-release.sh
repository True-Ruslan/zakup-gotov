#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: $0 <release-compose-file>" >&2
  exit 2
fi

COMPOSE_FILE="$1"
if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "release compose file does not exist: $COMPOSE_FILE" >&2
  exit 1
fi

if grep -Eq '^[[:space:]]*build:' "$COMPOSE_FILE"; then
  echo "published release compose must not contain build directives" >&2
  exit 1
fi

if grep -Eq '\$\{(API_IMAGE|WEB_IMAGE)' "$COMPOSE_FILE"; then
  echo "published release compose must pin application images directly" >&2
  exit 1
fi

mapfile -t application_refs < <(
  grep -E '^[[:space:]]+image: ghcr\.io/.+@sha256:[0-9a-f]{64}$' "$COMPOSE_FILE" \
    | sed -E 's/^[[:space:]]+image:[[:space:]]+//'
)

if [[ ${#application_refs[@]} -ne 2 ]]; then
  echo "published release compose must contain exactly two digest-pinned GHCR application images" >&2
  exit 1
fi

POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-release-smoke-password}"
WEB_PORT="${WEB_PORT:-3000}"
export POSTGRES_PASSWORD WEB_PORT

cleanup() {
  local status=$?

  if (( status != 0 )); then
    echo "published release verification failed; collecting compose diagnostics" >&2
    docker compose -f "$COMPOSE_FILE" ps --all || true
    docker compose -f "$COMPOSE_FILE" logs --no-color || true
  fi

  docker compose -f "$COMPOSE_FILE" down --volumes --remove-orphans >/dev/null 2>&1 || true
  exit "$status"
}
trap cleanup EXIT

docker compose -f "$COMPOSE_FILE" config >/dev/null
docker compose -f "$COMPOSE_FILE" pull
docker compose -f "$COMPOSE_FILE" up --detach --wait --wait-timeout 180

docker compose -f "$COMPOSE_FILE" exec -T api \
  curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health/readiness \
  | grep -q '"status":"UP"'

curl --fail --silent --show-error "http://127.0.0.1:${WEB_PORT}/" | grep -q 'Закуп готов'
