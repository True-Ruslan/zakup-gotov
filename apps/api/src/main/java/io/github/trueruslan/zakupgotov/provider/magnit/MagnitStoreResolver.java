package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.Objects;

public final class MagnitStoreResolver {

    private MagnitStoreResolver() {}

    public static MagnitStoreResolution resolve(MagnitStoreSearchEvidence evidence) {
        var input = Objects.requireNonNull(evidence, "evidence must not be null");
        if (input.conflictingStoreEvidence()) {
            return MagnitStoreResolution.empty(MagnitStoreResolutionStatus.CONFLICTING_STORE_EVIDENCE);
        }
        return switch (input.candidates().size()) {
            case 0 -> MagnitStoreResolution.empty(MagnitStoreResolutionStatus.NO_STORES);
            case 1 -> MagnitStoreResolution.resolved(input.candidates().getFirst());
            default -> MagnitStoreResolution.empty(MagnitStoreResolutionStatus.AMBIGUOUS);
        };
    }
}
