# M3.5.3 Pantry-aware WeeklyPlan → Comparison acceptance

Date: 2026-08-15

Status: **COMPLETE / ACCEPTED**

## Scope

M3.5.3 adds a new stateless Pantry-aware WeeklyPlan → Comparison composition boundary without changing the accepted M3.3 or M3.5.2 endpoints.

Accepted endpoint:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

## Accepted semantics

- request = provider-neutral locality + accepted M3.5.2 WeeklyPlan/Pantry vocabulary;
- accepted M3.5.2 remains authoritative for WeeklyPlan construction, original Shopping projection, Pantry adjustment evidence and remaining demand;
- only non-empty remaining demand is adapted into accepted ComparisonPreview;
- ShoppingItem UUID, order, normalized requirement and canonical quantity are preserved exactly across the M3.5.2 → ComparisonPreview bridge;
- bridge drift in cardinality, identity/order, requirement or quantity fails closed;
- fully Pantry-covered demand returns explicit `NO_REMAINING_DEMAND` and does not invoke ComparisonPreviewService/runtime retailer acquisition;
- zero-demand wire output omits `comparisonPreview` rather than serializing `null`;
- locality remains independently validated even when Pantry covers all demand;
- derived ComparisonPreview validation is translated into sanitized M3.5.3 problem details instead of leaking an internal 500;
- original weekly projection, Pantry evidence and remaining demand stay inspectable in the response;
- OpenAPI 3.1 and the generated TypeScript client are synchronized;
- architecture guards prevent provider/retailer/database/domain-owner coupling and protect accepted M3.3/M3.5.2 reverse dependency direction;
- no persistence, explicit omit-all exclusions, browser Pantry controls or provider/acquisition changes are part of this slice;
- ordinary CI makes no live retailer request.

## Evidence

Baseline main before M3.5.3: `f00595baad4d14a2dbc939aa4826e7e44e8b3148`.

Authoritative design:
`docs/superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md`

Implementation plan:
`docs/superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md`

Shipping evidence:
`docs/superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md`

TDD checkpoints:

- application RED `5430df3b...` → GREEN `23ae9f2f...`;
- HTTP RED `cc21cc13...` → GREEN `6bf4831d...`;
- contract RED `b3dc5255...` → generated-contract GREEN `41c36dc1...`;
- architecture first gate `2d438838...` exposed defects in the initial test rule and was corrected without weakening production boundaries;
- derived ComparisonPreview validation RED `66edf205...` → GREEN `63588570...`;
- review-found wire-contract RED `3cf6bc52...` proved Jackson emitted `comparisonPreview:null`; fixed and regression-tested at `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789`.

Final reviewed feature head:
`2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789`.

PR #128 exact-head acceptance:

- **9/9 normal PR workflow groups SUCCESS**;
- 0 failure / skipped / cancelled workflow groups;
- read-only review: **Looks good**;
- P0/P1/P2/P3/nitpicks: none after the wire-contract correction;
- unresolved review threads: 0.

Squash merge:
`079a53be066fa488ee01da18a109f4f2b1484800`.

Issue #127: closed `completed`.

Exact merge SHA post-merge acceptance:

- **8/8 normal push workflows SUCCESS**;
- 0 failures;
- CodeQL Java and JavaScript/TypeScript SUCCESS;
- Web CI / Web E2E SUCCESS.

## Decision

M3.5.3 is accepted as:

**implemented → tested → reviewed → merged → accepted**.

The next deterministic roadmap slice is **M3.5.4 — Responsive Pantry controls**, consuming the generated M3.5.3 contract without browser-side Pantry subtraction or comparison recomputation.
