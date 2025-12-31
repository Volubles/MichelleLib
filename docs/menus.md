# MichelleLib Menus (Paper-first, Folia-safe)

This document explains the **menu system** in MichelleLib and shows **best-practice patterns** for building inventory UIs on modern Paper.

It focuses on:
- **Correctness**: no "ghost items", no accidental dupes, no unsafe inventory mutations.
- **Thread safety**: Folia/Paper schedulers + correct entity ownership.
- **Clean API**: a high-level `MenuDefinition` layer for most menus, plus the low-level `MenuSession` for power use.

---

## Table of Contents

1. [Mental Model](#mental-model-important)
2. [Quick Start](#quick-start)
3. [MenuSettings (Engine Knobs)](#menusettings-engine-knobs)
4. [Layout Helpers](#layout-helpers-menubuilder)
5. [Pattern Layout](#pattern-layout-chest-menus)
6. [Navigation & Back Stack](#navigation-back-stack)
7. [Real-World Examples](#real-world-examples)
8. [Placeable Slots](#placeable-slots-advanced)
9. [Advanced Patterns](#advanced-patterns)
10. [Concurrency & Async Safety](#concurrency-and-async-safety-folia)
11. [Common Pitfalls & Solutions](#common-pitfalls--solutions)
12. [Troubleshooting](#troubleshooting)
13. [Performance Tips](#performance-tips)
14. [Integration Examples](#integration-examples)
15. [When to Use Low-Level API](#when-to-use-the-low-level-menusession)
16. [FAQ](#faq)
17. [Suggested Folder Structure](#suggested-folder-structure-in-your-plugin)

---

## Mental model (important)

### What you are building

MichelleLib menus are inventory UIs implemented with:
- A per-plugin `MenuService` (instance-based, no static singleton)
- A per-player `MenuSession` (stores menu state and handles click/drag/close routing)
- A high-level `MenuDefinition` (recommended) that *configures and opens* a menu with lifecycle hooks

### The two layers

- **High-level API (recommended)**: `MenuDefinition` / `MenuBuilder` / `MenuContext`
  - Defines layout and behavior declaratively
  - Installs lifecycle hooks
  - Keeps transitions safe
- **Low-level API (power-user)**: `MenuSession`
  - Directly opens inventories, sets items, manages back stack, etc.
  - You must be more disciplined (still safe by default, but you manage structure)

### Lifecycle flow

```
1. MenuDefinition.open(menus, player)
   ↓
2. MenuSession opens inventory (entity scheduler)
   ↓
3. onOpen hook runs (MenuContext available)
   ↓
4. Items are placed via setItem() calls
   ↓
5. Player interacts (clicks/drags/closes)
   ↓
6. MenuSession routes events to MenuItem handlers
   ↓
7. onClose hook runs (MenuCloseContext available)
   ↓
8. Session cleans up (items returned if configured)
```

---

## Why we do "next tick" transitions (Paper rule)

Paper's `InventoryClickEvent` javadoc explicitly warns:
- **Do not** call `openInventory()` or `closeInventory()` inside the click handler
- Inventory is **mid-mutation** and you can create server/client desync (ghost items) or inconsistent outcomes

MichelleLib enforces this by:
- Providing `MenuActions` (available via `ClickContext.actions()` and `MenuContext.actions()`)
- Scheduling transitions **next tick** on the player's owning entity scheduler (Folia-safe)

Rule of thumb:
- **Inside click handler**: use `ctx.actions().transition(...)` or `ctx.actions().close()`
- **Outside click handler**: you may open directly, but using `MenuDefinition.open(...)` is still preferred

---

## Quick start

### 1) Create `MenuService` in your plugin

```java
public final class MyPlugin extends JavaPlugin {
  private MenuService menus;

  @Override
  public void onEnable() {
    this.menus = MenuService.create(this);
  }

  @Override
  public void onDisable() {
    if (menus != null) {
      menus.shutdown(); // optional but recommended
    }
  }
}
```

### 2) Define a menu (high-level)

```java
import io.voluble.michellelib.menu.template.MenuDefinition;
import io.voluble.michellelib.menu.item.Items;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

MenuDefinition mainMenu =
  MenuDefinition.chest(6)
    .title(t -> Component.text("Main Menu"))
    .border(ctx -> new Items.DisplayItem(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)))
    .item(49, new Items.CloseItem())
    .onOpen(ctx -> {
      // dynamic items can be placed here too
      ctx.setItem(13, new Items.DisplayItem(ItemStack.of(Material.DIAMOND)));
    })
    .build();
```

### 3) Open it for a player

```java
mainMenu.open(menus, player);
```

---

## MenuSettings (engine knobs)

`MenuSettings` lets you tune correctness/performance.

### Defaults

`MenuSettings.defaults()` is conservative:
- click debounce: ~150ms
- client sync after manual cursor/hotbar mutations: enabled

### Builder usage

```java
MenuSettings settings = MenuSettings.builder()
  .clickDebounceMillis(120)
  .syncClientAfterManualMutation(true)
  .build();

MenuService menus = MenuService.create(this, settings);
```

### When to change `syncClientAfterManualMutation`

Leave it `true` unless you have measured that it is a bottleneck.

This sync is a safety net for:
- manual cursor changes
- hotbar number-key place logic
- shift-insert routing into "placeable" slots

If you disable it, you must be very confident your menu doesn't mutate those pathways (or that you handle sync elsewhere).

---

## Layout helpers (MenuBuilder)

These helpers reduce boilerplate and keep layout logic DRY.

### `border(...)`

Fills the outer frame (top row + bottom row + left/right sides).

```java
MenuDefinition framed =
  MenuDefinition.chest(5)
    .title(Component.text("Framed"))
    .border(ctx -> new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
    .build();
```

### `fill(...)`

Fills all slots with the given item/factory.

```java
MenuDefinition filled =
  MenuDefinition.chest(3)
    .title(Component.text("Filled"))
    .fill(ctx -> new Items.DisplayItem(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)))
    .build();
```

### `inner(...)`

Fills only the inside (excluding border).

```java
MenuDefinition inner =
  MenuDefinition.chest(6)
    .title(Component.text("Inner"))
    .border(new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
    .inner(new Items.DisplayItem(ItemStack.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE)))
    .build();
```

### `row(rowIndex, ...)` and `column(columnIndex, ...)`

```java
MenuDefinition lines =
  MenuDefinition.chest(6)
    .title(Component.text("Lines"))
    .row(0, new Items.DisplayItem(ItemStack.of(Material.RED_STAINED_GLASS_PANE)))
    .column(4, new Items.DisplayItem(ItemStack.of(Material.BLUE_STAINED_GLASS_PANE)))
    .build();
```

### More "UX sugar" helpers

#### `slots(item, int...slots)`

Set items in multiple specific slots without repeating `.item(...)`.

```java
MenuDefinition quick =
  MenuDefinition.chest(3)
    .title(Component.text("Quick Slots"))
    .slots(new Items.DisplayItem(ItemStack.of(Material.EMERALD)), 10, 11, 12, 13, 14, 15, 16)
    .build();
```

#### `corners(...)`

Sets the four corners of a chest menu (0, 8, bottom-left, bottom-right).

```java
MenuDefinition corners =
  MenuDefinition.chest(6)
    .title(Component.text("Corners"))
    .corners(new Items.DisplayItem(ItemStack.of(Material.GOLD_BLOCK)))
    .build();
```

#### `frameTopBottom(...)`

Fills only top + bottom rows (no left/right sides).

```java
MenuDefinition frame =
  MenuDefinition.chest(5)
    .title(Component.text("Frame"))
    .frameTopBottom(new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
    .build();
```

#### `center(...)`

Either place at a specific slot (`center(slot, ...)`) or at the computed center of a chest menu (`center(...)`).

```java
MenuDefinition centered =
  MenuDefinition.chest(6)
    .title(Component.text("Centered"))
    .center(new Items.DisplayItem(ItemStack.of(Material.NETHER_STAR))) // computed center slot
    .build();
```

#### `background(...)` (never overwrites explicit items)

Background is applied last and only fills **still-empty** slots (i.e. it will not overwrite `.item(...)` or pattern binds). Perfect for "pure filler".

```java
MenuDefinition bg =
  MenuDefinition.chest(6)
    .title(Component.text("Background"))
    .background(new Items.DisplayItem(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)))
    .border(new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
    .item(49, new Items.CloseItem())
    .build();
```

---

## Pattern layout (chest menus)

For chest menus you can define a simple pattern using characters.

### Example: framed menu + buttons

```java
MenuDefinition menu =
  MenuDefinition.chest(6)
    .title(Component.text("Pattern Menu"))
    .pattern(
      "#########",
      "#.......#",
      "#...P...#",
      "#.......#",
      "#.......#",
      "###C#####"
    )
    .bind('#', ctx -> new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
    .bind('P', ctx -> new Items.DisplayItem(ItemStack.of(Material.DIAMOND)))
    .bind('C', ctx -> new Items.CloseItem())
    .build();
```

**Visual representation:**
```
Row 0: [###][###][###][###][###][###][###][###][###]
Row 1: [#  ][   ][   ][   ][   ][   ][   ][   ][#  ]
Row 2: [#  ][   ][   ][   ][ P ][   ][   ][   ][#  ]
Row 3: [#  ][   ][   ][   ][   ][   ][   ][   ][#  ]
Row 4: [#  ][   ][   ][   ][   ][   ][   ][   ][#  ]
Row 5: [###][###][###][   ][ C ][   ][###][###][###]
```

Notes:
- Only the first 9 characters per row are used
- Pattern row count must match the chest rows
- Pattern application happens *after explicit hooks*, but it will still set slots you specify—so decide if you want pattern to be background or primary.

---

## Navigation (Back stack)

`MenuContext.open(other)` wires a safe "back" action automatically:
- It pushes a runnable that reopens the current menu
- It schedules opening the target menu next tick (safe in click handlers)

### Example: main menu → settings menu → back

```java
MenuDefinition settings =
  MenuDefinition.chest(3)
    .title(Component.text("Settings"))
    .item(22, ctx -> new Items.BackItem(ctx::back))
    .build();

MenuDefinition main =
  MenuDefinition.chest(3)
    .title(Component.text("Main"))
    .item(13, ctx -> new Items.OpenMenuItem(() -> ctx.open(settings)))
    .build();
```

If you want full manual control, use `MenuSession.pushBack(...)` and `MenuSession.goBack()`.

---

## Real-World Examples

### Example 1: Simple Shop Menu

```java
public final class ShopMenu {
  private static final MenuDefinition SHOP = MenuDefinition.chest(3)
    .title(Component.text("Shop"))
    .border(ctx -> new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
    .item(10, ctx -> createShopItem(Material.DIAMOND, 100))
    .item(11, ctx -> createShopItem(Material.EMERALD, 50))
    .item(12, ctx -> createShopItem(Material.GOLD_INGOT, 25))
    .item(16, ctx -> new Items.CloseItem())
    .build();

  private static MenuItem createShopItem(Material mat, int price) {
    return new MenuItem() {
      @Override
      public ItemStack render(Player viewer) {
        ItemStack stack = ItemStack.of(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(mat.name()));
        meta.lore(List.of(
          Component.text("Price: $" + price),
          Component.text("Click to buy!")
        ));
        stack.setItemMeta(meta);
        return stack;
      }

      @Override
      public boolean onClick(ClickContext ctx) {
        // Check balance, deduct, give item, etc.
        ctx.player().sendMessage("Purchased " + mat.name() + " for $" + price);
        return true;
      }
    };
  }

  public static void open(MenuService menus, Player player) {
    SHOP.open(menus, player);
  }
}
```

### Example 2: Paginated Item List

```java
import io.voluble.michellelib.menu.paginate.Paginator;

public final class ItemListMenu {
  private final List<ItemStack> allItems;
  private final Paginator<ItemStack> paginator;

  public ItemListMenu(List<ItemStack> items) {
    this.allItems = items;
    this.paginator = new Paginator<>(items, 28); // 28 slots for content (6 rows - 2 border rows)
  }

  public MenuDefinition build() {
    return MenuDefinition.chest(6)
      .title(Component.text("Items - Page " + (paginator.page() + 1)))
      .border(ctx -> new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
      .onOpen(ctx -> {
        // Place page items
        List<ItemStack> pageItems = paginator.pageItems();
        int[] contentSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < Math.min(pageItems.size(), contentSlots.length); i++) {
          ctx.setItem(contentSlots[i], new Items.DisplayItem(pageItems.get(i)));
        }

        // Navigation buttons
        if (paginator.page() > 0) {
          ctx.setItem(45, new Items.PageItem(false, () -> {
            paginator.prev();
            build().open(ctx.menus(), ctx.player());
          }));
        }
        if (paginator.page() < paginator.totalPages() - 1) {
          ctx.setItem(53, new Items.PageItem(true, () -> {
            paginator.next();
            build().open(ctx.menus(), ctx.player());
          }));
        }
      })
      .build();
  }
}
```

### Example 3: Settings Menu with Toggles

```java
public final class SettingsMenu {
  private final Map<UUID, Boolean> toggleStates = new ConcurrentHashMap<>();

  public MenuDefinition build(Player player) {
    return MenuDefinition.chest(3)
      .title(Component.text("Settings"))
      .border(ctx -> new Items.DisplayItem(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE)))
      .item(10, ctx -> createToggle("Auto-Smelt", player.getUniqueId(), Material.FURNACE))
      .item(11, ctx -> createToggle("Auto-Repair", player.getUniqueId(), Material.ANVIL))
      .item(12, ctx -> createToggle("Night Vision", player.getUniqueId(), Material.GOLDEN_CARROT))
      .item(16, ctx -> new Items.CloseItem())
      .build();
  }

  private MenuItem createToggle(String name, UUID playerId, Material icon) {
    return new MenuItem() {
      @Override
      public ItemStack render(Player viewer) {
        boolean enabled = toggleStates.getOrDefault(playerId, false);
        ItemStack stack = ItemStack.of(icon);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(List.of(
          Component.text("Status: " + (enabled ? "ON" : "OFF")),
          Component.text("Click to toggle")
        ));
        if (enabled) {
          meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
          stack.setItemMeta(meta);
          stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        } else {
          stack.setItemMeta(meta);
        }
        return stack;
      }

      @Override
      public boolean onClick(ClickContext ctx) {
        boolean current = toggleStates.getOrDefault(playerId, false);
        toggleStates.put(playerId, !current);
        // Re-render happens automatically
        return true;
      }
    };
  }
}
```

---

## Placeable slots (advanced)

Some menus need to accept items from players (e.g. "deposit slot", "mailbox slot", "input slot").

MichelleLib supports this via the `MenuItem` "placeable" capabilities:
- `isPlaceable()`
- `canAccept(stack, ctx)`
- `maxAcceptAmount(stack, ctx)`
- `onInsert(inserted, ctx)`
- `itemsToReturnOnClose(viewer)` + `returnPlacedItems()`

### A simple input slot

```java
public final class InputSlot implements PlaceableItem {
  private ItemStack stored;

  @Override
  public ItemStack render(Player viewer) {
    return stored == null ? ItemStack.of(Material.HOPPER) : stored.clone();
  }

  @Override
  public boolean canAccept(ItemStack stack, ClickContext ctx) {
    return stored == null; // only accept if empty
  }

  @Override
  public int maxAcceptAmount(ItemStack stack, ClickContext ctx) {
    return Math.min(1, stack.getAmount()); // accept 1 at a time
  }

  @Override
  public void onInsert(ItemStack inserted, ClickContext ctx) {
    stored = inserted.clone();
    // Optional: trigger async save
    ctx.player().sendMessage("Item stored!");
  }

  @Override
  public List<ItemStack> itemsToReturnOnClose(Player viewer) {
    return stored == null ? List.of() : List.of(stored.clone());
  }
}
```

### Deposit slot with validation

```java
public final class DepositSlot implements PlaceableItem {
  private final Predicate<ItemStack> validator;
  private ItemStack stored;

  public DepositSlot(Predicate<ItemStack> validator) {
    this.validator = validator;
  }

  @Override
  public ItemStack render(Player viewer) {
    if (stored != null) return stored.clone();
    ItemStack placeholder = ItemStack.of(Material.HOPPER);
    ItemMeta meta = placeholder.getItemMeta();
    meta.displayName(Component.text("Deposit Item"));
    meta.lore(List.of(Component.text("Place valid items here")));
    placeholder.setItemMeta(meta);
    return placeholder;
  }

  @Override
  public boolean canAccept(ItemStack stack, ClickContext ctx) {
    return validator.test(stack);
  }

  @Override
  public int maxAcceptAmount(ItemStack stack, ClickContext ctx) {
    return stack.getAmount(); // accept full stack
  }

  @Override
  public void onInsert(ItemStack inserted, ClickContext ctx) {
    stored = inserted.clone();
    // Process deposit (async if needed)
  }

  @Override
  public boolean returnPlacedItems() {
    return true; // return on close
  }

  @Override
  public List<ItemStack> itemsToReturnOnClose(Player viewer) {
    return stored == null ? List.of() : List.of(stored.clone());
  }
}
```

Important:
- Never do heavy I/O in `onInsert` (keep it fast)
- If you need DB writes, store state and schedule async persistence elsewhere
- Always clone ItemStacks when storing/returning to avoid reference issues

---

## Advanced Patterns

### Dynamic content with refresh heartbeat

```java
MenuDefinition dynamic = MenuDefinition.chest(3)
  .title(Component.text("Live Stats"))
  .refreshEveryTicks(20L) // refresh every second
  .onOpen(ctx -> {
    ctx.setItem(13, new MenuItem() {
      @Override
      public ItemStack render(Player viewer) {
        // This is called every second
        int online = Bukkit.getOnlinePlayers().size();
        ItemStack stack = ItemStack.of(Material.PLAYER_HEAD);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("Online: " + online));
        stack.setItemMeta(meta);
        return stack;
      }

      @Override
      public void onRefresh(Player viewer) {
        // Optional: called before render during refresh
      }
    });
  })
  .build();
```

### Filtered list menu

```java
public final class FilteredListMenu {
  private final List<ItemStack> allItems;
  private String filter = "";

  public MenuDefinition build() {
    return MenuDefinition.chest(6)
      .title(Component.text("Items - Filter: " + (filter.isEmpty() ? "None" : filter)))
      .border(ctx -> new Items.DisplayItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)))
      .onOpen(ctx -> {
        List<ItemStack> filtered = allItems.stream()
          .filter(item -> filter.isEmpty() || item.getType().name().contains(filter.toUpperCase()))
          .toList();

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < Math.min(filtered.size(), slots.length); i++) {
          ctx.setItem(slots[i], new Items.DisplayItem(filtered.get(i)));
        }

        // Filter button
        ctx.setItem(49, new MenuItem() {
          @Override
          public ItemStack render(Player viewer) {
            ItemStack stack = ItemStack.of(Material.HOPPER);
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(Component.text("Filter"));
            meta.lore(List.of(Component.text("Current: " + (filter.isEmpty() ? "None" : filter))));
            stack.setItemMeta(meta);
            return stack;
          }

          @Override
          public boolean onClick(ClickContext ctx) {
            // Open chat input or another menu for filter selection
            ctx.player().sendMessage("Use /menu filter <name> to filter");
            return true;
          }
        });
      })
      .build();
  }
}
```

### Multi-page with async data loading

```java
public final class AsyncDataMenu {
  private final MenuService menus;

  public MenuDefinition build(Player player) {
    return MenuDefinition.chest(6)
      .title(Component.text("Loading..."))
      .onOpen(ctx -> {
        // Show loading indicator
        ctx.setItem(22, new Items.DisplayItem(ItemStack.of(Material.HOURGLASS)));

        // Load data async
        menus.scheduler().runAsync(() -> {
          List<ItemStack> items = loadItemsFromDatabase(player);

          // Apply on entity thread
          menus.scheduler().runEntity(player, () -> {
            MenuSession session = menus.session(player);
            if (session.view() == null) return; // menu closed

            // Clear loading, place items
            session.clearItem(22);
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
            for (int i = 0; i < Math.min(items.size(), slots.length); i++) {
              session.setItem(slots[i], new Items.DisplayItem(items.get(i)));
            }
          });
        });
      })
      .build();
  }

  private List<ItemStack> loadItemsFromDatabase(Player player) {
    // Simulate DB call
    return List.of(ItemStack.of(Material.DIAMOND));
  }
}
```

---

## Concurrency and async safety (Folia)

### What is safe off-thread?

Treat most Bukkit API as **not thread-safe**. Do not:
- mutate inventories
- read/write world state
- open/close inventories
- teleport entities
from async threads.

### The only safe default for menu operations

MenuSession/MenuDefinition operations ultimately run via the player's entity scheduler:
- ensures correct region ownership on Folia
- still works on normal Paper

If you need async work:
- compute data async (DB calls, HTTP, heavy JSON)
- then schedule back onto entity thread (or global/region where appropriate) to apply menu changes

### Pattern: async compute → sync apply

```java
menus.scheduler().runAsync(() -> {
  List<MenuItem> computed = computeItemsOffThread();
  menus.scheduler().runEntity(player, () -> {
    MenuSession s = menus.session(player);
    if (s.view() == null) return; // menu might be closed
    // apply updates here
    for (int i = 0; i < computed.size(); i++) {
      s.setItem(i, computed.get(i));
    }
  });
});
```

### Thread ownership checks

```java
// Check if current thread owns the entity (Folia-aware)
if (Bukkit.isOwnedByCurrentRegion(player)) {
  // Safe to mutate player/inventory directly
  player.getInventory().addItem(item);
} else {
  // Schedule on entity thread
  player.getScheduler().execute(plugin, () -> {
    player.getInventory().addItem(item);
  }, null, 1L);
}
```

---

## Common Pitfalls & Solutions

### Pitfall 1: Opening menu inside click handler

**❌ Wrong:**
```java
@Override
public boolean onClick(ClickContext ctx) {
  otherMenu.open(menus, ctx.player()); // UNSAFE!
  return true;
}
```

**✅ Correct:**
```java
@Override
public boolean onClick(ClickContext ctx) {
  ctx.actions().transition(() -> otherMenu.open(menus, ctx.player()));
  return true;
}
```

### Pitfall 2: Mutating inventory directly

**❌ Wrong:**
```java
@Override
public boolean onClick(ClickContext ctx) {
  ctx.view().getTopInventory().setItem(0, item); // Bypasses MenuSession!
  return true;
}
```

**✅ Correct:**
```java
// Use MenuSession (via MenuContext in high-level API)
ctx.session().setItem(0, newItem);

// Or in low-level API
MenuSession session = menus.session(player);
session.setItem(0, newItem);
```

### Pitfall 3: Storing Player references

**❌ Wrong:**
```java
private static final Map<Player, MenuDefinition> openMenus = new HashMap<>(); // Memory leak!
```

**✅ Correct:**
```java
private static final Map<UUID, MenuDefinition> openMenus = new HashMap<>();
```

### Pitfall 4: Heavy work in render/onClick

**❌ Wrong:**
```java
@Override
public ItemStack render(Player viewer) {
  ItemStack item = loadFromDatabase(viewer); // Blocking I/O!
  return item;
}
```

**✅ Correct:**
```java
// Cache or load async, then render from cache
private final Map<UUID, ItemStack> cache = new ConcurrentHashMap<>();

@Override
public ItemStack render(Player viewer) {
  return cache.getOrDefault(viewer.getUniqueId(), ItemStack.of(Material.AIR));
}
```

### Pitfall 5: Not handling menu closure during async work

**❌ Wrong:**
```java
menus.scheduler().runAsync(() -> {
  List<ItemStack> items = loadItems();
  menus.scheduler().runEntity(player, () -> {
    session.setItem(0, new Items.DisplayItem(items.get(0))); // Menu might be closed!
  });
});
```

**✅ Correct:**
```java
menus.scheduler().runAsync(() -> {
  List<ItemStack> items = loadItems();
  menus.scheduler().runEntity(player, () -> {
    MenuSession session = menus.session(player);
    if (session.view() == null) return; // Check if menu is still open
    session.setItem(0, new Items.DisplayItem(items.get(0)));
  });
});
```

---

## Troubleshooting

### "My menu doesn't update immediately"

**Possible causes:**
1. You're mutating inventory directly instead of using `MenuSession.setItem()`
2. Your `render()` method is throwing an exception (check server logs)
3. You're not calling `setItem()` on the correct thread

**Solution:**
```java
// Always use MenuSession
MenuSession session = menus.session(player);
session.setItem(slot, item); // This schedules render on correct thread
```

### "Items disappear when I click"

**Possible causes:**
1. You're not canceling the event when manually handling clicks
2. You're modifying slots that the click event is also modifying

**Solution:**
```java
@Override
public boolean onClick(ClickContext ctx) {
  // Return true to cancel (prevent default behavior)
  // Then manually apply your changes
  ctx.session().setItem(ctx.slot(), newItem);
  return true; // Cancel the event
}
```

### "Menu opens but is empty"

**Possible causes:**
1. `onOpen` hook not running (check for exceptions)
2. Items being set before inventory is fully opened
3. Pattern/background not being applied

**Solution:**
```java
.onOpen(ctx -> {
  // This runs AFTER inventory is opened
  ctx.setItem(13, item); // Safe here
})
```

### "Ghost items appear"

**Possible causes:**
1. Opening/closing inventory inside click handler
2. Not syncing client after manual cursor mutations
3. Race conditions with async operations

**Solution:**
- Always use `MenuActions.transition()` for menu changes in click handlers
- Keep `syncClientAfterManualMutation` enabled in `MenuSettings`
- Check thread ownership before mutations

### "Menu doesn't close properly"

**Possible causes:**
1. `preventClose` flag stuck
2. Exception in `onClose` hook
3. Session not being cleaned up

**Solution:**
```java
// Check if preventClose is set unintentionally
// Ensure onClose hooks don't throw exceptions
// Call MenuService.removeSession(player) on player quit
```

---

## Performance Tips

### 1. Cache expensive renders

```java
private final Map<UUID, ItemStack> renderCache = new ConcurrentHashMap<>();
private long lastCacheUpdate = 0;

@Override
public ItemStack render(Player viewer) {
  long now = System.currentTimeMillis();
  if (now - lastCacheUpdate > 1000) { // Update cache every second
    renderCache.clear();
    lastCacheUpdate = now;
  }
  return renderCache.computeIfAbsent(viewer.getUniqueId(), u -> computeExpensiveRender(viewer));
}
```

### 2. Use refresh heartbeat sparingly

```java
// Only enable if you truly need live updates
.refreshEveryTicks(20L) // Every second is usually enough
```

### 3. Batch slot updates

```java
// Instead of multiple setItem calls, use slots() helper
.slots(item, 10, 11, 12, 13, 14, 15, 16) // One call, multiple slots
```

### 4. Avoid unnecessary item cloning

```java
// If item is immutable, reuse it
private static final ItemStack STATIC_ITEM = ItemStack.of(Material.DIAMOND);

@Override
public ItemStack render(Player viewer) {
  return STATIC_ITEM.clone(); // Only clone if needed
}
```

### 5. Use background() for filler

```java
// background() only fills empty slots, avoiding overwrite checks
.background(fillerItem) // More efficient than fill() when you have explicit items
```

---

## Integration Examples

### Command integration

```java
@CommandAlias("menu")
public final class MenuCommand extends BaseCommand {
  private final MenuService menus;
  private final MenuDefinition mainMenu;

  public MenuCommand(MenuService menus) {
    this.menus = menus;
    this.mainMenu = MenuDefinition.chest(3)
      .title(Component.text("Main Menu"))
      .item(13, new Items.CloseItem())
      .build();
  }

  @Default
  public void execute(Player player) {
    mainMenu.open(menus, player);
  }
}
```

### Event integration

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
  // Open welcome menu after a delay
  menus.scheduler().runEntityDelayed(event.getPlayer(), 40L, () -> {
    welcomeMenu.open(menus, event.getPlayer());
  });
}
```

### Permission-based menus

```java
public MenuDefinition build(Player player) {
  MenuBuilder builder = MenuDefinition.chest(3)
    .title(Component.text("Admin Panel"));

  if (player.hasPermission("admin.use")) {
    builder.item(10, adminItem);
  }
  if (player.hasPermission("mod.use")) {
    builder.item(11, modItem);
  }

  return builder.build();
}
```

### PlaceholderAPI integration

```java
import me.clip.placeholderapi.PlaceholderAPI;

@Override
public ItemStack render(Player viewer) {
  ItemStack stack = ItemStack.of(Material.PLAYER_HEAD);
  ItemMeta meta = stack.getItemMeta();
  String name = PlaceholderAPI.setPlaceholders(viewer, "%player_name%");
  meta.displayName(Component.text(name));
  stack.setItemMeta(meta);
  return stack;
}
```

---

## When to use the low-level `MenuSession`

Use `MenuSession` directly if:
- you are building a complex UI framework on top
- you need custom session-scoped state/behavior that doesn't fit `MenuDefinition`
- you want explicit control over refresh heartbeats and slot mutation logic
- you need dynamic menu structure that changes based on runtime state

Otherwise: prefer `MenuDefinition` for 95% of menus.

### Low-level example

```java
MenuSession session = menus.session(player);
session.open(6, Component.text("Custom Menu"));
session.setItem(13, new Items.DisplayItem(ItemStack.of(Material.DIAMOND)));
session.setCursorPolicy(CursorClosePolicy.DROP);
session.startRefreshHeartbeat(20L); // Refresh every second
```

---

## Anti-dupe / anti-ghost checklist

- **Do not open/close inventories inside click handler**
  - Use `MenuActions` transition helpers (MichelleLib does this for built-in items)
- **Use `rawSlot` logic** to determine top vs bottom inventory
  - MichelleLib does this internally
- **Cancel when you manually apply inventory changes**
  - then apply all desired changes yourself
- **When cursor/hotbar is manually mutated**, consider forcing `player.updateInventory()` next tick
  - default enabled in `MenuSettings`
- **Always clone ItemStacks** when storing/returning to avoid reference issues
- **Check menu is still open** before applying async-loaded data
- **Use entity scheduler** for all player/inventory operations on Folia

---

## FAQ

### "My menu doesn't update immediately"
- Ensure you are calling `MenuSession.setItem(...)` (not mutating Bukkit inventory directly)
- Ensure your render methods are deterministic and not throwing exceptions
- Check that you're on the correct thread (entity scheduler for player operations)

### "Can I share one inventory across players?"
Generally: no (bad idea). Inventories hold viewer-specific state and click routing can become ambiguous. Use per-player sessions/menus.

### "Can I store Player in a static field?"
Don't. Always use UUID as identifiers. Avoid memory leaks.

### "How do I handle menu state across server restarts?"
Store state in a database or file, then restore in `onOpen` hook. Don't rely on in-memory state.

### "Can I use this with other menu libraries?"
MichelleLib is designed to be shaded into your plugin. It should work alongside other libraries as long as they don't conflict on event handling. However, using multiple menu systems can cause confusion—prefer one system per plugin.

### "How do I test menus?"
- Use mock players in unit tests
- Test menu opening/closing
- Test click handlers
- Test async data loading scenarios
- Verify thread safety in concurrent scenarios

### "What's the performance impact?"
- Menu operations are lightweight (mostly just item placement)
- Refresh heartbeats add minimal overhead (only when enabled)
- Async operations are recommended for heavy I/O
- Default settings are tuned for good performance

### "Can I customize the click debounce?"
Yes, via `MenuSettings.builder().clickDebounceMillis(...)`. Default is 150ms which prevents accidental double-clicks.

---

## Suggested folder structure in your plugin

```
my-plugin/
  menus/
    MainMenu.java
    SettingsMenu.java
    ShopMenu.java
    ItemListMenu.java
  items/
    InputSlot.java
    ToggleItem.java
    ShopItem.java
    PaginatedItem.java
  commands/
    MenuCommand.java
```

Keep menu definitions in one place and keep item implementations composable.

---

## Best Practices Summary

1. **Always use `MenuActions` for transitions in click handlers**
2. **Use `MenuDefinition` for 95% of menus** (high-level API)
3. **Store state by UUID, not Player references**
4. **Do heavy I/O async, then apply on entity thread**
5. **Check if menu is still open before applying async-loaded data**
6. **Use `background()` for filler, not `fill()` when you have explicit items**
7. **Enable refresh heartbeat only when needed**
8. **Keep `syncClientAfterManualMutation` enabled unless you've measured it as a bottleneck**
9. **Clone ItemStacks when storing/returning**
10. **Use entity scheduler for all player/inventory operations**

---

## Additional Resources

- Paper API Documentation: https://docs.papermc.io/
- Folia Threading Guide: https://docs.papermc.io/folia/reference/overview
- Adventure Component API: https://docs.adventure.kyori.net/

---

**Happy menu building!** 🎨

*This file was created with the help of AI to improve documentation clarity.*