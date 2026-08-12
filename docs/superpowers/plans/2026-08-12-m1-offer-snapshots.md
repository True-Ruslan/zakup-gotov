# M1 Price / Availability Snapshot Implementation Plan

Status: **COMPLETE — final marker head must satisfy branch protection before merge**

**Goal:** Introduce an immutable internal offer snapshot derived from validated provider observations while keeping observation time distinct from provider-side freshness evidence.

**Architecture decision:** Keep `ObservedOffer` as the normalized provider trust-boundary record. Do not rename it into a snapshot and do not add ambiguous freshness fields to it. Add `OfferSnapshot` as a separate immutable provider-domain record created from an already validated `ObservedOffer`, plus explicit `FreshnessEvidence`. This preserves the approved Foundation rule that a comparable offer always carries retailer context and observation time while making it impossible to claim that the retailer updated data at the moment Zakup Gotov merely observed it.

Rejected alternatives:

1. **Extend `ObservedOffer` with provider freshness fields** — rejected because it mixes adapter validation with durable comparison semantics and encourages callers to treat optional provider timestamps as universal.
2. **Treat `ObservedOffer` itself as the snapshot** — rejected because it has no snapshot identity and leaves no explicit place to distinguish provider-side freshness from observation time.
3. **Separate immutable snapshot derived from `ObservedOffer` — selected** because the trust boundary and comparison record have different responsibilities while preserving exact provenance.

**Tech Stack:** Java 25, JUnit 5, AssertJ, Maven 3.9.16.

## Global constraints

- Snapshot creation must require an already-valid `ObservedOffer`; no second looser ingestion path.
- Snapshot must preserve retailer, source provider, acquisition mode, fulfillment context, SKU, price, currency, availability, observation time and source reference exactly.
- `AVAILABLE`, `UNAVAILABLE` and `UNKNOWN` remain distinct.
- `observedAt` means when Zakup Gotov observed the offer.
- `providerUpdatedAt` is optional and may exist only when the provider/source explicitly exposes trustworthy update-time semantics.
- `providerUpdatedAt` must not be after `observedAt`; if a source timestamp is not trustworthy, callers must use observation-only freshness rather than inventing or silently coercing it.
- No stale/fresh threshold is hard-coded in this slice; thresholds remain provider-specific/configurable later.
- No persistence, REST API, matching, basket ranking or live retailer access in this slice.
- TDD is mandatory.

---

### Task 1: Freshness evidence semantics — COMPLETE

**Files:**
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FreshnessBasis.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FreshnessEvidence.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/FreshnessEvidenceTest.java`

- [x] Write RED tests requiring observation-only and provider-timestamp freshness to be distinguishable.
- [x] Require non-null observation time and reject provider timestamps later than observation time.
- [x] Require absence of provider timestamp for `OBSERVATION_ONLY` and presence for `PROVIDER_UPDATED_AT` by construction.
- [x] Run Maven verify and record RED.
- [x] Implement minimal immutable freshness values.
- [x] Run Maven verify and record GREEN.

RED head `a042e6032506646268517cfea11296566e0a7026` reached `testCompile` and failed only because `FreshnessEvidence` / `FreshnessBasis` did not exist.

GREEN head `18a0528c0f2130ce89c88feb2a3bbad8f0ac39c7` added only the minimal freshness values; the original RED test was unchanged and full Maven `verify` passed.

Delivered behavior:

- observation-only freshness never invents provider update time;
- provider-update freshness keeps `providerUpdatedAt` distinct from `observedAt`;
- provider timestamp may equal but never exceed observation time;
- null observation/provider timestamps fail closed;
- no stale threshold or wall-clock heuristic is introduced.

---

### Task 2: Immutable offer snapshot derived from validated observation — COMPLETE

**Files:**
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshotId.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshot.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshotTest.java`
- `docs/PROJECT_STATE.md`
- `docs/ROADMAP.md`
- `CHANGELOG.md`

- [x] Write RED tests requiring exact provenance/price/availability copy from `ObservedOffer`.
- [x] Require observation-only factory and explicit provider-updated factory.
- [x] Require `UNKNOWN` availability to survive unchanged.
- [x] Require snapshot identity to be independent from offer identity/data.
- [x] Require provider-update timestamp validation through freshness evidence.
- [x] Run Maven verify and record RED.
- [x] Implement minimal immutable snapshot with no direct arbitrary-value constructor; public creation goes through validated `ObservedOffer` factories.
- [x] Run Maven verify and record GREEN.
- [x] Synchronize project state, roadmap and changelog; next active slice becomes deterministic product matching.
- [x] Run full exact-head repository CI/security gate and read-only change review.

RED head `fe7fa2c22d112163afce9feb3c282e387fb077a3` compiled the completed freshness model and failed at `testCompile` only because `OfferSnapshotId` / `OfferSnapshot` did not yet exist.

GREEN head `f395797cc57f3a3f65abdb1a548f4a987121be01` added the minimal snapshot implementation; the original RED tests were unchanged and full Maven `verify` passed. The same functional head also passed the complete repository-wide gate: API, Contract, Web/E2E, CodeQL Java+JS/TS, Dependency Review, Retailer Bridge, Container Security API+Web, Release Bundle and Release Contract.

Delivered behavior:

- `OfferSnapshotId` is an independent UUID identity;
- `OfferSnapshot` has a private constructor and public creation only from validated `ObservedOffer` factories;
- retailer, source provider, acquisition mode, fulfillment context, SKU, price, currency, availability and source reference copy exactly from the validated observation;
- observation time is represented by `FreshnessEvidence`, not a second ambiguous timestamp;
- `AVAILABLE`, `UNAVAILABLE` and `UNKNOWN` survive unchanged;
- optional provider update time is accepted only through explicit provider-timestamp freshness semantics;
- persistence/API/matching/basket/live-access remain out of scope.

Shipping evidence:

- functional GREEN head `f395797cc57f3a3f65abdb1a548f4a987121be01` passed the full repository gate;
- synchronized docs head `46514d2281a0a461d296b25670651f4435c528f7` passed API, Contract, Web/E2E, CodeQL Java+JS/TS, Dependency Review, Retailer Bridge, Container Security API+Web, Release Bundle and Release Contract;
- final read-only Change Review verdict on the synchronized implementation was **Looks good**, with no P0/P1/P2 findings;
- this final marker commit changes only historical shipping evidence; branch protection must still be green on the marker head before squash merge.