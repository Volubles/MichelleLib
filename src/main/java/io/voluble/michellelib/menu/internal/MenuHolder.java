package io.voluble.michellelib.menu.internal;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class MenuHolder implements InventoryHolder {
    private final UUID playerId;
    private final long viewToken;

    public MenuHolder(final UUID playerId, final long viewToken) {
        this.playerId = playerId;
        this.viewToken = viewToken;
    }

    public UUID playerId() {
        return playerId;
    }

    public long viewToken() {
        return viewToken;
    }

    @Override
    public Inventory getInventory() {
        return null; // not used; inventory is provided by Bukkit when created with this holder
    }
}


