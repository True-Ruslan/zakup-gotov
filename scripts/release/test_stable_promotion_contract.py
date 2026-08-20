import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
WORKFLOW = ROOT / ".github/workflows/stable-promotion.yml"


class StablePromotionContractTest(unittest.TestCase):
    def test_owner_gated_promotion_is_bound_to_accepted_rc7(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            "issue_comment:\n    types: [created]",
            "github.event.issue.number == 152",
            "github.event.comment.user.login == github.repository_owner",
            "github.event.comment.body == '/release-stable v0.1.0 from v0.1.0-rc.7'",
            "SOURCE_RC_TAG: v0.1.0-rc.7",
            "STABLE_TAG: v0.1.0",
            "SOURCE_SHA: b754f5193f852db0312011f3f6c3ec6c7dd22eb2",
            "SOURCE_API_DIGEST: sha256:1c5c4a104fee295cd579b0e69a23b508a297b1eb931a45c0ce71d8b1791e54e1",
            "SOURCE_WEB_DIGEST: sha256:5bc236f3f304dffe29f54921f5a2bbf27d3df67c18714d4cc268d6d25bafce68",
            "MANUAL_CANARY_RUN: '32359437905'",
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

    def test_promotion_reuses_verified_digests_and_never_rebuilds(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            'gh release download "$SOURCE_RC_TAG"',
            "scripts/release/verify_release_asset_checksums.py",
            'bash scripts/release/verify-published-release.sh "$SOURCE_DIR/compose.release.yaml"',
            '"$API_IMAGE@$SOURCE_API_DIGEST"',
            '"$WEB_IMAGE@$SOURCE_WEB_DIGEST"',
            '"$API_IMAGE:0.1.0"',
            '"$WEB_IMAGE:0.1.0"',
            '"$API_IMAGE:latest"',
            '"$WEB_IMAGE:latest"',
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

        forbidden = (
            "docker build ",
            "docker buildx build",
            "docker/build-push-action@",
            "candidate-",
        )
        for fragment in forbidden:
            with self.subTest(fragment=fragment):
                self.assertNotIn(fragment, workflow)

    def test_promotion_verifies_source_release_identity_and_stable_evidence(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            'gh release view "$SOURCE_RC_TAG"',
            '"source_sha == $expected_source"',
            '"api.digest == $expected_api_digest"',
            '"web.digest == $expected_web_digest"',
            '"stable == false"',
            '"version == \"0.1.0-rc.7\""',
            "stable-promotion-verification.json",
            '"promoted_from": "$SOURCE_RC_TAG"',
            '"manual_canary_run": "$MANUAL_CANARY_RUN"',
            'gh release create "$STABLE_TAG"',
            '--target "$SOURCE_SHA"',
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

    def test_permissions_are_minimal_for_promotion(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("permissions: {}", workflow)
        self.assertIn("contents: write", workflow)
        self.assertIn("packages: write", workflow)
        self.assertNotIn("attestations: write", workflow)
        self.assertNotIn("id-token: write", workflow)
        self.assertNotIn("pull-requests: write", workflow)

    def test_promotion_workflow_uses_immutable_action_pins(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        for line in workflow.splitlines():
            stripped = line.strip()
            if stripped.startswith("uses:"):
                with self.subTest(line=stripped):
                    self.assertRegex(stripped, r"@[0-9a-f]{40}(?:\s+#.*)?$")


if __name__ == "__main__":
    unittest.main()
