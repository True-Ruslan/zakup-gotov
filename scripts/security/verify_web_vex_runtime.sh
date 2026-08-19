#!/usr/bin/env bash
set -euo pipefail

image_ref="${1:?usage: verify_web_vex_runtime.sh IMAGE_REF [PLATFORM] [SOURCE]}"
platform="${2:-linux/amd64}"
source_mode="${3:-registry}"
resolved_image_ref="${image_ref}"

python3 scripts/security/validate_web_vex.py
command -v readelf >/dev/null

platform_os="${platform%%/*}"
platform_rest="${platform#*/}"
platform_arch="${platform_rest%%/*}"
if [[ -z "${platform_os}" || -z "${platform_arch}" || "${platform_os}" == "${platform}" ]]; then
  echo "web VEX runtime guard failed: PLATFORM must be OS/ARCH[/VARIANT]" >&2
  exit 64
fi

case "${source_mode}" in
  local)
    if ! docker image inspect "${image_ref}" >/dev/null 2>&1; then
      echo "web VEX runtime guard failed: local image is missing: ${image_ref}" >&2
      exit 1
    fi
    ;;
  registry)
    if [[ ! "${image_ref}" =~ ^.+@sha256:[0-9a-f]{64}$ ]]; then
      echo "web VEX runtime guard failed: registry image must be pinned by a sha256 index digest" >&2
      exit 1
    fi

    index_json="$(docker buildx imagetools inspect --raw "${image_ref}")"
    child_digest="$(
      printf '%s' "${index_json}" \
        | python3 scripts/security/resolve_oci_platform.py "${platform}"
    )"
    image_repository="${image_ref%@sha256:*}"
    if [[ -z "${image_repository}" ]]; then
      echo "web VEX runtime guard failed: cannot derive repository from ${image_ref}" >&2
      exit 1
    fi

    resolved_image_ref="${image_repository}@${child_digest}"
    echo "web VEX runtime platform (${platform}): ${image_ref} -> ${resolved_image_ref}"
    docker pull --platform "${platform}" "${resolved_image_ref}" >/dev/null
    ;;
  *)
    echo "web VEX runtime guard failed: SOURCE must be local or registry" >&2
    exit 64
    ;;
esac

actual_platform="$(
  docker image inspect --format '{{.Os}}/{{.Architecture}}' "${resolved_image_ref}"
)"
expected_platform="${platform_os}/${platform_arch}"
if [[ "${actual_platform}" != "${expected_platform}" ]]; then
  echo "web VEX runtime guard failed: expected ${expected_platform}, resolved ${actual_platform}" >&2
  exit 1
fi

workdir="$(mktemp -d)"
container_id=""
cleanup() {
  if [[ -n "${container_id}" ]]; then
    docker rm -f "${container_id}" >/dev/null 2>&1 || true
  fi
  rm -rf "${workdir}"
}
trap cleanup EXIT

container_id="$(docker create --platform "${platform}" "${resolved_image_ref}")"
container_config="$(docker inspect --format '{{json .Config.Entrypoint}} {{json .Config.Cmd}}' "${container_id}")"

echo "web VEX runtime config (${platform}, ${source_mode}): ${container_config}"
if grep -F -- '--experimental-quic' <<<"${container_config}" >/dev/null; then
  echo "web VEX runtime guard failed: --experimental-quic is enabled" >&2
  exit 1
fi
if ! grep -F -- '/nodejs/bin/node' <<<"${container_config}" >/dev/null; then
  echo "web VEX runtime guard failed: expected Distroless Node entrypoint is missing" >&2
  exit 1
fi

docker cp "${container_id}:/nodejs/bin/node" "${workdir}/runtime-node"
docker cp "${container_id}:/app" "${workdir}/app"

check_elf() {
  local binary="$1"
  local needed
  needed="$(readelf -d "${binary}" 2>/dev/null | grep 'NEEDED' || true)"
  echo "ELF dependencies: ${binary#${workdir}/}"
  printf '%s\n' "${needed}"
  if grep -E 'Shared library: \[(libssl|libcrypto)(\.so|[^]]*)\]' <<<"${needed}" >/dev/null; then
    echo "web VEX runtime guard failed: ${binary#${workdir}/} dynamically links system OpenSSL" >&2
    exit 1
  fi
}

check_elf "${workdir}/runtime-node"

native_count=0
while IFS= read -r -d '' addon; do
  native_count=$((native_count + 1))
  check_elf "${addon}"
done < <(find "${workdir}/app" -type f -name '*.node' -print0)

echo "web VEX runtime guard OK (${platform}, ${source_mode}): Node + ${native_count} native addon(s) do not link libssl/libcrypto and QUIC is not enabled"
