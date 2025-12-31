package io.voluble.michellelib.registry;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.BiomeTagKeys;
import io.papermc.paper.registry.keys.tags.BlockTagKeys;
import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTagKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Advanced registry access utilities for Paper.
 *
 * <p>Provides comprehensive methods for accessing registry entries with various error handling strategies,
 * streaming capabilities, tag support, and convenience methods for common operations.</p>
 *
 * <p>All methods are thread-safe and can be used from any thread.</p>
 */
public final class Registries {

    private Registries() {
    }

    /**
     * Gets a registry entry, returning {@code Optional.empty()} if not found.
     *
     * @param registryKey the registry key
     * @param key the key of the entry
     * @param <T> the entry type
     * @return an optional containing the entry if found, empty otherwise
     */
    @NotNull
    public static <T extends Keyed> Optional<T> getOptional(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull Key key
    ) {
        final Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        return Optional.ofNullable(registry.get(key));
    }

    /**
     * Gets a registry entry by typed key, returning {@code Optional.empty()} if not found.
     *
     * @param typedKey the typed key
     * @param <T> the entry type
     * @return an optional containing the entry if found, empty otherwise
     */
    @NotNull
    public static <T extends Keyed> Optional<T> getOptional(final @NotNull TypedKey<T> typedKey) {
        final Registry<T> registry = RegistryAccess.registryAccess().getRegistry(typedKey.registryKey());
        return Optional.ofNullable(registry.get(typedKey));
    }

    /**
     * Gets a registry entry or returns a default value if not found.
     *
     * @param registryKey the registry key
     * @param key the key of the entry
     * @param defaultValue the default value to return if not found
     * @param <T> the entry type
     * @return the entry if found, otherwise the default value
     */
    @NotNull
    public static <T extends Keyed> T getOrDefault(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull Key key,
            final @NotNull T defaultValue
    ) {
        return getOptional(registryKey, key).orElse(defaultValue);
    }

    /**
     * Gets a registry entry or returns a default value if not found.
     *
     * @param typedKey the typed key
     * @param defaultValue the default value to return if not found
     * @param <T> the entry type
     * @return the entry if found, otherwise the default value
     */
    @NotNull
    public static <T extends Keyed> T getOrDefault(
            final @NotNull TypedKey<T> typedKey,
            final @NotNull T defaultValue
    ) {
        return getOptional(typedKey).orElse(defaultValue);
    }

    /**
     * Gets a registry entry or throws a {@link RegistryException} with a descriptive message if not found.
     *
     * @param registryKey the registry key
     * @param key the key of the entry
     * @param <T> the entry type
     * @return the entry
     * @throws RegistryException if the entry is not found
     */
    @NotNull
    public static <T extends Keyed> T getOrThrow(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull Key key
    ) throws RegistryException {
        final Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        final T value = registry.get(key);
        if (value == null) {
            throw new RegistryException("No entry found for key " + key + " in registry " + registryKey);
        }
        return value;
    }

    /**
     * Gets a registry entry or throws a {@link RegistryException} with a descriptive message if not found.
     *
     * @param typedKey the typed key
     * @param <T> the entry type
     * @return the entry
     * @throws RegistryException if the entry is not found
     */
    @NotNull
    public static <T extends Keyed> T getOrThrow(final @NotNull TypedKey<T> typedKey) throws RegistryException {
        final Registry<T> registry = RegistryAccess.registryAccess().getRegistry(typedKey.registryKey());
        final T value = registry.get(typedKey);
        if (value == null) {
            throw new RegistryException("No entry found for key " + typedKey.key() + " in registry " + typedKey.registryKey());
        }
        return value;
    }

    // ---- Streaming and Filtering Methods ----

    /**
     * Returns a stream of all entries in the specified registry.
     *
     * @param registryKey the registry key
     * @param <T> the entry type
     * @return a stream of all registry entries
     */
    @NotNull
    public static <T extends Keyed> Stream<T> stream(@NotNull RegistryKey<T> registryKey) {
        return registry(registryKey).stream();
    }

    /**
     * Returns a stream of entries from the specified registry that match the given predicate.
     *
     * @param registryKey the registry key
     * @param predicate the predicate to test entries against
     * @param <T> the entry type
     * @return a stream of matching registry entries
     */
    @NotNull
    public static <T extends Keyed> Stream<T> stream(
        @NotNull RegistryKey<T> registryKey,
        @NotNull Predicate<T> predicate
    ) {
        return stream(registryKey).filter(predicate);
    }

    /**
     * Finds the first registry entry that matches the given predicate.
     *
     * @param registryKey the registry key
     * @param predicate the predicate to test entries against
     * @param <T> the entry type
     * @return an optional containing the first matching entry, empty if none found
     */
    @NotNull
    public static <T extends Keyed> Optional<T> findFirst(
        @NotNull RegistryKey<T> registryKey,
        @NotNull Predicate<T> predicate
    ) {
        return stream(registryKey, predicate).findFirst();
    }

    /**
     * Counts the number of entries in the specified registry.
     *
     * @param registryKey the registry key
     * @param <T> the entry type
     * @return the number of entries in the registry
     */
    public static <T extends Keyed> int count(@NotNull RegistryKey<T> registryKey) {
        return (int) stream(registryKey).count();
    }

    // ---- Tag Support ----

    /**
     * Gets a tag from the specified registry.
     *
     * @param tagKey the tag key
     * @param <T> the tag element type
     * @return the tag if found, null otherwise
     */
    @NotNull
    public static <T extends Keyed> Optional<Tag<T>> tag(@NotNull io.papermc.paper.registry.tag.TagKey<T> tagKey) {
        return Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(tagKey.registryKey()).getTag(tagKey));
    }

    /**
     * Checks if an entry is contained in the specified tag.
     *
     * @param tagKey the tag key
     * @param entry the entry to check
     * @param <T> the tag element type
     * @return true if the entry is in the tag, false otherwise
     */
    public static <T extends Keyed> boolean isTagged(
        @NotNull io.papermc.paper.registry.tag.TagKey<T> tagKey,
        @NotNull T entry
    ) {
        return tag(tagKey).map(tag -> tag.isTagged(entry)).orElse(false);
    }

    /**
     * Gets all entries in the specified tag as a stream.
     *
     * @param tagKey the tag key
     * @param <T> the tag element type
     * @return a stream of all entries in the tag
     */
    @NotNull
    public static <T extends Keyed> Stream<T> streamTag(@NotNull io.papermc.paper.registry.tag.TagKey<T> tagKey) {
        return tag(tagKey).map(Tag::getValues).orElse(Stream.empty());
    }

    // ---- Enhanced Lookup Methods ----

    /**
     * Gets a registry entry by string key, with automatic namespace handling.
     * If no namespace is provided, defaults to "minecraft".
     *
     * @param registryKey the registry key
     * @param key the key string (with or without namespace)
     * @param <T> the entry type
     * @return an optional containing the entry if found
     */
    @NotNull
    public static <T extends Keyed> Optional<T> getByString(
        @NotNull RegistryKey<T> registryKey,
        @NotNull String key
    ) {
        Key parsedKey = key.contains(":") ? Key.key(key) : Key.key("minecraft", key);
        return getOptional(registryKey, parsedKey);
    }

    /**
     * Gets a registry entry by string key or throws if not found.
     *
     * @param registryKey the registry key
     * @param key the key string (with or without namespace)
     * @param <T> the entry type
     * @return the entry
     * @throws RegistryException if not found
     */
    @NotNull
    public static <T extends Keyed> T getByStringOrThrow(
        @NotNull RegistryKey<T> registryKey,
        @NotNull String key
    ) throws RegistryException {
        Key parsedKey = key.contains(":") ? Key.key(key) : Key.key("minecraft", key);
        return getOrThrow(registryKey, parsedKey);
    }

    // ---- Common Tag Convenience Methods ----

    /**
     * Checks if a block is in the specified block tag.
     */
    public static boolean isBlockTagged(@NotNull io.papermc.paper.registry.tag.TagKey<org.bukkit.block.BlockType> tagKey, @NotNull org.bukkit.block.BlockType block) {
        return isTagged(tagKey, block);
    }

    /**
     * Checks if an item is in the specified item tag.
     */
    public static boolean isItemTagged(@NotNull io.papermc.paper.registry.tag.TagKey<org.bukkit.inventory.ItemType> tagKey, @NotNull org.bukkit.inventory.ItemType item) {
        return isTagged(tagKey, item);
    }

    /**
     * Checks if an entity type is in the specified entity type tag.
     */
    public static boolean isEntityTagged(@NotNull io.papermc.paper.registry.tag.TagKey<org.bukkit.entity.EntityType> tagKey, @NotNull org.bukkit.entity.EntityType entityType) {
        return isTagged(tagKey, entityType);
    }

    /**
     * Checks if a biome is in the specified biome tag.
     */
    public static boolean isBiomeTagged(@NotNull io.papermc.paper.registry.tag.TagKey<org.bukkit.block.Biome> tagKey, @NotNull org.bukkit.block.Biome biome) {
        return isTagged(tagKey, biome);
    }

    // ---- Enhanced Convenience Methods for Common Registries ----

    // ---- Items ----

    /**
     * Gets an item type from the item registry.
     */
    @NotNull
    public static Optional<org.bukkit.inventory.ItemType> item(final @NotNull Key key) {
        return getOptional(RegistryKey.ITEM, key);
    }

    /**
     * Gets an item type by string key.
     */
    @NotNull
    public static Optional<org.bukkit.inventory.ItemType> item(final @NotNull String key) {
        return getByString(RegistryKey.ITEM, key);
    }

    /**
     * Streams all item types.
     */
    @NotNull
    public static Stream<org.bukkit.inventory.ItemType> items() {
        return stream(RegistryKey.ITEM);
    }

    /**
     * Streams item types that match a predicate.
     */
    @NotNull
    public static Stream<org.bukkit.inventory.ItemType> items(@NotNull Predicate<org.bukkit.inventory.ItemType> predicate) {
        return stream(RegistryKey.ITEM, predicate);
    }

    // ---- Blocks ----

    /**
     * Gets a block type from the block registry.
     */
    @NotNull
    public static Optional<org.bukkit.block.BlockType> block(final @NotNull Key key) {
        return getOptional(RegistryKey.BLOCK, key);
    }

    /**
     * Gets a block type by string key.
     */
    @NotNull
    public static Optional<org.bukkit.block.BlockType> block(final @NotNull String key) {
        return getByString(RegistryKey.BLOCK, key);
    }

    /**
     * Streams all block types.
     */
    @NotNull
    public static Stream<org.bukkit.block.BlockType> blocks() {
        return stream(RegistryKey.BLOCK);
    }

    /**
     * Streams block types that match a predicate.
     */
    @NotNull
    public static Stream<org.bukkit.block.BlockType> blocks(@NotNull Predicate<org.bukkit.block.BlockType> predicate) {
        return stream(RegistryKey.BLOCK, predicate);
    }

    // ---- Enchantments ----

    /**
     * Gets an enchantment from the enchantment registry.
     */
    @NotNull
    public static Optional<org.bukkit.enchantments.Enchantment> enchantment(final @NotNull Key key) {
        return getOptional(RegistryKey.ENCHANTMENT, key);
    }

    /**
     * Gets an enchantment by string key.
     */
    @NotNull
    public static Optional<org.bukkit.enchantments.Enchantment> enchantment(final @NotNull String key) {
        return getByString(RegistryKey.ENCHANTMENT, key);
    }

    /**
     * Streams all enchantments.
     */
    @NotNull
    public static Stream<org.bukkit.enchantments.Enchantment> enchantments() {
        return stream(RegistryKey.ENCHANTMENT);
    }

    // ---- Potion Effects ----

    /**
     * Gets a potion effect type from the mob effect registry.
     */
    @NotNull
    public static Optional<org.bukkit.potion.PotionEffectType> potionEffect(final @NotNull Key key) {
        return getOptional(RegistryKey.MOB_EFFECT, key);
    }

    /**
     * Gets a potion effect type by string key.
     */
    @NotNull
    public static Optional<org.bukkit.potion.PotionEffectType> potionEffect(final @NotNull String key) {
        return getByString(RegistryKey.MOB_EFFECT, key);
    }

    /**
     * Streams all potion effect types.
     */
    @NotNull
    public static Stream<org.bukkit.potion.PotionEffectType> potionEffects() {
        return stream(RegistryKey.MOB_EFFECT);
    }

    // ---- Sounds ----

    /**
     * Gets a sound event from the sound event registry.
     */
    @NotNull
    public static Optional<org.bukkit.Sound> sound(final @NotNull Key key) {
        return getOptional(RegistryKey.SOUND_EVENT, key);
    }

    /**
     * Gets a sound by string key.
     */
    @NotNull
    public static Optional<org.bukkit.Sound> sound(final @NotNull String key) {
        return getByString(RegistryKey.SOUND_EVENT, key);
    }

    /**
     * Streams all sounds.
     */
    @NotNull
    public static Stream<org.bukkit.Sound> sounds() {
        return stream(RegistryKey.SOUND_EVENT);
    }

    // ---- Entity Types ----

    /**
     * Gets an entity type from the entity type registry.
     */
    @NotNull
    public static Optional<org.bukkit.entity.EntityType> entityType(final @NotNull Key key) {
        return getOptional(RegistryKey.ENTITY_TYPE, key);
    }

    /**
     * Gets an entity type by string key.
     */
    @NotNull
    public static Optional<org.bukkit.entity.EntityType> entityType(final @NotNull String key) {
        return getByString(RegistryKey.ENTITY_TYPE, key);
    }

    /**
     * Streams all entity types.
     */
    @NotNull
    public static Stream<org.bukkit.entity.EntityType> entityTypes() {
        return stream(RegistryKey.ENTITY_TYPE);
    }

    /**
     * Streams entity types that match a predicate.
     */
    @NotNull
    public static Stream<org.bukkit.entity.EntityType> entityTypes(@NotNull Predicate<org.bukkit.entity.EntityType> predicate) {
        return stream(RegistryKey.ENTITY_TYPE, predicate);
    }

    // ---- Biomes ----

    /**
     * Gets a biome from the biome registry.
     */
    @NotNull
    public static Optional<org.bukkit.block.Biome> biome(final @NotNull Key key) {
        return getOptional(RegistryKey.BIOME, key);
    }

    /**
     * Gets a biome by string key.
     */
    @NotNull
    public static Optional<org.bukkit.block.Biome> biome(final @NotNull String key) {
        return getByString(RegistryKey.BIOME, key);
    }

    /**
     * Streams all biomes.
     */
    @NotNull
    public static Stream<org.bukkit.block.Biome> biomes() {
        return stream(RegistryKey.BIOME);
    }

    // ---- Damage Types ----

    /**
     * Gets a damage type from the damage type registry.
     */
    @NotNull
    public static Optional<org.bukkit.damage.DamageType> damageType(final @NotNull Key key) {
        return getOptional(RegistryKey.DAMAGE_TYPE, key);
    }

    /**
     * Gets a damage type by string key.
     */
    @NotNull
    public static Optional<org.bukkit.damage.DamageType> damageType(final @NotNull String key) {
        return getByString(RegistryKey.DAMAGE_TYPE, key);
    }

    /**
     * Streams all damage types.
     */
    @NotNull
    public static Stream<org.bukkit.damage.DamageType> damageTypes() {
        return stream(RegistryKey.DAMAGE_TYPE);
    }

    /**
     * Gets the registry for the given registry key.
     *
     * @param registryKey the registry key
     * @param <T> the entry type
     * @return the registry
     */
    @NotNull
    public static <T extends Keyed> Registry<T> registry(final @NotNull RegistryKey<T> registryKey) {
        return RegistryAccess.registryAccess().getRegistry(registryKey);
    }
}

