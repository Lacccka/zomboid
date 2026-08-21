/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import java.util.List;
import oshi.ffm.driver.windows.registry.InstalledAppsDataFFM;
import oshi.software.os.ApplicationInfo;

public final class WindowsInstalledAppsFFM {
    private WindowsInstalledAppsFFM() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        return InstalledAppsDataFFM.queryInstalledApps();
    }
}

