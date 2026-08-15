package io.github.trueruslan.zakupgotov.basketoptimization;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;

class BasketOptimizationArchitectureTest {

    @Test
    void optimizerDoesNotReachIntoAcquisitionMatchingOrApplicationLayers() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAPackage("..basketoptimization..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..provider..",
                        "..matching..",
                        "..shopping..",
                        "..location..",
                        "..comparison..",
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
    void basketDependencyIsLimitedToBasketTotal() {
        assertDirectProjectDependencies("io.github.trueruslan.zakupgotov.basket", BasketTotal.class.getName());
    }

    @Test
    void retailerDependencyIsLimitedToRetailerIdentity() {
        assertDirectProjectDependencies("io.github.trueruslan.zakupgotov.retailer", RetailerId.class.getName());
    }

    @Test
    void acceptedUpstreamPackagesDoNotDependBackOnOptimizer() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage("..basket..", "..comparison..", "..retailer..", "..retailercheckout..")
                .should().dependOnClassesThat().resideInAPackage("..basketoptimization..")
                .check(classes);
    }

    private static void assertDirectProjectDependencies(String targetPackage, String... expectedClassNames) {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
        var dependencies = new LinkedHashSet<String>();

        for (var javaClass : classes) {
            if (!javaClass.getPackageName().equals("io.github.trueruslan.zakupgotov.basketoptimization")) {
                continue;
            }
            for (var dependency : javaClass.getDirectDependenciesFromSelf()) {
                var target = dependency.getTargetClass();
                if (target.getPackageName().equals(targetPackage)) {
                    dependencies.add(target.getName());
                }
            }
        }

        assertThat(dependencies).containsExactlyInAnyOrder(expectedClassNames);
    }
}
