#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPECTED_NODE="v$(tr -d '[:space:]' < .nvmrc)"
EXPECTED_PNPM="$(node -p "require('./package.json').packageManager.split('@')[1]")"

require_command() {
  local command_name="$1"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "ERROR: required command '$command_name' is not available." >&2
    exit 1
  fi
}

require_command java
require_command node
require_command pnpm
require_command docker

JAVA_VERSION_OUTPUT="$(java -version 2>&1)"
NODE_VERSION="$(node --version)"
PNPM_VERSION="$(pnpm --version)"

if ! grep -Eq 'version "25([.\"])' <<<"$JAVA_VERSION_OUTPUT"; then
  echo "ERROR: Java 25 is required." >&2
  echo "$JAVA_VERSION_OUTPUT" >&2
  exit 1
fi

if [[ "$NODE_VERSION" != "$EXPECTED_NODE" ]]; then
  echo "ERROR: Node $EXPECTED_NODE is required; found $NODE_VERSION." >&2
  exit 1
fi

if [[ "$PNPM_VERSION" != "$EXPECTED_PNPM" ]]; then
  echo "ERROR: pnpm $EXPECTED_PNPM is required; found $PNPM_VERSION." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker must be running; backend integration tests use Testcontainers." >&2
  exit 1
fi

echo "==> API verification (Java 25 + real PostgreSQL Testcontainers)"
./apps/api/mvnw -f apps/api/pom.xml --batch-mode --no-transfer-progress verify

echo "==> Frozen JavaScript workspace install"
pnpm install --frozen-lockfile

echo "==> OpenAPI generated-client drift check"
pnpm --filter @zakup-gotov/api-client check:generated

echo "==> API client typecheck/tests/build"
pnpm --filter @zakup-gotov/api-client typecheck
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build

echo "==> Web lint/typecheck/tests/build"
pnpm --filter web lint
pnpm --filter web typecheck
pnpm --filter web test
NEXT_TELEMETRY_DISABLED=1 pnpm --filter web build

echo "==> Verification complete"
