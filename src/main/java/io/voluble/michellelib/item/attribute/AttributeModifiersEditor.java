package io.voluble.michellelib.item.attribute;

import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplay;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Mutable editor for {@link ItemAttributeModifiers} contents.
 *
 * <p>This is a utility for composing an attribute modifier list before materializing it back into a
 * data component.</p>
 */
public final class AttributeModifiersEditor {

    public record Entry(
        @NotNull Attribute attribute,
        @NotNull AttributeModifier modifier,
        @NotNull EquipmentSlotGroup group,
        @NotNull AttributeModifierDisplay display
    ) {
        public Entry {
            Objects.requireNonNull(attribute, "attribute");
            Objects.requireNonNull(modifier, "modifier");
            Objects.requireNonNull(group, "group");
            Objects.requireNonNull(display, "display");
        }
    }

    private final List<Entry> entries;

    public AttributeModifiersEditor() {
        this.entries = new ArrayList<>();
    }

    public static @NotNull AttributeModifiersEditor from(final @NotNull ItemAttributeModifiers modifiers) {
        Objects.requireNonNull(modifiers, "modifiers");
        final AttributeModifiersEditor editor = new AttributeModifiersEditor();
        for (final ItemAttributeModifiers.Entry e : modifiers.modifiers()) {
            editor.add(e.attribute(), e.modifier(), e.getGroup(), e.display());
        }
        return editor;
    }

    public @NotNull List<Entry> entries() {
        return List.copyOf(this.entries);
    }

    public @NotNull AttributeModifiersEditor clear() {
        this.entries.clear();
        return this;
    }

    public @NotNull AttributeModifiersEditor removeIf(final @NotNull Predicate<Entry> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        this.entries.removeIf(predicate);
        return this;
    }

    public @NotNull AttributeModifiersEditor removeAttribute(final @NotNull Attribute attribute) {
        Objects.requireNonNull(attribute, "attribute");
        return removeIf(e -> e.attribute() == attribute);
    }

    public @NotNull AttributeModifiersEditor add(
        final @NotNull Attribute attribute,
        final @NotNull AttributeModifier modifier
    ) {
        Objects.requireNonNull(attribute, "attribute");
        Objects.requireNonNull(modifier, "modifier");
        return add(attribute, modifier, modifier.getSlotGroup(), AttributeModifierDisplay.reset());
    }

    public @NotNull AttributeModifiersEditor add(
        final @NotNull Attribute attribute,
        final @NotNull AttributeModifier modifier,
        final @NotNull EquipmentSlotGroup group,
        final @NotNull AttributeModifierDisplay display
    ) {
        this.entries.add(new Entry(attribute, modifier, group, display));
        return this;
    }
}



