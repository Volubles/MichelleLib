package io.voluble.michellelib.item.codec;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Paper-native NBT serialization helpers for {@link ItemStack}s and item arrays.
 *
 * <p>These APIs are migration-safe as they store data versioned NBT and use Minecraft's data converters.</p>
 */
public final class NbtItemStacks {
    private NbtItemStacks() {
    }

    public static byte @NotNull [] encode(final @NotNull ItemStack stack) {
        return stack.ensureServerConversions().serializeAsBytes();
    }

    public static @NotNull ItemStack decode(final byte @NotNull [] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }

    /**
     * Encodes items using Paper's array format ({@link ItemStack#serializeItemsAsBytes(Collection)}).
     * Null entries are preserved as {@link ItemStack#empty()}.
     */
    public static byte @NotNull [] encodeMany(final @NotNull Collection<@Nullable ItemStack> items) {
        final List<ItemStack> converted = new ArrayList<>(items.size());
        for (final ItemStack item : items) {
            if (item == null) {
                converted.add(null);
                continue;
            }
            converted.add(item.ensureServerConversions());
        }
        return ItemStack.serializeItemsAsBytes(converted);
    }

    public static byte @NotNull [] encodeMany(final @Nullable ItemStack @NotNull [] items) {
        final List<ItemStack> converted = new ArrayList<>(items.length);
        for (final ItemStack item : items) {
            if (item == null) {
                converted.add(null);
                continue;
            }
            converted.add(item.ensureServerConversions());
        }
        return ItemStack.serializeItemsAsBytes(converted);
    }

    public static @NotNull ItemStack @NotNull [] decodeMany(final byte @NotNull [] bytes) {
        return ItemStack.deserializeItemsFromBytes(bytes);
    }

    public static byte @NotNull [] encodeContents(final @NotNull Inventory inventory) {
        return encodeMany(inventory.getContents());
    }

    public static void decodeIntoContents(final @NotNull Inventory inventory, final byte @NotNull [] bytes) {
        inventory.setContents(decodeMany(bytes));
    }
}



