package io.voluble.michellelib.text.message;

import io.voluble.michellelib.config.YamlDocument;
import io.voluble.michellelib.config.YamlSnapshot;
import io.voluble.michellelib.config.YamlStore;
import io.voluble.michellelib.scheduler.PaperScheduler;
import io.voluble.michellelib.text.TextEngine;
import io.voluble.michellelib.text.prefix.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * YAML-backed message service with MiniMessage parsing.
 *
 * <p>Reload builds an immutable snapshot so message lookups are safe across threads.</p>
 */
public final class YamlMessages {
    private static final String DEFAULT_MISSING_TEMPLATE = "<red>Missing message: <white>{key}</white></red>";

    private final Plugin plugin;
    private final PaperScheduler scheduler;
    private final TextEngine engine;
    private final YamlDocument document;
    private final AtomicReference<MessageBundle> bundle = new AtomicReference<>(MessageBundle.empty());

    private volatile @Nullable Prefix prefix;
    private volatile @NotNull String missingTemplateMiniMessage = DEFAULT_MISSING_TEMPLATE;

    private YamlMessages(
            final @NotNull Plugin plugin,
            final @NotNull TextEngine engine,
            final @NotNull YamlDocument document
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.document = Objects.requireNonNull(document, "document");
        this.scheduler = new PaperScheduler(plugin);
    }

    public static @NotNull Builder builder(final @NotNull Plugin plugin, final @NotNull TextEngine engine) {
        return new Builder(plugin, engine);
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    public @NotNull PaperScheduler scheduler() {
        return scheduler;
    }

    public @NotNull YamlDocument document() {
        return document;
    }

    public @Nullable Prefix prefix() {
        return prefix;
    }

    public @NotNull MessageBundle bundle() {
        return bundle.get();
    }

    /**
     * Reloads from disk and swaps the in-memory snapshot.
     *
     * <p>This touches disk and should be called from the global thread or async scheduler.</p>
     */
    public void reload() {
        document.reload();
        bundle.set(MessageBundle.of(flatten(document.snapshot())));
    }

    public void reloadAsync() {
        reloadAsync(throwable -> plugin.getLogger().severe("Failed to reload messages: " + throwable.getMessage()));
    }

    public void reloadAsync(final @NotNull Consumer<Throwable> onError) {
        Objects.requireNonNull(onError, "onError");
        scheduler.runAsync(() -> {
            try {
                reload();
            } catch (final Throwable t) {
                onError.accept(t);
            }
        });
    }

    public @NotNull Component mini(final @NotNull String key, final @NotNull TagResolver... resolvers) {
        Objects.requireNonNull(key, "key");
        return engine.parse(rawOrMissing(key), resolvers);
    }

    public @NotNull Component mini(final @NotNull MessageKey key, final @NotNull TagResolver... resolvers) {
        Objects.requireNonNull(key, "key");
        return mini(key.path(), resolvers);
    }

    public @NotNull Component mini(final @Nullable Player player, final @NotNull String key, final @NotNull TagResolver... resolvers) {
        Objects.requireNonNull(key, "key");
        return engine.parse(player, rawOrMissing(key), resolvers);
    }

    public @NotNull Component mini(final @Nullable Player player, final @NotNull MessageKey key, final @NotNull TagResolver... resolvers) {
        Objects.requireNonNull(key, "key");
        return mini(player, key.path(), resolvers);
    }

    public @NotNull Component prefixed(final @Nullable Player player, final @NotNull String key, final @NotNull TagResolver... resolvers) {
        Objects.requireNonNull(key, "key");
        final Prefix p = this.prefix;
        if (p == null) {
            return mini(player, key, resolvers);
        }
        return p.apply(mini(player, key, resolvers));
    }

    public @NotNull Component prefixed(final @Nullable Player player, final @NotNull MessageKey key, final @NotNull TagResolver... resolvers) {
        Objects.requireNonNull(key, "key");
        return prefixed(player, key.path(), resolvers);
    }

    private @NotNull String rawOrMissing(final @NotNull String key) {
        final String raw = bundle.get().raw(key);
        if (raw != null) {
            return raw;
        }
        return missingTemplateMiniMessage.replace("{key}", key);
    }

    private static @NotNull Map<String, String> flatten(final @NotNull YamlSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        final Map<String, String> out = new LinkedHashMap<>();
        flattenInto(out, "", snapshot.get(""));
        return out;
    }

    private static void flattenInto(final @NotNull Map<String, String> out, final @NotNull String prefix, final @Nullable Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof Map<?, ?> map) {
            for (final Map.Entry<?, ?> e : map.entrySet()) {
                if (!(e.getKey() instanceof String k)) {
                    continue;
                }
                final String path = prefix.isEmpty() ? k : prefix + "." + k;
                flattenInto(out, path, e.getValue());
            }
            return;
        }

        if (value instanceof String s) {
            out.put(prefix, s);
            return;
        }

        if (value instanceof List<?> list && !list.isEmpty()) {
            boolean allStrings = true;
            for (final Object o : list) {
                if (!(o instanceof String)) {
                    allStrings = false;
                    break;
                }
            }
            if (allStrings) {
                @SuppressWarnings("unchecked")
                final List<String> lines = (List<String>) list;
                out.put(prefix, String.join("\n", lines));
            }
        }
    }

    public static final class Builder {
        private final Plugin plugin;
        private final TextEngine engine;

        private @Nullable YamlStore store;
        private @NotNull String filePath = "messages.yml";
        private @Nullable String defaultsResourcePath;
        private @Nullable Prefix prefix;
        private @NotNull String missingTemplateMiniMessage = DEFAULT_MISSING_TEMPLATE;

        private Builder(final @NotNull Plugin plugin, final @NotNull TextEngine engine) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
            this.engine = Objects.requireNonNull(engine, "engine");
        }

        public @NotNull Builder store(final @NotNull YamlStore store) {
            this.store = Objects.requireNonNull(store, "store");
            return this;
        }

        public @NotNull Builder file(final @NotNull String relativePath) {
            this.filePath = Objects.requireNonNull(relativePath, "relativePath");
            return this;
        }

        public @NotNull Builder defaultsFromResource(final @NotNull String resourcePath) {
            this.defaultsResourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
            return this;
        }

        public @NotNull Builder prefix(final @Nullable Prefix prefix) {
            this.prefix = prefix;
            return this;
        }

        public @NotNull Builder missingTemplateMiniMessage(final @NotNull String miniMessage) {
            this.missingTemplateMiniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
            return this;
        }

        public @NotNull YamlMessages build() {
            final YamlStore store = this.store != null ? this.store : YamlStore.create(plugin);
            final String defaults = this.defaultsResourcePath != null ? this.defaultsResourcePath : this.filePath;

            final YamlDocument doc = store.yaml(filePath).copyDefaultsFromResource(defaults);
            final YamlMessages messages = new YamlMessages(plugin, engine, doc);
            messages.prefix = this.prefix;
            messages.missingTemplateMiniMessage = this.missingTemplateMiniMessage;
            return messages;
        }

        public @NotNull YamlMessages buildAndLoad() {
            final YamlMessages messages = build();
            messages.reload();
            return messages;
        }
    }
}


