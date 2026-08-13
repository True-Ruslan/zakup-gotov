# M1 Shopping Core acceptance — 2026-08-13

Issue: #90  
Accepted main baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`  
Decision: **GO to M2 Recipes for deterministic product/core development**.

## What this GO means

M1's deterministic Shopping Core is accepted as a stable foundation for recipe-domain work:

`ShoppingList → ProductLocation/FulfillmentContext → ProviderEvidence → OfferSnapshot → Matching → BasketQuote → RetailerComparison`

The GO does **not** mean every retailer is production-ready. Retailer connectivity, production-access/right-to-operate decisions, browser lifecycle hardening and release proof continue as parallel mandatory work. Product-facing coverage/access states remain authoritative and fail closed.

## Acceptance matrix

| Requirement | Evidence | Result |
|---|---|---|
| all 8 canonical retailers remain visible | registry/read-model/controller + deterministic preview | PASS |
| complete / uncertain / incomplete / unavailable remain distinct | `ComparisonPreviewServiceTest`, deterministic integration/browser journey | PASS |
| package unknown fails safely | package/basket/preview regressions | PASS |
| quantity-unit mismatch fails safely | basket + preview regressions | PASS |
| ambiguous product matching never becomes a hidden winner | matching/basket/preview regressions | PASS |
| unmatched item never becomes a hidden winner | matching/basket/preview regressions | PASS |
| unknown availability remains uncertain | snapshot/basket/preview regressions | PASS |
| incomplete basket exposes no misleading complete total | basket/comparison/preview regressions | PASS |
| provenance/context mismatch fails closed | provider/snapshot/basket boundary tests | PASS |
| provider/store implementation identifiers do not leak into public preview | projection/API contract tests | PASS |
| exact addresses remain sensitive/redacted | location/provider boundary tests | PASS |
| technical coverage and production access remain independent | retailer registry/read-model/controller tests | PASS |
| blocked Magnit remains visible but unavailable | #89/#70 + public `/api/v1/retailers` regressions | PASS |
| production access is enforced before acquisition | #91 pre-acquisition gate | PASS |
| empty production-ready scope cannot invoke evidence source | #91 RED→GREEN regression | PASS |
| evidence source receives exact immutable allowed retailer set | #91 contract regression | PASS |
| out-of-scope retailer evidence is rejected | #91 fail-closed regression | PASS |
| production preview does not fall back to deterministic fixture evidence | production `NoopComparisonRuntimeEvidenceSource` + architecture/boundary tests | PASS |
| ordinary CI makes no retailer live requests | live probes require explicit guards; production preview source remains no-op | PASS |
| unknown JSON request fields fail closed | comparison preview controller regression | PASS |
| desktop/mobile critical journey remains verified | Playwright Web E2E | PASS |

## M1 implementation sequence accepted

- #72 retailer registry / coverage state
- #73 shopping list + canonical quantities
- #74 provider/path orchestration
- #75 location / fulfillment context
- #76 price / availability snapshots
- #77 deterministic matching
- #78 single-store basket quote
- #79 failure / coverage / freshness product boundary
- #80 stateless comparison-preview critical journey
- #81 structured package evidence plumbing
- #82 Magnit exact characteristic semantics
- #83 Magnit fixed-corpus package instrumentation
- #85 Magnit SKU-bound JSON-LD package evidence
- #86 Magnit bbox → `shopCode` deterministic boundary
- #87 / #69 merged-main Magnit location-resolution live proof
- #89 / #70 Magnit production-access decision: `BLOCKED` pending affirmative permission/licensed or supported access
- #91 pre-acquisition production-access enforcement

## Final hardening discovered during acceptance — #91

Before #91, `ComparisonPreviewService` applied production-access policy after `ComparisonRuntimeEvidenceSource.load(...)`. This protected output but did not structurally prevent a future live source from already making a request for a blocked retailer.

Accepted behavior now is:

1. compute immutable `requestedRetailers` only from registry entries where `isProductionReady()` is true;
2. if the set is empty, do not invoke the evidence source;
3. otherwise pass the exact immutable set to the evidence source;
4. reject returned evidence outside that set before matching/quote construction;
5. retain post-load production-readiness validation as defense in depth.

Under the accepted production registry there are currently **zero production-ready retailers**, therefore comparison preview cannot accidentally initiate retailer acquisition.

#91 squash-merged as `779d0b219a13e0bf82263a1e655fb732553ed5fe`. Its post-merge `main` run set contained eight push workflows; all completed without queued/in-progress/failure state.

## Magnit status at M1 exit

Technical evidence is accepted independently:

- coverage: `AVAILABLE_PUBLIC_WEB`;
- exact public product/store observations: technically reproducible;
- structured package evidence: accepted for the finite corpus;
- bbox → public `shopCode` resolution: accepted via merged-main live proof.

Production access is independently:

- `ProductionAccessStatus.BLOCKED`;
- product-facing `productionAccess=BLOCKED`;
- `comparisonStatus=UNAVAILABLE`;
- reason `PRODUCTION_ACCESS_BLOCKED`.

This is a Zakup Gotov fail-closed operating policy because affirmative permission for the intended recurring commercial acquisition/reuse model has not been established. It is not a claim that every automated Magnit request is prohibited or that a legal violation has been adjudicated.

## Remaining work that does not block M2 deterministic core development

These remain mandatory project work:

- #54 browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- #36 Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific production-access decisions before activation;
- package semantics for additional providers only when structured evidence proves them;
- successful real `v0.1.0-rc.3` release event with final promotion/SBOM/attestation/digest smoke evidence.

They do not invalidate the deterministic Shopping Core contracts required for Recipe → ShoppingList development.

## M2 entry contract

The first M2 vertical slice is:

`Recipe → explicit ingredients → canonical quantities → ShoppingList`

Initial scope:

- stable recipe identity and title;
- base servings;
- ingredient identity within the recipe;
- explicit ingredient name + quantity/unit;
- deterministic serving scaling;
- reuse existing shopping quantity canonicalization;
- merge only explicitly equivalent ingredient requirements under deterministic rules;
- preserve provenance from ShoppingList requirement back to recipe/ingredient;
- recipe → ShoppingList conversion;
- tests first, then API/OpenAPI/client/UI after the domain behavior is accepted.

Initial non-goals:

- AI parsing;
- arbitrary recipe-web import;
- fuzzy ingredient equivalence;
- nutrition optimization;
- pantry prediction.

## Decision

**M1 Shopping Core: ACCEPTED.**  
**M2 Recipes: GO.**

The product can now evolve recipe/planning semantics on top of the accepted deterministic shopping core while retailer connectivity and production activation continue as explicit parallel gates.
