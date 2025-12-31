# Chat Formatting System

MichelleLib provides a simple and powerful abstraction over Paper's `AsyncChatEvent` and `ChatRenderer` system, making it easy to format chat messages with prefixes, suffixes, and custom transformations.

## Why Use This Instead of AsyncChatEvent?

### The Problem with Direct AsyncChatEvent Usage

Using `AsyncChatEvent` directly requires:

1. **Boilerplate**: Creating a listener class, implementing `ChatRenderer`, handling event registration
2. **Manual composition**: Manually composing multiple formatters if you want to chain them
3. **Optimization complexity**: Manually detecting when you can use `ViewerUnaware` for performance
4. **No integration**: Manually integrating with existing text systems (Prefix, TextEngine, PlaceholderAPI)

### The MichelleLib Solution

**Before (direct AsyncChatEvent):**
```java
public class ChatListener implements Listener, ChatRenderer {
    private final TextEngine engine;
    private final Prefix prefix;
    
    public ChatListener(TextEngine engine, Prefix prefix) {
        this.engine = engine;
        this.prefix = prefix;
    }
    
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(this);
    }
    
    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        // Manual composition
        Component prefixed = prefix.apply(message);
        Component nameColored = sourceDisplayName.color(NamedTextColor.AQUA);
        
        // Manual PlaceholderAPI integration
        if (viewer instanceof Player player) {
            TagResolver papi = PlaceholderResolvers.papiTag(player);
            // ... more manual work
        }
        
        return nameColored.append(Component.text(": ")).append(prefixed);
    }
}

// Later in onEnable():
getServer().getPluginManager().registerEvents(new ChatListener(engine, prefix), this);
```

**After (MichelleLib abstraction):**
```java
ChatFormat format = ChatFormat.builder()
    .prefix(prefix)
    .format((source, displayName, message, viewer) -> {
        return displayName.color(NamedTextColor.AQUA)
            .append(Component.text(": "))
            .append(message);
    })
    .build();

ChatFormatService service = new ChatFormatService();
service.registerFormat(format);
getServer().getPluginManager().registerEvents(service, this);
```

### Key Benefits

1. **Less Boilerplate**: No need to create listener classes or implement `ChatRenderer` directly
2. **Built-in Composition**: Chain multiple formatters easily with the builder pattern
3. **Automatic Optimization**: Automatically uses `ViewerUnaware` when all formatters support it
4. **Native Integration**: Works seamlessly with `Prefix`, `TextEngine`, and PlaceholderAPI
5. **Centralized Management**: `ChatFormatService` makes it easy to manage multiple formats
6. **Thread-Safe**: Handles concurrent format registration safely
7. **Dynamic Control**: Enable/disable formatting without unregistering listeners
8. **Multiple Formats**: Easily apply multiple formats in sequence without manual composition

### Side-by-Side Comparison

| Feature | Direct AsyncChatEvent | MichelleLib |
|---------|----------------------|-------------|
| **Lines of code** | ~30-50 lines | ~5-10 lines |
| **Listener class needed** | ✅ Yes | ❌ No |
| **Manual ChatRenderer impl** | ✅ Required | ❌ Handled |
| **Format composition** | Manual | Builder pattern |
| **Prefix integration** | Manual | `.prefix()` method |
| **ViewerUnaware optimization** | Manual detection | Automatic |
| **Multiple formats** | Complex chaining | Simple registration |
| **Enable/disable** | Unregister listener | `.setEnabled()` |
| **Thread safety** | Your responsibility | Built-in |

### When to Use Direct AsyncChatEvent

You might still want to use `AsyncChatEvent` directly if:
- You need very fine-grained control over event priority or cancellation
- You want to cancel specific chat messages conditionally
- You're doing complex event manipulation beyond formatting
- You need to modify event properties beyond rendering

For most use cases (prefixes, suffixes, color formatting, placeholders), MichelleLib's abstraction is simpler and more maintainable.

## Overview

The chat system consists of three main components:

1. **`ChatFormatter`** - A functional interface for custom formatting logic
2. **`ChatFormat`** - A builder class that chains formatters together
3. **`ChatFormatService`** - A service that applies formats to all chat messages globally

## Quick Start

### Basic Usage

```java
public class MyPlugin extends JavaPlugin {
    
    private ChatFormatService chatService;
    private TextEngine textEngine;
    
    @Override
    public void onEnable() {
        this.textEngine = TextEngine.builder().build();
        
        // Create a chat format with a prefix
        ChatFormat format = ChatFormat.builder()
            .prefix(Prefix.miniMessage(textEngine, "<gray>[<red>Server</red>]</gray>"))
            .separator(textEngine, "<gray>: </gray>")
            .format((source, displayName, message, viewer) -> {
                return displayName.append(Component.text(": ")).append(message);
            })
            .build();
        
        // Register the service and apply the format
        this.chatService = new ChatFormatService();
        this.chatService.registerFormat(format);
        getServer().getPluginManager().registerEvents(this.chatService, this);
    }
}
```

## ChatFormat Builder

The `ChatFormat` builder provides a fluent API for constructing chat formats:

### Simple Format

```java
// Just "Player: message"
ChatFormat simple = ChatFormat.simple();
```

### With Prefix

```java
ChatFormat withPrefix = ChatFormat.withPrefix(
    Prefix.miniMessage(textEngine, "<gray>[<red>MyPlugin</red>]</gray>")
);
```

### Custom Builder

```java
ChatFormat format = ChatFormat.builder()
    // Add a prefix
    .prefix(textEngine, "<gray>[<green>Chat</green>]</gray>")
    
    // Add separator between name and message
    .separator(textEngine, "<dark_gray> » </dark_gray>")
    
    // Custom formatting
    .format((source, displayName, message, viewer) -> {
        Component name = displayName.color(NamedTextColor.GREEN);
        return name.append(Component.text(": ")).append(message);
    })
    
    // Add a suffix
    .append(textEngine, "<gray> [via Plugin]</gray>")
    
    .build();
```

### Viewer-Unaware Formatting

If your format doesn't need to know about the viewer (same output for everyone), use `formatUnaware` for better performance:

```java
ChatFormat format = ChatFormat.builder()
    .prefix(textEngine, "<gray>[Server]</gray>")
    .formatUnaware((source, displayName, message) -> {
        // Same output for all viewers - more efficient!
        return displayName.append(Component.text(": ")).append(message);
    })
    .build();
```

## ChatFormatService

The `ChatFormatService` automatically applies registered formats to all chat messages:

```java
ChatFormatService service = new ChatFormatService();

// Register multiple formats (applied in order)
service.registerFormat(format1);
service.registerFormat(format2);

// Enable/disable
service.setEnabled(false); // Temporarily disable
service.setEnabled(true);  // Re-enable

// Register the service as an event listener
getServer().getPluginManager().registerEvents(service, plugin);
```

### Multiple Formats

Multiple formats are applied in the order they were registered:

```java
service.registerFormat(prefixFormat);   // Applied first
service.registerFormat(colorFormat);    // Applied second
service.registerFormat(suffixFormat);   // Applied third
```

## Custom Formatters

### Viewer-Aware Formatter

Use when different viewers should see different messages:

```java
ChatFormatter customFormatter = (source, displayName, message, viewer) -> {
    if (viewer instanceof Player player) {
        // Different format for different players
        if (player.hasPermission("chat.see.colors")) {
            return displayName.color(NamedTextColor.RAINBOW)
                .append(Component.text(": "))
                .append(message);
        }
    }
    return displayName.append(Component.text(": ")).append(message);
};

ChatFormat format = ChatFormat.builder()
    .format(customFormatter)
    .build();
```

### Viewer-Unaware Formatter

Use when all viewers should see the same message (more efficient):

```java
ChatFormatter.ViewerUnawareChatFormatter formatter = 
    (source, displayName, message) -> {
        return displayName
            .color(NamedTextColor.GOLD)
            .append(Component.text(": "))
            .append(message);
    };

ChatFormat format = ChatFormat.builder()
    .formatUnaware(formatter)
    .build();
```

## Integration with Existing Systems

### With Prefix System

```java
Prefix serverPrefix = Prefix.miniMessage(
    textEngine, 
    "<gray>[<red>Server</red>]</gray>"
);

ChatFormat format = ChatFormat.builder()
    .prefix(serverPrefix)
    .format((source, displayName, message, viewer) -> {
        return displayName.append(Component.text(": ")).append(message);
    })
    .build();
```

### With TextEngine

```java
TextEngine engine = TextEngine.builder()
    .expandPlaceholderApiPercents(true)
    .enablePapiTag(true)
    .build();

ChatFormat format = ChatFormat.builder()
    .prefix(engine, "<gray>[<papi:luckperms_prefix>]</gray>")
    .format((source, displayName, message, viewer) -> {
        // PlaceholderAPI placeholders work in viewer context
        return displayName.append(Component.text(": ")).append(message);
    })
    .build();
```

## Advanced Examples

### Permission-Based Formatting

```java
ChatFormat format = ChatFormat.builder()
    .format((source, displayName, message, viewer) -> {
        if (viewer instanceof Player player) {
            if (player.hasPermission("chat.vip")) {
                return Component.text()
                    .append(Component.text("[VIP] ", NamedTextColor.GOLD))
                    .append(displayName)
                    .append(Component.text(": "))
                    .append(message)
                    .build();
            }
        }
        return displayName.append(Component.text(": ")).append(message);
    })
    .build();
```

### Rank-Based Prefixes with PlaceholderAPI

```java
ChatFormat format = ChatFormat.builder()
    .prefix(textEngine, "<papi:luckperms_prefix><gray>[</gray><papi:luckperms_suffix><gray>]</gray>")
    .separator(textEngine, "<gray> » </gray>")
    .format((source, displayName, message, viewer) -> {
        // Custom color based on source player's rank
        Component name = displayName;
        if (source.hasPermission("rank.admin")) {
            name = displayName.color(NamedTextColor.RED);
        } else if (source.hasPermission("rank.moderator")) {
            name = displayName.color(NamedTextColor.YELLOW);
        }
        return name.append(Component.text(": ")).append(message);
    })
    .build();
```

### Multiple Transformations

```java
ChatFormat format = ChatFormat.builder()
    .prefix(textEngine, "<gray>[Chat]</gray>")
    .transformDisplayName(name -> name.color(NamedTextColor.AQUA))
    .separator(textEngine, "<dark_gray> » </dark_gray>")
    .transformMessage(msg -> msg.decorate(TextDecoration.ITALIC))
    .append(textEngine, "<gray> ✓</gray>")
    .build();
```

## Thread Safety

- `ChatFormat` instances are immutable and thread-safe
- `ChatFormatService` uses thread-safe collections and can safely register/unregister formats from multiple threads
- Chat rendering happens asynchronously (via `AsyncChatEvent`), but format application is thread-safe

## Performance Considerations

1. **Use viewer-unaware formatters when possible** - They're more efficient as they only format once instead of once per viewer
2. **Avoid expensive operations in formatters** - Chat events are frequent, keep formatters lightweight
3. **Cache complex components** - If you're parsing MiniMessage or doing expensive lookups, consider caching the results

## Migration from Legacy Chat Events

If you're migrating from the old `AsyncPlayerChatEvent`:

**Old way:**
```java
@EventHandler
public void onChat(AsyncPlayerChatEvent event) {
    event.setFormat("[%1$s] %2$s");
    event.setMessage(event.getMessage().replaceAll("&", "§"));
}
```

**New way:**
```java
ChatFormat format = ChatFormat.builder()
    .format((source, displayName, message, viewer) -> {
        Component formatted = Component.text("[")
            .append(displayName)
            .append(Component.text("] "))
            .append(message);
        return formatted;
    })
    .build();

chatService.registerFormat(format);
```

*This file was created with the help of AI to improve documentation clarity.*

