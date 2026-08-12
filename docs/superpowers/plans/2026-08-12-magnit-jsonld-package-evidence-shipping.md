# Magnit SKU-bound JSON-LD Package Evidence Shipping Evidence

Updated: 2026-08-12
PR: #85
Reviewed code/docs candidate: `6c817ff6e562846f28a00d3a29873261e5054919`
Status: ready for final docs-only branch-protection gate

## Scope reviewed

- new pure `MagnitJsonLdPackageQuantityExtractor`;
- deterministic extractor tests;
- one-line Magnit corpus package-projection switch from visible text to exact-SKU JSON-LD;
- deterministic corpus regressions;
- design/plan/evidence documents;
- PROJECT_STATE / ROADMAP / CHANGELOG synchronization.

Temporary live-evidence workflow/test files were isolated on `evidence/magnit-jsonld-corpus-2026-08-12` and are not present in the feature branch diff or intended `main` history.

## TDD evidence

- extractor RED: API verification failed specifically because the new extractor contract did not exist;
- extractor GREEN: full API verification passed after the minimal pure implementation;
- corpus-projection RED: a JSON-LD-only fixture failed while the corpus still called the visible-text extractor;
- corpus-projection GREEN: changing only package projection to `MagnitJsonLdPackageQuantityExtractor.extract(html, expectedSku)` restored full API verification while preserving existing price/promo/availability regressions.

## Finite live evidence

Same existing fixed corpus:

- 20 requirements;
- two explicit shops `139147` and `773577`;
- exactly 40 requests;
- 40 package-evidence eligible pages;
- 20/20 + 20/20 HTTP 2xx;
- 20/20 + 20/20 usable observations;
- 20/20 stable product identity;
- failed requirements: 0.

Package distribution:

- `FOUND=36`;
- `MISSING=0`;
- `AMBIGUOUS_DIMENSIONS=4`;
- `CONFLICTING_VALUES=0`;
- `INVALID_VALUE=0`.

Earlier visible-text baseline on the same eligible-page count was `FOUND=0 / MISSING=40`.

One-shop sanitized diagnostic identified ambiguity only for:

- milk SKU `1000013732`;
- kefir SKU `1000330180`.

Both are ambiguous in both shop contexts, accounting for exactly 4/40 observations. The other fixed requirements were `FOUND` in the diagnostic.

Egg SKU `2047000014` produced structured mass evidence (`700 g`) rather than count evidence. Existing basket regression coverage requires canonical-unit equality and rejects `PIECE` vs `GRAM`, so mass cannot silently satisfy a count requirement.

## Independent review verdict

Verdict: **Looks good**.

Blocking findings:

- P0: none;
- P1: none;
- P2: none.

Non-blocking P3:

- JSON-LD script discovery intentionally uses a minimal exact-media-type HTML scanner rather than a DOM parser. This depends on the proven Magnit `application/ld+json` surface and would ignore an unproven media-type variant such as additional parameters. The fixed 40-page replay nevertheless classified every eligible page (`36 FOUND + 4 explicit ambiguity`) and no additional HTML dependency is justified by current evidence. Revisit only if future evidence shows missed JSON-LD scripts.

Review-specific checks passed:

- exact expected SKU binds JSON-LD Product evidence;
- foreign Product nodes cannot contaminate the result;
- title/name/description/URL/category/generic-size/count-looking fields cannot create quantity;
- malformed JSON-LD cannot be regex-repaired into evidence;
- object-valued weight is not guessed;
- invalid/conflicting/multi-dimensional values fail closed;
- no browser acquisition or additional retailer request was introduced;
- ordinary CI remains live-retailer-free;
- #69 and #70 remain explicit production blockers;
- package quantities remain canonical-unit compatible through #81 snapshot/basket semantics.

## Exact reviewed-candidate CI/security

All pull-request workflow groups completed successfully on `6c817ff6e562846f28a00d3a29873261e5054919`:

1. API CI — PASS
2. Contract CI — PASS
3. Web CI / responsive Web E2E — PASS
4. CodeQL — PASS
5. Dependency Review — PASS
6. Container Security CI — PASS
7. Retailer Bridge CI — PASS
8. Release Contract CI — PASS
9. Release Bundle CI — PASS

No failed workflow group remained on the reviewed candidate.

## Final gate

This shipping record changes only documentation. The resulting final head must independently pass the full branch-protection/CI/security set before #85 is marked ready and squash-merged.

After merge, `main` push-triggered workflows must pass before the slice is called **ACCEPTED**.