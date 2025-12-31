package io.voluble.michellelib.config;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin-scoped YAML file registry that resolves relative paths inside the plugin data folder.
 */
public final class YamlStore {
    private final Plugin plugin;
    private final Path dataFolder;
    private final Map<String, YamlDocument> documents = new ConcurrentHashMap<>();

    private YamlStore(final @NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.dataFolder = plugin.getDataFolder().toPath();
    }

    public static @NotNull YamlStore create(final @NotNull Plugin plugin) {
        return new YamlStore(plugin);
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    /**
     * Returns (and caches) a YAML document by its relative path.
     *
     * <p>Paths are resolved under the plugin data folder. Nested directories are supported.</p>
     */
    public @NotNull YamlDocument yaml(final @NotNull String relativePath) {
        Objects.requireNonNull(relativePath, "relativePath");
        final String normalized = normalize(relativePath);
        return documents.computeIfAbsent(normalized, p -> new YamlDocument(plugin, p, dataFolder.resolve(p)));
    }

    private static @NotNull String normalize(final @NotNull String relativePath) {
        String p = relativePath.replace('\\', '/');
        while (p.startsWith("/")) p = p.substring(1);
        if (p.isEmpty()) {
            throw new IllegalArgumentException("relativePath must not be empty");
        }
        if (p.contains(":")) {
            throw new IllegalArgumentException("relativePath must be relative and must not contain ':'");
        }
        if (p.startsWith("..") || p.contains("/../") || p.contains("../") || p.contains("/..")) {
            throw new IllegalArgumentException("relativePath must not traverse directories");
        }
        return p;
    }
}


