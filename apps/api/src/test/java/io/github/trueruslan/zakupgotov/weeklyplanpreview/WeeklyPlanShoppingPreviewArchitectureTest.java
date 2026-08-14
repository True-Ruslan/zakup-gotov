package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingPreviewArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String BOUNDARY = ROOT + "weeklyplanpreview";

    @Test
    void weeklyPlanPreviewApplicationBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream().anyMatch(javaClass -> javaClass.getPackageName().equals(BOUNDARY)))
                .as("M3.2 weeklyplanpreview production package must exist")
                .isTrue();
    }

    @Test
    void weeklyPlanPreviewDependsOnlyOnAcceptedRecipeShoppingAndWeeklyPlanProjectPackages() {
        var classes = productionClasses();

        var projectDependencies = classes.stream()
                .filter(javaClass -> javaClass.getPackageName().equals(BOUNDARY))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(BOUNDARY))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());

        assertThat(projectDependencies).allSatisfy(packageName -> assertThat(packageName).isIn(
                ROOT + "recipepreview",
                ROOT + "recipe",
                ROOT + "shopping",
                ROOT + "weeklyplan"));
    }

    @Test
    void weeklyPlanPreviewDoesNotReachComparisonProviderRetailerOrPersistenceLayers() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..weeklyplanpreview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..preview..",
                        "..recipecomparisonpreview..",
                        "..database..")
                .check(classes);
    }

    @Test
    void acceptedLowerBoundariesRemainIndependentFromWeeklyPlanPreview() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage(
                        "..shopping..",
                        "..recipe..",
                        "..recipepreview..",
                        "..weeklyplan..")
                .should().dependOnClassesThat().resideInAPackage("..weeklyplanpreview..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
