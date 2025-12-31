package io.voluble.michellelib.commands.tree;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.voluble.michellelib.commands.context.MichelleCommandContext;
import io.voluble.michellelib.commands.errors.CommandErrors;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A small DSL for declaring Brigadier command trees without repeating generics everywhere.
 *
 * <p>This still produces real Brigadier nodes; you can always "drop down" to Brigadier APIs
 * by calling {@link #brigadier()} on any node wrapper.</p>
 */
public final class CommandTree {
    private CommandTree() {
    }

    public static @NotNull Root root(final @NotNull String name) {
        Objects.requireNonNull(name, "name");
        return new Root(Commands.literal(name));
    }

    /**
     * Root command: behaves like a literal node, but also carries metadata (description + aliases)
     * used when registering with Paper.
     */
    public static final class Root extends LiteralNode<Root> {
        private final LiteralArgumentBuilder<CommandSourceStack> rootBuilder;
        private @Nullable String description;
        private List<String> aliases = List.of();

        private Root(final @NotNull LiteralArgumentBuilder<CommandSourceStack> builder) {
            super(builder);
            this.rootBuilder = builder;
        }

        public @NotNull Root description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        public @NotNull Root aliases(final @NotNull Collection<String> aliases) {
            Objects.requireNonNull(aliases, "aliases");
            this.aliases = List.copyOf(aliases);
            return this;
        }

        public @NotNull Root aliases(final @NotNull String... aliases) {
            Objects.requireNonNull(aliases, "aliases");
            return aliases(Arrays.asList(aliases));
        }

        public @NotNull CommandSpec spec() {
            return CommandSpec.of(rootBuilder, description, aliases);
        }
    }

    public interface Node {
        @NotNull ArgumentBuilder<CommandSourceStack, ?> brigadier();
    }

    /**
     * Common base implementation for node wrappers.
     */
    private abstract static class BaseNode<N extends BaseNode<N>> implements Node {
        private final ArgumentBuilder<CommandSourceStack, ?> builder;

        private BaseNode(final @NotNull ArgumentBuilder<CommandSourceStack, ?> builder) {
            this.builder = builder;
        }

        @Override
        public final @NotNull ArgumentBuilder<CommandSourceStack, ?> brigadier() {
            return builder;
        }

        protected final @NotNull N self() {
            @SuppressWarnings("unchecked")
            final N self = (N) this;
            return self;
        }

        public final @NotNull N requires(final @NotNull Predicate<CommandSourceStack> predicate) {
            Objects.requireNonNull(predicate, "predicate");
            builder.requires(predicate);
            return self();
        }

        /**
         * Requires a permission on the sender (not the executor).
         */
        public final @NotNull N requiresPermission(final @NotNull String permission) {
            Objects.requireNonNull(permission, "permission");
            return requires(source -> source.getSender().hasPermission(permission));
        }

        /**
         * Requires the sender to be a server operator.
         */
        public final @NotNull N requiresOp() {
            return requires(source -> source.getSender().isOp());
        }

        /**
         * Requires the sender to be console.
         *
         * <p>Note: This checks the sender, not the executor (so it still works with /execute contexts).</p>
         */
        public final @NotNull N requiresConsoleSender() {
            return requires(source -> source.getSender() instanceof ConsoleCommandSender);
        }

        /**
         * Requires the executor to be a player (the entity the command is executed "as").
         */
        public final @NotNull N requiresPlayerExecutor() {
            return requires(source -> source.getExecutor() instanceof Player);
        }

        /**
         * Restricts command execution to attended contexts, matching vanilla sensitive-command behavior.
         */
        public final @NotNull N restricted(final @NotNull Predicate<CommandSourceStack> predicate) {
            Objects.requireNonNull(predicate, "predicate");
            builder.requires(Commands.restricted(predicate));
            return self();
        }

        public final @NotNull N executes(final @NotNull Executor executor) {
            Objects.requireNonNull(executor, "executor");
            builder.executes(ctx -> executor.run(new MichelleCommandContext(ctx)));
            return self();
        }

        /**
         * Executes only if the command executor is a player, otherwise throws a configured command error.
         *
         * <p>This is a convenience to remove repetitive {@code instanceof Player} checks.</p>
         */
        public final @NotNull N executesPlayer(final @NotNull PlayerExecutor executor) {
            Objects.requireNonNull(executor, "executor");
            return executes(ctx -> {
                if (!(ctx.executor() instanceof Player player)) {
                    throw CommandErrors.failText("Only players can use this command.");
                }
                return executor.run(player, ctx);
            });
        }
    }

    /**
     * Literal node wrapper.
     */
    public static class LiteralNode<N extends LiteralNode<N>> extends BaseNode<N> {
        protected LiteralNode(final @NotNull LiteralArgumentBuilder<CommandSourceStack> builder) {
            super(builder);
        }

        public final @NotNull N literal(final @NotNull String literal, final @NotNull Consumer<LiteralNode<?>> child) {
            Objects.requireNonNull(literal, "literal");
            Objects.requireNonNull(child, "child");
            final LiteralArgumentBuilder<CommandSourceStack> raw = Commands.literal(literal);
            final LiteralNode<?> wrapper = new LiteralNode<>(raw);
            child.accept(wrapper);
            brigadier().then(raw);
            return self();
        }

        /**
         * Convenience overload for "literal + executes" without the nested lambda.
         *
         * <p>We intentionally keep this as a different method name to avoid overload ambiguity
         * between Consumer- and Executor-style lambdas.</p>
         */
        public final @NotNull N literalExec(final @NotNull String literal, final @NotNull Executor executor) {
            return literal(literal, node -> node.executes(executor));
        }

        public final <T> @NotNull N argument(final @NotNull String name, final @NotNull ArgumentType<T> type, final @NotNull Consumer<ArgumentNode<T>> child) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(child, "child");
            final RequiredArgumentBuilder<CommandSourceStack, T> raw = Commands.argument(name, type);
            final ArgumentNode<T> wrapper = new ArgumentNode<>(raw);
            child.accept(wrapper);
            brigadier().then(raw);
            return self();
        }

        /**
         * Convenience overload for "argument + executes" without the nested lambda.
         *
         * <p>We intentionally keep this as a different method name to avoid overload ambiguity
         * between Consumer- and Executor-style lambdas.</p>
         */
        public final <T> @NotNull N argumentExec(final @NotNull String name, final @NotNull ArgumentType<T> type, final @NotNull Executor executor) {
            return argument(name, type, node -> node.executes(executor));
        }
    }

    /**
     * Required argument node wrapper.
     */
    public static final class ArgumentNode<T> extends BaseNode<ArgumentNode<T>> {
        private final RequiredArgumentBuilder<CommandSourceStack, T> argBuilder;

        private ArgumentNode(final @NotNull RequiredArgumentBuilder<CommandSourceStack, T> builder) {
            super(builder);
            this.argBuilder = builder;
        }

        public @NotNull ArgumentNode<T> suggests(final @NotNull SuggestionProvider<CommandSourceStack> provider) {
            Objects.requireNonNull(provider, "provider");
            argBuilder.suggests(provider);
            return this;
        }

        /**
         * Convenience helper for common string suggestions with basic prefix filtering.
         */
        public @NotNull ArgumentNode<T> suggestStrings(final @NotNull Iterable<String> values) {
            Objects.requireNonNull(values, "values");
            return suggests((ctx, builder) -> {
                final String remaining = builder.getRemainingLowerCase();
                for (final String value : values) {
                    if (value == null) continue;
                    if (remaining.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                        builder.suggest(value);
                    }
                }
                return builder.buildFuture();
            });
        }

        public @NotNull ArgumentNode<T> literal(final @NotNull String literal, final @NotNull Consumer<LiteralNode<?>> child) {
            Objects.requireNonNull(literal, "literal");
            Objects.requireNonNull(child, "child");
            final LiteralArgumentBuilder<CommandSourceStack> raw = Commands.literal(literal);
            final LiteralNode<?> wrapper = new LiteralNode<>(raw);
            child.accept(wrapper);
            brigadier().then(raw);
            return this;
        }

        public @NotNull ArgumentNode<T> literalExec(final @NotNull String literal, final @NotNull Executor executor) {
            return literal(literal, node -> node.executes(executor));
        }

        public <T2> @NotNull ArgumentNode<T> argument(final @NotNull String name, final @NotNull ArgumentType<T2> type, final @NotNull Consumer<ArgumentNode<T2>> child) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(child, "child");
            final RequiredArgumentBuilder<CommandSourceStack, T2> raw = Commands.argument(name, type);
            final ArgumentNode<T2> wrapper = new ArgumentNode<>(raw);
            child.accept(wrapper);
            brigadier().then(raw);
            return this;
        }

        public <T2> @NotNull ArgumentNode<T> argumentExec(final @NotNull String name, final @NotNull ArgumentType<T2> type, final @NotNull Executor executor) {
            return argument(name, type, node -> node.executes(executor));
        }
    }

    /**
     * Small adapter that matches Brigadier's "int return" executor without exposing generics everywhere.
     */
    @FunctionalInterface
    public interface Executor {
        int run(MichelleCommandContext ctx) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface PlayerExecutor {
        int run(Player player, MichelleCommandContext ctx) throws CommandSyntaxException;
    }

    /**
     * Most commands should return {@link Command#SINGLE_SUCCESS}.
     */
    public static int ok() {
        return Command.SINGLE_SUCCESS;
    }
}


