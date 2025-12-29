package io.voluble.michellelib.menu.item;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public interface MenuItem {
    /**
     * Produce the visual item. Called on the owning region/main thread.
     * You can precompute asynchronously (but do not touch Bukkit state off-thread).
     */
    ItemStack render(Player viewer);

    /**
     * Handle clicks. Return true to cancel the click.
     */
    default boolean onClick(final ClickContext ctx) {
        return true;
    }

    /**
     * Optional periodic refresh hook (if the menu heartbeat is enabled).
     */
    default void onRefresh(final Player viewer) {
    }

    /**
     * Optional dynamic name line for debug overlays.
     */
    default Component debugName() {
        return Component.text(getClass().getSimpleName());
    }

    /**
     * Whether this menu item allows players to place their own items into this slot. Deny-by-default.
     */
    default boolean isPlaceable() {
        return false;
    }

    /**
     * Whether the provided stack may be accepted into this placeable slot under the given context. Deny-by-default.
     */
    default boolean canAccept(final ItemStack stack, final ClickContext ctx) {
        return false;
    }

    /**
     * Maximum amount from the provided stack that may be accepted in one operation.
     * By default caps to 64 and the available amount.
     */
    default int maxAcceptAmount(final ItemStack stack, final ClickContext ctx) {
        return Math.min(stack.getAmount(), 64);
    }

    /**
     * Callback invoked after a successful insertion of the provided amount into this placeable slot.
     * Called on the owning region/main thread.
     */
    default void onInsert(final ItemStack inserted, final ClickContext ctx) {
    }

    /**
     * Whether number-key hotbar placement into this slot is allowed.
     */
    default boolean allowNumberKeyPlace() {
        return true;
    }

    /**
     * Whether shift-insert from the player's inventory is allowed into this slot.
     */
    default boolean allowShiftInsert() {
        return true;
    }

    /**
     * Whether items placed by the player into this slot should be returned when the menu closes.
     */
    default boolean returnPlacedItems() {
        return true;
    }

    /**
     * Items that this slot wishes to return to the player on close.
     */
    default List<ItemStack> itemsToReturnOnClose(final Player viewer) {
        return Collections.emptyList();
    }
}


