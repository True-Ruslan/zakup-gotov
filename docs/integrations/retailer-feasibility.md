# Retailer Feasibility Matrix

Updated: 2026-08-12  
Status: **M0 technical feasibility complete; M1 may start**

## Decision vocabulary

Use explicit evidence labels rather than “seems supported” language:

- `AVAILABLE_BROWSER_BRIDGE` — accepted user-assisted first-party browser path;
- `AVAILABLE_PUBLIC_WEB` — accepted ordinary public-web technical path for its documented context boundary;
- `LIVE_PENDING` — deterministic implementation exists but real acceptance gate is outstanding;
- `SPIKE` — feasibility work is in progress;
- `RESEARCH` — source/path research only;
- `UNSUITABLE_*` — investigated path failed its acceptance gate;
- `BLOCKED` — authoritative access/usage constraint prevents the intended path;
- `UNRESOLVED` — a required production/legal/product decision is not yet established.

An accepted technical M0 path is not automatically production-cleared. Usage rights, location resolution and operating constraints remain explicit dimensions.

## Current registry snapshot

| Retailer/banner | Best current path | M0 technical status | Usage-rights / production status | Key limitations / next work |
|---|---|---|---|---|
| **Pyaterochka / 5ka** | User-assisted first-party browser bridge | **`AVAILABLE_BROWSER_BRIDGE`** | First-party user-assisted transport; supported/partner paths still worth pursuing | Reload-based page snapshot; direct anonymous HTTP unsuitable; broader corpus/context hardening continues |
| **Perekrestok** | User-assisted first-party browser bridge | **`AVAILABLE_BROWSER_BRIDGE`** | First-party user-assisted transport; supported/partner paths still worth pursuing | Reload-based page snapshot; direct server path unsuitable; #54 for SPA/store-change lifecycle |
| **Magnit grocery** | Ordinary public product pages with explicit public `shopCode` | **`AVAILABLE_PUBLIC_WEB` for explicit-store-context M0 feasibility** | **`UNRESOLVED` for recurring production reuse — #70** | 20/20 fixed corpus in both contexts; #69 for location→`shopCode`; regular/old price only when explicitly present; availability may be `UNKNOWN` |
| **Kuper** | Supported aggregator/provider path | `RESEARCH / SPIKE` | Must preserve Kuper provider provenance separately from retailer identity | #36; useful for broader coverage even though M0 second-mode gate is already closed |
| **Chizhik** | To be selected | `RESEARCH` | Not production-cleared | Mandatory registry coverage work |
| **Ozon Fresh** | To be selected | `RESEARCH` | Not production-cleared | Mandatory registry coverage work |
| **Samokat** | To be selected | `RESEARCH` | Not production-cleared | Mandatory registry coverage work |
| **Lenta** | To be selected | `RESEARCH` | Not production-cleared | Mandatory registry coverage work |
| **VkusVill** | To be selected | `RESEARCH` | Not production-cleared | Mandatory registry coverage work |

Universal retailer connectivity remains the invariant: a difficult or unavailable path is an explicit coverage state, not a reason to remove the retailer from the product registry.

## Accepted evidence: Pyaterochka

Direct ordinary server-side access failed closed and remains unsuitable.

The accepted browser bridge uses:

- official `5ka.ru` pages in the user's first-party browser profile;
- exact `pyaterochka` / `pyaterochka-browser` provenance;
- canonical official store-context resource evidence;
- sanitized normalized observations;
- `UNKNOWN` availability unless stock semantics are actually supported;
- no exported/replayed browser credentials.

Real gate on 2026-08-11:

- 12 normalized observations;
- exactly one fulfillment context;
- adapter v1;
- zero validation failures.

Evidence:

- [`pyaterochka-browser-bridge-phase-a.md`](pyaterochka-browser-bridge-phase-a.md);
- [`pyaterochka-browser-bridge-live-2026-08-11.md`](pyaterochka-browser-bridge-live-2026-08-11.md).

## Accepted evidence: Perekrestok

Direct ordinary server-side access failed closed and remains unsuitable.

The accepted browser adapter v2 uses the current semantic product-card DOM plus same-origin shop-resource context evidence while keeping browser credentials inside the first-party profile.

Repeated real gate on 2026-08-11:

- 90 normalized observations;
- exactly one fulfillment context;
- adapter version `2`;
- zero acceptance-validation failures;
- canonical source references without query/hash.

Evidence:

- [`perekrestok-browser-bridge-phase-a.md`](perekrestok-browser-bridge-phase-a.md);
- [`perekrestok-browser-bridge-live-2026-08-11.md`](perekrestok-browser-bridge-live-2026-08-11.md).

Issue #54 remains lifecycle hardening rather than a Phase A connectivity blocker.

## Accepted technical evidence: Magnit public web

### Request boundary

The Magnit feasibility probe uses ordinary public product pages and JDK `HttpClient` only:

- explicit public `shopCode` context;
- fixed timeouts;
- transparent project User-Agent;
- no Cookie/login;
- no Authorization or partner API key;
- no browser automation;
- no CAPTCHA handling;
- no proxy/IP rotation or fingerprint evasion;
- no retry/evasion loop.

Ordinary CI never depends on live Magnit traffic.

### Phase A

Both explicit contexts returned HTTP 2xx with expected SKU and RUB-price evidence for the fixed Phase A product.

Evidence: [`magnit-public-page-live-2026-08-12.md`](magnit-public-page-live-2026-08-12.md).

### Phase B

The fixed 20-requirement corpus was exercised against both explicit contexts.

Final merged-main run `31544035409` proved:

- 20/20 HTTP 2xx in each context;
- 20/20 expected-SKU/current-price usable observations in each context;
- stable identity 20/20;
- zero failed requirements;
- price-bound promo status on all 40 final observations;
- known explicit availability on 6/40 observations, `UNKNOWN` otherwise;
- no regular/old price invented when a second supported price was absent.

Evidence:

- [`magnit-phase-b.md`](magnit-phase-b.md);
- [`magnit-public-page-phase-b-live-2026-08-12.md`](magnit-public-page-phase-b-live-2026-08-12.md).

### Magnit decision

Technical status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context M0 feasibility**.

Production constraints:

- #69 — automatic user location/address → public `shopCode` selection is not yet proven;
- #70 — catalog usage rights for recurring production automated acquisition remain `UNRESOLVED`.

Until #70 is `ACCEPTABLE`, M1 must use Magnit deterministic fixtures/research evidence rather than silently enabling recurring production polling.

## M0 scorecard outcome

| Criterion | Pyaterochka | Perekrestok | Magnit |
|---|---|---|---|
| Reproducible fulfillment/store context | PASS | PASS | PASS for explicit `shopCode` |
| Usable product identity | PASS | PASS | PASS 20/20 |
| Current price | PASS | PASS | PASS 20/20 × 2 contexts |
| Availability semantics | Explicit / `UNKNOWN` | Explicit / `UNKNOWN` | Explicit where present / `UNKNOWN` otherwise |
| Promo semantics | Not a Phase A blocker | Not a Phase A blocker | PASS through price-bound marker |
| Sanitized deterministic verification | PASS | PASS | PASS |
| Acquisition mode | Browser bridge | Browser bridge | Public web |
| Production limitations explicit | PASS | PASS | PASS — #69/#70 |

Result: the project has an accepted independent non-X5 technical path and two acquisition modes. **M0 technical discovery is complete.**

## Remaining retailer research rules

Future retailer work must follow the same discipline:

1. start with supported/partner/public first-party surfaces;
2. distinguish retailer identity from provider/aggregator provenance;
3. treat direct API failure as a transport result, not a retailer-scope decision;
4. keep CAPTCHA bypass, stealth/fingerprint evasion and proxy rotation used to circumvent access controls out of scope;
5. keep ordinary CI offline/deterministic;
6. preserve sanitized fixtures and explicit live gates;
7. record usage-rights state separately from technical accessibility;
8. never infer stock, freshness, regular price or location scope beyond evidence.

## M1 implication

M1 Shopping Core may now start over deterministic provider observations and explicit retailer coverage states.

The accepted M0 paths provide architecture and fixture evidence, not permission to hide unresolved live-production constraints. See [`../superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](../superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).
