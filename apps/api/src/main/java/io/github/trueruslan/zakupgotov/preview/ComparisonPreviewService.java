package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.basket.SingleStoreBasketPlanner;
import io.github.trueruslan.zakupgotov.basket.SingleStoreBasketQuote;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonEvidence;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReadModelAssembler;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import io.github.trueruslan.zakupgotov.matching.MatchScope;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ComparisonPreviewService {

    private final RetailerRegistry retailerRegistry;
    private final ComparisonRuntimeEvidenceSource evidenceSource;
    private final SingleStoreBasketPlanner basketPlanner = new SingleStoreBasketPlanner();
    private final RetailerComparisonReadModelAssembler comparisonAssembler = new RetailerComparisonReadModelAssembler();

    public ComparisonPreviewService(
            RetailerRegistry retailerRegistry,
            ComparisonRuntimeEvidenceSource evidenceSource) {
        this.retailerRegistry = Objects.requireNonNull(retailerRegistry, "retailerRegistry must not be null");
        this.evidenceSource = Objects.requireNonNull(evidenceSource, "evidenceSource must not be null");
    }

    public ComparisonPreview create(ComparisonPreviewRequest request) {
        var input = ComparisonPreviewRequestFactory.create(request);
        var requestedRetailers = retailerRegistry.entries().stream()
                .filter(entry -> entry.isProductionReady())
                .map(entry -> entry.retailer().id())
                .collect(Collectors.toUnmodifiableSet());
        var runtimeEvidence = requestedRetailers.isEmpty()
                ? ComparisonRuntimeEvidence.empty()
                : Objects.requireNonNull(
                        evidenceSource.load(input.shoppingList(), input.productLocation(), requestedRetailers),
                        "runtime evidence must not be null");

        for (var evidence : runtimeEvidence.retailers()) {
            if (!requestedRetailers.contains(evidence.retailerId())) {
                throw new IllegalStateException(
                        "runtime evidence source returned unrequested retailer: "
                                + evidence.retailerId().canonicalId());
            }
        }

        var comparisonEvidence = new EnumMap<RetailerId, RetailerComparisonEvidence>(RetailerId.class);
        var quotes = new EnumMap<RetailerId, SingleStoreBasketQuote>(RetailerId.class);

        for (var evidence : runtimeEvidence.retailers()) {
            var registryEntry = retailerRegistry.require(evidence.retailerId());
            if (!registryEntry.isProductionReady()) {
                continue;
            }

            if (!evidence.providerOutcome().succeeded()) {
                comparisonEvidence.put(
                        evidence.retailerId(),
                        new RetailerComparisonEvidence(evidence.providerOutcome(), Optional.empty()));
                continue;
            }

            var contextId = evidence.fulfillmentContextId().orElseThrow(
                    () -> new IllegalArgumentException("successful runtime evidence requires fulfillment context"));
            var quote = basketPlanner.quote(
                    new MatchScope(evidence.retailerId(), contextId),
                    input.shoppingList(),
                    evidence.snapshots(),
                    evidence.packageQuantities());
            quotes.put(evidence.retailerId(), quote);
            comparisonEvidence.put(
                    evidence.retailerId(),
                    new RetailerComparisonEvidence(evidence.providerOutcome(), Optional.of(quote)));
        }

        var catalog = comparisonAssembler.assemble(retailerRegistry, comparisonEvidence);
        var requestedItems = input.shoppingList().items().stream()
                .map(item -> new ComparisonPreviewRequestedItem(
                        item.id().value(),
                        item.requirement().text(),
                        item.quantity()))
                .toList();
        var retailers = catalog.retailers().stream()
                .map(view -> projectRetailer(view, quotes.get(view.retailerId())))
                .toList();

        return new ComparisonPreview(input.productLocation().locality(), requestedItems, retailers);
    }

    private static ComparisonPreviewRetailer projectRetailer(
            RetailerComparisonView view,
            SingleStoreBasketQuote quote) {
        var items = view.comparisonStatus() == RetailerComparisonStatus.UNAVAILABLE || quote == null
                ? List.<ComparisonPreviewItem>of()
                : quote.items().stream().map(ComparisonPreviewService::projectItem).toList();
        return new ComparisonPreviewRetailer(
                view.retailerId().canonicalId(),
                view.displayName(),
                view.coverage(),
                view.productionAccess(),
                view.comparisonStatus(),
                view.reasons(),
                view.total(),
                view.freshness(),
                items);
    }

    private static ComparisonPreviewItem projectItem(io.github.trueruslan.zakupgotov.basket.BasketItemResolution resolution) {
        var candidateNames = new ArrayList<String>();
        for (var candidate : resolution.match().candidates()) {
            if (candidateNames.size() == 10) {
                break;
            }
            candidateNames.add(candidate.productName());
        }
        var selection = resolution.selection().map(ComparisonPreviewService::projectSelection);
        return new ComparisonPreviewItem(
                resolution.item().id().value(),
                resolution.item().requirement().text(),
                resolution.item().quantity(),
                resolution.status(),
                candidateNames,
                selection);
    }

    private static ComparisonPreviewSelection projectSelection(
            io.github.trueruslan.zakupgotov.basket.PackageSelection selection) {
        OfferSnapshot snapshot = selection.snapshot();
        return new ComparisonPreviewSelection(
                snapshot.productName(),
                selection.packageQuantity(),
                selection.packageCount(),
                selection.providedQuantity(),
                selection.lineTotal(),
                snapshot.currencyCode());
    }
}
