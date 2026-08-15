package io.github.trueruslan.zakupgotov.optimizationpreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketEconomicsKnowledgeStatus;
import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderConstraint;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizationStatus;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizer;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonCatalog;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshness;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshnessBasis;
import io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus;
import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewComputation;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewInput;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRetailer;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutComparabilityStatus;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutEligibilityStatus;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CheckoutOptimizationPreviewServiceTest {

    @Test
    void missingEconomicsIsExplicitUnknownAndCannotCreateComparableWinner() {
        var service = service((location, requestedRetailers) -> Map.of());

        var result = service.create(computation(
                ready(RetailerId.PYATEROCHKA, "Пятёрочка", "1000.00"),
                ready(RetailerId.PEREKRESTOK, "Перекрёсток", "900.00")));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.NO_COMPARABLE_CANDIDATES);
        assertThat(result.optimalRetailerIds()).isEmpty();
        assertThat(result.lowestComparableCheckoutTotal()).isEmpty();
        assertThat(result.retailers()).extracting(RetailerCheckoutPreview::retailerId)
                .containsExactly("pyaterochka", "perekrestok");

        for (var retailer : result.retailers()) {
            var assessment = retailer.assessment().orElseThrow();
            assertThat(assessment.deliveryFee().status()).isEqualTo(BasketEconomicsKnowledgeStatus.UNKNOWN);
            assertThat(assessment.deliveryFee().amount()).isEmpty();
            assertThat(assessment.serviceFee().status()).isEqualTo(BasketEconomicsKnowledgeStatus.UNKNOWN);
            assertThat(assessment.minimumOrder().status()).isEqualTo(BasketEconomicsKnowledgeStatus.UNKNOWN);
            assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.UNKNOWN);
            assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.NOT_COMPARABLE);
            assertThat(assessment.comparableCheckoutTotal()).isEmpty();
        }
    }

    @Test
    void knownEconomicsDelegatesToM42AndM43ForUniqueWinner() {
        var service = service((location, requestedRetailers) -> Map.of(
                RetailerId.PYATEROCHKA, economics("100", "0", "0"),
                RetailerId.PEREKRESTOK, economics("0", "0", "0")));

        var result = service.create(computation(
                ready(RetailerId.PYATEROCHKA, "Пятёрочка", "1000.00"),
                ready(RetailerId.PEREKRESTOK, "Перекрёсток", "900.00")));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.UNIQUE_WINNER);
        assertThat(result.optimalRetailerIds()).containsExactly("perekrestok");
        assertThat(result.lowestComparableCheckoutTotal()).contains(total("900.00"));
        assertThat(result.retailers().get(1).assessment().orElseThrow().comparableCheckoutTotal())
                .contains(total("900.00"));
    }

    @Test
    void exactEqualMinimaRemainTieInCatalogOrder() {
        var service = service((location, requestedRetailers) -> Map.of(
                RetailerId.PYATEROCHKA, economics("0", "0", "0"),
                RetailerId.PEREKRESTOK, economics("100", "0", "0")));

        var result = service.create(computation(
                ready(RetailerId.PYATEROCHKA, "Пятёрочка", "1000.0"),
                ready(RetailerId.PEREKRESTOK, "Перекрёсток", "900.00")));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.TIE);
        assertThat(result.optimalRetailerIds())
                .containsExactly("pyaterochka", "perekrestok");
        assertThat(result.lowestComparableCheckoutTotal()).contains(total("1000.0"));
    }

    @Test
    void economicsSourceIsScopedToAssessableRetailersAndRejectsOutOfScopeEvidence() {
        var requested = new AtomicReference<Set<RetailerId>>();
        var service = service((location, requestedRetailers) -> {
            requested.set(requestedRetailers);
            return Map.of(RetailerId.MAGNIT, economics("0", "0", "0"));
        });

        assertThatThrownBy(() -> service.create(computation(
                        ready(RetailerId.PYATEROCHKA, "Пятёрочка", "1000"),
                        incomplete(RetailerId.PEREKRESTOK, "Перекрёсток"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unrequested retailer");
        assertThat(requested.get()).containsExactly(RetailerId.PYATEROCHKA);
    }

    private static CheckoutOptimizationPreviewService service(CheckoutEconomicsEvidenceSource source) {
        return new CheckoutOptimizationPreviewService(
                source,
                new RetailerCheckoutAssessmentService(),
                new BasketOptimizer());
    }

    private static ComparisonPreviewComputation computation(RetailerComparisonView... views) {
        var location = ProductLocation.localityOnly(
                new ProductLocationId(UUID.fromString("d1000000-0000-0000-0000-000000000001")),
                "Москва");
        var shoppingList = new ShoppingList(
                new ShoppingListId(UUID.fromString("d2000000-0000-0000-0000-000000000001")));
        var input = new ComparisonPreviewInput(shoppingList, location);
        var catalog = new RetailerComparisonCatalog(List.of(views));
        var retailers = catalog.retailers().stream()
                .map(view -> new ComparisonPreviewRetailer(
                        view.retailerId().canonicalId(),
                        view.displayName(),
                        view.coverage(),
                        view.productionAccess(),
                        view.comparisonStatus(),
                        view.reasons(),
                        view.total(),
                        view.freshness(),
                        List.of()))
                .toList();
        return new ComparisonPreviewComputation(
                input,
                new ComparisonPreview("Москва", List.of(), retailers),
                catalog);
    }

    private static RetailerComparisonView ready(RetailerId id, String name, String subtotal) {
        return new RetailerComparisonView(
                id,
                name,
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(total(subtotal)),
                Optional.of(freshness()));
    }

    private static RetailerComparisonView incomplete(RetailerId id, String name) {
        return new RetailerComparisonView(
                id,
                name,
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.INCOMPLETE,
                List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                Optional.empty(),
                Optional.empty());
    }

    private static RetailerFreshness freshness() {
        return new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                Instant.parse("2026-08-15T10:00:00Z"),
                Optional.empty());
    }

    private static BasketEconomics economics(String deliveryFee, String serviceFee, String minimumOrder) {
        return new BasketEconomics(
                BasketFee.known(total(deliveryFee)),
                BasketFee.known(total(serviceFee)),
                MinimumOrderConstraint.known(total(minimumOrder)));
    }

    private static BasketTotal total(String amount) {
        return new BasketTotal(new BigDecimal(amount), "RUB");
    }
}
