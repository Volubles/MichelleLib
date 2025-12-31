package io.voluble.michellelib.cooldown;

import io.voluble.michellelib.config.YamlDocument;
import io.voluble.michellelib.config.YamlSnapshot;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * YAML-based cooldown persistence.
 *
 * <p>Disk I/O is performed by the owning {@link YamlDocument} methods and should be scheduled appropriately.</p>
 */
public final class YamlCooldownPersistence implements CooldownPersistence {
    private static final int VERSION = 1;

    private final YamlDocument document;

    public YamlCooldownPersistence(final @NotNull YamlDocument document) {
        this.document = Objects.requireNonNull(document, "document");
    }

    public @NotNull YamlDocument document() {
        return document;
    }

    @Override
    public void loadInto(final @NotNull CooldownStore store) {
        Objects.requireNonNull(store, "store");

        document.reload();
        final YamlSnapshot snap = document.snapshot();

        final Object raw = snap.get("cooldowns");
        if (!(raw instanceof Map<?, ?> map)) {
            return;
        }

        final long now = System.currentTimeMillis();
        for (final Map.Entry<?, ?> ownerEntry : map.entrySet()) {
            if (!(ownerEntry.getKey() instanceof String ownerKey)) {
                continue;
            }
            final UUID owner;
            try {
                owner = UUID.fromString(ownerKey);
            } catch (final IllegalArgumentException ignored) {
                continue;
            }

            if (!(ownerEntry.getValue() instanceof Map<?, ?> perOwner)) {
                continue;
            }

            for (final Map.Entry<?, ?> e : perOwner.entrySet()) {
                if (!(e.getKey() instanceof String key)) {
                    continue;
                }
                final long expiresAtMillis = toLong(e.getValue(), -1L);
                if (expiresAtMillis <= now) {
                    continue;
                }
                final long remainingMillis = expiresAtMillis - now;
                store.tryUse(new CooldownId(owner, key), java.time.Duration.ofMillis(remainingMillis));
            }
        }
    }

    @Override
    public void saveFrom(final @NotNull CooldownStore store) {
        Objects.requireNonNull(store, "store");

        final Map<CooldownId, Long> exported = store.exportExpiryEpochMillis();
        final Map<String, Map<String, Long>> byOwner = new LinkedHashMap<>();
        for (final Map.Entry<CooldownId, Long> e : exported.entrySet()) {
            final String owner = e.getKey().owner().toString();
            byOwner.computeIfAbsent(owner, u -> new LinkedHashMap<>()).put(e.getKey().key(), e.getValue());
        }

        document.edit((final YamlConfiguration yaml) -> {
            yaml.set("version", VERSION);
            yaml.set("cooldowns", null);
            for (final Map.Entry<String, Map<String, Long>> ownerEntry : byOwner.entrySet()) {
                final String base = "cooldowns." + ownerEntry.getKey();
                for (final Map.Entry<String, Long> cd : ownerEntry.getValue().entrySet()) {
                    yaml.set(base + "." + cd.getKey(), cd.getValue());
                }
            }
        });
        document.save();
    }

    private static long toLong(final Object value, final long def) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (final NumberFormatException ignored) {
            }
        }
        return def;
    }
}



