package io.voluble.michellelib.text.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Optional PlaceholderAPI support via reflection.
 *
 * <p>This keeps the library usable even if PlaceholderAPI is not installed at runtime.</p>
 *
 * <p><strong>Threading:</strong> PlaceholderAPI expansions are generally expected to run on the main thread.
 * This bridge will no-op when called off the main thread.</p>
 */
public final class PlaceholderApi {

    private PlaceholderApi() {
    }

    private static final @NotNull String PLUGIN_NAME = "PlaceholderAPI";
    private static volatile @Nullable Method setPlaceholdersMethod;
    private static volatile boolean methodLookupAttempted;

    public static boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin(PLUGIN_NAME) != null;
    }

    /**
     * Expands standard PlaceholderAPI placeholders like {@code %player_name%}.
     *
     * @param player the player context
     * @param input raw input
     * @return expanded input, or the original input when PAPI is unavailable / errors / async
     */
    public static @Nullable String expand(final @Nullable Player player, final @Nullable String input) {
        if (player == null || input == null || input.isEmpty()) {
            return input;
        }
        if (!isInstalled()) {
            return input;
        }
        if (!Bukkit.isPrimaryThread()) {
            return input;
        }

        final Method m = method();
        if (m == null) {
            return input;
        }

        try {
            final Object out = m.invoke(null, player, input);
            return out instanceof String s ? s : input;
        } catch (final ReflectiveOperationException ignored) {
            return input;
        }
    }

    private static @Nullable Method method() {
        Method cached = setPlaceholdersMethod;
        if (cached != null) {
            return cached;
        }
        if (methodLookupAttempted) {
            return null;
        }

        synchronized (PlaceholderApi.class) {
            cached = setPlaceholdersMethod;
            if (cached != null) {
                return cached;
            }
            if (methodLookupAttempted) {
                return null;
            }
            methodLookupAttempted = true;

            try {
                final Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                cached = clazz.getMethod("setPlaceholders", Player.class, String.class);
                setPlaceholdersMethod = cached;
                return cached;
            } catch (final ReflectiveOperationException ignored) {
                return null;
            }
        }
    }
}



