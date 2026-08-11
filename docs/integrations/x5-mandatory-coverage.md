# X5 Mandatory Coverage Strategy

Updated: 2026-08-11
Status: M0 product constraint and integration strategy
Tracking: issue #47

## Product decision

Pyaterochka and Perekrestok are mandatory retailer coverage for Zakup Gotov.

A failed anonymous direct-HTTP probe does not remove either banner from product scope. M0 cannot reach GO unless both banners have at least one reproducible acceptable data path.

The goal is not necessarily a direct retailer API. The goal is reliable banner- and store-specific product observations with explicit provenance.

## Current X5 state

- **Perekrestok:** `AVAILABLE_BROWSER_BRIDGE` for page-snapshot acquisition. Adapter v2 passed the repeated real first-party browser gate on 2026-08-11 with `status=ok`, 90 normalized observations, one fulfillment context, adapter version `2`, and zero validation failures.
- **Pyaterochka:** direct anonymous JDK path remains `DIRECT_ANONYMOUS_HTTP_UNSUITABLE` after `store-403`; accepted coverage is still outstanding.

Perekrestok therefore satisfies its M0 per-retailer connectivity requirement. Pyaterochka is now the remaining mandatory X5 connectivity blocker.

## Required observation contract

For each mandatory banner the accepted path must produce, at minimum:

- retailer/banner identity;
- explicit store or fulfillment context;
- stable product identity/SKU;
- current price and currency;
- availability or explicit `UNKNOWN`;
- observation timestamp;
- source/provenance identifying whether the observation came directly from X5, through an aggregator, or from a first-party user browser session;
- deterministic sanitized fixture replay for parser/contract tests.

No path may silently present aggregator data as a direct retailer observation.

## Track A — supported X5 partnership

Preferred long-term path remains supported access from X5/X5 Digital for store-scoped assortment, price, promotions and availability.

A partnership request should cover store/fulfillment resolution, banner/store ID, searchable assortment, stable SKU, current price/promotions, availability/freshness, rate limits/caching, comparison rights, sanitized fixture rights, attribution/deep links and sandbox/test credentials.

## Track B — aggregator-backed X5 coverage

Kuper and other supported aggregator surfaces remain a strong independent fallback/parallel path.

Any aggregator path is acceptable only if it preserves retailer/banner identity, underlying fulfillment context where available, stable product identity, explicit price/promotion/availability semantics, freshness and provenance such as `provider=kuper`, `retailer=pyaterochka` rather than pretending the observation came directly from X5.

Issue #36 continues to investigate supported Kuper Client apps API coverage.

## Track C — user-assisted first-party browser bridge

This path is now **proven for Perekrestok page snapshots**.

Architecture/boundary:

1. user opens the official retailer page in their own browser/profile;
2. user manually resolves login/CAPTCHA/store selection when required;
3. bridge reads semantic DOM, embedded structured state, and/or first-party resource evidence already visible in that legitimate browser context;
4. bridge normalizes observations locally;
5. only sanitized normalized observations leave the page context when needed.

Security boundary:

- CAPTCHA remains manual; no solver/bypass;
- no browser-fingerprint spoofing or stealth evasion;
- no proxy/IP rotation to defeat blocking;
- no capture/export/replay of session cookies/tokens/auth headers;
- no raw response-body persistence;
- no exact street address in fixtures/logs;
- stop on explicit provider blocking rather than escalating evasion.

### Perekrestok proof

Adapter v1 first failed transparently on 2026-08-10 because the current frontend no longer exposed its expected embedded product/store state.

PR #53 introduced adapter v2 using current semantic `.product-card` DOM plus same-origin `/api/customer/<version>/shop/<numeric-id>` resource-path evidence with TDD coverage for asynchronous resource and DOM timing.

Repeated real-browser v2 evidence on 2026-08-11:

- bridge status `ok`;
- 90 normalized observations;
- adapter version exactly `2`;
- exactly one fulfillment context (`656`);
- zero invalid observations under the acceptance predicate;
- canonical source references without query/hash;
- no credential/session export observed.

Decision: **Perekrestok = `AVAILABLE_BROWSER_BRIDGE`** for page-snapshot acquisition.

Live evidence: [`perekrestok-browser-bridge-live-2026-08-11.md`](perekrestok-browser-bridge-live-2026-08-11.md).

Issue #54 tracks persistent-session refresh after the first successful snapshot; it does not invalidate the accepted reload-based page-snapshot path.

## Previously tested direct paths

### Pyaterochka

The transparent JDK HTTP probe received `store-403` on the first coordinate-to-store request.

Current state: `DIRECT_ANONYMOUS_HTTP_UNSUITABLE`.

Next technical priority is to reuse the proven Perekrestok browser-bridge transport contract for Pyaterochka, while supported/aggregator access work continues in parallel.

### Perekrestok

The transparent first-party-cookie probe received `store-403` before store selection/search. That direct server-side path remains unsuitable, but it is no longer a product blocker because the browser bridge now provides an accepted acquisition path.

## Execution order

1. Treat issue #47 as the umbrella mandatory-X5 requirement.
2. Reuse the proven Perekrestok browser transport for Pyaterochka with the same privacy boundary, deterministic TDD and real-browser gate.
3. Continue Kuper issue #36 and supported X5 partnership work in parallel.
4. Keep direct anonymous 403 probes as regression/evidence only; do not turn them into stealth automation.
5. Run Perekrestok fixed-corpus/second-context validation as hardening, not as a Phase A acceptance blocker.
6. M0 GO still requires Pyaterochka plus at least one independent non-X5 retailer path and the remaining cross-mode criteria.

## M0 decision rule

Zakup Gotov must not enter M1 on the assumption that X5 can be omitted.

Current per-banner state:

- Perekrestok usable through Track C: **satisfied**;
- Pyaterochka usable through Track A, B or C: **outstanding**.

M0 additionally requires at least one independent non-X5 provider path, deterministic fixture tests for accepted paths, operational limitations/provenance visible in the product model, and the architecture-level multi-mode criteria defined in `ROADMAP.md` / `PROJECT_STATE.md`.
