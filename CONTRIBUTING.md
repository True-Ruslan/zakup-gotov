# Contributing

Zakup Gotov is in an early architecture and integration-discovery phase. Contributions are welcome, but changes must preserve the product and architecture decisions documented under `docs/`.

The mandatory engineering rules are defined in [`docs/ENGINEERING.md`](docs/ENGINEERING.md). The reproducible local setup and commands are defined in [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md). They apply to maintainers and contributors alike.

## Before contributing

1. Read `README.md`, `docs/DEVELOPMENT.md`, `docs/ENGINEERING.md`, `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, and relevant ADRs/specifications.
2. Search existing issues and pull requests.
3. For substantial behavior or architecture changes, document the decision before implementation.
4. Never include secrets, retailer credentials, private endpoints, or personal data in commits, fixtures, logs, screenshots, or issues.
5. Activate the repository-pinned Java/Node/pnpm toolchains and ensure Docker is running.

## Development principles

- Use TDD for executable behavior: RED -> verify expected failure -> GREEN -> regression suite -> REFACTOR.
- Prefer small, cohesive, reviewable pull requests.
- Use automated tests to prove behavior and regressions; coverage alone is not proof.
- Keep external retailer behavior behind provider adapters.
- Do not bypass module boundaries for convenience.
- Prefer explicit data freshness and uncertainty over silent fallback behavior.
- Avoid adding infrastructure until a measured requirement justifies it.
- Keep public API changes synchronized with OpenAPI contracts.
- Keep project documentation synchronized with repository reality in the same PR.
- Do not claim completion without fresh verification evidence.

## Verification before a pull request

Run the unified verification command:

```bash
./scripts/verify.sh
```

It verifies backend behavior against real PostgreSQL/Testcontainers, generated OpenAPI-client drift, strict client/web type checks, tests, lint, and production builds. Do not bypass a failed step to open a green-looking PR.

For changes affecting browser behavior, also run the Playwright workflow documented in `docs/DEVELOPMENT.md`.

Cloud security checks (CodeQL, Dependency Review, and other GitHub-native controls) remain required PR gates where configured.

## Pull requests

A pull request should include:

- problem and intended behavior;
- scope and non-goals;
- TDD/verification evidence where behavior changed;
- security/privacy impact where applicable;
- screenshots only for meaningful UI review that automated checks cannot replace;
- related issue/ADR/spec/plan if one exists;
- documentation/state/changelog updates where repository truth changed.

The target repository policy is squash-only merging through protected `main` after required checks pass.

## Commit and branch conventions

Use short-lived descriptive branches such as:

- `feat/...`
- `fix/...`
- `docs/...`
- `chore/...`
- `spike/...`

Commit messages should be concise and describe the actual change rather than the activity performed. Do not mix unrelated cleanup into functional commits.

## Testing expectations

Backend changes should use deterministic unit/module tests and real PostgreSQL integration tests through Testcontainers where persistence behavior matters.

Provider integrations require sanitized fixture/contract tests for successful and relevant failure responses. Normal CI tests must not depend on live retailer services; live validation belongs in explicit opt-in probes.

Web changes affecting critical journeys should include component tests and/or Playwright coverage at the appropriate level, including responsive behavior where relevant.

Repeated manual verification is treated as automation debt. A manual check must have a documented reason when reliable automation is not reasonable.

## Documentation and changelog

`docs/PROJECT_STATE.md` records factual current state. `docs/ROADMAP.md` records planned work. ADRs/specs record approved decisions. `CHANGELOG.md` records notable changes that actually happened.

If a PR changes any of these truths, update the corresponding documents in the same PR. Keep `[Unreleased]` current throughout development.

## Security reports

Do not report vulnerabilities through public issues. See `SECURITY.md`.

## License note

The repository is public, but no open-source license has been selected yet. Do not assume rights beyond GitHub's normal repository viewing/forking functionality until a license is explicitly added.
