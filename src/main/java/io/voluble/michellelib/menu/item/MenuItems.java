package io.voluble.michellelib.menu.item;

import io.voluble.michellelib.item.template.ItemTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Adapters for working with {@link MenuItem}s.
 */
public final class MenuItems {
    private MenuItems() {
    }

    /**
     * Wraps an {@link ItemTemplate} into a {@link MenuItem} that renders per-viewer.
     */
    public static @NotNull MenuItem template(final @NotNull ItemTemplate template) {
        Objects.requireNonNull(template, "template");
        return template::render;
    }
}



