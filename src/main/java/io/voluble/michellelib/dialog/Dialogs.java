package io.voluble.michellelib.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.voluble.michellelib.text.TextEngine;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Facade for building and managing Paper dialogs with safety defaults.
 *
 * <p>Provides ergonomic helpers that sit on top of Paper's dialog API, including
 * input validation, safe extraction utilities, and common dialog presets.</p>
 */
public final class Dialogs {

    private static final String NAMESPACE = "michellelib";

    private Dialogs() {
    }

    /**
     * Builds a namespaced key, normalizing the path to lowercase with underscores.
     *
     * @param path the path to normalize and namespace
     * @return a key with the michellelib namespace
     */
    public static @NotNull Key key(final @NotNull String path) {
        final String normalized = path
            .trim()
            .replace(' ', '_')
            .toLowerCase(Locale.ROOT);
        return Key.key(NAMESPACE, normalized);
    }

    /**
     * Shows a dialog to an audience, logging a warning if the dialog is null.
     *
     * @param audience the audience to show the dialog to
     * @param dialog the dialog to show, may be null
     * @param logger logger for warning messages
     * @param context context string for logging
     * @return true if the dialog was shown, false if it was null
     */
    public static boolean show(
        final @NotNull Audience audience,
        final @Nullable Dialog dialog,
        final @NotNull Logger logger,
        final @NotNull String context
    ) {
        if (dialog == null) {
            logger.warning(() -> "Dialog unavailable for context: " + context);
            return false;
        }
        audience.showDialog(dialog);
        return true;
    }

    /**
     * Fetches a dialog from the registry by its namespaced key.
     *
     * <p>Useful for config-phase lookups where dialogs are registered during bootstrap.</p>
     *
     * @param namespacedPath the namespaced key path (e.g., "namespace:path")
     * @return the dialog if found, null otherwise
     */
    public static @Nullable Dialog fromRegistry(final @NotNull String namespacedPath) {
        return RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.DIALOG)
            .get(Key.key(namespacedPath));
    }

    /**
     * Creates a notice dialog with a title and body text, using the default OK button.
     *
     * @param title the dialog title
     * @param body the dialog body text
     * @return a configured notice dialog
     */
    public static @NotNull Dialog notice(final @NotNull Component title, final @NotNull Component body) {
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .body(List.of(DialogBody.plainMessage(body)))
                .build()
            )
            .type(DialogType.notice())
        );
    }

    /**
     * Creates a notice dialog with MiniMessage strings using the provided TextEngine.
     *
     * @param textEngine the text engine for parsing MiniMessage
     * @param player the player for placeholder resolution (may be null)
     * @param title the dialog title as MiniMessage string
     * @param body the dialog body text as MiniMessage string
     * @param extraResolvers additional tag resolvers
     * @return a configured notice dialog
     */
    public static @NotNull Dialog notice(
        @NotNull TextEngine textEngine,
        @Nullable Player player,
        @NotNull String title,
        @NotNull String body,
        @NotNull TagResolver... extraResolvers
    ) {
        Component titleComponent = textEngine.parse(player, title, extraResolvers);
        Component bodyComponent = textEngine.parse(player, body, extraResolvers);
        return notice(titleComponent, bodyComponent);
    }

    /**
     * Creates a notice dialog with MiniMessage strings (no placeholders).
     *
     * @param textEngine the text engine for parsing MiniMessage
     * @param title the dialog title as MiniMessage string
     * @param body the dialog body text as MiniMessage string
     * @param extraResolvers additional tag resolvers
     * @return a configured notice dialog
     */
    public static @NotNull Dialog notice(
        @NotNull TextEngine textEngine,
        @NotNull String title,
        @NotNull String body,
        @NotNull TagResolver... extraResolvers
    ) {
        return notice(textEngine, null, title, body, extraResolvers);
    }

    /**
     * Creates a confirmation dialog that waits for a response.
     *
     * <p>Uses {@link DialogBase.DialogAfterAction#WAIT_FOR_RESPONSE} to maintain
     * proper pause semantics. Escape key closing is disabled.</p>
     *
     * @param title the dialog title
     * @param body the dialog body text
     * @param yesKey the key for the yes button action
     * @param noKey the key for the no button action
     * @return a configured confirmation dialog
     */
    public static @NotNull Dialog confirmation(
        final @NotNull Component title,
        final @NotNull Component body,
        final @NotNull Key yesKey,
        final @NotNull Key noKey
    ) {
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .body(List.of(DialogBody.plainMessage(body)))
                .canCloseWithEscape(false)
                .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(Component.translatable("gui.yes"))
                    .action(DialogAction.customClick(yesKey, null))
                    .build(),
                ActionButton.builder(Component.translatable("gui.no"))
                    .action(DialogAction.customClick(noKey, null))
                    .build()
            ))
        );
    }

    /**
     * Creates a confirmation dialog with MiniMessage strings using the provided TextEngine.
     *
     * @param textEngine the text engine for parsing MiniMessage
     * @param player the player for placeholder resolution (may be null)
     * @param title the dialog title as MiniMessage string
     * @param body the dialog body text as MiniMessage string
     * @param yesKey the key for the yes button action
     * @param noKey the key for the no button action
     * @param extraResolvers additional tag resolvers
     * @return a configured confirmation dialog
     */
    public static @NotNull Dialog confirmation(
        @NotNull TextEngine textEngine,
        @Nullable Player player,
        @NotNull String title,
        @NotNull String body,
        @NotNull Key yesKey,
        @NotNull Key noKey,
        @NotNull TagResolver... extraResolvers
    ) {
        Component titleComponent = textEngine.parse(player, title, extraResolvers);
        Component bodyComponent = textEngine.parse(player, body, extraResolvers);
        return confirmation(titleComponent, bodyComponent, yesKey, noKey);
    }

    /**
     * Creates a confirmation dialog with MiniMessage strings (no placeholders).
     *
     * @param textEngine the text engine for parsing MiniMessage
     * @param title the dialog title as MiniMessage string
     * @param body the dialog body text as MiniMessage string
     * @param yesKey the key for the yes button action
     * @param noKey the key for the no button action
     * @param extraResolvers additional tag resolvers
     * @return a configured confirmation dialog
     */
    public static @NotNull Dialog confirmation(
        @NotNull TextEngine textEngine,
        @NotNull String title,
        @NotNull String body,
        @NotNull Key yesKey,
        @NotNull Key noKey,
        @NotNull TagResolver... extraResolvers
    ) {
        return confirmation(textEngine, null, title, body, yesKey, noKey, extraResolvers);
    }

    /**
     * Creates a multi-action dialog with a list of action buttons.
     *
     * @param title the dialog title
     * @param actions the list of action buttons
     * @param columns the number of columns for the button layout (minimum 1)
     * @return a configured multi-action dialog
     */
    public static @NotNull Dialog multiAction(
        final @NotNull Component title,
        final @NotNull List<ActionButton> actions,
        final int columns
    ) {
        final int safeColumns = Math.max(1, columns);
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title).build())
            .type(DialogType.multiAction(actions, null, safeColumns))
        );
    }

    /**
     * Creates an action button backed by a callback handler with safety defaults.
     *
     * <p>The callback is configured with {@code uses(1)} and guards against null response views.
     * The handler is only invoked if a valid response view is available.</p>
     *
     * @param label the button label
     * @param handler the callback handler to invoke on click
     * @return a configured action button
     */
    public static @NotNull ActionButton callbackButton(
        final @NotNull Component label,
        final @NotNull DialogActionCallback handler
    ) {
        final DialogActionCallback safeHandler = (view, audience) -> {
            if (view == null) {
                return;
            }
            handler.accept(view, audience);
        };
        final ClickCallback.Options opts = ClickCallback.Options.builder()
            .uses(1)
            .build();
        return ActionButton.builder(label)
            .action(DialogAction.customClick(safeHandler, opts))
            .build();
    }

    /**
     * Creates an action button that executes a command template.
     *
     * <p>The command template supports input substitution using dialog input keys.
     * The button width is clamped to a safe range (1-1024).</p>
     *
     * @param label the button label
     * @param commandTemplate the command template string with input substitutions
     * @param width the button width (clamped to 1-1024)
     * @return a configured action button
     */
    public static @NotNull ActionButton commandButton(
        final @NotNull Component label,
        final @NotNull String commandTemplate,
        final int width
    ) {
        final int safeWidth = Math.max(1, Math.min(1024, width));
        return ActionButton.builder(label)
            .width(safeWidth)
            .action(DialogAction.commandTemplate(commandTemplate))
            .build();
    }

    /**
     * Creates a number range input (slider) with clamped bounds and step validation.
     *
     * <p>The initial value is clamped to the [start, end] range, and the step is
     * validated to be positive. The width is clamped to a safe range (1-1024).</p>
     *
     * @param key the input key for retrieval from responses
     * @param label the input label
     * @param start the minimum value
     * @param end the maximum value
     * @param initial the initial value (clamped to [start, end])
     * @param step the step increment (must be positive, defaults to 1 if invalid)
     * @param width the input width (clamped to 1-1024)
     * @return a configured number range input
     */
    public static @NotNull DialogInput numberSlider(
        final @NotNull String key,
        final @NotNull Component label,
        final float start,
        final float end,
        final float initial,
        final float step,
        final int width
    ) {
        final int safeWidth = Math.max(1, Math.min(1024, width));
        final float clampedInitial = clamp(initial, start, end);
        final float safeStep = step <= 0 ? 1f : step;
        return DialogInput.numberRange(key, label, start, end)
            .width(safeWidth)
            .initial(clampedInitial)
            .step(safeStep)
            .build();
    }

    /**
     * Creates a text input with length validation and sanitization.
     *
     * <p>The initial value is trimmed and control characters are removed. The value
     * is truncated to maxLength if necessary. The width is clamped to a safe range (1-1024).</p>
     *
     * @param key the input key for retrieval from responses
     * @param label the input label
     * @param width the input width (clamped to 1-1024)
     * @param maxLength the maximum text length (minimum 1)
     * @param initial the initial text value (sanitized and trimmed)
     * @return a configured text input
     */
    public static @NotNull DialogInput textBox(
        final @NotNull String key,
        final @NotNull Component label,
        final int width,
        final int maxLength,
        final @NotNull String initial
    ) {
        final int safeWidth = Math.max(1, Math.min(1024, width));
        final int safeMax = Math.max(1, maxLength);
        final String sanitized = sanitize(initial, safeMax);
        return DialogInput.text(key, label)
            .width(safeWidth)
            .maxLength(safeMax)
            .initial(sanitized)
            .build();
    }

    /**
     * Creates a single-choice input with a list of option entries.
     *
     * <p>At most one option may be marked as initial. The width is clamped to a safe range (1-1024).</p>
     *
     * @param key the input key for retrieval from responses
     * @param label the input label
     * @param optionIds the list of option IDs
     * @param initialId the ID of the initially selected option, may be null
     * @param width the input width (clamped to 1-1024)
     * @return a configured single-choice input
     */
    public static @NotNull DialogInput singleChoice(
        final @NotNull String key,
        final @NotNull Component label,
        final @NotNull List<String> optionIds,
        final @Nullable String initialId,
        final int width
    ) {
        final int safeWidth = Math.max(1, Math.min(1024, width));
        final List<io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry> entries =
            optionIds.stream()
                .map(id -> io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry.create(
                    id,
                    Component.text(id),
                    Objects.equals(id, initialId)
                ))
                .toList();
        return DialogInput.singleOption(key, label, entries)
            .width(safeWidth)
            .build();
    }

    /**
     * Creates a single-choice input with custom option components.
     *
     * <p>Allows full control over option labels using Adventure components.
     * At most one option may be marked as initial. The width is clamped to a safe range (1-1024).</p>
     *
     * @param key the input key for retrieval from responses
     * @param label the input label
     * @param options the list of option entries with custom components
     * @param width the input width (clamped to 1-1024)
     * @return a configured single-choice input
     */
    public static @NotNull DialogInput singleChoiceWithComponents(
        final @NotNull String key,
        final @NotNull Component label,
        final @NotNull List<io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry> options,
        final int width
    ) {
        final int safeWidth = Math.max(1, Math.min(1024, width));
        return DialogInput.singleOption(key, label, options)
            .width(safeWidth)
            .build();
    }

    /**
     * Creates a boolean input (checkbox) with validation.
     *
     * @param key the input key for retrieval from responses
     * @param label the input label
     * @param initial the initial boolean value
     * @param onTrue the template value when true (defaults to "true")
     * @param onFalse the template value when false (defaults to "false")
     * @return a configured boolean input
     */
    public static @NotNull DialogInput boolInput(
        final @NotNull String key,
        final @NotNull Component label,
        final boolean initial,
        final @NotNull String onTrue,
        final @NotNull String onFalse
    ) {
        return DialogInput.bool(key, label)
            .initial(initial)
            .onTrue(onTrue)
            .onFalse(onFalse)
            .build();
    }

    /**
     * Creates a boolean input with default template values.
     *
     * @param key the input key for retrieval from responses
     * @param label the input label
     * @param initial the initial boolean value
     * @return a configured boolean input
     */
    public static @NotNull DialogInput boolInput(
        final @NotNull String key,
        final @NotNull Component label,
        final boolean initial
    ) {
        return boolInput(key, label, initial, "true", "false");
    }

    /**
     * Creates an item body entry for displaying an item in the dialog.
     *
     * @param item the item stack to display
     * @return a configured item body entry
     */
    public static @NotNull DialogBody itemBody(final @NotNull org.bukkit.inventory.ItemStack item) {
        return DialogBody.item(item).build();
    }

    /**
     * Creates a dialog list dialog for navigating between registered dialogs.
     *
     * @param title the dialog title
     * @param dialogs the registry set of dialogs to display
     * @param exitAction the exit button, may be null
     * @param columns the number of columns (minimum 1)
     * @param buttonWidth the width of each button (clamped to 1-1024)
     * @return a configured dialog list dialog
     */
    public static @NotNull Dialog dialogList(
        final @NotNull Component title,
        final @NotNull io.papermc.paper.registry.set.RegistrySet<Dialog> dialogs,
        final @Nullable ActionButton exitAction,
        final int columns,
        final int buttonWidth
    ) {
        final int safeColumns = Math.max(1, columns);
        final int safeWidth = Math.max(1, Math.min(1024, buttonWidth));
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title).build())
            .type(DialogType.dialogList(dialogs, exitAction, safeColumns, safeWidth))
        );
    }

    /**
     * Creates a server links dialog for displaying server links.
     *
     * @param title the dialog title
     * @param exitAction the exit button, may be null
     * @param columns the number of columns (minimum 1)
     * @param buttonWidth the width of each button (clamped to 1-1024)
     * @return a configured server links dialog
     */
    public static @NotNull Dialog serverLinks(
        final @NotNull Component title,
        final @Nullable ActionButton exitAction,
        final int columns,
        final int buttonWidth
    ) {
        final int safeColumns = Math.max(1, columns);
        final int safeWidth = Math.max(1, Math.min(1024, buttonWidth));
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title).build())
            .type(DialogType.serverLinks(exitAction, safeColumns, safeWidth))
        );
    }

    /**
     * Creates a notice dialog with a custom action button.
     *
     * @param title the dialog title
     * @param body the dialog body text
     * @param actionButton the custom action button
     * @return a configured notice dialog
     */
    public static @NotNull Dialog notice(
        final @NotNull Component title,
        final @NotNull Component body,
        final @NotNull ActionButton actionButton
    ) {
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .body(List.of(DialogBody.plainMessage(body)))
                .build()
            )
            .type(DialogType.notice(actionButton))
        );
    }

    /**
     * Creates a confirmation dialog with custom buttons.
     *
     * @param title the dialog title
     * @param body the dialog body text
     * @param yesButton the yes button
     * @param noButton the no button
     * @param canCloseWithEscape whether the dialog can be closed with escape
     * @param afterAction the action to take after the dialog is closed
     * @return a configured confirmation dialog
     */
    public static @NotNull Dialog confirmation(
        final @NotNull Component title,
        final @NotNull Component body,
        final @NotNull ActionButton yesButton,
        final @NotNull ActionButton noButton,
        final boolean canCloseWithEscape,
        final @NotNull DialogBase.DialogAfterAction afterAction
    ) {
        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .body(List.of(DialogBody.plainMessage(body)))
                .canCloseWithEscape(canCloseWithEscape)
                .afterAction(afterAction)
                .build()
            )
            .type(DialogType.confirmation(yesButton, noButton))
        );
    }

    // ---- Enhanced Dialog Presets ----

    /**
     * Creates a warning dialog with an orange theme.
     *
     * @param title the dialog title
     * @param message the warning message
     * @param confirmText the confirmation button text
     * @param confirmKey the confirmation action key
     * @return a configured warning dialog
     */
    public static @NotNull Dialog warning(
        @NotNull Component title,
        @NotNull Component message,
        @NotNull Component confirmText,
        @NotNull Key confirmKey
    ) {
        ActionButton confirmButton = ActionButton.builder(confirmText)
            .action(DialogAction.customClick(confirmKey, null))
            .build();

        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title.color(NamedTextColor.GOLD))
                .body(List.of(DialogBody.plainMessage(message.color(NamedTextColor.YELLOW))))
                .canCloseWithEscape(true)
                .build())
            .type(DialogType.notice(confirmButton))
        );
    }

    /**
     * Creates a warning dialog with MiniMessage strings.
     *
     * @param textEngine the text engine for parsing MiniMessage
     * @param player the player for placeholder resolution (may be null)
     * @param title the dialog title as MiniMessage string
     * @param message the warning message as MiniMessage string
     * @param confirmText the confirmation button text as MiniMessage string
     * @param confirmKey the confirmation action key
     * @param extraResolvers additional tag resolvers
     * @return a configured warning dialog
     */
    public static @NotNull Dialog warning(
        @NotNull TextEngine textEngine,
        @Nullable Player player,
        @NotNull String title,
        @NotNull String message,
        @NotNull String confirmText,
        @NotNull Key confirmKey,
        @NotNull TagResolver... extraResolvers
    ) {
        Component titleComponent = textEngine.parse(player, title, extraResolvers);
        Component messageComponent = textEngine.parse(player, message, extraResolvers);
        Component confirmTextComponent = textEngine.parse(player, confirmText, extraResolvers);
        return warning(titleComponent, messageComponent, confirmTextComponent, confirmKey);
    }

    /**
     * Creates an error dialog with a red theme.
     *
     * @param title the dialog title
     * @param message the error message
     * @return a configured error dialog
     */
    public static @NotNull Dialog error(@NotNull Component title, @NotNull Component message) {
        ActionButton okButton = ActionButton.builder(Component.text("OK", NamedTextColor.WHITE))
            .action(DialogAction.customClick(Key.key("dialog:close"), null))
            .build();

        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title.color(NamedTextColor.RED))
                .body(List.of(DialogBody.plainMessage(message.color(NamedTextColor.RED))))
                .canCloseWithEscape(true)
                .build())
            .type(DialogType.notice(okButton))
        );
    }

    /**
     * Creates an error dialog with MiniMessage strings.
     *
     * @param textEngine the text engine for parsing MiniMessage
     * @param player the player for placeholder resolution (may be null)
     * @param title the dialog title as MiniMessage string
     * @param message the error message as MiniMessage string
     * @param extraResolvers additional tag resolvers
     * @return a configured error dialog
     */
    public static @NotNull Dialog error(
        @NotNull TextEngine textEngine,
        @Nullable Player player,
        @NotNull String title,
        @NotNull String message,
        @NotNull TagResolver... extraResolvers
    ) {
        Component titleComponent = textEngine.parse(player, title, extraResolvers);
        Component messageComponent = textEngine.parse(player, message, extraResolvers);
        return error(titleComponent, messageComponent);
    }

    /**
     * Creates a success dialog with a green theme.
     *
     * @param title the dialog title
     * @param message the success message
     * @param continueKey the continue action key
     * @return a configured success dialog
     */
    public static @NotNull Dialog success(
        @NotNull Component title,
        @NotNull Component message,
        @NotNull Key continueKey
    ) {
        ActionButton continueButton = ActionButton.builder(Component.text("Continue", NamedTextColor.GREEN))
            .action(DialogAction.customClick(continueKey, null))
            .build();

        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title.color(NamedTextColor.GREEN))
                .body(List.of(DialogBody.plainMessage(message.color(NamedTextColor.GREEN))))
                .canCloseWithEscape(true)
                .build())
            .type(DialogType.notice(continueButton))
        );
    }

    /**
     * Creates an input dialog for collecting user text input.
     *
     * @param title the dialog title
     * @param label the input field label
     * @param placeholder the placeholder text
     * @param maxLength the maximum input length
     * @param submitKey the submit action key
     * @param cancelKey the cancel action key
     * @return a configured input dialog
     */
    public static @NotNull Dialog inputDialog(
        @NotNull Component title,
        @NotNull Component label,
        @NotNull String placeholder,
        int maxLength,
        @NotNull Key submitKey,
        @NotNull Key cancelKey
    ) {
        ActionButton submitButton = ActionButton.builder(Component.text("Submit", NamedTextColor.GREEN))
            .action(DialogAction.customClick(submitKey, null))
            .build();

        ActionButton cancelButton = ActionButton.builder(Component.text("Cancel", NamedTextColor.GRAY))
            .action(DialogAction.customClick(cancelKey, null))
            .build();

        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .inputs(List.of(textBox("input", label, 300, maxLength, placeholder)))
                .canCloseWithEscape(true)
                .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                .build())
            .type(DialogType.confirmation(submitButton, cancelButton))
        );
    }

    /**
     * Creates a selection dialog with multiple options.
     *
     * @param title the dialog title
     * @param options the list of selectable options
     * @param selectKey the selection action key
     * @param cancelKey the cancel action key
     * @return a configured selection dialog
     */
    public static @NotNull Dialog selectionDialog(
        @NotNull Component title,
        @NotNull List<String> options,
        @NotNull Key selectKey,
        @NotNull Key cancelKey
    ) {
        List<io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry> optionEntries =
            options.stream()
                .map(option -> io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry.create(
                    option, Component.text(option), false))
                .toList();

        ActionButton selectButton = ActionButton.builder(Component.text("Select", NamedTextColor.BLUE))
            .action(DialogAction.customClick(selectKey, null))
            .build();

        ActionButton cancelButton = ActionButton.builder(Component.text("Cancel", NamedTextColor.GRAY))
            .action(DialogAction.customClick(cancelKey, null))
            .build();

        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .inputs(List.of(singleChoice("selection", Component.text("Choose an option"), optionEntries, 300)))
                .canCloseWithEscape(true)
                .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                .build())
            .type(DialogType.confirmation(selectButton, cancelButton))
        );
    }

    /**
     * Creates a numeric input dialog for entering numbers.
     *
     * @param title the dialog title
     * @param label the input field label
     * @param min the minimum value
     * @param max the maximum value
     * @param defaultValue the default value
     * @param submitKey the submit action key
     * @param cancelKey the cancel action key
     * @return a configured numeric input dialog
     */
    public static @NotNull Dialog numberDialog(
        @NotNull Component title,
        @NotNull Component label,
        float min,
        float max,
        float defaultValue,
        @NotNull Key submitKey,
        @NotNull Key cancelKey
    ) {
        ActionButton submitButton = ActionButton.builder(Component.text("Submit", NamedTextColor.GREEN))
            .action(DialogAction.customClick(submitKey, null))
            .build();

        ActionButton cancelButton = ActionButton.builder(Component.text("Cancel", NamedTextColor.GRAY))
            .action(DialogAction.customClick(cancelKey, null))
            .build();

        return Dialog.create(b -> b.empty()
            .base(DialogBase.builder(title)
                .inputs(List.of(numberSlider("number", label, min, max, defaultValue, 1.0f, 300)))
                .canCloseWithEscape(true)
                .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                .build())
            .type(DialogType.confirmation(submitButton, cancelButton))
        );
    }

    // ---- Workflow Helpers ----

    /**
     * Creates a dialog workflow for multi-step interactions.
     *
     * @param workflowId unique identifier for this workflow
     * @return a new workflow builder
     */
    public static @NotNull DialogWorkflow workflow(@NotNull String workflowId) {
        return new DialogWorkflow(workflowId);
    }

    /**
     * Builder for creating multi-step dialog workflows.
     */
    public static final class DialogWorkflow {
        private final String workflowId;
        private final Map<String, DialogStep> steps = new LinkedHashMap<>();
        private String currentStep;
        private Consumer<WorkflowResult> onComplete;
        private Consumer<WorkflowContext> onCancel;

        public DialogWorkflow(@NotNull String workflowId) {
            this.workflowId = workflowId;
        }

        /**
         * Adds a step to the workflow.
         */
        public @NotNull DialogWorkflow step(@NotNull String stepId, @NotNull DialogStep step) {
            steps.put(stepId, step);
            if (currentStep == null) {
                currentStep = stepId;
            }
            return this;
        }

        /**
         * Sets the completion handler.
         */
        public @NotNull DialogWorkflow onComplete(@NotNull Consumer<WorkflowResult> handler) {
            this.onComplete = handler;
            return this;
        }

        /**
         * Sets the cancellation handler.
         */
        public @NotNull DialogWorkflow onCancel(@NotNull Consumer<WorkflowContext> handler) {
            this.onCancel = handler;
            return this;
        }

        /**
         * Starts the workflow for an audience.
         */
        public void start(@NotNull Audience audience) {
            if (currentStep == null || !steps.containsKey(currentStep)) {
                return;
            }

            WorkflowContext context = new WorkflowContext(workflowId, audience, this);
            steps.get(currentStep).show(context);
        }

        /**
         * Advances to the next step.
         */
        void nextStep(@NotNull WorkflowContext context, @Nullable Object result) {
            // Find next step
            List<String> stepIds = new ArrayList<>(steps.keySet());
            int currentIndex = stepIds.indexOf(context.currentStepId);

            if (currentIndex >= 0 && currentIndex < stepIds.size() - 1) {
                String nextStepId = stepIds.get(currentIndex + 1);
                context.currentStepId = nextStepId;
                context.addResult(context.currentStepId, result);
                steps.get(nextStepId).show(context);
            } else {
                // Workflow complete
                if (onComplete != null) {
                    onComplete.accept(new WorkflowResult(context.results));
                }
            }
        }

        /**
         * Cancels the workflow.
         */
        void cancel(@NotNull WorkflowContext context) {
            if (onCancel != null) {
                onCancel.accept(context);
            }
        }
    }

    /**
     * Interface for workflow steps.
     */
    @FunctionalInterface
    public interface DialogStep {
        void show(@NotNull WorkflowContext context);
    }

    /**
     * Context for workflow execution.
     */
    public static final class WorkflowContext {
        public final String workflowId;
        public final Audience audience;
        public final Map<String, Object> results = new HashMap<>();
        public String currentStepId;

        private final DialogWorkflow workflow;

        WorkflowContext(@NotNull String workflowId, @NotNull Audience audience, @NotNull DialogWorkflow workflow) {
            this.workflowId = workflowId;
            this.audience = audience;
            this.workflow = workflow;
        }

        public void next(@Nullable Object result) {
            workflow.nextStep(this, result);
        }

        public void cancel() {
            workflow.cancel(this);
        }

        public void addResult(@NotNull String key, @Nullable Object value) {
            if (value != null) {
                results.put(key, value);
            }
        }
    }

    /**
     * Result of a completed workflow.
     */
    public static final class WorkflowResult {
        public final Map<String, Object> results;

        WorkflowResult(@NotNull Map<String, Object> results) {
            this.results = new HashMap<>(results);
        }

        public @Nullable <T> T get(@NotNull String key, @NotNull Class<T> type) {
            Object value = results.get(key);
            return type.isInstance(value) ? type.cast(value) : null;
        }
    }

    // ---- Convenient Workflow Steps ----

    /**
     * Creates a confirmation step.
     */
    public static @NotNull DialogStep confirmStep(
        @NotNull Component title,
        @NotNull Component message,
        @NotNull Key yesKey,
        @NotNull Key noKey
    ) {
        return context -> {
            Dialog dialog = confirmation(title, message, yesKey, noKey);
            context.audience.showDialog(dialog);
        };
    }

    /**
     * Creates a text input step.
     */
    public static @NotNull DialogStep inputStep(
        @NotNull Component title,
        @NotNull Component label,
        @NotNull String placeholder,
        int maxLength,
        @NotNull Key submitKey,
        @NotNull Key cancelKey
    ) {
        return context -> {
            Dialog dialog = inputDialog(title, label, placeholder, maxLength, submitKey, cancelKey);
            context.audience.showDialog(dialog);
        };
    }

    /**
     * Creates a selection step.
     */
    public static @NotNull DialogStep selectionStep(
        @NotNull Component title,
        @NotNull List<String> options,
        @NotNull Key selectKey,
        @NotNull Key cancelKey
    ) {
        return context -> {
            Dialog dialog = selectionDialog(title, options, selectKey, cancelKey);
            context.audience.showDialog(dialog);
        };
    }

    /**
     * Utility method for wiring a registry entry builder in bootstrap handlers.
     *
     * @param builder the registry entry builder
     * @param dialogBuilder the consumer that configures the dialog
     */
    public static void register(
        final @NotNull DialogRegistryEntry.Builder builder,
        final @NotNull Consumer<DialogRegistryEntry.Builder> dialogBuilder
    ) {
        dialogBuilder.accept(builder);
    }

    /**
     * Safely extracts a text value from a dialog response view.
     *
     * @param view the response view, may be null
     * @param key the input key
     * @return the text value if present, null otherwise
     */
    public static @Nullable String getText(final @Nullable DialogResponseView view, final @NotNull String key) {
        if (view == null) {
            return null;
        }
        return view.getText(key);
    }

    /**
     * Safely extracts a float value from a dialog response view.
     *
     * @param view the response view, may be null
     * @param key the input key
     * @return the float value if present, null otherwise
     */
    public static @Nullable Float getFloat(final @Nullable DialogResponseView view, final @NotNull String key) {
        if (view == null) {
            return null;
        }
        return view.getFloat(key);
    }

    /**
     * Safely extracts a boolean value from a dialog response view.
     *
     * @param view the response view, may be null
     * @param key the input key
     * @return the boolean value if present, null otherwise
     */
    public static @Nullable Boolean getBool(final @Nullable DialogResponseView view, final @NotNull String key) {
        if (view == null) {
            return null;
        }
        return view.getBoolean(key);
    }

    /**
     * Sanitizes a string value, trimming and removing control characters.
     *
     * <p>If the value is null, returns null. Otherwise, trims whitespace, removes
     * control characters, and truncates to maxLen if necessary.</p>
     *
     * @param value the value to sanitize, may be null
     * @param maxLen the maximum length
     * @return the sanitized value, or null if input was null
     */
    public static @Nullable String sanitizeOrNull(final @Nullable String value, final int maxLen) {
        if (value == null) {
            return null;
        }
        return sanitize(value, maxLen);
    }

    // ---- Enhanced Response Handling ----

    /**
     * Creates a response handler that validates and processes dialog responses.
     *
     * @param validator the validation function
     * @param processor the processing function
     * @param onError the error handler
     * @return a response handler function
     */
    public static @NotNull ResponseHandler createHandler(
        @NotNull ResponseValidator validator,
        @NotNull ResponseProcessor processor,
        @NotNull Consumer<String> onError
    ) {
        return (view, audience) -> {
            try {
                ValidationResult validation = validator.validate(view);
                if (!validation.valid()) {
                    onError.accept(validation.errorMessage());
                    return;
                }
                processor.process(view, audience);
            } catch (Exception e) {
                onError.accept("An error occurred: " + e.getMessage());
            }
        };
    }

    /**
     * Functional interface for response validation.
     */
    @FunctionalInterface
    public interface ResponseValidator {
        @NotNull ValidationResult validate(@Nullable DialogResponseView view);
    }

    /**
     * Result of response validation.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, @Nullable String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean valid() { return valid; }
        public @Nullable String errorMessage() { return errorMessage; }

        public static @NotNull ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static @NotNull ValidationResult invalid(@NotNull String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Functional interface for response processing.
     */
    @FunctionalInterface
    public interface ResponseProcessor {
        void process(@NotNull DialogResponseView view, @NotNull Audience audience);
    }

    /**
     * Functional interface for response handling.
     */
    @FunctionalInterface
    public interface ResponseHandler extends DialogActionCallback {
        void handle(@Nullable DialogResponseView view, @NotNull Audience audience);

        @Override
        default void accept(@Nullable DialogResponseView view, @NotNull Audience audience) {
            handle(view, audience);
        }
    }

    // ---- Common Validators ----

    /**
     * Creates a validator that checks if required text fields are present and not empty.
     */
    public static @NotNull ResponseValidator requireText(@NotNull String... fieldKeys) {
        return view -> {
            if (view == null) {
                return ValidationResult.invalid("No response data provided");
            }

            for (String key : fieldKeys) {
                String value = view.getText(key);
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.invalid("Field '" + key + "' is required");
                }
            }

            return ValidationResult.valid();
        };
    }

    /**
     * Creates a validator that checks if numeric fields are within range.
     */
    public static @NotNull ResponseValidator requireNumberInRange(
        @NotNull String fieldKey,
        double min,
        double max
    ) {
        return view -> {
            if (view == null) {
                return ValidationResult.invalid("No response data provided");
            }

            Float value = view.getFloat(fieldKey);
            if (value == null) {
                return ValidationResult.invalid("Field '" + fieldKey + "' is required");
            }

            if (value < min || value > max) {
                return ValidationResult.invalid("Field '" + fieldKey + "' must be between " + min + " and " + max);
            }

            return ValidationResult.valid();
        };
    }

    /**
     * Creates a validator that checks if a selection was made.
     */
    public static @NotNull ResponseValidator requireSelection(@NotNull String fieldKey) {
        return view -> {
            if (view == null) {
                return ValidationResult.invalid("No response data provided");
            }

            String value = view.getText(fieldKey);
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.invalid("Please make a selection for '" + fieldKey + "'");
            }

            return ValidationResult.valid();
        };
    }

    // ---- Async Workflow Support ----

    /**
     * Creates an async dialog workflow that returns a CompletableFuture.
     *
     * @param workflowId unique identifier for the workflow
     * @param audience the audience to show dialogs to
     * @param steps the workflow steps
     * @return a future that completes with the workflow result
     */
    public static @NotNull CompletableFuture<WorkflowResult> asyncWorkflow(
        @NotNull String workflowId,
        @NotNull Audience audience,
        @NotNull DialogStep... steps
    ) {
        CompletableFuture<WorkflowResult> future = new CompletableFuture<>();
        Map<String, Object> results = new HashMap<>();

        // This is a simplified implementation - in practice you'd want more sophisticated
        // state management and error handling
        DialogWorkflow workflow = new DialogWorkflow(workflowId);

        for (int i = 0; i < steps.length; i++) {
            final int stepIndex = i;
            final DialogStep step = steps[i];

            workflow.step("step_" + i, context -> {
                // Store context for completion
                step.show(new WorkflowContext(workflowId, audience, workflow) {
                    @Override
                    public void next(@Nullable Object result) {
                        results.put("step_" + stepIndex, result);
                        if (stepIndex == steps.length - 1) {
                            future.complete(new WorkflowResult(results));
                        } else {
                            // Continue to next step
                            super.next(result);
                        }
                    }
                });
            });
        }

        workflow.onComplete(future::complete);
        workflow.onCancel(ctx -> future.cancel(false));

        workflow.start(audience);
        return future;
    }

    private static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String sanitize(final String value, final int maxLen) {
        final String trimmed = value.strip();
        final String noControls = trimmed.replaceAll("\\p{Cntrl}", "");
        return noControls.length() <= maxLen ? noControls : noControls.substring(0, maxLen);
    }
}

