package io.github.trueruslan.zakupgotov.provider.chizhik;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChizhikPlainHttpProbeTest {

    @Test
    void buildsOnlyUnauthenticatedCatalogRequests() {
        var probe = ChizhikPlainHttpProbe.create();

        assertThat(probe.categoriesUri().toString())
                .isEqualTo("https://app.chizhik.club/api/v1/catalog/unauthorized/categories/");
        assertThat(probe.searchUri("молоко", 1).toString())
                .isEqualTo("https://app.chizhik.club/api/v1/catalog/unauthorized/products/"
                        + "?page=1&term=%D0%BC%D0%BE%D0%BB%D0%BE%D0%BA%D0%BE");
    }

    @Test
    void rejectsInvalidSearchInput() {
        var probe = ChizhikPlainHttpProbe.create();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> probe.searchUri(" ", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> probe.searchUri("молоко", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    void plainHttpPolicyUsesNoCapturedBrowserHeadersOrCredentials() {
        var probe = ChizhikPlainHttpProbe.create();

        assertThat(probe.requestHeaders()).containsOnlyKeys("Accept", "User-Agent");
        assertThat(probe.requestHeaders().keySet())
                .doesNotContainAnyElementsOf(Set.of(
                        "Cookie",
                        "Authorization",
                        "Referer",
                        "Origin",
                        "x-app-version",
                        "x-device-id",
                        "x-platform"));
    }

    @Test
    void classifiesHttpStatusesFailClosed() {
        assertThat(ChizhikPlainHttpProbe.classifyStatus(200)).isEqualTo(ChizhikPlainHttpProbe.Outcome.ACCESSIBLE);
        assertThat(ChizhikPlainHttpProbe.classifyStatus(302)).isEqualTo(ChizhikPlainHttpProbe.Outcome.REDIRECTED);
        assertThat(ChizhikPlainHttpProbe.classifyStatus(403)).isEqualTo(ChizhikPlainHttpProbe.Outcome.BLOCKED);
        assertThat(ChizhikPlainHttpProbe.classifyStatus(429)).isEqualTo(ChizhikPlainHttpProbe.Outcome.RATE_LIMITED);
        assertThat(ChizhikPlainHttpProbe.classifyStatus(500)).isEqualTo(ChizhikPlainHttpProbe.Outcome.HTTP_ERROR);
    }

    @Test
    void recordsOnlySanitizedShapeEvidenceForAccessibleCatalog() throws Exception {
        var transport = new QueueTransport()
                .respond(200, "[{\"id\":1,\"name\":\"Молочные продукты\",\"slug\":\"milk\"}]")
                .respond(200, "{\"count\":1,\"items\":[{\"id\":42,\"plu\":\"secret-plu\","
                        + "\"title\":\"Молоко тестовое\",\"price\":99.9}]}");
        var probe = ChizhikPlainHttpProbe.forTransport(transport);

        var result = probe.runPhaseA("молоко");

        assertThat(result.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.ACCESSIBLE);
        assertThat(result.categoriesStatus()).isEqualTo(200);
        assertThat(result.categoriesShape()).isTrue();
        assertThat(result.searchOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.ACCESSIBLE);
        assertThat(result.searchStatus()).isEqualTo(200);
        assertThat(result.productIdPresent()).isTrue();
        assertThat(result.productPluPresent()).isTrue();
        assertThat(result.productTitlePresent()).isTrue();
        assertThat(result.priceEvidence()).isTrue();

        assertThat(result.toEvidenceLine())
                .contains("CHIZHIK_PHASE_A")
                .contains("categories_outcome=ACCESSIBLE")
                .contains("search_outcome=ACCESSIBLE")
                .contains("product_plu_present=true")
                .doesNotContain("secret-plu")
                .doesNotContain("Молоко тестовое")
                .doesNotContain("молоко");
    }

    @Test
    void redirectStopsBeforeSearch() throws Exception {
        var transport = new QueueTransport().respond(302, "redirect-body-secret");
        var result = ChizhikPlainHttpProbe.forTransport(transport).runPhaseA("молоко");

        assertThat(result.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.REDIRECTED);
        assertThat(result.categoriesStatus()).isEqualTo(302);
        assertThat(result.searchOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.NOT_ATTEMPTED);
        assertThat(transport.requests()).isEqualTo(1);
        assertThat(result.toEvidenceLine()).doesNotContain("redirect-body-secret");
    }

    @Test
    void blockedAndRateLimitedResponsesRemainExplicit() throws Exception {
        var blocked = ChizhikPlainHttpProbe.forTransport(new QueueTransport().respond(403, "blocked-secret"))
                .runPhaseA("молоко");
        var rateLimited = ChizhikPlainHttpProbe.forTransport(new QueueTransport().respond(429, "rate-secret"))
                .runPhaseA("молоко");

        assertThat(blocked.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.BLOCKED);
        assertThat(rateLimited.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.RATE_LIMITED);
        assertThat(blocked.toEvidenceLine()).doesNotContain("blocked-secret");
        assertThat(rateLimited.toEvidenceLine()).doesNotContain("rate-secret");
    }

    @Test
    void timeoutAndNetworkFailureBecomeSanitizedOutcomes() throws Exception {
        var timeout = ChizhikPlainHttpProbe.forTransport(new QueueTransport().fail(new HttpTimeoutException("token=secret")))
                .runPhaseA("молоко");
        var network = ChizhikPlainHttpProbe.forTransport(new QueueTransport().fail(new IOException("host secret")))
                .runPhaseA("молоко");

        assertThat(timeout.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.TIMEOUT);
        assertThat(network.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.NETWORK_ERROR);
        assertThat(timeout.toEvidenceLine()).doesNotContain("secret");
        assertThat(network.toEvidenceLine()).doesNotContain("host");
    }

    @Test
    void livePhaseAProbeRunsOnlyWhenExplicitlyEnabled() throws Exception {
        assumeTrue(Boolean.getBoolean("zakup.live.chizhik"));

        var result = ChizhikPlainHttpProbe.create().runPhaseA("молоко");
        System.out.println(result.toEvidenceLine());

        assertThat(result.categoriesOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.ACCESSIBLE);
        assertThat(result.categoriesShape()).isTrue();
        assertThat(result.searchOutcome()).isEqualTo(ChizhikPlainHttpProbe.Outcome.ACCESSIBLE);
        assertThat(result.productIdPresent()).isTrue();
        assertThat(result.productTitlePresent()).isTrue();
        assertThat(result.priceEvidence()).isTrue();
    }

    private static final class QueueTransport implements ChizhikPlainHttpProbe.Transport {
        private final Queue<Object> outcomes = new ArrayDeque<>();
        private int requests;

        QueueTransport respond(int status, String body) {
            outcomes.add(new ChizhikPlainHttpProbe.ProbeResponse(status, body));
            return this;
        }

        QueueTransport fail(IOException exception) {
            outcomes.add(exception);
            return this;
        }

        int requests() {
            return requests;
        }

        @Override
        public ChizhikPlainHttpProbe.ProbeResponse get(URI uri) throws IOException {
            requests++;
            var outcome = outcomes.remove();
            if (outcome instanceof IOException exception) {
                throw exception;
            }
            return (ChizhikPlainHttpProbe.ProbeResponse) outcome;
        }
    }
}
