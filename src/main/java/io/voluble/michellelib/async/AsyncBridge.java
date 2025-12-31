package io.voluble.michellelib.async;

import io.voluble.michellelib.scheduler.PaperScheduler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities for bridging async operations back to the sync thread.
 *
 * <p>Common pattern: perform async work (database, file I/O, API calls) then
 * apply the result on the sync thread (send messages, modify world state).</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * AsyncBridge.executeAsyncThenSync(scheduler, plugin,
 *     () -> {
 *         // Async work - database query, file I/O, etc.
 *         return database.loadPlayerData(uuid);
 *     },
 *     (player, data) -> {
 *         // Sync work - send message, modify inventory, etc.
 *         player.sendMessage("Loaded: " + data);
 *     },
 *     player
 * );
 * }</pre>
 */
public final class AsyncBridge {
    private AsyncBridge() {
    }

    /**
     * Executes async work then applies the result on the sync thread.
     *
     * @param scheduler the scheduler to use
     * @param plugin the plugin instance
     * @param asyncWork the async work to perform
     * @param syncCallback the callback to run on the sync thread with the result
     * @param <T> the result type
     */
    public static <T> void executeAsyncThenSync(
            final @NotNull PaperScheduler scheduler,
            final @NotNull Plugin plugin,
            final @NotNull Supplier<T> asyncWork,
            final @NotNull Consumer<T> syncCallback
    ) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(asyncWork, "asyncWork");
        Objects.requireNonNull(syncCallback, "syncCallback");

        scheduler.runAsync(() -> {
            final T result = asyncWork.get();
            scheduler.runGlobal(() -> syncCallback.accept(result));
        });
    }

    /**
     * Executes async work then applies the result on the sync thread with context.
     *
     * <p>Useful when you need to pass context (e.g., a Player) to both async and sync operations.</p>
     *
     * @param scheduler the scheduler to use
     * @param plugin the plugin instance
     * @param asyncWork the async work to perform
     * @param syncCallback the callback to run on the sync thread with result and context
     * @param context the context object to pass to both operations
     * @param <T> the result type
     * @param <C> the context type
     */
    public static <T, C> void executeAsyncThenSync(
            final @NotNull PaperScheduler scheduler,
            final @NotNull Plugin plugin,
            final @NotNull Function<C, T> asyncWork,
            final @NotNull Consumer2<T, C> syncCallback,
            final @NotNull C context
    ) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(asyncWork, "asyncWork");
        Objects.requireNonNull(syncCallback, "syncCallback");
        Objects.requireNonNull(context, "context");

        scheduler.runAsync(() -> {
            final T result = asyncWork.apply(context);
            scheduler.runGlobal(() -> syncCallback.accept(result, context));
        });
    }

    /**
     * Executes async work then applies the result on the entity's thread.
     *
     * <p>Useful when you need to modify entity state after async work.</p>
     *
     * @param scheduler the scheduler to use
     * @param plugin the plugin instance
     * @param entity the entity to run the sync callback on
     * @param asyncWork the async work to perform
     * @param syncCallback the callback to run on the entity's thread
     * @param <T> the result type
     */
    public static <T> void executeAsyncThenEntity(
            final @NotNull PaperScheduler scheduler,
            final @NotNull Plugin plugin,
            final @NotNull Entity entity,
            final @NotNull Supplier<T> asyncWork,
            final @NotNull Consumer<T> syncCallback
    ) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(asyncWork, "asyncWork");
        Objects.requireNonNull(syncCallback, "syncCallback");

        scheduler.runAsync(() -> {
            final T result = asyncWork.get();
            scheduler.runEntity(entity, () -> syncCallback.accept(result));
        });
    }

    /**
     * Executes async work then applies the result on the player's thread.
     *
     * <p>Convenience method for Player-specific operations.</p>
     *
     * @param scheduler the scheduler to use
     * @param plugin the plugin instance
     * @param player the player to run the sync callback on
     * @param asyncWork the async work to perform
     * @param syncCallback the callback to run on the player's thread
     * @param <T> the result type
     */
    public static <T> void executeAsyncThenPlayer(
            final @NotNull PaperScheduler scheduler,
            final @NotNull Plugin plugin,
            final @NotNull Player player,
            final @NotNull Supplier<T> asyncWork,
            final @NotNull Consumer<T> syncCallback
    ) {
        executeAsyncThenEntity(scheduler, plugin, player, asyncWork, syncCallback);
    }

    /**
     * Creates a CompletableFuture that executes async work then completes on the sync thread.
     *
     * <p>Useful for composing async operations with CompletableFuture API.</p>
     *
     * @param scheduler the scheduler to use
     * @param plugin the plugin instance
     * @param asyncWork the async work to perform
     * @param <T> the result type
     * @return a CompletableFuture that completes on the sync thread
     */
    @NotNull
    public static <T> CompletableFuture<T> futureAsyncThenSync(
            final @NotNull PaperScheduler scheduler,
            final @NotNull Plugin plugin,
            final @NotNull Supplier<T> asyncWork
    ) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(asyncWork, "asyncWork");

        final CompletableFuture<T> future = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                final T result = asyncWork.get();
                scheduler.runGlobal(() -> future.complete(result));
            } catch (final Exception e) {
                scheduler.runGlobal(() -> future.completeExceptionally(e));
            }
        });
        return future;
    }

    /**
     * Functional interface for a two-parameter consumer.
     */
    @FunctionalInterface
    public interface Consumer2<T, U> {
        void accept(T t, U u);
    }
}

