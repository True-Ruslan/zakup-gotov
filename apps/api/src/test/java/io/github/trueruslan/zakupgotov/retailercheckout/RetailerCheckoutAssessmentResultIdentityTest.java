package io.github.trueruslan.zakupgotov.retailercheckout;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RetailerCheckoutAssessmentResultIdentityTest {

    @Test
    void exposesRetailerIdentityWithoutReachingThroughComparisonAtDownstreamCallers() {
        var result = new RetailerCheckoutAssessmentResult(
                new RetailerComparisonView(
                        RetailerId.PYATEROCHKA,
                        "Пятёрочка",
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.INCOMPLETE,
                        List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                        Optional.empty(),
                        Optional.empty()),
                Optional.empty());

        assertThat(result.retailerId()).isEqualTo(RetailerId.PYATEROCHKA);
    }
}
