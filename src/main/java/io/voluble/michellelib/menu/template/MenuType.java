package io.voluble.michellelib.menu.template;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Describes how a menu inventory should be created/opened.
 */
public sealed interface MenuType permits MenuType.ChestRows, MenuType.InventoryKind {
    void open(@NotNull MenuOpenTarget target, @NotNull Function<MenuOpenTarget, Component> titleProvider);

    /**
     * Chest style menu with configurable rows (1-6).
     */
    record ChestRows(int rows) implements MenuType {
        public ChestRows {
            if (rows < 1 || rows > 6) throw new IllegalArgumentException("rows must be 1..6");
        }

        @Override
        public void open(final @NotNull MenuOpenTarget target, final @NotNull Function<MenuOpenTarget, Component> titleProvider) {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(titleProvider, "titleProvider");
            target.session().open(rows, titleProvider.apply(target));
        }
    }

    /**
     * Non-chest inventory types (hopper, anvil, etc).
     */
    record InventoryKind(@NotNull InventoryType type) implements MenuType {
        public InventoryKind {
            Objects.requireNonNull(type, "type");
        }

        @Override
        public void open(final @NotNull MenuOpenTarget target, final @NotNull Function<MenuOpenTarget, Component> titleProvider) {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(titleProvider, "titleProvider");
            target.session().open(type, titleProvider.apply(target));
        }
    }
}


