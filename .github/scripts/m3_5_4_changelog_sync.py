from pathlib import Path

path = Path("CHANGELOG.md")
text = path.read_text(encoding="utf-8")
anchor = "- M3.5.3 acceptance records final reviewed head `2a10d5dd3e28ce6ff4eec21dd3555e8838d6f789`, squash merge `079a53be066fa488ee01da18a109f4f2b1484800`, issue #127 closure and 8/8 successful post-merge `main` workflows.\n"
addition = """- M3.5.4 advances the primary WeeklyPlan browser journey to the generated M3.5.3 Pantry-aware comparison contract while preserving existing WeeklyPlan ordering/day/servings/Recipe editing and the Recipe/manual-list secondary journeys.
- Optional request-scoped Pantry controls add/edit/remove explicit requirement + positive quantity + generated unit rows; browser-local row keys are never serialized and no Pantry persistence/history is introduced.
- Browser results render server-owned original weekly demand, ordered `UNCHANGED / PARTIALLY_COVERED / FULLY_COVERED` Pantry evidence and remaining demand without reimplementing matching, canonicalization or subtraction.
- Explicit `NO_REMAINING_DEMAND` renders a truthful terminal state with no fabricated retailer comparison; `COMPARED` without comparison evidence fails closed.
- Deterministic Playwright covers partial Pantry comparison, full Pantry coverage, mobile no-overflow, keyboard focus, unavailable service and Recipe/manual regressions with no live retailer requests.
"""
if addition in text:
    raise SystemExit(0)
count = text.count(anchor)
if count != 1:
    raise SystemExit(f"expected one M3.5.3 changelog anchor, got {count}")
path.write_text(text.replace(anchor, anchor + addition, 1), encoding="utf-8")
