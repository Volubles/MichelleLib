package io.voluble.michellelib.text.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface for formatting chat messages.
 *
 * <p>Implementations can transform the source player's display name and message
 * into a formatted component that will be sent to the viewer.</p>
 *
 * <p>For viewer-unaware formatting (same output for all viewers), use {@link ViewerUnawareChatFormatter}.</p>
 */
@FunctionalInterface
public interface ChatFormatter {

    /**
     * Formats a chat message for a specific viewer.
     *
     * @param source the player who sent the message
     * @param sourceDisplayName the display name of the source player
     * @param message the message content
     * @param viewer the player who will receive the message (may be the source)
     * @return the formatted component to send to the viewer
     */
    @NotNull
    Component format(
            @NotNull Player source,
            @NotNull Component sourceDisplayName,
            @NotNull Component message,
            @NotNull Audience viewer
    );

    /**
     * A viewer-unaware formatter that produces the same output for all viewers.
     *
     * <p>This is more efficient than a regular {@link ChatFormatter} as the message
     * is only formatted once instead of once per viewer.</p>
     */
    @FunctionalInterface
    interface ViewerUnawareChatFormatter {
        /**
         * Formats a chat message without considering the viewer.
         *
         * @param source the player who sent the message
         * @param sourceDisplayName the display name of the source player
         * @param message the message content
         * @return the formatted component to send to all viewers
         */
        @NotNull
        Component format(
                @NotNull Player source,
                @NotNull Component sourceDisplayName,
                @NotNull Component message
        );
    }
}

