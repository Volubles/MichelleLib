package io.voluble.michellelib.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses human-readable time strings into Duration or ticks.
 *
 * <p>Supports formats like:
 * <ul>
 *   <li>"1 second", "2 seconds"</li>
 *   <li>"1 minute", "5 minutes"</li>
 *   <li>"1 hour", "2 hours"</li>
 *   <li>"1 day", "7 days"</li>
 *   <li>"1 week", "2 weeks"</li>
 *   <li>"1 month", "3 months" (approximated as 30 days)</li>
 *   <li>"1 year", "2 years" (approximated as 365 days)</li>
 *   <li>Combinations: "1h 30m 15s", "2 days 5 hours"</li>
 *   <li>Shorthand: "1s", "2m", "3h", "4d", "5w", "6mo", "7y"</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * Duration duration = TimeParser.parse("1 hour 30 minutes");
 * long ticks = TimeParser.parseTicks("5m 30s");
 *
 * // For command arguments
 * ArgumentType<Duration> timeArg = Args.timeDuration();
 * }</pre>
 */
public final class TimeParser {

    private static final Pattern TIME_UNIT_PATTERN = Pattern.compile(
            "(\\d+)\\s*(second|sec|s|minute|min|m|hour|hr|h|day|d|week|wk|w|month|mo|year|yr|y)(?:s)?",
            Pattern.CASE_INSENSITIVE
    );

    private TimeParser() {
    }

    /**
     * Parses a time string into a Duration.
     *
     * @param input the time string to parse (e.g., "1 hour 30 minutes", "5m 30s")
     * @return the parsed duration
     * @throws TimeParseException if the string cannot be parsed
     */
    @NotNull
    public static Duration parse(final @NotNull String input) throws TimeParseException {
        Objects.requireNonNull(input, "input");
        final String normalized = input.trim();

        if (normalized.isEmpty()) {
            throw new TimeParseException("Empty time string");
        }

        long totalSeconds = 0;
        final Matcher matcher = TIME_UNIT_PATTERN.matcher(normalized);
        boolean foundAny = false;

        while (matcher.find()) {
            foundAny = true;
            final long value = Long.parseLong(matcher.group(1));
            final String unit = matcher.group(2).toLowerCase();

            if (unit.equals("s") || unit.equals("sec") || unit.equals("second")) {
                totalSeconds += value;
            } else if (unit.equals("m") || unit.equals("min") || unit.equals("minute")) {
                totalSeconds += value * 60;
            } else if (unit.equals("h") || unit.equals("hr") || unit.equals("hour")) {
                totalSeconds += value * 3600;
            } else if (unit.equals("d") || unit.equals("day")) {
                totalSeconds += value * 86400;
            } else if (unit.equals("w") || unit.equals("wk") || unit.equals("week")) {
                totalSeconds += value * 604800; // 7 days
            } else if (unit.equals("mo") || unit.equals("month")) {
                totalSeconds += value * 2592000; // 30 days
            } else if (unit.equals("y") || unit.equals("yr") || unit.equals("year")) {
                totalSeconds += value * 31536000; // 365 days
            }
        }

        if (!foundAny || totalSeconds == 0) {
            throw new TimeParseException("No valid time units found in: " + input);
        }

        return Duration.ofSeconds(totalSeconds);
    }

    /**
     * Parses a time string into ticks.
     *
     * @param input the time string to parse
     * @return the number of ticks
     * @throws TimeParseException if the string cannot be parsed
     */
    public static long parseTicks(final @NotNull String input) throws TimeParseException {
        return TickConverter.durationToTicks(parse(input));
    }

    /**
     * Parses a time string into ticks, returning a default value if parsing fails.
     *
     * @param input the time string to parse
     * @param defaultTicks the default value to return if parsing fails
     * @return the parsed ticks, or the default value if parsing failed
     */
    public static long parseTicksOrDefault(final @NotNull String input, final long defaultTicks) {
        try {
            return parseTicks(input);
        } catch (final TimeParseException e) {
            return defaultTicks;
        }
    }

    /**
     * Parses a time string into a Duration, returning a default value if parsing fails.
     *
     * @param input the time string to parse
     * @param defaultDuration the default value to return if parsing fails
     * @return the parsed duration, or the default value if parsing failed
     */
    @NotNull
    public static Duration parseOrDefault(
            final @NotNull String input,
            final @NotNull Duration defaultDuration
    ) {
        try {
            return parse(input);
        } catch (final TimeParseException e) {
            return defaultDuration;
        }
    }

    /**
     * Formats a Duration into a human-readable string.
     *
     * @param duration the duration to format
     * @return a human-readable string (e.g., "1 hour 30 minutes")
     */
    @NotNull
    public static String format(final @NotNull Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isZero() || duration.isNegative()) {
            return "0 seconds";
        }

        final long totalSeconds = duration.getSeconds();
        final StringBuilder sb = new StringBuilder();

        long remaining = totalSeconds;

        final long years = remaining / 31536000;
        if (years > 0) {
            sb.append(years).append(years == 1 ? " year" : " years");
            remaining -= years * 31536000;
        }

        final long months = remaining / 2592000;
        if (months > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(months).append(months == 1 ? " month" : " months");
            remaining -= months * 2592000;
        }

        final long weeks = remaining / 604800;
        if (weeks > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(weeks).append(weeks == 1 ? " week" : " weeks");
            remaining -= weeks * 604800;
        }

        final long days = remaining / 86400;
        if (days > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(days).append(days == 1 ? " day" : " days");
            remaining -= days * 86400;
        }

        final long hours = remaining / 3600;
        if (hours > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
            remaining -= hours * 3600;
        }

        final long minutes = remaining / 60;
        if (minutes > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
            remaining -= minutes * 60;
        }

        if (remaining > 0 || sb.length() == 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(remaining).append(remaining == 1 ? " second" : " seconds");
        }

        return sb.toString();
    }

    /**
     * Formats ticks into a human-readable string.
     *
     * @param ticks the number of ticks
     * @return a human-readable string
     */
    @NotNull
    public static String formatTicks(final long ticks) {
        return format(TickConverter.ticksToDuration(ticks));
    }

    /**
     * Exception thrown when time parsing fails.
     */
    public static final class TimeParseException extends RuntimeException {
        public TimeParseException(final @NotNull String message) {
            super(message);
        }
    }
}

