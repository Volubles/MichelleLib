package io.voluble.michellelib.menu.item;

import io.voluble.michellelib.menu.MenuSession;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Safe menu actions callable from inside click handlers.
 * <p>
 * Paper warns against calling open/close directly inside {@code InventoryClickEvent} because the inventory is mid-mutation.
 * These helpers schedule operations for the next tick on the player's owning entity scheduler.
 */
public final class MenuActions {
    private final MenuSession session;

    public MenuActions(final @NotNull MenuSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    /**
     * Run on the next tick on the player's owning thread.
     */
    public void nextTick(final @NotNull Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        session.runNextTick(runnable);
    }

    /**
     * Close the inventory safely (next tick).
     */
    public void close() {
        session.close();
    }

    /**
     * Run a "menu transition" safely (next tick).
     * <p>
     * Use this for actions that open another inventory/menu.
     */
    public void transition(final @NotNull Runnable runnable) {
        nextTick(runnable);
    }
}


