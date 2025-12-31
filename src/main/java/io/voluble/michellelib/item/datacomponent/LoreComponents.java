package io.voluble.michellelib.item.datacomponent;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience utilities for the {@link DataComponentTypes#LORE} component.
 */
public final class LoreComponents {
    private LoreComponents() {
    }

    /**
     * Returns the current patch lore lines, or an empty list if no patch lore is present.
     */
    public static @NotNull List<Component> patchLines(final @NotNull ItemStack stack) {
        final ItemLore lore = stack.getData(DataComponentTypes.LORE);
        if (lore == null) {
            return List.of();
        }
        return lore.lines();
    }

    /**
     * Sets the lore lines on the stack.
     */
    public static void setLines(final @NotNull ItemStack stack, final @NotNull List<? extends ComponentLike> lines) {
        stack.setData(DataComponentTypes.LORE, ItemLore.lore(lines));
    }

    /**
     * Appends a lore line while preserving existing patch lore lines.
     */
    public static void addLine(final @NotNull ItemStack stack, final @NotNull ComponentLike line) {
        final List<ComponentLike> newLines = new ArrayList<>(patchLines(stack));
        newLines.add(line);
        stack.setData(DataComponentTypes.LORE, ItemLore.lore(newLines));
    }

    /**
     * Resets lore back to the prototype default for the item type.
     */
    public static void reset(final @NotNull ItemStack stack) {
        stack.resetData(DataComponentTypes.LORE);
    }

    /**
     * Unsets lore (marks it as removed), even if the item type has a prototype lore.
     */
    public static void unset(final @NotNull ItemStack stack) {
        stack.unsetData(DataComponentTypes.LORE);
    }
}



