package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class WeeklyPlanPantryComparisonPreviewArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String COMPOSITION = ROOT + "weeklyplanpantrycomparisonpreview";
    private static final Set<String> ALLOWED_PROJECT_PACKAGES = Set.of(
            ROOT + "weeklyplanpantrypreview",
            ROOT + "weeklyplanpreview",
            ROOT + "preview");

    @Test
    void pantryAwareWeeklyComparisonProductionBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName().equals(COMPOSITION)))
                .as("M3.5.3 Pantry-aware WeeklyPlan comparison production package must exist")
                .isTrue();
    }

    @Test
    void compositionDependsOnlyOnAcceptedPantryPreviewWeeklyRequestAndComparisonPreviewPackages() {
        var projectDependencies = productionClasses().stream()
                .filter(javaClass -> javaClass.getPackageName().equals(COMPOSITION))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(COMPOSITION))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());

        assertThat(projectDependencies)
                .as("M3.5.3 may compose only accepted M3.5.2, WeeklyPlan request vocabulary and ComparisonPreview")
                .isNotEmpty()
                .allSatisfy(packageName -> assertThat(packageName).isIn(ALLOWED_PROJECT_PACKAGES));
        assertThat(projectDependencies)
                .contains(ROOT + "weeklyplanpantrypreview", ROOT + "weeklyplanpreview", ROOT + "preview");
    }

    @Test
    void compositionDoesNotReachDomainRetailerProviderPersistenceOrBrowserPackagesDirectly() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..weeklyplanpantrycomparisonpreview..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..pantry..",
                        "..shopping..",
                        "..recipe..",
                        "..weeklyplan..",
                        "..comparison..",
                        "..basket..",
                        "..matching..",
                        "..provider..",
                        "..retailer..",
                        "..database..",
                        "..persistence..",
                        "..web..")
                .check(classes);
    }

    @Test
    void acceptedM33AndM352BoundariesDoNotDependBackOnNewComposition() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage(
                        "..weeklyplancomparisonpreview..",
                        "..weeklyplanpantrypreview..",
                        "..weeklyplanpreview..")
                .should().dependOnClassesThat().resideInAPackage("..weeklyplanpantrycomparisonpreview..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
