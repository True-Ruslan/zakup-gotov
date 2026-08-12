package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderPathSelection;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Test-only deterministic evidence for API/browser acceptance. Performs no network I/O. */
final class DeterministicComparisonRuntimeEvidenceSource implements ComparisonRuntimeEvidenceSource {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T10:00:00Z");

    @Override
    public ComparisonRuntimeEvidence load(
            ShoppingList shoppingList,
            ProductLocation productLocation,
            Set<RetailerId> requestedRetailers) {
        Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        Objects.requireNonNull(productLocation, "productLocation must not be null");
        Objects.requireNonNull(requestedRetailers, "requestedRetailers must not be null");

        var allEvidence = List.of(
                completeEvidence(RetailerId.PYATEROCHKA, AvailabilityStatus.AVAILABLE),
                completeEvidence(RetailerId.PEREKRESTOK, AvailabilityStatus.UNKNOWN),
                packageUnknownEvidence(RetailerId.MAGNIT),
                unmatchedEvidence(RetailerId.LENTA),
                ambiguousEvidence(RetailerId.VKUSVILL),
                unitMismatchEvidence(RetailerId.OZON_FRESH),
                unavailableEvidence(RetailerId.SAMOKAT));
        return ComparisonRuntimeEvidence.of(allEvidence.stream()
                .filter(evidence -> requestedRetailers.contains(evidence.retailerId()))
                .toList());
    }

    private static RetailerRuntimeEvidence completeEvidence(
            RetailerId retailerId,
            AvailabilityStatus milkAvailability) {
        var context = context(retailerId);
        return successfulEvidence(
                retailerId,
                context,
                List.of(
                        offer(retailerId, context, "milk", "Молоко", "100.00", milkAvailability),
                        offer(retailerId, context, "eggs", "Яйца", "120.00", AvailabilityStatus.AVAILABLE)),
                List.of(
                        new Quantity(BigDecimal.ONE, QuantityUnit.LITER),
                        new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence packageUnknownEvidence(RetailerId retailerId) {
        var context = context(retailerId);
        return successfulEvidence(
                retailerId,
                context,
                List.of(
                        offer(retailerId, context, "milk", "Молоко", "90.00", AvailabilityStatus.AVAILABLE),
                        offer(retailerId, context, "eggs", "Яйца", "110.00", AvailabilityStatus.AVAILABLE)),
                Arrays.asList(null, new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence unmatchedEvidence(RetailerId retailerId) {
        var context = context(retailerId);
        return successfulEvidence(
                retailerId,
                context,
                List.of(offer(retailerId, context, "bread", "Хлеб", "70.00", AvailabilityStatus.AVAILABLE)),
                List.of(new Quantity(BigDecimal.ONE, QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence ambiguousEvidence(RetailerId retailerId) {
        var context = context(retailerId);
        return successfulEvidence(
                retailerId,
                context,
                List.of(
                        offer(retailerId, context, "milk-a", "Молоко", "100.00", AvailabilityStatus.AVAILABLE),
                        offer(retailerId, context, "milk-b", "Молоко", "105.00", AvailabilityStatus.AVAILABLE),
                        offer(retailerId, context, "eggs", "Яйца", "120.00", AvailabilityStatus.AVAILABLE)),
                List.of(
                        new Quantity(BigDecimal.ONE, QuantityUnit.LITER),
                        new Quantity(BigDecimal.ONE, QuantityUnit.LITER),
                        new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence unitMismatchEvidence(RetailerId retailerId) {
        var context = context(retailerId);
        return successfulEvidence(
                retailerId,
                context,
                List.of(
                        offer(retailerId, context, "milk", "Молоко", "100.00", AvailabilityStatus.AVAILABLE),
                        offer(retailerId, context, "eggs", "Яйца", "120.00", AvailabilityStatus.AVAILABLE)),
                List.of(
                        new Quantity(new BigDecimal("500"), QuantityUnit.GRAM),
                        new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence unavailableEvidence(RetailerId retailerId) {
        return new RetailerRuntimeEvidence(
                retailerId,
                new ProviderSearchOutcome(retailerId, Optional.empty(), List.of(), List.of()),
                List.of());
    }

    private static RetailerRuntimeEvidence successfulEvidence(
            RetailerId retailerId,
            String fulfillmentContext,
            List<ObservedOffer> offers,
            List<Quantity> packageQuantities) {
        var normalizedOffers = new ArrayList<ObservedOffer>();
        var snapshots = new ArrayList<OfferSnapshot>();
        for (var index = 0; index < offers.size(); index++) {
            var offer = withPackageQuantity(offers.get(index), packageQuantities.get(index));
            normalizedOffers.add(offer);
            snapshots.add(OfferSnapshot.observationOnly(
                    new OfferSnapshotId(UUID.nameUUIDFromBytes(
                            (retailerId.name() + "-acceptance-" + index).getBytes(StandardCharsets.UTF_8))),
                    offer));
        }
        var outcome = new ProviderSearchOutcome(
                retailerId,
                Optional.of(new ProviderPathSelection(
                        "fixture-" + retailerId.canonicalId(),
                        AcquisitionMode.DIRECT_API)),
                normalizedOffers,
                List.of());
        return RetailerRuntimeEvidence.withFulfillmentContext(
                retailerId,
                fulfillmentContext,
                outcome,
                snapshots);
    }

    private static ObservedOffer withPackageQuantity(ObservedOffer offer, Quantity packageQuantity) {
        if (packageQuantity == null) {
            return offer;
        }
        return new ObservedOffer(
                offer.retailerId(),
                offer.sourceProviderId(),
                offer.sourceMode(),
                offer.fulfillmentContextId(),
                offer.sku(),
                offer.productName(),
                offer.price(),
                offer.currencyCode(),
                offer.availability(),
                offer.observedAt(),
                offer.sourceReference(),
                Optional.of(packageQuantity));
    }

    private static ObservedOffer offer(
            RetailerId retailerId,
            String fulfillmentContext,
            String sku,
            String productName,
            String price,
            AvailabilityStatus availability) {
        return new ObservedOffer(
                retailerId,
                "fixture-" + retailerId.canonicalId(),
                AcquisitionMode.DIRECT_API,
                fulfillmentContext,
                sku,
                productName,
                new BigDecimal(price),
                "RUB",
                availability,
                OBSERVED_AT,
                "fixture://" + retailerId.canonicalId() + "/" + sku);
    }

    private static String context(RetailerId retailerId) {
        return "acceptance-" + retailerId.canonicalId();
    }
}
