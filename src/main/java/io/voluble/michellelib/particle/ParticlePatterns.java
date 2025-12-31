package io.voluble.michellelib.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * Predefined particle patterns and shapes for common effects.
 *
 * <p>Provides reusable patterns that can be combined with different particle types
 * and effects to create complex visual displays.</p>
 */
public final class ParticlePatterns {

    private ParticlePatterns() {}

    /**
     * A pattern that creates a simple circle.
     */
    public static final BiConsumer<Location, Particle> CIRCLE = (center, particle) -> {
        ParticleEffects.ring(center, particle, 1.0, 16, 0, 0.1, 32);
    };

    /**
     * A pattern that creates a double circle.
     */
    public static final BiConsumer<Location, Particle> DOUBLE_CIRCLE = (center, particle) -> {
        ParticleEffects.ring(center, particle, 0.8, 12, 0, 0.1, 32);
        ParticleEffects.ring(center, particle, 1.2, 20, 0, 0.1, 32);
    };

    /**
     * A pattern that creates a star shape.
     */
    public static final BiConsumer<Location, Particle> STAR = (center, particle) -> {
        Location loc = center.clone();
        double angleStep = Math.PI / 5; // 36 degrees

        for (int i = 0; i < 10; i++) {
            double angle = i * angleStep;
            double radius = (i % 2 == 0) ? 1.0 : 0.5; // Alternate between outer and inner points
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            loc.setX(center.getX() + x);
            loc.setZ(center.getZ() + z);

            ParticleEffects.burst(loc, particle, 1, 0.05, 32);
        }
    };

    /**
     * A pattern that creates a heart shape.
     */
    public static final BiConsumer<Location, Particle> HEART = (center, particle) -> {
        Location loc = center.clone();

        for (double t = 0; t < 2 * Math.PI; t += 0.1) {
            double x = 0.5 * (16 * Math.sin(t) * Math.sin(t) * Math.sin(t));
            double z = 0.5 * (13 * Math.cos(t) - 5 * Math.cos(2*t) - 2 * Math.cos(3*t) - Math.cos(4*t));

            loc.setX(center.getX() + x * 0.1); // Scale down
            loc.setZ(center.getZ() + z * 0.1);

            ParticleEffects.burst(loc, particle, 1, 0.02, 32);
        }
    };

    /**
     * A pattern that creates a spiral.
     */
    public static final BiConsumer<Location, Particle> SPIRAL = (center, particle) -> {
        ParticleEffects.spiral(center, particle, 1.5, 3.0, 2.0, 40, 0.1, 32);
    };

    /**
     * A pattern that creates a cube.
     */
    public static final BiConsumer<Location, Particle> CUBE = (center, particle) -> {
        Location corner1 = center.clone().add(-1, -1, -1);
        Location corner2 = center.clone().add(1, 1, 1);
        ParticleEffects.cube(corner1, corner2, particle, 0.5, 0.05, 32);
    };

    /**
     * A pattern that creates a cross.
     */
    public static final BiConsumer<Location, Particle> CROSS = (center, particle) -> {
        // Horizontal line
        ParticleEffects.line(
            center.clone().add(-2, 0, 0),
            center.clone().add(2, 0, 0),
            particle, 1.0, 0.05, 32
        );
        // Vertical line
        ParticleEffects.line(
            center.clone().add(0, -2, 0),
            center.clone().add(0, 2, 0),
            particle, 1.0, 0.05, 32
        );
    };

    /**
     * A pattern that creates an arrow pointing north.
     */
    public static final BiConsumer<Location, Particle> ARROW_NORTH = (center, particle) -> {
        // Arrow shaft
        ParticleEffects.line(
            center.clone().add(0, 0, 1),
            center.clone().add(0, 0, -1),
            particle, 1.0, 0.05, 32
        );
        // Arrow head
        ParticleEffects.line(
            center.clone().add(-0.5, 0, -1),
            center.clone().add(0, 0, -1.5),
            particle, 1.0, 0.05, 32
        );
        ParticleEffects.line(
            center.clone().add(0.5, 0, -1),
            center.clone().add(0, 0, -1.5),
            particle, 1.0, 0.05, 32
        );
    };

    /**
     * Creates a custom pattern from a set of relative positions.
     *
     * @param relativePositions the positions relative to center (each should be length 3: x,y,z)
     * @return a pattern that spawns particles at the specified relative positions
     */
    @NotNull
    public static BiConsumer<Location, Particle> custom(double[]... relativePositions) {
        return (center, particle) -> {
            Location loc = center.clone();
            for (double[] pos : relativePositions) {
                if (pos.length >= 3) {
                    loc.setX(center.getX() + pos[0]);
                    loc.setY(center.getY() + pos[1]);
                    loc.setZ(center.getZ() + pos[2]);
                    ParticleEffects.burst(loc, particle, 1, 0.05, 32);
                }
            }
        };
    }

    /**
     * Creates a pattern that follows a mathematical function.
     *
     * @param function a function that takes a parameter t (0 to 1) and returns a Vector offset
     * @param steps the number of steps to evaluate the function
     * @return a pattern that follows the mathematical function
     */
    @NotNull
    public static BiConsumer<Location, Particle> parametric(
        @NotNull ParametricFunction function,
        int steps
    ) {
        return (center, particle) -> {
            Location loc = center.clone();
            for (int i = 0; i < steps; i++) {
                double t = (double) i / (steps - 1);
                Vector offset = function.evaluate(t);

                loc.setX(center.getX() + offset.getX());
                loc.setY(center.getY() + offset.getY());
                loc.setZ(center.getZ() + offset.getZ());

                ParticleEffects.burst(loc, particle, 1, 0.05, 32);
            }
        };
    }

    /**
     * Functional interface for parametric equations.
     */
    @FunctionalInterface
    public interface ParametricFunction {
        /**
         * Evaluates the parametric function at parameter t.
         *
         * @param t the parameter (typically 0 to 1)
         * @return the vector offset from center
         */
        @NotNull
        Vector evaluate(double t);
    }

    // ---- Preset Mathematical Patterns ----

    /**
     * A pattern that creates a sine wave.
     */
    public static final BiConsumer<Location, Particle> SINE_WAVE = parametric(t -> {
        double x = (t - 0.5) * 4; // -2 to 2
        double y = Math.sin(t * 4 * Math.PI) * 0.5; // Sine wave with amplitude 0.5
        return new Vector(x, y, 0);
    }, 20);

    /**
     * A pattern that creates a figure-8 (lemniscate).
     */
    public static final BiConsumer<Location, Particle> FIGURE_EIGHT = parametric(t -> {
        double angle = t * 2 * Math.PI;
        double scale = 0.8;
        double x = scale * Math.sin(angle);
        double y = scale * Math.sin(angle) * Math.cos(angle);
        return new Vector(x, y, 0);
    }, 32);

    /**
     * A pattern that creates a rose curve.
     */
    public static final BiConsumer<Location, Particle> ROSE = parametric(t -> {
        double angle = t * 4 * Math.PI; // Two full rotations
        double k = 3; // Number of petals
        double r = 0.8 * Math.cos(k * angle);
        double x = r * Math.cos(angle);
        double y = r * Math.sin(angle);
        return new Vector(x, y, 0);
    }, 48);

    /**
     * Applies a pattern at a location with a particle type.
     *
     * @param pattern the pattern to apply
     * @param location the location to apply at
     * @param particle the particle type to use
     */
    public static void apply(
        @NotNull BiConsumer<Location, Particle> pattern,
        @NotNull Location location,
        @NotNull Particle particle
    ) {
        pattern.accept(location, particle);
    }
}
