/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface GraphicsCard {
    public String getName();

    public String getDeviceId();

    public String getVendor();

    public String getVersionInfo();

    public long getVRam();
}

