package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class OfferSnapshot {

    private final OfferSnapshotId id;
    private final RetailerId retailerId;
    private final String sourceProviderId;
    private final AcquisitionMode sourceMode;
    private final String fulfillmentContextId;
    private final String sku;
    private final String productName;
    private final BigDecimal price;
    private final String currencyCode;
    private final AvailabilityStatus availability;
    private final FreshnessEvidence freshness;
    private final String sourceReference;

    private OfferSnapshot(
            OfferSnapshotId id,
            ObservedOffer observation,
            FreshnessEvidence freshness) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(observation, "observation must not be null");
        this.freshness = Objects.requireNonNull(freshness, "freshness must not be null");
        if (!observation.observedAt().equals(freshness.observedAt())) {
            throw new IllegalArgumentException("freshness observedAt must match observation observedAt");
        }

        this.retailerId = observation.retailerId();
        this.sourceProviderId = observation.sourceProviderId();
        this.sourceMode = observation.sourceMode();
        this.fulfillmentContextId = observation.fulfillmentContextId();
        this.sku = observation.sku();
        this.productName = observation.productName();
        this.price = observation.price();
        this.currencyCode = observation.currencyCode();
        this.availability = observation.availability();
        this.sourceReference = observation.sourceReference();
    }

    public static OfferSnapshot observationOnly(
            OfferSnapshotId id,
            ObservedOffer observation) {
        Objects.requireNonNull(observation, "observation must not be null");
        return new OfferSnapshot(
                id,
                observation,
                FreshnessEvidence.observationOnly(observation.observedAt()));
    }

    public static OfferSnapshot withProviderUpdatedAt(
            OfferSnapshotId id,
            ObservedOffer observation,
            Instant providerUpdatedAt) {
        Objects.requireNonNull(observation, "observation must not be null");
        return new OfferSnapshot(
                id,
                observation,
                FreshnessEvidence.providerUpdatedAt(observation.observedAt(), providerUpdatedAt));
    }

    public OfferSnapshotId id() {
        return id;
    }

    public RetailerId retailerId() {
        return retailerId;
    }

    public String sourceProviderId() {
        return sourceProviderId;
    }

    public AcquisitionMode sourceMode() {
        return sourceMode;
    }

    public String fulfillmentContextId() {
        return fulfillmentContextId;
    }

    public String sku() {
        return sku;
    }

    public String productName() {
        return productName;
    }

    public BigDecimal price() {
        return price;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public AvailabilityStatus availability() {
        return availability;
    }

    public FreshnessEvidence freshness() {
        return freshness;
    }

    public String sourceReference() {
        return sourceReference;
    }
}
