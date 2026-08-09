package io.github.trueruslan.zakupgotov;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationArchitectureTest {

    @Test
    void applicationModulesFollowModulithRules() {
        ApplicationModules.of(ZakupGotovApplication.class).verify();
    }
}
