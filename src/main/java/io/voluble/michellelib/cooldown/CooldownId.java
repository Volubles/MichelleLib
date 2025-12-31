package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Composite identifier for a cooldown entry.
 */
public record CooldownId(@NotNull UUID owner, @NotNull String key) {
    public CooldownId {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(key, "key");
    }

    public static @NotNull CooldownId of(final @NotNull UUID owner, final @NotNull CooldownKey key) {
        Objects.requireNonNull(key, "key");
        return new CooldownId(owner, key.id());
    }
}



