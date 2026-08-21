/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.GlobalMemory;
import oshi.hardware.platform.mac.MacGlobalMemoryFFM;
import oshi.hardware.platform.mac.MacHardwareAbstractionLayer;

@ThreadSafe
public final class MacHardwareAbstractionLayerFFM
extends MacHardwareAbstractionLayer {
    @Override
    public GlobalMemory createMemory() {
        return new MacGlobalMemoryFFM();
    }
}

