# Pre-RC3 Documentation Synchronization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring canonical/public project documentation in sync with verified `main` before creating `v0.1.0-rc.3`.

**Architecture:** This is a docs-only synchronization. `PROJECT_STATE.md` remains the factual state snapshot, `ROADMAP.md` owns next-step sequencing, root `CHANGELOG.md` records notable Unreleased history, and `README.md` gives a concise public-facing summary. Runtime behavior and release workflows remain untouched.

**Tech Stack:** Markdown, GitHub pull requests, existing repository CI.

## Global Constraints

- No runtime, API, OpenAPI, generated client, provider behavior, database schema, release workflow, or dependency changes.
- Chizhik D1 implementation may be described as merged, but not as an accepted production acquisition path while the CI-hosted live probe remains `page-unavailable`.
- Technical accessibility must remain separate from production/right-to-operate approval.
- Ordinary CI must remain free of hidden live retailer traffic.
- M1–M5.1 accepted semantics must not be redefined.
- `v0.1.0-rc.3` remains the next mainline release validation; M5.2 remains intentionally unselected.

---

### Task 1: Synchronize canonical state and roadmap

**Files:**
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`

**Interfaces:**
- Consumes: merged `main` evidence through PR #153/#156/#158/#160/#164/#165/#166/#168 and issue #167.
- Produces: one consistent canonical description of current M5 status, completed bridge hardening, Chizhik A–D1 evidence, unresolved live D1 gate, and RC3 sequencing.

- [ ] **Step 1: Update `PROJECT_STATE.md` date and current-work summary**

Set `Updated:` to `2026-08-18`. Keep `Current phase: M5 — Productization` and `v0.1.0-rc.3` as the immediate operational target. Add a post-M5.1/current-connectivity subsection that records:

```text
#54/#153 browser-bridge persistent-session / SPA / store-change hardening — COMPLETE / ACCEPTED
Chizhik Phase A plain HTTPS probe — IMPLEMENTED / MERGED; negative/live evidence does not activate production
Chizhik Phase B browser discovery — IMPLEMENTED / MERGED; observation-only
Chizhik Phase C public catalog document probe + controlled workflow — IMPLEMENTED / MERGED; document reachability is not product/price connectivity
Chizhik Phase D1 active normal-browser foundation — IMPLEMENTED / MERGED; user-browser /api/v1/shops/ field canary succeeded, CI-hosted stock Chromium currently reports page-unavailable
Chizhik D2 store-scoped product/delivery offer mapping — NOT IMPLEMENTED / NEXT only after D1 transport disposition
```

- [ ] **Step 2: Remove stale outstanding #54 wording from `PROJECT_STATE.md`**

Replace the old parallel-work bullet that lists `#54` as outstanding with a completed-hardening statement and keep the remaining Kuper/Ozon Fresh/Samokat/Lenta/VkusVill work outstanding.

- [ ] **Step 3: Update `ROADMAP.md` parallel connectivity section**

Move #54 from outstanding work to completed operational hardening. Add the Chizhik D1 decision gate:

```text
- if reproducible CI-hosted stock Chromium obtains 200 + JSON + required store shape, a server-controlled browser worker remains viable;
- if CI-hosted Chromium remains blocked while an ordinary user browser succeeds, prefer the active MV3 Retailer Bridge path;
- only after that disposition, validate one store-scoped D2 product/delivery endpoint and map only evidenced fields to BrowserObservation/ObservedOffer.
```

- [ ] **Step 4: Preserve mainline sequencing in `ROADMAP.md`**

Keep `v0.1.0-rc.3` as `NEXT`, stable `v0.1.0` blocked until prerelease evidence is inspected, and M5.2 explicitly unselected until RC/manual-use evidence exists.

- [ ] **Step 5: Review Task 1 diff**

Expected: only the two Markdown files change; no statement claims Chizhik production activation or D1 acceptance beyond merged implementation.

- [ ] **Step 6: Commit Task 1**

```bash
git add docs/PROJECT_STATE.md docs/ROADMAP.md
git commit -m "docs: sync state and roadmap after Chizhik D1"
```

---

### Task 2: Synchronize changelog and public README

**Files:**
- Modify: `CHANGELOG.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: canonical state established in Task 1.
- Produces: public-facing project description and Unreleased history consistent with the canonical docs.

- [ ] **Step 1: Add Unreleased retailer-connectivity entries to `CHANGELOG.md`**

Under `## [Unreleased]`, record:

```text
Retailer Bridge long-lived session hardening for SPA/store changes, fresh-context gating, bounded resource evidence and revision-safe writes (#153).
Chizhik Phase A bounded plain HTTPS feasibility probe (#156).
Chizhik Phase B observation-only browser discovery (#158) and trusted main canary artifact publication (#160).
Chizhik Phase C bounded public catalog-document probe, controlled live workflow, and sanitized transport classification (#164/#165/#166).
Chizhik Phase D1 active browser-context store-directory foundation (#168), including strict fixed endpoint/shape validation and ordinary extension Chromium E2E.
```

State explicitly that the ordinary-user-browser `/api/v1/shops/` canary succeeded but current CI-hosted Phase D live status is `page-unavailable`, so D2/production offer acquisition remains unaccepted.

- [ ] **Step 2: Replace obsolete README status**

Change:

```text
Status: M0 — Product & Integration Discovery
```

into a current pre-release statement equivalent to:

```text
Status: M5 — Productization · pre-release · next release gate: v0.1.0-rc.3
```

- [ ] **Step 3: Replace obsolete README implementation summary**

Remove claims that Shopping Core, Recipes, matching and basket optimization are not implemented. Summarize accepted capabilities:

```text
M1 Shopping Core
M2 Recipes
M3 Weekly Planning + Pantry
M4 truthful checkout economics + deterministic basket optimization
M5.1 private browser-local WeeklyPlan/Pantry draft
```

Keep accounts/auth, server-side saved-plan history, analytics/feature flags/provider health, complete retailer production coverage and native mobile as future work.

- [ ] **Step 4: Update README retailer-connectivity paragraph**

Explain that product/core semantics are accepted, while retailer production coverage remains retailer-specific and evidence-gated. Mention that Perekrestok/Pyaterochka have accepted browser-bridge acquisition evidence, Magnit is technically reachable but production-blocked by project operating policy, and Chizhik is under active D1/D2 validation.

- [ ] **Step 5: Update README roadmap bullets**

Mark M0–M4 complete, M5 current, and M6 future. Keep `v0.1.0-rc.3` as the immediate release gate.

- [ ] **Step 6: Review Task 2 diff**

Expected: README no longer contradicts `PROJECT_STATE.md`; changelog distinguishes merged implementation from live/production acceptance.

- [ ] **Step 7: Commit Task 2**

```bash
git add CHANGELOG.md README.md
git commit -m "docs: refresh public status before rc3"
```

---

### Task 3: Verify documentation consistency and ship PR

**Files:**
- Review: `docs/PROJECT_STATE.md`
- Review: `docs/ROADMAP.md`
- Review: `CHANGELOG.md`
- Review: `README.md`
- Review: `docs/superpowers/specs/2026-08-18-pre-rc3-documentation-sync-design.md`
- Review: `docs/superpowers/plans/2026-08-18-pre-rc3-documentation-sync.md`

**Interfaces:**
- Consumes: Tasks 1–2.
- Produces: reviewable docs-only PR ready for standard repository CI.

- [ ] **Step 1: Search final branch content for stale contradictions**

Verify all of the following:

```text
README does not say Status: M0
README does not say shopping-list domain / recipes / matching / basket optimization are unimplemented
PROJECT_STATE/ROADMAP do not list #54 as outstanding
all four docs keep v0.1.0-rc.3 as the next mainline release gate
all four docs avoid claiming Chizhik production activation
M5.2 remains unselected
```

- [ ] **Step 2: Compare branch against `main`**

Expected changed paths are limited to:

```text
README.md
CHANGELOG.md
docs/PROJECT_STATE.md
docs/ROADMAP.md
docs/superpowers/specs/2026-08-18-pre-rc3-documentation-sync-design.md
docs/superpowers/plans/2026-08-18-pre-rc3-documentation-sync.md
```

- [ ] **Step 3: Open a docs-only pull request**

Use title:

```text
docs: synchronize project state before v0.1.0-rc.3
```

PR body must state that runtime/config/dependencies are unchanged, summarize #54 completion and Chizhik A–D1 evidence, and call out the unresolved `page-unavailable` D1 live result.

- [ ] **Step 4: Wait for exact-head standard PR CI and inspect all workflow groups**

Expected: required repository workflow groups complete successfully. A controlled provider-live status is evidence, not an ordinary PR build gate, unless the PR explicitly changes that workflow.

- [ ] **Step 5: Merge only on green exact head**

Use squash merge with expected-head protection.

- [ ] **Step 6: Verify post-merge `main` CI**

Confirm normal `main` push workflows complete successfully before considering documentation synchronization accepted.
