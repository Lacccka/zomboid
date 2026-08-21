/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
@Inherited
@Deprecated
public @interface ComEventCallback {
    public int dispid() default -1;

    public String name() default "";
}

