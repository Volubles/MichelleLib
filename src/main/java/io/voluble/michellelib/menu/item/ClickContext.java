package io.voluble.michellelib.menu.item;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Context passed to {@link MenuItem#onClick(ClickContext)}.
 * <p>
 * Includes a small {@link MenuActions} helper for safe operations that must not occur directly inside the click event
 * (open/close inventory, etc.). See Bukkit/Paper {@code InventoryClickEvent} javadoc.
 */
public record ClickContext(
		Player player,
		ClickType click,
		int slot,
		Inventory top,
		Inventory bottom,
		ItemStack cursor,
		boolean isTopInventory,
		boolean shiftClick,
		MenuActions actions
) {
}