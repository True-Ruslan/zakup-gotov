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
export API_IMAGE WEB_IMAGE

cleanup() {
  docker compose -f compose.release.yaml down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker build --tag "$API_IMAGE" --file apps/api/Dockerfile .
docker build --tag "$WEB_IMAGE" --file apps/web/Dockerfile .

docker compose -f compose.release.yaml config >/dev/null
docker compose -f compose.release.yaml up --detach --wait --wait-timeout 120

curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health/readiness | grep -q '"status":"UP"'
curl --fail --silent --show-error http://127.0.0.1:3000/ | grep -q 'Закуп готов'
