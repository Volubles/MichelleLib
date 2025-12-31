package io.voluble.michellelib.text.placeholders;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * TagResolvers for integrating external placeholder systems with MiniMessage.
 *
 * <p>See Paper docs: {@code dep/Paper-docs/docs/adventure/faq.md}.</p>
 */
public final class PlaceholderResolvers {

    private PlaceholderResolvers() {
    }

    /**
     * A MiniMessage tag of the format {@code <papi:luckperms_prefix>} which expands using PlaceholderAPI.
     *
     * <p>Returns {@link TagResolver#empty()} when PlaceholderAPI is not installed.</p>
     */
    public static @NotNull TagResolver papiTag(final @NotNull Player player) {
        if (!PlaceholderApi.isInstalled()) {
            return TagResolver.empty();
        }

        return TagResolver.resolver("papi", (final @NotNull ArgumentQueue args, final @NotNull net.kyori.adventure.text.minimessage.Context ctx) -> {
            final String placeholder = args.popOr("papi tag requires an argument").value();

            // PlaceholderAPI placeholders are typically wrapped with %...%
            final String expanded = PlaceholderApi.expand(player, "%" + placeholder + "%");
            final String legacyText = expanded == null ? "" : expanded;

            // PlaceholderAPI output is often legacy-colored - convert to component.
            final Component parsed = LegacyComponentSerializer.legacySection().deserialize(legacyText);
            return Tag.selfClosingInserting(parsed);
        });
    }

    /**
     * Convenience method for combining multiple resolvers.
     */
    public static @NotNull TagResolver combine(final @NotNull TagResolver... resolvers) {
        final TagResolver.Builder builder = TagResolver.builder();
        for (final TagResolver resolver : resolvers) {
            builder.resolver(resolver);
        }
        return builder.build();
    }
}


