# Roadmap

Updated: 2026-08-13

The roadmap is evidence-driven. Milestones change when integration evidence, product behavior or production constraints contradict an earlier assumption.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence remains Perekrestok/Pyaterochka browser bridge, Magnit public web, two acquisition modes, deterministic sanitized verification and retailer-neutral architecture. M0 is technical feasibility, not blanket production-access clearance.

## M1 — Shopping Core — FINAL ACCEPTANCE

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage, location/context, provenance, freshness, package evidence and uncertainty.

### Permanent M1 rules

- fixture/evidence-first core;
- unavailable/blocked retailers remain visible;
- exact addresses redacted by default;
- retailer/provider/acquisition/fulfillment context remain distinct;
- `UNKNOWN` availability is first-class;
- observation time is not provider-update time;
- package quantity is structured evidence, never inferred from presentation text;
- mass, volume and count are not interchangeable;
- source ambiguity/conflict fails closed;
- transport/identity failures are not counted as missing metadata;
- ordinary CI makes no live retailer requests;
- production activation is independently gated by right-to-operate/access policy;
- a public technical endpoint is never considered production authorization by itself.

### Completed implementation sequence

1. **Retailer registry + coverage state — COMPLETE (#72)**
2. **Shopping-list aggregate + canonical quantities — COMPLETE (#73)**
3. **Provider/path orchestration — COMPLETE (#74)**
4. **Location / fulfillment-context boundary — COMPLETE (#75)**
5. **Price / availability snapshots — COMPLETE (#76)**
6. **Deterministic product matching — COMPLETE (#77)**
7. **Single-store basket comparison — COMPLETE (#78)**
8. **Failure / coverage / freshness product boundary — COMPLETE (#79)**
9. **Critical product journey — COMPLETE / ACCEPTED (#80)**
10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**
11. **Magnit exact visible-characteristic semantics — COMPLETE / ACCEPTED (#82)**
12. **Magnit fixed-corpus package instrumentation — COMPLETE / ACCEPTED (#83)**
13. **Magnit SKU-bound JSON-LD package evidence — COMPLETE / ACCEPTED (#85)**
14. **Magnit deterministic bbox → `shopCode` domain boundary — COMPLETE / ACCEPTED (#86)**
15. **Magnit merged-main LOCATION_RESOLUTION live gate — COMPLETE / ACCEPTED (#87 / #69)**
16. **Magnit production access / right-to-operate — IMPLEMENTED / SHIPPING (#89 / #70)**

### Magnit accepted technical evidence

#### Package evidence

The same raw PUBLIC_WEB response supplies exact-SKU JSON-LD package evidence without another retailer request or browser execution.

Finite 20-product × 2-shop replay:

- HTTP 2xx: 40/40;
- usable: 40/40;
- stable identity: 20/20;
- `FOUND=36`;
- `MISSING=0`;
- `AMBIGUOUS_DIMENSIONS=4`;
- conflicts: 0;
- invalid: 0.

Milk SKU `1000013732` and kefir SKU `1000330180` remain deliberately ambiguous because both weight and volume are present. Count remains unproven; structured egg mass is not count.

#### Location/store context

Accepted first-party surface:

`POST /webgate/v1/stores-facade/search`

Accepted semantics:

- validated bbox → public candidate set;
- 0 → `NO_STORES`;
- 1 → `RESOLVED`;
- >1 → `AMBIGUOUS`;
- conflicting duplicate identity → `CONFLICTING_STORE_EVIDENCE`;
- explicit choice → `MANUAL`;
- never pick first/nearest without separately proven semantics.

Merged-main run `31642543544` on SHA `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1` proved the same public `shopCode=992301` across two direct stateless requests with no cookie jar, authenticator, redirects or Magnit app/auth headers.

Text/locality/address → coordinates remains intentionally unproven; no hidden geocoder is introduced.

### Magnit production access / right-to-operate — #70

Decision memo: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Chosen operational state: **`BLOCKED` pending affirmative permission or licensed/supported access terms**.

This keeps the two independent dimensions honest:

- technical coverage: `AVAILABLE_PUBLIC_WEB` / connected;
- production access: `BLOCKED` / not production-ready.

Public product behavior:

- `productionAccess=BLOCKED`;
- `comparisonStatus=UNAVAILABLE`;
- reason `PRODUCTION_ACCESS_BLOCKED`;
- no total/freshness/live Magnit evidence exposed as if production-ready.

`BLOCKED` is a conservative Zakup Gotov product-policy state. It does **not** claim that Magnit expressly prohibits every automated HTTP request and does not decide a legal dispute. It means current authoritative evidence does not establish affirmative permission for the intended recurring commercial catalog acquisition/reuse model, so production must remain off.

Unblocking requires a new source-backed review based on affirmative permission or another authoritative basis that covers the actual scope, storage, refresh/rate, attribution and redistribution constraints.

### Remaining M1 exit work — NEXT

#### 1. Final M1 acceptance pass

Verify the complete vertical path:

`ShoppingList → ProductLocation/FulfillmentContext → ProviderEvidence → OfferSnapshot → Matching → BasketQuote → RetailerComparison`

Acceptance must prove:

- complete/uncertain/incomplete/unavailable states remain distinct;
- package unknown and unit mismatch fail safely;
- ambiguous product matching and ambiguous store selection never become hidden winners;
- freshness/provenance boundaries survive end to end;
- addresses/provider/store IDs remain private/internal where required;
- incomplete baskets never expose misleading totals;
- `ProductionAccessStatus.BLOCKED` overrides runtime evidence and prevents a production-ready claim;
- production preview does not fall back to deterministic fixtures or live retailer requests;
- all eight canonical retailers remain visible.

The output should be a durable M1 acceptance document with exact test/CI evidence and an explicit **GO / NO-GO to M2** decision.

#### 2. Parallel connectivity/hardening

Continue without blocking deterministic M2 work unnecessarily:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific structured package semantics only when source evidence proves them;
- any retailer production activation only after its own explicit access/right-to-operate decision.

#### 3. Release proof

Publish and verify a successful real **`v0.1.0-rc.3`** GitHub Release event with final image promotion, SBOM/attestation and digest smoke evidence.

### M1 exit criteria

M1 exits only when:

- critical journey has automated API/integration/browser coverage;
- incomplete/ambiguous/uncertain states are transparent;
- one-store quote totals are deterministic and explainable;
- all canonical retailers remain explicit in coverage;
- package evidence remains source-structured through provider → snapshot → basket;
- source ambiguity never becomes guessed quantity;
- location/provider IDs remain provider-scoped;
- incomplete baskets cannot masquerade as winners;
- ordinary tests/product journey have no hidden live retailer dependency;
- production activation states are explicit rather than implied by technical feasibility;
- the final acceptance pass produces an explicit GO/NO-GO decision for M2.

## M2 — Recipes

Goal: make recipes a first-class deterministic source of shopping requirements.

### First vertical slice

`Recipe → ingredients → canonical quantities → ShoppingList`

Scope:

- recipe identity/title;
- servings;
- ingredient name + explicit quantity/unit;
- deterministic serving scaling;
- canonical unit normalization using existing shopping quantity rules;
- safe merging of compatible repeated ingredients;
- provenance from shopping requirement back to recipe/ingredient;
- recipe → ShoppingList conversion;
- API/OpenAPI/generated-client boundary;
- minimal responsive flow: create recipe → adjust servings → generate list → compare.

Non-goals for the first slice:

- AI recipe parsing;
- arbitrary web import;
- nutritional optimization;
- fuzzy ingredient equivalence;
- pantry prediction.

## M3 — Weekly Planning

Goal: combine several meals into one coherent shopping-requirement set.

Scope: weekly planner, safe duplicate merging/unit conversion, pantry/exclusion controls and shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat use with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: Android/iOS clients using the shared API vocabulary and generated client contracts after the web/core product is stable.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
