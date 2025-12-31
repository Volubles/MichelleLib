package io.voluble.michellelib.commands;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Convenience wrapper around {@link CommandService} for a simpler API.
 *
 * <p>This provides a streamlined interface for registering command modules with your plugin.</p>
 */
public final class CommandKit {
    private final CommandService service;

    public CommandKit(final @NotNull Plugin plugin) {
        this.service = CommandService.create(Objects.requireNonNull(plugin, "plugin"));
    }

    public @NotNull Plugin plugin() {
        return service.plugin();
    }

    public @NotNull CommandKit addModule(final @NotNull CommandModule module) {
        service.addModule(module);
        return this;
    }

    public void install() {
        service.install();
    }
}


