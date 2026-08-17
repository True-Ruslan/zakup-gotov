package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

public record ChizhikPublicCatalogDocumentObservation(
        ChizhikPublicCatalogDocumentStatus status,
        URI uri,
        String path,
        int httpStatus,
        String contentType,
        Instant observedAt) {

    public ChizhikPublicCatalogDocumentObservation {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(observedAt, "observedAt");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        if (httpStatus < 100 || httpStatus > 599) {
            throw new IllegalArgumentException("httpStatus must be an HTTP status");
        }
    }
}
