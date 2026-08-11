# Documentation index

This directory is the durable documentation entry point for Zakup Gotov. Documents have explicit responsibilities so plans, decisions, current state, and historical changes do not get mixed together.

## Start here

- [`PROJECT_STATE.md`](PROJECT_STATE.md) — factual current project snapshot, accepted paths, open constraints and immediate work.
- [`ROADMAP.md`](ROADMAP.md) — milestone sequence and current M1 Shopping Core scope.
- [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md) — explicit M0 technical completion and M1 GO decision.
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — platform and module boundaries.
- [`DEVELOPMENT.md`](DEVELOPMENT.md) — local development and verification.
- [`RELEASES.md`](RELEASES.md) — container/release verification and publication model.
- [`REPOSITORY_GOVERNANCE.md`](REPOSITORY_GOVERNANCE.md) — repository/merge/branch/security governance.

## Integration evidence

### Universal connectivity

- [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md) — permanent target-retailer coverage invariant and accepted acquisition modes.
- [`integrations/retailer-feasibility.md`](integrations/retailer-feasibility.md) — current retailer/provider feasibility matrix and production limitations.
- [`superpowers/plans/2026-08-10-m0b-provider-spikes.md`](superpowers/plans/2026-08-10-m0b-provider-spikes.md) — fixed M0B corpus and scorecard used for provider spikes.

### Pyaterochka

- [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md) — browser-bridge design/TDD/security boundary.
- [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md) — final live PASS evidence.

### Perekrestok

- [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md) — accepted reload-based browser-bridge path.
- [`integrations/perekrestok-browser-bridge-live-2026-08-10.md`](integrations/perekrestok-browser-bridge-live-2026-08-10.md) — initial real-browser mismatch/root cause.
- [`integrations/perekrestok-browser-bridge-live-2026-08-11.md`](integrations/perekrestok-browser-bridge-live-2026-08-11.md) — adapter-v2 live PASS.

### Magnit

- [`integrations/magnit-phase-a.md`](integrations/magnit-phase-a.md) — ordinary public-page Phase A hypothesis and acceptance.
- [`integrations/magnit-public-page-live-2026-08-12.md`](integrations/magnit-public-page-live-2026-08-12.md) — Phase A real live PASS.
- [`integrations/magnit-phase-b.md`](integrations/magnit-phase-b.md) — fixed 20-item Phase B parser/corpus/semantic contract and final technical status.
- [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md) — complete Phase B live chronology and final `AVAILABLE_PUBLIC_WEB` decision for explicit store contexts.

Production follow-up remains explicit:

- issue #69 — safe location/address → Magnit public `shopCode` resolution;
- issue #70 — Magnit recurring production catalog usage-rights decision.

## Engineering policy and operations

- [`ENGINEERING_POLICY.md`](ENGINEERING_POLICY.md) — TDD, verification, automation and documentation discipline.
- [`OBSERVABILITY.md`](OBSERVABILITY.md) — telemetry and disclosure rules.
- [`TESTING.md`](TESTING.md) — test strategy where present/applicable.
- root [`../CHANGELOG.md`](../CHANGELOG.md) — notable project history.

## Architecture decisions

See [`adr/`](adr/) for accepted architecture/build/platform decisions.

## Specs and implementation plans

- [`specs/`](specs/) — durable product/technical specifications.
- [`superpowers/specs/`](superpowers/specs/) — approved design/decision documents produced during implementation work.
- [`superpowers/plans/`](superpowers/plans/) — implementation plans and execution slices.

## Current milestone rule

M0 technical discovery is complete. M1 Shopping Core is the active milestone.

Do not treat M0 completion as permission to hide unresolved production access constraints. M1 must remain fixture-first, provenance-aware, coverage-explicit and fail-closed for availability/freshness/usage-rights uncertainty.
