package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class MagnitStoreSearchLiveProbeTest {

    @Test
    void liveRequestBoundaryUsesNoSessionOrApplicationCredentials() {
        var probe = MagnitStoreSearchLiveProbe.create();

        assertThat(probe.hasCookieHandler()).isFalse();
        assertThat(probe.requestHeaders().keySet())
                .doesNotContainAnyElementsOf(Set.of(
                        "Cookie",
                        "Authorization",
                        "X-Api-Key",
                        "X-Auth-Token",
                        "x-client-name",
                        "x-device-platform",
                        "x-new-magnit"));
        assertThat(probe.requestHeaders()).containsOnlyKeys("Accept", "Content-Type");
    }

    @Test
    void sanitizedEvidenceContainsOnlyStatusesCountsAndBooleans() {
        var result = new MagnitStoreSearchLiveProbe.LiveResult(
                200,
                1,
                true,
                false,
                200,
                1,
                true,
                false,
                true,
                false,
                2);

        assertThat(result.toEvidenceLine())
                .isEqualTo("MAGNIT_SHOPCODE_LOCATION"
                        + " first_status=200"
                        + " first_candidates=1"
                        + " first_has_992301=true"
                        + " first_set_cookie=false"
                        + " second_status=200"
                        + " second_candidates=1"
                        + " second_has_992301=true"
                        + " second_set_cookie=false"
                        + " same_candidate_set=true"
                        + " conflicting_evidence=false"
                        + " total_requests=2")
                .doesNotContain("address")
                .doesNotContain("latitude")
                .doesNotContain("longitude")
                .doesNotContain("cookie_value")
                .doesNotContain("token");
    }

    @Test
    void mergedMainLiveGateRunsOnlyWhenExplicitlyEnabled() throws Exception {
        assumeTrue(Boolean.getBoolean("zakup.live.magnit.shopcode"));

        var result = MagnitStoreSearchLiveProbe.create().runKnownPublicBoundingBoxTwice();
        System.out.println(result.toEvidenceLine());

        assertThat(result.totalRequests()).isEqualTo(2);
        assertThat(result.firstStatus()).isBetween(200, 299);
        assertThat(result.secondStatus()).isBetween(200, 299);
        assertThat(result.firstHas992301()).isTrue();
        assertThat(result.secondHas992301()).isTrue();
        assertThat(result.sameCandidateSet()).isTrue();
        assertThat(result.conflictingEvidence()).isFalse();
    }
}
