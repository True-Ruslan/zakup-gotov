package io.github.trueruslan.zakupgotov.provider;

/**
 * Deterministic provider implementation backed by recorded/synthetic fixtures only.
 * Ordinary CI must use this provider type through {@link ProviderFeasibilityHarness}.
 */
public interface FixtureRetailerProvider extends RetailerProvider {}
