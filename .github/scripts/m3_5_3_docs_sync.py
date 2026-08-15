from pathlib import Path
import re


def sub_once(text: str, pattern: str, replacement: str, label: str) -> str:
    updated, count = re.subn(pattern, replacement, text, count=1, flags=re.DOTALL)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, got {count}")
    return updated


state_path = Path("docs/PROJECT_STATE.md")
state = state_path.read_text(encoding="utf-8")
state = sub_once(
    state,
    r"- M3\.5\.2 Stateless Pantry-aware WeeklyPlan shopping preview API — \*\*COMPLETE / ACCEPTED\*\* \(#124 / #125\)\.\n\nCurrent deterministic target: \*\*M3\.5\.3 — Pantry-aware WeeklyPlan → Comparison composition\*\*\.",
    "- M3.5.2 Stateless Pantry-aware WeeklyPlan shopping preview API — **COMPLETE / ACCEPTED** (#124 / #125);\n- M3.5.3 Pantry-aware WeeklyPlan → Comparison composition — **COMPLETE / ACCEPTED** (#127 / #128).\n\nCurrent deterministic target: **M3.5.4 — Responsive Pantry controls**.",
    "PROJECT_STATE milestone",
)
state_block = """### M3.5.3 — Pantry-aware WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md`](superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md)  
Acceptance: [`m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md`](m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md)  
Accepted merge: `079a53be066fa488ee01da18a109f4f2b1484800`.

Accepted boundary:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

Accepted result:

- accepted M3.5.2 remains authoritative for original WeeklyPlan projection, Pantry evidence and remaining demand;
- only non-empty remaining demand enters accepted ComparisonPreview;
- full Pantry coverage returns explicit `NO_REMAINING_DEMAND` and never invokes ComparisonPreviewService/runtime retailer acquisition;
- zero-demand wire output omits `comparisonPreview` rather than serializing null;
- locality is validated independently of Pantry coverage;
- ShoppingItem identity/order/requirement/canonical quantity are preserved exactly and bridge drift fails closed;
- derived ComparisonPreview validation is sanitized under the M3.5.3 problem boundary;
- OpenAPI 3.1/generated TypeScript and architecture/regression gates are synchronized;
- existing M3.3/M3.5.2 behavior remains unchanged.

Acceptance proof:

- final reviewed feature head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no unresolved P0/P1/P2/P3/nitpicks or threads;
- squash merge `079a53be066fa488ee01da18a109f4f2b1484800`;
- issue #127 closed `completed`;
- exact merge SHA — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

## Next deterministic target — M3.5.4 Responsive Pantry controls

M3.5.4 should extend the accepted WeeklyPlan-first browser journey with request-scoped Pantry editing and consume only the generated M3.5.3 comparison contract.

Required boundary:

1. keep Pantry input request-scoped and presentation-only in browser state;
2. render original weekly demand, Pantry adjustment evidence and remaining demand before retailer comparison;
3. handle `NO_REMAINING_DEMAND` explicitly without fabricating retailer results;
4. perform no browser-side Pantry subtraction, canonicalization, comparison, package arithmetic or winner recomputation;
5. preserve existing WeeklyPlan, Recipe and manual-list critical journeys;
6. cover desktop/mobile/accessibility/fail-closed transport with deterministic Playwright and no live retailer requests.

"""
state = sub_once(
    state,
    r"## Next deterministic target — M3\.5\.3 Pantry-aware WeeklyPlan → Comparison composition\n.*?(?=Explicit omit-all / never-buy exclusions remain)",
    state_block,
    "PROJECT_STATE M3.5.3 target block",
)
state_path.write_text(state, encoding="utf-8")

roadmap_path = Path("docs/ROADMAP.md")
roadmap = roadmap_path.read_text(encoding="utf-8")
roadmap_block = """#### M3.5.3 — Pantry-aware WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md`](superpowers/specs/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md`](superpowers/plans/2026-08-15-m3-5-3-pantry-weekly-plan-comparison-shipping.md)  
Acceptance: [`m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md`](m3-5-3-pantry-weekly-plan-comparison-acceptance-2026-08-15.md)  
Accepted merge: `079a53be066fa488ee01da18a109f4f2b1484800`.

Boundary:

`POST /api/v1/weekly-plan-pantry-comparison-previews`

Accepted result:

- accepted M3.5.2 owns original weekly projection, Pantry evidence and remaining demand;
- only remaining demand enters accepted ComparisonPreview;
- `NO_REMAINING_DEMAND` is explicit and skips ComparisonPreviewService/runtime acquisition entirely;
- zero-demand responses omit the comparison payload on the wire;
- locality validation is independent of Pantry coverage;
- UUID/order/requirement/canonical quantity preservation is fail-closed;
- downstream comparison validation is sanitized;
- OpenAPI/generated TypeScript and architecture/regression gates are synchronized;
- M3.3 and M3.5.2 remain unchanged.

Acceptance proof:

- final reviewed head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789` — **9/9 PR workflows SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no unresolved review findings/threads;
- squash merge `079a53be066fa488ee01da18a109f4f2b1484800`;
- issue #127 closed `completed`;
- exact merge — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

#### M3.5.4 — Responsive Pantry controls — NEXT

Goal: extend the accepted WeeklyPlan browser journey with request-scoped Pantry editing and an inspectable original → Pantry-covered → remaining demand flow before retailer comparison.

Required boundary:

1. consume only generated M3.5.3 request/response vocabulary for Pantry-aware weekly comparison;
2. Pantry state remains request-scoped browser form state; no persistence/history in this slice;
3. render original canonical weekly shopping, Pantry adjustment evidence and remaining canonical demand from server output rather than recomputing subtraction in the browser;
4. show `NO_REMAINING_DEMAND` as a truthful terminal state with no fabricated retailer comparison;
5. keep server-generated WeeklyPlan/Recipe/Shopping identities and provenance out of ordinary user-facing output;
6. preserve current M3.4 WeeklyPlan ordering/day/servings/Recipe behavior and existing Recipe/manual-list journeys;
7. fail closed on missing config, timeout, network, malformed/unexpected response or non-product-safe errors;
8. add deterministic component + desktop/mobile/accessibility Playwright coverage with no live retailer requests.

Exit gate:

- design/UX behavior documented first;
- RED→GREEN transport/form/results/component/browser coverage;
- generated M3.5.3 contract only; no browser-side Pantry/comparison semantics;
- exact-head **9/9 PR workflows + clean review**;
- squash merge + **8/8 post-merge workflows**;
- canonical acceptance docs updated separately.

"""
roadmap = sub_once(
    roadmap,
    r"#### M3\.5\.3 — Pantry-aware WeeklyPlan → Comparison composition — NEXT\n.*?(?=#### Explicit omit-all exclusions — DEFERRED SEMANTIC DECISION)",
    roadmap_block,
    "ROADMAP M3.5.3/M3.5.4 block",
)
roadmap_path.write_text(roadmap, encoding="utf-8")

changelog_path = Path("docs/CHANGELOG.md")
changelog = changelog_path.read_text(encoding="utf-8")
addition = """- Stateless M3.5.3 `POST /api/v1/weekly-plan-pantry-comparison-previews` composes accepted M3.5.2 remaining demand into accepted ComparisonPreview without modifying M3.3 or M3.5.2.
- M3.5.3 returns explicit `COMPARED / NO_REMAINING_DEMAND`; full Pantry coverage skips ComparisonPreviewService/runtime retailer acquisition rather than fabricating non-empty demand.
- Locality remains independently validated, only non-empty remaining demand reaches comparison, and ShoppingItem UUID/order/requirement/canonical quantity drift fails closed.
- Zero-demand responses omit `comparisonPreview` on the wire; derived ComparisonPreview validation is translated into sanitized M3.5.3 problem details.
- OpenAPI 3.1/generated TypeScript plus architecture/regression coverage protect the new boundary and existing M3.3/M3.5.2 behavior.
- M3.5.3 acceptance records final reviewed head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789`, squash merge `079a53be066fa488ee01da18a109f4f2b1484800`, issue #127 closure and 8/8 successful post-merge `main` workflows.
"""
changelog = sub_once(
    changelog,
    r"(- M3\.5\.2 acceptance records final reviewed head `1e08ee4f5111bb493eeb100cfc2579d6fbafa708`, squash merge `0dfbef49d265069578968fdedd18828c9452baca`, issue #124 closure and 8/8 successful post-merge `main` workflows\.\n)",
    r"\1" + addition,
    "CHANGELOG M3.5.2 line",
)
changelog_path.write_text(changelog, encoding="utf-8")
