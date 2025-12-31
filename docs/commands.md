# MichelleLib Commands (Paper Brigadier DSL)

This document explains the **command system** in MichelleLib and shows **best-practice patterns** for building Brigadier commands on modern Paper.

It focuses on:
- **Reload-safe registration**: using Paper's `LifecycleEvents.COMMANDS` for automatic re-registration
- **Clean DSL**: a compact, readable API that avoids Brigadier's verbose generics
- **Type safety**: typed arguments with client-side validation and suggestions
- **Thread safety**: correct handling of command execution and async operations

---

## Table of Contents

1. [Mental Model](#mental-model-important)
2. [Quick Start](#quick-start)
3. [Your First Command](#your-first-command)
4. [Subcommands](#subcommands-tree-shape)
5. [Arguments](#arguments-typed-no-manual-parsing)
6. [Requirements & Permissions](#requirements-permissions--predicates)
7. [Suggestions](#suggestions-for-arguments)
8. [Custom Arguments](#custom-arguments-reusable-parsing--suggestions)
9. [Paper Argument Types](#minecraftpaper-arguments-without-pain-args--resolve)
10. [Command Errors](#command-errors-in-one-line-commanderrors)
11. [Organizing Commands](#organizing-commands-modules)
12. [Threading & Safety](#threading-and-safety-critical-paper-dev-notes)
13. [Advanced Patterns](#advanced-patterns)
14. [Best Practices](#practical-best-practices)
15. [Troubleshooting](#troubleshooting)
16. [FAQ](#faq)

---

## Mental model (important)

### What you are building

MichelleLib commands are Brigadier command trees implemented with:
- A per-plugin `CommandService` (instance-based, no static singleton)
- A modular `CommandModule` system (one module per feature area)
- A high-level `CommandTree` DSL that *builds* Brigadier nodes without verbose generics

### The registration flow

```
1. CommandService.create(plugin)
   ↓
2. service.addModule(new MyCommandsModule())
   ↓
3. service.install() (registers LifecycleEvents.COMMANDS handler)
   ↓
4. Paper calls handler → CommandRegistration created
   ↓
5. Each module's register() method runs
   ↓
6. Commands are registered with Paper's Commands registrar
```

### Why LifecycleEvents.COMMANDS matters

Paper's Brigadier registration is designed to handle "commands need to be rebuilt" moments:
- `/reload`-like flows
- Datapack command parsing contexts
- Internal dispatcher refreshes

By registering via `LifecycleEvents.COMMANDS`, your commands are **re-registered when Paper expects them to be**, so you don't build your own brittle "onReload" logic.

---

## Quick start

### 1) Create `CommandService` in your plugin

```java
public final class MyPlugin extends JavaPlugin {
  private CommandService commands;

  @Override
  public void onEnable() {
    commands = CommandService.create(this);
    commands.addModule(new AdminCommands());
    commands.addModule(new TeleportCommands());
    commands.install();
  }
}
```

Or use the simpler `CommandKit` wrapper:

```java
@Override
public void onEnable() {
  new CommandKit(this)
    .addModule(new AdminCommands())
    .addModule(new TeleportCommands())
    .install();
}
```

### 2) Create a command module

```java
public final class AdminCommands implements CommandModule {
  @Override
  public void register(CommandRegistration registration) {
    registration.register(
      CommandTree.root("admin")
        .description("Admin commands")
        .requiresPermission("myplugin.admin")
        .literalExec("reload", ctx -> {
          // Reload logic
          ctx.sender().sendRichMessage("<green>Reloaded!");
          return CommandTree.ok();
        })
        .spec()
    );
  }
}
```

That's it! Your command is registered and reload-safe.

---

## Your first command

A simple command that says hello:

```java
registration.register(
  CommandTree.root("hello")
    .description("Says hello.")
    .literalExec("world", ctx -> {
        ctx.sender().sendRichMessage("<green>Hello, world!");
        return CommandTree.ok();
    })
    .spec()
);
```

Notes:
- `description(...)` is used for Bukkit-style help text
- `literalExec(...)` exists to avoid Java overload ambiguity between "child builder lambdas" and "executor lambdas"
- `CommandTree.ok()` returns `Command.SINGLE_SUCCESS` (the standard success return value)

---

## Subcommands (tree shape)

Build command trees with multiple subcommands:

```
/admin reload
/admin tphere
/admin killall
```

```java
registration.register(
  CommandTree.root("admin")
    .description("Admin commands")
    .literalExec("reload", ctx -> {
        // Reload logic
        return CommandTree.ok();
    })
    .literalExec("tphere", ctx -> {
        // Teleport logic
        return CommandTree.ok();
    })
    .literalExec("killall", ctx -> {
        // Kill logic
        return CommandTree.ok();
    })
    .spec()
);
```

You can also nest subcommands:

```java
CommandTree.root("admin")
  .literal("user", userNode -> userNode
    .literalExec("ban", ctx -> { /* ... */ })
    .literalExec("kick", ctx -> { /* ... */ })
  )
  .literalExec("reload", ctx -> { /* ... */ })
```

---

## Arguments (typed, no manual parsing)

Example: `/flyspeed <speed>` with `0.0 - 1.0` bounds.

```java
registration.register(
  CommandTree.root("flyspeed")
    .description("Set your flight speed (0.0 - 1.0).")
    .argumentExec("speed", FloatArgumentType.floatArg(0.0f, 1.0f), ctx -> {
        final float speed = FloatArgumentType.getFloat(ctx.brigadier(), "speed");

        if (!(ctx.executor() instanceof Player player)) {
            ctx.sender().sendRichMessage("<red>Only players can fly.");
            return CommandTree.ok();
        }

        player.setFlySpeed(speed);
        player.sendRichMessage("<green>Set flyspeed to <white>" + speed + "</white>.");
        return CommandTree.ok();
    })
    .spec()
);
```

Why this is powerful:
- The **client validates** out-of-range input before sending the command
- Your code receives a guaranteed-valid float
- No manual parsing or error checking required

### Accessing arguments

Use `ctx.arg(name, type)` to get typed arguments:

```java
final String name = ctx.arg("name", String.class);
final int amount = ctx.arg("amount", int.class);
final Player player = ctx.arg("player", PlayerSelectorArgumentResolver.class);
```

---

## Requirements (permissions & predicates)

### Require a permission

```java
CommandTree.root("admin")
  .requiresPermission("myplugin.admin")
  .literalExec("reload", ctx -> {
      // ...
      return CommandTree.ok();
  });
```

### Custom predicate

```java
.requires(source -> source.getSender().isOp())
```

### Restricted execution

Restricted execution matches vanilla "confirm" behavior for sensitive commands:

```java
.restricted(source -> source.getSender().hasPermission("myplugin.dangerous"))
```

This wraps the predicate with Paper's `Commands.restricted(...)`.

### Built-in shortcuts

```java
.requiresOp()                    // Requires operator
.requiresConsoleSender()          // Requires console
.requiresPlayerExecutor()         // Requires player executor
```

---

## Suggestions (for arguments)

There are two main approaches:

1. **Use native Paper/Minecraft argument types** (best client UX)
   - Many arguments already provide suggestions automatically (players, items, etc.)
   - See Paper's `ArgumentTypes` for "minecraft arguments"

2. **Attach custom suggestions** (when you need domain-specific values)

### Suggesting strings with prefix filtering

For quick, static suggestions:

```java
CommandTree.root("selectname")
  .argument("name", StringArgumentType.word(), node -> node
    .suggestStrings(List.of("Alex", "Andreas", "Stephanie", "Sophie", "Emily"))
    .executes(ctx -> {
        final String name = ctx.arg("name", String.class);
        ctx.sender().sendRichMessage("<green>Selected <white>" + name + "</white>.");
        return CommandTree.ok();
    })
  );
```

This uses `CommandTree.ArgumentNode#suggestStrings(...)` which performs a simple `startsWith(...)` filter against the currently typed input.

### Full control with Brigadier SuggestionProvider

If you need tooltips, async, or complex filtering, use a `SuggestionProvider<CommandSourceStack>`:

```java
.suggests((ctx, builder) -> {
    builder.suggest("first");
    builder.suggest("second");
    return builder.buildFuture();
})
```

### Using the toolbox: `Suggest`

```java
CommandTree.root("giveamount")
  .argument("amount", Args.integer(1, 99), node -> node
    .suggests(Suggest.ints(1, 16, 32, 64))
    .executes(ctx -> {
        final int amount = ctx.arg("amount", int.class);
        ctx.sender().sendRichMessage("<green>Amount: <white>" + amount + "</white>.");
        return CommandTree.ok();
    })
  );
```

Available helpers:
- `Suggest.strings(...)` - String suggestions with prefix filtering
- `Suggest.ints(...)` - Integer suggestions
- `Suggest.onlinePlayers()` - Online player names
- `Suggest.enums(EnumClass.class)` - Enum value suggestions

---

## Custom arguments (reusable parsing + reusable suggestions)

The fastest way to reduce "command tree bloat" is to turn common argument patterns into **CustomArgumentType** implementations.

MichelleLib ships one reusable primitive:

- `io.voluble.michellelib.commands.arguments.EnumArgument<E>`

### EnumArgument (example)

```java
public enum IceCreamFlavor {
  VANILLA, CHOCOLATE, STRAWBERRY;
}

// In your module:
registration.register(
  CommandTree.root("icecream")
    .argument("flavor", registration.enumArgument(IceCreamFlavor.class), node -> node
      .executes(ctx -> {
          final IceCreamFlavor flavor = ctx.arg("flavor", IceCreamFlavor.class);
          ctx.sender().sendRichMessage("<green>You chose <aqua>" + flavor + "</aqua>.");
          return CommandTree.ok();
      })
    )
    .spec()
);
```

`EnumArgument`:
- Uses `StringArgumentType.word()` as the **native** type (client-friendly baseline parsing)
- Converts to the enum constant server-side (so you retrieve it as a real enum from `ctx.arg(...)`)
- Suggests available values with prefix filtering

**Rule of thumb:** if you find yourself copy/pasting the same "parse + validate + suggest" block twice, it's time for a custom argument.

---

## Minecraft/Paper arguments without pain: `Args` + `Resolve`

Paper's `ArgumentTypes` are extremely powerful, but some (notably selectors) return a *resolver* that must be resolved against the `CommandSourceStack`.

MichelleLib adds:
- `Args.*()` factories (so you don't need to memorize/import `ArgumentTypes`)
- `Resolve.*()` helpers to turn resolver arguments into real `Player` / `Entity` objects

### Example: `/whois <player>`

```java
registration.register(
  CommandTree.root("whois")
    .description("Show player information")
    .argumentExec("player", Args.player(), ctx -> {
        final Player player = Resolve.player(ctx, "player");
        ctx.sender().sendRichMessage("<green>Resolved player: <white>" + player.getName() + "</white>.");
        return CommandTree.ok();
    })
    .spec()
);
```

### Available `Args` factories

**Brigadier primitives:**
- `Args.bool()`
- `Args.integer()`, `Args.integer(min)`, `Args.integer(min, max)`
- `Args.floatArg()`, `Args.floatArg(min, max)`
- `Args.doubleArg()`
- `Args.word()`, `Args.string()`, `Args.greedyString()`

**Paper/Minecraft arguments:**
- `Args.player()`, `Args.players()` - Player selectors
- `Args.entity()`, `Args.entities()` - Entity selectors
- `Args.itemStack()` - Item stack
- `Args.blockState()` - Block state
- `Args.blockPosition()`, `Args.finePosition()` - Positions
- `Args.world()` - World
- `Args.gameMode()` - Game mode
- `Args.namespacedKey()` - Namespaced key
- And many more (see `Args` class for full list)

### Available `Resolve` helpers

- `Resolve.player(ctx, "argName")` - Single player
- `Resolve.players(ctx, "argName")` - List of players
- `Resolve.entity(ctx, "argName")` - Single entity
- `Resolve.entities(ctx, "argName")` - List of entities
- `Resolve.blockPosition(ctx, "argName")` - Block position
- `Resolve.finePosition(ctx, "argName")` - Fine position
- `Resolve.rotation(ctx, "argName")` - Rotation

---

## Cleaner "player-only" executors: `executesPlayer(...)`

This removes the repeated `instanceof Player` blocks:

```java
CommandTree.root("mycmd")
  .executesPlayer((player, ctx) -> {
      player.sendRichMessage("<green>You are a player.");
      return CommandTree.ok();
  });
```

If the executor is not a player, it throws a command error (`Only players can use this command.`).

---

## Command errors in one line: `CommandErrors`

Instead of verbose exception construction:

```java
throw CommandErrors.failText("Only players can do that.");
```

Or with a Component:

```java
throw CommandErrors.fail(Component.text("Only players can do that."));
```

When to prefer "return ok + send message" vs throwing?
- **Throw** when you want Brigadier-style "command failed" behavior
- **Send + return ok** when you want "soft failure" UX that still provides feedback

---

## Organizing commands (modules)

Instead of one giant "Commands.java" class, use modules:

- One module per feature area (admin, economy, moderation, etc.)
- Each module registers multiple roots

```java
new CommandKit(this)
  .addModule(new AdminCommands())
  .addModule(new TeleportCommands())
  .addModule(new EconomyCommands())
  .install();
```

This scales cleanly while keeping the registration lifecycle correct.

### Example module structure

```java
public final class AdminCommands implements CommandModule {
  private final MyPlugin plugin;
  
  public AdminCommands(MyPlugin plugin) {
    this.plugin = plugin;
  }
  
  @Override
  public void register(CommandRegistration registration) {
    registration.register(reloadCommand(registration).spec());
    registration.register(banCommand(registration).spec());
  }
  
  private CommandTree.Root reloadCommand(CommandRegistration registration) {
    return CommandTree.root("admin")
      .description("Reload the plugin")
      .requiresPermission("myplugin.admin.reload")
      .literalExec("reload", ctx -> {
        plugin.reload();
        ctx.sender().sendRichMessage("<green>Reloaded!");
        return CommandTree.ok();
      });
  }
  
  private CommandTree.Root banCommand(CommandRegistration registration) {
    return CommandTree.root("ban")
      .description("Ban a player")
      .requiresPermission("myplugin.admin.ban")
      .argumentExec("player", Args.player(), ctx -> {
        final Player target = Resolve.player(ctx, "player");
        // Ban logic
        return CommandTree.ok();
      });
  }
}
```

---

## Threading and safety (critical Paper dev notes)

### Command execution

- Command execution happens on the **main thread**
- Do not block (database, web requests, long disk IO)

If you must do expensive work:
- Capture the minimal state you need
- Offload work async
- Come back to the main thread to touch Bukkit API

Example:

```java
.executes(ctx -> {
    final Player player = (Player) ctx.executor();
    final String targetName = ctx.arg("target", String.class);
    
    // Offload expensive lookup
    Bukkit.getAsyncScheduler().runNow(plugin, task -> {
        // Do database lookup, web request, etc.
        final Player target = Bukkit.getPlayer(targetName);
        
        // Come back to main thread for Bukkit API
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target != null && target.isOnline()) {
                player.teleport(target);
                player.sendRichMessage("<green>Teleported!");
            }
        });
    });
    
    return CommandTree.ok();
})
```

### Suggestions

Suggestion providers can return a `CompletableFuture<Suggestions>`.

- If you build suggestions synchronously and return `builder.buildFuture()`, you are on the main thread
- If you use `CompletableFuture.supplyAsync(...)`, you are likely off-thread and must not call most Bukkit API there

---

## Advanced patterns

### Dropping down to Brigadier

Every node wrapper exposes the real Brigadier builder:

```java
final ArgumentBuilder<CommandSourceStack, ?> raw = node.brigadier();
```

And registration exposes Paper's `Commands` registrar:

```java
registration.paperCommands().getDispatcher(); // advanced use cases only
```

Use this sparingly. Most plugins should stick to `registration.register(...)` APIs to keep namespacing/help/plugin association correct.

### BasicCommand (Bukkit-style `String[] args`)

If you want a "simple" command that accepts arbitrary text:

```java
registration.registerBasic("broadcast", (stack, args) -> {
    final String msg = String.join(" ", args);
    stack.getSender().sendRichMessage("<gold>[Broadcast]</gold> " + msg);
});
```

This is useful for very simple text commands, but you lose:
- Typed parsing
- Structured subcommands
- Client-side validation

### Fixing client/server command visibility mismatch

Paper's recommendation for fixing "requires(...) but client still shows command" is `Player#updateCommands()` (thread-safe).

```java
CommandClientSync.updateCommands(player);
```

Use sparingly (bandwidth), and only when you changed something that affects visibility.

---

## Practical best practices

- **Prefer smaller roots** over "one mega root with 40 subcommands" unless it's a true "plugin root"
- **Keep executors tiny**:
  - Parse args
  - Validate sender/executor
  - Call a separate service method
- **Custom arguments** are the #1 tool for removing duplication
- **Avoid static plugin singletons**. Pass dependencies into modules/constructors
- **Always send feedback** on success and failure (use `CommandSender#sendRichMessage(...)`)
- **Use `executesPlayer(...)`** to avoid repetitive `instanceof Player` checks
- **Organize by feature**, not by command name (one module per feature area)

---

## Troubleshooting

### "Command not showing up"

- Make sure you called `service.install()` or `kit.install()`
- Check that your module's `register()` method is being called
- Verify the command isn't being filtered by `requires(...)` predicates
- Use `CommandClientSync.updateCommands(player)` if permissions changed dynamically

### "Command works but client doesn't show it"

This is normal! `.requires(...)` is evaluated server-side. The client may still display a command branch until it receives an updated command tree. Use `CommandClientSync.updateCommands(player)` sparingly if needed.

### "Argument parsing fails"

- Check that you're using the correct argument type for your use case
- Verify argument bounds (min/max) if using ranged types
- For custom arguments, ensure your `convert()` method handles all valid inputs

### "Suggestions not working"

- Make sure you're calling `.suggests(...)` on the correct argument node
- Check that your suggestion provider returns a `CompletableFuture<Suggestions>`
- Verify you're not calling Bukkit API from async suggestion providers

---

## FAQ

### "Do I need to declare commands in plugin.yml?"

No! Brigadier commands registered through Paper's `Commands` registrar do **not** require listing in `plugin.yml`. That's one of the reasons this system reduces boilerplate compared to classic Bukkit commands.

### "Can I use this with other command libraries?"

MichelleLib is designed to be shaded into your plugin. It should work alongside other libraries as long as they don't conflict on command registration. However, using multiple command systems can cause confusion—prefer one system per plugin.

### "How do I test commands?"

- Use mock `CommandSender` instances in unit tests
- Test argument parsing and validation
- Test permission requirements
- Test suggestion providers
- Verify thread safety in concurrent scenarios

### "What's the performance impact?"

- Command registration is lightweight (mostly just tree building)
- Command execution is on the main thread (no extra overhead)
- Suggestion providers can be async if needed
- Default settings are tuned for good performance

### "Can I reload commands?"

Yes! Commands are automatically re-registered when Paper's `LifecycleEvents.COMMANDS` fires (e.g., during `/reload`). You don't need to manually handle reloads.

### "How do I handle command aliases?"

Use `.aliases(...)` on the root:

```java
CommandTree.root("admin")
  .aliases("a", "adm")
  .description("Admin commands")
  // ...
```

---

**Happy command building!** 🎨

*This file was created with the help of AI to improve documentation clarity.*