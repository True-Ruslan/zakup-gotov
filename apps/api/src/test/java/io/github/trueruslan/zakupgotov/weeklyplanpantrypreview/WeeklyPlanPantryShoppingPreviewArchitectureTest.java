package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class WeeklyPlanPantryShoppingPreviewArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String COMPOSITION = ROOT + "weeklyplanpantrypreview";
    private static final Set<String> ALLOWED_PROJECT_PACKAGES = Set.of(
            ROOT + "weeklyplanpreview",
            ROOT + "pantry",
            ROOT + "shopping");

    @Test
    void pantryWeeklyCompositionProductionBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName().equals(COMPOSITION)))
                .as("M3.5.2 Pantry-aware WeeklyPlan preview production package must exist")
                .isTrue();
    }

    @Test
    void pantryWeeklyCompositionDependsOnlyOnAcceptedM32PantryAndShoppingProjectPackages() {
        var projectDependencies = productionClasses().stream()
                .filter(javaClass -> javaClass.getPackageName().equals(COMPOSITION))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(COMPOSITION))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());

        assertThat(projectDependencies)
                .as("M3.5.2 may compose only accepted M3.2, Pantry and neutral Shopping semantics")
                .isNotEmpty()
                .allSatisfy(packageName -> assertThat(packageName).isIn(ALLOWED_PROJECT_PACKAGES));
        assertThat(projectDependencies)
                .contains(ROOT + "weeklyplanpreview", ROOT + "pantry", ROOT + "shopping");
    }

    @Test
    void pantryWeeklyCompositionDoesNotReachComparisonRetailerProviderOrPersistencePackages() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..weeklyplanpantrypreview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..weeklyplancomparisonpreview..",
                        "..recipecomparisonpreview..",
                        "..preview..",
                        "..comparison..",
                        "..basket..",
                        "..matching..",
                        "..provider..",
                        "..retailer..",
                        "..database..")
                .check(classes);
    }

    @Test
    void acceptedM32AndM33BoundariesDoNotDependBackOnPantryWeeklyComposition() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage("..weeklyplanpreview..", "..weeklyplancomparisonpreview..")
                .should().dependOnClassesThat().resideInAPackage("..weeklyplanpantrypreview..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
