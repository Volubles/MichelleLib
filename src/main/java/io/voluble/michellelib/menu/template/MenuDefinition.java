package io.voluble.michellelib.menu.template;

import io.voluble.michellelib.menu.CursorClosePolicy;
import io.voluble.michellelib.menu.MenuService;
import io.voluble.michellelib.menu.MenuSession;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * High-level menu definition that can be opened for any player.
 * <p>
 * This is the preferred abstraction for most plugins. It stays safe by default:
 * - any open/close transitions from click handlers should be done via {@link io.voluble.michellelib.menu.item.MenuActions}
 * - lifecycle hooks run on the player's owning entity thread (Folia-safe)
 */
public final class MenuDefinition {
    private final MenuType type;
    private final Function<MenuOpenTarget, Component> titleProvider;
    private final long refreshPeriodTicks;
    private final CursorClosePolicy cursorPolicy;
    private final @Nullable String globalCacheKey;
    private final Consumer<MenuContext> onOpen;
    private final Consumer<MenuCloseContext> onClose;

    MenuDefinition(
        final @NotNull MenuType type,
        final @NotNull Function<MenuOpenTarget, Component> titleProvider,
        final long refreshPeriodTicks,
        final @NotNull CursorClosePolicy cursorPolicy,
        final @Nullable String globalCacheKey,
        final @NotNull Consumer<MenuContext> onOpen,
        final @NotNull Consumer<MenuCloseContext> onClose
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.titleProvider = Objects.requireNonNull(titleProvider, "titleProvider");
        this.refreshPeriodTicks = refreshPeriodTicks;
        this.cursorPolicy = Objects.requireNonNull(cursorPolicy, "cursorPolicy");
        this.globalCacheKey = globalCacheKey;
        this.onOpen = Objects.requireNonNull(onOpen, "onOpen");
        this.onClose = Objects.requireNonNull(onClose, "onClose");
    }

    public static @NotNull MenuBuilder builder(final @NotNull MenuType type) {
        return new MenuBuilder(type);
    }

    public static @NotNull MenuBuilder chest(final int rows) {
        return builder(new MenuType.ChestRows(rows));
    }

    public static @NotNull MenuBuilder inventory(final @NotNull org.bukkit.event.inventory.InventoryType type) {
        return builder(new MenuType.InventoryKind(type));
    }

    public void open(final @NotNull MenuService menus, final @NotNull Player player) {
        Objects.requireNonNull(menus, "menus");
        Objects.requireNonNull(player, "player");

        final MenuSession session = menus.session(player);
        final MenuOpenTarget target = new MenuOpenTarget(menus, session, player);
        final MenuContext ctx = new MenuContext(this, target);

        // Configure session behavior for this menu
        session.setCursorPolicy(cursorPolicy);
        if (globalCacheKey != null) session.setGlobalCacheKey(globalCacheKey);
        if (refreshPeriodTicks > 0L) session.startRefreshHeartbeat(refreshPeriodTicks);

        // Install lifecycle hooks for this menu
        session.setLifecycleHooks(
            () -> onOpen.accept(ctx),
            willReopen -> onClose.accept(new MenuCloseContext(ctx, willReopen))
        );

        // Open inventory (session handles click-safety deferral if needed)
        type.open(target, titleProvider);
    }
}


