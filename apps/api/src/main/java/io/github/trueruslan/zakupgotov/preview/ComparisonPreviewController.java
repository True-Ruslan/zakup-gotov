package io.github.trueruslan.zakupgotov.preview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comparison-previews")
public final class ComparisonPreviewController {

    private final ComparisonPreviewService service;

    public ComparisonPreviewController(ComparisonPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public ComparisonPreview create(@RequestBody ComparisonPreviewRequest request) {
        return service.create(request);
    }
}
