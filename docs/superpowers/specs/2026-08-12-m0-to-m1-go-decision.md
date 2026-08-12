# M0 → M1 go decision

Date: 2026-08-12  
Decision: **GO to M1 — Shopping Core**

## Decision summary

M0 Product & Integration Discovery has met its **technical feasibility exit criteria**. Zakup Gotov may begin M1 Shopping Core without pretending that every target retailer is production-ready.

The M0 proof is transport-diverse:

- Pyaterochka: `AVAILABLE_BROWSER_BRIDGE`;
- Perekrestok: `AVAILABLE_BROWSER_BRIDGE`;
- Magnit: `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts.

This establishes:

1. mandatory Pyaterochka coverage through an accepted acquisition path;
2. mandatory Perekrestok coverage through an accepted acquisition path;
3. an independent non-X5 accepted technical path through Magnit;
4. two distinct acquisition modes proven end to end: user-assisted browser bridge and ordinary public web;
5. deterministic sanitized fixtures/tests that preserve retailer/provider/context provenance;
6. a retailer-neutral acquisition boundary that can expand without retailer-specific shopping/basket domain logic.

## Evidence basis

### Browser bridge

Perekrestok and Pyaterochka both passed real first-party browser gates from merged `main` and are accepted for reload-based page-snapshot acquisition.

Their production permission surface remains intentionally narrow:

- browser credentials remain in the user's first-party profile;
- extension permissions remain minimal;
- raw credentials/request payloads are not persisted;
- availability is not invented;
- post-success SPA/store-change lifecycle hardening remains tracked separately in issue #54.

### Magnit public web

Magnit Phase A passed ordinary public HTTP under two explicit public `shopCode` contexts.

Phase B then proved the fixed 20-requirement corpus in both contexts. Final merged-main evidence is recorded in [`../../integrations/magnit-public-page-phase-b-live-2026-08-12.md`](../../integrations/magnit-public-page-phase-b-live-2026-08-12.md):

- 20/20 HTTP 2xx in both contexts;
- 20/20 usable expected-SKU/current-price observations in both contexts;
- stable identity 20/20;
- zero failed requirements;
- price-bound promo status on all 40 final observations;
- availability remains explicit where supported and `UNKNOWN` elsewhere;
- no regular/old price is invented when a second supported price is absent.

## Important distinction: M0 feasibility vs production clearance

**M0 GO is not a production-data-access authorization.**

The Magnit path has two explicit follow-up constraints:

- issue #69 — safe location/address → public `shopCode` resolution is not yet proven;
- issue #70 — current catalog usage rights for recurring production automated acquisition remain `UNRESOLVED`.

Therefore M1 must not quietly turn the Magnit spike into recurring production collection. Until #70 becomes `ACCEPTABLE`, the public-web path is usable for controlled feasibility/research evidence and deterministic fixtures, not for default production polling.

Likewise, M1 must not claim Magnit automatic location/store discovery until #69 is resolved. Explicit/manual context is the proven mode.

## M1 entry rules

M1 may now implement the shopping core, but the following rules are part of the decision:

1. **Fixture-first provider orchestration.** Shopping/basket domain work must be testable without live retailer traffic.
2. **Explicit retailer coverage state.** A target retailer that lacks a usable current path is shown as unavailable/unsupported, never silently omitted.
3. **Explicit provenance.** Browser-assisted, public-web, aggregator and future partner/direct observations cannot be confused.
4. **Explicit fulfillment context.** A price is never presented as retailer-wide if it belongs to one store/fulfillment context.
5. **Explicit freshness.** Observation time is not represented as provider update time when the provider does not expose one.
6. **Fail-closed availability.** `UNKNOWN` remains a valid state; product presence does not imply stock.
7. **No hidden legal assumption.** Production provider activation must respect the recorded usage-rights state.
8. **Universal connectivity continues.** M0 completion does not remove Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill, Kuper or other registry entries from mandatory future coverage work.

## First M1 implementation sequence

The preferred first slices are:

1. canonical retailer registry and explicit coverage-state model;
2. shopping-list aggregate with canonical quantity/unit primitives;
3. provider/path orchestration over deterministic fixture providers;
4. location/fulfillment-context input boundary;
5. price/availability observation snapshots with provenance/freshness;
6. deterministic product-matching baseline;
7. complete single-store basket comparison;
8. partial-provider failure and unavailable-coverage UX;
9. browser E2E for the critical manually-entered-list journey.

Do not begin recipes, weekly planning or basket optimization until the M1 shopping-list comparison path is accepted and tested.

## Open work that does not block M1 start

- #54 — browser bridge persistent-session lifecycle hardening;
- #69 — Magnit location → public `shopCode` resolution;
- #70 — Magnit production usage-rights decision;
- #36 — Kuper supported aggregator access;
- broader mandatory retailer registry onboarding;
- `v0.1.0-rc.3` release-pipeline proof.

These remain visible engineering/product work; none should be hidden by the M0 completion label.

## Final M0 status

**M0 technical discovery: COMPLETE.**  
**M1 Shopping Core: APPROVED TO START.**
