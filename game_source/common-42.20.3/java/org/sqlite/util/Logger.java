/*
 * Decompiled with CFR 0.152.
 */
package org.sqlite.util;

import java.util.function.Supplier;

public interface Logger {
    public void trace(Supplier<String> var1);

    public void info(Supplier<String> var1);

    public void warn(Supplier<String> var1);

    public void error(Supplier<String> var1, Throwable var2);
}

