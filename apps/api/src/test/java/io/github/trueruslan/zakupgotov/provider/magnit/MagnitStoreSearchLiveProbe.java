package io.github.trueruslan.zakupgotov.provider.magnit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import tools.jackson.databind.json.JsonMapper;

final class MagnitStoreSearchLiveProbe {

    private static final URI ENDPOINT = URI.create("https://magnit.ru/webgate/v1/stores-facade/search");
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String EXPECTED_SHOP_CODE = "992301";
    private static final Map<String, String> REQUEST_HEADERS = Map.of(
            "Accept", "application/json",
            "Content-Type", "application/json");
    private static final MagnitGeoBoundingBox KNOWN_PUBLIC_BOUNDING_BOX = new MagnitGeoBoundingBox(
            new MagnitGeoPoint(45.069, 38.967),
            new MagnitGeoPoint(45.065, 38.980));
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient httpClient;

    private MagnitStoreSearchLiveProbe(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    static MagnitStoreSearchLiveProbe create() {
        return new MagnitStoreSearchLiveProbe(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    boolean hasCookieHandler() {
        return httpClient.cookieHandler().isPresent();
    }

    Map<String, String> requestHeaders() {
        return REQUEST_HEADERS;
    }

    LiveResult runKnownPublicBoundingBoxTwice() throws IOException, InterruptedException {
        var requestBody = JSON.writeValueAsString(MagnitStoreSearchRequest.forBoundingBox(KNOWN_PUBLIC_BOUNDING_BOX));
        var first = post(requestBody);
        var second = post(requestBody);

        var firstCodes = shopCodes(first.evidence());
        var secondCodes = shopCodes(second.evidence());
        return new LiveResult(
                first.status(),
                firstCodes.size(),
                firstCodes.contains(EXPECTED_SHOP_CODE),
                first.setCookiePresent(),
                second.status(),
                secondCodes.size(),
                secondCodes.contains(EXPECTED_SHOP_CODE),
                second.setCookiePresent(),
                firstCodes.equals(secondCodes),
                first.evidence().conflictingStoreEvidence() || second.evidence().conflictingStoreEvidence(),
                2);
    }

    private Attempt post(String requestBody) throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder(ENDPOINT)
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
        REQUEST_HEADERS.forEach(builder::header);
        var response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Attempt(
                response.statusCode(),
                response.headers().firstValue("Set-Cookie").isPresent(),
                MagnitStoreSearchResponseParser.parse(response.body()));
    }

    private static Set<String> shopCodes(MagnitStoreSearchEvidence evidence) {
        var codes = new TreeSet<String>();
        evidence.candidates().forEach(candidate -> codes.add(candidate.shopCode()));
        return Set.copyOf(codes);
    }

    private record Attempt(int status, boolean setCookiePresent, MagnitStoreSearchEvidence evidence) {}

    record LiveResult(
            int firstStatus,
            int firstCandidates,
            boolean firstHas992301,
            boolean firstSetCookie,
            int secondStatus,
            int secondCandidates,
            boolean secondHas992301,
            boolean secondSetCookie,
            boolean sameCandidateSet,
            boolean conflictingEvidence,
            int totalRequests) {

        String toEvidenceLine() {
            return "MAGNIT_SHOPCODE_LOCATION"
                    + " first_status=" + firstStatus
                    + " first_candidates=" + firstCandidates
                    + " first_has_992301=" + firstHas992301
                    + " first_set_cookie=" + firstSetCookie
                    + " second_status=" + secondStatus
                    + " second_candidates=" + secondCandidates
                    + " second_has_992301=" + secondHas992301
                    + " second_set_cookie=" + secondSetCookie
                    + " same_candidate_set=" + sameCandidateSet
                    + " conflicting_evidence=" + conflictingEvidence
                    + " total_requests=" + totalRequests;
        }
    }
}
