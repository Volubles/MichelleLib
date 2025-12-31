package io.voluble.michellelib.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable snapshot of YAML data suitable for safe concurrent reads.
 */
public final class YamlSnapshot {
    private static final YamlSnapshot EMPTY = new YamlSnapshot(Collections.emptyMap());

    private final Map<String, Object> root;

    YamlSnapshot(final @NotNull Map<String, Object> root) {
        this.root = Map.copyOf(Objects.requireNonNull(root, "root"));
    }

    public static @NotNull YamlSnapshot empty() {
        return EMPTY;
    }

    public @NotNull Set<String> keys() {
        return root.keySet();
    }

    public @Nullable Object get(final @NotNull String path) {
        Objects.requireNonNull(path, "path");
        if (path.isEmpty()) {
            return root;
        }

        Object current = root;
        for (final String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> m)) {
                return null;
            }
            current = m.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public @NotNull String getString(final @NotNull String path, final @NotNull String def) {
        final Object v = get(path);
        if (v instanceof String s) {
            return s;
        }
        return def;
    }

    public int getInt(final @NotNull String path, final int def) {
        final Object v = get(path);
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (final NumberFormatException ignored) {
            }
        }
        return def;
    }

    public boolean getBoolean(final @NotNull String path, final boolean def) {
        final Object v = get(path);
        if (v instanceof Boolean b) {
            return b;
        }
        if (v instanceof String s) {
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
        }
        return def;
    }

    public double getDouble(final @NotNull String path, final double def) {
        final Object v = get(path);
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        if (v instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (final NumberFormatException ignored) {
            }
        }
        return def;
    }

    public @NotNull List<String> getStringList(final @NotNull String path) {
        final Object v = get(path);
        if (v instanceof List<?> list && !list.isEmpty()) {
            boolean allStrings = true;
            for (final Object o : list) {
                if (!(o instanceof String)) {
                    allStrings = false;
                    break;
                }
            }
            if (allStrings) {
                @SuppressWarnings("unchecked")
                final List<String> cast = (List<String>) list;
                return List.copyOf(cast);
            }
        }
        if (v instanceof String s && !s.isEmpty()) {
            return List.of(s);
        }
        return List.of();
    }

    public @Nullable YamlSnapshot section(final @NotNull String path) {
        final Object v = get(path);
        if (v instanceof Map<?, ?> m) {
            final Map<String, Object> out = new java.util.LinkedHashMap<>();
            for (final Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof String k) {
                    out.put(k, e.getValue());
                }
            }
            return out.isEmpty() ? YamlSnapshot.empty() : new YamlSnapshot(out);
        }
        return null;
    }

    static @NotNull Object deepFreeze(final @Nullable Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Map<?, ?> m) {
            final java.util.LinkedHashMap<String, Object> out = new java.util.LinkedHashMap<>();
            for (final Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof String k) {
                    out.put(k, deepFreeze(e.getValue()));
                }
            }
            return Map.copyOf(out);
        }
        if (value instanceof List<?> list) {
            final ArrayList<Object> out = new ArrayList<>(list.size());
            for (final Object o : list) {
                out.add(deepFreeze(o));
            }
            return List.copyOf(out);
        }
        return value;
    }
}



