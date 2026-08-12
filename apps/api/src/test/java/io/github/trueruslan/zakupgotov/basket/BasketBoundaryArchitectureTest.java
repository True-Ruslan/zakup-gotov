package io.github.trueruslan.zakupgotov.basket;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class BasketBoundaryArchitectureTest {

    @Test
    void upstreamProductionDomainsDoNotDependOnBasket() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage("..provider..", "..shopping..", "..matching..", "..retailer..")
                .should().dependOnClassesThat().resideInAPackage("..basket..")
                .check(classes);
    }
}
