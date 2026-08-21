/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util;

public interface NonThrowingAutoCloseable
extends AutoCloseable {
    @Override
    public void close();
}

