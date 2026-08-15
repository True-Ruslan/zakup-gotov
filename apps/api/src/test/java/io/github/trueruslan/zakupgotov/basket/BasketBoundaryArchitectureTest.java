package io.github.trueruslan.zakupgotov.basket;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BasketBoundaryArchitectureTest {

    private static final Set<String> BASKET_ECONOMICS_TYPES = Set.of(
            "BasketEconomicsKnowledgeStatus",
            "BasketFee",
            "MinimumOrderConstraint",
            "MinimumOrderStatus",
            "CheckoutTotalStatus",
            "BasketEconomics",
            "BasketEconomicsAssessment",
            "BasketEconomicsCalculator");

    @Test
    void upstreamProductionDomainsDoNotDependOnBasket() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");

        noClasses()
                .that().resideInAnyPackage("..provider..", "..shopping..", "..matching..", "..retailer..")
                .should().dependOnClassesThat().resideInAPackage("..basket..")
                .check(classes);
    }

    @Test
    void basketEconomicsFoundationDoesNotAcquireOrCompareRetailerData() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.trueruslan.zakupgotov");
        var economicsTypes = new DescribedPredicate<JavaClass>("M4.1 basket economics types") {
            @Override
            public boolean test(JavaClass javaClass) {
                return BASKET_ECONOMICS_TYPES.contains(javaClass.getSimpleName());
            }
        };

        noClasses()
                .that(economicsTypes)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..provider..", "..matching..", "..retailer..", "..comparison..")
                .check(classes);
    }
}
