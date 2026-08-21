/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32LogicalDisk {
    private static final String WIN32_LOGICAL_DISK = "Win32_LogicalDisk";

    private Win32LogicalDisk() {
    }

    public static WbemcliUtil.WmiResult<LogicalDiskProperty> queryLogicalDisk(String nameToMatch, boolean localOnly) {
        StringBuilder wmiClassName = new StringBuilder(WIN32_LOGICAL_DISK);
        boolean where = false;
        if (localOnly) {
            wmiClassName.append(" WHERE DriveType = 2 OR DriveType = 3 OR DriveType = 6");
            where = true;
        }
        if (nameToMatch != null) {
            wmiClassName.append(where ? " AND" : " WHERE").append(" Name=\"").append(nameToMatch).append('\"');
        }
        WbemcliUtil.WmiQuery<LogicalDiskProperty> logicalDiskQuery = new WbemcliUtil.WmiQuery<LogicalDiskProperty>(wmiClassName.toString(), LogicalDiskProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(logicalDiskQuery);
    }

    public static enum LogicalDiskProperty {
        ACCESS,
        DESCRIPTION,
        DRIVETYPE,
        FILESYSTEM,
        FREESPACE,
        NAME,
        PROVIDERNAME,
        SIZE,
        VOLUMENAME;

    }
}

