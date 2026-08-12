# Magnit Package Evidence Corpus Measurement Design

Updated: 2026-08-12
Status: implementation direction

## Context

#82 accepted a pure fail-closed `MagnitPackageQuantityExtractor` for exact official `Характеристики` fields `Вес, кг` and `Объем, л`. Production activation remains blocked by #69 and #70. The next question is empirical: how useful are those accepted semantics across the existing fixed 20-product Magnit corpus and two explicit shop contexts?

The existing `MagnitCorpusProbe` is already explicit/manual live research tooling guarded by `-Dzakup.live.magnit.corpus=true`; ordinary CI never runs the network corpus.

## Goal

Instrument the existing fixed-corpus probe so each identity-valid HTTP 2xx product page is classified by the accepted #82 extractor and the final evidence line reports the distribution of:

- `FOUND`
- `MISSING`
- `AMBIGUOUS_DIMENSIONS`
- `CONFLICTING_VALUES`
- `INVALID_VALUE`

This is measurement only. It must not alter request frequency, URLs, headers, shop codes, price/availability semantics, retailer coverage/access state or production wiring.

## Eligibility rule

A page participates in package-evidence statistics only when:

1. the HTTP response is 2xx; and
2. the expected SKU identity is proven by the existing page parser.

Non-2xx/error pages must not be counted as `MISSING`, because that would conflate transport failure with absent package metadata.

A 2xx page without expected-SKU evidence is also excluded because the extractor result cannot safely be attributed to the requested corpus item.

`packageEvidencePages` is therefore the count of identity-valid HTTP 2xx observations across both shop contexts.

Invariant:

`FOUND + MISSING + AMBIGUOUS_DIMENSIONS + CONFLICTING_VALUES + INVALID_VALUE == packageEvidencePages`

## Probe integration

`MagnitCorpusProbe.parseProductPage(...)` preserves its current price/promo/availability behavior and additionally attaches `MagnitPackageQuantityExtractor.extract(html)` when expected-SKU evidence exists. `missingSku()` carries an empty `MISSING` extraction only as a safe placeholder; it is never counted because identity eligibility is false.

`runFixedCorpus(...)` collects extraction results only from eligible observations and summarizes them once at the end.

The evidence line adds only aggregate counters; it must not print product HTML, source page fragments, titles, prices, addresses or arbitrary provider data.

## Deterministic tests

- page parser with expected SKU + exact weight characteristic carries `FOUND 450 GRAM`;
- expected SKU missing never becomes package-evidence eligible;
- summary classifies all five statuses correctly;
- summary invariant rejects inconsistent manual construction;
- evidence line emits the five aggregate counters plus eligible page count;
- existing price/promo/availability tests remain unchanged and passing.

## Live evidence behavior

The existing live test remains guarded by:

`-Dzakup.live.magnit.corpus=true`

When explicitly run, it continues exactly 40 requests: 20 fixed products × 2 explicit shop contexts.

The live assertion only checks bounds/invariants, not a predetermined FOUND threshold. The first measured distribution is evidence, not an acceptance target.

## Production/access constraints

- no recurring polling;
- no production Spring/provider activation;
- no new browser path or permissions;
- no location/address resolution changes;
- #69 and #70 unchanged;
- ordinary CI live-retailer-free.

## Exit criteria

- deterministic instrumentation tests pass;
- existing Magnit price/availability probe regressions pass;
- evidence summary cannot classify transport/identity failures as package `MISSING`;
- aggregate status invariant is structural/tested;
- exact-head CI/security passes;
- independent review has no blocking findings;
- optional explicit/manual live corpus run may be performed after instrumentation is accepted or as controlled evidence during review, but its result must not be presented as recurring production support.
