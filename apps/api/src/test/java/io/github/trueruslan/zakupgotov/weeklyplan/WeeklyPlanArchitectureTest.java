package io.github.trueruslan.zakupgotov.weeklyplan;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class WeeklyPlanArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String WEEKLY_PLAN = ROOT + "weeklyplan";

    @Test
    void weeklyPlanProductionBoundaryExists() {
        var classes = productionClasses();

        assertThat(classes.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName().equals(WEEKLY_PLAN)))
                .as("M3.1 weeklyplan production package must exist")
                .isTrue();
    }

    @Test
    void weeklyPlanDependsOnlyOnRecipeAndShoppingProjectPackages() {
        var classes = productionClasses();

        var projectDependencies = classes.stream()
                .filter(javaClass -> javaClass.getPackageName().equals(WEEKLY_PLAN))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(WEEKLY_PLAN))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());

        assertThat(projectDependencies)
                .allSatisfy(packageName -> assertThat(packageName)
                        .matches(ROOT + "(recipe|shopping)(\\..*)?"));
    }

    @Test
    void weeklyPlanDoesNotReachIntoDownstreamOrTransportPackages() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAPackage("..weeklyplan..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..preview..",
                        "..recipepreview..",
                        "..recipecomparisonpreview..",
                        "..provider..",
                        "..retailer..",
                        "..matching..",
                        "..basket..",
                        "..comparison..",
                        "..database..",
                        "org.springframework.web..")
                .check(classes);
    }

    @Test
    void acceptedRecipeAndShoppingPackagesRemainIndependentFromWeeklyPlan() {
        var classes = productionClasses();

        noClasses()
                .that().resideInAnyPackage("..recipe..", "..shopping..")
                .should().dependOnClassesThat().resideInAPackage("..weeklyplan..")
                .check(classes);
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
