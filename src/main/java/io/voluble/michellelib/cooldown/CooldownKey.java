package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

/**
 * Typed key for cooldown entries.
 *
 * <p>Plugins commonly implement this with an enum.</p>
 */
public interface CooldownKey {
    @NotNull String id();
}



