package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.net.URI;
import java.time.Clock;
import java.util.Locale;
import java.util.Objects;

/** Explicit opt-in probe for public Chizhik catalog documents. */
public final class ChizhikPublicCatalogDocumentProbe {

    private static final int DEFAULT_MAX_REDIRECTS = 3;

    private final ChizhikPublicCatalogDocumentTransport transport;
    private final Clock clock;
    private final int maxRedirects;

    ChizhikPublicCatalogDocumentProbe(
            ChizhikPublicCatalogDocumentTransport transport,
            Clock clock,
            int maxRedirects) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (maxRedirects < 0) {
            throw new IllegalArgumentException("maxRedirects must be non-negative");
        }
        this.maxRedirects = maxRedirects;
    }

    public static ChizhikPublicCatalogDocumentProbe live() {
        return new ChizhikPublicCatalogDocumentProbe(
                ChizhikPublicCatalogHttpTransport.create(),
                Clock.systemUTC(),
                DEFAULT_MAX_REDIRECTS);
    }

    public ChizhikPublicCatalogDocumentObservation probe(URI candidate) {
        var current = ChizhikPublicCatalogDocumentUriPolicy.requireAllowed(candidate);
        var redirectsFollowed = 0;

        while (true) {
            var response = Objects.requireNonNull(transport.head(current), "transport response");
            var contentType = normalizeContentType(response.contentType());

            if (isRedirect(response.statusCode()) && response.redirectLocation() != null) {
                if (redirectsFollowed >= maxRedirects) {
                    return observation(
                            ChizhikPublicCatalogDocumentStatus.REDIRECT_LIMIT_EXCEEDED,
                            current,
                            response,
                            contentType);
                }
                var target = current.resolve(response.redirectLocation());
                current = ChizhikPublicCatalogDocumentUriPolicy.requireAllowed(target);
                redirectsFollowed++;
                continue;
            }

            if (response.statusCode() != 200) {
                return observation(
                        ChizhikPublicCatalogDocumentStatus.HTTP_UNAVAILABLE,
                        current,
                        response,
                        contentType);
            }
            if (!"application/pdf".equals(contentType)) {
                return observation(
                        ChizhikPublicCatalogDocumentStatus.UNEXPECTED_CONTENT_TYPE,
                        current,
                        response,
                        contentType);
            }
            return observation(
                    ChizhikPublicCatalogDocumentStatus.REACHABLE_PDF,
                    current,
                    response,
                    contentType);
        }
    }

    private ChizhikPublicCatalogDocumentObservation observation(
            ChizhikPublicCatalogDocumentStatus status,
            URI uri,
            ChizhikPublicCatalogDocumentResponse response,
            String contentType) {
        return new ChizhikPublicCatalogDocumentObservation(
                status,
                uri,
                uri.getPath(),
                response.statusCode(),
                contentType,
                clock.instant());
    }

    private static boolean isRedirect(int statusCode) {
        return statusCode >= 300 && statusCode <= 399;
    }

    private static String normalizeContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            return "";
        }
        var separator = rawContentType.indexOf(';');
        var mediaType = separator >= 0 ? rawContentType.substring(0, separator) : rawContentType;
        return mediaType.trim().toLowerCase(Locale.ROOT);
    }
}
