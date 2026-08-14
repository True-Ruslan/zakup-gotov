package io.github.trueruslan.zakupgotov.pantry;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PantryArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String PANTRY = ROOT + "pantry";

    @Test
    void pantryProductionBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName().equals(PANTRY)))
                .as("M3.5.1 pantry production package must exist")
                .isTrue();
    }

    @Test
    void pantryDependsOnlyOnShoppingProjectPackage() {
        var classes = productionClasses();

        var projectDependencies = classes.stream()
                .filter(javaClass -> javaClass.getPackageName().equals(PANTRY))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(PANTRY))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());

        assertThat(projectDependencies)
                .allSatisfy(packageName -> assertThat(packageName)
                        .matches(ROOT + "shopping(\\..*)?"));
    }

    @Test
    void pantryDoesNotReachIntoUpstreamDownstreamTransportOrPersistencePackages() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..pantry..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..recipe..",
                        "..weeklyplan..",
                        "..weeklyplanpreview..",
                        "..weeklyplancomparisonpreview..",
                        "..preview..",
                        "..recipepreview..",
                        "..recipecomparisonpreview..",
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..database..",
                        "org.springframework..")
                .check(classes);
    }

    @Test
    void acceptedShoppingRecipeAndWeeklyPlanPackagesRemainIndependentFromPantry() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage("..shopping..", "..recipe..", "..weeklyplan..")
                .should().dependOnClassesThat().resideInAPackage("..pantry..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
