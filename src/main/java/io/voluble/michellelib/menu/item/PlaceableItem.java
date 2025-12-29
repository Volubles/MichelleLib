package io.voluble.michellelib.menu.item;

public interface PlaceableItem extends MenuItem {
    @Override
    default boolean isPlaceable() {
        return true;
    }
}


