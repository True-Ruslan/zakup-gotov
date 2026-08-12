package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.Objects;
import java.util.Optional;

public record MagnitStoreResolution(MagnitStoreResolutionStatus status, Optional<MagnitStoreCandidate> candidate) {

    public MagnitStoreResolution {
        status = Objects.requireNonNull(status, "status must not be null");
        candidate = Objects.requireNonNull(candidate, "candidate must not be null");
        if (status == MagnitStoreResolutionStatus.RESOLVED && candidate.isEmpty()) {
            throw new IllegalArgumentException("RESOLVED resolution must carry a candidate");
        }
        if (status != MagnitStoreResolutionStatus.RESOLVED && candidate.isPresent()) {
            throw new IllegalArgumentException("non-RESOLVED resolution must not carry a candidate");
        }
    }

    public static MagnitStoreResolution resolved(MagnitStoreCandidate candidate) {
        return new MagnitStoreResolution(MagnitStoreResolutionStatus.RESOLVED, Optional.of(candidate));
    }

    public static MagnitStoreResolution empty(MagnitStoreResolutionStatus status) {
        if (status == MagnitStoreResolutionStatus.RESOLVED) {
            throw new IllegalArgumentException("use resolved(...) for RESOLVED status");
        }
        return new MagnitStoreResolution(status, Optional.empty());
    }
}
