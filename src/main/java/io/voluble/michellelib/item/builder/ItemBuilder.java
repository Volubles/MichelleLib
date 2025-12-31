package io.voluble.michellelib.item.builder;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.TypedKey;
import io.voluble.michellelib.item.attribute.AttributeModifiersEditor;
import io.voluble.michellelib.item.datacomponent.ItemDataComponents;
import io.voluble.michellelib.registry.Registries;
import io.voluble.michellelib.text.TextEngine;
import io.voluble.michellelib.text.item.ItemText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link ItemStack}s using Paper data components.
 *
 * <p>This builder is mutable and intended for construction-time use.</p>
 */
public final class ItemBuilder {

    private ItemStack stack;
    private boolean noItalicByDefault = true;

    private ItemBuilder(final @NotNull ItemStack stack) {
        this.stack = Objects.requireNonNull(stack, "stack");
    }

    public static @NotNull ItemBuilder of(final @NotNull Material type) {
        return new ItemBuilder(ItemStack.of(type));
    }

    public static @NotNull ItemBuilder from(final @NotNull ItemStack base) {
        return new ItemBuilder(base.clone());
    }

    /**
     * Controls whether {@link io.voluble.michellelib.text.item.ItemText#noItalicIfAbsent(Component)}
     * is applied to item text set through this builder.
     */
    public @NotNull ItemBuilder noItalicByDefault(final boolean enabled) {
        this.noItalicByDefault = enabled;
        return this;
    }

    public @NotNull ItemBuilder amount(final int amount) {
        this.stack.setAmount(amount);
        return this;
    }

    public @NotNull ItemBuilder type(final @NotNull Material type) {
        this.stack = this.stack.withType(type);
        return this;
    }

    /**
     * Sets the item type using a registry key.
     *
     * @param itemKey the typed key for the item type
     * @return this builder
     * @throws io.voluble.michellelib.registry.RegistryException if the item type is not found in the registry
     */
    public @NotNull ItemBuilder itemType(final @NotNull TypedKey<ItemType> itemKey) {
        final ItemType itemType = Registries.getOrThrow(itemKey);
        final int amount = this.stack.getAmount();
        this.stack = itemType.createItemStack(amount);
        return this;
    }

    /**
     * Sets the item type using a registry key, with a fallback Material if not found.
     *
     * @param itemKey the typed key for the item type
     * @param fallback the material to use if the item type is not found
     * @return this builder
     */
    public @NotNull ItemBuilder itemType(
            final @NotNull TypedKey<ItemType> itemKey,
            final @NotNull Material fallback
    ) {
        final int amount = this.stack.getAmount();
        final ItemType itemType = Registries.getOptional(itemKey)
                .orElseGet(() -> {
                    final ItemType fallbackItemType = fallback.asItemType();
                    if (fallbackItemType == null) {
                        throw new IllegalArgumentException(fallback + " is not an item type");
                    }
                    return fallbackItemType;
                });
        this.stack = itemType.createItemStack(amount);
        return this;
    }

    public @NotNull ItemBuilder customName(final @NotNull Component name) {
        this.stack.setData(DataComponentTypes.CUSTOM_NAME, name);
        return this;
    }

    public @NotNull ItemBuilder customNameMini(final @NotNull TextEngine engine, final @Nullable String miniMessage) {
        return customName(engine.parse(miniMessage));
    }

    public @NotNull ItemBuilder customNameMini(final @NotNull TextEngine engine, final @Nullable Player player, final @Nullable String miniMessage) {
        return customName(engine.parse(player, miniMessage));
    }

    public @NotNull ItemBuilder itemName(final @NotNull Component name) {
        this.stack.setData(DataComponentTypes.ITEM_NAME, name);
        return this;
    }

    public @NotNull ItemBuilder itemNameMini(final @NotNull TextEngine engine, final @Nullable String miniMessage) {
        final Component parsed = engine.parse(miniMessage);
        return itemName(this.noItalicByDefault ? ItemText.noItalicIfAbsent(parsed) : parsed);
    }

    public @NotNull ItemBuilder itemNameMini(final @NotNull TextEngine engine, final @Nullable Player player, final @Nullable String miniMessage) {
        final Component parsed = engine.parse(player, miniMessage);
        return itemName(this.noItalicByDefault ? ItemText.noItalicIfAbsent(parsed) : parsed);
    }

    public @NotNull ItemBuilder rarity(final @NotNull ItemRarity rarity) {
        this.stack.setData(DataComponentTypes.RARITY, rarity);
        return this;
    }

    public @NotNull ItemBuilder enchantmentGlintOverride(final boolean enabled) {
        this.stack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, enabled);
        return this;
    }

    public @NotNull ItemBuilder unbreakable(final boolean enabled) {
        if (enabled) {
            this.stack.setData(DataComponentTypes.UNBREAKABLE);
        } else {
            this.stack.unsetData(DataComponentTypes.UNBREAKABLE);
        }
        return this;
    }

    /**
     * Updates the tooltip display component. If the component is not present, a new one is created.
     */
    public @NotNull ItemBuilder tooltip(final @NotNull Consumer<TooltipDisplay.Builder> edit) {
        Objects.requireNonNull(edit, "edit");

        final TooltipDisplay current = this.stack.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        final TooltipDisplay.Builder builder = TooltipDisplay.tooltipDisplay();
        if (current != null) {
            builder.hideTooltip(current.hideTooltip());
            builder.hiddenComponents(current.hiddenComponents());
        }

        edit.accept(builder);
        this.stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, builder.build());
        return this;
    }

    public @NotNull ItemBuilder hideTooltip(final boolean hide) {
        return tooltip(b -> b.hideTooltip(hide));
    }

    public @NotNull ItemBuilder hideComponentsInTooltip(final @NotNull DataComponentType... components) {
        return tooltip(b -> b.addHiddenComponents(components));
    }

    public @NotNull ItemBuilder hideAttributesInTooltip() {
        return hideComponentsInTooltip(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    }

    public @NotNull ItemBuilder hideEnchantmentsInTooltip() {
        return hideComponentsInTooltip(DataComponentTypes.ENCHANTMENTS, DataComponentTypes.STORED_ENCHANTMENTS);
    }

    public @NotNull ItemBuilder hideUnbreakableInTooltip() {
        return hideComponentsInTooltip(DataComponentTypes.UNBREAKABLE);
    }

    public @NotNull ItemBuilder resetTooltipDisplay() {
        this.stack.resetData(DataComponentTypes.TOOLTIP_DISPLAY);
        return this;
    }

    public @NotNull ItemBuilder lore(final @NotNull List<? extends ComponentLike> lines) {
        final List<ComponentLike> normalized = new ArrayList<>(lines.size());
        for (final ComponentLike line : lines) {
            final Component component = line.asComponent();
            normalized.add(this.noItalicByDefault ? ItemText.noItalicIfAbsent(component) : component);
        }
        this.stack.setData(DataComponentTypes.LORE, ItemLore.lore(normalized));
        return this;
    }

    public @NotNull ItemBuilder lore(final @NotNull ComponentLike... lines) {
        return lore(List.of(lines));
    }

    public @NotNull ItemBuilder loreMini(final @NotNull TextEngine engine, final @NotNull List<String> miniMessageLines) {
        final List<Component> lines = new ArrayList<>(miniMessageLines.size());
        for (final String s : miniMessageLines) {
            final Component parsed = engine.parse(s);
            lines.add(this.noItalicByDefault ? ItemText.noItalicIfAbsent(parsed) : parsed);
        }
        return lore(lines);
    }

    public @NotNull ItemBuilder loreMini(final @NotNull TextEngine engine, final @Nullable Player player, final @NotNull List<String> miniMessageLines) {
        final List<Component> lines = new ArrayList<>(miniMessageLines.size());
        for (final String s : miniMessageLines) {
            final Component parsed = engine.parse(player, s);
            lines.add(this.noItalicByDefault ? ItemText.noItalicIfAbsent(parsed) : parsed);
        }
        return lore(lines);
    }

    public @NotNull ItemBuilder loreMini(final @NotNull TextEngine engine, final @NotNull String... miniMessageLines) {
        return loreMini(engine, List.of(miniMessageLines));
    }

    public @NotNull ItemBuilder loreMini(final @NotNull TextEngine engine, final @Nullable Player player, final @NotNull String... miniMessageLines) {
        return loreMini(engine, player, List.of(miniMessageLines));
    }

    public @NotNull ItemBuilder addLoreLine(final @NotNull ComponentLike line) {
        final ItemLore current = this.stack.getData(DataComponentTypes.LORE);
        final ItemLore.Builder builder = ItemLore.lore();
        if (current != null) {
            builder.addLines(current.lines());
        }
        final Component component = line.asComponent();
        builder.addLine(this.noItalicByDefault ? ItemText.noItalicIfAbsent(component) : component);
        this.stack.setData(DataComponentTypes.LORE, builder.build());
        return this;
    }

    public @NotNull ItemBuilder addLoreLineMini(final @NotNull TextEngine engine, final @Nullable String miniMessageLine) {
        return addLoreLine(engine.parse(miniMessageLine));
    }

    public @NotNull ItemBuilder addLoreLineMini(final @NotNull TextEngine engine, final @Nullable Player player, final @Nullable String miniMessageLine) {
        return addLoreLine(engine.parse(player, miniMessageLine));
    }

    public @NotNull ItemBuilder resetLore() {
        this.stack.resetData(DataComponentTypes.LORE);
        return this;
    }

    public @NotNull ItemBuilder unsetLore() {
        this.stack.unsetData(DataComponentTypes.LORE);
        return this;
    }

    /**
     * Removes lore entirely (marks {@link io.papermc.paper.datacomponent.DataComponentTypes#LORE} as removed).
     */
    public @NotNull ItemBuilder clearLore() {
        return unsetLore();
    }

    public @NotNull ItemBuilder enchant(final @NotNull Enchantment enchantment, final int level) {
        final ItemEnchantments current = this.stack.getData(DataComponentTypes.ENCHANTMENTS);
        final Map<Enchantment, Integer> existing = current == null ? Map.of() : current.enchantments();
        final Map<Enchantment, Integer> copy = new HashMap<>(existing);
        copy.put(enchantment, level);
        this.stack.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(copy));
        return this;
    }

    /**
     * Adds an enchantment using a registry key.
     *
     * @param enchantKey the typed key for the enchantment
     * @param level the enchantment level
     * @return this builder
     * @throws io.voluble.michellelib.registry.RegistryException if the enchantment is not found in the registry
     */
    public @NotNull ItemBuilder enchant(final @NotNull TypedKey<Enchantment> enchantKey, final int level) {
        return enchant(Registries.getOrThrow(enchantKey), level);
    }

    public @NotNull ItemBuilder storedEnchant(final @NotNull Enchantment enchantment, final int level) {
        final ItemEnchantments current = this.stack.getData(DataComponentTypes.STORED_ENCHANTMENTS);
        final Map<Enchantment, Integer> existing = current == null ? Map.of() : current.enchantments();
        final Map<Enchantment, Integer> copy = new HashMap<>(existing);
        copy.put(enchantment, level);
        this.stack.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(copy));
        return this;
    }

    /**
     * Adds a stored enchantment using a registry key.
     *
     * @param enchantKey the typed key for the enchantment
     * @param level the enchantment level
     * @return this builder
     * @throws io.voluble.michellelib.registry.RegistryException if the enchantment is not found in the registry
     */
    public @NotNull ItemBuilder storedEnchant(final @NotNull TypedKey<Enchantment> enchantKey, final int level) {
        return storedEnchant(Registries.getOrThrow(enchantKey), level);
    }

    public @NotNull ItemBuilder customModelData(final @NotNull Consumer<CustomModelData.Builder> edit) {
        final CustomModelData.Builder builder = CustomModelData.customModelData();
        edit.accept(builder);
        this.stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build());
        return this;
    }

    public <T> @NotNull ItemBuilder set(final DataComponentType.@NotNull Valued<T> type, final @NotNull T value) {
        this.stack.setData(type, value);
        return this;
    }

    public @NotNull ItemBuilder set(final DataComponentType.@NotNull NonValued type) {
        this.stack.setData(type);
        return this;
    }

    public @NotNull ItemBuilder reset(final @NotNull DataComponentType type) {
        this.stack.resetData(type);
        return this;
    }

    public @NotNull ItemBuilder unset(final @NotNull DataComponentType type) {
        this.stack.unsetData(type);
        return this;
    }

    /**
     * Removes the tool component from the prototype (no longer behaves as a tool).
     */
    public @NotNull ItemBuilder removeToolComponent() {
        this.stack.unsetData(DataComponentTypes.TOOL);
        return this;
    }

    /**
     * Resets the tool component back to the prototype default for this item type.
     */
    public @NotNull ItemBuilder resetToolComponent() {
        this.stack.resetData(DataComponentTypes.TOOL);
        return this;
    }

    /**
     * Removes all default and custom attribute modifiers from this item.
     */
    public @NotNull ItemBuilder removeAttributeModifiers() {
        this.stack.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return this;
    }

    /**
     * Resets attribute modifiers back to the prototype defaults for this item type.
     */
    public @NotNull ItemBuilder resetAttributeModifiers() {
        this.stack.resetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return this;
    }

    /**
     * Edits attribute modifiers for this item.
     *
     * <p>When {@code includeDefaults} is true, the editor starts from the effective attribute list
     * (prototype default unless explicitly removed). When false, the editor starts from the current patch only.</p>
     *
     * <p>The resulting attribute list is written as an explicit patch component.</p>
     */
    public @NotNull ItemBuilder attributes(final boolean includeDefaults, final @NotNull Consumer<AttributeModifiersEditor> edit) {
        Objects.requireNonNull(edit, "edit");

        final ItemAttributeModifiers base = includeDefaults
            ? ItemDataComponents.effective(this.stack, DataComponentTypes.ATTRIBUTE_MODIFIERS)
            : this.stack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        final AttributeModifiersEditor editor = base == null ? new AttributeModifiersEditor() : AttributeModifiersEditor.from(base);
        edit.accept(editor);

        final ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.itemAttributes();
        for (final AttributeModifiersEditor.Entry e : editor.entries()) {
            builder.addModifier(e.attribute(), e.modifier(), e.group(), e.display());
        }

        this.stack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        return this;
    }

    public @NotNull ItemBuilder attributes(final @NotNull Consumer<AttributeModifiersEditor> edit) {
        return attributes(true, edit);
    }

    /**
     * Removes specific attributes from this item.
     *
     * <p>This includes both patch and prototype attributes (if {@code includeDefaults} is true).</p>
     */
    public @NotNull ItemBuilder attributesRemove(final @NotNull Attribute... attributes) {
        Objects.requireNonNull(attributes, "attributes");
        return attributes(editor -> {
            for (final Attribute attr : attributes) {
                editor.removeAttribute(attr);
            }
        });
    }

    /**
     * Removes specific attributes from this item, starting from patch-only attributes.
     */
    public @NotNull ItemBuilder attributesRemovePatchOnly(final @NotNull Attribute... attributes) {
        Objects.requireNonNull(attributes, "attributes");
        return attributes(false, editor -> {
            for (final Attribute attr : attributes) {
                editor.removeAttribute(attr);
            }
        });
    }

    /**
     * Clears all attribute modifiers from this item (including prototype defaults).
     */
    public @NotNull ItemBuilder attributesClear() {
        return attributes(editor -> editor.clear());
    }

    /**
     * Clears only patch attribute modifiers, restoring prototype defaults.
     */
    public @NotNull ItemBuilder attributesClearPatchOnly() {
        return attributes(false, editor -> editor.clear());
    }

    /**
     * Adds an attribute modifier to this item.
     *
     * <p>If an attribute already exists, it is replaced.</p>
     */
    public @NotNull ItemBuilder attributesAdd(
        final @NotNull Attribute attribute,
        final @NotNull AttributeModifier modifier
    ) {
        Objects.requireNonNull(attribute, "attribute");
        Objects.requireNonNull(modifier, "modifier");
        return attributes(editor -> {
            editor.removeAttribute(attribute);
            editor.add(attribute, modifier);
        });
    }

    /**
     * Adds an attribute modifier to this item, starting from patch-only attributes.
     *
     * <p>If an attribute already exists in the patch, it is replaced.</p>
     */
    public @NotNull ItemBuilder attributesAddPatchOnly(
        final @NotNull Attribute attribute,
        final @NotNull AttributeModifier modifier
    ) {
        Objects.requireNonNull(attribute, "attribute");
        Objects.requireNonNull(modifier, "modifier");
        return attributes(false, editor -> {
            editor.removeAttribute(attribute);
            editor.add(attribute, modifier);
        });
    }

    /**
     * Edits the Persistent Data Container (PDC) of this item.
     *
     * <p>The PDC instance is only valid inside the consumer. Use this for complex edits or custom types.</p>
     */
    public @NotNull ItemBuilder pdc(final @NotNull Consumer<PersistentDataContainer> edit) {
        Objects.requireNonNull(edit, "edit");
        this.stack.editPersistentDataContainer(edit);
        return this;
    }

    /**
     * Sets a string value in the PDC.
     */
    public @NotNull ItemBuilder pdcString(final @NotNull NamespacedKey key, final @NotNull String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return pdc(container -> container.set(key, PersistentDataType.STRING, value));
    }

    /**
     * Sets an integer value in the PDC.
     */
    public @NotNull ItemBuilder pdcInt(final @NotNull NamespacedKey key, final int value) {
        Objects.requireNonNull(key, "key");
        return pdc(container -> container.set(key, PersistentDataType.INTEGER, value));
    }

    /**
     * Sets a long value in the PDC.
     */
    public @NotNull ItemBuilder pdcLong(final @NotNull NamespacedKey key, final long value) {
        Objects.requireNonNull(key, "key");
        return pdc(container -> container.set(key, PersistentDataType.LONG, value));
    }

    /**
     * Sets a double value in the PDC.
     */
    public @NotNull ItemBuilder pdcDouble(final @NotNull NamespacedKey key, final double value) {
        Objects.requireNonNull(key, "key");
        return pdc(container -> container.set(key, PersistentDataType.DOUBLE, value));
    }

    /**
     * Sets a boolean value in the PDC.
     */
    public @NotNull ItemBuilder pdcBoolean(final @NotNull NamespacedKey key, final boolean value) {
        Objects.requireNonNull(key, "key");
        return pdc(container -> container.set(key, PersistentDataType.BOOLEAN, value));
    }

    /**
     * Sets a byte array value in the PDC.
     */
    public @NotNull ItemBuilder pdcByteArray(final @NotNull NamespacedKey key, final byte @NotNull [] value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return pdc(container -> container.set(key, PersistentDataType.BYTE_ARRAY, value));
    }

    /**
     * Sets an integer array value in the PDC.
     */
    public @NotNull ItemBuilder pdcIntArray(final @NotNull NamespacedKey key, final int @NotNull [] value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return pdc(container -> container.set(key, PersistentDataType.INTEGER_ARRAY, value));
    }

    /**
     * Sets a long array value in the PDC.
     */
    public @NotNull ItemBuilder pdcLongArray(final @NotNull NamespacedKey key, final long @NotNull [] value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return pdc(container -> container.set(key, PersistentDataType.LONG_ARRAY, value));
    }

    /**
     * Sets a custom type value in the PDC.
     */
    public <P, C> @NotNull ItemBuilder pdc(
        final @NotNull NamespacedKey key,
        final @NotNull PersistentDataType<P, C> type,
        final @NotNull C value
    ) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        return pdc(container -> container.set(key, type, value));
    }

    /**
     * Edits a nested Persistent Data Container in the PDC.
     *
     * <p>If the nested container does not exist, it is created.</p>
     */
    public @NotNull ItemBuilder pdcContainer(
        final @NotNull NamespacedKey key,
        final @NotNull Consumer<PersistentDataContainer> edit
    ) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(edit, "edit");
        return pdc(container -> {
            PersistentDataContainer nested = container.get(key, PersistentDataType.TAG_CONTAINER);
            if (nested == null) {
                nested = container.getAdapterContext().newPersistentDataContainer();
            }
            edit.accept(nested);
            container.set(key, PersistentDataType.TAG_CONTAINER, nested);
        });
    }

    /**
     * Removes a key from the PDC.
     */
    public @NotNull ItemBuilder pdcRemove(final @NotNull NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        return pdc(container -> container.remove(key));
    }

    public @NotNull ItemStack build() {
        return this.stack.clone();
    }

    public byte @NotNull [] serializeToBytes() {
        return build().ensureServerConversions().serializeAsBytes();
    }
}


