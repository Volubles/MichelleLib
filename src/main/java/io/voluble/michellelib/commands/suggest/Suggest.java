package io.voluble.michellelib.commands.suggest;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SuggestionProvider toolbox.
 *
 * <p>These helpers are optional conveniences. They do not replace Brigadier; they just remove
 * repetitive "remainingLowerCase + startsWith + builder.suggest" boilerplate.</p>
 *
 * <p>Threading note: if you use {@link #async(Executor, Supplier)}, do not call most Bukkit API in the supplier.</p>
 */
public final class Suggest {
    private Suggest() {
    }

    public static @NotNull SuggestionProvider<CommandSourceStack> strings(final @NotNull Iterable<String> values) {
        Objects.requireNonNull(values, "values");
        return (ctx, builder) -> {
            final String remaining = builder.getRemainingLowerCase();
            for (final String value : values) {
                if (value == null) continue;
                if (remaining.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(value);
                }
            }
            return builder.buildFuture();
        };
    }

    public static @NotNull SuggestionProvider<CommandSourceStack> strings(final @NotNull String... values) {
        Objects.requireNonNull(values, "values");
        return strings(Arrays.asList(values));
    }

    public static <T> @NotNull SuggestionProvider<CommandSourceStack> from(final @NotNull Iterable<T> values, final @NotNull Function<T, String> toString) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(toString, "toString");
        return (ctx, builder) -> {
            final String remaining = builder.getRemainingLowerCase();
            for (final T value : values) {
                if (value == null) continue;
                final String s = toString.apply(value);
                if (s == null) continue;
                if (remaining.isEmpty() || s.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(s);
                }
            }
            return builder.buildFuture();
        };
    }

    public static <E extends Enum<E>> @NotNull SuggestionProvider<CommandSourceStack> enums(final @NotNull Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");
        return enums(enumClass, e -> e.name().toLowerCase(Locale.ROOT));
    }

    public static <E extends Enum<E>> @NotNull SuggestionProvider<CommandSourceStack> enums(final @NotNull Class<E> enumClass, final @NotNull Function<E, String> formatter) {
        Objects.requireNonNull(enumClass, "enumClass");
        Objects.requireNonNull(formatter, "formatter");
        return (ctx, builder) -> {
            final String remaining = builder.getRemainingLowerCase();
            for (final E constant : enumClass.getEnumConstants()) {
                final String s = formatter.apply(constant);
                if (s == null) continue;
                if (remaining.isEmpty() || s.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(s);
                }
            }
            return builder.buildFuture();
        };
    }

    public static @NotNull SuggestionProvider<CommandSourceStack> ints(final int... values) {
        Objects.requireNonNull(values, "values");
        return (ctx, builder) -> {
            final String remaining = builder.getRemainingLowerCase();
            for (final int value : values) {
                final String s = String.valueOf(value);
                if (remaining.isEmpty() || s.startsWith(remaining)) {
                    builder.suggest(value);
                }
            }
            return builder.buildFuture();
        };
    }

    /**
     * Suggests online player names with prefix filtering.
     *
     * <p>This is synchronous and safe for Bukkit API usage.</p>
     */
    public static @NotNull SuggestionProvider<CommandSourceStack> onlinePlayers() {
        return onlinePlayers(players -> true);
    }

    public static @NotNull SuggestionProvider<CommandSourceStack> onlinePlayers(final @NotNull Function<Player, Boolean> filter) {
        Objects.requireNonNull(filter, "filter");
        return (ctx, builder) -> {
            final String remaining = builder.getRemainingLowerCase();
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (!filter.apply(player)) continue;
                final String name = player.getName();
                if (remaining.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        };
    }

    /**
     * Wraps a suggestion builder in a CompletableFuture executed on the provided executor.
     *
     * <p>Only use this if the supplier does NOT touch Bukkit API, unless you're on a scheduler thread
     * you control that is safe for it.</p>
     */
    public static @NotNull SuggestionProvider<CommandSourceStack> async(final @NotNull Executor executor, final @NotNull Supplier<Collection<String>> supplier) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(supplier, "supplier");
        return (CommandContext<CommandSourceStack> ctx, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) ->
            CompletableFuture.supplyAsync(supplier, executor).thenApply(values -> {
                final String remaining = builder.getRemainingLowerCase();
                for (final String value : values) {
                    if (value == null) continue;
                    if (remaining.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                        builder.suggest(value);
                    }
                }
                return builder.build();
            });
    }
}


