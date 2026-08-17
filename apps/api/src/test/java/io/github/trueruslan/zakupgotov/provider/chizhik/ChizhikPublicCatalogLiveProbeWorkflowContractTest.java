package io.github.trueruslan.zakupgotov.provider.chizhik;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ChizhikPublicCatalogLiveProbeWorkflowContractTest {

    private static final String WORKFLOW = readWorkflow();

    @Test
    void routesPhaseCThroughOptInProviderProbeMechanism() {
        assertThat(WORKFLOW).contains("  chizhik_catalog:");
        assertThat(WORKFLOW).contains("github.event.issue.number == 163");
        assertThat(WORKFLOW).contains("github.actor == 'True-Ruslan'");
        assertThat(WORKFLOW).contains("github.event.comment.body == '/provider-probe chizhik-catalog'");
        assertThat(WORKFLOW).contains("-Dtest=ChizhikPublicCatalogDocumentLiveProbeTest");
        assertThat(WORKFLOW).contains("-Dzakup.live.chizhik.catalog=true");
        assertThat(WORKFLOW).contains("CHIZHIK_PHASE_C");
        assertThat(WORKFLOW).contains("Provider Live Probe / Chizhik Phase C / ${outcome}");
    }

    @Test
    void keepsThePhaseCCandidateAndPermissionsNarrow() {
        assertThat(WORKFLOW).contains(
                "-Dzakup.live.chizhik.catalog.url=https://media.chizhik.club/media/backendprod-dpro/catalog/pdf_file/");
        assertThat(WORKFLOW).contains("contents: read");
        assertThat(WORKFLOW).contains("statuses: write");
        assertThat(WORKFLOW).doesNotContain("secrets:");
        assertThat(WORKFLOW).doesNotContain("Authorization");
        assertThat(WORKFLOW).doesNotContain("Cookie");
    }

    @Test
    void parsesStatusWithoutConfusingItWithHttpStatus() {
        assertThat(WORKFLOW).contains(
                "status=\"$(sed -n 's/^CHIZHIK_PHASE_C status=\\([^ ]*\\).*/\\1/p' <<<\"$evidence\")\"");
        assertThat(WORKFLOW).contains(
                "http_status=\"$(sed -n 's/.* http_status=\\([-0-9]*\\).*/\\1/p' <<<\"$evidence\")\"");
    }

    private static String readWorkflow() {
        Path workflow = Path.of("..", "..", ".github", "workflows", "provider-live-probe-chizhik-catalog.yml").normalize();
        try {
            return Files.readString(workflow);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Chizhik Phase C provider live probe workflow", exception);
        }
    }
}
