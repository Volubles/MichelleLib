package io.voluble.michellelib.item.datacomponent;

import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Introspection helpers for data components on {@link ItemStack}s.
 */
public final class ItemDataComponentInspector {
    private ItemDataComponentInspector() {
    }

    /**
     * Returns a stable, readable summary of the item type defaults and stack overrides.
     * <p>
     * The returned format is intended for logs and debugging, not user-facing chat.
     */
    public static @NotNull String describe(final @NotNull ItemStack stack) {
        final Material type = stack.getType();

        final Set<DataComponentType> prototypeTypes = type.isItem() ? type.getDefaultDataTypes() : Set.of();
        final Set<DataComponentType> patchTypes = stack.getDataTypes();

        final Comparator<DataComponentType> byKey = Comparator.comparing(t -> key(t).asString());
        final Set<DataComponentType> allKnown = new TreeSet<>(byKey);
        allKnown.addAll(prototypeTypes);
        allKnown.addAll(patchTypes);

        final Set<DataComponentType> overridden = new LinkedHashSet<>();
        final Set<DataComponentType> presentInPatch = new LinkedHashSet<>(new TreeSet<>(byKey));
        final Set<DataComponentType> removedFromPrototype = new LinkedHashSet<>();

        for (final DataComponentType component : allKnown) {
            if (stack.isDataOverridden(component)) {
                overridden.add(component);
            }
            if (patchTypes.contains(component)) {
                presentInPatch.add(component);
            }

            if (prototypeTypes.contains(component) && stack.isDataOverridden(component)) {
                final boolean stillPresent = stack.hasData(component);
                if (!stillPresent) {
                    removedFromPrototype.add(component);
                }
            }
        }

        final StringBuilder out = new StringBuilder(256);
        out.append("ItemStack{type=").append(type.getKey());
        out.append(", amount=").append(stack.getAmount());
        out.append(", prototypeTypes=").append(prototypeTypes.size());
        out.append(", patchTypes=").append(patchTypes.size());
        out.append(", overridden=").append(overridden.size());
        out.append(", removedFromPrototype=").append(removedFromPrototype.size());
        out.append("}");
        out.append('\n');

        out.append("prototype: ").append(formatKeys(prototypeTypes, byKey)).append('\n');
        out.append("patch:     ").append(formatKeys(patchTypes, byKey)).append('\n');
        out.append("overrides: ").append(formatKeys(overridden, byKey)).append('\n');
        out.append("removed:   ").append(formatKeys(removedFromPrototype, byKey));

        return out.toString();
    }

    private static @NotNull NamespacedKey key(final @NotNull DataComponentType type) {
        return type.getKey();
    }

    private static @NotNull String formatKeys(final @NotNull Set<DataComponentType> types, final @NotNull Comparator<DataComponentType> byKey) {
        if (types.isEmpty()) {
            return "[]";
        }
        final StringBuilder out = new StringBuilder(types.size() * 20);
        out.append('[');
        boolean first = true;
        final TreeSet<DataComponentType> sorted = new TreeSet<>(byKey);
        sorted.addAll(types);
        for (final DataComponentType t : sorted) {
            if (!first) {
                out.append(", ");
            }
            first = false;
            out.append(key(t).asString());
        }
        out.append(']');
        return out.toString();
    }
}


