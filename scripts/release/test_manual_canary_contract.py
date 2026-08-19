import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
WORKFLOW = ROOT / ".github/workflows/release-product-canary.yml"
CAPTURE_SCRIPT = ROOT / "apps/web/scripts/release-canary-capture.mjs"
RELEASE_CONTRACT_WORKFLOW = ROOT / ".github/workflows/release-contract-ci.yml"


class ManualProductCanaryContractTest(unittest.TestCase):
    def test_canary_is_owner_gated_and_bound_to_immutable_rc7(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            "issue_comment:\n    types: [created]",
            "github.event.issue.number == 152",
            "github.event.comment.user.login == github.repository_owner",
            "github.event.comment.body == '/release-canary rc.7'",
            "RC7_TAG: v0.1.0-rc.7",
            "RC7_SOURCE_SHA: b754f5193f852db0312011f3f6c3ec6c7dd22eb2",
            "gh release download \"$RC7_TAG\"",
            "SHA256SUMS",
            "compose.release.yaml",
            "release-verification.json",
            "sha256sum --check --ignore-missing SHA256SUMS",
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

    def test_canary_cannot_publish_or_mutate_release_state(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("contents: read", workflow)
        self.assertIn("packages: read", workflow)
        self.assertIn("issues: write", workflow)
        self.assertNotIn("packages: write", workflow)
        self.assertNotIn("attestations: write", workflow)
        self.assertNotIn("id-token: write", workflow)
        self.assertNotIn("gh release create", workflow)
        self.assertNotIn("gh release edit", workflow)
        self.assertNotIn("docker build", workflow)
        self.assertNotIn("docker buildx", workflow)
        self.assertNotRegex(workflow, r"ghcr\.io/[^\s\"']+:latest(?:\s|$)")

    def test_canary_uses_digest_pinned_release_bundle_and_review_evidence(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        capture = CAPTURE_SCRIPT.read_text(encoding="utf-8")

        required_workflow = (
            "published release compose must contain exactly two digest-pinned GHCR application images",
            "docker compose -f \"$COMPOSE_FILE\" pull",
            "docker compose -f \"$COMPOSE_FILE\" up --detach --wait --wait-timeout 180",
            "bash scripts/ci/install-playwright-chromium.sh pnpm --dir apps/web exec playwright install --with-deps chromium",
            "node apps/web/scripts/release-canary-capture.mjs normal",
            "docker compose -f \"$COMPOSE_FILE\" stop api",
            "node apps/web/scripts/release-canary-capture.mjs api-unavailable",
            "docker compose -f \"$COMPOSE_FILE\" start api",
            "node apps/web/scripts/release-canary-capture.mjs recovered",
            "actions/upload-artifact@",
            "manual review is still required",
        )
        for fragment in required_workflow:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

        required_capture = (
            "weekly-plan-comparison",
            "recipe-comparison",
            "comparison-preview",
            "release-canary-report.json",
            "page.screenshot",
            "manual-review-required",
        )
        for fragment in required_capture:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, capture)

    def test_canary_workflow_uses_immutable_action_pins(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        for line in workflow.splitlines():
            stripped = line.strip()
            if stripped.startswith("uses:"):
                with self.subTest(line=stripped):
                    self.assertRegex(stripped, r"@[0-9a-f]{40}(?:\s+#.*)?$")

    def test_release_contract_ci_parses_canary_workflow_yaml(self):
        release_contract_ci = RELEASE_CONTRACT_WORKFLOW.read_text(encoding="utf-8")
        self.assertIn(".github/workflows/release-product-canary.yml", release_contract_ci)


if __name__ == "__main__":
    unittest.main()
