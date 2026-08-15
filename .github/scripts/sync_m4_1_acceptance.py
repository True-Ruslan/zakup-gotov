from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, found {count}")
    return text.replace(old, new, 1)


def replace_section(text: str, start: str, end: str, replacement: str, label: str) -> str:
    start_index = text.find(start)
    if start_index < 0:
        raise SystemExit(f"{label}: start marker not found")
    end_index = text.find(end, start_index + len(start))
    if end_index < 0:
        raise SystemExit(f"{label}: end marker not found")
    if text.find(start, start_index + len(start)) >= 0:
        raise SystemExit(f"{label}: start marker is not unique")
    return text[:start_index] + replacement + text[end_index:]


project_state_path = Path("docs/PROJECT_STATE.md")
project_state = project_state_path.read_text()
project_state = replace_once(
    project_state,
    "- M3 Weekly Planning / Pantry deterministic product slice — **COMPLETE / ACCEPTED**.\n",
    "- M3 Weekly Planning / Pantry deterministic product slice — **COMPLETE / ACCEPTED**.\n"
    "- M4.1 Basket economics foundation — **COMPLETE / ACCEPTED** (#133 / #134).\n",
    "project-state milestone",
)
project_state = replace_once(
    project_state,
    "Current deterministic target: **M4.1 — Basket economics foundation**.",
    "Current deterministic target: **M4.2 — One-retailer truthful total comparison**.",
    "project-state target",
)
project_state = replace_section(
    project_state,
    "## Next deterministic target — M4.1 Basket economics foundation\n",
    "## Magnit production state\n",
    """## M4.1 — Basket economics foundation — COMPLETE / ACCEPTED

Acceptance: [`m4-1-basket-economics-foundation-acceptance-2026-08-15.md`](m4-1-basket-economics-foundation-acceptance-2026-08-15.md).  
Accepted implementation merge: `3ccaa7b2acc1e81d7360c55872882a4252c96cae`.

Accepted semantics:

- existing `BasketTotal(BigDecimal, ISO-4217)` remains the monetary convention;
- delivery/service fees are explicitly `KNOWN / UNKNOWN`; known zero is not unknown;
- minimum-order evidence is explicitly known/unknown and evaluates to `MET / NOT_MET / UNKNOWN` from merchandise subtotal only;
- merchandise subtotal and checkout-total knowledge are separate;
- any unknown material fee makes checkout total unknown without inventing zero or hiding merchandise subtotal;
- known economics components must share the merchandise-subtotal currency;
- exact `BigDecimal` arithmetic adds no hidden rounding/rescaling;
- public `BasketEconomicsAssessment` rejects contradictory status/amount constructions;
- the M4.1 foundation remains pure basket-domain code and does not acquire provider data or change accepted M1 quote/planner behavior.

Acceptance proof:

- final reviewed feature head `a0fcd626017f93e49fc6a70c4403b68404efe6d7` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved threads;
- squash merge `3ccaa7b2acc1e81d7360c55872882a4252c96cae` with expected-head protection;
- issue #133 closed `completed`;
- exact implementation merge — **8/8 normal push workflows SUCCESS**.

## Next deterministic target — M4.2 One-retailer truthful total comparison

Compose accepted M1 single-retailer basket evidence with accepted M4.1 economics without ranking a winner yet. M4.2 must keep arithmetic checkout-total knowledge separate from retailer eligibility: a known total with minimum order `NOT_MET` is ineligible, a known total with minimum order `UNKNOWN` has unknown eligibility, and an unknown material fee cannot become a cheapest claim. Existing `COMPLETE / UNCERTAIN / INCOMPLETE / UNAVAILABLE`, retailer visibility and production-access semantics remain authoritative. No live retailer request is required for deterministic acceptance.

""",
    "project-state M4.1 section",
)
project_state = project_state.replace(
    "Continue without blocking deterministic M3 work unless evidence invalidates accepted core assumptions:",
    "Continue without blocking deterministic M4 work unless evidence invalidates accepted core assumptions:",
)
project_state_path.write_text(project_state)

roadmap_path = Path("docs/ROADMAP.md")
roadmap = roadmap_path.read_text()
roadmap = replace_section(
    roadmap,
    "## M4 — Basket Optimization — CURRENT\n",
    "## M5 — Productization\n",
    """## M4 — Basket Optimization — CURRENT

Goal: optimize real checkout cost rather than naive SKU sums while preserving truthful eligibility, completeness, uncertainty, retailer visibility and production-access semantics.

Scope: explicit checkout economics, one-retailer truthful totals, richer package/substitute optimization, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

### M4.1 — Basket economics foundation — COMPLETE / ACCEPTED

Acceptance: [`m4-1-basket-economics-foundation-acceptance-2026-08-15.md`](m4-1-basket-economics-foundation-acceptance-2026-08-15.md)  
Accepted implementation merge: `3ccaa7b2acc1e81d7360c55872882a4252c96cae`.

Accepted result:

- explicit known/unknown delivery and service fees with known zero preserved;
- explicit known/unknown minimum-order threshold and `MET / NOT_MET / UNKNOWN` assessment from merchandise subtotal only;
- merchandise subtotal remains inspectable independently from checkout-total knowledge;
- unknown material fee fails closed rather than becoming zero;
- exact currency-compatible `BigDecimal` checkout arithmetic with no hidden rounding;
- self-validating assessment prevents contradictory economics state;
- pure basket-domain boundary with no provider acquisition, optimizer, HTTP/OpenAPI/UI or M1 quote mutation.

### M4.2 — One-retailer truthful total comparison — NEXT

Compose accepted `SingleStoreBasketQuote` merchandise evidence with M4.1 economics and expose a deterministic retailer-level assessment without choosing a winner.

Required semantics:

- checkout-total knowledge and retailer eligibility are independent;
- minimum order `MET` may be eligible subject to accepted basket/access state;
- minimum order `NOT_MET` is ineligible even when checkout arithmetic is known;
- minimum order `UNKNOWN` yields unknown eligibility and is never silently eligible;
- an unknown material fee keeps checkout total unknown and cannot support a cheapest claim;
- accepted `COMPLETE / UNCERTAIN / INCOMPLETE / UNAVAILABLE`, matching ambiguity, retailer visibility and production-access rules remain authoritative;
- deterministic acceptance uses supplied/sanitized evidence only and makes no live retailer requests.

### M4.3 — Basket optimizer

After M4.2 establishes truthful comparable retailer assessments, define deterministic optimizer eligibility, candidate ordering/tie semantics, package/substitution policy and confidence/freshness handling. Do not let an unknown/ineligible/incomplete candidate become a hidden winner.

### M4.4 — Optimization UX

Project accepted optimizer evidence into responsive browser flows with explainable subtotal/fees/minimum-order/eligibility/total states. Browser code must render server-owned decisions rather than recomputing economics or winners.

""",
    "roadmap M4 section",
)
roadmap_path.write_text(roadmap)

changelog_path = Path("CHANGELOG.md")
changelog = changelog_path.read_text()
changelog = replace_once(
    changelog,
    "#### Product and shopping core\n",
    """#### Basket economics

- M4.1 adds explicit `KNOWN / UNKNOWN` delivery and service fees while preserving known zero as real evidence rather than treating absence as free checkout.
- Minimum-order evidence is explicit and evaluates to `MET / NOT_MET / UNKNOWN` against merchandise subtotal only; delivery/service fees cannot satisfy a merchandise minimum.
- Merchandise subtotal remains separate from checkout-total knowledge; any unknown material fee fails closed without fabricating a zero fee or hiding known merchandise cost.
- Known economics components must share currency and exact `BigDecimal` arithmetic performs no implicit checkout rounding/rescaling.
- `BasketEconomicsAssessment` self-validates minimum-order status, checkout knowledge and checkout amount so contradictory public states cannot be constructed.
- M4.1 remains a pure basket-domain foundation with no optimizer, provider/browser/network acquisition, HTTP/OpenAPI/UI change or mutation of accepted M1 `SingleStoreBasketQuote` semantics.
- M4.1 acceptance records final reviewed head `a0fcd626017f93e49fc6a70c4403b68404efe6d7`, squash merge `3ccaa7b2acc1e81d7360c55872882a4252c96cae`, issue #133 closure and 8/8 successful post-merge `main` workflows.

#### Product and shopping core
""",
    "changelog basket economics added",
)
changelog = replace_once(
    changelog,
    "- Project phase advanced from M0 Product & Integration Discovery to M1 Shopping Core, then M2 Recipes, and now **M3 Weekly Planning / Pantry**.",
    "- Project phase advanced from M0 Product & Integration Discovery through M1 Shopping Core, M2 Recipes and M3 Weekly Planning / Pantry; the current deterministic phase is **M4 Basket Optimization**.",
    "changelog phase",
)
changelog = replace_once(
    changelog,
    "- The current deterministic target is **M3.5.3 Pantry-aware WeeklyPlan → Comparison composition**; accepted M3.3 remains unchanged and zero-remaining-demand semantics must be designed explicitly before production code.",
    """- M3.5.3 Pantry-aware WeeklyPlan → Comparison composition is **COMPLETE / ACCEPTED** after final reviewed head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789`, squash merge `079a53be066fa488ee01da18a109f4f2b1484800`, issue #127 closure and 8/8 successful post-merge `main` workflows.
- M3.5.4 responsive Pantry controls is **COMPLETE / ACCEPTED** after final reviewed head `d2fefd5391b9ec471192aff4120adfc4e7c0cb4c`, squash merge `7a437b612b4e0a36e10f2ae2a5708346f93431ce`, issue #130 closure and 8/8 successful post-merge `main` workflows.
- M3 Weekly Planning / Pantry is **COMPLETE / ACCEPTED**.
- M4.1 Basket economics foundation is **COMPLETE / ACCEPTED** after final reviewed head `a0fcd626017f93e49fc6a70c4403b68404efe6d7`, squash merge `3ccaa7b2acc1e81d7360c55872882a4252c96cae`, issue #133 closure and 8/8 successful post-merge `main` workflows.
- The current deterministic target is **M4.2 One-retailer truthful total comparison**; arithmetic checkout-total knowledge must remain separate from retailer eligibility and no winner is selected in this slice.""",
    "changelog deterministic target",
)
changelog_path.write_text(changelog)
