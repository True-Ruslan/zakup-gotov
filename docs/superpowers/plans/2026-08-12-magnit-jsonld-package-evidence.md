# Magnit SKU-bound JSON-LD Package Evidence Implementation Plan

Updated: 2026-08-12
Status: executable plan

Design: `docs/superpowers/specs/2026-08-12-magnit-jsonld-package-evidence-design.md`
Baseline: `main@bee69a7bf84f1c2b98f20f76fe244d4bf3ade4a6`
Branch: `feat/magnit-jsonld-package-evidence`

## Task 1 — RED: define JSON-LD extraction contract

Create `MagnitJsonLdPackageQuantityExtractorTest` before production code.

Cover:

- exact-SKU `Product.weight = 0.45` → `FOUND 450 GRAM`;
- exact-SKU `additionalProperty(name="Объем, л", value=0.5)` → `FOUND 500 MILLILITER`;
- exact-SKU weight + volume → `AMBIGUOUS_DIMENSIONS`;
- foreign SKU and non-Product nodes ignored;
- equivalent duplicate Product evidence deduplicated;
- conflicting weight values → `CONFLICTING_VALUES`;
- recognized malformed/zero/negative values → `INVALID_VALUE`;
- object-valued weight, generic volume/size/count/title/name/description/URL and non-JSON-LD scripts cannot create evidence;
- malformed JSON-LD cannot create evidence.

Commit test-only RED and verify the API compile/test gate fails specifically because `MagnitJsonLdPackageQuantityExtractor` does not exist.

## Task 2 — GREEN: implement the smallest pure extractor

Create `MagnitJsonLdPackageQuantityExtractor` under the existing Magnit provider package.

Implementation boundaries:

- extract only `script[type="application/ld+json"]` bodies;
- parse with Jackson 3 tree model (`tools.jackson.databind`), already supplied by Spring Boot 4.1 web stack;
- recursively inspect JSON-LD nodes;
- exact `Product` + exact expected SKU identity;
- support only scalar `weight` as kg and exact `additionalProperty.name="Объем, л"` scalar value as liters;
- aggregate through the existing `MagnitPackageQuantityExtraction` status model;
- no network, Spring bean, browser, title parsing or production wiring.

Run focused tests, then full API verification.

## Task 3 — RED/GREEN: project JSON-LD evidence into the existing corpus parser

Add/extend deterministic corpus tests so `MagnitCorpusProbe.parseProductPage(html, expectedSku)` must carry JSON-LD package evidence while preserving existing price/promo/availability behavior.

RED must fail while corpus still calls the visible-text extractor.

Then replace only package extraction with the JSON-LD extractor. Do not alter request construction, eligibility, price/promo/availability parsing, fixed corpus or request count.

Run focused Magnit tests and full API verification.

## Task 4 — finite live replay

After deterministic GREEN:

- execute the same explicit/manual fixed corpus against the same two shop contexts;
- exactly 40 requests;
- capture only aggregate `MAGNIT_PHASE_B` evidence;
- compare status distribution with visible-text baseline `FOUND=0 / MISSING=40`;
- do not introduce a schedule or merge one-shot workflow plumbing.

If the live result exposes unexpected structured cases, add deterministic regressions before shipping rather than weakening fail-closed rules.

## Task 5 — durable evidence/docs

Add a durable Magnit JSON-LD evidence note containing:

- provenance observations;
- old visible-text baseline;
- new finite JSON-LD corpus distribution;
- exact accepted source semantics;
- limitations/count deferral;
- #69/#70 production gates.

Update `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, and root `CHANGELOG.md` only to the level actually proven by implementation/live evidence.

## Task 6 — review and shipping

Run exact-head branch-protection workflows/security gates.

Perform independent change review for:

- identity binding;
- fail-closed precedence;
- malformed JSON handling;
- no title/slug fallback;
- no extra network/browser behavior;
- corpus request-count preservation;
- documentation accuracy.

Fix P0/P1/P2 findings. Record P3 follow-ups if non-blocking.

When exact reviewed head is green:

- add shipping evidence marker;
- rerun final docs-only exact-head gate if marker changes the branch;
- mark PR ready;
- squash merge using expected exact head SHA;
- verify post-merge `main` head and push-triggered CI before calling the slice accepted.