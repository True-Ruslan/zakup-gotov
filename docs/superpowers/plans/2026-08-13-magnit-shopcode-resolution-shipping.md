# Magnit shopCode resolution shipping evidence

Date: 2026-08-13
PR: #86
Issue: #69

## Reviewed candidate

- reviewed code SHA: `de2b97f0cab21487bc1d20c2ef0de94e8c33c14b`
- scope: deterministic Magnit bbox request contract, sanitized store-candidate parser, fail-closed resolver and provider-scoped fulfillment binding
- production HTTP activation: none
- text/address geocoding: not implemented because no acceptable public contract was proven

## Proof gate

Exact reviewed SHA passed all nine PR workflow groups:

- API CI — PASS
- Contract CI — PASS
- Web CI / responsive E2E — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- Container Security CI — PASS
- CodeQL — PASS
- Release Contract CI — PASS
- Release Bundle CI — PASS

The implementation additionally proved RED→GREEN checkpoints for geographic/request primitives, response parsing and resolution/binding semantics.

## Change review

Verdict: **Looks good**.

- P0: none
- P1: none
- P2: none
- P3: none blocking shipping
- open review threads: none

Review confirmed:

- only the proven `items.items[].externalId.storeCode + coordinates` response shape participates;
- address/name metadata is not retained by the domain model;
- zero, many and conflicting candidates never create automatic bindings;
- only exactly one candidate can create `RESOLVED`;
- explicit selection creates `MANUAL` using the same provider identity;
- `shopCode` remains internal `LocationContext.fulfillmentContextId`;
- no provider-specific identifier enters shopping/basket/public comparison vocabulary;
- direct request constructors cannot bypass bbox/store-type invariants;
- no production/live Magnit traffic is introduced by this PR.

## Shipping boundary

#86 is ready to merge after the final docs-only exact-head gate.

Merging #86 **does not yet close #69**. The issue acceptance contract requires a merged-main guarded live check that repeats the known public bbox twice and confirms stable `shopCode=992301` across a clean stateless boundary without auth/app headers or a cookie jar.

#70 remains independent and unresolved; recurring production acquisition stays disabled.

## Rollback

The change is additive domain/contract code with no persistence migration and no production network activation. Rollback is a normal revert of the squash merge; existing explicit-store Magnit feasibility and production no-op evidence remain unaffected.
