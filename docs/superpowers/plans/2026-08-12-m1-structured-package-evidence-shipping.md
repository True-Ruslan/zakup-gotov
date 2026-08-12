# M1 Structured Package Evidence — Shipping Evidence

Date: 2026-08-12  
PR: #81 `feat(m1): add structured package evidence plumbing`  
Branch: `feat/m1-structured-package-evidence`

This document is the docs-only shipping marker for the M1 structured package-evidence plumbing slice.

## Accepted scope

The slice establishes one provider-neutral internal evidence path for package quantities that have already been proven by a source as structured product metadata.

- `ObservedOffer` can carry optional canonical `Quantity packageQuantity` evidence.
- Existing observation construction remains source-compatible and defaults to empty package evidence.
- `OfferSnapshot` preserves the optional package quantity through immutable snapshot creation.
- `PackageQuantitySet.fromSnapshots(...)` projects only explicitly present snapshot evidence into basket bindings.
- Runtime comparison evidence derives package bindings from snapshots and rejects any separately supplied package set that disagrees with those snapshots.
- Deterministic acceptance fixtures attach package quantity at the provider observation boundary before snapshotting.
- Presentation text remains non-authoritative; tests explicitly prove names such as `Молоко 3,2%, 970мл` and `Вода 1,5л` do not create package evidence.

This slice does **not** claim a retailer/source extractor, does not change the public API/UI, does not add live retailer requests, does not broaden browser permissions, and does not change production-access state. Perekrestok, Pyaterochka and the other accepted retailer paths remain package-unknown until a source-specific field is separately proven.

## TDD and hardening completed before acceptance

1. Initial test-only RED required a package-aware provider observation, snapshot preservation and snapshot-to-basket projection; API CI failed on exactly the missing constructor/accessors/factory.
2. Minimal provider/snapshot/basket GREEN was implemented. Candidate `81e419e1805056354432b866d3b30d01cd8f3bbc` passed all repository workflow groups.
3. Review of the runtime path found that `RetailerRuntimeEvidence` still accepted a second independent `PackageQuantitySet`. A new test-only RED required disagreement between snapshots and parallel bindings to fail closed.
4. Runtime evidence was hardened to derive package quantities from snapshots and to reject explicit sets that differ from snapshot evidence.
5. The hardening correctly exposed one legacy `ComparisonPreviewServiceTest` fixture that still injected bindings after snapshotting. That fixture and the shared deterministic runtime source were migrated to attach package quantities to `ObservedOffer` before snapshot creation; the production invariant was not weakened.
6. Durable `PROJECT_STATE.md`, `ROADMAP.md` and root `CHANGELOG.md` were synchronized with the implemented plumbing and the explicit limitation that no production retailer extractor is yet accepted.

## Exact reviewed code/docs candidate

Reviewed candidate SHA: `715fcdbb7821311f4490f0dfd8b70904deb4bd82`

All required workflow groups completed successfully on that exact candidate:

- API CI — PASS
- Contract CI — PASS
- Web CI — PASS
- Web E2E — PASS as part of Web CI
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- CodeQL — PASS
- Container Security CI — PASS
- Release Bundle CI — PASS
- Release Contract CI — PASS

The API verify includes Spring Modulith/ArchUnit verification, provider/snapshot regressions, package projection tests, basket planner regressions, preview runtime/service tests and integration tests. Ordinary CI continues to make no live retailer requests.

## Read-only review gate

Verdict: **Looks good**

- P0: none
- P1: none
- P2: none
- P3: `RetailerRuntimeEvidence` retains compatibility overloads that accept an explicit `PackageQuantitySet`. Correctness is protected because the constructor requires exact equality with snapshot-derived bindings and then replaces the field with the canonical snapshot-derived set. After callers have fully migrated, these overloads should be deprecated/removed so the redundant channel disappears from the API surface entirely.

Review scope included:

- specification/non-goal compliance;
- legacy `ObservedOffer` source compatibility;
- canonical quantity semantics and immutability;
- observation → snapshot → basket package-evidence flow;
- title/presentation-text non-inference;
- runtime disagreement/fail-closed behavior;
- deterministic acceptance fixture migration;
- Spring Modulith/ArchUnit dependency direction;
- public API/privacy/security surface;
- live-network and browser-permission boundaries;
- production-access semantics;
- durable state/roadmap/changelog consistency.

No blocking security, privacy, API-compatibility, architecture or correctness issue was found.

## Final branch-protection gate

This marker changes documentation only. The final PR head after this commit must re-run and pass the repository branch-protection workflow set before squash merge. Merge must use the exact final head SHA; if the head changes, the gate is invalid and must be repeated.

## Post-merge expectation

After squash merge, verify the resulting `main` SHA and push-triggered workflows.

The next M1 engineering step is **not** name parsing. It is source-specific evidence research: prove one authoritative structured package field for one accepted retailer/source, document its semantics/provenance/absence behavior and production-access constraints, and only then implement an extractor. Browser permissions and response interception must not be broadened merely to obtain package size.
