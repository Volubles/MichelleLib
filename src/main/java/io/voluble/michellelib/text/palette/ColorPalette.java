package io.voluble.michellelib.text.palette;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A user-defined palette of named colors.
 *
 * <p>This library intentionally ships <strong>no default colors</strong>. Your plugin defines them:</p>
 *
 * <pre>{@code
 * public static final ColorPalette PALETTE = ColorPalette.builder()
 *   .hex("primary", "#C62828")
 *   .hex("success", "#8BC34A")
 *   .build();
 * }</pre>
 *
 * <p>If you want an optional starter set, see {@link io.voluble.michellelib.text.presets.echoes.EchoesPreset}.</p>
 */
public final class ColorPalette {

    private final @NotNull Map<String, TextColor> colors;

    private ColorPalette(final @NotNull Map<String, TextColor> colors) {
        this.colors = colors;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @Nullable TextColor get(final @NotNull String key) {
        return this.colors.get(normalize(key));
    }

    public @NotNull TextColor require(final @NotNull String key) {
        final TextColor color = get(key);
        if (color == null) {
            throw new IllegalArgumentException("Missing color key: " + key);
        }
        return color;
    }

    public @NotNull Map<String, TextColor> asMap() {
        return this.colors;
    }

    private static @NotNull String normalize(final @NotNull String key) {
        return key.trim().toLowerCase();
    }

    public static final class Builder {
        private final Map<String, TextColor> colors = new HashMap<>();

        private Builder() {
        }

        public @NotNull Builder color(final @NotNull String key, final @NotNull TextColor color) {
            this.colors.put(normalize(key), color);
            return this;
        }

        public @NotNull Builder hex(final @NotNull String key, final @NotNull String hex) {
            final TextColor color = TextColor.fromHexString(hex);
            if (color == null) {
                throw new IllegalArgumentException("Invalid hex color: " + hex);
            }
            return color(key, color);
        }

        public @NotNull ColorPalette build() {
            return new ColorPalette(Collections.unmodifiableMap(new HashMap<>(this.colors)));
        }
    }
}


