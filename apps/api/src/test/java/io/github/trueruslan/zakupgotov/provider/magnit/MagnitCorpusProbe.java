package io.github.trueruslan.zakupgotov.provider.magnit;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

final class MagnitCorpusProbe {

    private static final String ORIGIN = "https://magnit.ru";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(8);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(12);
    private static final int NEAR_SKU_WINDOW = 900;
    private static final int PRICE_BOUND_PROMO_WINDOW = 160;
    private static final String ARTICLE_MARKER = "Артикул ";
    private static final Map<String, String> REQUEST_HEADERS = Map.of(
            "Accept", "text/html,application/xhtml+xml",
            "User-Agent", "ZakupGotov-M0B-PublicPageProbe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)");
    private static final Pattern PRICE = Pattern.compile("(\\d{1,6}(?:[.,]\\d{1,2})?)\\s*₽");
    private static final Pattern DISCOUNT = Pattern.compile("-?\\d{1,2}%");
    private static final Pattern SCRIPT_OR_STYLE = Pattern.compile(
            "(?is)<(?:script|style)\\b[^>]*>.*?</(?:script|style)>");

    private static final List<CorpusItem> FIXED_CORPUS = List.of(
            new CorpusItem(
                    "milk",
                    "1000013732",
                    "1000013732-moloko_selo_zelenoe_ultrapasterizovannoe_2_5_950ml"),
            new CorpusItem("eggs", "2047000014", "2047000014-yaytso_kurinoe_stolovoe_so_10sht"),
            new CorpusItem(
                    "bread",
                    "1000134831",
                    "1000134831-khleb_borodinskiy_form_narez_rzhan_pshen_0_45kg_khlebzavod_6"),
            new CorpusItem("bananas", "9072651501", "9072651501-banany"),
            new CorpusItem("potatoes", "9072651210", "9072651210-kartofel_1kg"),
            new CorpusItem("onions", "9072651204", "9072651204-luk_repchatyy"),
            new CorpusItem("tomatoes", "3412070012", "3412070012-tomaty"),
            new CorpusItem("cucumbers", "3412110001", "3412110001-ogurtsy_gladkie"),
            new CorpusItem(
                    "chicken",
                    "1000233459",
                    "1000233459-grudka_tsb_okhl_lotok_1_kg_v_lotok_ooo_soyuzptitseprom_5"),
            new CorpusItem("beef/mince", "1000289907", "1000289907-farsh_eatmeat_govyazhiy_400g"),
            new CorpusItem("rice", "3152910003", "3152910003-ris-kruglyy-fasovannyy-800g"),
            new CorpusItem(
                    "buckwheat",
                    "3152910002",
                    "3152910002-krupa_grechnevaya_1_sort_fasovannaya_800g"),
            new CorpusItem("pasta", "1000166929", "1000166929-makarony_magnit_spagetti_500g"),
            new CorpusItem(
                    "sunflower oil",
                    "1000029331",
                    "1000029331-maslo_podsolnechnoe_rafinirovannoe_dezodorirovannoe_900ml"),
            new CorpusItem(
                    "butter",
                    "1855599922",
                    "1855599922-magnit_maslo_traditsionnoe_v_s_82_5_gost_180g_12"),
            new CorpusItem("cheese", "1000500641", "1000500641-landkaas_syr_gauda_45"),
            new CorpusItem(
                    "kefir",
                    "1000330180",
                    "1000330180-td_smetanin_kefir_1_800g_finpak_ooo_kubanrus_moloko_12"),
            new CorpusItem("sugar", "3133780401", "3133780401-sakhar_belyy_kristallicheskiy_fas_1kg"),
            new CorpusItem("salt", "3367460002", "3367460002-sol_povarennaya_pishchevaya_1kg"),
            new CorpusItem("tea", "1000534756", "1000534756-beseda_chay_chernyy_otbornyy_klassicheskiy_100pak_200g"));

    private final HttpClient httpClient;

    private MagnitCorpusProbe(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    static MagnitCorpusProbe create() {
        return new MagnitCorpusProbe(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    static List<CorpusItem> fixedCorpus() {
        return FIXED_CORPUS;
    }

    URI productUri(CorpusItem item, String shopCode) {
        if (shopCode == null || shopCode.isBlank()) {
            throw new IllegalArgumentException("shopCode must not be blank");
        }
        return URI.create(ORIGIN + "/product/" + item.productSlug() + "?shopCode=" + shopCode + "&shopType=1");
    }

    CorpusResult runFixedCorpus(String firstShop, String secondShop) throws IOException, InterruptedException {
        var firstHttp2xx = 0;
        var secondHttp2xx = 0;
        var firstUsable = 0;
        var secondUsable = 0;
        var stableIdentity = 0;
        var knownAvailability = 0;
        var promoObservations = 0;
        var nearSkuMultiplePriceObservations = 0;
        var nearSkuPromoMarkerObservations = 0;
        var priceBoundPromoMarkerObservations = 0;
        var failedRequirements = new ArrayList<String>();
        var packageExtractions = new ArrayList<MagnitPackageQuantityExtraction>();

        for (var item : FIXED_CORPUS) {
            var first = observe(item, firstShop);
            var second = observe(item, secondShop);

            if (first.http2xx()) {
                firstHttp2xx++;
            }
            if (second.http2xx()) {
                secondHttp2xx++;
            }
            if (first.usable()) {
                firstUsable++;
            }
            if (second.usable()) {
                secondUsable++;
            }
            if (first.packageEvidenceEligible()) {
                packageExtractions.add(first.observation().packageExtraction());
            }
            if (second.packageEvidenceEligible()) {
                packageExtractions.add(second.observation().packageExtraction());
            }
            if (first.http2xx() && second.http2xx()
                    && first.observation().skuEvidence()
                    && second.observation().skuEvidence()) {
                stableIdentity++;
            }
            if (first.observation().availability() != Availability.UNKNOWN) {
                knownAvailability++;
            }
            if (second.observation().availability() != Availability.UNKNOWN) {
                knownAvailability++;
            }
            if (first.observation().promo()) {
                promoObservations++;
            }
            if (second.observation().promo()) {
                promoObservations++;
            }
            if (first.rawShape().multiplePriceCandidates()) {
                nearSkuMultiplePriceObservations++;
            }
            if (second.rawShape().multiplePriceCandidates()) {
                nearSkuMultiplePriceObservations++;
            }
            if (first.rawShape().promoMarker()) {
                nearSkuPromoMarkerObservations++;
            }
            if (second.rawShape().promoMarker()) {
                nearSkuPromoMarkerObservations++;
            }
            if (first.priceBoundPromo().promoMarker()) {
                priceBoundPromoMarkerObservations++;
            }
            if (second.priceBoundPromo().promoMarker()) {
                priceBoundPromoMarkerObservations++;
            }
            if (!first.usable() || !second.usable()) {
                failedRequirements.add(item.requirement());
            }
        }

        return new CorpusResult(
                FIXED_CORPUS.size(),
                FIXED_CORPUS.size() * 2,
                firstHttp2xx,
                secondHttp2xx,
                firstUsable,
                secondUsable,
                stableIdentity,
                knownAvailability,
                promoObservations,
                nearSkuMultiplePriceObservations,
                nearSkuPromoMarkerObservations,
                priceBoundPromoMarkerObservations,
                PackageEvidenceSummary.summarize(packageExtractions),
                List.copyOf(failedRequirements));
    }

    private HttpObservation observe(CorpusItem item, String shopCode) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder(productUri(item, shopCode))
                .GET()
                .timeout(REQUEST_TIMEOUT);
        REQUEST_HEADERS.forEach(requestBuilder::header);

        var response = httpClient.send(
                requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        var observation = parseProductPage(response.body(), item.sku());
        var rawShape = inspectNearSkuRawShape(response.body(), item.sku());
        var priceBoundPromo = inspectPriceBoundPromoShape(response.body(), item.sku());
        return new HttpObservation(response.statusCode(), observation, rawShape, priceBoundPromo);
    }

    static PageObservation parseProductPage(String html, String expectedSku) {
        if (expectedSku == null || expectedSku.isBlank()) {
            throw new IllegalArgumentException("expectedSku must not be blank");
        }

        var cleanedHtml = SCRIPT_OR_STYLE.matcher(html == null ? "" : html).replaceAll(" ");
        var lowerHtml = cleanedHtml.toLowerCase(Locale.ROOT);
        var h1Start = lowerHtml.indexOf("<h1");
        if (h1Start < 0) {
            return PageObservation.missingSku();
        }

        var skuIndex = cleanedHtml.indexOf(expectedSku, h1Start);
        if (skuIndex < 0) {
            return PageObservation.missingSku();
        }

        var scope = cleanedHtml.substring(h1Start, skuIndex + expectedSku.length());
        var text = visibleText(scope);
        var lowerText = text.toLowerCase(Locale.ROOT);
        var prices = distinctPrices(text);
        var currentPrice = prices.isEmpty()
                ? MagnitPublicPageProbe.closestPriceToSku(html, expectedSku)
                : Optional.of(prices.getFirst());

        Optional<BigDecimal> regularPrice = Optional.empty();
        var renderedPromoMarker = lowerText.contains("финальная цена") || DISCOUNT.matcher(text).find();
        if (renderedPromoMarker && prices.size() >= 2 && prices.get(1).compareTo(prices.getFirst()) > 0) {
            regularPrice = Optional.of(prices.get(1));
        }
        var promo = renderedPromoMarker
                || (currentPrice.isPresent() && inspectPriceBoundPromoShape(html, expectedSku).promoMarker());

        var availability = Availability.UNKNOWN;
        if (lowerText.contains("нет в наличии")) {
            availability = Availability.UNAVAILABLE;
        } else if (lowerText.contains("добавить в корзину") || lowerText.contains("в корзину")) {
            availability = Availability.AVAILABLE;
        }

        return new PageObservation(
                true,
                currentPrice,
                regularPrice,
                promo,
                availability,
                MagnitJsonLdPackageQuantityExtractor.extract(html, expectedSku));
    }

    static RawShapeEvidence inspectNearSkuRawShape(String html, String expectedSku) {
        if (expectedSku == null || expectedSku.isBlank()) {
            throw new IllegalArgumentException("expectedSku must not be blank");
        }

        var text = visibleText(html);
        var multiplePriceCandidates = false;
        var promoMarker = false;
        var searchFrom = 0;
        var skuIndex = text.indexOf(expectedSku, searchFrom);

        while (skuIndex >= 0) {
            var windowStart = Math.max(0, skuIndex - NEAR_SKU_WINDOW);
            var currentArticleMarker = text.lastIndexOf(ARTICLE_MARKER, skuIndex);
            var previousArticleMarker = currentArticleMarker >= 0
                    ? text.lastIndexOf(ARTICLE_MARKER, currentArticleMarker - 1)
                    : -1;
            if (previousArticleMarker >= windowStart) {
                windowStart = previousArticleMarker + ARTICLE_MARKER.length();
            }

            var scope = text.substring(windowStart, skuIndex);
            var prices = distinctPrices(scope);
            multiplePriceCandidates |= prices.size() >= 2;
            var lowerScope = scope.toLowerCase(Locale.ROOT);
            promoMarker |= lowerScope.contains("финальная цена") || DISCOUNT.matcher(scope).find();

            searchFrom = skuIndex + expectedSku.length();
            skuIndex = text.indexOf(expectedSku, searchFrom);
        }

        return new RawShapeEvidence(multiplePriceCandidates, promoMarker);
    }

    static PriceBoundPromoEvidence inspectPriceBoundPromoShape(String html, String expectedSku) {
        if (expectedSku == null || expectedSku.isBlank()) {
            throw new IllegalArgumentException("expectedSku must not be blank");
        }

        var text = visibleText(html);
        var bestDistance = Integer.MAX_VALUE;
        String bestScope = null;
        var searchFrom = 0;
        var skuIndex = text.indexOf(expectedSku, searchFrom);

        while (skuIndex >= 0) {
            var matcher = PRICE.matcher(text);
            var priceStart = -1;
            var priceEnd = -1;
            while (matcher.find() && matcher.end() <= skuIndex) {
                priceStart = matcher.start();
                priceEnd = matcher.end();
            }

            if (priceEnd >= 0) {
                var distance = skuIndex - priceEnd;
                if (distance < bestDistance) {
                    var scopeStart = Math.max(0, priceStart - PRICE_BOUND_PROMO_WINDOW);
                    var currentArticleMarker = text.lastIndexOf(ARTICLE_MARKER, skuIndex);
                    var previousArticleMarker = currentArticleMarker >= 0
                            ? text.lastIndexOf(ARTICLE_MARKER, currentArticleMarker - 1)
                            : -1;
                    if (previousArticleMarker >= scopeStart) {
                        scopeStart = previousArticleMarker + ARTICLE_MARKER.length();
                    }

                    bestDistance = distance;
                    bestScope = text.substring(scopeStart, skuIndex);
                }
            }

            searchFrom = skuIndex + expectedSku.length();
            skuIndex = text.indexOf(expectedSku, searchFrom);
        }

        if (bestScope == null) {
            return new PriceBoundPromoEvidence(false);
        }

        var lowerScope = bestScope.toLowerCase(Locale.ROOT);
        var promoMarker = lowerScope.contains("финальная цена") || DISCOUNT.matcher(bestScope).find();
        return new PriceBoundPromoEvidence(promoMarker);
    }

    private static List<BigDecimal> distinctPrices(String text) {
        var prices = new ArrayList<BigDecimal>();
        var seen = new LinkedHashSet<BigDecimal>();
        var matcher = PRICE.matcher(text);
        while (matcher.find()) {
            var price = new BigDecimal(matcher.group(1).replace(',', '.'));
            if (seen.add(price)) {
                prices.add(price);
            }
        }
        return prices;
    }

    private static String visibleText(String html) {
        return (html == null ? "" : html)
                .replace("&nbsp;", " ")
                .replace("&#8381;", "₽")
                .replace("&#x20bd;", "₽")
                .replace("&#x20BD;", "₽")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    enum Availability {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN
    }

    record CorpusItem(String requirement, String sku, String productSlug) {
        CorpusItem {
            if (requirement == null || requirement.isBlank()) {
                throw new IllegalArgumentException("requirement must not be blank");
            }
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("sku must not be blank");
            }
            if (productSlug == null || !productSlug.startsWith(sku + "-")) {
                throw new IllegalArgumentException("productSlug must start with sku");
            }
        }
    }

    record PageObservation(
            boolean skuEvidence,
            Optional<BigDecimal> currentPrice,
            Optional<BigDecimal> regularPrice,
            boolean promo,
            Availability availability,
            MagnitPackageQuantityExtraction packageExtraction) {

        PageObservation {
            packageExtraction = Objects.requireNonNull(packageExtraction, "packageExtraction must not be null");
        }

        static PageObservation missingSku() {
            return new PageObservation(
                    false,
                    Optional.empty(),
                    Optional.empty(),
                    false,
                    Availability.UNKNOWN,
                    MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.MISSING));
        }
    }

    record RawShapeEvidence(boolean multiplePriceCandidates, boolean promoMarker) {}

    record PriceBoundPromoEvidence(boolean promoMarker) {}

    private record HttpObservation(
            int status,
            PageObservation observation,
            RawShapeEvidence rawShape,
            PriceBoundPromoEvidence priceBoundPromo) {
        boolean http2xx() {
            return status >= 200 && status < 300;
        }

        boolean usable() {
            return http2xx() && observation.skuEvidence() && observation.currentPrice().isPresent();
        }

        boolean packageEvidenceEligible() {
            return http2xx() && observation.skuEvidence();
        }
    }

    record PackageEvidenceSummary(
            int packageEvidencePages,
            int found,
            int missing,
            int ambiguousDimensions,
            int conflictingValues,
            int invalidValues) {

        PackageEvidenceSummary {
            if (packageEvidencePages < 0
                    || found < 0
                    || missing < 0
                    || ambiguousDimensions < 0
                    || conflictingValues < 0
                    || invalidValues < 0) {
                throw new IllegalArgumentException("package evidence counts must not be negative");
            }
            if (classifiedPages(found, missing, ambiguousDimensions, conflictingValues, invalidValues)
                    != packageEvidencePages) {
                throw new IllegalArgumentException("package evidence counts must equal eligible pages");
            }
        }

        static PackageEvidenceSummary summarize(List<MagnitPackageQuantityExtraction> extractions) {
            var input = Objects.requireNonNull(extractions, "extractions must not be null");
            var found = 0;
            var missing = 0;
            var ambiguousDimensions = 0;
            var conflictingValues = 0;
            var invalidValues = 0;

            for (var extraction : input) {
                Objects.requireNonNull(extraction, "extraction must not be null");
                switch (extraction.status()) {
                    case FOUND -> found++;
                    case MISSING -> missing++;
                    case AMBIGUOUS_DIMENSIONS -> ambiguousDimensions++;
                    case CONFLICTING_VALUES -> conflictingValues++;
                    case INVALID_VALUE -> invalidValues++;
                }
            }

            return new PackageEvidenceSummary(
                    input.size(),
                    found,
                    missing,
                    ambiguousDimensions,
                    conflictingValues,
                    invalidValues);
        }

        int classifiedPages() {
            return classifiedPages(found, missing, ambiguousDimensions, conflictingValues, invalidValues);
        }

        private static int classifiedPages(
                int found,
                int missing,
                int ambiguousDimensions,
                int conflictingValues,
                int invalidValues) {
            return found + missing + ambiguousDimensions + conflictingValues + invalidValues;
        }
    }

    record CorpusResult(
            int totalRequirements,
            int totalRequests,
            int firstHttp2xx,
            int secondHttp2xx,
            int firstUsable,
            int secondUsable,
            int stableIdentity,
            int knownAvailability,
            int promoObservations,
            int nearSkuMultiplePriceObservations,
            int nearSkuPromoMarkerObservations,
            int priceBoundPromoMarkerObservations,
            PackageEvidenceSummary packageEvidence,
            List<String> failedRequirements) {

        CorpusResult {
            packageEvidence = Objects.requireNonNull(packageEvidence, "packageEvidence must not be null");
        }

        String toEvidenceLine() {
            return "MAGNIT_PHASE_B"
                    + " total_requirements=" + totalRequirements
                    + " total_requests=" + totalRequests
                    + " first_http_2xx=" + firstHttp2xx
                    + " second_http_2xx=" + secondHttp2xx
                    + " first_usable=" + firstUsable
                    + " second_usable=" + secondUsable
                    + " stable_identity=" + stableIdentity
                    + " known_availability=" + knownAvailability
                    + " promo_observations=" + promoObservations
                    + " near_sku_multi_price=" + nearSkuMultiplePriceObservations
                    + " near_sku_promo_marker=" + nearSkuPromoMarkerObservations
                    + " price_bound_promo_marker=" + priceBoundPromoMarkerObservations
                    + " package_evidence_pages=" + packageEvidence.packageEvidencePages()
                    + " package_found=" + packageEvidence.found()
                    + " package_missing=" + packageEvidence.missing()
                    + " package_ambiguous_dimensions=" + packageEvidence.ambiguousDimensions()
                    + " package_conflicting_values=" + packageEvidence.conflictingValues()
                    + " package_invalid_values=" + packageEvidence.invalidValues()
                    + " failed_count=" + failedRequirements.size()
                    + " failed_requirements=" + String.join(",", failedRequirements);
        }
    }
}
