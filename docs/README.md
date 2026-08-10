# Documentation index

This directory is the durable documentation entry point for Zakup Gotov. Documents have explicit responsibilities so plans, decisions, current state, and historical changes do not get mixed together.

## Start here

| Purpose | Document |
|---|---|
| Current factual state | [`PROJECT_STATE.md`](PROJECT_STATE.md) |
| Product and engineering roadmap | [`ROADMAP.md`](ROADMAP.md) |
| Universal retailer connectivity design | [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md) |
| Retailer integration feasibility evidence | [`integrations/retailer-feasibility.md`](integrations/retailer-feasibility.md) |
| Mandatory Pyaterochka/Perekrestok strategy | [`integrations/x5-mandatory-coverage.md`](integrations/x5-mandatory-coverage.md) |
| Perekrestok browser-bridge Phase A evidence | [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md) |
| Local setup and verification | [`DEVELOPMENT.md`](DEVELOPMENT.md) |
| Container/release workflow | [`RELEASES.md`](RELEASES.md) |
| Mandatory engineering policy | [`ENGINEERING.md`](ENGINEERING.md) |
| Repository governance and security controls | [`REPOSITORY_GOVERNANCE.md`](REPOSITORY_GOVERNANCE.md) |
| Operational telemetry and privacy baseline | [`OBSERVABILITY.md`](OBSERVABILITY.md) |

Repository-level contributor/security/history files live at the root:

- [`../README.md`](../README.md) — public project overview;
- [`../CHANGELOG.md`](../CHANGELOG.md) — notable changes that actually happened;
- [`../CONTRIBUTING.md`](../CONTRIBUTING.md) — contribution workflow;
- [`../SECURITY.md`](../SECURITY.md) — vulnerability reporting and security policy;
- [`../CODE_OF_CONDUCT.md`](../CODE_OF_CONDUCT.md) — collaboration expectations.

## Architectural decisions

Durable decisions live in [`adr/`](adr/).

Current accepted foundation decisions include:

- [`adr/0001-platform-stack.md`](adr/0001-platform-stack.md) — long-term platform stack;
- [`adr/0002-build-and-workspace-tooling.md`](adr/0002-build-and-workspace-tooling.md) — repository build/toolchain conventions.

An ADR records **why a durable decision was made**. It should not be used as a task tracker or current-state file.

## Specifications

Approved product/technical designs live in [`superpowers/specs/`](superpowers/specs/).

Specifications define intended behavior and boundaries before implementation. Once approved, implementation should follow them or explicitly supersede them with a new decision.

## Implementation plans

Executable task breakdowns live in [`superpowers/plans/`](superpowers/plans/).

Plans describe **how an approved specification will be implemented**. Completion claims still depend on current automated verification and `PROJECT_STATE.md`, not merely checked boxes in an old plan.

## Documentation rules

The repository follows these rules:

1. `PROJECT_STATE.md` describes repository reality now.
2. `ROADMAP.md` describes future milestones and exit criteria.
3. ADRs describe durable architectural decisions.
4. Specifications describe approved designs.
5. Plans describe implementation sequencing.
6. `CHANGELOG.md` records notable changes that actually happened.
7. A PR that changes one of these truths updates the relevant document in the same PR.

Stale documentation is treated as a defect. See [`ENGINEERING.md`](ENGINEERING.md) for the complete policy.
