package io.voluble.michellelib.item.template;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reusable item template which renders an {@link ItemStack} on demand.
 *
 * <p>Templates are intended for menus and other frequent rendering contexts where item construction
 * should be centralized and consistent.</p>
 */
@FunctionalInterface
public interface ItemTemplate {

    @NotNull ItemStack render(final @Nullable Player viewer);

    default @NotNull ItemStack render() {
        return render(null);
    }
}



