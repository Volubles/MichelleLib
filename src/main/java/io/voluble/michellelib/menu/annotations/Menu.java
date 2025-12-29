package io.voluble.michellelib.menu.annotations;

import org.bukkit.event.inventory.InventoryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional marker annotation (not currently used by the runtime).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Menu {
    InventoryType value() default InventoryType.CHEST;

    String title() default "";

    boolean checkReachable() default false;

    String cacheKey() default ""; // global cache bucket
}


