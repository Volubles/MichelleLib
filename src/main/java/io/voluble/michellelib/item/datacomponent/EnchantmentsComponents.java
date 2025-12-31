package io.voluble.michellelib.item.datacomponent;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience utilities for enchantment-related components.
 */
public final class EnchantmentsComponents {
    private EnchantmentsComponents() {
    }

    /**
     * Adds or replaces an enchantment level on the {@link DataComponentTypes#ENCHANTMENTS} component.
     */
    public static void add(final @NotNull ItemStack stack, final @NotNull Enchantment enchantment, final int level) {
        mutate(stack, DataComponentTypes.ENCHANTMENTS, builder -> builder.add(enchantment, level));
    }

    /**
     * Adds or replaces an enchantment level on the {@link DataComponentTypes#STORED_ENCHANTMENTS} component.
     */
    public static void addStored(final @NotNull ItemStack stack, final @NotNull Enchantment enchantment, final int level) {
        mutate(stack, DataComponentTypes.STORED_ENCHANTMENTS, builder -> builder.add(enchantment, level));
    }

    private static void mutate(
        final @NotNull ItemStack stack,
        final DataComponentType.@NotNull Valued<ItemEnchantments> type,
        final @NotNull java.util.function.Consumer<ItemEnchantments.Builder> edit
    ) {
        final ItemEnchantments current = stack.getData(type);
        final Map<Enchantment, Integer> existing = current == null ? Map.of() : current.enchantments();
        final Map<Enchantment, Integer> copy = new HashMap<>(existing);

        final ItemEnchantments.Builder builder = ItemEnchantments.itemEnchantments();
        builder.addAll(copy);
        edit.accept(builder);
        stack.setData(type, builder.build());
    }
}


