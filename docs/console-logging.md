# Console Logging with Components

MichelleLib provides an easy-to-use wrapper around Paper's `ComponentLogger` that integrates with `TextEngine` for beautiful, colored console logs using MiniMessage.

## Overview

Paper provides `ComponentLogger`, which automatically serializes Adventure Components to ANSI-colored console output. This makes console logs much more readable and visually organized.

MichelleLib's `ComponentLoggerWrapper` wraps Paper's `ComponentLogger` and integrates it with your `TextEngine`, allowing you to use MiniMessage strings directly in your logs.

## Quick Start

```java
public class MyPlugin extends JavaPlugin {
    
    private ComponentLoggerWrapper logger;
    private TextEngine textEngine;
    
    @Override
    public void onEnable() {
        this.textEngine = TextEngine.builder().build();
        this.logger = new ComponentLoggerWrapper(
            getComponentLogger(),  // Paper's ComponentLogger
            textEngine
        );
        
        logger.info("<green>Plugin enabled successfully!</green>");
        logger.warn("<yellow>Configuration file not found, using defaults</yellow>");
    }
}
```

## Why Use Component-Based Logging?

### The Problem with Traditional Logging

Traditional logging uses plain strings:

```java
getLogger().info("Plugin enabled successfully!");
getLogger().warning("Configuration file not found, using defaults");
```

This produces plain, uncolored output that's hard to scan visually.

### The Solution: Component Logging

Component logging uses Adventure Components that are automatically colored in the console:

```java
logger.info("<green>Plugin enabled successfully!</green>");
logger.warn("<yellow>Configuration file not found, using defaults</yellow>");
```

This produces beautiful, color-coded output:
- Green for info messages
- Yellow for warnings  
- Red for errors
- And more!

## Basic Usage

### Logging Levels

```java
// INFO - general information
logger.info("<aqua>Player {} joined the game</aqua>", playerName);

// WARN - warnings that don't break functionality
logger.warn("<yellow>Unable to load configuration, using defaults</yellow>");

// ERROR - errors that may impact functionality
logger.error("<red>Failed to connect to database!</red>");

// DEBUG - detailed debugging information
logger.debug("<gray>Processing request: {}</gray>", requestId);

// TRACE - very detailed tracing information
logger.trace("<dark_gray>Executing query: {}</dark_gray>", sql);
```

### Using Components Directly

You can also pass Components directly if you prefer:

```java
Component message = Component.text("Plugin loaded")
    .color(NamedTextColor.GREEN)
    .append(Component.text(" successfully!")
        .color(NamedTextColor.AQUA));

logger.info(message);
```

### String Formatting

The wrapper supports simple `{}` placeholder replacement:

```java
logger.info("<green>Loaded {} items from database</green>", itemCount);
logger.warn("<yellow>Player {} attempted invalid action: {}</yellow>", playerName, action);
logger.error("<red>Error processing request {}: {}</red>", requestId, errorMessage);
```

### With Exceptions

Log exceptions with colored messages:

```java
try {
    // some code
} catch (IOException e) {
    logger.error("<red>Failed to read file: {}</red>", fileName, e);
    logger.warn("<yellow>Stack trace:</yellow>", e);
}
```

## MiniMessage Support

Since the wrapper uses your `TextEngine`, all MiniMessage features work:

```java
// Colors
logger.info("<green>Success!</green>");
logger.warn("<yellow>Warning!</yellow>");
logger.error("<red>Error!</red>");

// Decorations
logger.info("<bold>Important message</bold>");
logger.warn("<italic>Note: This is a note</italic>");

// Gradients (if supported)
logger.info("<gradient:green:blue>Fancy gradient text!</gradient>");

// Hover/click events
logger.info("<hover:show_text:'Tooltip'>Hover me!</hover>");

// PlaceholderAPI (if configured)
logger.info("<papi:server_name> is running");
```

## Integration with TextEngine

The wrapper uses your `TextEngine` instance, so it respects all your engine's settings:

```java
TextEngine engine = TextEngine.builder()
    .expandPlaceholderApiPercents(true)
    .enablePapiTag(true)
    .build();

ComponentLoggerWrapper logger = new ComponentLoggerWrapper(
    getComponentLogger(),
    engine
);

// PlaceholderAPI works automatically
logger.info("<papi:server_online> players online");
```

## Best Practices

### 1. Use Appropriate Colors

```java
// Good - color matches log level
logger.info("<green>Operation successful</green>");
logger.warn("<yellow>Non-critical issue occurred</yellow>");
logger.error("<red>Critical error!</red>");

// Avoid - misleading colors
logger.error("<green>Error occurred</green>"); // Don't do this!
```

### 2. Keep Messages Concise

```java
// Good
logger.info("<green>Loaded {} config entries</green>", count);

// Avoid - too verbose
logger.info("<green>Successfully loaded a total of {} configuration entries from the YAML file after parsing</green>", count);
```

### 3. Use Structured Logging

```java
// Good - structured information
logger.info("<aqua>[Database]</aqua> <green>Connected to {}:{}</green>", host, port);
logger.warn("<yellow>[Cache]</yellow> <yellow>Cache miss for key: {}</yellow>", key);

// Makes it easier to filter logs by component
```

### 4. Log Exceptions Properly

```java
// Good - include context
try {
    saveData();
} catch (IOException e) {
    logger.error("<red>Failed to save data to {}</red>", filePath, e);
}

// Avoid - losing exception
logger.error("<red>Failed to save data</red>"); // Where's the exception?!
```

## Common Patterns

### Plugin Lifecycle

```java
@Override
public void onEnable() {
    logger.info("<green>=======================================</green>");
    logger.info("<green>    {} v{} enabled</green>", 
        getDescription().getName(), 
        getDescription().getVersion());
    logger.info("<green>=======================================</green>");
    
    // ... initialization code ...
    
    logger.info("<green>Initialization complete!</green>");
}

@Override
public void onDisable() {
    logger.info("<yellow>Shutting down...</yellow>");
    // ... cleanup code ...
    logger.info("<yellow>Shutdown complete</yellow>");
}
```

### Configuration Loading

```java
public void loadConfig() {
    try {
        // ... load config ...
        logger.info("<green>Configuration loaded from {}</green>", configFile.getName());
    } catch (FileNotFoundException e) {
        logger.warn("<yellow>Config file not found, creating default...</yellow>");
        saveDefaultConfig();
    } catch (IOException e) {
        logger.error("<red>Failed to load config file: {}</red>", e.getMessage(), e);
    }
}
```

### Database Operations

```java
public void connectDatabase() {
    logger.info("<aqua>[Database]</aqua> <gray>Connecting to {}...</gray>", connectionString);
    try {
        // ... connect ...
        logger.info("<aqua>[Database]</aqua> <green>Connected successfully!</green>");
    } catch (SQLException e) {
        logger.error("<aqua>[Database]</aqua> <red>Connection failed: {}</red>", e.getMessage(), e);
    }
}
```

### Player Actions

```java
public void handlePlayerJoin(Player player) {
    logger.info("<green>[Player]</green> <aqua>{}</aqua> <gray>joined the server</gray>", 
        player.getName());
}

public void handlePlayerCommand(Player player, String command) {
    logger.debug("<gray>[Command]</gray> <aqua>{}</aqua> <gray>executed: {}</gray>", 
        player.getName(), command);
}
```

## Comparison with Traditional Logging

### Before (Traditional)

```java
getLogger().info("Plugin enabled successfully!");
getLogger().warning("Configuration file not found, using defaults");
getLogger().severe("Failed to connect to database: " + e.getMessage());
```

**Output:**
```
[INFO] Plugin enabled successfully!
[WARNING] Configuration file not found, using defaults
[SEVERE] Failed to connect to database: Connection timeout
```

### After (Component Logging)

```java
logger.info("<green>Plugin enabled successfully!</green>");
logger.warn("<yellow>Configuration file not found, using defaults</yellow>");
logger.error("<red>Failed to connect to database: {}</red>", e.getMessage(), e);
```

**Output (with colors):**
```
[INFO] Plugin enabled successfully!          (green text)
[WARNING] Configuration file not found, using defaults  (yellow text)
[ERROR] Failed to connect to database: Connection timeout  (red text)
```

The colored output is much easier to scan and visually identify different types of messages!

## Advanced Usage

### Custom Component Building

For complex log messages, build Components programmatically:

```java
Component message = Component.text()
    .append(Component.text("[", NamedTextColor.GRAY))
    .append(Component.text("Plugin", NamedTextColor.AQUA))
    .append(Component.text("] ", NamedTextColor.GRAY))
    .append(Component.text("Loaded ", NamedTextColor.GREEN))
    .append(Component.text(count, NamedTextColor.WHITE))
    .append(Component.text(" items", NamedTextColor.GREEN))
    .build();

logger.info(message);
```

### Dynamic Colors Based on Conditions

```java
String status = isHealthy ? "<green>Healthy</green>" : "<red>Unhealthy</red>";
logger.info("<aqua>[Status]</aqua> System status: {}", status);
```

## Thread Safety

`ComponentLoggerWrapper` is thread-safe and can be used from any thread. The underlying `ComponentLogger` handles thread safety internally.

## Performance Considerations

- Component serialization to ANSI is fast and handled efficiently by Paper
- MiniMessage parsing is cached by your `TextEngine`
- Component logging has negligible performance overhead compared to traditional logging

## Troubleshooting

### Colors Not Showing

If colors aren't appearing in your console:
1. Check that your terminal/console supports ANSI escape codes
2. Verify you're using Paper (ComponentLogger requires Paper)
3. Some log viewers may strip ANSI codes - check your log viewer settings

### MiniMessage Not Parsing

If MiniMessage isn't being parsed:
1. Verify your `TextEngine` is configured correctly
2. Check that the MiniMessage string is valid
3. Ensure you're using the wrapper methods, not the underlying logger directly

## API Reference

See the Javadoc for detailed API documentation:
- `ComponentLoggerWrapper` - Wrapper class with MiniMessage support
- Paper's `ComponentLogger` - The underlying logging interface
- `TextEngine` - For MiniMessage parsing configuration

