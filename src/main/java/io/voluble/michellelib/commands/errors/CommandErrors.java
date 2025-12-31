package io.voluble.michellelib.commands.errors;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Command error helpers.
 *
 * <p>Paper docs often show verbose "MessageComponentSerializer + exception type" boilerplate.
 * This class makes those 1-liners.</p>
 */
public final class CommandErrors {
    private CommandErrors() {
    }

    /**
     * Creates a Brigadier {@link Message} from a plain string.
     */
    public static @NotNull Message messageText(final @NotNull String text) {
        Objects.requireNonNull(text, "text");
        return new LiteralMessage(text);
    }

    /**
     * Creates a Brigadier {@link Message} from an Adventure {@link Component}.
     */
    public static @NotNull Message message(final @NotNull Component component) {
        Objects.requireNonNull(component, "component");
        return MessageComponentSerializer.message().serialize(component);
    }

    public static @NotNull CommandSyntaxException failText(final @NotNull String text) {
        return new SimpleCommandExceptionType(messageText(text)).create();
    }

    public static @NotNull CommandSyntaxException fail(final @NotNull Component component) {
        return new SimpleCommandExceptionType(message(component)).create();
    }
}


