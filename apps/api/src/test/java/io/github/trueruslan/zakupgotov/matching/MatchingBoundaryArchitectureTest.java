package io.github.trueruslan.zakupgotov.matching;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class MatchingBoundaryArchitectureTest {

    @Test
    void upstreamProductionDomainsDoNotDependOnMatching() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage("..provider..", "..shopping..", "..retailer..")
                .should().dependOnClassesThat().resideInAPackage("..matching..")
                .check(classes);
    }
}
