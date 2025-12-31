package io.voluble.michellelib.item.codec;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * NBT-based codec using Paper's migration-safe item serialization.
 *
 * <p>This uses {@link ItemStack#serializeAsBytes()} / {@link ItemStack#deserializeBytes(byte[])} and relies on
 * Minecraft data versioning and data converters for forward compatibility.</p>
 */
public final class NbtItemStackCodec implements ItemStackCodec {

    public static final @NotNull NbtItemStackCodec INSTANCE = new NbtItemStackCodec();

    private NbtItemStackCodec() {
    }

    @Override
    public byte @NotNull [] encode(final @NotNull ItemStack stack) {
        return stack.ensureServerConversions().serializeAsBytes();
    }

    @Override
    public @NotNull ItemStack decode(final byte @NotNull [] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }
}


