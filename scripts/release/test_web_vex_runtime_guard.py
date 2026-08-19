from __future__ import annotations

import json
import os
import pathlib
import subprocess
import tempfile
import textwrap
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
RESOLVER = ROOT / "scripts/security/resolve_oci_platform.py"
GUARD = ROOT / "scripts/security/verify_web_vex_runtime.sh"

AMD64_DIGEST = "sha256:" + "a" * 64
ARM64_DIGEST = "sha256:" + "b" * 64
PARENT_DIGEST = "sha256:" + "c" * 64
PARENT_REF = f"ghcr.io/example/zakup-gotov-web@{PARENT_DIGEST}"
AMD64_REF = f"ghcr.io/example/zakup-gotov-web@{AMD64_DIGEST}"
ARM64_REF = f"ghcr.io/example/zakup-gotov-web@{ARM64_DIGEST}"


def make_index(*, duplicate_arm64: bool = False) -> dict[str, object]:
    manifests: list[dict[str, object]] = [
        {
            "mediaType": "application/vnd.oci.image.manifest.v1+json",
            "digest": AMD64_DIGEST,
            "size": 1,
            "platform": {"os": "linux", "architecture": "amd64"},
        },
        {
            "mediaType": "application/vnd.oci.image.manifest.v1+json",
            "digest": ARM64_DIGEST,
            "size": 1,
            "platform": {"os": "linux", "architecture": "arm64"},
        },
        {
            "mediaType": "application/vnd.oci.image.manifest.v1+json",
            "digest": "sha256:" + "d" * 64,
            "size": 1,
            "platform": {"os": "unknown", "architecture": "unknown"},
            "annotations": {"vnd.docker.reference.type": "attestation-manifest"},
        },
    ]
    if duplicate_arm64:
        manifests.append(
            {
                "mediaType": "application/vnd.oci.image.manifest.v1+json",
                "digest": "sha256:" + "e" * 64,
                "size": 1,
                "platform": {"os": "linux", "architecture": "arm64"},
            }
        )
    return {
        "schemaVersion": 2,
        "mediaType": "application/vnd.oci.image.index.v1+json",
        "manifests": manifests,
    }


class OciPlatformResolverTest(unittest.TestCase):
    def _resolve(self, index: object, platform: str) -> subprocess.CompletedProcess[str]:
        return subprocess.run(
            ["python3", str(RESOLVER), platform],
            input=json.dumps(index),
            text=True,
            capture_output=True,
            cwd=ROOT,
            check=False,
        )

    def test_resolves_each_runtime_platform_and_ignores_attestation_descriptor(self):
        index = make_index()

        amd64 = self._resolve(index, "linux/amd64")
        arm64 = self._resolve(index, "linux/arm64")

        self.assertEqual(amd64.returncode, 0, amd64.stderr)
        self.assertEqual(amd64.stdout.strip(), AMD64_DIGEST)
        self.assertEqual(arm64.returncode, 0, arm64.stderr)
        self.assertEqual(arm64.stdout.strip(), ARM64_DIGEST)

    def test_fails_closed_when_platform_is_missing(self):
        result = self._resolve(make_index(), "linux/s390x")

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("found 0", result.stderr)

    def test_fails_closed_when_platform_is_ambiguous(self):
        result = self._resolve(make_index(duplicate_arm64=True), "linux/arm64")

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("found 2", result.stderr)

    def test_fails_closed_for_malformed_child_digest(self):
        index = make_index()
        manifests = index["manifests"]
        assert isinstance(manifests, list)
        manifests[1]["digest"] = "sha256:not-a-digest"

        result = self._resolve(index, "linux/arm64")

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("invalid sha256 digest", result.stderr)


class WebVexRegistryMultiarchIntegrationTest(unittest.TestCase):
    def test_sequential_platform_guards_use_distinct_child_manifest_refs(self):
        with tempfile.TemporaryDirectory() as temp_dir_name:
            temp_dir = pathlib.Path(temp_dir_name)
            fake_bin = temp_dir / "bin"
            fake_bin.mkdir()
            log_path = temp_dir / "docker.log"
            state_path = temp_dir / "parent-pull-platform"
            index_path = temp_dir / "index.json"
            index_path.write_text(json.dumps(make_index()), encoding="utf-8")

            docker = fake_bin / "docker"
            docker.write_text(
                textwrap.dedent(
                    """\
                    #!/usr/bin/env bash
                    set -euo pipefail

                    log() { printf '%s\\n' "$*" >> "${FAKE_DOCKER_LOG}"; }

                    if [[ "${1:-}" == "buildx" && "${2:-}" == "imagetools" && "${3:-}" == "inspect" && "${4:-}" == "--raw" ]]; then
                      log "imagetools ${5:-}"
                      cat "${FAKE_INDEX_FILE}"
                      exit 0
                    fi

                    if [[ "${1:-}" == "pull" && "${2:-}" == "--platform" ]]; then
                      platform="${3:-}"
                      ref="${4:-}"
                      log "pull ${platform} ${ref}"
                      if [[ "${ref}" == "${PARENT_REF}" ]]; then
                        if [[ ! -e "${FAKE_PARENT_STATE}" ]]; then
                          printf '%s' "${platform}" > "${FAKE_PARENT_STATE}"
                          exit 0
                        fi
                        previous="$(cat "${FAKE_PARENT_STATE}")"
                        if [[ "${previous}" != "${platform}" ]]; then
                          echo "cannot overwrite digest ${PARENT_DIGEST}" >&2
                          exit 1
                        fi
                      fi
                      exit 0
                    fi

                    if [[ "${1:-}" == "image" && "${2:-}" == "inspect" ]]; then
                      if [[ "${3:-}" == "--format" ]]; then
                        ref="${5:-}"
                        log "image-inspect ${ref}"
                        case "${ref}" in
                          "${AMD64_REF}") printf '%s\\n' 'linux/amd64' ;;
                          "${ARM64_REF}") printf '%s\\n' 'linux/arm64' ;;
                          *) printf '%s\\n' 'linux/amd64' ;;
                        esac
                      fi
                      exit 0
                    fi

                    if [[ "${1:-}" == "create" && "${2:-}" == "--platform" ]]; then
                      platform="${3:-}"
                      ref="${4:-}"
                      log "create ${platform} ${ref}"
                      case "${platform}" in
                        linux/amd64) [[ "${ref}" == "${AMD64_REF}" || "${ref}" == "${PARENT_REF}" ]] ;;
                        linux/arm64) [[ "${ref}" == "${ARM64_REF}" || "${ref}" == "${PARENT_REF}" ]] ;;
                        *) exit 1 ;;
                      esac
                      printf '%s\\n' 'fake-container'
                      exit 0
                    fi

                    if [[ "${1:-}" == "inspect" && "${2:-}" == "--format" ]]; then
                      log "container-inspect ${4:-}"
                      printf '%s\\n' '["/nodejs/bin/node"] ["apps/web/server.js"]'
                      exit 0
                    fi

                    if [[ "${1:-}" == "cp" ]]; then
                      source="${2:-}"
                      target="${3:-}"
                      log "cp ${source} ${target}"
                      if [[ "${source}" == *:/app ]]; then
                        mkdir -p "${target}"
                      else
                        mkdir -p "$(dirname "${target}")"
                        : > "${target}"
                      fi
                      exit 0
                    fi

                    if [[ "${1:-}" == "rm" ]]; then
                      exit 0
                    fi

                    echo "unexpected fake docker invocation: $*" >&2
                    exit 97
                    """
                ),
                encoding="utf-8",
            )
            docker.chmod(0o755)

            env = os.environ.copy()
            env.update(
                {
                    "PATH": f"{fake_bin}:{env['PATH']}",
                    "FAKE_DOCKER_LOG": str(log_path),
                    "FAKE_PARENT_STATE": str(state_path),
                    "FAKE_INDEX_FILE": str(index_path),
                    "PARENT_REF": PARENT_REF,
                    "PARENT_DIGEST": PARENT_DIGEST,
                    "AMD64_REF": AMD64_REF,
                    "ARM64_REF": ARM64_REF,
                }
            )

            for platform in ("linux/amd64", "linux/arm64"):
                result = subprocess.run(
                    ["bash", str(GUARD), PARENT_REF, platform],
                    text=True,
                    capture_output=True,
                    cwd=ROOT,
                    env=env,
                    check=False,
                )
                self.assertEqual(
                    result.returncode,
                    0,
                    f"{platform} guard failed:\nstdout:\n{result.stdout}\nstderr:\n{result.stderr}",
                )

            log_text = log_path.read_text(encoding="utf-8")
            self.assertIn(f"pull linux/amd64 {AMD64_REF}", log_text)
            self.assertIn(f"pull linux/arm64 {ARM64_REF}", log_text)
            self.assertIn(f"create linux/amd64 {AMD64_REF}", log_text)
            self.assertIn(f"create linux/arm64 {ARM64_REF}", log_text)
            self.assertNotIn(f"pull linux/amd64 {PARENT_REF}", log_text)
            self.assertNotIn(f"pull linux/arm64 {PARENT_REF}", log_text)
            self.assertNotIn(f"create linux/amd64 {PARENT_REF}", log_text)
            self.assertNotIn(f"create linux/arm64 {PARENT_REF}", log_text)


if __name__ == "__main__":
    unittest.main()
