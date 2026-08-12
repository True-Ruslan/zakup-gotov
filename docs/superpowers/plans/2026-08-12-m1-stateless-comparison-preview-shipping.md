# M1 Stateless Comparison Preview — Shipping Evidence

Date: 2026-08-12  
PR: #80 `feat(m1): add stateless comparison preview journey`  
Branch: `feat/m1-stateless-comparison-preview`

This document is the docs-only shipping marker for the approved M1 stateless comparison preview plan.

## Accepted scope

The slice provides a stateless manual shopping-list → locality/context → deterministic comparison product journey through `POST /api/v1/comparison-previews`, OpenAPI/generated TypeScript client and responsive Next.js UI.

The public boundary preserves every canonical retailer and product-safe `READY`, `UNCERTAIN`, `INCOMPLETE` and `UNAVAILABLE` outcomes while keeping SKU, source-provider IDs, acquisition modes, source references and fulfillment-context IDs internal.

Production runtime evidence remains strict no-op/fail-closed. Deterministic runtime evidence and the browser mock API are test-only; ordinary CI performs no live retailer requests. This acceptance therefore proves the M1 product/core journey and failure semantics, not production retailer acquisition readiness.

## Hardening completed before acceptance

- Web E2E item-gap assertions were scoped to their retailer cards with exact matching after explanatory retailer copy caused a duplicate text count.
- `ComparisonPreviewArchitectureTest` protects dependency direction into the new `preview` application boundary and fixture/test-support isolation.
- Unknown JSON request fields now fail closed instead of being silently ignored, keeping runtime behavior aligned with OpenAPI `additionalProperties: false` and preventing unsupported provider/store-shaped input from weakening the public contract.
- Durable `PROJECT_STATE.md`, `ROADMAP.md` and root `CHANGELOG.md` were synchronized with the implemented journey and its production-evidence limitations.

## Exact reviewed code candidate

Reviewed code SHA: `16eff25bd50d6df9202150cde0af62390201a4af`

All required workflow groups completed successfully on that exact code candidate:

- API CI — PASS
- Contract CI — PASS
- Web CI — PASS
- Web E2E desktop/mobile — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- CodeQL Java — PASS
- CodeQL JavaScript/TypeScript — PASS
- Container Security CI — PASS
- Release Bundle CI — PASS
- Release Contract CI — PASS

## Read-only review gate

Verdict: **Looks good**

- P0: none
- P1: none
- P2: none
- P3: browser E2E uses a deterministic Node mock API while the real Spring HTTP → shopping/location/evidence/snapshot/matching/basket/comparison path is proven separately by integration tests. This split is acceptable for the current M1 slice but should eventually converge on one reusable deterministic acceptance harness to reduce fixture drift risk.

Review scope included the public request/response contract, request validation and problem responses, production/test evidence composition, orchestration through existing domain layers, privacy/identifier boundaries, OpenAPI/generated client synchronization, server-action timeout/error behavior, responsive browser acceptance and architecture dependency direction.

## Final branch-protection gate

This marker intentionally changes documentation only. The final PR head after this commit must re-run and pass the repository branch-protection workflow set before squash merge. Merge must use the exact final head SHA; if the head changes, this gate is invalid and must be repeated.

## Post-merge expectation

After squash merge, verify the resulting `main` SHA and required workflows. The next M1 engineering focus is trusted structured package-quantity extraction where source evidence proves semantics, followed by open retailer connectivity/production-access blockers (#54, #69, #70, #36 and remaining mandatory retailer onboarding). M2 Recipes must not hide unresolved M1 evidence limitations.
