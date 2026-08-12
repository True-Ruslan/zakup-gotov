package io.github.trueruslan.zakupgotov.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductLocationTest {

    private static final ProductLocationId ID = new ProductLocationId(
            UUID.fromString("55555555-5555-5555-5555-555555555555"));

    @Test
    void keepsPreciseAddressRedactedByDefault() {
        var location = ProductLocation.withAddress(ID, " Москва ", " ул. Тестовая, 10 ");

        assertThat(location.id()).isEqualTo(ID);
        assertThat(location.locality()).isEqualTo("Москва");
        assertThat(location.address()).get().extracting(SensitiveAddress::reveal).isEqualTo("ул. Тестовая, 10");
        assertThat(location.address().orElseThrow().toString()).isEqualTo("[REDACTED]");
        assertThat(location.toString()).doesNotContain("Тестовая", "10");
    }

    @Test
    void supportsLocalityOnlyLocationWithoutInventingPreciseAddress() {
        var location = ProductLocation.localityOnly(ID, "  Москва  ");

        assertThat(location.locality()).isEqualTo("Москва");
        assertThat(location.address()).isEmpty();
        assertThat(location.toString()).contains("Москва").doesNotContain("provider", "store", "shopCode");
    }

    @Test
    void sensitiveAddressHasValueEqualityWithoutExposingRawValueInStringForm() {
        assertThat(SensitiveAddress.of(" ул. Тестовая, 10 "))
                .isEqualTo(SensitiveAddress.of("ул. Тестовая, 10"));
        assertThat(SensitiveAddress.of("ул. Тестовая, 10").hashCode())
                .isEqualTo(SensitiveAddress.of("ул. Тестовая, 10").hashCode());
        assertThat(SensitiveAddress.of("ул. Тестовая, 10").toString()).isEqualTo("[REDACTED]");
    }

    @Test
    void rejectsMissingIdentityBlankLocalityAndBlankAddress() {
        assertThatThrownBy(() -> new ProductLocationId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value");
        assertThatThrownBy(() -> ProductLocation.localityOnly(ID, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("locality");
        assertThatThrownBy(() -> ProductLocation.withAddress(ID, "Москва", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("address");
    }
}
