# MichelleLib Dialogs (Paper Dialog API)

This document explains the **dialog system** in MichelleLib and shows **best-practice patterns** for building and managing Paper dialogs.

It focuses on:
- **Ergonomic API**: Safety-first helpers that sit on top of Paper's dialog API
- **Input validation**: Automatic bounds checking, sanitization, and safe defaults
- **Flexibility**: Support for all dialog types with sensible presets and full customization
- **Thread safety**: Correct handling of dialogs during configuration phase and gameplay

---

## Table of Contents

1. [Mental Model](#mental-model-important)
2. [Quick Start](#quick-start)
3. [Dialog Types](#dialog-types)
4. [Input Types](#input-types)
5. [Building Dialogs](#building-dialogs)
6. [Registry Registration](#registry-registration)
7. [Handling Responses](#handling-responses)
8. [Advanced Patterns](#advanced-patterns)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## Mental Model (Important)

### What you are building

MichelleLib dialogs are Paper dialogs with a safety-focused facade. The `Dialogs` class provides:
- **Preset builders**: Common dialog patterns (notice, confirmation, multi-action)
- **Input helpers**: Validated input creation with automatic bounds checking
- **Safe extraction**: Null-safe utilities for reading dialog responses
- **Key management**: Namespaced key generation for dialog actions

### The two approaches

- **Dynamic creation**: Build dialogs on-demand for player-specific content
- **Registry registration**: Register dialogs during bootstrap for reuse across commands/listeners

---

## Quick Start

### Minimal notice dialog

```java
import io.voluble.michellelib.dialog.Dialogs;
import io.voluble.michellelib.text.TextEngine;
import net.kyori.adventure.text.Component;

Dialog dialog = Dialogs.notice(
    Component.text("Welcome!"),
    Component.text("Thanks for joining our server.")
);
player.showDialog(dialog);
```

### Confirmation dialog with callbacks

```java
Key yesKey = Dialogs.key("rules/agree");
Key noKey = Dialogs.key("rules/decline");

Dialog dialog = Dialogs.confirmation(
    Component.text("Accept Rules?"),
    Component.text("Please accept the rules to continue."),
    yesKey,
    noKey
);

Dialogs.show(player, dialog, plugin.getLogger(), "rules");
```

---

## Dialog Types

### Notice Dialog

Simple informational dialog with an OK button.

```java
// Basic notice
Dialog dialog = Dialogs.notice(title, body);

// Notice with custom button
ActionButton customButton = Dialogs.callbackButton(
    Component.text("Got it!"),
    (view, audience) -> {
        // Handle click
    }
);
Dialog dialog = Dialogs.notice(title, body, customButton);
```

### Confirmation Dialog

Two-button dialog for yes/no decisions.

```java
// Basic confirmation (waits for response, escape disabled)
Dialog dialog = Dialogs.confirmation(title, body, yesKey, noKey);

// Full customization
Dialog dialog = Dialogs.confirmation(
    title,
    body,
    yesButton,
    noButton,
    canCloseWithEscape,
    DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE
);
```

### Multi-Action Dialog

Hub dialog with multiple action buttons.

```java
List<ActionButton> actions = List.of(
    Dialogs.callbackButton(Component.text("Option 1"), handler1),
    Dialogs.callbackButton(Component.text("Option 2"), handler2),
    Dialogs.callbackButton(Component.text("Option 3"), handler3)
);

Dialog dialog = Dialogs.multiAction(
    Component.text("Main Menu"),
    actions,
    3  // columns
);
```

### Dialog List

Navigation hub linking to other registered dialogs.

```java
RegistrySet<Dialog> dialogs = RegistrySet.create(Set.of(
    Key.key("plugin:settings"),
    Key.key("plugin:profile"),
    Key.key("plugin:help")
));

ActionButton exitButton = Dialogs.callbackButton(
    Component.text("Back"),
    (view, audience) -> audience.closeDialog()
);

Dialog dialog = Dialogs.dialogList(
    Component.text("Navigation"),
    dialogs,
    exitButton,
    2,      // columns
    150     // button width
);
```

### Server Links

Dialog for displaying server links (retrieved from `Bukkit.getServer().getServerLinks()`).

```java
Dialog dialog = Dialogs.serverLinks(
    Component.text("Server Links"),
    exitButton,
    2,      // columns
    150     // button width
);
```

---

## Input Types

### Text Input

Single-line text field with validation.

```java
DialogInput textInput = Dialogs.textBox(
    "player_name",              // key
    Component.text("Name"),    // label
    300,                        // width
    32,                         // maxLength
    ""                          // initial value
);
```

### Number Slider

Range slider with step validation.

```java
DialogInput slider = Dialogs.numberSlider(
    "exp_percent",              // key
    Component.text("EXP %"),   // label
    0f,                         // start
    100f,                       // end
    50f,                        // initial (clamped to range)
    1f,                         // step
    300                         // width
);
```

### Single Choice

Radio button selection with custom labels.

```java
// Simple: string IDs with auto-generated labels
DialogInput choice = Dialogs.singleChoice(
    "mode",                     // key
    Component.text("Mode"),     // label
    List.of("easy", "normal", "hard"),  // option IDs
    "normal",                   // initial ID
    300                         // width
);

// Advanced: custom option components
List<SingleOptionDialogInput.OptionEntry> options = List.of(
    SingleOptionDialogInput.OptionEntry.create("easy", Component.text("Easy Mode"), false),
    SingleOptionDialogInput.OptionEntry.create("normal", Component.text("Normal Mode"), true),
    SingleOptionDialogInput.OptionEntry.create("hard", Component.text("Hard Mode"), false)
);

DialogInput choice = Dialogs.singleChoiceWithComponents(
    "mode",
    Component.text("Mode"),
    options,
    300
);
```

### Boolean Input

Checkbox with template values.

```java
// With defaults ("true"/"false")
DialogInput checkbox = Dialogs.boolInput(
    "accept_terms",             // key
    Component.text("Accept Terms"),
    false                       // initial
);

// With custom template values
DialogInput checkbox = Dialogs.boolInput(
    "notifications",
    Component.text("Enable Notifications"),
    true,
    "enabled",
    "disabled"
);
```

---

## Building Dialogs

### Dialog Base

The base defines the dialog's structure: title, body, inputs, and behavior.

```java
DialogBase base = DialogBase.builder(Component.text("Title"))
    .body(List.of(
        DialogBody.plainMessage(Component.text("Body text")),
        Dialogs.itemBody(itemStack)  // Display an item
    ))
    .inputs(List.of(
        Dialogs.textBox("name", Component.text("Name"), 300, 32, ""),
        Dialogs.numberSlider("level", Component.text("Level"), 1f, 100f, 1f, 1f, 300)
    ))
    .canCloseWithEscape(false)
    .pause(true)  // Single-player only
    .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
    .build();
```

### Dialog Body

Dialogs can contain multiple body elements:

```java
List<DialogBody> body = List.of(
    DialogBody.plainMessage(Component.text("Line 1")),
    DialogBody.plainMessage(Component.text("Line 2")),
    Dialogs.itemBody(itemStack)  // Display an item with default settings
);
```

### Action Buttons

Buttons can use callbacks or command templates.

```java
// Callback button (local handler, uses=1)
ActionButton callbackBtn = Dialogs.callbackButton(
    Component.text("Apply"),
    (view, audience) -> {
        Float value = Dialogs.getFloat(view, "slider");
        if (value != null && audience instanceof Player p) {
            // Apply value
        }
    }
);

// Command template button
ActionButton commandBtn = Dialogs.commandButton(
    Component.text("Teleport"),
    "tp $(target) $(x) $(y) $(z)",  // Template with input substitution
    150
);
```

### Complete Example

```java
DialogInput expInput = Dialogs.numberSlider(
    "exp", Component.text("EXP %"), 0f, 100f, 0f, 1f, 300
);

ActionButton applyButton = Dialogs.callbackButton(
    Component.text("Apply"),
    (view, audience) -> {
        Float exp = Dialogs.getFloat(view, "exp");
        if (exp != null && audience instanceof Player p) {
            p.setExp(Math.max(0f, Math.min(1f, exp / 100f)));
        }
    }
);

ActionButton cancelButton = ActionButton.create(
    Component.text("Cancel"),
    null,
    100,
    null
);

Dialog dialog = Dialog.create(b -> b.empty()
    .base(DialogBase.builder(Component.text("Set EXP"))
        .inputs(List.of(expInput))
        .build())
    .type(DialogType.confirmation(applyButton, cancelButton))
);

Dialogs.show(player, dialog, plugin.getLogger(), "exp-set");
```

---

## Registry Registration

Register dialogs during bootstrap for reuse and config-phase access.

```java
@Override
public void bootstrap(BootstrapContext ctx) {
    ctx.getLifecycleManager().registerEventHandler(
        RegistryEvents.DIALOG.compose(),
        e -> e.registry().register(
            DialogKeys.create(Key.key("plugin:welcome")),
            builder -> Dialogs.register(builder, b -> b
                .base(DialogBase.builder(Component.text("Welcome!"))
                    .body(List.of(DialogBody.plainMessage(Component.text("Welcome to the server!"))))
                    .build())
                .type(DialogType.notice())
            )
        )
    );
}
```

Fetch registered dialogs:

```java
Dialog dialog = Dialogs.fromRegistry("plugin:welcome");
if (dialog != null) {
    player.showDialog(dialog);
}
```

---

## Handling Responses

### Safe Extraction

Use the safe extractors to avoid null pointer exceptions:

```java
@EventHandler
public void onClick(PlayerCustomClickEvent event) {
    if (!(event.getCommonConnection() instanceof PlayerConfigurationConnection conn)) {
        return;
    }
    
    Key key = event.getIdentifier();
    if (key.equals(Dialogs.key("rules/agree"))) {
        // Handle agreement
    }
}

// In callback handlers
ActionButton button = Dialogs.callbackButton(
    Component.text("Submit"),
    (view, audience) -> {
        String name = Dialogs.getText(view, "name");
        Float level = Dialogs.getFloat(view, "level");
        Boolean enabled = Dialogs.getBool(view, "enabled");
        
        if (name != null && level != null && audience instanceof Player p) {
            // Process input
        }
    }
);
```

### Config-Phase Blocking

Block player connection until dialog is acknowledged:

```java
public final class RulesGatekeeper implements Listener {
    private final Map<UUID, CompletableFuture<Boolean>> pending = new ConcurrentHashMap<>();
    private static final Key AGREE = Dialogs.key("rules/agree");
    private static final Key DECLINE = Dialogs.key("rules/decline");

    @EventHandler
    public void onConfigure(AsyncPlayerConnectionConfigureEvent event) {
        Dialog dialog = Dialogs.fromRegistry("plugin:rules_dialog");
        if (dialog == null) return;

        PlayerConfigurationConnection conn = event.getConnection();
        UUID id = conn.getProfile().getId();
        if (id == null) return;

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        result.completeOnTimeout(false, 60, TimeUnit.SECONDS);
        pending.put(id, result);

        conn.getAudience().showDialog(dialog);

        if (!result.join()) {
            conn.getAudience().closeDialog();
            conn.disconnect(Component.text("You must accept the rules."));
        }
        pending.remove(id);
    }

    @EventHandler
    public void onClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerConfigurationConnection conn)) {
            return;
        }
        UUID id = conn.getProfile().getId();
        if (id == null) return;
        
        Key key = event.getIdentifier();
        if (key.equals(AGREE)) {
            complete(id, true);
        } else if (key.equals(DECLINE)) {
            complete(id, false);
        }
    }

    @EventHandler
    public void onClose(PlayerConnectionCloseEvent event) {
        pending.remove(event.getPlayerUniqueId());
    }

    private void complete(UUID id, boolean value) {
        CompletableFuture<Boolean> future = pending.get(id);
        if (future != null) {
            future.complete(value);
        }
    }
}
```

---

## Advanced Patterns

### Dynamic Content

Build dialogs with player-specific content:

```java
public Dialog buildWelcomeDialog(Player player) {
    Component personalizedBody = Component.text()
        .append(Component.text("Welcome, "))
        .append(Component.text(player.getName()))
        .append(Component.text("!"))
        .build();
    
    return Dialogs.notice(
        Component.text("Welcome"),
        personalizedBody
    );
}
```

### Input Validation

Validate inputs before processing:

```java
ActionButton submitButton = Dialogs.callbackButton(
    Component.text("Submit"),
    (view, audience) -> {
        String name = Dialogs.getText(view, "name");
        Float level = Dialogs.getFloat(view, "level");
        
        if (name == null || name.isBlank()) {
            audience.sendMessage(Component.text("Name is required!"));
            return;
        }
        
        if (level == null || level < 1 || level > 100) {
            audience.sendMessage(Component.text("Level must be between 1 and 100!"));
            return;
        }
        
        // Process valid input
    }
);
```

### Multi-Step Dialogs

Chain dialogs together:

```java
public void showSettingsDialog(Player player) {
    Dialog step1 = Dialogs.confirmation(
        Component.text("Settings"),
        Component.text("Do you want to configure settings?"),
        Dialogs.key("settings/yes"),
        Dialogs.key("settings/no")
    );
    
    Dialogs.show(player, step1, plugin.getLogger(), "settings");
}

@EventHandler
public void onClick(PlayerCustomClickEvent event) {
    Key key = event.getIdentifier();
    if (key.equals(Dialogs.key("settings/yes"))) {
        showSettingsMenu(event.getPlayer());
    }
}
```

---

## Best Practices

### Key Management

- Use `Dialogs.key()` for consistent namespacing
- Keys are automatically normalized (lowercase, spaces → underscores)
- All keys use the `michellelib` namespace

```java
Key agreeKey = Dialogs.key("rules/agree");  // → michellelib:rules/agree
```

### Input Safety

- Always use safe extractors (`getText`, `getFloat`, `getBool`)
- Validate extracted values before use
- Use bounded inputs (sliders, maxLength) to prevent unexpected values

### Callback Lifetime

- Callback buttons default to `uses(1)` for safety
- Prefer local callbacks over global listeners when possible
- Clean up pending futures on connection close

### Thread Safety

- Dialog creation is safe in bootstrap/config events
- Heavy I/O should be off-thread, then schedule back to main for player mutations
- Use `Dialogs.show()` for safe null checking

### Error Handling

```java
Dialog dialog = Dialogs.fromRegistry("plugin:important_dialog");
if (!Dialogs.show(player, dialog, plugin.getLogger(), "important")) {
    // Dialog was null or failed to show
    player.sendMessage(Component.text("Dialog unavailable. Please try again later."));
}
```

## Enhanced Dialog Presets

MichelleLib provides additional preset dialogs for common scenarios:

### Themed Dialogs

```java
// Warning dialogs (orange theme)
Dialog warning = Dialogs.warning(
    Component.text("Warning!", NamedTextColor.GOLD),
    Component.text("This action cannot be undone."),
    Key.key("dialog:proceed"),
    Key.key("dialog:cancel")
);

// Error dialogs (red theme)
Dialog error = Dialogs.error(
    Component.text("Error!", NamedTextColor.RED),
    Component.text("Something went wrong!")
);

// Success dialogs (green theme)
Dialog success = Dialogs.success(
    Component.text("Success!", NamedTextColor.GREEN),
    Component.text("Operation completed successfully."),
    Key.key("dialog:continue")
);
```

### Interactive Input Dialogs

```java
// Text input dialog
Dialog textDialog = Dialogs.inputDialog(
    Component.text("Enter Name"),
    Component.text("Player Name"),
    "Enter your name...",
    32, // max length
    Key.key("dialog:submit_name"),
    Key.key("dialog:cancel")
);

// Numeric input dialog
Dialog numberDialog = Dialogs.numberDialog(
    Component.text("Choose Level"),
    Component.text("Experience Level"),
    1, 100, 50, // min, max, default
    Key.key("dialog:set_level"),
    Key.key("dialog:cancel")
);

// Selection dialog
Dialog selectionDialog = Dialogs.selectionDialog(
    Component.text("Choose Class"),
    List.of("Warrior", "Mage", "Archer"),
    Key.key("dialog:select_class"),
    Key.key("dialog:cancel")
);
```

## Workflow Helpers

### Multi-Step Dialog Workflows

```java
// Create a workflow for character creation
DialogWorkflow workflow = Dialogs.workflow("character_creation")
    .step("name", Dialogs.inputStep(
        Component.text("Enter Character Name"),
        Component.text("Name"),
        "Enter name...",
        16,
        Key.key("dialog:name_submit"),
        Key.key("dialog:cancel")
    ))
    .step("class", Dialogs.selectionStep(
        Component.text("Choose Class"),
        List.of("Warrior", "Mage", "Archer"),
        Key.key("dialog:class_select"),
        Key.key("dialog:cancel")
    ))
    .onComplete(result -> {
        String name = result.get("name", String.class).orElse("Unknown");
        String charClass = result.get("class", String.class).orElse("Warrior");
        createCharacter(name, charClass);
    })
    .onCancel(context -> {
        context.audience().sendMessage(Component.text("Character creation cancelled."));
    });

// Start the workflow
workflow.start(player);
```

## TextEngine Integration

Dialogs now support MichelleLib's TextEngine for rich text formatting, placeholders, and MiniMessage parsing:

### Basic TextEngine Setup

```java
TextEngine engine = TextEngine.builder()
    .expandPlaceholderApiPercents(true)
    .enablePapiTag(true)
    .build();
```

### MiniMessage Dialogs

```java
// Notice with MiniMessage and placeholders
Dialog notice = Dialogs.notice(
    engine,
    player,
    "<gold>Welcome, <player>!",
    "<green>You have <emeralds> emeralds.</green>",
    Placeholder.component("player", player.name()),
    Placeholder.component("emeralds", Component.text(emeraldCount))
);

// Confirmation with formatting
Dialog confirm = Dialogs.confirmation(
    engine,
    player,
    "<red>Delete World?",
    "<gray>This action <bold>cannot</bold> be undone!</gray>",
    Key.key("dialog:delete"),
    Key.key("dialog:cancel")
);
```

### Input Dialogs with Formatting

```java
// Rich text input prompts
Dialog input = Dialogs.inputDialog(
    engine,
    player,
    "<gold>Enter Guild Name",
    "<yellow>Choose a unique name for your guild",
    "Enter name here...",
    32,
    Key.key("dialog:submit_guild"),
    Key.key("dialog:cancel")
);
```

## Advanced Response Handling

```java
// Create custom workflow steps
DialogStep customStep = context -> {
    Dialog dialog = Dialogs.confirmation(
        Component.text("Are you sure?"),
        Component.text("This will delete your character."),
        Key.key("dialog:confirm_delete"),
        Key.key("dialog:cancel_delete")
    );
    context.audience().showDialog(dialog);
};

// Add to workflow
workflow.step("confirm_delete", customStep);
```

### Async Workflows

```java
// Create async workflow that returns a future
CompletableFuture<Dialogs.WorkflowResult> future = Dialogs.asyncWorkflow(
    "async_example",
    player,
    // Step 1: Name input
    Dialogs.inputStep(
        Component.text("Enter Name"),
        Component.text("Your Name"),
        "Type here...",
        32,
        Key.key("dialog:name_done"),
        Key.key("dialog:cancel")
    ),
    // Step 2: Confirmation
    Dialogs.confirmStep(
        Component.text("Confirm"),
        Component.text("Is this correct?"),
        Key.key("dialog:yes"),
        Key.key("dialog:no")
    )
);

// Handle completion
future.thenAccept(result -> {
    String name = result.get("name", String.class).orElse("Unknown");
    player.sendMessage(Component.text("Hello, " + name + "!"));
});
```

## Advanced Response Handling

### Custom Validators

```java
// Create custom validation
Dialogs.ResponseValidator nameValidator = view -> {
    String name = Dialogs.getText(view, "name");
    if (name == null || name.trim().isEmpty()) {
        return Dialogs.ValidationResult.invalid("Name cannot be empty");
    }
    if (name.length() < 3) {
        return Dialogs.ValidationResult.invalid("Name must be at least 3 characters");
    }
    if (!name.matches("[a-zA-Z]+")) {
        return Dialogs.ValidationResult.invalid("Name can only contain letters");
    }
    return Dialogs.ValidationResult.valid();
};

// Create validated handler
Dialogs.ResponseHandler handler = Dialogs.createHandler(
    nameValidator,
    (view, audience) -> {
        String name = Dialogs.getText(view, "name");
        audience.sendMessage(Component.text("Welcome, " + name + "!"));
    },
    error -> player.sendMessage(Component.text("Error: " + error, NamedTextColor.RED))
);
```

### Built-in Validators

```java
// Require text fields
Dialogs.ResponseValidator textRequired = Dialogs.requireText("username", "password");

// Require numbers in range
Dialogs.ResponseValidator levelRange = Dialogs.requireNumberInRange("level", 1, 100);

// Require selection
Dialogs.ResponseValidator classSelected = Dialogs.requireSelection("character_class");
```

---

## Troubleshooting

### Dialog not showing

- Check that the dialog is not null using `Dialogs.show()`
- Verify Paper version is 1.21.7+
- Ensure `api-version: '1.21'` or newer in `plugin.yml`

### Input values are null

- Always use safe extractors (`getText`, `getFloat`, `getBool`)
- Check that the input key matches the key used in extraction
- Verify the response view is not null in callbacks

### Callback not firing

- Check that the button uses `DialogAction.customClick` with a callback
- Verify the callback hasn't exceeded its `uses` limit
- Ensure the dialog's `afterAction` is set correctly

### Registry lookup returns null

- Verify the dialog was registered during bootstrap
- Check the key namespace and path match exactly
- Ensure the registry event handler was registered correctly

---

## Reference

### Key Methods

- `Dialogs.key(String)` - Create a namespaced key
- `Dialogs.show(Audience, Dialog, Logger, String)` - Safe dialog display
- `Dialogs.fromRegistry(String)` - Fetch registered dialog
- `Dialogs.notice(Component, Component)` - Create notice dialog
- `Dialogs.confirmation(...)` - Create confirmation dialog
- `Dialogs.multiAction(...)` - Create multi-action dialog
- `Dialogs.dialogList(...)` - Create dialog list
- `Dialogs.serverLinks(...)` - Create server links dialog

### Input Helpers

- `Dialogs.textBox(...)` - Text input
- `Dialogs.numberSlider(...)` - Number range slider
- `Dialogs.singleChoice(...)` - Single choice (simple)
- `Dialogs.singleChoiceWithComponents(...)` - Single choice (advanced)
- `Dialogs.boolInput(...)` - Boolean checkbox

### Button Helpers

- `Dialogs.callbackButton(Component, DialogActionCallback)` - Callback button
- `Dialogs.commandButton(Component, String, int)` - Command template button

### Extraction Helpers

- `Dialogs.getText(DialogResponseView, String)` - Extract text
- `Dialogs.getFloat(DialogResponseView, String)` - Extract float
- `Dialogs.getBool(DialogResponseView, String)` - Extract boolean

---

## Examples

For more examples and advanced patterns, refer to the Paper dialog API documentation and experiment with the various helper methods provided by MichelleLib.

