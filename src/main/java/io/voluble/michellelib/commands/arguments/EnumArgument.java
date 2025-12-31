package io.voluble.michellelib.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A reusable "enum-like" argument that:
 * - Uses {@link StringArgumentType#word()} as the native type (client validation + basic parsing).
 * - Converts to an enum constant server-side.
 * - Suggests available enum values.
 */
public final class EnumArgument<E extends Enum<E>> implements CustomArgumentType.Converted<E, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(input ->
        MessageComponentSerializer.message().serialize(Component.text(input + " is not a valid value."))
    );

    private final Class<E> enumClass;
    private final ValueFormatter<E> formatter;
    private final DynamicCommandExceptionType invalidValueError;

    public EnumArgument(final @NotNull Class<E> enumClass) {
        this(enumClass, e -> e.name().toLowerCase(Locale.ROOT), ERROR_INVALID);
    }

    public EnumArgument(final @NotNull Class<E> enumClass, final @NotNull ValueFormatter<E> formatter, final @NotNull DynamicCommandExceptionType invalidValueError) {
        this.enumClass = Objects.requireNonNull(enumClass, "enumClass");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        this.invalidValueError = Objects.requireNonNull(invalidValueError, "invalidValueError");
    }

    @Override
    public @NotNull E convert(final @NotNull String nativeType) throws CommandSyntaxException {
        for (final E constant : enumClass.getEnumConstants()) {
            if (formatter.format(constant).equalsIgnoreCase(nativeType)) {
                return constant;
            }
        }
        throw invalidValueError.create(nativeType);
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(final @NotNull CommandContext<S> context, final @NotNull SuggestionsBuilder builder) {
        final String remaining = builder.getRemainingLowerCase();
        for (final E constant : enumClass.getEnumConstants()) {
            final String value = formatter.format(constant);
            if (remaining.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(value);
            }
        }
        return builder.buildFuture();
    }

    @FunctionalInterface
    public interface ValueFormatter<E> {
        @NotNull String format(E value);
    }
}


