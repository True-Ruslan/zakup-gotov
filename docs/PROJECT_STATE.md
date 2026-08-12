# Project State

Updated: 2026-08-13

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn recipes, meal plans or a manual grocery list into a location-aware comparison of complete retailer baskets while preserving price/availability evidence, package semantics, provenance, freshness and uncertainty.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core, final acceptance**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **final M1 acceptance pass, then M2 Recipes**

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility and production-access readiness are separate states.

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

## M1 delivered foundations

1. **Retailer registry / coverage state — COMPLETE (#72)** — canonical retailer identities with separate technical and production-access states.
2. **Shopping list / canonical quantities — COMPLETE (#73)** — stable identity, explicit mutations, canonical `kg → g`, `l → ml`.
3. **Provider/path orchestration — COMPLETE (#74)** — retailer/provider/acquisition/fulfillment provenance and fail-closed path selection.
4. **Location / fulfillment context — COMPLETE (#75)** — provider-neutral product location, sensitive-address redaction and provider-scoped bindings.
5. **Price / availability snapshots — COMPLETE (#76)** — immutable snapshots, observation/provider-freshness distinction, first-class `UNKNOWN`.
6. **Deterministic matching — COMPLETE (#77)** — exact-before-normalized, explicit matched/ambiguous/unmatched outcomes.
7. **Single-store basket quote — COMPLETE (#78)** — whole-package arithmetic, `COMPLETE / UNCERTAIN / INCOMPLETE`, no misleading incomplete total.
8. **Failure / coverage / freshness boundary — COMPLETE (#79)** — every canonical retailer remains visible with product-safe reasons.
9. **Stateless critical journey — COMPLETE / ACCEPTED (#80)** — comparison-preview API, generated client, responsive UI and desktop/mobile Playwright.
10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)** — `ObservedOffer → OfferSnapshot → PackageQuantitySet`; presentation text is non-authoritative.
11. **Magnit exact characteristic semantics — COMPLETE / ACCEPTED (#82)** — exact `Вес, кг` / `Объем, л`, ambiguity/conflict/invalid fail closed.
12. **Magnit fixed-corpus instrumentation — COMPLETE / ACCEPTED (#83)** — transport/identity failure separated from metadata quality.
13. **Magnit SKU-bound JSON-LD package evidence — COMPLETE / ACCEPTED (#85)** — exact `Product.sku`, proven weight/volume fields, no extra request/browser.
14. **Magnit bbox → `shopCode` boundary — COMPLETE / ACCEPTED (#86)** — deterministic public store-search contract and fail-closed resolution.
15. **Magnit merged-main LOCATION_RESOLUTION proof — COMPLETE / ACCEPTED (#87 / #69)** — exact default-branch stateless two-request reproduction.
16. **Magnit production right-to-operate decision — IMPLEMENTED / SHIPPING (#89 / #70)** — technical coverage remains `AVAILABLE_PUBLIC_WEB`, while recurring production reuse is product-policy `BLOCKED` pending affirmative permission or licensed/supported terms.

## Magnit technical evidence

### Package evidence

The same raw PUBLIC_WEB product response supplies exact-SKU JSON-LD package evidence without another retailer request or browser execution.

Accepted 20-product × 2-shop replay:

- HTTP 2xx: 40/40;
- usable observations: 40/40;
- stable identity: 20/20;
- `FOUND=36`;
- `MISSING=0`;
- `AMBIGUOUS_DIMENSIONS=4`;
- conflicts: 0;
- invalid: 0.

Milk SKU `1000013732` and kefir SKU `1000330180` remain deliberately ambiguous in both shop contexts because both weight and volume are present. Structured egg mass remains mass and cannot satisfy `PIECE` requirements.

### Location/store context

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

Merged-main run `31642543544` on SHA `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1` produced:

```text
MAGNIT_SHOPCODE_LOCATION first_status=200 first_candidates=1 first_has_992301=true first_set_cookie=false second_status=200 second_candidates=1 second_has_992301=true second_set_cookie=false same_candidate_set=true conflicting_evidence=false total_requests=2
```

Text/locality/address → coordinates remains intentionally unproven; no hidden geocoder is introduced.

## Magnit production-access decision — #70

Decision memo: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Current product state:

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- public comparison access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

Meaning of `BLOCKED`:

> Zakup Gotov has not established an affirmative right to operate the intended recurring production catalog-acquisition/reuse model, so production acquisition must remain disabled under current evidence.

This is an engineering/product operating state. It is **not** a claim that Magnit expressly prohibits every automated HTTP request and is not a legal adjudication.

Current authoritative evidence does not provide Zakup Gotov with an affirmative API/feed/catalog-reuse license for the intended recurring commercial product model. Current database-rights rules also make it unsafe to assume that a recurring catalog-oriented system is automatically covered by limited exceptions for insubstantial extraction. Therefore technical HTTP success does not promote the path to production-ready.

Unblock requires a new evidence-backed review based on affirmative permission such as written permission, supported partner/API/feed terms, published first-party reuse terms or another authoritative basis covering the actual operating model.

No production Spring/HTTP Magnit acquisition is activated by #70.

## Important remaining M1 limitations

- Production comparison evidence remains deliberately **no-op/fail-closed**. The critical journey proves product/API/core integration, not live production acquisition.
- Magnit technical package/location evidence is accepted, but production reuse remains blocked by product policy pending affirmative permission.
- Multi-dimensional package observations remain unknown under the current single-`Quantity` model; no density/category heuristic is permitted.
- `Количество в упаковке` remains unproven; mass is not count.
- Text/locality/address → coordinates is not accepted.
- **#54** browser-bridge persistent-session/store-change lifecycle hardening remains open.
- **#36** Kuper supported aggregator access remains open.
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and other mandatory retailer paths still require onboarding/hardening.
- A successful real `v0.1.0-rc.3` GitHub Release proof remains outstanding.

## M1 invariants

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
12. Corpus metrics separate transport/identity failure from metadata absence.
13. Mass, volume and count are not interchangeable.
14. Incomplete baskets never expose misleading complete-basket totals.
15. Production activation respects independent right-to-operate status.
16. Ordinary CI/browser acceptance makes no live retailer requests.
17. Production preview evidence does not fall back to deterministic fixtures.
18. Unknown JSON request fields fail closed.
19. Universal retailer connectivity remains mandatory.
20. Public technical accessibility is never treated as production authorization by itself.

## Immediate next work

1. **Final M1 acceptance pass** across `ShoppingList → ProductLocation/FulfillmentContext → ProviderEvidence → OfferSnapshot → Matching → BasketQuote → RetailerComparison`.
2. Acceptance must explicitly cover complete/uncertain/incomplete/unavailable outcomes, package unknown/unit mismatch, ambiguous matching/store selection, freshness/provenance, privacy/identifier non-leakage, and blocked production access.
3. Keep **#54** browser-bridge lifecycle hardening and **#36** Kuper supported-access investigation active in parallel.
4. Continue mandatory Chizhik/Ozon Fresh/Samokat/Lenta/VkusVill onboarding without weakening retailer-neutral boundaries.
5. Prove a successful real **`v0.1.0-rc.3` release event**.
6. After M1 is explicitly accepted, begin **M2 Recipes** with `Recipe → normalized ingredients → ShoppingList` as the first vertical slice.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
