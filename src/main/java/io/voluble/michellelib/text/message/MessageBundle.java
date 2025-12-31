package io.voluble.michellelib.text.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable snapshot of resolved message strings.
 */
public final class MessageBundle {
    private static final MessageBundle EMPTY = new MessageBundle(Collections.emptyMap());

    private final Map<String, String> rawByKey;

    private MessageBundle(final @NotNull Map<String, String> rawByKey) {
        this.rawByKey = Map.copyOf(Objects.requireNonNull(rawByKey, "rawByKey"));
    }

    public static @NotNull MessageBundle empty() {
        return EMPTY;
    }

    public static @NotNull MessageBundle of(final @NotNull Map<String, String> rawByKey) {
        if (rawByKey.isEmpty()) {
            return EMPTY;
        }
        return new MessageBundle(rawByKey);
    }

    public @NotNull Set<String> keys() {
        return rawByKey.keySet();
    }

    public @Nullable String raw(final @NotNull String key) {
        Objects.requireNonNull(key, "key");
        return rawByKey.get(key);
    }
}



