package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class MagnitStoreSearchResponseParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private MagnitStoreSearchResponseParser() {}

    public static MagnitStoreSearchEvidence parse(String json) {
        try {
            var root = JSON.readTree(json == null ? "" : json);
            var items = root == null ? null : root.path("items").path("items");
            if (items == null || !"ARRAY".equals(items.getNodeType().name())) {
                return MagnitStoreSearchEvidence.empty();
            }

            Map<String, MagnitGeoPoint> byCode = new HashMap<>();
            for (var item : items) {
                var candidate = parseCandidate(item);
                if (candidate == null) {
                    continue;
                }
                var previous = byCode.putIfAbsent(candidate.shopCode(), candidate.coordinates());
                if (previous != null && !previous.equals(candidate.coordinates())) {
                    return MagnitStoreSearchEvidence.conflict();
                }
            }

            var candidates = new ArrayList<MagnitStoreCandidate>();
            byCode.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> candidates.add(new MagnitStoreCandidate(entry.getKey(), entry.getValue())));
            return new MagnitStoreSearchEvidence(candidates, false);
        } catch (Exception ignored) {
            return MagnitStoreSearchEvidence.empty();
        }
    }

    private static MagnitStoreCandidate parseCandidate(JsonNode item) {
        if (item == null || !"OBJECT".equals(item.getNodeType().name())) {
            return null;
        }

        var code = scalarText(item.path("externalId").path("storeCode"));
        var latitude = scalarDouble(item.path("coordinates").path("latitude"));
        var longitude = scalarDouble(item.path("coordinates").path("longitude"));
        if (code == null || code.isBlank() || latitude == null || longitude == null) {
            return null;
        }

        try {
            return new MagnitStoreCandidate(code, new MagnitGeoPoint(latitude, longitude));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String scalarText(JsonNode node) {
        if (node == null) {
            return null;
        }
        var type = node.getNodeType().name();
        if (!"STRING".equals(type) && !"NUMBER".equals(type)) {
            return null;
        }
        var value = node.asString().trim();
        return value.isEmpty() ? null : value;
    }

    private static Double scalarDouble(JsonNode node) {
        var value = scalarText(node);
        if (value == null) {
            return null;
        }
        try {
            var parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
