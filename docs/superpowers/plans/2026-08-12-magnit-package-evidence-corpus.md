# Magnit Package Evidence Corpus Implementation Plan

## Goal

Instrument the existing explicit/manual Magnit fixed-corpus probe with #82 package-extraction status measurement while keeping transport and production activation unchanged.

## Task 1 — TDD contract

Add `MagnitCorpusPackageEvidenceTest` requiring:

- `PageObservation.packageExtraction()` for an identity-valid page;
- `PackageEvidenceSummary.summarize(...)` classification for all five statuses;
- summary invariant: status counts sum exactly to eligible page count;
- evidence-line fields `package_evidence_pages`, `package_found`, `package_missing`, `package_ambiguous_dimensions`, `package_conflicting_values`, `package_invalid_values`.

Run focused API test and confirm RED because the probe does not expose this instrumentation yet.

## Task 2 — Minimal probe instrumentation

Modify test-only `MagnitCorpusProbe`:

- attach #82 extraction to identity-valid `PageObservation`;
- define a package-evidence summary value with structural count invariant;
- classify only HTTP 2xx + expected-SKU observations;
- add aggregate counters to `CorpusResult` and evidence line;
- do not change request headers, timeouts, shop codes, URLs, price/promo/availability parsing or live-enable property.

Run focused tests and full Magnit probe regressions.

## Task 3 — Live-test bounds

Extend the existing guarded live test only with safe bounds and the aggregate invariant. Do not introduce a minimum FOUND threshold before evidence exists.

Ordinary CI must still skip the live network test.

## Task 4 — Shipping

Update PROJECT_STATE/ROADMAP/CHANGELOG only after deterministic instrumentation is green. Open draft PR, require exact-head full CI/security, independent read-only review and squash merge. Post-merge verify `main`.

An explicit live corpus run, if performed, remains controlled research evidence and does not change #69/#70 or production activation.