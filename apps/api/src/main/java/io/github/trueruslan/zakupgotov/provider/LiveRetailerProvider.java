package io.github.trueruslan.zakupgotov.provider;

/**
 * Provider implementation that may communicate with a live external retailer service.
 * It must be invoked only through an explicit live-probe path, never ordinary fixture CI.
 */
public interface LiveRetailerProvider extends RetailerProvider {}
