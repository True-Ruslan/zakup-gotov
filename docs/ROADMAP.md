# Roadmap

Updated: 2026-08-10

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## M0 — Product & Integration Discovery

Goal: prove that the core product promise is technically and legally viable before substantial feature development.

Deliverables:

- approved product/architecture foundation;
- repository quality and security baseline;
- provider integration research matrix for major target retailers;
- mandatory coverage strategy for Pyaterochka and Perekrestok;
- location/fulfillment-context model;
- provider port contract;
- feasibility harness for external integrations;
- supported-path spikes for both mandatory X5 banners plus at least one independent non-X5 provider;
- recorded fixtures and automated contract/parser tests;
- documented freshness, availability, rate-limit, provenance, operational, and legal constraints;
- go/no-go decision for M1.

Accepted provider paths may be direct retailer APIs, supported aggregator integrations, or user-assisted first-party browser connectors, provided provenance and limitations are explicit and the path does not depend on CAPTCHA bypass, credential forgery, session theft, fingerprint evasion, or deliberate access-control circumvention.

Exit criteria:

- **Pyaterochka** can return usable location/store-specific product/offer data through at least one acceptable path;
- **Perekrestok** can return usable location/store-specific product/offer data through at least one acceptable path;
- at least one independent non-X5 provider can return usable location-specific product/offer data through an acceptable path;
- accepted results are reproducible through automated tests/sanitized fixtures;
- direct, aggregator-backed, and browser-assisted provenance cannot be confused in the product model;
- known limitations are documented rather than hidden.

Detailed mandatory-X5 strategy: [`integrations/x5-mandatory-coverage.md`](integrations/x5-mandatory-coverage.md).

## M1 — Shopping Core

Goal: compare a manually entered grocery list across supported retailers.

Scope:

- shopping list CRUD;
- canonical units/quantities;
- address/location input;
- retailer discovery;
- provider orchestration;
- deterministic product matching baseline;
- package/quantity selection baseline;
- price and availability snapshots;
- complete-basket comparison;
- partial-provider failure UX;
- data freshness UX;
- provider provenance UX when observations come through an aggregator or user-assisted browser connector.

Exit criteria:

- critical journey is covered by automated integration and browser E2E tests;
- incomplete/ambiguous matches are transparent;
- one-store ranking is deterministic and explainable.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope:

- built-in and user-created recipes;
- servings;
- normalized ingredient quantities;
- instructions/content model;
- recipe -> shopping requirement conversion;
- recipe editing and duplication;
- import experiments only after core model is stable.

## M3 — Weekly Planning

Goal: generate one coherent shopping requirement set from several meals.

Scope:

- weekly meal planner;
- merge duplicate ingredients;
- unit conversion where safe;
- pantry/exclusion controls;
- shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize for real checkout cost rather than naive SKU sums.

Scope:

- package-size optimization;
- substitutes and user preferences;
- delivery/service fees where available;
- minimum order constraints;
- single-store convenience mode;
- multi-store lowest-total-cost mode;
- confidence/freshness penalties.

## M5 — Productization

Goal: make the product reliable and useful for repeat users while enabling fast product experiments.

Scope:

- accounts/authentication;
- saved addresses with privacy controls;
- saved lists/recipes/preferences;
- product analytics abstraction;
- feature flags/experiments;
- stronger provider health monitoring;
- performance and accessibility budgets;
- public SEO/content surfaces where justified.

## M6 — Native Mobile

Goal: ship native Android and iOS clients without redesigning the core platform.

Target stack:

- Expo;
- React Native;
- TypeScript;
- generated OpenAPI client;
- shared analytics vocabulary and design tokens.

Mobile-specific work may include barcode/camera flows, push notifications, deep links, native sharing, and location UX only when validated by product needs.

## Later candidates — not commitments

- broader retailer/affiliate partnerships beyond the mandatory M0 integration work;
- direct cart creation/checkout handoff;
- loyalty integration;
- price history and alerts;
- AI-assisted recipe import;
- AI-assisted product matching as a ranked optional stage;
- household collaboration;
- nutrition/dietary planning;
- retailer-facing or B2B APIs.

## Guiding rule

Do not add infrastructure because it is fashionable. Add a technology only when a measured product, reliability, scaling, or team constraint makes its benefit exceed its operational cost.
