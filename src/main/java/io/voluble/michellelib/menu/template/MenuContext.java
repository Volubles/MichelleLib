package io.voluble.michellelib.menu.template;

import io.voluble.michellelib.menu.MenuService;
import io.voluble.michellelib.menu.MenuSession;
import io.voluble.michellelib.menu.item.MenuItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Context for menu lifecycle hooks.
 */
public final class MenuContext {
    private final MenuDefinition menu;
    private final MenuOpenTarget target;

    MenuContext(final @NotNull MenuDefinition menu, final @NotNull MenuOpenTarget target) {
        this.menu = Objects.requireNonNull(menu, "menu");
        this.target = Objects.requireNonNull(target, "target");
    }

    public @NotNull MenuDefinition menu() {
        return menu;
    }

    public @NotNull MenuService menus() {
        return target.menus();
    }

    public @NotNull MenuSession session() {
        return target.session();
    }

    public @NotNull Player player() {
        return target.player();
    }

    /**
     * Safe, click-handler-friendly actions (close/transition).
     */
    public @NotNull io.voluble.michellelib.menu.item.MenuActions actions() {
        return target.actions();
    }

    public void setItem(final int slot, final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        session().setItem(slot, item);
    }

    /**
     * Open another menu and automatically wire a "back" action to reopen this menu.
     */
    public void open(final @NotNull MenuDefinition other) {
        Objects.requireNonNull(other, "other");
        // Push back action first, then transition.
        session().pushBack(() -> menu().open(menus(), player()));
        actions().transition(() -> other.open(menus(), player()));
    }

    public void back() {
        actions().transition(session()::goBack);
    }
}


