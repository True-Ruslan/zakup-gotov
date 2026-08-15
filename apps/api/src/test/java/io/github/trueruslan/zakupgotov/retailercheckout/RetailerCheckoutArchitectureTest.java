package io.github.trueruslan.zakupgotov.retailercheckout;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.LinkedHashSet;
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
    void retailerDomainDependencyIsLimitedToRetailerIdentityBinding() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
        var retailerDependencies = new LinkedHashSet<String>();

        for (var javaClass : classes) {
            if (!javaClass.getPackageName().equals("io.github.trueruslan.zakupgotov.retailercheckout")) {
                continue;
            }
            for (var dependency : javaClass.getDirectDependenciesFromSelf()) {
                var target = dependency.getTargetClass();
                if (target.getPackageName().equals("io.github.trueruslan.zakupgotov.retailer")) {
                    retailerDependencies.add(target.getName());
                }
            }
        }

        assertThat(retailerDependencies).containsExactly(RetailerId.class.getName());
    }

    @Test
    void acceptedBasketComparisonAndRetailerPackagesDoNotDependBackOnCheckoutComposition() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage("..basket..", "..comparison..", "..retailer..")
                .should().dependOnClassesThat().resideInAPackage("..retailercheckout..")
                .check(classes);
    }
}
