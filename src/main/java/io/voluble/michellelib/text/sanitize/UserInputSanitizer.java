package io.voluble.michellelib.text.sanitize;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sanitization helpers for user-provided strings before passing them through formatters/parsers.
 *
 * <p>This does not parse; it only escapes/neutralizes formatting tokens based on permissions.</p>
 */
public final class UserInputSanitizer {

    private UserInputSanitizer() {
    }

    public static final @NotNull String PERM_LEGACY = "neochat.format.legacy";
    public static final @NotNull String PERM_MINIMESSAGE = "neochat.format.minimessage";

    /**
     * Escapes legacy and/or MiniMessage tokens if the player lacks permissions.
     *
     * <ul>
     *   <li>If legacy is not allowed, doubles {@code &} to reduce the chance another formatter treats it as legacy.</li>
     *   <li>If MiniMessage is not allowed, escapes {@code <} as {@code \<} (MiniMessage escape) to prevent tag parsing.</li>
     * </ul>
     */
    public static @Nullable String sanitize(final @Nullable Player player, final @Nullable String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        final boolean allowLegacy = player != null && player.hasPermission(PERM_LEGACY);
        final boolean allowMiniMessage = player != null && player.hasPermission(PERM_MINIMESSAGE);

        String out = input;

        if (!allowLegacy) {
            out = escapeLegacyAmpersand(out);
        }
        if (!allowMiniMessage) {
            out = escapeMiniMessageTags(out);
        }

        return out;
    }

    private static @NotNull String escapeLegacyAmpersand(final @NotNull String input) {
        // Keep this simple and predictable: '&' -> '&&'
        return input.replace("&", "&&");
    }

    private static @NotNull String escapeMiniMessageTags(final @NotNull String input) {
        final StringBuilder sb = new StringBuilder(input.length());
        char prev = 0;
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == '<' && prev != '\\') {
                sb.append('\\');
            }
            sb.append(c);
            prev = c;
        }
        return sb.toString();
    }
}



