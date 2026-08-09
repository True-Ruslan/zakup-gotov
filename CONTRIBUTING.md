# Contributing

Zakup Gotov is in an early architecture and integration-discovery phase. Contributions are welcome, but changes should preserve the product and architecture decisions documented under `docs/`.

## Before contributing

1. Read `README.md`, `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, and relevant ADRs.
2. Search existing issues and pull requests.
3. For substantial behavior or architecture changes, open an issue/discussion first so the decision can be documented before implementation.
4. Never include secrets, retailer credentials, private endpoints, or personal data in commits, fixtures, logs, screenshots, or issues.

## Development principles

- Prefer small, reviewable pull requests.
- Use tests to prove behavior and regressions.
- Keep external retailer behavior behind provider adapters.
- Do not bypass module boundaries for convenience.
- Prefer explicit data freshness and uncertainty over silent fallback behavior.
- Avoid adding infrastructure until a measured requirement justifies it.
- Keep public API changes backward-compatible where practical and update OpenAPI contracts with implementation changes.

## Pull requests

A pull request should include:

- problem and intended behavior;
- scope and non-goals;
- tests/verification performed;
- security/privacy impact where applicable;
- screenshots for meaningful UI changes;
- related issue/ADR/spec if one exists.

The target repository policy is squash-only merging through protected `main` after required checks pass.

## Commit and branch conventions

Use short descriptive branches such as:

- `feat/...`
- `fix/...`
- `docs/...`
- `chore/...`
- `spike/...`

Commit messages should be concise and explain the change rather than the activity.

## Testing expectations

Backend changes should prefer deterministic unit/module tests and real PostgreSQL integration tests through Testcontainers where persistence behavior matters.

Provider integrations require fixture/contract tests for successful and failure responses. Tests must not depend on live retailer services unless explicitly marked as opt-in integration probes.

Web changes affecting critical journeys should include component tests and/or Playwright coverage at the appropriate level.

## Security reports

Do not report vulnerabilities through public issues. See `SECURITY.md`.

## License note

The repository is public, but no open-source license has been selected yet. Do not assume rights beyond GitHub's normal repository viewing/forking functionality until a license is explicitly added.