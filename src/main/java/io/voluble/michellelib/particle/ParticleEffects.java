package io.voluble.michellelib.particle;

import com.destroystokyo.paper.ParticleBuilder;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.audience.Audience;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Advanced particle effects API that wraps Paper's particle system.
 *
 * <p>Provides both easy-to-use preset effects and full access to Paper's particle API
 * without any functionality limitations. All methods support the same parameters as
 * Paper's native API while offering convenient defaults and common effect patterns.</p>
 *
 * <p><strong>Thread Safety:</strong> Particle spawning touches the world and must run on
 * the appropriate region/entity thread. Use {@link ParticleScheduler} for proper scheduling.</p>
 *
 * <p><strong>Safe Async Operations:</strong>
 * <ul>
 *   <li>Math calculations (particle paths, spirals, vectors)</li>
 *   <li>Building lists of particle locations</li>
 *   <li>Placeholder resolution</li>
 * </ul>
 * </p>
 *
 * <p><strong>NOT Safe Async:</strong>
 * <ul>
 *   <li>Spawning particles (touches world)</li>
 *   <li>Accessing entity data</li>
 * </ul>
 * </p>
 */
public final class ParticleEffects {

    private ParticleEffects() {}

    // ---- Basic spawning with full Paper API compatibility ----

    /**
     * Creates a particle builder with all Paper functionality available.
     * Use this when you need full control over particle spawning.
     *
     * @param particle the particle type
     * @return a configured particle builder
     */
    @NotNull
    public static ParticleBuilder builder(@NotNull Particle particle) {
        return particle.builder();
    }

    /**
     * Creates a thread-safe particle scheduler for proper Folia compatibility.
     *
     * @param plugin the plugin
     * @return a new particle scheduler
     */
    @NotNull
    public static ParticleScheduler scheduler(@NotNull Plugin plugin) {
        return new ParticleScheduler(plugin);
    }

    /**
     * Spawns particles using Paper's native API with all parameters available.
     * This is the same as calling world.spawnParticle() but with better ergonomics.
     *
     * @param world the world to spawn in
     * @param particle the particle type
     * @param location the spawn location
     * @param count the number of particles (0 for single particle)
     * @param offsetX the X offset/spread
     * @param offsetY the Y offset/spread
     * @param offsetZ the Z offset/spread
     * @param extra the extra data (speed, size, etc.)
     * @param data additional particle data (Color, ItemStack, etc.)
     */
    public static void spawn(
        @NotNull World world,
        @NotNull Particle particle,
        @NotNull Location location,
        int count,
        double offsetX,
        double offsetY,
        double offsetZ,
        double extra,
        @Nullable Object data
    ) {
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data);
    }

    /**
     * Spawns particles for specific receivers with full control.
     *
     * @param particle the particle type
     * @param location the spawn location
     * @param count the number of particles
     * @param offsetX the X offset/spread
     * @param offsetY the Y offset/spread
     * @param offsetZ the Z offset/spread
     * @param extra the extra data
     * @param data additional particle data
     * @param receivers the players to show particles to, within range
     */
    public static void spawnFor(
        @NotNull Particle particle,
        @NotNull Location location,
        int count,
        double offsetX,
        double offsetY,
        double offsetZ,
        double extra,
        @Nullable Object data,
        int range,
        boolean sphere,
        @NotNull Player... receivers
    ) {
        particle.builder()
            .location(location)
            .count(count)
            .offset(offsetX, offsetY, offsetZ)
            .extra(extra)
            .data(data)
            .receivers(range, sphere, receivers)
            .spawn();
    }

    // ---- Preset Effects: Basic Shapes ----

    /**
     * Creates a simple burst of particles at a location.
     *
     * @param location the center location
     * @param particle the particle type
     * @param count the number of particles
     * @param speed the particle speed
     * @param range the visibility range (0 = all players)
     */
    public static void burst(
        @NotNull Location location,
        @NotNull Particle particle,
        int count,
        double speed,
        int range
    ) {
        particle.builder()
            .location(location)
            .count(count > 0 ? count : 1)
            .extra(speed)
            .receivers(range > 0 ? range : 32, true)
            .spawn();
    }

    /**
     * Creates a spherical burst of particles.
     *
     * @param center the center location
     * @param particle the particle type
     * @param radius the sphere radius
     * @param density particles per cubic block
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void sphere(
        @NotNull Location center,
        @NotNull Particle particle,
        double radius,
        double density,
        double speed,
        int range
    ) {
        int count = (int) Math.ceil((4.0/3.0) * Math.PI * radius * radius * radius * density);
        particle.builder()
            .location(center)
            .count(count)
            .offset(radius, radius, radius)
            .extra(speed)
            .receivers(range > 0 ? range : 32, true)
            .spawn();
    }

    /**
     * Creates a ring of particles around a location.
     *
     * @param center the center location
     * @param particle the particle type
     * @param radius the ring radius
     * @param particleCount the number of particles in the ring
     * @param height the height of the ring
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void ring(
        @NotNull Location center,
        @NotNull Particle particle,
        double radius,
        int particleCount,
        double height,
        double speed,
        int range
    ) {
        Location loc = center.clone();
        double angleStep = 2 * Math.PI / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleStep;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            loc.setX(center.getX() + x);
            loc.setY(center.getY() + height);
            loc.setZ(center.getZ() + z);

            particle.builder()
                .location(loc)
                .count(1)
                .extra(speed)
                .receivers(range > 0 ? range : 32, true)
                .spawn();
        }
    }

    /**
     * Creates a spiral of particles.
     *
     * @param center the center location
     * @param particle the particle type
     * @param radius the spiral radius
     * @param height the total height
     * @param rotations number of full rotations
     * @param particleCount total number of particles
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void spiral(
        @NotNull Location center,
        @NotNull Particle particle,
        double radius,
        double height,
        double rotations,
        int particleCount,
        double speed,
        int range
    ) {
        Location loc = center.clone();
        double heightStep = height / particleCount;
        double angleStep = (2 * Math.PI * rotations) / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleStep;
            double currentRadius = radius * (1.0 - (double) i / particleCount); // Spiral inward
            double x = currentRadius * Math.cos(angle);
            double z = currentRadius * Math.sin(angle);
            double y = i * heightStep;

            loc.setX(center.getX() + x);
            loc.setY(center.getY() + y);
            loc.setZ(center.getZ() + z);

            particle.builder()
                .location(loc)
                .count(1)
                .extra(speed)
                .receivers(range > 0 ? range : 32, true)
                .spawn();
        }
    }

    // ---- Preset Effects: Entity Effects ----

    /**
     * Creates a particle aura around an entity.
     *
     * @param entity the entity to create aura around
     * @param particle the particle type
     * @param radius the aura radius
     * @param height the aura height
     * @param particleCount the number of particles
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void aura(
        @NotNull Entity entity,
        @NotNull Particle particle,
        double radius,
        double height,
        int particleCount,
        double speed,
        int range
    ) {
        Location center = entity.getLocation().add(0, height / 2, 0);
        ring(center, particle, radius, particleCount, 0, speed, range);
    }

    /**
     * Creates a particle aura around an entity using thread-safe scheduling.
     *
     * @param scheduler the particle scheduler for proper threading
     * @param entity the entity to create aura around
     * @param particle the particle type
     * @param radius the aura radius
     * @param height the aura height
     * @param particleCount the number of particles
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void aura(
        @NotNull ParticleScheduler scheduler,
        @NotNull Entity entity,
        @NotNull Particle particle,
        double radius,
        double height,
        int particleCount,
        double speed,
        int range
    ) {
        scheduler.withEntityLocation(entity, location -> {
            Location center = location.add(0, height / 2, 0);
            ring(center, particle, radius, particleCount, 0, speed, range);
        });
    }

    /**
     * Creates a particle trail that follows an entity.
     *
     * @param entity the entity to follow
     * @param particle the particle type
     * @param durationTicks how long to trail for (in ticks)
     * @param intervalTicks how often to spawn particles (in ticks)
     * @param speed the particle speed
     * @param range the visibility range
     * @param plugin the plugin for scheduling
     * @return the scheduled task (can be cancelled)
     */
    @NotNull
    public static ScheduledTask trail(
        @NotNull Entity entity,
        @NotNull Particle particle,
        int durationTicks,
        int intervalTicks,
        double speed,
        int range,
        @NotNull Plugin plugin
    ) {
        return plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
            plugin,
            task -> {
                if (!entity.isValid()) {
                    task.cancel();
                    return;
                }
                burst(entity.getLocation(), particle, 1, speed, range);
            },
            0,
            intervalTicks
        );
    }

    /**
     * Creates a particle trail that follows an entity using thread-safe scheduling.
     *
     * @param scheduler the particle scheduler for proper threading
     * @param entity the entity to follow
     * @param particle the particle type
     * @param durationTicks how long to trail for (in ticks)
     * @param intervalTicks how often to spawn particles (in ticks)
     * @param speed the particle speed
     * @param range the visibility range
     * @return the scheduled task (can be cancelled)
     */
    @NotNull
    public static ScheduledTask trail(
        @NotNull ParticleScheduler scheduler,
        @NotNull Entity entity,
        @NotNull Particle particle,
        int durationTicks,
        int intervalTicks,
        double speed,
        int range
    ) {
        return scheduler.scheduleEntityEffectRepeating(
            entity,
            task -> {
                if (!entity.isValid()) {
                    task.cancel();
                    return;
                }
                scheduler.withEntityLocation(entity, location ->
                    burst(location, particle, 1, speed, range)
                );
            },
            0,
            intervalTicks
        );
    }

    /**
     * Creates a helix trail around an entity.
     *
     * @param entity the entity to create helix around
     * @param particle the particle type
     * @param radius the helix radius
     * @param height the helix height
     * @param speed the particle speed
     * @param range the visibility range
     * @param plugin the plugin for scheduling
     * @return the bukkit runnable (can be cancelled)
     */
    @NotNull
    public static BukkitRunnable helixTrail(
        @NotNull Entity entity,
        @NotNull Particle particle,
        double radius,
        double height,
        double speed,
        int range,
        @NotNull Plugin plugin
    ) {
        return new BukkitRunnable() {
            private int tick = 0;
            private final int maxTicks = 20 * 5; // 5 seconds

            @Override
            public void run() {
                if (!entity.isValid() || tick >= maxTicks) {
                    cancel();
                    return;
                }

                Location entityLoc = entity.getLocation();
                double angle = (tick * 0.2) % (2 * Math.PI); // Slow rotation
                double y = (tick % 20) * (height / 20.0); // Rise over time

                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);

                Location particleLoc = entityLoc.clone().add(x, y, z);

                burst(particleLoc, particle, 1, speed, range);
                tick++;
            }
        };
    }

    // ---- Preset Effects: Environmental Effects ----

    /**
     * Creates a colored dust trail effect.
     *
     * @param location the location to spawn at
     * @param color the dust color
     * @param size the dust particle size
     * @param count the number of particles
     * @param range the visibility range
     */
    public static void coloredDust(
        @NotNull Location location,
        @NotNull Color color,
        float size,
        int count,
        int range
    ) {
        Particle.DustOptions data = new Particle.DustOptions(color, size);
        Particle.DUST.builder()
            .location(location)
            .count(count)
            .data(data)
            .receivers(range > 0 ? range : 32, true)
            .spawn();
    }

    /**
     * Creates a dust transition effect (color changing dust).
     *
     * @param location the location to spawn at
     * @param fromColor the starting color
     * @param toColor the ending color
     * @param size the dust particle size
     * @param count the number of particles
     * @param range the visibility range
     */
    public static void dustTransition(
        @NotNull Location location,
        @NotNull Color fromColor,
        @NotNull Color toColor,
        float size,
        int count,
        int range
    ) {
        Particle.DustTransition data = new Particle.DustTransition(fromColor, toColor, size);
        Particle.DUST_COLOR_TRANSITION.builder()
            .location(location)
            .count(count)
            .data(data)
            .receivers(range > 0 ? range : 32, true)
            .spawn();
    }

    /**
     * Creates a block break effect.
     *
     * @param location the location to spawn at
     * @param blockType the block type to break
     * @param count the number of particles
     * @param range the visibility range
     */
    public static void blockBreak(
        @NotNull Location location,
        @NotNull BlockType blockType,
        int count,
        int range
    ) {
        Particle.BLOCK_CRUMBLE.builder()
            .location(location)
            .count(count)
            .data(blockType.createBlockData())
            .receivers(range > 0 ? range : 32, true)
            .spawn();
    }

    /**
     * Creates an item break effect.
     *
     * @param location the location to spawn at
     * @param itemStack the item to break
     * @param count the number of particles
     * @param range the visibility range
     */
    public static void itemBreak(
        @NotNull Location location,
        @NotNull ItemStack itemStack,
        int count,
        int range
    ) {
        Particle.ITEM.builder()
            .location(location)
            .count(count)
            .data(itemStack)
            .receivers(range > 0 ? range : 32, true)
            .spawn();
    }

    // ---- Preset Effects: Magical Effects ----

    /**
     * Creates a magical circle effect.
     *
     * @param center the center location
     * @param radius the circle radius
     * @param particle the particle type
     * @param particleCount the number of particles
     * @param height the circle height
     * @param range the visibility range
     */
    public static void magicCircle(
        @NotNull Location center,
        double radius,
        @NotNull Particle particle,
        int particleCount,
        double height,
        int range
    ) {
        ring(center, particle, radius, particleCount, height, 0.1, range);
    }

    /**
     * Creates a rune circle effect with multiple rings.
     *
     * @param center the center location
     * @param minRadius the inner circle radius
     * @param maxRadius the outer circle radius
     * @param rings the number of rings
     * @param particlesPerRing particles per ring
     * @param height the circle height
     * @param range the visibility range
     */
    public static void runeCircle(
        @NotNull Location center,
        double minRadius,
        double maxRadius,
        int rings,
        int particlesPerRing,
        double height,
        int range
    ) {
        double radiusStep = (maxRadius - minRadius) / Math.max(1, rings - 1);

        for (int ring = 0; ring < rings; ring++) {
            double radius = minRadius + (ring * radiusStep);
            // Alternate particle types for visual variety
            Particle particle = (ring % 2 == 0) ? Particle.ENCHANT : Particle.PORTAL;
            ring(center, particle, radius, particlesPerRing, height, 0.05, range);
        }
    }

    /**
     * Creates a teleportation effect.
     *
     * @param entity the entity to teleport
     * @param durationTicks the effect duration
     * @param range the visibility range
     * @param plugin the plugin for scheduling
     * @return the bukkit runnable
     */
    @NotNull
    public static BukkitRunnable teleportEffect(
        @NotNull Entity entity,
        int durationTicks,
        int range,
        @NotNull Plugin plugin
    ) {
        return new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTicks) {
                    cancel();
                    return;
                }

                Location loc = entity.getLocation();
                double progress = (double) tick / durationTicks;

                // Create expanding rings
                double radius = progress * 2.0;
                int particles = (int) (progress * 20) + 5;

                ring(loc, Particle.PORTAL, radius, particles, 0, 0.1, range);

                // Add some sparkle
                if (tick % 2 == 0) {
                    sphere(loc, Particle.ENCHANT, 0.5, 2.0, 0.05, range);
                }

                tick++;
            }
        };
    }

    /**
     * Creates a teleportation effect for an entity using thread-safe scheduling.
     *
     * @param scheduler the particle scheduler for proper threading
     * @param entity the entity to teleport
     * @param durationTicks the effect duration
     * @param range the visibility range
     * @return the scheduled task
     */
    @NotNull
    public static ScheduledTask teleportEffect(
        @NotNull ParticleScheduler scheduler,
        @NotNull Entity entity,
        int durationTicks,
        int range
    ) {
        return scheduler.scheduleEntityEffectRepeating(
            entity,
            task -> {
                int tick = (int) task.getTickNumber();
                if (tick >= durationTicks) {
                    task.cancel();
                    return;
                }

                scheduler.withEntityLocation(entity, loc -> {
                    double progress = (double) tick / durationTicks;

                    // Create expanding rings
                    double radius = progress * 2.0;
                    int particles = (int) (progress * 20) + 5;

                    ring(loc, Particle.PORTAL, radius, particles, 0, 0.1, range);

                    // Add some sparkle
                    if (tick % 2 == 0) {
                        sphere(loc, Particle.ENCHANT, 0.5, 2.0, 0.05, range);
                    }
                });
            },
            0,
            1
        );
    }

    // ---- Advanced Effects: Custom Shapes ----

    /**
     * Creates a particle line between two points.
     *
     * @param start the starting location
     * @param end the ending location
     * @param particle the particle type
     * @param density particles per block
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void line(
        @NotNull Location start,
        @NotNull Location end,
        @NotNull Particle particle,
        double density,
        double speed,
        int range
    ) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        int particleCount = (int) Math.ceil(length * density);
        double stepSize = length / particleCount;

        for (int i = 0; i <= particleCount; i++) {
            Vector offset = direction.clone().multiply(i * stepSize);
            Location particleLoc = start.clone().add(offset);

            particle.builder()
                .location(particleLoc)
                .count(1)
                .extra(speed)
                .receivers(range > 0 ? range : 32, true)
                .spawn();
        }
    }

    /**
     * Creates a particle cube.
     *
     * @param corner1 first corner of the cube
     * @param corner2 second corner of the cube
     * @param particle the particle type
     * @param density particles per block face
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void cube(
        @NotNull Location corner1,
        @NotNull Location corner2,
        @NotNull Particle particle,
        double density,
        double speed,
        int range
    ) {
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        Location temp = corner1.clone();

        // Draw all 12 edges
        // Bottom face
        line(set(temp, minX, minY, minZ), set(temp, maxX, minY, minZ), particle, density, speed, range);
        line(set(temp, maxX, minY, minZ), set(temp, maxX, minY, maxZ), particle, density, speed, range);
        line(set(temp, maxX, minY, maxZ), set(temp, minX, minY, maxZ), particle, density, speed, range);
        line(set(temp, minX, minY, maxZ), set(temp, minX, minY, minZ), particle, density, speed, range);

        // Top face
        line(set(temp, minX, maxY, minZ), set(temp, maxX, maxY, minZ), particle, density, speed, range);
        line(set(temp, maxX, maxY, minZ), set(temp, maxX, maxY, maxZ), particle, density, speed, range);
        line(set(temp, maxX, maxY, maxZ), set(temp, minX, maxY, maxZ), particle, density, speed, range);
        line(set(temp, minX, maxY, maxZ), set(temp, minX, maxY, minZ), particle, density, speed, range);

        // Vertical edges
        line(set(temp, minX, minY, minZ), set(temp, minX, maxY, minZ), particle, density, speed, range);
        line(set(temp, maxX, minY, minZ), set(temp, maxX, maxY, minZ), particle, density, speed, range);
        line(set(temp, maxX, minY, maxZ), set(temp, maxX, maxY, maxZ), particle, density, speed, range);
        line(set(temp, minX, minY, maxZ), set(temp, minX, maxY, maxZ), particle, density, speed, range);
    }

    /**
     * Creates a particle wave effect.
     *
     * @param start the starting location
     * @param direction the wave direction
     * @param length the wave length
     * @param amplitude the wave amplitude
     * @param frequency the wave frequency
     * @param particle the particle type
     * @param particleCount the number of particles
     * @param speed the particle speed
     * @param range the visibility range
     */
    public static void wave(
        @NotNull Location start,
        @NotNull Vector direction,
        double length,
        double amplitude,
        double frequency,
        @NotNull Particle particle,
        int particleCount,
        double speed,
        int range
    ) {
        direction = direction.clone().normalize();
        Location loc = start.clone();

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double x = t * length;

            // Sine wave in Y direction perpendicular to movement
            Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
            double waveOffset = amplitude * Math.sin(t * frequency * 2 * Math.PI);

            Vector offset = direction.clone().multiply(x).add(perpendicular.multiply(waveOffset));

            loc.setX(start.getX() + offset.getX());
            loc.setY(start.getY() + offset.getY());
            loc.setZ(start.getZ() + offset.getZ());

            particle.builder()
                .location(loc)
                .count(1)
                .extra(speed)
                .receivers(range > 0 ? range : 32, true)
                .spawn();
        }
    }

    // ---- Utility Methods ----

    private static Location set(Location loc, double x, double y, double z) {
        loc.setX(x);
        loc.setY(y);
        loc.setZ(z);
        return loc;
    }

    /**
     * Creates a particle effect chain for complex animations.
     *
     * @param plugin the plugin for scheduling
     * @return a new effect chain builder
     */
    @NotNull
    public static EffectChain chain(@NotNull Plugin plugin) {
        return new EffectChain(plugin);
    }

    /**
     * Builder for creating complex particle effect sequences.
     */
    public static final class EffectChain {
        private final Plugin plugin;
        private final List<Consumer<ScheduledTask>> effects = new ArrayList<>();
        private int currentDelay = 0;

        private EffectChain(@NotNull Plugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Adds an effect to execute immediately.
         */
        @NotNull
        public EffectChain then(@NotNull Runnable effect) {
            effects.add(task -> effect.run());
            return this;
        }

        /**
         * Adds a delay before the next effect.
         */
        @NotNull
        public EffectChain delay(int ticks) {
            currentDelay += ticks;
            return this;
        }

        /**
         * Adds an effect after a specific delay.
         */
        @NotNull
        public EffectChain thenAfter(int delayTicks, @NotNull Runnable effect) {
            effects.add(task -> {
                plugin.getServer().getGlobalRegionScheduler().runDelayed(
                    plugin,
                    t -> effect.run(),
                    delayTicks
                );
            });
            return this;
        }

        /**
         * Executes the effect chain.
         *
         * @return the scheduled task
         */
        @NotNull
        public ScheduledTask execute() {
            return plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                for (Consumer<ScheduledTask> effect : effects) {
                    effect.accept(task);
                }
            });
        }
    }
}
