/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface Display {
    public byte[] getEdid();
}

