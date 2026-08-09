# Observability

Zakup Gotov uses vendor-neutral operational signals. Domain and integration code must not call a telemetry vendor SDK directly; instrumentation is expressed through Spring/Micrometer/OpenTelemetry-compatible abstractions so exporters can change without changing business behavior.

## HTTP actuator surface

The production HTTP management surface is intentionally small:

- `/actuator/health` — overall health;
- `/actuator/health/liveness` — process liveness;
- `/actuator/health/readiness` — readiness to serve requests;
- `/actuator/info` — non-sensitive application information.

The following are intentionally **not** exposed over HTTP:

- `/actuator/env`;
- `/actuator/configprops`;
- `/actuator/metrics`;
- any endpoint that can reveal configuration values, environment variables, credentials, request payloads, or detailed internal state without an explicit future security review.

Health responses do not expose component details by default.

## Logging safety

Request-detail logging is explicitly disabled by default.

External retailer/provider payloads may contain credentials, identifiers, store/location context, or user address information. Provider request/response bodies, authorization headers, cookies, precise addresses, and tokens must not be logged by default.

If a future integration requires diagnostic payload logging, it must introduce an explicit redaction contract and automated tests before any such logging is enabled.

## Reserved metric vocabulary

The following names are reserved for M0B/M1 instrumentation:

- `zakup.provider.request.duration` — provider request latency;
- `zakup.provider.request.errors` — provider request failures by normalized reason;
- `zakup.provider.offer.age` — age of the offer/availability observation used by the product;
- `zakup.matching.confidence` — normalized product-match confidence;
- `zakup.basket.completeness` — share of shopping requirements fulfilled by a candidate basket;
- `zakup.basket.compute.duration` — basket-computation latency.

Metric tags must remain low-cardinality. Never use raw addresses, product titles, URLs, exception messages, tokens, arbitrary user input, or external request IDs as metric labels.

## Trace vocabulary

Future provider spans should use stable internal operation names such as:

- `provider.resolve_context`;
- `provider.search_products`;
- `provider.fetch_offers`;
- `basket.compute`;
- `matching.rank`.

Provider/retailer identity may be represented by a bounded normalized identifier. Precise user location, credentials, and raw provider payloads must not be span attributes.

## Health semantics

- **Liveness** answers whether the process should be restarted. It must not depend on transient retailer availability.
- **Readiness** answers whether this instance can serve the product API. Core infrastructure such as the primary database may affect readiness; individual retailer degradation should normally be surfaced as provider-level partial failure rather than making the entire application unready.

This distinction is important for M0B: volatile external grocery systems are expected dependencies, not a reason to restart the application.

## Testing requirements

`ActuatorSecurityTest` is the executable guard for the public management surface. Any new HTTP-exposed actuator endpoint requires an explicit test and security/privacy rationale.

Provider metrics/traces added in M0B must have tests for naming, bounded labels, and redaction where practical. Observability must help diagnose external failures without becoming a path for leaking user or provider-sensitive data.
