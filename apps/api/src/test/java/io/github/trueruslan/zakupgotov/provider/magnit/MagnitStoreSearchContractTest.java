package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class MagnitStoreSearchContractTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void buildsTheProvenPublicBoundingBoxRequestWithoutUserAddressData() throws Exception {
        var box = new MagnitGeoBoundingBox(
                new MagnitGeoPoint(45.069, 38.967),
                new MagnitGeoPoint(45.065, 38.980));

        var request = MagnitStoreSearchRequest.forBoundingBox(box);
        var tree = JSON.valueToTree(request);

        assertThat(tree.at("/filters/geo/typeName").asString()).isEqualTo("box");
        assertThat(tree.at("/filters/geo/leftTopPoint/latitude").asDouble()).isEqualTo(45.069);
        assertThat(tree.at("/filters/geo/leftTopPoint/longitude").asDouble()).isEqualTo(38.967);
        assertThat(tree.at("/filters/geo/rightBottomPoint/latitude").asDouble()).isEqualTo(45.065);
        assertThat(tree.at("/filters/geo/rightBottomPoint/longitude").asDouble()).isEqualTo(38.980);
        assertThat(tree.at("/filters/storeTypeListV2"))
                .extracting(node -> node.asString())
                .containsExactly("MM", "GM", "DG", "MO", "ME", "MC", "DARKSTORE", "MM_MINI", "ZARYAD");

        var serialized = JSON.writeValueAsString(request);
        assertThat(serialized)
                .doesNotContainIgnoringCase("address")
                .doesNotContainIgnoringCase("locality")
                .doesNotContainIgnoringCase("cookie")
                .doesNotContainIgnoringCase("token");
    }

    @Test
    void storeTypeContractIsImmutableAndOrdered() {
        assertThat(MagnitStoreSearchRequest.storeTypes())
                .containsExactly("MM", "GM", "DG", "MO", "ME", "MC", "DARKSTORE", "MM_MINI", "ZARYAD");
        assertThatThrownBy(() -> MagnitStoreSearchRequest.storeTypes().add("OTHER"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void geoPointRejectsNonFiniteAndOutOfRangeCoordinates() {
        for (var latitude : List.of(Double.NaN, Double.POSITIVE_INFINITY, -90.0001, 90.0001)) {
            assertThatThrownBy(() -> new MagnitGeoPoint(latitude, 37.6))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("latitude");
        }
        for (var longitude : List.of(Double.NaN, Double.NEGATIVE_INFINITY, -180.0001, 180.0001)) {
            assertThatThrownBy(() -> new MagnitGeoPoint(55.7, longitude))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("longitude");
        }
    }

    @Test
    void boundingBoxRejectsDegenerateOrInvertedGeometry() {
        assertThatThrownBy(() -> new MagnitGeoBoundingBox(
                        new MagnitGeoPoint(55.0, 37.0),
                        new MagnitGeoPoint(55.0, 38.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitude");

        assertThatThrownBy(() -> new MagnitGeoBoundingBox(
                        new MagnitGeoPoint(54.0, 37.0),
                        new MagnitGeoPoint(55.0, 38.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitude");

        assertThatThrownBy(() -> new MagnitGeoBoundingBox(
                        new MagnitGeoPoint(55.0, 38.0),
                        new MagnitGeoPoint(54.0, 38.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitude");

        assertThatThrownBy(() -> new MagnitGeoBoundingBox(
                        new MagnitGeoPoint(55.0, 39.0),
                        new MagnitGeoPoint(54.0, 38.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitude");
    }
}