# Cooldowns (Folia-safe)

MichelleLib cooldowns are designed to be:

- **Thread-safe** for reads and checks (safe from async, region, and entity threads)
- **Atomic** (`tryUse` is one operation)
- **Optional persistence** via YAML (save/load should be scheduled asynchronously)

---

## Quick start (no persistence)

```java
import io.voluble.michellelib.cooldown.CooldownService;

public final class MyPlugin extends JavaPlugin {
  private CooldownService cooldowns;

  @Override
  public void onEnable() {
    this.cooldowns = CooldownService.builder(this).build();
  }
}
```

Usage:

```java
import java.time.Duration;

CooldownResult result = cooldowns.tryUse(player, "teleport", Duration.ofSeconds(30));
if (!result.allowed()) {
  player.sendRichMessage("<red>Wait " + result.remaining().toSeconds() + "s.");
  return;
}

// Allowed, continue
```

---

## Typed keys (recommended)

```java
public enum MyCooldowns implements CooldownKey {
  TELEPORT("teleport"),
  KIT("kit");

  private final String id;
  MyCooldowns(String id) { this.id = id; }
  @Override public String id() { return id; }
}
```

Usage:

```java
CooldownResult result = cooldowns.tryUse(player, MyCooldowns.TELEPORT, Duration.ofSeconds(30));
```

---

## YAML persistence (optional)

```java
import java.time.Duration;

this.cooldowns = CooldownService.builder(this)
  .yamlPersistence("cooldowns.yml")
  .autosaveEvery(Duration.ofMinutes(2))
  .pruneEvery(Duration.ofMinutes(5))
  .buildAndLoadAsync();
```

Recommended lifecycle:

```java
@Override
public void onDisable() {
  if (cooldowns != null) {
    cooldowns.shutdown();
  }
}
```

Threading notes:
- `tryUse(...)`, `remaining(...)`, `isCooling(...)` are safe from any thread
- `load()` / `save()` perform disk I/O via YAML and should be called from async/global scheduling
- `loadAsync()` / `saveAsync()` are provided for convenience

*This file was created with the help of AI to improve documentation clarity.*

