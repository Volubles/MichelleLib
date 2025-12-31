package io.voluble.michellelib.item.spec;

import io.voluble.michellelib.item.codec.NbtItemStacks;
import io.voluble.michellelib.item.template.ItemTemplate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable NBT-bytes-only item specification.
 *
 * <p>This is intended for persistence and reuse. The stored bytes are the Paper-native NBT encoding
 * produced by {@link org.bukkit.inventory.ItemStack#serializeAsBytes()} and are migration-safe.</p>
 */
public final class NbtItemSpec {

    private final byte[] nbtBytes;

    private NbtItemSpec(final byte @NotNull [] nbtBytes) {
        this.nbtBytes = Arrays.copyOf(nbtBytes, nbtBytes.length);
    }

    public static @NotNull NbtItemSpec of(final @NotNull ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return new NbtItemSpec(NbtItemStacks.encode(stack));
    }

    public static @NotNull NbtItemSpec fromBytes(final byte @NotNull [] nbtBytes) {
        Objects.requireNonNull(nbtBytes, "nbtBytes");
        return new NbtItemSpec(nbtBytes);
    }

    public byte @NotNull [] bytes() {
        return Arrays.copyOf(this.nbtBytes, this.nbtBytes.length);
    }

    public @NotNull ItemStack materialize() {
        return NbtItemStacks.decode(this.nbtBytes);
    }

    public @NotNull ItemTemplate asTemplate() {
        return viewer -> materialize();
    }
}



