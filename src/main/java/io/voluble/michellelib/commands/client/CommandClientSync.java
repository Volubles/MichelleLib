package io.voluble.michellelib.commands.client;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Helpers around client command synchronization.
 *
 * <p>Paper docs note that {@link Player#updateCommands()} is thread-safe and can be used to fix
 * client/server command visibility mismatches when using {@code requires(...)} predicates.</p>
 */
public final class CommandClientSync {
    private CommandClientSync() {
    }

    public static void updateCommands(final @NotNull Player player) {
        Objects.requireNonNull(player, "player");
        player.updateCommands();
    }

    public static void updateCommands(final @NotNull Collection<? extends Player> players) {
        Objects.requireNonNull(players, "players");
        for (final Player player : players) {
            if (player == null) continue;
            player.updateCommands();
        }
    }
}


