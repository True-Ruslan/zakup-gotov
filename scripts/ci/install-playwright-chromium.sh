#!/usr/bin/env bash
set -euo pipefail

if (( $# == 0 )); then
  echo "usage: $0 <playwright-install-command> [args...]" >&2
  exit 64
fi

readonly max_attempts="${PLAYWRIGHT_INSTALL_ATTEMPTS:-2}"
readonly attempt_timeout_seconds="${PLAYWRIGHT_INSTALL_TIMEOUT_SECONDS:-360}"
readonly apt_conf="/etc/apt/apt.conf.d/80-zakup-gotov-ci-network"

if ! [[ "${max_attempts}" =~ ^[1-9][0-9]*$ ]]; then
  echo "PLAYWRIGHT_INSTALL_ATTEMPTS must be a positive integer" >&2
  exit 64
fi
if ! [[ "${attempt_timeout_seconds}" =~ ^[1-9][0-9]*$ ]]; then
  echo "PLAYWRIGHT_INSTALL_TIMEOUT_SECONDS must be a positive integer" >&2
  exit 64
fi
if ! command -v timeout >/dev/null 2>&1; then
  echo "GNU timeout is required for bounded Playwright installation" >&2
  exit 1
fi

if command -v apt-get >/dev/null 2>&1; then
  if ! command -v sudo >/dev/null 2>&1; then
    echo "sudo is required to configure bounded APT networking on this runner" >&2
    exit 1
  fi

  printf '%s\n' \
    'Acquire::Retries "3";' \
    'Acquire::http::Timeout "20";' \
    'Acquire::https::Timeout "20";' \
    | sudo tee "${apt_conf}" >/dev/null

  cleanup_apt_conf() {
    sudo rm -f "${apt_conf}" >/dev/null 2>&1 || :
  }
  trap cleanup_apt_conf EXIT
fi

for (( attempt = 1; attempt <= max_attempts; attempt++ )); do
  echo "Playwright Chromium install attempt ${attempt}/${max_attempts} (timeout ${attempt_timeout_seconds}s)"

  if timeout \
    --signal=TERM \
    --kill-after=15s \
    "${attempt_timeout_seconds}s" \
    "$@"; then
    echo "Playwright Chromium install completed on attempt ${attempt}."
    exit 0
  else
    rc=$?
  fi

  if (( attempt == max_attempts )); then
    echo "Playwright Chromium install failed after ${max_attempts} bounded attempt(s), exit=${rc}." >&2
    exit "${rc}"
  fi

  backoff_seconds=$(( attempt * 5 ))
  echo "Playwright Chromium install attempt ${attempt} failed with exit=${rc}; retrying in ${backoff_seconds}s." >&2
  sleep "${backoff_seconds}"
done
