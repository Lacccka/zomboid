/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface FFMReturn {
    public int value();

    public boolean includesNT() default false;

    @Target(value={ElementType.METHOD})
    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface SizeOut {
        public int value();
    }

    @Target(value={ElementType.PARAMETER})
    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface Size {
    }
}

