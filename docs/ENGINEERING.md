# Engineering Policy

This document defines the default engineering rules for Zakup Gotov. These rules apply to product code, infrastructure, integrations, tests, documentation, and repository maintenance unless an accepted ADR explicitly replaces a rule.

## 1. Evidence before claims

A change is not complete because the code looks correct. Completion requires fresh verification evidence appropriate to the change.

- Test claims require a current successful test run.
- Build claims require a current successful build.
- CI claims require the actual GitHub checks to be green.
- Integration claims require reproducible evidence or clearly marked live probes.
- Unverified assumptions are documented as assumptions, not facts.

## 2. Test-driven development

Executable behavior is developed using RED -> GREEN -> REFACTOR.

1. Write the smallest test that describes the next behavior.
2. Run it and verify it fails for the expected reason.
3. Write the minimum production code needed to satisfy that test.
4. Run the focused test and then the relevant regression suite.
5. Refactor only while the suite remains green.

Production behavior must not be implemented first and covered retrospectively. Generated files and declarative configuration are exceptions only when there is no meaningful behavioral test-first cycle; they still require automated validation.

Tests should prove observable behavior rather than implementation details. Prefer real collaborators and Testcontainers over mocks when the real dependency is practical and deterministic.

## 3. Automation first

Repeated manual verification is technical debt.

The default test pyramid for this project is:

- unit tests for deterministic domain logic;
- module/architecture tests for Spring Modulith boundaries;
- integration tests against real PostgreSQL with Testcontainers;
- provider fixture/contract tests for every supported external integration;
- HTTP/API contract tests;
- component tests for web behavior;
- Playwright browser tests for critical user journeys and responsive smoke coverage;
- opt-in live probes only for checks that cannot be made deterministic offline.

A manual check is acceptable only when reliable automation is not technically reasonable, for example a third-party production-only behavior or genuinely visual judgement. Every recurring manual check should be documented together with why it cannot yet be automated.

No automated test may silently depend on a live retailer service in the normal CI path.

## 4. Documentation is part of the change

Documentation must describe repository reality, not intent that was never implemented.

The following files have distinct responsibilities:

- `README.md` — what the project is, current public status, and entry points.
- `docs/PROJECT_STATE.md` — factual snapshot of what is complete, in progress, blocked, and next.
- `docs/ROADMAP.md` — planned milestones and their evidence-based exit criteria.
- `docs/adr/` — durable architectural decisions and their status.
- `docs/superpowers/specs/` — approved product/technical specifications.
- `docs/superpowers/plans/` — executable implementation plans.
- `CHANGELOG.md` — notable changes that actually happened.
- `docs/DEVELOPMENT.md` — reproducible developer setup and verification workflow once executable code exists.

When a change affects one of these truths, that document changes in the same PR. Stale state documentation is treated as a defect.

## 5. Changelog discipline

`CHANGELOG.md` is maintained continuously under `[Unreleased]`.

Record changes that matter for understanding product or engineering history:

- user-visible behavior;
- architecture and public API changes;
- retailer integrations and meaningful compatibility changes;
- security changes;
- operational/deployment changes;
- important reliability or performance fixes.

Do not fill the changelog with mechanical formatting changes or internal refactors that do not alter behavior, architecture, security, or maintainability in a meaningful way.

Released entries must describe only changes that actually shipped in that release.

## 6. Git and pull requests

`main` represents reviewed repository truth.

- No direct feature development on `main`.
- Work happens on short-lived branches such as `feat/*`, `fix/*`, `chore/*`, `docs/*`, or `spike/*`.
- Prefer small cohesive commits with descriptive messages.
- Prefer small reviewable PRs that solve one coherent problem.
- Merge through PRs after required checks pass.
- Target merge strategy is squash-only with linear history.
- Never force-push protected `main` or delete it.
- Generated noise, secrets, local artifacts, and unrelated cleanup do not belong in a feature PR.

Each PR must state the problem, solution, non-goals, verification evidence, security/privacy impact, and documentation impact.

## 7. External integrations

Retailer integrations are volatile and must never leak their proprietary response models into the core domain.

Every provider implementation must have:

- a documented integration path and known legal/terms constraints;
- strict timeouts and explicit failure behavior;
- sanitized recorded fixtures for successful and relevant failure responses;
- parser/contract regression tests;
- explicit price/availability observation timestamps;
- no secrets or personal address data in fixtures or logs;
- an opt-in live probe separated from deterministic CI tests when live validation is useful.

A provider is not considered supported merely because one manual request succeeded.

## 8. Security and privacy by default

- Secrets never enter Git history.
- User addresses and precise locations are sensitive data.
- Logs and traces must not expose authorization material, provider credentials, or unnecessary personal/location data.
- Dependency, CodeQL, secret, and supply-chain checks become required gates when available.
- Security exceptions require an explicit written rationale; they are never silently ignored.

## 9. Quality gates

A normal functional PR is eligible to merge only when applicable checks succeed:

1. focused tests demonstrate the intended RED/GREEN behavior;
2. relevant unit/module/integration suites pass;
3. build and static checks pass;
4. API contracts remain synchronized;
5. critical browser flows pass when affected;
6. security/dependency checks pass;
7. documentation, `PROJECT_STATE.md`, and `CHANGELOG.md` are synchronized where applicable;
8. there are no known unexplained warnings or flaky tests introduced by the change.

Coverage percentage alone is not a quality goal. Tests must be capable of detecting realistic regressions.

## 10. Handling flakes and failures

A flaky test is a defect, not an acceptable CI condition. Do not normalize rerunning CI until it happens to pass.

When verification fails:

- determine the root cause;
- fix the product/test/environment issue;
- rerun the full relevant verification;
- document external platform incidents separately from product defects.

## 11. Scope discipline

Prefer the simplest design that proves the current requirement. Do not add infrastructure, abstractions, frameworks, or distributed components solely for possible future use.

New technology requires a measured need or an accepted ADR explaining why its benefit exceeds its operational and maintenance cost.
