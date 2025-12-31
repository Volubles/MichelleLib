package io.voluble.michellelib.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Ready-to-use particle effect presets for common scenarios.
 *
 * <p>Provides high-level methods for effects like auras, trails, explosions,
 * and magical effects with sensible defaults.</p>
 */
public final class ParticlePresets {

    private ParticlePresets() {}

    // ---- Aura Effects ----

    /**
     * Creates a flame aura around an entity.
     */
    public static void flameAura(@NotNull Entity entity) {
        ParticleEffects.aura(entity, Particle.FLAME, 0.8, 1.8, 8, 0.02, 32);
    }

    /**
     * Creates a magical aura with enchanted particles.
     */
    public static void magicAura(@NotNull Entity entity) {
        ParticleEffects.aura(entity, Particle.ENCHANT, 1.0, 2.0, 12, 0.1, 32);
    }

    /**
     * Creates a healing aura with heart particles.
     */
    public static void healingAura(@NotNull Entity entity) {
        ParticleEffects.aura(entity, Particle.HEART, 0.6, 1.5, 6, 0.1, 32);
    }

    /**
     * Creates a poisonous aura with green particles.
     */
    public static void poisonAura(@NotNull Entity entity) {
        ParticleEffects.aura(entity, Particle.COMPOSTER, 0.7, 1.6, 10, 0.05, 32);
    }

    // ---- Trail Effects ----

    /**
     * Creates a flame trail that follows an entity.
     */
    public static void flameTrail(@NotNull Entity entity, @NotNull Plugin plugin) {
        ParticleEffects.trail(entity, Particle.FLAME, 100, 2, 0.02, 32, plugin);
    }

    /**
     * Creates a magical trail with portal particles.
     */
    public static void magicTrail(@NotNull Entity entity, @NotNull Plugin plugin) {
        ParticleEffects.trail(entity, Particle.PORTAL, 200, 3, 0.1, 32, plugin);
    }

    /**
     * Creates a sparkling trail with crit particles.
     */
    public static void sparkleTrail(@NotNull Entity entity, @NotNull Plugin plugin) {
        ParticleEffects.trail(entity, Particle.CRIT_MAGIC, 150, 4, 0.05, 32, plugin);
    }

    // ---- Explosion Effects ----

    /**
     * Creates a standard explosion effect.
     */
    public static void explosion(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.EXPLOSION, 1, 0.1, 64);
        ParticleEffects.sphere(location, Particle.SMOKE, 1.5, 1.0, 0.05, 64);
    }

    /**
     * Creates a large explosion with fire.
     */
    public static void bigExplosion(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.EXPLOSION_LARGE, 1, 0.1, 128);
        ParticleEffects.sphere(location, Particle.FLAME, 2.0, 0.8, 0.1, 128);
        ParticleEffects.sphere(location, Particle.SMOKE_LARGE, 2.5, 0.5, 0.02, 128);
    }

    /**
     * Creates a magical explosion with enchanted particles.
     */
    public static void magicExplosion(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.FIREWORKS_SPARK, 20, 0.2, 64);
        ParticleEffects.sphere(location, Particle.ENCHANT, 1.5, 1.2, 0.15, 64);
        ParticleEffects.ring(location, Particle.PORTAL, 2.0, 16, 0, 0.1, 64);
    }

    // ---- Teleportation Effects ----

    /**
     * Creates a teleportation effect for an entity.
     */
    public static void teleportEffect(@NotNull Entity entity, @NotNull Plugin plugin) {
        ParticleEffects.teleportEffect(entity, 60, 32, plugin).runTaskTimer(plugin, 0, 1);
    }

    /**
     * Creates a teleport arrival effect.
     */
    public static void teleportArrive(@NotNull Location location) {
        ParticleEffects.sphere(location, Particle.PORTAL, 1.0, 2.0, 0.2, 32);
        ParticleEffects.ring(location, Particle.ENCHANT, 1.5, 12, 0, 0.1, 32);
    }

    /**
     * Creates a teleport departure effect.
     */
    public static void teleportDepart(@NotNull Location location) {
        ParticleEffects.sphere(location, Particle.REVERSE_PORTAL, 1.0, 2.0, 0.2, 32);
        ParticleEffects.ring(location, Particle.PORTAL, 1.5, 12, 0, 0.1, 32);
    }

    // ---- Status Effects ----

    /**
     * Creates a bleeding effect with red particles.
     */
    public static void bleeding(@NotNull Entity entity) {
        ParticleEffects.coloredDust(entity.getLocation().add(0, 1, 0),
            Color.fromRGB(200, 0, 0), 1.0f, 3, 32);
    }

    /**
     * Creates a frozen effect with blue particles.
     */
    public static void frozen(@NotNull Entity entity) {
        ParticleEffects.coloredDust(entity.getLocation().add(0, 1, 0),
            Color.fromRGB(100, 200, 255), 1.5f, 5, 32);
    }

    /**
     * Creates a shield effect with barrier particles.
     */
    public static void shield(@NotNull Entity entity) {
        ParticleEffects.aura(entity, Particle.BLOCK_MARKER, 1.2, 2.0, 16, 0.0, 32);
    }

    // ---- Environmental Effects ----

    /**
     * Creates a campfire effect.
     */
    public static void campfire(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.CAMPFIRE_COSY_SMOKE, 1, 0.02, 32);
        ParticleEffects.burst(location.clone().add(0, 0.5, 0), Particle.FLAME, 1, 0.01, 32);
    }

    /**
     * Creates a magical campfire with enchanted flames.
     */
    public static void magicCampfire(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.CAMPFIRE_SIGNAL_SMOKE, 1, 0.05, 32);
        ParticleEffects.burst(location.clone().add(0, 0.5, 0), Particle.SOUL_FIRE_FLAME, 2, 0.02, 32);
    }

    /**
     * Creates a waterfall effect.
     */
    public static void waterfall(@NotNull Location location) {
        for (int i = 0; i < 5; i++) {
            Location particleLoc = location.clone().add(
                (Math.random() - 0.5) * 2,
                -i * 0.5,
                (Math.random() - 0.5) * 2
            );
            ParticleEffects.burst(particleLoc, Particle.WATER_SPLASH, 2, 0.1, 32);
        }
    }

    // ---- Magical Effects ----

    /**
     * Creates a casting circle effect.
     */
    public static void castingCircle(@NotNull Location center) {
        ParticleEffects.runeCircle(center, 0.5, 2.0, 3, 16, 0, 32);
    }

    /**
     * Creates a summoning circle effect.
     */
    public static void summoningCircle(@NotNull Location center) {
        ParticleEffects.runeCircle(center, 1.0, 3.0, 4, 20, 0, 32);
        // Add some extra effects
        ParticleEffects.sphere(center, Particle.ENCHANT, 2.5, 0.3, 0.05, 32);
    }

    /**
     * Creates a spell casting effect.
     */
    public static void spellCast(@NotNull Entity caster) {
        Location loc = caster.getLocation().add(0, 1.5, 0);
        ParticleEffects.burst(loc, Particle.FIREWORKS_SPARK, 15, 0.3, 32);
        ParticleEffects.ring(loc, Particle.ENCHANT, 1.0, 8, 0, 0.2, 32);
    }

    /**
     * Creates a spell hit effect.
     */
    public static void spellHit(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.CRIT_MAGIC, 20, 0.4, 32);
        ParticleEffects.sphere(location, Particle.ENCHANT, 0.8, 1.5, 0.1, 32);
    }

    // ---- Player Feedback Effects ----

    /**
     * Creates a success effect (green particles).
     */
    public static void success(@NotNull Location location) {
        ParticleEffects.coloredDust(location, Color.fromRGB(0, 255, 0), 1.5f, 10, 32);
        ParticleEffects.burst(location, Particle.FIREWORK, 1, 0.1, 32);
    }

    /**
     * Creates a failure effect (red particles).
     */
    public static void failure(@NotNull Location location) {
        ParticleEffects.coloredDust(location, Color.fromRGB(255, 0, 0), 1.5f, 10, 32);
        ParticleEffects.burst(location, Particle.SMOKE, 5, 0.05, 32);
    }

    /**
     * Creates a warning effect (orange particles).
     */
    public static void warning(@NotNull Location location) {
        ParticleEffects.coloredDust(location, Color.fromRGB(255, 165, 0), 1.5f, 8, 32);
        ParticleEffects.burst(location, Particle.LARGE_SMOKE, 3, 0.03, 32);
    }

    /**
     * Creates a level up effect.
     */
    public static void levelUp(@NotNull Entity entity) {
        Location loc = entity.getLocation().add(0, 1, 0);
        ParticleEffects.sphere(loc, Particle.FIREWORK, 1.5, 1.0, 0.2, 64);
        ParticleEffects.ring(loc, Particle.ENCHANT, 2.0, 16, 0, 0.1, 64);
        ParticleEffects.coloredDust(loc, Color.fromRGB(255, 215, 0), 2.0f, 15, 64); // Gold
    }

    // ---- Seasonal Effects ----

    /**
     * Creates a snow effect around a location.
     */
    public static void snow(@NotNull Location location) {
        for (int i = 0; i < 8; i++) {
            Location particleLoc = location.clone().add(
                (Math.random() - 0.5) * 4,
                Math.random() * 3,
                (Math.random() - 0.5) * 4
            );
            ParticleEffects.burst(particleLoc, Particle.SNOWFLAKE, 1, 0.01, 32);
        }
    }

    /**
     * Creates a rain effect.
     */
    public static void rain(@NotNull Location location) {
        for (int i = 0; i < 12; i++) {
            Location particleLoc = location.clone().add(
                (Math.random() - 0.5) * 6,
                Math.random() * 4,
                (Math.random() - 0.5) * 6
            );
            ParticleEffects.burst(particleLoc, Particle.WATER_DROP, 1, 0.02, 32);
        }
    }

    /**
     * Creates a cherry blossom effect (falling petals).
     */
    public static void cherryBlossoms(@NotNull Location location) {
        for (int i = 0; i < 6; i++) {
            Location particleLoc = location.clone().add(
                (Math.random() - 0.5) * 3,
                Math.random() * 2,
                (Math.random() - 0.5) * 3
            );
            // Use a pink dust particle to simulate petals
            ParticleEffects.coloredDust(particleLoc, Color.fromRGB(255, 182, 193), 1.0f, 1, 32);
        }
    }

    // ---- Utility Methods ----

    /**
     * Plays a random magical effect at a location.
     */
    public static void randomMagic(@NotNull Location location) {
        Particle[] magicParticles = {
            Particle.ENCHANT, Particle.PORTAL, Particle.CRIT_MAGIC,
            Particle.FIREWORKS_SPARK, Particle.WITCH
        };
        Particle randomParticle = magicParticles[(int) (Math.random() * magicParticles.length)];
        ParticleEffects.burst(location, randomParticle, 10, 0.1, 32);
    }

    /**
     * Creates a celebratory effect with multiple particle types.
     */
    public static void celebrate(@NotNull Location location) {
        ParticleEffects.burst(location, Particle.FIREWORK, 5, 0.2, 64);
        ParticleEffects.sphere(location, Particle.FIREWORKS_SPARK, 1.0, 1.0, 0.3, 64);
        ParticleEffects.coloredDust(location, Color.fromRGB(255, 255, 0), 2.0f, 20, 64);
    }
}
