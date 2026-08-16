package io.github.trueruslan.zakupgotov.provider.chizhik;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

final class ChizhikPlainHttpProbe {

    private static final String API_BASE = "https://app.chizhik.club/api";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final Map<String, String> REQUEST_HEADERS = Map.of(
            "Accept", "application/json",
            "User-Agent", "ZakupGotov-M0B-PlainHttpProbe/0.1 (+https://github.com/True-Ruslan/zakup-gotov)");

    private static final Pattern ID_FIELD = Pattern.compile("\\\"id\\\"\\s*:\\s*\\d+");
    private static final Pattern CATEGORY_NAME_FIELD = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"[^\\\"]+\\\"");
    private static final Pattern PLU_FIELD = Pattern.compile("\\\"plu\\\"\\s*:\\s*\\\"[^\\\"]+\\\"");
    private static final Pattern TITLE_FIELD = Pattern.compile("\\\"title\\\"\\s*:\\s*\\\"[^\\\"]+\\\"");
    private static final Pattern PRICE_FIELD = Pattern.compile("\\\"price\\\"\\s*:\\s*-?\\d+(?:\\.\\d+)?");

    private final Transport transport;

    private ChizhikPlainHttpProbe(Transport transport) {
        this.transport = transport;
    }

    static ChizhikPlainHttpProbe create() {
        var client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        return new ChizhikPlainHttpProbe(uri -> {
            var builder = HttpRequest.newBuilder(uri)
                    .GET()
                    .timeout(REQUEST_TIMEOUT);
            REQUEST_HEADERS.forEach(builder::header);
            var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new ProbeResponse(response.statusCode(), response.body());
        });
    }

    static ChizhikPlainHttpProbe forTransport(Transport transport) {
        if (transport == null) {
            throw new IllegalArgumentException("transport must not be null");
        }
        return new ChizhikPlainHttpProbe(transport);
    }

    Map<String, String> requestHeaders() {
        return REQUEST_HEADERS;
    }

    URI categoriesUri() {
        return URI.create(API_BASE + "/v1/catalog/unauthorized/categories/");
    }

    URI searchUri(String query, int page) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be positive");
        }
        var encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create(API_BASE + "/v1/catalog/unauthorized/products/?page=" + page + "&term=" + encodedQuery);
    }

    PhaseAResult runPhaseA(String query) throws InterruptedException {
        var categories = safeGet(categoriesUri());
        if (categories.outcome() != Outcome.ACCESSIBLE) {
            return PhaseAResult.categoriesGateFailed(categories);
        }

        var categoriesShape = hasCategoryShape(categories.body());
        if (!categoriesShape) {
            return PhaseAResult.categoriesShapeFailed(categories);
        }

        var search = safeGet(searchUri(query, 1));
        if (search.outcome() != Outcome.ACCESSIBLE) {
            return PhaseAResult.searchGateFailed(categories, categoriesShape, search);
        }

        var body = search.body();
        return new PhaseAResult(
                categories.outcome(),
                categories.status(),
                categoriesShape,
                search.outcome(),
                search.status(),
                ID_FIELD.matcher(body).find(),
                PLU_FIELD.matcher(body).find(),
                TITLE_FIELD.matcher(body).find(),
                PRICE_FIELD.matcher(body).find());
    }

    private ProbeAttempt safeGet(URI uri) throws InterruptedException {
        try {
            var response = transport.get(uri);
            return new ProbeAttempt(classifyStatus(response.status()), response.status(), body(response.body()));
        } catch (HttpTimeoutException exception) {
            return new ProbeAttempt(Outcome.TIMEOUT, -1, "");
        } catch (IOException exception) {
            return new ProbeAttempt(Outcome.NETWORK_ERROR, -1, "");
        }
    }

    static Outcome classifyStatus(int status) {
        if (status >= 200 && status < 300) {
            return Outcome.ACCESSIBLE;
        }
        if (status >= 300 && status < 400) {
            return Outcome.REDIRECTED;
        }
        if (status == 401 || status == 403) {
            return Outcome.BLOCKED;
        }
        if (status == 429) {
            return Outcome.RATE_LIMITED;
        }
        return Outcome.HTTP_ERROR;
    }

    private static boolean hasCategoryShape(String body) {
        return ID_FIELD.matcher(body).find() && CATEGORY_NAME_FIELD.matcher(body).find();
    }

    private static String body(String body) {
        return body == null ? "" : body;
    }

    enum Outcome {
        ACCESSIBLE,
        REDIRECTED,
        BLOCKED,
        RATE_LIMITED,
        HTTP_ERROR,
        TIMEOUT,
        NETWORK_ERROR,
        NOT_ATTEMPTED
    }

    @FunctionalInterface
    interface Transport {
        ProbeResponse get(URI uri) throws IOException, InterruptedException;
    }

    record ProbeResponse(int status, String body) {}

    private record ProbeAttempt(Outcome outcome, int status, String body) {}

    record PhaseAResult(
            Outcome categoriesOutcome,
            int categoriesStatus,
            boolean categoriesShape,
            Outcome searchOutcome,
            int searchStatus,
            boolean productIdPresent,
            boolean productPluPresent,
            boolean productTitlePresent,
            boolean priceEvidence) {

        static PhaseAResult categoriesGateFailed(ProbeAttempt categories) {
            return new PhaseAResult(
                    categories.outcome(),
                    categories.status(),
                    false,
                    Outcome.NOT_ATTEMPTED,
                    -1,
                    false,
                    false,
                    false,
                    false);
        }

        static PhaseAResult categoriesShapeFailed(ProbeAttempt categories) {
            return new PhaseAResult(
                    categories.outcome(),
                    categories.status(),
                    false,
                    Outcome.NOT_ATTEMPTED,
                    -1,
                    false,
                    false,
                    false,
                    false);
        }

        static PhaseAResult searchGateFailed(ProbeAttempt categories, boolean categoriesShape, ProbeAttempt search) {
            return new PhaseAResult(
                    categories.outcome(),
                    categories.status(),
                    categoriesShape,
                    search.outcome(),
                    search.status(),
                    false,
                    false,
                    false,
                    false);
        }

        String toEvidenceLine() {
            return "CHIZHIK_PHASE_A"
                    + " categories_outcome=" + categoriesOutcome
                    + " categories_status=" + categoriesStatus
                    + " categories_shape=" + categoriesShape
                    + " search_outcome=" + searchOutcome
                    + " search_status=" + searchStatus
                    + " product_id_present=" + productIdPresent
                    + " product_plu_present=" + productPluPresent
                    + " product_title_present=" + productTitlePresent
                    + " price_evidence=" + priceEvidence;
        }
    }
}
