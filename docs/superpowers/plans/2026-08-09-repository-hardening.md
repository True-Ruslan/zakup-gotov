# Repository Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make GitHub Actions and repository governance safe enough to become enforced `main` gates without creating missing-check deadlocks or weakening existing verification.

**Architecture:** Keep existing API, Contract, Web, and Web E2E checks as independent recurring gates, but make their PR presence deterministic and their action dependencies immutable. Complete GitHub-native security gates separately in PR #8, then activate repository-admin settings/rulesets only after every required check name has been observed green.

**Tech Stack:** GitHub Actions, GitHub Rulesets, GitHub Advanced Security features available to public repositories, Dependabot, CodeQL, Dependency Review, `gh api` for repository-admin settings not exposed by the connected GitHub app.

## Global Constraints

- Do not weaken a failing security check merely to obtain GREEN.
- Permanent actions are pinned to full commit SHA; comments retain the human-readable major tag.
- Permanent verification workflows use read-only token permissions unless a job has a documented write requirement.
- `actions/checkout` does not persist credentials in read-only workflows.
- Required PR checks must be emitted predictably on every pull request before a ruleset requires them.
- Normal CI remains deterministic and never performs live retailer requests.
- Documentation and `[Unreleased]` changelog stay synchronized with actual repository state.

---

### Task 1: Harden recurring CI and make required-check presence deterministic

**Files:**
- Modify: `.github/workflows/api-ci.yml`
- Modify: `.github/workflows/contract-ci.yml`
- Modify: `.github/workflows/web-ci.yml`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Produces stable PR checks: `API CI`, `Contract CI`, `Web CI`, `Web E2E`.
- Uses immutable action SHAs observed from actual successful repository runs.

- [ ] **Step 1: Remove PR path filters from the three recurring workflows**

Every pull request must emit the same check names before these checks become ruleset requirements. Push-to-main execution may also remain unconditional for simplicity and repository-wide regression confidence at the current project size.

- [ ] **Step 2: Pin permanent actions by full commit SHA**

Use the SHAs verified from actual GitHub Actions runs:

```text
actions/checkout@v6    d23441a48e516b6c34aea4fa41551a30e30af803
actions/setup-java@v5  b6effb05e454b25005698d916606bdc6ffcbf961
actions/setup-node@v6  249970729cb0ef3589644e2896645e5dc5ba9c38
actions/cache@v5       caa296126883cff596d87d8935842f9db880ef25
```

Workflow syntax should retain comments such as `# v6` so Dependabot/reviewers can identify the intended release line.

- [ ] **Step 3: Disable persisted checkout credentials**

For every read-only checkout:

```yaml
with:
  persist-credentials: false
```

- [ ] **Step 4: Strengthen runtime behavior**

Add a workflow-level `concurrency` group that cancels superseded runs for the same PR/ref and finite job `timeout-minutes`. Change API CI from Maven `test` to `verify` so its required check matches the repository's one-command verification semantics.

- [ ] **Step 5: Open a draft PR and observe all four checks**

Expected: the documentation/workflow PR itself emits `API CI`, `Contract CI`, `Web CI`, and `Web E2E`, proving no required-check deadlock exists for non-application changes.

- [ ] **Step 6: Inspect actual logs**

Expected: action downloads resolve to exactly the pinned SHAs, checkout shows `persist-credentials: false`, backend uses Java 25/PostgreSQL Testcontainers and Maven `verify`, and all four checks finish GREEN.

- [ ] **Step 7: Synchronize state/changelog and squash-merge**

Only merge after the current PR head has all four checks green.

---

### Task 2: Harden PR #8 security workflows and complete GitHub-native security gates

**Files (branch `ci/m0a-security-gates` / PR #8):**
- Modify: `.github/workflows/codeql.yml`
- Modify: `.github/workflows/dependency-review.yml`
- Modify: `.github/dependabot.yml` only if validation reveals a concrete issue
- Modify: `docs/PROJECT_STATE.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Produces stable checks: `CodeQL / Java`, `CodeQL / JavaScript-TypeScript`, `Dependency Review`.

- [ ] **Step 1: Pin security workflow actions to verified full SHAs**

Use repository-run evidence:

```text
actions/checkout@v6                d23441a48e516b6c34aea4fa41551a30e30af803
github/codeql-action@v4            5595ccaf912efad79be6eef63a5619ff05969be3
actions/dependency-review-action@v4 2031cfc080254a8a887f58cffee85186f0e49e48
```

Set checkout `persist-credentials: false`.

- [ ] **Step 2: Enable repository Dependency Graph/security prerequisites through repository-admin settings**

Do not change the workflow to skip. Re-run the existing failed Dependency Review job only after the repository setting is enabled.

- [ ] **Step 3: Verify security checks on the current PR head**

Expected: CodeQL Java GREEN, CodeQL JavaScript/TypeScript GREEN, Dependency Review GREEN.

- [ ] **Step 4: Synchronize docs/changelog and squash-merge PR #8**

Task is incomplete while Dependency Review is red for any reason.

---

### Task 3: Activate repository-admin governance after exact checks are proven

**Files:**
- Modify documentation only if observed GitHub behavior differs from `docs/REPOSITORY_GOVERNANCE.md`.

**Interfaces:**
- Consumes exact successful check names from Tasks 1-2.
- Produces merge policy, Actions default permissions, security toggles, topics, automatic branch cleanup, and an active `main` ruleset.

- [ ] **Step 1: Configure repository metadata and merge policy with `gh api`**

Target: issues on, wiki/projects off until needed, squash-only, auto-merge on, delete merged branches, update-branch support, concise repository description/topics.

- [ ] **Step 2: Set Actions token defaults**

Set `default_workflow_permissions=read` and `can_approve_pull_request_reviews=false`.

- [ ] **Step 3: Enable security features**

Enable Dependabot alerts/security updates, secret scanning, push protection, and Private Vulnerability Reporting where GitHub accepts them for the public repository. Dependency Graph must be operational because Dependency Review has already proven the dependency.

- [ ] **Step 4: Create active `main` ruleset**

Require pull requests, linear history, resolved conversations, block deletion/non-fast-forward, and require the exact seven proven checks:

```text
API CI
Contract CI
Web CI
Web E2E
CodeQL / Java
CodeQL / JavaScript-TypeScript
Dependency Review
```

Do not require a second-human approval while the repository has one maintainer.

- [ ] **Step 5: Delete historical merged branches**

Keep only `main` plus active pull-request branches. Verify the branch list afterwards.

- [ ] **Step 6: Re-open a harmless docs PR or use the next real PR to prove ruleset behavior**

Expected: direct merge cannot bypass required checks, all required checks appear, and squash merge remains possible after GREEN.

---

### Task 4: Prepare social-preview handoff

**Files:**
- No repository source file is required unless the owner chooses to retain brand assets under `docs/assets/` later.

**Interfaces:**
- Produces a GitHub Social Preview asset and exact manual upload path because GitHub does not expose this setting through the connected repository toolset.

- [ ] **Step 1: Generate a simple product mark/social-preview asset**

Design direction: grocery basket + check/price signal, minimal geometry, no small text, high contrast, readable when reduced, neutral modern consumer-tech character.

- [ ] **Step 2: Owner uploads it in GitHub repository Settings → General → Social preview**

Do not claim the preview is configured until GitHub shows it on the repository page/share preview.

---

## Self-review results

- **Spec coverage:** repository hygiene, least-privilege Actions, immutable pins, deterministic required checks, GitHub-native security, ruleset/merge policy, branch lifecycle, and social-preview handoff are covered. Docker/GHCR release implementation is intentionally a separate plan because it is an independent executable subsystem.
- **Placeholder scan:** no TODO/TBD implementation placeholders remain.
- **Check consistency:** required-check names match existing workflow job names and observed CodeQL/Dependency Review job names.

## Completion gate

Repository hardening is complete only when recurring CI and security workflows are green with immutable action pins, GitHub security/admin settings are active, `main` ruleset protects the exact proven checks without path-filter deadlocks, merged branches are cleaned, and repository state/changelog describe the observed result rather than the intended configuration.
