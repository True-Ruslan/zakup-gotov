# Roadmap

Updated: 2026-08-13

The roadmap is evidence-driven. Milestones change when integration evidence, product behavior or production constraints contradict an earlier assumption.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence remains Perekrestok/Pyaterochka browser bridge, Magnit public web, two acquisition modes, deterministic sanitized verification and retailer-neutral architecture. M0 is technical feasibility, not blanket production-access clearance.

## M1 — Shopping Core — CURRENT

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage, location/context, provenance, freshness, package evidence and uncertainty.

### Permanent M1 rules

- fixture/evidence-first core;
- unavailable/blocked retailers remain visible;
- exact addresses redacted by default;
- retailer/provider/acquisition/fulfillment context remain distinct;
- `UNKNOWN` availability is first-class;
- observation time is not provider-update time;
- package quantity is structured evidence, never inferred from names/slugs/category/presentation text;
- mass, volume and count are not interchangeable;
- source ambiguity/conflict fails closed;
- transport/identity failures are not counted as missing metadata;
- ordinary CI makes no live retailer requests;
- production activation is independently gated by right-to-operate/access policy.

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

### Magnit evidence now accepted

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

Milk SKU `1000013732` and kefir SKU `1000330180` are deliberately ambiguous in both shop contexts because both weight and volume are present. Count remains unproven; structured egg mass is not count.

#### Location/store context

Accepted first-party surface:

`POST /webgate/v1/stores-facade/search`

Accepted product semantics:

- validated bbox → public candidate set;
- 0 candidates → `NO_STORES`;
- 1 → `RESOLVED`;
- >1 → `AMBIGUOUS`;
- conflicting duplicate store identity → `CONFLICTING_STORE_EVIDENCE`;
- explicit choice → `MANUAL`;
- never pick first/nearest without a separately proven rule.

PR #86 merged the deterministic domain boundary. PR #87 merged the guarded default-branch acceptance workflow.

Merged-main run `31642543544` on SHA `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1` produced:

```text
MAGNIT_SHOPCODE_LOCATION first_status=200 first_candidates=1 first_has_992301=true first_set_cookie=false second_status=200 second_candidates=1 second_has_992301=true second_set_cookie=false same_candidate_set=true conflicting_evidence=false total_requests=2
```

Therefore issue #69's technical `LOCATION_RESOLUTION` requirement is satisfied for the proven bbox/store-selection boundary.

Text/locality/address → coordinates remains intentionally unproven; no hidden geocoder is introduced.

### Remaining M1 exit work

#### 1. Magnit production usage/right-to-operate — **NEXT (#70)**

Technical feasibility is no longer the blocker. The next mandatory decision is whether and under what constraints the public Magnit surfaces may be used for recurring production acquisition.

The #70 decision must define one explicit operational state such as:

- production enabled under documented constraints;
- guarded/low-frequency/manual-only;
- disabled pending explicit authorization;
- another evidence-backed fail-closed mode.

Until #70 is accepted:

- no recurring Magnit polling;
- no comparison-preview live Magnit HTTP client;
- ordinary CI remains live-free;
- production runtime evidence remains no-op/fail-closed.

#### 2. Final M1 acceptance pass

After #70, verify the complete vertical path:

`ShoppingList → ProductLocation/FulfillmentContext → ProviderEvidence → OfferSnapshot → Matching → BasketQuote → RetailerComparison`

Acceptance must cover:

- complete/uncertain/incomplete/unavailable outcomes;
- package unknown and unit mismatch;
- ambiguous matching and ambiguous store selection;
- freshness/provenance boundaries;
- privacy and identifier non-leakage;
- incomplete basket cannot become a winner;
- no hidden fixture/live fallback.

#### 3. Parallel connectivity/hardening

Continue without blocking M2 unnecessarily:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific structured package semantics only when source evidence proves them.

#### 4. Release proof

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
- the remaining M1 production/access constraints have an accepted outcome.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

### First vertical slice

Start with:

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

Those can follow once the deterministic recipe model is accepted.

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
