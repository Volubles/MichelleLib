# Data components

Paper's data component API is a version-specific, strongly-typed interface for reading and mutating item data that is not cleanly representable via `ItemMeta`.

This repository provides convenience utilities under `io.voluble.michellelib.item.datacomponent` to make common patterns less error-prone.

## Concepts

- **Prototype**: default components defined by the item type (`Material` / `ItemType`).
- **Patch**: modifications on an `ItemStack`.
- **Removed component**: a patch can explicitly remove a prototype component; in that case `ItemStack#getData(...)` returns `null` even when the prototype has a default.

## Quick patterns

### Prototype defaults

- Use `Material#getDefaultData(...)` / `Material#hasDefaultData(...)` for prototype lookup.
- Use `ItemDataComponents.prototype(...)` / `ItemDataComponents.prototypeHas(...)` to keep call sites consistent.

### Effective values (prototype vs patch)

If the patch overrides a component, the patch value is the effective value (including removals).
If the patch does not override a component, the prototype default is the effective value.

Use `ItemDataComponents.effective(...)` for this semantic.

### Editing valued components

Use:
- `ItemDataComponents.editOrUnset(...)` when a `null` result should mark the component as removed.
- `ItemDataComponents.editOrReset(...)` when a `null` result should restore the prototype default.

### Inspecting components

Use `ItemDataComponentInspector.describe(stack)` for a readable prototype/patch/override summary suitable for logs.

### High-frequency helpers

- `LoreComponents`: set/add/reset/unset for `DataComponentTypes.LORE`.
- `EnchantmentsComponents`: add enchantments for `DataComponentTypes.ENCHANTMENTS` and `DataComponentTypes.STORED_ENCHANTMENTS`.

### Item builder + serialization

- `ItemBuilder`: fluent item construction using data components and `TextEngine` MiniMessage parsing.
- `NbtItemStackCodec`: migration-safe NBT encoding (`ItemStack#serializeAsBytes()`).
- `NbtItemStacks`: helpers for encoding/decoding many items (arrays/collections/inventories) using Paper's item-array NBT format.
- `ItemTemplate` / `ItemTemplates`: reusable per-viewer item rendering (useful for menus).
- `MenuItems.template(...)`: adapter from `ItemTemplate` to `MenuItem`.
- `NbtItemSpec`: immutable, NBT-bytes-only “store and re-materialize later” wrapper for single items.

### Tooltip display (hide flags)

Paper replaces legacy `ItemFlag` hiding with `DataComponentTypes.TOOLTIP_DISPLAY`.

- `ItemBuilder.hideTooltip(true)`: hides the entire tooltip.
- `ItemBuilder.hideAttributesInTooltip()`: hides attribute lines.
- `ItemBuilder.hideEnchantmentsInTooltip()`: hides enchantment lines.
- `ItemBuilder.hideComponentsInTooltip(...)`: hides arbitrary component sections.

## Example

```java
import io.voluble.michellelib.item.builder.ItemBuilder;
import io.voluble.michellelib.text.TextEngine;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

TextEngine text = TextEngine.builder().build();

var item = ItemBuilder.of(Material.DIAMOND_SWORD)
    .itemNameMini(text, "<gold>Relic Blade</gold>")
    .loreMini(text, java.util.List.of(
        "<gray>Prototype/patch friendly</gray>",
        "<dark_gray>Stored as NBT bytes</dark_gray>"
    ))
    .enchant(Enchantment.SHARPNESS, 5)
    .enchantmentGlintOverride(true)
    .build();

byte[] stored = item.serializeAsBytes();
var restored = org.bukkit.inventory.ItemStack.deserializeBytes(stored);
```

## Character inventories (store whole inventories)

Paper provides a dedicated NBT array format for items, exposed by `ItemStack.serializeItemsAsBytes(...)`.
Use `NbtItemStacks` to store and restore player inventories reliably.

```java
import io.voluble.michellelib.item.codec.NbtItemStacks;
import org.bukkit.entity.Player;

byte[] invBytes = NbtItemStacks.encodeMany(player.getInventory().getContents());

// later...
player.getInventory().setContents(NbtItemStacks.decodeMany(invBytes));
```

## Menus (per-viewer rendering)

Use `ItemTemplate` when the same "conceptual item" is rendered frequently (menus) but needs to vary per viewer.

```java
import io.voluble.michellelib.item.template.ItemTemplates;
import io.voluble.michellelib.menu.item.MenuItems;
import org.bukkit.Material;

var template = ItemTemplates.dynamic(Material.PAPER, (viewer, b) -> {
    b.itemNameMini(textEngine, viewer, "<yellow>Hello, %player_name%</yellow>");
});

menuBuilder.item(13, MenuItems.template(template));
```

## Attributes editor

`ItemBuilder.attributes(...)` edits attribute modifiers using data components.

```java
import io.voluble.michellelib.item.builder.ItemBuilder;
import org.bukkit.attribute.Attribute;

ItemStack item = ItemBuilder.of(Material.DIAMOND_SWORD)
    .attributes(editor -> {
        editor.removeAttribute(Attribute.ATTACK_DAMAGE);
    })
    .hideAttributesInTooltip()
    .build();
```

### Convenience methods

For common attribute operations, use the one-liner convenience methods:

```java
import io.voluble.michellelib.item.builder.ItemBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

// Remove specific attributes (includes prototype defaults)
ItemBuilder.of(Material.DIAMOND_SWORD)
    .attributesRemove(Attribute.ATTACK_DAMAGE, Attribute.ATTACK_SPEED)
    .build();

// Clear all attributes (including prototype defaults)
ItemBuilder.of(Material.DIAMOND_SWORD)
    .attributesClear()
    .build();

// Add an attribute modifier
ItemBuilder.of(Material.DIAMOND_SWORD)
    .attributesAdd(
        Attribute.ATTACK_DAMAGE,
        new AttributeModifier("custom_damage", 5.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
    )
    .build();
```

## Persistent Data Containers (PDC)

`ItemBuilder` supports setting custom plugin data using Paper's Persistent Data Container API.

### Basic usage

```java
import io.voluble.michellelib.item.builder.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;

NamespacedKey customId = new NamespacedKey(plugin, "custom_id");

ItemStack item = ItemBuilder.of(Material.DIAMOND_SWORD)
    .pdcString(customId, "sword_123")
    .pdcInt(new NamespacedKey(plugin, "level"), 5)
    .pdcBoolean(new NamespacedKey(plugin, "enchanted"), true)
    .build();
```

### Convenience methods

Common types have dedicated methods:

- `pdcString(NamespacedKey, String)` - string values
- `pdcInt(NamespacedKey, int)` - integer values
- `pdcLong(NamespacedKey, long)` - long values
- `pdcDouble(NamespacedKey, double)` - double values
- `pdcBoolean(NamespacedKey, boolean)` - boolean values
- `pdcByteArray(NamespacedKey, byte[])` - byte arrays
- `pdcIntArray(NamespacedKey, int[])` - integer arrays
- `pdcLongArray(NamespacedKey, long[])` - long arrays
- `pdcRemove(NamespacedKey)` - remove a key

### Custom types

For custom `PersistentDataType` implementations:

```java
import org.bukkit.persistence.PersistentDataType;

ItemBuilder.of(Material.DIAMOND_SWORD)
    .pdc(customKey, MyCustomType.INSTANCE, customValue)
    .build();
```

### Nested containers

Edit nested PDC containers:

```java
ItemBuilder.of(Material.DIAMOND_SWORD)
    .pdcContainer(new NamespacedKey(plugin, "metadata"), nested -> {
        nested.set(new NamespacedKey(plugin, "owner"), PersistentDataType.STRING, "PlayerName");
        nested.set(new NamespacedKey(plugin, "timestamp"), PersistentDataType.LONG, System.currentTimeMillis());
    })
    .build();
```

### Complex edits

For complex edits, use the `pdc(Consumer<PersistentDataContainer>)` method:

```java
ItemBuilder.of(Material.DIAMOND_SWORD)
    .pdc(container -> {
        container.set(key1, PersistentDataType.STRING, "value1");
        container.set(key2, PersistentDataType.INTEGER, 42);
        if (someCondition) {
            container.remove(key3);
        }
    })
    .build();
```

## NBT bytes spec (store one item definition)

`NbtItemSpec` stores a single item as Paper-native NBT bytes.

```java
import io.voluble.michellelib.item.spec.NbtItemSpec;

NbtItemSpec spec = NbtItemSpec.of(item);
byte[] bytes = spec.bytes();

// later...
ItemStack restoredItem = NbtItemSpec.fromBytes(bytes).materialize();
```


*This file was created with the help of AI to improve documentation clarity.*