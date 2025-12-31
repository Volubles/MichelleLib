package io.voluble.michellelib.text.console;

import io.voluble.michellelib.text.TextEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper around Paper's {@link ComponentLogger} that integrates with {@link TextEngine}
 * for easy MiniMessage-based console logging with automatic color support.
 *
 * <p>Paper's ComponentLogger automatically serializes Components to ANSI-colored console output,
 * making console logs more readable and visually organized.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * ComponentLoggerWrapper logger = new ComponentLoggerWrapper(
 *     plugin.getComponentLogger(),
 *     textEngine
 * );
 *
 * logger.info("<green>Plugin loaded successfully!</green>");
 * logger.warn("<yellow>Warning: <red>{}</red></yellow>", errorMessage);
 * logger.error("<red>Error occurred: <bold>{}</bold></red>", exception.getMessage());
 * }</pre>
 */
public final class ComponentLoggerWrapper {

    private final @NotNull ComponentLogger logger;
    private final @NotNull TextEngine textEngine;

    /**
     * Creates a new wrapper around the provided ComponentLogger.
     *
     * @param logger the ComponentLogger to wrap
     * @param textEngine the TextEngine for parsing MiniMessage
     */
    public ComponentLoggerWrapper(
            final @NotNull ComponentLogger logger,
            final @NotNull TextEngine textEngine
    ) {
        this.logger = logger;
        this.textEngine = textEngine;
    }

    /**
     * Gets the underlying ComponentLogger.
     */
    @NotNull
    public ComponentLogger logger() {
        return this.logger;
    }

    /**
     * Gets the TextEngine used for parsing.
     */
    @NotNull
    public TextEngine textEngine() {
        return this.textEngine;
    }

    /**
     * Logs a component at INFO level.
     */
    public void info(final @NotNull Component component) {
        this.logger.info(component);
    }

    /**
     * Logs a MiniMessage string at INFO level.
     */
    public void info(final @NotNull String miniMessage) {
        this.info(this.textEngine.parse(miniMessage));
    }

    /**
     * Logs a MiniMessage string with PlaceholderAPI support at INFO level.
     */
    public void info(final @Nullable Player player, final @NotNull String miniMessage) {
        this.info(this.textEngine.parse(player, miniMessage));
    }

    /**
     * Logs a MiniMessage string with arguments at INFO level.
     *
     * <p>Arguments are inserted into {} placeholders in order.
     */
    public void info(final @NotNull String miniMessage, final @NotNull Object... args) {
        this.info(formatWithArgs(miniMessage, args));
    }

    /**
     * Logs a component at WARN level.
     */
    public void warn(final @NotNull Component component) {
        this.logger.warn(component);
    }

    /**
     * Logs a MiniMessage string at WARN level.
     */
    public void warn(final @NotNull String miniMessage) {
        this.warn(this.textEngine.parse(miniMessage));
    }

    /**
     * Logs a MiniMessage string with PlaceholderAPI support at WARN level.
     */
    public void warn(final @Nullable Player player, final @NotNull String miniMessage) {
        this.warn(this.textEngine.parse(player, miniMessage));
    }

    /**
     * Logs a MiniMessage string with arguments at WARN level.
     */
    public void warn(final @NotNull String miniMessage, final @NotNull Object... args) {
        this.warn(formatWithArgs(miniMessage, args));
    }

    /**
     * Logs a component at ERROR level.
     */
    public void error(final @NotNull Component component) {
        this.logger.error(component);
    }

    /**
     * Logs a MiniMessage string at ERROR level.
     */
    public void error(final @NotNull String miniMessage) {
        this.error(this.textEngine.parse(miniMessage));
    }

    /**
     * Logs a MiniMessage string with PlaceholderAPI support at ERROR level.
     */
    public void error(final @Nullable Player player, final @NotNull String miniMessage) {
        this.error(this.textEngine.parse(player, miniMessage));
    }

    /**
     * Logs a MiniMessage string with arguments at ERROR level.
     */
    public void error(final @NotNull String miniMessage, final @NotNull Object... args) {
        this.error(formatWithArgs(miniMessage, args));
    }

    /**
     * Logs a component at DEBUG level.
     */
    public void debug(final @NotNull Component component) {
        this.logger.debug(component);
    }

    /**
     * Logs a MiniMessage string at DEBUG level.
     */
    public void debug(final @NotNull String miniMessage) {
        this.debug(this.textEngine.parse(miniMessage));
    }

    /**
     * Logs a MiniMessage string with PlaceholderAPI support at DEBUG level.
     */
    public void debug(final @Nullable Player player, final @NotNull String miniMessage) {
        this.debug(this.textEngine.parse(player, miniMessage));
    }

    /**
     * Logs a MiniMessage string with arguments at DEBUG level.
     */
    public void debug(final @NotNull String miniMessage, final @NotNull Object... args) {
        this.debug(formatWithArgs(miniMessage, args));
    }

    /**
     * Logs a component at TRACE level.
     */
    public void trace(final @NotNull Component component) {
        this.logger.trace(component);
    }

    /**
     * Logs a MiniMessage string at TRACE level.
     */
    public void trace(final @NotNull String miniMessage) {
        this.trace(this.textEngine.parse(miniMessage));
    }

    /**
     * Logs a MiniMessage string with PlaceholderAPI support at TRACE level.
     */
    public void trace(final @Nullable Player player, final @NotNull String miniMessage) {
        this.trace(this.textEngine.parse(player, miniMessage));
    }

    /**
     * Logs a MiniMessage string with arguments at TRACE level.
     */
    public void trace(final @NotNull String miniMessage, final @NotNull Object... args) {
        this.trace(formatWithArgs(miniMessage, args));
    }

    /**
     * Logs a component with an exception at ERROR level.
     */
    public void error(final @NotNull Component component, final @NotNull Throwable throwable) {
        this.logger.error(component, throwable);
    }

    /**
     * Logs a MiniMessage string with an exception at ERROR level.
     */
    public void error(final @NotNull String miniMessage, final @NotNull Throwable throwable) {
        this.error(this.textEngine.parse(miniMessage), throwable);
    }

    /**
     * Logs a MiniMessage string with arguments and an exception at ERROR level.
     */
    public void error(final @NotNull String miniMessage, final @NotNull Throwable throwable, final @NotNull Object... args) {
        this.error(formatWithArgs(miniMessage, args), throwable);
    }

    /**
     * Logs a component with an exception at WARN level.
     */
    public void warn(final @NotNull Component component, final @NotNull Throwable throwable) {
        this.logger.warn(component, throwable);
    }

    /**
     * Logs a MiniMessage string with an exception at WARN level.
     */
    public void warn(final @NotNull String miniMessage, final @NotNull Throwable throwable) {
        this.warn(this.textEngine.parse(miniMessage), throwable);
    }

    /**
     * Logs a MiniMessage string with arguments and an exception at WARN level.
     */
    public void warn(final @NotNull String miniMessage, final @NotNull Throwable throwable, final @NotNull Object... args) {
        this.warn(formatWithArgs(miniMessage, args), throwable);
    }

    private @NotNull String formatWithArgs(final @NotNull String template, final @NotNull Object... args) {
        String result = template;
        for (final Object arg : args) {
            result = result.replaceFirst("\\{\\}", String.valueOf(arg));
        }
        return result;
    }
}

