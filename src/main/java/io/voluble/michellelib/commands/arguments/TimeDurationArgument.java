package io.voluble.michellelib.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.voluble.michellelib.util.TimeParser;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Custom argument type for parsing human-readable time strings into Duration.
 *
 * <p>Supports formats like "1 second", "5 minutes", "1 hour 30 minutes", "2d 3h 15m", etc.</p>
 */
public final class TimeDurationArgument implements CustomArgumentType<Duration, String> {

    private static final DynamicCommandExceptionType ERROR_INVALID_TIME = new DynamicCommandExceptionType(
            input -> {
                return MessageComponentSerializer.message().serialize(
                        Component.text("Invalid time format: " + input)
                );
            }
    );

    private static final List<String> EXAMPLES = Arrays.asList(
            "1 second", "30 seconds",
            "1 minute", "5 minutes",
            "1 hour", "2 hours",
            "1 day", "7 days",
            "1 week", "2 weeks",
            "1 month", "3 months",
            "1 year", "2 years",
            "1h 30m", "2d 5h 15m", "1w 2d"
    );

    public static final TimeDurationArgument INSTANCE = new TimeDurationArgument();

    private TimeDurationArgument() {
    }

    @Override
    public @NotNull Duration parse(final @NotNull StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final String input = reader.readString();

        try {
            return TimeParser.parse(input);
        } catch (final TimeParser.TimeParseException e) {
            reader.setCursor(start);
            throw ERROR_INVALID_TIME.createWithContext(reader, input);
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public @NotNull Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(
            final @NotNull CommandContext<S> context,
            final @NotNull SuggestionsBuilder builder
    ) {
        final String remaining = builder.getRemainingLowerCase();

        for (final String example : EXAMPLES) {
            if (example.toLowerCase().startsWith(remaining)) {
                builder.suggest(example);
            }
        }

        // Suggest common units if nothing matches
        if (builder.getRemaining().isEmpty()) {
            builder.suggest("1 second");
            builder.suggest("1 minute");
            builder.suggest("1 hour");
            builder.suggest("1 day");
        }

        return builder.buildFuture();
    }

    /**
     * Gets the Duration argument from a command context.
     *
     * @param context the command context
     * @param name the argument name
     * @return the parsed duration
     */
    public static Duration getDuration(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Duration.class);
    }
}

