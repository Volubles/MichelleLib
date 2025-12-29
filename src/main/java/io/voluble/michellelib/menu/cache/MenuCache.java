package io.voluble.michellelib.menu.cache;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple cache backing menus: a global cache (shared by key) and a per-session cache keyed by player UUID.
 * <p>
 * This is intentionally untyped; consumers can wrap it with their own typed APIs.
 */
public final class MenuCache {
    private final Map<String, Object> global = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Object>> session = new ConcurrentHashMap<>();

    public @Nullable Object getGlobal(final String key) {
        return global.get(key);
    }

    public void putGlobal(final String key, final Object value) {
        global.put(key, value);
    }

    public <T> @Nullable T getGlobal(final String key, final Class<T> type) {
        Object v = global.get(key);
        return type.isInstance(v) ? type.cast(v) : null;
    }

    public @Nullable Object getSession(final UUID player, final String key) {
        return session.computeIfAbsent(player, u -> new ConcurrentHashMap<>()).get(key);
    }

    public void putSession(final UUID player, final String key, final Object value) {
        session.computeIfAbsent(player, u -> new ConcurrentHashMap<>()).put(key, value);
    }

    public <T> @Nullable T getSession(final UUID player, final String key, final Class<T> type) {
        Object v = session.computeIfAbsent(player, u -> new ConcurrentHashMap<>()).get(key);
        return type.isInstance(v) ? type.cast(v) : null;
    }

    public void clearSession(final UUID player) {
        session.remove(player);
    }

    public void clearAll() {
        session.clear();
        global.clear();
    }
}


