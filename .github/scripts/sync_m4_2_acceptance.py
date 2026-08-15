from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count == 0:
        if new in text:
            return text
        raise SystemExit(f"{label}: expected source text not found")
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one source occurrence, found {count}")
    return text.replace(old, new, 1)


project_state_path = Path("docs/PROJECT_STATE.md")
project_state = project_state_path.read_text()
project_state = replace_once(
    project_state,
    "- M4.1 Basket economics foundation — **COMPLETE / ACCEPTED** (#133 / #134).\n\nCurrent deterministic target: **M4.2 — One-retailer truthful total comparison**.",
    "- M4.1 Basket economics foundation — **COMPLETE / ACCEPTED** (#133 / #134).\n- M4.2 One-retailer truthful total comparison — **COMPLETE / ACCEPTED** (#136 / #137).\n\nCurrent deterministic target: **M4.3 — Basket optimizer**.",
    "project-state milestone/target",
)
project_state = replace_once(
    project_state,
    "## Next deterministic target — M4.2 One-retailer truthful total comparison\n\nCompose accepted M1 single-retailer basket evidence with accepted M4.1 economics without ranking a winner yet. M4.2 must keep arithmetic checkout-total knowledge separate from retailer eligibility: a known total with minimum order `NOT_MET` is ineligible, a known total with minimum order `UNKNOWN` has unknown eligibility, and an unknown material fee cannot become a cheapest claim. Existing `COMPLETE / UNCERTAIN / INCOMPLETE / UNAVAILABLE`, retailer visibility and production-access semantics remain authoritative. No live retailer request is required for deterministic acceptance.",
    "## M4.2 — One-retailer truthful total comparison — COMPLETE / ACCEPTED\n\nAcceptance: [`m4-2-one-retailer-truthful-total-acceptance-2026-08-15.md`](m4-2-one-retailer-truthful-total-acceptance-2026-08-15.md).  \nAccepted implementation merge: `69f9cb1afd1b16af938052bbca570cbd4ce52557`.\n\nAccepted semantics:\n\n- accepted M1 `RetailerComparisonView.total` remains merchandise subtotal and is never silently redefined as checkout total;\n- M4.1 economics are bound to `RetailerId` at the M4.2 public composition boundary and cross-retailer fee/minimum evidence fails closed before arithmetic;\n- checkout eligibility is explicit `ELIGIBLE / INELIGIBLE / UNKNOWN` and remains independent from arithmetic checkout-total knowledge;\n- known minimum `NOT_MET` is ineligible even when checkout arithmetic is fully known; upstream `UNCERTAIN` or unknown minimum never becomes silently eligible;\n- comparability is explicit `COMPARABLE / NOT_COMPARABLE` and requires `READY + ELIGIBLE + KNOWN checkout total`;\n- only comparable assessments expose `comparableCheckoutTotal`, exactly equal to the accepted M4.1 checkout total;\n- known arithmetic totals for ineligible/unknown/uncertain states remain inspectable but cannot support a cheapest claim;\n- `INCOMPLETE / UNAVAILABLE` produce no fabricated checkout assessment;\n- public assessment/result objects reject subtotal, eligibility, comparability and cross-comparison drift;\n- architecture permits only accepted `basket`, accepted `comparison` and the finite `RetailerId` bridge, with no provider/network/API/UI or winner/ranking behavior.\n\nAcceptance proof:\n\n- final reviewed feature head `1d6dae470c04ab1d8279f891766fc16698286edb` — **9/9 PR workflow groups SUCCESS**, 0 failure/skipped/cancelled;\n- read-only review **Looks good**, no P0/P1/P2/P3/nitpicks and no unresolved threads;\n- squash merge `69f9cb1afd1b16af938052bbca570cbd4ce52557` with expected-head protection;\n- issue #136 closed `completed`;\n- exact implementation merge — **8/8 normal push workflows SUCCESS**, including CodeQL Java and JavaScript/TypeScript.\n\n## Next deterministic target — M4.3 Basket optimizer\n\nDefine deterministic optimizer eligibility over accepted M4.2 checkout assessments. Only `COMPARABLE` candidates may compete for a winner. M4.3 must define deterministic candidate ordering and tie semantics, keep `INELIGIBLE / UNKNOWN / NOT_COMPARABLE / INCOMPLETE / UNAVAILABLE` candidates out of winner selection, and explicitly define package/substitution plus confidence/freshness policy before exposing any cheapest/winner claim. Deterministic acceptance remains supplied-evidence-only with no live retailer request.",
    "project-state M4.2 section",
)
project_state = replace_once(
    project_state,
    "21. Pantry-aware comparison must never fabricate shopping demand solely to satisfy a downstream non-empty comparison contract.",
    "21. Pantry-aware comparison must never fabricate shopping demand solely to satisfy a downstream non-empty comparison contract.\n22. Merchandise subtotal, checkout-total knowledge, checkout eligibility and optimizer comparability are separate facts; one must never be silently substituted for another.\n23. Retailer checkout economics must be bound to the same `RetailerId` as the retailer comparison before arithmetic; cross-retailer economics evidence fails closed.\n24. A known arithmetic checkout total does not imply an eligible or comparable candidate.\n25. Future winner selection may consider only explicit M4.2 `COMPARABLE` candidates; uncertain, ineligible, incomplete or unavailable evidence cannot become a hidden winner.",
    "project-state M4 invariants",
)
project_state_path.write_text(project_state)

roadmap_path = Path("docs/ROADMAP.md")
roadmap = roadmap_path.read_text()
roadmap = replace_once(
    roadmap,
    "### M4.2 — One-retailer truthful total comparison — NEXT\n\nCompose accepted `SingleStoreBasketQuote` merchandise evidence with M4.1 economics and expose a deterministic retailer-level assessment without choosing a winner.\n\nRequired semantics:\n\n- checkout-total knowledge and retailer eligibility are independent;\n- minimum order `MET` may be eligible subject to accepted basket/access state;\n- minimum order `NOT_MET` is ineligible even when checkout arithmetic is known;\n- minimum order `UNKNOWN` yields unknown eligibility and is never silently eligible;\n- an unknown material fee keeps checkout total unknown and cannot support a cheapest claim;\n- accepted `COMPLETE / UNCERTAIN / INCOMPLETE / UNAVAILABLE`, matching ambiguity, retailer visibility and production-access rules remain authoritative;\n- deterministic acceptance uses supplied/sanitized evidence only and makes no live retailer requests.\n\n### M4.3 — Basket optimizer\n\nAfter M4.2 establishes truthful comparable retailer assessments, define deterministic optimizer eligibility, candidate ordering/tie semantics, package/substitution policy and confidence/freshness handling. Do not let an unknown/ineligible/incomplete candidate become a hidden winner.",
    "### M4.2 — One-retailer truthful total comparison — COMPLETE / ACCEPTED\n\nAcceptance: [`m4-2-one-retailer-truthful-total-acceptance-2026-08-15.md`](m4-2-one-retailer-truthful-total-acceptance-2026-08-15.md)  \nAccepted implementation merge: `69f9cb1afd1b16af938052bbca570cbd4ce52557`.\n\nAccepted result:\n\n- existing M1 merchandise subtotal remains unchanged;\n- retailer-bound M4.1 economics fail closed on cross-retailer identity mismatch before arithmetic;\n- eligibility is `ELIGIBLE / INELIGIBLE / UNKNOWN` and independent from checkout-total knowledge;\n- comparability is `COMPARABLE / NOT_COMPARABLE`; only `READY + ELIGIBLE + KNOWN checkout total` is comparable;\n- `INCOMPLETE / UNAVAILABLE` receive no fabricated checkout assessment;\n- known zero fees and known arithmetic totals remain inspectable without upgrading ineligible/uncertain candidates;\n- self-validating public objects and architecture guards reject contradictory/cross-boundary evidence;\n- no winner/ranking, provider acquisition, HTTP/OpenAPI/UI or live retailer request is introduced.\n\nAcceptance proof:\n\n- final reviewed head `1d6dae470c04ab1d8279f891766fc16698286edb` — **9/9 PR workflows SUCCESS**;\n- clean read-only review, no unresolved threads;\n- squash merge `69f9cb1afd1b16af938052bbca570cbd4ce52557`;\n- issue #136 closed `completed`;\n- exact merge — **8/8 normal push workflows SUCCESS**.\n\n### M4.3 — Basket optimizer — NEXT\n\nDefine deterministic optimizer behavior over accepted M4.2 assessments. Only `COMPARABLE` candidates may compete. Specify candidate ordering, exact tie semantics, package/substitution policy and confidence/freshness handling before producing a winner. `INELIGIBLE / UNKNOWN / NOT_COMPARABLE / INCOMPLETE / UNAVAILABLE` candidates must remain visible but cannot become a hidden winner. Deterministic acceptance uses supplied/sanitized evidence only and makes no live retailer requests.",
    "roadmap M4.2/M4.3",
)
roadmap_path.write_text(roadmap)

changelog_path = Path("CHANGELOG.md")
changelog = changelog_path.read_text()
changelog = replace_once(
    changelog,
    "- M4.1 acceptance records final reviewed head `a0fcd626017f93e49fc6a70c4403b68404efe6d7`, squash merge `3ccaa7b2acc1e81d7360c55872882a4252c96cae`, issue #133 closure and 8/8 successful post-merge `main` workflows.",
    "- M4.1 acceptance records final reviewed head `a0fcd626017f93e49fc6a70c4403b68404efe6d7`, squash merge `3ccaa7b2acc1e81d7360c55872882a4252c96cae`, issue #133 closure and 8/8 successful post-merge `main` workflows.\n- M4.2 adds a downstream retailer-checkout composition layer that preserves M1 merchandise subtotal while exposing explicit `ELIGIBLE / INELIGIBLE / UNKNOWN` checkout eligibility and `COMPARABLE / NOT_COMPARABLE` optimizer comparability.\n- Retailer-neutral M4.1 economics are bound to `RetailerId` at the M4.2 public boundary; cross-retailer fee/minimum evidence fails closed before checkout arithmetic and no raw service bypass remains.\n- Only `READY + ELIGIBLE + KNOWN checkout total` exposes `comparableCheckoutTotal`; known arithmetic totals for unmet/unknown/uncertain states remain inspectable but cannot support a cheapest claim.\n- `INCOMPLETE / UNAVAILABLE` retailer comparisons produce no fabricated checkout assessment, while known zero fees and M4.1 mixed-currency/fail-closed rules are preserved.\n- M4.2 public assessment/result records self-validate subtotal, eligibility, comparability, comparable-total and comparison identity relationships.\n- M4.2 architecture limits the new layer to accepted basket/comparison types plus finite `RetailerId`, with no provider/network/API/UI/ranking dependency.\n- M4.2 acceptance records final reviewed head `1d6dae470c04ab1d8279f891766fc16698286edb`, squash merge `69f9cb1afd1b16af938052bbca570cbd4ce52557`, issue #136 closure and 8/8 successful post-merge `main` workflows.",
    "changelog M4.2 added",
)
changelog = replace_once(
    changelog,
    "- The current deterministic target is **M4.2 One-retailer truthful total comparison**; arithmetic checkout-total knowledge must remain separate from retailer eligibility and no winner is selected in this slice.",
    "- M4.2 One-retailer truthful total comparison is **COMPLETE / ACCEPTED** after final reviewed head `1d6dae470c04ab1d8279f891766fc16698286edb`, squash merge `69f9cb1afd1b16af938052bbca570cbd4ce52557`, issue #136 closure and 8/8 successful post-merge `main` workflows.\n- The current deterministic target is **M4.3 Basket optimizer**; only explicit M4.2 `COMPARABLE` candidates may compete, and deterministic ordering/tie plus package/substitution/confidence/freshness policy must be defined before any cheapest/winner claim.",
    "changelog current target",
)
changelog_path.write_text(changelog)
