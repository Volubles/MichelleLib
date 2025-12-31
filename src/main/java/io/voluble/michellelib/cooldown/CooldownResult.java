package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

/**
 * Result of a cooldown check or application.
 */
public final class CooldownResult {
    private final boolean allowed;
    private final @NotNull Duration remaining;

    private CooldownResult(final boolean allowed, final @NotNull Duration remaining) {
        this.allowed = allowed;
        this.remaining = Objects.requireNonNull(remaining, "remaining");
    }

    public static @NotNull CooldownResult permit() {
        return new CooldownResult(true, Duration.ZERO);
    }

    public static @NotNull CooldownResult denied(final @NotNull Duration remaining) {
        return new CooldownResult(false, remaining);
    }

    public boolean allowed() {
        return allowed;
    }

    public @NotNull Duration remaining() {
        return remaining;
    }
}


