package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class MagnitPublicPageProbeTest {

    private static final String PRODUCT_SLUG =
            "1000379971-moloko_sgushchennoe_360g_zhestyanaya_banka_zao_amkk_45";

    @Test
    void buildsSameProductForTwoExplicitStoreContexts() {
        var probe = MagnitPublicPageProbe.create();

        assertThat(probe.productUri(PRODUCT_SLUG, "139147").toString())
                .isEqualTo("https://magnit.ru/product/" + PRODUCT_SLUG + "?shopCode=139147&shopType=1");
        assertThat(probe.productUri(PRODUCT_SLUG, "773577").toString())
                .isEqualTo("https://magnit.ru/product/" + PRODUCT_SLUG + "?shopCode=773577&shopType=1");
    }

    @Test
    void requestPolicyUsesNoLoginOrPartnerCredentials() {
        var probe = MagnitPublicPageProbe.create();

        assertThat(probe.requestHeaders()).containsOnlyKeys("Accept", "User-Agent");
        assertThat(probe.requestHeaders().keySet())
                .doesNotContainAnyElementsOf(Set.of(
                        "Cookie",
                        "Authorization",
                        "X-Api-Key",
                        "X-Auth-Token",
                        "Sec-CH-UA"));
    }

    @Test
    void livePhaseAProbeRunsOnlyWhenExplicitlyEnabled() throws Exception {
        assumeTrue(Boolean.getBoolean("zakup.live.magnit"));

        var result = MagnitPublicPageProbe.create()
                .runPhaseA(PRODUCT_SLUG, "1000379971", "139147", "773577");
        System.out.println(result.toEvidenceLine());

        assertThat(result.firstStatus()).isBetween(200, 299);
        assertThat(result.firstSkuEvidence()).isTrue();
        assertThat(result.firstPricePresent()).isTrue();
        assertThat(result.secondStatus()).isBetween(200, 299);
        assertThat(result.secondSkuEvidence()).isTrue();
        assertThat(result.secondPricePresent()).isTrue();
    }
}
