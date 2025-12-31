package io.voluble.michellelib.text.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for components used in item meta (display name / lore).
 *
 * <p>Vanilla lore has a parent style of italic=true; Paper docs recommend using
 * {@link Component#decorationIfAbsent(TextDecoration, TextDecoration.State)} to disable italics
 * without overriding user-specified italics.</p>
 */
public final class ItemText {

    private ItemText() {
    }

    public static @NotNull Component noItalicIfAbsent(final @NotNull Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static @NotNull List<Component> noItalicIfAbsent(final @NotNull List<Component> components) {
        final List<Component> out = new ArrayList<>(components.size());
        for (final Component component : components) {
            out.add(noItalicIfAbsent(component));
        }
        return out;
    }
}



