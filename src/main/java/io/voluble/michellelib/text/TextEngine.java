package io.voluble.michellelib.text;

import io.voluble.michellelib.text.placeholders.PlaceholderApi;
import io.voluble.michellelib.text.placeholders.PlaceholderResolvers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configurable text "engine" for parsing/serializing MiniMessage in a consistent way.
 *
 * <p>MichelleLib does not ship your prefixes/palettes/messages. Instead, you build an engine with the
 * policies you want (tags, strictness, placeholder behavior) and reuse it.</p>
 */
public final class TextEngine {

    private final @NotNull MiniMessage miniMessage;
    private final @NotNull MiniMessage userMiniMessage;
    private final boolean expandPlaceholderApiPercents;
    private final boolean enablePapiTag;

    private TextEngine(
            final @NotNull MiniMessage miniMessage,
            final @NotNull MiniMessage userMiniMessage,
            final boolean expandPlaceholderApiPercents,
            final boolean enablePapiTag
    ) {
        this.miniMessage = miniMessage;
        this.userMiniMessage = userMiniMessage;
        this.expandPlaceholderApiPercents = expandPlaceholderApiPercents;
        this.enablePapiTag = enablePapiTag;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @NotNull MiniMessage miniMessage() {
        return this.miniMessage;
    }

    public @NotNull MiniMessage userMiniMessage() {
        return this.userMiniMessage;
    }

    public @NotNull Component parse(final @Nullable String input, final @NotNull TagResolver... extraResolvers) {
        final String message = input == null ? "" : input;
        return this.miniMessage.deserialize(message, extraResolvers);
    }

    public @NotNull Component parse(final @Nullable Player player, final @Nullable String input, final @NotNull TagResolver... extraResolvers) {
        if (player == null) {
            return parse(input, extraResolvers);
        }

        String message = input == null ? "" : input;
        if (this.expandPlaceholderApiPercents) {
            message = PlaceholderApi.expand(player, message);
        }

        if (this.enablePapiTag) {
            final TagResolver papi = PlaceholderResolvers.papiTag(player);
            final TagResolver[] all = new TagResolver[extraResolvers.length + 1];
            all[0] = papi;
            System.arraycopy(extraResolvers, 0, all, 1, extraResolvers.length);
            return this.miniMessage.deserialize(message, player, PlaceholderResolvers.combine(all));
        }

        return this.miniMessage.deserialize(message, player, extraResolvers);
    }

    /**
     * Parses user-controlled MiniMessage using the engine's dedicated "user" MiniMessage instance.
     * (Typically configured with a restricted tag allowlist.)
     */
    public @NotNull Component parseUser(final @Nullable String input, final @NotNull TagResolver... extraResolvers) {
        final String message = input == null ? "" : input;
        return this.userMiniMessage.deserialize(message, extraResolvers);
    }

    public @NotNull String serialize(final @NotNull Component component) {
        return this.miniMessage.serialize(component);
    }

    public static final class Builder {
        private MiniMessage miniMessage = MiniMessage.miniMessage();
        private MiniMessage userMiniMessage = MiniMessage.miniMessage();
        private boolean expandPlaceholderApiPercents = true;
        private boolean enablePapiTag = true;

        private Builder() {
        }

        public @NotNull Builder miniMessage(final @NotNull MiniMessage miniMessage) {
            this.miniMessage = miniMessage;
            return this;
        }

        public @NotNull Builder userMiniMessage(final @NotNull MiniMessage userMiniMessage) {
            this.userMiniMessage = userMiniMessage;
            return this;
        }

        public @NotNull Builder expandPlaceholderApiPercents(final boolean enabled) {
            this.expandPlaceholderApiPercents = enabled;
            return this;
        }

        public @NotNull Builder enablePapiTag(final boolean enabled) {
            this.enablePapiTag = enabled;
            return this;
        }

        public @NotNull TextEngine build() {
            return new TextEngine(this.miniMessage, this.userMiniMessage, this.expandPlaceholderApiPercents, this.enablePapiTag);
        }
    }
}



