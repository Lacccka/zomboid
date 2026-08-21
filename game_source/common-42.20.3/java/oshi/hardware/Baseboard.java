/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface Baseboard {
    public String getManufacturer();

    public String getModel();

    public String getVersion();

    public String getSerialNumber();
}

