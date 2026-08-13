package io.github.trueruslan.zakupgotov.recipepreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class RecipeShoppingPreviewArchitectureTest {

    @Test
    void recipePreviewApplicationBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName()
                                .equals("io.github.trueruslan.zakupgotov.recipepreview")))
                .as("M2.2 recipepreview production package must exist")
                .isTrue();
    }

    @Test
    void shoppingAndRecipeRemainIndependentFromRecipePreview() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..shopping..")
                .should().dependOnClassesThat().resideInAnyPackage("..recipe..", "..recipepreview..")
                .check(classes);

        noClasses()
                .that().resideInAPackage("..recipe..")
                .should().dependOnClassesThat().resideInAPackage("..recipepreview..")
                .check(classes);
    }

    @Test
    void recipePreviewDoesNotDependOnDownstreamAcquisitionOrPersistencePackages() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..recipepreview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..database..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
