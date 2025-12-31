package io.voluble.michellelib.cooldown;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.voluble.michellelib.config.YamlDocument;
import io.voluble.michellelib.config.YamlStore;
import io.voluble.michellelib.scheduler.PaperScheduler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Plugin-scoped cooldown service.
 *
 * <p>Read operations are safe from any thread. Persistence I/O must be scheduled asynchronously.</p>
 */
public final class CooldownService {
    private final Plugin plugin;
    private final PaperScheduler scheduler;
    private final CooldownStore store;
    private final CooldownClock clock;
    private final @Nullable CooldownPersistence persistence;

    private final CopyOnWriteArrayList<ScheduledTask> repeatingTasks = new CopyOnWriteArrayList<>();

    private CooldownService(
            final @NotNull Plugin plugin,
            final @NotNull CooldownClock clock,
            final @NotNull CooldownStore store,
            final @Nullable CooldownPersistence persistence
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.store = Objects.requireNonNull(store, "store");
        this.persistence = persistence;
        this.scheduler = new PaperScheduler(plugin);
    }

    public static @NotNull Builder builder(final @NotNull Plugin plugin) {
        return new Builder(plugin);
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    public @NotNull PaperScheduler scheduler() {
        return scheduler;
    }

    public @NotNull CooldownStore store() {
        return store;
    }

    public @NotNull CooldownClock clock() {
        return clock;
    }

    public @Nullable CooldownPersistence persistence() {
        return persistence;
    }

    public @NotNull CooldownResult tryUse(final @NotNull UUID owner, final @NotNull String key, final @NotNull Duration cooldown) {
        return store.tryUse(new CooldownId(owner, key), cooldown);
    }

    public @NotNull CooldownResult tryUse(final @NotNull UUID owner, final @NotNull CooldownKey key, final @NotNull Duration cooldown) {
        return store.tryUse(CooldownId.of(owner, key), cooldown);
    }

    public @NotNull CooldownResult tryUse(final @NotNull Player player, final @NotNull String key, final @NotNull Duration cooldown) {
        Objects.requireNonNull(player, "player");
        return tryUse(player.getUniqueId(), key, cooldown);
    }

    public @NotNull CooldownResult tryUse(final @NotNull Player player, final @NotNull CooldownKey key, final @NotNull Duration cooldown) {
        Objects.requireNonNull(player, "player");
        return tryUse(player.getUniqueId(), key, cooldown);
    }

    public @NotNull Duration remaining(final @NotNull UUID owner, final @NotNull String key) {
        return store.remaining(new CooldownId(owner, key));
    }

    public @NotNull Duration remaining(final @NotNull UUID owner, final @NotNull CooldownKey key) {
        return store.remaining(CooldownId.of(owner, key));
    }

    public boolean isCooling(final @NotNull UUID owner, final @NotNull String key) {
        return !remaining(owner, key).isZero();
    }

    public void clear(final @NotNull UUID owner, final @NotNull String key) {
        store.clear(new CooldownId(owner, key));
    }

    public void clear(final @NotNull UUID owner, final @NotNull CooldownKey key) {
        store.clear(CooldownId.of(owner, key));
    }

    public void clearOwner(final @NotNull UUID owner) {
        store.clearOwner(owner);
    }

    public void clearAll() {
        store.clearAll();
    }

    public int pruneExpired() {
        return store.pruneExpired();
    }

    public void load() {
        if (persistence == null) {
            return;
        }
        store.clearAll();
        persistence.loadInto(store);
    }

    public void loadAsync(final @Nullable Consumer<Throwable> onError) {
        if (persistence == null) {
            return;
        }
        scheduler.runAsync(() -> {
            try {
                load();
            } catch (final Throwable t) {
                if (onError != null) {
                    onError.accept(t);
                } else {
                    plugin.getLogger().severe("Failed to load cooldowns: " + t.getMessage());
                }
            }
        });
    }

    public void save() {
        if (persistence == null) {
            return;
        }
        persistence.saveFrom(store);
    }

    public void saveAsync(final @Nullable Consumer<Throwable> onError) {
        if (persistence == null) {
            return;
        }
        scheduler.runAsync(() -> {
            try {
                save();
            } catch (final Throwable t) {
                if (onError != null) {
                    onError.accept(t);
                } else {
                    plugin.getLogger().severe("Failed to save cooldowns: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Cancels repeating tasks and performs a final async save if persistence is configured.
     */
    public void shutdown() {
        for (final ScheduledTask t : repeatingTasks) {
            try {
                t.cancel();
            } catch (final Throwable ignored) {
            }
        }
        repeatingTasks.clear();
        saveAsync(null);
    }

    public static final class Builder {
        private final Plugin plugin;

        private CooldownClock clock = CooldownClock.system();
        private CooldownStore store;
        private @Nullable CooldownPersistence persistence;

        private @Nullable Duration autosaveEvery;
        private @Nullable Duration pruneEvery;

        private Builder(final @NotNull Plugin plugin) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
        }

        public @NotNull Builder clock(final @NotNull CooldownClock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public @NotNull Builder store(final @NotNull CooldownStore store) {
            this.store = Objects.requireNonNull(store, "store");
            return this;
        }

        public @NotNull Builder persistence(final @Nullable CooldownPersistence persistence) {
            this.persistence = persistence;
            return this;
        }

        public @NotNull Builder yamlPersistence(final @NotNull String filePath) {
            Objects.requireNonNull(filePath, "filePath");
            final YamlDocument doc = YamlStore.create(plugin).yaml(filePath).copyDefaultsFromResource(filePath);
            this.persistence = new YamlCooldownPersistence(doc);
            return this;
        }

        public @NotNull Builder yamlPersistence(final @NotNull YamlDocument document) {
            this.persistence = new YamlCooldownPersistence(Objects.requireNonNull(document, "document"));
            return this;
        }

        public @NotNull Builder autosaveEvery(final @Nullable Duration period) {
            this.autosaveEvery = period;
            return this;
        }

        public @NotNull Builder pruneEvery(final @Nullable Duration period) {
            this.pruneEvery = period;
            return this;
        }

        public @NotNull CooldownService build() {
            final CooldownStore store = this.store != null ? this.store : new InMemoryCooldownStore(clock);
            final CooldownService service = new CooldownService(plugin, clock, store, persistence);
            service.installRepeatingTasks(autosaveEvery, pruneEvery);
            return service;
        }

        public @NotNull CooldownService buildAndLoadAsync() {
            final CooldownService service = build();
            service.loadAsync(null);
            return service;
        }
    }

    private void installRepeatingTasks(final @Nullable Duration autosaveEvery, final @Nullable Duration pruneEvery) {
        if (pruneEvery != null && !pruneEvery.isNegative() && !pruneEvery.isZero()) {
            final long ticks = Math.max(1L, pruneEvery.toMillis() / 50L);
            repeatingTasks.add(scheduler.runGlobalAtFixedRate(task -> store.pruneExpired(), ticks, ticks));
        }

        if (persistence != null && autosaveEvery != null && !autosaveEvery.isNegative() && !autosaveEvery.isZero()) {
            final long ticks = Math.max(1L, autosaveEvery.toMillis() / 50L);
            repeatingTasks.add(scheduler.runGlobalAtFixedRate(task -> saveAsync(null), ticks, ticks));
        }
    }
}



