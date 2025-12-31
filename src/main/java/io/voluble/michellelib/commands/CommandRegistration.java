package io.voluble.michellelib.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.voluble.michellelib.commands.arguments.EnumArgument;
import io.voluble.michellelib.commands.tree.CommandSpec;
import io.voluble.michellelib.commands.tree.CommandTree;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Registration façade exposed to modules during {@link io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents#COMMANDS}.
 *
 * <p>This class stays intentionally small. If you need something advanced, you can always access
 * the underlying {@link Commands} registrar via {@link #paperCommands()}.</p>
 */
public final class CommandRegistration {
    private final Plugin plugin;
    private final Commands paperCommands;

    CommandRegistration(final @NotNull Plugin plugin, final @NotNull Commands paperCommands) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.paperCommands = Objects.requireNonNull(paperCommands, "paperCommands");
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    public @NotNull Commands paperCommands() {
        return paperCommands;
    }

    /**
     * Starts a {@link CommandTree} root.
     */
    public @NotNull CommandTree.Root tree(final @NotNull String rootName) {
        Objects.requireNonNull(rootName, "rootName");
        return CommandTree.root(rootName);
    }

    /**
     * Convenience factory for a reusable enum argument.
     */
    public <E extends Enum<E>> @NotNull EnumArgument<E> enumArgument(final @NotNull Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");
        return new EnumArgument<>(enumClass);
    }

    /**
     * Registers a Brigadier command built via {@link CommandSpec}.
     */
    public @NotNull Set<String> register(final @NotNull CommandSpec spec) {
        Objects.requireNonNull(spec, "spec");
        final LiteralCommandNode<CommandSourceStack> built = spec.root().build();
        return paperCommands.register(built, spec.description(), spec.aliases());
    }

    /**
     * Registers a Brigadier command node directly (lowest abstraction).
     */
    public @NotNull Set<String> register(
        final @NotNull LiteralCommandNode<CommandSourceStack> node,
        final String description,
        final @NotNull Collection<String> aliases
    ) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(aliases, "aliases");
        return paperCommands.register(node, description, aliases);
    }

    /**
     * Registers a basic (Bukkit-style) command.
     */
    public @NotNull Set<String> registerBasic(
        final @NotNull String label,
        final String description,
        final @NotNull List<String> aliases,
        final @NotNull BasicCommand command
    ) {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(aliases, "aliases");
        Objects.requireNonNull(command, "command");
        return paperCommands.register(label, description, aliases, command);
    }

    /**
     * Registers a basic (Bukkit-style) command with optional aliases.
     */
    public @NotNull Set<String> registerBasic(final @NotNull String label, final @NotNull BasicCommand command, final String... aliases) {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(command, "command");
        return paperCommands.register(label, null, Arrays.asList(aliases), command);
    }
}


