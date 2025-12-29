# MichelleLib 🌸

An advanced, comprehensive framework for Paper plugin development. Built with the latest Paper techniques and best practices, MichelleLib provides developers with powerful abstractions and utilities for creating high-quality Minecraft plugins.

Named after someone special.

This library is actively maintained and designed for developers who need robust, thread-safe, and Folia-compatible solutions. Paper-only.

MichelleLib should be shaded into your plugin's JAR file and can be used by multiple plugins simultaneously. It does not require plugin lifecycle management.

## Features

- **Advanced Menu System**: High-level menu definition API with lifecycle hooks, pattern-based layouts, navigation, and back stack support
- **Folia-Safe Scheduling**: Paper-first scheduler abstraction supporting Entity, Region, Global, and Async schedulers
- **Paper Brigadier Commands**: Reload-safe `LifecycleEvents.COMMANDS` registration + a compact DSL for building command trees
- **Thread Safety**: All components are designed with Folia compatibility in mind
- **Modern Paper APIs**: Uses the latest Paper techniques and recommended patterns
- **Common Dependencies**: Includes support for commonly used dependencies like PlaceholderAPI, NexoMC, and WorldGuard

## Requirements

- Paper (latest Minecraft version)
- Java 21

## Usage

Add MichelleLib as a dependency in your plugin's `pom.xml` and shade it into your plugin:

```xml
<dependency>
    <groupId>io.voluble.michelle</groupId>
    <artifactId>michelle-lib</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

## Documentation

- [Menus](docs/menus.md) - Complete guide to the menu system, including API reference, patterns, and best practices
- [Commands](docs/commands.md) - Brigadier command DSL + registration utilities for the Paper command API

## License

This project is licensed under the MIT License (Non-Commercial). You are free to use, modify, and distribute this software for non-commercial purposes. Commercial use, including selling the software or any derivative works, is prohibited without express written permission.

See [LICENSE](LICENSE) for full details.

---

*This project uses AI assistance for generating code comments and markdown documentation files.*

