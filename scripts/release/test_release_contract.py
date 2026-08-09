import pathlib
import unittest

from release_contract import (
    ReleaseMetadata,
    build_image_names,
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


if __name__ == "__main__":
    unittest.main()
