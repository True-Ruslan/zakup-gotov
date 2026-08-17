package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

public final class ChizhikPublicCatalogDocumentUriPolicy {

    private static final String HOST = "media.chizhik.club";
    private static final String PATH_PREFIX = "/media/backendprod-dpro/catalog/pdf_file/";

    private ChizhikPublicCatalogDocumentUriPolicy() {}

    public static URI requireAllowed(URI candidate) {
        Objects.requireNonNull(candidate, "candidate");

        if (!"https".equalsIgnoreCase(candidate.getScheme())) {
            throw rejected();
        }
        if (candidate.getHost() == null || !HOST.equalsIgnoreCase(candidate.getHost())) {
            throw rejected();
        }
        if (candidate.getPort() != -1 && candidate.getPort() != 443) {
            throw rejected();
        }
        if (candidate.getRawUserInfo() != null || candidate.getRawQuery() != null || candidate.getRawFragment() != null) {
            throw rejected();
        }

        var rawPath = candidate.getRawPath();
        if (rawPath == null || !rawPath.startsWith(PATH_PREFIX)) {
            throw rejected();
        }

        var normalizedPath = candidate.normalize().getRawPath();
        if (!rawPath.equals(normalizedPath)) {
            throw rejected();
        }

        var lowerPath = rawPath.toLowerCase(Locale.ROOT);
        var fileName = rawPath.substring(PATH_PREFIX.length());
        var lowerFileName = fileName.toLowerCase(Locale.ROOT);
        if (fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")
                || lowerFileName.contains("%2e")
                || lowerFileName.contains("%2f")
                || lowerFileName.contains("%5c")
                || !lowerPath.endsWith(".pdf")) {
            throw rejected();
        }

        return candidate;
    }

    private static IllegalArgumentException rejected() {
        return new IllegalArgumentException("URI is outside the allowed Chizhik public catalog document scope");
    }
}
