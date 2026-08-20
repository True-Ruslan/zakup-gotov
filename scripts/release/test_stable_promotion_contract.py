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
            "MANUAL_CANARY_HARNESS_SHA: da35a5cb7ef46c64d266cd29731167eaa4cbefb4",
            "MANUAL_ACCEPTANCE_COMMENT: '5354743275'",
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
            ".source_sha == $expected_source",
            ".api.digest == $expected_api_digest",
            ".web.digest == $expected_web_digest",
            ".stable == false",
            '.version == "0.1.0-rc.7"',
            '"promoted_from": "$SOURCE_RC_TAG"',
            '"manual_canary_run": "$MANUAL_CANARY_RUN"',
            '"promotion_workflow_run": "$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID"',
            'gh release create "$STABLE_TAG"',
            '--target "$SOURCE_SHA"',
            'gh release edit "$STABLE_TAG" --draft=false --latest',
            "Verify published stable release asset digests",
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

    def test_draft_and_evidence_precede_registry_and_public_release_mutation(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        source_verify = workflow.index("Verify rc.7 release metadata and accepted digests")
        evidence_prepare = workflow.index("Prepare stable release evidence and notes")
        draft_prepare = workflow.index("Prepare or resume exact stable draft release")
        evidence_upload = workflow.index("Upload stable draft evidence")
        source_smoke = workflow.index("Verify rc.7 registry tags and exact bundle")
        stable_promote = workflow.index("Promote exact accepted digests to stable version")
        latest_promote = workflow.index("Promote exact accepted digests to latest")
        identity_verify = workflow.index("Verify stable and latest registry identities")
        publish = workflow.index("Publish stable GitHub Release")
        asset_verify = workflow.index("Verify published stable release asset digests")

        self.assertLess(source_verify, evidence_prepare)
        self.assertLess(evidence_prepare, draft_prepare)
        self.assertLess(draft_prepare, evidence_upload)
        self.assertLess(evidence_upload, source_smoke)
        self.assertLess(source_smoke, stable_promote)
        self.assertLess(stable_promote, latest_promote)
        self.assertLess(latest_promote, identity_verify)
        self.assertLess(identity_verify, publish)
        self.assertLess(publish, asset_verify)

    def test_promotion_fails_closed_on_preexisting_nonresumable_stable_tag(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn('git ls-remote --exit-code --tags origin "refs/tags/$STABLE_TAG"', workflow)
        self.assertIn("stable tag already exists without a resumable exact draft release", workflow)
        self.assertIn(".isDraft == true", workflow)
        self.assertIn(".isPrerelease == false", workflow)
        self.assertIn(".targetCommitish == $source", workflow)

    def test_permissions_are_minimal_for_promotion(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("permissions: {}", workflow)
        self.assertIn("actions: read", workflow)
        self.assertIn("contents: write", workflow)
        self.assertIn("issues: read", workflow)
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
