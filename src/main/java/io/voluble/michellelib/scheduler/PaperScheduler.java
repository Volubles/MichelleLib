package io.voluble.michellelib.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Paper-first scheduling abstraction.
 * <p>
 * This uses Paper's Folia schedulers (Entity/Region/Global/Async) even when running on normal Paper.
 * That aligns with the Paper docs: supporting Paper + Folia with one codepath and correct thread ownership.
 */
public final class PaperScheduler {
    private final Plugin plugin;

    public PaperScheduler(final @NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public @NotNull Plugin plugin() {
        return plugin;
    }

    // ---- Global region

    public void runGlobal(final @NotNull Runnable run) {
        Objects.requireNonNull(run, "run");
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, run);
    }

    public @NotNull ScheduledTask runGlobal(final @NotNull Consumer<ScheduledTask> task) {
        Objects.requireNonNull(task, "task");
        return plugin.getServer().getGlobalRegionScheduler().run(plugin, task);
    }

    public @NotNull ScheduledTask runGlobalDelayed(final @NotNull Consumer<ScheduledTask> task, final long delayTicks) {
        Objects.requireNonNull(task, "task");
        return plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task, delayTicks);
    }

    public @NotNull ScheduledTask runGlobalAtFixedRate(final @NotNull Consumer<ScheduledTask> task, final long initialDelayTicks, final long periodTicks) {
        Objects.requireNonNull(task, "task");
        return plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task, initialDelayTicks, periodTicks);
    }

    // ---- Region

    public void runRegion(final @NotNull Location location, final @NotNull Runnable run) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(run, "run");
        plugin.getServer().getRegionScheduler().execute(plugin, location, run);
    }

    // ---- Entity

    /**
     * Runs the given runnable immediately if the current thread owns the entity's region. Otherwise schedules it for next tick
     * on the entity scheduler (thread-safe, follows the entity).
     */
    public void runEntity(final @NotNull Entity entity, final @NotNull Runnable run) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(run, "run");
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            run.run();
            return;
        }
        // execute() treats delay < 1 as 1.
        entity.getScheduler().execute(plugin, run, null, 1L);
    }

    public void runEntityDelayed(final @NotNull Entity entity, final long delayTicks, final @NotNull Runnable run) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(run, "run");
        entity.getScheduler().execute(plugin, run, null, delayTicks);
    }

    public @NotNull ScheduledTask runEntityAtFixedRate(final @NotNull Entity entity, final @NotNull Consumer<ScheduledTask> task, final long initialDelayTicks, final long periodTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        return Objects.requireNonNull(entity.getScheduler().runAtFixedRate(plugin, task, null, initialDelayTicks, periodTicks), "scheduledTask");
    }

    // ---- Async

    public void runAsync(final @NotNull Runnable run) {
        Objects.requireNonNull(run, "run");
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> run.run());
    }

    public void runAsyncDelayed(final @NotNull Runnable run, final @NotNull Duration delay) {
        Objects.requireNonNull(run, "run");
        Objects.requireNonNull(delay, "delay");
        long ms = Math.max(0L, delay.toMillis());
        plugin.getServer().getAsyncScheduler().runDelayed(plugin, task -> run.run(), ms, TimeUnit.MILLISECONDS);
    }
}


