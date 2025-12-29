package io.voluble.michellelib.menu;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.voluble.michellelib.menu.internal.MenuHolder;
import io.voluble.michellelib.menu.item.ClickContext;
import io.voluble.michellelib.menu.item.MenuActions;
import io.voluble.michellelib.menu.item.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MenuSession {
    private final MenuService menus;
    private final Player player;

    private InventoryView view;
    private final Map<Integer, MenuItem> slots = new HashMap<>();
    private final Deque<Runnable> backStack = new ArrayDeque<>();

    private boolean preventClose = false;
    private String globalCacheKey = "";
    private long viewToken = 0L;

    private boolean refreshHeartbeatActive = false;
    private long refreshPeriodTicks = 0L;
    private ScheduledTask refreshTask;

    private long lastActionAtNs = 0L;
    private boolean inHandler = false;

    private CursorClosePolicy cursorPolicy = CursorClosePolicy.RETURN;

    // lifecycle hooks for the currently open "high level" menu (MenuDefinition)
    private Runnable onOpenHook = () -> {};
    private java.util.function.Consumer<Boolean> onCloseHook = willReopen -> {};

    public MenuSession(final MenuService menus, final Player player) {
        this.menus = menus;
        this.player = player;
    }

    public Player player() {
        return player;
    }

    public InventoryView view() {
        return view;
    }

    public long currentViewToken() {
        return viewToken;
    }

    public void open(final InventoryType inventoryType, final Component title) {
        openInternal(() -> Bukkit.createInventory(
            new MenuHolder(player.getUniqueId(), viewToken),
            inventoryType,
            title
        ));
    }

    /**
     * Open a chest-style menu with a custom number of rows (1-6).
     */
    public void open(final int rows, final Component title) {
        int validRows = Math.max(1, Math.min(6, rows));
        int size = validRows * 9;
        openInternal(() -> Bukkit.createInventory(
            new MenuHolder(player.getUniqueId(), viewToken),
            size,
            title
        ));
    }

    private void openInternal(final java.util.function.Supplier<Inventory> inventorySupplier) {
        // If called from inside InventoryClickEvent, schedule to next tick to avoid Bukkit inventory mutation issues.
        if (inHandler) {
            menus.scheduler().runEntityDelayed(player, 1L, () -> openInternal(inventorySupplier));
            return;
        }

        menus.scheduler().runEntity(player, () -> {
            // mark that we're intentionally transitioning to a new menu (suppress close handling)
            this.preventClose = true;

            // stop old heartbeat / invalidate old view tasks
            cancelRefreshTask();

            // Return items from the previous menu before clearing slots
            returnItemsFromSlots();

            // increment view token and clear previous menu items
            this.viewToken++;
            this.slots.clear();

            Inventory inv = inventorySupplier.get();
            this.view = player.openInventory(inv);

            // High-level menu lifecycle hook (runs after inventory is opened).
            try {
                onOpenHook.run();
            } catch (Throwable ignored) {
            }

            // allow closes again on next tick (entity scheduler delay <1 treated as 1)
            menus.scheduler().runEntityDelayed(player, 1L, () -> this.preventClose = false);

            // restart heartbeat if configured
            if (refreshPeriodTicks > 0L) {
                startRefreshHeartbeat(refreshPeriodTicks);
            }
        });
    }

    /**
     * Close the currently open inventory safely. If called from inside InventoryClickEvent, this is deferred to next tick.
     */
    public void close() {
        runNextTick(player::closeInventory);
    }

    /**
     * Run on next tick on the player's owning entity thread (safe to call from InventoryClickEvent).
     */
    public void runNextTick(final Runnable runnable) {
        if (runnable == null) return;
        menus.scheduler().runEntityDelayed(player, 1L, runnable);
    }

    public void setGlobalCacheKey(final String key) {
        this.globalCacheKey = key;
    }

    public String globalCacheKey() {
        return globalCacheKey;
    }

    public void setPreventClose(final boolean prevent) {
        this.preventClose = prevent;
    }

    public boolean isPreventClose() {
        return preventClose;
    }

    public void setCursorPolicy(final CursorClosePolicy policy) {
        this.cursorPolicy = policy;
    }

    public void setItem(final int slot, final MenuItem item) {
        if (item == null) return;
        menus.scheduler().runEntity(player, () -> {
            if (view == null) return;
            int size = view.getTopInventory().getSize();
            if (slot < 0 || slot >= size) return;

            slots.put(slot, item);

            // Ensure scheduled render still targets the same view instance via holder token
            InventoryHolder h = view.getTopInventory().getHolder(false);
            if (!(h instanceof MenuHolder mh) || mh.viewToken() != this.viewToken) return;

            ItemStack stack = item.render(player);
            ItemStack current = view.getTopInventory().getItem(slot);
            if (!stacksEqual(current, stack)) {
                view.getTopInventory().setItem(slot, stack);
            }
        });
    }

    public void clearItem(final int slot) {
        menus.scheduler().runEntity(player, () -> {
            if (view == null) return;
            int size = view.getTopInventory().getSize();
            if (slot < 0 || slot >= size) return;
            slots.remove(slot);

            InventoryHolder h = view.getTopInventory().getHolder(false);
            if (!(h instanceof MenuHolder mh) || mh.viewToken() != this.viewToken) return;
            view.getTopInventory().setItem(slot, null);
        });
    }

    public Optional<MenuItem> getItem(final int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    public void handleClose(final boolean willReopen) {
        // If we're intentionally transitioning to another menu, skip close handling
        if (willReopen || this.preventClose) return;
        if (this.view == null) return;

        Inventory top = this.view.getTopInventory();
        InventoryHolder holder = top.getHolder(false);
        if (!(holder instanceof MenuHolder mh)) return;
        if (mh.viewToken() != this.viewToken) return;

        // High-level lifecycle hook runs before we clear items/slots.
        try {
            onCloseHook.accept(willReopen);
        } catch (Throwable ignored) {
        }

        // invalidate any scheduled tasks tied to the previous view and stop heartbeat
        this.viewToken++;
        cancelRefreshTask();

        // Ask items if they wish to return any items on close
        returnItemsFromSlots();
        this.slots.clear();

        // Handle cursor policy
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            switch (cursorPolicy) {
                case RETURN -> {
                    Inventory bottom = view.getBottomInventory();
                    int empty = bottom.firstEmpty();
                    if (empty >= 0) {
                        bottom.setItem(empty, cursor.clone());
                        player.setItemOnCursor(null);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), cursor.clone());
                        player.setItemOnCursor(null);
                    }
                }
                case DROP -> {
                    player.getWorld().dropItemNaturally(player.getLocation(), cursor.clone());
                    player.setItemOnCursor(null);
                }
                case VOID -> player.setItemOnCursor(null);
            }
        }
    }

    public void handleClick(final InventoryClickEvent event) {
        // basic debounce: skip if within 150ms of last action or re-entrant
        long now = System.nanoTime();
        if ((now - lastActionAtNs) < menus.settings().clickDebounceNs() || inHandler) {
            event.setCancelled(true);
            return;
        }
        lastActionAtNs = now;
        inHandler = true;

        final long tokenAtEntry = this.viewToken;
        final int rawSlot = event.getRawSlot();
        final int topSize = view.getTopInventory().getSize();
        final boolean isTop = rawSlot >= 0 && rawSlot < topSize;
        // local slot index for whichever inventory is clicked
        final int slot = event.getSlot();
        ClickType type = event.getClick();
        InventoryAction action = event.getAction();
        boolean shift = event.isShiftClick();
        // menu slots are indexed by TOP inventory slot indices (0..topSize-1)
        final MenuItem item = isTop ? slots.get(slot) : null;

        if (!isTop) {
            if (action != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(false);
                inHandler = false;
                return;
            }
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
            if (!(type == ClickType.LEFT || type == ClickType.RIGHT || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT || type == ClickType.NUMBER_KEY)) {
                inHandler = false;
                return;
            }
            if (item == null) {
                inHandler = false;
                return;
            }
        }

        ItemStack originalCursor = event.getCursor() == null ? null : event.getCursor().clone();
        ItemStack originalTop = isTop ? (view.getTopInventory().getItem(slot) == null ? null : view.getTopInventory().getItem(slot).clone()) : null;
        final ItemStack[] bottomSnapshot = new ItemStack[1];
        final boolean fIsTop = isTop;
        final ClickType fType = type;
        final int fSlot = slot;
        final int fHotbar = event.getHotbarButton();

        try {
            if (isTop && type == ClickType.NUMBER_KEY) {
                if (item == null || !item.isPlaceable() || !item.allowNumberKeyPlace()) {
                    inHandler = false;
                    return;
                }
            }

            boolean handledPlacement = false;
            boolean needsClientSync = false;
            Inventory topInv = view.getTopInventory();
            Inventory bottomInv = view.getBottomInventory();
            final MenuActions actions = new MenuActions(this);

            // Placeable flows
            if (isTop && item != null && item.isPlaceable()) {
                if (type == ClickType.NUMBER_KEY && item.allowNumberKeyPlace()) {
                    int hb = event.getHotbarButton();
                    if (hb >= 0) {
                        ItemStack hotbarStack = bottomInv.getItem(hb);
                        if (hotbarStack != null && !hotbarStack.getType().isAir()) {
                            bottomSnapshot[0] = hotbarStack.clone();
                            ClickContext ctx = new ClickContext(player, type, slot, topInv, bottomInv, event.getCursor(), true, false, actions);
                            if (item.canAccept(hotbarStack, ctx)) {
                                int accepted = Math.min(item.maxAcceptAmount(hotbarStack, ctx), hotbarStack.getAmount());
                                if (accepted > 0) {
                                    ItemStack inserted = hotbarStack.clone();
                                    inserted.setAmount(accepted);

                                    hotbarStack.setAmount(hotbarStack.getAmount() - accepted);
                                    bottomInv.setItem(hb, hotbarStack.getAmount() <= 0 ? null : hotbarStack);

                                    item.onInsert(inserted, ctx);
                                    handledPlacement = true;
                                    needsClientSync = true;

                                    // re-render touched top slot
                                    menus.scheduler().runEntity(player, () -> {
                                        if (this.viewToken != tokenAtEntry) return;
                                        ItemStack updated = item.render(player);
                                        ItemStack cur = topInv.getItem(slot);
                                        if (!stacksEqual(cur, updated)) {
                                            topInv.setItem(slot, updated);
                                        }
                                    });
                                }
                            }
                        }
                    }
                } else if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                    ClickContext ctx = new ClickContext(player, type, slot, topInv, bottomInv, event.getCursor(), true, shift, actions);
                    ItemStack cursor = event.getCursor();
                    if (item.canAccept(cursor, ctx)) {
                        int cap = item.maxAcceptAmount(cursor, ctx);
                        int rightCap = (type == ClickType.RIGHT) ? 1 : cursor.getAmount();
                        int accepted = Math.min(Math.min(cap, rightCap), cursor.getAmount());
                        if (accepted > 0) {
                            ItemStack inserted = cursor.clone();
                            inserted.setAmount(accepted);

                            cursor.setAmount(cursor.getAmount() - accepted);
                            player.setItemOnCursor(cursor.getAmount() <= 0 ? null : cursor);

                            item.onInsert(inserted, ctx);
                            handledPlacement = true;
                            needsClientSync = true;

                            menus.scheduler().runEntity(player, () -> {
                                if (this.viewToken != tokenAtEntry) return;
                                ItemStack updated = item.render(player);
                                ItemStack cur = topInv.getItem(slot);
                                if (!stacksEqual(cur, updated)) {
                                    topInv.setItem(slot, updated);
                                }
                            });
                        }
                    }
                }
            }

            // If number-key on top, always stop here (handled or not)
            if (isTop && type == ClickType.NUMBER_KEY) {
                inHandler = false;
                return;
            }

            // Shift-move from bottom to top placeable slot(s)
            if (!handledPlacement && !isTop && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                ItemStack origin = event.getCurrentItem();
                if (origin != null && !origin.getType().isAir()) {
                    bottomSnapshot[0] = origin.clone();
                    int targetSlot = findFirstEligiblePlaceableSlot(origin);
                    if (targetSlot >= 0) {
                        MenuItem targetItem = slots.get(targetSlot);
                        ClickContext ctx = new ClickContext(player, type, targetSlot, topInv, bottomInv, null, true, true, actions);
                        if (targetItem != null && targetItem.isPlaceable() && targetItem.allowShiftInsert() && targetItem.canAccept(origin, ctx)) {
                            int accepted = Math.min(targetItem.maxAcceptAmount(origin, ctx), origin.getAmount());
                            if (accepted > 0) {
                                ItemStack inserted = origin.clone();
                                inserted.setAmount(accepted);

                                origin.setAmount(origin.getAmount() - accepted);
                                bottomInv.setItem(slot, origin.getAmount() <= 0 ? null : origin);

                                targetItem.onInsert(inserted, ctx);
                                handledPlacement = true;
                                needsClientSync = true;

                                int s = targetSlot;
                                menus.scheduler().runEntity(player, () -> {
                                    if (this.viewToken != tokenAtEntry) return;
                                    ItemStack updated = targetItem.render(player);
                                    ItemStack cur = topInv.getItem(s);
                                    if (!stacksEqual(cur, updated)) {
                                        topInv.setItem(s, updated);
                                    }
                                });
                            }
                        }
                    }
                }

                if (!handledPlacement) {
                    inHandler = false;
                    return;
                }
            }

            // Fall back to normal item click handling if not handled by placement
            if (!handledPlacement && item != null) {
                ClickContext ctx = new ClickContext(player, type, slot, topInv, bottomInv, event.getCursor(), isTop, shift, actions);
                boolean cancel = item.onClick(ctx);
                event.setCancelled(cancel);

                menus.scheduler().runEntity(player, () -> {
                    if (this.viewToken != tokenAtEntry) return;
                    ItemStack updated = item.render(player);
                    ItemStack cur = view.getTopInventory().getItem(slot);
                    if (!stacksEqual(cur, updated)) {
                        view.getTopInventory().setItem(slot, updated);
                    }
                });
            }

            // If we mutated cursor/hotbar/bottom inventory manually, force a client sync next tick.
            // Paper warns inventory mutations during click can be overwritten/desynced; this avoids ghost items/dupes.
            if (needsClientSync && menus.settings().syncClientAfterManualMutation()) {
                runNextTick(player::updateInventory);
            }
        } catch (Throwable t) {
            menus.scheduler().runEntity(player, () -> {
                if (this.viewToken != tokenAtEntry) return;
                if (fIsTop) view.getTopInventory().setItem(fSlot, originalTop);
                player.setItemOnCursor(originalCursor);
                if (!fIsTop && bottomSnapshot[0] != null) {
                    view.getBottomInventory().setItem(fSlot, bottomSnapshot[0]);
                }
                if (fIsTop && fType == ClickType.NUMBER_KEY && bottomSnapshot[0] != null) {
                    int hb = fHotbar;
                    if (hb >= 0) view.getBottomInventory().setItem(hb, bottomSnapshot[0]);
                }
            });
        } finally {
            inHandler = false;
        }
    }

    public void handleDrag(final InventoryDragEvent event) {
        int topSize = view.getTopInventory().getSize();
        boolean touchesTop = event.getRawSlots().stream().anyMatch(s -> s < topSize);
        boolean touchesBottom = event.getRawSlots().stream().anyMatch(s -> s >= topSize);
        if (touchesTop || (touchesTop && touchesBottom)) {
            event.setCancelled(true);
        }
    }

    public void pushBack(final Runnable runnable) {
        backStack.push(runnable);
    }

    public void goBack() {
        if (!backStack.isEmpty()) backStack.pop().run();
    }

    public CompletableFuture<Void> setPageAsync(final int[] contentSlots, final List<MenuItem> items) {
        final long tokenAtStart = this.viewToken;
        CompletableFuture<Void> future = new CompletableFuture<>();
        menus.scheduler().runAsync(() -> {
            try {
                menus.scheduler().runEntity(player, () -> {
                    if (this.viewToken != tokenAtStart || view == null) return;
                    int limit = Math.min(contentSlots.length, items.size());
                    for (int i = 0; i < limit; i++) {
                        int s = contentSlots[i];
                        MenuItem mi = items.get(i);
                        slots.put(s, mi);
                        ItemStack next = mi.render(player);
                        ItemStack cur = view.getTopInventory().getItem(s);
                        if (!stacksEqual(cur, next)) {
                            view.getTopInventory().setItem(s, next);
                        }
                    }
                });
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Start a lightweight refresh heartbeat that calls onRefresh(viewer) on all items.
     * Passing periodTicks <= 0 disables it. Safe across view changes via viewToken check.
     */
    public void startRefreshHeartbeat(final long periodTicks) {
        this.refreshPeriodTicks = periodTicks;
        if (periodTicks <= 0L) {
            cancelRefreshTask();
            return;
        }

        final long tokenAtStart = this.viewToken;
        this.refreshHeartbeatActive = true;

        cancelRefreshTask();
        this.refreshTask = menus.scheduler().runEntityAtFixedRate(player, task -> {
            if (!refreshHeartbeatActive) return;
            if (this.view == null) return;
            if (this.viewToken != tokenAtStart) return;

            // Avoid CME if items mutate during refresh
            Set<Map.Entry<Integer, MenuItem>> snapshot = new HashSet<>(slots.entrySet());
            for (Map.Entry<Integer, MenuItem> entry : snapshot) {
                MenuItem mi = entry.getValue();
                if (mi == null) continue;
                try {
                    mi.onRefresh(player);
                    int s = entry.getKey();
                    ItemStack updated = mi.render(player);
                    ItemStack cur = view.getTopInventory().getItem(s);
                    if (!stacksEqual(cur, updated)) {
                        view.getTopInventory().setItem(s, updated);
                    }
                } catch (Throwable ignored) {
                }
            }
        }, periodTicks, periodTicks);
    }

    public void shutdown() {
        cancelRefreshTask();
        refreshHeartbeatActive = false;
        slots.clear();
        backStack.clear();
        view = null;
        onOpenHook = () -> {};
        onCloseHook = willReopen -> {};
    }

    /**
     * Install hooks used by {@link io.voluble.michellelib.menu.template.MenuDefinition}.
     */
    public void setLifecycleHooks(final Runnable onOpen, final java.util.function.Consumer<Boolean> onClose) {
        this.onOpenHook = (onOpen == null) ? () -> {} : onOpen;
        this.onCloseHook = (onClose == null) ? willReopen -> {} : onClose;
    }

    private void cancelRefreshTask() {
        this.refreshHeartbeatActive = false;
        if (this.refreshTask != null) {
            try {
                this.refreshTask.cancel();
            } catch (Throwable ignored) {
            }
            this.refreshTask = null;
        }
    }

    private boolean stacksEqual(final ItemStack a, final ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getType() != b.getType()) return false;
        if (a.getAmount() != b.getAmount()) return false;
        return a.isSimilar(b);
    }

    private void returnItemsFromSlots() {
        Inventory playerInv = player.getInventory();
        for (Map.Entry<Integer, MenuItem> entry : slots.entrySet()) {
            MenuItem mi = entry.getValue();
            if (mi == null) continue;
            if (!mi.returnPlacedItems()) continue;
            List<ItemStack> toReturn;
            try {
                toReturn = mi.itemsToReturnOnClose(player);
            } catch (Throwable t) {
                menus.plugin().getLogger().warning(() -> "[MichelleLib:Menu] Failed returning items for slot " + entry.getKey() + " (" + mi.getClass().getSimpleName() + "): " + t.getMessage());
                continue;
            }
            if (toReturn == null || toReturn.isEmpty()) continue;
            for (ItemStack stack : toReturn) {
                if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) continue;
                Map<Integer, ItemStack> overflow = playerInv.addItem(stack.clone());
                for (ItemStack leftover : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }
    }

    private int findFirstEligiblePlaceableSlot(final ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return -1;
        Inventory topInv = view.getTopInventory();
        Inventory bottomInv = view.getBottomInventory();
        return slots.entrySet().stream()
            .sorted(Comparator.comparingInt(Map.Entry::getKey))
            .filter(e -> {
                MenuItem mi = e.getValue();
                if (mi == null || !mi.isPlaceable()) return false;
                // This is only used for capability checks; actions will not be executed here.
                ClickContext ctx = new ClickContext(player, ClickType.SHIFT_LEFT, e.getKey(), topInv, bottomInv, null, true, true, new MenuActions(this));
                return mi.allowShiftInsert() && mi.canAccept(stack, ctx) && mi.maxAcceptAmount(stack, ctx) > 0;
            })
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(-1);
    }
}


