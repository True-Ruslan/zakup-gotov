package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class WeeklyPlanPantryOptimizationPreviewArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String COMPOSITION = ROOT + "weeklyplanpantryoptimizationpreview";
    private static final Set<String> ALLOWED_PROJECT_PACKAGES = Set.of(
            ROOT + "basketoptimization",
            ROOT + "optimizationpreview",
            ROOT + "retailercheckout",
            ROOT + "weeklyplanpantrycomparisonpreview",
            ROOT + "weeklyplanpantrypreview",
            ROOT + "weeklyplanpreview");

    @Test
    void weeklyOptimizationCompositionDependsOnlyOnAcceptedBoundaries() {
        var projectDependencies = projectDependencies();

        assertThat(projectDependencies)
                .isNotEmpty()
                .allSatisfy(packageName -> assertThat(packageName).isIn(ALLOWED_PROJECT_PACKAGES));
        assertThat(projectDependencies)
                .contains(
                        ROOT + "optimizationpreview",
                        ROOT + "weeklyplanpantrycomparisonpreview",
                        ROOT + "weeklyplanpantrypreview",
                        ROOT + "weeklyplanpreview");
    }

    @Test
    void weeklyOptimizationCompositionDoesNotReachProvidersPersistenceOrRetailerAcquisition() {
        assertThat(projectDependencies())
                .noneMatch(packageName -> packageName.startsWith(ROOT + "provider"))
                .noneMatch(packageName -> packageName.startsWith(ROOT + "database"))
                .noneMatch(packageName -> packageName.startsWith(ROOT + "persistence"))
                .noneMatch(packageName -> packageName.equals(ROOT + "comparison"))
                .noneMatch(packageName -> packageName.equals(ROOT + "retailer"));
    }

    @Test
    void acceptedM353AndOptimizationOwnersDoNotDependBackOnWeeklyOptimizationComposition() {
        noClasses()
                .that().resideInAnyPackage(
                        "..weeklyplanpantrycomparisonpreview..",
                        "..weeklyplanpantrypreview..",
                        "..weeklyplanpreview..",
                        "..optimizationpreview..",
                        "..basketoptimization..",
                        "..retailercheckout..")
                .should().dependOnClassesThat().resideInAPackage("..weeklyplanpantryoptimizationpreview..")
                .check(productionClasses());
    }

    private static Set<String> projectDependencies() {
        return productionClasses().stream()
                .filter(javaClass -> javaClass.getPackageName().equals(COMPOSITION))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(COMPOSITION))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
