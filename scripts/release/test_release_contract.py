import os
import pathlib
import unittest

from release_contract import (
    ReleaseMetadata,
    build_image_names,
    build_staging_image_names,
    parse_release,
    render_release_compose,
)


ROOT = pathlib.Path(__file__).resolve().parents[2]


class ReleaseMetadataTest(unittest.TestCase):
    def test_stable_release_may_publish_latest(self):
        metadata = parse_release("v1.2.3", prerelease=False, commit_sha="a" * 40)

        self.assertEqual(
            metadata,
            ReleaseMetadata(
                version="1.2.3",
                stable=True,
                version_tag="1.2.3",
                candidate_tag=f"candidate-{'a' * 40}",
                verified_tag=f"verified-{'a' * 40}",
                publish_latest=True,
            ),
        )

    def test_prerelease_never_publishes_latest(self):
        metadata = parse_release(
            "v1.2.3-rc.1", prerelease=True, commit_sha="b" * 40
        )

        self.assertEqual(metadata.version, "1.2.3-rc.1")
        self.assertFalse(metadata.stable)
        self.assertFalse(metadata.publish_latest)

    def test_release_flag_must_match_semver_prerelease_state(self):
        with self.assertRaisesRegex(ValueError, "prerelease flag"):
            parse_release("v1.2.3-rc.1", prerelease=False, commit_sha="c" * 40)

        with self.assertRaisesRegex(ValueError, "prerelease flag"):
            parse_release("v1.2.3", prerelease=True, commit_sha="c" * 40)

    def test_invalid_or_ambiguous_release_tags_are_rejected(self):
        invalid_tags = (
            "1.2.3",
            "v1.2",
            "v01.2.3",
            "v1.02.3",
            "v1.2.03",
            "v1.2.3+build.1",
            "v1.2.3-01",
            "v1.2.3-",
        )

        for tag in invalid_tags:
            with self.subTest(tag=tag):
                with self.assertRaises(ValueError):
                    parse_release(tag, prerelease=False, commit_sha="d" * 40)

    def test_commit_sha_must_be_full_lowercase_sha1(self):
        for sha in ("abc", "A" * 40, "g" * 40):
            with self.subTest(sha=sha):
                with self.assertRaisesRegex(ValueError, "commit SHA"):
                    parse_release("v1.2.3", prerelease=False, commit_sha=sha)


class ImageNameTest(unittest.TestCase):
    def test_ghcr_image_names_are_lowercase_and_repo_scoped(self):
        api, web = build_image_names("True-Ruslan", "zakup-gotov")

        self.assertEqual(api, "ghcr.io/true-ruslan/zakup-gotov-api")
        self.assertEqual(web, "ghcr.io/true-ruslan/zakup-gotov-web")

    def test_unverified_candidates_use_separate_staging_packages(self):
        api, web = build_staging_image_names("True-Ruslan", "zakup-gotov")

        self.assertEqual(api, "ghcr.io/true-ruslan/zakup-gotov-staging-api")
        self.assertEqual(web, "ghcr.io/true-ruslan/zakup-gotov-staging-web")
        self.assertNotEqual(api, build_image_names("True-Ruslan", "zakup-gotov")[0])
        self.assertNotEqual(web, build_image_names("True-Ruslan", "zakup-gotov")[1])


class ReleaseScriptModeTest(unittest.TestCase):
    def test_release_scripts_are_executable_in_checkout(self):
        scripts = (
            ROOT / "scripts/verify-release-bundle.sh",
            ROOT / "scripts/release/verify-published-release.sh",
        )

        for script in scripts:
            with self.subTest(script=script.relative_to(ROOT)):
                self.assertTrue(
                    os.access(script, os.X_OK),
                    f"{script.relative_to(ROOT)} must be executable in a clean checkout",
                )


class ComposeRenderTest(unittest.TestCase):
    def test_release_compose_pins_both_application_images_by_digest(self):
        source = (ROOT / "compose.release.yaml").read_text(encoding="utf-8")
        api_ref = f"ghcr.io/true-ruslan/zakup-gotov-api@sha256:{'1' * 64}"
        web_ref = f"ghcr.io/true-ruslan/zakup-gotov-web@sha256:{'2' * 64}"

        rendered = render_release_compose(source, api_ref=api_ref, web_ref=web_ref)

        self.assertIn(f"image: {api_ref}", rendered)
        self.assertIn(f"image: {web_ref}", rendered)
        self.assertNotIn("${API_IMAGE", rendered)
        self.assertNotIn("${WEB_IMAGE", rendered)
        self.assertIn("${POSTGRES_PASSWORD:?POSTGRES_PASSWORD must be set}", rendered)
        self.assertNotIn("build:", rendered)

    def test_release_compose_rejects_mutable_or_malformed_image_refs(self):
        source = (ROOT / "compose.release.yaml").read_text(encoding="utf-8")
        valid = f"ghcr.io/true-ruslan/zakup-gotov-api@sha256:{'1' * 64}"
        invalid = (
            "ghcr.io/true-ruslan/zakup-gotov-api:1.2.3",
            "docker.io/library/alpine@sha256:" + "1" * 64,
            "ghcr.io/True-Ruslan/zakup-gotov-api@sha256:" + "1" * 64,
            "ghcr.io/true-ruslan/zakup-gotov-api@sha256:abc",
        )

        for ref in invalid:
            with self.subTest(ref=ref):
                with self.assertRaises(ValueError):
                    render_release_compose(source, api_ref=ref, web_ref=valid)


class PublishingWorkflowContractTest(unittest.TestCase):
    @staticmethod
    def _workflow() -> str:
        return (ROOT / ".github/workflows/release.yml").read_text(encoding="utf-8")

    def test_release_workflow_preserves_supply_chain_boundary(self):
        workflow = self._workflow()

        required_fragments = (
            "release:\n    types: [published]",
            "permissions:\n  contents: read",
            "packages: write",
            "attestations: write",
            "id-token: write",
            "linux/amd64,linux/arm64",
            "provenance: mode=max",
            "sbom: true",
            "severity: CRITICAL,HIGH",
            "TRIVY_PLATFORM: linux/amd64",
            "TRIVY_PLATFORM: linux/arm64",
            "docker buildx imagetools create",
            "publish_latest",
            "render-compose",
            "verify-published-release.sh",
            "gh release upload",
        )

        for fragment in required_fragments:
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, workflow)

    def test_release_workflow_does_not_use_mutable_action_tags(self):
        workflow = self._workflow()

        for line in workflow.splitlines():
            stripped = line.strip()
            if stripped.startswith("uses:"):
                with self.subTest(line=stripped):
                    self.assertRegex(stripped, r"@[0-9a-f]{40}(?:\s+#.*)?$")

    def test_release_workflow_pins_qemu_and_buildkit_helper_images(self):
        workflow = self._workflow()

        self.assertRegex(
            workflow,
            r"image: tonistiigi/binfmt@sha256:[0-9a-f]{64}",
        )
        self.assertRegex(
            workflow,
            r"image=moby/buildkit@sha256:[0-9a-f]{64}",
        )
        self.assertNotIn("tonistiigi/binfmt:latest", workflow)
        self.assertNotIn("moby/buildkit:buildx-stable-1", workflow)

    def test_verification_and_security_gates_precede_version_publication(self):
        workflow = self._workflow()

        build_position = workflow.index("Build and push API candidate")
        scan_position = workflow.index(
            "Scan API candidate for HIGH/CRITICAL vulnerabilities on amd64"
        )
        staging_smoke_position = workflow.index("Verify staging candidate bundle")
        copy_position = workflow.index("Copy verified digests into final packages")
        final_smoke_position = workflow.index("Verify exact final-package candidate bundle")
        attest_position = workflow.index("Attest final API provenance")
        version_position = workflow.index("Promote verified final digests to release version")
        latest_position = workflow.index("Promote stable release to latest")
        upload_position = workflow.index("Attach verified release assets")

        self.assertLess(build_position, scan_position)
        self.assertLess(scan_position, staging_smoke_position)
        self.assertLess(staging_smoke_position, copy_position)
        self.assertLess(copy_position, final_smoke_position)
        self.assertLess(final_smoke_position, attest_position)
        self.assertLess(attest_position, version_position)
        self.assertLess(version_position, latest_position)
        self.assertLess(latest_position, upload_position)

    def test_latest_promotion_is_explicitly_conditional(self):
        workflow = self._workflow()
        latest_section = workflow[workflow.index("Promote stable release to latest") :]

        self.assertIn("if: steps.release.outputs.publish_latest == 'true'", latest_section)
        self.assertIn(":latest", latest_section)

    def test_candidate_builds_use_staging_packages_not_final_packages(self):
        workflow = self._workflow()

        self.assertIn("steps.release.outputs.api_candidate", workflow)
        self.assertIn("steps.release.outputs.web_candidate", workflow)
        self.assertIn("steps.release.outputs.api_staging_image", workflow)
        self.assertIn("steps.release.outputs.web_staging_image", workflow)
        self.assertIn("steps.release.outputs.api_verified_candidate", workflow)
        self.assertIn("steps.release.outputs.web_verified_candidate", workflow)


if __name__ == "__main__":
    unittest.main()
