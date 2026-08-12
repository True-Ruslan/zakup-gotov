# Magnit production access / right-to-operate decision — 2026-08-13

Status: **BLOCKED by Zakup Gotov product policy pending affirmative permission or licensed/supported access terms**.

This document is an engineering/product right-to-operate decision, not a legal opinion and not a claim that Magnit expressly prohibits every automated HTTP request.

## Decision

Zakup Gotov must **not enable recurring production acquisition or commercial catalog reuse from the Magnit public-web path under the current evidence**.

Technical status remains independently accepted:

- retailer coverage: `AVAILABLE_PUBLIC_WEB`;
- explicit public product/shop observations: technically reproducible;
- structured package evidence: accepted for the finite corpus;
- bbox → public `shopCode` location resolution: accepted;
- production access: `BLOCKED`.

`BLOCKED` means the product cannot treat this path as production-ready until a new source-backed review establishes affirmative permission for the intended use.

## Intended production use being assessed

The target use is materially broader than a one-off consumer page view:

- repeated automated observation across multiple products and stores;
- price/promotion/availability evidence used to compare retailer baskets;
- commercial/product reuse in a service available to users;
- retention of provenance and time-bound offer evidence sufficient to explain comparisons;
- recurring refresh rather than a single manual research request.

The decision is therefore based on the intended operating model, not on whether one anonymous public request can technically return HTTP 200.

## Authoritative and high-confidence sources reviewed

### 1. Magnit user agreement

Official source: https://magnit.ru/user_agreement

Current page reviewed on 2026-08-13 identifies itself as the user agreement for the service for ordering goods from Magnit stores.

Relevant signals:

- the agreement defines an Order as acquisition for personal/family/home or other use not connected with entrepreneurial activity;
- the User is a person using the service to place an Order;
- the service publishes store, price and availability information for the consumer ordering flow;
- the agreement does **not** provide Zakup Gotov with an API/feed/catalog-reuse license or other affirmative permission for recurring commercial extraction and republication;
- the service rules allow restrictions for misuse scenarios and mention software/technical means that distort service operation.

Interpretation used by this project:

- the agreement is useful evidence about the intended consumer purpose of the service;
- it is **not** interpreted as a blanket statement that every automated public request is forbidden;
- critically, it does not supply the affirmative permission needed by #70 for our recurring production reuse model.

### 2. Russian database-rights framework

High-confidence current legal reference:

- Civil Code of the Russian Federation, Article 1334: https://www.consultant.ru/document/cons_doc_LAW_64629/c8b26358cbae2a98f328bd8cb495a08f7e11caff/
- Civil Code of the Russian Federation, Article 1335.1: https://www.consultant.ru/document/cons_doc_LAW_64629/6a3a364978a1d1c94fdbf7cbf6d14b5c4bb528a1/

Relevant signals from the current text:

- a qualifying database producer has the exclusive right to extract database materials and reuse them, subject to statutory exceptions;
- a lawful user may in some circumstances extract/use an insubstantial part for other purposes;
- public-facing reuse of extracted materials carries a source-identification requirement in the cited exception;
- repeated extraction/use of insubstantial parts is not permitted under that exception when it conflicts with normal database use or unreasonably harms the producer's legitimate interests.

Interpretation used by this project:

- whether a specific Magnit dataset, extraction volume or concrete product implementation satisfies every legal element is a legal question that this engineering review does not decide;
- the target Zakup Gotov operating model is recurring and catalog-oriented, so the project cannot safely assume that an exception for an insubstantial part automatically authorizes the intended production system;
- this increases the need for affirmative permission/licensed access before production activation rather than reducing it.

## Sources that are insufficient for ACCEPTABLE

The following are deliberately **not** treated as permission:

- HTTP 200 from a public endpoint;
- absence of authentication;
- successful stateless live probes;
- search-engine indexing or an unverified robots summary;
- absence of an explicit sentence saying “scraping is prohibited”;
- technical ability to stay below an arbitrary request rate;
- the fact that price/store information is visible to consumers.

No authoritative first-party robots content was captured reliably enough to affect the decision. In any case robots directives would not by themselves grant a data-reuse license.

## Required answers from #70

### Commercial use

**Not cleared.** The reviewed Magnit agreement is consumer-ordering oriented and provides no affirmative commercial data-reuse permission for Zakup Gotov.

### Storage vs transient observation

**Recurring production storage/reuse is not cleared.**

The project may retain sanitized deterministic fixtures and narrowly scoped research/acceptance evidence needed to prove engineering behavior. That project-policy allowance is not a declaration of legal entitlement to build a production historical catalog.

### Rate limits / robots / technical restrictions

Because production access is blocked, there is no production request-rate budget to define yet.

Until re-review:

- no scheduled/recurring Magnit acquisition;
- no anti-bot bypass or evasion;
- no borrowed/authenticated consumer session for catalog collection;
- no hidden geocoder;
- ordinary CI remains live-free;
- any future evidence probe must stay explicit, finite, sanitized and separately justified.

If affirmative permission is later obtained, rate/robots/caching constraints must be documented from that permission or supported access contract before changing `ProductionAccessStatus`.

### Attribution / linking

No production attribution implementation is claimed sufficient today because production reuse itself is not cleared.

If future permission relies on a statutory or licensed mode requiring attribution, the product must preserve and expose the required Magnit/source link and provenance before activation.

## Why `BLOCKED`, not `UNRESOLVED`

The research question is now sufficiently answered for product operation even though no explicit universal prohibition was found:

1. #70's safety rule says absence of prohibition is not affirmative permission.
2. The intended production use requires a positive right-to-operate decision, not mere technical accessibility.
3. Current first-party terms do not provide that permission.
4. Current database-rights rules create material uncertainty for recurring catalog extraction/reuse and do not provide an obvious blanket safe harbor for the intended model.
5. Waiting in `UNRESOLVED` would obscure the operational result: **production must stay off**.

Therefore `BLOCKED` is used as a **product-policy state**: the path is technically connected but not production-ready under current evidence.

This does not state that Magnit has expressly forbidden all automated access or that a particular use has been adjudicated unlawful.

## Reopen / unblock criteria

Magnit may move from `BLOCKED` to `ACCEPTABLE` only after a new review obtains an affirmative basis covering the intended operating model, for example:

- written permission from Magnit/АО «Тандер»;
- supported partner/API/feed agreement with reuse rights;
- published first-party terms explicitly permitting the intended automated acquisition and reuse;
- another authoritative legal/licensing basis reviewed for the actual production scope.

That review must also document:

- allowed product/store scope;
- commercial use;
- storage/retention;
- refresh/rate limits;
- attribution/linking requirements;
- redistribution/display constraints;
- termination/revocation handling.

## Runtime consequence

The existing retailer registry and comparison boundary already model the desired state:

- technical coverage remains connected;
- `ProductionAccessStatus.BLOCKED` maps to product-facing production access `BLOCKED`;
- comparison status is `UNAVAILABLE` with `PRODUCTION_ACCESS_BLOCKED`;
- no total/freshness/live evidence is exposed as if the retailer were production-ready.

No Magnit production HTTP client should be introduced by this decision.

## Future contact

A direct permission request to Magnit may be a useful follow-up, but sending external correspondence is intentionally outside this change. If pursued, its response becomes new #70-style evidence and may trigger a re-review.
