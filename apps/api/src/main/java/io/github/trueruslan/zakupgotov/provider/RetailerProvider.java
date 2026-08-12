package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.List;
import java.util.Set;

public interface RetailerProvider {

    RetailerId retailerId();

    String sourceProviderId();

    AcquisitionMode acquisitionMode();

    ProviderAccessType accessType();

    Set<ProviderCapability> capabilities();

    List<ObservedOffer> search(LocationContext location, ProductQuery query);
}
