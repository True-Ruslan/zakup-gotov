package io.github.trueruslan.zakupgotov.preview;

import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.DATA_NOT_AVAILABLE;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.ITEM_AMBIGUOUS;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.ITEM_UNMATCHED;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.PACKAGE_QUANTITY_UNKNOWN;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.QUANTITY_UNIT_MISMATCH;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.SOURCE_UNAVAILABLE;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus.INCOMPLETE;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus.READY;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus.UNCERTAIN;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus.UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.basket.BasketItemResolutionStatus;
import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderPathSelection;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.Retailer;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComparisonPreviewServiceTest {

    private static final UUID MILK_ID = UUID.fromString("c281d71c-2b27-46ef-a7af-3d624a7447cf");
    private static final UUID EGGS_ID = UUID.fromString("66d66ee8-521f-48ef-82bd-bc9b850099c2");
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T10:00:00Z");

    @Test
    void assemblesMixedRetailerStatesFromOneDeterministicEvidenceInput() {
        var source = (ComparisonRuntimeEvidenceSource) (shoppingList, productLocation) -> ComparisonRuntimeEvidence.of(List.of(
                completeEvidence(RetailerId.PYATEROCHKA, AvailabilityStatus.AVAILABLE),
                completeEvidence(RetailerId.PEREKRESTOK, AvailabilityStatus.UNKNOWN),
                unmatchedEvidence(RetailerId.LENTA),
                ambiguousEvidence(RetailerId.VKUSVILL),
                unitMismatchEvidence(RetailerId.OZON_FRESH),
                packageUnknownEvidence(RetailerId.MAGNIT),
                unavailableEvidence(RetailerId.SAMOKAT)));
        var service = new ComparisonPreviewService(testRegistry(), source);

        var preview = service.create(request());

        assertThat(preview.locality()).isEqualTo("Москва");
        assertThat(preview.items()).extracting(ComparisonPreviewRequestedItem::id)
                .containsExactly(MILK_ID, EGGS_ID);
        assertThat(preview.items().getFirst().quantity())
                .isEqualTo(new Quantity(new BigDecimal("2000"), QuantityUnit.MILLILITER));
        assertThat(preview.retailers()).extracting(ComparisonPreviewRetailer::id)
                .containsExactly(
                        "pyaterochka",
                        "perekrestok",
                        "chizhik",
                        "magnit",
                        "lenta",
                        "vkusvill",
                        "ozon-fresh",
                        "samokat");

        var pyaterochka = preview.require("pyaterochka");
        assertThat(pyaterochka.comparisonStatus()).isEqualTo(READY);
        assertThat(pyaterochka.total()).isPresent();
        assertThat(pyaterochka.total().orElseThrow().amount()).isEqualByComparingTo("320.00");
        assertThat(pyaterochka.items()).extracting(ComparisonPreviewItem::status)
                .containsExactly(BasketItemResolutionStatus.FULFILLED, BasketItemResolutionStatus.FULFILLED);
        assertThat(pyaterochka.items().getFirst().selection()).isPresent();
        assertThat(pyaterochka.items().getFirst().selection().orElseThrow().productName()).isEqualTo("Молоко");
        assertThat(pyaterochka.items().getFirst().selection().orElseThrow().packageCount())
                .isEqualTo(BigInteger.valueOf(2));

        var perekrestok = preview.require("perekrestok");
        assertThat(perekrestok.comparisonStatus()).isEqualTo(UNCERTAIN);
        assertThat(perekrestok.items().getFirst().status()).isEqualTo(BasketItemResolutionStatus.AVAILABILITY_UNKNOWN);
        assertThat(perekrestok.total()).isPresent();

        assertThat(preview.require("chizhik").comparisonStatus()).isEqualTo(UNAVAILABLE);
        assertThat(preview.require("chizhik").reasons()).containsExactly(DATA_NOT_AVAILABLE);

        var magnit = preview.require("magnit");
        assertThat(magnit.comparisonStatus()).isEqualTo(INCOMPLETE);
        assertThat(magnit.reasons()).contains(PACKAGE_QUANTITY_UNKNOWN);
        assertThat(magnit.total()).isEmpty();

        var lenta = preview.require("lenta");
        assertThat(lenta.comparisonStatus()).isEqualTo(INCOMPLETE);
        assertThat(lenta.reasons()).contains(ITEM_UNMATCHED);

        var vkusvill = preview.require("vkusvill");
        assertThat(vkusvill.comparisonStatus()).isEqualTo(INCOMPLETE);
        assertThat(vkusvill.reasons()).contains(ITEM_AMBIGUOUS);
        assertThat(vkusvill.items().getFirst().candidateProductNames()).containsExactly("Молоко", "Молоко");

        var ozonFresh = preview.require("ozon-fresh");
        assertThat(ozonFresh.comparisonStatus()).isEqualTo(INCOMPLETE);
        assertThat(ozonFresh.reasons()).contains(QUANTITY_UNIT_MISMATCH);

        var samokat = preview.require("samokat");
        assertThat(samokat.comparisonStatus()).isEqualTo(UNAVAILABLE);
        assertThat(samokat.reasons()).containsExactly(SOURCE_UNAVAILABLE);
        assertThat(samokat.items()).isEmpty();
    }

    @Test
    void successfulZeroOfferSearchStillProducesUnmatchedBasketUsingExplicitInternalContext() {
        var outcome = new ProviderSearchOutcome(
                RetailerId.PYATEROCHKA,
                Optional.of(new ProviderPathSelection("fixture-pyaterochka", AcquisitionMode.DIRECT_API)),
                List.of(),
                List.of());
        var evidence = RetailerRuntimeEvidence.withFulfillmentContext(
                RetailerId.PYATEROCHKA,
                "ctx-empty",
                outcome,
                List.of());
        var source = (ComparisonRuntimeEvidenceSource) (shoppingList, productLocation) ->
                ComparisonRuntimeEvidence.of(List.of(evidence));

        var preview = new ComparisonPreviewService(testRegistry(), source).create(request());

        assertThat(preview.require("pyaterochka").comparisonStatus()).isEqualTo(INCOMPLETE);
        assertThat(preview.require("pyaterochka").reasons()).containsExactly(ITEM_UNMATCHED);
        assertThat(preview.require("pyaterochka").items()).extracting(ComparisonPreviewItem::status)
                .containsOnly(BasketItemResolutionStatus.UNMATCHED);
    }

    @Test
    void publicProjectionTypesExposeNoProviderOrStoreImplementationIdentifiers() {
        var forbidden = List.of(
                "sku", "sourceprovider", "acquisition", "sourcereference", "fulfillmentcontext",
                "cookie", "token", "url", "payload");

        for (var type : List.of(
                ComparisonPreview.class,
                ComparisonPreviewRequestedItem.class,
                ComparisonPreviewRetailer.class,
                ComparisonPreviewItem.class,
                ComparisonPreviewSelection.class)) {
            var names = Arrays.stream(type.getRecordComponents())
                    .map(component -> component.getName().toLowerCase())
                    .toList();
            for (var name : names) {
                assertThat(forbidden).noneMatch(name::contains);
            }
        }
    }

    private static ComparisonPreviewRequest request() {
        return new ComparisonPreviewRequest("Москва", List.of(
                new ComparisonPreviewItemRequest(MILK_ID, "Молоко", new BigDecimal("2"), QuantityUnit.LITER),
                new ComparisonPreviewItemRequest(EGGS_ID, "Яйца", new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRegistry testRegistry() {
        return RetailerRegistry.of(Arrays.stream(RetailerId.values())
                .map(id -> new RetailerRegistryEntry(
                        new Retailer(id),
                        RetailerCoverageState.AVAILABLE_DIRECT,
                        ProductionAccessStatus.ACCEPTABLE))
                .toList());
    }

    private static RetailerRuntimeEvidence completeEvidence(RetailerId retailerId, AvailabilityStatus milkAvailability) {
        var context = "ctx-" + retailerId.canonicalId();
        var milk = offer(retailerId, context, "milk", "Молоко", "100.00", milkAvailability);
        var eggs = offer(retailerId, context, "eggs", "Яйца", "120.00", AvailabilityStatus.AVAILABLE);
        return successfulEvidence(
                retailerId,
                context,
                List.of(milk, eggs),
                List.of(
                        new Quantity(new BigDecimal("1"), QuantityUnit.LITER),
                        new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence packageUnknownEvidence(RetailerId retailerId) {
        var context = "ctx-" + retailerId.canonicalId();
        var milk = offer(retailerId, context, "milk", "Молоко", "90.00", AvailabilityStatus.AVAILABLE);
        var eggs = offer(retailerId, context, "eggs", "Яйца", "110.00", AvailabilityStatus.AVAILABLE);
        return successfulEvidence(
                retailerId,
                context,
                List.of(milk, eggs),
                Arrays.asList(null, new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence unmatchedEvidence(RetailerId retailerId) {
        var context = "ctx-" + retailerId.canonicalId();
        return successfulEvidence(
                retailerId,
                context,
                List.of(offer(retailerId, context, "bread", "Хлеб", "70.00", AvailabilityStatus.AVAILABLE)),
                List.of(new Quantity(BigDecimal.ONE, QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence ambiguousEvidence(RetailerId retailerId) {
        var context = "ctx-" + retailerId.canonicalId();
        return successfulEvidence(
                retailerId,
                context,
                List.of(
                        offer(retailerId, context, "milk-a", "Молоко", "100.00", AvailabilityStatus.AVAILABLE),
                        offer(retailerId, context, "milk-b", "Молоко", "105.00", AvailabilityStatus.AVAILABLE),
                        offer(retailerId, context, "eggs", "Яйца", "120.00", AvailabilityStatus.AVAILABLE)),
                List.of(
                        new Quantity(new BigDecimal("1"), QuantityUnit.LITER),
                        new Quantity(new BigDecimal("1"), QuantityUnit.LITER),
                        new Quantity(new BigDecimal("10"), QuantityUnit.PIECE)));
    }

    private static RetailerRuntimeEvidence unitMismatchEvidence(RetailerId retailerId) {
        var context = "ctx-" + retailerId.canonicalId();
        var milk = offer(retailerId, context, "milk", "Молоко", "100.00", AvailabilityStatus.AVAILABLE);
        return successfulEvidence(
                retailerId,
                context,
                List.of(milk),
                List.of(new Quantity(new BigDecimal("500"), QuantityUnit.GRAM)));
    }

    private static RetailerRuntimeEvidence unavailableEvidence(RetailerId retailerId) {
        return new RetailerRuntimeEvidence(
                retailerId,
                new ProviderSearchOutcome(retailerId, Optional.empty(), List.of(), List.of()),
                List.of());
    }

    private static RetailerRuntimeEvidence successfulEvidence(
            RetailerId retailerId,
            String context,
            List<ObservedOffer> offers,
            List<Quantity> packageQuantities) {
        var normalizedOffers = new ArrayList<ObservedOffer>();
        var snapshots = new ArrayList<OfferSnapshot>();
        for (var index = 0; index < offers.size(); index++) {
            var offer = withPackageQuantity(offers.get(index), packageQuantities.get(index));
            normalizedOffers.add(offer);
            snapshots.add(OfferSnapshot.observationOnly(
                    new OfferSnapshotId(UUID.nameUUIDFromBytes((retailerId + "-snapshot-" + index).getBytes())),
                    offer));
        }
        var outcome = new ProviderSearchOutcome(
                retailerId,
                Optional.of(new ProviderPathSelection("fixture-" + retailerId.canonicalId(), AcquisitionMode.DIRECT_API)),
                normalizedOffers,
                List.of());
        return RetailerRuntimeEvidence.withFulfillmentContext(
                retailerId,
                context,
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
            String context,
            String sku,
            String productName,
            String price,
            AvailabilityStatus availability) {
        return new ObservedOffer(
                retailerId,
                "fixture-" + retailerId.canonicalId(),
                AcquisitionMode.DIRECT_API,
                context,
                sku,
                productName,
                new BigDecimal(price),
                "RUB",
                availability,
                OBSERVED_AT,
                "fixture://" + retailerId.canonicalId() + "/" + sku);
    }
}
