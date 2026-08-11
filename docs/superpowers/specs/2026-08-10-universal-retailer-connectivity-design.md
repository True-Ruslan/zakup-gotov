# Universal Retailer Connectivity — Design

Date: 2026-08-10
Status: Approved architecture, awaiting written-spec review before implementation planning
Tracking: issue #47 / PR #48

## 1. Product invariant

Zakup Gotov is not a comparison product for a small curated subset of grocery retailers. Its long-term product invariant is **universal target-retailer connectivity**: every retailer/banner placed in the product's target retailer registry is mandatory coverage and must have at least one reproducible data path.

A retailer is never silently removed from the product because one integration technique fails. A failed path changes that retailer's integration state and triggers work on the next path.

Initial priority registry includes at least:

- Pyaterochka;
- Perekrestok;
- Chizhik;
- Magnit and relevant Magnit grocery banners/services;
- Lenta;
- VkusVill;
- Ozon Fresh;
- Samokat;
- Kuper as an aggregator/provider surface;
- other major grocery chains added through the same registry rather than new core architecture.

The registry is extensible. Adding another chain must require an adapter/evidence package, not changes to basket-comparison domain logic.

## 2. Design goals

The connectivity layer must:

1. support multiple acquisition paths per retailer;
2. fail over between accepted paths without losing provenance;
3. preserve retailer, provider, store/fulfillment context, SKU, price, availability and freshness;
4. prevent retailer-specific response models from leaking into the shopping domain;
5. permit deterministic fixture replay for every accepted path;
6. keep live third-party dependencies out of ordinary CI;
7. allow browser-assisted collection when anonymous server-side HTTP is unavailable;
8. make missing retailer coverage an explicit tracked blocker rather than a hidden omission.

## 3. Acquisition-path hierarchy

Every retailer may have one or more independently testable paths.

### Path A — direct supported API

Preferred path when a retailer offers a documented consumer/read-side API or supported partner integration.

Examples: direct retailer API, X5/X5 Digital supported access, retailer partnership.

### Path B — aggregator-backed observation

Use an aggregator that already carries the underlying retailer assortment when the aggregator exposes an acceptable API/data path.

The normalized observation must preserve both identities, for example:

- `sourceProvider=kuper`;
- `retailer=pyaterochka`;
- `store/fulfillmentContext=<provider-scoped value>`.

Aggregator data must never be presented as a direct retailer API observation.

### Path C — public web/page data

Use ordinary public product/catalog HTML or public consumer JSON when it is available without a user session and can be exercised reproducibly.

This includes SSR pages such as the Magnit public product/catalog surface.

### Path D — user-assisted first-party browser bridge

Fallback for retailers whose useful catalog is available to an ordinary browser session but rejects anonymous server-side clients.

The user opens the official retailer surface and manually completes any access step required by that surface, including login, location/store selection, or CAPTCHA. The connector then reads data already rendered or delivered to that first-party browser context.

Preferred extraction order:

1. semantic DOM/product-card data;
2. embedded structured page state already present in the document;
3. first-party responses already available to the browser context, without exporting/replaying browser authorization material to the backend.

No server-side bypass path is part of this design.

## 4. Browser bridge architecture

The first implementation target is a Chromium extension because it keeps first-party session state in the browser and gives deterministic access to rendered product state.

### Components

#### `RetailerBrowserAdapter`

Retailer-specific browser parser. Responsibilities:

- detect supported retailer/banner and page type;
- identify active store/fulfillment context when visible;
- extract product identity, name, quantity/pack metadata, price/promo and availability evidence;
- emit normalized observations;
- expose an adapter version for fixture compatibility.

It does **not** own network credentials or basket logic.

#### `BrowserObservationCollector`

Retailer-neutral extension service that:

- invokes the selected browser adapter;
- validates required provenance fields;
- strips session/auth/location data that is not part of the normalized observation;
- batches sanitized observations;
- sends observations to the Zakup Gotov application only after local validation.

#### `RetailerRegistry`

Defines every target retailer and its available path implementations.

Each retailer has a coverage state such as:

- `REQUIRED_UNIMPLEMENTED`;
- `DISCOVERY`;
- `AVAILABLE_DIRECT`;
- `AVAILABLE_AGGREGATOR`;
- `AVAILABLE_PUBLIC_WEB`;
- `AVAILABLE_BROWSER_BRIDGE`;
- `DEGRADED`;
- `BLOCKED_EXTERNAL`.

`BLOCKED_EXTERNAL` is a visible engineering/product blocker, not permission to remove the retailer from scope.

#### Backend ingestion boundary

The backend accepts only normalized observations. It must never require raw cookies, browser storage, CAPTCHA artifacts, private provider tokens, full HTML pages, or precise street-address payloads simply to compare baskets.

## 5. Normalized provenance model

`ObservedOffer` remains the core offer trust boundary but universal connectivity requires provenance to distinguish retailer from acquisition provider.

The next domain evolution should preserve at least:

- `retailerId` — banner shown to the user;
- `sourceProviderId` — direct retailer, Kuper, browser bridge, etc.;
- `sourceMode` — direct API / aggregator / public web / browser bridge;
- `fulfillmentContextId`;
- provider/retailer SKU identity;
- normalized price and currency;
- availability (`AVAILABLE`, `UNAVAILABLE`, `UNKNOWN`);
- `observedAt`;
- `sourceReference`;
- optional promotion metadata;
- adapter/parser version where fixture compatibility needs it.

Two observations for the same retailer from different providers must not be conflated until later matching/reconciliation logic explicitly chooses between them.

## 6. Path selection

Provider orchestration uses an ordered path set per retailer.

Recommended default order:

1. direct supported API;
2. supported aggregator path;
3. stable public web/API path;
4. user-assisted browser bridge.

Selection is capability-aware rather than a blind retry chain. A path may support price but not availability, or catalog but not location resolution. The orchestrator must preserve `UNKNOWN` rather than manufacture missing semantics.

The browser bridge may become primary for a retailer when it is the only reliable path, but the architecture should allow migration to direct/partner access later without changing shopping-core contracts.

## 7. Security and privacy boundary

This design intentionally supports a user's own first-party browser session while keeping credentials local to that browser profile.

Required rules:

- login/CAPTCHA actions remain user-driven;
- browser cookies, local/session storage values and auth tokens are not exported to the Zakup Gotov backend, logs, fixtures or repository;
- no credential forging or session theft;
- no automated CAPTCHA solving/bypass service;
- no fingerprint/device-identity spoofing intended to defeat access controls;
- no proxy/IP rotation intended to defeat provider blocking;
- precise user addresses are minimized and never stored in fixtures;
- adapters stop and report a blocked/degraded state when the first-party surface no longer exposes the required data.

These constraints do not prevent automation of parsing, normalization, fixture capture, product matching, or basket comparison after the user has legitimate first-party access.

## 8. Testing strategy

All executable behavior follows RED -> GREEN -> REFACTOR.

### Adapter unit/fixture tests

For each retailer browser adapter:

- sanitized HTML/structured-state fixtures;
- product-card extraction tests;
- price/promo parsing;
- availability mapping;
- store-context extraction;
- malformed/missing-field fail-closed behavior;
- fixture-version regression tests.

### Extension integration tests

Use local fixture pages in Playwright/Chromium to prove:

- content script/adaptor discovery;
- observation collection;
- no credential/storage export;
- batch normalization;
- supported/unsupported page behavior.

### Backend contract tests

Prove normalized browser observations pass the same provider/offer trust boundary as direct providers.

### Live tests

Live retailer checks remain opt-in/manual and produce sanitized evidence only. Ordinary CI never depends on live retailer sites.

## 9. Onboarding a new retailer

A new chain should require the following work only:

1. add retailer metadata to `RetailerRegistry`;
2. research candidate acquisition paths;
3. implement at least one path adapter;
4. capture sanitized fixtures;
5. pass the fixed common grocery corpus and store-context tests;
6. document provenance/freshness/availability limitations;
7. mark the path available only after reproducible evidence.

No shopping-list, matching, basket-ranking or API-client core code should require retailer-specific branches.

## 10. Coverage acceptance

A retailer counts as connected only when at least one path reproducibly yields:

- explicit retailer/banner identity;
- explicit store/fulfillment context or a documented reason why the retailer is not store-scoped;
- stable product identity;
- current price/currency;
- availability or explicit `UNKNOWN`;
- observation time;
- source/provenance;
- deterministic sanitized fixture replay.

A single live success is not sufficient.

## 11. M0/M1 boundary

Universal retailer connectivity is the product architecture invariant, but implementation is staged by a mandatory retailer registry rather than by changing the architecture for every chain.

Before M1 Shopping Core begins, M0 must prove the complete connectivity architecture using:

- Pyaterochka;
- Perekrestok;
- at least one independent non-X5 retailer;
- at least two distinct acquisition modes among direct/aggregator/public-web/browser-bridge;
- deterministic fixture replay and provenance for all accepted paths.

After M1 starts, every retailer added to the target registry remains mandatory coverage work until it reaches an available state. The product must expose incomplete coverage rather than imply universal completeness prematurely.

## 12. First implementation slice after spec approval

Start with **Perekrestok Browser Bridge Phase A**:

1. create a minimal Chromium extension workspace;
2. detect one official Perekrestok product/search page after the user has established the first-party session;
3. extract retailer/store context, one product identity and price from DOM or embedded structured state;
4. normalize locally;
5. prove no Cookie/Auth/storage value leaves the extension;
6. run fixture-based tests and Playwright extension E2E;
7. only after Phase A passes, expand to the fixed 20-item corpus;
8. port the same browser contract to Pyaterochka next.

Kuper/X5 supported-access work proceeds in parallel so browser bridge remains replaceable as better supported paths become available.