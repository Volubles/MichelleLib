package io.voluble.michellelib.menu;

import io.voluble.michellelib.menu.cache.MenuCache;
import io.voluble.michellelib.menu.providers.ProviderRegistry;
import io.voluble.michellelib.menu.internal.MenuEventListener;
import io.voluble.michellelib.scheduler.PaperScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instance-based menu "engine" for a single owning plugin.
 * <p>
 * This is intentionally NOT a plugin singleton; it is safe to create multiple instances
 * (e.g. tests, isolated subsystems, or future shared dependency usage).
 */
public final class MenuService {
    private final Plugin plugin;
    private final PaperScheduler scheduler;
    private final MenuSettings settings;
    private final ProviderRegistry providers = new ProviderRegistry();
    private final MenuCache cache = new MenuCache();
    private final Map<UUID, MenuSession> sessions = new ConcurrentHashMap<>();

    private MenuService(final @NotNull Plugin plugin, final @NotNull MenuSettings settings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.scheduler = new PaperScheduler(plugin);
        Bukkit.getPluginManager().registerEvents(new MenuEventListener(this), plugin);
    }

    public static @NotNull MenuService create(final @NotNull Plugin plugin) {
        return new MenuService(plugin, MenuSettings.defaults());
    }

    public static @NotNull MenuService create(final @NotNull Plugin plugin, final @NotNull MenuSettings settings) {
        return new MenuService(plugin, settings);
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    public @NotNull PaperScheduler scheduler() {
        return scheduler;
    }

    public @NotNull MenuSettings settings() {
        return settings;
    }

    public @NotNull ProviderRegistry providers() {
        return providers;
    }

    public @NotNull MenuCache cache() {
        return cache;
    }

    public @NotNull MenuSession session(final @NotNull Player player) {
        Objects.requireNonNull(player, "player");
        return sessions.computeIfAbsent(player.getUniqueId(), u -> new MenuSession(this, player));
    }

    public @Nullable MenuSession existing(final @NotNull Player player) {
        Objects.requireNonNull(player, "player");
        return sessions.get(player.getUniqueId());
    }

    public boolean has(final @NotNull Player player) {
        Objects.requireNonNull(player, "player");
        return sessions.containsKey(player.getUniqueId());
    }

    /**
     * Removes local session state (and per-session cache) for this player.
     * <p>
     * This does not attempt to modify player inventory state (safe to call from quit events).
     */
    public void removeSession(final @NotNull Player player) {
        Objects.requireNonNull(player, "player");
        UUID id = player.getUniqueId();
        MenuSession removed = sessions.remove(id);
        if (removed != null) {
            removed.shutdown();
        }
        cache.clearSession(id);
    }

    /**
     * Clears all sessions/caches/providers for this service.
     * <p>
     * Call this on your plugin's disable hook if you want to be explicit.
     */
    public void shutdown() {
        for (MenuSession s : sessions.values()) {
            try {
                s.shutdown();
            } catch (Throwable ignored) {
            }
        }
        sessions.clear();
        cache.clearAll();
        providers.clear();
    }
}


