import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]


class PlaywrightInstallReliabilityContractTest(unittest.TestCase):
    @staticmethod
    def _helper() -> str:
        return (ROOT / "scripts/ci/install-playwright-chromium.sh").read_text(
            encoding="utf-8"
        )

    def test_install_is_bounded_and_retried_without_foreground_escape(self):
        helper = self._helper()

        required_fragments = (
            'PLAYWRIGHT_INSTALL_ATTEMPTS:-2',
            'PLAYWRIGHT_INSTALL_TIMEOUT_SECONDS:-360',
            'Acquire::Retries "3";',
            'Acquire::http::Timeout "20";',
            'Acquire::https::Timeout "20";',
            '--signal=TERM',
            '--kill-after=15s',
            '"${attempt_timeout_seconds}s"',
        )

        for fragment in required_fragments:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, helper)

        self.assertNotIn("--foreground", helper)

    def test_all_browser_ci_paths_delegate_to_the_bounded_helper(self):
        workflows = {
            "web": (ROOT / ".github/workflows/web-ci.yml").read_text(encoding="utf-8"),
            "bridge": (ROOT / ".github/workflows/retailer-bridge-ci.yml").read_text(
                encoding="utf-8"
            ),
            "release": (ROOT / ".github/workflows/release.yml").read_text(
                encoding="utf-8"
            ),
        }

        self.assertIn(
            "bash scripts/ci/install-playwright-chromium.sh pnpm --filter web exec playwright install --with-deps chromium",
            workflows["web"],
        )
        self.assertIn(
            "bash scripts/ci/install-playwright-chromium.sh pnpm --dir apps/retailer-bridge exec playwright install --with-deps chromium",
            workflows["bridge"],
        )
        self.assertIn(
            "bash scripts/ci/install-playwright-chromium.sh pnpm --filter web exec playwright install --with-deps chromium",
            workflows["release"],
        )

        self.assertNotIn(
            "run: pnpm --filter web exec playwright install --with-deps chromium",
            workflows["web"],
        )
        self.assertNotIn(
            "run: pnpm --dir apps/retailer-bridge exec playwright install --with-deps chromium",
            workflows["bridge"],
        )
        self.assertNotIn(
            "run: pnpm --filter web exec playwright install --with-deps chromium",
            workflows["release"],
        )


class WebVexPlatformContractTest(unittest.TestCase):
    def test_runtime_guard_separates_local_and_registry_sources(self):
        guard = (ROOT / "scripts/security/verify_web_vex_runtime.sh").read_text(
            encoding="utf-8"
        )
        container_workflow = (
            ROOT / ".github/workflows/container-security-ci.yml"
        ).read_text(encoding="utf-8")
        release_workflow = (ROOT / ".github/workflows/release.yml").read_text(
            encoding="utf-8"
        )

        self.assertIn('source_mode="${3:-registry}"', guard)
        self.assertIn('docker image inspect "${image_ref}"', guard)
        self.assertIn(
            'docker pull --platform "${platform}" "${image_ref}"',
            guard,
        )
        self.assertIn(
            'docker create --platform "${platform}" "${image_ref}"',
            guard,
        )
        self.assertIn(
            'verify_web_vex_runtime.sh "${{ matrix.image }}" linux/amd64 local',
            container_workflow,
        )
        self.assertEqual(
            release_workflow.count("bash scripts/security/verify_web_vex_runtime.sh"),
            2,
        )


if __name__ == "__main__":
    unittest.main()
