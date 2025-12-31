package io.voluble.michellelib.item.template;

import io.voluble.michellelib.item.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Factories for creating {@link ItemTemplate}s.
 */
public final class ItemTemplates {
    private ItemTemplates() {
    }

    /**
     * Returns a template that always returns a clone of the provided stack.
     */
    public static @NotNull ItemTemplate fixed(final @NotNull ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return viewer -> stack.clone();
    }

    /**
     * Returns a template that builds a base stack once, then clones and reuses it for each render.
     */
    public static @NotNull ItemTemplate fixed(final @NotNull Material type, final @NotNull Consumer<ItemBuilder> base) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(base, "base");

        final ItemBuilder builder = ItemBuilder.of(type);
        base.accept(builder);
        final ItemStack built = builder.build();
        return fixed(built);
    }

    /**
     * Returns a template that clones a pre-built base stack and then applies viewer-dependent mutations.
     */
    public static @NotNull ItemTemplate dynamic(
        final @NotNull ItemStack base,
        final @NotNull BiConsumer<@Nullable Player, ItemBuilder> perViewer
    ) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(perViewer, "perViewer");

        return viewer -> {
            final ItemBuilder builder = ItemBuilder.from(base);
            perViewer.accept(viewer, builder);
            return builder.build();
        };
    }

    /**
     * Returns a template that creates a new stack each render and applies viewer-dependent mutations.
     */
    public static @NotNull ItemTemplate dynamic(
        final @NotNull Material type,
        final @NotNull BiConsumer<@Nullable Player, ItemBuilder> perViewer
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(perViewer, "perViewer");

        return viewer -> {
            final ItemBuilder builder = ItemBuilder.of(type);
            perViewer.accept(viewer, builder);
            return builder.build();
        };
    }
}



