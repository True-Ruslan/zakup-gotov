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
            "MANUAL_CANARY_ARTIFACT_ID: '9402970517'",
            "MANUAL_CANARY_ARTIFACT_DIGEST: sha256:158afcff6c270526823ad372cf883cb5eeaf723eacfacab4d2a46fb68c625c25",
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

    def test_oci_aliases_use_raw_manifest_equivalence_not_top_level_descriptor_digest(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            "scripts/release/verify_oci_manifest_equivalence.py",
            "docker buildx imagetools inspect --raw",
            '"$API_IMAGE@$SOURCE_API_DIGEST"',
            '"$WEB_IMAGE@$SOURCE_WEB_DIGEST"',
            '"$API_IMAGE:$SOURCE_RC_VERSION"',
            '"$WEB_IMAGE:$SOURCE_RC_VERSION"',
            '"$API_IMAGE:0.1.0"',
            '"$WEB_IMAGE:0.1.0"',
            '"$API_IMAGE:latest"',
            '"$WEB_IMAGE:latest"',
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

        forbidden = (
            'grep -F "Digest: $SOURCE_API_DIGEST"',
            'grep -F "Digest: $SOURCE_WEB_DIGEST"',
            '."containerimage.descriptor".digest',
        )
        for fragment in forbidden:
            with self.subTest(fragment=fragment):
                self.assertNotIn(fragment, workflow)

    def test_promotion_verifies_source_release_identity_and_stable_evidence(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            'gh release download "$SOURCE_RC_TAG"',
            ".source_sha == $expected_source",
            ".api.digest == $expected_api_digest",
            ".web.digest == $expected_web_digest",
            ".stable == false",
            '.version == "0.1.0-rc.7"',
            'stable-promotion-verification.json',
            '--arg promoted_from "$SOURCE_RC_TAG"',
            '--arg manual_canary_run "$MANUAL_CANARY_RUN"',
            '--arg manual_acceptance_comment "$MANUAL_ACCEPTANCE_COMMENT"',
            '--arg promotion_workflow_run "$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID"',
            'method: "digest-preserving-promotion"',
            'promoted_from: $promoted_from',
            'manual_canary_run: $manual_canary_run',
            'manual_acceptance_comment: $manual_acceptance_comment',
            'promotion_workflow_run: $promotion_workflow_run',
            "Verify stable draft asset digests before registry mutation",
            "Verify published stable release",
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

    def test_stable_tag_is_exact_idempotent_and_release_uses_existing_tag(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            "Prepare or verify exact stable tag",
            'gh api "repos/$GITHUB_REPOSITORY/git/ref/tags/$STABLE_TAG"',
            'gh api --method POST "repos/$GITHUB_REPOSITORY/git/refs"',
            '--raw-field ref="refs/tags/$STABLE_TAG"',
            '--raw-field sha="$SOURCE_SHA"',
            '.object.type == "commit" and .object.sha == $source',
            "stable tag exists but does not resolve to the accepted rc.7 source",
            'gh api --method POST "repos/$GITHUB_REPOSITORY/releases"',
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

        self.assertNotIn('--raw-field target_commitish="$SOURCE_SHA"', workflow)

    def test_draft_assets_use_release_id_rest_upload_with_digest_safe_resume(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        required = (
            "Upload stable draft evidence through release asset API",
            'gh api "repos/$GITHUB_REPOSITORY/releases/$STABLE_RELEASE_ID/assets?per_page=100"',
            'https://uploads.github.com/repos/$GITHUB_REPOSITORY/releases/$STABLE_RELEASE_ID/assets?name=$asset_name',
            'Authorization: Bearer $GH_TOKEN',
            'Content-Type: application/octet-stream',
            '--data-binary "@$asset_path"',
            'asset already exists with unexpected digest',
            'state == "uploaded" and .name == $name and .digest == $digest',
        )
        for fragment in required:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

        self.assertNotIn('gh release upload "$STABLE_TAG"', workflow)
        self.assertNotIn("--clobber", workflow)

    def test_all_read_only_source_verification_precedes_first_release_write(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        source_metadata = workflow.index("Verify rc.7 release metadata and accepted digests")
        source_registry = workflow.index("Verify rc.7 registry identities and exact bundle")
        evidence_prepare = workflow.index("Prepare stable release evidence and notes")
        tag_prepare = workflow.index("Prepare or verify exact stable tag")
        draft_prepare = workflow.index("Prepare or resume exact stable draft release")
        evidence_upload = workflow.index("Upload stable draft evidence through release asset API")
        draft_verify = workflow.index("Verify stable draft asset digests before registry mutation")
        stable_promote = workflow.index("Promote exact accepted digests to stable version")
        latest_promote = workflow.index("Promote exact accepted digests to latest")
        identity_verify = workflow.index("Verify stable and latest registry identities")
        publish = workflow.index("Publish stable GitHub Release last")
        published_verify = workflow.index("Verify published stable release")

        self.assertLess(source_metadata, source_registry)
        self.assertLess(source_registry, evidence_prepare)
        self.assertLess(evidence_prepare, tag_prepare)
        self.assertLess(tag_prepare, draft_prepare)
        self.assertLess(draft_prepare, evidence_upload)
        self.assertLess(evidence_upload, draft_verify)
        self.assertLess(draft_verify, stable_promote)
        self.assertLess(stable_promote, latest_promote)
        self.assertLess(latest_promote, identity_verify)
        self.assertLess(identity_verify, publish)
        self.assertLess(publish, published_verify)

    def test_draft_resume_is_fail_closed_and_bound_to_stable_tag(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("multiple releases exist for stable tag $STABLE_TAG", workflow)
        self.assertIn(".draft == true and .prerelease == false", workflow)
        self.assertIn('tag_name="$STABLE_TAG"', workflow)
        self.assertNotIn("stable tag already exists without a resumable exact draft release", workflow)

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
