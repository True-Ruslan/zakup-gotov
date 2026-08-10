# Roadmap

Updated: 2026-08-10

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations.

Every retailer/banner added to the target registry is mandatory coverage work until at least one reproducible acquisition path is available. A failed direct API does not remove that retailer from scope; it moves integration work to another accepted path such as a supported aggregator, public web surface, or user-assisted first-party browser bridge.

The durable design is in [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery

Goal: prove that the core product promise and the universal retailer-connectivity architecture are technically viable before substantial shopping-core development.

Deliverables:

- approved product/architecture foundation;
- repository quality and security baseline;
- provider integration research matrix for major target retailers;
- universal target-retailer registry/connectivity design;
- mandatory Pyaterochka/Perekrestok coverage strategy as the first hard case;
- location/fulfillment-context model;
- provider port contract;
- feasibility harness for external integrations;
- supported-path spikes for both mandatory X5 banners plus at least one independent non-X5 provider;
- proof of at least two distinct acquisition modes among direct/partner API, aggregator, public web and user-assisted browser bridge;
- recorded fixtures and automated contract/parser tests;
- documented freshness, availability, rate-limit, provenance, operational, and legal constraints;
- go/no-go decision for M1.

Accepted provider paths may be direct retailer APIs, supported aggregator integrations, stable public web/API surfaces, or user-assisted first-party browser connectors. Provenance and limitations must remain explicit.

Exit criteria:

- **Pyaterochka** can return usable location/store-specific product/offer data through at least one acceptable path;
- **Perekrestok** can return usable location/store-specific product/offer data through at least one acceptable path;
- at least one independent non-X5 retailer can return usable location-specific product/offer data through an acceptable path;
- at least two acquisition modes have been proven end to end so M0 does not depend on a single transport assumption;
- accepted results are reproducible through automated tests/sanitized fixtures;
- direct, aggregator-backed, public-web, and browser-assisted provenance cannot be confused in the product model;
- the retailer registry and onboarding contract can add another chain without changes to shopping-list/basket domain logic;
- known limitations are documented rather than hidden.

Detailed X5 strategy: [`integrations/x5-mandatory-coverage.md`](integrations/x5-mandatory-coverage.md).

## M1 — Shopping Core

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage state for every target registry entry.

Scope:

- shopping list CRUD;
- canonical units/quantities;
- address/location input;
- retailer registry and coverage-state visibility;
- retailer discovery;
- provider/path orchestration;
- deterministic product matching baseline;
- package/quantity selection baseline;
- price and availability snapshots;
- complete-basket comparison;
- partial-provider failure UX;
- data freshness UX;
- provider provenance UX when observations come through an aggregator, public-web adapter, or user-assisted browser connector.

Exit criteria:

- critical journey is covered by automated integration and browser E2E tests;
- incomplete/ambiguous matches are transparent;
- one-store ranking is deterministic and explainable;
- unavailable target-retailer coverage is explicit rather than silently omitted.

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
- stronger provider and retailer-path health monitoring;
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

- broader retailer/affiliate partnerships beyond the mandatory registry integration work;
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