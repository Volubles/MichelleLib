package io.voluble.michellelib.menu.template;

import io.voluble.michellelib.menu.MenuService;
import io.voluble.michellelib.menu.MenuSession;
import io.voluble.michellelib.menu.item.MenuActions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Lightweight bundle passed to title providers etc.
 */
public final class MenuOpenTarget {
    private final MenuService menus;
    private final MenuSession session;
    private final Player player;
    private final MenuActions actions;

    public MenuOpenTarget(final @NotNull MenuService menus, final @NotNull MenuSession session, final @NotNull Player player) {
        this.menus = Objects.requireNonNull(menus, "menus");
        this.session = Objects.requireNonNull(session, "session");
        this.player = Objects.requireNonNull(player, "player");
        this.actions = new MenuActions(session);
    }

    public @NotNull MenuService menus() {
        return menus;
    }

    public @NotNull MenuSession session() {
        return session;
    }

    public @NotNull Player player() {
        return player;
    }

    public @NotNull MenuActions actions() {
        return actions;
    }
}


