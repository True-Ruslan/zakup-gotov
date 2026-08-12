package io.github.trueruslan.zakupgotov.provider;

public record LocationContext(String sourceProviderId, String fulfillmentContextId, String locality) {

    public LocationContext {
        sourceProviderId = requireText(sourceProviderId, "sourceProviderId");
        fulfillmentContextId = requireText(fulfillmentContextId, "fulfillmentContextId");
        locality = requireText(locality, "locality");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
