from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, got {count}")
    return text.replace(old, new, 1)


def replace_section(text: str, start: str, end: str, replacement: str, label: str) -> str:
    if text.count(start) != 1:
        raise SystemExit(f"{label}: start marker count={text.count(start)}")
    start_index = text.index(start)
    end_index = text.find(end, start_index + len(start))
    if end_index < 0:
        raise SystemExit(f"{label}: end marker not found")
    return text[:start_index] + replacement + text[end_index:]


state_path = Path("docs/PROJECT_STATE.md")
state = state_path.read_text(encoding="utf-8")
state = replace_once(
    state,
    "Current phase: **M3 — Weekly Planning / Pantry**",
    "Current phase: **M4 — Basket Optimization**",
    "PROJECT_STATE phase",
)
state = replace_once(
    state,
    "- M3.5.3 Pantry-aware WeeklyPlan → Comparison composition — **COMPLETE / ACCEPTED** (#127 / #128).",
    "- M3.5.3 Pantry-aware WeeklyPlan → Comparison composition — **COMPLETE / ACCEPTED** (#127 / #128);\n- M3.5.4 Responsive Pantry controls — **COMPLETE / ACCEPTED** (#130 / #131);\n- M3 Weekly Planning / Pantry deterministic product slice — **COMPLETE / ACCEPTED**.",
    "PROJECT_STATE milestone line",
)
state = replace_once(
    state,
    "Current deterministic target: **M3.5.4 — Responsive Pantry controls**.",
    "Current deterministic target: **M4.1 — Basket economics foundation**.",
    "PROJECT_STATE target",
)
state = replace_once(
    state,
    "## M3.5 — Pantry / exclusions semantics — IN PROGRESS",
    "## M3.5 — Pantry / exclusions semantics — COMPLETE / ACCEPTED",
    "PROJECT_STATE M3.5 status",
)
state_block = """### M3.5.4 — Responsive Pantry controls — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md`](superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md)  
Acceptance: [`m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md`](m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md)  
Accepted merge: `7a437b612b4e0a36e10f2ae2a5708346f93431ce`.

Accepted browser result:

- primary WeeklyPlan journey consumes generated M3.5.3 Pantry-aware comparison only;
- Pantry rows are optional request-scoped browser state with no persistence/history;
- original demand, Pantry audit and remaining demand render directly from server evidence;
- production browser performs no Pantry matching/canonicalization/subtraction;
- `NO_REMAINING_DEMAND` is a truthful terminal state with no fabricated retailer comparison;
- Recipe/manual-list journeys remain regression-covered;
- deterministic desktop/mobile/accessibility acceptance makes no live retailer request.

Acceptance proof:

- final reviewed feature head `d2fefd5391b9ec471192aff4120adfc4e7c0cb4c` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks or unresolved threads;
- squash merge `7a437b612b4e0a36e10f2ae2a5708346f93431ce`;
- issue #130 closed `completed`;
- exact merge SHA — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

Explicit omit-all / never-buy exclusions remain intentionally deferred. They are not Pantry stock and must not be represented as zero/negative quantities.

## Next deterministic target — M4.1 Basket economics foundation

Start M4 with a semantics-first design before implementation. The first slice should define explicit retailer/basket economics evidence such as delivery/service fees and minimum-order constraints, how unknown values affect truthful comparison state, and when an effective checkout total may be exposed. It must preserve accepted package, completeness, uncertainty, production-access and no-hidden-winner invariants before any richer optimizer is introduced.

"""
state = replace_section(
    state,
    "## Next deterministic target — M3.5.4 Responsive Pantry controls\n",
    "Explicit omit-all / never-buy exclusions remain",
    state_block,
    "PROJECT_STATE M3.5.4 section",
)
state_path.write_text(state, encoding="utf-8")

roadmap_path = Path("docs/ROADMAP.md")
roadmap = roadmap_path.read_text(encoding="utf-8")
roadmap = replace_once(
    roadmap,
    "## M3 — Weekly Planning / Pantry — CURRENT",
    "## M3 — Weekly Planning / Pantry — COMPLETE / ACCEPTED",
    "ROADMAP M3 status",
)
roadmap_block = """#### M3.5.4 — Responsive Pantry controls — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md`](superpowers/specs/2026-08-15-m3-5-4-responsive-pantry-controls-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md`](superpowers/plans/2026-08-15-m3-5-4-responsive-pantry-controls-shipping.md)  
Acceptance: [`m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md`](m3-5-4-responsive-pantry-controls-acceptance-2026-08-15.md)  
Accepted merge: `7a437b612b4e0a36e10f2ae2a5708346f93431ce`.

Accepted result:

- primary WeeklyPlan browser transport uses generated M3.5.3 only;
- request-scoped Pantry controls are optional and stateless;
- server-owned original/audit/remaining demand is rendered without browser subtraction;
- full Pantry coverage renders `NO_REMAINING_DEMAND` without retailer output;
- mobile/accessibility/fail-closed and Recipe/manual regressions are deterministic and network-safe.

Acceptance proof:

- final reviewed head `d2fefd5391b9ec471192aff4120adfc4e7c0cb4c` — **9/9 PR workflows SUCCESS**, 0 failure/skipped/cancelled;
- read-only review **Looks good**, no unresolved findings/threads;
- squash merge `7a437b612b4e0a36e10f2ae2a5708346f93431ce`;
- issue #130 closed `completed`;
- exact merge — **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

"""
roadmap = replace_section(
    roadmap,
    "#### M3.5.4 — Responsive Pantry controls — NEXT\n",
    "#### Explicit omit-all exclusions — DEFERRED SEMANTIC DECISION\n",
    roadmap_block,
    "ROADMAP M3.5.4 section",
)
m4_block = """## M4 — Basket Optimization — CURRENT

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

### M4.1 — Basket economics foundation — NEXT

Begin with design/semantics rather than an optimizer. Define explicit delivery/service-fee and minimum-order evidence, unknown/unavailable handling, the relationship between merchandise subtotal and effective checkout total, and fail-closed comparison rules before ranking or multi-store optimization. Preserve existing package, completeness, uncertainty, retailer visibility and production-access invariants.

"""
roadmap = replace_section(
    roadmap,
    "## M4 — Basket Optimization\n",
    "## M5 — Productization\n",
    m4_block,
    "ROADMAP M4 section",
)
roadmap_path.write_text(roadmap, encoding="utf-8")

changelog_path = Path("CHANGELOG.md")
changelog = changelog_path.read_text(encoding="utf-8")
anchor = "- Deterministic Playwright covers partial Pantry comparison, full Pantry coverage, mobile no-overflow, keyboard focus, unavailable service and Recipe/manual regressions with no live retailer requests.\n"
addition = "- M3.5.4 acceptance records final reviewed head `d2fefd5391b9ec471192aff4120adfc4e7c0cb4c`, squash merge `7a437b612b4e0a36e10f2ae2a5708346f93431ce`, issue #130 closure and 8/8 successful post-merge `main` workflows.\n"
if addition not in changelog:
    changelog = replace_once(changelog, anchor, anchor + addition, "CHANGELOG M3.5.4 anchor")
changelog_path.write_text(changelog, encoding="utf-8")
