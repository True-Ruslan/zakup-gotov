package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MagnitStoreSearchResponseParserTest {

    @Test
    void parsesOnlyShopCodeAndCoordinatesFromTheProvenResponseShape() {
        var result = MagnitStoreSearchResponseParser.parse("""
                {
                  "items": {
                    "items": [
                      {
                        "address": "must not be retained",
                        "name": "must not be retained",
                        "coordinates": {"latitude": 45.067057, "longitude": 38.973527},
                        "externalId": {"owner": "magnit", "storeCode": "992301"},
                        "storeTypeV2": "MM"
                      }
                    ]
                  }
                }
                """);

        assertThat(result.conflictingStoreEvidence()).isFalse();
        assertThat(result.candidates()).containsExactly(
                new MagnitStoreCandidate("992301", new MagnitGeoPoint(45.067057, 38.973527)));
        assertThat(result.toString())
                .doesNotContain("must not be retained")
                .doesNotContain("address")
                .doesNotContain("name");
    }

    @Test
    void acceptsNumericStoreCodeAndNumericStringsForCoordinates() {
        var result = MagnitStoreSearchResponseParser.parse("""
                {"items":{"items":[{
                  "coordinates":{"latitude":"55.750","longitude":"37.620"},
                  "externalId":{"storeCode":12345}
                }]}}
                """);

        assertThat(result.candidates()).containsExactly(
                new MagnitStoreCandidate("12345", new MagnitGeoPoint(55.750, 37.620)));
    }

    @Test
    void ignoresMalformedEntriesAndUnrelatedJson() {
        var result = MagnitStoreSearchResponseParser.parse("""
                {
                  "storeCode": "outside-proven-shape",
                  "coordinates": {"latitude": 1, "longitude": 2},
                  "items": {
                    "items": [
                      {"externalId":{"storeCode":"missing-coordinates"}},
                      {"coordinates":{"latitude":55.7,"longitude":37.6}},
                      {"coordinates":{"latitude":999,"longitude":37.6},"externalId":{"storeCode":"bad-lat"}},
                      {"coordinates":{"latitude":55.7,"longitude":"unknown"},"externalId":{"storeCode":"bad-lon"}},
                      {"coordinates":{"latitude":55.7,"longitude":37.6},"externalId":{"storeCode":"   "}},
                      null,
                      42
                    ]
                  }
                }
                """);

        assertThat(result.candidates()).isEmpty();
        assertThat(result.conflictingStoreEvidence()).isFalse();
    }

    @Test
    void deduplicatesEquivalentRepeatedStoreEvidence() {
        var result = MagnitStoreSearchResponseParser.parse("""
                {"items":{"items":[
                  {"coordinates":{"latitude":45.067057,"longitude":38.973527},"externalId":{"storeCode":"992301"}},
                  {"coordinates":{"latitude":45.067057,"longitude":38.973527},"externalId":{"storeCode":"992301"}}
                ]}}
                """);

        assertThat(result.candidates()).containsExactly(
                new MagnitStoreCandidate("992301", new MagnitGeoPoint(45.067057, 38.973527)));
        assertThat(result.conflictingStoreEvidence()).isFalse();
    }

    @Test
    void failsClosedWhenOneStoreCodeHasConflictingCoordinates() {
        var result = MagnitStoreSearchResponseParser.parse("""
                {"items":{"items":[
                  {"coordinates":{"latitude":45.067057,"longitude":38.973527},"externalId":{"storeCode":"992301"}},
                  {"coordinates":{"latitude":45.067100,"longitude":38.973527},"externalId":{"storeCode":"992301"}}
                ]}}
                """);

        assertThat(result.candidates()).isEmpty();
        assertThat(result.conflictingStoreEvidence()).isTrue();
    }

    @Test
    void malformedOrStructurallyUnexpectedJsonCreatesNoStoreEvidence() {
        for (var payload : new String[] {
            "not-json",
            "{}",
            "{\"items\":null}",
            "{\"items\":{\"items\":{\"storeCode\":\"992301\"}}}"
        }) {
            var result = MagnitStoreSearchResponseParser.parse(payload);
            assertThat(result.candidates()).as(payload).isEmpty();
            assertThat(result.conflictingStoreEvidence()).as(payload).isFalse();
        }
    }
}