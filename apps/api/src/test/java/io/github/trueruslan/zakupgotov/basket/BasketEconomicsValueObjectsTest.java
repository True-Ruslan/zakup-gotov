package io.github.trueruslan.zakupgotov.basket;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BasketEconomicsValueObjectsTest {

    @Test
    void feeKnowledgeStateCannotContradictAmountPresence() {
        var zero = money("0", "RUB");

        assertThatThrownBy(() -> new BasketFee(BasketEconomicsKnowledgeStatus.KNOWN, Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KNOWN fee");
        assertThatThrownBy(() -> new BasketFee(BasketEconomicsKnowledgeStatus.UNKNOWN, Optional.of(zero)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN fee");
    }

    @Test
    void minimumOrderKnowledgeStateCannotContradictThresholdPresence() {
        var zero = money("0", "RUB");

        assertThatThrownBy(() -> new MinimumOrderConstraint(
                        BasketEconomicsKnowledgeStatus.KNOWN,
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KNOWN minimum order");
        assertThatThrownBy(() -> new MinimumOrderConstraint(
                        BasketEconomicsKnowledgeStatus.UNKNOWN,
                        Optional.of(zero)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN minimum order");
    }

    @Test
    void negativeKnownAmountsRemainRejectedByExistingBasketMoneyConvention() {
        assertThatThrownBy(() -> BasketFee.known(money("-0.01", "RUB")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
        assertThatThrownBy(() -> MinimumOrderConstraint.known(money("-1.00", "RUB")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void economicsRejectsNullComponents() {
        var unknownFee = BasketFee.unknown();
        var unknownMinimum = MinimumOrderConstraint.unknown();

        assertThatThrownBy(() -> new BasketEconomics(null, unknownFee, unknownMinimum))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("deliveryFee");
        assertThatThrownBy(() -> new BasketEconomics(unknownFee, null, unknownMinimum))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("serviceFee");
        assertThatThrownBy(() -> new BasketEconomics(unknownFee, unknownFee, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("minimumOrder");
    }

    @Test
    void assessmentRejectsMinimumOrderStatusThatContradictsKnownThreshold() {
        var subtotal = money("950.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("100.00", "RUB")),
                BasketFee.known(money("50.00", "RUB")),
                MinimumOrderConstraint.known(money("1000.00", "RUB")));

        assertThatThrownBy(() -> new BasketEconomicsAssessment(
                        subtotal,
                        economics,
                        MinimumOrderStatus.MET,
                        CheckoutTotalStatus.KNOWN,
                        Optional.of(money("1100.00", "RUB"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum order status");
    }

    @Test
    void assessmentRejectsCheckoutKnowledgeThatContradictsUnknownFee() {
        var subtotal = money("900.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.unknown(),
                BasketFee.known(money("20.00", "RUB")),
                MinimumOrderConstraint.unknown());

        assertThatThrownBy(() -> new BasketEconomicsAssessment(
                        subtotal,
                        economics,
                        MinimumOrderStatus.UNKNOWN,
                        CheckoutTotalStatus.KNOWN,
                        Optional.of(money("920.00", "RUB"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkout total status");
    }

    @Test
    void assessmentRejectsCheckoutAmountThatContradictsKnownFees() {
        var subtotal = money("1200.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("149.00", "RUB")),
                BasketFee.known(money("39.00", "RUB")),
                MinimumOrderConstraint.known(money("1000.00", "RUB")));

        assertThatThrownBy(() -> new BasketEconomicsAssessment(
                        subtotal,
                        economics,
                        MinimumOrderStatus.MET,
                        CheckoutTotalStatus.KNOWN,
                        Optional.of(money("1387.99", "RUB"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkout total amount");
    }

    private static BasketTotal money(String amount, String currency) {
        return new BasketTotal(new BigDecimal(amount), currency);
    }
}
