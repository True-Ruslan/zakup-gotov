package io.github.trueruslan.zakupgotov.provider.magnit;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class MagnitPublicPageProbe {

    private static final String ORIGIN = "https://magnit.ru";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final Map<String, String> REQUEST_HEADERS = Map.of(
            "Accept", "text/html,application/xhtml+xml",
            "User-Agent", "ZakupGotov-M0B-PublicPageProbe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)");
    private static final Pattern PRICE = Pattern.compile("(\\d{1,6}(?:[.,]\\d{1,2})?)\\s*₽");

    private final HttpClient httpClient;

    private MagnitPublicPageProbe(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    static MagnitPublicPageProbe create() {
        return new MagnitPublicPageProbe(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    Map<String, String> requestHeaders() {
        return REQUEST_HEADERS;
    }

    URI productUri(String productSlug, String shopCode) {
        if (productSlug == null || productSlug.isBlank()) {
            throw new IllegalArgumentException("productSlug must not be blank");
        }
        if (shopCode == null || shopCode.isBlank()) {
            throw new IllegalArgumentException("shopCode must not be blank");
        }
        return URI.create(ORIGIN + "/product/" + productSlug + "?shopCode=" + shopCode + "&shopType=1");
    }

    PhaseAResult runPhaseA(String productSlug, String expectedSku, String firstShop, String secondShop)
            throws IOException, InterruptedException {
        if (expectedSku == null || expectedSku.isBlank()) {
            throw new IllegalArgumentException("expectedSku must not be blank");
        }

        var first = get(productUri(productSlug, firstShop));
        var firstEvidence = PageEvidence.from(first, expectedSku);
        if (!firstEvidence.complete()) {
            return PhaseAResult.firstGateFailed(firstEvidence);
        }

        var second = get(productUri(productSlug, secondShop));
        var secondEvidence = PageEvidence.from(second, expectedSku);
        if (!secondEvidence.complete()) {
            return PhaseAResult.secondGateFailed(firstEvidence, secondEvidence);
        }

        return new PhaseAResult(
                firstEvidence.status(),
                firstEvidence.skuEvidence(),
                true,
                secondEvidence.status(),
                secondEvidence.skuEvidence(),
                true,
                firstEvidence.price().orElseThrow().compareTo(secondEvidence.price().orElseThrow()) == 0);
    }

    private HttpResponse<String> get(URI uri) throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(REQUEST_TIMEOUT);
        REQUEST_HEADERS.forEach(builder::header);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String visibleText(String html) {
        return (html == null ? "" : html)
                .replace("&nbsp;", " ")
                .replace("&#8381;", "₽")
                .replace("&#x20bd;", "₽")
                .replace("&#x20BD;", "₽")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ");
    }

    private record PageEvidence(int status, boolean skuEvidence, Optional<BigDecimal> price) {

        static PageEvidence from(HttpResponse<String> response, String expectedSku) {
            var body = response.body() == null ? "" : response.body();
            var text = visibleText(body);
            var priceMatcher = PRICE.matcher(text);
            Optional<BigDecimal> price = priceMatcher.find()
                    ? Optional.of(new BigDecimal(priceMatcher.group(1).replace(',', '.')))
                    : Optional.empty();
            return new PageEvidence(response.statusCode(), body.contains(expectedSku), price);
        }

        boolean complete() {
            return status >= 200 && status < 300 && skuEvidence && price.isPresent();
        }
    }

    record PhaseAResult(
            int firstStatus,
            boolean firstSkuEvidence,
            boolean firstPricePresent,
            int secondStatus,
            boolean secondSkuEvidence,
            boolean secondPricePresent,
            boolean pricesEqual) {

        static PhaseAResult firstGateFailed(PageEvidence first) {
            return new PhaseAResult(
                    first.status(), first.skuEvidence(), first.price().isPresent(), -1, false, false, false);
        }

        static PhaseAResult secondGateFailed(PageEvidence first, PageEvidence second) {
            return new PhaseAResult(
                    first.status(),
                    first.skuEvidence(),
                    first.price().isPresent(),
                    second.status(),
                    second.skuEvidence(),
                    second.price().isPresent(),
                    false);
        }

        String toEvidenceLine() {
            return "MAGNIT_PHASE_A"
                    + " first_status=" + firstStatus
                    + " first_sku_evidence=" + firstSkuEvidence
                    + " first_price_present=" + firstPricePresent
                    + " second_status=" + secondStatus
                    + " second_sku_evidence=" + secondSkuEvidence
                    + " second_price_present=" + secondPricePresent
                    + " prices_equal=" + pricesEqual;
        }
    }
}
