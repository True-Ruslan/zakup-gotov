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

    private static BasketTotal money(String amount, String currency) {
        return new BasketTotal(new BigDecimal(amount), currency);
    }
}
