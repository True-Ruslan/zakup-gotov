package io.github.trueruslan.zakupgotov.provider.chizhik;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ChizhikPublicCatalogDocumentProbeTest {

    private static final URI DOCUMENT = URI.create(
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf");
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-17T16:45:00Z");
    private static final Clock CLOCK = Clock.fixed(OBSERVED_AT, ZoneOffset.UTC);

    @Test
    void acceptsOnlyExactOfficialPublicCatalogPdfScope() {
        assertThat(ChizhikPublicCatalogDocumentUriPolicy.requireAllowed(DOCUMENT)).isEqualTo(DOCUMENT);
        assertThat(ChizhikPublicCatalogDocumentUriPolicy.requireAllowed(URI.create(
                "https://media.chizhik.club:443/media/backendprod-dpro/catalog/pdf_file/CATALOG.PDF")))
                .isEqualTo(URI.create(
                        "https://media.chizhik.club:443/media/backendprod-dpro/catalog/pdf_file/CATALOG.PDF"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf",
            "https://chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf",
            "https://app.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf",
            "https://media.chizhik.club.evil.example/media/backendprod-dpro/catalog/pdf_file/catalog.pdf",
            "https://media.chizhik.club:8443/media/backendprod-dpro/catalog/pdf_file/catalog.pdf",
            "https://user@media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf?store=secret",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.pdf#fragment",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/other/catalog.pdf",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/catalog.html",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/../secret.pdf",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/%2e%2e/secret.pdf",
            "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/%2Fsecret.pdf"
    })
    void rejectsUrisOutsideNarrowPublicDocumentScope(String candidate) {
        assertThatThrownBy(() -> ChizhikPublicCatalogDocumentUriPolicy.requireAllowed(URI.create(candidate)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordsReachablePdfWithoutReadingABody() {
        var transport = new FakeTransport(new ChizhikPublicCatalogDocumentResponse(200, "Application/PDF; charset=binary", null));
        var probe = new ChizhikPublicCatalogDocumentProbe(transport, CLOCK, 3);

        var observation = probe.probe(DOCUMENT);

        assertThat(observation.status()).isEqualTo(ChizhikPublicCatalogDocumentStatus.REACHABLE_PDF);
        assertThat(observation.uri()).isEqualTo(DOCUMENT);
        assertThat(observation.path()).isEqualTo("/media/backendprod-dpro/catalog/pdf_file/catalog.pdf");
        assertThat(observation.httpStatus()).isEqualTo(200);
        assertThat(observation.contentType()).isEqualTo("application/pdf");
        assertThat(observation.observedAt()).isEqualTo(OBSERVED_AT);
        assertThat(transport.requested()).containsExactly(DOCUMENT);
    }

    @Test
    void keepsNonPdfAndHttpFailuresTruthful() {
        var nonPdf = new ChizhikPublicCatalogDocumentProbe(
                new FakeTransport(new ChizhikPublicCatalogDocumentResponse(200, "text/html", null)), CLOCK, 3)
                .probe(DOCUMENT);
        assertThat(nonPdf.status()).isEqualTo(ChizhikPublicCatalogDocumentStatus.UNEXPECTED_CONTENT_TYPE);
        assertThat(nonPdf.contentType()).isEqualTo("text/html");

        var unavailable = new ChizhikPublicCatalogDocumentProbe(
                new FakeTransport(new ChizhikPublicCatalogDocumentResponse(403, "text/html", null)), CLOCK, 3)
                .probe(DOCUMENT);
        assertThat(unavailable.status()).isEqualTo(ChizhikPublicCatalogDocumentStatus.HTTP_UNAVAILABLE);
        assertThat(unavailable.httpStatus()).isEqualTo(403);
    }

    @Test
    void followsOnlyBoundedRedirectsWithinTheSamePublicScope() {
        var redirected = URI.create(
                "https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/redirected.pdf");
        var transport = new FakeTransport(
                new ChizhikPublicCatalogDocumentResponse(302, "", redirected),
                new ChizhikPublicCatalogDocumentResponse(200, "application/pdf", null));

        var observation = new ChizhikPublicCatalogDocumentProbe(transport, CLOCK, 3).probe(DOCUMENT);

        assertThat(observation.status()).isEqualTo(ChizhikPublicCatalogDocumentStatus.REACHABLE_PDF);
        assertThat(observation.uri()).isEqualTo(redirected);
        assertThat(transport.requested()).containsExactly(DOCUMENT, redirected);
    }

    @Test
    void rejectsRedirectThatLeavesOfficialPublicScopeBeforeFollowingIt() {
        var transport = new FakeTransport(new ChizhikPublicCatalogDocumentResponse(
                302,
                "",
                URI.create("https://example.com/catalog.pdf")));

        assertThatThrownBy(() -> new ChizhikPublicCatalogDocumentProbe(transport, CLOCK, 3).probe(DOCUMENT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(transport.requested()).containsExactly(DOCUMENT);
    }

    @Test
    void stopsAfterConfiguredRedirectLimit() {
        var next = URI.create("https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/next.pdf");
        var transport = new FakeTransport(
                new ChizhikPublicCatalogDocumentResponse(302, "", next),
                new ChizhikPublicCatalogDocumentResponse(302, "", DOCUMENT));

        var observation = new ChizhikPublicCatalogDocumentProbe(transport, CLOCK, 1).probe(DOCUMENT);

        assertThat(observation.status()).isEqualTo(ChizhikPublicCatalogDocumentStatus.REDIRECT_LIMIT_EXCEEDED);
        assertThat(transport.requested()).containsExactly(DOCUMENT, next);
    }

    private static final class FakeTransport implements ChizhikPublicCatalogDocumentTransport {
        private final Deque<ChizhikPublicCatalogDocumentResponse> responses = new ArrayDeque<>();
        private final List<URI> requested = new ArrayList<>();

        private FakeTransport(ChizhikPublicCatalogDocumentResponse... responses) {
            this.responses.addAll(List.of(responses));
        }

        @Override
        public ChizhikPublicCatalogDocumentResponse head(URI uri) {
            requested.add(uri);
            return responses.removeFirst();
        }

        private List<URI> requested() {
            return List.copyOf(requested);
        }
    }
}
