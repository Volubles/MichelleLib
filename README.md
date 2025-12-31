# MichelleLib 🌸

An advanced, comprehensive framework for Paper plugin development. Built with the latest Paper techniques and best practices, MichelleLib provides developers with powerful abstractions and utilities for creating high-quality Minecraft plugins.

Named after someone special.

This library is actively maintained and designed for developers who need robust, thread-safe, and Folia-compatible solutions. Paper-only.

MichelleLib should be shaded into your plugin's JAR file and can be used by multiple plugins simultaneously. It does not require plugin lifecycle management.

## Features

- **Advanced Particle Effects API**: Comprehensive particle system with presets, patterns, and full Paper API compatibility without limitations
- **Paper Dialogs**: Enhanced facade for building and managing Paper dialogs with safety defaults, input validation, workflow helpers, and common presets
- **Registry Access Abstractions**: Advanced registry utilities with streaming, caching, tag support, and fluent query APIs
- **Advanced Menu System**: High-level menu definition API with lifecycle hooks, pattern-based layouts, navigation, and back stack support
- **Folia-Safe Scheduling**: Paper-first scheduler abstraction supporting Entity, Region, Global, and Async schedulers
- **Paper Brigadier Commands**: Reload-safe `LifecycleEvents.COMMANDS` registration + a compact DSL for building command trees
- **YAML Configs & Messages**: Nested YAML files with default resource copying + thread-safe `messages.yml` snapshot with MiniMessage parsing
- **Cooldowns**: Folia-safe, atomic per-player cooldowns with optional YAML persistence
- **Thread Safety**: All components are designed with Folia compatibility in mind
- **Modern Paper APIs**: Uses the latest Paper techniques and recommended patterns
- **Common Dependencies**: Includes support for commonly used dependencies like PlaceholderAPI, NexoMC, and WorldGuard

## Requirements

- Paper (latest Minecraft version)
- Java 21

## Usage

Add MichelleLib as a dependency in your plugin's `pom.xml` and shade it into your plugin:

```xml
<dependencies>
    <dependency>
        <groupId>io.voluble.michelle</groupId>
        <artifactId>michelle-lib</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

Then configure the `maven-shade-plugin` to shade MichelleLib into your plugin JAR:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.3</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>io.voluble.michellelib</pattern>
                                <shadedPattern>your.plugin.package.lib.michellelib</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Important**: Replace `your.plugin.package.lib.michellelib` with your actual package name to avoid conflicts when multiple plugins use MichelleLib. For example, if your plugin is `com.example.myplugin`, use `com.example.myplugin.lib.michellelib`.

After shading, MichelleLib will be embedded directly in your plugin JAR - it won't need to be placed in the `plugins` folder separately.

## Documentation

- [Particle Effects](docs/particles.md) - Advanced particle effects API with presets, patterns, and full Paper API compatibility
- [Dialogs](docs/dialogs.md) - Enhanced Paper dialog API facade with helpers for building dialogs, handling inputs, workflows, and managing responses
- [Registry Access](docs/registries.md) - Advanced registry utilities with streaming, caching, tag support, and fluent query APIs
- [Menus](docs/menus.md) - Complete guide to the menu system, including API reference, patterns, and best practices
- [Commands](docs/commands.md) - Brigadier command DSL + registration utilities for the Paper command API
- [YAML Configs & Messages](docs/config.md) - YAML file loading utilities + `messages.yml` MiniMessage service
- [Cooldowns](docs/cooldowns.md) - Folia-safe cooldown service with optional persistence
- [Chat Formatting](docs/chat.md) - Abstraction over Paper's AsyncChatEvent and ChatRenderer for easy chat formatting
- [Console Logging](docs/console-logging.md) - ComponentLogger wrapper for beautiful, colored console logs with MiniMessage
- [Item Data Components](docs/item-data.md) - Utilities for working with Paper's data component API for item manipulation

## License

This project is licensed under the MIT License (Non-Commercial). You are free to use, modify, and distribute this software for non-commercial purposes. Commercial use, including selling the software or any derivative works, is prohibited without express written permission.

See [LICENSE](LICENSE) for full details.

---

*This project uses AI assistance for generating code comments and markdown documentation files.*

