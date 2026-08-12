# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **Magnit package-characteristics provenance investigation: determine whether exact structured characteristics live in embedded/bootstrap data, a separate public page request, or browser-rendered DOM; do not parse product names or activate recurring polling**

## Product connectivity invariant

Universal Retailer Connectivity remains permanent:

> Every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Accepted acquisition-mode families are supported/direct API, aggregator-backed observations, stable public web/API surfaces and user-assisted first-party browser bridge.

## M0 exit status

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts |
| Two acquisition modes | **PASS** | Browser bridge + ordinary public web |
| Deterministic verification | **PASS** | sanitized fixtures/E2E + Magnit Phase B regressions |
| Retailer-neutral boundary | **PASS** | provider harness + canonical retailer registry |

M0 completion is **technical feasibility**, not blanket production-data-access approval.

## M1 delivered foundations

1. **Retailer registry / coverage state — COMPLETE (#72)**  
   Canonical retailer/banner identities, explicit technical coverage state, independent production-access status and registry completeness.

2. **Shopping list / canonical quantities — COMPLETE (#73)**  
   Stable UUID identity, explicit mutation semantics, positive quantities and canonical `kg → g`, `l → ml` conversion.

3. **Provider/path orchestration — COMPLETE (#74)**  
   Retailer/source-provider/acquisition-mode/fulfillment provenance, deterministic path priority and fail-closed validation.

4. **Location / fulfillment context — COMPLETE (#75)**  
   Provider-neutral location, sensitive-address redaction and typed provider-scoped fulfillment bindings.

5. **Price / availability snapshots — COMPLETE (#76)**  
   Immutable snapshots, explicit freshness evidence and first-class `UNKNOWN` availability.

6. **Deterministic product matching — COMPLETE (#77)**  
   Exact-before-normalized matching, explicit matched/ambiguous/unmatched outcomes, retailer/context isolation and no fuzzy/AI baseline.

7. **Single-store basket quote — COMPLETE (#78)**  
   Explicit package evidence, whole-package arithmetic, per-item resolution states, `COMPLETE` / `UNCERTAIN` / `INCOMPLETE` basket states, no total for incomplete baskets and fail-closed mixed-currency behavior.

8. **Failure / coverage / freshness product boundary — COMPLETE (#79)**  
   All eight canonical retailers remain visible; technical coverage, production access, comparison status, product-safe reasons and freshness are separated. Provider IDs, acquisition modes, source references and precise addresses remain internal.

9. **Stateless critical comparison journey — COMPLETE / ACCEPTED (#80)**  
   `POST /api/v1/comparison-previews`, locality-only public context, manual-list input, deterministic comparison orchestration, generated client, responsive UI and desktop/mobile Playwright are merged. Production comparison evidence remains strict no-op/fail-closed.

10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**  
    Merged as `d8a9e5f0f67defeeac410e0a006eab57dc2bb637`. `ObservedOffer` can carry optional canonical package quantity, snapshots preserve it, basket/runtime bindings derive from snapshots, and presentation text is explicitly non-authoritative. Full PR and post-merge gates passed.

11. **Magnit structured package characteristics — COMPLETE / ACCEPTED (#82)**  
    Merged as `3753a9296562354939e86876a8096c15b2957e35`.
    - pure `MagnitPackageQuantityExtractor`, no HTTP/Spring activation;
    - exact `Характеристики` fields `Вес, кг` and `Объем, л` only;
    - canonical kg→g and l→ml conversion;
    - duplicate equivalent values deduplicated;
    - weight + volume → `AMBIGUOUS_DIMENSIONS`;
    - conflicting same-dimension values → `CONFLICTING_VALUES`;
    - malformed/zero/negative values → `INVALID_VALUE`;
    - title/slug/description/script/style/out-of-section numbers cannot create package evidence;
    - `Количество в упаковке` deferred from v1;
    - `FOUND` output is compatible with #81 provider/snapshot evidence while ambiguous output stays unknown.

12. **Magnit fixed-corpus package-evidence instrumentation — COMPLETE / ACCEPTED (#83)**  
    Merged as `bee69a7bf84f1c2b98f20f76fe244d4bf3ade4a6`.
    - existing explicit/manual 20-product × 2-shop corpus keeps its transport behavior and 40-request bound;
    - identity-valid pages carry #82 extraction alongside price/promo/availability evidence;
    - package metrics include only HTTP 2xx + expected-SKU observations;
    - transport/error and wrong-identity pages are excluded instead of becoming false `MISSING` cases;
    - `PackageEvidenceSummary` reports all five extraction states and structurally requires classified counts to equal eligible pages;
    - evidence output contains aggregate counters only;
    - guarded live run remains opt-in via `-Dzakup.live.magnit.corpus=true`;
    - ordinary CI remains live-retailer-free.

    Exact reviewed candidate and final shipping marker passed all PR workflow groups; independent review found no P0/P1/P2 issues. After squash merge, all eight push-triggered `main` workflows passed with zero failures.

    Design: [`superpowers/specs/2026-08-12-magnit-package-evidence-corpus-design.md`](superpowers/specs/2026-08-12-magnit-package-evidence-corpus-design.md)  
    Shipping: [`superpowers/plans/2026-08-12-magnit-package-evidence-corpus-shipping.md`](superpowers/plans/2026-08-12-magnit-package-evidence-corpus-shipping.md)

## Magnit live package corpus — ACCEPTED RESEARCH EVIDENCE / CURRENT NO-GO

A deliberate one-shot run was executed from accepted #83 `main` using temporary evidence commit `bf129c0aae3fdd80f043bfec90eafe8c545a8f7e`, GitHub Actions run `31623235860`.

The run remained finite and explicit:
- 20 fixed products;
- shop contexts `139147` and `773577`;
- exactly 40 public product-page requests;
- no schedule or recurring polling;
- package quality counted only for HTTP 2xx + expected-SKU observations.

Exact aggregate result:

```text
MAGNIT_PHASE_B total_requirements=20 total_requests=40 first_http_2xx=20 second_http_2xx=20 first_usable=20 second_usable=20 stable_identity=20 known_availability=6 promo_observations=40 near_sku_multi_price=0 near_sku_promo_marker=40 price_bound_promo_marker=40 package_evidence_pages=40 package_found=0 package_missing=40 package_ambiguous_dimensions=0 package_conflicting_values=0 package_invalid_values=0 failed_count=0 failed_requirements=
```

Interpretation:
- transport was healthy: 40/40 HTTP 2xx;
- price/SKU surface was healthy: 40/40 usable observations and stable identity for all 20 products across both contexts;
- package metadata on the **current raw/server-side PUBLIC_WEB HTML surface** was absent for every eligible observation: **0 FOUND / 40 MISSING**;
- therefore #82 semantics remain valid, but the current Java `HttpClient` public-page path is **NO-GO for Magnit package quantity**;
- do not wire this raw HTML path into production basket package evidence and do not compensate with title/slug parsing.

Durable evidence: [`integrations/magnit-package-evidence-corpus-live-2026-08-12.md`](integrations/magnit-package-evidence-corpus-live-2026-08-12.md).

## Important M1 limitations

- Production comparison evidence remains deliberately **no-op/fail-closed**. The critical journey proves product/API/core integration, not live production retailer acquisition.
- Perekrestok and Pyaterochka accepted browser paths still do not prove a dedicated structured package field.
- Magnit's current raw PUBLIC_WEB HTML supports SKU/current-price feasibility but yielded **0/40** structured package fields in the fixed live corpus.
- Official rendered Magnit pages can expose labeled characteristics, so their provenance must be established before choosing a package acquisition path.
- Magnit location/address → public `shopCode` remains unresolved (#69).
- Magnit recurring production acquisition usage rights remain unresolved (#70); recurring polling stays disabled.
- `Количество в упаковке` support remains deferred; do not pursue it until the provenance question and multi-dimensional model are understood.
- Browser-bridge persistent-session/store-change lifecycle hardening remains open (#54).
- Kuper supported aggregator access remains open (#36).
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer paths still require onboarding/hardening.
- A successful real `v0.1.0-rc.3` GitHub Release proof remains outstanding.

## Magnit status

Technical price/SKU path: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context feasibility**.

Package evidence path:
- exact weight/volume semantics — accepted in #82;
- current raw `HttpClient` HTML corpus — **NO-GO, 0/40 FOUND**;
- embedded/bootstrap structured data — not yet proven;
- separate browser page request — not yet proven;
- browser-rendered DOM — not yet proven;
- count semantics — deferred.

Constraints:
- **#69** — automatic location/address → public `shopCode` resolution not proven;
- **#70** — recurring production catalog acquisition usage rights `UNRESOLVED`.

## M1 invariants

1. Core shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer stays visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness remain independent.
4. Precise addresses are sensitive and redacted by default.
5. Retailer, source provider, acquisition mode and fulfillment context remain distinct internally and do not leak into public comparison semantics.
6. `UNKNOWN` availability is never coerced to available/unavailable.
7. Observation time is never misrepresented as provider freshness.
8. Matching ambiguity never becomes a hidden winner and no fuzzy/AI tie-break is introduced implicitly.
9. Package quantity is explicit structured evidence; missing evidence is never guessed from names, URLs, category or other presentation text.
10. Package evidence attached to runtime basket calculation derives from immutable snapshot evidence.
11. Source-specific extraction fails closed on conflicting or multi-dimensional fields unless a separate domain rule is explicitly proven.
12. Corpus package metrics classify only transport-successful, identity-valid product pages.
13. A successful price/SKU transport does not imply structured package metadata availability.
14. Incomplete baskets never expose a misleading complete-basket total.
15. Production activation respects usage-rights state.
16. Ordinary CI and browser acceptance make no live retailer requests.
17. Production preview evidence fails closed rather than falling back to deterministic fixtures.
18. Unknown public JSON request fields fail closed rather than being ignored.
19. Universal retailer connectivity remains mandatory for every registry entry.

## Immediate next work

1. **Magnit package-characteristics provenance investigation — NEXT.** Distinguish raw HTML, embedded/bootstrap machine data, separate public page requests and browser-rendered DOM using sanitized/aggregate evidence. Do not change acquisition mode until a reproducible surface is proven.
2. If structured data exists in raw/bootstrap/public request form, design a narrow exact-field extractor and replay it over the same fixed corpus before any production wiring.
3. If characteristics exist only after browser execution, treat a Magnit browser-based acquisition path as a separate architecture/evidence decision rather than silently changing `AVAILABLE_PUBLIC_WEB` semantics.
4. Continue **#69** Magnit location → `shopCode`, **#70** usage-rights resolution and **#54** browser-bridge lifecycle hardening.
5. Continue **#36** Kuper supported-access investigation and mandatory Chizhik/Ozon Fresh/Samokat/Lenta/VkusVill onboarding.
6. Prove a successful real `v0.1.0-rc.3` release event.
7. Move to **M2 Recipes** only after remaining M1 evidence/production constraints are explicitly accepted rather than hidden.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
