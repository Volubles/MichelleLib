package io.voluble.michellelib.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for converting between Minecraft ticks and time units.
 *
 * <p>Minecraft runs at 20 ticks per second. This class provides convenient
 * conversion methods between ticks and various time units.</p>
 */
public final class TickConverter {
    private static final int TICKS_PER_SECOND = 20;
    private static final long MILLIS_PER_TICK = 1000 / TICKS_PER_SECOND;

    private TickConverter() {
    }

    /**
     * Converts seconds to ticks.
     */
    public static long secondsToTicks(final long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Converts ticks to seconds.
     */
    public static long ticksToSeconds(final long ticks) {
        return ticks / TICKS_PER_SECOND;
    }

    /**
     * Converts minutes to ticks.
     */
    public static long minutesToTicks(final long minutes) {
        return secondsToTicks(minutes * 60);
    }

    /**
     * Converts ticks to minutes.
     */
    public static long ticksToMinutes(final long ticks) {
        return ticksToSeconds(ticks) / 60;
    }

    /**
     * Converts hours to ticks.
     */
    public static long hoursToTicks(final long hours) {
        return minutesToTicks(hours * 60);
    }

    /**
     * Converts ticks to hours.
     */
    public static long ticksToHours(final long ticks) {
        return ticksToMinutes(ticks) / 60;
    }

    /**
     * Converts a Duration to ticks.
     */
    public static long durationToTicks(final @NotNull Duration duration) {
        return (long) (duration.toMillis() / (double) MILLIS_PER_TICK);
    }

    /**
     * Converts ticks to a Duration.
     */
    @NotNull
    public static Duration ticksToDuration(final long ticks) {
        return Duration.ofMillis(ticks * MILLIS_PER_TICK);
    }

    /**
     * Converts a TimeUnit duration to ticks.
     */
    public static long toTicks(final long duration, final @NotNull TimeUnit unit) {
        return durationToTicks(Duration.ofMillis(unit.toMillis(duration)));
    }

    /**
     * Converts ticks to a specific TimeUnit.
     */
    public static long toTimeUnit(final long ticks, final @NotNull TimeUnit unit) {
        return unit.convert(ticksToDuration(ticks));
    }
}

