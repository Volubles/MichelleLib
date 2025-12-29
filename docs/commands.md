# Commands (Paper Brigadier)

MichelleLib includes a small, shade-friendly command toolbox built on top of **Paper’s Brigadier command API**
(`io.papermc.paper.command.brigadier`) and **Paper’s reload-safe lifecycle registration**
(`LifecycleEvents.COMMANDS`).

## Installing

In your plugin `onEnable()`:

```java
new CommandKit(this)
    .addModule(registration -> {
        registration.register(
            registration.tree("hello")
                .description("Says hello.")
                .literalExec("world", ctx -> {
                    ctx.sender().sendRichMessage("<green>Hello, world!");
                    return CommandTree.ok();
                })
                .spec()
        );
    })
    .install();
```

You can also use `CommandService.create(plugin)` if you prefer the "service" naming.

## Key APIs

- **`io.voluble.michellelib.commands.CommandKit` / `CommandService`**: installs `LifecycleEvents.COMMANDS` and runs your modules
- **`io.voluble.michellelib.commands.CommandRegistration`**: wrapper around Paper `Commands` registrar
- **`io.voluble.michellelib.commands.tree.CommandTree`**: compact DSL for Brigadier trees
- **`io.voluble.michellelib.commands.arguments.Args`**: factories for Brigadier + Paper `ArgumentTypes`
- **`io.voluble.michellelib.commands.arguments.Resolve`**: resolves selector-style Paper arguments into Bukkit objects
- **`io.voluble.michellelib.commands.suggest.Suggest`**: convenience `SuggestionProvider` helpers


