package io.github.trueruslan.zakupgotov.provider;

import java.util.List;
import java.util.Set;

public interface RetailerProvider {

    String providerId();

    ProviderAccessType accessType();

    Set<ProviderCapability> capabilities();

    List<ObservedOffer> search(LocationContext location, ProductQuery query);
}
