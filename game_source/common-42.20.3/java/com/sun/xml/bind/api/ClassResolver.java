/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.api;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

public abstract class ClassResolver {
    @Nullable
    public abstract Class<?> resolveElementName(@NotNull String var1, @NotNull String var2) throws Exception;
}

