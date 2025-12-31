# Registry Access API

Advanced registry utilities with streaming, caching, tag support, and fluent query APIs for accessing Minecraft's game registries.

## Overview

The registry API provides comprehensive access to Minecraft's registries with multiple access patterns:

- **Registries**: Main utility class with safe lookups, streaming, and tag support
- **RegistryHelper**: Fluent query builders, caching, and batch operations
- **TypedKeyCodec**: Serialization utilities for TypedKey
- **RegistryValidation**: Validation utilities for user input

## Basic Access

### Safe Lookups

```java
// Optional-based lookups (recommended)
Optional<ItemType> diamond = Registries.item("diamond");
Optional<BlockType> stone = Registries.block("stone");
Optional<EntityType> zombie = Registries.entityType("zombie");

// Or with Key objects
Optional<ItemType> sword = Registries.item(Key.key("minecraft:diamond_sword"));

// Get or throw with custom exceptions
ItemType item = Registries.getOrThrow(RegistryKey.ITEM, Key.key("diamond"));
```

### Streaming Access

```java
// Stream all items
Registries.items().forEach(item -> {
    // Process each item
});

// Stream with filtering
Registries.items()
    .filter(ItemType::isEdible)
    .forEach(food -> sendMessage("Food: " + food.key()));

// Count items
long weaponCount = Registries.items()
    .filter(item -> item.key().value().contains("sword"))
    .count();
```

## Fluent Queries

### Basic Queries

```java
// Find items by namespace
List<ItemType> minecraftItems = RegistryHelper.query(RegistryKey.ITEM)
    .namespace("minecraft")
    .toList();

// Find blocks that are solid
List<BlockType> solidBlocks = RegistryHelper.query(RegistryKey.BLOCK)
    .where(BlockType::isSolid)
    .toList();

// Limit results
List<EntityType> hostileMobs = RegistryHelper.query(RegistryKey.ENTITY_TYPE)
    .where(entity -> entity.getEntityClass().isAssignableFrom(Monster.class))
    .limit(10)
    .toList();
```

### Advanced Queries

```java
// Complex multi-criteria queries
List<ItemType> specialItems = RegistryHelper.query(RegistryKey.ITEM)
    .namespace("minecraft")
    .where(item -> item.key().value().contains("diamond"))
    .sortedByKey()
    .limit(5)
    .toList();

// Find first matching item
Optional<ItemType> firstSword = RegistryHelper.query(RegistryKey.ITEM)
    .where(item -> item.key().value().contains("sword"))
    .sortedByKey()
    .first();
```

## Tag Support

### Tag Lookups

```java
// Get items in a tag
Optional<Tag<ItemType>> pickaxes = Registries.tag(ItemTagKeys.PICKAXES);
pickaxes.ifPresent(tag -> {
    tag.getValues().forEach(pickaxe -> {
        // Process each pickaxe
    });
});

// Check if item is in tag
boolean isPickaxe = Registries.isItemTagged(ItemTagKeys.PICKAXES, diamondPickaxe);

// Stream tag contents
Registries.streamTag(ItemTagKeys.PICKAXES)
    .forEach(pickaxe -> sendMessage("Pickaxe: " + pickaxe.key()));
```

### Common Tags

```java
// Block tags
Registries.streamTag(BlockTagKeys.LOGS).forEach(log -> {});
Registries.streamTag(BlockTagKeys.LEAVES).forEach(leaf -> {});

// Entity tags
Registries.streamTag(EntityTypeTagKeys.ARROWS).forEach(arrow -> {});
Registries.streamTag(EntityTypeTagKeys.BOATS).forEach(boat -> {});

// Item tags
Registries.streamTag(ItemTagKeys.SWORDS).forEach(sword -> {});
Registries.streamTag(ItemTagKeys.BANNERS).forEach(banner -> {});
```

## Caching

### Cached Registry Access

```java
// Create a cached registry for frequently accessed items
CachedRegistry<ItemType> itemCache = RegistryHelper.cached(RegistryKey.ITEM);

// Fast lookups (populates cache on first access)
Optional<ItemType> diamond = itemCache.get("diamond");
Optional<ItemType> sword = itemCache.get(Key.key("minecraft:diamond_sword"));

// Invalidate cache when needed
itemCache.invalidate();
```

## Batch Operations

### Batch Lookups

```java
// Prepare batch lookup
BatchRegistry<ItemType> batch = RegistryHelper.batch(RegistryKey.ITEM)
    .lookup("diamond")
    .lookup("iron_ingot")
    .lookup("gold_ingot")
    .lookup(Key.key("minecraft:emerald"));

// Execute all lookups at once
Map<Key, Optional<ItemType>> results = batch.resolve();

// Get only successfully found items
Map<Key, ItemType> foundItems = batch.resolvePresent();
```

## Safe Registry Lookups

The `Registries` class provides safe methods for accessing registry entries:

### Basic Lookups

```java
import io.voluble.michellelib.registry.Registries;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;

// Optional-based lookup (safe, no exceptions)
Optional<ItemType> diamond = Registries.item(Key.key("minecraft:diamond_sword"));
if (diamond.isPresent()) {
    ItemStack stack = diamond.get().createItemStack(1);
}

// With default value
Enchantment sharpness = Registries.enchantment(Key.key("minecraft:sharpness"))
    .orElse(/* some default enchantment */);

// Throws RegistryException if not found (with descriptive message)
ItemType item = Registries.getOrThrow(RegistryKey.ITEM, Key.key("minecraft:diamond"));
```

### Convenience Methods

```java
// Common registries have convenience methods
Optional<ItemType> item = Registries.item(Key.key("minecraft:diamond_sword"));
Optional<BlockType> block = Registries.block(Key.key("minecraft:stone"));
Optional<Enchantment> enchant = Registries.enchantment(Key.key("minecraft:sharpness"));
Optional<PotionEffectType> effect = Registries.potionEffect(Key.key("minecraft:speed"));
Optional<Sound> sound = Registries.sound(Key.key("minecraft:block.note_block.pling"));
Optional<EntityType> entity = Registries.entityType(Key.key("minecraft:creeper"));
```

### Using TypedKeys

```java
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;

// Using generated typed keys
TypedKey<Enchantment> sharpnessKey = EnchantmentKeys.SHARPNESS;
Enchantment sharpness = Registries.getOrThrow(sharpnessKey);

// Using custom typed keys
TypedKey<ItemType> customItem = TypedKey.create(
    RegistryKey.ITEM,
    Key.key("myplugin:custom_item")
);
Optional<ItemType> item = Registries.getOptional(customItem);
```

## Registry Key Serialization

Store registry references in PersistentDataContainer or configuration files using `TypedKeyCodec`:

### PersistentDataContainer Storage

```java
import io.voluble.michellelib.registry.codec.TypedKeyCodec;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

// Store a registry reference in item NBT
PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
TypedKey<Enchantment> enchantKey = EnchantmentKeys.SHARPNESS;

pdc.set(
    new NamespacedKey(plugin, "stored_enchantment"),
    TypedKeyCodec.persistentDataType(),
    enchantKey
);

// Later, retrieve it
TypedKey<?> storedKey = pdc.get(
    new NamespacedKey(plugin, "stored_enchantment"),
    TypedKeyCodec.persistentDataType()
);
if (storedKey != null && storedKey.registryKey() == RegistryKey.ENCHANTMENT) {
    @SuppressWarnings("unchecked")
    TypedKey<Enchantment> enchantKey2 = (TypedKey<Enchantment>) storedKey;
    Enchantment enchant = Registries.getOrThrow(enchantKey2);
}
```

### String Serialization

```java
// Convert TypedKey to string for config storage
TypedKey<ItemType> itemKey = TypedKey.create(
    RegistryKey.ITEM,
    Key.key("minecraft:diamond_sword")
);
String serialized = TypedKeyCodec.toString(itemKey);
// Result: "minecraft:item|minecraft:diamond_sword"

// Parse back from string
Optional<TypedKey<?>> parsed = TypedKeyCodec.fromString(serialized);
```

### Configuration Files

```java
// Save to config
config.set("reward_item", TypedKeyCodec.toString(itemKey));

// Load from config
String saved = config.getString("reward_item");
TypedKey<?> loaded = TypedKeyCodec.fromString(saved)
    .orElseThrow(() -> new IllegalArgumentException("Invalid item key"));
```

## Registry Validation

Validate user input (from configs, commands, etc.) against registries using `RegistryValidation`:

### Single Validation

```java
import io.voluble.michellelib.registry.validation.RegistryValidation;

// Validate a single input
RegistryValidation.ValidationResult<ItemType> result = 
    RegistryValidation.validate(
        RegistryKey.ITEM,
        "minecraft:diamond_sword"
    );

if (result.isValid()) {
    ItemType item = result.value().orElseThrow();
    // Use the item...
} else {
    String error = result.error().orElse("Unknown error");
    logger.warn("Invalid item: " + error);
}
```

### Multiple Validation

```java
// Validate a list of items from config
List<String> itemNames = config.getStringList("allowed_items");
RegistryValidation.ValidationReport<ItemType> report = 
    RegistryValidation.validateReport(RegistryKey.ITEM, itemNames);

if (report.isValid()) {
    List<ItemType> validItems = report.validValues();
    // Use all valid items...
} else {
    // Report all errors at once
    logger.error("Invalid items: " + report.formatErrors());
    // Invalid inputs: invalid_item_1: No entry found..., invalid_item_2: Invalid key format...
}
```

### Config Loading with Validation

```java
// Load items from config with automatic validation
List<String> rawItems = config.getStringList("shop.items");
List<ItemType> validItems = RegistryValidation.validateAndCollect(
    RegistryKey.ITEM,
    rawItems
);

// Invalid items are automatically filtered out
// Log invalid items separately if needed
List<String> invalid = RegistryValidation.validateReport(RegistryKey.ITEM, rawItems)
    .invalidInputs();
if (!invalid.isEmpty()) {
    logger.warn("Invalid items in config: " + invalid);
}
```

### Flexible Key Parsing

The validator automatically handles both namespaced and unnamespaced keys:

```java
// Both work the same:
RegistryValidation.validate(RegistryKey.ITEM, "minecraft:diamond_sword");
RegistryValidation.validate(RegistryKey.ITEM, "diamond_sword"); // assumes minecraft: namespace
```

## ItemBuilder Integration

Use registry keys directly in `ItemBuilder`:

### Item Types

```java
import io.voluble.michellelib.item.builder.ItemBuilder;
import io.papermc.paper.registry.keys.ItemTypeKeys;

// Using generated typed keys
ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD)
    .itemType(ItemTypeKeys.DIAMOND_SWORD);

// Using custom typed keys
TypedKey<ItemType> customItem = TypedKey.create(
    RegistryKey.ITEM,
    Key.key("myplugin:custom_item")
);
ItemBuilder builder2 = ItemBuilder.of(Material.STONE)
    .itemType(customItem, Material.STONE); // fallback if not found
```

### Enchantments

```java
import io.papermc.paper.registry.keys.EnchantmentKeys;

ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD)
    .enchant(EnchantmentKeys.SHARPNESS, 5)
    .enchant(EnchantmentKeys.UNBREAKING, 3);

// Custom enchantments
TypedKey<Enchantment> customEnchant = TypedKey.create(
    RegistryKey.ENCHANTMENT,
    Key.key("myplugin:super_enchant")
);
builder.enchant(customEnchant, 10);
```

## Command Argument Shortcuts

Use convenience methods in `Args` for common registry arguments:

```java
import io.voluble.michellelib.commands.arguments.Args;

// Instead of:
ArgumentType<ItemType> itemArg = Args.resource(RegistryKey.ITEM);

// Use:
ArgumentType<ItemType> itemArg = Args.itemType();
ArgumentType<Enchantment> enchantArg = Args.enchantment();
ArgumentType<BlockType> blockArg = Args.blockType();
ArgumentType<PotionEffectType> effectArg = Args.potionEffect();
ArgumentType<Sound> soundArg = Args.sound();
ArgumentType<EntityType> entityArg = Args.entityType();
ArgumentType<Biome> biomeArg = Args.biome();
```

### Example Command

```java
CommandSpec command = CommandSpec.builder()
    .argument("item", Args.itemType())
    .argument("enchant", Args.enchantment())
    .argument("level", Args.integer(1, 10))
    .handler(context -> {
        ItemType item = context.get("item");
        Enchantment enchant = context.get("enchant");
        int level = context.get("level");
        
        ItemStack stack = item.createItemStack(1);
        stack.addEnchantment(enchant, level);
        context.getSender().sendMessage("Created enchanted item!");
    })
    .build();
```

## Best Practices

### 1. Use Optional-Based Lookups When Input is User-Controlled

```java
// Good - handles invalid input gracefully
Optional<ItemType> item = Registries.item(Key.key(userInput));
if (item.isPresent()) {
    // Use item...
} else {
    // Handle error
}

// Avoid - throws exception on invalid input
ItemType item = Registries.getOrThrow(RegistryKey.ITEM, Key.key(userInput)); // Can throw!
```

### 2. Validate Config Values at Load Time

```java
// Good - validate once when loading config
List<ItemType> items = RegistryValidation.validateAndCollect(
    RegistryKey.ITEM,
    config.getStringList("items")
);

// Avoid - validate on every use (inefficient)
for (String name : config.getStringList("items")) {
    Optional<ItemType> item = Registries.item(Key.key(name));
    // ...
}
```

### 3. Store TypedKeys for Persistence

```java
// Good - store registry keys, not strings
TypedKey<ItemType> itemKey = /* ... */;
pdc.set(key, TypedKeyCodec.persistentDataType(), itemKey);

// Avoid - storing raw strings loses type safety
pdc.set(key, PersistentDataType.STRING, "minecraft:diamond_sword");
```

### 4. Use Generated Keys When Available

```java
// Good - compile-time safety
TypedKey<Enchantment> sharpness = EnchantmentKeys.SHARPNESS;

// Okay - runtime lookup (for dynamic keys)
TypedKey<Enchantment> custom = TypedKey.create(
    RegistryKey.ENCHANTMENT,
    Key.key("myplugin:custom")
);
```

### 5. Batch Validate Multiple Entries

```java
// Good - validate all at once, report all errors
ValidationReport<ItemType> report = RegistryValidation.validateReport(
    RegistryKey.ITEM,
    itemList
);
if (!report.isValid()) {
    logger.error("Invalid items: " + report.formatErrors());
}

// Avoid - validate one at a time (less efficient, harder to report errors)
for (String name : itemList) {
    ValidationResult<ItemType> result = RegistryValidation.validate(...);
    // ...
}
```

## Common Use Cases

### Custom Item Registry

```java
// Store custom item references
public class ItemRegistry {
    private static final Map<String, TypedKey<ItemType>> CUSTOM_ITEMS = new HashMap<>();
    
    public static void register(String id, TypedKey<ItemType> itemKey) {
        CUSTOM_ITEMS.put(id, itemKey);
    }
    
    public static Optional<ItemStack> createItem(String id) {
        TypedKey<ItemType> key = CUSTOM_ITEMS.get(id);
        if (key == null) return Optional.empty();
        
        ItemType type = Registries.getOptional(key).orElse(null);
        if (type == null) return Optional.empty();
        
        return Optional.of(type.createItemStack(1));
    }
}
```

### Config-Based Item Loading

```java
public void loadShopItems(FileConfiguration config) {
    List<String> itemIds = config.getStringList("shop.items");
    ValidationReport<ItemType> report = RegistryValidation.validateReport(
        RegistryKey.ITEM,
        itemIds
    );
    
    if (!report.isValid()) {
        getLogger().warning("Invalid shop items: " + report.formatErrors());
    }
    
    List<ItemType> validItems = report.validValues();
    // Use validItems...
}
```

### Enchantment Storage

```java
// Store preferred enchantments in player data
public void savePlayerEnchantments(Player player, List<TypedKey<Enchantment>> enchants) {
    PersistentDataContainer pdc = player.getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(plugin, "preferred_enchants");
    
    // Store as list of strings
    List<String> serialized = enchants.stream()
        .map(TypedKeyCodec::toString)
        .collect(Collectors.toList());
    
    pdc.set(key, PersistentDataType.LIST.strings(), serialized);
}

public List<Enchantment> loadPlayerEnchantments(Player player) {
    PersistentDataContainer pdc = player.getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(plugin, "preferred_enchants");
    
    List<String> serialized = pdc.get(key, PersistentDataType.LIST.strings());
    if (serialized == null) return List.of();
    
    return serialized.stream()
        .map(TypedKeyCodec::fromString)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(k -> k.registryKey() == RegistryKey.ENCHANTMENT)
        .map(k -> Registries.getOptional((TypedKey<Enchantment>) k))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
}
```

## API Reference

- `Registries` - Safe registry lookups with optional/default handling
- `TypedKeyCodec` - Serialization utilities for TypedKey
- `RegistryValidation` - Validation utilities for user input
- `ItemBuilder` - Registry-aware item building
- `Args` - Convenience argument types for common registries

