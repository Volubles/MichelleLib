package io.voluble.michellelib.menu.internal;

import io.voluble.michellelib.menu.MenuService;
import io.voluble.michellelib.menu.MenuSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class MenuEventListener implements Listener {
    private final MenuService menus;

    public MenuEventListener(final MenuService menus) {
        this.menus = menus;
    }

    @EventHandler
    public void onClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        MenuSession s = menus.existing(p);
        if (s == null || s.view() == null) return;
        if (!e.getView().equals(s.view())) return;
        if (!(e.getView().getTopInventory().getHolder(false) instanceof MenuHolder mh)) return;
        if (mh.viewToken() != s.currentViewToken()) return;
        s.handleClick(e);
    }

    @EventHandler
    public void onDrag(final InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        MenuSession s = menus.existing(p);
        if (s == null || s.view() == null || !e.getView().equals(s.view())) return;
        if (!(e.getView().getTopInventory().getHolder(false) instanceof MenuHolder mh)) return;
        if (mh.viewToken() != s.currentViewToken()) return;
        s.handleDrag(e);
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        MenuSession s = menus.existing(p);
        if (s == null || s.view() == null) return;
        if (!e.getView().equals(s.view())) return;
        if (!(e.getView().getTopInventory().getHolder(false) instanceof MenuHolder mh)) return;
        if (mh.viewToken() != s.currentViewToken()) return;
        boolean willReopen = s.isPreventClose();
        // Ensure inventory/cursor operations happen on the owning entity thread on Folia.
        menus.scheduler().runEntity(p, () -> s.handleClose(willReopen));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        // Don't touch inventory state here; just drop session state.
        menus.removeSession(e.getPlayer());
    }
}


