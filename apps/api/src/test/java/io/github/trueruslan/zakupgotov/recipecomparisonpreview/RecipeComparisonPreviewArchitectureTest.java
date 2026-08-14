package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
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
    void composedBoundaryDoesNotReachPastAcceptedApplicationBoundaries() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..recipecomparisonpreview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..recipe..",
                        "..shopping..",
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..database..")
                .check(classes);
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
