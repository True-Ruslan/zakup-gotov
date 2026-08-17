package io.github.trueruslan.zakupgotov.provider.chizhik;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ChizhikPublicCatalogDocumentLiveProbeTest {

    @Test
    void probesOnlyWhenExplicitlyEnabledAndEmitsSanitizedEvidence() {
        assumeTrue(Boolean.getBoolean("zakup.live.chizhik.catalog"));

        var rawCandidate = System.getProperty("zakup.live.chizhik.catalog.url", "").trim();
        assertThat(rawCandidate).isNotBlank();
        var candidate = ChizhikPublicCatalogDocumentUriPolicy.requireAllowed(URI.create(rawCandidate));

        ChizhikPublicCatalogDocumentObservation observation;
        try {
            observation = ChizhikPublicCatalogDocumentProbe.live().probe(candidate);
        } catch (RuntimeException exception) {
            System.out.println("CHIZHIK_PHASE_C status=TRANSPORT_ERROR");
            fail("Chizhik Phase C live probe transport failed");
            return;
        }

        var contentType = observation.contentType().isBlank() ? "none" : observation.contentType();
        System.out.printf(
                "CHIZHIK_PHASE_C status=%s http_status=%d content_type=%s path=%s observed_at=%s%n",
                observation.status(),
                observation.httpStatus(),
                contentType,
                observation.path(),
                observation.observedAt());

        assertThat(observation.status()).isEqualTo(ChizhikPublicCatalogDocumentStatus.REACHABLE_PDF);
        assertThat(observation.httpStatus()).isEqualTo(200);
        assertThat(observation.contentType()).isEqualTo("application/pdf");
    }
}
