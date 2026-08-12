# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **ship #83 Magnit fixed-corpus package-evidence instrumentation; then obtain an explicit/manual distribution without bypassing #69/#70**

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
   `POST /api/v1/comparison-previews`, locality-only public context, manual-list input, full deterministic comparison orchestration, generated client, responsive UI and desktop/mobile Playwright are merged. Production comparison evidence remains strict no-op/fail-closed.

10. **Structured package-evidence plumbing — COMPLETE / ACCEPTED (#81)**  
    Merged as `d8a9e5f0f67defeeac410e0a006eab57dc2bb637`. `ObservedOffer` can carry optional canonical package quantity, snapshots preserve it, basket/runtime bindings derive from snapshots, and presentation text is explicitly non-authoritative. Full PR gate and post-merge `main` gate passed.

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
    - `FOUND` output is proven compatible with #81 provider/snapshot evidence while ambiguous output remains unknown.

    Exact reviewed candidate and docs-only shipping marker passed the complete PR workflow gate; read-only review reported no P0/P1/P2 findings. After squash merge, all eight push-triggered `main` workflows completed successfully with zero failures.

    Design: [`superpowers/specs/2026-08-12-magnit-structured-package-characteristics-design.md`](superpowers/specs/2026-08-12-magnit-structured-package-characteristics-design.md)  
    Evidence: [`integrations/magnit-structured-package-characteristics-2026-08-12.md`](integrations/magnit-structured-package-characteristics-2026-08-12.md)  
    Shipping: [`superpowers/plans/2026-08-12-magnit-structured-package-characteristics-shipping.md`](superpowers/plans/2026-08-12-magnit-structured-package-characteristics-shipping.md)

12. **Magnit fixed-corpus package-evidence instrumentation — IMPLEMENTED / SHIPPING (#83)**  
    #83 instruments the pre-existing explicit/manual 20-product × 2-shop Magnit research corpus without changing transport behavior:
    - identity-valid pages carry the accepted #82 extraction alongside existing price/promo/availability evidence;
    - package statistics include only HTTP 2xx responses with expected-SKU identity evidence;
    - non-2xx/error pages and wrong-identity pages are excluded rather than being mislabeled `MISSING`;
    - a structural `PackageEvidenceSummary` reports `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES`, `INVALID_VALUE`;
    - status counts must sum exactly to `packageEvidencePages`;
    - the aggregate evidence line adds only counters, never HTML/page fragments or sensitive product/provider data;
    - the guarded live test keeps the same `-Dzakup.live.magnit.corpus=true` opt-in and the same 40-request fixed corpus;
    - no minimum `FOUND` threshold is invented before the first measurement exists;
    - ordinary CI remains live-retailer-free.

    Design: [`superpowers/specs/2026-08-12-magnit-package-evidence-corpus-design.md`](superpowers/specs/2026-08-12-magnit-package-evidence-corpus-design.md)  
    Plan: [`superpowers/plans/2026-08-12-magnit-package-evidence-corpus.md`](superpowers/plans/2026-08-12-magnit-package-evidence-corpus.md)

## Important M1 limitations

- Production comparison evidence remains deliberately **no-op/fail-closed**. The critical journey proves product/API/core integration, not live production retailer acquisition.
- Perekrestok and Pyaterochka accepted browser paths still do not prove a dedicated structured package field. Do not parse product names or widen browser permissions merely to obtain package size.
- Magnit has a technically proven exact-field extractor and a fixed-corpus measurement harness, but this is **not production activation**.
- The first package-status distribution has not yet been accepted as evidence until the guarded live corpus is explicitly run.
- Magnit location/address → public `shopCode` resolution remains unresolved (#69).
- Magnit recurring production acquisition usage rights remain unresolved (#70); recurring polling remains disabled until authoritative acceptance.
- `Количество в упаковке` support is deferred pending separate source/domain evidence.
- Browser-bridge persistent-session/store-change lifecycle hardening remains open (#54).
- Kuper supported aggregator access remains open (#36).
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer paths still require onboarding/hardening.
- A successful real `v0.1.0-rc.3` GitHub Release proof remains outstanding.

## Magnit status

Technical path: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context feasibility**. Existing Phase B proved stable usable observations in explicit `shopCode` contexts; availability remains `UNKNOWN` where stock semantics are not proven.

Structured package evidence:
- `Вес, кг` — supported when unambiguous;
- `Объем, л` — supported when unambiguous;
- simultaneous weight + volume — fail closed / unknown;
- `Количество в упаковке` — deferred;
- corpus instrumentation — implemented in #83, live distribution pending explicit run.

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
13. Incomplete baskets never expose a misleading complete-basket total.
14. Production activation respects usage-rights state.
15. Ordinary CI and browser acceptance make no live retailer requests.
16. Production preview evidence fails closed rather than falling back to deterministic fixtures.
17. Unknown public JSON request fields fail closed rather than being ignored.
18. Universal retailer connectivity remains mandatory for every registry entry.

## Immediate next work

1. **Finish shipping #83** with exact-head CI/security and independent review.
2. Run the existing guarded **explicit/manual Magnit fixed corpus** once and record the first accepted package-status distribution. This remains research evidence, not recurring production polling.
3. Use the measured distribution to decide whether weight/volume support is sufficient and whether `Количество в упаковке` / multiple package dimensions need a domain extension.
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
