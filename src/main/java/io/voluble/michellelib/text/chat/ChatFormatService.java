package io.voluble.michellelib.text.chat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.chat.ChatRenderer.ViewerUnaware;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing global chat formats.
 *
 * <p>This service registers a listener that applies configured chat formats to all
 * chat messages. Multiple formats can be registered, and they will be applied in order.</p>
 *
 * <p>Thread-safe for concurrent format registration/unregistration.</p>
 */
public final class ChatFormatService implements Listener {

    private final List<ChatFormat> formats = new CopyOnWriteArrayList<>();
    private volatile boolean enabled = true;

    /**
     * Creates a new chat format service.
     */
    public ChatFormatService() {
    }

    /**
     * Registers a chat format to be applied to all chat messages.
     *
     * @param format the format to register
     */
    public void registerFormat(final @NotNull ChatFormat format) {
        this.formats.add(format);
    }

    /**
     * Unregisters a chat format.
     *
     * @param format the format to unregister
     */
    public void unregisterFormat(final @NotNull ChatFormat format) {
        this.formats.remove(format);
    }

    /**
     * Clears all registered formats.
     */
    public void clearFormats() {
        this.formats.clear();
    }

    /**
     * Enables or disables chat formatting.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if chat formatting is enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Gets a copy of all registered formats.
     */
    @NotNull
    public List<ChatFormat> getFormats() {
        return new ArrayList<>(this.formats);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    private void onAsyncChat(final @NotNull AsyncChatEvent event) {
        if (!this.enabled || this.formats.isEmpty()) {
            return;
        }

        final ChatRenderer existingRenderer = event.renderer();
        final ChatRenderer compositeRenderer = createCompositeRenderer(existingRenderer, this.formats);
        event.renderer(compositeRenderer);
    }

    private static @NotNull ChatRenderer createCompositeRenderer(
            final @Nullable ChatRenderer existingRenderer,
            final @NotNull List<ChatFormat> formats
    ) {
        final boolean allViewerUnaware = formats.stream().allMatch(ChatFormat::isViewerUnaware)
                && (existingRenderer == null || existingRenderer instanceof ViewerUnaware);

        if (allViewerUnaware) {
            return ChatRenderer.viewerUnaware(
                    new CompositeViewerUnawareRenderer(existingRenderer, formats));
        }
        return new CompositeChatRenderer(existingRenderer, formats);
    }

    private static final class CompositeChatRenderer implements ChatRenderer {
        private final @Nullable ChatRenderer existingRenderer;
        private final @NotNull List<ChatFormat> formats;

        CompositeChatRenderer(
                final @Nullable ChatRenderer existingRenderer,
                final @NotNull List<ChatFormat> formats
        ) {
            this.existingRenderer = existingRenderer;
            this.formats = formats;
        }

        @Override
        public @NotNull Component render(
                final @NotNull Player source,
                final @NotNull Component sourceDisplayName,
                final @NotNull Component message,
                final @NotNull Audience viewer
        ) {
            Component result = message;

            if (this.existingRenderer != null) {
                result = this.existingRenderer.render(source, sourceDisplayName, result, viewer);
            }

            for (final ChatFormat format : this.formats) {
                result = format.render(source, sourceDisplayName, result, viewer);
            }

            return result;
        }
    }

    private static final class CompositeViewerUnawareRenderer implements ViewerUnaware {
        private final @Nullable ChatRenderer existingRenderer;
        private final @NotNull List<ChatFormat> formats;

        CompositeViewerUnawareRenderer(
                final @Nullable ChatRenderer existingRenderer,
                final @NotNull List<ChatFormat> formats
        ) {
            this.existingRenderer = existingRenderer;
            this.formats = formats;
        }

        @Override
        public @NotNull Component render(
                final @NotNull Player source,
                final @NotNull Component sourceDisplayName,
                final @NotNull Component message
        ) {
            Component result = message;

            if (this.existingRenderer instanceof ViewerUnaware viewerUnaware) {
                result = viewerUnaware.render(source, sourceDisplayName, result);
            } else if (this.existingRenderer != null) {
                result = this.existingRenderer.render(source, sourceDisplayName, result, source);
            }

            for (final ChatFormat format : this.formats) {
                result = format.render(source, sourceDisplayName, result, source);
            }

            return result;
        }
    }
}

