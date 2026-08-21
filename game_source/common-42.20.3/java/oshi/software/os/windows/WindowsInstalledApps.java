/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import java.util.List;
import oshi.driver.windows.registry.InstalledAppsData;
import oshi.software.os.ApplicationInfo;

public final class WindowsInstalledApps {
    private WindowsInstalledApps() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        return InstalledAppsData.queryInstalledApps();
    }
}

