package io.github.trueruslan.zakupgotov.comparison;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ComparisonBoundaryArchitectureTest {

    @Test
    void upstreamProductionDomainsDoNotDependOnComparisonReadModel() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage(
                        "..retailer..",
                        "..provider..",
                        "..shopping..",
                        "..matching..",
                        "..basket..",
                        "..location..")
                .should().dependOnClassesThat().resideInAPackage("..comparison..")
                .check(classes);
    }
}
