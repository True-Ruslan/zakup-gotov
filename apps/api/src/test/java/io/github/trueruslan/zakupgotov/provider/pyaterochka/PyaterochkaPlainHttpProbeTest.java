package io.github.trueruslan.zakupgotov.provider.pyaterochka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PyaterochkaPlainHttpProbeTest {

    @Test
    void buildsStoreScopedConsumerApiRequests() {
        var probe = PyaterochkaPlainHttpProbe.create();

        assertThat(probe.storeLookupUri(37.6208, 55.7539).toString())
                .isEqualTo("https://5d.5ka.ru/api/orders/v1/orders/stores/?lon=37.6208&lat=55.7539");
        assertThat(probe.searchUri("12345", "молоко", 3).toString())
                .isEqualTo("https://5d.5ka.ru/api/catalog/v3/stores/12345/search"
                        + "?mode=store&include_restrict=true&q=%D0%BC%D0%BE%D0%BB%D0%BE%D0%BA%D0%BE&limit=3");
    }

    @Test
    void plainHttpPolicyUsesNoCapturedBrowserHeadersOrCredentials() {
        var probe = PyaterochkaPlainHttpProbe.create();

        assertThat(probe.requestHeaders()).containsOnlyKeys("Accept", "User-Agent");
        assertThat(probe.requestHeaders().keySet())
                .doesNotContainAnyElementsOf(Set.of(
                        "Cookie",
                        "Authorization",
                        "x-app-version",
                        "x-device-id",
                        "x-platform"));
    }

    @Test
    void livePhaseAProbeRunsOnlyWhenExplicitlyEnabled() throws Exception {
        assumeTrue(Boolean.getBoolean("zakup.live.pyaterochka"));

        var result = PyaterochkaPlainHttpProbe.create().runPhaseA(37.6208, 55.7539, "молоко");
        System.out.println(result.toEvidenceLine());

        assertThat(result.storeLookupStatus()).isBetween(200, 299);
        assertThat(result.sapCode()).isNotBlank();
        assertThat(result.searchStatus()).isBetween(200, 299);
        assertThat(result.productPlu()).isNotBlank();
        assertThat(result.priceEvidence()).isTrue();
    }
}
