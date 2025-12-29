package io.voluble.michellelib.menu.template;

import io.voluble.michellelib.menu.CursorClosePolicy;
import io.voluble.michellelib.menu.item.MenuItem;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for {@link MenuDefinition}.
 * <p>
 * This is intentionally not "magic": it just composes onOpen/onClose hooks and slot placements.
 */
public final class MenuBuilder {
    private final MenuType type;

    private Function<MenuOpenTarget, Component> titleProvider = t -> Component.empty();
    private long refreshPeriodTicks = 0L;
    private CursorClosePolicy cursorPolicy = CursorClosePolicy.RETURN;
    private @Nullable String globalCacheKey;

    private final List<Consumer<MenuContext>> onOpenHooks = new ArrayList<>();
    private final List<Consumer<MenuCloseContext>> onCloseHooks = new ArrayList<>();

    // slot -> factory(ctx)
    private final Map<Integer, Function<MenuContext, MenuItem>> items = new HashMap<>();

    /**
     * Background/filler item(s) that are only applied to slots that are not already set by explicit items or pattern binds.
     */
    private Function<MenuContext, MenuItem> backgroundFill;

    // pattern support (chest only)
    private String[] pattern;
    private final Map<Character, Function<MenuContext, MenuItem>> patternBindings = new HashMap<>();

    MenuBuilder(final @NotNull MenuType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public @NotNull MenuBuilder title(final @NotNull Component title) {
        Objects.requireNonNull(title, "title");
        this.titleProvider = t -> title;
        return this;
    }

    public @NotNull MenuBuilder title(final @NotNull Function<MenuOpenTarget, Component> titleProvider) {
        this.titleProvider = Objects.requireNonNull(titleProvider, "titleProvider");
        return this;
    }

    public @NotNull MenuBuilder refreshEveryTicks(final long periodTicks) {
        this.refreshPeriodTicks = periodTicks;
        return this;
    }

    public @NotNull MenuBuilder cursorPolicy(final @NotNull CursorClosePolicy policy) {
        this.cursorPolicy = Objects.requireNonNull(policy, "policy");
        return this;
    }

    public @NotNull MenuBuilder globalCacheKey(final @Nullable String key) {
        this.globalCacheKey = key;
        return this;
    }

    public @NotNull MenuBuilder onOpen(final @NotNull Consumer<MenuContext> hook) {
        onOpenHooks.add(Objects.requireNonNull(hook, "hook"));
        return this;
    }

    public @NotNull MenuBuilder onClose(final @NotNull Consumer<MenuCloseContext> hook) {
        onCloseHooks.add(Objects.requireNonNull(hook, "hook"));
        return this;
    }

    public @NotNull MenuBuilder item(final int slot, final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too, so lambdas can become ambiguous with our factory overload.
        return item(slot, (Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder item(final int slot, final @NotNull Function<MenuContext, MenuItem> itemFactory) {
        Objects.requireNonNull(itemFactory, "itemFactory");
        items.put(slot, itemFactory);
        return this;
    }

    // -------------------------------------------------------------------------
    // Layout helpers (high-level, DRY, and safe)
    // -------------------------------------------------------------------------

    /**
     * Fill every slot in the menu (top inventory) with an item.
     * <p>
     * Only valid for chest menus (size = rows * 9).
     */
    public @NotNull MenuBuilder fill(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too; without a cast the lambda may be inferred as MenuItem#render.
        return fill((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder fill(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int size = chestSizeOrThrow();
        for (int slot = 0; slot < size; slot++) {
            item(slot, factory);
        }
        return this;
    }

    /**
     * Fill the outer border (frame) of a chest menu.
     */
    public @NotNull MenuBuilder border(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too; without a cast the lambda may be inferred as MenuItem#render.
        return border((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder border(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();

        // top row
        for (int c = 0; c < 9; c++) item(c, factory);
        // bottom row
        int baseBottom = (rows - 1) * 9;
        for (int c = 0; c < 9; c++) item(baseBottom + c, factory);
        // left/right columns (excluding corners already set)
        for (int r = 1; r < rows - 1; r++) {
            item(r * 9, factory);
            item(r * 9 + 8, factory);
        }
        return this;
    }

    /**
     * Fill a specific row (0-based) of a chest menu.
     */
    public @NotNull MenuBuilder row(final int rowIndex, final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too; without a cast the lambda may be inferred as MenuItem#render.
        return row(rowIndex, (Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder row(final int rowIndex, final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();
        if (rowIndex < 0 || rowIndex >= rows) throw new IllegalArgumentException("rowIndex out of range");
        int base = rowIndex * 9;
        for (int c = 0; c < 9; c++) item(base + c, factory);
        return this;
    }

    /**
     * Fill a specific column (0-based) of a chest menu.
     */
    public @NotNull MenuBuilder column(final int columnIndex, final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too; without a cast the lambda may be inferred as MenuItem#render.
        return column(columnIndex, (Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder column(final int columnIndex, final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();
        if (columnIndex < 0 || columnIndex >= 9) throw new IllegalArgumentException("columnIndex out of range");
        for (int r = 0; r < rows; r++) item(r * 9 + columnIndex, factory);
        return this;
    }

    /**
     * Fill all slots except border.
     */
    public @NotNull MenuBuilder inner(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too; without a cast the lambda may be inferred as MenuItem#render.
        return inner((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder inner(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < 8; c++) {
                item(r * 9 + c, factory);
            }
        }
        return this;
    }

    /**
     * Set items on specific slots.
     */
    public @NotNull MenuBuilder slots(final @NotNull MenuItem item, final int... slots) {
        Objects.requireNonNull(item, "item");
        return slots((Function<MenuContext, MenuItem>) ctx -> item, slots);
    }

    public @NotNull MenuBuilder slots(final @NotNull Function<MenuContext, MenuItem> factory, final int... slots) {
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(slots, "slots");
        for (int slot : slots) {
            item(slot, factory);
        }
        return this;
    }

    /**
     * Set the four corners of a chest menu.
     */
    public @NotNull MenuBuilder corners(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        return corners((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder corners(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();
        int lastRowBase = (rows - 1) * 9;
        return slots(factory, 0, 8, lastRowBase, lastRowBase + 8);
    }

    /**
     * Only fill the top and bottom rows (no sides).
     */
    public @NotNull MenuBuilder frameTopBottom(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        return frameTopBottom((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder frameTopBottom(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();
        // top row
        for (int c = 0; c < 9; c++) item(c, factory);
        // bottom row
        int baseBottom = (rows - 1) * 9;
        for (int c = 0; c < 9; c++) item(baseBottom + c, factory);
        return this;
    }

    /**
     * Semantic alias for placing an item at a given slot.
     */
    public @NotNull MenuBuilder center(final int slot, final @NotNull MenuItem item) {
        return item(slot, item);
    }

    public @NotNull MenuBuilder center(final int slot, final @NotNull Function<MenuContext, MenuItem> factory) {
        return item(slot, factory);
    }

    /**
     * Place an item at the computed "center" of a chest menu.
     * <p>
     * For chest rows:
     * - column = 4
     * - row = rows / 2 (floor)
     */
    public @NotNull MenuBuilder center(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        return center((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder center(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        int rows = chestRowsOrThrow();
        int centerRow = rows / 2;
        int slot = centerRow * 9 + 4;
        return item(slot, factory);
    }

    /**
     * Background/filler that never overwrites explicit items or pattern placements.
     * <p>
     * Applied after explicit items and pattern binds during open.
     */
    public @NotNull MenuBuilder background(final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        return background((Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder background(final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        // background currently only supports chest menus (top size known)
        chestRowsOrThrow();
        this.backgroundFill = factory;
        return this;
    }

    /**
     * Define a chest pattern (each string is a row; width is up to 9).
     * Only valid for {@link MenuType.ChestRows}.
     */
    public @NotNull MenuBuilder pattern(final @NotNull String... rows) {
        Objects.requireNonNull(rows, "rows");
        if (!(type instanceof MenuType.ChestRows chest)) {
            throw new IllegalStateException("pattern() is only supported for ChestRows menus");
        }
        if (rows.length != chest.rows()) {
            throw new IllegalArgumentException("pattern row count must equal chest rows (" + chest.rows() + ")");
        }
        this.pattern = rows;
        return this;
    }

    public @NotNull MenuBuilder bind(final char key, final @NotNull MenuItem item) {
        Objects.requireNonNull(item, "item");
        // Disambiguate: MenuItem is a SAM type too, so lambdas can become ambiguous with our factory overload.
        return bind(key, (Function<MenuContext, MenuItem>) ctx -> item);
    }

    public @NotNull MenuBuilder bind(final char key, final @NotNull Function<MenuContext, MenuItem> factory) {
        Objects.requireNonNull(factory, "factory");
        this.patternBindings.put(key, factory);
        return this;
    }

    public @NotNull MenuDefinition build() {
        final Consumer<MenuContext> onOpen = ctx -> {
            // Track which slots we set so background never overwrites.
            final java.util.Set<Integer> occupied = new java.util.HashSet<>();

            // explicit hooks first
            for (Consumer<MenuContext> hook : onOpenHooks) hook.accept(ctx);

            // place explicit items
            for (Map.Entry<Integer, Function<MenuContext, MenuItem>> e : items.entrySet()) {
                MenuItem item = e.getValue().apply(ctx);
                if (item != null) {
                    ctx.setItem(e.getKey(), item);
                    occupied.add(e.getKey());
                }
            }

            // apply pattern after explicit items (pattern can be used for filler/background)
            if (pattern != null && !patternBindings.isEmpty()) {
                for (int r = 0; r < pattern.length; r++) {
                    String row = pattern[r];
                    int width = Math.min(9, row.length());
                    for (int c = 0; c < width; c++) {
                        char ch = row.charAt(c);
                        Function<MenuContext, MenuItem> factory = patternBindings.get(ch);
                        if (factory == null) continue;
                        int slot = r * 9 + c;
                        MenuItem item = factory.apply(ctx);
                        if (item != null) {
                            ctx.setItem(slot, item);
                            occupied.add(slot);
                        }
                    }
                }
            }

            // apply background last, but never overwrite any already-set slot
            if (backgroundFill != null) {
                int size = chestSizeOrThrow();
                for (int slot = 0; slot < size; slot++) {
                    if (occupied.contains(slot)) continue;
                    MenuItem item = backgroundFill.apply(ctx);
                    if (item == null) continue;
                    ctx.setItem(slot, item);
                }
            }
        };

        final Consumer<MenuCloseContext> onClose = cctx -> {
            for (Consumer<MenuCloseContext> hook : onCloseHooks) hook.accept(cctx);
        };

        return new MenuDefinition(
            type,
            titleProvider,
            refreshPeriodTicks,
            cursorPolicy,
            globalCacheKey,
            onOpen,
            onClose
        );
    }

    private int chestRowsOrThrow() {
        if (!(type instanceof MenuType.ChestRows chest)) {
            throw new IllegalStateException("This layout helper is only supported for ChestRows menus");
        }
        return chest.rows();
    }

    private int chestSizeOrThrow() {
        return chestRowsOrThrow() * 9;
    }
}


