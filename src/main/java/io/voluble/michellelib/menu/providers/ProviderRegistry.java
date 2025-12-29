package io.voluble.michellelib.menu.providers;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple type -> instance registry for menu-related providers.
 * <p>
 * This exists to keep the menu module extensible without hard dependencies.
 */
public final class ProviderRegistry {
    private final Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    public <T> void register(final Class<T> type, final T impl) {
        map.put(type, impl);
    }

    public <T> @Nullable T get(final Class<T> type) {
        Object v = map.get(type);
        return v == null ? null : type.cast(v);
    }

    public <T> void unregister(final Class<T> type) {
        map.remove(type);
    }

    public void clear() {
        map.clear();
    }
}


