package io.github.trueruslan.zakupgotov.provider.pyaterochka;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class PyaterochkaPlainHttpProbe {

    private static final String API_BASE = "https://5d.5ka.ru/api";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final Map<String, String> REQUEST_HEADERS = Map.of(
            "Accept", "application/json",
            "User-Agent", "ZakupGotov-M0B-PlainHttpProbe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)");
    private static final Pattern SAP_CODE = Pattern.compile("\\\"sapCode\\\"\\s*:\\s*\\\"?([^\\\",}]+)\\\"?");
    private static final Pattern PLU = Pattern.compile("\\\"plu\\\"\\s*:\\s*\\\"?([^\\\",}]+)\\\"?");
    private static final Pattern PRICE_FIELD = Pattern.compile("\\\"[^\\\"]*price[^\\\"]*\\\"\\s*:", Pattern.CASE_INSENSITIVE);

    private final HttpClient httpClient;

    private PyaterochkaPlainHttpProbe(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    static PyaterochkaPlainHttpProbe create() {
        return new PyaterochkaPlainHttpProbe(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    Map<String, String> requestHeaders() {
        return REQUEST_HEADERS;
    }

    URI storeLookupUri(double longitude, double latitude) {
        return URI.create(API_BASE + "/orders/v1/orders/stores/?lon=" + longitude + "&lat=" + latitude);
    }

    URI searchUri(String sapCode, String query, int limit) {
        if (sapCode == null || sapCode.isBlank()) {
            throw new IllegalArgumentException("sapCode must not be blank");
        }
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        if (limit < 1 || limit > 20) {
            throw new IllegalArgumentException("limit must be between 1 and 20");
        }
        var encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create(API_BASE + "/catalog/v3/stores/" + sapCode + "/search"
                + "?mode=store&include_restrict=true&q=" + encodedQuery + "&limit=" + limit);
    }

    PhaseAResult runPhaseA(double longitude, double latitude, String query) throws IOException, InterruptedException {
        var storeResponse = get(storeLookupUri(longitude, latitude));
        if (!isSuccess(storeResponse.statusCode())) {
            return PhaseAResult.storeGateFailed(storeResponse.statusCode());
        }

        var sapCode = firstMatch(SAP_CODE, storeResponse.body());
        if (sapCode.isEmpty()) {
            return PhaseAResult.storeShapeFailed(storeResponse.statusCode());
        }

        var searchResponse = get(searchUri(sapCode.get(), query, 3));
        if (!isSuccess(searchResponse.statusCode())) {
            return PhaseAResult.searchGateFailed(storeResponse.statusCode(), sapCode.get(), searchResponse.statusCode());
        }

        var plu = firstMatch(PLU, searchResponse.body());
        var priceEvidence = PRICE_FIELD.matcher(searchResponse.body()).find();
        return new PhaseAResult(
                storeResponse.statusCode(),
                sapCode.get(),
                searchResponse.statusCode(),
                plu.orElse(""),
                priceEvidence);
    }

    private HttpResponse<String> get(URI uri) throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(REQUEST_TIMEOUT);
        REQUEST_HEADERS.forEach(builder::header);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }

    private static Optional<String> firstMatch(Pattern pattern, String body) {
        var matcher = pattern.matcher(body == null ? "" : body);
        return matcher.find() ? Optional.of(matcher.group(1).trim()) : Optional.empty();
    }

    record PhaseAResult(
            int storeLookupStatus,
            String sapCode,
            int searchStatus,
            String productPlu,
            boolean priceEvidence) {

        static PhaseAResult storeGateFailed(int status) {
            return new PhaseAResult(status, "", -1, "", false);
        }

        static PhaseAResult storeShapeFailed(int status) {
            return new PhaseAResult(status, "", -1, "", false);
        }

        static PhaseAResult searchGateFailed(int storeStatus, String sapCode, int searchStatus) {
            return new PhaseAResult(storeStatus, sapCode, searchStatus, "", false);
        }

        String toEvidenceLine() {
            return "PYATEROCHKA_PHASE_A"
                    + " store_status=" + storeLookupStatus
                    + " sap_code_present=" + !sapCode.isBlank()
                    + " search_status=" + searchStatus
                    + " plu_present=" + !productPlu.isBlank()
                    + " price_evidence=" + priceEvidence;
        }
    }
}
