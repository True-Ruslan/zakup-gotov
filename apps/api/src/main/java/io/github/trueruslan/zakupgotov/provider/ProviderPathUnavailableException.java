package io.github.trueruslan.zakupgotov.provider;

public final class ProviderPathUnavailableException extends RuntimeException {

    public ProviderPathUnavailableException(String message) {
        super(message);
    }

    public ProviderPathUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
