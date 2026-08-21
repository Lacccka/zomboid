/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind;

import com.sun.xml.bind.AccessorFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE, ElementType.PACKAGE})
public @interface XmlAccessorFactory {
    public Class<? extends AccessorFactory> value();
}

