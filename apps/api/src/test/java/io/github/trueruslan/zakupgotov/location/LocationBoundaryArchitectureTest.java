package io.github.trueruslan.zakupgotov.location;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class LocationBoundaryArchitectureTest {

    @Test
    void locationDomainDoesNotDependOnProviderOrRetailerPackages() {
        var classes = new ClassFileImporter().importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAPackage("..location..")
                .should().dependOnClassesThat().resideInAnyPackage("..provider..", "..retailer..")
                .check(classes);
    }
}
