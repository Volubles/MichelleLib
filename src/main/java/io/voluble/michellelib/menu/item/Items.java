package io.voluble.michellelib.menu.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Small bundle of common MenuItem implementations.
 * <p>
 * This is intentionally "tiny"; consumers are expected to build their own higher-level item DSLs.
 */
public final class Items {
    private Items() {
    }

    public static final class DisplayItem implements MenuItem {
        private final ItemStack base;

        public DisplayItem(final ItemStack base) {
            this.base = base;
        }

        @Override
        public ItemStack render(final Player viewer) {
            return base.clone();
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            return true;
        }
    }

    public static final class UpdatableItem implements MenuItem {
        private int value = 0;
        private final Material mat;
        private final Component name;

        public UpdatableItem(final Material mat, final Component name) {
            this.mat = mat;
            this.name = name;
        }

        @Override
        public ItemStack render(final Player viewer) {
            ItemStack it = new ItemStack(mat);
            ItemMeta im = it.getItemMeta();
            im.displayName(name);
            im.lore(List.of(Component.text("Value: " + value)));
            it.setItemMeta(im);
            return it;
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            value++;
            ctx.player().playSound(ctx.player(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1f, 1.2f);
            return true;
        }
    }

    public static final class BackItem implements MenuItem {
        private final Runnable back;

        public BackItem(final Runnable back) {
            this.back = back;
        }

        @Override
        public ItemStack render(final Player viewer) {
            return new ItemStack(Material.ARROW);
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            // opening another inventory/menu inside InventoryClickEvent is unsafe; schedule next tick.
            ctx.actions().transition(back);
            return true;
        }
    }

    public static final class CloseItem implements MenuItem {
        @Override
        public ItemStack render(final Player viewer) {
            return new ItemStack(Material.BARRIER);
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            // closing inside InventoryClickEvent is unsafe; schedule next tick.
            ctx.actions().close();
            return true;
        }
    }

    public static final class OpenMenuItem implements MenuItem {
        private final Runnable open;

        public OpenMenuItem(final Runnable open) {
            this.open = open;
        }

        @Override
        public ItemStack render(final Player viewer) {
            return new ItemStack(Material.BOOK);
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            // opening another inventory/menu inside InventoryClickEvent is unsafe; schedule next tick.
            ctx.actions().transition(open);
            return true;
        }
    }

    public static final class PageItem implements MenuItem {
        private final boolean next;
        private final Runnable action;

        public PageItem(final boolean next, final Runnable action) {
            this.next = next;
            this.action = action;
        }

        @Override
        public ItemStack render(final Player viewer) {
            return new ItemStack(next ? Material.ARROW : Material.SPECTRAL_ARROW);
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            action.run();
            return true;
        }
    }

    public static final class ScrollItem implements MenuItem {
        private final int delta;
        private final Runnable action;

        public ScrollItem(final int delta, final Runnable action) {
            this.delta = delta;
            this.action = action;
        }

        @Override
        public ItemStack render(final Player viewer) {
            return new ItemStack(delta > 0 ? Material.GREEN_DYE : Material.RED_DYE);
        }

        @Override
        public boolean onClick(final ClickContext ctx) {
            action.run();
            return true;
        }
    }
}


