/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinNT;

public interface PhysicalMonitorEnumerationAPI {
    public static final int PHYSICAL_MONITOR_DESCRIPTION_SIZE = 128;

    @Structure.FieldOrder(value={"hPhysicalMonitor", "szPhysicalMonitorDescription"})
    public static class PHYSICAL_MONITOR
    extends Structure {
        public WinNT.HANDLE hPhysicalMonitor;
        public char[] szPhysicalMonitorDescription = new char[128];
    }
}

