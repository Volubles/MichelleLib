package io.voluble.michellelib.registry.codec;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Codec for serializing and deserializing {@link TypedKey} instances.
 *
 * <p>Useful for storing registry references in PersistentDataContainer or configuration files.</p>
 */
public final class TypedKeyCodec {

    private TypedKeyCodec() {
    }

    /**
     * Creates a PersistentDataType for storing TypedKey instances.
     *
     * <p>The format is: "registry_key|namespace:key" (e.g., "minecraft:item|minecraft:diamond_sword")</p>
     */
    @NotNull
    public static PersistentDataType<String, TypedKey<?>> persistentDataType() {
        return new PersistentDataType<String, TypedKey<?>>() {
            @Override
            public @NotNull Class<String> getPrimitiveType() {
                return String.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public @NotNull Class<TypedKey<?>> getComplexType() {
                return (Class<TypedKey<?>>) (Class<?>) TypedKey.class;
            }

            @Override
            public @NotNull String toPrimitive(
                    final @NotNull TypedKey<?> complex,
                    final @NotNull PersistentDataAdapterContext context
            ) {
                return complex.registryKey().key().asString() + "|" + complex.key().asString();
            }

            @Override
            public @NotNull TypedKey<?> fromPrimitive(
                    final @NotNull String primitive,
                    final @NotNull PersistentDataAdapterContext context
            ) {
                return fromString(primitive).orElseThrow(() ->
                        new IllegalArgumentException("Invalid TypedKey format: " + primitive));
            }
        };
    }

    /**
     * Converts a TypedKey to a string representation.
     *
     * <p>Format: "registry_key|namespace:key"</p>
     */
    @NotNull
    public static String toString(final @NotNull TypedKey<?> typedKey) {
        return typedKey.registryKey().key().asString() + "|" + typedKey.key().asString();
    }

    /**
     * Parses a string representation back into a TypedKey.
     *
     * <p>Format: "registry_key|namespace:key"</p>
     *
     * @param string the string to parse
     * @return an optional containing the TypedKey if parsing succeeded
     */
    @NotNull
    public static Optional<TypedKey<?>> fromString(final @NotNull String string) {
        final int separatorIndex = string.indexOf('|');
        if (separatorIndex == -1) {
            return Optional.empty();
        }

        final String registryKeyString = string.substring(0, separatorIndex);
        final String keyString = string.substring(separatorIndex + 1);

        try {
            final Key registryKey = Key.key(registryKeyString);
            final Key key = Key.key(keyString);

            final RegistryKey<?> registryKeyInstance = findRegistryKey(registryKey);
            if (registryKeyInstance == null) {
                return Optional.empty();
            }

            return Optional.of(TypedKey.create(registryKeyInstance, key));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Converts a TypedKey to a NamespacedKey (for legacy APIs).
     */
    @NotNull
    public static NamespacedKey toNamespacedKey(final @NotNull TypedKey<?> typedKey) {
        return new NamespacedKey(typedKey.key().namespace(), typedKey.key().value());
    }

    /**
     * Creates a TypedKey from a NamespacedKey and registry key.
     */
    @NotNull
    public static <T extends org.bukkit.Keyed> TypedKey<T> fromNamespacedKey(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull NamespacedKey namespacedKey
    ) {
        return TypedKey.create(registryKey, Key.key(namespacedKey.namespace(), namespacedKey.getKey()));
    }

    private static RegistryKey<?> findRegistryKey(final @NotNull Key key) {
        try {
            return (RegistryKey<?>) RegistryKey.class.getField(key.value().toUpperCase()).get(null);
        } catch (final ReflectiveOperationException e) {
            return null;
        }
    }
}

