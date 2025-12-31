package io.voluble.michellelib.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Centralized NamespacedKey management for a plugin.
 *
 * <p>Paper recommends reusing NamespacedKey instances rather than creating new ones.
 * This class provides a centralized registry for all keys used by a plugin, ensuring
 * keys are reused and making it easier to manage all PDC keys in one place.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * public final class MyKeys {
 *     private static final PluginKeys KEYS = PluginKeys.create(plugin);
 *
 *     public static final NamespacedKey PLAYER_DATA = KEYS.key("player_data");
 *     public static final NamespacedKey CUSTOM_ENCHANT = KEYS.key("custom_enchant");
 *     public static final NamespacedKey LAST_LOGIN = KEYS.key("last_login");
 * }
 *
 * // Later:
 * pdc.set(MyKeys.PLAYER_DATA, PersistentDataType.STRING, "value");
 * }</pre>
 */
public final class PluginKeys {
    private final @NotNull Plugin plugin;
    private final @NotNull ConcurrentMap<String, NamespacedKey> cache = new ConcurrentHashMap<>();

    private PluginKeys(final @NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Creates a new PluginKeys instance for the given plugin.
     */
    @NotNull
    public static PluginKeys create(final @NotNull Plugin plugin) {
        return new PluginKeys(plugin);
    }

    /**
     * Gets or creates a NamespacedKey with the given identifier.
     *
     * <p>The key is cached, so subsequent calls with the same identifier return the same instance.</p>
     *
     * @param identifier the key identifier (e.g., "player_data", "custom_enchant")
     * @return a NamespacedKey in the plugin's namespace
     */
    @NotNull
    public NamespacedKey key(final @NotNull String identifier) {
        Objects.requireNonNull(identifier, "identifier");
        return this.cache.computeIfAbsent(identifier, id -> new NamespacedKey(this.plugin, id));
    }

    /**
     * Gets or creates a NamespacedKey with a path-like identifier.
     *
     * <p>Useful for organizing keys hierarchically (e.g., "data/players/stats").</p>
     *
     * @param path the path-like identifier
     * @return a NamespacedKey in the plugin's namespace
     */
    @NotNull
    public NamespacedKey path(final @NotNull String path) {
        return key(path);
    }

    /**
     * Clears the key cache.
     *
     * <p>Generally not needed unless you're dynamically creating and discarding many keys.</p>
     */
    public void clearCache() {
        this.cache.clear();
    }

    /**
     * Gets the plugin this instance belongs to.
     */
    @NotNull
    public Plugin plugin() {
        return this.plugin;
    }
}

