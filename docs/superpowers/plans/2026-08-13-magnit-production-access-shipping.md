# Magnit production access policy shipping evidence

Date: 2026-08-13
PR: #89
Issue: #70

## Reviewed candidate

- reviewed policy/docs SHA: `beeb66091f610e2d2625f9bf6890020fa4f59b1e`
- technical coverage remains: `AVAILABLE_PUBLIC_WEB`
- production access becomes: `BLOCKED`
- production Magnit HTTP activation: none
- recurring polling: none

## Decision semantics

`BLOCKED` is a Zakup Gotov product-policy state: current authoritative evidence does not establish affirmative permission for the intended recurring production catalog acquisition/reuse model, so the path must not be treated as production-ready.

It is not a claim that Magnit expressly prohibits every automated HTTP request and is not a legal adjudication that the intended use is unlawful.

Decision memo: `docs/integrations/magnit-production-access-decision-2026-08-13.md`.

## TDD evidence

1. Policy RED: registry expectations required `BLOCKED` while production returned `UNRESOLVED`; API suite reported exactly two expected failures and no compiler/infrastructure noise.
2. Minimal production change: only the Magnit registry production-access state changed `UNRESOLVED → BLOCKED`; technical coverage stayed `AVAILABLE_PUBLIC_WEB`.
3. Public-contract RED: existing comparison assembler and controller tests then failed exactly because runtime/public API returned `BLOCKED` while tests still expected `PENDING`.
4. Public-contract GREEN: tests now require `CONNECTED + BLOCKED + UNAVAILABLE + PRODUCTION_ACCESS_BLOCKED`, no total/freshness and no provider-identifier leakage.
5. Full API verification passed on implementation/public-contract SHA `521b93617353a1ccb578454a26a0f9ceaac94675`.

## Exact-head CI

All nine PR workflow groups passed on reviewed candidate `beeb66091f610e2d2625f9bf6890020fa4f59b1e`:

- API CI — PASS
- Contract CI — PASS
- Web CI / responsive E2E — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- Container Security CI — PASS
- CodeQL Java + JavaScript/TypeScript — PASS
- Release Contract CI — PASS
- Release Bundle CI — PASS

## Change review

Verdict: **Looks good**.

- P0: none
- P1: none
- P2: none
- blocking P3: none
- open review threads: none

Review confirmed:

- technical connectivity is not downgraded or conflated with access policy;
- `ProductionAccessStatus.BLOCKED` maps to public `BLOCKED`, `UNAVAILABLE`, `PRODUCTION_ACCESS_BLOCKED`;
- totals/freshness are not exposed for the blocked retailer;
- no Magnit production HTTP/client wiring is added;
- no recurring acquisition or anti-bot/session behavior is introduced;
- legal/source discussion is framed as an engineering right-to-operate decision and explicitly avoids claiming a universal scraping prohibition or adjudicated illegality;
- unblock criteria require new affirmative source-backed evidence.

## Shipping boundary

#89 is ready to merge after the final docs-only exact-head gate.

After merge and green `main`, #70 can close as **completed with outcome BLOCKED**. This closes the decision task, not the possibility of future Magnit production access: new affirmative permission/licensed/supported terms may reopen the decision and move the state to `ACCEPTABLE`.

The immediate product next step is the final M1 acceptance pass before M2 Recipes.

## Rollback

The runtime change is a single registry policy state plus tests/docs. Reverting the squash merge returns Magnit access to `UNRESOLVED`; it does not affect technical public-web/package/location evidence.
