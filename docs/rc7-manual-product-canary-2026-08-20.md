# v0.1.0-rc.7 manual product canary

Status: **EVIDENCE HARNESS IMPLEMENTED / RUNTIME EVIDENCE AND MANUAL VERDICT PENDING**

Immutable release source:

```text
v0.1.0-rc.7
b754f5193f852db0312011f3f6c3ec6c7dd22eb2
```

The canary exercises the published `rc.7` release bundle. The final API and Web application images are digest-pinned by the release evidence. The published Compose still names PostgreSQL as `postgres:18.4-alpine`; the harness therefore records the exact RepoDigest actually pulled for PostgreSQL so the manual canary run remains auditable without rewriting the immutable rc.7 artifact.

The harness does not rebuild application images, move tags, mutate OCI `latest`, or treat automation as manual acceptance.

## Trigger

After the evidence harness is merged to the default branch, the repository owner starts it by posting this exact comment on issue #152:

```text
/release-canary rc.7
```

`.github/workflows/release-product-canary.yml` accepts only that owner-authored command on issue #152. The evidence job has only `contents: read` + `packages: read`; the separate notification job has only `issues: write`. Neither job has package-write, attestation, OIDC or release-mutation permission.

### Initial trigger validation failure

The first owner command posted after PR #183 did not start the evidence jobs. GitHub rejected the workflow before runner execution because `jobs.canary.env` attempted to resolve `${{ runner.temp }}`. GitHub's context-availability contract does not make the `runner` context available at that workflow key.

Recovery PR #184 keeps the same trigger, permissions and immutable rc.7 binding, but resolves the temporary paths inside the first runner step from the standard `$RUNNER_TEMP` environment variable and exports them to later steps through `$GITHUB_ENV`. A regression contract now fails if `${{ runner.temp }}` is reintroduced into this workflow.

The failed pre-run validation produced no product evidence and therefore does not count as a canary attempt or acceptance signal.

## Immutable input verification

Before starting the product, the workflow:

1. proves `v0.1.0-rc.7` still resolves to `b754f5193f852db0312011f3f6c3ec6c7dd22eb2`;
2. proves GitHub Release metadata is still `draft=false`, `prerelease=true`;
3. downloads only `compose.release.yaml`, `release-verification.json` and `SHA256SUMS` from the published rc.7 release;
4. verifies the downloaded evidence against `SHA256SUMS`;
5. rejects Compose application images unless exactly the final API and Web packages are GHCR digest references;
6. rejects application `build:` directives, mutable application-image variables and application `latest` references;
7. pulls and starts the published bundle without building application images;
8. resolves the actually running PostgreSQL image to its immutable `RepoDigest` and stores that dependency evidence in the report/artifact.

This does not retroactively make the rc.7 Compose fully digest-pinned: the PostgreSQL reference in that immutable artifact remains a version tag. Whether to require digest-pinning of non-application runtime dependencies before stable `v0.1.0` is a separate release-hardening decision informed by this canary evidence.

## Evidence scenarios

The Playwright review harness uses synthetic product inputs only and captures:

1. WeeklyPlan → Pantry → comparison → truthful optimization state on desktop, including explicit unknown delivery/non-comparability behavior;
2. private local draft save → reload → restore → explicit clear;
3. Recipe comparison;
4. manual-list comparison;
5. narrow/mobile layout at 390×844, including horizontal-overflow assertion;
6. explicit API-unavailable fail-closed state;
7. API recovery followed by Web restart and a successful comparison.

The normal, API-unavailable and recovered phases are collected independently so partial evidence survives a later phase failure. Any failed UI scenario also attempts a full-page failure screenshot before teardown.

## Evidence artifact

Each run uploads an artifact named:

```text
rc7-manual-product-canary-<workflow-run-id>
```

It contains, at minimum when the corresponding phases execute:

- `release-canary-report.json` with `verdict: manual-review-required`;
- normal and failure screenshots for the product scenarios as applicable;
- exact release tag and source SHA;
- the two digest-pinned application image references;
- `postgres-runtime-image.json` with the exact pulled PostgreSQL image ID/RepoDigest;
- downloaded `compose.release.yaml`, `release-verification.json` and `SHA256SUMS`;
- Compose runtime state before/after the canary.

The workflow posts the Actions run URL and phase outcomes back to issue #152. A failed phase keeps stable release acceptance blocked while preserving the evidence artifact for diagnosis.

## Manual review boundary

Automation proves reproducibility and captures the real immutable-release UI states; it does **not** make the product-acceptance decision.

The reviewer must inspect the screenshots/report for coherent user-visible behavior, responsive layout, truthful unavailable/error presentation, draft UX, optimization honesty and successful recovery. Only after that review may issue #152 record PASS/FAIL for the manual product canary.

Stable `v0.1.0` remains blocked until issue #152 records a satisfactory manual review against the immutable rc.7 evidence. A green evidence-capture workflow by itself is **not** an acceptance verdict.
