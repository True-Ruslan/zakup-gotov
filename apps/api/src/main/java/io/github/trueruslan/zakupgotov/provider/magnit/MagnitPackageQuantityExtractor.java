package io.github.trueruslan.zakupgotov.provider.magnit;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class MagnitPackageQuantityExtractor {

    private static final String CHARACTERISTICS = "Характеристики";
    private static final String WEIGHT_LABEL = "Вес, кг";
    private static final String VOLUME_LABEL = "Объем, л";
    private static final List<String> SECTION_END_MARKERS = List.of(
            "Условия хранения",
            "Документы",
            "Отзывы",
            "Дополнительная информация");
    private static final Pattern SCRIPT_OR_STYLE = Pattern.compile(
            "(?is)<(?:script|style)\\b[^>]*>.*?</(?:script|style)>");
    private static final Pattern WEIGHT = characteristicPattern(WEIGHT_LABEL);
    private static final Pattern VOLUME = characteristicPattern(VOLUME_LABEL);

    private MagnitPackageQuantityExtractor() {}

    public static MagnitPackageQuantityExtraction extract(String html) {
        var section = characteristicsSection(html);
        if (section == null) {
            return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.MISSING);
        }

        var weight = parseDimension(section, WEIGHT_LABEL, WEIGHT, QuantityUnit.KILOGRAM);
        var volume = parseDimension(section, VOLUME_LABEL, VOLUME, QuantityUnit.LITER);

        if (weight.invalid() || volume.invalid()) {
            return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.INVALID_VALUE);
        }
        if (weight.quantities().size() > 1 || volume.quantities().size() > 1) {
            return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.CONFLICTING_VALUES);
        }
        if (weight.present() && volume.present()) {
            return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.AMBIGUOUS_DIMENSIONS);
        }
        if (weight.quantities().size() == 1) {
            return MagnitPackageQuantityExtraction.found(weight.quantities().iterator().next());
        }
        if (volume.quantities().size() == 1) {
            return MagnitPackageQuantityExtraction.found(volume.quantities().iterator().next());
        }
        return MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.MISSING);
    }

    private static DimensionEvidence parseDimension(
            String section,
            String label,
            Pattern pattern,
            QuantityUnit unit) {
        var matcher = pattern.matcher(section);
        var present = false;
        var invalid = false;
        Set<Quantity> quantities = new LinkedHashSet<>();
        while (matcher.find()) {
            present = true;
            var raw = matcher.group(1).replace(',', '.');
            try {
                var amount = new BigDecimal(raw);
                if (amount.signum() <= 0) {
                    invalid = true;
                    continue;
                }
                quantities.add(new Quantity(amount, unit));
            } catch (IllegalArgumentException exception) {
                invalid = true;
            }
        }

        if (!present && containsLabel(section, label)) {
            invalid = true;
            present = true;
        }
        return new DimensionEvidence(present, invalid, Set.copyOf(quantities));
    }

    private static String characteristicsSection(String html) {
        var visible = visibleText(html);
        var start = visible.indexOf(CHARACTERISTICS);
        if (start < 0) {
            return null;
        }
        start += CHARACTERISTICS.length();

        var end = visible.length();
        for (var marker : SECTION_END_MARKERS) {
            var candidate = visible.indexOf(marker, start);
            if (candidate >= 0 && candidate < end) {
                end = candidate;
            }
        }
        return visible.substring(start, end).trim();
    }

    private static Pattern characteristicPattern(String label) {
        return Pattern.compile(
                "(?iu)(?:^|\\s)" + Pattern.quote(label) + "\\s*:?[\\s]+([^\\s]+)");
    }

    private static boolean containsLabel(String section, String label) {
        return Pattern.compile("(?iu)(?:^|\\s)" + Pattern.quote(label) + "(?:\\s|:|$)")
                .matcher(section)
                .find();
    }

    private static String visibleText(String html) {
        var withoutScripts = SCRIPT_OR_STYLE.matcher(html == null ? "" : html).replaceAll(" ");
        return withoutScripts
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&#xA0;", " ")
                .replace("&#xa0;", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record DimensionEvidence(boolean present, boolean invalid, Set<Quantity> quantities) {}
}
