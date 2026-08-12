# M1 Product Comparison Read Model Implementation Plan

Status: **IMPLEMENTED — final repository shipping gate pending on docs-synchronized head**

**Goal:** expose a stable, user-safe retailer comparison/readiness contract from the M1 domain core through REST/OpenAPI/shared client into a truthful responsive web status surface.

**Design:** [`../specs/2026-08-12-m1-product-comparison-read-model-design.md`](../specs/2026-08-12-m1-product-comparison-read-model-design.md)

## Global constraints

- TDD RED→GREEN for every runtime behavior.
- Ordinary CI performs no live retailer requests.
- All canonical retailers stay visible.
- Technical coverage and production access stay independent.
- Provider IDs, acquisition modes, source references and precise addresses never cross the public comparison boundary.
- `UNKNOWN` availability stays uncertainty.
- Incomplete/unavailable comparisons expose no basket total or aggregate freshness.
- No invented stale/fresh threshold.
- OpenAPI remains the generated-client source of truth.

## Task 1 — product read model from registry state — COMPLETE

- [x] Canonical retailer order/display-name RED.
- [x] Independent coverage/production-access mapping RED.
- [x] Coverage → access → runtime-data precedence RED.
- [x] Minimal comparison enums/value objects/catalog/assembler.
- [x] Full Maven `verify` GREEN.

Evidence:

- RED `903cc8d4fbc4e00a3a43cc9d9831e20507388110`: 42 compile errors, all only from the intentionally absent `comparison` contract.
- GREEN `09b895d4420f0fcc422ad4907b2262069865b088`: full Maven `verify` PASS.

Delivered:

- all eight canonical retailers remain visible in registry order;
- technical coverage maps independently from production-access readiness;
- coverage/access failures take precedence over runtime evidence;
- a connected + production-ready retailer without runtime evidence is explicitly `UNAVAILABLE / DATA_NOT_AVAILABLE`.

## Task 2 — provider/basket/freshness evidence mapping — COMPLETE

- [x] Source-unavailable provider outcome RED.
- [x] Complete basket → `READY` + total + freshness RED.
- [x] Unknown availability → `UNCERTAIN` RED.
- [x] Incomplete basket → stable deduplicated reasons, no total/freshness RED.
- [x] Observation/provider timestamp aggregation RED.
- [x] Cross-retailer/structural evidence validation RED.
- [x] Full Maven `verify` GREEN.

Evidence:

- RED `bcfe2df2d17cfaab09cb7c1ddff19a92725e0238`: 134 tests, six expected behavioral failures and zero errors; existing assembler deliberately ignored runtime evidence.
- GREEN `89ae3311605a6a8096c48cfc0d89cc1381a38258`: full Maven `verify` PASS.

Delivered:

- provider-path failure becomes product-safe `SOURCE_UNAVAILABLE`;
- complete/uncertain/incomplete basket semantics survive the product boundary unchanged;
- incomplete reasons retain shopping-list order and deduplicate deterministically;
- incomplete/unavailable results expose neither total nor aggregate freshness;
- aggregate observation time is the oldest selected observation;
- provider timestamp is exposed only when every selected snapshot has trusted provider-side update evidence, using the oldest selected provider timestamp;
- provider identity/acquisition mode remain internal evidence, not public product state.

## Task 3 — architecture boundary — COMPLETE

- [x] Add `ComparisonBoundaryArchitectureTest`.
- [x] Protect upstream `retailer/provider/shopping/matching/basket/location` packages from depending on `comparison`.
- [x] Full Maven `verify` GREEN.

Evidence:

- `64f1056a152b816f7d313d3350ab24cf15211be3`: architecture rule added; full Maven `verify` PASS.

## Task 4 — REST + OpenAPI + generated client — COMPLETE

### REST

- RED `b0886d337f8a768b38414918d7a6522d7fb54988`: 136 tests, exactly one failure because `/api/v1/retailers` returned 404; all comparison/domain tests remained GREEN.
- GREEN `6dcb0cf6360aff39ae488f5ed9b6c750aa00992f`: `GET /api/v1/retailers`, eight canonical retailers, stable order, truthful registry/readiness state, `NON_NULL` optional total/freshness and no internal source fields; full Maven `verify` PASS.

### OpenAPI / shared client

- RED `8879d83f24658d6ca25f8f359cf5604dc6b30307`: generated client test failed only because `RETAILERS_PATH` did not exist.
- Contract update `17f7a3e1a62e0bedcaabb33e2687258e0198ca32` adds the retailer readiness schema/path to OpenAPI.
- `55d84ecfb10ffd4bc97842b988250ae4a367c255` adds `RETAILERS_PATH`.
- The generated-schema gate intentionally exposed the exact `openapi-typescript 7.13.0` diff rather than accepting a hand-written approximation.
- GREEN `2019465610dc50b0c7ce6277eb854536c2a7cf9e`: generated schema synchronized; generated check, TypeScript typecheck, Vitest and client build all PASS.

## Task 5 — web status surface — COMPLETE

### Product-state component

- RED `bdca7bc14826c6c71535d8ea0e3ac6461095404c`: Web typecheck failed only because `retailer-coverage` / `retailer-readiness` did not exist.
- Loader `e24370dcb8c31f1fd002a29cb884c2c017e6c356`: server-side `API_BASE_URL` + generated client; missing env, fetch error, invalid/empty data all fail closed to explicit unavailable state.
- Initial component `a6e27e3608e546b067401311f04f236e9c78d841` renders semantic retailer states and an accessible service-unavailable alert without fake retailer cards.
- `e1e6a46fdfe2056adc1714d95d530101d7fae09c`: explicit generated-schema callback types satisfy strict `noImplicitAny`; behavior unchanged.
- `a42475e800acf5ae1d2f692023311807c120ef31`: fixes test harness scoping/cleanup discovered by Vitest; production component unchanged; lint/typecheck/Vitest/build PASS.

### Home integration

- RED `0daa281ef9770f997514ee6c3853a6f8637aa9c5`: only home-page assertion failed because the old page still rendered `M0 · Product & Integration Discovery`.
- GREEN `849d94a0564b0e2488561db724bdfb5ecc0cf7e0`: dynamic async M1 page uses the typed loader once, renders retailer coverage and keeps one H1/documentation link; unit/type/build PASS and production build proves `/` is dynamic without requiring a live API.

### Responsive browser acceptance

- Existing E2E on `849d94a...` produced clean RED: 2/4 failures only because desktop/mobile tests still expected the retired M0 copy; focus tests already passed.
- `356ef5eaadddd21a6026e3b3cd7c65bde539c411` changes browser acceptance to M1 service-unavailable behavior, no fake retailer list/cards and horizontal-overflow protection.
- That run exposed a test-locator ambiguity: Next.js route announcer also uses `role=alert`; production service alert itself was present with the correct text.
- GREEN `c101ad1e10e85abad71358f6cdd6013191f5b44d` filters the service alert by its exact product message; Web CI, production build and responsive Playwright **4/4 PASS** on desktop/mobile.

## Task 6 — durable docs and shipping — IN PROGRESS

- [x] Implement runtime/product/API/web behavior with independent RED→GREEN checkpoints.
- [x] Prove functional Web CI + responsive E2E on `c101ad1e10e85abad71358f6cdd6013191f5b44d`.
- [ ] Synchronize `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md` and this plan.
- [ ] Run full exact-head repository CI/security gate on the docs-synchronized head.
- [ ] Perform read-only Change Review.
- [ ] Record final shipping evidence and rerun marker-head branch protection.
- [ ] Mark PR ready and squash merge with expected-head SHA guard.

## Important limitations

- The controller intentionally supplies no fabricated provider/basket runtime evidence; current endpoint output reflects the canonical registry and production-access readiness truth only.
- Accepted retailer adapters still do not expose a universal trusted package-quantity field, so this slice does not claim a production end-to-end basket flow.
- No retailer-specific stale/fresh threshold is invented; only evidence timestamps cross the product boundary.
- Ordinary CI remains offline from live retailer systems.

## Next after merge

M1 moves to the **critical product journey**: enter a shopping list, choose location/fulfillment context where supported, invoke comparison through the public contract, and inspect complete/uncertain/incomplete/unavailable retailer outcomes in responsive browser E2E. Structured package-quantity extraction remains parallel evidence-driven integration work only where a retailer/source proves trustworthy semantics.

## Exit condition

The slice is complete only after the merged `main` contains the product read model, REST/OpenAPI/generated client and truthful responsive web state with no live retailer dependency, and the exact merge candidate has passed the full repository/security gate and Change Review.
