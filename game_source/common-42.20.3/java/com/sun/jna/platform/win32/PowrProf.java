/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface PowrProf
extends Library {
    public static final PowrProf INSTANCE = Native.load("PowrProf", PowrProf.class);

    public int CallNtPowerInformation(int var1, Pointer var2, int var3, Pointer var4, int var5);

    public static interface POWER_INFORMATION_LEVEL {
        public static final int LastSleepTime = 15;
        public static final int LastWakeTime = 14;
        public static final int ProcessorInformation = 11;
        public static final int SystemBatteryState = 5;
        public static final int SystemExecutionState = 16;
        public static final int SystemPowerCapabilities = 4;
        public static final int SystemPowerInformation = 12;
        public static final int SystemPowerPolicyAc = 0;
        public static final int SystemPowerPolicyCurrent = 8;
        public static final int SystemPowerPolicyDc = 1;
        public static final int SystemReserveHiberFile = 10;
    }
}

