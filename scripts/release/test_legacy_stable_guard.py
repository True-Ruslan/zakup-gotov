import pathlib
import subprocess
import sys
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
RELEASE_CONTRACT = ROOT / "scripts/release/release_contract.py"


class LegacyStableReleaseGuardTest(unittest.TestCase):
    def test_release_event_metadata_cli_rejects_direct_stable_publication(self):
        result = subprocess.run(
            [
                sys.executable,
                str(RELEASE_CONTRACT),
                "metadata",
                "--tag",
                "v0.1.0",
                "--prerelease",
                "false",
                "--commit-sha",
                "a" * 40,
                "--owner",
                "True-Ruslan",
                "--repository",
                "zakup-gotov",
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn(
            "stable releases must use the digest-preserving stable promotion workflow",
            result.stderr,
        )

    def test_prerelease_metadata_cli_remains_supported(self):
        result = subprocess.run(
            [
                sys.executable,
                str(RELEASE_CONTRACT),
                "metadata",
                "--tag",
                "v0.1.0-rc.8",
                "--prerelease",
                "true",
                "--commit-sha",
                "b" * 40,
                "--owner",
                "True-Ruslan",
                "--repository",
                "zakup-gotov",
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("stable=false", result.stdout)
        self.assertIn("publish_latest=false", result.stdout)


if __name__ == "__main__":
    unittest.main()
