import json
import pathlib
import subprocess
import sys
import tempfile
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts/release/verify_oci_manifest_equivalence.py"
OCI_INDEX = "application/vnd.oci.image.index.v1+json"
OCI_MANIFEST = "application/vnd.oci.image.manifest.v1+json"


def descriptor(digest: str, architecture: str, *, size: int = 1234) -> dict:
    return {
        "mediaType": OCI_MANIFEST,
        "digest": digest,
        "size": size,
        "platform": {
            "architecture": architecture,
            "os": "linux",
        },
    }


def index(*manifests: dict, annotations: dict | None = None) -> dict:
    payload = {
        "schemaVersion": 2,
        "mediaType": OCI_INDEX,
        "manifests": list(manifests),
    }
    if annotations is not None:
        payload["annotations"] = annotations
    return payload


class OciManifestEquivalenceTest(unittest.TestCase):
    def run_verifier(self, source: dict, alias: dict) -> subprocess.CompletedProcess[str]:
        with tempfile.TemporaryDirectory() as temp_dir:
            temp = pathlib.Path(temp_dir)
            source_path = temp / "source.json"
            alias_path = temp / "alias.json"
            source_path.write_text(json.dumps(source), encoding="utf-8")
            alias_path.write_text(json.dumps(alias), encoding="utf-8")
            return subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    str(source_path),
                    str(alias_path),
                ],
                cwd=ROOT,
                text=True,
                capture_output=True,
                check=False,
            )

    def assert_passes(self, source: dict, alias: dict) -> None:
        result = self.run_verifier(source, alias)
        self.assertEqual(0, result.returncode, msg=result.stderr or result.stdout)

    def assert_fails(self, source: dict, alias: dict) -> None:
        result = self.run_verifier(source, alias)
        self.assertNotEqual(0, result.returncode, msg="verifier unexpectedly accepted non-equivalent indexes")

    def test_accepts_equivalent_indexes_despite_json_key_order_and_whitespace(self):
        amd64 = descriptor("sha256:" + "a" * 64, "amd64")
        arm64 = descriptor("sha256:" + "b" * 64, "arm64")
        source = index(amd64, arm64)
        alias = {
            "manifests": [
                {
                    "platform": {"os": "linux", "architecture": "amd64"},
                    "size": 1234,
                    "digest": "sha256:" + "a" * 64,
                    "mediaType": OCI_MANIFEST,
                },
                {
                    "platform": {"architecture": "arm64", "os": "linux"},
                    "digest": "sha256:" + "b" * 64,
                    "mediaType": OCI_MANIFEST,
                    "size": 1234,
                },
            ],
            "mediaType": OCI_INDEX,
            "schemaVersion": 2,
        }
        self.assert_passes(source, alias)

    def test_accepts_changed_top_level_annotations_when_referenced_manifests_are_identical(self):
        amd64 = descriptor("sha256:" + "a" * 64, "amd64")
        arm64 = descriptor("sha256:" + "b" * 64, "arm64")
        source = index(amd64, arm64, annotations={"org.opencontainers.image.version": "0.1.0-rc.7"})
        alias = index(amd64, arm64, annotations={"org.opencontainers.image.version": "0.1.0"})
        self.assert_passes(source, alias)

    def test_rejects_changed_child_digest(self):
        source = index(descriptor("sha256:" + "a" * 64, "amd64"))
        alias = index(descriptor("sha256:" + "c" * 64, "amd64"))
        self.assert_fails(source, alias)

    def test_rejects_changed_child_size(self):
        digest = "sha256:" + "a" * 64
        source = index(descriptor(digest, "amd64", size=1234))
        alias = index(descriptor(digest, "amd64", size=1235))
        self.assert_fails(source, alias)

    def test_rejects_changed_platform(self):
        digest = "sha256:" + "a" * 64
        source = index(descriptor(digest, "amd64"))
        alias = index(descriptor(digest, "arm64"))
        self.assert_fails(source, alias)

    def test_rejects_missing_or_extra_child_descriptor(self):
        amd64 = descriptor("sha256:" + "a" * 64, "amd64")
        arm64 = descriptor("sha256:" + "b" * 64, "arm64")
        self.assert_fails(index(amd64, arm64), index(amd64))
        self.assert_fails(index(amd64), index(amd64, arm64))

    def test_rejects_reordered_descriptors(self):
        amd64 = descriptor("sha256:" + "a" * 64, "amd64")
        arm64 = descriptor("sha256:" + "b" * 64, "arm64")
        self.assert_fails(index(amd64, arm64), index(arm64, amd64))

    def test_rejects_descriptor_metadata_drift(self):
        digest = "sha256:" + "a" * 64
        source_descriptor = descriptor(digest, "amd64")
        source_descriptor["annotations"] = {"org.opencontainers.image.ref.name": "source"}
        alias_descriptor = descriptor(digest, "amd64")
        alias_descriptor["annotations"] = {"org.opencontainers.image.ref.name": "alias"}
        self.assert_fails(index(source_descriptor), index(alias_descriptor))

    def test_rejects_invalid_or_unsupported_top_level_contract(self):
        manifest = {
            "schemaVersion": 2,
            "mediaType": OCI_MANIFEST,
            "config": {"mediaType": "application/vnd.oci.image.config.v1+json", "digest": "sha256:" + "d" * 64, "size": 10},
            "layers": [],
        }
        self.assert_fails(manifest, manifest)
        self.assert_fails({"schemaVersion": 1, "mediaType": OCI_INDEX, "manifests": []}, {"schemaVersion": 1, "mediaType": OCI_INDEX, "manifests": []})
        self.assert_fails(index(), index())

    def test_rejects_duplicate_child_descriptors(self):
        amd64 = descriptor("sha256:" + "a" * 64, "amd64")
        self.assert_fails(index(amd64, amd64), index(amd64, amd64))


if __name__ == "__main__":
    unittest.main()
