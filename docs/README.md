# Documentation index

This directory is the durable documentation entry point for Zakup Gotov. Documents have explicit responsibilities so plans, decisions, current state, and historical changes do not get mixed together.

## Start here

- [`PROJECT_STATE.md`](PROJECT_STATE.md) — factual current project snapshot, accepted paths, open constraints and immediate work.
- [`ROADMAP.md`](ROADMAP.md) — milestone sequence and current M1 Shopping Core scope.
- [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md) — explicit M0 technical completion and M1 GO decision.
- [`DEVELOPMENT.md`](DEVELOPMENT.md) — local development and verification.
- [`ENGINEERING.md`](ENGINEERING.md) — mandatory TDD, verification, automation and documentation discipline.
- [`RELEASES.md`](RELEASES.md) — container/release verification and publication model.
- [`REPOSITORY_GOVERNANCE.md`](REPOSITORY_GOVERNANCE.md) — repository, merge, branch and security governance.
- [`OBSERVABILITY.md`](OBSERVABILITY.md) — telemetry, health and disclosure rules.

## Integration evidence

### Universal connectivity

- [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md) — permanent target-retailer coverage invariant and accepted acquisition modes.
- [`integrations/retailer-feasibility.md`](integrations/retailer-feasibility.md) — current retailer/provider feasibility matrix and production limitations.
- [`superpowers/plans/2026-08-10-m0b-provider-spikes.md`](superpowers/plans/2026-08-10-m0b-provider-spikes.md) — fixed M0B corpus and scorecard used for provider spikes.

### Pyaterochka

- [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md) — browser-bridge design, TDD evidence and security boundary.
- [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md) — final real-browser PASS evidence.

### Perekrestok

- [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md) — accepted reload-based browser-bridge path.
- [`integrations/perekrestok-browser-bridge-live-2026-08-10.md`](integrations/perekrestok-browser-bridge-live-2026-08-10.md) — initial real-browser mismatch and root cause.
- [`integrations/perekrestok-browser-bridge-live-2026-08-11.md`](integrations/perekrestok-browser-bridge-live-2026-08-11.md) — adapter-v2 live PASS.

### Magnit

- [`integrations/magnit-phase-a.md`](integrations/magnit-phase-a.md) — ordinary public-page Phase A hypothesis and acceptance contract.
- [`integrations/magnit-public-page-live-2026-08-12.md`](integrations/magnit-public-page-live-2026-08-12.md) — Phase A real live PASS.
- [`integrations/magnit-phase-b.md`](integrations/magnit-phase-b.md) — fixed 20-item Phase B parser, corpus and semantic contract.
- [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md) — complete Phase B live chronology and final `AVAILABLE_PUBLIC_WEB` technical decision for explicit store contexts.

Production follow-up remains explicit:

- issue #69 — safe location/address → Magnit public `shopCode` resolution;
- issue #70 — Magnit recurring production catalog usage-rights decision.

## Architecture decisions

See [`adr/`](adr/) for accepted platform and build decisions.

## Approved designs and implementation plans

- [`superpowers/specs/`](superpowers/specs/) — approved product and technical designs/decisions.
- [`superpowers/plans/`](superpowers/plans/) — executable implementation plans and historical delivery slices.

Repository-level contributor, security and history files live at the root, including [`../CHANGELOG.md`](../CHANGELOG.md), [`../CONTRIBUTING.md`](../CONTRIBUTING.md) and [`../SECURITY.md`](../SECURITY.md).

## Current milestone rule

M0 technical discovery is complete. M1 Shopping Core is the active milestone.

Do not treat M0 completion as permission to hide unresolved production access constraints. M1 must remain fixture-first, provenance-aware, coverage-explicit and fail-closed for availability, freshness and usage-rights uncertainty.
