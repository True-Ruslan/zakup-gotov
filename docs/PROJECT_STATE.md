# Project State

Updated: 2026-08-13

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn recipes, meal plans or a manual grocery list into a location-aware comparison of complete retailer baskets while preserving price/availability evidence, package semantics, provenance, freshness and uncertainty.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M2 — Recipes**  
M0 status: **technical discovery COMPLETE**  
M1 status: **Shopping Core COMPLETE / ACCEPTED**  
M1→M2 decision: **GO** — [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md)  
Current focus: **ship M2.1 deterministic Recipe → ShoppingList domain slice (#94 / #93), then design the application/API boundary**

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility and production-access readiness are separate states. Advancing deterministic product/core milestones does not imply every retailer is production-ready.

## M0 — COMPLETE

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` |
| Two acquisition modes | **PASS** | browser bridge + public web |
| Deterministic verification | **PASS** | sanitized fixtures/E2E + finite guarded probes |
| Retailer-neutral boundary | **PASS** | provider harness + canonical retailer registry |

M0 completion proves technical feasibility, not blanket permission for recurring production acquisition.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Final acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).

Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

### Accepted implementation sequence

1. **Retailer registry / coverage state — #72** — canonical retailer identities with separate technical and production-access states.
2. **Shopping list / canonical quantities — #73** — stable identity, explicit mutations, canonical `kg → g`, `l → ml`.
3. **Provider/path orchestration — #74** — retailer/provider/acquisition/fulfillment provenance and fail-closed path selection.
4. **Location / fulfillment context — #75** — provider-neutral product location, sensitive-address redaction and provider-scoped bindings.
5. **Price / availability snapshots — #76** — immutable snapshots, observation/provider-freshness distinction, first-class `UNKNOWN`.
6. **Deterministic matching — #77** — exact-before-normalized, explicit matched/ambiguous/unmatched outcomes.
7. **Single-store basket quote — #78** — whole-package arithmetic, `COMPLETE / UNCERTAIN / INCOMPLETE`, no misleading incomplete total.
8. **Failure / coverage / freshness boundary — #79** — every canonical retailer remains visible with product-safe reasons.
9. **Stateless critical journey — #80** — comparison-preview API, generated client, responsive UI and desktop/mobile Playwright.
10. **Structured package-evidence plumbing — #81** — `ObservedOffer → OfferSnapshot → PackageQuantitySet`; presentation text is non-authoritative.
11. **Magnit exact characteristic semantics — #82** — exact `Вес, кг` / `Объем, л`, ambiguity/conflict/invalid fail closed.
12. **Magnit fixed-corpus instrumentation — #83** — transport/identity failure separated from metadata quality.
13. **Magnit SKU-bound JSON-LD package evidence — #85** — exact `Product.sku`, proven weight/volume fields, no extra request/browser.
14. **Magnit bbox → `shopCode` boundary — #86** — deterministic public store-search contract and fail-closed resolution.
15. **Magnit merged-main LOCATION_RESOLUTION proof — #87 / #69** — exact default-branch stateless two-request reproduction.
16. **Magnit production right-to-operate decision — #89 / #70** — technical coverage remains `AVAILABLE_PUBLIC_WEB`; recurring production reuse is product-policy `BLOCKED` pending affirmative permission/licensed or supported terms.
17. **Pre-acquisition production-access gate — #91 / #90** — evidence sources receive only immutable production-ready retailer IDs; empty scope means no source invocation; out-of-scope evidence fails closed.

### Final M1 acceptance properties

- all eight canonical retailers remain visible;
- `READY / UNCERTAIN / INCOMPLETE / UNAVAILABLE` remain distinct;
- unmatched/ambiguous/package-unknown/unit-mismatch cases fail safely;
- incomplete baskets expose no misleading complete total;
- `UNKNOWN` availability remains uncertain;
- freshness and provenance boundaries remain explicit;
- addresses/provider/store implementation identifiers do not leak into product-facing preview;
- technical connectivity and production access are independent;
- production access is enforced **before** runtime evidence acquisition;
- blocked/pending/discovery retailers cannot enter acquisition scope;
- production preview cannot fall back to deterministic fixtures or hidden live retailer traffic;
- ordinary CI remains retailer-network-free.

The acceptance decision is **GO to M2 Recipes for deterministic product/core development**. It does not claim production retailer completeness.

## Magnit status at M1 exit

### Technical package evidence

Accepted 20-product × 2-shop JSON-LD replay:

- HTTP 2xx: 40/40;
- usable observations: 40/40;
- stable identity: 20/20;
- `FOUND=36`;
- `MISSING=0`;
- `AMBIGUOUS_DIMENSIONS=4`;
- conflicts: 0;
- invalid: 0.

Milk SKU `1000013732` and kefir SKU `1000330180` remain deliberately ambiguous in both shop contexts because both weight and volume are present. Structured egg mass remains mass and cannot satisfy `PIECE` requirements.

### Technical location/store context

Accepted first-party contract:

`POST /webgate/v1/stores-facade/search`

Accepted rules:

- validated bbox → candidate set;
- 0 → `NO_STORES`;
- exactly 1 → `RESOLVED`;
- >1 → `AMBIGUOUS`;
- conflicting duplicate identity → `CONFLICTING_STORE_EVIDENCE`;
- explicit choice → `MANUAL`;
- no implicit first/nearest-store heuristic.

Merged-main run `31642543544` on SHA `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1` proved public `shopCode=992301` across two direct stateless requests with identical candidate sets and no session/auth/redirect dependence.

Text/locality/address → coordinates remains intentionally unproven; no hidden geocoder is introduced.

### Production-access decision — #70

Decision memo: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Current product state:

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because an affirmative right to operate the intended recurring production catalog-acquisition/reuse model has not been established. It is not a claim that Magnit expressly prohibits every automated HTTP request and is not a legal adjudication.

No production Spring/HTTP Magnit acquisition is activated.

## M2 — Recipes — CURRENT

### M2.1 — Recipe domain and Recipe → ShoppingList — IMPLEMENTED / TESTED / SHIPPING (#94 / #93)

Approved design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md).  
Implementation plan: [`superpowers/plans/2026-08-13-m2-1-recipe-domain.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain.md).

Current reviewed implementation head before shipping docs: `734ed53712b4327039eabfb358548828aa1a1dbe`.

Implemented behavior:

- separate top-level `recipe` domain with `RecipeId`, `RecipeIngredientId`, normalized `RecipeTitle`, positive-integer `RecipeServings`, immutable ordered `RecipeIngredient` list and duplicate-ID rejection;
- each ingredient reuses existing `ShoppingRequirement` and `Quantity`; Recipe introduces no duplicate unit/canonicalization model;
- pure `RecipeShoppingListConverter` with no Spring, persistence, network, clock or retailer dependency;
- serving scaling sums each compatible merge group before applying `targetServings / baseServings`;
- terminating decimal division remains exact; non-terminating ratios use deterministic `MathContext.DECIMAL128`; no `double`/`float` path exists;
- merge key is exact normalized `ShoppingRequirement` + canonical `QuantityUnit`;
- case differences, synonyms and physical-dimension mismatches do not merge;
- output group order follows first ingredient occurrence;
- generated `ShoppingItemId` is deterministic and list-scoped from `ShoppingListId + requirement text + canonical unit`, independent of amount/target servings;
- kg/g and l/ml representations converge through the existing `Quantity` canonicalization before identity/merge decisions;
- provenance is returned separately as `ShoppingItemId → ordered List<RecipeIngredientRef(RecipeId, RecipeIngredientId)>` and is deep-immutable;
- artificial generated-ID collisions across different merge keys fail closed;
- Shopping Core production types remain recipe-agnostic and unchanged.

TDD/verification state:

- value-object RED → GREEN complete;
- immutable aggregate RED → GREEN complete;
- scaling/merge RED → GREEN complete;
- deterministic identity/provenance RED → GREEN complete;
- collision fail-closed RED → GREEN complete;
- conversion/lineage validation RED → GREEN complete;
- full API `verify` PASS on `734ed537…`, including Spring Modulith architecture verification;
- exact implementation head has 9/9 PR workflow groups `success`;
- independent review verdict: **Looks good**, no P0/P1/P2; review threads empty.

This status is **not yet ACCEPTED**. Acceptance requires the final shipping-doc head to pass the same exact-head gate, squash merge, and green post-merge `main` verification.

### M2.1 non-goals preserved

Not added in this slice:

- REST/OpenAPI/generated-client contracts;
- persistence/repository layer;
- recipe web UI;
- AI/NLP recipe parsing or arbitrary web import;
- fuzzy/case-insensitive ingredient equivalence;
- nutrition/calorie optimization;
- pantry prediction;
- fractional servings input;
- multi-recipe aggregation.

### Next product slice after M2.1 acceptance

Design the application/API boundary:

`Recipe request → Recipe domain → RecipeShoppingListConversion → comparison input`

That follow-up owns REST/OpenAPI/generated-client semantics. A responsive create/edit/servings flow should only be layered on top after the application contract is accepted.

## Parallel mandatory work

These continue without blocking deterministic M2 domain work unless new evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator access investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific structured package semantics only where source evidence proves them;
- retailer-specific production-access decisions before activation;
- successful real **`v0.1.0-rc.3` GitHub Release** proving final image promotion, SBOM/attestation and digest smoke evidence.

## Permanent core invariants

1. Shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer remains visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness are independent.
4. Precise addresses are sensitive and redacted by default.
5. Provider/acquisition/fulfillment identifiers remain internal.
6. `UNKNOWN` availability is never coerced.
7. Observation time is not misrepresented as provider freshness.
8. Matching ambiguity never becomes a hidden winner.
9. Package quantity is explicit structured evidence and is never guessed from presentation text.
10. Basket package bindings derive from immutable snapshot evidence.
11. Source ambiguity/conflict remains fail-closed.
12. Mass, volume and count are not interchangeable.
13. Incomplete baskets never expose misleading complete-basket totals.
14. Production activation respects independent right-to-operate status.
15. Production-access policy scopes acquisition before source invocation.
16. Evidence outside requested retailer scope is a contract violation.
17. Ordinary CI/browser acceptance makes no live retailer requests.
18. Production preview evidence does not fall back to deterministic fixtures.
19. Unknown JSON request fields fail closed.
20. Universal retailer connectivity remains mandatory.
21. Public technical accessibility is never treated as production authorization by itself.
22. Recipe semantics reuse Shopping Core quantity/requirement normalization instead of duplicating it.
23. Recipe provenance remains outside Shopping Core types.
24. Recipe exact-safe merging never introduces fuzzy/AI equivalence implicitly.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
