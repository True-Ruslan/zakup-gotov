# M3.5.1 Pantry Subtraction Semantics — Acceptance

Date: 2026-08-15  
Status: **COMPLETE / ACCEPTED**  
Issue: #121  
Implementation PR: #122

Baseline before M3.5.1: `e11fd532c8d1f927a14cb886abaa9e9988f9b21b`  
Final reviewed feature head: `b48a88e4ded457f81245223b75477be16ccf3051`  
Accepted implementation squash merge: `bcc644bb243a63941e7629755f1b3196d94332c2`

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md`](superpowers/specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics.md`](superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md`](superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md)

## Accepted boundary

M3.5.1 adds one pure deterministic domain boundary:

`canonical ShoppingList + request-scoped PantryItem[] → PantryAdjustment(remaining ShoppingList + ordered audit evidence)`

The layer is deliberately independent from WeeklyPlan application composition, Comparison Preview, HTTP/OpenAPI, browser UI, persistence and retailer/provider acquisition.

## Accepted semantics

- Pantry matching reuses the accepted exact `(ShoppingRequirement, canonical QuantityUnit)` semantics already used by Recipe/WeeklyPlan aggregation;
- `Quantity` remains authoritative for validation and canonicalization, therefore kg→g and l→ml compatibility is reused rather than reimplemented;
- requirement equality remains case-sensitive and no fuzzy/synonym/transliteration/stemming/AI equivalence is introduced;
- duplicate Pantry rows with the same exact key are additive;
- Pantry stock is consumed sequentially in source ShoppingList order and can cover demand only once;
- every source requirement consumes `min(required, available)`, so Pantry surplus cannot create zero or negative Shopping quantities;
- unmatched requirements remain unchanged;
- partial coverage preserves source ShoppingListId, ShoppingItemId, ShoppingRequirement and order while reducing only Quantity;
- full coverage removes the item from the remaining ShoppingList but retains explicit source-ID audit evidence;
- evidence distinguishes `UNCHANGED`, `PARTIALLY_COVERED` and `FULLY_COVERED` and validates unit/arithmetic invariants;
- source ShoppingList and caller-owned Pantry collections are not mutated;
- `pantry` depends only on the accepted `shopping` project package; accepted Shopping/Recipe/WeeklyPlan packages do not depend back on Pantry.

## TDD evidence

### Evidence model

RED:

`e95b076825278a4653939fe06599d5b42b3097f5`

API verification failed before Pantry production types existed.

An initial implementation candidate `a3d502d0667b48c3a21bf8f4ac0c75e7b54c91f6` exposed a Java compact-record constructor compile issue. The test contract remained unchanged; the implementation was corrected.

GREEN:

`0b04b775b80e480c7082872b70729ec01663109d`

Full API CI / Maven `verify` succeeded.

### Subtraction core

RED:

`289e973463bf2d391442a9645651851ad587e177`

API test compilation failed because `PantryShoppingListAdjuster` did not exist.

GREEN:

`a88092c914ffe5c80e4d4ad1da672ba8dcd2033d`

Full API CI / Maven `verify` succeeded with partial/full/unmatched, canonical units, duplicate stock, single-consumption, excess, exact case-sensitive matching and immutability coverage.

### Architecture boundary

`2066135d8a275feb78904bba71fec0dce7cf9625`

Full API CI / Maven `verify` succeeded with ArchUnit guards proving Pantry → Shopping-only dependency direction and no reverse dependency from accepted Shopping/Recipe/WeeklyPlan packages.

## Final PR acceptance proof

On exact reviewed PR #122 head `b48a88e4ded457f81245223b75477be16ccf3051`:

- all **9/9 normal PR workflow groups succeeded**;
- failure count was zero;
- full API CI / Maven `verify` succeeded;
- read-only review verdict: **Looks good**;
- P0: none;
- P1: none;
- P2: none;
- P3: none;
- nitpicks: none;
- unresolved review threads: **0**.

The review changed no repository files.

## Merge and post-merge acceptance proof

PR #122 was marked ready only after the exact-head CI/review gate and squash-merged with expected-head protection.

Accepted implementation merge:

`bcc644bb243a63941e7629755f1b3196d94332c2`

Post-merge evidence on that exact `main` SHA:

- exactly **8 normal push workflows** were created;
- **8/8 SUCCESS, 0 failures**;
- Web CI and responsive Web E2E succeeded;
- CodeQL Java and JavaScript/TypeScript both succeeded;
- issue #121 is closed with state reason `completed`.

Therefore M3.5.1 is accepted as:

**implemented → tested → reviewed → merged → accepted**.

## Non-goals preserved

M3.5.1 does not add:

- API endpoint/OpenAPI/generated-client exposure;
- WeeklyPlan → Pantry application composition;
- Pantry-aware retailer comparison;
- browser UI;
- saved Pantry inventory/history/database schema;
- dietary/medical or boolean `never buy` exclusions;
- fuzzy/synonym/AI equivalence;
- retailer/provider behavior;
- nutrition/macros;
- calendar/time-zone semantics.

Explicit omit-all exclusions remain a separate future semantic decision and are not encoded as zero/negative Pantry quantities.

## Decision

**M3.5.1 Pure Pantry Subtraction Semantics is COMPLETE / ACCEPTED.**

The next deterministic slice is **M3.5.2 — Stateless Pantry-aware WeeklyPlan shopping composition/API**.

M3.5.2 should compose the accepted M3.2 WeeklyPlan shopping projection with the accepted Pantry adjustment layer in a new stateless boundary, preserve inspectable original weekly requirements + pantry adjustment evidence + remaining Shopping requirements, keep existing M3.2/M3.3 behavior unchanged, and continue to defer persistence. Pantry-aware retailer comparison and responsive controls remain later slices.