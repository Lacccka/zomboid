/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface SoundCard {
    public String getDriverVersion();

    public String getName();

    public String getCodec();
}

