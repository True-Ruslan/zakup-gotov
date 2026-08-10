# X5 Mandatory Coverage Strategy

Updated: 2026-08-10
Status: M0 product constraint and integration strategy
Tracking: issue #47

## Product decision

Pyaterochka and Perekrestok are mandatory retailer coverage for Zakup Gotov.

A failed anonymous direct-HTTP probe does not remove either banner from product scope. M0 cannot reach GO unless both banners have at least one reproducible acceptable data path.

The goal is not necessarily a direct retailer API. The goal is reliable banner- and store-specific product observations with explicit provenance.

## Required observation contract

For each mandatory banner the accepted path must produce, at minimum:

- retailer/banner identity;
- explicit store or fulfillment context;
- stable product identity/SKU;
- current price and currency;
- availability or explicit `UNKNOWN`;
- observation timestamp;
- source/provenance identifying whether the observation came directly from X5, through an aggregator, or from a first-party user browser session;
- deterministic sanitized fixture replay for parser/contract tests.

No path may silently present aggregator data as a direct retailer observation.

## Track A — supported X5 partnership

Preferred long-term path: obtain supported access from X5/X5 Digital for store-scoped assortment, price, promotions and availability.

Primary-source evidence:

- X5 publicly invites digital partners and explicitly describes partnership around services/infrastructure for express delivery from X5 retail stores: https://www.x5.ru/ru/partners/
- X5 has a history of partner data products and automated data delivery through Dialog X5 / DataBridge: https://www.x5.ru/ru/news/h5-otkryvaet-dannye-s-pomoshhyu-data-bridge/
- X5 exposes partnership channels for its digital ecosystem and X5 Club; the latter publishes `partner@x5club.ru`: https://www.x5.ru/ru/partners/marketing/loyalty-programs/

### Partnership request

Ask X5 for a supported read-side integration intended for a consumer grocery-planning/comparison product. Required fields:

1. store/fulfillment resolution from coarse location;
2. banner and store ID;
3. searchable assortment;
4. stable SKU/product identifier;
5. current price and promotions;
6. availability/stock semantics;
7. freshness/observation timestamps;
8. rate limits and caching policy;
9. permission for cross-retailer comparison;
10. permission to retain sanitized fixtures for deterministic tests;
11. attribution/deep-link requirements;
12. sandbox/test credentials if available.

## Track B — aggregator-backed X5 coverage

This is the strongest independent fallback discovered so far.

Primary-source evidence:

- X5 states that Yandex Eats carried the full Pyaterochka and Perekrestok assortment and that prices, discounts and promotions matched the retail chains: https://www.x5.ru/ru/news/x5-zapustila-dostavku-cherez-yandeks-edu/
- Kuper's current consumer surface lists both `ПЯТЁРОЧКА` and `ПЕРЕКРЁСТОК` among supported retailers: https://kuper.ru/
- Kuper publishes an official API program that includes `Client apps API` and an integration contact: https://docs.kuper.ru/

This means X5 coverage should not depend only on direct `5d.5ka.ru` or `perekrestok.ru/api/customer/...` access.

### Acceptance for an aggregator path

An aggregator path is acceptable only if it preserves:

- source retailer/banner identity;
- underlying store/fulfillment context where available;
- product identity stable enough for matching;
- retailer-equivalent or explicitly aggregator-specific price semantics;
- explicit promotion semantics;
- availability/freshness semantics;
- clear provenance such as `provider=kuper`, `retailer=pyaterochka` rather than pretending the observation came directly from X5.

Issue #36 should explicitly determine whether Kuper Client apps API can expose Pyaterochka/Perekrestok store-scoped catalog observations to Zakup Gotov.

## Track C — user-assisted first-party browser bridge

Technical fallback when public anonymous APIs reject automated server-side access.

The connector runs in the user's own browser/profile against the official retailer surface. The user performs any login, store selection and CAPTCHA manually. After the first-party site has granted the user access, Zakup Gotov may extract only information already rendered or exposed to that browser context.

### Recommended architecture

1. Browser extension or local browser-side companion.
2. User opens the official Pyaterochka/Perekrestok page.
3. User manually resolves login/CAPTCHA/store selection when required.
4. Connector reads one of, in order of preference:
   - semantic DOM/product cards;
   - embedded structured page state/JSON already delivered to the page;
   - first-party responses already visible to the authenticated browser context, without exporting/replaying authorization material elsewhere.
5. Connector normalizes observations locally into the shared provider contract.
6. Only sanitized normalized observations are sent to the Zakup Gotov backend when needed.

### Security boundary

- CAPTCHA remains manual; no solver or bypass service.
- No browser-fingerprint spoofing or stealth automation.
- No proxy/IP rotation used to defeat provider blocking.
- No capture/export of another user's session.
- Cookies/tokens remain inside the user's browser profile and are never persisted by Zakup Gotov backend, logs, CI or fixtures.
- Do not synthesize or replay private `Auth`/device credentials outside the first-party browser context.
- Stop on explicit provider blocking rather than escalating evasion.
- Exact street addresses are not written into fixtures or logs.

### Why this path is materially different from CAPTCHA bypass

The user remains the actor who obtains legitimate first-party browser access. Zakup Gotov does not defeat the access-control challenge; it reads data the first-party site has already chosen to render to that user session.

## Previously tested direct paths

### Pyaterochka

The transparent JDK HTTP probe received `store-403` on the first coordinate-to-store request. That specific anonymous server-side path remains unsuitable.

This result now means only:

`DIRECT_ANONYMOUS_HTTP_UNSUITABLE`

It does **not** mean `PYATEROCHKA_UNSUPPORTED_PRODUCT_SCOPE`.

### Perekrestok

The transparent first-party-cookie probe also received `store-403` on the nearby-store API before store selection/search.

The third-party reference demonstrates that an ordinary browser session can obtain additional browser-context authorization, but Zakup Gotov will not export, forge or replay that authorization from a server-side scraper.

The next technical fallback is the user-assisted browser bridge, not browser-identity evasion.

## Execution order

1. Treat issue #47 as the umbrella mandatory-X5 requirement.
2. Expand Kuper issue #36 with explicit Pyaterochka/Perekrestok coverage questions.
3. Prepare an X5 digital-partnership request in parallel.
4. Implement a read-only user-assisted browser-bridge proof of concept for one Perekrestok product page first, because its consumer surface already exposes stable product/price state once a browser session is established.
5. Repeat the same browser-bridge contract for Pyaterochka.
6. Keep direct anonymous 403 probes as regression/evidence only; do not turn them into stealth automation.
7. M0 GO requires both banners to pass through at least one of Track A, B or C, plus one independent non-X5 provider path.

## M0 decision rule

Zakup Gotov must not enter M1 on the assumption that X5 can be omitted.

M0 GO requires:

- Pyaterochka usable through Track A, B or C;
- Perekrestok usable through Track A, B or C;
- at least one independent non-X5 provider usable through an acceptable path;
- deterministic fixture tests for all accepted paths;
- operational limitations and provenance visible in the product model.
