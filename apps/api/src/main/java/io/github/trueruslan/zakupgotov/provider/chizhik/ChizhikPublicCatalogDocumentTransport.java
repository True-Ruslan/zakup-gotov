package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.net.URI;

interface ChizhikPublicCatalogDocumentTransport {
    ChizhikPublicCatalogDocumentResponse head(URI uri);
}
