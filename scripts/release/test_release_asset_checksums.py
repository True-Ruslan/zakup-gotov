import hashlib
import pathlib
import tempfile
import unittest

from verify_release_asset_checksums import verify_assets


class ReleaseAssetChecksumTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = pathlib.Path(self.tmp.name)
        (self.root / "compose.release.yaml").write_text("services: {}\n", encoding="utf-8")
        (self.root / "release-verification.json").write_text('{"version":"0.1.0-rc.7"}\n', encoding="utf-8")

    def digest(self, name: str) -> str:
        return hashlib.sha256((self.root / name).read_bytes()).hexdigest()

    def write_checksums(self, lines: list[str]) -> pathlib.Path:
        path = self.root / "SHA256SUMS"
        path.write_text("\n".join(lines) + "\n", encoding="utf-8")
        return path

    def test_verifies_downloaded_basenames_against_release_dist_paths(self):
        checksums = self.write_checksums([
            f"{self.digest('compose.release.yaml')}  dist/compose.release.yaml",
            f"{self.digest('release-verification.json')}  dist/release-verification.json",
            f"{'a' * 64}  dist/api-manifest.json",
        ])

        verify_assets(
            checksums,
            self.root,
            ["compose.release.yaml", "release-verification.json"],
        )

    def test_rejects_checksum_mismatch(self):
        checksums = self.write_checksums([
            f"{'0' * 64}  dist/compose.release.yaml",
            f"{self.digest('release-verification.json')}  dist/release-verification.json",
        ])

        with self.assertRaisesRegex(ValueError, "checksum mismatch"):
            verify_assets(checksums, self.root, ["compose.release.yaml"])

    def test_rejects_missing_required_entry(self):
        checksums = self.write_checksums([
            f"{self.digest('release-verification.json')}  dist/release-verification.json",
        ])

        with self.assertRaisesRegex(ValueError, "exactly one checksum entry"):
            verify_assets(checksums, self.root, ["compose.release.yaml"])

    def test_rejects_duplicate_required_entry(self):
        digest = self.digest("compose.release.yaml")
        checksums = self.write_checksums([
            f"{digest}  dist/compose.release.yaml",
            f"{digest} *dist/compose.release.yaml",
        ])

        with self.assertRaisesRegex(ValueError, "exactly one checksum entry"):
            verify_assets(checksums, self.root, ["compose.release.yaml"])

    def test_rejects_malformed_digest(self):
        checksums = self.write_checksums([
            "not-a-sha256  dist/compose.release.yaml",
        ])

        with self.assertRaisesRegex(ValueError, "malformed checksum"):
            verify_assets(checksums, self.root, ["compose.release.yaml"])


if __name__ == "__main__":
    unittest.main()
