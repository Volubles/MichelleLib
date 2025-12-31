package io.voluble.michellelib.commands.context;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Convenience wrapper around Brigadier {@link CommandContext} and Paper {@link CommandSourceStack}.
 *
 * <p>This keeps command logic readable and reduces repeated boilerplate.</p>
 */
public final class MichelleCommandContext {
    private final CommandContext<CommandSourceStack> ctx;

    public MichelleCommandContext(final @NotNull CommandContext<CommandSourceStack> ctx) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
    }

    public @NotNull CommandContext<CommandSourceStack> brigadier() {
        return ctx;
    }

    public @NotNull CommandSourceStack source() {
        return ctx.getSource();
    }

    public @NotNull CommandSender sender() {
        return source().getSender();
    }

    public @Nullable Entity executor() {
        return source().getExecutor();
    }

    public <T> @NotNull T arg(final @NotNull String name, final @NotNull Class<T> type) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        return ctx.getArgument(name, type);
    }
}


