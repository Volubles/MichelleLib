package io.voluble.michellelib.text.message;

import org.jetbrains.annotations.NotNull;

/**
 * Typed message key for YAML-backed message bundles.
 *
 * <p>Plugins commonly implement this with an enum.</p>
 */
public interface MessageKey {
    @NotNull String path();
}



