package io.voluble.michellelib.text.presets.echoes;

import io.voluble.michellelib.text.TextEngine;
import io.voluble.michellelib.text.palette.ColorPalette;
import io.voluble.michellelib.text.prefix.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Optional "Echoes" preset (colors + prefix labels) for convenience.
 *
 * <p>This is NOT required for the library to function. Treat it as an example/defaults bundle you can
 * use personally, copy, or ignore entirely.</p>
 */
public final class EchoesPreset {

    private EchoesPreset() {
    }

    // Core System Colors
    public static final @NotNull String PRIMARY = "#C62828"; // Deep Crimson – main titles, key actions
    public static final @NotNull String ACCENT = "#EF5350";  // Soft Crimson – hover, minor accents

    // Semantic Feedback
    public static final @NotNull String SUCCESS = "#8BC34A"; // Calm green – success confirmations
    public static final @NotNull String WARNING = "#FFB300"; // Amber – warnings/cooldowns
    public static final @NotNull String ERROR = "#D32F2F";   // Strong caution red – critical failures

    // Neutral / UI Text
    public static final @NotNull String NEUTRAL = "#CFD8DC"; // Soft gray-blue – system info & timestamps
    public static final @NotNull String TITLE = "#FFFFFF";   // Pure white – headers & clean readability

    // Optional: Rare/Mystic Highlight
    public static final @NotNull String MYSTIC = "#9575CD";  // Subtle Purple – rare/uncommon elements

    // Message "labels" (not components yet — you decide how to render them)
    public static final @NotNull String LABEL_SUCCESS = "Success";
    public static final @NotNull String LABEL_ERROR = "Error";
    public static final @NotNull String LABEL_WARNING = "Warning";
    public static final @NotNull String LABEL_INFO = "Info";
    public static final @NotNull String LABEL_PRIMARY = "Echoes";
    public static final @NotNull String LABEL_MYSTIC = "Echoes";

    /**
     * Convenience: build a {@link ColorPalette} matching this preset.
     *
     * <p>Keys are intentionally simple so you can reference them like {@code palette.require("primary")}.</p>
     */
    public static @NotNull ColorPalette palette() {
        return ColorPalette.builder()
                .hex("primary", PRIMARY)
                .hex("accent", ACCENT)
                .hex("success", SUCCESS)
                .hex("warning", WARNING)
                .hex("error", ERROR)
                .hex("neutral", NEUTRAL)
                .hex("title", TITLE)
                .hex("mystic", MYSTIC)
                .build();
    }

    /**
     * A simple colored prefix like {@code Echoes} (no brackets by default).
     *
     * <p>You can keep multiple prefixes by calling this with different labels/colors.</p>
     */
    public static @NotNull Prefix prefix(final @NotNull TextEngine engine, final @NotNull String label, final @NotNull String hexColor) {
        final TextColor color = requireHex(hexColor);
        final Component prefix = Component.text(label).color(color);
        return Prefix.component(prefix);
    }

    public static @NotNull Prefix primaryPrefix(final @NotNull TextEngine engine) {
        return prefix(engine, LABEL_PRIMARY, PRIMARY);
    }

    public static @NotNull Prefix successPrefix(final @NotNull TextEngine engine) {
        return prefix(engine, LABEL_SUCCESS, SUCCESS);
    }

    public static @NotNull Prefix warningPrefix(final @NotNull TextEngine engine) {
        return prefix(engine, LABEL_WARNING, WARNING);
    }

    public static @NotNull Prefix errorPrefix(final @NotNull TextEngine engine) {
        return prefix(engine, LABEL_ERROR, ERROR);
    }

    public static @NotNull Prefix infoPrefix(final @NotNull TextEngine engine) {
        return prefix(engine, LABEL_INFO, NEUTRAL);
    }

    public static @NotNull Prefix mysticPrefix(final @NotNull TextEngine engine) {
        return prefix(engine, LABEL_MYSTIC, MYSTIC);
    }

    private static @NotNull TextColor requireHex(final @NotNull String hex) {
        final TextColor color = TextColor.fromHexString(hex);
        if (color == null) {
            throw new IllegalArgumentException("Invalid hex color: " + hex);
        }
        return color;
    }
}



