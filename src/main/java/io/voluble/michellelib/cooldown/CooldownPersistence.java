package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

/**
 * Persistence strategy for cooldown state.
 */
public interface CooldownPersistence {
    void loadInto(@NotNull CooldownStore store);

    void saveFrom(@NotNull CooldownStore store);
}



