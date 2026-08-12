package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.List;
import java.util.Objects;

public record MagnitStoreSearchEvidence(List<MagnitStoreCandidate> candidates, boolean conflictingStoreEvidence) {

    public MagnitStoreSearchEvidence {
        candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates must not be null"));
        if (conflictingStoreEvidence && !candidates.isEmpty()) {
            throw new IllegalArgumentException("conflicting store evidence must not expose candidates");
        }
    }

    public static MagnitStoreSearchEvidence empty() {
        return new MagnitStoreSearchEvidence(List.of(), false);
    }

    public static MagnitStoreSearchEvidence conflict() {
        return new MagnitStoreSearchEvidence(List.of(), true);
    }
}
