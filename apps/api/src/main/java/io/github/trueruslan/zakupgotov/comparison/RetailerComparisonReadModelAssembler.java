package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.basket.BasketItemResolutionStatus;
import io.github.trueruslan.zakupgotov.basket.BasketQuoteStatus;
import io.github.trueruslan.zakupgotov.provider.FreshnessEvidence;
import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RetailerComparisonReadModelAssembler {

    public RetailerComparisonCatalog assemble(
            RetailerRegistry registry,
            Map<RetailerId, RetailerComparisonEvidence> evidenceByRetailer) {
        Objects.requireNonNull(registry, "registry must not be null");
        return assembleEntries(registry.entries(), evidenceByRetailer);
    }

    RetailerComparisonCatalog assembleEntries(
            List<RetailerRegistryEntry> entries,
            Map<RetailerId, RetailerComparisonEvidence> evidenceByRetailer) {
        Objects.requireNonNull(entries, "entries must not be null");
        Objects.requireNonNull(evidenceByRetailer, "evidenceByRetailer must not be null");
        validateEvidenceKeys(evidenceByRetailer);

        var views = new ArrayList<RetailerComparisonView>();
        for (var entry : entries) {
            Objects.requireNonNull(entry, "retailer entry must not be null");
            views.add(assembleEntry(entry, evidenceByRetailer.get(entry.retailer().id())));
        }
        return new RetailerComparisonCatalog(views);
    }

    private static void validateEvidenceKeys(Map<RetailerId, RetailerComparisonEvidence> evidenceByRetailer) {
        for (var entry : evidenceByRetailer.entrySet()) {
            var retailerId = Objects.requireNonNull(entry.getKey(), "evidence retailer id must not be null");
            var evidence = Objects.requireNonNull(entry.getValue(), "retailer comparison evidence must not be null");
            if (evidence.providerOutcome().retailerId() != retailerId) {
                throw new IllegalArgumentException("evidence retailer must match provider outcome retailer");
            }
        }
    }

    private static RetailerComparisonView assembleEntry(
            RetailerRegistryEntry entry,
            RetailerComparisonEvidence evidence) {
        var coverage = coverage(entry.coverageState());
        var productionAccess = productionAccess(entry.productionAccessStatus());

        if (coverage != RetailerCoverageStatus.CONNECTED) {
            return unavailable(
                    entry,
                    coverage,
                    productionAccess,
                    switch (coverage) {
                        case DISCOVERY -> RetailerComparisonReason.COVERAGE_DISCOVERY;
                        case DEGRADED -> RetailerComparisonReason.COVERAGE_DEGRADED;
                        case BLOCKED -> RetailerComparisonReason.COVERAGE_BLOCKED;
                        case CONNECTED -> throw new IllegalStateException("connected coverage handled separately");
                    });
        }
        if (productionAccess != RetailerProductionAccessStatus.READY) {
            return unavailable(
                    entry,
                    coverage,
                    productionAccess,
                    productionAccess == RetailerProductionAccessStatus.BLOCKED
                            ? RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED
                            : RetailerComparisonReason.PRODUCTION_ACCESS_PENDING);
        }
        if (evidence == null) {
            return unavailable(
                    entry,
                    coverage,
                    productionAccess,
                    RetailerComparisonReason.DATA_NOT_AVAILABLE);
        }
        if (!evidence.providerOutcome().succeeded()) {
            return unavailable(
                    entry,
                    coverage,
                    productionAccess,
                    RetailerComparisonReason.SOURCE_UNAVAILABLE);
        }

        var quote = evidence.basketQuote().orElseThrow();
        return switch (quote.status()) {
            case COMPLETE -> new RetailerComparisonView(
                    entry.retailer().id(),
                    displayName(entry.retailer().id()),
                    coverage,
                    productionAccess,
                    RetailerComparisonStatus.READY,
                    List.of(),
                    quote.total(),
                    Optional.of(freshness(quote.items())));
            case UNCERTAIN -> new RetailerComparisonView(
                    entry.retailer().id(),
                    displayName(entry.retailer().id()),
                    coverage,
                    productionAccess,
                    RetailerComparisonStatus.UNCERTAIN,
                    List.of(RetailerComparisonReason.AVAILABILITY_UNKNOWN),
                    quote.total(),
                    Optional.of(freshness(quote.items())));
            case INCOMPLETE -> new RetailerComparisonView(
                    entry.retailer().id(),
                    displayName(entry.retailer().id()),
                    coverage,
                    productionAccess,
                    RetailerComparisonStatus.INCOMPLETE,
                    incompleteReasons(quote.items()),
                    Optional.empty(),
                    Optional.empty());
        };
    }

    private static RetailerComparisonView unavailable(
            RetailerRegistryEntry entry,
            RetailerCoverageStatus coverage,
            RetailerProductionAccessStatus productionAccess,
            RetailerComparisonReason reason) {
        return new RetailerComparisonView(
                entry.retailer().id(),
                displayName(entry.retailer().id()),
                coverage,
                productionAccess,
                RetailerComparisonStatus.UNAVAILABLE,
                List.of(reason),
                Optional.empty(),
                Optional.empty());
    }

    private static List<RetailerComparisonReason> incompleteReasons(
            List<io.github.trueruslan.zakupgotov.basket.BasketItemResolution> items) {
        var reasons = new LinkedHashSet<RetailerComparisonReason>();
        for (var item : items) {
            var reason = reason(item.status());
            if (reason != null) {
                reasons.add(reason);
            }
        }
        return List.copyOf(reasons);
    }

    private static RetailerComparisonReason reason(BasketItemResolutionStatus status) {
        return switch (status) {
            case FULFILLED -> null;
            case AVAILABILITY_UNKNOWN -> RetailerComparisonReason.AVAILABILITY_UNKNOWN;
            case UNMATCHED -> RetailerComparisonReason.ITEM_UNMATCHED;
            case AMBIGUOUS -> RetailerComparisonReason.ITEM_AMBIGUOUS;
            case UNAVAILABLE -> RetailerComparisonReason.ITEM_UNAVAILABLE;
            case PACKAGE_QUANTITY_UNKNOWN -> RetailerComparisonReason.PACKAGE_QUANTITY_UNKNOWN;
            case QUANTITY_UNIT_MISMATCH -> RetailerComparisonReason.QUANTITY_UNIT_MISMATCH;
        };
    }

    private static RetailerFreshness freshness(
            List<io.github.trueruslan.zakupgotov.basket.BasketItemResolution> items) {
        Instant oldestObservedAt = null;
        Instant oldestProviderUpdatedAt = null;
        var allProviderTimestamped = true;

        for (var item : items) {
            var snapshot = item.selection().orElseThrow().snapshot();
            FreshnessEvidence evidence = snapshot.freshness();
            oldestObservedAt = oldest(oldestObservedAt, evidence.observedAt());

            var providerUpdatedAt = evidence.providerUpdatedAt();
            if (providerUpdatedAt.isEmpty()) {
                allProviderTimestamped = false;
            } else {
                oldestProviderUpdatedAt = oldest(oldestProviderUpdatedAt, providerUpdatedAt.orElseThrow());
            }
        }

        var observedAt = Objects.requireNonNull(oldestObservedAt, "selected basket freshness must have observation time");
        if (allProviderTimestamped) {
            return new RetailerFreshness(
                    RetailerFreshnessBasis.PROVIDER_TIMESTAMP,
                    observedAt,
                    Optional.of(Objects.requireNonNull(
                            oldestProviderUpdatedAt,
                            "provider timestamp freshness must have provider update time")));
        }
        return new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                observedAt,
                Optional.empty());
    }

    private static Instant oldest(Instant current, Instant candidate) {
        return current == null || candidate.isBefore(current) ? candidate : current;
    }

    private static RetailerCoverageStatus coverage(RetailerCoverageState state) {
        return switch (Objects.requireNonNull(state, "coverage state must not be null")) {
            case AVAILABLE_DIRECT, AVAILABLE_AGGREGATOR, AVAILABLE_PUBLIC_WEB, AVAILABLE_BROWSER_BRIDGE ->
                    RetailerCoverageStatus.CONNECTED;
            case REQUIRED_UNIMPLEMENTED, DISCOVERY -> RetailerCoverageStatus.DISCOVERY;
            case DEGRADED -> RetailerCoverageStatus.DEGRADED;
            case BLOCKED_EXTERNAL -> RetailerCoverageStatus.BLOCKED;
        };
    }

    private static RetailerProductionAccessStatus productionAccess(ProductionAccessStatus status) {
        return switch (Objects.requireNonNull(status, "production access status must not be null")) {
            case ACCEPTABLE -> RetailerProductionAccessStatus.READY;
            case NOT_ASSESSED, UNRESOLVED -> RetailerProductionAccessStatus.PENDING;
            case BLOCKED -> RetailerProductionAccessStatus.BLOCKED;
        };
    }

    private static String displayName(RetailerId retailerId) {
        return switch (Objects.requireNonNull(retailerId, "retailerId must not be null")) {
            case PYATEROCHKA -> "Пятёрочка";
            case PEREKRESTOK -> "Перекрёсток";
            case CHIZHIK -> "Чижик";
            case MAGNIT -> "Магнит";
            case LENTA -> "Лента";
            case VKUSVILL -> "ВкусВилл";
            case OZON_FRESH -> "Ozon Fresh";
            case SAMOKAT -> "Самокат";
        };
    }
}
