# YAML Configs & Messages

MichelleLib provides a small, shade-friendly YAML layer with three main components:

## When to use what

**YamlMessages** — **High value, use for `messages.yml`**
- Thread-safe message lookups with MiniMessage parsing
- Integrates with your `TextEngine` (PlaceholderAPI, prefixes, etc.)
- Flattens YAML into an immutable snapshot for safe concurrent reads
- **Use this for all message files** — it's the recommended approach

**YamlSnapshot** — **Use when you need thread-safe config reads**
- Required for Folia compatibility (reading configs from async/region threads)
- Required if you read configs from async tasks
- Optional if you only read configs on the main thread (but still recommended)

**YamlStore** — **Use when you need nested folders or want convenience**
- Supports nested directories (`menus/shop.yml`, `data/stats.yml`)
- Automatic default resource copying
- Path safety (prevents directory traversal)
- Optional if you only need `config.yml` in the root folder

---

## Messages (`messages.yml`) with MiniMessage

**Recommended: Use `YamlMessages` for all message files.**

`YamlMessages` flattens a YAML file into an immutable snapshot so lookups are safe across threads, then parses with your `TextEngine`.

```java
import io.voluble.michellelib.text.TextEngine;
import io.voluble.michellelib.text.message.YamlMessages;

public final class MyPlugin extends JavaPlugin {
  private TextEngine text;
  private YamlMessages messages;

  @Override
  public void onEnable() {
    this.text = TextEngine.builder().build();
    this.messages = YamlMessages.builder(this, text)
      .file("messages.yml")
      .defaultsFromResource("messages.yml")
      .buildAndLoad();
  }
}
```

Usage:

```java
player.sendMessage(messages.mini(player, "errors.no-permission"));
player.sendMessage(messages.prefixed(player, "general.saved"));
```

If a key is missing, a MiniMessage fallback is used:

```java
.missingTemplateMiniMessage("<red>Missing message: <white>{key}</white></red>")
```

**Why use YamlMessages?**
- **Thread-safe**: message lookups work from any thread (async, region, entity)
- **Integrated**: works with your `TextEngine` (PlaceholderAPI, custom tags, etc.)
- **Convenient**: one-line message lookup with automatic parsing
- **Safe**: immutable snapshot prevents crashes on Folia

---

## YAML files (general purpose)

**When to use:**
- You need thread-safe config reads (Folia/async)
- You want nested folder support (`menus/shop.yml`, `data/stats.yml`)
- You want automatic default resource copying
- You prefer a cleaner API over `plugin.getConfig()`

**When you can skip:**
- Simple plugins that only read configs on the main thread
- You only need `config.yml` in the root folder
- You're fine with `plugin.getConfig()` (not thread-safe, but works for simple cases)

Create a store and open documents by relative path:

```java
import io.voluble.michellelib.config.YamlDocument;
import io.voluble.michellelib.config.YamlSnapshot;
import io.voluble.michellelib.config.YamlStore;

public final class MyPlugin extends JavaPlugin {
  private YamlStore yamls;
  private YamlDocument config;

  @Override
  public void onEnable() {
    this.yamls = YamlStore.create(this);
    this.config = yamls.yaml("config.yml").copyDefaultsFromResource("config.yml");
    this.config.reload();
  }
}
```

**Prefer reading from the immutable snapshot at runtime** (thread-safe from any thread):

```java
YamlSnapshot snap = config.snapshot();
String name = snap.getString("server.name", "Default");
int limit = snap.getInt("limits.max", 25);
```

**Nested folders work the same way:**

```java
YamlDocument menus = yamls.yaml("menus/main.yml").copyDefaultsFromResource("menus/main.yml");
menus.reload();
```

**If access to the raw Bukkit config is required** (must be on main/global thread):

```java
String raw = config.read(yaml -> yaml.getString("server.name"));
config.edit(yaml -> yaml.set("server.name", "New Name"));
config.save();
```

**Thread-safety notes:**
- `snapshot()` - Safe from any thread (async, region, entity)
- `read()` / `edit()` - Must be on main/global thread (uses Bukkit API)
- `reload()` / `save()` - Should be on global thread or async (disk I/O)

*This file was created with the help of AI to improve documentation clarity.*
