package io.voluble.michellelib.menu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MenuProvider {
    /**
     * Intended to mark methods called once on open to lay out static items / frames.
     * (Not currently used by the runtime.)
     */
}


