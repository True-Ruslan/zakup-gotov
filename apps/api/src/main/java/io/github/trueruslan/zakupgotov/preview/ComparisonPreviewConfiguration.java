package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ComparisonPreviewConfiguration {

    @Bean
    RetailerRegistry comparisonPreviewRetailerRegistry() {
        return RetailerRegistry.initial();
    }

    @Bean
    ComparisonRuntimeEvidenceSource comparisonRuntimeEvidenceSource() {
        return new NoopComparisonRuntimeEvidenceSource();
    }

    @Bean
    ComparisonPreviewService comparisonPreviewService(
            RetailerRegistry comparisonPreviewRetailerRegistry,
            ComparisonRuntimeEvidenceSource comparisonRuntimeEvidenceSource) {
        return new ComparisonPreviewService(
                comparisonPreviewRetailerRegistry,
                comparisonRuntimeEvidenceSource);
    }
}
