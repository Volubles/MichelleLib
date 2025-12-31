package io.voluble.michellelib.config;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Utility for copying embedded plugin resources into the plugin data folder.
 */
public final class ResourceFileCopier {
    private ResourceFileCopier() {
    }

    /**
     * Copies a resource from the plugin JAR to the target path if the target does not exist.
     *
     * @return true if a copy was performed, false if the target already existed or the resource was not found
     */
    public static boolean copyIfMissing(
            final @NotNull Plugin plugin,
            final @NotNull String resourcePath,
            final @NotNull Path target
    ) throws IOException {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(resourcePath, "resourcePath");
        Objects.requireNonNull(target, "target");

        if (Files.exists(target)) {
            return false;
        }

        final Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        final @Nullable InputStream resource = plugin.getResource(resourcePath);
        if (resource == null) {
            return false;
        }

        final Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try (resource) {
            Files.copy(resource, tmp, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException ignored) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (final IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (final IOException ignored) {
            }
            throw e;
        }
    }
}



