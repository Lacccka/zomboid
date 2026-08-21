/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import javax.xml.namespace.NamespaceContext;

public interface NamespaceContext2
extends NamespaceContext {
    public String declareNamespace(String var1, String var2, boolean var3);

    public int force(@NotNull String var1, @NotNull String var2);
}

