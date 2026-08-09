#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

required_files=(
  "apps/api/Dockerfile"
  "apps/web/Dockerfile"
  "compose.release.yaml"
)

for path in "${required_files[@]}"; do
  if [[ ! -f "$path" ]]; then
    echo "release bundle contract missing required file: $path" >&2
    exit 1
  fi
done

docker version
docker compose version

API_IMAGE="${API_IMAGE:-zakup-gotov-api:ci}"
WEB_IMAGE="${WEB_IMAGE:-zakup-gotov-web:ci}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-release-ci-password}"
export API_IMAGE WEB_IMAGE POSTGRES_PASSWORD

on_exit() {
  local status=$?

  if (( status != 0 )); then
    echo "release bundle verification failed; collecting compose diagnostics" >&2
    docker compose -f compose.release.yaml ps --all || true
    docker compose -f compose.release.yaml logs --no-color || true
  fi

  docker compose -f compose.release.yaml down --volumes --remove-orphans >/dev/null 2>&1 || true
  exit "$status"
}
trap on_exit EXIT

docker build --tag "$API_IMAGE" --file apps/api/Dockerfile .
docker build --tag "$WEB_IMAGE" --file apps/web/Dockerfile .

docker compose -f compose.release.yaml config >/dev/null
docker compose -f compose.release.yaml up --detach --wait --wait-timeout 120

docker compose -f compose.release.yaml exec -T api \
  curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health/readiness \
  | grep -q '"status":"UP"'
curl --fail --silent --show-error http://127.0.0.1:3000/ | grep -q 'Закуп готов'
