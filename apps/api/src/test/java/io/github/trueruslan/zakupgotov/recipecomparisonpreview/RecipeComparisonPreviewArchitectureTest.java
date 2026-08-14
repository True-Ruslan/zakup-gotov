package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RecipeComparisonPreviewArchitectureTest {

    @Test
    void recipeComparisonPreviewApplicationBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName()
                                .equals("io.github.trueruslan.zakupgotov.recipecomparisonpreview")))
                .as("M2.3 recipecomparisonpreview production package must exist")
                .isTrue();
    }

    @Test
    void composedBoundaryDoesNotReachIntoRecipeOrComparisonInternals() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..recipecomparisonpreview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..recipe..",
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..database..")
                .check(classes);
    }

    @Test
    void composerUsesOnlyCanonicalShoppingQuantityValueTypes() {
        var classes = productionClasses();

        var shoppingDependencies = classes.stream()
                .filter(javaClass -> javaClass.getPackageName()
                        .equals("io.github.trueruslan.zakupgotov.recipecomparisonpreview"))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass().getName())
                .filter(name -> name.startsWith("io.github.trueruslan.zakupgotov.shopping."))
                .collect(Collectors.toSet());

        assertThat(shoppingDependencies).allSatisfy(name -> assertThat(name).isIn(
                "io.github.trueruslan.zakupgotov.shopping.Quantity",
                "io.github.trueruslan.zakupgotov.shopping.QuantityUnit"));
    }

    @Test
    void acceptedApplicationBoundariesRemainIndependentFromComposer() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage("..recipepreview..", "..preview..")
                .should().dependOnClassesThat().resideInAPackage("..recipecomparisonpreview..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
