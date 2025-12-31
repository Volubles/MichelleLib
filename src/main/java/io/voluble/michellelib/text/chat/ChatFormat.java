package io.voluble.michellelib.text.chat;

import io.voluble.michellelib.text.TextEngine;
import io.voluble.michellelib.text.prefix.Prefix;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A builder for constructing chat formats with multiple formatters.
 *
 * <p>Formatters are applied in the order they are added. This allows chaining
 * prefixes, suffixes, and other transformations.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * ChatFormat format = ChatFormat.builder()
 *     .prefix(Prefix.miniMessage(textEngine, "<gray>[<red>Server</red>]</gray>"))
 *     .format((source, displayName, message, viewer) -> {
 *         return displayName.append(Component.text(": ")).append(message);
 *     })
 *     .build();
 * }</pre>
 */
public final class ChatFormat {

    private final @NotNull List<ChatFormatter> formatters;
    private final @Nullable ChatFormatter.ViewerUnawareChatFormatter viewerUnawareFormatter;

    private ChatFormat(
            final @NotNull List<ChatFormatter> formatters,
            final @Nullable ChatFormatter.ViewerUnawareChatFormatter viewerUnawareFormatter
    ) {
        this.formatters = List.copyOf(formatters);
        this.viewerUnawareFormatter = viewerUnawareFormatter;
    }

    /**
     * Creates a new builder for constructing a chat format.
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a simple chat format that displays: "Player: message".
     */
    public static @NotNull ChatFormat simple() {
        return builder()
                .format((source, displayName, message, viewer) ->
                        displayName.append(Component.text(": ")).append(message))
                .build();
    }

    /**
     * Creates a chat format with a prefix.
     */
    public static @NotNull ChatFormat withPrefix(final @NotNull Prefix prefix) {
        return builder()
                .prefix(prefix)
                .format((source, displayName, message, viewer) ->
                        displayName.append(Component.text(": ")).append(message))
                .build();
    }

    /**
     * Applies all formatters to create the final message component.
     */
    @NotNull
    Component render(
            final @NotNull Player source,
            final @NotNull Component sourceDisplayName,
            final @NotNull Component message,
            final @NotNull Audience viewer
    ) {
        if (this.viewerUnawareFormatter != null) {
            return this.viewerUnawareFormatter.format(source, sourceDisplayName, message);
        }

        Component result = message;
        for (final ChatFormatter formatter : this.formatters) {
            result = formatter.format(source, sourceDisplayName, result, viewer);
        }
        return result;
    }

    /**
     * Checks if this format is viewer-unaware (more efficient).
     */
    boolean isViewerUnaware() {
        return this.viewerUnawareFormatter != null;
    }

    /**
     * Builder for constructing chat formats.
     */
    public static final class Builder {
        private final List<ChatFormatter> formatters = new ArrayList<>();
        private ChatFormatter.ViewerUnawareChatFormatter viewerUnawareFormatter;

        private Builder() {
        }

        /**
         * Adds a custom formatter that can produce different output per viewer.
         */
        public @NotNull Builder format(final @NotNull ChatFormatter formatter) {
            if (this.viewerUnawareFormatter != null) {
                throw new IllegalStateException("Cannot mix viewer-aware and viewer-unaware formatters");
            }
            this.formatters.add(formatter);
            return this;
        }

        /**
         * Adds a viewer-unaware formatter (same output for all viewers, more efficient).
         */
        public @NotNull Builder formatUnaware(final @NotNull ChatFormatter.ViewerUnawareChatFormatter formatter) {
            if (!this.formatters.isEmpty()) {
                throw new IllegalStateException("Cannot mix viewer-aware and viewer-unaware formatters");
            }
            this.viewerUnawareFormatter = formatter;
            return this;
        }

        /**
         * Adds a prefix before the message.
         */
        public @NotNull Builder prefix(final @NotNull Prefix prefix) {
            return format((source, displayName, message, viewer) -> prefix.apply(message));
        }

        /**
         * Adds a prefix parsed from MiniMessage.
         */
        public @NotNull Builder prefix(final @NotNull TextEngine engine, final @NotNull String prefixMiniMessage) {
            return prefix(Prefix.miniMessage(engine, prefixMiniMessage));
        }

        /**
         * Prepends a component to the message.
         */
        public @NotNull Builder prepend(final @NotNull Component component) {
            return format((source, displayName, message, viewer) -> component.append(message));
        }

        /**
         * Prepends a component parsed from MiniMessage.
         */
        public @NotNull Builder prepend(final @NotNull TextEngine engine, final @NotNull String miniMessage) {
            return prepend(engine.parse(miniMessage));
        }

        /**
         * Appends a component to the message.
         */
        public @NotNull Builder append(final @NotNull Component component) {
            return format((source, displayName, message, viewer) -> message.append(component));
        }

        /**
         * Appends a component parsed from MiniMessage.
         */
        public @NotNull Builder append(final @NotNull TextEngine engine, final @NotNull String miniMessage) {
            return append(engine.parse(miniMessage));
        }

        /**
         * Transforms the source display name using the provided function.
         */
        public @NotNull Builder transformDisplayName(final @NotNull Function<Component, Component> transformer) {
            return format((source, displayName, message, viewer) -> {
                final Component transformed = transformer.apply(displayName);
                return transformed.append(Component.text(": ")).append(message);
            });
        }

        /**
         * Transforms the message using the provided function.
         */
        public @NotNull Builder transformMessage(final @NotNull Function<Component, Component> transformer) {
            return format((source, displayName, message, viewer) -> {
                final Component transformed = transformer.apply(message);
                return displayName.append(Component.text(": ")).append(transformed);
            });
        }

        /**
         * Adds a separator between the display name and message.
         */
        public @NotNull Builder separator(final @NotNull Component separator) {
            return format((source, displayName, message, viewer) ->
                    displayName.append(separator).append(message));
        }

        /**
         * Adds a separator parsed from MiniMessage.
         */
        public @NotNull Builder separator(final @NotNull TextEngine engine, final @NotNull String separatorMiniMessage) {
            return separator(engine.parse(separatorMiniMessage));
        }

        /**
         * Adds a separator parsed from MiniMessage (viewer-unaware).
         */
        public @NotNull Builder separatorUnaware(final @NotNull TextEngine engine, final @NotNull String separatorMiniMessage) {
            final Component separator = engine.parse(separatorMiniMessage);
            return formatUnaware((source, displayName, message) ->
                    displayName.append(separator).append(message));
        }

        /**
         * Builds the chat format.
         */
        public @NotNull ChatFormat build() {
            if (this.formatters.isEmpty() && this.viewerUnawareFormatter == null) {
                return simple();
            }
            return new ChatFormat(this.formatters, this.viewerUnawareFormatter);
        }
    }
}

