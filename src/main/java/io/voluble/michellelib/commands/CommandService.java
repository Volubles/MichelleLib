package io.voluble.michellelib.commands;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Plugin-scoped Brigadier command "engine" built on Paper's Lifecycle command registration.
 *
 * <p>This follows Paper's recommended pattern: register using {@link LifecycleEvents#COMMANDS}
 * so Paper can rebuild commands safely when required.</p>
 */
public final class CommandService {
    private final Plugin plugin;
    private final List<CommandModule> modules = new CopyOnWriteArrayList<>();

    private volatile boolean installed;

    private CommandService(final @NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public static @NotNull CommandService create(final @NotNull Plugin plugin) {
        return new CommandService(plugin);
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    /**
     * Adds a module that can register one or more commands.
     *
     * <p>This is safe to call from other shaded submodules inside the same plugin at runtime.</p>
     */
    public @NotNull CommandService addModule(final @NotNull CommandModule module) {
        modules.add(Objects.requireNonNull(module, "module"));
        return this;
    }

    /**
     * Installs the lifecycle hook that registers commands (and re-registers them on reload events).
     *
     * <p>Call this once in {@code onEnable()} (or in a Paper bootstrap if you use one).</p>
     */
    public synchronized void install() {
        if (installed) return;
        installed = true;

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final var registrar = event.registrar();
            final CommandRegistration registration = new CommandRegistration(plugin, registrar);
            for (final CommandModule module : modules) {
                module.register(registration);
            }
        });
    }
}


