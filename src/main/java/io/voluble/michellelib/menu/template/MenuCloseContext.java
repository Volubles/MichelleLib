package io.voluble.michellelib.menu.template;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class MenuCloseContext {
    private final MenuContext ctx;
    private final boolean willReopen;

    MenuCloseContext(final @NotNull MenuContext ctx, final boolean willReopen) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        this.willReopen = willReopen;
    }

    public @NotNull MenuContext context() {
        return ctx;
    }

    public boolean willReopen() {
        return willReopen;
    }
}


