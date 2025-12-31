package io.voluble.michellelib.cooldown;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe cooldown store.
 */
public final class InMemoryCooldownStore implements CooldownStore {
    private final CooldownClock clock;
    private final ConcurrentMap<CooldownId, Entry> entries = new ConcurrentHashMap<>();

    public InMemoryCooldownStore(final @NotNull CooldownClock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public @NotNull CooldownResult tryUse(final @NotNull CooldownId id, final @NotNull Duration cooldown) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(cooldown, "cooldown");

        final long nowNano = clock.nowNanoTime();
        final long nowMillis = clock.nowEpochMillis();
        final long cdNanos = Math.max(0L, cooldown.toNanos());
        final long cdMillis = Math.max(0L, cooldown.toMillis());

        final Entry out = entries.compute(id, (k, existing) -> {
            if (existing != null) {
                final long remainingNanos = existing.expiresAtNano - nowNano;
                if (remainingNanos > 0L) {
                    return existing;
                }
            }
            return new Entry(nowNano + cdNanos, nowMillis + cdMillis);
        });

        final long remaining = out.expiresAtNano - nowNano;
        if (remaining <= 0L) {
            return CooldownResult.permit();
        }
        if (cdNanos == 0L) {
            return CooldownResult.permit();
        }

        final boolean isNew = out.expiresAtNano == nowNano + cdNanos;
        if (isNew) {
            return CooldownResult.permit();
        }
        return CooldownResult.denied(Duration.ofNanos(remaining));
    }

    @Override
    public @NotNull Duration remaining(final @NotNull CooldownId id) {
        Objects.requireNonNull(id, "id");
        final Entry e = entries.get(id);
        if (e == null) {
            return Duration.ZERO;
        }
        final long remaining = e.expiresAtNano - clock.nowNanoTime();
        if (remaining <= 0L) {
            entries.remove(id, e);
            return Duration.ZERO;
        }
        return Duration.ofNanos(remaining);
    }

    @Override
    public void clear(final @NotNull CooldownId id) {
        Objects.requireNonNull(id, "id");
        entries.remove(id);
    }

    @Override
    public void clearOwner(final @NotNull UUID owner) {
        Objects.requireNonNull(owner, "owner");
        entries.keySet().removeIf(id -> owner.equals(id.owner()));
    }

    @Override
    public void clearAll() {
        entries.clear();
    }

    @Override
    public int pruneExpired() {
        final long nowNano = clock.nowNanoTime();
        int removed = 0;
        for (final var it = entries.entrySet().iterator(); it.hasNext(); ) {
            final var e = it.next();
            if (e.getValue().expiresAtNano - nowNano <= 0L) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    @Override
    public @NotNull OptionalLong expiryEpochMillis(final @NotNull CooldownId id) {
        Objects.requireNonNull(id, "id");
        final Entry e = entries.get(id);
        if (e == null) {
            return OptionalLong.empty();
        }
        final long remaining = e.expiresAtNano - clock.nowNanoTime();
        if (remaining <= 0L) {
            entries.remove(id, e);
            return OptionalLong.empty();
        }
        return OptionalLong.of(e.expiresAtEpochMillis);
    }

    @Override
    public @NotNull Map<CooldownId, Long> exportExpiryEpochMillis() {
        final long nowNano = clock.nowNanoTime();
        final Map<CooldownId, Long> out = new HashMap<>();
        for (final var e : entries.entrySet()) {
            final Entry v = e.getValue();
            if (v.expiresAtNano - nowNano <= 0L) {
                entries.remove(e.getKey(), v);
                continue;
            }
            out.put(e.getKey(), v.expiresAtEpochMillis);
        }
        return Map.copyOf(out);
    }

    public int size() {
        return entries.size();
    }

    private record Entry(long expiresAtNano, long expiresAtEpochMillis) {
    }
}


