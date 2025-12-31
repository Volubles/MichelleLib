package io.voluble.michellelib.text.prefix;

import io.voluble.michellelib.text.TextEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reusable message prefix.
 *
 * <p>MichelleLib intentionally does not ship "your server's" prefixes. Instead, plugins define their own:
 *
 * <pre>{@code
 * public final class MyTexts {
 *   public static final TextEngine TEXT = TextEngine.builder().build();
 *   public static final Prefix PREFIX = Prefix.miniMessage(TEXT, "<gray>[<red>MyPlugin</red>]</gray>");
 * }
 *
 * // later:
 * player.sendMessage(MyTexts.PREFIX.miniMessage(MyTexts.TEXT, player, "<green>Saved!</green>"));
 * }</pre>
 */
public final class Prefix {

    private static final @NotNull Component DEFAULT_SEPARATOR = Component.space();

    private final @NotNull Component prefix;
    private final @NotNull Component separator;

    private Prefix(final @NotNull Component prefix, final @NotNull Component separator) {
        this.prefix = prefix;
        this.separator = separator;
    }

    /**
     * Creates a prefix from a component.
     */
    public static @NotNull Prefix component(final @NotNull Component prefix) {
        return new Prefix(prefix, DEFAULT_SEPARATOR);
    }

    /**
     * Creates a prefix from a MiniMessage string.
     */
    public static @NotNull Prefix miniMessage(final @NotNull TextEngine engine, final @NotNull String prefixMiniMessage) {
        return component(engine.parse(prefixMiniMessage));
    }

    /**
     * Returns a copy of this prefix with a custom separator inserted between the prefix and body.
     */
    public @NotNull Prefix withSeparator(final @NotNull Component separator) {
        return new Prefix(this.prefix, separator);
    }

    /**
     * Applies this prefix to a component body.
     */
    public @NotNull Component apply(final @NotNull Component body) {
        if (this.prefix.equals(Component.empty())) {
            return body;
        }
        return this.prefix.append(this.separator).append(body);
    }

    /**
     * Parses a MiniMessage body (with optional PlaceholderAPI support, depending on the engine) and applies the prefix.
     */
    public @NotNull Component miniMessage(final @NotNull TextEngine engine, final @Nullable Player player, final @NotNull String bodyMiniMessage) {
        return apply(engine.parse(player, bodyMiniMessage));
    }

    /**
     * Parses user input using the engine's user MiniMessage instance and applies the prefix.
     */
    public @NotNull Component userMiniMessage(final @NotNull TextEngine engine, final @NotNull String userInputMiniMessage) {
        return apply(engine.parseUser(userInputMiniMessage));
    }

    /**
     * Applies a "base" color to the entire combined message (prefix + body).
     *
     * <p>This is useful if you want the whole message to inherit a color without manually coloring
     * both the prefix and the body. Any explicitly colored parts of the body will still override
     * this base color.</p>
     */
    public @NotNull Component applyWithBaseColor(final @NotNull Component body, final @NotNull TextColor baseColor) {
        return apply(body).color(baseColor);
    }

    public @NotNull Component miniMessageWithBaseColor(
            final @NotNull TextEngine engine,
            final @Nullable Player player,
            final @NotNull String bodyMiniMessage,
            final @NotNull TextColor baseColor
    ) {
        return applyWithBaseColor(engine.parse(player, bodyMiniMessage), baseColor);
    }
}


