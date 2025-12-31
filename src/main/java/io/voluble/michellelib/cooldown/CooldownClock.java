package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

/**
 * Clock abstraction for cooldown calculations.
 */
public interface CooldownClock {
    long nowNanoTime();

    long nowEpochMillis();

    static @NotNull CooldownClock system() {
        return SystemCooldownClock.INSTANCE;
    }
}



