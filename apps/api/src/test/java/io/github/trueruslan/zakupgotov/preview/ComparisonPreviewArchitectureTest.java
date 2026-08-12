package io.github.trueruslan.zakupgotov.preview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ComparisonPreviewArchitectureTest {

    @Test
    void upstreamProductionDomainsDoNotDependOnPreview() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage(
                        "..shopping..",
                        "..location..",
                        "..provider..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..retailer..")
                .should().dependOnClassesThat().resideInAPackage("..preview..")
                .check(classes);
    }

    @Test
    void productionPreviewDoesNotDependOnFixtureOrTestSupportNamespaces() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..preview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..fixture..",
                        "..fixtures..",
                        "..testsupport..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
