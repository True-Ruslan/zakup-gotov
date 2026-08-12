# Magnit shopCode merged-main live gate shipping evidence

Date: 2026-08-13
PR: #87
Issue: #69

## Reviewed candidate

- reviewed implementation SHA: `b434fb1ff7006ebf036605275ab5fdf6115cccf8`
- scope: test-only live probe plus an explicit issue-comment workflow
- production runtime HTTP activation: none
- schedules: none

## TDD proof

1. RED: the first contract failed only because `MagnitStoreSearchLiveProbe` did not exist.
2. GREEN: the test-only probe compiled and the complete API verification passed with live networking skipped by default.
3. Hardening RED: no-auth/no-redirect tests failed only because `hasAuthenticator()` and `followsRedirects()` did not exist.
4. Hardening GREEN: the client now uses `Redirect.NEVER`, has no `Authenticator` and no `CookieHandler`; full API verification passed.

## Exact-head CI

All nine PR workflow groups passed on the reviewed implementation SHA:

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

- the live probe is under `src/test` and cannot activate production traffic;
- ordinary CI never performs the network requests because the live test requires `zakup.live.magnit.shopcode=true`;
- the workflow can run only for issue #69, actor `True-Ruslan`, exact command `/provider-probe magnit-shopcode`;
- the request uses the accepted production `MagnitStoreSearchRequest` contract;
- response interpretation uses the accepted production `MagnitStoreSearchResponseParser`;
- the client supplies no cookie jar, authenticator or Magnit app/auth headers and does not follow redirects;
- only statuses, candidate counts, booleans and request count are emitted as evidence;
- raw response bodies, addresses, coordinates, cookie values and tokens are never written to evidence;
- exactly two requests are required;
- both attempts must be 2xx, contain public `shopCode=992301`, have identical candidate-code sets and contain no conflicting evidence.

## Issue-state correction

#69 was automatically closed when #86 merged even though its acceptance criteria require a merged-main live gate. It was reopened before shipping #87 and must remain open until the post-merge live workflow passes.

## Shipping boundary

#87 is ready to merge after the final docs-only exact-head gate.

The merge itself is not acceptance of #69. After #87 is merged and `main` is green, the exact issue comment `/provider-probe magnit-shopcode` must trigger the default-branch workflow. Only a successful merged-main run satisfies the final `LOCATION_RESOLUTION` gate.

#70 remains independent and unresolved. No recurring production acquisition is enabled by this change.

## Rollback

The PR is test/workflow-only. Rollback is a normal revert of the squash merge; production runtime behavior and existing explicit Magnit store contexts are unchanged.
