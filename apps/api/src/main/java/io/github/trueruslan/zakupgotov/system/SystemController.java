package io.github.trueruslan.zakupgotov.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public final class SystemController {

    @GetMapping
    public SystemInfo getSystemInfo() {
        return new SystemInfo("zakup-gotov-api", "UP");
    }

    public record SystemInfo(String name, String status) {
    }
}
