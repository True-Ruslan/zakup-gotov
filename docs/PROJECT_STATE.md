# Project State

Updated: 2026-08-13

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn recipes, meal plans or a manual grocery list into a location-aware comparison of complete retailer baskets while preserving price/availability evidence, package semantics, provenance, freshness and uncertainty.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **resolve #70 Magnit production usage/right-to-operate policy, then perform final M1 acceptance and move to M2 Recipes**

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility and production-access readiness are separate states.

## M0 exit status — COMPLETE

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` |
| Two acquisition modes | **PASS** | Browser bridge + public web |
| Deterministic verification | **PASS** | sanitized fixtures/E2E + finite live probes |
| Retailer-neutral boundary | **PASS** | provider harness + canonical retailer registry |

M0 completion means technical feasibility, not blanket permission for recurring production acquisition.

## M1 delivered foundations

1. **Retailer registry / coverage state — COMPLETE (#72)**  
   Canonical retailer identities with separate technical coverage and production-access status.

2. **Shopping list / canonical quantities — COMPLETE (#73)**  
   Stable UUID identity, explicit mutation semantics and canonical `kg → g`, `l → ml` conversion.

3. **Provider/path orchestration — COMPLETE (#74)**  
   Retailer/provider/acquisition-mode/fulfillment provenance with deterministic path priority and fail-closed validation.

4. **Location / fulfillment context — COMPLETE (#75)**  
   Provider-neutral product location, sensitive-address redaction and provider-scoped fulfillment bindings.

5. **Price / availability snapshots — COMPLETE (#76)**  
   Immutable snapshots, explicit freshness evidence and first-class `UNKNOWN` availability.

6. **Deterministic product matching — COMPLETE (#77)**  
   Exact-before-normalized matching with matched/ambiguous/unmatched outcomes and no fuzzy/AI baseline.

7. **Single-store basket quote — COMPLETE (#78)**  
   Whole-package arithmetic, explicit item states, `COMPLETE / UNCERTAIN / INCOMPLETE`, no total for incomplete baskets and fail-closed currency handling.

8. **Failure / coverage / freshness product boundary — COMPLETE (#79)**  
   All eight canonical retailers remain visible; product-safe reasons are separated from internal provider/acquisition details.

9. **Stateless critical comparison journey — COMPLETE / ACCEPTED (#80)**  
   `POST /api/v1/comparison-previews`, generated client, responsive web UI and desktop/mobile Playwright. Production evidence remains strict no-op/fail-closed.

10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**  
    `ObservedOffer → OfferSnapshot → PackageQuantitySet` preserves only explicit structured package evidence. Presentation names are non-authoritative. Merged as `d8a9e5f0f67defeeac410e0a006eab57dc2bb637`.

11. **Magnit exact characteristic semantics — COMPLETE / ACCEPTED (#82)**  
    Pure fail-closed semantics for exact `Вес, кг` and `Объем, л`; simultaneous dimensions/conflicts/invalid values never become guessed quantities. Count remains deferred. Merged as `3753a9296562354939e86876a8096c15b2957e35`.

12. **Magnit fixed-corpus instrumentation — COMPLETE / ACCEPTED (#83)**  
    Existing 20-product × 2-shop corpus reports package extraction only for HTTP-2xx, expected-SKU observations. The initial visible-text result `FOUND=0 / MISSING=40` was correctly reclassified as a visible-rendering blind spot rather than absence of source metadata. Merged as `bee69a7bf84f1c2b98f20f76fe244d4bf3ade4a6`.

13. **Magnit SKU-bound JSON-LD package evidence — COMPLETE / ACCEPTED (#85)**  
    The same raw PUBLIC_WEB response is parsed without another retailer request or browser execution. Only exact JSON-LD `Product.sku`, proven scalar `weight` and exact `additionalProperty.name="Объем, л"` participate.

    Accepted fixed-corpus replay:
    - HTTP 2xx: 40/40;
    - usable observations: 40/40;
    - stable product identity: 20/20;
    - `FOUND=36`;
    - `MISSING=0`;
    - `AMBIGUOUS_DIMENSIONS=4`;
    - conflicts: 0;
    - invalid: 0.

    The four ambiguity observations are milk SKU `1000013732` and kefir SKU `1000330180` in both shop contexts. Egg evidence is structured mass, not package count; mass cannot satisfy a `PIECE` requirement.

14. **Magnit deterministic bbox → `shopCode` boundary — COMPLETE / ACCEPTED (#86)**  
    Merged as `c3d10c672b6b67e8f03cc17823041bc88cc9bdee` and passed the complete post-merge `main` gate.

    Proven first-party contract:
    - `POST /webgate/v1/stores-facade/search`;
    - geographic `box` request;
    - response candidate path `items.items[].externalId.storeCode + coordinates`;
    - existing provider identity `magnit-public-page`;
    - `shopCode` stays internal `LocationContext.fulfillmentContextId`.

    Resolution semantics:
    - 0 candidates → `NO_STORES`;
    - exactly 1 → `RESOLVED`;
    - >1 → `AMBIGUOUS`;
    - one code with conflicting coordinates → `CONFLICTING_STORE_EVIDENCE`;
    - explicit store selection → `MANUAL`;
    - no implicit first/nearest-store rule.

15. **Magnit merged-main LOCATION_RESOLUTION live acceptance — COMPLETE / ACCEPTED (#87 / #69)**  
    PR #87 merged as `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1`. Its ordinary `main` push baseline completed successfully before the owner-only live command was issued.

    Merged-main workflow run `31642543544` checked out that exact SHA and executed exactly two direct stateless requests with:
    - no cookie jar;
    - no authenticator;
    - `Redirect.NEVER`;
    - no Magnit app/auth headers;
    - production request serializer and response parser;
    - sanitized evidence only.

    Accepted evidence:

    ```text
    MAGNIT_SHOPCODE_LOCATION first_status=200 first_candidates=1 first_has_992301=true first_set_cookie=false second_status=200 second_candidates=1 second_has_992301=true second_set_cookie=false same_candidate_set=true conflicting_evidence=false total_requests=2
    ```

    The focused live test reported 3 tests, 0 failures, 0 errors and Maven `BUILD SUCCESS`.

    Therefore **Magnit technical `LOCATION_RESOLUTION` is accepted for the proven bbox/store-selection boundary**. This does not prove arbitrary text/address → coordinates and does not authorize recurring production polling.

## Magnit current status

Technical path: **`AVAILABLE_PUBLIC_WEB`**.

Accepted technical capabilities now include:

- explicit public `shopCode` product observations;
- SKU/current-price/promo evidence on the finite corpus;
- SKU-bound JSON-LD weight/volume package evidence;
- fail-closed bbox → store candidate resolution;
- merged-main stateless reproduction of stable public `shopCode` evidence.

Remaining Magnit blocker:

- **#70 — production usage/right-to-operate decision is UNRESOLVED.**

No production Spring/HTTP client is activated from comparison preview, and recurring polling remains disabled until #70 is explicitly accepted.

## Important remaining M1 limitations

- Production comparison evidence remains deliberately **no-op/fail-closed**. The critical journey proves product/API/core integration, not live production retailer acquisition.
- Magnit JSON-LD evidence is strong on the fixed corpus but is not universal-catalog proof.
- Multi-dimensional milk/kefir package observations remain unknown under the current single-`Quantity` model; no density/category heuristic is permitted.
- `Количество в упаковке` is still unproven; structured egg mass is not count evidence.
- Text/locality/address → coordinates is not accepted; the proven Magnit resolution input is a validated bbox or explicit manual candidate selection.
- **#70** recurring Magnit production acquisition rights remain unresolved.
- **#54** browser-bridge persistent-session/store-change lifecycle hardening remains open.
- **#36** Kuper supported aggregator access remains open.
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and other mandatory retailer paths still require onboarding/hardening.
- A successful real `v0.1.0-rc.3` GitHub Release proof remains outstanding.

## M1 invariants

1. Shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer remains visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness are independent.
4. Precise addresses are sensitive and redacted by default.
5. Provider/acquisition/fulfillment identifiers remain internal and do not leak into public comparison semantics.
6. `UNKNOWN` availability is never coerced.
7. Observation time is not misrepresented as provider freshness.
8. Matching ambiguity never becomes a hidden winner.
9. Package quantity is explicit structured evidence and is never guessed from names/URLs/category/presentation text.
10. Basket package bindings derive from immutable snapshot evidence.
11. Source ambiguity/conflict remains fail-closed.
12. Corpus quality metrics separate transport/identity failure from metadata absence.
13. Mass, volume and count are not interchangeable.
14. Incomplete baskets never expose misleading complete-basket totals.
15. Production activation respects independent usage-rights status.
16. Ordinary CI/browser acceptance makes no live retailer requests.
17. Production preview evidence does not fall back to deterministic fixtures.
18. Unknown JSON request fields fail closed.
19. Universal retailer connectivity remains mandatory.
20. A technically public endpoint is not treated as recurring-production authorization without an explicit #70-style policy decision.

## Immediate next work

1. **#70 — decide Magnit production usage/right-to-operate policy** from current authoritative first-party/legal/robots/terms evidence. Technical success must not silently authorize recurring acquisition.
2. After #70, run a **final M1 acceptance pass** over shopping → location/fulfillment → evidence → snapshots → matching → basket → comparison, including failure/ambiguity/privacy/freshness invariants.
3. Keep **#54** browser-bridge lifecycle hardening and **#36** Kuper supported-access investigation active in parallel.
4. Continue mandatory Chizhik/Ozon Fresh/Samokat/Lenta/VkusVill onboarding without weakening retailer-neutral boundaries.
5. Prove a successful real **`v0.1.0-rc.3` release event**.
6. Once M1 constraints are explicitly accepted, begin **M2 Recipes** with `Recipe → normalized ingredients → ShoppingList` as the first vertical slice.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
