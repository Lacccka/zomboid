/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import java.util.List;
import oshi.annotation.concurrent.Immutable;

@Immutable
public interface UsbDevice
extends Comparable<UsbDevice> {
    public String getName();

    public String getVendor();

    public String getVendorId();

    public String getProductId();

    public String getSerialNumber();

    public String getUniqueDeviceId();

    public List<UsbDevice> getConnectedDevices();
}

