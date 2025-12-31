package io.voluble.michellelib.particle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.voluble.michellelib.scheduler.PaperScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Thread-safe particle effect scheduler for proper Folia compatibility.
 *
 * <p>Handles the scheduling of particle effects to ensure they run on appropriate threads.
 * Particle spawning must run on the main/region thread as it touches the world.</p>
 *
 * <p><strong>Safe async operations:</strong>
 * <ul>
 *   <li>Math calculations (particle paths, spirals, vectors)</li>
 *   <li>Building lists of particle locations</li>
 *   <li>Placeholder resolution</li>
 *   <li>Database/file IO</li>
 * </ul>
 * </p>
 *
 * <p><strong>NOT safe async:</strong>
 * <ul>
 *   <li>Spawning particles (touches world)</li>
 *   <li>Accessing entity data</li>
 *   <li>World operations</li>
 * </ul>
 * </p>
 */
public final class ParticleScheduler {

    private final PaperScheduler scheduler;

    public ParticleScheduler(@NotNull Plugin plugin) {
        this.scheduler = new PaperScheduler(plugin);
    }

    /**
     * Schedules a location-based particle effect on the location's region thread.
     *
     * <p>Particle spawning touches the world and must run on the appropriate region thread.</p>
     *
     * @param location the location where particles will spawn
     * @param effect the particle effect to run (must spawn particles, not just calculate)
     */
    public void scheduleLocationEffect(@NotNull Location location, @NotNull Runnable effect) {
        scheduler.getPlugin().getServer().getRegionScheduler()
            .execute(scheduler.getPlugin(), location, effect);
    }

    /**
     * Schedules an entity-based particle effect (runs on entity's region thread).
     *
     * @param entity the entity the effect is centered on
     * @param effect the particle effect to run
     */
    public void scheduleEntityEffect(@NotNull Entity entity, @NotNull Runnable effect) {
        scheduler.runEntity(entity, effect);
    }

    /**
     * Schedules an entity-based particle effect with delay (runs on entity's region thread).
     *
     * @param entity the entity the effect is centered on
     * @param effect the particle effect to run
     * @param delayTicks delay in ticks
     */
    public void scheduleEntityEffectDelayed(@NotNull Entity entity, @NotNull Runnable effect, long delayTicks) {
        scheduler.runEntityDelayed(entity, delayTicks, effect);
    }

    /**
     * Schedules a repeating entity-based particle effect (runs on entity's region thread).
     *
     * @param entity the entity the effect is centered on
     * @param effect the particle effect to run
     * @param initialDelayTicks initial delay in ticks
     * @param periodTicks period between executions in ticks
     * @return the scheduled task
     */
    @NotNull
    public ScheduledTask scheduleEntityEffectRepeating(
        @NotNull Entity entity,
        @NotNull Consumer<ScheduledTask> effect,
        long initialDelayTicks,
        long periodTicks
    ) {
        return scheduler.runEntityAtFixedRate(entity, effect, initialDelayTicks, periodTicks);
    }

    /**
     * Runs an effect immediately if safe, otherwise schedules it.
     *
     * @param entity the entity the effect is centered on
     * @param effect the particle effect to run
     */
    public void runEntityEffectNow(@NotNull Entity entity, @NotNull Runnable effect) {
        scheduler.runEntity(entity, effect);
    }

    /**
     * Safely gets an entity's location and runs a location-based particle effect.
     * Handles the entity location access on the entity's region thread, then schedules
     * particle spawning on the location's region thread.
     *
     * @param entity the entity to get location from
     * @param effect the particle effect that takes the location (will spawn particles)
     */
    public void withEntityLocation(@NotNull Entity entity, @NotNull Consumer<Location> effect) {
        scheduler.runEntity(entity, () -> {
            Location location = entity.getLocation();
            // Schedule particle spawning on the location's region thread
            scheduleLocationEffect(location, () -> effect.accept(location));
        });
    }

    /**
     * Calculates particle locations async, then schedules spawning on the appropriate thread.
     *
     * <p>Use this pattern when you need to do heavy math calculations for particle paths.
     * The calculation runs async, then the actual spawning is scheduled to the region thread.</p>
     *
     * @param location the target location for particles
     * @param asyncCalculation calculation that returns a list of locations (runs async)
     * @param spawnEffect effect that spawns particles at the calculated locations (runs on region thread)
     */
    public void calculateAsyncSpawnSync(
        @NotNull Location location,
        @NotNull java.util.function.Supplier<java.util.List<Location>> asyncCalculation,
        @NotNull Consumer<java.util.List<Location>> spawnEffect
    ) {
        scheduler.getPlugin().getServer().getAsyncScheduler().runNow(scheduler.getPlugin(), task -> {
            // Safe async: Math calculations
            java.util.List<Location> calculatedLocations = asyncCalculation.get();
            
            // Schedule actual spawning on region thread
            scheduleLocationEffect(location, () -> spawnEffect.accept(calculatedLocations));
        });
    }

    /**
     * Gets the underlying scheduler.
     *
     * @return the paper scheduler
     */
    @NotNull
    public PaperScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Gets the plugin.
     *
     * @return the plugin
     */
    @NotNull
    public Plugin getPlugin() {
        return scheduler.plugin();
    }
}
