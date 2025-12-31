package io.voluble.michellelib.registry;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Fluent registry access helper for advanced registry operations.
 *
 * <p>Provides a fluent API for complex registry queries, caching, and batch operations.</p>
 */
public final class RegistryHelper {

    private RegistryHelper() {}

    /**
     * Creates a registry query builder for advanced operations.
     *
     * @param registryKey the registry to query
     * @param <T> the entry type
     * @return a new query builder
     */
    @NotNull
    public static <T extends Keyed> QueryBuilder<T> query(@NotNull RegistryKey<T> registryKey) {
        return new QueryBuilder<>(registryKey);
    }

    /**
     * Fluent query builder for registry operations.
     */
    public static final class QueryBuilder<T extends Keyed> {
        private final RegistryKey<T> registryKey;
        private final List<QueryFilter<T>> filters = new ArrayList<>();
        private Comparator<T> sorter;
        private int limit = -1;

        private QueryBuilder(@NotNull RegistryKey<T> registryKey) {
            this.registryKey = registryKey;
        }

        /**
         * Adds a filter to match entries by key namespace.
         */
        @NotNull
        public QueryBuilder<T> namespace(@NotNull String namespace) {
            filters.add(entry -> namespace.equals(entry.key().namespace()));
            return this;
        }

        /**
         * Adds a filter to match entries by key value.
         */
        @NotNull
        public QueryBuilder<T> value(@NotNull String value) {
            filters.add(entry -> value.equals(entry.key().value()));
            return this;
        }

        /**
         * Adds a custom predicate filter.
         */
        @NotNull
        public QueryBuilder<T> where(@NotNull java.util.function.Predicate<T> predicate) {
            filters.add(predicate);
            return this;
        }

        /**
         * Adds a sorter for the results.
         */
        @NotNull
        public QueryBuilder<T> sorted(@NotNull Comparator<T> comparator) {
            this.sorter = comparator;
            return this;
        }

        /**
         * Sorts results by key namespace, then value.
         */
        @NotNull
        public QueryBuilder<T> sortedByKey() {
            this.sorter = Comparator.comparing((T entry) -> entry.key().namespace())
                                   .thenComparing(entry -> entry.key().value());
            return this;
        }

        /**
         * Limits the number of results.
         */
        @NotNull
        public QueryBuilder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Executes the query and returns matching entries as a list.
         */
        @NotNull
        public List<T> toList() {
            Registry<T> registry = Registries.registry(registryKey);
            Stream<T> stream = registry.stream();

            // Apply filters
            for (QueryFilter<T> filter : filters) {
                stream = stream.filter(filter);
            }

            // Apply sorting
            if (sorter != null) {
                stream = stream.sorted(sorter);
            }

            // Apply limit
            if (limit > 0) {
                stream = stream.limit(limit);
            }

            return stream.toList();
        }

        /**
         * Executes the query and returns the first matching entry.
         */
        @NotNull
        public Optional<T> first() {
            return toList().stream().findFirst();
        }

        /**
         * Executes the query and returns the count of matching entries.
         */
        public int count() {
            return toList().size();
        }
    }

    /**
     * Internal filter interface.
     */
    @FunctionalInterface
    private interface QueryFilter<T extends Keyed> extends java.util.function.Predicate<T> {}

    /**
     * Creates a cached registry accessor for frequently accessed entries.
     *
     * @param registryKey the registry to cache
     * @param <T> the entry type
     * @return a cached accessor
     */
    @NotNull
    public static <T extends Keyed> CachedRegistry<T> cached(@NotNull RegistryKey<T> registryKey) {
        return new CachedRegistry<>(registryKey);
    }

    /**
     * Cached registry accessor for improved performance on frequently accessed entries.
     */
    public static final class CachedRegistry<T extends Keyed> {
        private final RegistryKey<T> registryKey;
        private final Map<Key, T> cache = new HashMap<>();
        private volatile boolean cachePopulated = false;

        private CachedRegistry(@NotNull RegistryKey<T> registryKey) {
            this.registryKey = registryKey;
        }

        /**
         * Gets an entry from the cache, populating it if necessary.
         */
        @NotNull
        public Optional<T> get(@NotNull Key key) {
            ensureCachePopulated();
            return Optional.ofNullable(cache.get(key));
        }

        /**
         * Gets an entry by string key from the cache.
         */
        @NotNull
        public Optional<T> get(@NotNull String key) {
            Key parsedKey = key.contains(":") ? Key.key(key) : Key.key("minecraft", key);
            return get(parsedKey);
        }

        /**
         * Clears the cache, forcing it to be repopulated on next access.
         */
        public void invalidate() {
            cache.clear();
            cachePopulated = false;
        }

        /**
         * Gets the number of cached entries.
         */
        public int size() {
            ensureCachePopulated();
            return cache.size();
        }

        private void ensureCachePopulated() {
            if (!cachePopulated) {
                synchronized (this) {
                    if (!cachePopulated) {
                        Registry<T> registry = Registries.registry(registryKey);
                        for (T entry : registry) {
                            cache.put(entry.key(), entry);
                        }
                        cachePopulated = true;
                    }
                }
            }
        }
    }

    /**
     * Creates a batch registry accessor for multiple lookups.
     *
     * @param registryKey the registry to access
     * @param <T> the entry type
     * @return a batch accessor
     */
    @NotNull
    public static <T extends Keyed> BatchRegistry<T> batch(@NotNull RegistryKey<T> registryKey) {
        return new BatchRegistry<>(registryKey);
    }

    /**
     * Batch registry accessor for efficient multiple lookups.
     */
    public static final class BatchRegistry<T extends Keyed> {
        private final RegistryKey<T> registryKey;
        private final Map<Key, T> results = new HashMap<>();

        private BatchRegistry(@NotNull RegistryKey<T> registryKey) {
            this.registryKey = registryKey;
        }

        /**
         * Adds a key to look up.
         */
        @NotNull
        public BatchRegistry<T> lookup(@NotNull Key key) {
            results.put(key, null); // Placeholder, will be filled on resolve
            return this;
        }

        /**
         * Adds a string key to look up.
         */
        @NotNull
        public BatchRegistry<T> lookup(@NotNull String key) {
            Key parsedKey = key.contains(":") ? Key.key(key) : Key.key("minecraft", key);
            return lookup(parsedKey);
        }

        /**
         * Resolves all lookups and returns the results map.
         */
        @NotNull
        public Map<Key, Optional<T>> resolve() {
            Registry<T> registry = Registries.registry(registryKey);
            Map<Key, Optional<T>> resolved = new HashMap<>();

            for (Key key : results.keySet()) {
                resolved.put(key, Optional.ofNullable(registry.get(key)));
            }

            return resolved;
        }

        /**
         * Resolves all lookups and returns only the found entries.
         */
        @NotNull
        public Map<Key, T> resolvePresent() {
            Map<Key, Optional<T>> resolved = resolve();
            Map<Key, T> present = new HashMap<>();

            resolved.forEach((key, optional) -> {
                optional.ifPresent(value -> present.put(key, value));
            });

            return present;
        }
    }
}
