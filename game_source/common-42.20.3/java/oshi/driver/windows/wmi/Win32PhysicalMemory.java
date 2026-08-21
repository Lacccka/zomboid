/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32PhysicalMemory {
    private static final String WIN32_PHYSICAL_MEMORY = "Win32_PhysicalMemory";

    private Win32PhysicalMemory() {
    }

    public static WbemcliUtil.WmiResult<PhysicalMemoryProperty> queryphysicalMemory() {
        WbemcliUtil.WmiQuery<PhysicalMemoryProperty> physicalMemoryQuery = new WbemcliUtil.WmiQuery<PhysicalMemoryProperty>(WIN32_PHYSICAL_MEMORY, PhysicalMemoryProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(physicalMemoryQuery);
    }

    public static WbemcliUtil.WmiResult<PhysicalMemoryPropertyWin8> queryphysicalMemoryWin8() {
        WbemcliUtil.WmiQuery<PhysicalMemoryPropertyWin8> physicalMemoryQuery = new WbemcliUtil.WmiQuery<PhysicalMemoryPropertyWin8>(WIN32_PHYSICAL_MEMORY, PhysicalMemoryPropertyWin8.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(physicalMemoryQuery);
    }

    public static enum PhysicalMemoryProperty {
        BANKLABEL,
        CAPACITY,
        SPEED,
        MANUFACTURER,
        PARTNUMBER,
        SMBIOSMEMORYTYPE,
        SERIALNUMBER;

    }

    public static enum PhysicalMemoryPropertyWin8 {
        BANKLABEL,
        CAPACITY,
        SPEED,
        MANUFACTURER,
        MEMORYTYPE,
        PARTNUMBER,
        SERIALNUMBER;

    }
}

