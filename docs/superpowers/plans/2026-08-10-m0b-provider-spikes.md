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

## Slice 1 — Shared feasibility harness

Branch: `feat/m0b-provider-feasibility-harness`

Deliver:

- `ProviderAccessType` (`OFFICIAL_API`, `PUBLIC_UNOFFICIAL_API`, `PARTNER_API`);
- `ProviderCapability`;
- provider-scoped `LocationContext`;
- `ProductQuery`;
- common `RetailerProvider` port;
- `FixtureRetailerProvider` for deterministic recorded/synthetic sources;
- `LiveRetailerProvider` for adapters that may communicate with an external retailer;
- offline `ProviderFeasibilityHarness` whose public search signature accepts only `FixtureRetailerProvider`;
- separate explicit `ProviderLiveProbe` whose public search signature accepts only `LiveRetailerProvider`;
- shared validation for provider/location identity, required `PRODUCT_SEARCH` + `PRICE` capabilities, and returned-offer provenance;
- tests proving fixture success, structural fixture/live isolation, explicit live-probe execution, provider/location consistency, capability gating, and offer-context consistency;
- research matrix synchronization.

The fixture/live boundary must not depend on an enum value supplied by the provider itself. Review of the first implementation found that such metadata could be misdeclared accidentally; the structural type split is the required design.

Exit gate: focused tests + full API verify + full repository CI/security/release-bundle gates pass.

## Slice 2 — Pyaterochka technical probe

Branch target: `spike/m0b-pyaterochka`

### Phase A — plain-HTTP gate

Use the public 5ka consumer flow and Open-Inflation `pyaterochka_api` only as research references. Determine whether the minimum required calls are reproducible using an ordinary HTTP client without Camoufox, stealth plugins or proxies.

Required proof:

1. fixed Moscow locality/context;
2. selected store / `sapCode` or equivalent fulfillment identifier;
3. product/category/search response with stable SKU/PLU identity;
4. price and availability semantics;
5. source/freshness metadata where available;
6. no bypass mechanisms.

If this gate fails because anti-bot circumvention is required, stop the spike and record `UNSUITABLE_PUBLIC_PATH`.

### Phase B — fixture corpus

Use 20 common grocery requirements:

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

Capture sanitized raw responses and write deterministic parser/contract tests. Repeat against a second store/context and quantify coverage and price/availability differences.

## Slice 3 — Perekrestok

Branch target: `spike/m0b-perekrestok`

Reuse the same corpus and harness. Specifically prove whether city/session context can be narrowed to a concrete store/fulfillment context and whether price/availability are genuinely context-specific.

Do not infer availability from catalog presence when the provider does not expose explicit stock semantics; use `UNKNOWN`.

## Slice 4 — Magnit independent path

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

## Slice 5 — Chizhik gated probe

Branch target: `spike/m0b-chizhik`

The existing third-party wrapper uses Camoufox and documents optional proxies. Zakup Gotov must first prove plain HTTP. If the service requires stealth/proxy behavior, do not build a production adapter.

## Slice 6 — second-wave discovery

- Ozon Fresh: characterize fulfillment-zone/store, consumer catalog, SKU, price, stock and delivery constraints.
- Samokat: characterize location-dependent consumer backend and determine whether a non-evasive public path exists.
- Kuper: continue issue #36 official access/rights inquiry in parallel.

## M0 decision report

After Pyaterochka + Perekrestok + Magnit, publish a provider scorecard with at least:

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

M0 is GO only when at least two provider paths satisfy the roadmap exit criteria. Prefer one X5 banner plus Magnit or another independent provider before moving to M1 Shopping Core.
