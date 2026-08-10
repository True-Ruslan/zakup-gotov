# M0B Provider Feasibility — Implementation Plan

Date: 2026-08-10
Status: Active

## Goal

Prove or disprove the core Zakup Gotov assumption before M1: at least two acceptable provider paths must return reproducible location-specific grocery offers with deterministic fixture tests. Prefer at least one provider outside X5.

## Non-negotiable constraints

- TDD for executable behavior.
- No live retailer calls in ordinary PR CI.
- No CAPTCHA bypass, anti-bot bypass, browser-fingerprint evasion, proxy/IP rotation, or blocking circumvention.
- No borrowed/private user sessions or forged provider credentials.
- Precise user addresses and provider secrets must not enter fixtures/logs.
- Third-party wrappers are research references, not production dependencies by default.
- A manual request is evidence for investigation, never enough for `SUPPORTED` status.

## Slice 1 — Shared feasibility harness — COMPLETE

Merged in PR #37.

Delivered:

- `ProviderAccessType` (`OFFICIAL_API`, `PUBLIC_UNOFFICIAL_API`, `PARTNER_API`);
- `ProviderCapability`;
- provider-scoped `LocationContext`;
- `ProductQuery`;
- common `RetailerProvider` port;
- structural `FixtureRetailerProvider` / `LiveRetailerProvider` separation;
- offline `ProviderFeasibilityHarness` accepting fixture providers only;
- explicit `ProviderLiveProbe` for live-capable adapters;
- shared provider/location/capability/offer-provenance validation.

The fixture/live boundary does not depend on an enum supplied by the provider itself. Review of the first implementation caught that weakness and a second TDD cycle replaced it with the structural split.

## Slice 2 — Pyaterochka technical probe — STOPPED / UNSUITABLE_PUBLIC_PATH

Implementation: PR #39.  
Machine-readable evidence transport: PR #40 and PR #41.  
Tracking: issue #38.

### Phase A result

The non-evasive JDK HTTP probe targeted the researched public consumer backend `https://5d.5ka.ru/api` with:

- transparent Zakup Gotov `User-Agent`;
- `Accept: application/json`;
- no Cookie or Authorization;
- no captured `x-app-version`, `x-device-id`, `x-platform` headers;
- no browser automation;
- no CAPTCHA interaction;
- no proxy/IP rotation;
- no retry/evasion loop;
- fixed connect/request timeouts.

The outcome-bearing live run on `main` SHA `73d9f18d714bd1eafc165e7f5941405a0ce10b5b` produced:

`Provider Live Probe / Pyaterochka / store-403`

The first coordinate → store lookup was therefore rejected with HTTP 403 before any `sapCode`, product search, PLU/SKU, price, fixture, or corpus step could execute.

This satisfies the plan's stop condition. Existing third-party research depends on Camoufox/browser warm-up, possible CAPTCHA interaction and browser-derived headers; Zakup Gotov will not adopt those techniques to circumvent the 403.

Decision: **`UNSUITABLE_PUBLIC_PATH`** for the currently known Pyaterochka consumer backend.

### Phase B

Not started and intentionally cancelled. No fixtures or 20-item corpus should be fabricated from a path that failed Phase A.

Pyaterochka may be reconsidered only if X5/Pyaterochka later provides a documented/acceptable API or another genuinely public non-evasive path emerges.

## Slice 3 — Perekrestok — NEXT

Branch target: `spike/m0b-perekrestok`

Reuse the shared harness and the same non-evasive gate. Specifically prove whether public consumer requests can establish a concrete store/fulfillment context and whether price/availability are genuinely context-specific.

Phase A must prove, using ordinary HTTP only:

1. location/fulfillment context;
2. stable store/context identifier;
3. product search for a fixed query;
4. stable provider product ID;
5. price evidence;
6. no browser/anti-bot/proxy bypass requirement.

If Phase A passes, run the fixed 20-item corpus below against two contexts and save sanitized deterministic fixtures. If it fails on a guarded path, record the failure and stop rather than bypassing it.

Do not infer availability from catalog presence when the provider does not expose explicit stock semantics; use `UNKNOWN`.

## Slice 4 — Magnit independent path — HIGH PRIORITY

Branch target: `spike/m0b-magnit`

Priority reason: Magnit is independent from X5 and its public catalog already exposes store-scoped `shopCode` behavior and prices.

Required proof:

- discover only the ordinary requests used by the public catalog;
- two explicit `shopCode` contexts;
- 20-item corpus;
- stable product identity;
- regular/promo price distinction where present;
- availability semantics;
- sanitized fixtures;
- no browser/anti-bot bypass requirement.

Magnit should be started immediately after or in parallel with the Perekrestok Phase A gate so M0 does not depend on X5.

## Fixed 20-item corpus

Use the same requirements for every provider that passes Phase A:

- milk;
- eggs;
- bread;
- bananas;
- potatoes;
- onions;
- tomatoes;
- cucumbers;
- chicken;
- beef/mince;
- rice;
- buckwheat;
- pasta;
- sunflower oil;
- butter;
- cheese;
- kefir;
- sugar;
- salt;
- tea.

For each accepted provider, measure coverage and repeat against a second store/context where possible.

## Slice 5 — Chizhik gated probe

Branch target: `spike/m0b-chizhik`

The existing third-party wrapper uses Camoufox and documents optional proxies. Zakup Gotov must first prove plain HTTP. If the service requires stealth/proxy behavior, do not build a production adapter.

## Slice 6 — second-wave discovery

- Ozon Fresh: characterize fulfillment-zone/store, consumer catalog, SKU, price, stock and delivery constraints.
- Samokat: characterize location-dependent consumer backend and determine whether a non-evasive public path exists.
- Kuper: continue issue #36 official access/rights inquiry in parallel.

## M0 decision report

Provider scorecard must include failed candidates as evidence, not only successful ones.

| Metric | Meaning |
|---|---|
| Location reproducibility | Same explicit context can be selected repeatedly |
| Corpus coverage | Requirements with usable candidate offer / 20 |
| SKU stability | Product identity survives fixture replay |
| Price | Current price can be normalized |
| Availability | Explicit, unknown, or unusable |
| Freshness | Observation/provider timestamp semantics |
| Fixture determinism | Offline replay passes without network |
| Public-path stability | No bypass/evasion required |
| Usage-rights status | Acceptable / unresolved / blocked |

Current scorecard evidence starts with Pyaterochka: location resolution **FAIL (`HTTP 403`)**, all downstream metrics **NOT TESTED** because the public path was stopped fail-closed.

M0 is GO only when at least two provider paths satisfy the roadmap exit criteria. Prefer one provider independent from X5 before moving to M1 Shopping Core.
