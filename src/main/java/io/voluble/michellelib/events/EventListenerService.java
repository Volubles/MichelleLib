package io.voluble.michellelib.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service for managing event listener registration and cleanup.
 *
 * <p>Automatically unregisters all listeners when the plugin is disabled,
 * preventing memory leaks and ensuring proper cleanup.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     private EventListenerService eventService;
 *
 *     @Override
 *     public void onEnable() {
 *         this.eventService = EventListenerService.create(this);
 *
 *         // Register listeners
 *         eventService.register(new MyListener());
 *         eventService.register(new AnotherListener());
 *     }
 *
 *     @Override
 *     public void onDisable() {
 *         // Automatically unregisters all listeners
 *         if (this.eventService != null) {
 *             this.eventService.unregisterAll();
 *         }
 *     }
 * }
 * }</pre>
 */
public final class EventListenerService {
    private final @NotNull Plugin plugin;
    private final @NotNull PluginManager pluginManager;
    private final @NotNull List<Listener> registeredListeners = new ArrayList<>();

    private EventListenerService(final @NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.pluginManager = plugin.getServer().getPluginManager();
    }

    /**
     * Creates a new EventListenerService for the given plugin.
     */
    @NotNull
    public static EventListenerService create(final @NotNull Plugin plugin) {
        return new EventListenerService(plugin);
    }

    /**
     * Registers a listener.
     *
     * @param listener the listener to register
     */
    public void register(final @NotNull Listener listener) {
        Objects.requireNonNull(listener, "listener");
        this.pluginManager.registerEvents(listener, this.plugin);
        synchronized (this.registeredListeners) {
            this.registeredListeners.add(listener);
        }
    }

    /**
     * Unregisters a specific listener.
     *
     * @param listener the listener to unregister
     */
    public void unregister(final @NotNull Listener listener) {
        Objects.requireNonNull(listener, "listener");
        HandlerList.unregisterAll(listener);
        synchronized (this.registeredListeners) {
            this.registeredListeners.remove(listener);
        }
    }

    /**
     * Unregisters all listeners registered through this service.
     *
     * <p>Call this in {@code onDisable()} to ensure proper cleanup.</p>
     */
    public void unregisterAll() {
        synchronized (this.registeredListeners) {
            for (final Listener listener : this.registeredListeners) {
                HandlerList.unregisterAll(listener);
            }
            this.registeredListeners.clear();
        }
    }

    /**
     * Gets the plugin this service belongs to.
     */
    @NotNull
    public Plugin plugin() {
        return this.plugin;
    }
}

