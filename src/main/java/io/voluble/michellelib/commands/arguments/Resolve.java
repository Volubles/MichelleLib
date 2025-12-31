package io.voluble.michellelib.commands.arguments;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnBlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnFinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.position.ColumnBlockPosition;
import io.papermc.paper.command.brigadier.argument.position.ColumnFinePosition;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Rotation;
import io.voluble.michellelib.commands.context.MichelleCommandContext;
import io.voluble.michellelib.commands.errors.CommandErrors;
import net.kyori.adventure.chat.SignedMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Helpers for resolving Paper selector arguments.
 *
 * <p>Paper's {@code ArgumentTypes.player()/entity()} return a resolver object that must be
 * resolved using the {@code CommandSourceStack}. This class hides that boilerplate.</p>
 */
public final class Resolve {
    private Resolve() {
    }

    /**
     * Resolves any Paper {@link ArgumentResolver} argument (one-liner wrapper).
     */
    public static <T, R extends ArgumentResolver<T>> @NotNull T resolver(
        final @NotNull MichelleCommandContext ctx,
        final @NotNull String argumentName,
        final @NotNull Class<R> resolverType
    ) throws CommandSyntaxException {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(argumentName, "argumentName");
        Objects.requireNonNull(resolverType, "resolverType");
        final R resolver = ctx.arg(argumentName, resolverType);
        return resolver.resolve(ctx.source());
    }

    public static @NotNull List<Player> players(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(argumentName, "argumentName");
        final PlayerSelectorArgumentResolver resolver = ctx.arg(argumentName, PlayerSelectorArgumentResolver.class);
        return resolver.resolve(ctx.source());
    }

    public static @NotNull Player player(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        final List<Player> players = players(ctx, argumentName);
        if (players.isEmpty()) {
            throw CommandErrors.failText("No players matched that selector.");
        }
        return players.getFirst();
    }

    public static @NotNull List<Entity> entities(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(argumentName, "argumentName");
        final EntitySelectorArgumentResolver resolver = ctx.arg(argumentName, EntitySelectorArgumentResolver.class);
        return resolver.resolve(ctx.source());
    }

    public static @NotNull Entity entity(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        final List<Entity> entities = entities(ctx, argumentName);
        if (entities.isEmpty()) {
            throw CommandErrors.failText("No entities matched that selector.");
        }
        return entities.getFirst();
    }

    public static @NotNull BlockPosition blockPosition(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        return resolver(ctx, argumentName, BlockPositionResolver.class);
    }

    public static @NotNull ColumnBlockPosition columnBlockPosition(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        return resolver(ctx, argumentName, ColumnBlockPositionResolver.class);
    }

    public static @NotNull FinePosition finePosition(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        return resolver(ctx, argumentName, FinePositionResolver.class);
    }

    public static @NotNull ColumnFinePosition columnFinePosition(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        return resolver(ctx, argumentName, ColumnFinePositionResolver.class);
    }

    public static @NotNull Rotation rotation(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        return resolver(ctx, argumentName, RotationResolver.class);
    }

    public static @NotNull Collection<PlayerProfile> playerProfiles(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        return resolver(ctx, argumentName, PlayerProfileListResolver.class);
    }

    public static @NotNull CompletableFuture<SignedMessage> signedMessage(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) throws CommandSyntaxException {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(argumentName, "argumentName");
        final SignedMessageResolver resolver = ctx.arg(argumentName, SignedMessageResolver.class);
        return resolver.resolveSignedMessage(argumentName, ctx.brigadier());
    }

    public static @NotNull String signedMessageContent(final @NotNull MichelleCommandContext ctx, final @NotNull String argumentName) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(argumentName, "argumentName");
        final SignedMessageResolver resolver = ctx.arg(argumentName, SignedMessageResolver.class);
        return resolver.content();
    }
}


