package io.voluble.michellelib.registry;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a registry operation fails.
 */
public final class RegistryException extends RuntimeException {

    public RegistryException(final @NotNull String message) {
        super(message);
    }

    public RegistryException(final @NotNull String message, final @NotNull Throwable cause) {
        super(message, cause);
    }
}

