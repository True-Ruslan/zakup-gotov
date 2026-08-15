package io.github.trueruslan.zakupgotov.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BasketEconomicsCalculatorTest {

    @Test
    void knownFeesProduceKnownCheckoutTotal() {
        var subtotal = money("1200.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("149.00", "RUB")),
                BasketFee.known(money("39.00", "RUB")),
                MinimumOrderConstraint.known(money("1000.00", "RUB")));

        var assessment = BasketEconomicsCalculator.assess(subtotal, economics);

        assertThat(assessment.merchandiseSubtotal()).isEqualTo(subtotal);
        assertThat(assessment.economics()).isEqualTo(economics);
        assertThat(assessment.minimumOrderStatus()).isEqualTo(MinimumOrderStatus.MET);
        assertThat(assessment.checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.KNOWN);
        assertThat(assessment.checkoutTotal()).contains(money("1388.00", "RUB"));
    }

    @Test
    void knownZeroFeesRemainKnownRatherThanBecomingUnknown() {
        var subtotal = money("499.90", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("0", "RUB")),
                BasketFee.known(money("0.00", "RUB")),
                MinimumOrderConstraint.known(money("0", "RUB")));

        var assessment = BasketEconomicsCalculator.assess(subtotal, economics);

        assertThat(economics.deliveryFee().status()).isEqualTo(BasketEconomicsKnowledgeStatus.KNOWN);
        assertThat(economics.deliveryFee().amount()).contains(money("0", "RUB"));
        assertThat(economics.serviceFee().status()).isEqualTo(BasketEconomicsKnowledgeStatus.KNOWN);
        assertThat(assessment.minimumOrderStatus()).isEqualTo(MinimumOrderStatus.MET);
        assertThat(assessment.checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.KNOWN);
        assertThat(assessment.checkoutTotal()).contains(money("499.90", "RUB"));
    }

    @Test
    void unknownDeliveryFeeFailsClosedWithoutLosingMerchandiseSubtotal() {
        var subtotal = money("900.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.unknown(),
                BasketFee.known(money("20.00", "RUB")),
                MinimumOrderConstraint.known(money("800.00", "RUB")));

        var assessment = BasketEconomicsCalculator.assess(subtotal, economics);

        assertThat(economics.deliveryFee().status()).isEqualTo(BasketEconomicsKnowledgeStatus.UNKNOWN);
        assertThat(economics.deliveryFee().amount()).isEmpty();
        assertThat(assessment.merchandiseSubtotal()).isEqualTo(subtotal);
        assertThat(assessment.minimumOrderStatus()).isEqualTo(MinimumOrderStatus.MET);
        assertThat(assessment.checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.UNKNOWN);
        assertThat(assessment.checkoutTotal()).isEmpty();
    }

    @Test
    void unknownServiceFeeFailsClosedEvenWhenDeliveryIsKnownZero() {
        var subtotal = money("900.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("0", "RUB")),
                BasketFee.unknown(),
                MinimumOrderConstraint.unknown());

        var assessment = BasketEconomicsCalculator.assess(subtotal, economics);

        assertThat(assessment.merchandiseSubtotal()).isEqualTo(subtotal);
        assertThat(assessment.minimumOrderStatus()).isEqualTo(MinimumOrderStatus.UNKNOWN);
        assertThat(assessment.checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.UNKNOWN);
        assertThat(assessment.checkoutTotal()).isEmpty();
    }

    @Test
    void minimumOrderUsesMerchandiseSubtotalOnly() {
        var subtotal = money("950.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("100.00", "RUB")),
                BasketFee.known(money("50.00", "RUB")),
                MinimumOrderConstraint.known(money("1000.00", "RUB")));

        var assessment = BasketEconomicsCalculator.assess(subtotal, economics);

        assertThat(assessment.minimumOrderStatus()).isEqualTo(MinimumOrderStatus.NOT_MET);
        assertThat(assessment.checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.KNOWN);
        assertThat(assessment.checkoutTotal()).contains(money("1100.00", "RUB"));
    }

    @Test
    void exactDecimalArithmeticDoesNotApplyHiddenRoundingOrRescaling() {
        var subtotal = money("100.005", "RUB");
        var economics = new BasketEconomics(
                BasketFee.known(money("0.015", "RUB")),
                BasketFee.known(money("0.010", "RUB")),
                MinimumOrderConstraint.known(money("100.004", "RUB")));

        var assessment = BasketEconomicsCalculator.assess(subtotal, economics);

        assertThat(assessment.minimumOrderStatus()).isEqualTo(MinimumOrderStatus.MET);
        assertThat(assessment.checkoutTotal()).contains(money("100.030", "RUB"));
        assertThat(assessment.checkoutTotal().orElseThrow().amount().scale()).isEqualTo(3);
    }

    @Test
    void mixedCurrencyKnownComponentsFailFast() {
        var subtotal = money("1000.00", "RUB");

        assertThatThrownBy(() -> BasketEconomicsCalculator.assess(
                        subtotal,
                        new BasketEconomics(
                                BasketFee.known(money("1.00", "USD")),
                                BasketFee.known(money("0", "RUB")),
                                MinimumOrderConstraint.unknown())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");

        assertThatThrownBy(() -> BasketEconomicsCalculator.assess(
                        subtotal,
                        new BasketEconomics(
                                BasketFee.known(money("0", "RUB")),
                                BasketFee.known(money("0", "RUB")),
                                MinimumOrderConstraint.known(money("10.00", "USD")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void rejectsNullInputsAndInvalidKnowledgeShapes() {
        var subtotal = money("100.00", "RUB");
        var economics = new BasketEconomics(
                BasketFee.unknown(),
                BasketFee.unknown(),
                MinimumOrderConstraint.unknown());

        assertThatThrownBy(() -> BasketEconomicsCalculator.assess(null, economics))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("merchandiseSubtotal");
        assertThatThrownBy(() -> BasketEconomicsCalculator.assess(subtotal, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("economics");
    }

    private static BasketTotal money(String amount, String currency) {
        return new BasketTotal(new BigDecimal(amount), currency);
    }
}
