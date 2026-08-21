/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface Firmware {
    public String getManufacturer();

    public String getName();

    public String getDescription();

    public String getVersion();

    public String getReleaseDate();
}

