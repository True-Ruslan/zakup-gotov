# Magnit Package Evidence Corpus — Shipping Evidence

Date: 2026-08-12  
PR: #83 `test(magnit): measure package evidence across fixed corpus`

## Accepted scope

- instrument the existing explicit/manual Magnit 20-product × 2-shop corpus with the already accepted #82 package extractor;
- preserve request count, shop contexts, URLs, headers, timeout, price/promo/availability parsing and live-enable property;
- count package metadata quality only for HTTP 2xx observations whose expected SKU identity is proven;
- exclude transport/error and wrong-identity observations rather than classifying them as package `MISSING`;
- structurally summarize `FOUND`, `MISSING`, `AMBIGUOUS_DIMENSIONS`, `CONFLICTING_VALUES`, `INVALID_VALUE`;
- require classified status counts to equal `packageEvidencePages` exactly;
- expose aggregate counters only in the evidence line;
- keep the live run explicit/manual behind `-Dzakup.live.magnit.corpus=true`;
- invent no minimum `FOUND` threshold before the first measured distribution exists;
- make no production provider/Spring/polling/access-state change and leave #69/#70 unchanged.

## TDD evidence

RED head: `0a6ab52a3e3e4e377585ecdeac6c412021a54092`

API test compilation failed exactly because the new instrumentation contract did not yet exist:
- `PageObservation.packageExtraction()` missing;
- `MagnitCorpusProbe.PackageEvidenceSummary` missing.

GREEN implementation then:
- attached #82 extraction to identity-valid `PageObservation`;
- added fail-closed package-evidence eligibility (`http2xx && skuEvidence`);
- added structural status summary and aggregate evidence-line counters;
- extended existing deterministic/live-test assertions without changing network behavior.

## Exact reviewed candidate

SHA: `578ad640500f08b7b3767f1a3b427a0c5f8ae1a6`

All required PR workflow groups passed:
- API CI — PASS
- Contract CI — PASS
- Web CI + responsive Web E2E — PASS
- Retailer Bridge CI — PASS
- Dependency Review — PASS
- CodeQL — PASS
- Container Security CI — PASS
- Release Bundle CI — PASS
- Release Contract CI — PASS

Ordinary CI made no live Magnit corpus request.

## Read-only review

Verdict: **Looks good**

- P0: none
- P1: none
- P2: none
- P3: the new transport eligibility predicate itself has no dedicated unit-test hook. It is intentionally private/test-tool-local and consists only of `http2xx() && observation.skuEvidence()`. Exposing the private HTTP observation shape or using reflection solely for that assertion would increase test coupling. The surrounding deterministic tests cover identity failure, status summary invariants and evidence-line behavior; the guarded live test additionally verifies package page/count bounds. Revisit only if eligibility logic becomes more complex than this two-condition fail-closed predicate.

Review scope included:
- eligibility semantics and separation of transport failure from metadata absence;
- exact status-count invariant;
- existing price/promo/availability regression preservation;
- live opt-in and request-count preservation;
- evidence-line privacy/content;
- no production/runtime wiring;
- #69/#70 production-access constraints;
- durable PROJECT_STATE/ROADMAP/CHANGELOG consistency.

No blocking correctness, security, privacy, architecture or access-policy issue was found.

## Final gate

This marker changes documentation only. The final PR head must pass the full branch-protection workflow set again before squash merge. Merge must use the exact final head SHA.

## Post-merge next step

Run the guarded fixed corpus explicitly once and record the first package-status distribution as research evidence. The run remains finite/manual and must not be interpreted as recurring production polling or as resolution of #69/#70.
