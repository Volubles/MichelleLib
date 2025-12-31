package io.voluble.michellelib.item.datacomponent;

import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Utilities for working with Paper's data component API on {@link ItemStack}s.
 * <p>
 * Paper exposes "prototype" (item type defaults) and "patch" (item modifications).
 * A patch may explicitly remove a component; in that case the effective value is {@code null}
 * even if the prototype has a default.
 */
public final class ItemDataComponents {
    private ItemDataComponents() {
    }

    /**
     * Returns the prototype (default) value for an item type.
     */
    public static <T> @Nullable T prototype(final @NotNull Material type, final DataComponentType.@NotNull Valued<T> component) {
        return type.getDefaultData(component);
    }

    /**
     * Returns whether the prototype (default) has the given component.
     */
    public static boolean prototypeHas(final @NotNull Material type, final @NotNull DataComponentType component) {
        return type.hasDefaultData(component);
    }

    /**
     * Returns the effective value for the given component on this stack.
     * <p>
     * If the component is overridden, the patch value is returned (including {@code null} for removals).
     * If the component is not overridden, the prototype default is returned.
     */
    public static <T> @Nullable T effective(final @NotNull ItemStack stack, final DataComponentType.@NotNull Valued<T> component) {
        if (stack.isDataOverridden(component)) {
            return stack.getData(component);
        }
        return stack.getType().getDefaultData(component);
    }

    /**
     * Returns whether the given non-valued component is effectively present on this stack.
     * <p>
     * If the component is overridden, the patch presence is returned.
     * If the component is not overridden, the prototype presence is returned.
     */
    public static boolean effective(final @NotNull ItemStack stack, final DataComponentType.@NotNull NonValued component) {
        if (stack.isDataOverridden(component)) {
            return stack.hasData(component);
        }
        return stack.getType().hasDefaultData(component);
    }

    /**
     * Edits a valued component by mapping its current patch value.
     * <p>
     * A {@code null} result unsets the component (marks it as removed).
     */
    public static <T> void editOrUnset(
        final @NotNull ItemStack stack,
        final DataComponentType.@NotNull Valued<T> component,
        final @NotNull Function<@Nullable T, @Nullable T> mapper
    ) {
        final T current = stack.getData(component);
        final T mapped = mapper.apply(current);
        if (mapped == null) {
            stack.unsetData(component);
            return;
        }
        stack.setData(component, mapped);
    }

    /**
     * Edits a valued component by mapping its current patch value.
     * <p>
     * A {@code null} result resets the component to its prototype default for the item type.
     */
    public static <T> void editOrReset(
        final @NotNull ItemStack stack,
        final DataComponentType.@NotNull Valued<T> component,
        final @NotNull Function<@Nullable T, @Nullable T> mapper
    ) {
        final T current = stack.getData(component);
        final T mapped = mapper.apply(current);
        if (mapped == null) {
            stack.resetData(component);
            return;
        }
        stack.setData(component, mapped);
    }

    /**
     * Sets a valued component only when the new value differs from the current patch value.
     * <p>
     * This avoids unnecessary patch churn for code that may run frequently (e.g. menu renders).
     */
    public static <T> void setIfChanged(
        final @NotNull ItemStack stack,
        final DataComponentType.@NotNull Valued<T> component,
        final @NotNull T value
    ) {
        final T current = stack.getData(component);
        if (Objects.equals(current, value)) {
            return;
        }
        stack.setData(component, value);
    }
}



