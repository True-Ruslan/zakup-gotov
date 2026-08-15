package io.github.trueruslan.zakupgotov.retailercheckout;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class RetailerCheckoutArchitectureTest {

    @Test
    void checkoutCompositionDoesNotReachIntoAcquisitionOrOtherProductLayers() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAPackage("..retailercheckout..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..shopping..",
                        "..location..",
                        "..preview..",
                        "..database..",
                        "..recipe..",
                        "..recipepreview..",
                        "..recipecomparisonpreview..",
                        "..weeklyplan..",
                        "..weeklyplanpreview..",
                        "..weeklyplancomparisonpreview..",
                        "..weeklyplanpantrypreview..",
                        "..weeklyplanpantrycomparisonpreview..",
                        "..pantry..",
                        "..operations..",
                        "..system..",
                        "org.springframework..",
                        "org.jooq..",
                        "jakarta..")
                .check(classes);
    }

    @Test
    void acceptedBasketAndComparisonPackagesDoNotDependBackOnCheckoutComposition() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage("..basket..", "..comparison..")
                .should().dependOnClassesThat().resideInAPackage("..retailercheckout..")
                .check(classes);
    }
}
