package io.github.trueruslan.zakupgotov.optimizationpreview;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CheckoutOptimizationPreviewArchitectureTest {

    private static final String ROOT = "io.github.trueruslan.zakupgotov.";
    private static final String OPTIMIZATION_PREVIEW = ROOT + "optimizationpreview";
    private static final Set<String> ALLOWED_PROJECT_PACKAGES = Set.of(
            ROOT + "basket",
            ROOT + "basketoptimization",
            ROOT + "comparison",
            ROOT + "location",
            ROOT + "preview",
            ROOT + "retailer",
            ROOT + "retailercheckout");

    @Test
    void optimizationPreviewDependsOnlyOnAcceptedEconomicsAndComparisonBoundaries() {
        var projectDependencies = projectDependencies();

        assertThat(projectDependencies)
                .isNotEmpty()
                .allSatisfy(packageName -> assertThat(packageName).isIn(ALLOWED_PROJECT_PACKAGES));
        assertThat(projectDependencies)
                .contains(
                        ROOT + "basket",
                        ROOT + "basketoptimization",
                        ROOT + "comparison",
                        ROOT + "location",
                        ROOT + "preview",
                        ROOT + "retailer",
                        ROOT + "retailercheckout");
    }

    @Test
    void optimizationPreviewDoesNotReachProvidersPersistenceOrWeeklyComposition() {
        assertThat(projectDependencies())
                .noneMatch(packageName -> packageName.startsWith(ROOT + "provider"))
                .noneMatch(packageName -> packageName.startsWith(ROOT + "database"))
                .noneMatch(packageName -> packageName.startsWith(ROOT + "persistence"))
                .noneMatch(packageName -> packageName.startsWith(ROOT + "weeklyplan"));
    }

    @Test
    void acceptedOptimizationOwnersDoNotDependBackOnProjection() {
        noClasses()
                .that().resideInAnyPackage(
                        "..basket..",
                        "..basketoptimization..",
                        "..comparison..",
                        "..retailercheckout..",
                        "..preview..")
                .should().dependOnClassesThat().resideInAPackage("..optimizationpreview..")
                .check(productionClasses());
    }

    private static Set<String> projectDependencies() {
        return productionClasses().stream()
                .filter(javaClass -> javaClass.getPackageName().equals(OPTIMIZATION_PREVIEW))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .map(dependency -> dependency.getTargetClass())
                .filter(target -> target.getName().startsWith(ROOT))
                .filter(target -> !target.getPackageName().equals(OPTIMIZATION_PREVIEW))
                .map(target -> target.getPackageName())
                .collect(Collectors.toSet());
    }

    private static JavaClasses productionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
    }
}
