package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.net.URI;

record ChizhikPublicCatalogDocumentResponse(
        int statusCode,
        String contentType,
        URI redirectLocation) {

    ChizhikPublicCatalogDocumentResponse {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("statusCode must be an HTTP status");
        }
        contentType = contentType == null ? "" : contentType.trim();
    }
}
