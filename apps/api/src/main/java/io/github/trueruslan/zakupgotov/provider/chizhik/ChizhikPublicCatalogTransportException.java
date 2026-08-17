package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.Objects;
import javax.net.ssl.SSLException;

final class ChizhikPublicCatalogTransportException extends IllegalStateException {

    private final ChizhikPublicCatalogTransportFailureKind failureKind;

    ChizhikPublicCatalogTransportException(
            ChizhikPublicCatalogTransportFailureKind failureKind,
            Throwable cause) {
        super("Chizhik public catalog transport failed", cause);
        this.failureKind = Objects.requireNonNull(failureKind, "failureKind");
    }

    ChizhikPublicCatalogTransportFailureKind failureKind() {
        return failureKind;
    }

    static ChizhikPublicCatalogTransportFailureKind classify(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable");

        for (var current = throwable; current != null; current = current.getCause()) {
            if (current instanceof UnknownHostException) {
                return ChizhikPublicCatalogTransportFailureKind.DNS;
            }
            if (current instanceof HttpTimeoutException) {
                return ChizhikPublicCatalogTransportFailureKind.TIMEOUT;
            }
            if (current instanceof SSLException) {
                return ChizhikPublicCatalogTransportFailureKind.TLS;
            }
            if (current instanceof ConnectException) {
                return ChizhikPublicCatalogTransportFailureKind.CONNECT;
            }
            if (current instanceof InterruptedException) {
                return ChizhikPublicCatalogTransportFailureKind.INTERRUPTED;
            }
        }

        for (var current = throwable; current != null; current = current.getCause()) {
            if (current instanceof IOException) {
                return ChizhikPublicCatalogTransportFailureKind.IO;
            }
        }

        return ChizhikPublicCatalogTransportFailureKind.IO;
    }
}
