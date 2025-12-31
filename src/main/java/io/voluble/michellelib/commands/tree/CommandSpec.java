package io.voluble.michellelib.commands.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * A fully declared root command: a root literal plus metadata for Paper's registrar.
 */
public record CommandSpec(
    LiteralArgumentBuilder<CommandSourceStack> root,
    @Nullable String description,
    List<String> aliases
) {
    public CommandSpec {
        aliases = List.copyOf(aliases);
    }

    public static CommandSpec of(final LiteralArgumentBuilder<CommandSourceStack> root) {
        return new CommandSpec(root, null, List.of());
    }

    public static CommandSpec of(final LiteralArgumentBuilder<CommandSourceStack> root, final @Nullable String description, final Collection<String> aliases) {
        return new CommandSpec(root, description, List.copyOf(aliases));
    }
}


