package io.github.trueruslan.zakupgotov.provider.perekrestok;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
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

final class PerekrestokPlainHttpProbe {

    private static final String ORIGIN = "https://www.perekrestok.ru";
    private static final String API_BASE = ORIGIN + "/api/customer/1.4.1.0";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final Map<String, String> REQUEST_HEADERS = Map.of(
            "Accept", "application/json",
            "User-Agent", "ZakupGotov-M0B-PlainHttpProbe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)");
    private static final Pattern SHOP_ID = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");
    private static final Pattern PLU = Pattern.compile("\\\"plu\\\"\\s*:\\s*\\\"?([^\\\",}]+)\\\"?");
    private static final Pattern PRICE = Pattern.compile(
            "\\\"priceTag\\\"\\s*:\\s*\\{[^}]*\\\"price\\\"\\s*:\\s*(\\d+)",
            Pattern.DOTALL);

    private final CookieManager cookieManager;
    private final HttpClient httpClient;

    private PerekrestokPlainHttpProbe(CookieManager cookieManager, HttpClient httpClient) {
        this.cookieManager = cookieManager;
        this.httpClient = httpClient;
    }

    static PerekrestokPlainHttpProbe create() {
        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        var client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        return new PerekrestokPlainHttpProbe(cookies, client);
    }

    Map<String, String> requestHeaders() {
        return REQUEST_HEADERS;
    }

    URI warmupUri() {
        return URI.create(ORIGIN + "/");
    }

    URI nearbyStoresUri(double longitude, double latitude) {
        return URI.create(API_BASE + "/shop"
                + "?orderBy=distance&orderDirection=asc&page=1&perPage=3"
                + "&lat=" + latitude + "&lng=" + longitude);
    }

    URI selectPickupUri(String shopId) {
        if (shopId == null || shopId.isBlank()) {
            throw new IllegalArgumentException("shopId must not be blank");
        }
        return URI.create(API_BASE + "/delivery/mode/pickup/" + shopId);
    }

    URI searchUri(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        var encoded = URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create(API_BASE + "/catalog/search/all?textQuery=" + encoded + "&entityTypes%5B%5D=product");
    }

    PhaseAResult runPhaseA(double longitude, double latitude, String query) throws IOException, InterruptedException {
        var warmup = get(warmupUri());
        var sessionCookiePresent = cookieManager.getCookieStore().getCookies().stream()
                .anyMatch(cookie -> cookie.getName().equalsIgnoreCase("session"));

        var stores = get(nearbyStoresUri(longitude, latitude));
        if (!isSuccess(stores.statusCode())) {
            return PhaseAResult.storeGateFailed(warmup.statusCode(), sessionCookiePresent, stores.statusCode());
        }

        var shopId = firstMatch(SHOP_ID, stores.body());
        if (shopId.isEmpty()) {
            return PhaseAResult.storeShapeFailed(warmup.statusCode(), sessionCookiePresent, stores.statusCode());
        }

        var selection = put(selectPickupUri(shopId.get()));
        if (!isSuccess(selection.statusCode())) {
            return PhaseAResult.selectionGateFailed(
                    warmup.statusCode(), sessionCookiePresent, stores.statusCode(), shopId.get(), selection.statusCode());
        }

        var search = get(searchUri(query));
        if (!isSuccess(search.statusCode())) {
            return PhaseAResult.searchGateFailed(
                    warmup.statusCode(), sessionCookiePresent, stores.statusCode(), shopId.get(),
                    selection.statusCode(), search.statusCode());
        }

        var plu = firstMatch(PLU, search.body()).orElse("");
        var priceEvidence = PRICE.matcher(search.body() == null ? "" : search.body()).find();
        return new PhaseAResult(
                warmup.statusCode(),
                sessionCookiePresent,
                stores.statusCode(),
                shopId.get(),
                selection.statusCode(),
                search.statusCode(),
                plu,
                priceEvidence);
    }

    private HttpResponse<String> get(URI uri) throws IOException, InterruptedException {
        return send(HttpRequest.newBuilder(uri).GET());
    }

    private HttpResponse<String> put(URI uri) throws IOException, InterruptedException {
        return send(HttpRequest.newBuilder(uri).PUT(HttpRequest.BodyPublishers.noBody()));
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) throws IOException, InterruptedException {
        builder.timeout(REQUEST_TIMEOUT);
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
            int warmupStatus,
            boolean sessionCookiePresent,
            int storeStatus,
            String shopId,
            int selectionStatus,
            int searchStatus,
            String productPlu,
            boolean priceEvidence) {

        static PhaseAResult storeGateFailed(int warmupStatus, boolean session, int storeStatus) {
            return new PhaseAResult(warmupStatus, session, storeStatus, "", -1, -1, "", false);
        }

        static PhaseAResult storeShapeFailed(int warmupStatus, boolean session, int storeStatus) {
            return new PhaseAResult(warmupStatus, session, storeStatus, "", -1, -1, "", false);
        }

        static PhaseAResult selectionGateFailed(
                int warmupStatus, boolean session, int storeStatus, String shopId, int selectionStatus) {
            return new PhaseAResult(warmupStatus, session, storeStatus, shopId, selectionStatus, -1, "", false);
        }

        static PhaseAResult searchGateFailed(
                int warmupStatus,
                boolean session,
                int storeStatus,
                String shopId,
                int selectionStatus,
                int searchStatus) {
            return new PhaseAResult(
                    warmupStatus, session, storeStatus, shopId, selectionStatus, searchStatus, "", false);
        }

        String toEvidenceLine() {
            return "PEREKRESTOK_PHASE_A"
                    + " warmup_status=" + warmupStatus
                    + " session_cookie_present=" + sessionCookiePresent
                    + " store_status=" + storeStatus
                    + " shop_id_present=" + !shopId.isBlank()
                    + " selection_status=" + selectionStatus
                    + " search_status=" + searchStatus
                    + " plu_present=" + !productPlu.isBlank()
                    + " price_evidence=" + priceEvidence;
        }
    }
}
