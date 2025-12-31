# Particle Effects API

MichelleLib provides a comprehensive particle effects API that wraps Paper's particle system while maintaining full compatibility and adding powerful convenience features.

## Overview

The particle effects API consists of three main components:

- **ParticleEffects**: Main API with presets and full Paper API access
- **ParticlePatterns**: Predefined patterns and shapes
- **ParticlePresets**: Ready-to-use effect collections

## Basic Usage

### Full Paper API Access

```java
// Full access to Paper's particle API - no limitations
ParticleEffects.builder(Particle.FLAME)
    .location(location)
    .count(10)
    .offset(1, 1, 1)
    .extra(0.1)
    .receivers(32, true)
    .spawn();

// Or use the convenience spawn method
ParticleEffects.spawn(world, Particle.FLAME, location, 10, 1, 1, 1, 0.1, null);
```

### Preset Effects

```java
// Simple effects
ParticleEffects.burst(location, Particle.CRIT_MAGIC, 20, 0.2, 32);
ParticleEffects.sphere(center, Particle.ENCHANT, 2.0, 1.0, 0.1, 32);
ParticleEffects.ring(center, Particle.PORTAL, 3.0, 16, 0, 0.05, 32);

// Complex shapes
ParticleEffects.spiral(center, Particle.FIREWORKS_SPARK, 2.0, 4.0, 3.0, 60, 0.1, 32);
ParticleEffects.cube(corner1, corner2, Particle.SOUL, 0.8, 0.02, 32);
```

## Particle Patterns

Predefined patterns for consistent visual effects:

```java
// Apply patterns
ParticlePatterns.CIRCLE.apply(center, Particle.ENCHANT);
ParticlePatterns.HEART.apply(center, Particle.CRIT_MAGIC);
ParticlePatterns.SPIRAL.apply(center, Particle.FIREWORKS_SPARK);

// Create custom patterns
BiConsumer<Location, Particle> customPattern = ParticlePatterns.custom(
    new double[]{0, 0, 0},    // Center
    new double[]{1, 0, 0},    // East
    new double[]{-1, 0, 0},   // West
    new double[]{0, 0, 1},    // North
    new double[]{0, 0, -1}    // South
);
customPattern.accept(location, Particle.FLAME);
```

## Particle Presets

Ready-to-use effects for common scenarios:

```java
// Entity effects
ParticlePresets.flameAura(entity);
ParticlePresets.magicAura(entity);
ParticlePresets.healingAura(entity);
ParticlePresets.shield(entity);

// Environmental effects
ParticlePresets.campfire(location);
ParticlePresets.magicCampfire(location);
ParticlePresets.waterfall(location);

// Status effects
ParticlePresets.bleeding(entity);
ParticlePresets.frozen(entity);
ParticlePresets.levelUp(entity);

// Feedback effects
ParticlePresets.success(location);
ParticlePresets.failure(location);
ParticlePresets.warning(location);
ParticlePresets.celebrate(location);
```

## Advanced Effects

### Trails and Following Effects

```java
// Entity trails
ParticleEffects.trail(entity, Particle.FLAME, 200, 3, 0.02, 32, plugin);

// Helix trails
ParticleEffects.helixTrail(entity, Particle.ENCHANT, 1.0, 3.0, 0.1, 32, plugin)
    .runTaskTimer(plugin, 0, 1);
```

### Effect Chains

```java
ParticleEffects.chain(plugin)
    .then(() -> ParticlePresets.magicExplosion(location))
    .delay(10) // Half second delay
    .then(() -> ParticlePresets.success(location))
    .delay(20) // 1 second delay
    .then(() -> ParticlePresets.celebrate(location))
    .execute();
```

### Colored Particles

```java
// Dust particles
ParticleEffects.coloredDust(location, Color.RED, 1.5f, 15, 32);

// Transition effects
ParticleEffects.dustTransition(location, Color.BLUE, Color.PURPLE, 2.0f, 20, 32);
```

### Material Particles

```java
// Block effects
ParticleEffects.blockBreak(location, BlockType.STONE, 10, 32);

// Item effects
ParticleEffects.itemBreak(location, new ItemStack(Material.DIAMOND_SWORD), 5, 32);
```

## Custom Effects

### Parametric Patterns

```java
// Create a parametric function (mathematical curve)
ParticlePatterns.ParametricFunction figureEight = t -> {
    double angle = t * 4 * Math.PI; // Two rotations
    double scale = 1.5;
    double x = scale * Math.sin(angle);
    double y = scale * Math.sin(angle) * Math.cos(angle);
    return new Vector(x, y, 0);
};

// Apply the pattern
ParticlePatterns.parametric(figureEight, 64).accept(center, Particle.CRIT_MAGIC);
```

### Complex Animations

```java
// Multi-stage teleportation effect
ParticleEffects.chain(plugin)
    .then(() -> ParticlePresets.teleportDepart(location))
    .delay(30) // 1.5 seconds
    .then(() -> {
        // Custom particle effect during teleport
        for (int i = 0; i < 5; i++) {
            Location effectLoc = location.clone().add(
                (Math.random() - 0.5) * 2,
                Math.random() * 2,
                (Math.random() - 0.5) * 2
            );
            ParticleEffects.burst(effectLoc, Particle.PORTAL, 3, 0.2, 32);
        }
    })
    .delay(20) // 1 second
    .then(() -> ParticlePresets.teleportArrive(location))
    .execute();
```

## Thread-Safe Scheduling

**Important Rule**: Anything that touches the world, entities, or players must run on the main/region thread. That includes spawning particles.

### Safe Async Operations

You can do these async:
- **Math calculations** (particle paths, spirals, vectors)
- **Placeholder resolution**
- **Database/file IO**
- **Building lists of particle locations**

### NOT Safe Async

- **Spawning particles** (touches world)
- **Accessing entity data**
- **World operations**

### Using ParticleScheduler

For proper Folia compatibility, use the `ParticleScheduler`:

```java
// Create a scheduler
ParticleScheduler scheduler = ParticleEffects.scheduler(plugin);

// Entity-based effects (runs on entity's region thread)
ParticleEffects.aura(scheduler, entity, Particle.FLAME, 1.0, 2.0, 12, 0.05, 32);
ParticleEffects.teleportEffect(scheduler, entity, 60, 32);
ParticleEffects.trail(scheduler, entity, Particle.ENCHANT, 200, 3, 0.1, 32);

// Location-based effects (runs on location's region thread)
scheduler.scheduleLocationEffect(location, () -> {
    ParticleEffects.burst(location, Particle.FLAME, 10, 0.1, 32);
});
```

### Async Calculation, Sync Spawning Pattern

Calculate particle locations async, then spawn on the region thread:

```java
ParticleScheduler scheduler = ParticleEffects.scheduler(plugin);

// Calculate locations async (safe - just math)
scheduler.calculateAsyncSpawnSync(
    centerLocation,
    () -> {
        // Safe async: Math calculations
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double angle = i * 0.1;
            double x = Math.cos(angle) * 5;
            double z = Math.sin(angle) * 5;
            locations.add(centerLocation.clone().add(x, 0, z));
        }
        return locations;
    },
    calculatedLocations -> {
        // Runs on region thread: Actually spawn particles
        for (Location loc : calculatedLocations) {
            ParticleEffects.burst(loc, Particle.ENCHANT, 1, 0.05, 32);
        }
    }
);
```

## TextEngine Integration

Particle effects work seamlessly with MichelleLib's text system for dynamic content:

```java
// This would be used in conjunction with dialog APIs that now support TextEngine
TextEngine engine = TextEngine.builder()
    .expandPlaceholderApiPercents(true)
    .enablePapiTag(true)
    .build();

// Dialogs now support MiniMessage strings
Dialog dialog = Dialogs.confirmation(
    engine,
    player,
    "<gold>Teleport to <location>?",  // MiniMessage with placeholders
    "<gray>This will cost <cost> emeralds.",
    Key.key("dialog:confirm"),
    Key.key("dialog:cancel"),
    Placeholder.component("location", destinationName),
    Placeholder.component("cost", Component.text(teleportCost))
);
```

## Performance Considerations

- Use appropriate receiver ranges to limit particle visibility
- Prefer `ParticleEffects.builder()` for complex or reusable effects
- Use presets for common effects to avoid code duplication
- Consider particle count limits for performance-critical sections
- Use `ParticleScheduler` for entity-based effects to ensure proper threading
- Cache frequently used `ParticleScheduler` instances per plugin
