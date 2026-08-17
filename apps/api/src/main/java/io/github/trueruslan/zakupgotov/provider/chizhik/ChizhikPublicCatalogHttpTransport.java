package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class ChizhikPublicCatalogHttpTransport implements ChizhikPublicCatalogDocumentTransport {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient client;

    private ChizhikPublicCatalogHttpTransport(HttpClient client) {
        this.client = client;
    }

    static ChizhikPublicCatalogHttpTransport create() {
        return new ChizhikPublicCatalogHttpTransport(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    @Override
    public ChizhikPublicCatalogDocumentResponse head(URI uri) {
        var request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            var contentType = response.headers().firstValue("Content-Type").orElse("");
            var redirectLocation = response.headers()
                    .firstValue("Location")
                    .map(URI::create)
                    .orElse(null);
            return new ChizhikPublicCatalogDocumentResponse(
                    response.statusCode(),
                    contentType,
                    redirectLocation);
        } catch (IOException exception) {
            throw new ChizhikPublicCatalogTransportException(
                    ChizhikPublicCatalogTransportException.classify(exception), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ChizhikPublicCatalogTransportException(
                    ChizhikPublicCatalogTransportFailureKind.INTERRUPTED, exception);
        }
    }
}
