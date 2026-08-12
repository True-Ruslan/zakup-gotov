package io.github.trueruslan.zakupgotov.retailer;

public enum RetailerId {
    PYATEROCHKA("pyaterochka"),
    PEREKRESTOK("perekrestok"),
    CHIZHIK("chizhik"),
    MAGNIT("magnit"),
    LENTA("lenta"),
    VKUSVILL("vkusvill"),
    OZON_FRESH("ozon-fresh"),
    SAMOKAT("samokat");

    private final String canonicalId;

    RetailerId(String canonicalId) {
        this.canonicalId = canonicalId;
    }

    public String canonicalId() {
        return canonicalId;
    }
}
