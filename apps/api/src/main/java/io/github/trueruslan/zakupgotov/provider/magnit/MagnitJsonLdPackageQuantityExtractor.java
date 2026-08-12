package io.github.trueruslan.zakupgotov.provider.magnit;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class MagnitJsonLdPackageQuantityExtractor {

    private static final String JSON_LD_MEDIA_TYPE = "application/ld+json";
    private static final String PRODUCT_TYPE = "Product";
    private static final String VOLUME_LABEL = "Объем, л";
    private static final Pattern SCRIPT = Pattern.compile(
            "(?is)<script\\b([^>]*)>(.*?)</script\\s*>");
    private static final Pattern TYPE_ATTRIBUTE = Pattern.compile(
            "(?is)\\btype\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private MagnitJsonLdPackageQuantityExtractor() {}

    public static MagnitPackageQuantityExtraction extract(String html, String expectedSku) {
        if (expectedSku == null || expectedSku.isBlank()) {
            throw new IllegalArgumentException("expectedSku must not be blank");
        }

        var evidence = new Evidence();
        var matcher = SCRIPT.matcher(html == null ? "" : html);
        while (matcher.find()) {
            if (!isJsonLdScript(matcher.group(1))) {
                continue;
            }
            try {
                collect(JSON.readTree(matcher.group(2)), expectedSku.trim(), evidence);
            } catch (Exception ignored) {
                // Malformed JSON-LD is not repaired heuristically and cannot create evidence.
            }
        }
        return evidence.toExtraction();
    }

    private static boolean isJsonLdScript(String attributes) {
        var matcher = TYPE_ATTRIBUTE.matcher(attributes == null ? "" : attributes);
        if (!matcher.find()) {
            return false;
        }
        var type = matcher.group(1) != null
                ? matcher.group(1)
                : matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
        return JSON_LD_MEDIA_TYPE.equalsIgnoreCase(type.trim());
    }

    private static void collect(JsonNode node, String expectedSku, Evidence evidence) {
        if (node == null) {
            return;
        }
        if (node.getNodeType().name().equals("OBJECT") && isMatchingProduct(node, expectedSku)) {
            collectProduct(node, evidence);
        }
        if (node.getNodeType().name().equals("OBJECT") || node.getNodeType().name().equals("ARRAY")) {
            for (var child : node) {
                collect(child, expectedSku, evidence);
            }
        }
    }

    private static boolean isMatchingProduct(JsonNode node, String expectedSku) {
        return hasProductType(node.get("@type")) && expectedSku.equals(scalarText(node.get("sku")));
    }

    private static boolean hasProductType(JsonNode type) {
        if (type == null) {
            return false;
        }
        if (isString(type)) {
            return PRODUCT_TYPE.equals(type.asString());
        }
        if (type.getNodeType().name().equals("ARRAY")) {
            for (var candidate : type) {
                if (isString(candidate) && PRODUCT_TYPE.equals(candidate.asString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void collectProduct(JsonNode product, Evidence evidence) {
        var weight = product.get("weight");
        if (weight != null && isScalarNumberOrString(weight)) {
            evidence.addWeight(weight.asString());
        }

        var additionalProperty = product.get("additionalProperty");
        if (additionalProperty == null) {
            return;
        }
        if (additionalProperty.getNodeType().name().equals("ARRAY")) {
            for (var property : additionalProperty) {
                collectAdditionalProperty(property, evidence);
            }
        } else {
            collectAdditionalProperty(additionalProperty, evidence);
        }
    }

    private static void collectAdditionalProperty(JsonNode property, Evidence evidence) {
        if (property == null || !property.getNodeType().name().equals("OBJECT")) {
            return;
        }
        if (!VOLUME_LABEL.equals(scalarText(property.get("name")))) {
            return;
        }

        var value = property.get("value");
        if (value == null || !isScalarNumberOrString(value)) {
            evidence.invalid = true;
            return;
        }
        evidence.addVolume(value.asString());
    }

    private static String scalarText(JsonNode node) {
        return node != null && isScalarNumberOrString(node) ? node.asString().trim() : null;
    }

    private static boolean isScalarNumberOrString(JsonNode node) {
        var type = node.getNodeType().name();
        return type.equals("STRING") || type.equals("NUMBER");
    }

    private static boolean isString(JsonNode node) {
        return node.getNodeType().name().equals("STRING");
    }

    private static final class Evidence {
        private final Set<Quantity> weights = new LinkedHashSet<>();
        private final Set<Quantity> volumes = new LinkedHashSet<>();
        private boolean invalid;

        private void addWeight(String raw) {
            add(raw, QuantityUnit.KILOGRAM, weights);
        }

        private void addVolume(String raw) {
            add(raw, QuantityUnit.LITER, volumes);
        }

        private void add(String raw, QuantityUnit unit, Set<Quantity> target) {
            try {
                var amount = new BigDecimal(raw.trim().replace(',', '.'));
                if (amount.signum() <= 0) {
                    invalid = true;
                    return;
                }
                target.add(new Quantity(amount, unit));
            } catch (IllegalArgumentException exception) {
                invalid = true;
            }
        }

        private MagnitPackageQuantityExtraction toExtraction() {
            if (invalid) {
                return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.INVALID_VALUE);
            }
            if (weights.size() > 1 || volumes.size() > 1) {
                return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.CONFLICTING_VALUES);
            }
            if (!weights.isEmpty() && !volumes.isEmpty()) {
                return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.AMBIGUOUS_DIMENSIONS);
            }
            if (weights.size() == 1) {
                return MagnitPackageQuantityExtraction.found(weights.iterator().next());
            }
            if (volumes.size() == 1) {
                return MagnitPackageQuantityExtraction.found(volumes.iterator().next());
            }
            return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.MISSING);
        }
    }
}
