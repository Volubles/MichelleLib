package io.voluble.michellelib.item.codec;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Encodes and decodes {@link ItemStack}s for persistent storage.
 */
public interface ItemStackCodec {

    byte @NotNull [] encode(final @NotNull ItemStack stack);

    @NotNull ItemStack decode(final byte @NotNull [] bytes);
}



