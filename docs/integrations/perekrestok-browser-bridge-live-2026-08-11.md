# Perekrestok Browser Bridge — Live PASS 2026-08-11

Status: `AVAILABLE_BROWSER_BRIDGE`
Tracking: issue #52 / umbrella #47 / PR #53

## Scope

This record captures the repeated real first-party Perekrestok browser gate after adapter v2 was merged to `main`.

Only sanitized normalized evidence is recorded. No cookies, authorization material, request headers, response bodies, arbitrary browser-storage values, exact address, CAPTCHA data, or raw production HTML are included.

## Environment

- browser family: Chromium-compatible normal user browser profile (Yandex Browser);
- retailer origin: official `www.perekrestok.ru`;
- page type: category/catalog page;
- bridge: Zakup Gotov Retailer Bridge;
- adapter version: `2`.

## Live result

The updated extension was loaded/reloaded in the normal first-party browser profile and the catalog page was fully reloaded after store selection.

Observed bridge diagnostics:

- `data-zg-bridge-status`: `ok`;
- `data-zg-bridge-count`: `90`;
- result: **PASS**.

A sanitized validation over `zg.latestObservations` then reported:

- observation count: `90`;
- adapter versions: exactly `2`;
- fulfillment contexts: exactly one context, `656`;
- invalid normalized observations under the acceptance predicate: `0`.

The acceptance predicate required every observation to have:

- adapter version `2`;
- nonblank fulfillment context;
- nonblank SKU;
- integer `priceMinor >= 0`;
- currency `RUB`;
- availability in `AVAILABLE`, `UNAVAILABLE`, `UNKNOWN`;
- canonical `sourceReference` without query string or fragment.

Three sanitized sample observations all satisfied the contract. Their prices were represented as integer minor units and their availability was `UNKNOWN`, matching the documented DOM semantics where catalog presence alone is not treated as stock proof.

## Acceptance decision

The repeated real-browser v2 gate satisfies the Phase A acceptance criteria:

- bridge status `ok`: **PASS**;
- observation count greater than zero: **PASS** (`90`);
- adapter version `2`: **PASS**;
- exactly one fulfillment context: **PASS** (`656`);
- SKU present: **PASS**;
- integer non-negative price: **PASS**;
- currency `RUB`: **PASS**;
- availability explicit/`UNKNOWN`: **PASS**;
- canonical source reference with no query/hash: **PASS**;
- normalized validation failures: **0**;
- credential/session export observed: **false**;
- raw production HTML persisted: **false**.

## Decision

Perekrestok Browser Bridge Phase A is **LIVE PASS**.

Perekrestok connectivity advances from `BROWSER_BRIDGE_LIVE_PENDING` to **`AVAILABLE_BROWSER_BRIDGE`** for page-snapshot acquisition through the user-assisted first-party browser transport.

This status means Zakup Gotov has one reproducible accepted Perekrestok acquisition path. It does **not** imply a supported direct retailer API, server-side scraping capability, or persistent-session lifecycle support.

Issue #54 remains the explicit non-blocking follow-up before the bridge is treated as a long-lived session transport across same-document store changes or SPA navigation after the first successful snapshot.

Next M0 work is to reuse the proven browser-bridge transport contract for Pyaterochka and continue proving at least one independent non-X5 retailer path.
