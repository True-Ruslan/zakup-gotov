# M3.5.3 Pantry-aware WeeklyPlan → Comparison — Shipping Evidence

Date: 2026-08-15
Issue: #127
PR: #128
Baseline main: `f00595baad4d14a2dbc939aa4826e7e44e8b3148`

## Scope

M3.5.3 adds one new stateless composition boundary:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

It delegates WeeklyPlan + Pantry semantics to accepted M3.5.2, compares only remaining demand, and returns an explicit `COMPARED | NO_REMAINING_DEMAND` outcome. Full Pantry coverage does not invoke `ComparisonPreviewService` and therefore does not trigger retailer runtime acquisition merely to satisfy a non-empty comparison contract.

Existing M3.3 and M3.5.2 endpoints are unchanged.

## Authoritative design and plan

- design: `docs/superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md`
- implementation plan: `docs/superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md`
- design commit: `4fb27b9dc2501780eb1d096eea4d52bfd2fce6f0`
- initial implementation-plan commit: `3813721994f2ef4482d277c76e1778c046dd41fc`
- hardening design synchronization: `fbf05770072a33634dff739952074a7865da4381`

The final design explicitly records two hardening findings discovered during implementation: accepted M3.5.2 exposes canonical `shopping.Quantity` vocabulary to the composition layer, and accepted downstream ComparisonPreview validation is sanitized into `comparison.*` fields rather than leaking as a 500.

## RED → GREEN evidence

### Service composition / zero demand

RED: `5430df3b20f15a38ccf9b0322dbb472f6d5d734f`

API CI failed in `Run API verification` with compile errors for the intentionally missing M3.5.3 production types. The test contract required partial Pantry comparison, zero-demand short-circuit with comparison call count zero, empty-Pantry identity behavior, locality validation and fail-closed projection drift.

GREEN: `23ae9f2fb82753f03cfeb2c1c72c673d5530bdee`

Full Maven verification succeeded with the new service/value objects. The implementation delegates M3.5.2 exactly once, validates locality before the zero-demand branch, skips comparison on empty remaining demand and verifies comparison projection drift on non-empty demand.

### HTTP boundary

RED: `cc21cc13ad903606b326ce68f0678ddadf5e950f`

API CI failed because the intentionally required M3.5.3 controller and exception-handler boundary did not yet exist.

GREEN: `6bf4831da26497f520c58ee4f30ed46e4c848428`

Full API CI succeeded with deterministic MockMvc coverage for `COMPARED`, `NO_REMAINING_DEMAND`, invalid locality under full Pantry coverage, nested Pantry validation, malformed JSON, unknown top-level fields and unsupported units.

### OpenAPI / generated TypeScript client

RED: `b3dc52554321fd1d2ca2192b8d3ef73c1d59af12`

Contract CI proved existing generated-schema freshness still passed while TypeScript typecheck failed specifically because the new path, operation and M3.5.3 schemas did not exist.

GREEN contract artifacts were generated exactly from OpenAPI, not hand-edited:

- generated OpenAPI/schema commit: `cf1d3d5dc920d838fdfcc6354899e3b8b117522b`
- clean feature head after removing the temporary generation helper: `41c36dc1c2bd227968eb26fdcd5db8d01584081d`

On `41c36dc1...`, Contract CI passed generated-schema freshness, TypeScript typecheck, Vitest and API-client build.

#### Generation-tooling note

Because the GitHub connector has no safe partial-file patch primitive for the large OpenAPI document and local package tooling was unavailable, a temporary feature-branch-only workflow applied a deterministic patch and ran the repository-pinned `openapi-typescript 7.13.0` generator. Its first attempt intentionally failed fast on invalid YAML indentation before any generated contract commit was created. The patch was corrected, exact generation succeeded, and the temporary workflow was immediately deleted. It is not part of the final PR scope.

### Architecture gate

First gate head: `2d438838ca1138d8d90229c2621d68a556560b80`

The first architecture run exposed two test-rule defects rather than production coupling:

1. the allowlist omitted canonical `shopping.Quantity`, even though that type is exposed by accepted M3.5.2 remaining ShoppingItems and is only read by M3.5.3;
2. a broad `..web..` ArchUnit pattern matched `org.springframework.web`, not a project browser/UI package.

The gate was corrected at `307a42d8a1c5ab5e866e3306f0234bf2ab4bd670` to use project-root dependency analysis, explicitly allow read-only canonical Shopping quantity vocabulary, retain strict provider/retailer/database/domain-owner exclusions and protect M3.3/M3.5.2 against reverse dependencies.

The corrected architecture tests passed on the subsequent API run at `66edf205...` and remain part of the final Maven gate.

### Derived ComparisonPreview validation hardening

RED: `66edf2052e089bef147b31441e25086c6ccef817`

Full Maven execution reached the new service test and failed exactly because an accepted `InvalidComparisonPreviewRequestException` (for example, derived remaining demand above the accepted 100-item comparison limit) escaped raw instead of being mapped into the M3.5.3 sanitized problem boundary. All four corrected M3.5.3 architecture tests passed in this same run.

GREEN: `63588570740ce50784bd150eb0be94437f2f98c0`

Full Maven verification succeeded after mapping accepted downstream comparison validation to `comparison.*` field paths. Internal composition drift remains fail-closed and is not converted to caller validation.

## Accepted invariants implemented

- original WeeklyPlan Shopping projection and Pantry evidence remain inspectable through the complete M3.5.2 result;
- only remaining ShoppingItems enter comparison;
- exact UUID/order/requirement/canonical quantity is preserved;
- fully covered demand returns 200 `NO_REMAINING_DEMAND` with no comparison payload;
- zero demand does not evaluate the comparison creator;
- locality validity is independent of Pantry coverage;
- accepted M3.5.2 validation and accepted derived ComparisonPreview validation are sanitized;
- comparison projection cardinality/identity/order/requirement/quantity drift fails closed;
- no Pantry persistence, exclusions, browser controls or provider/acquisition behavior was added;
- ordinary tests do not make live retailer requests.

## Final acceptance protocol

The exact final feature head is certified externally in PR #128 rather than written back into this file, because recording that SHA here would itself mutate the head being certified.

Required before merge:

1. exactly 9 normal PR workflow groups on the final head;
2. 9/9 SUCCESS and zero failure/skipped/cancelled;
3. independent read-only review on that exact head with no P0/P1/P2/P3/nitpicks;
4. zero unresolved review threads;
5. mergeable=true;
6. squash merge with expected-head protection.

Required after implementation merge:

1. issue #127 closed `completed`;
2. exactly 8 normal `main` push workflow groups on the merge SHA;
3. 8/8 SUCCESS and zero failures;
4. separate docs-only acceptance PR synchronizing PROJECT_STATE, ROADMAP and CHANGELOG and advancing the deterministic roadmap to M3.5.4 Responsive Pantry controls.
