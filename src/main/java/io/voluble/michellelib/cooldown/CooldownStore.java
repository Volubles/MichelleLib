package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.OptionalLong;

/**
 * Backing store for cooldown entries.
 */
public interface CooldownStore {
    @NotNull CooldownResult tryUse(@NotNull CooldownId id, @NotNull Duration cooldown);

    @NotNull Duration remaining(@NotNull CooldownId id);

    void clear(@NotNull CooldownId id);

    void clearOwner(@NotNull java.util.UUID owner);

    void clearAll();

    /**
     * Removes expired entries and returns the number removed.
     */
    int pruneExpired();

    /**
     * Returns the expiry time (epoch millis) for a cooldown, if available.
     *
     * <p>This exists to support persistence exports without exposing internal representations.</p>
     */
    @NotNull OptionalLong expiryEpochMillis(@NotNull CooldownId id);

    /**
     * Exports all non-expired cooldown expiry times (epoch millis).
     */
    @NotNull Map<CooldownId, Long> exportExpiryEpochMillis();
}


