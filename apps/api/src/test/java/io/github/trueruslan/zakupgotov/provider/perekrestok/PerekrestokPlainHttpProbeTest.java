package io.github.trueruslan.zakupgotov.provider.perekrestok;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PerekrestokPlainHttpProbeTest {

    @Test
    void buildsStoreSelectionAndSearchRequests() {
        var probe = PerekrestokPlainHttpProbe.create();

        assertThat(probe.warmupUri().toString())
                .isEqualTo("https://www.perekrestok.ru/");
        assertThat(probe.nearbyStoresUri(37.6208, 55.7539).toString())
                .isEqualTo("https://www.perekrestok.ru/api/customer/1.4.1.0/shop"
                        + "?orderBy=distance&orderDirection=asc&page=1&perPage=3&lat=55.7539&lng=37.6208");
        assertThat(probe.selectPickupUri("1235").toString())
                .isEqualTo("https://www.perekrestok.ru/api/customer/1.4.1.0/delivery/mode/pickup/1235");
        assertThat(probe.searchUri("молоко").toString())
                .isEqualTo("https://www.perekrestok.ru/api/customer/1.4.1.0/catalog/search/all"
                        + "?textQuery=%D0%BC%D0%BE%D0%BB%D0%BE%D0%BA%D0%BE&entityTypes%5B%5D=product");
    }

    @Test
    void requestPolicyDoesNotSendBrowserDerivedAuthorization() {
        var probe = PerekrestokPlainHttpProbe.create();

        assertThat(probe.requestHeaders()).containsOnlyKeys("Accept", "User-Agent");
        assertThat(probe.requestHeaders().keySet())
                .doesNotContainAnyElementsOf(Set.of(
                        "Cookie",
                        "Authorization",
                        "Auth",
                        "X-Auth-Token",
                        "Sec-CH-UA"));
    }

    @Test
    void livePhaseAProbeRunsOnlyWhenExplicitlyEnabled() throws Exception {
        assumeTrue(Boolean.getBoolean("zakup.live.perekrestok"));

        var result = PerekrestokPlainHttpProbe.create().runPhaseA(37.6208, 55.7539, "молоко");
        System.out.println(result.toEvidenceLine());

        assertThat(result.storeStatus()).isBetween(200, 299);
        assertThat(result.shopId()).isNotBlank();
        assertThat(result.selectionStatus()).isBetween(200, 299);
        assertThat(result.searchStatus()).isBetween(200, 299);
        assertThat(result.productPlu()).isNotBlank();
        assertThat(result.priceEvidence()).isTrue();
    }
}
