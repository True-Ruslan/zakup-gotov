# M1 Price / Availability Snapshot Implementation Plan

Status: **ACTIVE**

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

### Task 1: Freshness evidence semantics

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FreshnessBasis.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/FreshnessEvidence.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/FreshnessEvidenceTest.java`

- [ ] Write RED tests requiring observation-only and provider-timestamp freshness to be distinguishable.
- [ ] Require non-null observation time and reject provider timestamps later than observation time.
- [ ] Require absence of provider timestamp for `OBSERVATION_ONLY` and presence for `PROVIDER_UPDATED_AT` by construction.
- [ ] Run Maven verify and record RED.
- [ ] Implement minimal immutable freshness values.
- [ ] Run Maven verify and record GREEN.

---

### Task 2: Immutable offer snapshot derived from validated observation

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshotId.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshot.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshotTest.java`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`

- [ ] Write RED tests requiring exact provenance/price/availability copy from `ObservedOffer`.
- [ ] Require observation-only factory and explicit provider-updated factory.
- [ ] Require `UNKNOWN` availability to survive unchanged.
- [ ] Require snapshot identity to be independent from offer identity/data.
- [ ] Require provider-update timestamp validation through freshness evidence.
- [ ] Run Maven verify and record RED.
- [ ] Implement minimal immutable snapshot with no direct arbitrary-value constructor; public creation goes through validated `ObservedOffer` factories.
- [ ] Run Maven verify and record GREEN.
- [ ] Synchronize project state, roadmap and changelog; next active slice becomes deterministic product matching.
- [ ] Run full exact-head repository CI/security gate, review and squash merge.
