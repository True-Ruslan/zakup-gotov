package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class FreshnessEvidenceTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T06:30:00Z");
    private static final Instant PROVIDER_UPDATED_AT = Instant.parse("2026-08-12T06:10:00Z");

    @Test
    void observationOnlyFreshnessDoesNotInventProviderUpdateTime() {
        var freshness = FreshnessEvidence.observationOnly(OBSERVED_AT);

        assertThat(freshness.observedAt()).isEqualTo(OBSERVED_AT);
        assertThat(freshness.providerUpdatedAt()).isEmpty();
        assertThat(freshness.basis()).isEqualTo(FreshnessBasis.OBSERVATION_ONLY);
    }

    @Test
    void providerTimestampFreshnessKeepsProviderTimeDistinctFromObservationTime() {
        var freshness = FreshnessEvidence.providerUpdatedAt(OBSERVED_AT, PROVIDER_UPDATED_AT);

        assertThat(freshness.observedAt()).isEqualTo(OBSERVED_AT);
        assertThat(freshness.providerUpdatedAt()).contains(PROVIDER_UPDATED_AT);
        assertThat(freshness.basis()).isEqualTo(FreshnessBasis.PROVIDER_UPDATED_AT);
    }

    @Test
    void allowsProviderTimestampEqualToObservationTime() {
        var freshness = FreshnessEvidence.providerUpdatedAt(OBSERVED_AT, OBSERVED_AT);

        assertThat(freshness.providerUpdatedAt()).contains(OBSERVED_AT);
    }

    @Test
    void rejectsProviderTimestampAfterObservationTime() {
        var futureProviderTime = Instant.parse("2026-08-12T06:30:01Z");

        assertThatThrownBy(() -> FreshnessEvidence.providerUpdatedAt(OBSERVED_AT, futureProviderTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("providerUpdatedAt")
                .hasMessageContaining("observedAt");
    }

    @Test
    void rejectsMissingTimestamps() {
        assertThatThrownBy(() -> FreshnessEvidence.observationOnly(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("observedAt");
        assertThatThrownBy(() -> FreshnessEvidence.providerUpdatedAt(OBSERVED_AT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("providerUpdatedAt");
    }
}
