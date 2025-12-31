package io.voluble.michellelib.text.legacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy text serializers (section {@code §} and ampersand {@code &}).
 *
 * <p>Paper docs strongly recommend not mixing legacy formatting with MiniMessage parsing.
 * Keep legacy handling isolated and migrate legacy config to MiniMessage where possible.</p>
 */
public final class LegacyTexts {

    private LegacyTexts() {
    }

    public static final @NotNull LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();
    public static final @NotNull LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.legacyAmpersand();

    public static @NotNull Component deserializeSection(final @Nullable String input) {
        return SECTION.deserialize(input == null ? "" : input);
    }

    public static @NotNull Component deserializeAmpersand(final @Nullable String input) {
        return AMPERSAND.deserialize(input == null ? "" : input);
    }

    public static @NotNull String serializeSection(final @NotNull Component component) {
        return SECTION.serialize(component);
    }

    public static @NotNull String serializeAmpersand(final @NotNull Component component) {
        return AMPERSAND.serialize(component);
    }
}



