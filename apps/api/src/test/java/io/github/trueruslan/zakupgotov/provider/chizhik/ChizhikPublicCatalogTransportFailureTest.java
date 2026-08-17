package io.github.trueruslan.zakupgotov.provider.chizhik;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.jupiter.api.Test;

class ChizhikPublicCatalogTransportFailureTest {

    @Test
    void classifiesFiniteSanitizedTransportFailureKindsAcrossCauseChains() {
        assertThat(ChizhikPublicCatalogTransportException.classify(new UnknownHostException("private-host")))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.DNS);
        assertThat(ChizhikPublicCatalogTransportException.classify(new ConnectException("private-address")))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.CONNECT);
        assertThat(ChizhikPublicCatalogTransportException.classify(new HttpTimeoutException("private-uri")))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.TIMEOUT);
        assertThat(ChizhikPublicCatalogTransportException.classify(new SSLHandshakeException("private-tls")))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.TLS);
        assertThat(ChizhikPublicCatalogTransportException.classify(new IOException("private-io")))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.IO);
        assertThat(ChizhikPublicCatalogTransportException.classify(new InterruptedException("private-interrupt")))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.INTERRUPTED);
        assertThat(ChizhikPublicCatalogTransportException.classify(
                        new IllegalStateException("wrapper", new UnknownHostException("private-host"))))
                .isEqualTo(ChizhikPublicCatalogTransportFailureKind.DNS);
    }
}
