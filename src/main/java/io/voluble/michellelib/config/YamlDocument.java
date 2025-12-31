package io.voluble.michellelib.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents one YAML file inside a plugin's data folder.
 *
 * <p>Reload and save touch disk and should not be done on region/entity threads.</p>
 */
public final class YamlDocument {
    private final Plugin plugin;
    private final String relativePath;
    private final Path filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicReference<YamlSnapshot> snapshot = new AtomicReference<>(YamlSnapshot.empty());

    private volatile @Nullable String defaultsResourcePath;
    private volatile @NotNull YamlConfiguration yaml = new YamlConfiguration();

    YamlDocument(final @NotNull Plugin plugin, final @NotNull String relativePath, final @NotNull Path filePath) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.relativePath = Objects.requireNonNull(relativePath, "relativePath");
        this.filePath = Objects.requireNonNull(filePath, "filePath");
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    public @NotNull String relativePath() {
        return relativePath;
    }

    public @NotNull Path filePath() {
        return filePath;
    }

    public @NotNull File file() {
        return filePath.toFile();
    }

    /**
     * Configures this document to copy a default resource into place when the file does not exist.
     *
     * <p>The resource path is resolved within the owning plugin's JAR.</p>
     */
    public @NotNull YamlDocument copyDefaultsFromResource(final @NotNull String resourcePath) {
        this.defaultsResourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
        return this;
    }

    /**
     * Reloads the document from disk, creating parent directories as needed.
     *
     * <p>If {@link #copyDefaultsFromResource(String)} was configured and the target file does not exist,
     * the resource will be copied first.</p>
     */
    public void reload() {
        final Path parent = filePath.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }

            final @Nullable String resourcePath = this.defaultsResourcePath;
            if (resourcePath != null) {
                ResourceFileCopier.copyIfMissing(plugin, resourcePath, filePath);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            final YamlConfiguration loaded = new YamlConfiguration();
            loaded.load(filePath.toFile());

            lock.writeLock().lock();
            try {
                this.yaml = loaded;
            } finally {
                lock.writeLock().unlock();
            }

            snapshot.set(toSnapshot(loaded));
        } catch (final IOException e) {
            throw new YamlDocumentException("Failed to load YAML file: " + relativePath, e);
        } catch (final InvalidConfigurationException e) {
            throw new YamlDocumentException("Invalid YAML in file: " + relativePath, e);
        }
    }

    /**
     * Saves the current in-memory YAML to disk.
     */
    public void save() {
        lock.readLock().lock();
        try {
            final Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            this.yaml.save(filePath.toFile());
        } catch (final IOException e) {
            throw new YamlDocumentException("Failed to save YAML file: " + relativePath, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the current in-memory YAML configuration.
     *
     * <p>The returned object must not be mutated concurrently with {@link #reload()} or {@link #save()}.</p>
     */
    @Deprecated(forRemoval = false)
    public @NotNull YamlConfiguration yaml() {
        lock.readLock().lock();
        try {
            return this.yaml;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the current immutable snapshot of this YAML document.
     */
    public @NotNull YamlSnapshot snapshot() {
        return snapshot.get();
    }

    /**
     * Runs a read-only operation against the live YAML document under lock.
     */
    public <T> @NotNull T read(final @NotNull Function<YamlConfiguration, T> reader) {
        Objects.requireNonNull(reader, "reader");
        lock.readLock().lock();
        try {
            return Objects.requireNonNull(reader.apply(this.yaml), "result");
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Applies a mutation to the live YAML document under lock, then refreshes the snapshot.
     */
    public void edit(final @NotNull Consumer<YamlConfiguration> editor) {
        Objects.requireNonNull(editor, "editor");
        lock.writeLock().lock();
        try {
            editor.accept(this.yaml);
            snapshot.set(toSnapshot(this.yaml));
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static @NotNull YamlSnapshot toSnapshot(final @NotNull YamlConfiguration yaml) {
        final Map<String, Object> root = new LinkedHashMap<>();
        for (final String key : yaml.getKeys(false)) {
            final Object raw = yaml.get(key);
            root.put(key, freezeValue(raw));
        }
        return root.isEmpty() ? YamlSnapshot.empty() : new YamlSnapshot(root);
    }

    private static @NotNull Object freezeValue(final @Nullable Object raw) {
        if (raw instanceof MemorySection section) {
            final Map<String, Object> map = new LinkedHashMap<>();
            for (final String k : section.getKeys(false)) {
                map.put(k, freezeValue(section.get(k)));
            }
            return Map.copyOf(map);
        }
        return YamlSnapshot.deepFreeze(raw);
    }
}


